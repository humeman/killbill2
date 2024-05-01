package today.tecktip.killbill.frontend.game.objects.explosives;

import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.objects.Entity;
import today.tecktip.killbill.frontend.game.objects.GameObject;
import today.tecktip.killbill.frontend.game.objects.renderers.ExplosionRenderer;
import today.tecktip.killbill.frontend.game.objects.renderers.StaticSpriteObjectRenderer;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicBombCommand.BasicBombCommandData;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicBombCommand.BombType;
import today.tecktip.killbill.frontend.screens.GameScreen;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Rectangle;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;

public class Bomb extends GameObject {

    private static final Map<BombType, String> BOMB_TEXTURES = Map.of(
        BombType.PLUSHIE, "objects_explosives_plushie",
        BombType.COMPUTER, "objects_explosives_computer",
        BombType.CLAYMORE_ROOMBA, "objects_explosives_claymore_roomba"
    );

    private static final Map<BombType, String> BOMB_ALT_TEXTURES = Map.of(
        BombType.PLUSHIE, "objects_explosives_plushie_b",
        BombType.COMPUTER, "objects_explosives_computer_b",
        BombType.CLAYMORE_ROOMBA, "objects_explosives_claymore_roomba_b"
    );
    
    private static final Map<BombType, Float> BOMB_SIZES = Map.of(
        BombType.PLUSHIE, 1f,
        BombType.COMPUTER, 1f,
        BombType.CLAYMORE_ROOMBA, 1f
    );
    
    private static final Map<BombType, Float> BOMB_FUSE_DURATIONS = Map.of(
        BombType.PLUSHIE, 3f,
        BombType.COMPUTER, 5f,
        BombType.CLAYMORE_ROOMBA, 1.5f
    );
    
    private static final Map<BombType, Integer> DAMAGE_AT_EPICENTER = Map.of(
        BombType.PLUSHIE, 5,
        BombType.COMPUTER, 8,
        BombType.CLAYMORE_ROOMBA, 5
    );
    
    private static final Map<BombType, Integer> DAMAGE_RADIUS = Map.of(
        BombType.PLUSHIE, 3,
        BombType.COMPUTER, 4,
        BombType.CLAYMORE_ROOMBA, 4
    );

    private Texture normalTexture;
    private Texture flashTexture;
    private Texture explosionTexture;

    /**
     * The rectangle where the tile is located.
     */
    private Rectangle rectangle;

    /**
     * Bomb data from UDP.
     */
    private final BasicBombCommandData data;

    /**
     * True if this player placed the bomb (meaning they are responsible for entity damage).
     */
    private boolean launchedByMe;

    /**
     * Time since the bomb was placed.
     */
    private float timer;

    /**
     * Time since last flash.
     */
    private float flashTimer;

    /**
     * Current flash state.
     */
    private boolean isFlashing;

    /**
     * If the bomb exploded.
     */
    private boolean exploded;

    /**
     * True when damage has been dealt.
     */
    private boolean damageDealt;

    private ExplosionRenderer explosionRenderer;

    /**
     * Constructs a new texture object.
     * @param tileX Tile X coordinate
     * @param tileY Tile Y coordinate
     * @param tileWidth Tile width
     * @param tileHeight Tile height
     * @param texture Texture to draw
     * @param flags Object flags to use
     */
    public Bomb(final BasicBombCommandData data) {
        super(new StaticSpriteObjectRenderer(KillBillGame.get().getTextureLoader().get(BOMB_TEXTURES.get(data.getBombType()))), new ObjectFlag[]{ObjectFlag.NEEDS_PRE_RENDER_UPDATE});
        launchedByMe = data.getLaunchedBy() != null && data.getLaunchedBy().equals(KillBillGame.get().getUser().id());
        rectangle = new Rectangle(
            new FixedLocation(
                (float) data.getOrigin().x() * GlobalGameConfig.GRID_SIZE, 
                (float) data.getOrigin().y() * GlobalGameConfig.GRID_SIZE),
            new FixedSize(
                BOMB_SIZES.get(data.getBombType()) * GlobalGameConfig.GRID_SIZE, 
                BOMB_SIZES.get(data.getBombType()) * GlobalGameConfig.GRID_SIZE)
        );

        this.data = data;
        timer = 0;
        damageDealt = false;
        exploded = false;
        flashTimer = 0;
        normalTexture = KillBillGame.get().getTextureLoader().get(BOMB_TEXTURES.get(data.getBombType()));
        flashTexture = KillBillGame.get().getTextureLoader().get(BOMB_ALT_TEXTURES.get(data.getBombType()));
        explosionTexture = KillBillGame.get().getTextureLoader().get("objects_explosives_explosion");
        explosionRenderer = null;
    }

    public Bomb(final BasicBombCommandData data, final boolean launchedByMe) {
        this(data);
        this.launchedByMe = launchedByMe;
    }

    @Override
    public void beforeRender(final float delta) {
        // Add to the timer
        timer += delta;

        if (exploded) {
            // Deal with rendering here...
            final float damageRadius = DAMAGE_RADIUS.get(data.getBombType());
            if (!damageDealt && timer >= 0.5f) {
                // Now deal damage
                ((GameScreen) KillBillGame.get().getCurrentScreen()).getGameRenderer().forEachObject(
                    object -> {
                        if (object instanceof Entity) {
                            Entity entity = (Entity) object;

                            // We handle our own damage
                            if (launchedByMe && entity.hasFlag(ObjectFlag.ATTACKABLE)) {
                                // We are allowed to damage this one
                                
                                // Find out their range
                                double distance = Math.sqrt(
                                    Math.pow((entity.getRectangle().getCenterX() - getRectangle().getCenterX()) / GlobalGameConfig.GRID_SIZE, 2) +
                                    Math.pow((entity.getRectangle().getCenterY() - getRectangle().getCenterY()) / GlobalGameConfig.GRID_SIZE, 2)
                                );

                                if (distance < damageRadius) {
                                    // Within range.
                                    // Find the ratio of damage (more damage at center of bomb)
                                    int damageAmount = (int) (DAMAGE_AT_EPICENTER.get(data.getBombType()) * ((damageRadius - distance) / damageRadius));

                                    entity.damage(damageAmount);
                                }
                            }
                        }
                        return false;
                    }
                );
                damageDealt = true;
            }

            if (explosionRenderer.isDone()) {
                // Remove from game
                // This is safe because of magic.
                ((GameScreen) KillBillGame.get().getCurrentScreen()).getGameRenderer().removeObject(this);
            }
            return;
        }

        // Past the explosion time?
        if (timer > BOMB_FUSE_DURATIONS.get(data.getBombType())) {
            // Go boom
            explosionRenderer = new ExplosionRenderer(explosionTexture, DAMAGE_RADIUS.get(data.getBombType()));
            setRenderer(explosionRenderer);
            timer = 0;
            exploded = true;
            return;
        }

        // If not? We're currently flashing in preparation for an explosion.
        flashTimer -= delta;

        if (flashTimer <= 0) {
            isFlashing = !isFlashing;
            flashTimer = 0.25f * (1 - timer / BOMB_FUSE_DURATIONS.get(data.getBombType()));
            // Adjust this so the flash is longer and longer as we get closer to detonation
            flashTimer = isFlashing ? Math.max(0.1f, flashTimer) : Math.max(0.025f, flashTimer);

            // Change texture
            ((StaticSpriteObjectRenderer) renderer).changeTexture(isFlashing ? flashTexture : normalTexture);
        }
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
