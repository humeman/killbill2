package today.tecktip.killbill.frontend.game.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.effects.ResistanceEffect;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.objects.renderers.ObjectRenderer;
import today.tecktip.killbill.frontend.screens.GameScreen;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Rectangle;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;
import today.tecktip.killbill.frontend.util.MathUtil;

/**
 * An entity on the grid (like a player or enemy).
 * @author cs
 */
public class Entity extends GameObject {
    /**
     * The rectangle where the tile is located.
     */
    private Rectangle rectangle;

    /**
     * The currently held item.
     */
    private Item heldItem;

    /**
     * The entity's maximum health.
     */
    protected int maxHealth;

    /**
     * The entity's current health.
     */
    private int health;

    /**
     * Timer since last damage for visual effects.
     */
    private float damageTimer;

    private boolean movedLastFrame;

    private GameObject collidedObject;

    /**
     * Constructs a new Entity (a movable, non-grid object like a player or enemy).
     * <p>
     * The widths and heights are multiplied by {@link GlobalGameConfig#GRID_SIZE} for the actual pixel location.
     * @param x X coordinate
     * @param y Y coordinate
     * @param tileWidth Tile width
     * @param tileHeight Tile height
     * @param texture Texture to draw
     * @param flags Object flags to use
     */
    public Entity(final float x, final float y, final float tileWidth, final float tileHeight, final ObjectRenderer renderer, final ObjectFlag[] flags) {
        super(renderer, flags);
        rectangle = new Rectangle(
            new FixedLocation(x, y),
            new FixedSize(tileWidth * GlobalGameConfig.GRID_SIZE, tileHeight * GlobalGameConfig.GRID_SIZE)
        );

        heldItem = null;

        maxHealth = -1;
        damageTimer = -1;
        movedLastFrame = false;
    }

    public boolean movedLastFrame() {
        return movedLastFrame;
    }

    /**
     * Sets the entity's max health.
     * @param maxHealth Hitpoints or -1 for infinite health
     */
    public void setMaxHealth(final int maxHealth) {
        this.maxHealth = maxHealth;
        health = maxHealth;
    }

    /**
     * Offsets the entity's health. If you want damage effects, use {@link #damage}.
     * @param offset Offset to apply
     */
    public void offsetHealth(final int offset) {
        setHealth(health + offset);
    }

    /**
     * Changes the entity's health. Applies damage effects if lower than before.
     * @param health New health
     */
    public void setHealth(final int health) {
        if (health < this.health) {
            setFlag(ObjectFlag.FROZEN);
            damageTimer = 0;
        }

        this.health = health;
    }

    /**
     * Deals damage to the entity (with red tint and death)
     * @param damageAmount Damage health amount (positive)
     */
    public void damage(final int damageAmount) {
        if (damageTimer >= 0) return; // Already being damaged

        //if invincible, do nothing
        if (KillBillGame.get().getPlayer().hasEffect(ResistanceEffect.class)) {
            return;
        }
        offsetHealth(-1 * damageAmount);
        setFlag(ObjectFlag.FROZEN);
        damageTimer = 0;
    } 

    /**
     * Gets the entity's maximum health.
     * @return Max health
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Gets the entity's current health.
     * @return Health
     */
    public int getHealth() {
        return health;
    }

    /**
     * Changes the player's location with no regard for collisions.
     * @param x New X coordinate
     * @param y New Y coordinate
     */
    public void setLocation(final float x, final float y) {
        movedLastFrame = true;
        rectangle.getLocation().setX(x);
        rectangle.getLocation().setY(y);
        if (renderer != null)
            renderer.updateLocation(this);
    }

    /**
     * Offsets the entity's location.
     * @param xOffset X pixel offset
     * @param yOffset Y pixel offset
     */
    public void move(final float xOffset, final float yOffset, final boolean setAngle) {
        movedLastFrame = true;

        // Change the rotation depending on where they're moving
        if (setAngle) {
            int total = 0;
            int count = 0;
            if (xOffset < 0) {
                total += 270;
                count++;
            } else if (xOffset > 0) {
                total += 90;
                count++;
            }
            if (yOffset < 0) {
                // Up
                count++; 
            } else if (yOffset > 0) {
                total += 180;
                count++;
            }

            if (count != 0) {
                if (xOffset < 0 && yOffset < 0) total = 630;
                setRotation(total / count);
            }
        }

        translateX(xOffset);
        translateY(yOffset);

        renderer.updateLocation(this);
    }

