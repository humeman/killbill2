package today.tecktip.killbill.frontend.http.requests;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.common.gameserver.games.GameConfig;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.http.NativeHttpClient.ResponseErrorHandler;
import today.tecktip.killbill.frontend.http.requests.UserRequests.DeleteUserResponseBody;
import today.tecktip.killbill.frontend.http.requests.data.Game;
import today.tecktip.killbill.frontend.http.requests.data.Game.RawGame;

/**
 * Handles requests to the game endpoints.
 * <p>
 * <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/games.md">Documentation</a>
 * @author cs
 */
public class GameRequests {

    /**
     * Request body for the get game request.
     * @param id UUID of the game.
     */
    public static record GetGameRequestBody(
            @NotNull UUID id
    ) {}

    /**
     * Raw response body for the get game request.
     * @param game The raw game body.
     */
    private static record RawGetGameResponseBody(
            @JsonProperty("game") @NotNull RawGame game,
            @JsonProperty("users") @NotNull List<String> users
    ) {}

    /**
     * Response body for the get game request.
     * @param game The game retrieved from the API.
     */
    public static record GetGameResponseBody(
            @NotNull Game game,
            @NotNull List<UUID> users
    ) {}

    /**
     * Handler for the get game request's response data.
     */
    public static interface GetGameResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final GetGameResponseBody body);
    }

    /**
     * Gets a game by its ID.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/games.md">GET /games</a> asynchronously.
     * @see GetGameResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void getGame(
        final GetGameRequestBody body,
        final GetGameResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "games",
                Map.of(
                    "id", body.id().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawGetGameResponseBody.class, 
                response -> {
                    handler.handle(
                        new GetGameResponseBody(
                            Game.fromRaw(response.getData().game()),
                            response.getData().users.stream()
                                    .map(UUID::fromString)
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
     * Request body for the list games request.
     * @param userId Filters results to include only this user.
     */
    public static record ListGamesRequestBody(
            @NotNull UUID userId
    ) {}

    /**
     * Inner representation of raw items in the game list.
     * @param game Game object
     * @param users User UUID list
     */
    private static record RawListGamesResponseInnerObject(
            @JsonProperty("game") @NotNull RawGame game,
            @JsonProperty("users") @NotNull List<String> users
    ) {}

    /**
     * Raw response body for the list games request.
     * @param games The list of raw game bodies.
     */
    private static record RawListGamesResponseBody(
            @JsonProperty("games") @NotNull List<RawListGamesResponseInnerObject> games
    ) {}

    /**
     * Inner representation of items in the game list.
     * @param game Game object
     * @param users User UUID list
     */
    public static record ListGamesResponseInnerObject(
            @NotNull Game game,
            @NotNull List<UUID> users
    ) {}

    /**
     * Response body for the list games request.
     * @param games The games retrieved from the API.
     */
    public static record ListGamesResponseBody(
            @NotNull List<ListGamesResponseInnerObject> games
    ) {}

    /**
     * Handler for the list games request's response data.
     */
    public static interface ListGamesResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final ListGamesResponseBody body);
    }

    /**
     * Gets a list of games, optionally filtered by a user ID.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/games.md">GET /games/list</a> asynchronously.
     * @see ListGamesResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void listGames(
        final ListGamesRequestBody body,
        final ListGamesResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.get(
                "games/list",
                body.userId == null ? Map.of() : Map.of(
                    "userId", body.userId().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawListGamesResponseBody.class, 
                response -> {
                    handler.handle(
                        new ListGamesResponseBody(
                            response.getData().games().stream()
                                .map(rawObj -> {
                                    return new ListGamesResponseInnerObject(
                                        Game.fromRaw(rawObj.game()),
                                        rawObj.users().stream()
                                                .map(UUID::fromString)
                                                .toList()
                                    );
                                })
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
     * Gets a list of all active games.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/games.md">GET /games/list</a> asynchronously.
     * @see ListGamesResponseBody
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void listGames(
        final ListGamesResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        listGames(new ListGamesRequestBody(null), handler, errorHandler);
    }

    /**
     * Request body for the create game request.
     * @param name A short description of the game.
     * @param users The UUIDs of any users to invite to the game.
     * @param config Game config object.
     * @param map Map .kbmap string.
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static record CreateGameRequestBody(
            @NotNull String name,
            @NotNull List<UUID> users,
            @NotNull GameConfig config,
            @NotNull String map
    ) {}

    /**
     * Raw response body for the create game request.
     * @param game The raw game body.
     */
    private static record RawCreateGameResponseBody(
            @JsonProperty("game") @NotNull RawGame game,
            @JsonProperty("users") @NotNull List<String> users) {}

    /**
     * Response body for the create game request.
     * @param game The game created by the API.
     */
    public static record CreateGameResponseBody(
            @NotNull Game game,
            @NotNull List<UUID> users
    ) {}

    /**
     * Handler for the create game request's response data.
     */
    public static interface CreateGameResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final CreateGameResponseBody body);
    }

    /**
     * Starts a new game.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/games.md">POST /games</a> asynchronously.
     * @see CreateGameResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void createGame(
        final CreateGameRequestBody body,
        final CreateGameResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.post(
                "games",
                body,
                client.getAuthenticationHeaders(), 
                RawCreateGameResponseBody.class, 
                response -> {
                    handler.handle(
                        new CreateGameResponseBody(
                            Game.fromRaw(response.getData().game()),
                            response.getData().users().stream()
                                .map(UUID::fromString)
                                .toList()
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
     * Request body for the delete game request.
     * @param id The ID of the game to delete
     */
    public static record DeleteGameRequestBody(
            @NotNull UUID id
    ) {}

    /**
     * Raw response body for the delete game request.
     * @param game The raw game body.
     * @param users The users in the deleted game.
     */
    private static record RawDeleteGameResponseBody(
            @JsonProperty("game") @NotNull RawGame game,
            @JsonProperty("users") @NotNull List<String> users
    ) {}

    /**
     * Response body for the delete game request.
     * @param game The game deleted by the API.
     * @param users The users in the deleted game.
     */
    public static record DeleteGameResponseBody(
            @NotNull Game game,
            @NotNull List<UUID> users
    ) {}

    /**
     * Handler for the delete game request's response data.
     */
    public static interface DeleteGameResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final DeleteGameResponseBody body);
    }

    /**
     * Deletes a game.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/games.md">DELETE /games</a> asynchronously.
     * @see DeleteGameResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void deleteGame(
        final DeleteGameRequestBody body,
        final DeleteGameResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.delete(
                "games",
                Map.of(
                    "id", body.id().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawDeleteGameResponseBody.class, 
                response -> {
                    handler.handle(
                        new DeleteGameResponseBody(
                            Game.fromRaw(response.getData().game()),
                            response.getData().users().stream()
                                .map(UUID::fromString)
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
     * Request body for the invite user request.
     * @param gameId The ID of the game to invite the user to
     * @param userId The ID of the user to invite to the game
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static record InviteUserRequestBody(
            @NotNull UUID gameId,
            @NotNull UUID userId
    ) {}

    /**
     * Raw response body for the invite user request.
     * @param game The game modified by the API.
     * @param users The updated user list.
     */
    private static record RawInviteUserResponseBody(
            @JsonProperty("game") @NotNull RawGame game,
            @JsonProperty("users") @NotNull List<String> users
    ) {}

    /**
     * Response body for the invite user request.
     * @param game The game modified by the API.
     * @param users The updated user list.
     */
    public static record InviteUserResponseBody(
            @NotNull Game game,
            @NotNull List<UUID> users
    ) {}

    /**
     * Handler for the invite user request's response data.
     */
    public static interface InviteUserResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final InviteUserResponseBody body);
    }

    /**
     * Invites a user to an existing game.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/games.md">POST /games/users</a> asynchronously.
     * @see InviteUserResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void inviteUser(
        final InviteUserRequestBody body,
        final InviteUserResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.post(
                "games/users",
                body,
                client.getAuthenticationHeaders(), 
                RawInviteUserResponseBody.class, 
                response -> {
                    handler.handle(
                        new InviteUserResponseBody(
                            Game.fromRaw(response.getData().game()),
                            response.getData().users().stream()
                                .map(UUID::fromString)
                                .toList()
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
     * Request body for the remove user request.
     * @param gameId The ID of the game to remove the user from
     * @param userId The ID of the user to remove from the game
     */
    public static record RemoveUserRequestBody(
            @NotNull UUID gameId,
            @NotNull UUID userId
    ) {}

    /**
     * Raw response body for the remove user request.
     * @param game The game modified by the API.
     * @param users The updated user list.
     */
    private static record RawRemoveUserResponseBody(
            @JsonProperty("game") @NotNull RawGame game,
            @JsonProperty("users") @NotNull List<String> users
    ) {}

    /**
     * Response body for the remove user request.
     * @param game The game modified by the API.
     * @param users The updated user list.
     */
    public static record RemoveUserResponseBody(
            @NotNull Game game,
            @NotNull List<UUID> users
    ) {}

    /**
     * Handler for the remove user request's response data.
     */
    public static interface RemoveUserResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final RemoveUserResponseBody body);
    }

    /**
     * Removes a user from an existing game.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/games.md">DELETE /games/users</a> asynchronously.
     * @see DeleteUserResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void removeUser(
        final RemoveUserRequestBody body,
        final RemoveUserResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.delete(
                "games/users",
                Map.of(
                    "userId", body.userId().toString(),
                    "gameId", body.gameId().toString()
                ),
                client.getAuthenticationHeaders(), 
                RawRemoveUserResponseBody.class, 
                response -> {
                    handler.handle(
                        new RemoveUserResponseBody(
                            Game.fromRaw(response.getData().game()),
                            response.getData().users().stream()
                                .map(UUID::fromString)
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
     * Request body for the connect to game request.
     * @param gameId The ID of the game to connect to
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static record ConnectGameRequestBody(
            @NotNull UUID gameId
    ) {}

    /**
     * Response body for the connect to game request.
     * @param host The UDP server host.
     * @param port The UDP server port.
     * @param gameKey The unique game key for the authenticated user.
     */
    public static record ConnectGameResponseBody(
            @JsonProperty("host") @NotNull String host,
            @JsonProperty("port") @NotNull int port,
            @JsonProperty("gameKey") @NotNull String gameKey
    ) {}

    /**
     * Handler for the connect to game request's response data.
     */
    public static interface ConnectGameResponseBodyHandler {
        /**
         * Handle a response body.
         * @param body Response body
         */
        public void handle(final ConnectGameResponseBody body);
    }

    /**
     * Connects to a game.
     * <p>
     * Runs <a href="https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/api/routes/games.md">POST /games/connect</a> asynchronously.
     * @see ConnectGameResponseBody
     * @param body Request body
     * @param handler Response data handler
     * @param errorHandler Response error handler
     */
    public static void connect(
        final ConnectGameRequestBody body,
        final ConnectGameResponseBodyHandler handler,
        final ResponseErrorHandler errorHandler
    ) {
        NativeHttpClient client = KillBillGame.get().getHttpClient();

        try {
            client.post(
                "games/connect",
                body,
                client.getAuthenticationHeaders(), 
                ConnectGameResponseBody.class, 
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
