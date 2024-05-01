package today.tecktip.killbill.backend.gameserver.games.basic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import today.tecktip.killbill.backend.db.gameusers.GameUser;
import today.tecktip.killbill.backend.db.users.User;
import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState.BasicGameRunState;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvGameStateCommand.GameStateFieldFilter;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicRecvGameStateCommand.RecvGameStateInvokeContext;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicSendChatCommand.RecvSystemMessageInvokeContext;
import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.MessageHandler.UdpClient;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.BasicPlayerConfig;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.misc.TimestampedValue;

/**
 * User state for the BASIC game type.
 * @author cs
 */
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class BasicGameUserState extends GameUserState {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicGameUserState.class);

    private BasicGameState parent;

    private TimestampedValue<Coordinates> coords;

    private TimestampedValue<Integer> rotation;

    private BasicPlayerType originalPlayerType;

    private TimestampedValue<BasicPlayerType> playerType;

    private TimestampedValue<Integer> health;

    private TimestampedValue<String> heldItemTexture;

    private TimestampedValue<String> texturePrefix;

    private boolean initialized;

    private BasicPlayerConfig playerConfig; 

    /**
     * Constructs a new user state for the BASIC game type.
     * <p>
     * Key is expected to be validated before instantiation.
     * @param parent Parent game state
     * @param user User as represented in the database
     * @param gameUser Game user as represented in the database
     * @param client User's UDP client
     */
	public BasicGameUserState(final BasicGameState parent, final User user, final GameUser gameUser, final UdpClient client) {
		super(parent, user, gameUser, client);

        this.parent = parent;
        initialized = false;
        playerType = new TimestampedValue<>(null);
        coords = new TimestampedValue<>(null);
        texturePrefix = new TimestampedValue<>(null);
        rotation = new TimestampedValue<>(-1);
        health = new TimestampedValue<>(-1);
        heldItemTexture = new TimestampedValue<>("none");
	}

    @Override
    public void connect() {
        // Initialize if they joined mid-game
        if (parent.isInState(BasicGameRunState.PLAYING)) {
            // If they haven't been assigned a state at this point, don't let them play
            if (!initialized) {
                init(BasicPlayerType.SPECTATOR, 0);
            }
        }

        if (parent.isInState(BasicGameRunState.ENDED)) {
            // Don't allow connections
            throw new IllegalStateException("Cannot connect to an ended game.");
        }

        super.connect();

        // Notify users
        final List<UUID> userFilter = new ArrayList<UUID>();
        for (final UUID sId : parent.getConnectedUsers().keySet()) {
            if (!sId.equals(user.id())) userFilter.add(sId);
        } 

        try {
            SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_GAME_STATE)
                .run(
                    SpringMessageHandler.get(),
                    new RecvGameStateInvokeContext(parent, List.of(GameStateFieldFilter.USERS), userFilter)
                );
        } catch (final MessageFailure e) {
            LOGGER.error("Failure in invoking RECV_GAME_STATE, some clients may be desynced temporarily.", e);
        }

        try {
            SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE)
                .run(
                    SpringMessageHandler.get(),
                    new RecvSystemMessageInvokeContext(getUser().name() + " joined.", parent)
                );
        } catch (final MessageFailure e) {
            LOGGER.error("Failure in sending join system message.", e);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Initializes a player based on their player type and index.
     * @param type Player type.
     * @param typeId "id" field of the config within that type to assign to the player.
     */
    public void init(final BasicPlayerType type, final int typeId) {
        if (!initialized) {
            // Wipe their connect time so there's a grace period
            connectedAt = Instant.now();

            playerConfig = null;
            for (final BasicPlayerConfig possibleConfig : parent.getGame().config().getBasicConfig().getPlayerConfig().get(type)) {
                if (possibleConfig.getId() == typeId) {
                    playerConfig = possibleConfig;
                }
            }
            if (playerConfig == null) {
                throw new IllegalArgumentException("No such player config ID: " + type + "[" + typeId + "]");
            }

            coords.set(playerConfig.getSpawnpoint());
            rotation.set(0);
            health.set(playerConfig.getMaxHealth());
            playerType.set(type);
            originalPlayerType = type;
            heldItemTexture.set(null);
            texturePrefix.set(playerConfig.getTexturePrefix());
            initialized = true;
        }
    }

    @Override
    public void disconnect() {
        super.disconnect();
        // Notify users
        final List<UUID> userFilter = new ArrayList<UUID>();
        for (final UUID sId : parent.getConnectedUsers().keySet()) {
            if (!sId.equals(user.id())) userFilter.add(sId);
        }

        try {
            SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_GAME_STATE)
                .run(
                    SpringMessageHandler.get(),
                    new RecvGameStateInvokeContext(parent, List.of(GameStateFieldFilter.USERS), userFilter)
                );
        } catch (final MessageFailure e) {
            LOGGER.error("Failure in invoking RECV_GAME_STATE, some clients may be desynced temporarily.", e);
        }

        try {
            SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE)
                .run(
                    SpringMessageHandler.get(),
                    new RecvSystemMessageInvokeContext(getUser().name() + " left.", parent)
                );
        } catch (final MessageFailure e) {
            LOGGER.error("Failure in sending join system message.", e);
        }

        parent.checkGameEnd();
    }

    /**
     * Gets the user's current coordinates.
     * @return Coordinates
     */
    public Coordinates getCoordinates() {
        return coords.get();
    }

    /**
     * Returns current coordinates in the expected JSON transmission format of a list of two doubles.
     * @return Coordinates, JSON serializable
     */
    @JsonProperty("coordinates")
    public Double[] getCoordinatesAsList() {
        return new Double[]{coords.get().x(), coords.get().y()};
    }

    /**
     * Sets the user's current coordinates.
     * @param coords New coordinates
     */
    public void setCoordinates(final Coordinates coords) {
        this.coords.set(coords);
    }

    /**
     * Sets the user's current coordinates if the specified timestamp is after the current value's.
     * @param createdAt When the new value was generated by the other end
     * @param coords New coordinates
     */
    public void setCoordinates(final Instant createdAt, final Coordinates coords) {
        this.coords.set(createdAt, coords);
    }

    /**
     * Gets the user's rotation.
     * @return Rotation in degrees, 0 to 359 inclusive
     */
    @JsonProperty("rotation")
    public int getRotation() {
        return rotation.get();
    }

    /**
     * Sets the user's rotation.
     * @param color New rotation, 0 to 359 inclusive
     */
    public void setRotation(final int rotation) {
        this.rotation.set(rotation);
    }

    /**
     * Sets the user's rotation if the specified timestamp is after the current value's.
     * @param createdAt When the new value was generated by the other end
     * @param rotation New rotation
     */
    public void setRotation(final Instant createdAt, final int rotation) {
        this.rotation.set(createdAt, rotation);
    }   
    
    /**
     * Gets the player's type.
     * @return Player type
     */
    @JsonProperty("playerType")
    public BasicPlayerType getPlayerType() {
        return playerType.get();
    }

    /**
     * Gets the user's player type when they first joined.
     * @return Player type at {@link #init}
     */
    public BasicPlayerType getOriginalPlayerType() {
        return originalPlayerType;
    }

    /**
     * Sets the user's player type.
     * @param playerType New player type
     */
    public void setPlayerType(final BasicPlayerType playerType) {
        this.playerType.set(playerType);
    }

    /**
     * Sets the user's player type if the specified timestamp is after the current value's.
     * @param createdAt When the new value was generated by the other end
     * @param playerType New player type
     */
    public void setPlayerType(final Instant createdAt, final BasicPlayerType playerType) {
        this.playerType.set(createdAt, playerType);
    }   

    /**
     * Gets the user's health.
     * @return Health
     */
    @JsonProperty("health")
    public int getHealth() {
        return health.get();
    }

    /**
     * Gets the user's maximum health health.
     * @return Max health
     */
    @JsonProperty("maxHealth")
    public int getMaxHealth() {
        return playerConfig.getMaxHealth();
    }

    /**
     * Sets the user's health.
     * @param color New health
     */
    public void setHealth(final int health) {
        this.health.set(health);
    }

    /**
     * Sets the user's health if the specified timestamp is after the current value's.
     * @param createdAt When the new value was generated by the other end
     * @param health New health
     */
    public void setHealth(final Instant createdAt, final int health) {
        this.health.set(createdAt, health);
    }   

    /**
     * Gets the user's held item texture.
     * @return Held texture asset name
     */
    @JsonProperty("heldItemTexture")
    public String getHeldItemTexture() {
        return heldItemTexture.get();
    }

    /**
     * Sets the user's held item texture.
     * @param color Held texture asset name
     */
    public void setHeldItemTexture(final String heldItemTexture) {
        this.heldItemTexture.set(heldItemTexture);
    }

    /**
     * Sets the user's held item texture if the specified timestamp is after the current value's.
     * @param createdAt When the new value was generated by the other end
     * @param health New health
     */
    public void setHeldItemTexture(final Instant createdAt, final String heldItemTexture) {
        this.heldItemTexture.set(createdAt, heldItemTexture);
    }   

    /**
     * Gets the user's texture prefix.
     * @return Texture asset prefix
     */
    @JsonProperty("texturePrefix")
    public String getTexturePrefix() {
        return texturePrefix.get();
    }

    /**
     * Sets the user's texture prefix.
     * @param texturePrefix Texture asset prefix
     */
    public void setTexturePrefix(final String texturePrefix) {
        this.texturePrefix.set(texturePrefix);
    }

    /**
     * Sets the user's texture prefix if the specified timestamp is after the current value's.
     * @param createdAt When the new value was generated by the other end
     * @param texturePrefix New texture prefix
     */
    public void setTexturePrefix(final Instant createdAt, final String texturePrefix) {
        this.texturePrefix.set(createdAt, texturePrefix);
    }   

    /**
     * Checks if this player is in range of the destination.
     * @param destination The destination coordinates
     * @param True if in range
     */
    public boolean isInRange(final Coordinates destination, final float rangeSquared) {
        return getDistanceSquared(destination) <= rangeSquared;
    }

    public float getDistanceSquared(final Coordinates destination) {
        return (float) Math.pow(destination.x() - getCoordinates().x(), 2)
        + (float) Math.pow(destination.y() - getCoordinates().y(), 2);
    }
}
