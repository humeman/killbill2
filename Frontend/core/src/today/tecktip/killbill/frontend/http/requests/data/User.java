package today.tecktip.killbill.frontend.http.requests.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;

/**
 * Record representing a user retrieved from the API.
 * @param id User's UUID
 * @param created Timestamp for when the user was created
 * @param name User's name
 * @param role User's role
 * @author cs
 */
public record User(
    @NotNull UUID id,
    @NotNull Instant created, 
    @NotNull String name, 
    @NotNull UserRole role,
    @NotNull Long winsAsBill,
    @NotNull Long winsAsPlayer,
    @NotNull Long playtime) {
    
    /**
     * Parses a raw API data-type user into a regular User.
     * @param rawUser Raw user response from API
     * @return Parsed regular User
     */
    public static User fromRaw(final RawUser rawUser) {
        try {
            return new User(
                UUID.fromString(rawUser.id()),
                Instant.parse(rawUser.created()),
                rawUser.name(),
                UserRole.valueOf(rawUser.role()),
                rawUser.winsAsBill(),
                rawUser.winsAsPlayer(),
                rawUser.playtime()
            );
        } catch (final Throwable t) {
            throw new CatastrophicException("Unable to parse response from the API: ", t);
        }
    }

    /**
     * Returns a user to its raw API data-type.
     * @return Raw representation, sendable to the API
     */
    public RawUser toRaw() {
        return new RawUser(
            id.toString(),
            created.toString(),
            name,
            role.toString(),
            winsAsBill,
            winsAsPlayer,
            playtime
        );
    }

    /**
     * The raw data for a user as received by the API.
     * @param id User's UUID
     * @param created ISO timestamp for when the user was created
     * @param name User's name
     * @param role User's role
     */
    public static record RawUser(
        @JsonProperty("id") @NotNull String id,
        @JsonProperty("created") @NotNull String created,
        @JsonProperty("name") @NotNull String name,
        @JsonProperty("role") @NotNull String role,
        @JsonProperty("winsAsBill") @NotNull Long winsAsBill,
        @JsonProperty("winsAsPlayer") @NotNull Long winsAsPlayer,
        @JsonProperty("playtime") @NotNull Long playtime) {}

    /**
     * Representation of a user's roles as sent by the API.
     */
    public static enum UserRole {
        /**
         * Regular default user.
         */
        USER,

        /**
         * Administrator. Bypasses most permission checks.
         */
        ADMIN
    }
}
