package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Delete a dataset from every type and test if it gets really deleted
 *
 * @author mark.szabo
 */
@Listeners({ScreenShooter.class})
public class T030ManageDataSetsTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T030ManageDataSetsTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    @Test(priority = 301)
    public void manageProcesses() throws InterruptedException {
        manageSomething("process", "process", false);
    }

    @Test(priority = 302)
    public void manageLCIAMethods() throws InterruptedException {
        manageSomething("lciaMethod", "lciamethod", false);
    }

    @Test(priority = 303)
    public void manageElementaryFlows() throws InterruptedException {
        manageSomething("elementaryFlow", "flow", false);
    }

    @Test(priority = 304)
    public void manageProductFlows() throws InterruptedException {
        manageSomething("productFlow", "flow", false);
    }

    @Test(priority = 305)
    public void manageFlowProperties() throws InterruptedException {
        manageSomething("flowProperty", "flowproperty", false);
    }

    @Test(priority = 306)
    public void manageUnitGroups() throws InterruptedException {
        manageSomething("unitGroup", "unitgroup", true);
    }

    @Test(priority = 307)
    public void manageSources() throws InterruptedException {
        manageSomething("source", "source", false);
    }

    @Test(priority = 308)
    public void manageContacts() throws InterruptedException {
        manageSomething("contact", "contact", false);
    }

    /**
     * Delete a dataset from every type and test if it gets really deleted. It's almost the same for every type, so this
     * method can delete from which you want
     *
     * @param menu          Language property "admin." + menu + ".manageList" contains the menu link text
     * @param link          Table id: "[@id='" + link+ "Table_data']"
     * @param isitUnitGroup usually almost any dataset can be deleted. But because of dependencies by unit Group we have to delete
     *                      a specified one
     * @throws InterruptedException when one of the several waits is interrupted
     */
    public void manageSomething(String menu, String link, boolean isitUnitGroup) throws InterruptedException {
        driver.manage().deleteAllCookies();
        log.info(TestContext.lang.getProperty("admin." + menu + ".manageList") + " test started");
        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        navigateToManageDataSetsView(menu);
        log.trace("Change datastock");

        Thread.sleep(TestContext.wait * 2);

        // Choose data stock RootStock1
//			testFunctions.findAndWaitOnElement( By.xpath( ".//*[@id='admin_header']/div[2]//table/tbody/tr[1]/td[2]/div" ) ).click();
        testFunctions.findAndWaitOnElement(By.xpath("//*[contains(@class, 'selectDataStockHTMLClass')]")).click();
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@data-label='RootStock1']")).click();

        // wait
        Thread.sleep(TestContext.wait * 2);

        // testFunctions.findandwaitanElement(By.xpath(".//*[ends-with(@id, 'selectDataStock_panel')]/div/ul/li[2]")).click();
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='" + link
                + "Table_data']/tr[1]/td[1]/div/div[2]/span")));
        /*
         * UnitGroups has a lot of dependencies,
         * so we have to delete a specific one, which can be deleted
         * it will be "Units of volume*length"
         * in other cases just delete the first one
         */
        String uuid;
        log.trace("select dataset");

        // read the UUID of the second element
        uuid = testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='" + link + "Table:1:nameColumnContentUuid']")).getText();
        // select the first item
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='" + link + "Table_data']/tr[1]/td[1]/div/div[2]/span")).click();

        // wait for Delete button to enabled
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.elementToBeClickable(By.xpath(".//*[@id='" + link
                + "Tablebtn']")));
        // click Delete Selected Entries
        log.trace("Click delete");
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='" + link + "Tablebtn']")).click();

        // wait
        Thread.sleep(TestContext.wait * 2);

        // Sure? Click OK
        WebElement btnOk = testFunctions.findDialogButton(TestContext.lang.getProperty("admin.confirmDlg.delete"), TestContext.lang.getProperty("admin.ok"));
        btnOk.click();

        Thread.sleep(TestContext.wait);

        // wait
        Thread.sleep(TestContext.wait * 2);

        // check results
        navigateToJobsOverview();
        testFunctions.getValidStringWaiting(
                () -> testFunctions.findAndWaitOnElement(By.xpath(".//*[@id=\"showJobsTable:0:jobState\"]")).getText(),
                (s) -> TestContext.lang.getProperty("task.status.complete").equalsIgnoreCase(s),
                Duration.of(60, ChronoUnit.SECONDS),
                true,
                true,
                "Expected Job state to be '" +
                        TestContext.lang.getProperty("task.status.complete") +
                        "'");
        assertLastJobHasErrorInfoSuccessCounts(0, 0, 1);
        // navigate back
        navigateToManageDataSetsView(menu);

        // wait
        Thread.sleep(TestContext.wait * 2);
        // refresh page
        //		driver.navigate().refresh();

        log.trace("check if element was deleted correctly");
        // check if element was deleted correctly
        if (testFunctions.isElementNotPresent(By.linkText(uuid)))
            org.testng.Assert.fail("An element should have been deleted, but it is still there");

        log.info(TestContext.lang.getProperty("admin." + menu + ".manageList") + " test finished");
    }

    private void navigateToJobsOverview() {
        // Create an action for mouse-moves
        Actions action = new Actions(driver);
        // Mouse over the menu 'User'
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.jobs")))).build()
                .perform();
        // Mouse over and click the submenu 'Manage ***'
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.jobs.showList")))).click()
                .build().perform();
    }

    private void navigateToManageDataSetsView(String menu) {
        // Create an action for mouse-moves
        Actions action = new Actions(driver);
        // Mouse over the menu 'User'
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.dataset.manageList")))).build()
                .perform();
        // Mouse over and click the submenu 'Manage ***'
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin." + menu + ".manageList")))).click()
                .build().perform();
    }

    private void assertLastJobHasErrorInfoSuccessCounts(int expectedErrorCount, int expectedInfoCount, int expectedSuccessCount) {
        String errorCountString = testFunctions.findAndWaitOnElement(By.xpath("//*[@id=\"showJobsTable:0:errorsCount\"]")).getText();
        String infoCountString = testFunctions.findAndWaitOnElement(By.xpath("//*[@id=\"showJobsTable:0:infosCount\"]")).getText();
        String successCountString = testFunctions.findAndWaitOnElement(By.xpath("//*[@id=\"showJobsTable:0:successesCount\"]")).getText();
        int errorCount = Integer.parseInt(errorCountString);
        int infoCount = Integer.parseInt(infoCountString);
        int successCount = Integer.parseInt(successCountString);

        if (expectedErrorCount != errorCount
                || expectedInfoCount != infoCount
                || expectedSuccessCount != successCount) {
            org.testng.Assert.fail("Wrong counts in job meta data view. Found errors:" + errorCount +
                    ", infos:" + infoCount +
                    ", successes:" + successCount +
                    ". --- Expected: errors:" + errorCount +
                    " , infos:" + infoCount +
                    ", successes:" + successCount);
        }
    }

}
