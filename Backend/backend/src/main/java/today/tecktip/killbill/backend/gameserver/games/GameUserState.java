package today.tecktip.killbill.backend.gameserver.games;

import java.time.Instant;

import today.tecktip.killbill.backend.db.gameusers.GameUser;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.common.gameserver.MessageHandler.UdpClient;

/**
 * Represents the state for one connected game user.
 * @author cs.
 */
public abstract class GameUserState {
    /**
     * Seconds after which the key is expected to be refreshed with the database.
     */
    private static final int KEY_REFRESH_SECONDS = 10;

    /**
     * Seconds after no heartbeats at which the user is presumed to have timed out.
     */
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * Parent game associated with this user state.
    */
    protected GameState parent;

    /**
     * The database {@link GameUser} associated with this state.
     */
    protected GameUser gameUser;

    /**
     * The database {@link User} associated with this state.
     */
    protected User user;

    /**
     * The UDP client where this user is connected.
     */
    protected UdpClient client;

    /**
     * Time after which the key must be refreshed.
     */
    private Instant keyExpiresAt;

    /**
     * Time after which the user will have timed out if they haven't sent a new heartbeat.
     */
    private Instant heartbeatExpiresAt;

    /**
     * A way to require that the 'connect' command is called before any other actions.
     */
    private boolean isConnected;

    /**
     * When the user connected.
     */
    protected Instant connectedAt;

    /**
     * Time the player has been connected for.
     */
    private long playtime;

    /**
     * Constructs a state representation of a {@link GameUser}.
     * @param parent Parent game state
     * @param user User's representation in the database
     * @param gameUser User's game user representation in the database
     * @param user Database game user to represent
     * @param client User's UDP client
     */
    public GameUserState(final GameState parent, final User user, final GameUser gameUser, final UdpClient client) {
        this.parent = parent;
        this.client = client;
        this.user = user;
        this.gameUser = gameUser;
        resetExpiration();
        isConnected = false;
        playtime = -1;
    }

    /**
     * Checks if the key is expired.
     * @return True if expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(keyExpiresAt);
    }

    /**
     * Resets the key to not expire until {@link #KEY_REFRESH_SECONDS}.
     */
    public void resetExpiration() {
        keyExpiresAt = Instant.now().plusSeconds(KEY_REFRESH_SECONDS);
    }

    /**
     * Gets the database game user associated with this user state.
     * @return Database game user
     */
    public GameUser getGameUser() {
        return gameUser;
    }

    /**
     * Gets the database user associated with this user state.
     * @return Database user
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets this user's UDP client.
     * @return User UDP client
     */
    public UdpClient getClient() {
        return client;
    }

    /**
     * Replaces the client in use for this user.
     * @param client New client
     */
    public void setClient(final UdpClient client) {
        this.client = client;
    }

    /**
     * Connects the user. Override in your user state to perform connection tasks.
     */
    public void connect() {
        updateHeartbeat();
        isConnected = true;
        connectedAt = Instant.now();
    }

    /**
     * True if the user connected up to 3 seconds ago.
     * @return Cooldown
     */
    public boolean isInConnectCooldown() {
        if (!isConnected) return true;

        return !connectedAt.isBefore(Instant.now().minusSeconds(3));
    }

    /**
     * Disconnects the user. Override in your user state to perform disconnection tasks.
     */
    public void disconnect() {
        if (!isConnected) return;
        playtime += Instant.now().getEpochSecond() - connectedAt.getEpochSecond();
        isConnected = false;
    }

    /**
     * Gets the player's playtime.
     * @return Playtime in seconds, or -1 if they haven't connected.
     */
    public long getPlaytime() {
        if (isConnected) // Currently connected, need an offset
            return playtime + Instant.now().getEpochSecond() - connectedAt.getEpochSecond();
        // Not connected, return overall playtime
        return playtime;
    }

    /**
     * Checks if the user is connected.
     * @return True if connected
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Updates the last heartbeat to now.
     */
    public void updateHeartbeat() {
        heartbeatExpiresAt = Instant.now().plusSeconds(TIMEOUT_SECONDS);
    }

    /**
     * Checks if the user's last heartbeat is expired.
     * @return True if the user has timed out
     */
    public boolean isTimedOut() {
        if (!isConnected) return false;
        return heartbeatExpiresAt.isBefore(Instant.now());
    }
}
