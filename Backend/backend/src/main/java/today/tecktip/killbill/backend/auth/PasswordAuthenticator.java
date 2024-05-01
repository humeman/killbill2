package today.tecktip.killbill.backend.auth;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import today.tecktip.killbill.backend.db.authdetails.AuthDetail;
import today.tecktip.killbill.backend.db.authdetails.AuthDetails;
import today.tecktip.killbill.backend.db.keys.Key;
import today.tecktip.killbill.backend.db.keys.Keys;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.db.users.Users;
import today.tecktip.killbill.backend.exceptions.AuthenticationFailure;
import today.tecktip.killbill.backend.exceptions.InvalidArgumentException;
import today.tecktip.killbill.backend.exceptions.NotFoundException;
import today.tecktip.killbill.backend.exceptions.TransientServerError;

/**
 * Static helper methods for getting tokens from a username and password.
 * @author cs
 */
@Controller
public class PasswordAuthenticator {
    /**
     * Encodes passwords with Argon 2.
     */
    private static final PasswordEncoder ENCODER = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    /**
     * Maximum permissible valid keys per user at once.
     */
    private static final int MAX_SESSIONS_PER_USER = 10;

    /**
     * Authenticates a user with a username and password. Alias to {@link #authenticate(UUID, String)}.
     * @param username Username
     * @param password Password
     * @return Generated key for this session
     * @throws AuthenticationFailure Not authorized
     * @throws TransientServerError Unable to contact database
     */
    public static Key authenticate(final String username, final String password) {
        // Search for the specified user
        User user;
        try {
            user = Users.getUserByName(username);
        } catch (NotFoundException e) {
            throw new AuthenticationFailure("Invalid username or password.");
        } catch (SQLException e) {
            System.err.println(e);
            throw new TransientServerError("Failed to contact database.");
        }

        return authenticate(user.id(), password);
    }

    /**
     * Authenticates a user with a password. Alias to {@link #authenticate(UUID, String)}.
     * @param user User to authenticate
     * @param password Password
     * @return Generated key for this session
     * @throws AuthenticationFailure Not authorized
     * @throws TransientServerError Unable to contact database
     */
    public static Key authenticate(final User user, final String password) {
        return authenticate(user.id(), password);
    }

    /**
     * Authenticates a user with a user ID and password.
     * @param userId ID of user to authenticate
     * @param password Password
     * @return Generated key for this session
     * @throws AuthenticationFailure Not authorized
     * @throws TransientServerError Unable to contact database
     */
    public static Key authenticate(final UUID userId, final String password) {
        AuthDetail pw;
        try {
            pw = AuthDetails.getAuthDetail(userId);
        } catch (NotFoundException e) {
            throw new AuthenticationFailure("Invalid username or password.");
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        if (!ENCODER.matches(password, pw.password())) {
            throw new AuthenticationFailure("Invalid username or password.");
        }

        try {
            // Find active sessions
            if (Keys.keyCount(userId) > MAX_SESSIONS_PER_USER) {
                throw new AuthenticationFailure("Too many active sessions. Sign out of another device to continue.");
            }
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        // Generate a key
        try {
            return Authenticator.createKey(userId);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }
    }

    /**
     * Sets the user's password, invalidates all keys, then generates a new key.
     * @param authDetail Auth detail to update
     * @param password New password
     * @return New Key to use
     * @throws TransientServerError Unable to contact database
     */
    public static Key setPassword(final AuthDetail authDetail, final String password) throws TransientServerError {
        // Update password
        AuthDetail detail = new AuthDetail(
            authDetail.userId(),
            Instant.now(),
            ENCODER.encode(password),
            authDetail.email()
        );
        try {
            AuthDetails.putAuthDetail(detail);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.");
        }

        // Invalidate all keys
        try {
            Keys.deleteKeys(authDetail.userId());
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.");
        }

        // Create a new key
        try {
            return Authenticator.createKey(authDetail.userId());
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.");
        }
    }

    /**
     * Sets the user's email.
     * @param authDetail Auth detail to update
     * @param email New email
     * @throws TransientServerError Unable to contact database
     */
    public static void setEmail(final AuthDetail authDetail, final String email) throws TransientServerError {
        // Update email
        AuthDetail detail = new AuthDetail(
            authDetail.userId(),
            Instant.now(),
            authDetail.password(),
            ENCODER.encode(email)
        );
        try {
            AuthDetails.putAuthDetail(detail);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.");
        }
    }

    /**
     * Sets the user's email and password. Helpful for creating new users, and shouldn't be touched if they already exist.
     * @param userId User's ID
     * @param email New email
     * @param password New email
     * @return New Key to use
     * @throws TransientServerError Unable to contact database
     */
    public static Key setEmailAndPassword(final UUID userId, final String email, final String password) throws TransientServerError {
        // Create detail
        AuthDetail detail = new AuthDetail(
            userId,
            Instant.now(),
            ENCODER.encode(password),
            ENCODER.encode(email)
        );
        try {
            AuthDetails.putAuthDetail(detail);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.");
        }

        // Invalidate all other keys (shouldn't exist, but just to be sure)
        try {
            Keys.deleteKeys(userId);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.");
        }

        // Create a new key
        try {
            return Authenticator.createKey(userId);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.");
        }
    }

    /**
     * Validates that a password is sufficiently secure.
     * <p>
     * TODO: Make this more strict
     * @param password Password to check
     * @throws InvalidArgumentException Password does not meet requirements
     */
    public static void validatePassword(final String password) {
        if (password.length() < 8) {
            throw new InvalidArgumentException("Password must be at least 8 characters long");
        }
    }

	/**
	 * This class should not be manually instantiated.
	 */
	public PasswordAuthenticator() { }
}
