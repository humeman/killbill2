package today.tecktip.killbill.common.gameserver.messages;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import today.tecktip.killbill.common.gameserver.CommandLoader;
import today.tecktip.killbill.common.gameserver.games.GameType;

/**
 * Represents a base incoming message.
 * @param createdAt When the message was generated.
 * @param messageId Message id to be acknowledged in the response
 * @param ackMessageId Message ID this message is acknowledging
 * @param data Message payload
 * @param key Game API key
 * 
 * @author cs
 */
public record IncomingMessage(
    Instant createdAt,
    UUID messageId,
    UUID ackMessageId,
    MessageData data,
    String key
) {
    /**
     * Generates an IncomingMessage from a JSON node.
     * @param gameType Type of game being played. Leave as null for no data parsing.
     * @param commandLoader Command loader (should be already loaded). Leave as null for no data parsing.
     * @param node JSON payload for the message
     * @return Parsed message
     */
    public static IncomingMessage from(final GameType gameType, final CommandLoader commandLoader, final JsonNode node) {
        Instant createdAt = null;
        UUID messageId = null;
        UUID ackMessageId = null;

        JsonNode createdAtNode = node.get("createdAt");
        if (!createdAtNode.isLong()) {
            throw new IllegalArgumentException("'createdAt' must be supplied as milliseconds since epoch.");
        }
        long createdAtMs = createdAtNode.longValue();
        createdAt = Instant.ofEpochMilli(createdAtMs);

        JsonNode msgIdNode = node.get("messageId");
        JsonNode ackMsgIdNode = node.get("ackMessageId");
        if (msgIdNode != null && !msgIdNode.isNull()) {
            try {
                messageId = UUID.fromString(msgIdNode.asText());
            } catch (final Throwable e) {
                throw new IllegalArgumentException("Could not parse message ID as UUID.", e);
            }
        } else if (ackMsgIdNode != null && !ackMsgIdNode.isNull()) {
            try {
                ackMessageId = UUID.fromString(ackMsgIdNode.asText());
            } catch (final Throwable e) {
                throw new IllegalArgumentException("Could not parse ack message ID as UUID.");
            }
        }

        final String key = node.get("key").textValue();
        if (key == null) {
            throw new IllegalArgumentException("Incoming messages must contain a game key field.");
        }

        JsonNode dataJson = node.get("data");
        if (dataJson == null) throw new IllegalArgumentException("Missing 'data' field (Object, required).");
        if (!dataJson.isObject()) throw new IllegalArgumentException("'data' field must be an Object.");

        MessageData data = null;
        if (gameType != null && commandLoader != null) {
            data = MessageData.from(gameType, commandLoader, dataJson);
        }
        return new IncomingMessage(createdAt, messageId, ackMessageId, data, key);
    }
}
