package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;

/**
 * Sends chat over the game server
 * @author Caleb Zea
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicSendChatCommand {
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        @JsonProperty("message")
        public String getMessage() {
            return message;
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
         * Sender ID
         */
        private final UUID userId;

        /**
         * Constructs send chat command data
         * @param message chat message
         */
        public BasicRecvChatCommandData(
            @JsonProperty("message") final String message,
            @JsonProperty("userId") final String userId
        ) {
            super(MessageDataType.COMMAND_RECV_CHAT);
            this.message = message;
            this.userId = UUID.fromString(userId);
        }

        /**
         * Gets the chat message
         * @return message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets the sender's ID.
         * @return Sender UUID
         */
        public UUID getUserId() {
            return userId;
        }
    }

    /**
     * Incoming system message command data.
     */
    public static class BasicRecvSystemMessageCommandData extends MessageData {
        /**
         * chat message
         */
        private final String message;

        /**
         * Constructs send chat command data
         * @param message chat message
         */
        public BasicRecvSystemMessageCommandData(
            @JsonProperty("message") final String message
        ) {
            super(MessageDataType.COMMAND_RECV_CHAT);
            this.message = message;
        }

        /**
         * Gets the chat message
         * @return message
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_RECV_CHAT)
    public BasicRecvChatCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicRecvChatCommandData.class);
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE)
    public BasicRecvSystemMessageCommandData parseSystem(final JsonNode node) {
        return MAPPER.convertValue(node, BasicRecvSystemMessageCommandData.class);
    }

    /**
     * Receives a chat message.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_CHAT)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .ackMessageId(message.messageId())
                    .setKey(handler)
                    .success()
                    .data(new EmptyData())
                    .build()
            );
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON error", e);
        }
    }

    /**
     * Receives a system message.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE)
    public void runSystem(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .ackMessageId(message.messageId())
                    .setKey(handler)
                    .success()
                    .data(new EmptyData())
                    .build()
            );
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON error", e);
        }
    }

    /**
     * Sends a chat message.
     * @param handler Message handler
     * @param context Instance of {@link SendChatInvokeContext}
     * @throws MessageFailure Failed to send
     */
    @InvokeMethod(type = MessageDataType.COMMAND_SEND_CHAT)
    public void run(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        String message = ((SendChatInvokeContext) context).getMessage();

        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .randomMessageId()
                    .setKey(handler)
                    .success()
                    .data(new BasicSendChatCommandData(message))
                    .build()
            );
        } catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON error", e);
        }
    }

    /**
     * Context for the chat command
     */
    public static class SendChatInvokeContext extends InvokeContext {
        /**
         * chat message
         */
        private String message;


        /**
         * Constructs receive chat command
         * @param message chat message
         */
        public SendChatInvokeContext(String message) {
            this.message = message;
        }

        /**
         * Gets the message
         * @return message
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicSendChatCommand() { }
}