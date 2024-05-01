package today.tecktip.killbill.frontend.game.objects.explosives;

import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.objects.DummyPlayer;
import today.tecktip.killbill.frontend.game.objects.Entity;
import today.tecktip.killbill.frontend.game.objects.GameObject;
import today.tecktip.killbill.frontend.game.objects.renderers.ExplosionRenderer;
import today.tecktip.killbill.frontend.game.objects.renderers.StaticSpriteObjectRenderer;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicProjectileCommand.BasicProjectileCommandData;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicProjectileCommand.ProjectileType;
import today.tecktip.killbill.frontend.screens.GameScreen;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Rectangle;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;

public class Projectile extends GameObject {

    private static final Map<ProjectileType, String> PROJECTILE_TEXTURES = Map.of(
        ProjectileType.PENGUIN, "objects_explosives_penguin",
        ProjectileType.RAM, "objects_explosives_ram"
    );
    
    private static final Map<ProjectileType, Float> PROJECTILE_SIZES = Map.of(
        ProjectileType.PENGUIN, 0.5f,
        ProjectileType.RAM,  0.5f
    );
    
    private static final Map<ProjectileType, Integer> DAMAGE_AT_EPICENTER = Map.of(
        ProjectileType.PENGUIN, 4,
        ProjectileType.RAM,  6
    );
    
    private static final Map<ProjectileType, Float> DAMAGE_RADIUS = Map.of(
        ProjectileType.PENGUIN, 2.5f,
        ProjectileType.RAM,   3f
    );
    
    private static final Map<ProjectileType, Float> TILES_PER_SECOND = Map.of(
        ProjectileType.PENGUIN, 5f,
        ProjectileType.RAM,   8f
    );
    
    private static final Map<ProjectileType, Float> DECEL_PER_SECOND = Map.of(
        ProjectileType.PENGUIN, 3f,
        ProjectileType.RAM,   6f
    );

    private Texture explosionTexture;

    /**
     * The rectangle where the tile is located.
     */
    private Rectangle rectangle;

    /**
     * Bomb data from UDP.
     */
    private final BasicProjectileCommandData data;

    /**
     * True if this player launched the projectile (meaning they are responsible for entity damage).
     */
    private boolean launchedByMe;

    /**
     * Time since the projectile was launched.
     */
    private float timer;

    /**
     * If the projectile exploded.
     */
    private boolean exploded;

    /**
     * True when damage has been dealt.
     */
    private boolean damageDealt;

    private ExplosionRenderer explosionRenderer;

    private float currentSpeed;

    /**
     * Constructs a new projectile.
     */
    public Projectile(final BasicProjectileCommandData data) {
        super(new StaticSpriteObjectRenderer(KillBillGame.get().getTextureLoader().get(PROJECTILE_TEXTURES.get(data.getProjectileType()))), new ObjectFlag[]{ObjectFlag.NEEDS_PRE_RENDER_UPDATE});
        launchedByMe = data.getLaunchedBy() != null && data.getLaunchedBy().equals(KillBillGame.get().getUser().id());
        rectangle = new Rectangle(
            new FixedLocation(
                (float) data.getOrigin().x() * GlobalGameConfig.GRID_SIZE, 
                (float) data.getOrigin().y() * GlobalGameConfig.GRID_SIZE),
            new FixedSize(
                PROJECTILE_SIZES.get(data.getProjectileType()) * GlobalGameConfig.GRID_SIZE, 
                PROJECTILE_SIZES.get(data.getProjectileType()) * GlobalGameConfig.GRID_SIZE)
        );

        this.data = data;
        timer = 0;
        damageDealt = false;
        exploded = false;
        explosionTexture = KillBillGame.get().getTextureLoader().get("objects_explosives_explosion");
        explosionRenderer = null;
        currentSpeed = TILES_PER_SECOND.get(data.getProjectileType());
    }

    public Projectile(final BasicProjectileCommandData data, final boolean launchedByMe) {
        this(data);
        this.launchedByMe = launchedByMe;
    }

    @Override
    public void beforeRender(final float delta) {
        // Add to the timer
        timer += delta;

        if (exploded) {
            // Deal with rendering here...
            final float damageRadius = DAMAGE_RADIUS.get(data.getProjectileType());
            if (!damageDealt && timer >= 0.5f) {
                // Now deal damage
                ((GameScreen) KillBillGame.get().getCurrentScreen()).getGameRenderer().forEachObject(
                    object -> {
                        if (object instanceof Entity) {
                            Entity entity = (Entity) object;

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
                                    int damageAmount = (int) (DAMAGE_AT_EPICENTER.get(data.getProjectileType()) * (damageRadius - distance) / damageRadius);

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

        // Decelerate
        currentSpeed -= delta * DECEL_PER_SECOND.get(data.getProjectileType());

        // If we're stopped, explode
        if (currentSpeed <= 0) {
            explode();
            return;
        }

        // Direction in radians
        float angle = (float) Math.toRadians(data.getDirection() - 90);

        // Move along
        float dx = currentSpeed * GlobalGameConfig.GRID_SIZE * ((float) Math.cos(angle)) * delta;
        float dy = currentSpeed * GlobalGameConfig.GRID_SIZE * ((float) Math.sin(angle)) * delta;

        getRectangle().getLocation().offsetX(dx).offsetY(dy);

        GameObject collidedObject = null;
        for (final GameObject object : ((GameScreen) KillBillGame.get().getScreen()).getGameRenderer().getObjects()) {
            if (object == this) continue;
            if (launchedByMe && object == KillBillGame.get().getPlayer()) continue;
            if (object.hasFlag(ObjectFlag.SOLID) && isCollidingWith(object)) {
                // We are colliding with something.
                if (data.getLaunchedBy() != null && object instanceof DummyPlayer) {
                    if (((DummyPlayer) object).getUserState().getUserId().equals(data.getLaunchedBy())) continue;
                }

                collidedObject = object;
                break;
            }
        }

        if (collidedObject != null) explode();
    }

    private void explode() {
        // Go boom
        explosionRenderer = new ExplosionRenderer(explosionTexture, DAMAGE_RADIUS.get(data.getProjectileType()));
        setRenderer(explosionRenderer);
        exploded = true;
        timer = 0f;
        damageDealt = false;
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
