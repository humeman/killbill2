package today.tecktip.killbill.backend.gameserver.games.basic.commands;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameState;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
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
import today.tecktip.killbill.common.gameserver.messages.exceptions.IllegalStateExceptionData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InternalServerErrorData;
import today.tecktip.killbill.common.gameserver.messages.generic.EmptyData;
import today.tecktip.killbill.common.maploader.MapDirective.DirectiveType;

/**
 * Allows clients to interact with objects.
 * @author cs
 */
@Command(gameTypes = {GameType.BASIC})
public class BasicInteractCommand {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicInteractCommand.class);
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
        @JsonCreator
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
    @ParseMethod(type = MessageDataType.COMMAND_INTERACT)
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
    @CommandMethod(type = MessageDataType.COMMAND_INTERACT)
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

        final BasicInteractCommandData data = (BasicInteractCommandData) message.data();
                          
        // Send this interaction out
        try {
            handler.getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_INTERACTION).run(
                handler,
                new RecvInteractionContext(game, data.getDirectiveType(), data.getId(), data.getAction(), user.getUser().id())
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

        // Store it for rejoins
        game.addInteraction(data);
    }

    /**
     * Runs the Receive Interaction command.
     * @throws JsonProcessingException Unable to serialize response as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_RECV_INTERACTION)
    public void send(final MessageHandler handler, final InvokeContext context) throws JsonProcessingException {
        BasicGameState gameState = (BasicGameState) ((RecvInteractionContext) context).getGameState();
        DirectiveType type = ((RecvInteractionContext) context).getDirectiveType();
        int id = ((RecvInteractionContext) context).getId();
        int action = ((RecvInteractionContext) context).getAction();
        UUID sender = ((RecvInteractionContext) context).getSender();

        // Generate a message body for each connected user and send it out
        final BasicInteractCommandData data = new BasicInteractCommandData(
            MessageDataType.COMMAND_RECV_INTERACTION,
            type,
            id,
            action
        );
        for (GameUserState userState : gameState.getConnectedUsers().values()) {
            if (userState.getUser().id().equals(sender)) continue;

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
    public static class RecvInteractionContext extends InvokeContext {
        /**
         * Game state for this invocation
         */
        private GameState gameState;
        private final DirectiveType type;
        private final int id;
        private final int action;
        private final UUID sender;
        private final List<UUID> sendTo;

        /**
         * Constructs context data for the receive interaction command.
         * @param gameState Game state to send state for
         */
        public RecvInteractionContext(
            final GameState gameState,
            DirectiveType type,
            int id,
            int action,
            UUID sender
        ) {
            super();

            this.gameState = gameState;
            this.type = type;
            this.id = id;
            this.action = action;
            this.sender = sender;
            sendTo = null;
        }

        public RecvInteractionContext(
            final GameState gameState,
            final BasicInteractCommandData data,
            final List<UUID> sendTo
        ) {
            super();

            this.gameState = gameState;
            this.type = data.getDirectiveType();
            this.id = data.getId();
            this.action = data.getAction();
            this.sendTo = sendTo;
            sender = null;
        }

        /**
         * Gets the game state for this invocation.
         * @return Game state to send state for
         */
        public GameState getGameState() {
            return gameState;
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

        /**
         * Gets the sender, if applicable.
         * @return Sender or null
         */
        public UUID getSender() {
            return sender;
        }

        /**
         * Gets the list of people to send to, if applicable.
         * @return Receivers or null
         */
        public List<UUID> getSendTo() {
            return sendTo;
        }
    }

    /**
     * This class should not be manually instantiated.
     */
    public BasicInteractCommand() { }
}