package today.tecktip.killbill.frontend.natives;

import java.io.File;
import java.time.Instant;
import java.util.Locale;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.CreateParams.Flags;
import de.jcm.discordgamesdk.activity.Activity;

/**
 * Sets up discord presence for the game. Only compatible with desktop platforms
 *  (for obvious reasons).
 * 
 * @author cs
 */
public class DiscordPresence {
    private boolean initialized;

    private Core core;

    public DiscordPresence() {
        initialized = false;
        String suffix;

        final String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        if (osName.contains("windows")) {
            suffix = ".dll";
        } else if (osName.contains("linux")) {
            suffix = ".so";
        } else if (osName.contains("mac os")) {
            suffix = ".dylib";
        } else {
            throw new RuntimeException("Invalid OS name (can't enable discord presence): " + osName);
        }

        if (arch.equals("amd64")) {
			arch = "x86_64";
        }

		Core.init(new File("lib/discord_game_sdk/lib/" + arch + "/discord_game_sdk" + suffix));
        initialized = true;
    }

    public void setPresence() {
        if (!initialized) return;

        try (final CreateParams params = new CreateParams()) {
            params.setClientID(1221600237806944448L);
            params.setFlags(Flags.NO_REQUIRE_DISCORD);

            core = new Core(params);

            try (final Activity activity = new Activity()) {
                activity.setDetails("Spending quality time with Bill.");
                activity.setState("Do not contact me.");
                activity.timestamps().setStart(Instant.now());
                activity.assets().setLargeImage("title");
                core.activityManager().updateActivity(activity);
            }
        }
    }
    public void runCallbacks() {
        if (!initialized) return;
        try {
            core.runCallbacks();
        } catch (final Throwable t) {
            System.err.println("Discord presence crash: " + t);
            initialized = false;
        }
    }
}
