package today.tecktip.killbill.frontend.http.requests.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;

/**
 * Record representing a DM retrieved from the API.
 * @param id Message UUID
 * @param fromId Sending user's UUID
 * @param toId Receiving user's UUID
 * @param created Timestamp for when the message was sent
 * @param message Message content
 * @param state Message read state
 * @author cs
 */
public record Dm(
    @NotNull UUID id,
    @NotNull UUID fromId, 
    @NotNull UUID toId,
    @NotNull Instant created, 
    @NotNull String message,
    @NotNull MessageState state) {
    
    /**
     * Parses a raw API data-type friend into a regular Dm.
     * @param rawDm Raw dm response from API
     * @return Parsed regular Dm
     */
    public static Dm fromRaw(final RawDm rawDm) {
        try {
            return new Dm(
                UUID.fromString(rawDm.id()),
                UUID.fromString(rawDm.fromId()),
                UUID.fromString(rawDm.toId()),
                Instant.parse(rawDm.created()),
                rawDm.message(),
                MessageState.valueOf(rawDm.state())
            );
        } catch (final Throwable t) {
            throw new CatastrophicException("Unable to parse response from the API: ", t);
        }
    }

    /**
     * Returns a DM to its raw API data-type.
     * @return Raw representation, sendable to the API
     */
    public RawDm toRaw() {
        return new RawDm(
            id.toString(),
            fromId.toString(),
            toId.toString(),
            created.toString(),
            message,
            state.toString()
        );
    }

    /**
     * The raw data for a message as received by the API.
     * @param id Message UUID
     * @param fromId Sending user's UUID
     * @param toId Receiving user's UUID
     * @param created Timestamp for when the message was sent
     * @param message Message content
     * @param state Message read state
     */
    public static record RawDm(
        @JsonProperty("id") @NotNull String id,
        @JsonProperty("fromId") @NotNull String fromId,
        @JsonProperty("toId") @NotNull String toId,
        @JsonProperty("created") @NotNull String created,
        @JsonProperty("message") @NotNull String message,
        @JsonProperty("state") @NotNull String state) {}

    /**
     * Representation of a message's state as sent by the API.
     */
    public static enum MessageState {
        /**
         * Read by the receiving user
         */
        READ,

        /**
         * Not yet read
         */
        UNREAD
    }
}
