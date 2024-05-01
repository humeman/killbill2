package today.tecktip.killbill.backend.routes.users;

import java.sql.SQLException;
import java.util.regex.Pattern;

import today.tecktip.killbill.backend.db.users.Users;
import today.tecktip.killbill.backend.exceptions.NotFoundException;

/**
 * Static helper methods for validating request data for creating users.
 * @author cs
 */
public class UserValidator {
    /**
     * RegEx pattern for valid usernames.
     */
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]*$");

    /**
     * Validates a username against these rules:
     * <ul>
     *  <li>Between 3-20 characters</li>
     *  <li>Not <code>admin</code></li>
     *  <li>Matches the {@link VALID_NAME_PATTERN}<li>
     * </ul>
     * @param name Name to validate
     * @return True if valid
     */
    public static boolean isValidName(final String name) {
        if (name.length() > 20 || name.length() < 3) return false;
        if (name.toLowerCase().equals("admin")) return false;
        return VALID_NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Checks if a name is taken against the SQL database.
     * @param name Name to check for
     * @return True if the name is taken
     * @throws SQLException Unable to contact database
     */
    public static boolean nameIsTaken(final String name) throws SQLException {
        try {
            Users.getUserByName(name);
        } catch (NotFoundException e) {
            return false;
        }
        return true;
    }

	/**
	 * This class should not be instantiated.
	 */
	private UserValidator() { throw new AssertionError(); }
}
