package today.tecktip.killbill.backend.gameserver.commands;

import org.springframework.messaging.MessageHandlingException;

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
 * Connect command.
 * @author cs
 */
@Command
public class ConnectCommand {
    /**
     * Incoming connect command data.
     */
    public static class ConnectCommandIncomingData extends MessageData {
        /**
         * Constructs a new incoming connect command message.
         */
        public ConnectCommandIncomingData() {
            super(MessageDataType.COMMAND_CONNECT);
        }

        /**
         * Parses a JSON node into connect command data.
         * @param node JSON node
         * @return Parsed data
         */
        public static ConnectCommandIncomingData parse(final JsonNode node) {
            return new ConnectCommandIncomingData();
        }
    }

    /**
     * Outgoing connect command data.
     */
    public static class ConnectCommandOutgoingData extends MessageData {
        /**
         * Constructs a new incoming connect command message.
         */
        public ConnectCommandOutgoingData() {
            super(MessageDataType.RESP_CONNECT);
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_CONNECT)
    public ConnectCommandIncomingData parse(final JsonNode node) {
        return ConnectCommandIncomingData.parse(node);
    }

    /**
     * Runs the connect command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     * @throws MessageHandlingException Reply failed
     */
    @CommandMethod(type = MessageDataType.COMMAND_CONNECT)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws JsonProcessingException, MessageHandlingException {
        final GameUserState user = SpringMessageHandler.userStateFrom(context);

        user.connect();
        user.getClient().send(
            OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .ackMessageId(message.messageId())
                .data(new ConnectCommandOutgoingData())
                .build());
    }

    /**
     * This class should not be manually instantiated.
     */
    public ConnectCommand() { }
}
