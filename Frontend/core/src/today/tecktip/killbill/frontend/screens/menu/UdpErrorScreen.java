package today.tecktip.killbill.frontend.screens.menu;

import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.screens.MenuScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.XScaledSize;
import today.tecktip.killbill.frontend.ui.Size.YScaledSize;
import today.tecktip.killbill.frontend.ui.elements.Button;
import today.tecktip.killbill.frontend.ui.elements.Image;
import today.tecktip.killbill.frontend.ui.elements.Label;

/**
 * A generic catastrophic error screen for UDP errors.
 * @author cs
 */
public class UdpErrorScreen extends MenuScreen {
    private Label reason;

    /**
     * Constructs the test screen.
     */
    public UdpErrorScreen() {
        super(new Color(0, 0, 0, 1));
    }

    /**
     * Assigns a reason for the error. Must be defined before use.
     */
    public void init(final String reason) {
        this.reason.setText(reason);
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
        reason = Label.newBuilder()
            .setCentered(true)
            .setLocation(new ScaledLocation(0.5f, 0.65f))
            .setText("")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR)
                    .setScaledSize(48)
            )
            .build();
        uiRenderer.add(reason);

        uiRenderer.add(Button.newBuilder()
            .setLocation(new ScaledLocation(.4f, 0.25f))
            .setSize(new XScaledSize(.2f,.5f))
            .setTexture("ui_mini_button")
            .setText("back")
            .setOnPress(
                () -> {
                    KillBillGame.get().changeScreen(Screens.USER_SCREEN);
                }
            )
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR)
                    .setScaledSize(36)
            )
            .build());
    }
}
