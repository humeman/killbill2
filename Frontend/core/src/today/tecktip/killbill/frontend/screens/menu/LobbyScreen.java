package today.tecktip.killbill.frontend.screens.menu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.maploader.KillBillMap;
import today.tecktip.killbill.common.maploader.MapLoader;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.gameserver.game.LocalGameUserState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameUserState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState.BasicGameRunState;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangeGameStateCommand.GameStateFieldFilter;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicSendChatCommand.BasicRecvChatCommandData;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicSendChatCommand.BasicRecvSystemMessageCommandData;
import today.tecktip.killbill.frontend.http.requests.data.Game;
import today.tecktip.killbill.frontend.screens.MenuScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.ScaledSize;
import today.tecktip.killbill.frontend.ui.Size.XScaledSize;
import today.tecktip.killbill.frontend.ui.Size.YScaledSize;
import today.tecktip.killbill.frontend.ui.elements.Button;
import today.tecktip.killbill.frontend.ui.elements.Image;
import today.tecktip.killbill.frontend.ui.elements.Label;
import today.tecktip.killbill.frontend.ui.renderers.ChatDisplay;
import today.tecktip.killbill.frontend.ui.renderers.ChatDisplay.PlayerChatMessage;
import today.tecktip.killbill.frontend.ui.renderers.ChatDisplay.SystemChatMessage;

/**
 * The lobby screen where players see their comrades or enemies before battle.
 */
public class LobbyScreen extends MenuScreen {

    private Label players;
    private Label info;
    private Label name;
    private Button start;

    private float refreshTimer;

    private KillBillMap map;

    /**
     * The game we're playing in right now
     */
    private Game game;

    /**
     * Chat display
     */
    private ChatDisplay chatDisplay;

    private boolean leaving;

    /**
     * Constructs the lobby screen.
     */
    public LobbyScreen() { 
        super(new Color(0, 0, 0, 1));
        refreshTimer = 0;
        map = null;
        leaving = false;
    }

    /**
     * Assigns a game to the lobby. Must be defined before use.
     * @param game DB game to attach this to
     */
    public void assignGame(final Game game) {
        this.game = game;
    }

