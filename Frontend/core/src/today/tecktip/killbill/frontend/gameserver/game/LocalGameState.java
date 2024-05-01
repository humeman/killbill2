package today.tecktip.killbill.frontend.gameserver.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import today.tecktip.killbill.frontend.http.requests.data.Game;

public abstract class LocalGameState {
    /**
     * Static reference to current instance.
     */
    private static LocalGameState GAME;

    /**
     * The database game this game state is representing.
     */
    protected final Game game;

    /**
     * The user states associated with this game.
     */
    protected final HashMap<UUID, LocalGameUserState> users = new HashMap<>();

    private AtomicBoolean lock;

    /**
     * Constructs a new GameState.
     * @param game Database game this state is tied to
     */
    public LocalGameState(final Game game) {
        this.game = game;
        GAME = this;
        lock = new AtomicBoolean(false);
    }

    public boolean acquireLock() {
        return lock.compareAndSet(false, true);
    }

    public void releaseLock() {
        lock.set(false);
    }

    /**
     * Gets the database game associated with this state.
     * @return Game database representation
     */
    public Game getGame() {
        return game;
    }

    /**
     * Gets the state for a user that is registered to this game.
     * @param userId User's ID
     * @return User's state
     */
    public LocalGameUserState getUser(final UUID userId) {
        return users.get(userId);
    }

    /**
     * Gets an unmodifiable map of all users in the game.
     * @return Unmodifable game map (user ID to game state)
     */
    public Map<UUID, LocalGameUserState> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    /**
     * Gets an unmodifiable map of all users currently connected to the game.
     * @return Unmodifable game map (user ID to game state)
     */
    public Map<UUID, LocalGameUserState> getConnectedUsers() {
        return Collections.unmodifiableMap(users.entrySet().stream()
            .filter(e -> e.getValue().isConnected())
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    /**
     * Registers a user to the game.
     * @param user Database GameUser ID
     * @param client UDP client to attach to GameUser
     */
    public abstract void addUser(final UUID user);

    /**
     * Gets a game's state.
     * @return Game state
     */
    public static LocalGameState get() {
        return GAME;
    }

    /**
     * Destroys a game.
     */
    public static void destroy() {
        GAME = null;
    }
}
