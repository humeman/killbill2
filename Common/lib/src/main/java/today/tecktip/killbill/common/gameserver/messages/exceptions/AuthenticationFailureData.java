package today.tecktip.killbill.common.gameserver.messages.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;

import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;

/**
 * UDP response which represents an authentication failure.
 * @author cs
 */
public class AuthenticationFailureData extends MessageData {
    /**
     * The reason authentication failed.
     */
    private final String reason;

    /**
     * Constructs a new outgoing authentication failure message.
     * @param reason The reason authentication failed
     */
    public AuthenticationFailureData(@JsonProperty("reason") final String reason) {
        super(MessageDataType.AUTHENTICATION_FAILURE);
        this.reason = reason;
    }

    /**
     * JSON property for the exception's reason.
     * @return The reason authentication failed
     */
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }
}
