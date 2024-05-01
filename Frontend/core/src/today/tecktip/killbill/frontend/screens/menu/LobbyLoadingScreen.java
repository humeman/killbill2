package today.tecktip.killbill.frontend.screens.menu;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;
import today.tecktip.killbill.frontend.http.requests.GameRequests;
import today.tecktip.killbill.frontend.http.requests.GameRequests.ConnectGameRequestBody;
import today.tecktip.killbill.frontend.http.requests.GameRequests.ConnectGameResponseBody;
import today.tecktip.killbill.frontend.http.requests.data.Game;
import today.tecktip.killbill.frontend.http.requests.data.User;
import today.tecktip.killbill.frontend.screens.MenuScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.YScaledSize;
import today.tecktip.killbill.frontend.ui.elements.Image;
import today.tecktip.killbill.frontend.ui.elements.Label;

/**
 * The lobby screen, displayed before the game starts.
 * @author cs
 */
public class LobbyLoadingScreen extends MenuScreen {
    /**
     * The game we're playing in right now
     */
    private Game game;

    /**
     * The user we're authenticated as
     */
    private User user;

    private Label loadingLabel;

    private float connectionTime;

    /**
     * Constructs the test screen.
     */
    public LobbyLoadingScreen() {
        super(new Color(0, 0, 0, 1));
        game = null;
        user = null;
        connectionTime = 0;
    }

    /**
     * Assigns a game to the lobby. Must be defined before use.
     * @param game DB game to attach this to
     * @param user DB user playing right now
     */
    public void assignGame(final Game game, final User user) {
        this.game = game;
        this.user = user;
    }

    @Override
    public void onSwitch() {
        if (game == null || user == null) throw new CatastrophicException("Switched to lobby before a game and user were set!");
        super.onSwitch();

        connectionTime = 0;

        // Load into the game
        GameRequests.connect(
            new ConnectGameRequestBody(game.id()),
            body -> {
                try {
                    initClient(body);
                } catch (final Exception e) {
                    Screens.UDP_ERROR_SCREEN.init("connection failed :(");
                    KillBillGame.get().changeScreen(Screens.UDP_ERROR_SCREEN);
                    return;
                }
            },
            e -> {

                Screens.UDP_ERROR_SCREEN.init("connection failed :(");
                KillBillGame.get().changeScreen(Screens.UDP_ERROR_SCREEN);
                return;
            }
        );
    }

    @Override
    public void onCreate() {
        uiRenderer.add(
            Image.newBuilder()
                .setTexture("ui_bg_*")
                .setSize(new YScaledSize(1, 1))
                .setLocation(new FixedLocation(0, 0))
                .setTint(new Color(1, 1, 1, 0.4f))
                .build());

        // Create a placeholder "loading" label while we connect
        loadingLabel = Label.newBuilder()
            .setCentered(true)
            .setLocation(new ScaledLocation(0.5f, 0.5f))
            .setText("connecting to server...")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(1, 1, 1, 1))
                    .setScaledSize(48)
            )
            .build();
        uiRenderer.add(loadingLabel);
    }

    @Override
    public void drawFirst(final float delta) {
        connectionTime += delta;

        if (connectionTime > GlobalGameConfig.UDP_CONNECT_TIMEOUT) {
            try {
                KillBillGame.get().getUdpClient().disconnect();
            } catch (final Exception e) {}

            Screens.UDP_ERROR_SCREEN.init("connection timed out :(");
            KillBillGame.get().changeScreen(Screens.UDP_ERROR_SCREEN);
            return;
        } else {
            loadingLabel.setText(String.format("connecting... (%.1fs)", GlobalGameConfig.UDP_CONNECT_TIMEOUT - connectionTime));
        }

        super.drawFirst(delta);
    }
    
    /**
     * Initializes the UDP client based on the game key supplied.
     * @param body UDP connection body
     * @throws IOException
     */
    private void initClient(final ConnectGameResponseBody body) throws IOException {
        Gdx.app.log(LobbyLoadingScreen.class.getSimpleName(), "Connecting to UDP: " + body.host() + ":" + body.port());

        ClientMessageHandler udpClient = new ClientMessageHandler(
            body.host(), 
            body.port(), 
            body.gameKey(),
            KillBillGame.get().getCommandLoader(), 
            (e, msg) -> {
                Gdx.app.error("", "Send error: " + msg, e);
            }, 
            e -> {
                Gdx.app.error("", "Recv error", e);
            }
        );

        KillBillGame.get().setUdpClient(udpClient);

        udpClient.setCallback(MessageDataType.RESP_CONNECT, this::onConnect);

        try {
            udpClient.connect(game);
        } catch (final MessageFailure e) {
            Screens.UDP_ERROR_SCREEN.init("connection failed :(");
            KillBillGame.get().changeScreen(Screens.UDP_ERROR_SCREEN);
            return;
        }

        KillBillGame.get().setUdpClient(udpClient);
    }

    public void onConnect(final IncomingMessage message) {
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.RESP_CONNECT);
        Screens.LOBBY_SCREEN.assignGame(game);
        KillBillGame.get().changeScreen(Screens.LOBBY_SCREEN);
    }
}
