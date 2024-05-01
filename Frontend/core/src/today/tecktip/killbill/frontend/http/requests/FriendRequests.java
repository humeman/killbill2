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
import today.tecktip.killbill.frontend.http.requests.data.Friend;
import today.tecktip.killbill.frontend.http.requests.data.Friend.RawFriend;

/**
 * Handles requests to the friend endpoints.
 * <p>
 * <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/friends.md">Documentation</a>
 * @author cs
 */
public class FriendRequests {

    /**
     * Request body for the get friend request.
     * @param userId1 UUID of the first user in the friend link.
     * @param userId2 UUID of the second user in the friend link.
     */
    public static record GetFriendRequestBody(
            @NotNull UUID userId1,
            @NotNull UUID userId2
    ) {}

    /**
     * Raw response body for the get friend request.
     * @param friend The raw friend body.
     */
    private static record RawGetFriendResponseBody(
            @JsonProperty("friend") @NotNull RawFriend friend
    ) {}

    /**
     * Response body for the get friend request.
     * @param friend The friend retrieved from the API.
     */
    public static record GetFriendResponseBody(
            @NotNull Friend friend
    ) {}

    /**
     * Handler for the get friend request's response data.
     */
    public static interface GetFriendResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final GetFriendResponseBody body);
    }

    /**
     * Gets a friend link by two user IDs.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/friends.md">GET /friends</a> asynchronously.
     * @see GetFriendResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void getFriend(
        final GetFriendRequestBody body,
        final GetFriendResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "friends",
                Map.of(
                    "userId1", body.userId1().toString(),
                    "userId2", body.userId2().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawGetFriendResponseBody.class, 
                response -> {
                    handler.handle(
                        new GetFriendResponseBody(
                            Friend.fromRaw(response.getData().friend())
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
     * Request body for the list friends request.
     * @param userId Filters results to include only this user.
     */
    public static record ListFriendsRequestBody(
            @NotNull UUID userId
    ) {}

    /**
     * Raw response body for the list friends request.
     * @param friends The list of raw friend body.
     */
    private static record RawListFriendsResponseBody(
            @JsonProperty("friends") @NotNull List<RawFriend> friends
    ) {}

    /**
     * Response body for the list friends request.
     * @param friends The friends retrieved from the API.
     */
    public static record ListFriendsResponseBody(
            @NotNull List<Friend> friends
    ) {}

    /**
     * Handler for the list friends request's response data.
     */
    public static interface ListFriendsResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final ListFriendsResponseBody body);
    }

    /**
     * Gets a list of friend links.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/friends.md">GET /friends/list</a> asynchronously.
     * @see ListFriendsResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void listFriends(
        final ListFriendsRequestBody body,
        final ListFriendsResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "friends/list",
                Map.of(
                    "userId", body.userId() == null ? null : body.userId().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawListFriendsResponseBody.class, 
                response -> {
                    handler.handle(
                        new ListFriendsResponseBody(
                            response.getData().friends().stream()
                                .map(Friend::fromRaw)
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
     * Request body for the create friend link request.
     * @param userId The ID of the user to send a friend request to
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static record CreateFriendRequestBody(
            @NotNull UUID userId
    ) {}

    /**
     * Raw response body for the create friend request.
     * @param friend The raw friend body.
     */
    private static record RawCreateFriendResponseBody(
            @JsonProperty("friend") @NotNull RawFriend friend
    ) {}

    /**
     * Response body for the create friend request.
     * @param friend The friend created by the API.
     */
    public static record CreateFriendResponseBody(
            @NotNull Friend friend
    ) {}

    /**
     * Handler for the create friend request's response data.
     */
    public static interface CreateFriendResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final CreateFriendResponseBody body);
    }

    /**
     * Sends a friend request to another user.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/friends.md">POST /friends</a> asynchronously.
     * @see CreateFriendResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void createFriend(
        final CreateFriendRequestBody body,
        final CreateFriendResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.post(
                "friends",
                body,
                client.getAuthenticationHeaders(), 
                RawCreateFriendResponseBody.class, 
                response -> {
                    handler.handle(
                        new CreateFriendResponseBody(
                            Friend.fromRaw(response.getData().friend())
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
     * Accepts a friend request from another user.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/friends.md">PUT /friends</a> asynchronously.
     * @see CreateFriendResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void acceptFriend(
        final CreateFriendRequestBody body,
        final CreateFriendResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.put(
                "friends",
                body,
                client.getAuthenticationHeaders(), 
                RawCreateFriendResponseBody.class, 
                response -> {
                    handler.handle(
                        new CreateFriendResponseBody(
                            Friend.fromRaw(response.getData().friend())
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
     * Request body for the delete friend link request.
     * @param userId The ID of the user to delete a friend link with
     */
    public static record DeleteFriendRequestBody(
            @NotNull UUID userId
    ) {}

    /**
     * Raw response body for the delete friend request.
     * @param friend The raw friend body.
     */
    private static record RawDeleteFriendResponseBody(
            @JsonProperty("friend") @NotNull RawFriend friend
    ) {}

    /**
     * Response body for the delete friend request.
     * @param friend The friend deleted by the API.
     */
    public static record DeleteFriendResponseBody(
            @NotNull Friend friend
    ) {}

    /**
     * Handler for the delete friend request's response data.
     */
    public static interface DeleteFriendResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final DeleteFriendResponseBody body);
    }

    /**
     * Deletes a friend link with another user.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/friends.md">DELETE /friends</a> asynchronously.
     * @see DeleteFriendResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void deleteFriend(
        final DeleteFriendRequestBody body,
        final DeleteFriendResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.delete(
                "friends",
                Map.of(
                    "userId", body.userId().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawDeleteFriendResponseBody.class, 
                response -> {
                    handler.handle(
                        new DeleteFriendResponseBody(
                            Friend.fromRaw(response.getData().friend())
                        )
                    );
                },
                errorHandler
            );
        } catch (final URISyntaxException e) {
            throw new CatastrophicException("Invalid URI.", e);
        }
    }
}
