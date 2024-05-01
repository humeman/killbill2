package today.tecktip.killbill.frontend.game.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.common.maploader.MapDirective.DirectiveType;
import today.tecktip.killbill.common.maploader.directives.ChestDirective;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.items.ItemGenerator;
import today.tecktip.killbill.frontend.game.objects.renderers.StaticSpriteObjectRenderer;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicInteractCommand.BasicInteractCommandData;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicInteractCommand.BasicInteractContext;
import today.tecktip.killbill.frontend.ui.Rectangle;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;

/**
 * Represents a loot table item.
 * @author cs
 */
public class Chest extends GameObject {

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
     * Directive containing a loot table.
     */
    private ChestDirective directive;

    /**
     * Constructs a new texture object.
     * @param tileX Tile X coordinate
     * @param tileY Tile Y coordinate
     * @param tileWidth Tile width
     * @param tileHeight Tile height
     * @param texture Texture to draw
     * @param flags Object flags to use
     * @param directive Chest directive which contains the loot table
     */
    public Chest(final float tileX, final float tileY, final float tileWidth, final float tileHeight, final Texture texture, final ObjectFlag[] flags, final ChestDirective directive) {
        super(new StaticSpriteObjectRenderer(texture), flags);
        this.tileX = tileX;
        this.tileY = tileY;

        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

        this.directive = directive;

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

    public void runUdpInteraction(final BasicInteractCommandData data) {
        if (data.getId() == directive.getId()) {
            // Action 0: use
            if (data.getAction() == 0) {
                if (getRenderer() instanceof StaticSpriteObjectRenderer) {
                    ((StaticSpriteObjectRenderer) getRenderer()).changeTexture(
                        KillBillGame.get().getTextureLoader().get(directive.getOpenTexture())
                    );
                }
                
                flags.remove(ObjectFlag.INTERACTABLE);
            } else {
                Gdx.app.error(Chest.class.getSimpleName(), "Unsupported action type: " + data.getAction());
            }
        }
    }

    @Override
    public boolean onInteract(Entity entity) {
        if (entity != KillBillGame.get().getPlayer()) return false;

        try {
            KillBillGame.get().getUdpClient().getCommandLoader().invokeMethodFor(
                GameType.BASIC,
                MessageDataType.COMMAND_INTERACT)
                .run(
                    KillBillGame.get().getUdpClient(), 
                    new BasicInteractContext(DirectiveType.CHEST, directive.getId(), 0));
        } catch (final MessageFailure e) {
            Gdx.app.error(Chest.class.getSimpleName(), "Message failed.", e);
        }

        if (getRenderer() instanceof StaticSpriteObjectRenderer) {
            ((StaticSpriteObjectRenderer) getRenderer()).changeTexture(
                KillBillGame.get().getTextureLoader().get(directive.getOpenTexture())
            );
        }
        
        flags.remove(ObjectFlag.INTERACTABLE);

        // Grab a loot table item
        final ItemType itemType = directive.rollLootTable();

        final Item item = ItemGenerator.generate(itemType);
        KillBillGame.get().getPlayer().addToInventory(item);

        return true;
    }
}
