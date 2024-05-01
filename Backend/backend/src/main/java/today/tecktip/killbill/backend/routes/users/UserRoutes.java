package today.tecktip.killbill.backend.routes.users;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import today.tecktip.killbill.backend.auth.Authenticator;
import today.tecktip.killbill.backend.auth.PasswordAuthenticator;
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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * Routes for managing users.
 * 
 * @author cs
 */
@RestController
@RequestMapping("/users")
public class UserRoutes {
    /**
     * A response body for listing users.
     * @param users List of users retrieved
     */
    private record GetUsersResponseBody(List<User> users) {}

    /**
     * Gets a list of all users.
     * <p>
     * Admin-only method.
     * 
     * @param authToken Authorization header
     * @return List of all users registered
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException No users are registered
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @GetMapping("/list")
    public MessageBody listUsers(
            @RequestHeader(value = "Authorization", required = true) String authToken
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);
        Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);

        List<User> users;
        try {
            users = Users.getUsers();
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("No users found.");
        }

        return MessageBody.ofSuccess(new GetUsersResponseBody(users));
    }

    /**
     * A response body for a single user.
     * @param user User retrieved
     */
    private record GetUserResponseBody(User user) {}

    /**
     * Gets a single user.
     * <p>
     * Authenticated method. User or admin.
     * 
     * @param authToken Authorization header
     * @param sid Optional ID for a user to search for
     * @param name Optional name for a user to search for
     * @return List of all users registered
     * @throws TransientServerError SQL contact failed
     * @throws ServerError User not found
     * @throws InvalidArgumentException Arguments not as expected
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @GetMapping("")
    public MessageBody getUser(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam(value = "id", required = false) String sid,
            @RequestParam(required = false) String name
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure, ServerError {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        UUID id = authenticatedUser.id();
        User user = null;
        if (sid != null) {
            try {
                id = UUID.fromString(sid);
                if (id == null) throw new Exception();
            } catch (Exception e) {
                throw new InvalidArgumentException("Unable to parse ID as UUID.");
            }
        }
        else if (name != null) {
            if (!UserValidator.isValidName(name)) {
                throw new InvalidArgumentException("Name is not of the expected format.");
            }
            try {
                user = Users.getUserByName(name);
            } catch (NotFoundException e) {
                throw new InvalidArgumentException("Search yielded no results for specified name.");
            } catch (SQLException e) {
                throw new TransientServerError("Failed to contact database.", e);
            }
        }

        try {
            if (user == null)
                user = Users.getUser(id);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("User not found.");
        }

        return MessageBody.ofSuccess(new GetUserResponseBody(user));
    }

    /**
     * Request body for user creation.
     * @param name Username
     * @param role User role
     */
    private record CreateUserRequestBody(UserRole role, @NotNull String username, @NotNull @Email String email, @NotNull String password) {}

    /**
     * Response body for user creation.
     * @param user Created user
     * @param key User's authentication key
     */
    private record CreateUserResponseBody(@NotNull User user, @NotNull String key) {}

