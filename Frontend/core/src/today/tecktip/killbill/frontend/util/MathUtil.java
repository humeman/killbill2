package today.tecktip.killbill.frontend.util;

public class MathUtil {
    
    public static float distanceToEdgeAtAngle(final float width, final int angleToCenter) {
        int angle = angleToCenter % 90;
        /*
         *  |                   |
         *  |                   |
         *  |         X         | cos(0) = a / h -> h = a / cos(0)
         *  |       /0.         |
         *  |     /   .         |  . = width / 2
         *  |   /     .         |  / = x
         *  |_/_______._________|  0 = angle
         *  -45       0         45
         */ 
        return (width / 2) / (float) Math.cos(Math.toRadians(angle));
    }
}
