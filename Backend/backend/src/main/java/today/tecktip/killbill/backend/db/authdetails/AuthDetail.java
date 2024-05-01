package today.tecktip.killbill.backend.db.authdetails;

import java.time.Instant;
import java.util.UUID;

/**
 * Data structure for a user's email and password as retrieved from the SQL database.
 * <p>
 * This is kept intentionally separate from the User table to reduce the risk that
 * any of this information is unintentionally sent in a user-facing request. A password
 * should never be used outside of the {@link today.tecktip.killbill.backend.auth.PasswordAuthenticator}.
 * @param userId UUID for this user
 * @param updated When the password was last updated
 * @param password Password (passed through Argon 2)
 * @param email Email address (passed through Argon 2)
 */
public record AuthDetail(
    UUID userId,
    Instant updated,
    String password,
    String email
) {}
