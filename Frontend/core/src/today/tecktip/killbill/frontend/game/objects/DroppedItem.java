package today.tecktip.killbill.frontend.game.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.items.ItemGenerator;
import today.tecktip.killbill.frontend.game.objects.renderers.StaticSpriteObjectRenderer;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalDroppedItemState;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicDroppedItemCommand.RemoveDroppedItemContext;
import today.tecktip.killbill.frontend.screens.GameScreen;
import today.tecktip.killbill.frontend.ui.Rectangle;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;

/**
 * Represents an object which a player dropped.
 * @author cs
 */
public class DroppedItem extends GameObject {

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

    private final BasicLocalDroppedItemState item;

    /**
     * Constructs a new texture object.
     * @param tileX Tile X coordinate
     * @param tileY Tile Y coordinate
     * @param tileWidth Tile width
     * @param tileHeight Tile height
     * @param texture Texture to draw
     * @param flags Object flags to use
     */
    public DroppedItem(final float tileX, final float tileY, final float tileWidth, final float tileHeight, final Texture texture, final BasicLocalDroppedItemState item) {
        super(new StaticSpriteObjectRenderer(texture), new ObjectFlag[]{ObjectFlag.INTERACTABLE});
        this.tileX = tileX;
        this.tileY = tileY;

        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

        this.item = item;

        updateRectangle();
    }

    public BasicLocalDroppedItemState getItem() {
        return item;
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
        // Pick up the item
        Item newItem = ItemGenerator.generate(item.getType());
        newItem.setQuantity(item.getQuantity());

        if (KillBillGame.get().getPlayer().addToInventory(newItem)) {
            // Remove the item
            GameScreen screen = (GameScreen) KillBillGame.get().getCurrentScreen();
            screen.getGameRenderer().removeObject(this);

            // Send out UDP state change
            try {
                KillBillGame.get().getUdpClient().getCommandLoader().invokeMethodFor(
                    GameType.BASIC, MessageDataType.COMMAND_REMOVE_DROPPED_ITEM
                ).run(
                    KillBillGame.get().getUdpClient(),
                    new RemoveDroppedItemContext(item.getId())
                );
            } catch (final MessageFailure e) {
                Gdx.app.error(getClass().getSimpleName(), "Failed to send dropped item removal. Will not be kept", e);
                KillBillGame.get().getPlayer().removeItem(KillBillGame.get().getPlayer().getIndexOfItem(newItem));
            }

            return true;
        }

        return false;
    }
}
