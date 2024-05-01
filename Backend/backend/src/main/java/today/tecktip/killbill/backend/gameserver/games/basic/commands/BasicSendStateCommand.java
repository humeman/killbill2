package today.tecktip.killbill.backend.gameserver.games.basic.commands;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicDroppedItemState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicEntityState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState.BasicGameRunState;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicDroppedItemCommand.RecvCreateDroppedItemContext;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicInteractCommand.BasicInteractCommandData;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicInteractCommand.RecvInteractionContext;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvEntityStateCommand.RecvEntityStateInvokeContext;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvPlayerStateCommand.PlayerStateFieldFilter;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvPlayerStateCommand.RecvPlayerStateInvokeContext;
import today.tecktip.killbill.common.gameserver.games.GameType;
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
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;

/**
 * Allows clients to request all state data when they connect.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicSendStateCommand {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicSendStateCommand.class);

    /**
     * JSON deserializer
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonSerialize
    public static class BasicSendStateCommandData extends MessageData {
        /**
         * Constructs a send state command.
         */
        public BasicSendStateCommandData() {
            super(MessageDataType.COMMAND_SEND_STATE);
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_SEND_STATE)
    public BasicSendStateCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicSendStateCommandData.class);
    }

    /**
     * Runs the Send State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_SEND_STATE)
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
                    .data(new IllegalStateExceptionData("COMMAND_SEND_STATE requires connection."))
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

        user.getClient().send(
            OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .ackMessageId(message.messageId())
                .data(new EmptyData())
                .build());

        // 1: Send all players
        final List<UUID> me = List.of(user.getUser().id());
        for (final GameUserState rUserState : game.getConnectedUsers().values()) {
            BasicGameUserState userState = (BasicGameUserState) rUserState;

            try {
                SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_PLAYER_STATE)
                    .run(
                        SpringMessageHandler.get(),
                        new RecvPlayerStateInvokeContext(game, userState, null, List.of(PlayerStateFieldFilter.values()), me)
                    );
            } catch (final MessageFailure e) {
                LOGGER.error("Failure in invoking COMMAND_RECV_PLAYER_STATE, some clients may be desynced.", e);
            }
        }

        // 2: Send all entities
        for (final BasicEntityState entityState : game.getEntities().values()) {
            try {
                SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_ENTITY_STATE)
                    .run(
                        SpringMessageHandler.get(),
                        new RecvEntityStateInvokeContext(game, entityState, me, null)
                    );
            } catch (final MessageFailure e) {
                LOGGER.error("Failure in invoking COMMAND_RECV_ENTITY_STATE, some clients may be desynced.", e);
            }
        }

        // 3: Send all dropped items
        for (final BasicDroppedItemState itemState : game.getDroppedItems().values()) {
            try {
                SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM)
                    .run(
                        SpringMessageHandler.get(),
                        new RecvCreateDroppedItemContext(game, itemState, me)
                    );
            } catch (final MessageFailure e) {
                LOGGER.error("Failure in invoking COMMAND_RECV_NEW_DROPPED_ITEM, some clients may be desynced.", e);
            }
        }

        // 4: Send all interactions
        for (final BasicInteractCommandData interactData : game.getInteractions()) {
            try {
                SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_INTERACTION)
                    .run(
                        SpringMessageHandler.get(),
                        new RecvInteractionContext(game, interactData, me)
                    );
            } catch (final MessageFailure e) {
                LOGGER.error("Failure in invoking COMMAND_RECV_INTERACTION, some clients may be desynced.", e);
            }
        }
    }
}