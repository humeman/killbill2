package today.tecktip.killbill.common.gameserver.messages.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;

import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;

/**
 * UDP response which represents invalid state.
 * @author cs
 */
public class IllegalStateExceptionData extends MessageData {
    /**
     * The reason for the illegal state
     */
    private final String reason;

    /**
     * Constructs a new outgoing illegal state argument.
     * @param reason The reason for the illegal state
     */
    public IllegalStateExceptionData(@JsonProperty("reason") final String reason) {
        super(MessageDataType.ILLEGAL_STATE_EXCEPTION);
        this.reason = reason;
    }

    /**
     * JSON property for the exception's reason.
     * @return The reason for the illegal state
     */
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }
}
