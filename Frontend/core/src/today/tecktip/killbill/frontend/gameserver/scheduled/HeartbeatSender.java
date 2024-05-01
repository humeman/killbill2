package today.tecktip.killbill.frontend.gameserver.scheduled;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeContext;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;

/**
 * Scheduled task which handles sending heartbeats to the server.
 * @author cs
 */
public class HeartbeatSender {
    /**
     * Logs go here
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(HeartbeatSender.class);

    /**
     * The minimum delay between heartbeat sends.
     */
    private static final int HEARTBEAT_DELAY_SECONDS = 3;

    /**
     * Constructs a new heartbeat sender task.
     */
    public HeartbeatSender() {}

    /**
     * Runs the scheduled heartbeat sender task.
     * @param handler Message handler
     */
    public void run(final ClientMessageHandler handler) {
        if (((ClientMessageHandler) handler).isReady() && ((ClientMessageHandler) handler).getLastSend().plusSeconds(HEARTBEAT_DELAY_SECONDS).isBefore(Instant.now())) {
            try {
                handler.getCommandLoader().invokeMethodFor(
                    ((ClientMessageHandler) handler).getGame().config().getGameType(),
                    MessageDataType.COMMAND_HEARTBEAT
                ).run(handler, new InvokeContext());
            } catch (final MessageFailure e) {
                LOGGER.error("Failed to send heartbeat. Will retry shortly.", e);
                return;
            }
        }
    }
}
