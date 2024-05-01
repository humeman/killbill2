package today.tecktip.killbill.frontend.http.requests.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;

/**
 * Record representing a friend retrieved from the API.
 * @param fromId Sending user's UUID
 * @param toId Receiving user's UUID
 * @param created Timestamp for when the link was created
 * @param state Friend link state
 * @author cs
 */
public record Friend(
    @NotNull UUID fromId, 
    @NotNull UUID toId,
    @NotNull Instant created, 
    @NotNull FriendState state) {
    
    /**
     * Parses a raw API data-type friend into a regular Friend.
     * @param rawFriend Raw friend response from API
     * @return Parsed regular Friend
     */
    public static Friend fromRaw(final RawFriend rawFriend) {
        try {
            return new Friend(
                UUID.fromString(rawFriend.fromId()),
                UUID.fromString(rawFriend.toId()),
                Instant.parse(rawFriend.created()),
                FriendState.valueOf(rawFriend.state())
            );
        } catch (final Throwable t) {
            throw new CatastrophicException("Unable to parse response from the API: ", t);
        }
    }

    /**
     * Returns a friend to its raw API data-type.
     * @return Raw representation, sendable to the API
     */
    public RawFriend toRaw() {
        return new RawFriend(
            fromId.toString(),
            toId.toString(),
            created.toString(),
            state.toString()
        );
    }

    /**
     * The raw data for a user as received by the API.
     * @param fromId Sending user's UUID
     * @param toId Receiving user's UUID
     * @param created Timestamp for when the link was created
     * @param state Friend link state
     */
    public static record RawFriend(
        @JsonProperty("fromId") @NotNull String fromId,
        @JsonProperty("toId") @NotNull String toId,
        @JsonProperty("created") @NotNull String created,
        @JsonProperty("state") @NotNull String state) {}

    /**
     * Representation of a friend link's state as sent by the API.
     */
    public static enum FriendState {
        /**
         * 'from' has invited 'to', but 'to' has not accepted.
         */
        INVITED,

        /**
         * The users are friends.
         */
        FRIENDS,

        /**
         * 'from' has blocked 'to'.
         */
        BLOCKED
    }
}
