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
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;

/**
 * Heartbeat command. Clients are expected to send this once per second. If none
 *  are received for 10s or more, the client is disconnected (timed out).
 * @author cs
 */
@Command
public class HeartbeatCommand {
    /**
     * Incoming heartbeat command data.
     */
    public static class HeartbeatCommandIncomingData extends MessageData {
        /**
         * Constructs a new incoming heartbeat command message.
         */
        public HeartbeatCommandIncomingData() {
            super(MessageDataType.COMMAND_HEARTBEAT);
        }

        /**
         * Parses a JSON node into heartbeat command data.
         * @param node JSON node
         * @return Parsed data
         */
        public static HeartbeatCommandIncomingData parse(final JsonNode node) {
            return new HeartbeatCommandIncomingData();
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_HEARTBEAT)
    public HeartbeatCommandIncomingData parse(final JsonNode node) {
        return HeartbeatCommandIncomingData.parse(node);
    }

    /**
     * Runs the Heartbeat command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     * @throws MessageHandlingException Reply failed
     */
    @CommandMethod(type = MessageDataType.COMMAND_HEARTBEAT)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws JsonProcessingException, MessageHandlingException {
        final GameUserState user = SpringMessageHandler.userStateFrom(context);

        // Require connection
        if (!user.isConnected()) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("COMMAND_HEARTBEAT requires connection."))
                    .build());
            return;
        }

        // The heartbeat time will be reset by the message handler.

        // Just ack so they know we got the message.
        user.getClient().send(
            OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .ackMessageId(message.messageId())
                .data(new EmptyData())
                .build());
    }

    /**
     * This class should not be manually instantiated.
     */
    public HeartbeatCommand() {  }
}