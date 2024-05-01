package today.tecktip.killbill.frontend.gameserver.game.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;
import today.tecktip.killbill.frontend.gameserver.game.LocalGameState;
import today.tecktip.killbill.frontend.gameserver.game.LocalGameUserState;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangeGameStateCommand.ChangeGameStateInvokeContext;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangeGameStateCommand.GameStateFieldFilter;
import today.tecktip.killbill.frontend.http.requests.data.Game;

/**
 * Game state for the BASIC game type.
 * @author cs
 */
public class BasicLocalGameState extends LocalGameState {
    private BasicGameRunState runState;

    /**
     * Notes any fields updated locally that have yet to be synced.
     */
    private List<GameStateFieldFilter> updatedFields;

    private Map<String, BasicLocalDroppedItemState> items;

    private BasicLocalDroppedItemState recentItem;

    private Map<Integer, BasicLocalEntityState> entities;

    private BasicPlayerType winningTeam;

    /**
     * Constructs a new {@link BasicGameState}.
     * @param game Parent game this is representing
     */
    public BasicLocalGameState(final Game game) {
        super(game);
        runState = BasicGameRunState.UNKNOWN;
        items = new HashMap<>();
        updatedFields = new ArrayList<>(GameStateFieldFilter.values().length);
        recentItem = null;
        entities = new HashMap<>();
        winningTeam = null;
    }
    public BasicGameRunState getState() {
        return runState;
    }

    public void setState(final BasicGameRunState runState) {
        this.runState = runState;
    }

    public void checkUserList(final List<UUID> connectedUsers) {
        for (final UUID userId : connectedUsers) {
            // Check that the user is present in the state
            LocalGameUserState userState = users.get(userId);
            if (userState == null) {
                // This user has new state, but has not been initialized yet.
                // Add them to the state and kick off the init process.
                addUser(userId);
                userState = getUser(userId);
            }
    
            BasicLocalGameUserState user = (BasicLocalGameUserState) userState;

            // Queue some things, then continue.
            if (!user.isConnected()) {
                // Get their data and mark as connected
                user.connect();
                user.getPlayerData(getState().equals(BasicGameRunState.PLAYING));
            } 
        }

        for (final Map.Entry<UUID, LocalGameUserState> entry : getUsers().entrySet()) {
            if (!connectedUsers.contains(entry.getKey())) {
                // Disconnect 'em
                if (entry.getValue().isConnected()) entry.getValue().disconnect(); 
            }
        }
        
    }

    /**
     * Gets a list of all users that have their state available.
     * @return User list (user ID to game state)
     */
    public List<BasicLocalGameUserState> getReadyUsers() {
        return users.values().stream()
            .map(e -> { return (BasicLocalGameUserState) e; })
            .filter(e -> { return e.isReady(); })
            .toList();
    }

    @Override
    public void addUser(UUID userId)  {
        LocalGameUserState userState = new BasicLocalGameUserState(this, userId);

        users.put(userId, userState);
    }

    /**
     * Marks that a particular field was updated locally. It will be sent on next {@link #sync}.
     * @param field Field that was updated
     */
    public void setFieldChanged(final GameStateFieldFilter field) {
        if (!updatedFields.contains(field))
            updatedFields.add(field);
    }

    /**
     * Syncs the game's local state to the UDP server if anything has changed.
     * @throws MessageFailure Failed to send
     */
    public void sync() throws MessageFailure {
        // Actual game state changes
        if (updatedFields.size() != 0) {
            ClientMessageHandler.get().getCommandLoader().invokeMethodFor(getGame().config().getGameType(), MessageDataType.COMMAND_CHANGE_GAME_STATE)
                .run(
                    ClientMessageHandler.get(),
                    new ChangeGameStateInvokeContext(this, updatedFields)
                );
            updatedFields.clear();
        }

        // Update data if we're currently playing
        if (runState.equals(BasicGameRunState.PLAYING)) {
            // Check entities real quick
            List<Integer> entitesToRemove = null;
            for (final Map.Entry<Integer, BasicLocalEntityState> kv : entities.entrySet()) {
                // Sync it
                kv.getValue().sync();

                // If they're dead, remove them
                if (kv.getValue().getHealth() <= 0) {
                    if (entitesToRemove == null) entitesToRemove = new ArrayList<>();
                    entitesToRemove.add(kv.getKey());
                }
            }

            if (entitesToRemove != null) {
                for (final Integer k : entitesToRemove) entities.remove(k);
            }

            // Just sync players
            for (final Map.Entry<UUID, LocalGameUserState> kv : users.entrySet()) {
                BasicLocalGameUserState userState = (BasicLocalGameUserState) kv.getValue();

                if (!userState.isReady()) continue;

                if (userState.getUserId().equals(KillBillGame.get().getUser().id())) {
                    // Sync as self
                    userState.sync();
                } else {
                    // Sync as other
                    userState.syncAsOther();
                }
            }
        }
    }

    public BasicLocalDroppedItemState addDroppedItem(final String id, final Coordinates location, final ItemType itemType, final int quantity)  {
        BasicLocalDroppedItemState item = new BasicLocalDroppedItemState(id, location, itemType, quantity);
        items.put(id, item);
        return item;
    }

    public BasicLocalDroppedItemState getDroppedItem(final String id)  {
        return items.get(id);
    }

    public boolean removeDroppedItem(final String id)  {
        return items.remove(id) != null;
    }

    public Map<String, BasicLocalDroppedItemState> getDroppedItems() {
        return items;
    }

    public BasicLocalDroppedItemState getRecentItem() {
        return recentItem;
    }

    public void setRecentItem(final BasicLocalDroppedItemState item) {
        recentItem = item;
    }

    public BasicLocalEntityState getEntity(final int entityId) {
        return entities.get(entityId);
    }

    public void setEntity(final int entityId, final BasicLocalEntityState entityState) {
        entities.put(entityId, entityState);
    }

    public Map<Integer, BasicLocalEntityState> getEntities() {
        return entities;
    }

    public BasicLocalEntityState removeEntity(final int entityId) {
        return entities.remove(entityId);
    }

    public BasicPlayerType getWinningTeam() {
        return winningTeam;
    }

    public void setWinningTeam(final BasicPlayerType winningTeam) {
        this.winningTeam = winningTeam;
    }

    /**
     * The possible states a game can be in.
     */
    public static enum BasicGameRunState {
        /**
         * No UDP data yet.
         */
        UNKNOWN,

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

