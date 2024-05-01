package today.tecktip.killbill.frontend.game.objects.renderers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.objects.Entity;
import today.tecktip.killbill.frontend.game.objects.GameObject;

/**
 * Draws a static (unchanging) sprite as the texture for the object.
 * @author cs
 */
public class MovementBasedRenderer extends ObjectRenderer {
    /**
     * Held item texture width
     */
    private float itemWidth;

    /**
     * Held item texture X
     */
    private float itemX;

    /**
     * Held item texture Y
     */
    private float itemY;

    /**
     * Held item texture
     */
    private Texture heldItemTexture;

    /**
     * Held item texture region
     */
    private TextureRegion heldItemRegion;

    private TextureRegion stillTexture;
    private TextureRegion holdTexture;
    private TextureRegion walk1Texture;
    private TextureRegion walkHold1Texture;
    private TextureRegion walk2Texture;
    private TextureRegion walkHold2Texture;

    private float walkSwapTimer;
    private boolean walkState;

    private final Entity entity;

    private String texturePrefix;

    /**
     * Constructs a new static sprite object renderer.
     * @param texture Texture to draw
     */
    public MovementBasedRenderer(final Entity entity, final String texturePrefix) {
        heldItemTexture = null;
        heldItemRegion = null;
        this.entity = entity;
        this.texturePrefix = texturePrefix;
        populateTextures();
    }

    private void populateTextures() {
        stillTexture = new TextureRegion(KillBillGame.get().getTextureLoader().get(texturePrefix + "_still"));
        holdTexture = new TextureRegion(KillBillGame.get().getTextureLoader().get(texturePrefix + "_hold"));
        walk1Texture = new TextureRegion(KillBillGame.get().getTextureLoader().get(texturePrefix + "_walk_1"));
        walkHold1Texture = new TextureRegion(KillBillGame.get().getTextureLoader().get(texturePrefix + "_walk_hold_1"));
        walk2Texture = new TextureRegion(KillBillGame.get().getTextureLoader().get(texturePrefix + "_walk_2"));
        walkHold2Texture = new TextureRegion(KillBillGame.get().getTextureLoader().get(texturePrefix + "_walk_hold_2"));
    }

    /**
     * Updates the held item location and rotation.
     * @param entity Entity this is rendering
     */
    public void updateLocation(final Entity entity) {
        // Origin X and Y
        float x0 = entity.getRectangle().getX() + entity.getRectangle().getWidth() / 2;
        float y0 = entity.getRectangle().getY() + entity.getRectangle().getHeight() / 2;

        // We want to offset the item based on angles, so it always appears to be fixed
        //  in the entity's hand.
        // Our target offset angle:
        // (This could wrap around the circle, but that'll mess up our rotation, so mod 360)
        int itemAngle = (entity.getRotation() + 240) % 360;
        if (itemAngle < 0) itemAngle += 360;
        itemAngle %= 360;
        // Convert that to radians since that's what Math.cos and Math.sin use
        double itemAngleRad = Math.toRadians((double) itemAngle);

        // Sample of how this looks:
        /*
         *      s
         *   ______X  <-- Item goes here
         *   |    /
         * c |   /
         *   |  / <-- distance from user
         *   | / 
         *   |/ <-- theta = 60 deg
         *   U  <-- User
         */

        // Find our cos and sin values
        double cos = Math.cos(itemAngleRad);
        double sin = Math.sin(itemAngleRad);

        // Our overall distance depends on how big the user is (we want it right at the corner of the object).
        // Use pythagorean thm to find that.
        double distance = Math.sqrt(Math.pow(entity.getRectangle().getWidth() / 2, 2) + Math.pow(entity.getRectangle().getHeight() / 2, 2));

        itemWidth = entity.getRectangle().getWidth() * 3 / 4;

        // We just found the center of the object, so subtract half its width to get the bottom left.
        itemX = (float) ((x0 + (distance * cos)) - itemWidth / 2);
        itemY = (float) ((y0 + (distance * sin)) - itemWidth / 2);
    }

    @Override
    public void renderTo(final float delta, final SpriteBatch batch, final GameObject object) {
        if (texturePrefix == null) return;

        // Decide on a texture
        TextureRegion region;
        if (!entity.movedLastFrame()) {
            region = stillTexture;
        } else {
            // Default is 0
            walkSwapTimer += delta;

            if (walkSwapTimer >= 0.5f) {
                walkState = !walkState;
                walkSwapTimer = 0;
            }

            region = walkState ? walk1Texture : walk2Texture;
        }

        // Player
        batch.draw(
            region, 
            object.getRectangle().getX(), 
            object.getRectangle().getY(),
            object.getRectangle().getWidth() / 2,
            object.getRectangle().getHeight() / 2,
            object.getRectangle().getWidth(), 
            object.getRectangle().getHeight(),
            1f,
            1f,
            (float) object.getRotation() + 180
        );
    }

    @Override
    public void renderTo(final float delta, final SpriteBatch batch, final GameObject object, final Item item) {
        if (texturePrefix == null) return;

        // Player
        TextureRegion region;
        if (!entity.movedLastFrame()) {
            region = holdTexture;
        } else {
            // Default is 0
            walkSwapTimer += delta;

            if (walkSwapTimer >= 0.5f) {
                walkState = !walkState;
                walkSwapTimer = 0;
            }

            region = walkState ? walkHold1Texture : walkHold2Texture;
        }

        batch.draw(
            region, 
            object.getRectangle().getX(), 
            object.getRectangle().getY(), 
            object.getRectangle().getWidth() / 2,
            object.getRectangle().getHeight() / 2,
            object.getRectangle().getWidth(), 
            object.getRectangle().getHeight(),
            1f,
            1f,
            (float) object.getRotation() + 180
        );

        // Item
        if (item.getHeldTexture() != heldItemTexture) {
            heldItemTexture = item.getHeldTexture();
            heldItemRegion = new TextureRegion(item.getHeldTexture());
        }

        batch.draw(
            heldItemRegion, 
            itemX, 
            itemY,
            itemWidth / 2,
            itemWidth / 2,
            itemWidth,
            itemWidth,
            1f,
            1f,
            (float) object.getRotation() + 270
        );
    }

    /**
     * Updates the texture for this item.
     * @param texture New texture
     */
    public void changeTexture(final String texturePrefix) {
        if (texturePrefix.equals(this.texturePrefix)) return;
        this.texturePrefix = texturePrefix;

        populateTextures();
    }
}
