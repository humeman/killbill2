package today.tecktip.killbill.frontend;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import today.tecktip.killbill.frontend.screens.KillBillScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.common.gameserver.CommandLoader;
import today.tecktip.killbill.frontend.config.Keybinds;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.game.objects.Player;
import today.tecktip.killbill.frontend.gameserver.ClientMessageHandler;
import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.http.requests.data.User;
import today.tecktip.killbill.frontend.resources.FontLoader;
import today.tecktip.killbill.frontend.resources.MapPackageLoader;
import today.tecktip.killbill.frontend.resources.TextureLoader;

/**
 * Base Kill Bill Game class.
 * @author al, cs
 */
public class KillBillGame extends Game {
	/**
	 * Static storage of the active instance of the game.
	 */
	private static KillBillGame inst = null;

	/**
	 * Gets the active KillBillGame instance.
	 */
	public static KillBillGame get() {
		if (inst == null) {
			throw new CatastrophicException("KillBillGame is not initialized yet.");
		}
		return inst;
	}

	/**
	 * The platform the game is running on.
	 */
	private Platform platform;
	
	/**
	 * The input type to use.
	 */
	private InputType inputType;

	/**
	 * Screen to change to after rendering completes.
	 */
	private KillBillScreen nextScreen;

	/**
	 * Loads textures from the assets directory.
	 */
	private TextureLoader textureLoader;

	/**
	 * Loads fonts from the assets directory.
	 */
	private FontLoader fontLoader;

	/**
	 * Loads maps from the assets directory.
	 */
	private MapPackageLoader mapLoader;

	/**
	 * Active native-impl HTTP client.
	 */
	private NativeHttpClient httpClient = null;

	/**
	 * UDP client message handler.
	 */
	private ClientMessageHandler udpClient = null;

	/**
	 * Loads UDP commands dynamically based on annotations.
	 */
	private CommandLoader commandLoader;

	/**
	 * Handles which key types match to which keys.
	 */
	private Keybinds keybinds;

	/**
	 * Player that is currently in use.
	 */
	private Player player;

	/**
	 * The API user that's authenticated.
	 */
	private User apiUser;

	/**
	 * Render methods to call on each frame render.
	 */
	private List<NothingMethod> renderMethods;

	/**
	 * Current width of the screen.
	 */
	private int width;

	/**
	 * Current height of the screen.
	 */
	private int height;

	/**
	 * Debug mode state.
	 */
	private boolean debugEnabled;

	/**
	 * Constructs a new KillBillGame.
	 * @param platform The platform the game is running on
	 * @param inputType The input type to use.
	 * @param httpClient Statically accessible native HTTP client
	 * @param commandLoader UDP command loader
	 */
	public KillBillGame(
			final Platform platform,
			final InputType inputType,
			final NativeHttpClient httpClient,
			final CommandLoader commandLoader
		) {
		this.httpClient = httpClient;
		this.platform = platform;
		this.inputType = inputType;
		this.commandLoader = commandLoader;
		textureLoader = new TextureLoader();
		fontLoader = new FontLoader();
		mapLoader = new MapPackageLoader();
		keybinds = new Keybinds();
		renderMethods = new ArrayList<>();
		inst = this;
		nextScreen = null;
		debugEnabled = false;
		player = null;
		apiUser = null;
	}

	/**
	 * Returns the native-implementation HTTP client associated with this game.
	 * @return HTTP client
	 */
	public NativeHttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * Gets the texture loader associated with this game.
	 * @return Texture loader
	 */
	public TextureLoader getTextureLoader() {
		return textureLoader;
	}

	/**
	 * Gets the font loader associated with this game.
	 * @return Font loader
	 */
	public FontLoader getFontLoader() {
		return fontLoader;
	}

	/**
	 * Gets the map loader associated with this game.
	 * @return Map loader
	 */
	public MapPackageLoader getMapLoader() {
		return mapLoader;
	}

	/**
	 * Gets the platform the game is currently running on.
	 * @return Platform
	 */
	public Platform getPlatform() {
		return platform;
	}

	/**
	 * Gets the input type to use.
	 * @return Input tpye
	 */
	public InputType getInputType() {
		return inputType;
	}

	/**
	 * Gets the keybinds set on this game.
	 * @return Keybinds
	 */
	public Keybinds getKeybinds() {
		return keybinds;
	}

	/**
	 * Gets the width of the screen.
	 * @return Width in pixels
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the height of the screen.
	 * @return Height in pixels
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * <strong>Do not call this to resize the screen!</strong>
	 * <p>
	 * Updates the internal screen size so renderers can pick up on it.
	 * @param width New width
	 */
	public void setWidth(final int width) {
		this.width = width;
	}

