package today.tecktip.killbill.backend.gameserver.games.basic.commands;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameState;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState.BasicGameRunState;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.CommandContext;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.CommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.exceptions.IllegalStateExceptionData;

/**
 * Gives updated game state info to all players.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicRecvGameStateCommand {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicRecvGameStateCommand.class);

    /**
     * Outgoing recv state command + response data.
     */
    public static class BasicRecvGameStateData extends MessageData {

        private BasicGameRunState runState;

        private List<UUID> users;

        private BasicPlayerType winningTeam;

        /**
         * Constructs get state command data.
         * @param state New state
         */
        public BasicRecvGameStateData(
            @JsonProperty(value = "runState", required = false) final BasicGameRunState runState,
            @JsonProperty(value = "users", required = false) final List<UUID> users,
            @JsonProperty(value = "winningTeam", required = false) final BasicPlayerType winningTeam
        ) {
            super(MessageDataType.COMMAND_RECV_GAME_STATE);
            this.runState = runState;
            this.users = users;
            this.winningTeam = winningTeam;
        }

        /**
         * Constructs get state response data.
         * @param state Game state to send
         */
        public BasicRecvGameStateData(
            final BasicGameState state
        ) {
            super(MessageDataType.RESP_GET_GAME_STATE);
            this.runState = state.getState();
            this.users = state.getConnectedUsers().entrySet().stream()
                .map(kv -> {
                    return kv.getValue().getUser().id();
                })
                .toList();
            this.winningTeam = state.getWinningTeam();
        }

        /**
         * Gets the JSON property for the connected user IDs.
         * @return List of connected user IDs
         */
        @JsonProperty("users")
        public List<String> getUserIdsAsString() {
            if (users == null) return null;
            return users.stream()
                .map(v -> {
                    return v.toString();
                })
                .toList();
        }

        /**
         * Gets the JSON property for the game run state.
         * @return Game run state as a string
         */
        @JsonProperty("runState")
        public String getRunStateAsString() {
            if (runState == null) return null;
            return runState.toString();
        }

        /**
         * Gets the JSON property for the winning team.
         * @return Winning team as a string
         */
        @JsonProperty("winningTeam")
        public String getWinningTeam() {
            if (winningTeam == null) return null;
            return winningTeam.toString();
        }
    }

    /**
     * Incoming get state command data.
     */
    public static class BasicGetGameStateCommandData extends MessageData {
        /**
         * Constructs get state response data.
         * @param state New state
         */
        public BasicGetGameStateCommandData() {
            super(MessageDataType.COMMAND_GET_GAME_STATE);
        }
    }

    @ParseMethod(type = MessageDataType.COMMAND_GET_GAME_STATE)
    public BasicGetGameStateCommandData parse(final JsonNode node) {
        return new BasicGetGameStateCommandData();
    }

    /**
     * Runs the Get Game State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_GET_GAME_STATE)
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
                    .data(new IllegalStateExceptionData("COMMAND_GET_GAME_STATE requires connection."))
                    .build());
            return;
        }

        user.getClient().send(
            OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .ackMessageId(message.messageId())
                .data(new BasicRecvGameStateData(game))
                .build());
    }

    /**
     * Runs the Refresh Player State client command.
     * @param gameState Game state to refresh locations for
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_RECV_GAME_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws JsonProcessingException {
        BasicGameState gameState = (BasicGameState) ((RecvGameStateInvokeContext) context).getGameState();
        List<UUID> userFilter = ((RecvGameStateInvokeContext) context).getFilter();
        List<GameStateFieldFilter> fieldFilter = ((RecvGameStateInvokeContext) context).getFieldFilter();

        // Generate a message body for each connected user and send it out
        final BasicRecvGameStateData data = new BasicRecvGameStateData(
            fieldFilter.contains(GameStateFieldFilter.RUN_STATE) ? gameState.getState() : null,
            fieldFilter.contains(GameStateFieldFilter.USERS) ? gameState.getConnectedUsers().entrySet().stream().map(kv -> { return kv.getValue().getUser().id(); }).toList() : null,
            fieldFilter.contains(GameStateFieldFilter.WINNING_TEAM) ? gameState.getWinningTeam() : null
        );
        for (GameUserState userState : gameState.getConnectedUsers().values()) {
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

            try {
                userState.getClient().send(
                    OutgoingMessage.newBuilder()
                        .setKey(handler)
                        .success()
                        .randomMessageId()
                        .data(data)
                        .build());
            } catch (final Throwable t) {
                LOGGER.error("Failure in game state send to userId={}, continuing anyway", userState.getUser().id(), t);
            }
        }
    }

    /**
     * Context for the receive game state command invocation.
     */
    public static class RecvGameStateInvokeContext extends InvokeContext {
        /**
         * Game state for this invocation
         */
        private GameState gameState;

        /**
         * The UUIDs to filter sends to.
         */
        private List<UUID> filter;

        /**
         * Fields to filter in when sending data.
         */
        private List<GameStateFieldFilter> fieldFilter;

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param fieldFilter Fields to send through (ie: modified fields)
         */
        public RecvGameStateInvokeContext(final GameState gameState, final List<GameStateFieldFilter> fieldFilter) {
            this.gameState = gameState;
            this.fieldFilter = fieldFilter;
            filter = null;
        }

        /**
         * Constructs context data for the receive player state command.
         * @param gameState Game state to send state for
         * @param fieldFilter Fields to send through (ie: modified fields)
         * @param filter Filters to only send to specified users
         */
        public RecvGameStateInvokeContext(final GameState gameState, final List<GameStateFieldFilter> fieldFilter, final List<UUID> filter) {
            this.gameState = gameState;
            this.fieldFilter = fieldFilter;
            this.filter = filter;
        }

        /**
         * Gets the game state for this invocation.
         * @return Game state to send state for
         */
        public GameState getGameState() {
            return gameState;
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
        public List<GameStateFieldFilter> getFieldFilter() {
            return fieldFilter;
        }
    }

    /**
     * Defines fields to filter to when sending player states.
     * Avoids sending extraneous data.
     */
    public static enum GameStateFieldFilter {
        RUN_STATE,
        USERS,
        WINNING_TEAM
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicRecvGameStateCommand() { }
}