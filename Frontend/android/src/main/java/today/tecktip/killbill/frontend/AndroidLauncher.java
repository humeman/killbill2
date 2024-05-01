package today.tecktip.killbill.frontend;

import android.os.Bundle;
import android.os.StrictMode;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.gameserver.HardcodedCommandLoader;
import today.tecktip.killbill.frontend.natives.AndroidNativeHttpClient;

/**
 * Launches the libGDX app on Android.
 */
public class AndroidLauncher extends AndroidApplication {

	private static KillBillGame gameInstance = null;

	public static KillBillGame getGame() {
		return gameInstance;
	}

	/**
	 * Executed on application create.
	 * @param savedInstanceState This probably does something
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;

		// Create natives
		NativeHttpClient httpClient = new AndroidNativeHttpClient("http://207.244.252.28:31218", getApplicationContext());

		HardcodedCommandLoader commandLoader = new HardcodedCommandLoader();
		commandLoader.populate();

		gameInstance = new KillBillGame(KillBillGame.Platform.ANDROID, KillBillGame.InputType.TOUCH, httpClient, commandLoader);
		initialize(gameInstance, config);
	}
}
