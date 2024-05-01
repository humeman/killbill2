package today.tecktip.killbill.backend.db.chat;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import today.tecktip.killbill.backend.db.Database;
import today.tecktip.killbill.backend.exceptions.NotFoundException;

/**
 * Database to facilitate messages between 2 users
 * @author cz
 */
public class Dms {
    /**
     * Upserts a DM.
     */
    private static String QUERY_PUT =
        "INSERT INTO Dms (id, fromId, toId, created, message, state) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE created = values(created), message = values(message), state = values(state)";
    /**
     * Gets all DMs.
     */
    private static String QUERY_GET_ALL_ADMIN = 
        "SELECT * FROM Dms";
    /**
     * Gets unread DMs between to specified user.
     */
    private static String QUERY_GET_UNREAD_TO = 
        "SELECT * FROM Dms WHERE state = 'UNREAD' AND toId = ?";
    /**
     * Gets every unread DM.
     */
    private static String QUERY_GET_UNREAD_ALL_ADMIN =
        "SELECT * FROM Dms WHERE state = 'UNREAD'";
    /**
     * Gets all dms with a specified 'from' and 'to' ID.
     */
    private static String QUERY_GET_FROM_TO = 
    "SELECT * FROM Dms WHERE (fromId = ? AND toId = ?) OR (fromId = ? AND toId = ?)";
    /**
     * Gets all dms for given ID
     */
    private static String QUERY_GET_ALL_USER = 
    "SELECT * FROM Dms WHERE fromId = ?";
    /**
     * Deletes all dms to a user.
     */
    private static String QUERY_DELETE_ALL = 
        "DELETE FROM Dms WHERE (fromId = ? AND toId = ?) OR (fromId = ? AND toId = ?)";
    /**
     * Deletes a single message.
     */
    private static String QUERY_DELETE_ONE = 
        "DELETE FROM Dms WHERE id = ?";
    /** 
     * Gets the number of unread messages that user has 
    */
    private static String QUERY_COUNT_UNREAD_USER = 
        "SELECT COUNT(*) FROM Dms WHERE toId = ? AND state = 'UNREAD'";
    /**
     * Gets one message by messageId
     */
    private static String QUERY_GET_ONE =
        "SELECT * FROM Dms WHERE id = ?";
    /**
     * creates a new dm or replces a preexisting one
     * @param dm message to upsert
     * @throws SQLException unable to contact server
     */
    public static void putDm(final Dm dm) throws SQLException{
        Objects.requireNonNull(dm, "'dm' cannot be null");
        Database.execute(
        QUERY_PUT,
        preparedStatement -> {
            preparedStatement.setString(1, dm.id().toString());
            preparedStatement.setString(2, dm.fromId().toString());
            preparedStatement.setString(3, dm.toId().toString());
            preparedStatement.setLong(4, dm.created().toEpochMilli());
            preparedStatement.setString(5, dm.message());
            preparedStatement.setString(6, dm.state().toString());
        }
        );
    }

