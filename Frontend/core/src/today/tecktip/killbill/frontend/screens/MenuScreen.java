package today.tecktip.killbill.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.Keybinds.KeyType;
import today.tecktip.killbill.frontend.http.requests.AuthRequests;
import today.tecktip.killbill.frontend.ui.UiRenderer;

/**
 * A screen suitable for rendering menus.
 */
public abstract class MenuScreen extends KillBillScreen {
    /**
     * Renders UI elements to the fixed batch.
     */
    protected final UiRenderer uiRenderer;

    /**
     * Background color to display.
     */
    protected Color backgroundColor;

    /**
     * The sprite batch for drawing HUD elements or fixed elements.
     */
    protected final SpriteBatch hudBatch;

    /**
     * Handles incoming keypresses and other input events for this screen.
     */
    private final InputAdapter inputAdapter = new InputAdapter() {
        @Override
        public boolean touchDown(int x, int y, int pointer, int button) {
            return uiRenderer.runClick(x, KillBillGame.get().getHeight() - y);
        }

        @Override
        public boolean keyDown(final int key) {
            if (KillBillGame.get().getKeybinds().isKeyPressed(KeyType.CLOSE)) {
                // Sign out if possible
                if (KillBillGame.get().getHttpClient().getAuthenticationHeaders().size() != 0) {
                    AuthRequests.signOut(
                        resp -> {
                            Gdx.app.log(GameScreen.class.getSimpleName(), "Signed out.");
                            System.exit(0);
                        },
                        error -> {
                            Gdx.app.error(GameScreen.class.getSimpleName(), "Sign out failed.", error);
                            System.exit(1);
                        }
                    );
                }
            }

            return uiRenderer.runKey(key);
        }

        @Override
        public boolean keyTyped(final char key) {
            return uiRenderer.runTyped(key);
        }
    };

    /**
     * Constructs a new MenuScreen with no elements.
     */
    protected MenuScreen(final Color backgroundColor) {
        uiRenderer = new UiRenderer();
        hudBatch = new SpriteBatch();
        this.backgroundColor = backgroundColor;
    }

    @Override
    public abstract void onCreate();

    @Override
    public void onSwitch() {
        Gdx.input.setInputProcessor(inputAdapter);
    }
    @Override
    public void drawFirst(float delta) {
        ScreenUtils.clear(backgroundColor);

    }

    @Override
    public void drawHud(float delta) {
        hudBatch.begin();
        uiRenderer.render(hudBatch, delta);
        hudBatch.end();
    }

    @Override
    public void drawGame(float delta) { }

    @Override
    public void onPause() { }

    @Override
    public void onResume() { }

    @Override
    public void onSwitchOff() { }

    @Override
    public void onDestroy() { 
        hudBatch.dispose();
    }

    @Override
    public void onResize() {
        hudBatch.getProjectionMatrix().setToOrtho2D(0, 0, KillBillGame.get().getWidth(), KillBillGame.get().getHeight());
        uiRenderer.resize();
    }

    @Override
    public UiRenderer getUiRenderer() {
        return uiRenderer;
    }
}
