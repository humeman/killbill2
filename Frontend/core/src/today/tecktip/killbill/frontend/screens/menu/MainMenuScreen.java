package today.tecktip.killbill.frontend.screens.menu;

import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.config.Keybinds.KeyType;
import today.tecktip.killbill.frontend.screens.MenuScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.XScaledSize;
import today.tecktip.killbill.frontend.ui.Size.YScaledSize;
import today.tecktip.killbill.frontend.ui.elements.Button;
import today.tecktip.killbill.frontend.ui.elements.Image;

/**
 * Main menu screen for login/signup
 * @author cs
 */
public class MainMenuScreen extends MenuScreen {

    public Button signInButton;
    public Button signUpButton;


    /**
     * Constructs the test screen.
     */
    public MainMenuScreen() {
        super(new Color(0, 0, 0, 1));
    }

    @Override
    public void onCreate() {
        uiRenderer.add(
            Image.newBuilder()
                .setTexture("ui_bg_*")
                .setSize(new YScaledSize(1, 1))
                .setLocation(new FixedLocation(0, 0))
                .build());

        uiRenderer.add(
            Image.newBuilder()
                .setTexture("ui_killbill")
                .setSize(new XScaledSize(.6f, .25f))
                .setLocation(new ScaledLocation(0.35f, .6f))
                .build());

        signInButton = Button.newBuilder()
            .setLocation(new ScaledLocation(0.5f, 0.375f))
            .setSize(new XScaledSize(0.3f, 0.33f))
            .setTexture("ui_button")
            .setText("sign in")
            .setOnPress(
                    () -> {
                        //throw new CatastrophicException("HEY");
                        KillBillGame.get().changeScreen(Screens.LOGIN_SCREEN);
                    }
            )
            .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                            .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                            .setScaledSize(56)
            )
            .build();

        uiRenderer.add(signInButton);

        signUpButton = Button.newBuilder()
            .setLocation(new ScaledLocation(0.5f, 0.15f))
            .setSize(new XScaledSize(0.3f, 0.33f))
            .setTexture("ui_button")
            .setText("sign up")
            .setOnPress(
                    () -> {
                        KillBillGame.get().changeScreen(Screens.SIGNUP_SCREEN);
                    }
            )
            .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                            .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                            .setScaledSize(56)
            )
            .build();

        uiRenderer.add(signUpButton);
    }

    @Override
    public void drawFirst(final float delta) {
        if (KillBillGame.get().getKeybinds().isKeyJustPressed(KeyType.RELOAD_MAP)) {
            // Swap out to the dev screen
            KillBillGame.get().changeScreen(Screens.DEV_SCREEN);
        }

        super.drawFirst(delta);
    }
}
