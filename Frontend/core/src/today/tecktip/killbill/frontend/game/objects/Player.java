package today.tecktip.killbill.frontend.game.objects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.KillBillGame.InputType;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.config.Keybinds.KeyType;
import today.tecktip.killbill.frontend.game.effects.Effect;
import today.tecktip.killbill.frontend.game.effects.SpeedEffect;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.objects.renderers.MovementBasedRenderer;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameUserState;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicDroppedItemCommand.CreateDroppedItemContext;
import today.tecktip.killbill.frontend.screens.GameScreen;
import today.tecktip.killbill.frontend.screens.game.UdpGameScreen;

/**
 * A player (as represented on the game grid) in the game.
 * If you're looking for another player in the game, they're represented
 *  as {@link DummyPlayer}. This is the player that is being controlled
 *  by this instance.
 * @author cs
 */
public class Player extends Entity {
    /**
     * The user state for this player.
     */
    private BasicLocalGameUserState userState;

    /**
     * The multiplier for movement speed.
     */
    private float movementMultiplier;

    /**
     * If false, the object has not yet been rendered.
     */
    private boolean hasRendered;

    /**
     * Movement types to apply on the next frame.
     */
    private List<KeyType> movementTypes;

    /**
     * Inventory slots.
     */
    private List<Item> inventory;

    /**
     * The effects the player has.
     */
    private List<Effect> effects;

    /**
     * Current item index.
     */
    private int heldItemIndex;

    private boolean spectator;

    /**
     * Constructs a new Player.
     * <p>
     * @param x Pixel X coordinate
     * @param y Pixel Y coordinate
     * @param tileWidth Tile width
     * @param tileHeight Tile height
     * @param texture Texture to draw
     * @param userState The user state for this player
     */
    public Player(final float x, final float y, final float tileWidth, final float tileHeight, final BasicLocalGameUserState userState) {
        super(
            x, 
            y, 
            tileWidth, 
            tileHeight, 
            null, 
            new ObjectFlag[] {ObjectFlag.PROCESSES_INPUT, ObjectFlag.NEEDS_PRE_RENDER_UPDATE, ObjectFlag.SOLID, ObjectFlag.ATTACKABLE}
        );
        if (userState != null) setRenderer(new MovementBasedRenderer(this, userState.getTexturePrefix()));
        else setRenderer(new MovementBasedRenderer(this, "players_gates"));
        setMaxHealth(GlobalGameConfig.PLAYER_HEALTH);
        this.userState = userState;
        this.movementMultiplier = 1.0f;
        movementTypes = new ArrayList<>(5);
        effects = new ArrayList<>();
        hasRendered = false;

        inventory = new ArrayList<>(GlobalGameConfig.INVENTORY_SIZE);
        for (int i = 0; i < GlobalGameConfig.INVENTORY_SIZE; i++) {
            inventory.add(null);
        }
        heldItemIndex = 0;
        spectator = false;
    }

    /**
     * Applies an effect to the player.
     * @param effect The effect to apply
     */
    public void applyEffect(final Effect effect) {
        // Check if this is a duplicate
        for (final Effect currentEffect : effects) {
            if (currentEffect.getClass().equals(effect.getClass())) {
                // If the duration is longer, switch it out
                if (currentEffect.getRemainingDuration() < effect.getRemainingDuration()) {
                    effects.remove(currentEffect);
                } else {
                    // The current one is longer. Ignore this
                    return;
                }
            }
        }

        effects.add(effect);
    }

    /**
     * Gets the list of the user's effects. Do not modify this.
     * @return List of user's active effects
     */
    public List<Effect> getEffects() {
        return effects;
    }

    /**
     * Gets the first active instance of the specified effect type.
     * @param effectClass Class of the effect to check for 
     * @return The available effect or null
     */
    @SuppressWarnings("unchecked") // Liar
    public <T extends Effect> T getEffect(final Class<T> effectClass) {
        for (final Effect effect : effects) {
            if (effect.getClass().equals(effectClass)) return (T) effect;
        }
        return null;
    }

