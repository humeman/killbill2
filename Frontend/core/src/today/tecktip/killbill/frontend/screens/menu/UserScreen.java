package today.tecktip.killbill.frontend.screens.menu;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.requests.AuthRequests;
import today.tecktip.killbill.frontend.http.requests.GameRequests;
import today.tecktip.killbill.frontend.http.requests.UserRequests;
import today.tecktip.killbill.frontend.http.requests.GameRequests.ListGamesRequestBody;
import today.tecktip.killbill.frontend.http.requests.GameRequests.ListGamesResponseInnerObject;
import today.tecktip.killbill.frontend.http.requests.data.User.UserRole;
import today.tecktip.killbill.frontend.screens.GameScreen;
import today.tecktip.killbill.frontend.screens.MenuScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.XScaledSize;
import today.tecktip.killbill.frontend.ui.Size.YScaledSize;
import today.tecktip.killbill.frontend.ui.UiElement;
import today.tecktip.killbill.frontend.ui.elements.Button;
import today.tecktip.killbill.frontend.ui.elements.Image;
import today.tecktip.killbill.frontend.ui.elements.Label;
import today.tecktip.killbill.frontend.util.TimeUtil;

/**
 * Main user page after logging in
 * @author cs
 */
public class UserScreen extends MenuScreen {

    public Label username;

    private Label role;

    private Label joinedAt;

    private Label winsAsBill;

    private Label winsAsPlayer;

    private Label playtime;

    private List<UiElement> gameElements;

    public List<ListGamesResponseInnerObject> games;

    private boolean readyForGames;

    private float refreshTimer;
    public Button createGameButton;
    public Button friendsButton;

    /**
     * Constructs the user screen.
     */
    public UserScreen() {
        super(new Color(0, 0, 0, 1));
        gameElements = new ArrayList<>();
        readyForGames = false;
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

        username = Label.newBuilder()
            .setLocation(new ScaledLocation(0.25f, 0.8f))
            .setCentered(true)
            .setText("test")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                .setScaledSize(52)
            )
            .build();

        uiRenderer.add(username);

