package today.tecktip.killbill.backend.db.keys;

import java.util.UUID;
import java.time.Instant;

import org.apache.tomcat.util.codec.binary.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.backend.auth.Authenticator.AuthenticationBody;

/**
 * Data structure for a user's key as retrieved from the SQL database.
 * @param id Key
 * @param userId UUID for the user associated with this key
 * @param created Timestamp for when the key was created
 * @param expires Timestamp for when the key expires
 */
public record Key(
    UUID id,
    UUID userId,
    Instant created,
    Instant expires
) {
    /**
     * Used internally to map keys into a Base64 authorization string.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Translates this key into a Base64 key compatible with the Authorization header.
     * @return Base64 key
     * @throws JsonProcessingException Unable to serialize as JSON
     */
    public String toBase64() throws JsonProcessingException {
        return Base64.encodeBase64String(MAPPER.writeValueAsString(new AuthenticationBody(userId, id)).getBytes());
    }
}
