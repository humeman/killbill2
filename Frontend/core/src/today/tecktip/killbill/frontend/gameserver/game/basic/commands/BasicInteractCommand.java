package today.tecktip.killbill.frontend.gameserver.game.basic.commands;

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
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;
import today.tecktip.killbill.common.maploader.MapDirective.DirectiveType;

/**
 * Allows clients to interact with objects.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicInteractCommand {
    /**
     * Jackson thingy
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Incoming interact command data.
     */
    public static class BasicInteractCommandData extends MessageData {
        private final DirectiveType directiveType;
        private final int id;
        private final int action;

        /**
         * Constructs an interact command.
         */
        public BasicInteractCommandData(
            @JsonProperty(value = "directiveType", required = true) String directiveType,
            @JsonProperty(value = "id", required = true) int id,
            @JsonProperty(value = "action", required = true) int action
        ) {
            super(MessageDataType.COMMAND_INTERACT);

            this.directiveType = DirectiveType.valueOf(directiveType);
            this.id = id;
            this.action = action;
        }

        public BasicInteractCommandData(
            final MessageDataType typeOverride,
            DirectiveType directiveType,
            int id,
            int action
        ) {
            super(typeOverride);

            this.directiveType = directiveType;
            this.id = id;
            this.action = action;
        }

        /**
         * Gets the directive acted on.
         * @return Directive type
         */
        public DirectiveType getDirectiveType() {
            return directiveType;
        }
        
        /**
         * Gets the directive acted on as a JSON property.
         * @return Directive type as a string
         */
        @JsonProperty("directiveType")
        public String getDirectiveTypeJsonProperty() {
            return directiveType.toString();
        }

        /**
         * ID of the specified directive interacted on.
         * @return Map directive ID
         */
        @JsonProperty("id")
        public int getId() {
            return id;
        }

        /**
         * Internal action ID.
         * @return Action ID
         */
        @JsonProperty("action")
        public int getAction() {
            return action;
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.COMMAND_RECV_INTERACTION)
    public BasicInteractCommandData parse(final JsonNode node) {
        return MAPPER.convertValue(node, BasicInteractCommandData.class);
    }

    /**
     * Runs the Interact command.
     * @param handler Message handler which received the command
     * @param game Game state associated with this execution
     * @param user User state associated with this execution
     * @param message Message data
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @CommandMethod(type = MessageDataType.COMMAND_RECV_INTERACTION)
    public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure {
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
            throw new MessageFailure("JSON failure", e);
        }
    }

    /**
     * Runs the Receive Interaction command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_INTERACT)
    public void send(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        DirectiveType type = ((BasicInteractContext) context).getDirectiveType();
        int id = ((BasicInteractContext) context).getId();
        int action = ((BasicInteractContext) context).getAction();

        // Generate a message body for each connected user and send it out
        final BasicInteractCommandData data = new BasicInteractCommandData(
            MessageDataType.COMMAND_INTERACT,
            type,
            id,
            action
        );
        try {
            handler.send(
                null,
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .success()
                    .randomMessageId()
                    .data(data)
                    .build());
        }
        catch (final JsonProcessingException e) {
            throw new MessageFailure("JSON failure", e);
        }
    }

    /**
     * Context for the receive game state command invocation.
     */
    public static class BasicInteractContext extends InvokeContext {
        private final DirectiveType type;
        private final int id;
        private final int action;


        /**
         * Constructs context data for the receive interaction command.
         * @param gameState Game state to send state for
         */
        public BasicInteractContext(
            DirectiveType type,
            int id,
            int action
        ) {
            super();

            this.type = type;
            this.id = id;
            this.action = action;
        }

        /**
         * Gets the directive acted on.
         * @return Directive type
         */
        public DirectiveType getDirectiveType() {
            return type;
        }

        /**
         * ID of the specified directive interacted on.
         * @return Map directive ID
         */
        public int getId() {
            return id;
        }

        /**
         * Internal action ID.
         * @return Action ID
         */
        public int getAction() {
            return action;
        }
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicInteractCommand() { }
}