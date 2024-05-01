package today.tecktip.killbill.backend.auth;

import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import today.tecktip.killbill.backend.db.games.Game;
import today.tecktip.killbill.backend.db.games.Games;
import today.tecktip.killbill.backend.db.gameusers.GameUser;
import today.tecktip.killbill.backend.db.gameusers.GameUsers;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.db.users.Users;
import today.tecktip.killbill.backend.exceptions.AuthenticationFailure;
import today.tecktip.killbill.backend.exceptions.NotFoundException;
import today.tecktip.killbill.backend.gameserver.games.GameState;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.common.gameserver.MessageHandler.UdpClient;

/**
 * Handles authentication to game servers.
 * @author cs
 */
public class GameServerAuthenticator {
    /**
     * Body of a user's authentication key.
     * @param gameId Game ID
     * @param userId User ID
     * @param key User's game key
     */
    public static record GameAuthenticationBody(UUID gameId, UUID userId, UUID key) {}

    /**
     * Used internally to read JSON encoded keys.
     */
    private static ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Requires authentication on an API request.
     * @param key Incoming message 'key' field to authenticate
     * @param client UDP client associated with the authenticated user
     * @return GameUserState body matching the authenticated user
     * @throws AuthenticationFailure Not authorized
     * @throws SQLException Unable to validate key with database
     */
    public static GameUserState requireAuthentication(final String key, final UdpClient client) throws SQLException {
        if (key == null) {
            throw new AuthenticationFailure("Missing required field: 'key'");
        }

        GameAuthenticationBody body = decodeAuthenticationBody(key);

        // Check that this game is in game memory
        GameState game = GameState.get(body.gameId());

        // Check if the user's lying to us or if the game actually is missing
        if (game == null) {
            try {
                Game dbGame = Games.getGame(body.gameId());

                // It really exists. We dun goofed.
                // We'll create it now.
                GameState.register(dbGame);
                game = GameState.get(dbGame.id());
            } catch (final NotFoundException e) {
                throw new AuthenticationFailure("Invalid key.");
            }
        }

        // Now, get the user
        GameUserState user = game.getUser(body.userId());

        // Again, check if we dun goofed
        if (user == null) {
            try {
                GameUser gameUser = GameUsers.getGameUser(body.gameId(), body.userId());
                User dbUser = Users.getUser(body.userId());

                // It really exists. We dun goofed.
                // Add them now.
                game.addUser(dbUser, gameUser, client);
                user = game.getUser(body.userId());
            } catch (final NotFoundException e) {
                throw new AuthenticationFailure("Invalid key.");
            }
        }

        // Validate their key
        if (!user.getGameUser().key().equals(body.key())) {
            throw new AuthenticationFailure("Invalid key.");
        }

        // And make sure that this isn't a different client to what we have stored
        if (!user.getClient().equals(client)) {
            // If the client is currently disconnected: We can switch this without consequence
            if (!user.isConnected())
                user.setClient(client);

            else {
                // They're already connected in another location. This is an error
                throw new AuthenticationFailure("You are already connected in another location. Disconnect it or wait 15 seconds for it to time out (if this is a network error).");
            }
        }
        
        return user;
    }

    /**
     * Decodes an <code>Authorization</code> header into an {@link GameAuthenticationBody}, if possible.
     * @param bearerToken Content of the <code>Authorization</code> header
     * @return Decoded content of the <code>Authorization</code> header
     * @throws AuthenticationFailure Couldn't parse header
     */
    private static GameAuthenticationBody decodeAuthenticationBody(String bearerToken) {
        if (bearerToken == null) throw new AuthenticationFailure("Authorization is required for this method.");

        String token = new String(Base64.getDecoder().decode(bearerToken));

        try {
            JsonNode node = MAPPER.readTree(token);
            JsonNode gameId = node.get("gameId");
            if (gameId == null || gameId.isMissingNode() || !JsonNodeType.STRING.equals(gameId.getNodeType())) throw new IllegalArgumentException("Required field: gameId (string)");
            JsonNode userId = node.get("userId");
            if (userId == null || userId.isMissingNode() || !JsonNodeType.STRING.equals(userId.getNodeType())) throw new IllegalArgumentException("Required field: userId (string)");
            JsonNode key = node.get("key");
            if (key == null || key.isMissingNode() || !JsonNodeType.STRING.equals(key.getNodeType())) throw new IllegalArgumentException("Required field: key (string)");

            UUID ugame = UUID.fromString(gameId.asText());
            UUID uid = UUID.fromString(userId.asText());
            UUID ukey = UUID.fromString(key.asText());
            return new GameAuthenticationBody(ugame, uid, ukey);
        }
        catch (JsonProcessingException | IllegalArgumentException e) {
            System.err.println(e);
            throw new AuthenticationFailure("Invalid key.");
        }
    }

	/**
	 * This class should not be manually instantiated.
	 */
	public GameServerAuthenticator() { }
}