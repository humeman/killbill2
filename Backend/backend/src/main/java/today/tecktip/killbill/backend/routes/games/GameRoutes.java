package today.tecktip.killbill.backend.routes.games;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import today.tecktip.killbill.backend.auth.Authenticator;
import today.tecktip.killbill.backend.db.games.Game;
import today.tecktip.killbill.backend.db.games.GameWithUsers;
import today.tecktip.killbill.backend.db.games.Games;
import today.tecktip.killbill.backend.db.games.GameWithUsers.GameWithUsersPub;
import today.tecktip.killbill.backend.db.gameusers.GameUser;
import today.tecktip.killbill.backend.db.gameusers.GameUsers;
import today.tecktip.killbill.backend.db.keys.Key;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.db.users.UserRole;
import today.tecktip.killbill.backend.db.users.Users;
import today.tecktip.killbill.backend.exceptions.AuthenticationFailure;
import today.tecktip.killbill.backend.exceptions.InvalidArgumentException;
import today.tecktip.killbill.backend.exceptions.NotFoundException;
import today.tecktip.killbill.backend.exceptions.ServerError;
import today.tecktip.killbill.backend.exceptions.TransientServerError;
import today.tecktip.killbill.common.gameserver.games.GameConfig;
import today.tecktip.killbill.common.maploader.MapLoader;
import today.tecktip.killbill.backend.routes.MessageBody;

import jakarta.validation.Valid;

/**
 * Routes for managing games.
 * 
 * @author cs
 */
@RestController
@RequestMapping("/games")
public class GameRoutes {
    /**
     * Port to run UDP game server on.
     */
    private int udpPort;

    /**
     * Used by Spring Boot to store the UDP port env var.
     * @param udpPort UDP_PORT env var
     */
    @Value("${env_vars.udp_port}")
    private void setUdpPort(final String udpPort) {
        this.udpPort = Integer.valueOf(udpPort);
    }

    /**
     * Host to run UDP game server on.
     */
    private String udpHost;
    
    /**
     * Used by Spring Boot to store the UDP host env var.
     * @param udpPort UDP_HOST env var
     */
    @Value("${env_vars.udp_host}")
    private void setUdpHost(final String udpHost) {
        this.udpHost = udpHost;
    }

    /**
     * Request body for game listings.
     * @param games List of games matching query
     */
    private record GetGamesResponseBody(List<GameWithUsersPub> games) {}

    /**
     * Lists active games.
     * @param authToken Authentication token header. To list all games, admin key is required.
     * @param userId User ID to list games for.
     * @return List of games this user is hosting or invited to.
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @GetMapping("/list")
    public MessageBody listGames(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @RequestParam(required = false) String userId
    ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        UUID filterId = null;
        if (userId != null) {
            try {
                filterId = UUID.fromString(userId);
                if (filterId == null) throw new Exception();
            } catch (Exception e) {
                throw new InvalidArgumentException("Unable to parse ID as UUID.");
            }
        }

        if (filterId == null || !authenticatedUser.id().equals(filterId)) Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);
 

        List<GameWithUsersPub> gamesWithUsers = new ArrayList<>();
        try {
            if (filterId == null) {
                for (Game game : Games.getGames()) {
                    gamesWithUsers.add(game.withUsers().userFriendly());
                }
            } else {
                
                for (GameUser gameUser : GameUsers.getGameUsersByUser(filterId)) {
                    gamesWithUsers.add(gameUser.toGameWithUsers().userFriendly());
                }

            }
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        return MessageBody.ofSuccess(new GetGamesResponseBody(gamesWithUsers));
    }

    /**
     * Gets a single game.
     * @param authToken Authentication token header.
     * @param id ID of the game to retrieve.
     * @return Game with the specified ID.
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected/game not found
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @GetMapping("")
    public MessageBody getGame(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @RequestParam(required = true) String id
    ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        UUID gameUuid;
        try {
            gameUuid = UUID.fromString(id);
            if (gameUuid == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse ID as UUID.");
        }

        GameWithUsers game;
        try {
            game = Games.getGame(gameUuid).withUsers();

            // Verify user is in GameUsers or is an admin
            if (!game.containsUser(authenticatedUser.id())) Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Game not found.");
        }

        return MessageBody.ofSuccess(game.userFriendly());
    }

    /**
     * Request body for creating a new game.
     * @param name Short description for the game
     * @param users List of users to invite, not including authenticated user
     * @param config Game config, as documented in {@link https://git.las.iastate.edu/cs309/2024spr/hb4_4/-/blob/main/Documents/backend/gameserver/gameconfig.md}
     * @param map Map file
     */
    private record CreateGameRequestBody(String name, List<String> users, JsonNode config, String map) {}

