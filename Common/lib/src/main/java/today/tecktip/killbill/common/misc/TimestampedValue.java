package today.tecktip.killbill.common.misc;

import java.time.Instant;

/**
 * A generic encapsulator class which tracks a timestamp for when a value was last updated.
 * This proves useful for UDP applications, where data may arrive totally out of order if a
 *  packet gets dropped. This ensures we always keep the latest value.
 * @author cs
 */
public class TimestampedValue<T> {
    /**
     * The value currently being stored.
     */
    private T value;

    /**
     * The timestamp for when the latest value was applied.
     */
    private Instant lastUpdated;
    
    /**
     * Constructs a new TimestampedValue.
     * @param initialValue The initial value to store.
     */
    public TimestampedValue(final T initialValue) {
        value = initialValue;
        lastUpdated = Instant.ofEpochMilli(1);
    }

    /**
     * Changes the value regardless of the timestamp, setting the lastUpdated time
     *  to now.
     * @param newValue The new value to apply.
     */
    public void set(final T newValue) {
        value = newValue;
        lastUpdated = Instant.now();
    }

    /**
     * Sets the value only if the specified timestamp is after the current timestamp.
     * @param updatedAt The timestamp for when this value was created
     * @param newValue The value to store if the value is newer.
     */
    public void set(final Instant updatedAt, final T newValue) {
        if (updatedAt.isAfter(lastUpdated)) {
            lastUpdated = updatedAt;
            value = newValue;
        }
    }

    /**
     * Gets the currently held data.
     * @return Latest data value
     */
    public T get() {
        return value;
    }
}
