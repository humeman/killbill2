package today.tecktip.killbill.frontend.http.requests;

import java.net.URISyntaxException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.http.NativeHttpClient.ResponseErrorHandler;

/**
 * Handles requests to the authentication endpoints.
 * <p>
 * <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/auth.md">Documentation</a>
 * @author cs
 */
public class AuthRequests {

    /**
     * Response body for the test key request.
     */
    public static record TestKeyResponseBody(
            @JsonProperty("name") @NotNull String name,
            @JsonProperty("version") @NotNull String version
    ) {}

    /**
     * Handler for the test key request's response data.
     */
    public static interface TestKeyResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final TestKeyResponseBody body);
    }

    /**
     * Tests an authentication key for validity.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/auth.md">GET /auth</a> asynchronously.
     * @see TestKeyResponseBody
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void testKey(
        final TestKeyResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "auth", 
                Map.of(),
                client.getAuthenticationHeaders(), 
                TestKeyResponseBody.class, 
                response -> {
                    handler.handle(response.getData());
                },
                errorHandler
            );
        } catch (final URISyntaxException e) {
            throw new CatastrophicException("Invalid URI.", e);
        }
    }

    /**
     * Response body for the sign in request.
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static record SignInRequestBody(
            @NotNull String username,
            @NotNull String password
    ) {}

    /**
     * Response body for the sign in request.
     */
    public static record SignInResponseBody(
            @JsonProperty("key") @NotNull String key
    ) {}

    /**
     * Handler for the sign in request's response data.
     */
    public static interface SignInResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final SignInResponseBody body);
    }

    /**
     * Signs in a user. The key is not assigned to the HTTP client automatically.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/auth.md">POST /auth</a> asynchronously.
     * @see SignInResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void signIn(
        final SignInRequestBody body,
        final SignInResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.post(
                "auth", 
                body,
                Map.of(), 
                SignInResponseBody.class, 
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

    /**
     * Response body for the sign out request.
     * @param all If true, all keys are invalidated
     */
    public static record SignOutRequestBody(
            boolean all
    ) {}

    /**
     * Response body for the sign out request.
     */
    public static record SignOutResponseBody() {}

    /**
     * Handler for the sign out request's response data.
     */
    public static interface SignOutResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final SignOutResponseBody body);
    }

    /**
     * Invalidates the provided authentication key (signs a user out). Providing a request body of true will invalidate all keys for the user.
     * The key is not removed from the HTTP client automatically.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/auth.md">DELETE /auth</a> asynchronously.
     * @see SignInResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void signOut(
        final SignOutRequestBody body,
        final SignOutResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();


        try {
            client.delete(
                "auth", 
                Map.of(
                        "all", String.valueOf(body.all)
                ),
                client.getAuthenticationHeaders(),
                SignOutResponseBody.class,
                response -> {
                    client.setApiKey(null);
                    handler.handle(response.getData());
                },
                errorHandler
            );
        } catch (final URISyntaxException e) {
            throw new CatastrophicException("Invalid URI.", e);
        }
    }

    /**
     * Invalidates the current authentication key (signs a user out). The key is not removed from the HTTP client automatically.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/auth.md">DELETE /auth</a> asynchronously.
     * @see SignInResponseBody
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void signOut(
        final SignOutResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        signOut(new SignOutRequestBody(false), handler, errorHandler);
    }

    /**
     * Response body for the change password request.
     * @param newPassword New password
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static record ChangePasswordRequestBody(
        @NotNull String newPassword
    ) {}

    /**
     * Response body for the change password request.
     */
    public static record ChangePasswordResponseBody(
        @JsonProperty("key") String key
    ) {}

    /**
     * Handler for the change password request's response data.
     */
    public static interface ChangePasswordResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final ChangePasswordResponseBody body);
    }

    /**
     * Changes a user's password, retrieving a new API key.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/auth.md">PUT /auth/password</a> asynchronously.
     * @see ChangePasswordResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void changePassword(
        final ChangePasswordRequestBody body,
        final ChangePasswordResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) throws JsonProcessingException {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.put(
                "auth/password", 
                body,
                client.getAuthenticationHeaders(),
                ChangePasswordResponseBody.class,
                response -> {
                    handler.handle(response.getData());
                },
                errorHandler
            );
        } catch (final URISyntaxException e) {
            throw new CatastrophicException("Invalid URI.", e);
        }
    }

    /**
     * Response body for the change email request.
     * @param email New email
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static record ChangeEmailRequestBody(
        @NotNull String email
    ) {}

    /**
     * Response body for the change password request.
     */
    public static record ChangeEmailResponseBody() {}

    /**
     * Handler for the change password request's response data.
     */
    public static interface ChangeEmailResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final ChangeEmailResponseBody body);
    }

    /**
     * Changes a user's email.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/auth.md">PUT /auth/email</a> asynchronously.
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void changeEmail(
        final ChangeEmailRequestBody body,
        final ChangeEmailResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) throws JsonProcessingException {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.put(
                "auth/email", 
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
        }
    }
}

