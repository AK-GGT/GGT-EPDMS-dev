package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration test - change the default stock
 *
 * @author mark.szabo
 */
@Listeners({ScreenShooter.class})
public class T016ConfigurationTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T016ConfigurationTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    ;

    /**
     * Configuration test - change the default stock
     *
     * @throws InterruptedException
     */
    @Test(priority = 161)
    public void configuration() throws InterruptedException {
        log.info("'Global configuration - change default data stock' test started");

        /*
         * TODO when running the whole testsuite T016ConfigurationTest fails with a strange error: [Exception...
         * "Component returned failure code: 0x80004005 (NS_ERROR_FAILURE) [nsINativeMouse.click]" nsresult:
         * "0x80004005 (NS_ERROR_FAILURE)" location:
         * "JS frame :: file:///C:/Users/mark.szabo/AppData/Local/Temp/anonymous2900692300178494497webdriver-profile/extensions/fxdriver@googlecode.com/components/command_processor.js :: WebElement.clickElement :: line 10257"
         * data: no](..)
         * but if I run the test alone, it runs fine. So possible reason: previous test is Export, and saving file
         * automatically cause this error.
         * More: https://code.google.com/p/selenium/issues/detail?id=6420
         */

        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        // click on the menu
        Actions action = new Actions(driver);
        // Hover over the menu
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.globalConfig")))).build().perform();
        // Click Configuration
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.config")))).click().build().perform();
        log.debug("Change the default stock");
        // click the list
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='selectRootDataStock_label']")).click();
        // find RootStock1
        int i = 1;
        while (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='selectRootDataStock_panel']/div/ul/li[" + i + "]")).getText().endsWith(
                "RootStock1"))
            i++;
        // click RootStock1
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='selectRootDataStock_panel']/div/ul/li[" + i + "]")).click();
        // click Save configuration
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.config.saveConfig") + "')]")).click();
        // wait for the message
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(".//*[@id='messages']")));
        // check the message
        if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText()
                .equals(TestContext.lang.getProperty("facesMsg.config.saveSuccess")))
            org.testng.Assert.fail("Wrong message: " + testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText());
        // test, if it really changed
        log.debug("Default stock changed, check it on the user interface");
        // go to user interface
        driver.manage().deleteAllCookies();
        driver.get(TestContext.PRIMARY_SITE_URL);
        // login as admin
        testFunctions.login("admin", "default", true, true);
        // find the actual rootstock
        if (!testFunctions.findAndWaitOnElement(By.xpath("//label[contains(@id, 'selectDataStock_label')]")).getText().equals("RootStock1"))
            org.testng.Assert.fail("After changeing the default datastock on the admin side it doesn't take effekt on the user interface");
        // check some data
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("common.flowProperties"))).click();
        for (i = 1; i < 6; i++)
            testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='flowpropertyTable_data']/tr[" + i + "]/td[1]"));

        log.info("'Global configuration - change default data stock' test finished");
    }

}