    /**
     * Moves the character in the X direction, checking collisions if necessary.
     * @param xOffset Offset to apply to X
     */
    private void translateX(final float xOffset) {
        // Insignificant movement (virtually 0)
        if (Math.abs(xOffset) < 0.0005f) {
            return;
        }

        if (!hasFlag(ObjectFlag.SOLID)) {
            getRectangle().getLocation().offsetX(xOffset);
            return; // Collisions don't matter
        }

        // Check other objects on the screen
        collidedObject = null;
        GameScreen currentScreen = (GameScreen) KillBillGame.get().getScreen();
        currentScreen.getGameRenderer().forEachObject(
            object -> {
                if (object == this) return false;
                if (object.hasFlag(ObjectFlag.SOLID) && wouldCollideAt(object, getRectangle().getX() + xOffset, getRectangle().getY())) {
                    // We are colliding with something.
                    collidedObject = object;
                    return true;
                }
                return false;
            }
        );
        if (collidedObject != null) {
            // We need to move in a particular step size towards the other object.
            // No object can be half a pixel, so the step size is 1.
            // However, we could be moving forward or backward here.
            int iter = xOffset < 0 ? -1 : 1;

            // Keep track of the coordinate where we would collide. We want these to be single-pixel steps, from their current coordinate
            // (which is rendered as the truncated version of that coordinate), so we'll start there and round up our offset.
            int goal = (int) getRectangle().getX() + (int) Math.ceil(xOffset);
            for (int current = (int) getRectangle().getX(); xOffset < 0 ? current >= goal : current <= goal; current += iter) {
                // Our step increases by one for each of these. See if we hit something. We should.
                if (wouldCollideAt(collidedObject, current, getRectangle().getY())) {
                    // No good! Set our position to one before this location.
                    this.getRectangle().getLocation().setX(current - iter);
                    break;
                }
            }
        } else {
            // Just translate there, this is safe to do
            getRectangle().getLocation().offsetX(xOffset);
        }
    }

    /**
     * Moves the character in the Y direction, checking collisions if necessary.
     * @param xOffset Offset to apply to Y
     */
    private void translateY(final float yOffset) {
        // Insignificant movement (virtually 0)
        if (Math.abs(yOffset) < 0.0005f) {
            return;
        }

        if (!hasFlag(ObjectFlag.SOLID)) {
            getRectangle().getLocation().offsetY(yOffset);
            return; // Collisions don't matter
        }

        // Check other objects on the screen
        collidedObject = null;
        GameScreen currentScreen = (GameScreen) KillBillGame.get().getScreen();
        currentScreen.getGameRenderer().forEachObject(
            object -> {
                if (object == this) return false;
                if (object.hasFlag(ObjectFlag.SOLID) && wouldCollideAt(object, getRectangle().getX(), getRectangle().getY() + yOffset)) {
                    // We are colliding with something.
                    collidedObject = object;
                    return true;
                }
                return false;
            }
        );

        if (collidedObject != null) {
            // We need to move in a particular step size towards the other object.
            // No object can be half a pixel, so the step size is 1.
            // However, we could be moving forward or backward here.
            int iter = yOffset < 0 ? -1 : 1;

            // Keep track of the coordinate where we would collide. We want these to be single-pixel steps, from their current coordinate
            // (which is rendered as the truncated version of that coordinate), so we'll start there and round up our offset.
            int goal = (int) getRectangle().getY() + (int) Math.ceil(yOffset);
            for (int current = (int) getRectangle().getY(); yOffset < 0 ? current >= goal : current <= goal; current += iter) {
                // Our step increases by one for each of these. See if we hit something. We should.
                if (wouldCollideAt(collidedObject, getRectangle().getX(), current)) {
                    // No good! Set our position to one before this location.
                    this.getRectangle().getLocation().setY(current - iter);
                    break;
                }
            }
        } else {
            // Just translate there, this is safe to do
            getRectangle().getLocation().offsetY(yOffset);
        }
    }

