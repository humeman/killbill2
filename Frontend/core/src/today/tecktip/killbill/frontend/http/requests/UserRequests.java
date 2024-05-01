package today.tecktip.killbill.frontend.http.requests;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.http.NativeHttpClient.ResponseErrorHandler;
import today.tecktip.killbill.frontend.http.requests.data.User;
import today.tecktip.killbill.frontend.http.requests.data.User.RawUser;
import today.tecktip.killbill.frontend.http.requests.data.User.UserRole;

/**
 * Handles requests to the user endpoints.
 * <p>
 * <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/users.md">Documentation</a>
 * @author cs
 */
public class UserRequests {

    /**
     * Request body for the get user request.
     * If neither id or name is supplied, the authenticated user is retrieved.
     * @param id (optional): UUID of the user to retrieve.
     * @param name (optional): Case-sensitive name of the user to retrieve.
     */
    public static record GetUserRequestBody(
            @NotNull UUID id,
            @NotNull String name
    ) {}

    /**
     * Raw response body for the get user request.
     * @param user The raw user body.
     */
    private static record RawGetUserResponseBody(
            @JsonProperty("user") @NotNull RawUser user
    ) {}

    /**
     * Response body for the get user request.
     * @param user The user retrieved from the API.
     */
    public static record GetUserResponseBody(
            @NotNull User user
    ) {}

