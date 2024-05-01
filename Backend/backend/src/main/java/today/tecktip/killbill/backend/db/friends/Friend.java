package today.tecktip.killbill.backend.db.friends;

import java.time.Instant;
import java.util.UUID;

/**
 * Data structure for a friend as retrieved from the SQL database.
 * @param fromId UUID for the requesting user
 * @param toId UUID for the receiving user
 * @param created Timestamp when the friend link was created
 * @param state Friend link's state
 */
public record Friend(
    UUID fromId,
    UUID toId,
    Instant created,
    FriendState state
) {}
