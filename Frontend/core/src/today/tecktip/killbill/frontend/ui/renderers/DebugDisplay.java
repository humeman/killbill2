package today.tecktip.killbill.frontend.ui.renderers;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.Keybinds.KeyType;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.elements.Label;
import today.tecktip.killbill.frontend.util.ByteUtil;

/**
 * An extension to the UiRenderer (a package of elements that can be
 *  added to or removed from a UiRenderer).
 * @author cs
 */
public class DebugDisplay extends RendererExtension {

    /**
     * The debug display string generators.
     */
    private List<DebugStringGeneratorMethod> generators;

    /**
     * The label to draw.
     */
    private final Label label;

    /**
     * Lines on the debug display.
     */
    private final List<String> lines;

    /**
     * Frames since last render.
     */
    private int frames;

    /**
     * Time since FPS was last taken.
     */
    private float deltaSum;

    /**
     * The current FPS.
     */
    private int fps;

    /**
     * Creates a new debug display.
     */
    public DebugDisplay() {
        super(true);
        generators = new ArrayList<>();
        lines = new ArrayList<>();

        label = Label.newBuilder()
            .setLocation(new FixedLocation(0, 0))
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(new Color(1, 1, 1, 1))
                .setBorder(new Color(0, 0, 0, 1), 2)
                .setScaledSize(16)
            )
            .setText("")
            .build();

        elements.add(label);

        fps = -1;
        frames = 0;
        deltaSum = 0;

        addGenerator(
            delta -> {
                return String.format("Kill Bill %s", System.getenv("VERSION") != null ? System.getenv("VERSION") : "");
            }
        );

        addGenerator(
            delta -> {
                updateFps(delta);
                return String.format("FPS: %d", fps);
            }
        );

        addGenerator(
            delta -> {
                long total = Runtime.getRuntime().totalMemory();
                long ramUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                return String.format("RAM: %s/%s", ByteUtil.bytesToReadable(ramUsage), ByteUtil.bytesToReadable(total));
            }
        );
    }

    /**
     * Adds a new debug string generator method.
     * @param method Method to add
     */
    public void addGenerator(final DebugStringGeneratorMethod method) {
        generators.add(method);
        lines.add("");
    }

    @Override
    public boolean runKey(final int key) {
        if (KillBillGame.get().getKeybinds().isMemberOf(KeyType.DEBUG, key)) {
            KillBillGame.get().setDebugEnabled(!KillBillGame.get().isDebugEnabled());
            return true;
        }

        super.runKey(key);
        return false;
    }

    @Override
    public void render(final SpriteBatch batch, final float delta) {
        if (!KillBillGame.get().isDebugEnabled()) return;

        for (int i = 0; i < generators.size(); i++) {
            lines.set(i, generators.get(i).generate(delta));
        }

        label.setText(String.join("\n", lines));

        updateLocation();

        super.render(batch, delta);
    }

    /**
     * Updates the location of the label.
     */
    private void updateLocation() {
        label.getRectangle().getLocation().setX(10);
        label.getRectangle().getLocation().setY(KillBillGame.get().getHeight() - 10);
    }

    /**
     * Updates the FPS. Call every frame.
     * @param delta Delta time since last frame render.
     */
    private void updateFps(final float delta) {
        deltaSum += delta;
        frames++;

        if (deltaSum >= 1.0) {
            fps = frames;
            frames = 0;
            deltaSum = 0;
        }
    }

    /**
     * A functional interface for generating debug display strings.
     */
    public static interface DebugStringGeneratorMethod {
        /**
         * Generates a new debug string.
         * @param delta Time since last render
         * @return String to display
         */
        public String generate(float delta);
    }
}
