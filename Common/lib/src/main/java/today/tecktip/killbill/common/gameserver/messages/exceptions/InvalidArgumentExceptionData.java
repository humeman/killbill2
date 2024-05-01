package today.tecktip.killbill.common.gameserver.messages.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;

import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;

/**
 * UDP response which represents invalid input.
 * @author cs
 */
public class InvalidArgumentExceptionData extends MessageData {
    /**
     * The reason for the input being rejected
     */
    private final String reason;

    /**
     * Constructs a new outgoing illegal argument.
     * @param reason The reason for the input being rejected
     */
    public InvalidArgumentExceptionData(@JsonProperty("reason") final String reason) {
        super(MessageDataType.INVALID_ARGUMENT_EXCEPTION);
        this.reason = reason;
    }

    /**
     * JSON property for the exception's reason.
     * @return The reason for the input being rejected
     */
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }
}
