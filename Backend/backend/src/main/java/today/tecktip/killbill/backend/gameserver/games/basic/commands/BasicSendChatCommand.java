package today.tecktip.killbill.backend.gameserver.games.basic.commands;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameState;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.exceptions.MessageFailure;
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
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;

/**
 * Sends chat over the game server
 * @author Caleb Zea
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicSendChatCommand {
    /**
     * Incoming chat command data.
     */
    public static class BasicSendChatCommandData extends MessageData {
        /**
         * chat message
         */
        private final String message;

        /**
         * Constructs send chat command data
         * @param message chat message
         */
        public BasicSendChatCommandData(final String message) {
            super(MessageDataType.COMMAND_SEND_CHAT);
            this.message = message;
        }

        /**
         * Gets the chat message
         * @return message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Parses a JSON node into chat command data.
         * @param node JSON node
         * @return Parsed data
         */
        public static BasicSendChatCommandData parse(final JsonNode node) throws IllegalArgumentException {
            return new BasicSendChatCommandData(node.get("message").textValue());
        }
    }
    /**
     * Incoming chat command data.
     */
    public static class BasicRecvChatCommandData extends MessageData {
        /**
         * chat message
         */
        private final String message;

        /**
         * Sender's UUID
         */
        private final UUID userId;

        /**
         * Constructs send chat command data
         * @param message chat message
         * @param userId Sender's ID
         */
        public BasicRecvChatCommandData(final String message, final UUID userId) {
            super(MessageDataType.COMMAND_RECV_CHAT);
            this.message = message;
            this.userId = userId;
        }

        /**
         * Gets the chat message
         * @return message
         */
        @JsonProperty("message")
        public String getMessage() {
            return message;
        }

        /**
         * Gets the sender's UUID.
         * @return Sender UUID
         */
        @JsonProperty("userId")
        public String getUserId() {
            return userId.toString();
        }
    }

    /**
     * Outgoing system message command data.
     */
    public static class BasicRecvSystemMessageCommandData extends MessageData {
        /**
         * System message to display
         */
        private final String message;

        /**
         * Constructs send system message command data
         * @param message chat message
         */
        public BasicRecvSystemMessageCommandData(final String message) {
            super(MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE);
            this.message = message;
        }

        /**
         * Gets the chat message
         * @return message
         */
        @JsonProperty("message")
        public String getMessage() {
            return message;
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_SEND_CHAT)
    public BasicSendChatCommandData parse(final JsonNode node) {
        return BasicSendChatCommandData.parse(node);
    }

    /**
     * Sends a chat message
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_SEND_CHAT)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws JsonProcessingException {
        final GameUserState user = SpringMessageHandler.userStateFrom(context);
        final GameState game = SpringMessageHandler.gameStateFrom(context);

        // Require connection
        if (!user.isConnected()) {
            user.getClient().send(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .failure()
                    .ackMessageId(message.messageId())
                    .data(new IllegalStateExceptionData("COMMAND_SEND_CHAT requires connection."))
                    .build());
            return;
        }

        // Send to the other users
        try {
            handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_CHAT)
                .run(handler, new RecvChatInvokeContext(((BasicSendChatCommandData) message.data()).getMessage(), (BasicGameUserState) user, game));
        } catch (MessageFailure e) {
            System.out.println(e);
        }

        // Send back a response
        user.getClient().send(
            OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .ackMessageId(message.messageId())
                .data(new EmptyData())
                .build());
    }

    /**
     * Sends a chat message to all players other than the sender.
     * @param handler Message handler
     * @param context Instance of {@link RecvChatInvokeContext}
     * @throws JsonProcessingException Unable to serialize response
     */
    @InvokeMethod(type = MessageDataType.COMMAND_RECV_CHAT)
    public void run(final MessageHandler handler, final InvokeContext context) throws JsonProcessingException {
        GameState game = ((RecvChatInvokeContext) context).getGameState();
        BasicGameUserState user = ((RecvChatInvokeContext) context).getSender();
        String message = ((RecvChatInvokeContext) context).getMessage();

        for (GameUserState userState : game.getConnectedUsers().values()) {
            if (userState != user)
                userState.getClient().send(
                    OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(new BasicRecvChatCommandData(message, user.getUser().id()))
                    .build());
        }
    }

    /**
     * Sends a system chat message to all players.
     * @param handler Message handler
     * @param context Instance of {@link RecvSystemMessageInvokeContext}
     * @throws JsonProcessingException Unable to serialize response
     */
    @InvokeMethod(type = MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE)
    public void runSystem(final MessageHandler handler, final InvokeContext context) throws JsonProcessingException {
        GameState game = ((RecvSystemMessageInvokeContext) context).getGameState();
        String message = ((RecvSystemMessageInvokeContext) context).getMessage();

        for (GameUserState userState : game.getConnectedUsers().values()) {
            userState.getClient().send(
                OutgoingMessage.newBuilder()
                .setKey(handler)
                .success()
                .randomMessageId()
                .data(new BasicRecvSystemMessageCommandData(message))
                .build());
        }
    }

    /**
     * Context for the chat command
     */
    public static class RecvChatInvokeContext extends InvokeContext {
        /**
         * chat message
         */
        private String message;

        /**
         * person who sent message
         */
        private BasicGameUserState sender;

        /**
         * game state
         */
        private GameState gameState;

        /**
         * Constructs receive chat command
         * @param message chat message
         * @param sender message sender
         * @param gameState game state
         */
        public RecvChatInvokeContext(String message, BasicGameUserState sender, GameState gameState) {
            this.message = message;
            this.sender = sender;
            this.gameState = gameState;
        }

        /**
         * Gets the message
         * @return message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets the sender
         * @return sender
         */
        public BasicGameUserState getSender() {
            return sender;
        }

        /**
         * Gets the game state
         * @return game state
         */
        public GameState getGameState() {
            return gameState;
        }
    }

    /**
     * Context for the send system message command.
     */
    public static class RecvSystemMessageInvokeContext extends InvokeContext {
        /**
         * chat message
         */
        private String message;

        /**
         * game state
         */
        private GameState gameState;

        /**
         * Constructs receive system message command
         * @param message chat message
         * @param gameState game state
         */
        public RecvSystemMessageInvokeContext(String message, GameState gameState) {
            this.message = message;
            this.gameState = gameState;
        }

        /**
         * Gets the message
         * @return message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets the game state
         * @return game state
         */
        public GameState getGameState() {
            return gameState;
        }
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicSendChatCommand() { }
}