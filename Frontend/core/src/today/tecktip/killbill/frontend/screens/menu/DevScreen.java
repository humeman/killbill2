package today.tecktip.killbill.frontend.screens.menu;

import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
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

/**
 * Allows devs to make maps easier.
 * @author cs
 */
public class DevScreen extends MenuScreen {

    private Label mapLabel;
    private int mapI;

    private boolean changed;

    /**
     * Constructs the user screen.
     */
    public DevScreen() {
        super(new Color(0, 0, 0, 1));
        changed = false;
    }

    @Override
    public void onCreate() {
        
        uiRenderer.add(
            Image.newBuilder()
                .setTexture("ui_bg_*")
                .setSize(new YScaledSize(1, 1))
                .setLocation(new FixedLocation(0, 0))
                .setTint(new Color(1, 1, 1, 0.6f))
                .build());

        uiRenderer.add(
            Label.newBuilder()
                .setLocation(new ScaledLocation(0.5f, 0.85f))
                .setCentered(true)
                .setText("map viewer")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(64)
                )
                .build()
        );

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
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.46f, 0.05f))
                .setSize(new XScaledSize(0.2f, 0.33f))
                .setTexture("ui_button")
                .setText("go")
                .setOnPress(
                    () -> {
                        Screens.DEV_GAME_SCREEN.setMapPackage(mapI);
                        KillBillGame.get().changeScreen(Screens.DEV_GAME_SCREEN);
                    }
                )
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(40)
                )
                .build());

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.34f, 0.05f))
                .setSize(new XScaledSize(0.2f / 3, 1f))
                .setTexture("ui_button_back")
                .setOnPress(() -> {
                    KillBillGame.get().changeScreen(Screens.MAIN_MENU_SCREEN);
                })
                .build());
    }

    @Override
    public void onSwitch() {
        mapI = 0;
        populateMap();
        super.onSwitch();
    }

    private void populateMap() {
        mapLabel.setText("Map: " + KillBillGame.get().getMapLoader().get(mapI).map().getDisplayName());
    }

    @Override
    public void drawFirst(float delta) {
        if (changed) {
            populateMap();
            changed = false;
        }
        super.drawFirst(delta);
    }
}
