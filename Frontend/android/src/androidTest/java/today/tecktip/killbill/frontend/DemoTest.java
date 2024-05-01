package today.tecktip.killbill.frontend;


import androidx.test.rule.ActivityTestRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class DemoTest {
    @Rule
    public ActivityTestRule<AndroidLauncher> activityRule = new ActivityTestRule<>(AndroidLauncher.class);

    @Test
    public void printGame() {
        System.out.println(AndroidLauncher.getGame());
        throw new RuntimeException();
    }
}