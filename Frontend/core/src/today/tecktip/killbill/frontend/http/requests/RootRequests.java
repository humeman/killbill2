package today.tecktip.killbill.frontend.http.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URISyntaxException;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.http.NativeHttpClient.ResponseErrorHandler;

/**
 * Handles requests to the root endpoints.
 * <p>
 * <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/root.md">Documentation</a>
 * @author cs
 */
public class RootRequests {

    /**
     * Response body for the root request.
     */
    public static record StatusResponseBody(
            @JsonProperty("name") @NotNull String name,
            @JsonProperty("version") @NotNull String version
    ) {}

    /**
     * Handler for the root request's response data.
     */
    public static interface StatusResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final StatusResponseBody body);
    }

    /**
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/root.md">GET /</a> asynchronously.
     * @see StatusResponseBody
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void getStatus(
        final StatusResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "", 
                Map.of(),
                client.getAuthenticationHeaders(), 
                StatusResponseBody.class, 
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
