package today.tecktip.killbill.frontend.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.objects.Player;
import today.tecktip.killbill.frontend.resources.FontLoader;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.elements.Label;

/**
 * An extension which draws a player's inventory to the screen
 * @author cs
 */
public class InventoryDisplay extends RendererExtension {

    /**
     * The player to grab the inventory of.
     */
    private Player player;

    /**
     * Unselected bg texture behind the items in the inventory bar
     */
    private Texture unselectedBackgroundTexture;

    /**
     * Selected bg texture behind the items in the inventory bar
     */
    private Texture selectedBackgroundTexture;

    /**
     * Slightly transparent square for cooldowns
     */
    private Texture cooldownTexture;

    /**
     * The font builder for item quantity fonts.
     */
    private FontLoader.Builder quantityFontBuilder;

    /**
     * The font builder for item name fonts.
     */
    private FontLoader.Builder nameFontBuilder;

    /**
     * Label for the item's name.
     */
    private Label itemNameLabel;

    /**
     * Currently held item.
     */
    private Item currentHeldItem;

    /**
     * Item name label fade time in seconds
     */
    private float fadeTime;

    /**
     * Creates a new debug display.
     */
    public InventoryDisplay(final Player player) {
        super(true);
        unselectedBackgroundTexture = KillBillGame.get().getTextureLoader().get("ui_inventory_bg_unselected");
        selectedBackgroundTexture = KillBillGame.get().getTextureLoader().get("ui_inventory_bg_selected");
        cooldownTexture = KillBillGame.get().getTextureLoader().get("ui_cooldown_square");
        this.player = player;

        quantityFontBuilder = KillBillGame.get().getFontLoader().newBuilder("main")
            .setColor(new Color(1, 1, 1, 1))
            .setBorder(new Color(0, 0, 0, 1), 2)
            .setScaledSize(20);

        nameFontBuilder = KillBillGame.get().getFontLoader().newBuilder("main")
            .setColor(new Color(1, 1, 1, 1))
            .setBorder(new Color(0, 0, 0, 1), 2)
            .setScaledSize(24);

        currentHeldItem = null;
        fadeTime = -1;

        generateLabels();
    }

    /**
     * Generates item quantity labels.
     */
    private void generateLabels() {
        // Professionally eyeballed :thumbsup:
        int slotWidth = KillBillGame.get().getWidth() / 20;
        int itemWidth = slotWidth * 4 / 5;
        int spacing = KillBillGame.get().getWidth() / 200;

        int x = (KillBillGame.get().getWidth() - (slotWidth) * GlobalGameConfig.INVENTORY_SIZE) / 2;

        elements.clear();
        for (int i = 0; i < GlobalGameConfig.INVENTORY_SIZE; i++) {
            elements.add(
                Label.newBuilder()
                    .setFontBuilder(quantityFontBuilder)
                    .setText("")
                    .setLocation(new FixedLocation(x + itemWidth, 3 * spacing))
                    .build()
            );

            x += KillBillGame.get().getWidth() / 20;
        }

        // Now we add the item name label
        itemNameLabel = Label.newBuilder()
            .setFontBuilder(nameFontBuilder)
            .setText("")
            .setLocation(new FixedLocation(KillBillGame.get().getWidth() / 2, spacing + slotWidth * 2))
            .setCentered(true)
            .build();

        elements.add(itemNameLabel);
    }

    @Override
    public void render(final SpriteBatch batch, final float delta) {
        int slotWidth = KillBillGame.get().getWidth() / 20;
        int itemWidth = slotWidth * 4 / 5;
        int spacing = KillBillGame.get().getWidth() / 200;

        int x = (KillBillGame.get().getWidth() - (slotWidth) * GlobalGameConfig.INVENTORY_SIZE) / 2;

        for (int i = 0; i < GlobalGameConfig.INVENTORY_SIZE; i++) {
            batch.draw(
                i == player.getHeldItemIndex() ? selectedBackgroundTexture : unselectedBackgroundTexture,
                x,
                spacing,
                slotWidth,
                slotWidth
            );

            Item item = player.getItem(i);
            if (item != null) {
                // Do pre-render stuff real quick
                item.beforeRender(delta);

                if (item.getQuantity() != 1) {
                    ((Label) elements.get(i)).setText(String.valueOf(item.getQuantity()));
                }

                batch.draw(
                    item.getInventoryTexture(),
                    x + spacing,
                    2 * spacing,
                    itemWidth,
                    itemWidth
                );

                // Draw the cooldown if applicable
                if (item.getCurrentCooldown() > 0) {
                    // Find out the percentage
                    float cooldownFactor = item.getCurrentCooldown() / item.getCooldown();
                    
                    // Draw with a height of whatever the cooldown is
                    batch.draw(
                        cooldownTexture,
                        x + spacing,
                        2 * spacing,
                        itemWidth,
                        itemWidth * cooldownFactor
                    );
                }
            }

            x += slotWidth;
        }

        // Item label
        Item heldItem = player.getHeldItem();
        // Check if the item just changed
        // Looking for instances here, doesn't matter if they're identical
        if (heldItem != currentHeldItem) {
            if (heldItem == null) {
                itemNameLabel.setText("");
                fadeTime = -1;
            }
            else {
                itemNameLabel.setText(heldItem.getName());
                fadeTime = 0;
            }
            currentHeldItem = heldItem;
        }

        if (fadeTime >= 0) {
            fadeTime += delta;

            if (fadeTime < 0.5f) {
                // Fade in
                itemNameLabel.setTint(new Color(1, 1, 1, fadeTime * 2));
            }

            // Hold for 2 seconds

            // Fade out
            if (fadeTime > 2.5f) {
                itemNameLabel.setTint(new Color(1, 1, 1, (3 - fadeTime) * 2));
            }

            if (fadeTime > 3f) {
                itemNameLabel.setTint(new Color(1, 1, 1, 1));
                itemNameLabel.setText("");
                fadeTime = -1;
            }
        }

        super.render(batch, delta);
    }

    @Override
    public void resize() {
        generateLabels();

        super.resize();
    }

    @Override
    public boolean runClick(final float clickX, final float clickY) {
        // See if this is within the bounds of the inventory items
        int slotWidth = KillBillGame.get().getWidth() / 20;
        int spacing = KillBillGame.get().getWidth() / 200;

        int x = (KillBillGame.get().getWidth() - (slotWidth) * GlobalGameConfig.INVENTORY_SIZE) / 2;

        for (int i = 0; i < GlobalGameConfig.INVENTORY_SIZE; i++) {
            if (clickX >= x && clickX < x + slotWidth && clickY >= spacing && clickY < spacing + slotWidth) {
                // Clicked!
                if (player.getHeldItemIndex() == i) {
                    // Double click = drop
                    player.drop();
                } else {
                    player.setHeldItem(i);
                }
                return true;
            }

            x += slotWidth;
        }

        return super.runClick(clickX, clickY);
    }
}
