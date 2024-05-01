package today.tecktip.killbill.frontend.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.exceptions.RuntimeApiException;
import today.tecktip.killbill.frontend.http.requests.UserRequests;
import today.tecktip.killbill.frontend.http.requests.data.User.UserRole;
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
 * Login screen, takes in the username and password as text and then logs in
 * @author cz
 */
public class SignupScreen extends MenuScreen {
    public TextInput usernameInput;

    public TextInput emailInput;

    public TextInput passwordInput;

    public Button proceedButton;

    private Label errorLabel;

    /**
     * Constructs the login screen.
     */
    public SignupScreen() {
        super(new Color(0, 0, 0, 1));
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
            .setLocation(new ScaledLocation(0.5f, 0.925f))
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
                .setText("sign up")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(86)
                )
                .build()
        );

        emailInput = TextInput.newBuilder()
            .setLocation(new ScaledLocation(.1f, .65f))
            .setSize(new ScaledSize(.8f,.1f))
            .setTexture("ui_square")
            .setPlaceholder("email")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(new Color(0, 0, 0, 1))
                .setScaledSize(48)
            )
            .build();

        uiRenderer.add(emailInput);

        usernameInput = TextInput.newBuilder()
            .setLocation(new ScaledLocation(0.1f, 0.5f))
            .setSize(new ScaledSize(0.8f, 0.1f))
            .setTexture("ui_square")
            .setPlaceholder("username")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(0, 0, 0, 1))
                    .setScaledSize(48)
            )
            .build();

        uiRenderer.add(usernameInput);

        passwordInput = TextInput.newBuilder()
            .setSecret(true)
            .setLocation(new ScaledLocation(.1f, .35f))
            .setSize(new ScaledSize(.8f,.1f))
            .setTexture("ui_square")
            .setPlaceholder("password")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(new Color(0, 0, 0, 1))
                .setScaledSize(48)
            )
            .build();

        uiRenderer.add(passwordInput);

        emailInput.setNext(usernameInput);
        usernameInput.setNext(passwordInput);
        passwordInput.setNext(emailInput);

        

        proceedButton = Button.newBuilder()
            .setLocation(new ScaledLocation(0.45f, 0.1f))
            .setSize(new XScaledSize(0.3f, 0.33f))
            .setTexture("ui_button")
            .setText("proceed")
            .setOnPress(this::signUp)
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(56)
            )
            .build();

        uiRenderer.add(proceedButton);

        emailInput.setSubmit(proceedButton);
        usernameInput.setSubmit(proceedButton);
        passwordInput.setSubmit(proceedButton);

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.25f, 0.1f))
                .setSize(new XScaledSize(0.1f, 1f))
                .setTexture("ui_button_back")
                .setOnPress(() -> {
                    KillBillGame.get().changeScreen(Screens.MAIN_MENU_SCREEN);
                })
                .build()
        );
    }

    public void signUp() {
        //Takes in the username password and email from text boxes
        String username = usernameInput.getText();
        String password = passwordInput.getText();
        String email = emailInput.getText();

        //Signs in the user with entered username and password
        UserRequests.createUser( 
            new UserRequests.CreateUserRequestBody(username, email, password, UserRole.USER),
            response -> {
                usernameInput.setText("");
                passwordInput.setText("");
                emailInput.setText("");
                errorLabel.setText("");
                // Get the API key back
                KillBillGame.get().getHttpClient().setApiKey(response.key());
                KillBillGame.get().setUser(response.user());
                KillBillGame.get().changeScreen(Screens.USER_SCREEN);
            },
            e -> {
                Gdx.app.error(LoginScreen.class.getName(), "Login error:", e);
                if (e.getCause() instanceof RuntimeApiException) {
                    RuntimeApiException apiE = (RuntimeApiException) e.getCause();

                    if (apiE.getStatusCode() == 400) {
                        errorLabel.setText("error: invalid input");
                    } else {
                        errorLabel.setText("error: unexpected server error");
                    }
                } else {
                    errorLabel.setText("error: unexpected error while contacting server");
                }
            }
        );
    }
}