    /**
     * Creates a user.
     * <p>
     * Admin auth required to create an admin user.
     * 
     * @param authToken Authorization header. Required only if creating an admin.
     * @param request User creation request body
     * @return User and key
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected
     * @throws AuthenticationFailure Invalid token or permissions
     * @throws ServerError Failed to generate key
     */
    @PostMapping("")
    public MessageBody createUser(
            @RequestHeader(value = "Authorization", required = false) String authToken, 
            @Valid @RequestBody(required = true) CreateUserRequestBody request
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure, ServerError {
        Key key = null;
        User authenticatedUser = null;
        if (authToken != null) {
            key = Authenticator.requireAuthentication(authToken);
            authenticatedUser = Authenticator.getAuthenticatedUser(key);
        }

        // Confirm request parameters
        if (!UserValidator.isValidName(request.username())) {
            throw new InvalidArgumentException("Usernames must be 3-20 characters and alphanumeric (plus underscores).");
        }

        try {
            if (UserValidator.nameIsTaken(request.username())) {
                throw new InvalidArgumentException("This name is taken.");
            }
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        UserRole targetRole = request.role() == null ? UserRole.USER : request.role();

        // Confirm authorization if creating an admin
        if (!UserRole.USER.equals(targetRole)) {
            // Verify authentication
            if (key == null) throw new AuthenticationFailure("Must be authenticated to create a non-regular user.");
            Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);
        }

        // Generate the user
        User user = new User(
            UUID.randomUUID(),
            Instant.now(),
            request.username(),
            targetRole,
            0L,
            0L,
            0L
        );

        // Validate user's details
        // Email is validated by Jakarta with the @Email annotation.
        PasswordAuthenticator.validatePassword(request.password());

        // Send out to database
        Key newKey;
        try {
            newKey = PasswordAuthenticator.setEmailAndPassword(user.id(), request.email(), request.password()); // Do this first so we don't end up with broken accounts if it fails
            Users.putUser(user);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to send new user/key pair to database.", e);
        }

        try {
            return MessageBody.ofSuccess(new CreateUserResponseBody(user, newKey.toBase64()));
        } catch (JsonProcessingException e) {
            throw new ServerError("Failed to serialize new key as JSON.", e);
        }
    }

    /**
     * Response body for user deletion.
     * @param user Deleted user
     */
    private record DeleteUserResponseBody(@NotNull User user) {}

    /**
     * Deletes a user.
     * <p>
     * Admin auth required to delete another user.
     * 
     * @param authToken Authorization header. Admin auth required only if sid is not the authenticated user.
     * @param sid ID of user to delete
     * @return Deleted user
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @DeleteMapping("")
    public MessageBody deleteUser(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @Valid @RequestParam(value = "id", required = true) String sid
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        // Get the user we want to delete
        UUID id;
        try {
            id = UUID.fromString(sid);
            if (id == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse ID as UUID.");
        }
        // Confirm ID matches or user is admin
        if (!authenticatedUser.id().equals(id) && !authenticatedUser.role().equals(UserRole.ADMIN)) {
            throw new AuthenticationFailure("Cannot delete another user.");
        }

        User user;
        try {
            user = Users.getUser(id);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Specified user does not exist.");
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }
        
        try {
            Users.deleteUser(id);
            Keys.deleteKeys(id);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        return MessageBody.ofSuccess(new DeleteUserResponseBody(user));
    }

    /**
     * Request body for username change.
     * @param username New username
     */
    private record ChangeUsernameRequestBody(@NotNull String username) {}

    /**
     * Creates a user.
     * <p>
     * Admin auth required to create an admin user.
     * 
     * @param authToken Authorization header. Required only if creating an admin.
     * @param request User creation request body
     * @return User and key
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected
     * @throws AuthenticationFailure Invalid token or permissions
     * @throws ServerError Failed to generate key
     */
    @PutMapping("/username")
    public MessageBody changeUsername(
            @RequestHeader(value = "Authorization", required = false) String authToken, 
            @Valid @RequestBody(required = true) ChangeUsernameRequestBody request
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure, ServerError {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        // Confirm request parameters
        if (!UserValidator.isValidName(request.username())) {
            throw new InvalidArgumentException("Usernames must be 3-20 characters and alphanumeric (plus underscores).");
        }

        try {
            if (UserValidator.nameIsTaken(request.username())) {
                throw new InvalidArgumentException("This name is taken.");
            }
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }


        // Generate the user
        User newUser = new User(
            authenticatedUser.id(),
            authenticatedUser.created(),
            request.username(),
            authenticatedUser.role(),
            authenticatedUser.winsAsBill(),
            authenticatedUser.winsAsPlayer(),
            authenticatedUser.playtime()
        );


        // Send out to database
        try {
            Users.putUser(newUser);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to update user.", e);
        }

        return MessageBody.ofSuccess(null);
    }

	/**
	 * This class should not be instantiated manually.
	 */
	public UserRoutes() { }
}
