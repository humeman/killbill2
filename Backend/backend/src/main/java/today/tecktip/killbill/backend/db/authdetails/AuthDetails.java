package today.tecktip.killbill.backend.db.authdetails;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import today.tecktip.killbill.backend.db.Database;
import today.tecktip.killbill.backend.exceptions.NotFoundException;

/**
 * Database helper methods for managing emails and passwords.
 * <p>
 * <strong>SECURITY WARNING!</strong> Never use this class directly. All password authentication
 * should be done through the {@link today.tecktip.killbill.backend.auth.PasswordAuthenticator}.
 * @author cs
 */
public class AuthDetails {
    /**
     * Upserts an auth detail.
     */
    private static String QUERY_PUT =
        "INSERT INTO UserAuthDetails (userId, updated, password, email) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE updated = values(updated), password = values(password), email = values(email)";
    /**
     * Gets an auth detail password for a specified user.
     */
    private static String QUERY_GET = 
        "SELECT * FROM UserAuthDetails WHERE userId = ?";
    /**
     * Deletes an auth detail.
     */
    private static String QUERY_DELETE = 
        "DELETE FROM UserAuthDetails WHERE userId = ?";

    /**
     * Gets an auth detail by a user's ID.
     * @param userId User ID to retrieve
     * @return Auth detail associated with the specified user
     * @throws SQLException Unable to execute query
     * @throws NotFoundException Auth detail does not exist
     */
    public static AuthDetail getAuthDetail(final UUID userId) throws SQLException, NotFoundException {
        Objects.requireNonNull(userId, "'userId' cannot be null");
        final ArrayList<AuthDetail> details = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET,
            preparedStatement -> {
                preparedStatement.setString(1, userId.toString());
            },
            resultSet -> {
                details.add(parse(resultSet));
            }
        );
        if (details.size() != 1) throw new NotFoundException("No details found matching this user ID.");
        return details.get(0);
    }

    /**
     * Upserts an auth detail into the database.
     * @param detail Auth detail to upsert
     * @throws SQLException Unable to execute statement
     */
    public static void putAuthDetail(final AuthDetail detail) throws SQLException {
        Objects.requireNonNull(detail, "'detail' cannot be null");
        Database.execute(
            QUERY_PUT,
            preparedStatement -> {
                preparedStatement.setString(1, detail.userId().toString());
                preparedStatement.setLong(2, detail.updated().toEpochMilli());
                preparedStatement.setString(3, detail.password());
                preparedStatement.setString(4, detail.email());

            }
        );
    }

    /**
     * Deletes an auth detail from the database.
     * @param userId User's ID
     * @throws SQLException Unable to execute statement
     */
    public static void deleteAuthDetail(final UUID userId) throws SQLException {
        Objects.requireNonNull(userId, "'userId' cannot be null");
        Database.execute(
            QUERY_DELETE,
            preparedStatement -> {
                preparedStatement.setString(1, userId.toString());
            }
        );
    }

    /**
     * Parses a {@link ResultSet} into a {@link AuthDetail}.
     * @param set Result of executing a query
     * @return Auth detail returned in the query
     * @throws SQLException Unable to parse
     */
    private static AuthDetail parse(final ResultSet set) throws SQLException {
        Objects.requireNonNull(set, "'set' cannot be null");
        return new AuthDetail(
            UUID.fromString(set.getString("userId")),
            Instant.ofEpochMilli(set.getLong("updated")),
            set.getString("password"),
            set.getString("email")
        );
    }

    /**
	 * This class should not be instantiated.
	 */
	private AuthDetails() { throw new AssertionError(); }
}