    /**
     * Gets all dms registered in the database.
     * @return All dms
     * @throws SQLException Unable to execute query
     */
    public static List<Dm> getAllDmsEver() throws SQLException {
        final ArrayList<Dm> dms = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_ALL_ADMIN,

            resultSet -> {
                dms.add(parse(resultSet));
            }
        );
        return dms;
    }

    /**
     * Deletes all dms between 2 users
     * @param userId1 person 1
     * @param userId2 person 2
     * @throws SQLException query failed
     */
    public static void deleteAll(final UUID userId1, final UUID userId2) throws SQLException {
        Objects.requireNonNull(userId1, "'userId1' cannot be null");
        Objects.requireNonNull(userId2, "'userId2' cannot be null");
           
        Database.execute(
            QUERY_DELETE_ALL,
            preparedStatement -> {
                preparedStatement.setString(1, userId1.toString());
                preparedStatement.setString(2, userId2.toString());
                preparedStatement.setString(3, userId2.toString());
                preparedStatement.setString(4, userId1.toString());
            }
        );
    }

    /**
     * Gets all Dms between userId1 and userId2
     * @param userId1 person 1
     * @param userId2 person 2
     * @return dms between the 2 users
     * @throws SQLException query failed
     */
    public static List<Dm> getAllFromTo(final UUID userId1, final UUID userId2) throws SQLException {
        Objects.requireNonNull(userId1, "'userId1' cannot be null");
        Objects.requireNonNull(userId2, "'userId2' cannot be null");
        final ArrayList<Dm> dms = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_FROM_TO,
            preparedStatement -> {
                preparedStatement.setString(1, userId1.toString());
                preparedStatement.setString(2, userId2.toString());
                preparedStatement.setString(3, userId2.toString());
                preparedStatement.setString(4, userId1.toString());
            },
            resultSet -> {
                dms.add(parse(resultSet));
            }
        );
        return dms;
    }

    /**
     * Gets all Dms that fromUser has sent
     * @param fromUser user to read from
     * @return dms from the fromuser
     * @throws SQLException query done broked
     */
    public static List<Dm> getAllFrom(final UUID fromUser) throws SQLException {
        Objects.requireNonNull(fromUser, "'fromUser' cannot be null");
        final ArrayList<Dm> dms = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_ALL_USER,
            preparedStatement -> {
                preparedStatement.setString(1, fromUser.toString());
            },
            resultSet -> {
                dms.add(parse(resultSet));
            }
        );
        return dms;
    }

    /**
     * Deletes the message with given messageId
     * @param messageId unique id for the message
     * @throws SQLException query failed
     */
    public static void deleteOne(final UUID messageId) throws SQLException {
        Objects.requireNonNull(messageId, "'messageId' cannot be null");
        Database.execute(
            QUERY_DELETE_ONE,
            preparedStatement -> {
                preparedStatement.setString(1, messageId.toString());
            }
        );
    }

    /**
     * Gets all unread Dms to a singular user.
     * @param userId User
     * @return Unread messages between those users
     * @throws SQLException Unable to execute query
     * @throws NotFoundException User does not exist
     */
    public static List<Dm> getUnreadTo(final UUID userId) throws SQLException {
        Objects.requireNonNull(userId, "'userId1' cannot be null");
        final ArrayList<Dm> dms = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_UNREAD_TO,
            preparedStatement -> {
                preparedStatement.setString(1, userId.toString());
            },
            resultSet -> {
                dms.add(parse(resultSet));
            }
        );
        return dms;
    }

    /**
     * Gets all unread dms registered in the database.
     * @return All dms
     * @throws SQLException Unable to execute query
     */
    public static List<Dm> getAllUnreadDmsEver() throws SQLException {
        final ArrayList<Dm> dms = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_UNREAD_ALL_ADMIN,

            resultSet -> {
                dms.add(parse(resultSet));
            }
        );
        return dms;
    }

    /**
     * Parses a {@link ResultSet} into a {@link Key}.
     * @param set Result of executing a query
     * @return dm returned in the query
     * @throws SQLException Unable to parse
     */
    private static Dm parse(final ResultSet set) throws SQLException {
        Objects.requireNonNull(set, "'set' cannot be null");
        return new Dm(
            UUID.fromString(set.getString("id")),
            UUID.fromString(set.getString("fromId")),
            UUID.fromString(set.getString("toId")),
            Instant.ofEpochMilli(set.getLong("created")),
            set.getString("message"),
            MessageState.valueOf(set.getString("state"))
        );
    }

    /**
     * returns the amount of unread messages that user has
     * @param toId user's id
     * @return number of unread messages
     * @throws SQLException unable to count
     */
    public static int countUnreadUser(final UUID toId) throws SQLException {
        Objects.requireNonNull(toId, "toId cannot be null");
        AtomicInteger numUnread = new AtomicInteger(0);
        Database.executeQuery(
            QUERY_COUNT_UNREAD_USER,

            preparedStatement -> {
                preparedStatement.setString(1, toId.toString());
            },
            resultSet -> {
                numUnread.set(resultSet.getInt(1));
            }
        );
        return numUnread.get();

    }

    /**
     * Gets message by searching with the ID
     * @param messageId messageID to query
     * @return message with passed ID
     * @throws SQLException it done broked
     */
    public static Dm getDmById(final UUID messageId) throws SQLException {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        final ArrayList<Dm> dm = new ArrayList<>();
        Database.executeQuery(
            QUERY_GET_ONE, 

            preparedStatement -> {
                preparedStatement.setString(1, messageId.toString());
            },
            resultSet -> {
                dm.add(parse(resultSet));
            }
        );
        if (dm.size() != 1) throw new NotFoundException("No Dm found with this ID");
        return dm.get(0);
    }

    /**
	 * This class should not be instantiated. Do not do it. I will scream
	 */
	private Dms() { throw new AssertionError(); }
}