        role = Label.newBuilder()
            .setLocation(new ScaledLocation(0.25f, 0.675f))
            .setCentered(true)
            .setText("test")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.TERTIARY_COLOR.cpy())
                .setScaledSize(24)
            )
            .build();

        uiRenderer.add(role);

        joinedAt = Label.newBuilder()
            .setLocation(new ScaledLocation(0.25f, 0.6f))
            .setCentered(true)
            .setText("")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.TERTIARY_COLOR.cpy())
                .setScaledSize(24)
            )
            .build();

        uiRenderer.add(joinedAt);

        

        winsAsBill = Label.newBuilder()
            .setLocation(new ScaledLocation(0.25f, 0.5f))
            .setCentered(true)
            .setText("")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.TERTIARY_COLOR.cpy())
                .setScaledSize(24)
            )
            .build();

        uiRenderer.add(winsAsBill);

        winsAsPlayer = Label.newBuilder()
            .setLocation(new ScaledLocation(0.25f, 0.45f))
            .setCentered(true)
            .setText("")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.TERTIARY_COLOR.cpy())
                .setScaledSize(24)
            )
            .build();

        uiRenderer.add(winsAsPlayer);

        playtime = Label.newBuilder()
            .setLocation(new ScaledLocation(0.25f, 0.4f))
            .setCentered(true)
            .setText("")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.TERTIARY_COLOR.cpy())
                .setScaledSize(24)
            )
            .build();

        uiRenderer.add(playtime);

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.05f, 0.15f))
                .setSize(new XScaledSize(0.15f, 0.5f))
                .setTexture("ui_mini_button")
                .setText("sign out")
                .setOnPress(
                    () -> {
                        AuthRequests.signOut(
                            resp -> {
                                Gdx.app.log(GameScreen.class.getSimpleName(), "Signed out.");
                                KillBillGame.get().setUser(null);
                                KillBillGame.get().changeScreen(Screens.MAIN_MENU_SCREEN);
                            },
                            error -> {
                                Gdx.app.error(GameScreen.class.getSimpleName(), "Sign out failed.", error);
                                KillBillGame.get().setUser(null);
                                KillBillGame.get().changeScreen(Screens.MAIN_MENU_SCREEN);
                            }
                        );
                    }
                )
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(24)
                )
                .build());

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.3f, 0.15f))
                .setSize(new XScaledSize(0.15f, 0.5f))
                .setTexture("ui_mini_button")
                .setText("edit")
                .setOnPress(
                    () -> {
                        KillBillGame.get().changeScreen(Screens.EDIT_PROFILE_SCREEN);
                    }
                )
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(24)
                )
                .build());

        uiRenderer.add(
            Label.newBuilder()
                .setLocation(new ScaledLocation(0.55f, 0.9f))
                .setCentered(false)
                .setText("F R I E N D S")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(40)
                )
                .build());

        friendsButton = Button.newBuilder()
            .setLocation(new ScaledLocation(1 - .05f - .0725f, 0.82f))
            .setSize(new XScaledSize(0.0725f, 1f))
            .setTexture("ui_mini_button_forward")
            .setOnPress(
                () -> {
                    KillBillGame.get().changeScreen(Screens.FRIEND_SCREEN);
                }
            )
            .build();
        uiRenderer.add(friendsButton);

        uiRenderer.add(
            Label.newBuilder()
                .setLocation(new ScaledLocation(0.55f, 0.7f))
                .setCentered(false)
                .setText("G A M E S")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(40)
                )
                .build());

        createGameButton = Button.newBuilder()
            .setLocation(new ScaledLocation(1 - .05f - .0725f, 0.62f))
            .setSize(new XScaledSize(0.0725f, 1f))
            .setTexture("ui_mini_button_plus")
            .setOnPress(
                    () -> {
                        KillBillGame.get().changeScreen(Screens.CREATE_GAME_SCREEN);
                    }
            )
            .build();

        uiRenderer.add(createGameButton);
    }

    @Override
    public void onSwitch() {
        username.setText(KillBillGame.get().getUser().name());
        role.setText("[" + (KillBillGame.get().getUser().role().equals(UserRole.USER) ? "PLAYER" : KillBillGame.get().getUser().role().toString()) + "]");
        joinedAt.setText("joined " + KillBillGame.get().getUser().created().toString().split("T")[0].replace('-', '/'));

        winsAsBill.setText(
            KillBillGame.get().getUser().winsAsBill() + " win" + 
            (KillBillGame.get().getUser().winsAsBill() == 1 ? "" : "s") + 
            " as bill");
        winsAsPlayer.setText(
            KillBillGame.get().getUser().winsAsPlayer() + " win" + 
            (KillBillGame.get().getUser().winsAsPlayer() == 1 ? "" : "s") + 
            " as player");
        playtime.setText(TimeUtil.secondsToReadable(KillBillGame.get().getUser().playtime()) + " wasted");

        populateGames();
        super.onSwitch();
    }

    @Override
    public void drawFirst(final float delta) {
        refreshTimer += delta;

        if (refreshTimer > 5f) {
            refreshTimer = 0;
            UserRequests.getAuthenticatedUser(
                userResponse -> {
                    KillBillGame.get().setUser(userResponse.user());
                    onSwitch();
                },
                e -> {
                    throw new CatastrophicException("User retrieval error: ", e);
                }
            );
        }

        if (readyForGames) {
            for (final UiElement element : gameElements) {
                uiRenderer.remove(element);
            }
            gameElements.clear();

            float yScale = 0.45f;
            for (final ListGamesResponseInnerObject game : games) {
                gameElements.add(
                    Button.newBuilder()
                        .setLocation(new ScaledLocation(0.55f, yScale))
                        .setSize(new XScaledSize(0.0725f * 0.75f, 1f))
                        .setTexture("ui_mini_mini_button_forward")
                        .setOnPress(
                            () -> {
                                Screens.LOBBY_LOADING_SCREEN.assignGame(game.game(), KillBillGame.get().getUser());
                                KillBillGame.get().changeScreen(Screens.LOBBY_LOADING_SCREEN);
                            }
                        )
                        .build());

                gameElements.add(
                    Label.newBuilder()
                        .setLocation(new ScaledLocation(0.625f, yScale + 0.035f))
                        .setCentered(false)
                        .setText(game.game().name().toString())
                        .setFontBuilder(
                            KillBillGame.get().getFontLoader().newBuilder("main")
                            .setColor(GlobalGameConfig.TERTIARY_COLOR.cpy())
                            .setScaledSize(24)
                        )
                        .build());

                gameElements.add(
                    Label.newBuilder()
                        .setLocation(new ScaledLocation(0.625f, yScale + 0.07f))
                        .setCentered(false)
                        .setText(game.users().size() + " playing | " + game.game().created().toString().split("T")[1].split("\\.")[0])
                        .setFontBuilder(
                            KillBillGame.get().getFontLoader().newBuilder("main")
                            .setColor(GlobalGameConfig.SECONDARY_COLOR.cpy())
                            .setScaledSize(18)
                        )
                        .build());

                yScale -= 0.125f;
                if (yScale <= 0.05f) break;
            }

            for (final UiElement element : gameElements) {
                uiRenderer.add(element);
            }
            readyForGames = false;
        }


        super.drawFirst(delta);
    }

    public void populateGames() {
        GameRequests.listGames(
            new ListGamesRequestBody(KillBillGame.get().getUser().id()),
            response -> {
                games = response.games();
                readyForGames = true;
            },
            error -> {
                throw new CatastrophicException("Unable to retrieve games", error);
            }
        );
    }
}
