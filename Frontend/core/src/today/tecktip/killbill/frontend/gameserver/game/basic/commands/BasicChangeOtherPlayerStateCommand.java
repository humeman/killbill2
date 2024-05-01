package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameUserState;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangePlayerStateCommand.ChangePlayerStateInvokeContext;

/**
 * Sends other player state changes.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicChangeOtherPlayerStateCommand {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BasicChangeOtherPlayerStateCommandData extends MessageData {
        /**
         * The damage to deal to the player.
         */
        private final Integer damage;

        /**
         * The target user's ID.
         */
        private final UUID userId;

        /**
         * Constructs a new change location command message.
         * @param coordinates New coordinates
         */
        public BasicChangeOtherPlayerStateCommandData(
            @JsonProperty(value = "userId", required = true) final UUID userId,
            @JsonProperty(value = "damage", required = false) final Integer damage
        ) {
            super(MessageDataType.COMMAND_CHANGE_OTHER_PLAYER_STATE);
            if (damage != null) {
                this.damage = damage;
            } else this.damage = null;

            this.userId = userId;
        }

        /**
         * Gets the target user's ID.
         * @return Target user ID
         */
        public UUID getUserId() {
            return userId;
        }

        /**
         * Gets the JSON property for the target user ID.
         * @return Target user ID as string
         */
        @JsonProperty("userId")
        public String getUserIdJsonProperty() {
            return userId.toString();
        }

        /**
         * Gets the damage applied in this command.
         * @return Damage or null
         */
        @JsonProperty("damage")
        public Integer getDamage() {
            return damage;
        }
    }

    /**
     * Runs the Change Other Player State command.
     * @param handler Message handler
     * @param context Instance of {@link ChangePlayerStateInvokeContext}
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_CHANGE_OTHER_PLAYER_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        final ChangeOtherPlayerStateInvokeContext ctx = (ChangeOtherPlayerStateInvokeContext) context;

        final BasicChangeOtherPlayerStateCommandData data = new BasicChangeOtherPlayerStateCommandData(
            ctx.getUserState().getUserId(),
            ctx.getFieldFilter().contains(OtherPlayerStateFieldFilter.DAMAGE) ? ctx.getUserState().getDamage() : null
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
     * Context for the change player state command invocation.
     */
    public static class ChangeOtherPlayerStateInvokeContext extends InvokeContext {

        /**
         * User state for this invocation
         */
        private BasicLocalGameUserState userState;

        /**
         * Fields to filter in when sending data.
         */
        private List<OtherPlayerStateFieldFilter> fieldFilter;

        /**
         * Constructs context data for the change player state command.
         * @param userState User state to send state for
         * @param fieldFilter Fields to send through (ie: modified fields)
         */
        public ChangeOtherPlayerStateInvokeContext(final BasicLocalGameUserState userState, final List<OtherPlayerStateFieldFilter> fieldFilter) {
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
        public List<OtherPlayerStateFieldFilter> getFieldFilter() {
            return fieldFilter;
        }
    }

    /**
     * Defines fields to filter to when sending player states.
     * Avoids sending extraneous data.
     */
    public static enum OtherPlayerStateFieldFilter {
        DAMAGE
    }

    /**
     * This class should not be instantiated.
     */
    public BasicChangeOtherPlayerStateCommand() {}
}