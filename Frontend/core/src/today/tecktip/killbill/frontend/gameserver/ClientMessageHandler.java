package today.tecktip.killbill.frontend.gameserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.InterruptedException;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.CommandLoader;
import today.tecktip.killbill.common.gameserver.MessageHandler;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.gameserver.game.LocalGameState;
import today.tecktip.killbill.frontend.gameserver.scheduled.HeartbeatSender;
import today.tecktip.killbill.frontend.http.requests.data.Game;

/**
 * Handles communication from the client to the UDP game server. 
 * 
 * @author cs
 */
public class ClientMessageHandler implements MessageHandler {
    /**
     * The size of the incoming and outgoing data buffers in bytes.
     */
    public static final int MAX_DATA_SIZE_BYTES = 10240; // 10KB

    /**
     * The charset the server will communicate with.
     */
    public static final Charset SERVER_CHARSET = Charset.forName("UTF-16");

    /**
     * The delay to use between each execution of the listen/send thread loops.
     */
    public static final int THREAD_MICROS_DELAY = 200;

    /**
     * The maximum size of the outgoing queue before messages are rejected.
     */
    public static final int MAX_QUEUE_SIZE = 500;

    /**
     * Jackson JSON serializer.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Last instantiated object of this type.
     */
    private static ClientMessageHandler handler = null;

    /**
     * UDP client channel
     */
    private DatagramChannel channel;

    /**
     * UDP channel listener thread
     */
    private Thread listenerThread;

    /**
     * UDP channel sender thread
     */
    private Thread senderThread;

    /**
     * UDP channel housekeeping thread
     */
    private Thread housekeepingThread;

    /**
     * Whether the client is connected.
     */
    private volatile boolean connected;

    /**
     * Whether the server has acknowledged our send requests, meaning the client is ready.
     */
    private boolean ready;

    /**
     * The game this handler is connected to.
     */
    private Game game;

    /**
     * Gets the game state to be used for all the datas.
     */
    private LocalGameState gameState;

    /**
     * Outgoing message queue.
     */
    private ConcurrentLinkedQueue<OutgoingMessage> sendQueue;

    /**
     * The address (host and port) to the UDP game server.
     */
    private InetSocketAddress serverAddress;

    /**
     * Handles exceptions while sending messages.
     */
    private SendErrorHandler sendErrorHandler;

    /**
     * Handles exceptions while receiving messages.
     */
    private ListenErrorHandler listenErrorHandler;

    /**
     * A map of outgoing responses, kept for the {@link #DEFAULT_OUTGOING_MESSAGE_VIABILITY} in case
     *  the server asks for them again.
     */
    private Map<UUID, OutgoingResponse> outgoingResponses;

    /**
     * A map of outgoing commands, kept for the {@link #DEFAULT_OUTGOING_MESSAGE_VIABILITY} in case
     *  we do not receive a response.
     */
    private Map<UUID, OutgoingCommand> outgoingCommands;

    /**
     * Command loader
     */
    private final CommandLoader commandLoader;

    /**
     * Tasks to be run by the housekeeping thread on a loop.
     */
    private final List<ScheduledTaskMethod> scheduledTasks;

    /**
     * Game server API key to use for authentication.
     */
    private final String key;

    /**
     * Last time a message was delivered to the server.
     */
    private Instant lastSend;

    /**
     * Last time a message was received from the server.
     */
    private Instant lastRecv;

    /**
     * The optional callbacks to run when data gets sent
     */
    private Map<MessageDataType, MessageCallbackMethod> callbacks;

    private AtomicBoolean threadLock;
    
    /**
     * Constructs a new ClientMessageHandler.
     * @param host Game server host
     * @param port Game server port
     * @param key Game server key
     * @param commandLoader Command loader (must be loaded at call)
     * @param sendErrorHandler Error handler method for send errors
     * @param listenErrorHandler Error handler method for receive errors
     */
    public ClientMessageHandler(
        final String host,
        final int port,
        final String key,
        final CommandLoader commandLoader,
        final SendErrorHandler sendErrorHandler,
        final ListenErrorHandler listenErrorHandler
    ) throws IOException {
        this.commandLoader = commandLoader;
        this.sendErrorHandler = sendErrorHandler;
        this.listenErrorHandler = listenErrorHandler;
        this.key = key;
        scheduledTasks = new ArrayList<>();
        serverAddress = new InetSocketAddress(host, port);
        connected = false;
        listenerThread = null;
        senderThread = null;
        lastRecv = null;
        ready = false;
        callbacks = new HashMap<>();
        threadLock = new AtomicBoolean(false);

        sendQueue = new ConcurrentLinkedQueue<>();
        outgoingResponses = new HashMap<>();
        outgoingCommands = new HashMap<>();

        scheduledTasks.add(new HeartbeatSender()::run);

        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(0));