    /**
     * Checks if the player has an active instance of the specified effect type.
     * @param effectClass Class of the effect to check for 
     * @return True if the player has the effect
     */
    public boolean hasEffect(final Class<? extends Effect> effectClass) {
        for (final Effect effect : effects) {
            if (effect.getClass().equals(effectClass)) return true;
        }
        return false;
    }

    /**
     * Changes the mutlipler for the player's movement speed. Only effective if this is the active player.
     * @param movementMultiplier Movement multiplier. 1.0 = regular speed.
     */
    public void setMovementMultiplier(final float movementMultiplier) {
        this.movementMultiplier = movementMultiplier;
    }

    /**
     * Sets a movement type for the next render.
     * @param type One of UP, DOWN, LEFT, RIGHT, or SHIFT.
     */
    public void setMovementType(final KeyType type) {
        if (movementTypes.contains(type)) return;
        movementTypes.add(type);
    }

    /**
     * Unsets a movement type.
     * @param type One of UP, DOWN, LEFT, RIGHT, or SHIFT.
     */
    public void removeMovementType(final KeyType type) {
        movementTypes.remove(type);
    }

    public void setSpectator() {
        spectator = true;
        unsetFlag(ObjectFlag.SOLID);
        removeItem(heldItemIndex);
        setHeldItem(heldItemIndex);
    }

    @Override
    public boolean processInput(final int key) {
        return false;
    }

    @Override
    public void setHealth(final int health) {
        super.setHealth(health);

        if (userState != null) {
            userState.setHealth(health);
            try {
                userState.sync();
            } catch (final MessageFailure e) {
                Gdx.app.error(getClass().getSimpleName(), "Failed to sync new health.", e);
            }
        }

        // If dead, show the die screen
        if (health <= 0) {
            if (KillBillGame.get().getCurrentScreen() instanceof UdpGameScreen) {
                ((UdpGameScreen) KillBillGame.get().getCurrentScreen()).die();
            } else setSpectator();
        }
    }

    public void setHealthNoSync(final int health) {
        super.setHealth(health);

        // If dead, show the die screen
        if (health <= 0) {
            if (KillBillGame.get().getCurrentScreen() instanceof UdpGameScreen) {
                ((UdpGameScreen) KillBillGame.get().getCurrentScreen()).die();
            } else setSpectator();
        }
    }

