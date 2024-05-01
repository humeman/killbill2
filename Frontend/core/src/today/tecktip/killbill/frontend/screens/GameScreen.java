package today.tecktip.killbill.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.config.Keybinds.KeyType;
import today.tecktip.killbill.frontend.game.GameRenderer;
import today.tecktip.killbill.frontend.game.objects.Entity;
import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.ui.UiRenderer;
import today.tecktip.killbill.frontend.ui.renderers.DebugDisplay;
import today.tecktip.killbill.frontend.ui.renderers.TouchInputDisplay;

/**
 * A screen for rendering a room in the game. Contains fixed (menu/UI) and dynamic (game) renderers.
 * @author al, cs
 */
public abstract class GameScreen extends KillBillScreen {
    /**
     * Renders UI elements to the fixed batch.
     */
    protected final UiRenderer uiRenderer;

    /**
     * Renders the game to the dynamic batch.
     */
    protected final GameRenderer gameRenderer;

    /**
     * The camera for rendering the game's batch.
     */
    protected final OrthographicCamera gameCamera;
    
    /**
     * The viewport for rendering the game's batch.
     */
    protected final ExtendViewport gameViewport;

    /**
     * The sprite batch for rendering game objects.
     */
    protected final SpriteBatch gameBatch;

    /**
     * The sprite batch for drawing HUD elements or fixed elements.
     */
    protected final SpriteBatch hudBatch;

    /**
     * Background color to display.
     */
    private final Color backgroundColor;

    /**
     * The X coordinate where the camera should be centered.
     */
    private float xCameraLocation;

    /**
     * The Y coordinate where the camera should be centered.
     */
    private float yCameraLocation;

    /**
     * UI enabled state
     */
    private boolean uiEnabled;

    /**
     * Handles incoming keypresses and other input events for this screen.
     */
    private final InputAdapter defaultInputAdapter = new InputAdapter() {
        @Override
        public boolean touchDown(int x, int y, int pointer, int button) {
            return uiRenderer.runClick(x, KillBillGame.get().getHeight() - y);
        }

        public boolean touchUp(int x, int y, int pointer, int button) {
            return uiRenderer.runUnclick(x, KillBillGame.get().getHeight() - y);
        }

        @Override
        public boolean keyDown(final int key) {
            if (KillBillGame.get().getKeybinds().isKeyJustPressed(KeyType.TOGGLE_UI)) {
                uiEnabled = !uiEnabled;
            }

            if (uiRenderer.runKey(key)) return true;

            if (gameRenderer.forEachObject(
                object -> {
                    if (object.hasFlag(ObjectFlag.PROCESSES_INPUT)) {
                        if (object.processInput(key)) return true;
                    }
                    return false;
                }
            )) return true;

            return false;
        }

        @Override
        public boolean keyTyped(final char key) {
            return uiRenderer.runTyped(key);
        }
    };

    /**
     * The input processor to use.
     */
    private InputProcessor inputAdapter;

    /**
     * Shape renderer for debugging.
     */
    private ShapeRenderer shapeRenderer;

    /**
     * Debugging info renderer.
     */
    protected DebugDisplay debugDisplay;

    protected TouchInputDisplay touchInputDisplay; 

    /**
     * Constructs a new empty GameScreen.
     * @param backgroundColor Background color to fill in before each render.
     */
    protected GameScreen(final Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        inputAdapter = defaultInputAdapter;
        uiEnabled = true;
        shapeRenderer = new ShapeRenderer();
        uiRenderer = new UiRenderer();
        hudBatch = new SpriteBatch();
        gameRenderer = new GameRenderer();
        gameBatch = new SpriteBatch();
        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(false, KillBillGame.get().getWidth(), KillBillGame.get().getHeight());
        gameViewport = new ExtendViewport(
            GlobalGameConfig.GRID_SIZE * 15, 
            GlobalGameConfig.GRID_SIZE * 8,
            GlobalGameConfig.GRID_SIZE * 20,
            GlobalGameConfig.GRID_SIZE * 20,
            gameCamera
        );
        gameViewport.setScaling(Scaling.fit);
        gameViewport.setWorldSize(GlobalGameConfig.WORLD_SIZE_X_TILES * 16, GlobalGameConfig.WORLD_SIZE_Y_TILES * 16);
        gameViewport.update(KillBillGame.get().getWidth(), KillBillGame.get().getHeight(), true);
        resizeCameras();
        moveCameras();

        // Create debug display
        debugDisplay = new DebugDisplay();
        uiRenderer.add(debugDisplay);
        debugDisplay.addGenerator(
            delta -> {
                if (KillBillGame.get().getPlayer() != null) {
                    return String.format(
                        "Location: (%.1f, %.1f)", 
                        KillBillGame.get().getPlayer().getRectangle().getX() / GlobalGameConfig.GRID_SIZE, 
                        KillBillGame.get().getPlayer().getRectangle().getY() / GlobalGameConfig.GRID_SIZE);
                }
                return "Location: Unknown";
            }
        );

        // Create touch input display
        touchInputDisplay = new TouchInputDisplay();
        uiRenderer.add(touchInputDisplay);
    }

