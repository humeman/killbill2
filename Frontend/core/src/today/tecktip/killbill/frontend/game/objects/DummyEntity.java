package today.tecktip.killbill.frontend.game.objects;

import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.items.DummyItem;
import today.tecktip.killbill.frontend.game.objects.entities.ClaymoreRoomba;
import today.tecktip.killbill.frontend.game.objects.entities.Employee;
import today.tecktip.killbill.frontend.game.objects.renderers.MovementBasedRenderer;
import today.tecktip.killbill.frontend.game.objects.renderers.ObjectRenderer;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalEntityState;

/**
 * An entity received over the game server.
 * @author cs
 */
public abstract class DummyEntity extends Entity {
    /**
     * The entity state for this player.
     */
    private BasicLocalEntityState entityState;

    protected int state;

    private boolean movedLastFrame;

    private int checks;

    /**
     * Constructs a new dummy entity.
     * @param tileWidth Tile width
     * @param tileHeight Tile height
     * @param texture Texture to draw
     * @param entityState The entity state for this player
     */
    public DummyEntity(final float tileWidth, final float tileHeight, final ObjectRenderer renderer, final BasicLocalEntityState entityState) {
        super(
            (float) (entityState.getCoordinates().x() * GlobalGameConfig.GRID_SIZE),
            (float) (entityState.getCoordinates().y() * GlobalGameConfig.GRID_SIZE),
            tileWidth,
            tileHeight,
            renderer,
            new ObjectFlag[] {ObjectFlag.ATTACKABLE}
        );
        this.entityState = entityState;
        state = entityState.getState();
        move(0);
        movedLastFrame = false;
    }

    @Override
    public void damage(final int damage) {
        super.damage(damage);
        entityState.setHealth(getHealth());
    }

    @Override
    public boolean processInput(final int key) {
        return false;
    }

    /**
     * Moves the entity using the game state location.
     * @param delta Time since last render
     */
    public void move(final float delta) {
        setLocation(
            (float) (entityState.getCoordinates().x() * GlobalGameConfig.GRID_SIZE), 
            (float) (entityState.getCoordinates().y() * GlobalGameConfig.GRID_SIZE));
    }
    
    @Override
    public boolean movedLastFrame() {
        if (movedLastFrame) {
            checks++;
            if (checks > 10) {
                checks = 0;
                movedLastFrame = false;
            }
        }

        return movedLastFrame;
    }

    /**
     * Updates the entity with data from their game state.
     */
    public void updateState() {
        if (entityState.wasUpdated()) {
            float newX = (float) entityState.getCoordinates().x() * GlobalGameConfig.GRID_SIZE;
            float newY = (float) entityState.getCoordinates().y() * GlobalGameConfig.GRID_SIZE;

            if (Math.abs(newX - getRectangle().getX()) > 0.005f || Math.abs(newY - getRectangle().getY()) > 0.005f) {
                setLocation(newX, newY);
                movedLastFrame = true;
                checks = 0;
            } else {
                movedLastFrame = false;
            }
            setRotation(entityState.getRotation() - 180);
            renderer.updateLocation(this);
            setHealth(entityState.getHealth());
            if (entityState.getHeldItemTexture() != null) {
                boolean change = true;
                if (getHeldItem() != null) {
                    if (getHeldItem().getHeldTextureName().equals(entityState.getHeldItemTexture())) change = false;
                }

                if (change) {
                    if (entityState.getHeldItemTexture().equals("none")) {
                        setHeldItem(null);
                    } else {
                        setHeldItem(new DummyItem(KillBillGame.get().getTextureLoader().get(entityState.getHeldItemTexture()), entityState.getHeldItemTexture()));
                    }
                }
            }

            if (entityState.getState() != state) {
                onStateChange(entityState.getState());
                state = entityState.getState();
            }

            if (renderer instanceof MovementBasedRenderer) {
                ((MovementBasedRenderer) renderer).changeTexture(entityState.getTexturePrefix());
            }


            entityState.clearUpdate();
        }
    }

    /**
     * Don't change the state in here
     */
    protected abstract void onStateChange(final int newState);

    /**
     * Gets the entity state this player represents.
     * @return The entity state for this player.
     */
    public BasicLocalEntityState getEntityState() {
        return entityState;
    }

    public static DummyEntity createFrom(final BasicLocalEntityState entityState) {
        DummyEntity e;
        switch (entityState.getType()) {
            case CLAYMORE_ROOMBA:
                e = new ClaymoreRoomba(entityState);
                break;
            case EMPLOYEE:
                e = new Employee(entityState);
                break;
            default:
                throw new IllegalArgumentException("Unsupported entity: " + entityState.getType());
        }
        e.setRotation(entityState.getRotation());
        e.setHealth(entityState.getHealth());
        return e;
    }
}