    /**
     * Handler for the get user request's response data.
     */
    public static interface GetUserResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final GetUserResponseBody body);
    }

    /**
     * Searches for a user by username or ID.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/user.md">GET /users</a> asynchronously.
     * @see GetUserResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void getUser(
        final GetUserRequestBody body,
        final GetUserResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        HashMap<String, String> args = new HashMap<>();
        if (body.id() != null) args.put("id", body.id().toString());
        if (body.name() != null) args.put("name", body.name());

        try {
            client.get(
                "users",
                args,
                client.getAuthenticationHeaders(), 
                RawGetUserResponseBody.class, 
                response -> {
                    handler.handle(
                        new GetUserResponseBody(
                            User.fromRaw(response.getData().user())
                        )
                    );
                },
                errorHandler
            );
        } catch (final URISyntaxException e) {
            throw new CatastrophicException("Invalid URI.", e);
        }
    }

    /**
     * Gets the authenticated user.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/user.md">GET /users</a> asynchronously.
     * @see GetUserResponseBody
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void getAuthenticatedUser(
        final GetUserResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        getUser(new GetUserRequestBody(null, null), handler, errorHandler);
    }

    /**
     * Raw response body for the list users request.
     * @param users The list of raw user bodies.
     */
    private static record RawListUsersResponseBody(
            @JsonProperty("users") @NotNull List<RawUser> users
    ) {}

    /**
     * Response body for the list users request.
     * @param users The list of users.
     */
    public static record ListUsersResponseBody(
            @NotNull List<User> users
    ) {}

    /**
     * Handler for the list users request's response data.
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static interface ListUsersResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final ListUsersResponseBody body);
    }

    /**
     * Gets a list of all users registered.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/user.md">GET /users/list</a> asynchronously.
     * @see ListUsersResponseBody
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void listUsers(
        final ListUsersResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "users/list", 
                null,
                client.getAuthenticationHeaders(), 
                RawListUsersResponseBody.class,
                response -> {
                    handler.handle(
                        new ListUsersResponseBody(
                            response.getData().users().stream()
                                .map(User::fromRaw)
                                .toList()
                        )
                    );
                },
                errorHandler
            );
        } catch (final URISyntaxException e) {
            throw new CatastrophicException("Invalid URI.", e);
        }
    }    

    /**
     * Request body for the create user request.
     * @param username The user's username. 3-20 characters, alphanumeric and _.
     * @param email A valid email address for password resets.
     * @param password The user's password. >8 characters and must meet complexity requirements.
     * @param role The role type to assign to the user. Defaults to USER.
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static record CreateUserRequestBody(
            @NotNull String username,
            @NotNull @Email String email,
            @NotNull String password,
            UserRole role
    ) {}

    /**
     * Raw response body for the create user request.
     * @param user The raw user body.
     * @param key An initial user authentication key to use for future requests.
     */
    private static record RawCreateUserResponseBody(
            @JsonProperty("user") @NotNull RawUser user,
            @JsonProperty("key") @NotNull String key
    ) {}

    /**
     * Response body for the create user request.
     * @param user The user that was created.
     * @param key An initial user authentication key to use for future requests.
     */
    public static record CreateUserResponseBody(
            @NotNull User user,
            @NotNull String key
    ) {}

    /**
     * Handler for the create user request's response data.
     */
    public static interface CreateUserResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final CreateUserResponseBody body);
    }

    /**
     * Creates a new user.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/user.md">POST /users</a> asynchronously.
     * @see CreateUserResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void createUser(
        final CreateUserRequestBody body,
        final CreateUserResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.post(
                "users",
                body,
                client.getAuthenticationHeaders(), 
                RawCreateUserResponseBody.class, 
                response -> {
                    handler.handle(
                        new CreateUserResponseBody(
                            User.fromRaw(response.getData().user()),
                            response.getData().key()
                        )
                    );
                },
                errorHandler
            );
        } catch (final URISyntaxException e) {
            throw new CatastrophicException("Invalid URI.", e);
        } catch (final JsonProcessingException e) {
            throw new CatastrophicException("Failed to serialize body as JSON.", e);
        } 
    }

    /**
     * Request body for the delete user request.
     * @param id UUID of the user to be deleted.
     */
    public static record DeleteUserRequestBody(
            @NotNull UUID id
    ) {}

    /**
     * Raw response body for the delete user request.
     * @param user The raw user body.
     */
    private static record RawDeleteUserResponseBody(
            @JsonProperty("user") @NotNull RawUser user
    ) {}

    /**
     * Response body for the delete user request.
     * @param user The user deleted from the API.
     */
    public static record DeleteUserResponseBody(
            @NotNull User user
    ) {}

    /**
     * Handler for the delete user request's response data.
     */
    public static interface DeleteUserResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final DeleteUserResponseBody body);
    }

    /**
     * Deletes a user.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/user.md">DELETE /users</a> asynchronously.
     * @see DeleteUserResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void deleteUser(
        final DeleteUserRequestBody body,
        final DeleteUserResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.delete(
                "users",
                Map.of(
                    "id", body.id().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawDeleteUserResponseBody.class, 
                response -> {
                    handler.handle(
                        new DeleteUserResponseBody(
                            User.fromRaw(response.getData().user())
                        )
                    );
                },
                errorHandler
            );
        } catch (final URISyntaxException e) {
            throw new CatastrophicException("Invalid URI.", e);
        }
    }

    /**
     * Request body for the change username request.
     * @param username The user's username. 3-20 characters, alphanumeric and _.
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static record ChangeUsernameRequestBody(
            @NotNull String username
    ) {}

    /**
     * Response body for the delete user request.
     */
    public static record ChangeEmailResponseBody() {}

    /**
     * Handler for the create user request's response data.
     */
    public static interface ChangeUsernameResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final ChangeEmailResponseBody body);
    }

    /**
     * Changes a user's username.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/user.md">PUT /users/username</a> asynchronously.
     * @see ChangeUsernameRequestBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void changeUsername(
        final ChangeUsernameRequestBody body,
        final ChangeUsernameResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.put(
                "users/username",
                body,
                client.getAuthenticationHeaders(), 
                ChangeEmailResponseBody.class, 
                response -> {
                    handler.handle(response.getData());
                },
                errorHandler
            );
        } catch (final URISyntaxException e) {
            throw new CatastrophicException("Invalid URI.", e);
        } catch (final JsonProcessingException e) {
            throw new CatastrophicException("Failed to serialize body as JSON.", e);
        } 
    }
}
