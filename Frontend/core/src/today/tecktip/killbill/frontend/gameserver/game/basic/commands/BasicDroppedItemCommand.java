package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
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
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;
import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalDroppedItemState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState;

/**
 * Allows clients to drop items.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicDroppedItemCommand {

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
    @ParseMethod(type = MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM)
    public BasicCreateDroppedItemCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicCreateDroppedItemCommandData.class);
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_RECV_REMOVE_DROPPED_ITEM)
    public BasicRemoveDroppedItemCommandData parseRemove(final JsonNode node) {
        return MAPPER.convertValue(node, BasicRemoveDroppedItemCommandData.class);
    }

    /**
     * Runs the Receive New Dropped Item command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
        BasicLocalGameState gameState = (BasicLocalGameState) KillBillGame.get().getUdpClient().getGameState();
        BasicCreateDroppedItemCommandData data = (BasicCreateDroppedItemCommandData) message.data();

        if (gameState.getDroppedItem(data.getId()) == null)
            gameState.addDroppedItem(data.getId(), data.getLocation(), data.getItemType(), data.getQuantity());

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
            throw new MessageFailure("JSON failure", e);
        }
    }

    /**
     * Runs the Remove Dropped Item command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_REMOVE_DROPPED_ITEM)
    public void runRemove(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
        BasicLocalGameState gameState = (BasicLocalGameState) KillBillGame.get().getUdpClient().getGameState();
        BasicRemoveDroppedItemCommandData data = (BasicRemoveDroppedItemCommandData) message.data();

        gameState.removeDroppedItem(data.getId());

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
            throw new MessageFailure("JSON failure", e);
        }
    }

    /**
     * Runs the Create Dropped Item command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_CREATE_DROPPED_ITEM)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        BasicLocalGameState gameState = (BasicLocalGameState) KillBillGame.get().getUdpClient().getGameState();
        Item item = ((CreateDroppedItemContext) context).getItem();

        BasicLocalDroppedItemState itemState = gameState.addDroppedItem(
            UUID.randomUUID().toString(),
            new Coordinates(
                KillBillGame.get().getPlayer().getRectangle().getCenterX() / GlobalGameConfig.GRID_SIZE,
                KillBillGame.get().getPlayer().getRectangle().getCenterY() / GlobalGameConfig.GRID_SIZE
            ),
            item.getType(),
            item.getQuantity());
        
        gameState.setRecentItem(itemState);

        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(new BasicCreateDroppedItemCommandData(
                        MessageDataType.COMMAND_CREATE_DROPPED_ITEM, 
                        itemState.getLocation(), 
                        itemState.getType(), 
                        itemState.getQuantity(), 
                        itemState.getId()))
                    .build());
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON failure", e);
        }
    }

    /**
     * Runs the Remove Dropped Item command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_REMOVE_DROPPED_ITEM)
    public void sendRemove(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        BasicLocalGameState gameState = (BasicLocalGameState) KillBillGame.get().getUdpClient().getGameState();
        String id = ((RemoveDroppedItemContext) context).getItemId();

        gameState.removeDroppedItem(id);

        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(new BasicRemoveDroppedItemCommandData(id))
                    .build()); 
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON failure", e);
        }
    }

    /**
     * Context for the create dropped item invocation.
     */
    public static class CreateDroppedItemContext extends InvokeContext {
        final Item item;

        /**
         * Constructs context data for the create dropped item command.
         */
        public CreateDroppedItemContext(
            final Item item
        ) {
            super();

            this.item = item;
        }

        /**
         * Gets the item.
         * @return Item
         */
        public Item getItem() {
            return item;
        }
    }

    /**
     * Context for the remove dropped item command invocation.
     */
    public static class RemoveDroppedItemContext extends InvokeContext {
        private String itemId;


        /**
         * Constructs context data for the remove dropped item command.
         */
        public RemoveDroppedItemContext(
            final String itemId
        ) {
            super();

            this.itemId = itemId;
        }

        public String getItemId() {
            return itemId;
        }
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicDroppedItemCommand() { }
}