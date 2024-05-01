package today.tecktip.killbill.backend.gameserver.games.basic.commands;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicEntityState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicEntityState.EntityStateFieldFilter;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState.BasicGameRunState;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvEntityStateCommand.EntityRemovalType;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvEntityStateCommand.RecvEntityStateInvokeContext;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvEntityStateCommand.RecvRemoveEntityStateInvokeContext;
import today.tecktip.killbill.backend.gameserver.games.basic.entities.ClaymoreRoomba;
import today.tecktip.killbill.backend.gameserver.games.basic.entities.Employee;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.CommandContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.CommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.exceptions.IllegalStateExceptionData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InternalServerErrorData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InvalidArgumentExceptionData;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;
import today.tecktip.killbill.common.maploader.directives.EntityDirective.EntityType;

/**
 * Updates an entity's state.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicChangeEntityStateCommand {
    /**
     * JSON deserializer
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BasicChangeEntityStateCommandData extends MessageData {
        /**
         * The damage to deal to the entity.
         */
        private final Integer damage;

        /**
         * The target entity's ID.
         */
        private final int entityId;

        /**
         * Constructs a new change location command message.
         * @param coordinates New coordinates
         */
        public BasicChangeEntityStateCommandData(
            @JsonProperty(value = "entityId", required = true) final int entityId,
            @JsonProperty(value = "damage", required = false) final Integer damage
        ) {
            super(MessageDataType.COMMAND_CHANGE_OTHER_PLAYER_STATE);
            if (damage != null) {
                this.damage = damage;
            } else this.damage = null;

            this.entityId = entityId;
        }

        /**
         * Gets the target entity's ID.
         * @return Target entity's ID
         */
        public int getEntityId() {
            return entityId;
        }

        /**
         * Gets the damage applied in this command.
         * @return Damage or null
         */
        public Integer getDamage() {
            return damage;
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_CHANGE_ENTITY_STATE)
    public BasicChangeEntityStateCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicChangeEntityStateCommandData.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
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
            @JsonProperty(value = "coordinates", required = true) final List<Double> coordinates,
            @JsonProperty(value = "entityType", required = true) final String entityType
        ) {
            super(MessageDataType.COMMAND_SUMMON_ENTITY);
            this.coordinates = Coordinates.fromList(coordinates);
            this.entityType = EntityType.valueOf(entityType);
        }

        /**
         * Gets the target entity's type.
         * @return Target entity's type
         */
        public EntityType getEntityType() {
            return entityType;
        }

        /**
         * Gets the coordinates to spawn the entity at.
         * @return Coordinates
         */
        public Coordinates getCoordinates() {
            return coordinates;
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_SUMMON_ENTITY)
    public BasicSummonEntityCommandData parseSummon(final JsonNode node) {
        return MAPPER.convertValue(node, BasicSummonEntityCommandData.class);
    }

    /**
     * Runs the Change Entity State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_CHANGE_ENTITY_STATE)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws JsonProcessingException {
        final BasicGameUserState user = (BasicGameUserState) SpringMessageHandler.userStateFrom(context);
        final BasicGameState game = (BasicGameState) SpringMessageHandler.gameStateFrom(context);

        final BasicChangeEntityStateCommandData data = (BasicChangeEntityStateCommandData) message.data();

        // Require connection
        if (!user.isConnected()) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("COMMAND_CHANGE_ENTITY_STATE requires connection."))
                    .build());
            return;
        }

        // Require playing state
        if (!game.isInState(BasicGameRunState.PLAYING)) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("Game is not active yet."))
                    .build());
            return;
        }

        final BasicEntityState entityState = game.getEntities().get(data.getEntityId());

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

        final List<EntityStateFieldFilter> fieldFilter = new ArrayList<>();

        if (data.getDamage() != null) {
            entityState.setHealth(entityState.getHealth() - data.getDamage());
            fieldFilter.add(EntityStateFieldFilter.HEALTH);
        }

        // Dead? If so, send a removal message instead
        try {
            if (entityState.getHealth() > 0) {
                handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_ENTITY_STATE).run(
                    handler,
                    new RecvEntityStateInvokeContext(game, entityState, user.getUser().id(), fieldFilter)
                );
            } else {
                // Health <= 0, so they're dead
                // Remove from queue, then notify
                game.getEntities().remove(entityState.getId());
                handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_REMOVE_ENTITY).run(
                    handler,
                    new RecvRemoveEntityStateInvokeContext(game, entityState, EntityRemovalType.DIE, user.getUser().id())
                );
            }
        } catch (final MessageFailure e) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InternalServerErrorData("Failed to message other clients."))
                    .build());
            return;
        }

        user.getClient().send(
            OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .ackMessageId(message.messageId())
                .data(new EmptyData())
                .build());
    }

    /**
     * Runs the Summon Entity command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_SUMMON_ENTITY)
    public void runSummon(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws JsonProcessingException {
        final BasicGameUserState user = (BasicGameUserState) SpringMessageHandler.userStateFrom(context);
        final BasicGameState game = (BasicGameState) SpringMessageHandler.gameStateFrom(context);

        final BasicSummonEntityCommandData data = (BasicSummonEntityCommandData) message.data();

        // Require connection
        if (!user.isConnected()) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("COMMAND_CHANGE_ENTITY_STATE requires connection."))
                    .build());
            return;
        }

        // Require playing state
        if (!game.isInState(BasicGameRunState.PLAYING)) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("Game is not active yet."))
                    .build());
            return;
        }

        // Require playing state
        if (!user.getPlayerType().equals(BasicPlayerType.BILL)) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InvalidArgumentExceptionData("Only Bill can summon entities."))
                    .build());
            return;
        }

        // Get the biggest entity ID we can use
        int id = 0;
        for (final Integer entityId : game.getEntities().keySet()) {
            if (entityId > id) id = entityId;
        } 
        id++;

        BasicEntityState newEntity = null;
        switch (data.getEntityType()) {
            case EMPLOYEE:
                newEntity = new Employee(
                    id,
                    0,
                    game,
                    data.getCoordinates().copy()
                );
                break;
            case CLAYMORE_ROOMBA:
                newEntity = new ClaymoreRoomba(
                    id, 
                    0,
                    game, 
                    data.getCoordinates().copy()
                );
                break;
            default:
                newEntity = null;
                break;
        }

        // Add to the state
        if (newEntity == null) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InvalidArgumentExceptionData("Creation failed."))
                    .build());
            return;
        }
        
        game.getEntities().put(id, newEntity);

        user.getClient().send(
            OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .ackMessageId(message.messageId())
                .data(new EmptyData())
                .build());

        // Send out the entity state to everyone
        try {
            handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_ENTITY_STATE)
                .run(
                    handler,
                    new RecvEntityStateInvokeContext(game, newEntity)
                );
        } catch (final MessageFailure e) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InternalServerErrorData("Failed to message clients."))
                    .build());
            return;
        }
    }
}