    /**
     * Creates a game.
     * @param authToken Authentication token header.
     * @param request Details for game being created.
     * @return Created game.
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected/game not found
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @PostMapping("")
    public MessageBody createGame(
            @RequestHeader(value = "Authorization", required = true) String authToken, 
            @Valid @RequestBody(required = true) CreateGameRequestBody request
        ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
            Key key = Authenticator.requireAuthentication(authToken);
            User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        // Confirm request parameters
        if (!GameValidator.isValidName(request.name)) {
            throw new InvalidArgumentException("Game names must be 3-64 characters long and not contain invalid characters.");
        }

        // Confirm user isn't hosting a game
        try {
            Games.getGameByHost(authenticatedUser.id());
            throw new InvalidArgumentException("You are already hosting a game. End it first.");
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {}

        // Confirm user list count
        if (request.users.size() > 4) {
            throw new InvalidArgumentException("Cannot invite more than 4 users.");
        }

        // Compile user list
        List<User> users = new ArrayList<>();
        for (String sid : request.users) {
            UUID uId;
            try {
                uId = UUID.fromString(sid);
                if (uId == null) throw new Exception();
            } catch (Exception e) {
                throw new InvalidArgumentException("Unable to parse user IDs as UUID.");
            }

            User user;
            try {
                user = Users.getUser(uId);
            } catch (SQLException e) {
                throw new TransientServerError("Failed to contact database.", e);
            } catch (NotFoundException e) {
                throw new InvalidArgumentException("User not found.");
            }

            users.add(user);
        }

        // Confirm authenticated user is in not list
        for (User user : users) {
            if (user.id().equals(authenticatedUser.id())) throw new InvalidArgumentException("You can't invite yourself.");
        }

        // Add GameUser for self
        users.add(authenticatedUser);

        // Attempt to parse config
        GameConfig config;
        try {
            config = GameConfig.fromJsonNode(request.config());
        } catch (JsonProcessingException e) {
            throw new InvalidArgumentException("Invalid game config: " + e);
        }

        // Attempt to parse map
        try {
            MapLoader.load(List.of(new ByteArrayInputStream(request.map().getBytes())));
        } catch (final Throwable t) {
            System.err.println(t);
            throw new InvalidArgumentException("Map parse failed: " + t.getMessage());
        }

        // Generate the game
        Game game = new Game(
            request.name,
            UUID.randomUUID(),
            Instant.now(),
            authenticatedUser.id(),
            config,
            request.map
        );

        // Create GameUsers list
        List<GameUser> gameUsers = new ArrayList<>();
        for (User user : users) {
            gameUsers.add(new GameUser(game.id(), user.id(), UUID.randomUUID()));
        }

        // Send out to database
        try {
            Games.putGame(game);
            for (GameUser gameUser : gameUsers)
                GameUsers.putGameUser(gameUser);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to send new game to database.", e);
        }

        // Done :)
        return MessageBody.ofSuccess(new GameWithUsers(game, gameUsers).userFriendly());
    }

    /**
     * Request body for inviting a user to an already existing game.
     */
    private record InviteUserRequestBody(String gameId, String userId) {}

