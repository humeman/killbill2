package today.tecktip.killbill.common.gameserver.messages.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;

import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;

/**
 * UDP response which represents an unexpected exception.
 * @author cs
 */
public class InternalServerErrorData extends MessageData {
    /**
     * The reason for the server error
     */
    private final String reason;

    /**
     * Constructs a new outgoing internal server error.
     * @param reason The reason for the server error
     */
    public InternalServerErrorData(@JsonProperty("reason") final String reason) {
        super(MessageDataType.INTERNAL_SERVER_ERROR);
        this.reason = reason;
    }

    /**
     * JSON property for the exception's reason.
     * @return The reason for the server error
     */
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }
}
