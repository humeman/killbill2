package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState.BasicGameRunState;

/**
 * Receives game state changes.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicRecvGameStateCommand {
    /**
     * JSON deserializer
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Outgoing recv state command + response data.
     */
    public static class BasicRecvGameStateData extends MessageData {

        private BasicGameRunState runState;

        private BasicPlayerType winningTeam;

        private List<UUID> users;

        /**
         * Constructs get state command data.
         * @param state New state
         */
        public BasicRecvGameStateData(
            @JsonProperty(value = "runState", required = false) final String runState,
            @JsonProperty(value = "users", required = false) final List<String> users,
            @JsonProperty(value = "winningTeam", required = false) final String winningTeam
        ) {
            super(MessageDataType.COMMAND_RECV_GAME_STATE);

            if (runState != null) this.runState = BasicGameRunState.valueOf(runState);
            else this.runState = null;

            if (users != null) this.users = users.stream().map(UUID::fromString).toList();
            else this.users = null;
 
            if (winningTeam != null) this.winningTeam = BasicPlayerType.valueOf(winningTeam);
            else this.winningTeam = null;
        }

        /**
         * Constructs get state command data, assuming this is an outgoing message.
         * @param runState New state
         */
        public BasicRecvGameStateData(
            final BasicGameRunState runState
        ) {
            super(MessageDataType.COMMAND_CHANGE_GAME_STATE);
            this.runState = runState;
        }

        /**
         * Redefines the message data type. For internal use only.
         * @param newType New data type
         */
        public void setType(final MessageDataType newType) {
            type = newType;
        }

        /**
         * Gets the changed list of connected user IDs.
         * @return List of connected user IDs or null
         */
        @JsonIgnore
        public List<UUID> getUserIds() {
            return users;
        }

        /**
         * Gets the changed game run state.
         * @return Game run state or null
         */
        @JsonIgnore
        public BasicGameRunState getState() {
            return runState;
        }

        /**
         * Gets the JSON property version of the run state.
         * @return Run state as a string
         */
        @JsonProperty("runState")
        public String getStateAsString() {
            if (runState != null) return runState.toString();
            else return null;
        }

        /**
         * Gets the winning team, given only if the game just ended.
         * @return Winning team or null
         */
        @JsonIgnore
        public BasicPlayerType getWinningTeam() {
            return winningTeam;
        }
    }

    /**
     * Outgoing get state command data.
     */
    public static class BasicGetGameStateCommandData extends MessageData {
        /**
         * Constructs get game state request data.
         */
        public BasicGetGameStateCommandData() {
            super(MessageDataType.COMMAND_GET_GAME_STATE);
        }
    }

    @ParseMethod(type = MessageDataType.COMMAND_RECV_GAME_STATE)
    public BasicRecvGameStateData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicRecvGameStateData.class);
    }

    @ParseMethod(type = MessageDataType.RESP_GET_GAME_STATE)
    public BasicRecvGameStateData parseResp(final JsonNode node) {
        // No problem to duplicate these, since the data is identical. We just need the type to be
        // the same.
        BasicRecvGameStateData data = MAPPER.convertValue(node, BasicRecvGameStateData.class);
        data.setType(MessageDataType.RESP_GET_GAME_STATE);
        return data;
    }

    /**
     * Runs the Recv Game State command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_GAME_STATE)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
        final BasicLocalGameState game = (BasicLocalGameState) ClientMessageHandler.get().getGameState();

        final BasicRecvGameStateData data = (BasicRecvGameStateData) message.data();

        // Update values
        if (data.getWinningTeam() != null) {
            game.setWinningTeam(data.getWinningTeam());
        }

        if (data.getState() != null) {
            if (!data.getState().equals(game.getState())) {
                game.setState(data.getState());
            }
        }

        if (data.getUserIds() != null) {
            game.checkUserList(data.getUserIds());
        }

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

    /**
     * Reads in data from responses to GET_GAME_STATE.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     */
    @ResponseMethod(type = MessageDataType.RESP_GET_GAME_STATE)
    public void runResponse(final MessageHandler handler, final IncomingMessage message, final CommandContext context) {
        final BasicLocalGameState game = (BasicLocalGameState) ClientMessageHandler.get().getGameState();

        final BasicRecvGameStateData data = (BasicRecvGameStateData) message.data();

        // Update values
        if (data.getWinningTeam() != null) {
            game.setWinningTeam(data.getWinningTeam());
        }
        
        if (data.getState() != null) {
            if (!data.getState().equals(game.getState())) {
                game.setState(data.getState());
            }
        }

        if (data.getUserIds() != null) {
            game.checkUserList(data.getUserIds());
        }
    }

    /**
     * Runs the Get Game State command.
     * @param handler Message handler
     * @param context Ignored, leave as null
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_GET_GAME_STATE)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(new BasicGetGameStateCommandData())
                    .build());
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON error", e);
        }
    }


    /**
     * This class should not be instantiated.
     */
    public BasicRecvGameStateCommand() {}
}