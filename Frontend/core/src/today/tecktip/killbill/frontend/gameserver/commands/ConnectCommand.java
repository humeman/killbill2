package today.tecktip.killbill.frontend.gameserver.commands;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.MessageHandler.CommandContext;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.annotations.ResponseMethod;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;
import today.tecktip.killbill.frontend.gameserver.game.LocalGameState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState;
import today.tecktip.killbill.frontend.http.requests.data.Game;

/**
 * Initiates a connection with the server.
 * @author cs
 */
@Command
public class ConnectCommand {

    /**
     * Outgoing connect command data. Empty at this time.
     */
    @JsonSerialize
    public class ConnectCommandData extends MessageData {
        /**
         * Constructs data for the connect command.
         */
        public ConnectCommandData() {
            super(MessageDataType.COMMAND_CONNECT);
        }
    }

    /**
     * Incoming connect response data.
     */
    public static class ConnectResponseData extends MessageData {
        /**
         * Constructs a new incoming connect response message.
         */
        public ConnectResponseData() {
            super(MessageDataType.RESP_CONNECT);
        }

        /**
         * Parses a JSON node into connect response data.
         * @param node JSON node
         * @return Parsed data
         */
        public static ConnectResponseData parse(final JsonNode node) {
            return new ConnectResponseData();
        }
    }

    /**
     * Parse method shortcut for this command's incoming data.
     * @param node JSON data
     * @return Parsed incoming data
     */
    @ParseMethod(type = MessageDataType.RESP_CONNECT)
    public ConnectResponseData parse(final JsonNode node) {
        return new ConnectResponseData();
    }

    /**
     * Tells the server that this client intends to connect.
     * @param handler Message handler
     * @param context Context, ignored
     * @throws JsonProcessingException Unable to serialize request as JSON
     */
    @InvokeMethod(type = MessageDataType.COMMAND_CONNECT)
    public void connect(final MessageHandler handler, final InvokeContext context) throws MessageFailure {
        // Bypasses ready state
        try {
            ((ClientMessageHandler) handler).sendImmediately(
                OutgoingMessage.newBuilder()
                    .setKey(handler)
                    .randomMessageId()
                    .data(new ConnectCommandData())
                    .build()
            );
        } catch (IOException e) {
            throw new MessageFailure("Unable to send message immediately: ", e);
        }
    }

    /**
     * Handles the connect response sent by the server.
     * @param handler Message handler
     * @param message Message
     * @param context Context
     */
    @ResponseMethod(type = MessageDataType.RESP_CONNECT)
    public void handleResponse(final MessageHandler handler, final IncomingMessage message, final CommandContext context) {
        // Mark the client as ready
        Gdx.app.log(ConnectCommand.class.getSimpleName(), "Ready response received from server!");

        // Build up our game state
        final Game game = ((ClientMessageHandler) handler).getGame();
        final LocalGameState gameState;
        switch (game.config().getGameType()) {
            case BASIC:
                gameState = new BasicLocalGameState(game);
                break;
            default:
                throw new CatastrophicException("Game state " + game.config().getGameType() + " doesn't have an associated state!");
        }

        ((ClientMessageHandler) handler).ready(gameState);
    }
}
