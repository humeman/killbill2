package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

import java.util.List;
import java.util.UUID;

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
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;
import today.tecktip.killbill.frontend.gameserver.game.LocalGameUserState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameUserState;

/**
 * Receives player state changes.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicRecvPlayerStateCommand {
    /**
     * JSON deserializer
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Data attached to refresh player state commands or get player state responses.
     */
    public static class BasicRecvPlayerStateData extends MessageData {
        /**
         * New player type.
         */
        private final BasicPlayerType playerType;

        /**
         * New user coordinates.
         */
        private final Coordinates coordinates;

        /**
         * New rotation in degrees.
         */
        private final Integer rotation;

        /**
         * User ID with changed coordinates.
         */
        private final UUID userId;

        /**
         * New health.
         */
        private final Integer health;

        /**
         * New max health.
         */
        private final Integer maxHealth;

        /**
         * New held item texture.
         */
        private final String heldItemTexture;

        /**
         * New texture prefix.
         */
        private final String texturePrefix;

        /**
         * Constructs a new basic refresh user state client command response.
         * @param userId User ID with changed coordinates
         * @param coordinates Updated coordinates
         */
        public BasicRecvPlayerStateData(
            @JsonProperty(value = "userId", required = true) UUID userId,
            @JsonProperty(value = "coordinates", required = false) List<Double> coordinates,
            @JsonProperty(value = "rotation", required = false) Integer rotation,
            @JsonProperty(value = "playerType", required = false) String playerType,
            @JsonProperty(value = "health", required = false) Integer health,
            @JsonProperty(value = "maxHealth", required = false) Integer maxHealth,
            @JsonProperty(value = "heldItemTexture", required = false) String heldItemTexture,
            @JsonProperty(value = "texturePrefix", required = false) String texturePrefix
        ) {
            super(MessageDataType.COMMAND_RECV_PLAYER_STATE);
            if (coordinates != null) {
                this.coordinates = Coordinates.fromList(coordinates);
            } else {
                this.coordinates = null;
            }
            this.userId = userId;
            this.rotation = rotation;
            if (playerType != null) {
                this.playerType = BasicPlayerType.valueOf(playerType);
            } else {
                this.playerType = null;
            }
            this.health = health;
            this.maxHealth = maxHealth;
            this.heldItemTexture = heldItemTexture;
            this.texturePrefix = texturePrefix;
        }

        /**
         * Redefines the message data type. For internal use only.
         * @param newType New data type
         */
        public void setType(final MessageDataType newType) {
            type = newType;
        }

        /**
         * Gets the coordinates JSON property for this command.
         * @return Coordinates
         */
        @JsonProperty("coordinates")
        public List<Double> getCoordinatesAsList() {
            if (coordinates == null) return null;
            return coordinates.toList();
        }

        /**
         * Gets the player's new coordinates, if applicable.
         * @return New coordinates
         */
        public Coordinates getCoordinates() {
            return coordinates;
        }

        /**
         * Gets the user ID JSON property for this command.
         * @return User ID
         */
        @JsonProperty("userId")
        public String getUserIdAsString() {
            if (userId == null) return null;
            return userId.toString();
        }

        /**
         * Gets the ID of the user whose state changed.
         * @return User ID
         */
        public UUID getUserId() {
            return userId;
        }

        /**
         * Gets the user's new rotation.
         * @return Rotation in degrees
         */
        @JsonProperty("rotation")
        public Integer getRotation() {
            if (rotation == null) return null;
            return rotation;
        }

        /**
         * Gets the player type JSON property for this command.
         * @return Player type as a string
         */
        @JsonProperty("playerType")
        public String getPlayerTypeAsString() {
            if (playerType == null) return null;
            return playerType.toString();
        }

        /**
         * Gets the player type of this player, if it changed.
         * @return Player type
         */
        public BasicPlayerType getPlayerType() {
            return playerType;
        }

        /**
         * Gets the user's new health.
         * @return Health
         */
        @JsonProperty("health")
        public Integer getHealth() {
            return health;
        }

        /**
         * Gets the user's new max health.
         * @return Max health
         */
        @JsonProperty("maxHealth")
        public Integer getMaxHealth() {
            return maxHealth;
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
         * @return Texture prefix
         */
        @JsonProperty("texturePrefix")
        public String getTexturePrefix() {
            return texturePrefix;
        }
    }

    /**
     * Outgoing get state command data.
     */
    public static class BasicGetPlayerStateCommandData extends MessageData {
        /**
         * Target user ID.
         */
        private UUID userId;
        
        /**
         * Constructs get player state request data.
         * @param userId ID of the user to retrieve state for.
         */
        public BasicGetPlayerStateCommandData(
            UUID userId
        ) {
            super(MessageDataType.COMMAND_GET_PLAYER_STATE);
            this.userId = userId;
        }

        /**
         * Gets the targeted user's ID.
         * @return
         */
        public UUID getUserId() {
            return userId;
        }

        /**
         * Gets the JSON property for the target user ID.
         * @return Target user ID as a string
         */
        @JsonProperty("userId")
        public String getUserIdAsString() {
            return userId.toString();
        }
    }

    @ParseMethod(type = MessageDataType.COMMAND_RECV_PLAYER_STATE)
    public BasicRecvPlayerStateData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicRecvPlayerStateData.class);
    }

    @ParseMethod(type = MessageDataType.RESP_GET_PLAYER_STATE)
    public BasicRecvPlayerStateData parseResp(final JsonNode node) {
        // No problem to duplicate these, since the data is identical. We just need the type to be
        // the same.
        BasicRecvPlayerStateData data = MAPPER.convertValue(node, BasicRecvPlayerStateData.class);
        data.setType(MessageDataType.RESP_GET_PLAYER_STATE);
        return data;
    }

    /**
     * Runs the Recv Player State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_PLAYER_STATE)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
        final BasicLocalGameState game = (BasicLocalGameState) ClientMessageHandler.get().getGameState();

        final BasicRecvPlayerStateData data = (BasicRecvPlayerStateData) message.data();

        // Find the specified user
        LocalGameUserState targetUser = game.getConnectedUsers().get(data.getUserId());
        if (targetUser == null) {
            // This user has new state, but has not been initialized yet.
            // Add them to the state and kick off the init process.
            game.addUser(data.getUserId());
            targetUser = game.getUser(data.getUserId());

            // Queue some things, then continue.
            targetUser.getPlayerData(false);
        }
        BasicLocalGameUserState targetUserState = (BasicLocalGameUserState) targetUser;
        // The player is available. Update state.
        updateState(targetUserState, data, message);

        // We will not update the player state.

        targetUserState.setReady();

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
     * Runs the Recv Player State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @ResponseMethod(type = MessageDataType.RESP_GET_PLAYER_STATE)
    public void runResponse(final MessageHandler handler, final IncomingMessage message, final CommandContext context) {
        final BasicLocalGameState game = (BasicLocalGameState) ClientMessageHandler.get().getGameState();

        final BasicRecvPlayerStateData data = (BasicRecvPlayerStateData) message.data();

        // Find the specified user
        LocalGameUserState targetUser = game.getConnectedUsers().get(data.getUserId());
        if (targetUser == null) {
            // This user has new state, but has not been initialized yet.
            // Add them to the state and kick off the init process.
            game.addUser(data.getUserId());
            targetUser = game.getUser(data.getUserId());
        } 
        BasicLocalGameUserState targetUserState = (BasicLocalGameUserState) targetUser;
        // The player is available. Update state.
        updateState(targetUserState, data, message);

        targetUserState.setReady();
    }

    private void updateState(final BasicLocalGameUserState targetUserState, final BasicRecvPlayerStateData data, final IncomingMessage message) {
        if (data.getRotation() != null) {
            targetUserState.setRotation(message.createdAt(), data.getRotation());
        }

        if (data.getCoordinates() != null) {
            targetUserState.setCoordinates(message.createdAt(), data.getCoordinates());
        }

        if (data.getPlayerType() != null) {
            targetUserState.setPlayerType(message.createdAt(), data.getPlayerType());
        }

        if (data.getHealth() != null) {
            if (targetUserState.getUserId().equals(KillBillGame.get().getUser().id())) {
                System.err.println("Updated own health to " + data.getHealth());
            }
            targetUserState.setHealth(message.createdAt(), data.getHealth());
        }

        if (data.getMaxHealth() != null) {
            targetUserState.setMaxHealth(message.createdAt(), data.getMaxHealth());
        }

        if (data.getHeldItemTexture() != null) {
            targetUserState.setHeldItemTexture(message.createdAt(), data.getHeldItemTexture());
        }

        if (data.getTexturePrefix() != null) {
            targetUserState.setTexturePrefix(message.createdAt(), data.getTexturePrefix());
        }
    }

    /**
     * Runs the Refresh Player State client command.
     * @param gameState Game state to refresh locations for
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_GET_PLAYER_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        UUID userId = ((GetPlayerStateInvokeContext) context).getUserId();
        
        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(new BasicGetPlayerStateCommandData(userId))
                    .build());
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON error", e);
        }
    }

    /**
     * Context for the get player state command invocation.
     */
    public static class GetPlayerStateInvokeContext extends InvokeContext {
        /**
         * User ID to retrieve
         */
        private UUID userId;

        /**
         * Constructs context data for the get player state command.
         * @param userId ID of user to retrieve
         */
        public GetPlayerStateInvokeContext(final UUID userId) {
            this.userId = userId;
        }

        /**
         * Gets the UUID to retrieve state for.
         * @return User ID to update
         */
        public UUID getUserId() {
            return userId;
        }
    }

    /**
     * This class should not be instantiated.
     */
    public BasicRecvPlayerStateCommand() {}
}