    /**
     * Invites a user to an already existing game.
     * <p>
     * Must be the host of the specified game or an admin.
     * @param authToken Authentication token header.
     * @param request Details for the user being invited.
     * @return Updated game.
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected/game not found
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @PostMapping("/users")
    public MessageBody inviteUser(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @Valid @RequestBody(required = true) InviteUserRequestBody request
    ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        UUID gameId;
        UUID inviteeId;
        try {
            gameId = UUID.fromString(request.gameId);
            inviteeId = UUID.fromString(request.userId);
            if (gameId == null || inviteeId == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse IDs as UUID.");
        }

        // Get game
        GameWithUsers game;
        try {
            game = Games.getGame(gameId).withUsers();
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Game not found.");
        }

        // Verify user is host or an admin
        if (!game.game().hostId().equals(authenticatedUser.id())) Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);

        // Verify user isn't already invited
        if (game.containsUser(inviteeId)) throw new InvalidArgumentException("User is already invited.");

        // Verify user exists
        try {
            Users.getUser(inviteeId);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Invitee does not exist.");
        }

        // Check length of invitees
        if (game.users().size() == 5) {
            throw new InvalidArgumentException("You cannot invite more than 4 people to a game.");
        }

        // Invite them
        GameUser gameUser = new GameUser(game.game().id(), inviteeId, UUID.randomUUID());
        try {
            GameUsers.putGameUser(gameUser);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Invitee does not exist.");
        }

        game.users().add(gameUser);

        return MessageBody.ofSuccess(game.userFriendly());
    }

    /**
     * Uninvites a user from an already existing game.
     * <p>
     * Must be the host of the specified game or an admin.
     * @param authToken Authentication token header.
     * @param gameId Game ID for user being deleted.
     * @param userId User ID for user being deleted.
     * @return Updated game.
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected/game not found
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @DeleteMapping("/users")
    public MessageBody removeUser(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @RequestParam(required = true) String gameId,
        @RequestParam(required = true) String userId
    ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        UUID gameUuid;
        UUID targetUuid;
        try {
            gameUuid = UUID.fromString(gameId);
            targetUuid = UUID.fromString(userId);
            if (gameUuid == null || targetUuid == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse IDs as UUID.");
        }

        // Get game
        GameWithUsers game;
        try {
            game = Games.getGame(gameUuid).withUsers();
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Game not found.");
        }

        // Verify user is host, removing themself, or an admin
        if (!game.game().hostId().equals(authenticatedUser.id()) && !targetUuid.equals(authenticatedUser.id())) Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);

        // Get user
        GameUser target;
        try {
            target = game.getUser(targetUuid);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("User is not in the game.");
        }

        // Verify the target isn't the host
        if (targetUuid.equals(game.game().hostId())) {
            throw new InvalidArgumentException("Cannot remove the host. End the game instead.");
        }

        // Delete them
        try {
            GameUsers.deleteGameUser(target);
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } 

        return MessageBody.ofSuccess(game.userFriendly());
    }

    /**
     * Closes an active game.
     * <p>
     * Must be the host of the specified game or an admin.
     * @param authToken Authentication token header.
     * @param id ID for the game to be deleted.
     * @return Game that was removed
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected/game not found
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @DeleteMapping("")
    public MessageBody removeGame(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @RequestParam(required = true) String id
    ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        UUID gameUuid;
        try {
            gameUuid = UUID.fromString(id);
            if (id == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to parse game ID as UUID.");
        }

        // Get game
        GameWithUsers game;
        try {
            game = Games.getGame(gameUuid).withUsers();
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Game not found.");
        }

        // Verify user is host or an admin
        if (!game.game().hostId().equals(authenticatedUser.id())) Authenticator.requireRole(authenticatedUser, UserRole.ADMIN);

        // Delete users
        try {
            for (GameUser user : game.users()) {
                GameUsers.deleteGameUser(user);
            }
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("User is not in the game.");
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        }

        // Delete game
        try {
            Games.deleteGame(game.game().id());
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } 

        return MessageBody.ofSuccess(game.userFriendly());
    }

    /**
     * Request body for connecting to a game.
     */
    private record ConnectGameRequestBody(String gameId) {}

    /**
     * Response body for connecting to a game.
     */
    private record ConnectGameResponseBody(String host, int port, String gameKey) {}

    /**
     * Connects to a game, getting UDP details for it.
     * <p>
     * Must be a player in the game. Admin does not bypass this.
     * @param authToken Authentication token header.
     * @param request Details for the game being joined.
     * @return Updated game.
     * @throws TransientServerError SQL contact failed
     * @throws InvalidArgumentException Arguments not as expected/game not found
     * @throws AuthenticationFailure Invalid token or permissions
     */
    @PostMapping("/connect")
    public MessageBody inviteUser(
        @RequestHeader(value = "Authorization", required = true) String authToken,
        @Valid @RequestBody(required = true) ConnectGameRequestBody request
    ) throws TransientServerError, InvalidArgumentException, AuthenticationFailure {
        Key key = Authenticator.requireAuthentication(authToken);
        User authenticatedUser = Authenticator.getAuthenticatedUser(key);

        UUID gameId;
        try {
            gameId = UUID.fromString(request.gameId);
            if (gameId == null) throw new Exception();
        } catch (Exception e) {
            throw new InvalidArgumentException("Unable to game ID as UUID.");
        }

        // Get game
        GameWithUsers game;
        try {
            game = Games.getGame(gameId).withUsers();
        } catch (SQLException e) {
            throw new TransientServerError("Failed to contact database.", e);
        } catch (NotFoundException e) {
            throw new InvalidArgumentException("Game not found.");
        }

        // Verify user is in the game
        GameUser user = null;
        for (final GameUser gameUser : game.users()) {
            if (gameUser.userId().equals(authenticatedUser.id())) {
                user = gameUser;
                break;
            }
        }

        if (user == null) {
            throw new InvalidArgumentException("You are not a player in that game.");
        }

        try {
            return MessageBody.ofSuccess(
                new ConnectGameResponseBody(
                    udpHost, 
                    udpPort, 
                    user.getGameKey()
                )
            );
        } catch (final JsonProcessingException e) {
            throw new ServerError("Unable to serialize game key.", e);
        }
    }

    /**
	 * This class should not be instantiated manually.
	 */
	public GameRoutes() { }
}