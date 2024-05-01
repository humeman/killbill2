package today.tecktip.killbill.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entrypoint for the Kill Bill 2 API SpringBoot application.
 * 
 * @author cs
 */
@SpringBootApplication
@EnableIntegration
@EnableScheduling
public class Application {
	/**
	 * Executes the Kill Bill API.
	 * <p>
	 * It is expected that the following environment variables are set when this is called:
	 * <ul>
	 * 	<li><code>MYSQL_HOST</code>: MySQL host to connect to</li>
	 * 	<li><code>MYSQL_USER</code>: MySQL database user to sign in with</li>
	 * 	<li><code>MYSQL_PASS</code>: MySQL user password</li>
	 * 	<li><code>MYSQL_DB</code>: MySQL database name</li>
	 * 	<li><code>ADMIN_INIT_KEY</code>: Authorization key for <code>/admin</code> methods</li>
	 * </ul>
	 * @param args Commandline args. Ignored at this time.
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * This class should not be instantiated manually.
	 */
	public Application() { }
}
