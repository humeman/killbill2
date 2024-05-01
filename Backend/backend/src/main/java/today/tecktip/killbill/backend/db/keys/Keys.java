package today.tecktip.killbill.backend.db.keys;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import java.time.Instant;

import today.tecktip.killbill.backend.db.Database;
import today.tecktip.killbill.backend.exceptions.NotFoundException;

/**
 * Database helper methods for managing users' keys.
 * @author cs
 */
public class Keys {
    /**
     * Upserts a key.
     */
    private static String QUERY_PUT =
        "INSERT INTO UserKeys (id, userId, created, expires) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE expires = values(expires)";
    /**
     * Gets all keys.
     */
    private static String QUERY_GET_ALL = 
        "SELECT * FROM UserKeys";
    /**
     * Gets all keys with a specified user ID.
     */
    private static String QUERY_GET = 
        "SELECT * FROM UserKeys WHERE userId = ?";
    /**
     * Gets a key with a specified user ID and key ID.
     */
    private static String QUERY_GET_ONE = 
        "SELECT * FROM UserKeys WHERE id = ? AND userId = ?";
    /**
     * Deletes all keys for a user.
     */
    private static String QUERY_DELETE = 
        "DELETE FROM UserKeys WHERE userId = ?";
    /**
     * Deletes a key.
     */
    private static String QUERY_DELETE_ONE = 
        "DELETE FROM UserKeys WHERE id = ? AND userId = ?";
    /**
     * Gets the number of keys registered to a user.
     */
    private static String QUERY_COUNT = 
        "SELECT SUM(userId = ?) FROM UserKeys";

    /**
     * Gets all keys registered in the database.
     * @return All keys
     * @throws SQLException Unable to execute query
     */
    public static List<Key> getKeys() throws SQLException {
        final ArrayList<Key> keys = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_ALL,
            resultSet -> {
                keys.add(parse(resultSet));
            }
        );
        return keys;
    }

    /**
     * Gets a list of keys by a user ID.
     * @param id User ID associated with the key
     * @return Keys associated with the specified user ID
     * @throws SQLException Unable to execute query
     */
    public static List<Key> getKeys(final UUID id) throws SQLException {
        Objects.requireNonNull(id, "'id' cannot be null");
        final ArrayList<Key> keys = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET,
            preparedStatement -> {
                preparedStatement.setString(1, id.toString());
            },
            resultSet -> {
                keys.add(parse(resultSet));
            }
        );
        return keys;
    }

    /**
     * Gets a single key by a user ID.
     * @param id User ID associated with the key
     * @param userId Key ID
     * @return Key associated with the specified user ID
     * @throws SQLException Unable to execute query
     * @throws NotFoundException Key does not exist
     */
    public static Key getKey(final UUID id, final UUID userId) throws SQLException, NotFoundException {
        Objects.requireNonNull(id, "'id' cannot be null");
        Objects.requireNonNull(userId, "'userId' cannot be null");
        final ArrayList<Key> keys = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_ONE,
            preparedStatement -> {
                preparedStatement.setString(1, id.toString());
                preparedStatement.setString(2, userId.toString());
            },
            resultSet -> {
                keys.add(parse(resultSet));
            }
        );
        if (keys.size() != 1) throw new NotFoundException("No keys found matching this ID and user.");
        return keys.get(0);
    }

    /**
     * Upserts a key into the database.
     * @param key Key to upsert
     * @throws SQLException Unable to execute statement
     */
    public static void putKey(final Key key) throws SQLException {
        Objects.requireNonNull(key, "'key' cannot be null");
        Database.execute(
            QUERY_PUT,
            preparedStatement -> {
                preparedStatement.setString(1, key.id().toString());
                preparedStatement.setString(2, key.userId().toString());
                preparedStatement.setLong(3, key.created().toEpochMilli());
                preparedStatement.setLong(4, key.expires().toEpochMilli());
            }
        );
    }

    /**
     * Deletes all keys for a user from the database.
     * @param userId User's ID
     * @throws SQLException Unable to execute statement
     */
    public static void deleteKeys(final UUID userId) throws SQLException {
        Objects.requireNonNull(userId, "'userId' cannot be null");
        Database.execute(
            QUERY_DELETE,
            preparedStatement -> {
                preparedStatement.setString(1, userId.toString());
            }
        );
    }

    /**
     * Deletes a key from the database.
     * @param key ID for the specified key
     * @throws SQLException Unable to execute statement
     */
    public static void deleteKey(final Key key) throws SQLException {
        Objects.requireNonNull(key, "'key' cannot be null");
        Database.execute(
            QUERY_DELETE_ONE,
            preparedStatement -> {
                preparedStatement.setString(1, key.id().toString());
                preparedStatement.setString(2, key.userId().toString());
            }
        );
    }

    /**
     * Retrieves the number of keys active on a user's account.
     * @param userId User to check key count for
     * @return Number of keys
     * @throws SQLException Unable to contact database.
     */
    public static int keyCount(UUID userId) throws SQLException {
        Objects.requireNonNull(userId, "'userId' cannot be null");
        AtomicInteger count = new AtomicInteger(0);
        Database.executeQuery(
            QUERY_COUNT,
            preparedStatement -> {
                preparedStatement.setString(1, userId.toString());
            },
            resultSet -> {
                count.set(resultSet.getInt(1));
            }
        );
        return count.get();
    }

    /**
     * Parses a {@link ResultSet} into a {@link Key}.
     * @param set Result of executing a query
     * @return Key returned in the query
     * @throws SQLException Unable to parse
     */
    private static Key parse(final ResultSet set) throws SQLException {
        Objects.requireNonNull(set, "'set' cannot be null");
        return new Key(
            UUID.fromString(set.getString("id")),
            UUID.fromString(set.getString("userId")),
            Instant.ofEpochMilli(set.getLong("created")),
            Instant.ofEpochMilli(set.getLong("expires"))
        );
    }

	/**
	 * This class should not be instantiated.
	 */
	private Keys() { throw new AssertionError(); }
}
