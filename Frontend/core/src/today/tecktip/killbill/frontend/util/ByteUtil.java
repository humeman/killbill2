package today.tecktip.killbill.frontend.util;

/**
 * Handles dealing with byte data.
 * @author cs
 */
public class ByteUtil {
    /**
     * Extensions to use for byte counts.
     */
    private static final String[] BYTE_EXTENSIONS = new String[] {"B", "K", "M", "G", "T", "P"};

    /**
     * Converts a number of bytes into a readable format.
     * @param bytes Byte count
     * @return Readable byte count (KB, MB, ...)
     */
    public static String bytesToReadable(final long bytes) {
        double count = bytes;

        int i = 0;
        while (count > 1024) {
            i++;
            count /= 1024;
        }

        String res = String.format("%.1f", bytes / (Math.pow(1024, i)));

        if (res.endsWith(".0")) {
            res = res.substring(0, res.length() - 2);
        }

        return res + BYTE_EXTENSIONS[i];
    }
}
