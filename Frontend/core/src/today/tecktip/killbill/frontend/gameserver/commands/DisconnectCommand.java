package today.tecktip.killbill.frontend.gameserver.commands;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;

/**
 * Ends a connection with the server.
 * @author cs
 */
@Command
public class DisconnectCommand {

    /**
     * Outgoing disconnect command data. Empty at this time.
     */
    @JsonSerialize
    public class DisconnectCommandData extends MessageData {
        /**
         * Constructs data for the connect command.
         */
        public DisconnectCommandData() {
            super(MessageDataType.COMMAND_DISCONNECT);
        }
    }

    /**
     * Tells the server that this client intends to connect.
     * @param handler Message handler
     * @throws JsonProcessingException Unable to serialize request as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_DISCONNECT)
    public void disconnect(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        // Bypasses ready state
        try {
            ((ClientMessageHandler) handler).sendImmediately(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .randomMessageId()
                    .data(new DisconnectCommandData())
                    .build()
            );
        } catch (IOException e) {
            throw new MessageFailure("Unable to send message immediately: ", e);
        }
        Gdx.app.log(DisconnectCommand.class.getSimpleName(), "Sent disconnect command to server.");
    }
}
