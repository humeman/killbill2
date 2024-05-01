package today.tecktip.killbill.backend.routes.games;

import java.util.regex.Pattern;

/**
 * Static helper methods for validating request data for creating games.
 * @author cs
 */
public class GameValidator {
    /**
     * RegEx pattern for valid game names.
     */
    private static final Pattern VALID_GAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9 \\-_!@#$%^&*()<>,.;:'\\\"\\[\\]{}/?|+=~]*$"
    );

    /**
     * Validates a game name against these rules:
     * <ul>
     *  <li>Between 3-64 characters</li>
     *  <li>Matches the {@link VALID_GAME_PATTERN}<li>
     * </ul>
     * @param name Name to validate
     * @return True if valid
     */
    public static boolean isValidName(final String name) {
        if (name.length() > 64 || name.length() < 3) return false;
        return VALID_GAME_PATTERN.matcher(name).matches();
    }

	/**
	 * This class should not be instantiated.
	 */
	private GameValidator() { throw new AssertionError(); }
}
