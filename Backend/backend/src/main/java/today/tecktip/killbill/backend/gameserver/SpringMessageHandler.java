package today.tecktip.killbill.backend.gameserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.backend.auth.GameServerAuthenticator;
import today.tecktip.killbill.backend.exceptions.AuthenticationFailure;
import today.tecktip.killbill.backend.gameserver.games.GameState;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.OutgoingMessage;
import today.tecktip.killbill.common.gameserver.messages.exceptions.AuthenticationFailureData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InternalServerErrorData;
import today.tecktip.killbill.common.gameserver.messages.exceptions.InvalidArgumentExceptionData;
import today.tecktip.killbill.common.gameserver.ClasspathCommandLoader;
import today.tecktip.killbill.common.gameserver.CommandLoader;
import today.tecktip.killbill.common.gameserver.MessageHandler;

/**
 * Handles incoming and outgoing UDP messages.
 * @author cs
 */
@MessageEndpoint
@Component("messageHandler")
public class SpringMessageHandler implements MessageHandler {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringMessageHandler.class);

    /**
     * The charset the server will communicate with.
     */
    public static final Charset SERVER_CHARSET = Charset.forName("UTF-16");

    /**
     * Maximum number of times a failed outgoing message will be retried by the server
     */
    private static final int MAX_RETRIES = 3;

    /**
     * Delay increment between retries
     */
    private static final int RETRY_DELAY_INCREMENT_MS = 100;

    /**
     * Time after which outgoing messages will be discarded from the retry queue.
     */
    public static final int DEFAULT_OUTGOING_MESSAGE_VIABILITY = 4000;

    /**
     * Time increment between re-asks for un-acked outgoing messages with a messageId.
     */
    private static final int UNACKED_RETRY_DELAY_INCREMENT_MS = 250;

    /**
     * Milliseconds between game ticks.
     */
    private static final int GAME_TICK_MS = 1000 / 20;
    
    /**
     * Object mapper for JSON serialization and deserialization
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Static reference to the currently active instance.
     */
    private static SpringMessageHandler ACTIVE_INSTANCE = null;

    /**
     * UDP out channel. Use {@link #send} instead.
     */
    private UnicastSendingMessageHandler udpOut;

    /**
     * Connected clients.
     */
    private Map<String, UdpClient> clients;

    /**
     * Games to be removed in the future.
     */
    private Map<GameState, Instant> deadGames;

    /**
     * Classpath command searcher.
     */
    private CommandLoader commandLoader;

    /**
     * Time the last game tick was run.
     */
    private Instant lastTick;

    /**
     * Port to run UDP game server on.
     */
    private static int UDP_PORT;

    /**
     * Used by Spring Boot to store the UDP port env var.
     * @param udpPort UDP_PORT env var
     */
    @Value("${env_vars.udp_port}")
    private void setUdpPort(final String udpPort) {
        UDP_PORT = Integer.valueOf(udpPort);
    }

    /**
     * Secret key for the server to send to clients in the 'key' field.
     * <p>
     * Has little effect at the moment when communication is unencrypted,
     *  but could help to prevent IP spoofing in the future. Will need to
     *  be randomized based on clients then, though.
     */
    @Value("${env_vars.server_key}")
    private String serverKey;

    /**
     * Scheduler to run retries in the future.
     */
    @Autowired
    private TaskScheduler taskScheduler;

    /**
     * Retrieves the inbound MessageChannel.
     * @return Inbound channel
     */
    @Bean
    public MessageChannel inboundChannel() {
        return new DirectChannel();
    }
    
    /**
     * Configures the incoming UDP message channel.
     * @return UDP channel adapter
     */
    @Bean
    public UnicastReceivingChannelAdapter udpIn() {
        UnicastReceivingChannelAdapter adapter = new UnicastReceivingChannelAdapter(UDP_PORT);
        adapter.setOutputChannel(inboundChannel());        
        adapter.setOutputChannelName("inboundChannel");
        adapter.setLengthCheck(true);
        adapter.setReceiveBufferSize(5000);
        return adapter;
    }

    /**
     * Configures the outgoing UDP message channel.
     * @return UDP channel adapter
     */
    @Bean
    public UnicastSendingMessageHandler udpOut() {
        UnicastSendingMessageHandler adapter = new UnicastSendingMessageHandler("headers['ip_packetAddress']");
        adapter.setSocketExpression(new SpelExpressionParser().parseExpression("@udpIn.socket"));
        adapter.setLengthCheck(true);
        this.udpOut = adapter;
        return adapter;
    }

    /**
     * Receives incoming messages from the UDP channel.
     * @param message Message headers and payload
     */
    @ServiceActivator(inputChannel = "inboundChannel")
    public void recv(final Message<byte[]> message) {
        final UdpClient client = getClient(message, this);
        
        try {
            doRecv(client, message);
        } catch (final MessageHandlingException e) {
            // Nothing we can do here. The command has to have asked for no retries for this to occur.
            // Just fail silently -- the client will send a RESEND request if it's important.
        } catch (final Exception e) {
            LOGGER.error("Unexpected error during UDP processing: ", e);
            try {
                client.send(
                    OutgoingMessage.newBuilder()
                        .setKey(this)
                        .failure()
                        .data(new InternalServerErrorData("Unexpected server error during processing."))
                        .build());
            } catch (final JsonProcessingException e1) {
                LOGGER.error("JSON processing error while sending back exception error message: ", e1);
            } catch (final MessageHandlingException e1) {
                LOGGER.error("Unable to send error response to client: ", e1);
            }
        }
    }

    /**
     * Receives a message. Wrapped for error handling purposes.
     * <p>
     * This method executes commands based on the {@link GameType} the user is linked to.
     * 
     * @param client Client to send responses to
     * @param message Message headers and payloads
     * @throws JsonProcessingException Unable to deserialize response
     */
    private void doRecv(final UdpClient client, final Message<byte[]> message) throws JsonProcessingException, MessageHandlingException {
        // We're parsing the message on our own to improve validation here, which is why
        // we ask for a String body.
        // Parse JSON
        final JsonNode node;
        try {
            node = MAPPER.readTree(new String(message.getPayload(), SERVER_CHARSET));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to parse message payload as JSON.");
        }

        GameUserState user;
        try {
            user = GameServerAuthenticator.requireAuthentication(node.get("key").textValue(), client);
        } catch (final AuthenticationFailure e) {
            LOGGER.warn("Failed to authenticate user: ", e);
            client.send(
                OutgoingMessage.newBuilder()
                    .setKey(this)
                    .failure()
                    .data(new AuthenticationFailureData(e.getReason()))
                    .build());
            return;
        } catch (final SQLException e) {
            client.send(
                OutgoingMessage.newBuilder()
                    .setKey(this)   
                    .failure()
                    .data(new InternalServerErrorData("Unable to contact database."))
                    .build());
            return;
        }
        
        // Reset heartbeat timer since we just got a message
        user.updateHeartbeat();

        // Find the relevant parsers for our game type
        GameState gameState = GameState.get(user.getGameUser().gameId());
        final IncomingMessage msg;
        try {
            msg = IncomingMessage.from(gameState.getGame().config().getGameType(), commandLoader, node);
        } catch (final IllegalArgumentException e) {
            client.send(
                OutgoingMessage.newBuilder()
                    .setKey(this)
                    .failure()
                    .data(new InvalidArgumentExceptionData(e.getMessage()))
                    .build());
            return;
        }

        // Check if this is a duplicate message (meaning our response was dropped)
        final OutgoingResponse qMsg = client.respQueue().get(msg.messageId());
        if (qMsg != null) {
            // Send that message again
            client.send(
                qMsg.message()
            );
            return;
        }

        // Check if this is an empty ack
        if (MessageDataType.EMPTY.equals(msg.data().getType())) {
            ackIfAuthorized(msg.ackMessageId(), client);
            return;
        }
        
        // Check if this is a command or a response that can be executed. If so, get the corresponding method
        MessageCommandMethod commandMethod = commandLoader.commandMethodFor(gameState.getGame().config().getGameType(), msg.data().getType());
        MessageCommandMethod respMethod = commandLoader.commandMethodFor(gameState.getGame().config().getGameType(), msg.data().getType());

        MessageCommandMethod m;
        // Check if this is a command
        if (commandMethod != null && msg.messageId() != null) {
            // Make sure the messageId is not taken (crazy edge case)
            if (client.respQueue().containsKey(msg.messageId())) {
                client.send(
                    OutgoingMessage.newBuilder()
                        .setKey(this)
                        .failure()
                        .data(new InvalidArgumentExceptionData("The specified 'messageId' is taken."))
                        .build());
                return;
            }

            m = commandMethod;
        } else if (respMethod != null && msg.ackMessageId() != null) {
            // Make sure any ackMessageIds correspond to a message
            if (!client.cmdQueue().containsKey(msg.ackMessageId())) {
                client.send(
                    OutgoingMessage.newBuilder()
                        .setKey(this)
                        .failure()
                        .data(new InvalidArgumentExceptionData("The specified 'ackMessageId' does not correspond to any existing outgoing message."))
                        .build());
                return;
            }

            m = respMethod;
        } else {
            client.send(
                OutgoingMessage.newBuilder()
                    .setKey(this)
                    .failure()
                    .ackMessageId(msg.messageId())
                    .messageId(msg.ackMessageId())
                    .data(new InvalidArgumentExceptionData("Invalid " + (msg.messageId() == null ? "response" : "command") + " type: " + msg.data().getType()))
                    .build());
            return;
        }
        
        final SpringCommandContext context = new SpringCommandContext(user, gameState);
        try {
            m.run(this, msg, context);
        } catch (final IllegalArgumentException e) {
            client.send(OutgoingMessage.newBuilder()
                .setKey(this)
                .failure()
                .ackMessageId(msg.messageId())
                .messageId(msg.ackMessageId())
                .data(new InvalidArgumentExceptionData(e.getMessage()))
                .build());
        } catch (final Throwable e) {
            LOGGER.error("Exception while running command/response: \ndata={}\nfrom={}:{}", msg, client.ip(), client.port(), e);
            client.send(OutgoingMessage.newBuilder()
                .setKey(this)
                .failure()
                .ackMessageId(msg.messageId())
                .messageId(msg.ackMessageId())
                .data(new InvalidArgumentExceptionData("Internal server error while processing."))
                .build());
            return;
        }
    }

    @Override
    public void send(final UdpClient client, final OutgoingMessage message) throws JsonProcessingException {
        send(client, message, MAX_RETRIES);
    }

    @Override
    public void send(final UdpClient client, final OutgoingMessage message, final int maxRetries) throws JsonProcessingException {
        Message<byte[]> msg = MessageBuilder
            .withPayload(MAPPER.writeValueAsString(message).getBytes(SERVER_CHARSET))
            .setHeader(IpHeaders.PACKET_ADDRESS, new InetSocketAddress(client.ip(), client.port()))
            .build();
        
        // Store in queue if this is acking something
        if (message.ackMessageId() != null) {
            client.respQueue().put(
                message.ackMessageId(),
                new OutgoingResponse(client, message, Instant.now().plusMillis(DEFAULT_OUTGOING_MESSAGE_VIABILITY))
            );
        }

        // And if this requires ack, store the ID in another queue so we can request resend
        if (message.messageId() != null && message.viability() != null) {
            client.cmdQueue().put(
                message.messageId(),
                new OutgoingCommand(client, message, 1, Instant.now(), Instant.now().plusMillis(DEFAULT_OUTGOING_MESSAGE_VIABILITY))
            );
        }

        try {
            LOGGER.debug("Sending: " + new String(msg.getPayload(), SERVER_CHARSET));
            udpOut.handleMessage(msg);
        } catch (final MessageHandlingException e) {
            // Message failed. Retry the send a few times.
            LOGGER.error("Failure in UDP delivery. Will retry: ", e);
            if (maxRetries > 0)
                taskScheduler.schedule(
                    () -> {
                        retrySend(1, maxRetries, msg);
                    },
                    Instant.now().plusMillis(RETRY_DELAY_INCREMENT_MS)
                );
            else throw e;
        }
    }

    /**
     * Retries a message send when a {@link MessagingException} occurred.
     * @param i Number of retries so far
     * @param maxRetries Max times to retry
     * @param message Message to try resend for
     */
    private void retrySend(final int i, int maxRetries, final Message<byte[]> message) {
        // Try to send again
        try {
            udpOut.handleMessage(message);
        } catch (final MessageHandlingException e) {
            // Check if we should resend again
            final int newI = i + 1;
            if (newI > maxRetries) {
                LOGGER.warn("Failing retry for message: {} after {} attempts", message, i);
                return; // Too many retries
            }

            taskScheduler.schedule(
                () -> {
                    retrySend(newI, maxRetries, message);
                },
                Instant.now().plusMillis(RETRY_DELAY_INCREMENT_MS * newI)
            );
        }
    }

    /**
     * Acknowledges (removes from the queue) a message that was sent only if it was addressed to the specified client.
     * @param messageId Message ID
     * @param client UDP client requesting ack
     */
    public void ackIfAuthorized(final UUID messageId, final UdpClient client) {
        client.cmdQueue().remove(messageId);
    }

    /**
     * Re-sends a message that was sent only if it was addressed to the specified client.
     * @param messageId Message ID
     * @param client UDP client requesting resend
     * @throws JsonProcessingException Unable to serialize as JSON
     * @throws IllegalArgumentException Not found or not addressed to this client
     */
    public void resendOutgoingResponseIfAuthorized(final UUID messageId, final UdpClient client) throws JsonProcessingException {
        OutgoingResponse msg = client.respQueue().get(messageId);

        if (msg == null) throw new IllegalArgumentException("No such message.");

        // Clear from queue
        client.respQueue().remove(messageId);

        // Resend
        client.send(msg.message());
    }

    /**
     * Re-sends an outgoing command to a client that was sent only if it was addressed to the specified client.
     * @param messageId Message ID
     * @param client UDP client requesting resend
     * @throws JsonProcessingException Unable to serialize as JSON
     * @throws IllegalArgumentException Not found or not addressed to this client
     */
    public void resendOutgoingCommandIfAuthorized(final UUID messageId, final UdpClient client) throws JsonProcessingException {
        OutgoingCommand msg = client.cmdQueue().get(messageId);

        if (msg == null) throw new IllegalArgumentException("No such message.");

        // Clear from queue
        client.cmdQueue().remove(messageId);

        // Resend
        client.send(msg.message());
    }

    /**
     * A scheduled task to purge any unacknowledged messages (messages that are past their ackDeadline).
     * <p>
     * Runs every 500ms.
     */
    @Scheduled(fixedRate = 500)
	public void purgeUnackedMessages() {
        final Instant now = Instant.now();

        ArrayList<UUID> toPurge;
        
        for (final UdpClient client : clients.values()) {
            toPurge = null;
            for (final Map.Entry<UUID, OutgoingResponse> entry : client.respQueue().entrySet()) {
                if (entry.getValue().ackDeadline().isBefore(now)) {
                    if (toPurge == null) toPurge = new ArrayList<>();
                    toPurge.add(entry.getKey());
                }
            }

            if (toPurge != null) {
                for (final UUID messageId : toPurge) {
                    client.respQueue().remove(messageId);
                }
            }
        }
	}

    /**
     * A scheduled task to ask for any un-acked messages.
     * Runs every 50ms.
     */
    @Scheduled(fixedRate = 50)
	public void resendUnackedMessages() {
        final Instant now = Instant.now();
        ArrayList<Map.Entry<UUID, OutgoingCommand>> toResend;
        ArrayList<UUID> toPurge;
        for (final UdpClient client : clients.values()) {
            toPurge = null;
            toResend = null;
            for (final Map.Entry<UUID, OutgoingCommand> entry : client.cmdQueue().entrySet()) {
                if (entry.getValue().viabilityDeadline().isBefore(now)) {
                    if (toPurge == null) toPurge = new ArrayList<>();
                    toPurge.add(entry.getKey());
                    continue;
                }

                if (entry.getValue().lastSent().isBefore(now.plusMillis(entry.getValue().sendCount() * UNACKED_RETRY_DELAY_INCREMENT_MS))) {
                    if (toResend == null) toResend = new ArrayList<>();
                    toResend.add(entry);
                }
            }

            if (toResend != null) {
                for (final Map.Entry<UUID, OutgoingCommand> entry : toResend) {
                    // Send out the request again
                    try {
                        entry.getValue().client().send(
                            entry.getValue().message()
                        );

                        // Update last sent time
                        client.cmdQueue().put(
                            entry.getKey(),
                            new OutgoingCommand(entry.getValue().client(), entry.getValue().message(), entry.getValue().sendCount() + 1, Instant.now(), entry.getValue().viabilityDeadline())
                        );
                    } catch (JsonProcessingException e) {
                        LOGGER.warn("Skipping resend for unacked message {} due to JSON error: ", entry.getKey(), e);
                        // Drop the message
                        if (toPurge == null)  toPurge = new ArrayList<>();
                        toPurge.add(entry.getKey());
                    }
                }
            }

            if (toPurge != null) {
                for (final UUID messageId : toPurge) {
                    client.cmdQueue().remove(messageId);
                }
            }
        }
	}

    /**
     * A scheduled task to disconnect any 'dead' clients.
     * <p>
     * Runs every second.
     */
    @Scheduled(fixedRate = 1000)
	public void disconnectDeadClients() {
        GameState.forEach(
            gameState -> {
                gameState.disconnectTimedOutClients();
            }
        );
	}

    @Scheduled(fixedRate = GAME_TICK_MS)
    public void runGameTick() {
        final float delta;
        if (lastTick == null) {
            delta = 0;
        } else {
            delta = (Instant.now().toEpochMilli() - lastTick.toEpochMilli()) / 1000f;
        }
        lastTick = Instant.now();

        try {
            GameState.forEach(
                gameState -> {
                    gameState.runGameTick(delta);
                }
            );
        } catch (final Throwable t) {
            System.err.println(t);
            t.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 5000)
	public void removeDeadGames() {
        List<GameState> toRemove = null;
        for (final Map.Entry<GameState, Instant> kv : deadGames.entrySet()) {
            if (kv.getValue().isBefore(Instant.now())) {
                LOGGER.info("Clearing out dead game with id=" + kv.getKey().getGame().id() + ".");
                GameState.destroy(kv.getKey());

                if (toRemove == null) toRemove = new ArrayList<>();
                toRemove.add(kv.getKey());
            }
        }

        if (toRemove != null) {
            for (final GameState game : toRemove) {
                deadGames.remove(game);
            }
        }
	}

    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    /**
     * Schedules a game to be deleted after the specified point in time.
     * @param game Game to delete
     * @param deleteAfter Timestamp to remove the game after
     */
    public void scheduleGameDeletion(final GameState game, final Instant deleteAfter) {
        deadGames.put(game, deleteAfter);
    }

    /**
     * Gets a {@link UdpClient} from a message's headers, creating a memory instance as necessary.
     * @param message Message to parse
     * @param handler Message handler attach client to 
     * @return Client where responses can be sent
     */
    public UdpClient getClient(final Message<?> message, final MessageHandler handler) {
        final int port = (Integer) message.getHeaders().get(IpHeaders.PORT);
        final String clientName = message.getHeaders().get(IpHeaders.IP_ADDRESS) + ":" + port;

        UdpClient client = clients.get(clientName);
        if (client != null) return client;

        // Doesn't exist, create a new Client 
        client = new UdpClient(
            (String) message.getHeaders().get(IpHeaders.IP_ADDRESS), 
            port, 
            handler,
            new HashMap<>(),
            new HashMap<>()
        );

        clients.put(clientName, client);

        return client;
    }

    @Override
    public CommandLoader getCommandLoader() {
        return commandLoader;
    }

    @Override
    public String getKey() {
        return serverKey;
    }

    /**
     * This class should not be manually instantiated.
     */
    public SpringMessageHandler() {
        clients = new HashMap<>();
        deadGames = new HashMap<>();
        commandLoader = new ClasspathCommandLoader();
        try {
            ((ClasspathCommandLoader) commandLoader).load(SpringMessageHandler.class.getPackageName());
        } catch (final Throwable t) {
            throw new RuntimeException("Exception while scanning classpath for gameserver commands:", t);
        }
        ACTIVE_INSTANCE = this;
    }

    /**
     * Gets the user state from a command context through casting.
     * @param context Generic command context
     * @return User state
     */
    public static GameUserState userStateFrom(final CommandContext context) {
        if (!(context instanceof SpringCommandContext)) throw new IllegalArgumentException("Passed context is not a SpringCommandContext.");
        return ((SpringCommandContext) context).getUser();
    }

    /**
     * Gets the game state from a command context through casting.
     * @param context Generic command context
     * @return Game state
     */
    public static GameState gameStateFrom(final CommandContext context) {
        if (!(context instanceof SpringCommandContext)) throw new IllegalArgumentException("Passed context is not a SpringCommandContext.");
        return ((SpringCommandContext) context).getGame();
    }

    /**
     * Gets the active instance of SpringMessageHandler.
     * @return Last instantiated instance
     */
    public static SpringMessageHandler get() {
        if (ACTIVE_INSTANCE == null) {
            throw new IllegalStateException("No SpringMessageHandler has been instantiated yet.");
        }
        return ACTIVE_INSTANCE;
    }

    /**
     * Command context for commands initiated by the server. Includes game and user states.
     */
    public class SpringCommandContext extends CommandContext {
        /**
         * User state
         */
        private final GameUserState user;

        /**
         * Game state
         */
        private final GameState game;
        
        /**
         * Constructs a new Spring command context.
         * @param user User state
         * @param game Game state
         */
        public SpringCommandContext(final GameUserState user, final GameState game) {
            this.user = user;
            this.game = game;
        }

        /**
         * Gets the user state for this context.
         * @return User state
         */
        public GameUserState getUser() {
            return user;
        }

        /**
         * Gets the game state for this context.
         * @return Game state
         */
        public GameState getGame() {
            return game;
        }
    }
}
