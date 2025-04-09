package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Listeners({ScreenShooter.class})
public class T041SearchProcessesTest extends AbstractUITest {

    protected final static Logger log = LogManager.getLogger(T041SearchProcessesTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    @Test(priority = 411)
    public void searchProcesses() throws InterruptedException {
        log.info("'Search processes' test started");
        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        Thread.sleep(3 * TestContext.wait);

        driver.get(TestContext.PRIMARY_SITE_URL + "?stock=RootStock1");

        searchProcess("test", 2, "1999", "2002", "LCI result", false);
        searchProcess("", -1, "", "", "LCI result", true);

        log.info("'Search processes' test finished");
    }

    // @Test( priority = 412, dependsOnMethods = { "searchProcesses" } )
    public void searchProcessesAcrossNetwork() throws InterruptedException {
        log.info("'Search processes across network' test started");
        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        Thread.sleep(3 * TestContext.wait);

        // searchProcessAcrossNetwork( "", "", -1, "", "", "", false, true );
        // searchProcessAcrossNetwork( "registry1", "test", 2, "1999", "2005", "LCI result", false, false );
        // searchProcessAcrossNetwork( "registry1", "", -1, "", "", "", true, false );

        log.info("'Search processes across network' test finished");
    }

    public void searchProcess(String name, int location, String yearLower, String yearUpper, String type, boolean correct) throws InterruptedException {
        log.trace("Searching process ");

        Actions action = new Actions(driver);

        String searchLabel = TestContext.lang.getProperty("public.search") + " " + TestContext.lang.getProperty("common.processes");
        action.moveToElement(driver.findElement(By.linkText(searchLabel))).build()
                .perform();
        testFunctions.findAndWaitOnElement(By.xpath("//a[contains(.,'" + searchLabel + "')]")).click();
        Thread.sleep(TestContext.wait);

        // Fill in the form
        if (name.length() > 0) {
            testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='name']")).clear();
            testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='name']")).sendKeys(name);
        }
        if (location != -1) {
            testFunctions.findAndWaitOnElement(By.xpath("//td//div[@id='location']//ul/li[contains(.,'" + location + "')]")).click();
        }
        if (yearLower.length() > 0) {
            testFunctions.findAndWaitOnElement(By.xpath("//label[@id='referenceYearLower_label']")).click();
            testFunctions.findAndWaitOnElement(By.xpath(".//div[@id='referenceYearLower_panel']/div/ul/li[contains(.,'" + yearLower + "')]")).click();
            Thread.sleep(TestContext.wait);
        }
        if (yearUpper.length() > 0) {
            testFunctions.findAndWaitOnElement(By.xpath("//label[@id='referenceYearUpper_label']")).click();
            testFunctions.findAndWaitOnElement(By.xpath(".//div[@id='referenceYearUpper_panel']/div/ul/li[contains(.,'" + yearUpper + "')]")).click();
            Thread.sleep(TestContext.wait);
        }
        if (type.length() > 0) {
            testFunctions.findAndWaitOnElement(By.xpath("//label[@id='type_label']")).click();
            testFunctions.findAndWaitOnElement(By.xpath(".//div[@id='type_panel']/div/ul/li[contains(.,'" + type + "')]")).click();
            Thread.sleep(TestContext.wait);
        }
        Thread.sleep(3 * TestContext.wait);

        // Click Save
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("public.search") + "')]")).click();
        Thread.sleep(3 * TestContext.wait);
        // check the result

        if (correct) {
            (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='processTable_data']")));
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='processTable_data']//td[1]")).isDisplayed())
                org.testng.Assert.fail("Error.");
            else {
                log.trace("Process searched succesfull");
            }
        } else {
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='processTable_data']//td")).getText().contains(
                    TestContext.lang.getProperty("common.list.noneFound").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        }
    }

    public void searchProcessAcrossNetwork(String registry, String name, int location, String yearLower, String yearUpper, String type, boolean correct,
                                           boolean lackOfReg)
            throws InterruptedException {
        log.trace("Searching process across network ");

        Actions action = new Actions(driver);

        String searchLabel = TestContext.lang.getProperty("public.search") + " " + TestContext.lang.getProperty("common.processes");
        action.moveToElement(driver.findElement(By.linkText(searchLabel))).build().perform();
        testFunctions.findAndWaitOnElement(By.xpath("//a[contains(.,'" + searchLabel + "')]")).click();
        Thread.sleep(TestContext.wait);

        // Fill in the form
        if (name.length() > 0) {
            testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='name']")).clear();
            testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='name']")).sendKeys(name);
        }
        if (location != -1) {
            testFunctions.findAndWaitOnElement(By.xpath("//td/div/ul/li[contains(.,'" + location + "')]")).click();
        }
        if (yearLower.length() > 0) {
            testFunctions.findAndWaitOnElement(By.xpath("//label[@id='referenceYearLower_label']")).click();
            testFunctions.findAndWaitOnElement(By.xpath(".//div[@id='referenceYearLower_panel']/div/ul/li[contains(.,'" + yearLower + "')]")).click();
            Thread.sleep(TestContext.wait);
        }
        if (yearUpper.length() > 0) {
            testFunctions.findAndWaitOnElement(By.xpath("//label[@id='referenceYearUpper_label']")).click();
            testFunctions.findAndWaitOnElement(By.xpath(".//div[@id='referenceYearUpper_panel']/div/ul/li[contains(.,'" + yearUpper + "')]")).click();
            Thread.sleep(TestContext.wait);
        }
        if (type.length() > 0) {
            testFunctions.findAndWaitOnElement(By.xpath("//label[@id='type_label']")).click();
            testFunctions.findAndWaitOnElement(By.xpath(".//div[@id='type_panel']/div/ul/li[contains(.,'" + type + "')]")).click();
            Thread.sleep(TestContext.wait);
        }
        Thread.sleep(3 * TestContext.wait);

        testFunctions.findAndWaitOnElement(By.xpath("//input[@id='distributed']")).click();
        Thread.sleep(TestContext.wait);
        if (registry.length() > 0) {
            new Select(testFunctions.findAndWaitOnElement(By.xpath("//select[@id='reg']"))).selectByVisibleText(registry);
        }
        // Click Save
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("public.search") + "')]")).click();
        // wait a little
        Thread.sleep(3 * TestContext.wait);

        // check the resut

        if (correct) {
            (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='processTable_data']")));
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='processTable_data']//td[1]")).isDisplayed())
                org.testng.Assert.fail("Error.");
            else {
                log.trace("Process searched succesfull");
            }
        } else if (lackOfReg) {
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains("reg: Validation Error: Value is required."))
                org.testng.Assert.fail("Error.");
        } else {
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='processTable_data']//td")).getText().contains(
                    TestContext.lang.getProperty("common.list.noneFound").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        }
    }

}
