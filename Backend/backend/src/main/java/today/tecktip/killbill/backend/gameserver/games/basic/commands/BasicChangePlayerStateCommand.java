package today.tecktip.killbill.backend.gameserver.games.basic.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState.BasicGameRunState;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvPlayerStateCommand.PlayerStateFieldFilter;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvPlayerStateCommand.RecvPlayerStateInvokeContext;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicSendChatCommand.RecvSystemMessageInvokeContext;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.games.GameType;
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
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;

/**
 * Updates a user's state.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicChangePlayerStateCommand {
    /**
     * JSON deserializer
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Incoming change location command data.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BasicChangePlayerStateCommandData extends MessageData {
        /**
         * The user's new coordinates.
         */
        private final Coordinates coordinates;

        /**
         * The player's facing direction.
         */
        private final Integer rotation;

        /**
         * The player's new health.
         */
        private final Integer health;

        /**
         * The player's new held item texture.
         */
        private final String heldItemTexture;

        /**
         * Constructs a new change location command message.
         * @param coordinates New coordinates
         */
        public BasicChangePlayerStateCommandData(
            @JsonProperty(value = "userId", required = false) final String userId,
            @JsonProperty(value = "coordinates", required = false) final List<Double> coordinates, 
            @JsonProperty(value = "rotation", required = false) final Integer rotation, 
            @JsonProperty(value = "health", required = false) final Integer health,
            @JsonProperty(value = "heldItemTexture", required = false) final String heldItemTexture
        ) {
            super(MessageDataType.COMMAND_CHANGE_PLAYER_STATE);
            if (coordinates != null) {
                this.coordinates = Coordinates.fromList(coordinates);
            } else this.coordinates = null;

            if (rotation != null) {
                this.rotation = rotation;
                if (rotation < 0 || rotation >= 360) throw new IllegalArgumentException("Rotation must be between 0 and 359 inclusive.");
            } else this.rotation = null;

            this.health = health;
            this.heldItemTexture = heldItemTexture;
        }

        /**
         * Gets the coordinates sent in this command.
         * @return Coordinates or null
         */
        public Coordinates getCoordinates() {
            return coordinates;
        }

        /**
         * Gets the rotation sent in this command.
         * @return Rotation or null
         */
        public Integer getRotation() {
            return rotation;
        }

        /**
         * Gets the health sent in this command.
         * @return Health or null
         */
        public Integer getHealth() {
            return health;
        }

        /**
         * Gets the held item texture sent in this command.
         * @return Held item texture or null
         */
        public String getHeldItemTexture() {
            return heldItemTexture;
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_CHANGE_PLAYER_STATE)
    public BasicChangePlayerStateCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicChangePlayerStateCommandData.class);
    }

    /**
     * Runs the Change Player State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_CHANGE_PLAYER_STATE)
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
                    .data(new IllegalStateExceptionData("COMMAND_CHANGE_PLAYER_STATE requires connection."))
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

        BasicChangePlayerStateCommandData data = (BasicChangePlayerStateCommandData) message.data();

        final List<PlayerStateFieldFilter> fieldFilter = new ArrayList<>();

        UUID senderFilter = user.getUser().id();

        if (data.getCoordinates() != null) {
            user.setCoordinates(message.createdAt(), data.getCoordinates());
            fieldFilter.add(PlayerStateFieldFilter.COORDINATES);
        }
        if (data.getRotation() != null) {
            user.setRotation(message.createdAt(), data.getRotation());
            fieldFilter.add(PlayerStateFieldFilter.ROTATION);
        }
        if (data.getHealth() != null) {
            user.setHealth(message.createdAt(), data.getHealth());
            fieldFilter.add(PlayerStateFieldFilter.HEALTH);

            // Did they die?
            if (user.getHealth() <= 0) {
                // Type is now Spectator, and don't filter the sender out.
                senderFilter = null;
                try {
                    handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE).run(
                        handler,
                        new RecvSystemMessageInvokeContext("[" + user.getPlayerType() + "] " + user.getUser().name() + " died.", game)
                    );
                } catch (final MessageFailure e) {
                    System.err.println("Failed to send player death message." + e);
                    e.printStackTrace();
                }
                user.setPlayerType(BasicPlayerType.SPECTATOR);
                fieldFilter.add(PlayerStateFieldFilter.PLAYER_TYPE);
                user.setTexturePrefix(game.getGame().config().getBasicConfig().getPlayerConfig().get(BasicPlayerType.SPECTATOR).get(0).getTexturePrefix());
                fieldFilter.add(PlayerStateFieldFilter.TEXTURE_PREFIX);

                game.checkGameEnd();
            }
        }
        if (data.getHeldItemTexture() != null) {
            user.setHeldItemTexture(message.createdAt(), data.getHeldItemTexture());
            fieldFilter.add(PlayerStateFieldFilter.HELD_ITEM_TEXTURE);
        }

        // Send this state change out
        try {
            handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_PLAYER_STATE).run(
                handler,
                new RecvPlayerStateInvokeContext(game, user, senderFilter, fieldFilter)
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
     * This class should not be manually instantiated.
     */
    public BasicChangePlayerStateCommand() { }
}