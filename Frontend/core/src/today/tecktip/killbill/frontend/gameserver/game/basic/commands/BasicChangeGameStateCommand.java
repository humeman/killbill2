package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicRecvGameStateCommand.BasicRecvGameStateData;

/**
 * Sends game state changes.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicChangeGameStateCommand {
    /**
     * Runs the Change Game State command.
     * @param handler Message handler
     * @param context Instance of {@link ChangeGameStateInvokeContext}
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_CHANGE_GAME_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        final ChangeGameStateInvokeContext ctx = (ChangeGameStateInvokeContext) context;

        final BasicRecvGameStateData data = new BasicRecvGameStateData(
            ctx.getFieldFilter().contains(GameStateFieldFilter.RUN_STATE) ? ctx.getGameState().getState() : null
        );
        
        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(data)
                    .build());
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON error", e);
        }
    }

    /**
     * Context for the change game state command invocation.
     */
    public static class ChangeGameStateInvokeContext extends InvokeContext {

        /**
         * Game state for this invocation
         */
        private BasicLocalGameState gameState;

        /**
         * Fields to filter in when sending data.
         */
        private List<GameStateFieldFilter> fieldFilter;

        /**
         * Constructs context data for the change game state command.
         * @param gameState Game state to send state for
         * @param fieldFilter Fields to send through (ie: modified fields)
         */
        public ChangeGameStateInvokeContext(final BasicLocalGameState gameState, final List<GameStateFieldFilter> fieldFilter) {
            this.gameState = gameState;
            this.fieldFilter = fieldFilter;
        }

        /**
         * Gets the game state for this invocation.
         * @return Game state to send state for
         */
        public BasicLocalGameState getGameState() {
            return gameState;
        }

        /**
         * Gets the fields to filter sends to.
         * @return Field filter
         */
        public List<GameStateFieldFilter> getFieldFilter() {
            return fieldFilter;
        }
    }

    /**
     * Defines fields to filter to when sending game states.
     * Avoids sending extraneous data.
     */
    public static enum GameStateFieldFilter {
        RUN_STATE
    }

    /**
     * This class should not be instantiated.
     */
    public BasicChangeGameStateCommand() {}
}