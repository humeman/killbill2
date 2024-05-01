package today.tecktip.killbill.frontend.gameserver.game.basic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.misc.TimestampedValue;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;
import today.tecktip.killbill.frontend.gameserver.game.LocalGameState;
import today.tecktip.killbill.frontend.gameserver.game.LocalGameUserState;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangeOtherPlayerStateCommand.ChangeOtherPlayerStateInvokeContext;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangeOtherPlayerStateCommand.OtherPlayerStateFieldFilter;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangePlayerStateCommand.ChangePlayerStateInvokeContext;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangePlayerStateCommand.PlayerStateFieldFilter;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicRecvPlayerStateCommand.GetPlayerStateInvokeContext;
import today.tecktip.killbill.frontend.http.requests.UserRequests;
import today.tecktip.killbill.frontend.http.requests.UserRequests.GetUserRequestBody;
import today.tecktip.killbill.frontend.http.requests.data.User;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;

/**
 * User state for the BASIC game type.
 * @author cs
 */
public class BasicLocalGameUserState extends LocalGameUserState {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicLocalGameUserState.class);

    private TimestampedValue<Coordinates> coords;

    private TimestampedValue<Integer> rotation;

    private TimestampedValue<BasicPlayerType> playerType;

    private TimestampedValue<Integer> health;

    private TimestampedValue<Integer> maxHealth;

    private TimestampedValue<String> heldItemTexture;

    private TimestampedValue<String> texturePrefix;

    private int damage;

    private boolean isReady;

    private User user;

    private boolean userRequestSent;

    private FixedSize coordinateDiff;

    private BasicPlayerType originalTeam;

    /**
     * Notes any fields updated locally that have yet to be synced.
     */
    private List<PlayerStateFieldFilter> updatedFields;

    /**
     * Constructs a new user state for the BASIC game type.
     * <p>
     * Key is expected to be validated before instantiation.
     * @param parent Parent game state
     * @param userId Game user ID as represented in the database
     */
	public BasicLocalGameUserState(final LocalGameState parent, final UUID userId) {
		super(parent, userId);

        coords = new TimestampedValue<>(null);
        playerType = new TimestampedValue<>(null);
        heldItemTexture = new TimestampedValue<>(null);
        texturePrefix = new TimestampedValue<>(null);
        rotation = new TimestampedValue<>(-1);
        health = new TimestampedValue<>(-1);
        maxHealth = new TimestampedValue<>(-1);
        isReady = false;
        user = null;
        userRequestSent = false;
        updatedFields = new ArrayList<>(PlayerStateFieldFilter.values().length);
        coordinateDiff = null;
        damage = -1;
        originalTeam = null;
	}

    /**
     * Checks if game state has been received for this player.
     * A player may have been seen before by the UDP client (from a message or
     *  because of a game state change), but no state information has been received
     *  yet. This ensures that the state is not used when that is the case.
     * @return True if ready
     */
    public boolean isReady() {
        return isReady && user != null;
    }

    /**
     * Sets that a client is ready for use in the game.
     */
    public void setReady() {
        isReady = true;
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
     * Sets the user's current coordinates according to a value generated by the client.
     * @param newCoords New coordinates
     */
    public void setCoordinates(final Coordinates newCoords) {
        if (coords.get() != null) {
            if (coordinateDiff == null) {
                coordinateDiff = new FixedSize((float) (coords.get().x() - newCoords.x()), (float) (coords.get().y() - newCoords.y()));
            } else {
                coordinateDiff.offsetWidth((float) (coords.get().x() - newCoords.x()));
                coordinateDiff.offsetHeight((float) (coords.get().y() - newCoords.y()));
            }
            if (Math.abs(coordinateDiff.getWidth()) >= GlobalGameConfig.MIN_MOVE_PER_SEND || Math.abs(coordinateDiff.getHeight()) >= GlobalGameConfig.MIN_MOVE_PER_SEND) {
                updatedFields.add(PlayerStateFieldFilter.COORDINATES);
                coordinateDiff = null;
            }
        } else {
            updatedFields.add(PlayerStateFieldFilter.COORDINATES);
        }
        this.coords.set(newCoords);
    }

    /**
     * Sets the user's current coordinates according to an incoming UDP message.
     * @param createdAt When the new value was generated by the other end
     * @param coords New coordinates
     */
    public void setCoordinates(final Instant createdAt, final Coordinates coords) {
        wasUpdated = true;
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
     * Sets the user's rotation according to a value generated by the client.
     * @param newRotation New rotation, 0 to 359 inclusive
     */
    public void setRotation(final int newRotation) {
        if (newRotation != this.rotation.get()) updatedFields.add(PlayerStateFieldFilter.ROTATION);
        this.rotation.set(newRotation);
    }

    /**
     * Sets the user's rotation according to an incoming UDP message.
     * @param createdAt When the new value was generated by the other end
     * @param rotation New rotation
     */
    public void setRotation(final Instant createdAt, final int rotation) {
        wasUpdated = true;
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
     * Sets the user's player type according to a value generated by the client.
     * @param playerType New player type
     */
    public void setPlayerType(final BasicPlayerType playerType) {
        if (!playerType.equals(BasicPlayerType.SPECTATOR)) originalTeam = playerType;

        if (!playerType.equals(this.playerType.get())) updatedFields.add(PlayerStateFieldFilter.PLAYER_TYPE);
        this.playerType.set(playerType);
    }

    /**
     * Sets the user's player type according to an incoming UDP message.
     * @param createdAt When the new value was generated by the other end
     * @param playerType New player type
     */
    public void setPlayerType(final Instant createdAt, final BasicPlayerType playerType) {
        if (!playerType.equals(BasicPlayerType.SPECTATOR)) originalTeam = playerType;
        this.playerType.set(createdAt, playerType);
    }   
    
    public BasicPlayerType getOriginalTeam() {
        return originalTeam;
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
     * Sets the user's health according to a value generated by the client.
     * @param health New health
     */
    public void setHealth(final int health) {
        if (health != this.health.get()) updatedFields.add(PlayerStateFieldFilter.HEALTH);

        // Going down? If so, store the damage
        if (health < this.health.get()) {
            damage = this.health.get() - health;
        }

        this.health.set(health);
    }

    /**
     * Sets the user's health according to an incoming UDP message.
     * @param createdAt When the new value was generated by the other end
     * @param health New health
     */
    public void setHealth(final Instant createdAt, final int health) {
        wasUpdated = true;
        this.health.set(createdAt, health);
    }   

    /**
     * Gets the user's maximum health health.
     * @return Max health
     */
    @JsonProperty("maxHealth")
    public int getMaxHealth() {
        return maxHealth.get();
    }

    /**
     * Sets the user's max health according to a value generated by the client.
     * @param maxHealth New max health
     */
    public void setMaxHealth(final int maxHealth) {
        if (maxHealth != this.maxHealth.get()) updatedFields.add(PlayerStateFieldFilter.MAX_HEALTH);
        this.maxHealth.set(maxHealth);
    }

    /**
     * Sets the user's max health according to an incoming UDP message.
     * @param createdAt When the new value was generated by the other end
     * @param maxHealth New max health
     */
    public void setMaxHealth(final Instant createdAt, final int maxHealth) {
        wasUpdated = true;
        this.maxHealth.set(createdAt, maxHealth);
    }   

    /**
     * Gets the last dealt damage.
     * @return Damage or -1
     */
    public int getDamage() {
        return damage;
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
        if (getHeldItemTexture() == null ||
            !getHeldItemTexture().equals(heldItemTexture)) {
            updatedFields.add(PlayerStateFieldFilter.HELD_ITEM_TEXTURE);
        }
        this.heldItemTexture.set(heldItemTexture);
    }

    /**
     * Sets the user's held item texture if the specified timestamp is after the current value's.
     * @param createdAt When the new value was generated by the other end
     * @param health New health
     */
    public void setHeldItemTexture(final Instant createdAt, final String heldItemTexture) {
        wasUpdated = true;
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

    @Override
    public User getUser() {
        if (user == null) throw new CatastrophicException("Attempted access of API data before retrieved");
        return user;
    }

    @Override
    public void getPlayerData(final boolean getUdp) {
        // Get API data if unavailable (and not yet sent for)
        if (user == null && !userRequestSent) {
            userRequestSent = true;
            UserRequests.getUser(
                new GetUserRequestBody(userId, null), 
                response -> {
                    user = response.user();
                }, 
                error -> {
                    userRequestSent = false;
                    throw new CatastrophicException("API error while retrieving connected user: ", error);
                });
        }

        // Update UDP data
        if (getUdp) {
            try {
                ClientMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_GET_PLAYER_STATE)
                    .run(
                        ClientMessageHandler.get(), 
                        new GetPlayerStateInvokeContext(userId)
                    );
            } catch (final MessageFailure e) {
                LOGGER.warn("Unable to send request for player data for userId={}.", userId, e);
            }
        }
    }

    /**
     * Syncs the player's local state to the UDP server if anything has changed.
     * Only should be called if this is the active player's state.
     * @throws MessageFailure Failed to send
     */
    public void sync() throws MessageFailure {
        if (updatedFields.size() == 0) return;
        if (!isReady()) return;

        ClientMessageHandler.get().getCommandLoader().invokeMethodFor(parent.getGame().config().getGameType(), MessageDataType.COMMAND_CHANGE_PLAYER_STATE)
            .run(
                ClientMessageHandler.get(),
                new ChangePlayerStateInvokeContext(this, updatedFields)
            );
        updatedFields.clear();
    }

    /**
     * Syncs the state to UDP as a dummy player (another player in the game).
     * @throws MessageFailure Failed to send
     */
    public void syncAsOther() throws MessageFailure {
        if (updatedFields.size() == 0) return;
        if (!isReady()) return;

        // The only applicable change right now is health (damage)
        if (!updatedFields.contains(PlayerStateFieldFilter.HEALTH)) return;

        // Check if we dealt damage
        if (damage > 0) {
            // Sync
            ClientMessageHandler.get().getCommandLoader().invokeMethodFor(parent.getGame().config().getGameType(), MessageDataType.COMMAND_CHANGE_OTHER_PLAYER_STATE)
                .run(
                    ClientMessageHandler.get(),
                    new ChangeOtherPlayerStateInvokeContext(this, List.of(OtherPlayerStateFieldFilter.DAMAGE))
                );
            updatedFields.clear();

            damage = -1;
        }
        // We can't heal someone else, so no other case matters
        updatedFields.remove(PlayerStateFieldFilter.HEALTH);
    }
}
