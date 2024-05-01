package today.tecktip.killbill.backend.gameserver.games.basic.commands;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvGameStateCommand.GameStateFieldFilter;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvGameStateCommand.RecvGameStateInvokeContext;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState.BasicGameRunState;
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
import today.tecktip.killbill.common.gameserver.messages.exceptions.InternalServerErrorData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InvalidArgumentExceptionData;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;

/**
 * Allows the host to begin/end the game.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicChangeGameStateCommand {
    /**
     * Jackson thingy
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Incoming change game command data.
     */
    public static class BasicChangeGameStateCommandData extends MessageData {
        private final BasicGameRunState runState;

        /**
         * Constructs a change game state command.
         * @param state New state
         */
        public BasicChangeGameStateCommandData(
            @JsonProperty(value = "runState", required = false) String newState
        ) {
            super(MessageDataType.COMMAND_CHANGE_GAME_STATE);

            if (newState != null) runState = BasicGameRunState.valueOf(newState);
            else this.runState = null;
        }

        /**
         * Gets the new state the game is in.
         * @return New state
         */
        public BasicGameRunState getNewState() {
            return runState;
        }

        /**
         * Parses a JSON node into resend command data.
         * @param node JSON node
         * @return Parsed data
         */
        public static BasicChangeGameStateCommandData parse(final JsonNode node) throws IllegalArgumentException {
            return MAPPER.convertValue(node, BasicChangeGameStateCommandData.class);
        }
    }

    /**
     * Outgoing game state changed data.
     */
    public static class BasicGameStateChangedResponseData extends MessageData {
        /**
         * Constructs game state changed response data.
         * @param state New state
         */
        public BasicGameStateChangedResponseData() {
            super(MessageDataType.RESP_GAME_STATE_CHANGED);
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_CHANGE_GAME_STATE)
    public BasicChangeGameStateCommandData parse(final JsonNode node) {
        return BasicChangeGameStateCommandData.parse(node);
    }

    /**
     * Runs the Change Game State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_CHANGE_GAME_STATE)
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

        // User must be the host
        if (!user.getUser().id().equals(game.getGame().hostId())) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new InvalidArgumentExceptionData("You must be the host to use COMMAND_CHANGE_GAME_STATE."))
                    .build());
            return;
        }

        final BasicChangeGameStateCommandData data = (BasicChangeGameStateCommandData) message.data();
        final List<GameStateFieldFilter> fieldFilter = new ArrayList<>();

        // Are we starting the game?
        BasicGameRunState runState = null;
        if (data.getNewState() != null) {
            switch (data.getNewState()) {
                case LOBBY:
                    // Cannot be in any other state. Realistically this shouldn't have been called
                    user.getClient().send(
                        OutgoingMessage.newBuilder()
                            .setKey(handler)
                            .failure()
                            .ackMessageId(message.messageId())
                            .data(new IllegalStateExceptionData("Illegal state: cannot return to LOBBY."))
                            .build());
                    return;
                case PLAYING:
                    // Must be in the lobby
                    if (!game.isInState(BasicGameRunState.LOBBY)) {
                        user.getClient().send(
                            OutgoingMessage.newBuilder()
                                .setKey(handler)
                                .failure()
                                .ackMessageId(message.messageId())
                                .data(new IllegalStateExceptionData("Illegal state: can only enter PLAYING from LOBBY."))
                                .build());
                        return;
                    }
                    break;
                default:
                    break;
            }

            // Set the state
            if (!game.getState().equals(data.getNewState())) {
                runState = game.getState();
                game.setState(data.getNewState());
                fieldFilter.add(GameStateFieldFilter.RUN_STATE);
            }
        }

        // In
        if (runState != null) {
            game.onStateChange(runState);
        }

        // Send this state change out
        try {
            handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_GAME_STATE).run(
                handler,
                new RecvGameStateInvokeContext(game, fieldFilter)
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
    public BasicChangeGameStateCommand() { }
}