    /**
     * Checks if this entity is looking at another object.
     * @param otherObject Object to check
     * @return True if looking at the object and within range.
     */
    public boolean canInteractWith(final GameObject otherObject) {
        // Slowly iterate over the equation for the specified angles
        float x;
        float y;
        for (final int angleOffset : GlobalGameConfig.INTERACT_ANGLES) {
            // Iterate over the looking line up to the reach
            // Start at the edge of the object to make this more accurate
            float distanceOffset = Math.abs(MathUtil.distanceToEdgeAtAngle(getRectangle().getWidth(), getRotation() + 90 + angleOffset));

            for (int i = 0; i <= GlobalGameConfig.REACH_PX + distanceOffset; i += 5) {
                x = lookingLineX(distanceOffset + i, angleOffset);
                y = lookingLineY(distanceOffset + i, angleOffset);

                if (otherObject.getRectangle().containsPoint(x, y)) return true;
            }
        }

        return false;
    }

    /**
     * Interacts with any objects in range.
     */
    public void interact() {
        // Get the screen we're using
        final GameScreen screen = (GameScreen) KillBillGame.get().getCurrentScreen();

        screen.getGameRenderer().forEachObject(
            object -> {
                // Only if it's interactable
                if (!object.hasFlag(ObjectFlag.INTERACTABLE)) return false;

                // No sense in doing this if it's far outside of range
                if (
                    Math.abs(getRectangle().getCenterX() - object.getRectangle().getCenterX()) > GlobalGameConfig.REACH_PX + getRectangle().getWidth() + object.getRectangle().getWidth()
                    || Math.abs(getRectangle().getCenterY() - object.getRectangle().getCenterY()) > GlobalGameConfig.REACH_PX + getRectangle().getHeight() + object.getRectangle().getHeight()
                ) {
                    return false;
                }

                if (canInteractWith(object)) {
                    if (object.onInteract(this)) return true;
                }

                return false;
            }
        );
    }

    /**
     * Changes the currently held item.
     * @param item New held item or null for no item
     */
    public void setHeldItem(final Item item) {
        this.heldItem = item;
    }

    /**
     * Gets the currently held item.
     * @return Held item or null if nothing is held
     */
    public Item getHeldItem() {
        return heldItem;
    }

    @Override
    public Rectangle getRectangle() {
        return rectangle;
    }

    @Override
    public void renderTo(float delta, SpriteBatch batch) {
        // Pre-render tasks: Do damage animations
        boolean red = false;
        if (damageTimer >= 0) {
            // First .5s: KB
            if (damageTimer <= 2f) {
                red = true;
            } else {
                unsetFlag(ObjectFlag.FROZEN);
                red = false;
                damageTimer = -1;
            }

            damageTimer += delta;
        }

        Color oldColor = null;
        if (red) {
            oldColor = batch.getColor().cpy();
            batch.setColor(new Color(1, 0, 0, 1));
        }

        if (heldItem == null)
            renderer.renderTo(delta, batch, this);
        else 
            renderer.renderTo(delta, batch, this, heldItem);

        if (oldColor != null) {
            batch.setColor(oldColor);
        }

        movedLastFrame = false;
    }

    /**
     * Gets the X coordinate of the looking line at the specified distance.
     * @param distance Distance from the player's center
     * @param angleOffset Offsets the angle by a specified number of degrees
     */
    public float lookingLineX(float distance, int angleOffset) {
        // Get the real angle (angles are 90 degrees off here)
        int angle = (getRotation() + angleOffset - 90) % 360;
        if (angle < 0) angle += 360;
        double angleRad = Math.toRadians((double) angle);

        // Get the cosine component
        float cos = (float) Math.cos(angleRad);

        // Plug it in
        return getRectangle().getCenterX() + distance * cos;
    }

    /**
     * Gets the Y coordinate of the looking line at the specified distance.
     * @param distance Distance from the player's center
     * @param angleOffset Offsets the angle by a specified number of degrees
     */
    public float lookingLineY(float distance, int angleOffset) {
        // Get the real angle (angles are 90 degrees off here)
        int angle = (getRotation() + angleOffset - 90) % 360;
        if (angle < 0) angle += 360;
        double angleRad = Math.toRadians((double) angle);

        // Get the sine component
        float sin = (float) Math.sin(angleRad);

        // Plug it in
        return getRectangle().getCenterY() + distance * sin;
    }

    @Override
    public boolean onInteract(final Entity entity) {
        // Do nothing
        return false;
    }
    
}
