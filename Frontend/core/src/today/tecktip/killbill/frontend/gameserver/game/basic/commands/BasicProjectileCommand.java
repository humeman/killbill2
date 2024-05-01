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
 * Allows clients to summon projectiles.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicProjectileCommand {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicProjectileCommand.class);
    
    /**
     * Jackson thingy
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Incoming projectile command data.
     */
    public static class BasicProjectileCommandData extends MessageData {
        private final ProjectileType projectileType;
        private final Coordinates origin;
        private final int direction;
        private UUID launchedBy;

        /**
         * Constructs a projectile command.
         */
        @JsonCreator
        public BasicProjectileCommandData(
            @JsonProperty(value = "projectileType", required = true) String projectileType,
            @JsonProperty(value = "origin", required = true) List<Double> origin,
            @JsonProperty(value = "direction", required = true) int direction,
            @JsonProperty(value = "launchedBy", required = false) String launchedBy
        ) {
            super(MessageDataType.COMMAND_RECV_PROJECTILE);

            this.projectileType = ProjectileType.valueOf(projectileType);
            this.origin = Coordinates.fromList(origin);
            this.direction = direction;
            if (launchedBy != null)
                this.launchedBy = UUID.fromString(launchedBy);
            else this.launchedBy = null;
        }

        public BasicProjectileCommandData(
            final MessageDataType typeOverride,
            final ProjectileType projectileType,
            final int direction,
            final Coordinates origin
        ) {
            super(typeOverride);

            this.projectileType = projectileType;
            this.direction = direction;
            this.origin = origin;
            launchedBy = null;
        }

        /**
         * Gets the projectile's type.
         * @return Projectile type
         */
        public ProjectileType getProjectileType() {
            return projectileType;
        }
        
        /**
         * Gets the projectile's type as a JSON property.
         * @return Projectile type as a string
         */
        @JsonProperty("projectileType")
        public String getProjectileTypeJsonProperty() {
            return projectileType.toString();
        }

        /**
         * Coordinates where the projectile originated.
         * @return Origin
         */
        public Coordinates getOrigin() {
            return origin;
        }

        /**
         * Gets the projectile's origin as a JSON property.
         * @return Projectile's origin as a list of doubles
         */
        @JsonProperty("origin")
        public List<Double> getOriginJsonProperty() {
            return origin.toList();
        }

        @JsonProperty("direction")
        public int getDirection() {
            return direction;
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
    @ParseMethod(type = MessageDataType.COMMAND_RECV_PROJECTILE)
    public BasicProjectileCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicProjectileCommandData.class);
    }

    /**
     * Runs the Receive Projectile command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_PROJECTILE)
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
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON failure", e);
        }
    }

    /**
     * Runs the Create Projectile command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_CREATE_PROJECTILE)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        CreateProjectileContext ctx = (CreateProjectileContext) context;

        final BasicProjectileCommandData newData = new BasicProjectileCommandData(
            MessageDataType.COMMAND_CREATE_PROJECTILE,
            ctx.getType(),
            ctx.getDirection(),
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
            LOGGER.error("Failure in projectile creation, will not damage other players", t);
        }
    }

    /**
     * Context for the create projectile command invocation.
     */
    public static class CreateProjectileContext extends InvokeContext {
        private final Coordinates origin;
        private final ProjectileType type;
        private final int direction;

        /**
         * Constructs context data for the receive bomb command.
         * @param gameState Game state to send state for
         */
        public CreateProjectileContext(
            final Coordinates origin,
            final ProjectileType type,
            final int direction
        ) {
            super();

            this.origin = origin;
            this.type = type;
            this.direction = direction;
        }

        public Coordinates getOrigin() {
            return origin;
        }

        public ProjectileType getType() {
            return type;
        }

        public int getDirection() {
            return direction;
        }
    }

    public static enum ProjectileType {
        PENGUIN,
        RAM
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicProjectileCommand() { }
}