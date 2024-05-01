package today.tecktip.killbill.backend.gameserver.games.basic;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import today.tecktip.killbill.backend.db.games.Game;
import today.tecktip.killbill.backend.db.games.Games;
import today.tecktip.killbill.backend.db.gameusers.GameUser;
import today.tecktip.killbill.backend.db.gameusers.GameUsers;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.db.users.Users;
import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameState;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicInteractCommand.BasicInteractCommandData;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvEntityStateCommand.EntityRemovalType;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvEntityStateCommand.RecvRemoveEntityStateInvokeContext;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvGameStateCommand.GameStateFieldFilter;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvGameStateCommand.RecvGameStateInvokeContext;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicSendChatCommand.RecvSystemMessageInvokeContext;
import today.tecktip.killbill.backend.gameserver.games.basic.entities.ClaymoreRoomba;
import today.tecktip.killbill.backend.gameserver.games.basic.entities.Employee;
import today.tecktip.killbill.backend.gameserver.map.PathfindingGrid;
import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler.UdpClient;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.common.maploader.KillBillMap;
import today.tecktip.killbill.common.maploader.MapLoader;
import today.tecktip.killbill.common.maploader.directives.EntityDirective;

/**
 * Game state for the BASIC game type.
 * @author cs
 */
public class BasicGameState extends GameState {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicGameState.class);
    
    /**
     * Random number generator
     */
    private static final Random RANDOM = new Random();

    /**
     * The run state for the game.
     */
    private BasicGameRunState runState;

    /**
     * Map that this game is using.
     */
    private KillBillMap map;

    /**
     * Pathfinding grid for this map.
     */
    private PathfindingGrid grid;

    /**
     * Dropped items.
     */
    private Map<String, BasicDroppedItemState> items;

    /**
     * Map of entities that exist.
     */
    private Map<Integer, BasicEntityState> entities;

    /**
     * All block interactions that have run so far.
     */
    private List<BasicInteractCommandData> interactions;

    private boolean ending;

    private BasicPlayerType winningTeam;
    
    /**
     * Constructs a new {@link BasicGameState}.
     * @param game Parent game this is representing
     */
    public BasicGameState(final Game game) {
        super(game);
        runState = BasicGameRunState.LOBBY;
        entities = new HashMap<>();
        items = new HashMap<>();
        interactions = new ArrayList<>();
        map = MapLoader.load(List.of(new ByteArrayInputStream(game.map().getBytes())));
        grid = new PathfindingGrid(map);
        ending = false;
        winningTeam = null;
    }

    /**
     * Changes the run state of the game. Requires an invoke of {@link BasicRecvStateChangeCommand}.
     * @param newState New state to apply
     */
    public void setState(final BasicGameRunState newState) {
        runState = newState;
    }

    /**
     * Gets the current run state.
     * @return Game run state
     */
    public BasicGameRunState getState() {
        return runState;
    }

    /**
     * Checks if the game is in the specified run state.
     * @param target Target run state
     * @return True if state matches
     */
    public boolean isInState(final BasicGameRunState target) {
        return runState.equals(target);
    }

    @Override
    public void addUser(User user, GameUser gameUser, UdpClient client)  {
        GameUserState userState = new BasicGameUserState(this, user, gameUser, client);

        users.put(user.id(), userState);
    }

    @Override
    public void runGameTick(final float delta) {
        List<Integer> toRemove = null;
        for (final Map.Entry<Integer, BasicEntityState> kv : entities.entrySet()) {
            BasicEntityState entityState = kv.getValue();
            boolean remove = entityState.onTick(delta);

            if (remove) {
                // Notify clients
                try {
                    SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_REMOVE_ENTITY)
                        .run(
                            SpringMessageHandler.get(),
                            new RecvRemoveEntityStateInvokeContext(this, entityState, EntityRemovalType.DESPAWN)
                        );
                } catch (final MessageFailure e) {
                    LOGGER.error("Failed to notify clients of removed entity.", e);
                }

                if (toRemove == null) {
                    toRemove = new ArrayList<>();
                }
                toRemove.add(kv.getKey());
            } else {
                entityState.sync();
            }
        }

        if (toRemove != null) {
            for (final Integer id : toRemove) entities.remove(id);
        }
    }

    public void onStateChange(final BasicGameRunState oldState) {
        // Initialize each user if they're not initialized yet
        // First, field filter will be everything

        if (oldState.equals(BasicGameRunState.LOBBY) && runState.equals(BasicGameRunState.PLAYING)) {
            // Decide which player will be our Bill
            final int billIndex = RANDOM.nextInt(getConnectedUsers().size());
            int i = 0;
            int playerIndex = 0;

            for (final GameUserState user : getConnectedUsers().values()) {
                if (billIndex == i) {
                    ((BasicGameUserState) user).init(BasicPlayerType.BILL, 0);
                } else {
                    ((BasicGameUserState) user).init(BasicPlayerType.PLAYER, playerIndex);
                    playerIndex++;
                }

                i++;
            }

            // Summon entities
            map.forEachDirectiveOfType(
                entity -> {
                    BasicEntityState newEntity = null;
                    switch (entity.getEntityType()) {
                        case EMPLOYEE:
                            newEntity = new Employee(
                                entity.getId(),
                                entity.getRotation(),
                                this,
                                entity.getLocation().copy()
                            );
                            break;
                        case CLAYMORE_ROOMBA:
                            newEntity = new ClaymoreRoomba(
                                entity.getId(), 
                                entity.getRotation(),
                                this, 
                                entity.getLocation().copy()
                            );
                            break;
                        default:
                            newEntity = null;
                            break;
                    }

                    // Add to the state
                    if (newEntity != null) {
                        entities.put(entity.getId(), newEntity);
                    }
                }, EntityDirective.class);
        }
    }

    public BasicDroppedItemState addDroppedItem(final String id, final Coordinates location, final ItemType itemType, final int quantity)  {
        BasicDroppedItemState item = new BasicDroppedItemState(id, location, itemType, quantity);
        items.put(id, item);
        return item;
    }

    public BasicDroppedItemState getDroppedItem(final String id)  {
        return items.get(id);
    }

    public boolean removeDroppedItem(final String id)  {
        return items.remove(id) != null;
    }

    public Map<String, BasicDroppedItemState> getDroppedItems() {
        return items;
    }

    public PathfindingGrid getPathfindingGrid() {
        return grid;
    }

    public void end() {
        // Should be only one team remaining besides spectators.
        int playerCount = 0;

        for (final GameUserState rUser : getConnectedUsers().values()) {
            final BasicGameUserState user = (BasicGameUserState) rUser;

            if (user.getPlayerType().equals(BasicPlayerType.PLAYER)) playerCount++;
        }

        winningTeam = playerCount == 0 ? BasicPlayerType.BILL : BasicPlayerType.PLAYER;
        setState(BasicGameRunState.ENDED);
        try {
            SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_GAME_STATE)
                .run(
                    SpringMessageHandler.get(),
                    new RecvGameStateInvokeContext(this, List.of(GameStateFieldFilter.RUN_STATE, GameStateFieldFilter.WINNING_TEAM))
                );
        } catch (final MessageFailure e) {
            LOGGER.error("Failed to end game!", e);
        }

        // Remove the game from the database
        try {
            GameUsers.deleteGameUsers(game.id());
            Games.deleteGame(game.id());
        } catch (final SQLException e) {
            LOGGER.error("Failed to clear game from database!", e);
        }

        // Iterate over each user and update their stats
        for (final GameUserState rUser : getUsers().values()) {
            final BasicGameUserState user = (BasicGameUserState) rUser;

            long playtime = user.getPlaytime();
            if (playtime == -1) continue; // No stats for you :)

            boolean wonAsBill = user.getOriginalPlayerType().equals(BasicPlayerType.BILL)
                && winningTeam.equals(BasicPlayerType.BILL);
            boolean wonAsPlayer = user.getOriginalPlayerType().equals(BasicPlayerType.PLAYER)
                && winningTeam.equals(BasicPlayerType.PLAYER);

            // Increment the user's stats
            try {
                User dbUser = Users.getUser(user.getUser().id());

                User newUser = new User(
                    dbUser.id(),
                    dbUser.created(),
                    dbUser.name(),
                    dbUser.role(),
                    dbUser.winsAsBill() + (wonAsBill ? 1 : 0),
                    dbUser.winsAsPlayer() + (wonAsPlayer ? 1 : 0),
                    dbUser.playtime() + playtime
                );

                Users.putUser(newUser);
            } catch (final SQLException e) {
                LOGGER.error("Failed to update user's stats post-game!", e);
            }
        }

        // Game is dead. Schedule for deletion in 10 seconds
        SpringMessageHandler.get().scheduleGameDeletion(this, Instant.now().plusSeconds(10));
    }

    public void checkGameEnd() {
        if (ending) return;
        if (!runState.equals(BasicGameRunState.PLAYING)) return;

        // Should be only one team remaining besides spectators.
        int playerCount = 0;
        int billCount = 0;

        for (final GameUserState rUser : getConnectedUsers().values()) {
            final BasicGameUserState user = (BasicGameUserState) rUser;

            if (user.getPlayerType().equals(BasicPlayerType.PLAYER)) playerCount++;
            else if (user.getPlayerType().equals(BasicPlayerType.BILL)) billCount++;
        }

        if (playerCount == 0 || billCount == 0) {
            ending = true;
            // Game over! The game will be ended in 5 seconds
            try {
                SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE)
                    .run(SpringMessageHandler.get(), new RecvSystemMessageInvokeContext(playerCount == 0 ? "Bill has prevailed." : "Bill has been defeated.", this));
            } catch (final MessageFailure e) {
                LOGGER.error("Failed to send chat message for game end.", e);
            }

            SpringMessageHandler.get().getTaskScheduler().schedule(
                this::end,
                Instant.now().plusSeconds(5)
            );
        }
    }

    /**
     * Gets all connected players in range of the specified destination.
     * @param destination The destination coordinates
     * @param rangeSquared The expected range in tiles squared
     * @return Null if no players, list of players if any in range
     */
    public List<BasicGameUserState> getPlayersInRange(final Coordinates destination, final float rangeSquared) {
        List<BasicGameUserState> inRange = null;

        for (final GameUserState rUserState : getConnectedUsers().values()) {
            BasicGameUserState userState = (BasicGameUserState) rUserState;
            // Calculate euclidian distance
            double distance = Math.pow(destination.x() - userState.getCoordinates().x(), 2)
                + Math.pow(destination.y() - userState.getCoordinates().y(), 2);

            if (distance <= rangeSquared) {
                if (inRange == null) inRange = new ArrayList<>();
                inRange.add(userState);
            }
        }

        return inRange;
    }

    /**
     * Runs a method on every connected user.
     * @param method Method to call (return true to cancel future executions)
     * @return True if any method returned true
     */
    public boolean forEachConnectedUser(final PerUserMethod method) {
        for (final GameUserState rUserState : getConnectedUsers().values()) {
            BasicGameUserState userState = (BasicGameUserState) rUserState;

            if (method.run(userState)) return true;
        }
        return false;
    }

    public Map<Integer, BasicEntityState> getEntities() {
        return entities;
    }

    public List<BasicInteractCommandData> getInteractions() {
        return interactions;
    }

    public void addInteraction(final BasicInteractCommandData data) {
        interactions.add(data);
    }

    public BasicPlayerType getWinningTeam() {
        return winningTeam;
    }


    public static interface PerUserMethod {
        public boolean run(final BasicGameUserState userState);
    }

    /**
     * The possible states a game can be in.
     */
    public static enum BasicGameRunState {
        /**
         * Players are waiting in the lobby.
         */
        LOBBY,

        /**
         * The game is active.
         */
        PLAYING,

        /**
         * The game has ended.
         */
        ENDED
    }
}
