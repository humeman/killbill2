package today.tecktip.killbill.backend.gameserver.commands;

import org.springframework.messaging.MessageHandlingException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.CommandContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.CommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;

/**
 * Basic ping command.
 * @author cs
 */
@Command
public class PingCommand {
    /**
     * Incoming ping command data.
     */
    public static class PingCommandIncomingData extends MessageData {
        /**
         * Constructs a new incoming ping command message.
         */
        public PingCommandIncomingData() {
            super(MessageDataType.COMMAND_PING);
        }

        /**
         * Parses a JSON node into ping command data.
         * @param node JSON node
         * @return Parsed data
         */
        public static PingCommandIncomingData parse(final JsonNode node) {
            return new PingCommandIncomingData();
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_PING)
    public PingCommandIncomingData parse(final JsonNode node) {
        return PingCommandIncomingData.parse(node);
    }

    /**
     * Response to ping commands.
     */
    public static class PingCommandOutgoingData extends MessageData {
        /**
         * Response message.
         */
        private final String message;

        /**
         * Constructs a new ping command response.
         * @param message Message to send back
         */
        public PingCommandOutgoingData(final String message) {
            super(MessageDataType.RESP_PING);
            this.message = message;
        }

        /**
         * Gets the message JSON property for this command.
         * @return Message
         */
        @JsonProperty("message")
        public String getMessage() {
            return message;
        }
    }

    /**
     * Runs the Ping command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     * @throws MessageHandlingException Reply failed
     */
    @CommandMethod(type = MessageDataType.COMMAND_PING)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws JsonProcessingException, MessageHandlingException {
        final GameUserState user = SpringMessageHandler.userStateFrom(context);

        user.getClient().send(
            OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .ackMessageId(message.messageId())
                .data(new PingCommandOutgoingData("Pong!"))
                .build());
    }

    /**
     * This class should not be manually instantiated.
     */
    public PingCommand() {  }
}
