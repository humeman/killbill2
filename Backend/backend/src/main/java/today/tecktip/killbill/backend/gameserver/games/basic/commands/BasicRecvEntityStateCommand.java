package today.tecktip.killbill.backend.gameserver.games.basic.commands;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameState;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicEntityState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicEntityState.EntityStateFieldFilter;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.CommandContext;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.CommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.exceptions.IllegalStateExceptionData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InvalidArgumentExceptionData;
import today.tecktip.killbill.common.maploader.directives.EntityDirective.EntityType;

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
            int entityId,
            Coordinates coordinates,
            Integer rotation,
            EntityType entityType,
            Integer health,
            Integer state,
            String heldItemTexture,
            String texturePrefix
        ) {
            super(MessageDataType.COMMAND_RECV_ENTITY_STATE);
            this.entityId = entityId;
            this.coordinates = coordinates;
            this.rotation = rotation;
            this.entityType = entityType;
            this.health = health;
            this.state = state;
            this.heldItemTexture = heldItemTexture;
            this.texturePrefix = texturePrefix;
        }

        /**
         * Constructs refresh entity state data with all entity state data included.
         */
        public BasicRecvEntityStateCommandData(
            final MessageDataType typeOverride,
            final BasicEntityState entity
        ) {
            super(typeOverride);
            this.coordinates = entity.getCoordinates();
            this.entityId = entity.getId();
            this.rotation = entity.getRotation();
            this.entityType = entity.getType();
            this.health = entity.getHealth();
            this.state = entity.getState();
            this.heldItemTexture = entity.getHeldItemTexture();
            this.texturePrefix = entity.getTexturePrefix();
        }

        /**
         * Gets the coordinates JSON property for this command.
         * @return Coordinates
         */
        @JsonProperty("coordinates")
        public List<Double> getCoordinates() {
            if (coordinates == null) return null;
            return coordinates.toList();
        }

        /**
         * Gets the entity ID JSON property for this command.
         * @return Entity ID
         */
        @JsonProperty("entityId")
        public int getEntityId() {
            return entityId;
        }

        /**
         * Gets the user's new rotation.
         * @return Rotation in degrees
         */
        @JsonProperty("rotation")
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
         * Gets the entity's type as a JSON property.
         * @return Entity type (as a string)
         */
        @JsonProperty("entityType")
        public String getEntityTypeJsonProperty() {
            if (entityType == null) return null;
            return entityType.toString();
        }

        /**
         * Gets the entity's new health.
         * @return Health
         */
        @JsonProperty("health")
        public Integer getHealth() {
            return health;
        }

        /**
         * Gets the entity's new state.
         * @return State ID
         */
        @JsonProperty("state")
        public Integer getState() {
            return state;
        }

        /**
         * Gets the user's new held item texture.
         * @return Held item texture
         */
        @JsonProperty("heldItemTexture")
        public String getHeldItemTexture() {
            return heldItemTexture;
        }

        /**
         * Gets the user's new texture prefix.
         * @return Texture asset prefix
         */
        @JsonProperty("texturePrefix")
        public String getTexturePrefix() {
            return texturePrefix;
        }
    }

    /**
     * Incoming get state command data.
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
            @JsonProperty(value = "entityId", required = true) int entityId
        ) {
            super(MessageDataType.COMMAND_GET_ENTITY_STATE);
            this.entityId = entityId;
        }

        /**
         * Gets the targeted entity's ID.
         * @return Entity ID
         */
        public int getEntityId() {
            return entityId;
        }
    }

    @ParseMethod(type = MessageDataType.COMMAND_GET_ENTITY_STATE)
    public BasicGetEntityStateCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicGetEntityStateCommandData.class);
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
            final int entityId,
            final EntityRemovalType removalType
        ) {
            super(MessageDataType.COMMAND_RECV_REMOVE_ENTITY);
            this.entityId = entityId;
            this.removalType = removalType;
        }

        /**
         * Gets the targeted entity's ID.
         * @return Entity ID
         */
        @JsonProperty("entityId")
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

        /**
         * Gets the JSON property of the removal type.
         * @return Removal type as string
         */
        @JsonProperty("removalType")
        public String getRemovalTypeJsonProperty() {
            return removalType.toString();
        }
    }

    /**
     * Runs the Get Entity State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_GET_ENTITY_STATE)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws JsonProcessingException {
        final BasicGameUserState user = (BasicGameUserState) SpringMessageHandler.userStateFrom(context);
        final BasicGameState game = (BasicGameState) SpringMessageHandler.gameStateFrom(context);

        // Require connection
        if (!user.isConnected()) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("COMMAND_GET_ENTITY_STATE requires connection."))
                    .build());
            return;
        }

        // Find the specified entity
        final BasicEntityState entityState = game.getEntities().get(((BasicGetEntityStateCommandData) message.data()).getEntityId());

        if (entityState == null) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InvalidArgumentExceptionData("No such entity."))
                    .build());
            return;
        }

        user.getClient().send(
            OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .ackMessageId(message.messageId())
                .data(new BasicRecvEntityStateCommandData(MessageDataType.RESP_GET_ENTITY_STATE, entityState))
                .build());
    }

    /**
     * Runs the Refresh Entity State client command.
     * @param gameState Game state to refresh locations for
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_RECV_ENTITY_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws JsonProcessingException {
        GameState gameState = ((RecvEntityStateInvokeContext) context).getGameState();
        BasicEntityState entityState = ((RecvEntityStateInvokeContext) context).getEntityState();
        List<EntityStateFieldFilter> fieldFilter = ((RecvEntityStateInvokeContext) context).getFieldFilter();
        UUID skipSendFor = ((RecvEntityStateInvokeContext) context).getSkipSendFor();
        List<UUID> userFilter = ((RecvEntityStateInvokeContext) context).getFilter();

        boolean onlyLocation;

        // Generate a message body for each connected user and send it out
        final BasicRecvEntityStateCommandData data;
        if (fieldFilter == null) {
            data = new BasicRecvEntityStateCommandData(MessageDataType.COMMAND_RECV_ENTITY_STATE, entityState);
            onlyLocation = false;
        } else {
            data = new BasicRecvEntityStateCommandData(
                entityState.getId(),
                fieldFilter.contains(EntityStateFieldFilter.COORDINATES) ? entityState.getCoordinates() : null,
                fieldFilter.contains(EntityStateFieldFilter.ROTATION) ? entityState.getRotation() : null,
                fieldFilter.contains(EntityStateFieldFilter.TYPE) ? entityState.getType() : null,
                fieldFilter.contains(EntityStateFieldFilter.HEALTH) ? entityState.getHealth() : null,
                fieldFilter.contains(EntityStateFieldFilter.STATE) ? entityState.getState() : null,
                fieldFilter.contains(EntityStateFieldFilter.HELD_ITEM_TEXTURE) ? entityState.getHeldItemTexture() : null,
                fieldFilter.contains(EntityStateFieldFilter.TEXTURE_PREFIX) ? entityState.getTexturePrefix() : null
            );

            if (fieldFilter.size() == 1 && (
                    fieldFilter.contains(EntityStateFieldFilter.COORDINATES)
                    || fieldFilter.contains(EntityStateFieldFilter.ROTATION))) {
                onlyLocation = true;
            }
            else if (fieldFilter.size() == 2 && 
                fieldFilter.contains(EntityStateFieldFilter.COORDINATES)
                && fieldFilter.contains(EntityStateFieldFilter.ROTATION)) {
                onlyLocation = true;
            } else {
                onlyLocation = false;
            }
        }
        for (GameUserState userState : gameState.getConnectedUsers().values()) {
            // Assumes they changed the state, so they don't need to get it again
            if (skipSendFor != null && userState.getUser().id().equals(skipSendFor)) {
                continue;
            }

            if (userFilter != null) {
                // Check if this user is in the filter
                boolean send = false;
                for (final UUID targetId : userFilter) {
                    if (targetId.equals(userState.getUser().id())) {
                        send = true;
                        break;
                    }
                }
                if (!send) {
                    continue;
                }
            }

            if (onlyLocation)
                userState.getClient().send(
                    OutgoingMessage.newBuilder()
                        .setKey(handler)
                        .success()
                        .data(data)
                        .build());
            else 
                userState.getClient().send(
                    OutgoingMessage.newBuilder()
                        .setKey(handler)
                        .success()
                        .randomMessageId()
                        .data(data)
                        .build());
        }
    }

    /**
     * Runs the Receive Remove Entity client command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_RECV_REMOVE_ENTITY)
    public void sendRemove(final MessageHandler handler, final InvokeContext context) throws JsonProcessingException {
        GameState gameState = ((RecvRemoveEntityStateInvokeContext) context).getGameState();
        BasicEntityState entityState = ((RecvRemoveEntityStateInvokeContext) context).getEntityState();
        EntityRemovalType removalType = ((RecvRemoveEntityStateInvokeContext) context).getRemovalType();
        UUID skipSendFor = ((RecvRemoveEntityStateInvokeContext) context).getSkipSendFor();
        List<UUID> userFilter = ((RecvRemoveEntityStateInvokeContext) context).getFilter();

        // Generate a message body for each connected user and send it out
        final BasicRecvRemoveEntityStateCommandData data = new BasicRecvRemoveEntityStateCommandData(entityState.getId(), removalType);
        for (GameUserState userState : gameState.getConnectedUsers().values()) {
            // Assumes they changed the state, so they don't need to get it again
            if (skipSendFor != null && userState.getUser().id().equals(skipSendFor)) {
                continue;
            }

            if (userFilter != null) {
                // Check if this user is in the filter
                boolean send = false;
                for (final UUID targetId : userFilter) {
                    if (targetId.equals(userState.getUser().id())) {
                        send = true;
                        break;
                    }
                }
                if (!send) {
                    continue;
                }
            }

            userState.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(data)
                    .build());
        }
    }

    /**
     * Context for the receive player state command invocation.
     */
    public static class RecvEntityStateInvokeContext extends InvokeContext {
        /**
         * Game state for this invocation
         */
        private BasicGameState gameState;

        /**
         * Entity state for this invocation
         */
        private BasicEntityState entityState;

        /**
         * Fields to filter in when sending data.
         */
        private List<EntityStateFieldFilter> fieldFilter;

        /**
         * Denotes a user that shouldn't receive the state change (user that sent the command, usually).
         */
        private UUID skipSendFor;

        /**
         * The UUIDs to filter sends to.
         */
        private List<UUID> filter;

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param entityState Entity that changed
         * @param skipSendFor Denotes a user that shouldn't receive the state change. Can be null
         * @param fieldFilter Fields to send through (ie: modified fields)
         */
        public RecvEntityStateInvokeContext(final BasicGameState gameState, final BasicEntityState entityState, final UUID skipSendFor, final List<EntityStateFieldFilter> fieldFilter) {
            this.gameState = gameState;
            this.entityState = entityState;
            this.fieldFilter = fieldFilter;
            this.skipSendFor = skipSendFor;
            filter = null;
        }

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param entityState Entity that changed
         * @param skipSendFor Denotes a user that shouldn't receive the state change. Can be null
         * @param fieldFilter Fields to send through (ie: modified fields)
         */
        public RecvEntityStateInvokeContext(final BasicGameState gameState, final BasicEntityState entityState, final List<UUID> userFilter, final List<EntityStateFieldFilter> fieldFilter) {
            this.gameState = gameState;
            this.entityState = entityState;
            this.fieldFilter = fieldFilter;
            skipSendFor = null;
            filter = userFilter;
        }

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param entityState Entity that changed
         */
        public RecvEntityStateInvokeContext(final BasicGameState gameState, final BasicEntityState entityState, final List<EntityStateFieldFilter> fieldFilter) {
            this.gameState = gameState;
            this.entityState = entityState;
            this.fieldFilter = fieldFilter;
            skipSendFor = null;
            filter = null;
        }

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param entityState Entity that changed
         */
        public RecvEntityStateInvokeContext(final BasicGameState gameState, final BasicEntityState entityState) {
            this.gameState = gameState;
            this.entityState = entityState;
            fieldFilter = null;
            skipSendFor = null;
            filter = null;
        }

        /**
         * Gets the game state for this invocation.
         * @return Game state to send state for
         */
        public BasicGameState getGameState() {
            return gameState;
        }

        /**
         * Gets the user state for this invocation.
         * @return User state to send state for
         */
        public BasicEntityState getEntityState() {
            return entityState;
        }

        /**
         * Gets the fields to filter sends to.
         * @return Field filter
         */
        public List<EntityStateFieldFilter> getFieldFilter() {
            return fieldFilter;
        }

        /**
         * Denotes a user that shouldn't receive the state change.
         * @return User to skip or null
         */
        public UUID getSkipSendFor() {
            return skipSendFor;
        }

        /**
         * Gets the UUIDs to filter sends to.
         * @return User IDs to send state to
         */
        public List<UUID> getFilter() {
            return filter;
        }
    }

    /**
     * Context for the receive remove entity state command invocation.
     */
    public static class RecvRemoveEntityStateInvokeContext extends InvokeContext {
        /**
         * Game state for this invocation
         */
        private BasicGameState gameState;

        /**
         * Entity state for this invocation
         */
        private BasicEntityState entityState;

        /**
         * Reason the entity is being removed.
         */
        private EntityRemovalType removalType;

        /**
         * Denotes a user that shouldn't receive the state change (user that sent the command, usually).
         */
        private UUID skipSendFor;

        /**
         * The UUIDs to filter sends to.
         */
        private List<UUID> filter;

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param entityState Entity that changed
         * @param skipSendFor Denotes a user that shouldn't receive the state change. Can be null
         * @param fieldFilter Fields to send through (ie: modified fields)
         */
        public RecvRemoveEntityStateInvokeContext(final BasicGameState gameState, final BasicEntityState entityState, final EntityRemovalType removalType, final UUID skipSendFor) {
            this.gameState = gameState;
            this.entityState = entityState;
            this.skipSendFor = skipSendFor;
            this.removalType = removalType;
            filter = null;
        }

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param entityState Entity that changed
         * @param skipSendFor Denotes a user that shouldn't receive the state change. Can be null
         * @param fieldFilter Fields to send through (ie: modified fields)
         */
        public RecvRemoveEntityStateInvokeContext(final BasicGameState gameState, final BasicEntityState entityState, final EntityRemovalType removalType, final List<UUID> userFilter) {
            this.gameState = gameState;
            this.entityState = entityState;
            this.removalType = removalType;
            skipSendFor = null;
            filter = userFilter;
        }

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param entityState Entity that changed
         */
        public RecvRemoveEntityStateInvokeContext(final BasicGameState gameState, final BasicEntityState entityState, final EntityRemovalType removalType) {
            this.gameState = gameState;
            this.entityState = entityState;
            this.removalType = removalType;
            skipSendFor = null;
            filter = null;
        }

        /**
         * Gets the game state for this invocation.
         * @return Game state to send state for
         */
        public BasicGameState getGameState() {
            return gameState;
        }

        /**
         * Gets the user state for this invocation.
         * @return User state to send state for
         */
        public BasicEntityState getEntityState() {
            return entityState;
        }

        /**
         * Denotes a user that shouldn't receive the state change.
         * @return User to skip or null
         */
        public UUID getSkipSendFor() {
            return skipSendFor;
        }

        /**
         * Gets the UUIDs to filter sends to.
         * @return User IDs to send state to
         */
        public List<UUID> getFilter() {
            return filter;
        }

        /**
         * Gets the reason why an entity is being removed.
         * @return Removal type
         */
        public EntityRemovalType getRemovalType() {
            return removalType;
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