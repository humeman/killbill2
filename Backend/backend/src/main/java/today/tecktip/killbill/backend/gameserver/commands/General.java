package today.tecktip.killbill.backend.gameserver.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.exceptions.AuthenticationFailureData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.IllegalStateExceptionData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InternalServerErrorData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InvalidArgumentExceptionData;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;

/**
 * Receives any errors, forwarding them to the proper error channel.
 * @author cs
 */
@Command
public class General {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ParseMethod(type = MessageDataType.AUTHENTICATION_FAILURE)
    public AuthenticationFailureData parseAuthenticationFailure(final JsonNode node) {
        return MAPPER.convertValue(node, AuthenticationFailureData.class);
    }

    @ParseMethod(type = MessageDataType.ILLEGAL_STATE_EXCEPTION)
    public IllegalStateExceptionData parseIllegalStateException(final JsonNode node) {
        return MAPPER.convertValue(node, IllegalStateExceptionData.class);
    }

    @ParseMethod(type = MessageDataType.INTERNAL_SERVER_ERROR)
    public InternalServerErrorData parseInternalServerError(final JsonNode node) {
        return MAPPER.convertValue(node, InternalServerErrorData.class);
    }

    @ParseMethod(type = MessageDataType.INVALID_ARGUMENT_EXCEPTION)
    public InvalidArgumentExceptionData parseInvalidArgumentException(final JsonNode node) {
        return MAPPER.convertValue(node, InvalidArgumentExceptionData.class);
    }

    @ParseMethod(type = MessageDataType.EMPTY)
    public EmptyData parseEmpty(final JsonNode node) {
        return new EmptyData();
    }
}
