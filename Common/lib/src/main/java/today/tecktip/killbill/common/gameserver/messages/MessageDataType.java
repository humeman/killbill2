package today.tecktip.killbill.common.gameserver.messages;

/**
 * All of the possible message data types.
 * @author cs
 */
public enum MessageDataType {
    /* UNIVERSAL COMMANDS */
        /**
         * Represents a ping coming from a client.
         */
        COMMAND_PING,
        /**
         * Response to {@link #COMMAND_PING}.
         */
        RESP_PING,

        /**
         * Initiates a connection.
         */
        COMMAND_CONNECT,

        /**
         * Response to {@link #COMMAND_CONNECT}.
         */
        RESP_CONNECT,

        /**
         * Disconnects from the server.
         */
        COMMAND_DISCONNECT,

        /**
         * A client heartbeat.
         */
        COMMAND_HEARTBEAT,

    /* BASIC GAME COMMANDS */
        /**
         * send chat message
         */
        COMMAND_SEND_CHAT,

        /**
         * receive chat message
         */
        COMMAND_RECV_CHAT,

        /**
         * A system chat message to all players.
         */
        COMMAND_RECV_SYSTEM_MESSAGE,

        /** 
         * Updates the game's state.
         */
        COMMAND_CHANGE_GAME_STATE,

        /**
         * Notes that a game state change was successful.
         */
        RESP_GAME_STATE_CHANGED,

        /**
         * Tells clients that the game state has changed.
         */
        COMMAND_RECV_GAME_STATE,

        /**
         * Gets the game's state.
         */
        COMMAND_GET_GAME_STATE,

        /**
         * Returns the game's current state.
         */
        RESP_GET_GAME_STATE,

        /** 
         * Changes a player's state.
         */
        COMMAND_CHANGE_PLAYER_STATE,

        /**
         * Changes another player's state (in a somewhat controlled fashion).
         */
        COMMAND_CHANGE_OTHER_PLAYER_STATE,

        /**
         * Tells clients that a player state has changed.
         */
        COMMAND_RECV_PLAYER_STATE,

        /**
         * Gets a player's state.
         */
        COMMAND_GET_PLAYER_STATE,

        /**
         * Returns the player's current state.
         */
        RESP_GET_PLAYER_STATE,

        /**
         * Deals damage to another player.
         */
        COMMAND_DAMAGE_PLAYER,

        /**
         * Interacts with an object like a chest.
         */
        COMMAND_INTERACT,

        /**
         * Asks the server to send out all state data.
         * Called by clients when they join a PLAYING game or when
         *  the game transitions from LOBBY to PLAYING.
         */
        COMMAND_SEND_STATE,

        /**
         * Receives an object interaction performed by another client.
         */
        COMMAND_RECV_INTERACTION,

        /**
         * Allows clients to summon entities (Bill only).
         */
        COMMAND_SUMMON_ENTITY,

        /**
         * Changes the state for an entity.
         */
        COMMAND_CHANGE_ENTITY_STATE,

        /**
         * Receives updated entity state.
         */
        COMMAND_RECV_ENTITY_STATE,

        /**
         * Requests full entity state.
         */
        COMMAND_GET_ENTITY_STATE,

        /**
         * Returns full entity state.
         */
        RESP_GET_ENTITY_STATE,

        /**
         * Destroys an existing entity.
         */
        COMMAND_RECV_REMOVE_ENTITY,

        /**
         * Add a new dropped item.
         */
        COMMAND_CREATE_DROPPED_ITEM,

        /**
         * Register a dropped item.
         */
        COMMAND_RECV_NEW_DROPPED_ITEM,

        /**
         * Remove a dropped item.
         */
        COMMAND_REMOVE_DROPPED_ITEM,

        /**
         * Unregister a dropped item.
         */
        COMMAND_RECV_REMOVE_DROPPED_ITEM,

        /**
         * Creates a new projectile.
         */
        COMMAND_CREATE_PROJECTILE,

        /**
         * Notifies clients of a new projectile.
         */
        COMMAND_RECV_PROJECTILE,

        /**
         * Creates a new bomb.
         */
        COMMAND_CREATE_BOMB,

        /**
         * Notifies clients of a new bomb.
         */
        COMMAND_RECV_BOMB,

    /* UNIVERSAL */

    /**
     * Global error type: Authentication failure.
     */
    AUTHENTICATION_FAILURE,
    
    /**
     * Internal server error while processing.
     */
    INTERNAL_SERVER_ERROR,
    
    /**
     * Invalid argument in JSON payload.
     */
    INVALID_ARGUMENT_EXCEPTION,

    /**
     * Notes that the action performed would result in a game or user in an illegal state
     */
    ILLEGAL_STATE_EXCEPTION,

    /**
     * Empty message for ACKs.
     */
    EMPTY;
}