    /**
     * Gets the game renderer in use for this screen.
     * @return Game renderer
     */
    public GameRenderer getGameRenderer() {
        return gameRenderer;
    }

    /**
     * Multiplexes the default input processor with another input processor.
     * @param processor Input processor to multiplex with default
     */
    public void setInputProcessor(final InputProcessor processor) {
        inputAdapter = new InputMultiplexer(defaultInputAdapter, processor);
    }

    @Override
    public UiRenderer getUiRenderer() {
        return uiRenderer;
    }

    /**
     * Changes the location of the game camera.
     * @param x X coordinate (center of screen)
     * @param y Y coordinate (center of screen)
     */
    public void setCameraLocation(final float x, final float y) {
        xCameraLocation = x;
        yCameraLocation = y;
        moveCameras();
        resizeCameras();
    }

    /**
     * Moves the cameras to the current x and y camera locations.
     */
    private void moveCameras() {
        gameCamera.position.set(
            xCameraLocation,
            yCameraLocation,
            0
        );
    }

    /**
     * Resizes the cameras when the display size changes.
     */
    private void resizeCameras() {
        gameCamera.update();
        gameBatch.setProjectionMatrix(gameCamera.combined);
    }

    @Override
    public void onCreate() {
        xCameraLocation = 0;
        yCameraLocation = 0;
        resizeCameras();
    }

    @Override
    public void onSwitch() {
        Gdx.input.setInputProcessor(inputAdapter);
    }

    @Override
    public void drawFirst(float delta) {
        gameRenderer.forEachObject(
            object -> {
                if (object.hasFlag(ObjectFlag.NEEDS_PRE_RENDER_UPDATE)) {
                    object.beforeRender(delta);
                }
                return false;
            }
        );

        ScreenUtils.clear(backgroundColor);
    }

    @Override
    public void drawHud(float delta) {
        if (uiEnabled) {
            hudBatch.begin();
            hudBatch.enableBlending();
            uiRenderer.render(hudBatch, delta);
            hudBatch.end();
        }

    }

    @Override
    public final void drawGame(float delta) {
        gameViewport.apply();
        gameBatch.enableBlending();
        gameBatch.begin();
        gameRenderer.render(gameBatch, delta);
        gameBatch.end();

        if (KillBillGame.get().isDebugEnabled()) {
            shapeRenderer.setProjectionMatrix(gameCamera.combined);
            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(0, 0, 1, 1);
            gameRenderer.forEachObject(
                object -> {
                    object.getRenderer().drawBoundingBox(shapeRenderer, object);
                    return false;
                }
            );
            shapeRenderer.setColor(0, 1, 0, 1);
            gameRenderer.forEachObject(
                object -> {
                    if (object instanceof Entity)
                        object.getRenderer().drawLookingLine(shapeRenderer, (Entity) object);
                    return false;
                }
            );
            shapeRenderer.end();
        }
    }

    @Override
    public void onPause() { }

    @Override
    public void onResume() { }

    @Override
    public void onSwitchOff() { }

    @Override
    public void onDestroy() {
        hudBatch.dispose();
        gameBatch.dispose();
    }

    @Override
    public void onResize() {
        gameViewport.update(KillBillGame.get().getWidth(), KillBillGame.get().getHeight(), true);
        hudBatch.getProjectionMatrix().setToOrtho2D(0, 0, KillBillGame.get().getWidth(), KillBillGame.get().getHeight());
        moveCameras();
        resizeCameras();
        uiRenderer.resize();
    }
}
