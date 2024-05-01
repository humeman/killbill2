package today.tecktip.killbill.frontend.screens;

import com.badlogic.gdx.Screen;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.ui.UiRenderer;

/**
 * A base class which represents a Kill Bill game screen.
 * @author cs
 */
public abstract class KillBillScreen implements Screen {
    /**
     * Called when creating the game.
     */
    public abstract void onCreate();

    /**
     * Called when creating the game.
     */
    public void create() {
        onCreate();
    }

    /**
     * Called when this screen becomes the current screen.
     */
    public abstract void onSwitch();

    @Override
    public void show() {
        onSwitch();
    }

    /**
     * Performs tasks to be run before rendering anything else.
     * @param delta Time in seconds since last frame
     */
    public abstract void drawFirst(float delta);
    
    /**
     * Draws the HUD to the hudBatch on each frame.
     * @param delta Time in seconds since last frame
     */
    public abstract void drawHud(float delta);

    /**
     * Draws the game to the screen. Not yet used.
     * @param delta Time in seconds since last frame
     */
    public abstract void drawGame(float delta);

    @Override
    public void render(float delta) {
        drawFirst(delta);
        drawGame(delta);
        drawHud(delta);
    }

    /**
     * Called when the screen dimensions change.
     */
    public abstract void onResize();

    @Override
    public void resize(int width, int height) {
        KillBillGame.get().setWidth(width);
        KillBillGame.get().setHeight(height);
        onResize();
    }

    /**
     * Called when the screen is active and gets paused.
     */
    public abstract void onPause();

    @Override
    public void pause() {
        onPause();
    }

    /**
     * Called when the screen is unpaused.
     */
    public abstract void onResume();

    @Override
    public void resume() {
        onResume();
    }

    /**
     * Called when the screen is switched off of.
     */
    public abstract void onSwitchOff();

    @Override
    public void hide() {
        onSwitchOff();
    }

    /**
     * Called when the screen is destroyed.
     */
    public abstract void onDestroy();

    @Override
    public void dispose() {
        onDestroy();
    }

    /**
     * Gets the UI renderer for this screen, if applicable.
     * @return UI renderer or null
     */
    public abstract UiRenderer getUiRenderer();
}
