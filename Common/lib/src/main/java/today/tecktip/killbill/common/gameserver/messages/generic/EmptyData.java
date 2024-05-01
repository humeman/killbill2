package today.tecktip.killbill.common.gameserver.messages.generic;

import com.fasterxml.jackson.databind.JsonNode;

import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;

/**
 * Reusable empty response designed for acknowledges with no data.
 * @author cs
 */
public class EmptyData extends MessageData {
    /**
     * Constructs a new empty message.
     */
    public EmptyData() {
        super(MessageDataType.EMPTY);
    }

    /**
     * Parses a JSON node into empty command data.
     * @param node JSON node
     * @return Parsed data
     */
    public static EmptyData parse(final JsonNode node) throws IllegalArgumentException {
        return new EmptyData();
    }
}