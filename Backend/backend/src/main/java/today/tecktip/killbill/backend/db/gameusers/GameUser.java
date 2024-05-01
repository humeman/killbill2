package today.tecktip.killbill.backend.db.gameusers;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.backend.auth.GameServerAuthenticator.GameAuthenticationBody;
import today.tecktip.killbill.backend.db.games.GameWithUsers;
import today.tecktip.killbill.backend.db.games.Games;

/**
 * Data structure for a user in a game as retrieved from the SQL database.
 * @param gameId UUID for the game this user is a player in
 * @param userId User's ID
 * @param key A key used for authentication
 */
public record GameUser(
    UUID gameId,
    UUID userId,
    UUID key
) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * From a single GameUser, this creates a GameWithUsers containing the game and all other users in the game.
     * @return Game and all users
     * @throws SQLException Unable to contact database
     */
    public GameWithUsers toGameWithUsers() throws SQLException {
        return new GameWithUsers(Games.getGame(gameId), GameUsers.getGameUsersByGame(gameId));
    }

    /**
     * Gets the unique, base64-encoded game key that allows this user to connect to the UDP server.
     * @return Base64 encoded game auth key
     * @throws JsonProcessingException Unable to serialize as JSON
     */
    public String getGameKey() throws JsonProcessingException {
        return Base64.getEncoder().encodeToString(
            MAPPER.writeValueAsString(
                new GameAuthenticationBody(gameId, userId, key)).getBytes(Charset.defaultCharset())
        );
    }
}
