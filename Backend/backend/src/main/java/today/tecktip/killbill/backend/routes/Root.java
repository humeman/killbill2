package today.tecktip.killbill.backend.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import today.tecktip.killbill.backend.Application;

/**
 * Handles basic static calls at the root of the API.
 * 
 * @author cs
 */
@RestController
@RequestMapping("/")
public class Root {
	/**
	 * The current version sent to users on the API.
	 * <p>
	 * Set via the VERSION env var. Auto-defined when using Gradle.
	 */
    @Value("${env_vars.version}")
    private String VERSION;

    /**
     * Record representing the server status response for <code>GET /</code> calls.
     * @param name Package name the API is running on
     * @param version Package version the API is running on
     */
    public record Status(String name, String version) {}

    /**
     * Returns basic status for the server.
     * @return Status response body
     */
    @GetMapping("/")
    MessageBody root() {
        Status status = new Status(Application.class.getName(), VERSION);

        return MessageBody.ofSuccess(status);
    }

	/**
	 * This class should not be instantiated manually.
	 */
	public Root() { }
}
