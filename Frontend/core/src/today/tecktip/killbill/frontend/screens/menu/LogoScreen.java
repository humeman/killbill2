package today.tecktip.killbill.frontend.screens.menu;

import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.screens.MenuScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.XScaledSize;
import today.tecktip.killbill.frontend.ui.elements.Image;

/**
 * Logo Screen
 * @author cz, cs
 */
public class LogoScreen extends MenuScreen {

    private float timer;

    private Image logo;

    /**
     * Constructs the test screen.
     */
    public LogoScreen() {
        super(new Color(0, 0, 0, 1));
    }

    @Override
    public void onCreate() {
        logo = Image.newBuilder()
            .setTexture("ui_michaelsoft")
            .setSize(new XScaledSize(.8f, .142f))
            .setLocation(new ScaledLocation(0.1f, (1 - .142f) / 2))
            .build();
        uiRenderer.add(logo);
    }

    @Override
    public void onSwitch() {
        timer = 0;
    }
    
    @Override
    public void drawFirst(float delta) {
        super.drawFirst(delta);
        timer += delta;

        if (timer < 1f) {
            // Fade in sequence
            logo.setTint(new Color(1, 1, 1, timer));
        }
        else if (timer > 3f && timer < 4f) {
            // Fade out
            logo.setTint(new Color(1, 1, 1, 4f - timer));
        }
        else if (timer >= 4f && timer < 5f) {
            logo.setTint(new Color(1, 1, 1, 0));
        }
        else if (timer > 5f) {
            KillBillGame.get().changeScreen(Screens.MAIN_MENU_SCREEN);
        }
    }

}
