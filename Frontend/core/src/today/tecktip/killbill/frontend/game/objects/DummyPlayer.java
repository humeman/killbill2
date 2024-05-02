package today.tecktip.killbill.frontend.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.items.DummyItem;
import today.tecktip.killbill.frontend.game.objects.renderers.MovementBasedRenderer;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameUserState;

/**
 * A player received over the game server (uncontrollable from this game).
 * @author cs
 */
public class DummyPlayer extends Entity {
    /**
     * The user state for this player.
     */
    private BasicLocalGameUserState userState;

    private boolean spectator;

    private boolean movedLastFrame;

    private int checks;

    /**
     * Constructs a new dummy player.
     * <p>
     * @param tileWidth Tile width
     * @param tileHeight Tile height
     * @param texture Texture to draw
     * @param userState The user state for this player
     */
    public DummyPlayer(final float tileWidth, final float tileHeight, final BasicLocalGameUserState userState) {
        super(
            (float) ((BasicLocalGameUserState) userState).getCoordinates().x() * GlobalGameConfig.GRID_SIZE,
            (float) ((BasicLocalGameUserState) userState).getCoordinates().y() * GlobalGameConfig.GRID_SIZE,
            tileWidth,
            tileHeight,
            null,
            new ObjectFlag[] {ObjectFlag.ATTACKABLE}
        );
        setRenderer(new MovementBasedRenderer(this, userState.getTexturePrefix()));
        this.userState = userState;
        spectator = false;
        move(0);
    }

    @Override
    public void damage(final int damage) {
        super.damage(damage);
        userState.setHealth(getHealth());
    }

    @Override
    public boolean processInput(final int key) {
        return false;
    }

    /**
     * Moves the player using the game state location.
     * @param delta Time since last render
     */
    public void move(final float delta) {
        setLocation(
            (float) ((BasicLocalGameUserState) userState).getCoordinates().x() * GlobalGameConfig.GRID_SIZE, 
            (float) ((BasicLocalGameUserState) userState).getCoordinates().y() * GlobalGameConfig.GRID_SIZE);
    }

    @Override
    public void renderTo(float delta, SpriteBatch batch) {
        if (spectator && 
            !KillBillGame.get().getPlayer().getUserState().getPlayerType().equals(BasicPlayerType.SPECTATOR)) {
            // Don't render
            return;
        }
        super.renderTo(delta, batch);
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
     * Updates the player with data from their game state.
     */
    public void updateState() {
        if (userState.wasUpdated()) {
            float newX = (float) userState.getCoordinates().x() * GlobalGameConfig.GRID_SIZE;
            float newY = (float) userState.getCoordinates().y() * GlobalGameConfig.GRID_SIZE;

            if (Math.abs(newX - getRectangle().getX()) > 0.005f || Math.abs(newY - getRectangle().getY()) > 0.005f) {
                setLocation(newX, newY);
                movedLastFrame = true;
                checks = 0;
            } else {
                movedLastFrame = false;
            }
            setRotation(userState.getRotation());
            renderer.updateLocation(this);
            setHealth(userState.getHealth());
            if (userState.getHeldItemTexture() != null) {
                boolean change = true;
                if (getHeldItem() != null) {
                    if (getHeldItem().getHeldTextureName().equals(userState.getHeldItemTexture())) change = false;
                }

                if (change) {
                    if (userState.getHeldItemTexture().equals("none")) {
                        setHeldItem(null);
                    } else {
                        setHeldItem(new DummyItem(KillBillGame.get().getTextureLoader().get(userState.getHeldItemTexture()), userState.getHeldItemTexture()));
                    }
                }
            }
            if (renderer instanceof MovementBasedRenderer) {
                ((MovementBasedRenderer) renderer).changeTexture(userState.getTexturePrefix());
            }
            if (!spectator && userState.getPlayerType().equals(BasicPlayerType.SPECTATOR)) {
                unsetFlag(ObjectFlag.SOLID);
                unsetFlag(ObjectFlag.ATTACKABLE);
                unsetFlag(ObjectFlag.INTERACTABLE);
                unsetFlag(ObjectFlag.FROZEN);
                spectator = true;
            }

            userState.clearUpdate();
        }
    }

    /**
     * Gets the user state this player represents.
     * @return The user state for this player.
     */
    public BasicLocalGameUserState getUserState() {
        return userState;
    }
}
