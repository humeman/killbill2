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
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameUserState;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicRecvPlayerStateCommand.BasicRecvPlayerStateData;

/**
 * Sends player state changes.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicChangePlayerStateCommand {
    /**
     * Runs the Change Player State command.
     * @param handler Message handler
     * @param context Instance of {@link ChangePlayerStateInvokeContext}
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_CHANGE_PLAYER_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        final ChangePlayerStateInvokeContext ctx = (ChangePlayerStateInvokeContext) context;

        final BasicRecvPlayerStateData data = new BasicRecvPlayerStateData(
            null, 
            ctx.getFieldFilter().contains(PlayerStateFieldFilter.COORDINATES) ? ctx.getUserState().getCoordinates().toList() : null,
            ctx.getFieldFilter().contains(PlayerStateFieldFilter.ROTATION) ? ctx.getUserState().getRotation() : null,
            null,
            ctx.getFieldFilter().contains(PlayerStateFieldFilter.HEALTH) ? ctx.getUserState().getHealth() : null,
            null,
            ctx.getFieldFilter().contains(PlayerStateFieldFilter.HELD_ITEM_TEXTURE) ? ctx.getUserState().getHeldItemTexture() : null,
            null
        );

        data.setType(MessageDataType.COMMAND_CHANGE_PLAYER_STATE);
        
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
     * Context for the change player state command invocation.
     */
    public static class ChangePlayerStateInvokeContext extends InvokeContext {

        /**
         * User state for this invocation
         */
        private BasicLocalGameUserState userState;

        /**
         * Fields to filter in when sending data.
         */
        private List<PlayerStateFieldFilter> fieldFilter;

        /**
         * Constructs context data for the change player state command.
         * @param userState User state to send state for
         * @param fieldFilter Fields to send through (ie: modified fields)
         */
        public ChangePlayerStateInvokeContext(final BasicLocalGameUserState userState, final List<PlayerStateFieldFilter> fieldFilter) {
            this.userState = userState;
            this.fieldFilter = fieldFilter;
        }

        /**
         * Gets the user state for this invocation.
         * @return User state to send state for
         */
        public BasicLocalGameUserState getUserState() {
            return userState;
        }

        /**
         * Gets the fields to filter sends to.
         * @return Field filter
         */
        public List<PlayerStateFieldFilter> getFieldFilter() {
            return fieldFilter;
        }
    }

    /**
     * Defines fields to filter to when sending player states.
     * Avoids sending extraneous data.
     */
    public static enum PlayerStateFieldFilter {
        COORDINATES,
        ROTATION,
        PLAYER_TYPE,
        HEALTH,
        MAX_HEALTH,
        HELD_ITEM_TEXTURE,
        TEXTURE_PREFIX
    }

    /**
     * This class should not be instantiated.
     */
    public BasicChangePlayerStateCommand() {}
}