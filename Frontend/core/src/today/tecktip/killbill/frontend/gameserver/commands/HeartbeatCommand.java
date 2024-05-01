package today.tecktip.killbill.frontend.gameserver.commands;

import com.fasterxml.jackson.core.JsonProcessingException;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;

/**
 * Heartbeat command. Clients are expected to send this once per second. If none
 *  are received for 10s or more, the client is disconnected (timed out).
 * @author cs
 */
@Command
public class HeartbeatCommand {
    /**
     * Outgoing heartbeat command data.
     */
    public static class HeartbeatCommandData extends MessageData {
        /**
         * Constructs a new outgoing heartbeat command message.
         */
        public HeartbeatCommandData() {
            super(MessageDataType.COMMAND_HEARTBEAT);
        }
    }

    /**
     * Tells the server that this client intends to connect.
     * @param handler Message handler
     * @param context Context, ignored
     * @throws JsonProcessingException Unable to serialize request as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_HEARTBEAT)
    public void invoke(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .randomMessageId()
                    .data(new HeartbeatCommandData())
                    .build()
            );
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON error", e);
        }
    }
}