    @Override
    public void onCreate() {
        chatDisplay = null;
        
        uiRenderer.add(
            Image.newBuilder()
                .setTexture("ui_bg_*")
                .setSize(new YScaledSize(1, 1))
                .setLocation(new FixedLocation(0, 0))
                .setTint(new Color(1, 1, 1, 0.4f))
                .build());

        name = Label.newBuilder()
            .setLocation(new ScaledLocation(.05f, .925f))
            .setCentered(false)
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR)
                    .setScaledSize(40)
            )
            .setText("")
            .build();

        uiRenderer.add(name);

        uiRenderer.add(Button.newBuilder()
            .setLocation(new ScaledLocation(.8f, 0.825f))
            .setSize(new XScaledSize(.15f,.5f))
            .setTexture("ui_mini_button")
            .setText("leave")
            .setOnPress(this::leave)
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR)
                    .setScaledSize(36)
            )
            .build());

        start = Button.newBuilder()
            .setLocation(new ScaledLocation(.8f, 0.625f))
            .setSize(new XScaledSize(.15f,.5f))
            .setTexture("ui_mini_button")
            .setText("start")
            .setOnPress(this::start)
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR)
                    .setScaledSize(36)
            )
            .build();

        uiRenderer.add(start);

        //CHAT ON THE BOTTOM LEFT ADDED HERE
        info = Label.newBuilder()
            .setLocation(new ScaledLocation(.05f, .85f))
            .setCentered(false)
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.TERTIARY_COLOR)
                    .setScaledSize(24)
            )
            .setText("")
            .build();

        uiRenderer.add(info);

        players = Label.newBuilder()
            .setLocation(new ScaledLocation(.70f,.1f))
            .setCentered(true)
            .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                            .setColor(GlobalGameConfig.TERTIARY_COLOR)
                            .setScaledSize(30)
            )
            .setText("")
            .build();

        uiRenderer.add(players);

    }

    @Override
    public void onSwitch() {
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_GAME_STATE, this::updateState);
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.RESP_GET_GAME_STATE, this::updateState);
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_CHAT, this::recvPlayerChat);
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE, this::recvSystemChat);

        if (chatDisplay != null) uiRenderer.remove(chatDisplay);
        chatDisplay = new ChatDisplay(
            false, 
            new ScaledLocation(.05f, .05f),
            new ScaledSize(0.5f, 0.075f),
            32
        );
        getUiRenderer().add(chatDisplay);

        leaving = false;
        map = null;

        try {
            KillBillGame.get().getUdpClient().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_GET_GAME_STATE)
                .run(KillBillGame.get().getUdpClient(), null);
        } catch (final MessageFailure e) {
            throw new CatastrophicException("Failure in UDP state recv: ", e);
        }

        updateState(null);

        super.onSwitch();
    }

    @Override
    public void drawFirst(final float delta) {
        super.drawFirst(delta);

        if (!KillBillGame.get().getUdpClient().isConnected() && !leaving) {
            Screens.UDP_ERROR_SCREEN.init("connection lost :(");
            KillBillGame.get().changeScreen(Screens.UDP_ERROR_SCREEN);
            return;
        }

        // The client periodically does things, like grabbing players from
        // the API. We don't want to miss that, so this is updated every
        // once in a while
        refreshTimer += delta;

        if (refreshTimer > 1f) {
            updateState(null);
            refreshTimer = 0;
        }
    }

    public void start() {
        BasicLocalGameState gameState = (BasicLocalGameState) KillBillGame.get().getUdpClient().getGameState();
        gameState.setState(BasicGameRunState.PLAYING);
        gameState.setFieldChanged(GameStateFieldFilter.RUN_STATE);

        try {
            gameState.sync();
        } catch (final MessageFailure e) {
            Gdx.app.error(LobbyScreen.class.getSimpleName(), "Failed to sync game state.", e);
        }
    }

    public void updateState(final IncomingMessage message) {
        BasicLocalGameState gameState = (BasicLocalGameState) KillBillGame.get().getUdpClient().getGameState();

        if (gameState.getState().equals(BasicGameRunState.LOBBY)) {
            if (map == null) {
                map = MapLoader.load(List.of(new ByteArrayInputStream(gameState.getGame().map().getBytes())));
            }

            // Just update info
            name.setText(game.name());
            info.setText("Map: " + map.getDisplayName());

            players.setText(
                gameState.getConnectedUsers().size() + " in lobby:\n" + 
                String.join("\n", gameState.getConnectedUsers().entrySet().stream().map(user -> { 
                    try {
                        return user.getValue().getUser().name(); 
                    } catch (final CatastrophicException e) {
                        // This is absolute garbage! :)
                        return "...";
                    }
                }).toList())
            );

            start.setVisible(game.hostId().equals(KillBillGame.get().getUser().id()) && gameState.getConnectedUsers().size() >= 2);
        }

        else if (gameState.getState().equals(BasicGameRunState.PLAYING)) {
            KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_GAME_STATE);
            KillBillGame.get().getUdpClient().clearCallback(MessageDataType.RESP_GET_GAME_STATE);
            KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_CHAT);
            KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE);
            Screens.UDP_GAME_SCREEN.init(
                (BasicLocalGameUserState) gameState.getUser(KillBillGame.get().getUser().id()),
                gameState
            );
            KillBillGame.get().changeScreen(Screens.UDP_GAME_SCREEN);
        }
    }
    
    public void recvPlayerChat(final IncomingMessage message) {
        BasicLocalGameState gameState = (BasicLocalGameState) KillBillGame.get().getUdpClient().getGameState();
        BasicRecvChatCommandData data = (BasicRecvChatCommandData) message.data();
        // Determine sender
        final LocalGameUserState user = gameState.getUser(data.getUserId());
        
        chatDisplay.addMessage(
            new PlayerChatMessage(user.getUser(), data.getMessage(), message.createdAt())
        );
    }
    
    public void recvSystemChat(final IncomingMessage message) {
        BasicRecvSystemMessageCommandData data = (BasicRecvSystemMessageCommandData) message.data();
        
        chatDisplay.addMessage(
            new SystemChatMessage(data.getMessage(), message.createdAt())
        );
    }

    public void leave() {
        leaving = true;
        try {
            KillBillGame.get().getUdpClient().disconnect();
        } catch (final IOException | InterruptedException e) {
            Gdx.app.error(getClass().getSimpleName(), "Failed to disconnect UDP.", e);
        }

        KillBillGame.get().changeScreen(Screens.USER_SCREEN);
    }
}
