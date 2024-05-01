package today.tecktip.killbill.backend.db.games;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;

import today.tecktip.killbill.backend.db.Database;
import today.tecktip.killbill.backend.exceptions.NotFoundException;
import today.tecktip.killbill.common.gameserver.games.GameConfig;

/**
 * Database helper methods for managing games.
 * @author cs
 */
public class Games {
    /**
     * Upserts a game.
     */
    private static String QUERY_PUT =
        "INSERT INTO Games (id, name, created, hostId, config, map) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = values(name), hostId = values(hostId), config = values(config), map = values(map)";
    /**
     * Gets all games.
     */
    private static String QUERY_GET_ALL = 
        "SELECT * FROM Games";
    /**
     * Gets a game with a specified game ID.
     */
    private static String QUERY_GET = 
        "SELECT * FROM Games WHERE id = ?";
    /**
     * Gets a game with a specified host ID.
     */
    private static String QUERY_GET_BY_HOST = 
        "SELECT * FROM Games WHERE hostId = ?";
    /**
     * Deletes a game.
     */
    private static String QUERY_DELETE = 
        "DELETE FROM Games WHERE id = ?";

    /**
     * Gets all games registered in the database.
     * @return All games
     * @throws SQLException Unable to execute query
     */
    public static List<Game> getGames() throws SQLException {
        final ArrayList<Game> games = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_ALL,
            resultSet -> {
                games.add(parse(resultSet));
            }
        );
        return games;
    }

    /**
     * Gets a game by the game ID.
     * @param id Game's ID
     * @return Game associated with this ID
     * @throws SQLException Unable to execute query
     * @throws NotFoundException No such game exists
     */
    public static Game getGame(final UUID id) throws SQLException, NotFoundException {
        Objects.requireNonNull(id, "'id' cannot be null");
        final ArrayList<Game> games = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET,
            preparedStatement -> {
                preparedStatement.setString(1, id.toString());
            },
            resultSet -> {
                games.add(parse(resultSet));
            }
        );
        if (games.size() != 1) throw new NotFoundException("No games found matching this ID.");
        return games.get(0);
    }

    /**
     * Gets a game by the host's user ID.
     * @param id User ID to retrieve hosted game for
     * @return Game the specified user is hosting
     * @throws SQLException Unable to execute query
     * @throws NotFoundException User is not hosting a game
     */
    public static Game getGameByHost(final UUID id) throws SQLException, NotFoundException {
        Objects.requireNonNull(id, "'id' cannot be null");
        final ArrayList<Game> games = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_BY_HOST,
            preparedStatement -> {
                preparedStatement.setString(1, id.toString());
            },
            resultSet -> {
                games.add(parse(resultSet));
            }
        );
        if (games.size() != 1) throw new NotFoundException("No games found with this user as the host.");
        return games.get(0);
    }

    /**
     * Upserts a game into the database.
     * @param game Game to upsert
     * @throws SQLException Unable to execute statement
     */
    public static void putGame(final Game game) throws SQLException {
        Objects.requireNonNull(game, "'game' cannot be null");
        Database.execute(
            QUERY_PUT,
            preparedStatement -> {
                preparedStatement.setString(1, game.id().toString());
                preparedStatement.setString(2, game.name());
                preparedStatement.setLong(3, game.created().toEpochMilli());
                preparedStatement.setString(4, game.hostId().toString());
                try {
                    preparedStatement.setString(5, game.config().toJson());
                } catch (final JsonProcessingException e) {
                    throw new SQLException("Invalid JSON");
                }
                preparedStatement.setString(6, game.map());
            }
        );
    }

    /**
     * Deletes a game from the database.
     * @param id Game's ID
     * @throws SQLException Unable to execute statement
     */
    public static void deleteGame(final UUID id) throws SQLException {
        Objects.requireNonNull(id, "'id' cannot be null");
        Database.execute(
            QUERY_DELETE,
            preparedStatement -> {
                preparedStatement.setString(1, id.toString());
            }
        );
    }

    /**
     * Parses a {@link ResultSet} into a {@link Game}.
     * @param set Result of executing a query
     * @return Game returned in the query
     * @throws SQLException Unable to parse
     */
    private static Game parse(final ResultSet set) throws SQLException {
        Objects.requireNonNull(set, "'set' cannot be null");
        
        GameConfig config;
        try {
            config = GameConfig.fromJson(set.getString("config"));
        } catch (final JsonProcessingException e) {
            throw new SQLException("Unable to parse JSON config.");
        }

        return new Game(
            set.getString("name"),
            UUID.fromString(set.getString("id")),
            Instant.ofEpochMilli(set.getLong("created")),
            UUID.fromString(set.getString("hostId")),
            config,
            set.getString("map")
        );
    }

	/**
	 * This class should not be instantiated.
	 */
	private Games() { throw new AssertionError(); }
}
