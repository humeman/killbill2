package today.tecktip.killbill.backend.db.chat;

import java.time.Instant;
import java.util.UUID;

/**
 * Data structure for a dm as retrieved from the SQL database.
 * @param id Unique message UUID
 * @param fromId UUID for the requesting user
 * @param toId UUID for the receiving user
 * @param created Timestamp when the dm was created
 * @param message message to send from 'fromId' to 'toId'
 * @param state Whether or not the message has been read by the user it was sent to
 */
public record Dm(
    UUID id,
    UUID fromId,
    UUID toId,
    Instant created,
    String message,
    MessageState state
) {}