    @Override
    public void beforeRender(final float delta) {
        // Update coordinates
        boolean forceUpdate = false;
        if (userState != null && userState.isReady() && userState.wasUpdated()) {
            setLocation(
                (float) userState.getCoordinates().x() * GlobalGameConfig.GRID_SIZE,
                (float) userState.getCoordinates().y() * GlobalGameConfig.GRID_SIZE);

            maxHealth = userState.getMaxHealth();
            setHealthNoSync(userState.getHealth());
            System.out.println("Changed health.");
            if (renderer instanceof MovementBasedRenderer) {
                ((MovementBasedRenderer) renderer).changeTexture(userState.getTexturePrefix());
            }
            forceUpdate = true;
            userState.clearUpdate();
        }

        // Check effects
        List<Effect> toRemove = null;
        for (final Effect effect : effects) {
            if (effect.update(delta)) {
                if (toRemove == null) toRemove = new ArrayList<>();
                toRemove.add(effect);
            }
        }

        if (toRemove != null) {
            for (final Effect effect : toRemove) {
                effects.remove(effect);
            }
        }

        // Perform movement
        boolean paused = false;
        if (KillBillGame.get().getCurrentScreen() instanceof UdpGameScreen) {
            if (((UdpGameScreen) KillBillGame.get().getCurrentScreen()).isPaused()) paused = true;
        }

        if (!paused && KillBillGame.get().getInputType().equals(InputType.KEYBOARD_MOUSE)) {
            movementTypes.clear();
            if (KillBillGame.get().getKeybinds().isKeyPressed(KeyType.UP)) {
                movementTypes.add(KeyType.UP);
            }
            if (KillBillGame.get().getKeybinds().isKeyPressed(KeyType.DOWN)) {
                movementTypes.add(KeyType.DOWN);
            }
            if (KillBillGame.get().getKeybinds().isKeyPressed(KeyType.RIGHT)) {
                movementTypes.add(KeyType.RIGHT);
            }
            if (KillBillGame.get().getKeybinds().isKeyPressed(KeyType.LEFT)) {
                movementTypes.add(KeyType.LEFT);
            }
            if (KillBillGame.get().getKeybinds().isKeyPressed(KeyType.SPRINT)) {
                movementTypes.add(KeyType.SPRINT);
            }

            if (!spectator) {
                // Inventory slots
                for (int i = 0; i < GlobalGameConfig.INVENTORY_SIZE; i++) {
                    if (KillBillGame.get().getKeybinds().isKeyJustPressed(GlobalGameConfig.INVENTORY_KEYS[i])) {
                        setHeldItem(i);
                        if (getHeldItem() != null)
                            getHeldItem().resetCooldown();
                    }
                }
    
                // Interactions
                if (!spectator && KillBillGame.get().getKeybinds().isKeyJustPressed(KeyType.INTERACT)) {
                    interact();
                }
    
                // Item use
                if (KillBillGame.get().getKeybinds().isKeyJustPressed(KeyType.USE)) {
                    use();
                }
    
                if (KillBillGame.get().getKeybinds().isKeyJustPressed(KeyType.DROP)) {
                    drop();
                }
            }
        }

        // Quick item sanity check, since we could start with an item in hand
        if (userState != null && getHeldItem() != null && userState.getHeldItemTexture() == null) {
            userState.setHeldItemTexture(getHeldItem().getHeldTextureName());
        }

        if (movementTypes.size() != 0 || forceUpdate) move(delta);
    }

    /**
     * Moves the player using the movement types currently set.
     * @param delta Time since last render
     */
    public void move(final float delta) {
        if (getUserState() != null && !getUserState().isReady()) return;

        float xOffset = 0;
        float yOffset = 0;
        if (movementTypes.contains(KeyType.UP)) {
            yOffset += movementMultiplier * delta * GlobalGameConfig.PLAYER_PIXELS_PER_SECOND;
        }
        if (movementTypes.contains(KeyType.DOWN)) {
            yOffset -= movementMultiplier * delta * GlobalGameConfig.PLAYER_PIXELS_PER_SECOND;
        }
        if (movementTypes.contains(KeyType.RIGHT)) {
            xOffset += movementMultiplier * delta * GlobalGameConfig.PLAYER_PIXELS_PER_SECOND;
        }
        if (movementTypes.contains(KeyType.LEFT)) {
            xOffset -= movementMultiplier * delta * GlobalGameConfig.PLAYER_PIXELS_PER_SECOND;
        }

        if (movementTypes.contains(KeyType.SPRINT)) {
            xOffset *= GlobalGameConfig.SPRINT_MULTIPLIER;
            yOffset *= GlobalGameConfig.SPRINT_MULTIPLIER;
        }
        
        if (KillBillGame.get().getPlayer().hasEffect(SpeedEffect.class)) {
            xOffset *= getEffect(SpeedEffect.class).getSpeedMultiplier();
            yOffset *= getEffect(SpeedEffect.class).getSpeedMultiplier();
        }

        // If both are set, the player will move faster than we want. We need to offset this.
        /*
         *   |\
         *   |  \  sqrt(2) * x
         * x |    \
         *   |______\
         *      x
         * 
         * Assuming we want x to be the total, we just divide each factor by sqrt(2).
         */
        if (xOffset != 0 && yOffset != 0) {
            xOffset /= Math.sqrt(2);
            yOffset /= Math.sqrt(2);
        }

        move(xOffset, yOffset, true);

        if (xOffset != 0 || yOffset != 0 || !hasRendered) {
            if (KillBillGame.get().getScreen() instanceof GameScreen) {
                ((GameScreen) KillBillGame.get().getScreen()).setCameraLocation(
                    getRectangle().getX() + getRectangle().getWidth() / 2,
                    getRectangle().getY() + getRectangle().getHeight() / 2
                );
            }
            hasRendered = true;
        }

        // Send state update
        if (userState != null) {
            userState.setCoordinates(new Coordinates(getRectangle().getLocation().getTileX(), getRectangle().getLocation().getTileY()));
            userState.setRotation(getRotation());
        }
    }

