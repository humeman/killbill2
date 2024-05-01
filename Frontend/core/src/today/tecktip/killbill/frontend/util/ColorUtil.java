package today.tecktip.killbill.frontend.util;

import com.badlogic.gdx.graphics.Color;

/**
 * Utilities for dealing with colors.
 * @author cs
 */
public class ColorUtil {
    /**
     * Copies a color into a new instance.
     * @param color Color to copy
     * @return New unlinked color
     */
    public static Color copyColor(final Color color) {
        return new Color(color.r, color.g, color.b, color.a);
    }
}
