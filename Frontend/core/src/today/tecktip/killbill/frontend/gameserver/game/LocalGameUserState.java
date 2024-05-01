package today.tecktip.killbill.frontend.gameserver.game;

import java.util.UUID;

import today.tecktip.killbill.frontend.http.requests.data.User;

public abstract class LocalGameUserState {
    /**
     * Parent game associated with this user state.
    */
    protected LocalGameState parent;

    /**
     * The database user ID associated with this state.
     */
    protected UUID userId;

    /**
     * Whether or not the user is connected, as reported by the server.
     */
    private boolean isConnected;

    /**
     * Notes if this state was updated by the UDP server.
     */
    protected boolean wasUpdated;

    /**
     * Constructs a state representation of a game user.
     * @param parent Parent game state
     * @param userId Database game user to represent
     * @param client User's UDP client
     */
    public LocalGameUserState(final LocalGameState parent, final UUID userId) {
        this.parent = parent;
        this.userId = userId;
        isConnected = false;
        wasUpdated = false;
    }

    /**
     * Gets the database game user associated with this user state.
     * @return Database game user ID
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Connects the user. Override in your user state to perform connection tasks.
     */
    public void connect() {
        isConnected = true;
    }

    /**
     * Disconnects the user. Override in your user state to perform disconnection tasks.
     */
    public void disconnect() {
        isConnected = false;
    }

    /**
     * Checks if the user is connected.
     * @return True if connected
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Checks if the state was recently updated.
     * @return True if updated
     */
    public boolean wasUpdated() {
        return wasUpdated;
    }

    /**
     * Resets the updated state.
     */
    public void clearUpdate() {
        wasUpdated = false;
    }

    /**
     * Gets the user's API data, and then requests player state.
     * @param getUdp If true, sends a request to the UDP server for complete player state.
     */
    public abstract void getPlayerData(final boolean getUdp);

    /**
     * Gets the API user associated with this state.
     * @return API user
     */
    public abstract User getUser();

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof LocalGameUserState)) return false;

        return ((LocalGameUserState) o).getUserId().equals(getUserId());
    }
}
