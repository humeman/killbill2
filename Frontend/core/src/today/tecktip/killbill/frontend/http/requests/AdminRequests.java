package today.tecktip.killbill.frontend.http.requests;

import java.net.URISyntaxException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.http.NativeHttpClient.ResponseErrorHandler;
import today.tecktip.killbill.frontend.http.requests.data.User;
import today.tecktip.killbill.frontend.http.requests.data.User.RawUser;

/**
 * Handles requests to the admin endpoints.
 * <p>
 * <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/admin.md">Documentation</a>
 * @author cs
 */
public class AdminRequests {
    /**
     * Raw response body for the create admin request.
     */
    private static record RawCreateAdminResponseBody(
            @JsonProperty("user") @NotNull RawUser user,
            @JsonProperty("key") @NotNull String key
    ) {}

    /**
     * Response body for the create admin request.
     */
    public static record CreateAdminResponseBody(
            @NotNull User user,
            @NotNull String key
    ) {}

    /**
     * Handler for the create admin request's response data.
     */
    public static interface CreateAdminResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final CreateAdminResponseBody body);
    }

    /**
     * Creates the default admin user if they don't exist already.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/admin.md">POST /admin/create_admin</a> asynchronously.
     * @see CreateAdminResponseBody
     * @param adminInitKey Admin init key as configured in backend
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void createAdmin(
        final String adminInitKey,
        final CreateAdminResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.post(
                "admin/create_admin", 
                null,
                Map.of("Authorization", "Bearer " + adminInitKey), 
                RawCreateAdminResponseBody.class, 
                response -> {
                    handler.handle(
                        new CreateAdminResponseBody(
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
     * Response body for the get admin key request.
     */
    public static record GetAdminKeyResponseBody(
            @JsonProperty("key") @NotNull String key
    ) {}

    /**
     * Handler for the get admin key request's response data.
     */
    public static interface GetAdminKeyResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final GetAdminKeyResponseBody body);
    }

    /**
     * Gets the admin key for the default admin user.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/admin.md">GET /admin/get_admin_key</a> asynchronously.
     * @see GetAdminKeyResponseBody
     * @param adminInitKey Admin init key as configured in backend
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void getAdminKey(
        final String adminInitKey,
        final GetAdminKeyResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "admin/get_admin_key", 
                Map.of(),
                Map.of("Authorization", "Bearer " + adminInitKey), 
                GetAdminKeyResponseBody.class, 
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
