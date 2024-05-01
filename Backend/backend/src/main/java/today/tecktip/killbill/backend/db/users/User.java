package today.tecktip.killbill.backend.db.users;

import java.time.Instant;
import java.util.UUID;

/**
 * Data structure for a user as retrieved from the SQL database.
 * @param id UUID for this user
 * @param created Timestamp when the user was created
 * @param name Username
 * @param role User role
 */
public record User(
    UUID id,
    Instant created,
    String name,
    UserRole role,
    Long winsAsBill,
    Long winsAsPlayer,
    Long playtime
) {}
