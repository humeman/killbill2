package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Allows clients to summon bombs.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicBombCommand {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicBombCommand.class);
    
    /**
     * Jackson thingy
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Incoming bomb command data.
     */
    public static class BasicBombCommandData extends MessageData {
        private final BombType bombType;
        private final Coordinates origin;
        private UUID launchedBy;

        /**
         * Constructs a bomb command.
         */
        @JsonCreator
        public BasicBombCommandData(
            @JsonProperty(value = "bombType", required = true) String bombType,
            @JsonProperty(value = "origin", required = true) List<Double> origin,
            @JsonProperty(value = "launchedBy", required = true) String launchedBy
        ) {
            super(MessageDataType.COMMAND_RECV_BOMB);

            this.bombType = BombType.valueOf(bombType);
            this.origin = Coordinates.fromList(origin);
            this.launchedBy = UUID.fromString(launchedBy);
        }

        public BasicBombCommandData(
            final MessageDataType typeOverride,
            final BombType bombType,
            final Coordinates origin
        ) {
            super(typeOverride);

            this.bombType = bombType;
            this.origin = origin;
            launchedBy = null;
        }

        /**
         * Gets the bomb's type.
         * @return Bomb type
         */
        public BombType getBombType() {
            return bombType;
        }
        
        /**
         * Gets the bomb's type as a JSON property.
         * @return Bomb type as a string
         */
        @JsonProperty("bombType")
        public String getBombTypeJsonProperty() {
            return bombType.toString();
        }

        /**
         * Coordinates where the bomb originated.
         * @return Origin
         */
        public Coordinates getOrigin() {
            return origin;
        }

        /**
         * Gets the bomb's origin as a JSON property.
         * @return Bomb's origin as a list of doubles
         */
        @JsonProperty("origin")
        public List<Double> getOriginJsonProperty() {
            return origin.toList();
        }

        public UUID getLaunchedBy() {
            return launchedBy;
        }

        @JsonProperty("launchedBy")
        public String getLaunchedByJsonProperty() {
            if (launchedBy != null) return launchedBy.toString();
            else return null;
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_RECV_BOMB)
    public BasicBombCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicBombCommandData.class);
    }

    /**
     * Runs the Receive Bomb command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_BOMB)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
        // We don't do anything here :)
        // Like chat, it's on the UDP game screen to intercept this with a handler

        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .ackMessageId(message.messageId())
                    .data(new EmptyData())
                    .build());
        } catch (JsonProcessingException e) {
            throw new MessageFailure("JSON failure", e);
        }
    }

    /**
     * Runs the Create Bomb command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_CREATE_BOMB)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        CreateBombContext ctx = (CreateBombContext) context;

        // Generate a message body for each connected user and send it out
        final BasicBombCommandData newData = new BasicBombCommandData(
            MessageDataType.COMMAND_CREATE_BOMB,
            ctx.getType(),
            ctx.getOrigin()
        );
        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(newData)
                    .build());
        } catch (final Throwable t) {
            LOGGER.error("Failure in bomb creation, will not damage other players", t);
        }
    }

    /**
     * Context for the receive game state command invocation.
     */
    public static class CreateBombContext extends InvokeContext {
        private final Coordinates origin;
        private final BombType type;

        /**
         * Constructs context data for the receive bomb command.
         * @param gameState Game state to send state for
         */
        public CreateBombContext(
            final Coordinates origin,
            final BombType type
        ) {
            super();

            this.origin = origin;
            this.type = type;
        }

        public Coordinates getOrigin() {
            return origin;
        }

        public BombType getType() {
            return type;
        }
    }

    public static enum BombType {
        PLUSHIE,
        COMPUTER,
        CLAYMORE_ROOMBA
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicBombCommand() { }
}