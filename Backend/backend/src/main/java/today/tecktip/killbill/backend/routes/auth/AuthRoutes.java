package today.tecktip.killbill.backend.routes.auth;

import java.sql.SQLException;

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
import today.tecktip.killbill.backend.db.authdetails.AuthDetails;
import today.tecktip.killbill.backend.db.keys.Key;
import today.tecktip.killbill.backend.db.keys.Keys;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.exceptions.AuthenticationFailure;
import today.tecktip.killbill.backend.exceptions.InvalidArgumentException;
import today.tecktip.killbill.backend.exceptions.ServerError;
import today.tecktip.killbill.backend.exceptions.TransientServerError;
import today.tecktip.killbill.backend.routes.MessageBody;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Routes for authentication.
 * 
 * @author cs
 */
@RestController
@RequestMapping("/auth")
public class AuthRoutes {
    /**
     * Simple method which can be used to validate a user's auth token.
     * 
     * @param authToken Authorization header
     * @return Empty body
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException No users are registered
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @GetMapping("")
    public MessageBody listUsers(
            @RequestHeader(value = "Authorization", required = true) String authToken
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        Authenticator.getAuthenticatedUser(key);

        return MessageBody.ofSuccess(null);
    }
    
    /**
     * A response body for an API key.
     * @param users List of users retrieved
     */
    private record APIKeyResponseBody(String key) {}

    /**
     * A request body for a sign-in request.
     * @param username Username
     * @param password Password
     */
    private record SignInRequestBody(@NotNull String username, @NotNull String password) {}

    /**
     * Signs in a user.
     * @param request Request body.
     * @return New API key
     * @throws TransientServerError Unable to contact database
     * @throws AuthenticationFailure Invalid username or password
     */
    @PostMapping("")
    public MessageBody signIn(
            @Valid @RequestBody(required = true) SignInRequestBody request
        ) throws TransientServerError, AuthenticationFailure {
        // Try to authenticate
        Key key = PasswordAuthenticator.authenticate(request.username(), request.password());

        try {
            return MessageBody.ofSuccess(new APIKeyResponseBody(key.toBase64()));
        } catch (JsonProcessingException e) {
            throw new ServerError("Failed to serialize key as JSON.", e);
        }
    }

    /**
     * Signs out a user.
     * @param authToken Authorization header
     * @param all If specified and true, deletes all keys registered to the user.
     * @return Empty body
     * @throws TransientServerError Unable to contact database
     * @throws AuthenticationFailure Not authorized to clear keys
     */
    @DeleteMapping("")
    public MessageBody signOut(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam(required = false) Boolean all
        ) throws TransientServerError, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        
        try {
            if (all) {
                Keys.deleteKeys(key.userId());
            } else {
                Keys.deleteKey(key);
            }
        } catch (final SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        return MessageBody.ofSuccess(null);
    }

    /**
     * A request body for an email-change request.
     * @param email Email address
     */
    private record ChangeEmailRequestBody(@NotNull String email) {}

    /**
     * Updates a user's email.
     * @param authToken Authorization header
     * @param request Request body
     * @return Empty body
     * @throws TransientServerError Unable to contact database
     * @throws AuthenticationFailure Not authorized to change email
     */
    @PutMapping("/email")
    public MessageBody updateEmail(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @Valid @RequestBody(required = true) ChangeEmailRequestBody request
        ) throws TransientServerError, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);
        
        try {
            PasswordAuthenticator.setEmail(AuthDetails.getAuthDetail(authenticatedUser.id()), request.email());
        } catch (final SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        return MessageBody.ofSuccess(null);
    }

    /**
     * A request body for a password-change request.
     * @param newPassword New password
     */
    private record ChangePasswordRequestBody(@NotNull String newPassword) {}

    /**
     * Updates a user's password.
     * @param authToken Authorization header
     * @param request Request body
     * @return New API key
     * @throws TransientServerError Unable to contact database
     * @throws AuthenticationFailure Not authorized to change password
     */
    @PutMapping("/password")
    public MessageBody updatePassword(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @Valid @RequestBody(required = true) ChangePasswordRequestBody request
        ) throws TransientServerError, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        Key newKey;
        try {
            newKey = PasswordAuthenticator.setPassword(AuthDetails.getAuthDetail(authenticatedUser.id()), request.newPassword());
        } catch (final SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        try {
            return MessageBody.ofSuccess(new APIKeyResponseBody(newKey.toBase64()));
        } catch (JsonProcessingException e) {
            throw new ServerError("Failed to serialize key as JSON.", e);
        }
    }

	/**
	 * This class should not be instantiated manually.
	 */
	public AuthRoutes() { }
}
