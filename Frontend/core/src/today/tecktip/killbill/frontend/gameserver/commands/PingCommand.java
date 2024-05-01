package today.tecktip.killbill.frontend.gameserver.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.CommandContext;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.annotations.ResponseMethod;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;

/**
 * Pings the server.
 * @author cs
 */
@Command
public class PingCommand {
    /**
     * Logs go here
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(PingCommand.class);

    /**
     * Outgoing ping command data. Empty at this time and forever.
     */
    @JsonSerialize
    public class PingCommandData extends MessageData {
        /**
         * Constructs data for the connect command.
         */
        public PingCommandData() {
            super(MessageDataType.COMMAND_PING);
        }
    }

    /**
     * Incoming connect response data.
     */
    public static class PingResponseData extends MessageData {
        /**
         * Response message.
         */
        private final String message;

        /**
         * Constructs a new ping command response.
         * @param message Message to send back
         */
        public PingResponseData(final String message) {
            super(MessageDataType.RESP_PING);
            this.message = message;
        }
        
        /**
         * Gets the message sent back by the server.
         * @return Message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Parses a JSON node into connect response data.
         * @param node JSON node
         * @return Parsed data
         */
        public static PingResponseData parse(final JsonNode node) {
            return new PingResponseData(node.get("message").textValue());
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.RESP_PING)
    public PingResponseData parse(final JsonNode node) {
        return PingResponseData.parse(node);
    }

    /**
     * Tells the server to respond to a ping with some data.
     * @param handler Message handler
     * @param context Invoke context, ignored
     * @throws JsonProcessingException Unable to serialize request as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_PING)
    public void invoke(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .randomMessageId()
                    .data(new PingCommandData())
                    .build()
            );
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON error", e);
        }
    }

    /**
     * Handles the ping response sent by the server.
     * @param handler Message handler
     * @param message Message
     * @param context Context
     */
    @ResponseMethod(type = MessageDataType.RESP_PING)
    public void handleResponse(final MessageHandler handler, final IncomingMessage message, final CommandContext context) {
        LOGGER.info("Ping! The server says: ", ((PingResponseData) message.data()).getMessage());
    }
}
