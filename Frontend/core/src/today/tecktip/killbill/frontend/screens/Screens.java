package today.tecktip.killbill.frontend.screens;

import java.lang.reflect.Field;

import today.tecktip.killbill.frontend.screens.menu.FriendScreen;
import today.tecktip.killbill.frontend.screens.menu.GameEndScreen;
import today.tecktip.killbill.frontend.screens.menu.LobbyLoadingScreen;
import today.tecktip.killbill.frontend.screens.game.DevGameScreen;
import today.tecktip.killbill.frontend.screens.game.UdpGameScreen;
import today.tecktip.killbill.frontend.screens.menu.ChatScreen;
import today.tecktip.killbill.frontend.screens.menu.CreateGameScreen;
import today.tecktip.killbill.frontend.screens.menu.DevScreen;
import today.tecktip.killbill.frontend.screens.menu.EditProfileScreen;
import today.tecktip.killbill.frontend.screens.menu.LobbyScreen;
import today.tecktip.killbill.frontend.screens.menu.LoginScreen;
import today.tecktip.killbill.frontend.screens.menu.LogoScreen;
import today.tecktip.killbill.frontend.screens.menu.MainMenuScreen;
import today.tecktip.killbill.frontend.screens.menu.SignupScreen;
import today.tecktip.killbill.frontend.screens.menu.TestScreen;
import today.tecktip.killbill.frontend.screens.menu.UdpErrorScreen;
import today.tecktip.killbill.frontend.screens.menu.UserScreen;

/**
 * Static references to instances of each of our screen types.
 * Note that all actions requring KillBillGame to be initialized should use a <code>create()</code> method.
 * @author cs
 */
public class Screens {
    /* ----- Put your screens here ----- */

    /**
     * A simple testing screen.
     */
    public static final TestScreen TEST_SCREEN = new TestScreen();

    /**
     * Main menu screen, redirects to LOGIN or SIGNUP
     */
    public static final MainMenuScreen MAIN_MENU_SCREEN = new MainMenuScreen();

    /**
     * Logo Screen
     */
    public static final LogoScreen LOGO_SCREEN = new LogoScreen();

    /**
     * Login Screen
     */
    public static final LoginScreen LOGIN_SCREEN = new LoginScreen();
    /**
     * chat Screen
     */
    public static final ChatScreen CHAT_SCREEN = new ChatScreen();

    /**
     * Friend screen
     */
    public static final FriendScreen FRIEND_SCREEN = new FriendScreen();

    /**
     * Game Screen
     */
    public static final LobbyScreen LOBBY_SCREEN = new LobbyScreen();

    /**
     * Map viewing game screen
     */
    public static final DevGameScreen DEV_GAME_SCREEN = new DevGameScreen();

    /**
     * Loads up the dev game screen
     */
    public static final DevScreen DEV_SCREEN = new DevScreen();

    /**
     * Signup screen
     */
    public static final SignupScreen SIGNUP_SCREEN = new SignupScreen();

    /**
     * User screen
     */
    public static final UserScreen USER_SCREEN = new UserScreen();

    /**
     * Loading into game
     */
    public static final LobbyLoadingScreen LOBBY_LOADING_SCREEN = new LobbyLoadingScreen();

    /**
     * Create game screen
     */
    public static final CreateGameScreen CREATE_GAME_SCREEN = new CreateGameScreen();

    /**
     * Game screen
     */
    public static final UdpGameScreen UDP_GAME_SCREEN = new UdpGameScreen();

    /**
     * Edit profile screen
     */
    public static final EditProfileScreen EDIT_PROFILE_SCREEN = new EditProfileScreen();

    /**
     * Game over screen
     */
    public static final GameEndScreen GAME_END_SCREEN = new GameEndScreen();

    /**
     * UDP error screen
     */
    public static final UdpErrorScreen UDP_ERROR_SCREEN = new UdpErrorScreen();

    /* ----- Don't touch these ----- */
    /**
     * The default screen to open when the game starts.
     */
    public static final KillBillScreen _DEFAULT_SCREEN = LOGO_SCREEN;

    /**
     * Iterates over all screens registered here.
     * @param method Method to call on each screen
     */
    public static void iterAll(final ScreenOperatorMethod method) throws IllegalAccessException {
        for (Field field : Screens.class.getDeclaredFields()) {
            if (field.getName().endsWith("_SCREEN") && !field.getName().startsWith("_")) {
                // If this is a valid screen, operate on it
                if (KillBillScreen.class.isAssignableFrom(field.getType()))
                    method.operate((KillBillScreen) field.get(null));
            }
        }
    }

    /**
     * Lambda method interface for interacting with screens dynamically.
     */
    public static interface ScreenOperatorMethod {
        /**
         * Executes a task on a screen.
         * @param screen Screen to use
         */
        public void operate(final KillBillScreen screen);
    }
}