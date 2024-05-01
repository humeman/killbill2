package today.tecktip.killbill.frontend.http.requests;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.http.NativeHttpClient.ResponseErrorHandler;
import today.tecktip.killbill.frontend.http.requests.data.Dm;
import today.tecktip.killbill.frontend.http.requests.data.Dm.RawDm;

/**
 * Handles requests to the DM endpoints.
 * <p>
 * <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/dm.md">Documentation</a>
 * @author cs
 */
public class DmRequests {
    
    /**
     * Request body for the get DM request.
     */
    public static record GetDmRequestBody(
            @NotNull UUID messageId
    ) {}

    /**
     * Raw response body for the get DM request.
     */
    private static record RawGetDmResponseBody(
            @JsonProperty("dm") @NotNull RawDm dm
    ) {}

    /**
     * Response body for the get DM request.
     */
    public static record GetDmResponseBody(
            @NotNull Dm dm
    ) {}

    /**
     * Handler for the get DM request's response data.
     */
    public static interface GetDmResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final GetDmResponseBody body);
    }

    /**
     * Gets a DM by ID.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/dms.md">GET /dms</a> asynchronously.
     * @see GetDmResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void getDm(
        final GetDmRequestBody body,
        final GetDmResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "dms", 
                Map.of(
                    "messageId", body.messageId().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawGetDmResponseBody.class, 
                response -> {
                    handler.handle(
                        new GetDmResponseBody(
                            Dm.fromRaw(response.getData().dm())
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
     * Request body for the list DMs by sender request.
     */
    private static record ListDmsBySenderRequestBody(
            @NotNull UUID userId
    ) {}

    /**
     * Request body for the list DMs by channel request.
     */
    public static record ListDmsByChannelRequestBody(
            @NotNull UUID userId1,
            @NotNull UUID userId2
    ) {}

    /**
     * Request body for the list unread DMs by receiver request.
     */
    private static record ListUnreadDmsByReceiverRequestBody(
            @NotNull UUID userId
    ) {}

    /**
     * Raw response body for the list DMs requests.
     */
    private static record RawListDmsResponseBody(
            @JsonProperty("dms") @NotNull List<RawDm> dms
    ) {}

    /**
     * Response body for the list DMs requests.
     */
    public static record ListDmsResponseBody(
            @NotNull List<Dm> dms
    ) {}

    /**
     * Handler for the list DMs requests' response data.
     */
    public static interface ListDmsResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final ListDmsResponseBody body);
    }

    /**
     * Gets all DMs sent by a user.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/dms.md">GET /dms/list_from</a> asynchronously.
     * @see ListDmsResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void listDms(
        final ListDmsBySenderRequestBody body,
        final ListDmsResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "dms/list_from", 
                Map.of(
                    "userId", body.userId().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawListDmsResponseBody.class, 
                response -> {
                    handler.handle(
                        new ListDmsResponseBody(
                            response.getData().dms().stream()
                                .map(Dm::fromRaw)
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
     * Gets all DMs between two users.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/dms.md">GET /dms/list</a> asynchronously.
     * @see ListDmsResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void listDms(
        final ListDmsByChannelRequestBody body,
        final ListDmsResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "dms/list", 
                Map.of(
                    "userId1", body.userId1().toString(),
                    "userId2", body.userId2().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawListDmsResponseBody.class, 
                response -> {
                    handler.handle(
                        new ListDmsResponseBody(
                            response.getData().dms().stream()
                                .map(Dm::fromRaw)
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
     * Gets all unread DMs addressed to a user.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/dms.md">GET /dms/list_unread</a> asynchronously.
     * @see ListDmsResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void listDms(
        final ListUnreadDmsByReceiverRequestBody body,
        final ListDmsResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "dms/list_unread", 
                Map.of(
                    "userId", body.userId().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawListDmsResponseBody.class, 
                response -> {
                    handler.handle(
                        new ListDmsResponseBody(
                            response.getData().dms().stream()
                                .map(Dm::fromRaw)
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
     * Request body for the delete DM by ID request.
     */
    private static record DeleteDmRequestBody(
            @NotNull UUID messageId
    ) {}

    /**
     * Request body for the delete DMs by channel request.
     */
    private static record DeleteDmsByChannelRequestBody(
            @NotNull UUID userId1,
            @NotNull UUID userId2
    ) {}

    /**
     * Response body for the delete DM requests.
     */
    public static record DeleteDmResponseBody() {}

    /**
     * Handler for the delete DM requests' response data.
     */
    public static interface DeleteDmResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final DeleteDmResponseBody body);
    }

    /**
     * Deletes a specific message.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/dms.md">DELETE /dms</a> asynchronously.
     * @see DeleteDmResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void deleteDm(
        final DeleteDmRequestBody body,
        final DeleteDmResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.delete(
                "dms", 
                Map.of(
                    "messageId", body.messageId().toString()
                ),
                client.getAuthenticationHeaders(), 
                DeleteDmResponseBody.class, 
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
     * Deletes all messages between two users.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/dms.md">DELETE /dms/all</a> asynchronously.
     * @see DeleteDmResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void deleteDm(
        final DeleteDmsByChannelRequestBody body,
        final DeleteDmResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.delete(
                "dms/all", 
                Map.of(
                    "userId1", body.userId1().toString(),
                    "userId2", body.userId2().toString()
                ),
                client.getAuthenticationHeaders(), 
                DeleteDmResponseBody.class, 
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
     * Request body for the send DM request.
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static record SendDmRequestBody(
            @NotNull UUID toId,
            @NotNull String message
    ) {}

    /**
     * Raw response body for the send DM request.
     */
    private static record RawSendDmResponseBody(
            @JsonProperty("dm") @NotNull RawDm dm
    ) {}

    /**
     * Response body for the send DM request.
     */
    public static record SendDmResponseBody(
            @NotNull Dm dm
    ) {}

    /**
     * Handler for the send DM request's response data.
     */
    public static interface SendDmResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final SendDmResponseBody body);
    }

    /**
     * Sends a DM.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/dms.md">POST /dms</a> asynchronously.
     * @see SendDmResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void sendDm(
        final SendDmRequestBody body,
        final SendDmResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.post(
                "dms", 
                body,
                client.getAuthenticationHeaders(), 
                RawSendDmResponseBody.class, 
                response -> {
                    handler.handle(
                        new SendDmResponseBody(
                            Dm.fromRaw(response.getData().dm())
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
}
