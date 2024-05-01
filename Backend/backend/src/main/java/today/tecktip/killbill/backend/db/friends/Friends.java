package today.tecktip.killbill.backend.db.friends;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import today.tecktip.killbill.backend.db.Database;
import today.tecktip.killbill.backend.exceptions.NotFoundException;

/**
 * Database helper methods for managing friends.
 * @author cs
 */
public class Friends {
    /**
     * Upserts a friend.
     */
    private static String QUERY_PUT =
        "INSERT INTO UserFriends (fromId, toId, created, state) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE created = values(created), state = values(state)";
    /**
     * Gets all friends.
     */
    private static String QUERY_GET_ALL = 
        "SELECT * FROM UserFriends";
    /**
     * Gets a friend with a specified 'from' ID.
     */
    private static String QUERY_GET = 
        "SELECT * FROM UserFriends WHERE fromId = ? OR toId = ?";
    /**
     * Gets a friend with a specified 'from' and 'to' ID.
     */
    private static String QUERY_GET_FROM_TO = 
        "SELECT * FROM UserFriends WHERE (fromId = ? AND toId = ?) OR (fromId = ? AND toId = ?)";
    /**
     * Deletes all friend mappings for a user.
     */
    private static String QUERY_DELETE_ALL = 
        "DELETE FROM UserFriends WHERE fromId = ? OR toId = ?";
    /**
     * Deletes a single friend link.
     */
    private static String QUERY_DELETE_ONE = 
        "DELETE FROM UserFriends WHERE fromId = ? AND toId = ?";

    /**
     * Gets all friends registered in the database.
     * @return All friends
     * @throws SQLException Unable to execute query
     */
    public static List<Friend> getFriends() throws SQLException {
        final ArrayList<Friend> friends = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_ALL,
            resultSet -> {
                friends.add(parse(resultSet));
            }
        );
        return friends;
    }

    /**
     * Gets a list of friend links by a user ID.
     * @param userId User ID to retrieve (from or to)
     * @return User associated with the specified ID
     * @throws SQLException Unable to execute query
     */
    public static List<Friend> getFriends(final UUID userId) throws SQLException {
        Objects.requireNonNull(userId, "'userId' cannot be null");
        final ArrayList<Friend> friends = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET,
            preparedStatement -> {
                preparedStatement.setString(1, userId.toString());
                preparedStatement.setString(2, userId.toString());
            },
            resultSet -> {
                friends.add(parse(resultSet));
            }
        );
        return friends;
    }

    /**
     * Gets an individual friend link.
     * @param userId1 First user
     * @param userId2 Second user
     * @return Friend link associating both users
     * @throws SQLException Unable to execute query
     * @throws NotFoundException User does not exist
     */
    public static Friend getFriend(final UUID userId1, final UUID userId2) throws SQLException, NotFoundException {
        Objects.requireNonNull(userId1, "'userId1' cannot be null");
        Objects.requireNonNull(userId2, "'userId2' cannot be null");
        final ArrayList<Friend> friends = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_FROM_TO,
            preparedStatement -> {
                preparedStatement.setString(1, userId1.toString());
                preparedStatement.setString(2, userId2.toString());
                preparedStatement.setString(3, userId2.toString());
                preparedStatement.setString(4, userId1.toString());
            },
            resultSet -> {
                friends.add(parse(resultSet));
            }
        );
        if (friends.size() != 1) throw new NotFoundException("No friends found matching these IDs.");
        return friends.get(0);
    }

    /**
     * Upserts a friend into the database.
     * @param friend Friend to upsert
     * @throws SQLException Unable to execute statement
     */
    public static void putFriend(final Friend friend) throws SQLException {
        Objects.requireNonNull(friend, "'friend' cannot be null");
        Database.execute(
            QUERY_PUT,
            preparedStatement -> {
                preparedStatement.setString(1, friend.fromId().toString());
                preparedStatement.setString(2, friend.toId().toString());
                preparedStatement.setLong(3, friend.created().toEpochMilli());
                preparedStatement.setString(4, friend.state().toString());
            }
        );
    }

    /**
     * Deletes a single friend link from the database.
     * @param friend Friend link
     * @throws SQLException Unable to execute statement
     */
    public static void deleteFriend(final Friend friend) throws SQLException {
        Objects.requireNonNull(friend, "'friend' cannot be null");
        Database.execute(
            QUERY_DELETE_ONE,
            preparedStatement -> {
                preparedStatement.setString(1, friend.fromId().toString());
                preparedStatement.setString(2, friend.toId().toString());
            }
        );
    }

    /**
     * Deletes all friend links associated with a user.
     * @param userId User's ID
     * @throws SQLException Unable to execute statement
     */
    public static void deleteFriends(final UUID userId) throws SQLException {
        Objects.requireNonNull(userId, "'userId' cannot be null");
        Database.execute(
            QUERY_DELETE_ALL,
            preparedStatement -> {
                preparedStatement.setString(1, userId.toString());
            }
        );
    }


    /**
     * Parses a {@link ResultSet} into a {@link Friend}.
     * @param set Result of executing a query
     * @return Friend returned in the query
     * @throws SQLException Unable to parse
     */
    private static Friend parse(final ResultSet set) throws SQLException {
        Objects.requireNonNull(set, "'set' cannot be null");
        return new Friend(
            UUID.fromString(set.getString("fromId")),
            UUID.fromString(set.getString("toId")),
            Instant.ofEpochMilli(set.getLong("created")),
            FriendState.valueOf(set.getString("state"))
        );
    }

    /**
	 * This class should not be instantiated.
	 */
	private Friends() { throw new AssertionError(); }
}
