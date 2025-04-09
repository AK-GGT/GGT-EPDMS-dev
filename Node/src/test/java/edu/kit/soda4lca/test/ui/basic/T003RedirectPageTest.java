package edu.kit.soda4lca.test.ui.basic;

import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Properties;

/**
 * Try to get redirected to requested page in admin area directly after login
 *
 * @author sarai.eilebrecht
 */
public class T003RedirectPageTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T003RedirectPageTest.class);


    /**
     * try to get to requested page directly after login with different user/password combinations
     *
     * @throws InterruptedException
     */
    @Test
    public void redirectpage() throws InterruptedException {
        // Redirect Page test
        log.info("redirect test started");
        driver.manage().deleteAllCookies();
        Properties lang = TestContext.lang;
        driver.get("http://localhost:8080/Node/admin/datasets/manageProcessList.xhtml?stock=default");
        testFunctions.loginOnLoginSite("admin", "default", false);
        log.debug("Redirect Page test started - Try to get to Manage Processes page with different credentials");
        testFunctions.findAndWaitOnElement(By.id("processTableForm"));
        testFunctions.logout();
        driver.get("http://localhost:8080/Node/admin/datasets/manageProcessList.xhtml?stock=default");
        testFunctions.loginOnLoginSite("admin", "", false);
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='messages']/div")));
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));
        if (testFunctions.isElementNotPresent(By.linkText(lang.getProperty("admin.adminArea"))))
            org.testng.Assert.fail("Admin area button is presented but password is incorrect.");
        if (testFunctions.isElementNotPresent(By.id("processTableForm")))
            org.testng.Assert.fail("Manage Processes is presented but password is incorrect.");
        log.info("Redirect Page test finished");
    }

}

