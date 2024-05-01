package today.tecktip.killbill.backend.routes.admin;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import today.tecktip.killbill.backend.auth.Authenticator;
import today.tecktip.killbill.backend.db.keys.Key;
import today.tecktip.killbill.backend.db.keys.Keys;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.db.users.UserRole;
import today.tecktip.killbill.backend.db.users.Users;
import today.tecktip.killbill.backend.exceptions.AuthenticationFailure;
import today.tecktip.killbill.backend.exceptions.InvalidArgumentException;
import today.tecktip.killbill.backend.exceptions.NotFoundException;
import today.tecktip.killbill.backend.exceptions.ServerError;
import today.tecktip.killbill.backend.exceptions.TransientServerError;
import today.tecktip.killbill.backend.routes.MessageBody;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.validation.constraints.NotNull;

/**
 * Routes for managing admin stuff.
 * 
 * @author cs
 */
@RestController
@RequestMapping("/admin")
public class AdminRoutes {
    /**
     * Env var for the Admin Init Key.
     * <p>
     * Used in the initialization of the API to generate the first admin account, and recover the key if necessary.
     */
    @Value("${env_vars.admin_init_key}")
    private String ADMIN_INIT_KEY;

    /**
     * Response body for the creation of a new admin user.
     */
    private record CreateUserResponseBody(@NotNull User user, @NotNull String key) {}

    /**
     * Creates the default admin user user.
     * <p>
     * Requires the ADMIN_INIT_KEY for authentication.
     * 
     * @param authToken Authorization header.
     * @return User and key
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException User already exists
     * @throws AuthenticationFailure Invalid token or permissions
     * @throws ServerError Failed to generate key
     */
    @PostMapping("/create_admin")
    public MessageBody createAdmin(
            @RequestHeader(value = "Authorization", required = true) String authToken
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure, ServerError {
        // Manually verify authentication token
        if (!authToken.startsWith("Bearer ")) {
            throw new AuthenticationFailure("Key must be in format `Bearer ADMIN_INIT_KEY`.");
        }

        String token = authToken.split(" ", 2)[1];

        if (!System.getenv("ADMIN_INIT_KEY").equals(token)) {
            throw new AuthenticationFailure("Invalid admin init key.");
        }

        // Check if admin already exists
        try {
            Users.getUserByName("admin");
            // Success? That's bad
            throw new InvalidArgumentException("Admin user already exists.");
        } catch (NotFoundException e) {}
        catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        // Generate the user
        User user = new User(
            UUID.randomUUID(),
            Instant.now(),
            "admin",
            UserRole.ADMIN,
            0L,
            0L,
            0L
        );

        // Generate a key
        Key key;
        try {
            key = Authenticator.createKey(user);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        // Send out to database
        try {
            Users.putUser(user);
            Keys.putKey(key);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to send new user to database.", e);
        }

        try {
            return MessageBody.ofSuccess(new CreateUserResponseBody(user, key.toBase64()));
        } catch (JsonProcessingException e) {
            throw new ServerError("Failed to serialize new key as JSON.", e);
        }
    }

    /**
     * Response body for the default admin key.
     */
    private record GetAdminKeyResponseBody(@NotNull String key) {}

    /**
     * Retrieves the default admin user's key.
     * <p>
     * Requires the ADMIN_INIT_KEY for authentication.
     * 
     * @param authToken Authorization header.
     * @return Key
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException User doesn't exist
     * @throws AuthenticationFailure Invalid token or permissions
     * @throws ServerError Failed to generate key
     */
    @GetMapping("/get_admin_key")
    public MessageBody getAdminKey(
            @RequestHeader(value = "Authorization", required = true) String authToken
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure, ServerError {
        // Manually verify authentication token
        if (!authToken.startsWith("Bearer ")) {
            throw new AuthenticationFailure("Key must be in format `Bearer ADMIN_INIT_KEY`.");
        }

        String token = authToken.split(" ", 2)[1];

        if (!ADMIN_INIT_KEY.equals(token)) {
            throw new AuthenticationFailure("Invalid admin init key.");
        }

        User admin;
        try {
            admin = Users.getUserByName("admin");
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Admin user doesn't exist yet.");
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        // Generate a key
        Key key;
        try {
            key = Authenticator.createKey(admin);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        try {
            return MessageBody.ofSuccess(new GetAdminKeyResponseBody(key.toBase64()));
        } catch (JsonProcessingException e) {
            throw new ServerError("Failed to serialize key as JSON.", e);
        }
    }
    
    /**
	 * This class should not be instantiated manually.
	 */
	public AdminRoutes() { }
}
