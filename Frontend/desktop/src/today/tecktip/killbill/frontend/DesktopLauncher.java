package today.tecktip.killbill.frontend;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import today.tecktip.killbill.common.gameserver.ClasspathCommandLoader;
import today.tecktip.killbill.frontend.KillBillGame.InputType;
import today.tecktip.killbill.frontend.KillBillGame.Platform;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.natives.DesktopNativeHttpClient;
import today.tecktip.killbill.frontend.natives.DiscordPresence;

/**
 * Launches the libGDX core game on desktop platforms.
 */
public class DesktopLauncher {
	/**
	 * Starts the game.
	 * @param arg Commandline args. Not used at this time.
	 */
	public static void main(String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setResizable(true);
		config.setTitle("Kill Bill 2");

		// "http://192.3.113.200:8080"
		NativeHttpClient httpClient = new DesktopNativeHttpClient("http://207.244.252.28:31218");

		// Discord stuff
		DiscordPresence presence = null;
		try {
			presence = new DiscordPresence();
			presence.setPresence();
		} catch (final Throwable t) {
			t.printStackTrace();
			System.err.println("Unable to start Discord presence. Skipping.");
		}

		final ClasspathCommandLoader loader = new ClasspathCommandLoader();
		try {
			loader.load("today.tecktip.killbill.frontend");
		} catch (final Throwable t) {
			throw new CatastrophicException("Unable to scan classpath for UDP commands: ", t);
		}
		final KillBillGame game = new KillBillGame(Platform.DESKTOP, InputType.KEYBOARD_MOUSE, httpClient, loader);
		if (presence != null)
			game.addRenderMethod(presence::runCallbacks);

		new Lwjgl3Application(game, config);
	}

	/**
	 * This class should not be instantiated.
	 */
	private DesktopLauncher() { throw new AssertionError(); }
}
