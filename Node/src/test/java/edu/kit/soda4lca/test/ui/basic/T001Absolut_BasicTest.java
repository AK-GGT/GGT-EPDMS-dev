package edu.kit.soda4lca.test.ui.basic;

import com.codeborne.selenide.WebDriverRunner;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import java.util.Properties;

import static com.codeborne.selenide.Selenide.open;

/**
 * First test, just look around on the user interface and try to log in with different user/password combinations
 *
 * @author mark.szabo
 */
public class T001Absolut_BasicTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T001Absolut_BasicTest.class);

    /**
     * just look around on the user interface, if everything is there
     */
    @Test(priority = 11)
    public void absolut_Basic() {
        log.info("Absolut Basic test started");
        WebDriverRunner.setWebDriver(driver);
        String site = TestContext.PRIMARY_SITE_URL;
        Properties lang = TestContext.lang;
        // delete previous session
        WebDriverRunner.clearBrowserCache();

        // open the main site
        open(site);

        log.debug("looking for menu and menu items");
        // is the menu there?
        testFunctions.findAndWaitOnElement(By.className("ui-menu"));
        // is the content "Welcome" there?
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='content']"));

        // are all the menuoptions there?

        // Admin area SHOULDN'T be here
        if (testFunctions.isElementNotPresent(By.linkText(lang.getProperty("admin.adminArea"))))
            org.testng.Assert.fail("Admin area button is presented without logging on.");
        // Processes
        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("common.processes")));
        // LCIA Methods
        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("common.lciaMethods")));
        // Elementary Flows
        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("common.elementaryFlows")));
        // Product Flows
        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("common.productFlows")));
        // Flow Properties
        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("common.flowProperties")));
        // Unit Groups
        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("common.unitGroups")));
        // Sources
        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("common.sources")));
        // Contacts
        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("common.contacts")));
        // Search Processes
        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("public.search") + " " + lang.getProperty("common.processes")));
        log.info("Absolut Basic test finished");
    }

    /**
     * try to log in with different user/password combinations
     *
     * @throws InterruptedException
     */
    @Test(priority = 12, dependsOnMethods = {"absolut_Basic"})
    public void basic_Login() throws InterruptedException {
        // Login test
        log.info("Login test started");
        driver.manage().deleteAllCookies();

        log.debug("Basic Login test started - Try to log in with different credentials");
//		testFunctions.login( "admin", "default", true );
//		testFunctions.login( "admin", "admin", false );
//		testFunctions.login( "admin", "", false );
//		testFunctions.login( "somebody", "default", false );
//		testFunctions.login( "", "default", false );
//		testFunctions.login( "somebody", "password", false );
//		testFunctions.login( "", "", false );
        log.info("Basic Login test finished");
    }

}
