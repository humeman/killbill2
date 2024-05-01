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
import today.tecktip.killbill.common.gameserver.messages.exceptions.IllegalStateExceptionData;

/**
 * Disconnect command.
 * @author cs
 */
@Command
public class DisconnectCommand {
    /**
     * Incoming disconnect command data.
     */
    public static class DisconnectCommandIncomingData extends MessageData {
        /**
         * Constructs a new incoming disconnect command message.
         */
        public DisconnectCommandIncomingData() {
            super(MessageDataType.COMMAND_DISCONNECT);
        }

        /**
         * Parses a JSON node into disconnect command data.
         * @param node JSON node
         * @return Parsed data
         */
        public static DisconnectCommandIncomingData parse(final JsonNode node) {
            return new DisconnectCommandIncomingData();
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_DISCONNECT)
    public DisconnectCommandIncomingData parse(final JsonNode node) {
        return DisconnectCommandIncomingData.parse(node);
    }

    /**
     * Runs the disconnect command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     * @throws MessageHandlingException Reply failed
     */
    @CommandMethod(type = MessageDataType.COMMAND_DISCONNECT)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws JsonProcessingException, MessageHandlingException {
        final GameUserState user = SpringMessageHandler.userStateFrom(context);

        if (!user.isConnected()) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("COMMAND_DISCONNECT requires connection."))
                    .build());
            return;
        }

        user.disconnect();
    }

    /**
     * This class should not be manually instantiated.
     */
    public DisconnectCommand() {  }
}
