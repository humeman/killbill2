package today.tecktip.killbill.common.gameserver.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import today.tecktip.killbill.common.gameserver.CommandLoader;
import today.tecktip.killbill.common.gameserver.games.GameType;

/**
 * Represents the base inner data of a message. Extended to represent an ingoing or
 * outgoing command.
 * @author cs
 */
public abstract class MessageData {
    /**
     * The type of this message data.
     */
    protected MessageDataType type;

    /**
     * Constructs a new {@link MessageData} instance.
     * @param type Type of this message data
     */
    protected MessageData(final MessageDataType type) {
        this.type = type;
    }

    /**
     * Retrieves the type of message data this contains.
     * @return Data type of this message's data.
     */
    @JsonProperty("type")
    public MessageDataType getType() {
        return type;
    }

    /**
     * Parses out the MessageData of this JSON object depending on the supplied type.
     * @param gameType Type of game being played
     * @param commandLoader Command loader (should be already loaded)
     * @param json The "data" field of the message received
     * @return A parsed data object
     */
    public static MessageData from(final GameType gameType, final CommandLoader commandLoader, final JsonNode json) {
        // Convert type to a type enum
        if (!json.has("type")) {
            throw new IllegalArgumentException("Message data must include a 'type' field.");
        }

        final String typeStr = json.get("type").asText();
        final MessageDataType type;
        try {
            type = MessageDataType.valueOf(typeStr);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid message data type.");
        }

        MessageDataParseMethod method = commandLoader.parseMethodFor(gameType, type);

        if (method == null) {
            throw new UnsupportedOperationException("This data type is not currently accepted.");
        }
        return method.parse(json);
    }

    /**
     * A method used to parse incoming message data JSON into {@link MessageData}.
     */
    public static interface MessageDataParseMethod {
        /**
         * Parses a JSON payload into {@link MessageData}.
         * @param node JSON payload
         * @return Resulting message data instance
         * @throws IllegalArgumentException Invalid data. Message is sent back to user.
         */
        public MessageData parse(final JsonNode node) throws IllegalArgumentException;
    }
}