	/**
	 * <strong>Do not call this to resize the screen!</strong>
	 * <p>
	 * Updates the internal screen size so renderers can pick up on it.
	 * @param height New height
	 */
	public void setHeight(final int height) {
		this.height = height;
	}

	/**
	 * Gets the currently rendering screen.
	 * @return Current screen
	 */
	public KillBillScreen getCurrentScreen() {
		if (!(getScreen() instanceof KillBillScreen)) {
			throw new CatastrophicException("The active screen is not an instance of KillBillGame: " + getScreen());
		}

		return (KillBillScreen) getScreen();
	}

	/**
	 * Changes to a new screen when rendering completes.
	 * @param newScreen Screen to change to
	 */
	public void changeScreen(final KillBillScreen newScreen) {
		this.nextScreen = newScreen;
	}

	/**
	 * Gets the current client message handler, if one is defined.
	 * @return UDP client
	 */
	public ClientMessageHandler getUdpClient() {
		if (udpClient == null) {
			throw new CatastrophicException("Attempted to retrieve UDP client before one was set.");
		}
		return udpClient;
	}

	/**
	 * Gets the (pre-loaded) UDP classpath command loader.
	 * @return Command loader
	 */
	public CommandLoader getCommandLoader() {
		return commandLoader;
	}

	/**
	 * Sets the game's UDP client message handler.
	 * @param handler Message handler to use
	 */
	public void setUdpClient(final ClientMessageHandler handler) {
		udpClient = handler;
	}

	/**
	 * Adds a method to be called on each frame render.
	 * @param method Method to register
	 */
	public void addRenderMethod(final NothingMethod method) {
		renderMethods.add(method);
	}

	/**
	 * Sets the player instance currently in use.
	 * @param player Player
	 */
	public void setPlayer(final Player player) {
		this.player = player;
	}

	/**
	 * Gets the player instance currently in use.
	 * @return Player
	 */
	public Player getPlayer() {
		if (player == null) throw new CatastrophicException("No player set.");
		return player;
	}

	/**
	 * Sets the API user instance currently in use.
	 * @param user Authenticated user
	 */
	public void setUser(final User user) {
		apiUser = user;
	}

	/**
	 * Gets the API user instance currently in use.
	 * @return Authenticated user
	 */
	public User getUser() {
		if (apiUser == null) throw new CatastrophicException("No user set.");
		return apiUser;
	}

	@Override
	public void create() {
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();

		try {
			// Kill Bill > Spring Boot
			logBanner();

			// Load our textures
			textureLoader.load();

			// Load fonts
			fontLoader.load();

			// Load maps
			mapLoader.load();

			// Initialize all the screens
			Screens.iterAll(screen -> {
				screen.create();
			});

			// Set the current screen
			super.setScreen(Screens._DEFAULT_SCREEN);
			
		} catch (final Throwable t) {
			throw new CatastrophicException("Initialization error: ", t);
		}
	}

	@Override
	public void render() {
		super.render();

		for (final NothingMethod method : renderMethods) {
			try {
				method.run();
			} catch (final Throwable t) {
				throw new CatastrophicException("Failure in render method: ", t);
			}
		}

		// Keep this at the end!
		if (nextScreen != null) {
			screen = nextScreen;
			//throw new RuntimeException("CHANG.e");
			super.setScreen(screen);
			nextScreen = null;
		}
	}
	
	@Override
	public void dispose() {
	}

	/**
	 * A very important method.
	 * @throws FileNotFoundException Couldn't read equally important file
	 */
	private void logBanner() throws FileNotFoundException {
		try (Scanner scanner = new Scanner(Gdx.files.internal("text/kb.txt").read())) {
			while (scanner.hasNextLine()) {
				System.out.println(scanner.nextLine());
			}
		}
	}

	/**
	 * Enables or disables debugging features.
	 * @param state New debug state
	 */
	public void setDebugEnabled(final boolean state) {
		debugEnabled = state;
	}

	/**
	 * Checks if debugging mode is enabled.
	 * @return True = debugging on
	 */
	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	/**
	 * The possible platforms the game can run on.
	 */
	public enum Platform {
		/**
		 * Desktop devices (Linux, Windows, Mac)
		 */
		DESKTOP,

		/**
		 * Android devices
		 */
		ANDROID
	}

	/**
	 * The possible input types we support.
	 */
	public enum InputType {
		/**
		 * Keyboard and mouse
		 */
		KEYBOARD_MOUSE,

		/**
		 * Touchscreens
		 */
		TOUCH
	}

	/**
	 * Functional interface that takes no parameters and returns nothing.
	 */
	public static interface NothingMethod {
		/**
		 * Does something. :)
		 */
		public void run();
	}
}
