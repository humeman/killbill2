package today.tecktip.killbill.backend.db.users;

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
 * Database helper methods for managing users.
 * @author cs
 */
public class Users {
    /**
     * Upserts a user.
     */
    private static String QUERY_PUT =
        "INSERT INTO Users (id, created, name, role, winsAsBill, winsAsPlayer, playtime) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = values(name), role = values(role), winsAsBill = values(winsAsBill), winsAsPlayer = values(winsAsPlayer), playtime = values(playtime)";
    /**
     * Gets all users.
     */
    private static String QUERY_GET_ALL = 
        "SELECT * FROM Users";
    /**
     * Gets a user with a specified ID.
     */
    private static String QUERY_GET = 
        "SELECT * FROM Users WHERE id = ?";
    /**
     * Searches for users with a particular name.
     */
    private static String QUERY_GET_BY_NAME = 
        "SELECT * FROM Users WHERE UPPER(name) LIKE UPPER(?)";
    /**
     * Deletes a user.
     */
    private static String QUERY_DELETE = 
        "DELETE FROM Users WHERE id = ?";

    /**
     * Gets all users registered in the database.
     * @return All users
     * @throws SQLException Unable to execute query
     */
    public static List<User> getUsers() throws SQLException {
        final ArrayList<User> users = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_ALL,
            resultSet -> {
                users.add(parse(resultSet));
            }
        );
        return users;
    }

    /**
     * Gets a single user by a user ID.
     * @param id User ID to retrieve
     * @return User associated with the specified ID
     * @throws SQLException Unable to execute query
     * @throws NotFoundException User does not exist
     */
    public static User getUser(final UUID id) throws SQLException, NotFoundException {
        Objects.requireNonNull(id, "'id' cannot be null");
        final ArrayList<User> users = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET,
            preparedStatement -> {
                preparedStatement.setString(1, id.toString());
            },
            resultSet -> {
                users.add(parse(resultSet));
            }
        );
        if (users.size() != 1) throw new NotFoundException("No users found matching this ID.");
        return users.get(0);
    }

    /**
     * Searches for a user by their username.
     * @param name User name to search for
     * @return User associated with this name
     * @throws SQLException Unable to execute query
     * @throws NotFoundException User does not exist
     */
    public static User getUserByName(final String name) throws SQLException, NotFoundException {
        Objects.requireNonNull(name, "'name' cannot be null");
        final ArrayList<User> users = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_BY_NAME,
            preparedStatement -> {
                preparedStatement.setString(1, name.toString());
            },
            resultSet -> {
                users.add(parse(resultSet));
            }
        );
        if (users.size() != 1) throw new NotFoundException("No users found matching this name.");
        return users.get(0);
    }

    /**
     * Upserts a user into the database.
     * @param user User to upsert
     * @throws SQLException Unable to execute statement
     */
    public static void putUser(final User user) throws SQLException {
        Objects.requireNonNull(user, "'user' cannot be null");
        Database.execute(
            QUERY_PUT,
            preparedStatement -> {
                preparedStatement.setString(1, user.id().toString());
                preparedStatement.setLong(2, user.created().toEpochMilli());
                preparedStatement.setString(3, user.name());
                preparedStatement.setString(4, user.role().toString());
                preparedStatement.setLong(5, user.winsAsBill());
                preparedStatement.setLong(6, user.winsAsPlayer());
                preparedStatement.setLong(7, user.playtime());
            }
        );
    }

    /**
     * Deletes a user from the database.
     * @param id User's ID
     * @throws SQLException Unable to execute statement
     */
    public static void deleteUser(final UUID id) throws SQLException {
        Objects.requireNonNull(id, "'id' cannot be null");
        Database.execute(
            QUERY_DELETE,
            preparedStatement -> {
                preparedStatement.setString(1, id.toString());
            }
        );
    }

    /**
     * Parses a {@link ResultSet} into a {@link User}.
     * @param set Result of executing a query
     * @return User returned in the query
     * @throws SQLException Unable to parse
     */
    private static User parse(final ResultSet set) throws SQLException {
        Objects.requireNonNull(set, "'set' cannot be null");
        return new User(
            UUID.fromString(set.getString("id")),
            Instant.ofEpochMilli(set.getLong("created")),
            set.getString("name"),
            UserRole.valueOf(set.getString("role")),
            set.getLong("winsAsBill"),
            set.getLong("winsAsPlayer"),
            set.getLong("playtime")
        );
    }

    /**
	 * This class should not be instantiated.
	 */
	private Users() { throw new AssertionError(); }
}
