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
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.CommandContext;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.CommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.exceptions.IllegalStateExceptionData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InvalidArgumentExceptionData;

/**
 * Sends a user's entire state to connected clients.
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
         * Player's type.
         */
        private final BasicPlayerType playerType;

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
            @JsonProperty(value = "coordinates", required = false) Coordinates coordinates,
            @JsonProperty(value = "rotation", required = false) Integer rotation,
            @JsonProperty(value = "playerType", required = false) BasicPlayerType playerType,
            @JsonProperty(value = "health", required = false) Integer health,
            @JsonProperty(value = "maxHealth", required = false) Integer maxHealth,
            @JsonProperty(value = "heldItemTexture", required = false) String heldItemTexture,
            @JsonProperty(value = "texturePrefix", required = false) String texturePrefix
        ) {
            super(MessageDataType.COMMAND_RECV_PLAYER_STATE);
            this.coordinates = coordinates;
            this.userId = userId;
            this.rotation = rotation;
            this.playerType = playerType;
            this.health = health;
            this.maxHealth = maxHealth;
            this.heldItemTexture = heldItemTexture;
            this.texturePrefix = texturePrefix;
        }

        /**
         * Constructs GET_PLAYER_STATE data.
         * @param userState User to get state of
         */
        public BasicRecvPlayerStateData(
            final BasicGameUserState userState
        ) {
            super(MessageDataType.RESP_GET_PLAYER_STATE);
            this.coordinates = userState.getCoordinates();
            this.userId = userState.getUser().id();
            this.rotation = userState.getRotation();
            this.playerType = userState.getPlayerType();
            this.health = userState.getHealth();
            this.maxHealth = userState.getMaxHealth();
            this.heldItemTexture = userState.getHeldItemTexture();
            this.texturePrefix = userState.getTexturePrefix();
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
         * Gets the user ID JSON property for this command.
         * @return User ID
         */
        @JsonProperty("userId")
        public String getUserId() {
            return userId.toString();
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
         * Gets the user's new player type.
         * @return Player type (as a string)
         */
        @JsonProperty("playerType")
        public String getPlayerType() {
            if (playerType == null) return null;
            return playerType.toString();
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
     * Incoming get state command data.
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
            @JsonProperty(value = "userId", required = true) String userId
        ) {
            super(MessageDataType.COMMAND_GET_PLAYER_STATE);
            this.userId = UUID.fromString(userId);
        }

        /**
         * Gets the targeted user's ID.
         * @return
         */
        public UUID getUserId() {
            return userId;
        }
    }

    @ParseMethod(type = MessageDataType.COMMAND_GET_PLAYER_STATE)
    public BasicGetPlayerStateCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicGetPlayerStateCommandData.class);
    }

    /**
     * Runs the Get Player State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_GET_PLAYER_STATE)
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
                    .data(new IllegalStateExceptionData("COMMAND_GET_PLAYER_STATE requires connection."))
                    .build());
            return;
        }

        // Find the specified user
        final GameUserState targetUser = game.getConnectedUsers().get(((BasicGetPlayerStateCommandData) message.data()).getUserId());
        if (targetUser == null) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InvalidArgumentExceptionData("The specified user is not connected."))
                    .build());
            return;
        }

        user.getClient().send(
            OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .ackMessageId(message.messageId())
                .data(new BasicRecvPlayerStateData((BasicGameUserState) targetUser))
                .build());
    }

    /**
     * Runs the Refresh Player State client command.
     * @param gameState Game state to refresh locations for
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_RECV_PLAYER_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws JsonProcessingException {
        GameState gameState = ((RecvPlayerStateInvokeContext) context).getGameState();
        BasicGameUserState thisUser = (BasicGameUserState) ((RecvPlayerStateInvokeContext) context).getUserState();
        List<UUID> userFilter = ((RecvPlayerStateInvokeContext) context).getFilter();
        List<PlayerStateFieldFilter> fieldFilter = ((RecvPlayerStateInvokeContext) context).getFieldFilter();
        UUID skipSendFor = ((RecvPlayerStateInvokeContext) context).getSkipSendFor();

        // Generate a message body for each connected user and send it out
        final BasicRecvPlayerStateData data = new BasicRecvPlayerStateData(
            thisUser.getUser().id(),
            fieldFilter.contains(PlayerStateFieldFilter.COORDINATES) ? thisUser.getCoordinates() : null,
            fieldFilter.contains(PlayerStateFieldFilter.ROTATION) ? thisUser.getRotation() : null,
            fieldFilter.contains(PlayerStateFieldFilter.PLAYER_TYPE) ? thisUser.getPlayerType() : null,
            fieldFilter.contains(PlayerStateFieldFilter.HEALTH) ? thisUser.getHealth() : null,
            fieldFilter.contains(PlayerStateFieldFilter.MAX_HEALTH) ? thisUser.getMaxHealth() : null,
            fieldFilter.contains(PlayerStateFieldFilter.HELD_ITEM_TEXTURE) ? thisUser.getHeldItemTexture() : null,
            fieldFilter.contains(PlayerStateFieldFilter.TEXTURE_PREFIX) ? thisUser.getTexturePrefix() : null
        );

        boolean onlyLocation;
        if (fieldFilter.size() == 1 && (
                fieldFilter.contains(PlayerStateFieldFilter.COORDINATES)
                || fieldFilter.contains(PlayerStateFieldFilter.ROTATION))) {
            onlyLocation = true;
        }
        else if (fieldFilter.size() == 2 && 
            fieldFilter.contains(PlayerStateFieldFilter.COORDINATES)
            && fieldFilter.contains(PlayerStateFieldFilter.ROTATION)) {
            onlyLocation = true;
        } else {
            onlyLocation = false;
        }

        for (GameUserState userState : gameState.getConnectedUsers().values()) {
            // Don't send their own state back
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
            if (fieldFilter.contains(PlayerStateFieldFilter.HEALTH)) {
                System.err.println("Sending new health!");
            }
        }
    }

    /**
     * Context for the receive player state command invocation.
     */
    public static class RecvPlayerStateInvokeContext extends InvokeContext {
        /**
         * Game state for this invocation
         */
        private BasicGameState gameState;

        /**
         * User state for this invocation
         */
        private BasicGameUserState userState;

        /**
         * The UUIDs to filter sends to.
         */
        private List<UUID> filter;

        /**
         * Fields to filter in when sending data.
         */
        private List<PlayerStateFieldFilter> fieldFilter;

        /**
         * Denotes a user that shouldn't receive the state change (user that sent the command, usually).
         */
        private UUID skipSendFor;

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param userState User state to send state for
         * @param sendToSelf Denotes a user that shouldn't receive the state change. Can be null
         * @param fieldFilter Fields to send through (ie: modified fields)
         */
        public RecvPlayerStateInvokeContext(final BasicGameState gameState, final BasicGameUserState userState, final UUID skipSendFor, final List<PlayerStateFieldFilter> fieldFilter) {
            this.gameState = gameState;
            this.userState = userState;
            this.fieldFilter = fieldFilter;
            this.skipSendFor = skipSendFor;
            filter = null;
        }

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param userState User state to send state for
         * @param skipSendFor Denotes a user that shouldn't receive the state change. Can be null
         * @param fieldFilter Fields to send through (ie: modified fields)
         * @param filter Filters to only send to specified users
         */
        public RecvPlayerStateInvokeContext(final BasicGameState gameState, final BasicGameUserState userState, final UUID skipSendFor, final List<PlayerStateFieldFilter> fieldFilter, final List<UUID> filter) {
            this.gameState = gameState;
            this.userState = userState;
            this.fieldFilter = fieldFilter;
            this.filter = filter;
            this.skipSendFor = skipSendFor;
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
        public BasicGameUserState getUserState() {
            return userState;
        }

        /**
         * Gets the UUIDs to filter sends to.
         * @return User IDs to send state to
         */
        public List<UUID> getFilter() {
            return filter;
        }

        /**
         * Gets the fields to filter sends to.
         * @return Field filter
         */
        public List<PlayerStateFieldFilter> getFieldFilter() {
            return fieldFilter;
        }

        /**
         * Denotes a user that shouldn't receive the state change.
         * @return User to skip or null
         */
        public UUID getSkipSendFor() {
            return skipSendFor;
        }
    }

    /**
     * Defines fields to filter to when sending player states.
     * Avoids sending extraneous data.
     */
    public static enum PlayerStateFieldFilter {
        COORDINATES,
        ROTATION,
        PLAYER_TYPE,
        HEALTH,
        MAX_HEALTH,
        HELD_ITEM_TEXTURE,
        TEXTURE_PREFIX
    }

    /**
     * This class should not be instantiated.
     */
    public BasicRecvPlayerStateCommand() {}
}