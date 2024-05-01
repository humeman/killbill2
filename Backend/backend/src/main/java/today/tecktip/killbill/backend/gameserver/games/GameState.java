package today.tecktip.killbill.backend.gameserver.games;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import today.tecktip.killbill.backend.db.games.Game;
import today.tecktip.killbill.backend.db.gameusers.GameUser;
import today.tecktip.killbill.backend.db.gameusers.GameUsers;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.exceptions.NotFoundException;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.common.gameserver.MessageHandler.UdpClient;

/**
 * Represents the state of a game in memory.
 * @author cs
 */
public abstract class GameState {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GameState.class);

    /**
     * All games stored in memory right now.
     */
    private static final HashMap<UUID, GameState> games = new HashMap<>();

    /**
     * The database game this game state is representing.
     */
    protected final Game game;

    /**
     * The user states associated with this game.
     */
    protected final HashMap<UUID, GameUserState> users = new HashMap<>();

    /**
     * Constructs a new GameState.
     * @param game Database game this state is tied to
     */
    public GameState(final Game game) {
        games.put(game.id(), this);
        this.game = game;
    }

    /**
     * Gets the database game associated with this state.
     * @return Game database representation
     */
    public Game getGame() {
        return game;
    }

    /**
     * Gets the state for a user that is registered to this game, refreshing their
     * authentication token as necessary.
     * @param userId User's ID
     * @return User's state
     * @throws SQLException Unable to refresh token
     */
    public GameUserState getUser(final UUID userId) throws SQLException {
        GameUserState user = users.get(userId);

        // Return prematurely that they weren't found if it's not there
        if (user == null) return null;

        // Validate authentication if expired
        if (user.isExpired()) {
            try {
                GameUser gUser = GameUsers.getGameUser(game.id(), userId);
                if (!gUser.key().equals(user.getGameUser().key())) {
                    throw new NotFoundException(null);
                }
            } catch (NotFoundException e) {
                users.remove(userId);
                return null;
            }
            user.resetExpiration();
        }

        return user;
    }

    /**
     * Gets an unmodifiable map of all users in the game.
     * @return Unmodifable game map (user ID to game state)
     */
    public Map<UUID, GameUserState> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    /**
     * Gets an unmodifiable map of all users currently connected to the game.
     * @return Unmodifable game map (user ID to game state)
     */
    public Map<UUID, GameUserState> getConnectedUsers() {
        return Collections.unmodifiableMap(users.entrySet().stream()
            .filter(e -> e.getValue().isConnected())
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    /**
     * Registers a user to the game.
     * @param user Database User
     * @param gameUser Database GameUser
     * @param client UDP client to attach to GameUser
     */
    public abstract void addUser(final User user, final GameUser gameUser, final UdpClient client);

    /**
     * Runs a game tick (every {@link SpringMessageHandler#GAME_TICK_MS} ms)
     * @param delta Time since last game tick
     */
    public abstract void runGameTick(final float delta);

    /**
     * Registers a new game to memory, generating its state.
     * @param game Database game
     */
    public static void register(final Game game) {
        GameState gameState;
        switch (game.config().getGameType()) {
            case BASIC:
                gameState = new BasicGameState(game);
                break;
            default:
                throw new IllegalArgumentException("Invalid game type: " + game.config().getGameType());
        }
        games.put(game.id(), gameState);
    }

    /**
     * Gets a game's state.
     * @param gameId Game ID
     * @return Game state
     */
    public static GameState get(final UUID gameId) {
        return games.get(gameId);
    }

    /**
     * Runs an operator once for each game registered.
     * @param method Method to call on each game
     */
    public static void forEach(final GameOperatorMethod method) {
        for (final GameState state : games.values()) {
            method.run(state);
        }
    }

    /**
     * Destroys a game. Only removes from state, it must be removed from the database after.
     * @param game Game to destroy
     */
    public static void destroy(final GameState game) {
        games.remove(game.getGame().id());
    }

    /**
     * Disconnects any timed out clients in this game.
     */
	public void disconnectTimedOutClients() {
		for (final Map.Entry<UUID, GameUserState> entry : users.entrySet()) {
            if (entry.getValue().isTimedOut()) {
                LOGGER.info("Disconnecting " + entry.getKey() + ": timed out");
                entry.getValue().disconnect();
            }
        }
	}

    /**
     * Functional interface for operating on gamestates one by one.
     */
    public interface GameOperatorMethod {
        /**
         * Called on a particular game state.
         * @param state Game state to operate on
         */
        public void run(final GameState state);
    }
}
