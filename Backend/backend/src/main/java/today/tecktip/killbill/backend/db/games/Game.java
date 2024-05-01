package today.tecktip.killbill.backend.db.games;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import today.tecktip.killbill.backend.db.gameusers.GameUsers;
import today.tecktip.killbill.common.gameserver.games.GameConfig;

/**
 * Data structure for a game as retrieved from the SQL database.
 * @param name Basic user-supplied description for the game
 * @param id Game ID
 * @param created Timestamp when the game was created
 * @param hostId User ID for the game's creator
 * @param config Game's config
 */
public record Game(
    String name,
    UUID id,
    Instant created,
    UUID hostId,
    GameConfig config,
    String map
) {

    /**
     * Generates a GameWithUsers object with this game and all of its players.
     * @return Game and all users
     * @throws SQLException Unable to contact database
     */
    public GameWithUsers withUsers() throws SQLException {
        return new GameWithUsers(this, GameUsers.getGameUsersByGame(id));
    }
}
