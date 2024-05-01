package today.tecktip.killbill.backend.gameserver.games.basic.commands;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameState;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicDroppedItemState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.common.exceptions.MessageFailure;
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
import today.tecktip.killbill.common.gameserver.messages.exceptions.InternalServerErrorData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InvalidArgumentExceptionData;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;
import today.tecktip.killbill.common.maploader.ItemType;

/**
 * Allows clients to drop items.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicDroppedItemCommand {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicInteractCommand.class);

    /**
     * Jackson thingy
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Incoming create dropped item command data.
     */
    public static class BasicCreateDroppedItemCommandData extends MessageData {
        private final ItemType itemType;
        private final int quantity;
        private final Coordinates location;
        private final String id;

        /**
         * Constructs an interact command.
         */
        @JsonCreator
        public BasicCreateDroppedItemCommandData(
            @JsonProperty(value = "location", required = true) List<Double> location,
            @JsonProperty(value = "itemType", required = true) String itemType,
            @JsonProperty(value = "quantity", required = true) int quantity,
            @JsonProperty(value = "id", required = true) String id
        ) {
            super(MessageDataType.COMMAND_CREATE_DROPPED_ITEM);

            this.location = Coordinates.fromList(location);
            this.itemType = ItemType.valueOf(itemType);
            this.quantity = quantity;
            this.id = id;
        }

        public BasicCreateDroppedItemCommandData(
            final MessageDataType typeOverride,
            Coordinates location,
            ItemType itemType,
            int quantity,
            String id
        ) {
            super(typeOverride);

            this.itemType = itemType;
            this.location = location;
            this.quantity = quantity;
            this.id = id;
        }

        /**
         * Gets the item type.
         * @return Item type
         */
        public ItemType getItemType() {
            return itemType;
        }
        
        /**
         * Gets the item type as a JSON property.
         * @return Item type as a string
         */
        @JsonProperty("itemType")
        public String getItemTypeJsonProperty() {
            return itemType.toString();
        }

        /**
         * Gets the item location.
         * @return Item location
         */
        public Coordinates getLocation() {
            return location;
        }
        
        /**
         * Gets the location as a JSON property.
         * @return Location as a list of doubles
         */
        @JsonProperty("location")
        public List<Double> getLocationJsonProperty() {
            return location.toList();
        }

        /**
         * ID of the specified item.
         * @return Item ID
         */
        @JsonProperty("id")
        public String getId() {
            return id;
        }

        /**
         * Quantity of items dropped.
         * @return Quantity
         */
        @JsonProperty("quantity")
        public int getQuantity() {
            return quantity;
        }
    }

    /**
     * Incoming create dropped item command data.
     */
    public static class BasicRemoveDroppedItemCommandData extends MessageData {
        private final String id;

        /**
         * Constructs an interact command.
         */
        @JsonCreator
        public BasicRemoveDroppedItemCommandData(
            @JsonProperty(value = "id", required = true) String id
        ) {
            super(MessageDataType.COMMAND_REMOVE_DROPPED_ITEM);

            this.id = id;
        }

        public BasicRemoveDroppedItemCommandData(
            final MessageDataType typeOverride,
            String id
        ) {
            super(typeOverride);

            this.id = id;
        }

        /**
         * ID of the specified item.
         * @return Item ID
         */
        @JsonProperty("id")
        public String getId() {
            return id;
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_CREATE_DROPPED_ITEM)
    public BasicCreateDroppedItemCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicCreateDroppedItemCommandData.class);
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_REMOVE_DROPPED_ITEM)
    public BasicRemoveDroppedItemCommandData parseRemove(final JsonNode node) {
        return MAPPER.convertValue(node, BasicRemoveDroppedItemCommandData.class);
    }

    /**
     * Runs the Create Dropped Item command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_CREATE_DROPPED_ITEM)
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
                    .data(new IllegalStateExceptionData("COMMAND_CREATE_DROPPED_ITEM requires connection."))
                    .build());
            return;
        }

        final BasicCreateDroppedItemCommandData data = (BasicCreateDroppedItemCommandData) message.data();
        if (game.getDroppedItem(data.getId()) != null) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InvalidArgumentExceptionData("Item exists already."))
                    .build());
            return;
        }      
   
        game.addDroppedItem(data.getId(), data.getLocation(), data.getItemType(), data.getQuantity());
        
        // Send this out to clients
        try {
            handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM).run(
                handler,
                new RecvCreateDroppedItemContext(game, data, user.getUser().id())
            );
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
     * Runs the Remove Dropped Item command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_REMOVE_DROPPED_ITEM)
    public void runRemove(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws JsonProcessingException {
        final BasicGameUserState user = (BasicGameUserState) SpringMessageHandler.userStateFrom(context);
        final BasicGameState game = (BasicGameState) SpringMessageHandler.gameStateFrom(context);

        // Require connection
        if (!user.isConnected()) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("COMMAND_CHANGE_GAME_STATE requires connection."))
                    .build());
            return;
        }

        final BasicRemoveDroppedItemCommandData data = (BasicRemoveDroppedItemCommandData) message.data();
        if (!game.removeDroppedItem(data.getId())) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InvalidArgumentExceptionData("No such item."))
                    .build());
            return;
        }      
        // Send this out to clients
        try {
            handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_REMOVE_DROPPED_ITEM).run(
                handler,
                new RecvRemoveDroppedItemContext(game, data, user.getUser().id())
            );
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
     * Runs the Receive New Dropped Item command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM)
    public void send(final MessageHandler handler, final InvokeContext context) throws JsonProcessingException {
        BasicGameState gameState = (BasicGameState) ((RecvCreateDroppedItemContext) context).getGameState();
        BasicCreateDroppedItemCommandData data = ((RecvCreateDroppedItemContext) context).getData();
        UUID sender = ((RecvCreateDroppedItemContext) context).getSender();
        List<UUID> sendTo = ((RecvCreateDroppedItemContext) context).getSendTo();

        // Generate a message body for each connected user and send it out
        final BasicCreateDroppedItemCommandData newData = new BasicCreateDroppedItemCommandData(
            MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM,
            data.getLocation(),
            data.getItemType(),
            data.getQuantity(),
            data.getId()
        );
        for (GameUserState userState : gameState.getConnectedUsers().values()) {
            if (sender != null && userState.getUser().id().equals(sender)) continue;
            if (sendTo != null && !sendTo.contains(userState.getUser().id())) continue;

            try {
                userState.getClient().send(
                    OutgoingMessage.newBuilder()
                        .setKey(handler)
                        .success()
                        .randomMessageId()
                        .data(newData)
                        .build());
            } catch (final Throwable t) {
                LOGGER.error("Failure in game state send to userId={}, continuing anyway", userState.getUser().id(), t);
            }
        }
    }

    /**
     * Runs the Receive Remove Dropped Item command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_RECV_REMOVE_DROPPED_ITEM)
    public void sendRemove(final MessageHandler handler, final InvokeContext context) throws JsonProcessingException {
        BasicGameState gameState = (BasicGameState) ((RecvRemoveDroppedItemContext) context).getGameState();
        BasicRemoveDroppedItemCommandData data = ((RecvRemoveDroppedItemContext) context).getData();
        UUID sender = ((RecvRemoveDroppedItemContext) context).getSender();

        // Generate a message body for each connected user and send it out
        final BasicRemoveDroppedItemCommandData newData = new BasicRemoveDroppedItemCommandData(
            MessageDataType.COMMAND_RECV_REMOVE_DROPPED_ITEM,
            data.getId()
        );
        for (GameUserState userState : gameState.getConnectedUsers().values()) {
            if (userState.getUser().id().equals(sender)) continue;

            try {
                userState.getClient().send(
                    OutgoingMessage.newBuilder()
                        .setKey(handler)
                        .success()
                        .randomMessageId()
                        .data(newData)
                        .build());
            } catch (final Throwable t) {
                LOGGER.error("Failure in game state send to userId={}, continuing anyway", userState.getUser().id(), t);
            }
        }
    }

    /**
     * Context for the receive game state command invocation.
     */
    public static class RecvCreateDroppedItemContext extends InvokeContext {
        /**
         * Game state for this invocation
         */
        private GameState gameState;
        private final BasicCreateDroppedItemCommandData data;
        private final UUID sender;
        private final List<UUID> sendTo;

        /**
         * Constructs context data for the receive interaction command.
         * @param gameState Game state to send state for
         */
        public RecvCreateDroppedItemContext(
            final GameState gameState,
            final BasicCreateDroppedItemCommandData data,
            final UUID sender
        ) {
            super();

            this.gameState = gameState;
            this.data = data;
            this.sender = sender;
            sendTo = null;
        }

        public RecvCreateDroppedItemContext(
            final GameState gameState,
            final BasicDroppedItemState state,
            final List<UUID> sendTo
        ) {
            this.gameState = gameState;
            this.data = new BasicCreateDroppedItemCommandData(
                MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM,
                state.getLocation(),
                state.getType(),
                state.getQuantity(),
                state.getId());
            this.sendTo = sendTo;
            sender = null;
        }

        /**
         * Gets the game state for this invocation.
         * @return Game state to send state for
         */
        public GameState getGameState() {
            return gameState;
        }

        /**
         * Gets the data.
         * @return Data
         */
        public BasicCreateDroppedItemCommandData getData() {
            return data;
        }

        /**
         * Gets the sender, if applicable.
         * @return Sender or null
         */
        public UUID getSender() {
            return sender;
        }

        /**
         * Gets the users to send this to, if applicable.
         * @return Senders or null
         */
        public List<UUID> getSendTo() {
            return sendTo;
        }
    }

    /**
     * Context for the receive game state command invocation.
     */
    public static class RecvRemoveDroppedItemContext extends InvokeContext {
        /**
         * Game state for this invocation
         */
        private GameState gameState;
        private final BasicRemoveDroppedItemCommandData data;
        private final UUID sender;


        /**
         * Constructs context data for the receive interaction command.
         * @param gameState Game state to send state for
         */
        public RecvRemoveDroppedItemContext(
            final GameState gameState,
            final BasicRemoveDroppedItemCommandData data,
            final UUID sender
        ) {
            super();

            this.gameState = gameState;
            this.data = data;
            this.sender = sender;
        }

        /**
         * Gets the game state for this invocation.
         * @return Game state to send state for
         */
        public GameState getGameState() {
            return gameState;
        }

        /**
         * Gets the data.
         * @return Data
         */
        public BasicRemoveDroppedItemCommandData getData() {
            return data;
        }

        /**
         * Gets the sender, if applicable.
         * @return Sender or null
         */
        public UUID getSender() {
            return sender;
        }
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicDroppedItemCommand() { }
}