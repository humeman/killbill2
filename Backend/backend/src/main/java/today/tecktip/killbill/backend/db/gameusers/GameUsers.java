package today.tecktip.killbill.backend.db.gameusers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import today.tecktip.killbill.backend.db.Database;
import today.tecktip.killbill.backend.exceptions.NotFoundException;

/**
 * Database helper methods for managing game players/invitees.
 * @author cs
 */
public class GameUsers {
    /**
     * Upserts a game user.
     */
    private static String QUERY_PUT =
        "INSERT INTO GameUsers (gameId, userId, gameKey) VALUES (?, ?, ?)";
    /**
     * Gets all game users.
     */
    private static String QUERY_GET_ALL = 
        "SELECT * FROM GameUsers";
    /**
     * Gets a game user with a specified game ID.
     */
    private static String QUERY_GET_BY_GAME = 
        "SELECT * FROM GameUsers WHERE gameId = ?";
    /**
     * Gets all game users with with a specified user ID.
     */
    private static String QUERY_GET_BY_USER = 
        "SELECT * FROM GameUsers WHERE userId = ?";
    /**
     * Gets a game user with a specified game ID and user ID.
     */
    private static String QUERY_GET_SINGLE_USER = 
        "SELECT * FROM GameUsers WHERE gameId = ? AND userId = ?";
    /**
     * Deletes all game users associated with a game.
     */
    private static String QUERY_DELETE = 
        "DELETE FROM GameUsers WHERE gameId = ?";
    /**
     * Deletes a single game user.
     */
    private static String QUERY_DELETE_ONE = 
        "DELETE FROM GameUsers WHERE gameId = ? AND userId = ?";

    /**
     * Gets all game users registered in the database.
     * @return All game users
     * @throws SQLException Unable to execute query
     */
    public static List<GameUser> getGameUsers() throws SQLException {
        final ArrayList<GameUser> gameusers = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_ALL,
            resultSet -> {
                gameusers.add(parse(resultSet));
            }
        );
        return gameusers;
    }

    /**
     * Gets all game users registered in the database filtered by a game ID.
     * @param gameId Game ID to filter by
     * @return All game users in this game
     * @throws SQLException Unable to execute query
     */
    public static List<GameUser> getGameUsersByGame(final UUID gameId) throws SQLException {
        Objects.requireNonNull(gameId, "'gameId' cannot be null");
        final ArrayList<GameUser> gameusers = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_BY_GAME,
            preparedStatement -> {
                preparedStatement.setString(1, gameId.toString());
            },
            resultSet -> {
                gameusers.add(parse(resultSet));
            }
        );
        return gameusers;
    }

    /**
     * Gets all game users registered in the database filtered by a user ID.
     * @param userId User ID to filter by
     * @return All game users associated with the specified user
     * @throws SQLException Unable to execute query
     */
    public static List<GameUser> getGameUsersByUser(final UUID userId) throws SQLException {
        Objects.requireNonNull(userId, "'userId' cannot be null");
        final ArrayList<GameUser> gameusers = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_BY_USER,
            preparedStatement -> {
                preparedStatement.setString(1, userId.toString());
            },
            resultSet -> {
                gameusers.add(parse(resultSet));
            }
        );
        return gameusers;
    }

    /**
     * Gets a game user by the game and user ID.
     * @param gameId Game ID
     * @param userId User ID
     * @return Game user associated with the specified ID
     * @throws SQLException Unable to execute query
     * @throws NotFoundException Game user does not exist
     */
    public static GameUser getGameUser(final UUID gameId, final UUID userId) throws SQLException, NotFoundException {
        Objects.requireNonNull(gameId, "'gameId' cannot be null");
        Objects.requireNonNull(userId, "'userId' cannot be null");
        final ArrayList<GameUser> gameusers = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_SINGLE_USER,
            preparedStatement -> {
                preparedStatement.setString(1, gameId.toString());
                preparedStatement.setString(2, userId.toString());
            },
            resultSet -> {
                gameusers.add(parse(resultSet));
            }
        );
        if (gameusers.size() != 1) throw new NotFoundException("No game users found matching this ID.");
        return gameusers.get(0);
    }

    /**
     * Upserts a game user into the database.
     * @param gameuser Game user to upsert
     * @throws SQLException Unable to execute statement
     */
    public static void putGameUser(final GameUser gameuser) throws SQLException {
        Objects.requireNonNull(gameuser, "'gameuser' cannot be null");
        Database.execute(
            QUERY_PUT,
            preparedStatement -> {
                preparedStatement.setString(1, gameuser.gameId().toString());
                preparedStatement.setString(2, gameuser.userId().toString());
                preparedStatement.setString(3, gameuser.key().toString());
            }
        );
    }

    /**
     * Deletes all game users associated with a game from the database.
     * @param gameId Game's ID
     * @throws SQLException Unable to execute statement
     */
    public static void deleteGameUsers(final UUID gameId) throws SQLException, NotFoundException {
        Objects.requireNonNull(gameId, "'gameId' cannot be null");
        Database.execute(
            QUERY_DELETE,
            preparedStatement -> {
                preparedStatement.setString(1, gameId.toString());
            }
        );
    }

    /**
     * Deletes a game user from the database.
     * @param gameuser User to delete
     * @throws SQLException Unable to execute statement
     */
    public static void deleteGameUser(final GameUser gameuser) throws SQLException {
        Objects.requireNonNull(gameuser, "'gameuser' cannot be null");
        Database.execute(
            QUERY_DELETE_ONE,
            preparedStatement -> {
                preparedStatement.setString(1, gameuser.gameId().toString());
                preparedStatement.setString(2, gameuser.userId().toString());
            }
        );
    }

    /**
     * Parses a {@link ResultSet} into a {@link GameUser}.
     * @param set Result of executing a query
     * @return GameUser returned in the query
     * @throws SQLException Unable to parse
     */
    private static GameUser parse(final ResultSet set) throws SQLException {
        Objects.requireNonNull(set, "'set' cannot be null");
        return new GameUser(
            UUID.fromString(set.getString("gameId")),
            UUID.fromString(set.getString("userId")),
            UUID.fromString(set.getString("gameKey"))
        );
    }

	/**
	 * This class should not be instantiated.
	 */
	private GameUsers() { throw new AssertionError(); }
}
