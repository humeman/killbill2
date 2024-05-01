package today.tecktip.killbill.frontend;


import static org.junit.Assert.*;

import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.requests.GameRequests;
import today.tecktip.killbill.frontend.http.requests.data.Friend;
import today.tecktip.killbill.frontend.http.requests.data.User;
import today.tecktip.killbill.frontend.screens.KillBillScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.screens.menu.CreateGameScreen;
import today.tecktip.killbill.frontend.screens.menu.FriendScreen;
import today.tecktip.killbill.frontend.screens.menu.LoginScreen;
import today.tecktip.killbill.frontend.screens.menu.MainMenuScreen;
import today.tecktip.killbill.frontend.screens.menu.SignupScreen;
import today.tecktip.killbill.frontend.screens.menu.UserScreen;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class MeTest {
    @Rule
    public ActivityTestRule<AndroidLauncher> activityRule = new ActivityTestRule<>(AndroidLauncher.class);

    public void waitForScreenChange(KillBillScreen to) throws InterruptedException {
        int total = 0;
        while (true) {
            try {
                if (KillBillGame.get().getCurrentScreen() == to) break;
            } catch (CatastrophicException e) {}

            TimeUnit.SECONDS.sleep(1);
            total++;

            if (total > 15) {
                fail("Timed out while waiting for screen");
            }
        }
    }

    @Test
    public void testSignIn() throws InterruptedException {
        System.out.println(AndroidLauncher.getGame());

        while (AndroidLauncher.getGame() == null);
        KillBillGame game = AndroidLauncher.getGame();

        while (true) {
            try {
                KillBillGame.get().getCurrentScreen();
                break;
            } catch (Throwable t) {}
        }

        waitForScreenChange(Screens.MAIN_MENU_SCREEN);

        MainMenuScreen mainmenu = (MainMenuScreen) game.getCurrentScreen();
        mainmenu.signInButton.onClicked();

        waitForScreenChange(Screens.LOGIN_SCREEN);

        LoginScreen loginScreen = (LoginScreen) game.getCurrentScreen();

        loginScreen.usernameInput.setText("test1");
        loginScreen.passwordInput.setText("password");

        TimeUnit.SECONDS.sleep(1);

        loginScreen.proceedButton.onClicked();

        waitForScreenChange(Screens.USER_SCREEN);

        UserScreen userScreen = (UserScreen) game.getCurrentScreen();

        // Wait for stuff to populate
        TimeUnit.SECONDS.sleep(3);

        assertEquals("test1", userScreen.username.getText());
    }

    @Test
    public void testSignUp() throws InterruptedException {
        System.out.println(AndroidLauncher.getGame());

        while (AndroidLauncher.getGame() == null);
        KillBillGame game = AndroidLauncher.getGame();

        while (true) {
            try {
                KillBillGame.get().getCurrentScreen();
                break;
            } catch (Throwable t) {}
        }

        waitForScreenChange(Screens.MAIN_MENU_SCREEN);

        MainMenuScreen mainmenu = (MainMenuScreen) game.getCurrentScreen();
        mainmenu.signUpButton.onClicked();

        waitForScreenChange(Screens.SIGNUP_SCREEN);

        SignupScreen signupScreen = (SignupScreen) game.getCurrentScreen();

        Random rand = new Random();

        String username = "testUser" + rand.nextInt(1000000);

        signupScreen.emailInput.setText("test@test.test");
        signupScreen.usernameInput.setText(username);
        signupScreen.passwordInput.setText("password");

        TimeUnit.SECONDS.sleep(1);

        signupScreen.proceedButton.onClicked();

        waitForScreenChange(Screens.USER_SCREEN);

        UserScreen userScreen = (UserScreen) game.getCurrentScreen();

        // Wait for stuff to populate
        TimeUnit.SECONDS.sleep(3);

        assertEquals(username, userScreen.username.getText());
    }

    @Test
    public void testCreateGame() throws InterruptedException {
        System.out.println(AndroidLauncher.getGame());

        while (AndroidLauncher.getGame() == null);
        KillBillGame game = AndroidLauncher.getGame();

        while (true) {
            try {
                KillBillGame.get().getCurrentScreen();
                break;
            } catch (Throwable t) {}
        }

        waitForScreenChange(Screens.MAIN_MENU_SCREEN);

        MainMenuScreen mainmenu = (MainMenuScreen) game.getCurrentScreen();
        mainmenu.signUpButton.onClicked();

        waitForScreenChange(Screens.SIGNUP_SCREEN);

        SignupScreen signupScreen = (SignupScreen) game.getCurrentScreen();

        Random rand = new Random();

        String username = "testUser" + rand.nextInt(1000000);

        signupScreen.emailInput.setText("test@test.test");
        signupScreen.usernameInput.setText(username);
        signupScreen.passwordInput.setText("password");

        TimeUnit.SECONDS.sleep(1);

        signupScreen.proceedButton.onClicked();

        waitForScreenChange(Screens.USER_SCREEN);

        UserScreen userScreen = (UserScreen) game.getCurrentScreen();

        // Wait for stuff to populate
        TimeUnit.SECONDS.sleep(3);

        // Go to the game thing
        userScreen.createGameButton.onClicked();

        waitForScreenChange(Screens.CREATE_GAME_SCREEN);

        CreateGameScreen cgScreen = (CreateGameScreen) game.getCurrentScreen();

        cgScreen.gameName.setText("my test game");
        cgScreen.username.setText("test1");
        cgScreen.addUserButton.onClicked();
        // wait
        TimeUnit.SECONDS.sleep(3);

        // make sure he's there
        boolean valid = false;
        for (final User u : cgScreen.invitedUsers) {
            if (u.name().equals("test1")) {
                valid = true;
                break;
            }
        }
        if (!valid) fail("he is not in the game.");

        cgScreen.createGameButton.onClicked();

        // WAIT
        waitForScreenChange(Screens.USER_SCREEN);

        // wait for populate
        TimeUnit.SECONDS.sleep(3);

        // make sure the game got made
        valid = false;
        for (final GameRequests.ListGamesResponseInnerObject gameObj : userScreen.games) {
            if (gameObj.game().name().equals("my test game")) {
                valid = true;
                break;
            }
        }
        if (!valid) fail("Game didn't get created");
    }

    @Test
    public void testAddFriend() throws InterruptedException {
        System.out.println(AndroidLauncher.getGame());

        while (AndroidLauncher.getGame() == null);
        KillBillGame game = AndroidLauncher.getGame();

        while (true) {
            try {
                KillBillGame.get().getCurrentScreen();
                break;
            } catch (Throwable t) {}
        }

        waitForScreenChange(Screens.MAIN_MENU_SCREEN);

        MainMenuScreen mainmenu = (MainMenuScreen) game.getCurrentScreen();
        mainmenu.signUpButton.onClicked();

        waitForScreenChange(Screens.SIGNUP_SCREEN);

        SignupScreen signupScreen = (SignupScreen) game.getCurrentScreen();

        Random rand = new Random();

        String username = "testUser" + rand.nextInt(1000000);

        signupScreen.emailInput.setText("test@test.test");
        signupScreen.usernameInput.setText(username);
        signupScreen.passwordInput.setText("password");

        TimeUnit.SECONDS.sleep(1);

        signupScreen.proceedButton.onClicked();

        waitForScreenChange(Screens.USER_SCREEN);

        UserScreen userScreen = (UserScreen) game.getCurrentScreen();
        TimeUnit.SECONDS.sleep(3);

        userScreen.friendsButton.onClicked();

        waitForScreenChange(Screens.FRIEND_SCREEN);

        FriendScreen fScreen = (FriendScreen) game.getCurrentScreen();

        fScreen.search.setText("test1");
        fScreen.addFriendButton.onClicked();
        // wait
        TimeUnit.SECONDS.sleep(3);

        // make sure he's there
        boolean valid = false;
        for (final Friend f : fScreen.friends) {
            if (f.fromId().equals(KillBillGame.get().getUser().id())) {
                assertEquals(Friend.FriendState.INVITED, f.state());
                valid = true;
                break;
            }
        }
        if (!valid) fail("he is not my friend.");
    }
}