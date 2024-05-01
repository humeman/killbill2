package today.tecktip.killbill.frontend.screens.menu;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.KillBillGame.Platform;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.requests.UserRequests;
import today.tecktip.killbill.frontend.resources.FontLoader;
import today.tecktip.killbill.frontend.screens.MenuScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.YScaledSize;
import today.tecktip.killbill.frontend.ui.elements.Image;
import today.tecktip.killbill.frontend.ui.elements.Label;

/**
 * The classic BSOD end screen.
 * @author cs
 */
public class GameEndScreen extends MenuScreen {
    private static final Random RANDOM = new Random();

    private static final Color WIN_BG_COLOR = new Color(13f/255, 126.0f/255, 0f, 1f);
    private static final Color LOSE_BG_COLOR = new Color(0f, 120f/255, 215f/255, 1f);

    private static final String WIN_FACE = "ui_happy";
    private static final String LOSE_FACE = "ui_sad";

    private static final String WIN_TITLE = "Your PC didn't run into a problem and doesn't need to restart.\nCongratulations!";
    private static final String LOSE_TITLE = "Your PC ran into a problem and needs to restart. We're\njust collecting some error info, and then we'll restart for\nyou.";
    
    private static final String WIN_STOP_CODE = "Stop code: YOU_WINNED";
    private static final String LOSE_STOP_CODE = "Stop code: YUO_LOOSED";

    private Image faceImage;

    private Image qrImage;

    private Label stopCodeLabel;

    private Label titleLabel;

    private Label percentLabel;

    private Label infoLabel;

    private int percent;

    private float cooldown;

    private boolean oldFullscreenState;

    private int oldWidth;
    private int oldHeight;

    private boolean started;

    /**
     * Constructs the test screen.
     */
    public GameEndScreen() {
        super(new Color(0, 0, 0, 1));
    }
    
    public void init(final boolean won) {
        percent = 0;
        cooldown = 5;
        if (won) {
            backgroundColor = WIN_BG_COLOR;
            faceImage.setTexture(KillBillGame.get().getTextureLoader().get(WIN_FACE));
            titleLabel.setText(WIN_TITLE);
            stopCodeLabel.setText(WIN_STOP_CODE);
        } else {
            backgroundColor = LOSE_BG_COLOR;
            faceImage.setTexture(KillBillGame.get().getTextureLoader().get(LOSE_FACE));
            titleLabel.setText(LOSE_TITLE);
            stopCodeLabel.setText(LOSE_STOP_CODE);
        }

        started = false;
    }

    @Override
    public void onCreate() {
        // Face
        faceImage = Image.newBuilder()
            .setTexture(LOSE_FACE)
            .setLocation(new ScaledLocation(0.1f, 0.65f))
            .setSize(new YScaledSize(1, 0.2f))
            .build();
        uiRenderer.add(faceImage);
        
        FontLoader.Builder titleFont = KillBillGame.get().getFontLoader().newBuilder("segoe")
            .setColor(Color.WHITE)
            .setScaledSize(20);

        // Title text
        titleLabel = Label.newBuilder()
            .setFontBuilder(titleFont)
            .setCentered(false)
            .setLocation(new ScaledLocation(0.1f, 0.625f))
            .setText(LOSE_TITLE)
            .build();
        uiRenderer.add(titleLabel);

        // Percentage text
        percentLabel = Label.newBuilder()
            .setFontBuilder(titleFont)
            .setCentered(false)
            .setLocation(new ScaledLocation(0.1f, 0.425f))
            .setText("0% complete")
            .build();
        uiRenderer.add(percentLabel);

        // QR code
        qrImage = Image.newBuilder()
            .setTexture("ui_qr")
            .setLocation(new ScaledLocation(0.1f, 0.15f))
            .setSize(new YScaledSize(1, 0.175f))
            .build();
        uiRenderer.add(qrImage);
        
        FontLoader.Builder infoFont = KillBillGame.get().getFontLoader().newBuilder("segoe")
            .setColor(Color.WHITE)
            .setScaledSize(12);

        // Info label
        infoLabel = Label.newBuilder()
            .setFontBuilder(infoFont)
            .setCentered(false)
            .setLocation(new ScaledLocation(0.215f, 0.315f))
            .setText("For more information about this issue and possible fixes, visit https://tecktip.today")
            .build();
        uiRenderer.add(infoLabel);
        
        // Stop code header
        uiRenderer.add(Label.newBuilder()
            .setFontBuilder(infoFont)
            .setCentered(false)
            .setLocation(new ScaledLocation(0.215f, 0.2175f))
            .setText("If you call a support person, give them this info:")
            .build());

        stopCodeLabel = Label.newBuilder()
            .setFontBuilder(infoFont)
            .setCentered(false)
            .setLocation(new ScaledLocation(0.215f, 0.175f))
            .setText(LOSE_STOP_CODE)
            .build();
        uiRenderer.add(stopCodeLabel);

        started = false;
    }

    @Override
    public void onSwitch() {
        // I have no idea why, but this gets called twice
        if (!started) {
            started = true;
            if (KillBillGame.get().getPlatform().equals(Platform.DESKTOP)) {
                oldFullscreenState = Gdx.graphics.isFullscreen();
                // Store old FS mode
                oldWidth = Gdx.graphics.getWidth();
                oldHeight = Gdx.graphics.getHeight();
            }
        }

        // If this is a PC, freeze for a bit and go fullscreen
        if (KillBillGame.get().getPlatform().equals(Platform.DESKTOP)) {
            if (!oldFullscreenState)
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

            try {
                TimeUnit.SECONDS.sleep(3); // We love blocking operations on the main thread :)
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
    
    @Override
    public void drawFirst(float delta) {
        super.drawFirst(delta);
        cooldown -= delta;
        
        if (percent == 100) {
            if (cooldown <= 0) {
                // All done.
                // Undo fullscreen changes
                if (KillBillGame.get().getPlatform().equals(Platform.DESKTOP) && !oldFullscreenState) {
                    if (!Gdx.graphics.setWindowedMode(oldWidth, oldHeight)) throw new CatastrophicException("Couldn't go back to windowed mode");
                }

                // Go back to main screen
                UserRequests.getAuthenticatedUser(
                    userResponse -> {
                        KillBillGame.get().setUser(userResponse.user());
                        KillBillGame.get().changeScreen(Screens.USER_SCREEN);
                    },
                    e -> {
                        throw new CatastrophicException("User retrieval error: ", e);
                    }
                );
            }
            return;
        }

        // Randomly increment the percent by a random amount
        if (cooldown <= 0) {
            cooldown = Math.max(2, RANDOM.nextInt(5));
            percent += Math.max(8, RANDOM.nextInt(20));
            percent = Math.min(100, percent);
            percentLabel.setText(percent + "% complete");
        }
    }
}
