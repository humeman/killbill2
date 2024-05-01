package today.tecktip.killbill.frontend.game.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.objects.renderers.StaticSpriteObjectRenderer;
import today.tecktip.killbill.frontend.ui.Rectangle;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;

/**
 * Represents an object which exists just for decoration.
 * @author cs
 */
public class TextureObject extends GameObject {

    /**
     * X coordinate in tiles (tileX * {@link GlobalGameConfig#GRID_SIZE}).
     */
    private final float tileX;

    /**
     * Y coordinate in tiles (tileY * {@link GlobalGameConfig#GRID_SIZE}).
     */
    private final float tileY;
    
    /**
     * Width in tiles (tileWidth * {@link GlobalGameConfig#GRID_SIZE}).
     */
    private final float tileWidth;

    /**
     * Height in tiles (tileHeight * {@link GlobalGameConfig#GRID_SIZE}).
     */
    private final float tileHeight;

    /**
     * The rectangle where the tile is located.
     */
    private Rectangle rectangle;

    /**
     * Constructs a new texture object.
     * @param tileX Tile X coordinate
     * @param tileY Tile Y coordinate
     * @param tileWidth Tile width
     * @param tileHeight Tile height
     * @param texture Texture to draw
     * @param flags Object flags to use
     */
    public TextureObject(final float tileX, final float tileY, final float tileWidth, final float tileHeight, final Texture texture, final ObjectFlag[] flags) {
        super(new StaticSpriteObjectRenderer(texture), flags);
        this.tileX = tileX;
        this.tileY = tileY;

        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

        updateRectangle();
    }

    /**
     * Refreshes the rectangle location and size.
     */
    private void updateRectangle() {
        this.rectangle = new Rectangle(
            new FixedLocation(tileX * GlobalGameConfig.GRID_SIZE, tileY * GlobalGameConfig.GRID_SIZE),
            new FixedSize(tileWidth * GlobalGameConfig.GRID_SIZE, tileHeight * GlobalGameConfig.GRID_SIZE)
        );
    }

    @Override
    public Rectangle getRectangle() {
        return rectangle;
    }

    @Override
    public void renderTo(final float delta, final SpriteBatch batch) {
        renderer.renderTo(delta, batch, this);
    }

    @Override
    public boolean onInteract(Entity entity) {
        return false;
    }
}