        handler = this;
    }

    /**
     * Initiates a connection to the server and begins listening/processing the queue.
     * @param game Game to connect to
     * @throws IOException Unable to connect
     * @throws MessageFailure Failed to send connect message to server
     * @throws IllegalStateException Already connected
     */
    public void connect(final Game game) throws IOException, MessageFailure {
        if (connected) {
            throw new IllegalStateException("Already connected.");
        }
        this.game = game;
        connected = true;
        lastRecv = null;
        try {
            channel.connect(serverAddress);

            listenerThread = new Thread(this::listen);
            listenerThread.start();

            senderThread = new Thread(this::sendQueue);
            senderThread.start();

            housekeepingThread = new Thread(this::housekeeping);
            housekeepingThread.start();
        } catch (final Throwable t) {
            connected = false;
            throw t;
        }

        // And let's start up the client
        try {
            commandLoader.invokeMethodFor(game.config().getGameType(), MessageDataType.COMMAND_CONNECT).run(this, new InvokeContext());
        } catch (final Throwable t) {
            throw new MessageFailure("Failed to send connect command to server.", t);
        }
    }

    /**
     * Closes the active connection with the server and cleans up active threads.
     * @throws IllegalStateException Not connected
     * @throws IOException Unable to close channel
     * @throws InterruptedException Unable to join listener/sender threads
     */
    public void disconnect() throws IOException, InterruptedException {
        if (!connected) throw new IllegalStateException("Not connected.");

        connected = false;
        ready = false;

        try {
            commandLoader.invokeMethodFor(getGame().config().getGameType(), MessageDataType.COMMAND_DISCONNECT).run(this, new InvokeContext());
        } catch (final MessageFailure e) {
            Gdx.app.error(ClientMessageHandler.class.getSimpleName(), "Failure in sending disconnect to server. Will close channel anyway.", e);
        }

        channel.close();
    }

    /**
     * Notes that the client is ready for use. Don't run this -- execute the connect command instead
     *  or bad things will happen.
     * @param gameState Game state to use
     */
    public void ready(final LocalGameState gameState) {
        ready = true;
        this.gameState = gameState;
    }

    /**
     * Returns the ready state of this client. A client is ready once the server acknowledges the
     * connect command.
     * @return True if ready
     */
    public boolean isReady() {
        return ready;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Internal listener loop, designed to be run as a thread, which reads incoming messages.
     */
    private void listen() {
        ByteBuffer listenBuf = ByteBuffer.allocate(MAX_DATA_SIZE_BYTES);
        String content;
        while (connected) {
            try {
                if (channel.receive(listenBuf) != null) {
                    lastRecv = Instant.now();
                    listenBuf.flip();
                    // Clear off the first 4 bytes as the message length
                    int length = listenBuf.getInt();
                    content = new String(listenBuf.array(), 4, length, SERVER_CHARSET);

                    if (GlobalGameConfig.DEBUG) {
                        Gdx.app.log(ClientMessageHandler.class.getSimpleName(), "-> " + content);
                    }
                    recv(content);
                    listenBuf.clear();
                }
            } catch (final Throwable t) {
                listenErrorHandler.handle(t);
                listenBuf.clear();
            }
            
            try {
                TimeUnit.MICROSECONDS.sleep(THREAD_MICROS_DELAY);
            } catch (final Throwable t) { }
        }
    }

    /**
     * Internal queue processor loop, designed to be run as a thread, which sends outgoing messages.
     */
    private void sendQueue() {
        OutgoingMessage msg = null;
        while (connected) {
            msg = sendQueue.poll();

            if (msg == null) continue;

            try {
                sendImmediately(msg);
            } catch (final Throwable t) {
                sendErrorHandler.handle(t, msg);
            }
            
            try {
                TimeUnit.MICROSECONDS.sleep(THREAD_MICROS_DELAY);
            } catch (final Throwable t) {
                sendErrorHandler.handle(t, null);
            }
        }
    }

    /**
     * Thread loop which purges messages, schedules resends, ...
     */
    public void housekeeping() {
        int i = 0;
        List<Map.Entry<UUID, OutgoingCommand>> toResend = null;
        List<UUID> toPurge = null;
        Instant now;
        while (connected) {
            now = Instant.now();
            if (lastRecv != null && now.isAfter(lastRecv.plusSeconds(15))) {
                // timed out :(
                connected = false;
                Gdx.app.error(getClass().getSimpleName(), "UDP connection timed out.");
                break;
            }

            // LOCK THE LOCKS
            while (!threadLock.compareAndSet(false, true)) {
                try {
                    TimeUnit.MICROSECONDS.sleep(500);
                } catch (final Throwable t) {
                    sendErrorHandler.handle(t, null);
                }
            }
            try {
                // Check 
                if (i == 0) {
                    // Only every 500ms for this one
                    // Purge any sent responses (presumed server got them)
                    for (final Map.Entry<UUID, OutgoingResponse> entry : outgoingResponses.entrySet()) {
                        if (entry.getValue().ackDeadline().isBefore(now)) {
                            if (toPurge == null) toPurge = new ArrayList<>();
                            toPurge.add(entry.getKey());
                        }
                    }

                    if (toPurge != null) {
                        for (final UUID messageId : toPurge) {
                            outgoingResponses.remove(messageId);
                        }

                        toPurge = null;
                    }
                }

                // Every 50ms for this one
                // Resend any un-acked commands.
                for (final Map.Entry<UUID, OutgoingCommand> entry : outgoingCommands.entrySet()) {
                    if (entry.getValue().viabilityDeadline().isBefore(now)) {
                        if (toPurge == null) toPurge = new LinkedList<>();
                        toPurge.add(entry.getKey());
                        continue;
                    }

                    else if (entry.getValue().lastSent().plusMillis(entry.getValue().sendCount() * MessageHandler.UNACKED_RETRY_DELAY_INCREMENT_MS).isBefore(now)) {
                        if (toResend == null) toResend = new LinkedList<>();
                        toResend.add(entry);
                    }
                }
            } finally {
                threadLock.set(false);
            }

            // Resend any resends
            if (toResend != null) {
                for (final Map.Entry<UUID, OutgoingCommand> entry : toResend) {
                    send(entry.getValue().message());
                }
                toResend = null;
            }

            if (toPurge != null) {
                for (final UUID messageId : toPurge) {
                    outgoingCommands.remove(messageId);
                }
                toPurge = null;
            }

            // Run extra housekeeping tasks
            for (final ScheduledTaskMethod task : scheduledTasks) {
                try {
                    task.run(this);
                } catch (final Throwable t) {
                    throw new CatastrophicException("Failure in scheduled housekeeping task:", t);
                }
            }

            try {
                TimeUnit.MICROSECONDS.sleep(50 * THREAD_MICROS_DELAY);
            } catch (final Throwable t) {
                sendErrorHandler.handle(t, null);
            }

            i++;
            if (i == 10) i = 0;
        }
    }

    /**
     * Queues a message for delivery.
     * @param msg Message to be sent
     */
    public void send(final OutgoingMessage msg) {
        if (sendQueue.size() > MAX_QUEUE_SIZE) {
            throw new IllegalStateException("Too many queued messages!");
        }

        if (!ready) {
            throw new IllegalStateException("Client is not ready.");
        }

        sendQueue.add(msg);
    }

    /**
     * Sends a message, bypassing the queue.
     * @param msg Message to send
     */
    public void sendImmediately(final OutgoingMessage msg) throws JsonProcessingException, IOException, MessageFailure {
        // Obtain the lock
        while (!threadLock.compareAndSet(false, true)) {
            try {
                TimeUnit.MICROSECONDS.sleep(500);
            } catch (final Throwable t) {
                sendErrorHandler.handle(t, null);
            }
        }
        try {

            final Instant now = Instant.now();
            // If this message acks something, store it in the outgoing response queue
            if (msg.ackMessageId() != null) {
                if (!outgoingResponses.containsKey(msg.ackMessageId())) {
                    if (outgoingResponses.size() >= MAX_QUEUE_SIZE) {
                        throw new CatastrophicException("Outgoing responses exceeded max queue size!");
                    }
                    outgoingResponses.put(
                        msg.ackMessageId(),
                        new OutgoingResponse(
                            null,
                            msg,
                            now.plusMillis(DEFAULT_OUTGOING_MESSAGE_VIABILITY)
                        )
                    );
                }
            }
            else if (msg.messageId() != null && msg.viability() != null) {
                if (!outgoingCommands.containsKey(msg.messageId())) {
                    if (outgoingCommands.size() >= MAX_QUEUE_SIZE) {
                        throw new CatastrophicException("Outgoing commands exceeded max queue size!");
                    }

                    outgoingCommands.put(
                        msg.messageId(),
                        new OutgoingCommand(
                            null,
                            msg,
                            1,
                            now,
                            now.plusMillis(DEFAULT_OUTGOING_MESSAGE_VIABILITY)
                        )
                    );
                }
            }
            else {
                throw new IllegalArgumentException("Either a messageId or ackMessageId must be specified to send.");
            }
            
            String dataStr = MAPPER.writeValueAsString(msg);
            byte[] data = dataStr.getBytes(SERVER_CHARSET);
            // According to Spring docs, we have to prepend the data with a 4-byte message length value
            // to confirm the entire message was transmitted.
            final ByteBuffer sendBuf = ByteBuffer.allocate(4 + data.length);
            sendBuf.putInt(data.length);
            sendBuf.put(data);
            sendBuf.flip();

            int bytesSent = channel.send(sendBuf, serverAddress);
            if (GlobalGameConfig.DEBUG) {
                Gdx.app.log(ClientMessageHandler.class.getSimpleName(), "<- " + dataStr);
            }
            if (bytesSent != sendBuf.array().length) {
                throw new MessageFailure("Bytes-sent discrepancy: sent=" + bytesSent + ", exp=" + sendBuf.array().length);
            }

            lastSend = Instant.now();
        } finally {
            threadLock.set(false);
        }
    }

    @Override
    public void send(final UdpClient client, final OutgoingMessage message) throws JsonProcessingException {
        send(message);
    }

    @Override
    public void send(final UdpClient client, final OutgoingMessage message, final int maxRetries) throws JsonProcessingException {
        send(message);
    }

    /**
     * Handles incoming data.
     * @param content JSON body
     */
    private void recv(final String content) {
        // Obtain the lock
        while (!threadLock.compareAndSet(false, true)) {
            try {
                TimeUnit.MICROSECONDS.sleep(50);
            } catch (final Throwable t) {
                sendErrorHandler.handle(t, null);
            }
        }

        // Obtain the game lock
        if (gameState != null) {
            while (!gameState.acquireLock()) {
                try {
                    TimeUnit.MICROSECONDS.sleep(10);
                } catch (final Throwable t) {
                    sendErrorHandler.handle(t, null);
                }
            }
        }

        // Parse the message
        MessageCommandMethod method = null;
        final IncomingMessage msg;
        boolean success = false;
        try {
            try {
                final JsonNode node = MAPPER.readTree(content);

                msg = IncomingMessage.from(game.config().getGameType(), commandLoader, node);
            } catch (final Throwable t) {
                throw new IllegalArgumentException("Unable to parse message payload as JSON.", t);
            }

            // This is a command we're supposed to respond to
            if (msg.messageId() != null) {
                // Check if it's something we've already seen (meaning the response was dropped)
                final OutgoingResponse outgoingResp = outgoingResponses.get(msg.messageId());
                if (outgoingResp != null) {
                    // Send it instead
                    sendQueue.add(outgoingResp.message());
                    return;
                }

                // A new command. Run it normally
                method = commandLoader.commandMethodFor(game.config().getGameType(), msg.data().getType());
            } else if (msg.ackMessageId() != null) {
                // This is an acknowledgement.
                // Ack the command first
                if (outgoingCommands.remove(msg.ackMessageId()) == null) {
                    Gdx.app.error(ClientMessageHandler.class.getSimpleName(), "Server acked a message that doesn't exist: " + msg.ackMessageId() + "\n" + msg);
                    return;
                }

                // If it's empty, end here
                if (msg.data().getType().equals(MessageDataType.EMPTY)) {
                    return;
                }

                // Otherwise, we need to run the method for this type
                method = commandLoader.responseMethodFor(game.config().getGameType(), msg.data().getType());
            } else {
                // Both null is an implicit command
                method = commandLoader.commandMethodFor(game.config().getGameType(), msg.data().getType());
            }

            // If we don't have a method to call by now, this is an invalid type
            if (method == null) {
                throw new IllegalArgumentException("Invalid message received from server (no method for type): " + msg.data().getType());
            }
            success = true;
        } finally {
            try {
                threadLock.set(false);
            } catch (final Throwable t) {}
            if (!success && gameState != null) gameState.releaseLock();
        }

        try {
            // Call the method
            final ClientCommandContext context = new ClientCommandContext(game);
            try {
                method.run(this, msg, context);
            } catch (final Throwable t) {
                throw new RuntimeException("Exception while running command/response: \ndata=" + msg, t);
            }

            // Check if there's a callback
            final MessageCallbackMethod callbackMethod = callbacks.get(msg.data().getType());
            if (callbackMethod != null) {
                callbackMethod.run(msg);
            }
        } finally {
            if (gameState != null) gameState.releaseLock();
        }
    }
    

    @Override
    public CommandLoader getCommandLoader() {
        return commandLoader;
    }

    /**
     * Gets the game this client is running on.
     * @param game API game representation
     */
    public Game getGame() {
        return game;
    }

    @Override
    public String getKey() {
        return key;
    }

    /**
     * Gets the game state this client is running on.
     * @param game Local game state
     */
    public LocalGameState getGameState() {
        return gameState;
    }

    /**
     * Gets the time the last message was sent to the server.
     * @return Last send time (or null if not ready)
     */
    public Instant getLastSend() {
        if (!ready) return null;
        return lastSend;
    }

    /**
     * Defines a callback method when the client receives a data type. Called after the run command is processed.
     * @param type Data type to forward
     * @param method Method to call
     */
    public void setCallback(final MessageDataType type, final MessageCallbackMethod method) {
        callbacks.put(type, method);
    }

    /**
     * Clears all optional data callbacks.
     */
    public void clearCallbacks() {
        callbacks.clear();
    }

    /**
     * Clears out one callback.
     * @param type Type to clear
     */
    public void clearCallback(final MessageDataType type) {
        callbacks.remove(type);
    }

    /**
     * Gets a string with debugging info.
     * @return Debug string
     */
    public String getDebugString() {
        return String.format(
            "Queue: (s%d c%d r%d)/%d",
            sendQueue.size(),
            outgoingCommands.size(),
            outgoingResponses.size(),
            MAX_QUEUE_SIZE
        );
    }

    /**
     * Handles errors while listening.
     */
    public interface ListenErrorHandler {
        /**
        * Executed on a listen error.
        * @param t Exception that was thrown (can be null)
        */
        public void handle(final Throwable t);
    }

    /**
     * Handles errors while sending.
     */
    public interface SendErrorHandler {
        /**
         * Executed on a send error.
         * @param t Exception that was thrown (can be null)
         * @param msg OutgoingMessage which triggered the error (can be null)
         */
        public void handle(final Throwable t, final OutgoingMessage msg);
    }

    /**
     * A task that runs every 50ms.
     */
    public interface ScheduledTaskMethod {
        /**
         * Executed every 50ms by the housekeeping thread.
         * @param handler Message handler that triggered message
         */
        public void run(final ClientMessageHandler handler);
    }

    /**
     * A task that gets called as a data callback.
     */
    public interface MessageCallbackMethod {
        /**
         * Executed whenever the requested message type is delivered.
         * @param message Incoming message
         */
        public void run(final IncomingMessage message);
    }

    /**
     * Command context for command and response callbacks in the client.
     */
    public class ClientCommandContext extends CommandContext {
        /**
         * Connected game
         */
        private Game game;

        /**
         * Constructs a new client command context.
         * @param game The game this client is connected to
         */
        public ClientCommandContext(final Game game) {
            this.game = game;
        }

        /**
         * Gets the game this client is connected to.
         * @return Connected game
         */
        public Game getGame() {
            return game;
        }
    }



    /**
     * Gets the last instantiated instance of this class.
     * @return Client message handler
     */
    public static ClientMessageHandler get() {
        return handler;
    }
}
