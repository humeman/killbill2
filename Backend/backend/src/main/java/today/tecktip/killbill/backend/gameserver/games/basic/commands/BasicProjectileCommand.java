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
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;

/**
 * Allows clients to interact with objects.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicProjectileCommand {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicInteractCommand.class);
    
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
            @JsonProperty(value = "direction", required = true) int direction
        ) {
            super(MessageDataType.COMMAND_CREATE_PROJECTILE);

            this.projectileType = ProjectileType.valueOf(projectileType);
            this.origin = Coordinates.fromList(origin);
            this.direction = direction;
            launchedBy = null;
        }

        public BasicProjectileCommandData(
            final MessageDataType typeOverride,
            final ProjectileType projectileType,
            final Coordinates origin,
            final int direction,
            final UUID launchedBy
        ) {
            super(typeOverride);

            this.projectileType = projectileType;
            this.origin = origin;
            this.direction = direction;
            this.launchedBy = launchedBy;
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
         * Direction the projectile is heading.
         * @return Direction (angle, 0-360, regular libGDX coords)
         */
        @JsonProperty("direction")
        public int getDirection() {
            return direction;
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
         * @return Projectile origin as a list of doubles
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
    @ParseMethod(type = MessageDataType.COMMAND_CREATE_PROJECTILE)
    public BasicProjectileCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicProjectileCommandData.class);
    }

    /**
     * Runs the Create Projectile command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_CREATE_PROJECTILE)
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
                    .data(new IllegalStateExceptionData("COMMAND_CHANGE_GAME_STATE requires connection."))
                    .build());
            return;
        }

        final BasicProjectileCommandData data = (BasicProjectileCommandData) message.data();

        // Send this interaction out
        try {
            handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_PROJECTILE).run(
                handler,
                new RecvProjectileContext(game, data, user.getUser().id())
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
     * Runs the Receive Interaction command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_RECV_PROJECTILE)
    public void send(final MessageHandler handler, final InvokeContext context) throws JsonProcessingException {
        BasicGameState gameState = (BasicGameState) ((RecvProjectileContext) context).getGameState();
        BasicProjectileCommandData data = ((RecvProjectileContext) context).getData();
        UUID sender = ((RecvProjectileContext) context).getSender();

        // Generate a message body for each connected user and send it out
        final BasicProjectileCommandData newData = new BasicProjectileCommandData(
            MessageDataType.COMMAND_RECV_PROJECTILE,
            data.getProjectileType(),
            data.getOrigin(),
            data.getDirection(),
            sender
        );
        for (GameUserState userState : gameState.getConnectedUsers().values()) {
            if (sender != null && userState.getUser().id().equals(sender)) continue;

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
    public static class RecvProjectileContext extends InvokeContext {
        /**
         * Game state for this invocation
         */
        private GameState gameState;
        private final BasicProjectileCommandData data;
        private final UUID sender;

        /**
         * Constructs context data for the receive interaction command.
         * @param gameState Game state to send state for
         */
        public RecvProjectileContext(
            final GameState gameState,
            BasicProjectileCommandData data,
            UUID sender
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
         * Gets the projectile's data.
         * @return Data
         */
        public BasicProjectileCommandData getData() {
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

    public static enum ProjectileType {
        PENGUIN,
        RAM
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicProjectileCommand() { }
}