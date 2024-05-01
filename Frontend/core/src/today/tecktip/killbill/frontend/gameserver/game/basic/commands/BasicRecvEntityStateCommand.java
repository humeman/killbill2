package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.CommandContext;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.CommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.annotations.ResponseMethod;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;
import today.tecktip.killbill.common.maploader.directives.EntityDirective.EntityType;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalEntityState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState;

/**
 * Sends an entity's state to connected clients.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicRecvEntityStateCommand {
    /**
     * JSON deserializer
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Data attached to refresh entity state commands or get entity state responses.
     */
    public static class BasicRecvEntityStateCommandData extends MessageData {
        /**
         * New user coordinates.
         */
        private final Coordinates coordinates;

        /**
         * New rotation in degrees.
         */
        private final Integer rotation;

        /**
         * Sequential entity ID.
         */
        private final int entityId;

        /**
         * Entity's type.
         */
        private final EntityType entityType;

        /**
         * New health.
         */
        private final Integer health;

        /**
         * New state.
         */
        private final Integer state;

        /**
         * New held item texture.
         */
        private final String heldItemTexture;

        /**
         * New texture prefix.
         */
        private final String texturePrefix;

        /**
         * Constructs a new basic refresh entity state client command response.
         */
        public BasicRecvEntityStateCommandData(
            @JsonProperty(value = "entityId", required = true) int entityId,
            @JsonProperty(value = "coordinates", required = false) List<Double> coordinates,
            @JsonProperty(value = "rotation", required = false) Integer rotation,
            @JsonProperty(value = "entityType", required = false) String entityType,
            @JsonProperty(value = "health", required = false) Integer health,
            @JsonProperty(value = "state", required = false) Integer state,
            @JsonProperty(value = "heldItemTexture", required = false) String heldItemTexture,
            @JsonProperty(value = "texturePrefix", required = false) String texturePrefix
        ) {
            super(MessageDataType.COMMAND_RECV_ENTITY_STATE);
            this.entityId = entityId;
            if (coordinates != null)
                this.coordinates = Coordinates.fromList(coordinates);
            else
                this.coordinates = null;
            this.rotation = rotation;
            if (entityType != null)
                this.entityType = EntityType.valueOf(entityType);
            else
                this.entityType = null;
            this.health = health;
            this.state = state;
            this.heldItemTexture = heldItemTexture;
            this.texturePrefix = texturePrefix;
        }

        /**
         * Overrides the message data type.
         * @param type Data type
         */
        public void setType(final MessageDataType type) {
            this.type = type;
        }

        /**
         * Gets the coordinates JSON property for this command.
         * @return Coordinates
         */
        public Coordinates getCoordinates() {
            if (coordinates == null) return null;
            return coordinates;
        }

        /**
         * Gets the entity ID JSON property for this command.
         * @return Entity ID
         */
        public int getEntityId() {
            return entityId;
        }

        /**
         * Gets the user's new rotation.
         * @return Rotation in degrees
         */
        public Integer getRotation() {
            return rotation;
        }

        /**
         * Gets the entity's type.
         * @return Entity type
         */
        public EntityType getEntityType() {
            return entityType;
        }

        /**
         * Gets the entity's new health.
         * @return Health
         */
        public Integer getHealth() {
            return health;
        }

        /**
         * Gets the entity's new state.
         * @return State ID
         */
        public Integer getState() {
            return state;
        }

        /**
         * Gets the user's new held item texture.
         * @return Held item texture
         */
        public String getHeldItemTexture() {
            return heldItemTexture;
        }
        
        /**
         * Gets the user's new texture prefix.
         * @return Texture asset prefix
         */
        public String getTexturePrefix() {
            return texturePrefix;
        }
    }

    /**
     * Outgoing get state command data.
     */
    public static class BasicGetEntityStateCommandData extends MessageData {
        /**
         * Target entity ID.
         */
        private int entityId;
        
        /**
         * Constructs get entity state request data.
         * @param entityId ID of the entity to retrieve state for.
         */
        public BasicGetEntityStateCommandData(
            int entityId
        ) {
            super(MessageDataType.COMMAND_GET_ENTITY_STATE);
            this.entityId = entityId;
        }

        /**
         * Gets the targeted entity's ID.
         * @return Entity ID
         */
        @JsonProperty("entityId")
        public int getEntityId() {
            return entityId;
        }
    }

    @ParseMethod(type = MessageDataType.COMMAND_RECV_ENTITY_STATE)
    public BasicRecvEntityStateCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicRecvEntityStateCommandData.class);
    }

    @ParseMethod(type = MessageDataType.RESP_GET_ENTITY_STATE)
    public BasicRecvEntityStateCommandData parseGet(final JsonNode node) {
        final BasicRecvEntityStateCommandData data = MAPPER.convertValue(node, BasicRecvEntityStateCommandData.class);
        data.setType(MessageDataType.RESP_GET_ENTITY_STATE);
        return data;
    }

    /**
     * Incoming remove entity command data.
     */
    public static class BasicRecvRemoveEntityStateCommandData extends MessageData {
        /**
         * Target entity ID.
         */
        private int entityId;

        private EntityRemovalType removalType;
        
        /**
         * Constructs get entity state request data.
         * @param entityId ID of the entity to retrieve state for.
         */
        public BasicRecvRemoveEntityStateCommandData(
            @JsonProperty(value = "entityId", required = true) final int entityId,
            @JsonProperty(value = "removalType", required = true) final EntityRemovalType removalType
        ) {
            super(MessageDataType.COMMAND_RECV_REMOVE_ENTITY);
            this.entityId = entityId;
            this.removalType = removalType;
        }

        /**
         * Gets the targeted entity's ID.
         * @return Entity ID
         */
        public int getEntityId() {
            return entityId;
        }

        /**
         * Gets the targeted entity's removal reason.
         * @return Removal reason
         */
        public EntityRemovalType getRemovalType() {
            return removalType;
        }
    }

    @ParseMethod(type = MessageDataType.COMMAND_RECV_REMOVE_ENTITY)
    public BasicRecvRemoveEntityStateCommandData parseRemove(final JsonNode node) {
        return MAPPER.convertValue(node, BasicRecvRemoveEntityStateCommandData.class);
    }

    /**
     * Runs the Get Entity State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_GET_ENTITY_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        final GetEntityStateInvokeContext ctx = (GetEntityStateInvokeContext) context;

        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(new BasicGetEntityStateCommandData(ctx.getEntityId()))
                    .build());
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON failure", e);
        }
    }

    /**
     * Runs the Recv Entity State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_ENTITY_STATE)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
        final BasicLocalGameState game = (BasicLocalGameState) ClientMessageHandler.get().getGameState();
        final BasicRecvEntityStateCommandData data = (BasicRecvEntityStateCommandData) message.data();

        // Find the specified entity
        BasicLocalEntityState targetEntity = game.getEntity(data.getEntityId());
        if (targetEntity == null) {
            // This user has new state, but has not been initialized yet.
            // We can only initialize from this if this is a full state.
            // Otherwise, send a GET for the full state.
            if (data.getEntityType() != null && data.getCoordinates() != null && data.getHealth() != null) {
                game.setEntity(
                    data.getEntityId(),
                    new BasicLocalEntityState(
                        data.getEntityId(),
                        data.getEntityType(),
                        data.getCoordinates(),
                        data.getHealth(),
                        data.getTexturePrefix()
                ));

                targetEntity = game.getEntity(data.getEntityId());
                targetEntity.setRotation(data.getRotation());
            } else {
                handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_GET_ENTITY_STATE)
                    .run(handler, new GetEntityStateInvokeContext(data.getEntityId()));
            }
        }

        if (targetEntity != null) {
            // Run a state update on the entity.
            updateState(targetEntity, data, message);
        }

        
        if (message.messageId() != null) {
            try {
                handler.send(
                    null,
                    OutgoingMessage.newBuilder()
                        .setKey(handler)
                        .success()
                        .ackMessageId(message.messageId())
                        .data(new EmptyData())
                        .build());
            } catch (final JsonProcessingException e) {
                throw new MessageFailure("JSON error", e);
            }
        }
    }

    /**
     * Runs the Get Entity State response.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @ResponseMethod(type = MessageDataType.RESP_GET_ENTITY_STATE)
    public void runResp(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
        final BasicLocalGameState game = (BasicLocalGameState) ClientMessageHandler.get().getGameState();
        final BasicRecvEntityStateCommandData data = (BasicRecvEntityStateCommandData) message.data();

        // Find the specified entity
        BasicLocalEntityState targetEntity = game.getEntity(data.getEntityId());
        if (targetEntity == null) {
            // This user has new state, but has not been initialized yet.
            // Add them to the state and kick off the init process.
            game.setEntity(
                data.getEntityId(),
                new BasicLocalEntityState(
                    data.getEntityId(),
                    data.getEntityType(),
                    data.getCoordinates(),
                    data.getHealth(),
                    data.getTexturePrefix()
            ));

            targetEntity = game.getEntity(data.getEntityId());
            targetEntity.setRotation(data.getRotation());
        }
        // Run a state update on the entity.
        updateState(targetEntity, data, message);
    }

    private void updateState(final BasicLocalEntityState entity, final BasicRecvEntityStateCommandData data, final IncomingMessage message) {
        if (data.getRotation() != null) {
            entity.setRotation(message.createdAt(), data.getRotation());
        }

        if (data.getCoordinates() != null) {
            entity.setCoordinates(message.createdAt(), data.getCoordinates());
        }

        if (data.getHealth() != null) {
            entity.setHealth(message.createdAt(), data.getHealth());
        }

        if (data.getHeldItemTexture() != null) {
            entity.setHeldItemTexture(message.createdAt(), data.getHeldItemTexture());
        }

        if (data.getTexturePrefix() != null) {
            entity.setTexturePrefix(message.createdAt(), data.getTexturePrefix());
        }

        if (data.getState() != null) {
            entity.setState(message.createdAt(), data.getState());
        }
    }

    /**
     * Runs the Receive Remove Entity client command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_REMOVE_ENTITY)
    public void runRemove(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
        BasicLocalGameState gameState = (BasicLocalGameState) ClientMessageHandler.get().getGameState();

        gameState.removeEntity(((BasicRecvRemoveEntityStateCommandData) message.data()).getEntityId());

        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .ackMessageId(message.messageId())
                    .data(new EmptyData())
                    .build());
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON error", e);
        }
    }

    /**
     * Context for the get entity state command invocation.
     */
    public static class GetEntityStateInvokeContext extends InvokeContext {
        /**
         * Entity state for this invocation
         */
        private final int entityId;

        /**
         * Constructs context data for the receive player state command.
         * @param entityId ID of the entity to retrieve
         */
        public GetEntityStateInvokeContext(final int entityId) {
            this.entityId = entityId;
        }

        /**
         * Gets the entity ID for this invocation.
         * @return Entity ID to send state for
         */
        public int getEntityId() {
            return entityId;
        }
    }

    public static enum EntityRemovalType {
        DESPAWN,
        DIE
    }
    

    /**
     * This class should not be instantiated.
     */
    public BasicRecvEntityStateCommand() {}
}