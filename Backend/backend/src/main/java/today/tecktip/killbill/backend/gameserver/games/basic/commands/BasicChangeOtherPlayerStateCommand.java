package today.tecktip.killbill.backend.gameserver.games.basic.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState.BasicGameRunState;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvPlayerStateCommand.PlayerStateFieldFilter;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvPlayerStateCommand.RecvPlayerStateInvokeContext;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicSendChatCommand.RecvSystemMessageInvokeContext;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.CommandContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.CommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.exceptions.IllegalStateExceptionData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InternalServerErrorData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InvalidArgumentExceptionData;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;

/**
 * Updates a user's state.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicChangeOtherPlayerStateCommand {
    /**
     * JSON deserializer
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BasicChangeOtherPlayerStateCommandData extends MessageData {
        /**
         * The damage to deal to the player.
         */
        private final Integer damage;

        /**
         * The target user's ID.
         */
        private final UUID userId;

        /**
         * Constructs a new change location command message.
         * @param coordinates New coordinates
         */
        public BasicChangeOtherPlayerStateCommandData(
            @JsonProperty(value = "userId", required = true) final UUID userId,
            @JsonProperty(value = "damage", required = false) final Integer damage
        ) {
            super(MessageDataType.COMMAND_CHANGE_OTHER_PLAYER_STATE);
            if (damage != null) {
                this.damage = damage;
            } else this.damage = null;

            this.userId = userId;
        }

        /**
         * Gets the target user's ID.
         * @return Target user ID
         */
        public UUID getUserId() {
            return userId;
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
    @ParseMethod(type = MessageDataType.COMMAND_CHANGE_OTHER_PLAYER_STATE)
    public BasicChangeOtherPlayerStateCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicChangeOtherPlayerStateCommandData.class);
    }

    /**
     * Runs the Change Player State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_CHANGE_OTHER_PLAYER_STATE)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws JsonProcessingException {
        final BasicGameUserState user = (BasicGameUserState) SpringMessageHandler.userStateFrom(context);
        final BasicGameState game = (BasicGameState) SpringMessageHandler.gameStateFrom(context);

        final BasicChangeOtherPlayerStateCommandData data = (BasicChangeOtherPlayerStateCommandData) message.data();

        // Require connection
        if (!user.isConnected()) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("COMMAND_CHANGE_OTHER_PLAYER_STATE requires connection."))
                    .build());
            return;
        }

        final GameUserState targetUserM;
        try {
            targetUserM = game.getUser(data.getUserId());
        } catch (final SQLException e) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InternalServerErrorData("This should never happen."))
                    .build());
            return;
        }

        if (targetUserM == null) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InvalidArgumentExceptionData("No such player."))
                    .build());
            return;
        }

        final BasicGameUserState targetUser = (BasicGameUserState) targetUserM;

        // Require target connection
        if (!targetUser.isConnected()) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("COMMAND_CHANGE_OTHER_PLAYER_STATE requires target connection."))
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

        final List<PlayerStateFieldFilter> fieldFilter = new ArrayList<>();

        UUID senderFilter = user.getUser().id();

        if (data.getDamage() != null) {
            targetUser.setHealth(targetUser.getHealth() - data.getDamage());
            fieldFilter.add(PlayerStateFieldFilter.HEALTH);

            // Did they die?
            if (targetUser.getHealth() <= 0) {
                // Type is now Spectator, and don't filter the sender out.
                senderFilter = null;
                try {
                    handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE).run(
                        handler,
                        new RecvSystemMessageInvokeContext("[" + user.getPlayerType() + "] " + user.getUser().name() + " killed [" + targetUser.getPlayerType() + "] " + targetUser.getUser().name() + ".", game)
                    );
                } catch (final MessageFailure e) {
                    System.err.println("Failed to send player death message." + e);
                    e.printStackTrace();
                }
                targetUser.setTexturePrefix(game.getGame().config().getBasicConfig().getPlayerConfig().get(BasicPlayerType.SPECTATOR).get(0).getTexturePrefix());
                targetUser.setPlayerType(BasicPlayerType.SPECTATOR);
                fieldFilter.add(PlayerStateFieldFilter.PLAYER_TYPE);
                fieldFilter.add(PlayerStateFieldFilter.TEXTURE_PREFIX);

                // Perform end-game stuff if necessary
                game.checkGameEnd();
            }
        }

        // Send this state change out
        try {
            handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_PLAYER_STATE).run(
                handler,
                new RecvPlayerStateInvokeContext(game, targetUser, senderFilter, fieldFilter)
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
}