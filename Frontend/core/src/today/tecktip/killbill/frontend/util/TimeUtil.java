package today.tecktip.killbill.frontend.util;

import java.util.Map;

/**
 * Handles dealing with time data.
 * @author cs
 */
public class TimeUtil {
    /**
     * Extensions to use for byte counts.
     */
    private static final Map<Integer, String> TIME_EXTENSIONS = Map.of(
        60, "m",
        60 * 60, "h",
        60 * 60 * 24, "d"
    );

    /**
     * Converts a number of seconds into a readable format.
     * @param seconds Seconds
     * @return Readable byte count (KB, MB, ...)
     */
    public static String secondsToReadable(final long seconds) {
        long current = seconds;
        StringBuilder timeBuilder = new StringBuilder();

        while (current > 0) {
            // Find the next largest time interval

            int secondAmount = -1;
            for (final Map.Entry<Integer, String> kv : TIME_EXTENSIONS.entrySet()) {
                if (current >= kv.getKey()) secondAmount = kv.getKey();
            }

            if (secondAmount == -1) {
                // Seconds are all that remain. We don't care about these
                current = 0;
            }
            else {
                // Append our new time
                long amount = current / secondAmount;
                current %= secondAmount;

                timeBuilder.append(amount + TIME_EXTENSIONS.get(secondAmount) + " ");
            }
        }

        if (timeBuilder.toString().length() == 0) return "0s";

        return timeBuilder.toString().substring(0, timeBuilder.length() - 1);
    }
}
