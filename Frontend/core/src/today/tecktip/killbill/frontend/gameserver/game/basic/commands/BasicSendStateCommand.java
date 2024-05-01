package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;

/**
 * Allows clients to request game state.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicSendStateCommand {
    /**
     * Incoming request game state command data.
     */
    @JsonSerialize
    public static class BasicSendStateCommandData extends MessageData {
        /**
         * Constructs an interact command.
         */
        public BasicSendStateCommandData() {
            super(MessageDataType.COMMAND_SEND_STATE);
        }
    }

    /**
     * Runs the Send State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_SEND_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(new BasicSendStateCommandData())
                    .build());
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON failure", e);
        }
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicSendStateCommand() { }
}