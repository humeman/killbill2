package today.tecktip.killbill.frontend.screens.menu;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;
import today.tecktip.killbill.frontend.http.requests.GameRequests;
import today.tecktip.killbill.frontend.http.requests.UserRequests;
import today.tecktip.killbill.frontend.http.requests.GameRequests.ConnectGameRequestBody;
import today.tecktip.killbill.frontend.http.requests.GameRequests.ConnectGameResponseBody;
import today.tecktip.killbill.frontend.http.requests.GameRequests.ListGamesRequestBody;
import today.tecktip.killbill.frontend.http.requests.data.Game;
import today.tecktip.killbill.frontend.screens.MenuScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.ScaledSize;
import today.tecktip.killbill.frontend.ui.elements.Button;
import today.tecktip.killbill.frontend.ui.elements.Label;

/**
 * A demo testing screen for playing around with.
 * @author cs
 */
public class TestScreen extends MenuScreen {

    /**
     * Status label
     */
    private Label label;

    /**
     * Constructs the test screen.
     */
    public TestScreen() {
        super(new Color(0, 0, 0, 1));
    }

    @Override
    public void onCreate() {
        label = Label.newBuilder()
            .setLocation(new ScaledLocation(0.5f, 0.8f))
            .setCentered(true)
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(1, 1, 1, 1))
                    .setBorder(new Color(0, 0, 0, 1), 2)
                    .setScaledSize(48)
            )
            .setText("press the button.")
            .build();

        uiRenderer.add(label);

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.3f, 0.4f))
                .setSize(new ScaledSize(0.4f, 0.2f))
                .setTexture("ui_test_button")
                .setText("connect")
                .setOnPress(() -> {
                    UserRequests.getAuthenticatedUser(
                        userBody -> {
                            GameRequests.listGames(
                                new ListGamesRequestBody(userBody.user().id()),
                                body -> {
                                    if (body.games().size() != 0) {
                                        GameRequests.connect(
                                            new ConnectGameRequestBody(body.games().get(0).game().id()), 
                                            body1 -> {
                                                try {
                                                    initClient(body1, body.games().get(0).game());
                                                } catch (final IOException e1) {
                                                    label.setText(e1.getMessage());
                                                    Gdx.app.error("", "Init error: ", e1);
                                                }
                                            }, 
                                            e1 -> {
                                                label.setText(e1.getMessage());
                                                Gdx.app.error("", "HTTP connect error: ", e1);
                                            }
                                        );
                                    }
                                }, 
                                e -> {
                                    label.setText(e.getMessage());
                                    Gdx.app.error("", "List error: ", e);
                                }
                            );
                        }, 
                        e2 -> {
                            label.setText(e2.getMessage());
                            Gdx.app.error("", "User recv error: ", e2);

                        });
                })
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                        .setColor(new Color(1, 1, 1, 1))
                        .setBorder(new Color(0, 0, 0, 1), 2)
                        .setScaledSize(48)
                )
                .build()
        );
    }
    
    /**
     * Initializes the UDP client based on the game key supplied.
     * @param body 
     * @throws IOException
     */
    private void initClient(final ConnectGameResponseBody body, final Game game) throws IOException {
        Gdx.app.log("", body.host() + ":" + body.port() + " - " + body.gameKey());

        ClientMessageHandler udpClient = new ClientMessageHandler(
            body.host(), 
            body.port(), 
            body.gameKey(),
            KillBillGame.get().getCommandLoader(), 
            (e, msg) -> {
                label.setText(e.getMessage());
                Gdx.app.error("", "Send error: " + msg, e);
            }, 
            e -> {
                label.setText(e.getMessage());
                Gdx.app.error("", "Recv error", e);
            }
        );

        KillBillGame.get().setUdpClient(udpClient);

        try {
            udpClient.connect(game);
        } catch (final MessageFailure e) {
            label.setText(e.getMessage());
            Gdx.app.error("", "Connect error", e);
        }

        KillBillGame.get().setUdpClient(udpClient);
        KillBillGame.get().setScreen(Screens.CHAT_SCREEN);
    }
}
