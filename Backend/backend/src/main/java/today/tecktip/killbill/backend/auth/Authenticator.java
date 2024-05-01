package today.tecktip.killbill.backend.auth;

import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.time.Duration;

import today.tecktip.killbill.backend.db.keys.Key;
import today.tecktip.killbill.backend.db.keys.Keys;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.db.users.UserRole;
import today.tecktip.killbill.backend.db.users.Users;
import today.tecktip.killbill.backend.exceptions.AuthenticationFailure;
import today.tecktip.killbill.backend.exceptions.NotFoundException;
import today.tecktip.killbill.backend.exceptions.TransientServerError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * Static helper methods for authenticating requests.
 * @author cs
 */
@Controller
public class Authenticator {
	/**
	 * The duration, in seconds, after which keys expire.
	 * <p>
	 * Set via the KEY_EXPIRATION_SECONDS env var. Auto-defined when using Gradle.
	 */
    private static Duration KEY_EXPIRATION_DURATION;

    /**
     * Used by Spring Boot to store the key expiration seconds env var.
     * @param keyExpirationSeconds KEY_EXPIRATION_SECONDS env var
     */
    @Value("${env_vars.key_expiration_seconds}")
    private void setKeyExpirationSeconds(final String keyExpirationSeconds) {
        KEY_EXPIRATION_DURATION = Duration.ofSeconds(Integer.parseInt(keyExpirationSeconds));
    }

    /**
     * Body of a user's authentication key.
     * @param userId User ID
     * @param keyId User's key
     */
    public static record AuthenticationBody(UUID userId, UUID keyId) {}

    /**
     * Used internally to read JSON encoded keys.
     */
    private static ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Requires authentication on an API request.
     * @param bearerToken Content of the <code>Authorization</code> header
     * @return Validated key body
     * @throws AuthenticationFailure Not authorized
     * @throws TransientServerError Unable to contact database
     */
    public static Key requireAuthentication(final String bearerToken) {
        AuthenticationBody body = decodeAuthenticationBody(bearerToken);

        // Confirm this with the server
        Key key;
        try {
            key = Keys.getKey(body.keyId(), body.userId());
        } catch (NotFoundException e) {
            throw new AuthenticationFailure("Invalid key.");
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.");
        }

        // Verify not expired
        if (key.expires().isBefore(Instant.now())) {
            throw new AuthenticationFailure("Invalid key.");
        }

        return key;
    }

    /**
     * Requires authentication on an API request only if the <code>Authorization</code> header is provided.
     * @param bearerToken Content of the <code>Authorization</code> header
     * @return Validated key body or <code>null</code>
     * @throws AuthenticationFailure Not authorized
     * @throws TransientServerError Unable to contact database
     */
    public static Key requireAuthenticationIfPresent(final String bearerToken) {
        if (bearerToken == null || bearerToken.isEmpty()) {
            return null;
        }

        return requireAuthentication(bearerToken);
    }

    /**
     * Requires that a user has a specific role.
     * @param user User to check
     * @param role Target role
     * @throws AuthenticationFailure Not authorized
     */
    public static void requireRole(final User user, final UserRole role) {
        if (!user.role().equals(role)) {
            throw new AuthenticationFailure("Missing required role: " + role);
        }
    }

    /**
     * Gets the {@link User} associated with an {@link Key}.
     * @param key User's authentication key.
     * @return User associated with this authentication body
     * @throws AuthenticationFailure No such user
     * @throws TransientServerError Unable to contact database
     */
    public static User getAuthenticatedUser(final Key key) {
        User user;
        try {
            user = Users.getUser(key.userId());
        } catch (NotFoundException e) {
            throw new AuthenticationFailure("Invalid key.");
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.");
        }

        return user;
    }

    /**
     * Generates a key for a user. Alias to {@link #createKey(UUID)}.
     * @param user User to generate key for
     * @return Generated key
     * @throws SQLException Unable to send key to database
     */
    public static Key createKey(final User user) throws SQLException {
        return createKey(user.id());
    }

    /**
     * Generates a key for a user.
     * @param userId ID of user to generate key for
     * @return Generated key
     * @throws SQLException Unable to send key to database
     */
    public static Key createKey(final UUID userId) throws SQLException {
        Key key = new Key(
            UUID.randomUUID(),
            userId,
            Instant.now(),
            Instant.now().plus(KEY_EXPIRATION_DURATION)
        );

        Keys.putKey(key);

        return key;
    }

    /**
     * Decodes an <code>Authorization</code> header into an {@link AuthenticationBody}, if possible.
     * @param bearerToken Content of the <code>Authorization</code> header
     * @return Decoded content of the <code>Authorization</code> header
     * @throws AuthenticationFailure Couldn't parse header
     */
    private static AuthenticationBody decodeAuthenticationBody(String bearerToken) {
        if (bearerToken == null) throw new AuthenticationFailure("Authorization is required for this method.");
        
        if (!bearerToken.startsWith("Bearer ")) {
            throw new AuthenticationFailure("Key must be in format `Bearer BASE64_STR`.");
        }

        String token = new String(Base64.getDecoder().decode(bearerToken.split(" ", 2)[1]));

        try {
            JsonNode node = MAPPER.readTree(token);
            JsonNode userId = node.get("userId");
            if (userId == null || userId.isMissingNode() || !JsonNodeType.STRING.equals(userId.getNodeType())) throw new AuthenticationFailure("Required field: userId (string)");
            JsonNode keyId = node.get("keyId");
            if (keyId == null || keyId.isMissingNode() || !JsonNodeType.STRING.equals(keyId.getNodeType())) throw new AuthenticationFailure("Required field: keyId (string)");

            UUID uid = UUID.fromString(userId.asText());
            UUID ukey = UUID.fromString(keyId.asText());
            return new AuthenticationBody(uid, ukey);
        }
        catch (JsonProcessingException | IllegalArgumentException e) {
            System.err.println(e);
            throw new AuthenticationFailure("Invalid key.");
        }
    }

	/**
	 * This class should not be manually instantiated.
	 */
	public Authenticator() { }
}