    /**
     * Adds an item to the next available slot in the inventory.
     * @param item Item to add
     * @return True if a slot was available and the item was picked up
     */
    public boolean addToInventory(final Item item) {
        if (spectator) return false;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i) == null) {
                inventory.set(i, item);

                if (i == heldItemIndex) {
                    setHeldItem(heldItemIndex); // Refresh
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Removes an item at an index from the player's index.
     * @param index Index to remove from
     * @return Removed item
     */
    public Item removeItem(final int index) {
        Item oldItem = inventory.set(index, null);
        if (index == heldItemIndex) {
            setHeldItem(heldItemIndex); // Refresh
        }

        return oldItem;
    }

    /**
     * Gets the item in the inventory at the specified index.
     * @param index Index of item
     * @return Item or null if no item there
     */
    public Item getItem(final int index) {
        return inventory.get(index);
    }

    /**
     * Gets the index of the held item in the inventory.
     * @return Held item index
     */
    public int getHeldItemIndex() {
        return heldItemIndex;
    }

    /**
     * Gets the index of the specified item.
     * @param item Item to grab
     * @return Index
     */
    public int getIndexOfItem(final Item item) {
        return inventory.indexOf(item);
    }

    /**
     * Changes the held item to a specific index.
     * @param index Index to use
     */
    public void setHeldItem(final int index) {
        if (spectator) return;
        heldItemIndex = index;
        super.setHeldItem(inventory.get(index));

        // Update state
        if (userState != null) {
            // Interesting...
            if (getHeldItem() == null) {
                userState.setHeldItemTexture("none");
            } else {
                userState.setHeldItemTexture(getHeldItem().getHeldTextureName());
            }
        }
    }

    /**
     * Uses the held item.
     */
    public void use() {
        if (spectator) return;
        if (getHeldItem() == null) return;
        getHeldItem().runUse(this);
    }

    public void drop() {
        if (spectator) return;
        if (getHeldItem() == null) return;

        try {
            KillBillGame.get().getUdpClient().getCommandLoader().invokeMethodFor(
                GameType.BASIC,
                MessageDataType.COMMAND_CREATE_DROPPED_ITEM)
                .run(
                    KillBillGame.get().getUdpClient(),
                    new CreateDroppedItemContext(getHeldItem())
                );
        } catch (final MessageFailure e) {
            Gdx.app.log(getClass().getSimpleName(), "Failed to send item drop.", e);
            return;
        }

        DroppedItem dItem = new DroppedItem(
            getRectangle().getCenterX() / GlobalGameConfig.GRID_SIZE, getRectangle().getCenterY() / GlobalGameConfig.GRID_SIZE, 0.5f, 0.5f,
            getHeldItem().getHeldTexture(),
            ((BasicLocalGameState) KillBillGame.get().getUdpClient().getGameState()).getRecentItem()
        );

        removeItem(heldItemIndex);
        userState.setHeldItemTexture("none");

        ((GameScreen) KillBillGame.get().getCurrentScreen()).getGameRenderer().addObject(dItem);
    }

    /**
     * Gets the user state this player represents.
     * @return The user state for this player.
     */
    public BasicLocalGameUserState getUserState() {
        return userState;
    }
}
