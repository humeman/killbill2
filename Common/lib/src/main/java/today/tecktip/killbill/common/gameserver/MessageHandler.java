package today.tecktip.killbill.common.gameserver;


import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;

public interface MessageHandler {
    /**
     * Maximum number of times a failed outgoing message will be retried by the server
     */
    public static final int MAX_RETRIES = 3;

    /**
     * Delay increment between retries
     */
    public static final int RETRY_DELAY_INCREMENT_MS = 100;

    /**
     * Time after which outgoing messages will be discarded from the retry queue.
     */
    public static final int DEFAULT_OUTGOING_MESSAGE_VIABILITY = 4000;

    /**
     * Time increment between re-asks for un-acked outgoing messages with a messageId.
     */
    public static final int UNACKED_RETRY_DELAY_INCREMENT_MS = 500;

    /**
     * Sends a message to a UDP client. Prefer {@link UdpClient#send(OutgoingMessage)} where possible.
     * <p>
     * <strong>Retries will occur up to {@link #MAX_RETRIES} times. If this is not wanted, use {@link #send(UdpClient, OutgoingMessage, int)} with a retryCount of 0.</strong>
     * @param client Client to send message to
     * @param message Message to send
     * @throws JsonProcessingException Unable to serialize as JSON
     */
    public void send(final UdpClient client, final OutgoingMessage message) throws JsonProcessingException;

    /**
     * Sends a message to a UDP client with an overridden number of retries. Prefer {@link UdpClient#send(OutgoingMessage)} where possible.
     * <p>
     * <strong>Use the default {@link #MAX_RETRIES} by calling {@link #send(UdpClient, OutgoingMessage)} unless absolutely necessary.</strong>
     * @param client Client to send message to
     * @param message Message to send
     * @param maxRetries Max number of times the send is retried in case of exceptions
     * @throws JsonProcessingException Unable to serialize as JSON
     * @throws MessageHandlingException Send failed (only if maxRetries is 0)
     */
    public void send(final UdpClient client, final OutgoingMessage message, final int maxRetries) throws JsonProcessingException;

    /**
     * Gets the command loader associated with this message handler.
     * @return Pre-loaded command loader
     */
    public CommandLoader getCommandLoader();

    /**
     * Gets the key used to authenticate with the gameserver (or clients).
     * @return Game key
     */
    public String getKey();

    /**
     * Represents the IP, port, and authentication details for a UDP client.
     * @param ip Client's IP address
     * @param port Client's port
     * @param handler Message handler creating the client
     * @param respQueue Response message queue
     * @param cmdQueue Command queue
     */
    public record UdpClient(String ip, int port, MessageHandler handler, HashMap<UUID, OutgoingResponse> respQueue, HashMap<UUID, OutgoingCommand> cmdQueue) {
        /**
         * Sends a message to this client. Retries up to {@link #MAX_RETRIES} times.
         * @param message Outgoing message to send
         * @throws JsonProcessingException Failed to serialize as JSON
         */
        public void send(final OutgoingMessage message) throws JsonProcessingException {
            handler.send(this, message);
        }

        /**
         * Sends a message to this client with an overridden maximum number of retries.
         * <p>
         * <strong>Use the default {@link #MAX_RETRIES} by calling {@link #send(OutgoingMessage)} unless absolutely necessary.</strong>
         * @param message Outgoing message to send
         * @param maxRetries Maximum number of times to retry
         * @throws JsonProcessingException Failed to serialize as JSON
         * @throws MessageHandlingException Send failed (only if maxRetries is 0)
         */
        public void send(final OutgoingMessage message, final int maxRetries) throws JsonProcessingException {
            handler.send(this, message);
        }
    }

    /**
     * Basic interface representing commands that can be called by the command handler.
     */
    public static interface MessageCommandMethod {
        /**
         * Executes a command.
         * @param handler Message handler instance for sending back messages
         * @param message Message payload
         * @param context Context associated with the call
         * @throws Exception Any unhandled or unexpected exceptions that will be caught by the message handler
         */
        public void run(final MessageHandler handler, final IncomingMessage message, final CommandContext context) throws MessageFailure;
    }
    

    /**
     * Basic interface representing commands that can be invoked (sent to the other end) by the command handler.
     */
    public interface InvokeCommandMethod {
        /**
         * Invokes a command.
         * @param handler Message handler instance for sending messages
         * @param context Context associated with the call
         * @throws Exception Any unhandled or unexpected exceptions that will be caught by the message handler
         */
        public void run(final MessageHandler handler, final InvokeContext context) throws MessageFailure;
    }

    /**
     * Stores an outgoing response-type message, along with its UDP client, so that it can be re-sent in case it was dropped.
     * @param client UDP client the packet was sent to
     * @param message The outoing message
     * @param ackDeadline After this time, the message will be auto-cleared
     */
    public record OutgoingResponse(UdpClient client, OutgoingMessage message, Instant ackDeadline) {
        public OutgoingResponse {
            Objects.requireNonNull(message, "'message' cannot be null");
            Objects.requireNonNull(ackDeadline, "'ackDeadline' cannot be null");
        }
    }

    /**
     * Stores an outgoing command-type message, along with its UDP client, so that it can be re-sent in case the client does not ack it in time.
     * @param client UDP client the packet was sent to
     * @param message The outoing message
     * @param sends The number of times the message has been sent so far
     * @param lastSent The last time the message was sent
     * @param viabilityDeadline After this time with no response, the message will be auto-cleared
     */
    public record OutgoingCommand(UdpClient client, OutgoingMessage message, int sendCount, Instant lastSent, Instant viabilityDeadline) {
        public OutgoingCommand {
            Objects.requireNonNull(message, "'message' cannot be null");
            Objects.requireNonNull(lastSent, "'lastSent' cannot be null");
            Objects.requireNonNull(viabilityDeadline, "'viabilityDeadline' cannot be null");
        }
    }

    /**
     * Contains extra data (like game or user states) to be sent along to command callbacks.
     * Empty by default, designed to be extended with any necessary data.
     */
    public class CommandContext { }

    /**
     * Contains extra data (like game or user states) to be sent along to invoke callbacks.
     * Empty by default, designed to be extended with any necessary data.
     */
    public class InvokeContext { }
}
