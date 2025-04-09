package edu.kit.soda4lca.test.ui.basic;

import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.util.Properties;


/**
 * Try to log in with different user/password combinations with rememberme flag set or not set
 *
 * @author sarai.eilebrecht
 */
public class T002RememberMeTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T002RememberMeTest.class);


    /**
     * try to log in with different user/password combinations with rememberme flag set or not set and
     * try to update page and see link to admin area after session expires
     *
     * @throws InterruptedException
     */
    @Test
    public void rememberMe() throws InterruptedException {
        // Login test with rememberme
        // Session needs to expire to test whether rememberme is correctly set.
        // Hence thread must wait at least as long as timeout.
        Properties lang = TestContext.lang;
        TestContext testContext = TestContext.getInstance();
        WebDriver ff = testContext.getDriver();

        log.info("Login test started");
        driver.manage().deleteAllCookies();

        log.debug("Basic Remember Me test started - Try to log in with different credentials");

        testFunctions.login("admin", "default", true, true, 1, true);
        Thread.sleep(90000);
        ff.navigate().refresh();
        log.debug("Page is refreshed after first login with rememberme flag set");
        if (!testFunctions.isElementNotPresent(By.linkText(lang.getProperty("admin.adminArea"))))
            org.testng.Assert.fail("Admin area button is not presented but rememberme is set.");

        ff.manage().deleteAllCookies();
        ff.navigate().refresh();
        log.debug("Page is refreshed after all cookies are deleted");
        if (testFunctions.isElementNotPresent(By.linkText(lang.getProperty("admin.adminArea"))))
            org.testng.Assert.fail("Admin area button is presented after rememberme cookie is deleted.");
        testFunctions.login("admin", "default", true, true, 1, false);
        Thread.sleep(90000);
        ff.navigate().refresh();
        log.debug("Page is refreshed after login without rememberme flag set");
        if (testFunctions.isElementNotPresent(By.linkText(lang.getProperty("admin.adminArea"))))
            org.testng.Assert.fail("Admin area button is presented after session is expired.");
        testFunctions.login("admin", "admin", false, true, 1, true);
        log.debug("Try to log in with incorrect password");
        if (testFunctions.isElementNotPresent(By.linkText(lang.getProperty("admin.adminArea"))))
            org.testng.Assert.fail("Admin area button is presented but password is not correct.");
        log.info("Basic Remember Me test finished");
    }

}
