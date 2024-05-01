package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.maploader.directives.EntityDirective.EntityType;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalEntityState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalEntityState.EntityStateFieldFilter;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangePlayerStateCommand.ChangePlayerStateInvokeContext;

/**
 * Sends entity state changes.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicChangeEntityStateCommand {

    public static class BasicChangeEntityStateCommandData extends MessageData {
        /**
         * The damage to deal to the player.
         */
        private final Integer damage;

        /**
         * The target entity's ID.
         */
        private final int entityId;

        /**
         * Constructs a new change entity state command message.
         */
        public BasicChangeEntityStateCommandData(
            final int entityId,
            final Integer damage
        ) {
            super(MessageDataType.COMMAND_CHANGE_ENTITY_STATE);
            this.damage = damage;
            this.entityId = entityId;
        }

        /**
         * Gets the target entity's ID.
         * @return Target entity ID
         */
        @JsonProperty("entityId")
        public int getEntityId() {
            return entityId;
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

    public static class BasicSummonEntityCommandData extends MessageData {
        /**
         * The entity to summon.
         */
        private final Coordinates coordinates;

        /**
         * The target entity's type.
         */
        private final EntityType entityType;

        /**
         * Constructs a new summon entity command message.
         */
        public BasicSummonEntityCommandData(
            final Coordinates coordinates,
            final EntityType entityType
        ) {
            super(MessageDataType.COMMAND_SUMMON_ENTITY);
            this.coordinates = coordinates;
            this.entityType = entityType;
        }

        /**
         * Gets the target entity's type.
         * @return Target entity's type
         */
        public EntityType getEntityType() {
            return entityType;
        }

        /**
         * Gets the entity type JSON property.
         * @return Entity type as a string
         */
        @JsonProperty("entityType")
        public String getEntityTypeJsonProperty() {
            return entityType.toString();
        }

        /**
         * Gets the coordinates to spawn the entity at.
         * @return Coordinates
         */
        public Coordinates getCoordinates() {
            return coordinates;
        }

        /**
         * Gets the coordinates JSON property.
         * @return Coordinates as a list of doubles
         */
        @JsonProperty("coordinates")
        public List<Double> getCoordinatesJsonProperty() {
            return coordinates.toList();
        }
    }

    /**
     * Runs the Change Other Player State command.
     * @param handler Message handler
     * @param context Instance of {@link ChangePlayerStateInvokeContext}
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_CHANGE_ENTITY_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        final ChangeEntityStateInvokeContext ctx = (ChangeEntityStateInvokeContext) context;

        final BasicChangeEntityStateCommandData data = new BasicChangeEntityStateCommandData(
            ctx.getEntityState().getId(),
            ctx.getFieldFilter().contains(EntityStateFieldFilter.DAMAGE) ? ctx.getEntityState().getDamage() : null
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
    public static class ChangeEntityStateInvokeContext extends InvokeContext {

        /**
         * Entity state for this invocation
         */
        private BasicLocalEntityState entityState;

        /**
         * Fields to filter in when sending data.
         */
        private List<EntityStateFieldFilter> fieldFilter;

        /**
         * Constructs context data for the change entity state command.
         */
        public ChangeEntityStateInvokeContext(final BasicLocalEntityState entityState, final List<EntityStateFieldFilter> fieldFilter) {
            this.entityState = entityState;
            this.fieldFilter = fieldFilter;
        }

        /**
         * Gets the entity state for this invocation.
         * @return Entity state to send state for
         */
        public BasicLocalEntityState getEntityState() {
            return entityState;
        }

        /**
         * Gets the fields to filter sends to.
         * @return Field filter
         */
        public List<EntityStateFieldFilter> getFieldFilter() {
            return fieldFilter;
        }
    }

    /**
     * Runs the Summon Entity command.
     * @param handler Message handler
     * @param context Instance of {@link ChangePlayerStateInvokeContext}
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_SUMMON_ENTITY)
    public void sendSummon(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        final SummonEntityInvokeContext ctx = (SummonEntityInvokeContext) context;

        final BasicSummonEntityCommandData data = new BasicSummonEntityCommandData(
            ctx.getCoordinates(),
            ctx.getEntityType()
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
     * Context for the summon entity command invocation.
     */
    public static class SummonEntityInvokeContext extends InvokeContext {

        /**
         * Entity type
         */
        private EntityType entityType;

        /**
         * Spawnpoint coordinates
         */
        private Coordinates coordinates;

        /**
         * Constructs context data for the change entity state command.
         */
        public SummonEntityInvokeContext(final Coordinates coordinates, final EntityType entityType) {
            this.entityType = entityType;
            this.coordinates = coordinates;
        }

        /**
         * Gets the entity type for this invocation.
         * @return Entity type to send state for
         */
        public EntityType getEntityType() {
            return entityType;
        }

        /**
         * Gets the entity's spawnpoint.
         * @return Coordinates
         */
        public Coordinates getCoordinates() {
            return coordinates;
        }
    }

    /**
     * This class should not be instantiated.
     */
    public BasicChangeEntityStateCommand() {}
}