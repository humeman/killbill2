package today.tecktip.killbill.frontend.screens.menu;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.http.requests.GameRequests;
import today.tecktip.killbill.frontend.http.requests.GameRequests.CreateGameRequestBody;
import today.tecktip.killbill.frontend.http.requests.UserRequests;
import today.tecktip.killbill.frontend.http.requests.UserRequests.GetUserRequestBody;
import today.tecktip.killbill.frontend.http.requests.data.User;
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
import today.tecktip.killbill.frontend.ui.elements.TextInput;

/**
 * Allows a player to create a game
 * @author cs
 */
public class CreateGameScreen extends MenuScreen {

    public TextInput gameName;
    public TextInput username;

    private Label errorLabel;
    public Label usersLabel;
    private Label mapLabel;
    private int mapI;

    public List<User> invitedUsers;

    private boolean changed;

    public Button addUserButton;

    public Button createGameButton;

    /**
     * Constructs the user screen.
     */
    public CreateGameScreen() {
        super(new Color(0, 0, 0, 1));
        invitedUsers = new ArrayList<>();
        changed = false;
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

        errorLabel = Label.newBuilder()
            .setLocation(new ScaledLocation(0.5f, 0.95f))
            .setCentered(true)
            .setText("")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                .setScaledSize(32)
            )
            .build();

        uiRenderer.add(errorLabel);

        uiRenderer.add(
            Label.newBuilder()
                .setLocation(new ScaledLocation(0.5f, 0.85f))
                .setCentered(true)
                .setText("create game")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(64)
                )
                .build()
        );
        
        gameName = TextInput.newBuilder()
            .setLocation(new ScaledLocation(.1f, .65f))
            .setSize(new ScaledSize(.8f,.075f))
            .setTexture("ui_square")
            .setPlaceholder("game name")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(new Color(0, 0, 0, 1))
                .setScaledSize(36)
            )
            .build();

        uiRenderer.add(gameName);

        username = TextInput.newBuilder()
            .setLocation(new ScaledLocation(.1f, .55f))
            .setSize(new ScaledSize(.725f,.075f))
            .setTexture("ui_square")
            .setPlaceholder("username")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(new Color(0, 0, 0, 1))
                .setScaledSize(36)
            )
            .build();

        uiRenderer.add(username);

        gameName.setNext(username);
        username.setNext(gameName);

        addUserButton = Button.newBuilder()
            .setLocation(new ScaledLocation(0.825f, .55f))
            .setSize(new ScaledSize(0.075f, .075f))
            .setTexture("ui_mini_button_plus")
            .setOnPress(this::addUser)
            .build();

        uiRenderer.add(addUserButton);

        username.setSubmit(addUserButton);

        mapLabel = Label.newBuilder()
            .setLocation(new ScaledLocation(0.5f, 0.45f + 0.075f/2))
            .setCentered(true)
            .setText("")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.TERTIARY_COLOR.cpy())
                .setScaledSize(40)
            )
            .build();

        uiRenderer.add(mapLabel);

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.1f, .45f))
                .setSize(new ScaledSize(0.075f, .075f))
                .setTexture("ui_mini_button_back")
                .setOnPress(
                    () -> {
                        mapI--;
                        if (mapI <= 0) mapI = KillBillGame.get().getMapLoader().getAll().size() - 1;
                        changed = true;
                    })
                .build());

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.825f, .45f))
                .setSize(new ScaledSize(.075f, .075f))
                .setTexture("ui_mini_button_forward")
                .setOnPress(
                    () -> {
                        mapI++;
                        if (mapI >= KillBillGame.get().getMapLoader().getAll().size()) mapI = 0;
                        changed = true;
                    }
                )
                .build());

        uiRenderer.add(
            Label.newBuilder()
                .setLocation(new ScaledLocation(0.5f, 0.35f))
                .setCentered(true)
                .setText("P L A Y E R S")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(40)
                )
                .build()
        );
        
        usersLabel = Label.newBuilder()
            .setLocation(new ScaledLocation(0.5f, 0.275f))
            .setCentered(true)
            .setText("")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.TERTIARY_COLOR.cpy())
                .setScaledSize(32)
            )
            .build();

        uiRenderer.add(usersLabel);

        createGameButton = Button.newBuilder()
            .setLocation(new ScaledLocation(0.46f, 0.05f))
            .setSize(new XScaledSize(0.2f, 0.33f))
            .setTexture("ui_button")
            .setText("create")
            .setOnPress(this::createGame)
            .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                            .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                            .setScaledSize(40)
            )
            .build();

        uiRenderer.add(createGameButton);

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.34f, 0.05f))
                .setSize(new XScaledSize(0.2f / 3, 1f))
                .setTexture("ui_button_back")
                .setOnPress(() -> {
                    KillBillGame.get().changeScreen(Screens.USER_SCREEN);
                })
                .build());
    }

    @Override
    public void onSwitch() {
        gameName.setText("");
        username.setText("");
        mapI = 0;
        invitedUsers.clear();
        populateUsers();
        populateMap();
        super.onSwitch();
    }

    private void populateUsers() {
        String usersStr = KillBillGame.get().getUser().name() + ", ";
        for (final User user : invitedUsers) {
            usersStr += user.name() + ", ";
        }

        usersLabel.setText(usersStr.substring(0, usersStr.length() - 2));
    }

    private void populateMap() {
        mapLabel.setText("Map: " + KillBillGame.get().getMapLoader().get(mapI).map().getDisplayName());
    }

    @Override
    public void drawFirst(float delta) {
        if (changed) {
            populateUsers();
            populateMap();
            changed = false;
        }
        super.drawFirst(delta);
    }

    private void addUser() {
        if (invitedUsers.size() == 4) {
            errorLabel.setText("Too many users!");
        }

        UserRequests.getUser(
            new GetUserRequestBody(null, username.getText()), 
            response -> {
                if (response.user().id().equals(KillBillGame.get().getUser().id())) {
                    errorLabel.setText("you can't play against yourself, you lonely man");
                    return;
                }
                for (final User user : invitedUsers) {
                    if (user.id().equals(response.user().id())) {
                        errorLabel.setText("you already invited that one >:(");
                        return;
                    }
                }
                invitedUsers.add(response.user());
                username.setText("");
                changed = true;
            },
            e -> {
                Gdx.app.error(CreateGameScreen.class.getName(), "Failed to search for user", e);
                errorLabel.setText("error: invalid user");
            });
    }

    private void createGame() {
        if (invitedUsers.size() == 0) {
            errorLabel.setText("you have to invite someone first");
            return;
        }

        GameRequests.createGame(
            new CreateGameRequestBody(gameName.getText(), invitedUsers.stream().map(v -> { return v.id(); }).toList(), KillBillGame.get().getMapLoader().get(mapI).config(), KillBillGame.get().getMapLoader().get(mapI).map().toString()), 
            response -> {
                KillBillGame.get().changeScreen(Screens.USER_SCREEN);
            }, 
            e -> {
                Gdx.app.error(CreateGameScreen.class.getName(), "Failed to create game", e);
                errorLabel.setText("error: game creation failed");
            });
    }
}
