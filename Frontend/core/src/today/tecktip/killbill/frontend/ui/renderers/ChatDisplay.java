package today.tecktip.killbill.frontend.ui.renderers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.Keybinds.KeyType;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicSendChatCommand.SendChatInvokeContext;
import today.tecktip.killbill.frontend.http.requests.data.User;
import today.tecktip.killbill.frontend.resources.FontLoader;
import today.tecktip.killbill.frontend.ui.Location;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size;
import today.tecktip.killbill.frontend.ui.elements.Label;
import today.tecktip.killbill.frontend.ui.elements.TextInput;

/**
 * A UiRenderer extension which supports UDP chat.
 * @author cs
 */
public class ChatDisplay extends RendererExtension {
    private static final int MAX_MESSAGES = 10;

    private TextInput textBox;

    private List<LabelAndMessage> labels;

    private List<ChatMessage> toAdd;

    private boolean dynamicBox;
    
    private AtomicBoolean lock;

    /**
     * Creates a new chat display.
     */
    public ChatDisplay(final boolean dynamicBox, final Location location, final Size size, final int boxFontSize) {
        super(true);
        toAdd = new ArrayList<>();
        labels = new ArrayList<>(MAX_MESSAGES);
        this.dynamicBox = dynamicBox;

        textBox = TextInput.newBuilder()
            .setLocation(location)
            .setSize(size)
            .setTexture("ui_test_input")
            .setPlaceholder("enter a message...")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(0, 0, 0, 1))
                    .setScaledSize(boxFontSize)
            )
            .build();

        elements.add(textBox);

        if (dynamicBox) textBox.setVisible(false);

        lock = new AtomicBoolean(false);
    }

    @Override
    public void render(final SpriteBatch batch, float delta) {
        updateMessages(delta);
        super.render(batch, delta);
    }

    public void addMessage(final ChatMessage message) {
        while (lock.compareAndSet(false, true));
        try {
            toAdd.add(message);
        } finally {
            lock.set(false);
        }
    }

    public void updateMessages(final float delta) {
        while (!lock.compareAndSet(false, true));

        try {
            if (toAdd.size() > 0) {
                for (final ChatMessage message : toAdd) {
                    if (labels.size() >= MAX_MESSAGES) {
                        labels.remove(0); // Oldest message
                    }
            
                    // If there are any messages now, move them up
                    final float yOffset = 1 / 20f;
                    for (final LabelAndMessage lam : labels) {
                        ((ScaledLocation) lam.label.getLocation()).offsetYScale(yOffset);
                        lam.label.updateFonts(false);
                    }
                    
                    // Register
                    Label newLabel = Label.newBuilder()
                        .setFontBuilder(message.getFontBuilder())
                        .setLocation(new ScaledLocation(0.05f, yOffset * 5))
                        .setText(message.getMessage())
                        .setCentered(false)
                        .build();
                    labels.add(
                        new LabelAndMessage(
                            newLabel,
                            message,
                            0
                        )
                    );
                    elements.add(newLabel);
                }
                toAdd.clear();
            }

            // All we're doing here is fading out and/or removing old messages
            List<LabelAndMessage> toRemove = null;
            for (final LabelAndMessage lam : labels) {
                lam.offsetRenderTime(delta);

                if (lam.getRenderTime() > 9f && lam.getRenderTime() < 10f) {
                    // Fade out now
                    float a = 10f - lam.getRenderTime();
                    if (lam.getLabel().getTint() == null) {
                        lam.getLabel().setTint(lam.getChatMessage().getColor());
                    }
                    lam.getLabel().getTint().a = a;
                }
                else if (lam.getRenderTime() >= 10f) {
                    // Remove it
                    if (toRemove == null) toRemove = new ArrayList<>();
                    toRemove.add(lam);
                }
            }

            if (toRemove != null) {
                for (final LabelAndMessage lam : toRemove) {
                    elements.remove(lam.getLabel());
                    labels.remove(lam);
                }
            }
        } finally {
            lock.set(false);
        }
    }

    @Override
    public boolean runKey(final int key) {
        if (KillBillGame.get().getKeybinds().isMemberOf(KeyType.CHAT, key)) {
            boolean send = true;
            if (dynamicBox) {
                textBox.setVisible(!textBox.isVisible());
                if (textBox.isVisible()) {
                    textBox.select();
                    send = false;
                } 
            }
            if (send) {
                // Message is ready
                try {
                    KillBillGame.get().getUdpClient().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_SEND_CHAT)
                        .run(
                            KillBillGame.get().getUdpClient(), 
                            new SendChatInvokeContext(textBox.getText())
                    );
                } catch (final MessageFailure e) {
                    addMessage(new ClientChatMessage("Error: couldn't send message"));
                }
                addMessage(new PlayerChatMessage(KillBillGame.get().getUser(), textBox.getText(), Instant.now()));
                textBox.setText("");
            }
            return true;
        } else if (dynamicBox && textBox.isVisible() && KillBillGame.get().getKeybinds().isMemberOf(KeyType.CLOSE, key)) {
            textBox.setText("");
            textBox.unselect();
            textBox.setVisible(false);
            return true;
        } else {
            return super.runKey(key);
        }
    }

    public interface ChatMessage {
        public String getMessage();
        public Instant getSendTime();
        public FontLoader.Builder getFontBuilder();
        public Color getColor();
    }

    public static class PlayerChatMessage implements ChatMessage {
        private static FontLoader.Builder fontBuilder = null;

        private User user;
        private String content;
        private Instant sendTime;

        public PlayerChatMessage(final User user, final String content, final Instant sendTime) {
            if (fontBuilder == null) {
                fontBuilder = KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(1f, 1f, 1f, 0.8f))
                    .setBorder(Color.BLACK, 1)
                    .setScaledSize(28);
            }

            this.user = user;
            this.content = content;
            this.sendTime = sendTime;
        }

        @Override
        public String getMessage() {
            return String.format("[%s] %s", user.name(), content);
        }

        @Override
        public Instant getSendTime() {
            return sendTime;
        }

        @Override
        public FontLoader.Builder getFontBuilder() {
            return fontBuilder;
        }

        @Override
        public Color getColor() {
            return new Color(1f, 1f, 1f, 0.8f);
        }
    }

    public static class SystemChatMessage implements ChatMessage {
        private static FontLoader.Builder fontBuilder = null;
        private String content;
        private Instant sendTime;

        public SystemChatMessage(final String content, final Instant sendTime) {
            if (fontBuilder == null) {
                fontBuilder = KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(0.7f, 0.7f, 1f, 0.8f))
                    .setBorder(Color.BLACK, 1)
                    .setScaledSize(28);
            }
            this.content = content;
            this.sendTime = sendTime;
        }

        @Override
        public String getMessage() {
            return content;
        }

        @Override
        public Instant getSendTime() {
            return sendTime;
        }

        @Override
        public FontLoader.Builder getFontBuilder() {
            return fontBuilder;
        }

        @Override
        public Color getColor() {
            return new Color(0.7f, 0.7f, 1f, 0.8f);
        }
    }

    public static class ClientChatMessage implements ChatMessage {
        private static FontLoader.Builder fontBuilder = null;
        private String content;
        private Instant sendTime;

        public ClientChatMessage(final String content) {
            if (fontBuilder == null) {
                fontBuilder = KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(1f, 0.7f, 0.7f, 0.8f))
                    .setBorder(Color.BLACK, 1)
                    .setScaledSize(28);
            }
            this.content = content;
            this.sendTime = Instant.now();
        }

        @Override
        public String getMessage() {
            return content;
        }

        @Override
        public Instant getSendTime() {
            return sendTime;
        }

        @Override
        public FontLoader.Builder getFontBuilder() {
            return fontBuilder;
        }

        @Override
        public Color getColor() {
            return new Color(1f, 0.7f, 0.7f, 0.8f);
        }
    }

    private static class LabelAndMessage {

        private Label label;
        private ChatMessage chatMessage;
        private float renderTime;

        public LabelAndMessage(final Label label, final ChatMessage chatMessage, final float renderTime) {
            this.label = label;
            this.chatMessage = chatMessage;
            this.renderTime = renderTime;
        }

        public Label getLabel() {
            return label;
        }

        public ChatMessage getChatMessage() {
            return chatMessage;
        }

        public void offsetRenderTime(final float delta) {
            renderTime += delta;
        }

        public float getRenderTime() {
            return renderTime;
        }
        
    }
}
