package today.tecktip.killbill.backend.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * Basic controller that applies env vars to static variables via Spring Boot.
 * @author cs
 */
@Controller
public class DatabaseEnv {
    /**
     * MySQL username.
     */
    private static String MYSQL_USER;
    /**
     * Used by Spring Boot to store the MySQL user env var.
     * @param mySqlUser MYSQL_USER env var
     */
    @Value("${env_vars.mysql_user}")
    private void setMySqlUser(final String mySqlUser) {
        MYSQL_USER = mySqlUser;
    }
    /**
     * Get the MYSQL_USER environment variable.
     * @return MySQL username.
     */
    public static String getMySqlUser() {
        return MYSQL_USER;
    }
    
    /**
     * MySQL password.
     */
    private static String MYSQL_PASS;
    /**
     * Used by Spring Boot to store the MySQL password env var.
     * @param mySqlUser MYSQL_PASS env var
     */
    @Value("${env_vars.mysql_pass}")
    private void setMySqlPass(final String mySqlPass) {
        MYSQL_PASS = mySqlPass;
    }
    /**
     * Get the MYSQL_PASS environment variable.
     * @return MySQL password.
     */
    public static String getMySqlPass() {
        return MYSQL_PASS;
    }

    /**
     * MySQL host.
     */
    private static String MYSQL_HOST;
    /**
     * Used by Spring Boot to store the MySQL host env var.
     * @param mySqlUser MYSQL_HOST env var
     */
    @Value("${env_vars.mysql_host}")
    private void setMySqlHost(final String mySqlHost) {
        MYSQL_HOST = mySqlHost;
    }
    /**
     * Get the MYSQL_HOST environment variable.
     * @return MySQL host.
     */
    public static String getMySqlHost() {
        return MYSQL_HOST;
    }

    /**
     * MySQL database.
     */
    private static String MYSQL_DB;
    /**
     * Used by Spring Boot to store the MySQL database env var.
     * @param mySqlUser MYSQL_DB env var
     */
    @Value("${env_vars.mysql_db}")
    private void setMySqlDb(final String mySqlDb) {
        MYSQL_DB = mySqlDb;
    }
    /**
     * Get the MYSQL_DB environment variable.
     * @return MySQL database.
     */
    public static String getMySqlDb() {
        return MYSQL_DB;
    }

	/**
	 * This class should not be instantiated manually.
	 */
	public DatabaseEnv() { }
}
