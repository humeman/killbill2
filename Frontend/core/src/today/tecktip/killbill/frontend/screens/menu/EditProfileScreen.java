package today.tecktip.killbill.frontend.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.fasterxml.jackson.core.JsonProcessingException;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.requests.AuthRequests;
import today.tecktip.killbill.frontend.http.requests.UserRequests;
import today.tecktip.killbill.frontend.http.requests.AuthRequests.ChangeEmailRequestBody;
import today.tecktip.killbill.frontend.http.requests.AuthRequests.ChangePasswordRequestBody;
import today.tecktip.killbill.frontend.http.requests.UserRequests.ChangeUsernameRequestBody;
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
 * Allows the user to edit their profile.
 * @author cs
 */
public class EditProfileScreen extends MenuScreen {

    private TextInput username;
    private TextInput email;
    private TextInput password;
    private Button passwordChangeButton;
    private Button usernameChangeButton;
    private Button emailChangeButton;


    private Label errorLabel;

    /**
     * Constructs the edit profile screen.
     */
    public EditProfileScreen() {
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
                .setText("edit profile")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(86)
                )
                .build()
        );

        username = TextInput.newBuilder()
            .setLocation(new ScaledLocation(0.1f, 0.65f))
            .setSize(new ScaledSize(0.675f, 0.1f))
            .setTexture("ui_square")
            .setPlaceholder("username")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(0, 0, 0, 1))
                    .setScaledSize(48)
            )
            .build();

        uiRenderer.add(username);

        uiRenderer.add(usernameChangeButton = Button.newBuilder()
            .setLocation(new ScaledLocation(0.8f, 0.65f))
            .setSize(new YScaledSize(1f, 0.1f))
            .setTexture("ui_mini_mini_button_confirm")
            .setOnPress(
                () -> {
                    UserRequests.changeUsername(
                        new ChangeUsernameRequestBody(username.getText()), 
                        response -> {
                            reload();
                        },
                        e -> {
                            Gdx.app.error(EditProfileScreen.class.getSimpleName(), "Error while changing username", e);
                            errorLabel.setText("error: failed to change username");
                        });
                }
            )
            .build());

        email = TextInput.newBuilder()
            .setLocation(new ScaledLocation(0.1f, 0.5f))
            .setSize(new ScaledSize(0.675f, 0.1f))
            .setTexture("ui_square")
            .setPlaceholder("email")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(0, 0, 0, 1))
                    .setScaledSize(48)
            )
            .build();

        uiRenderer.add(email);

        uiRenderer.add(
            emailChangeButton = Button.newBuilder()
            .setLocation(new ScaledLocation(0.8f, 0.5f))
            .setSize(new YScaledSize(1f, 0.1f))
            .setTexture("ui_mini_mini_button_confirm")
            .setOnPress(
                () -> {
                    try {
                        AuthRequests.changeEmail(
                            new ChangeEmailRequestBody(email.getText()), 
                            response -> {
                                reload();
                            },
                            e -> {
                                Gdx.app.error(EditProfileScreen.class.getSimpleName(), "Error while changing email", e);
                                errorLabel.setText("error: failed to change email");
                            });
                    } catch (final JsonProcessingException e) {
                        throw new CatastrophicException("JSON error", e);
                    }
                }
            )
            .build());

        password = TextInput.newBuilder()
            .setSecret(true)
            .setLocation(new ScaledLocation(0.1f, 0.35f))
            .setSize(new ScaledSize(0.675f, 0.1f))
            .setTexture("ui_square")
            .setPlaceholder("password")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(0, 0, 0, 1))
                    .setScaledSize(48)
            )
            .build();

        uiRenderer.add(password);

        username.setNext(email);
        email.setNext(password);
        password.setNext(username);

        uiRenderer.add(passwordChangeButton = Button.newBuilder()
            .setLocation(new ScaledLocation(0.8f, 0.35f))
            .setSize(new YScaledSize(1f, 0.1f))
            .setTexture("ui_mini_mini_button_confirm")
            .setOnPress(
                () -> {
                    try {
                        AuthRequests.changePassword(
                            new ChangePasswordRequestBody(password.getText()), 
                            response -> {
                                KillBillGame.get().getHttpClient().setApiKey(response.key());
                                reload();
                            },
                            e -> {
                                Gdx.app.error(EditProfileScreen.class.getSimpleName(), "Error while changing password", e);
                                errorLabel.setText("error: failed to change password");
                            });
                    } catch (final JsonProcessingException e) {
                        throw new CatastrophicException("JSON error", e);
                    }
                }
            )
            .build());
        password.setSubmit(passwordChangeButton);
        email.setSubmit(emailChangeButton);
        username.setSubmit(usernameChangeButton);

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.45f, 0.05f))
                .setSize(new XScaledSize(0.1f, 1f))
                .setTexture("ui_button_back")
                .setOnPress(
                    () -> {
                        KillBillGame.get().changeScreen(Screens.USER_SCREEN);
                    }
                )
                .build()
        );
    }

    @Override
    public void onSwitch() {
        super.onSwitch();

        username.setText("");
        password.setText("");
        email.setText("");

    }

    public void reload() {
        UserRequests.getAuthenticatedUser(
            response -> {
                KillBillGame.get().setUser(response.user());
                KillBillGame.get().changeScreen(Screens.USER_SCREEN);
            },
            e -> {
                Gdx.app.error(EditProfileScreen.class.getSimpleName(), "Error while refreshing user", e);
                errorLabel.setText("error: failed to get user");
            });
    }
}
