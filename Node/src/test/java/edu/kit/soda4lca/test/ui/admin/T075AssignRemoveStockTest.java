package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.testng.ScreenShooter;
import de.iai.ilcd.service.util.DataSetsTypes;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@Listeners({ScreenShooter.class})
public class T075AssignRemoveStockTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T075AssignRemoveStockTest.class);
    private int assignButton = 8;

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    /**
     * Logs in.
     *
     * @throws InterruptedException
     */
    @BeforeClass
    public void login() throws InterruptedException {
        // import some data
        WebDriverRunner.setWebDriver(driver);
        WebDriverRunner.clearBrowserCache();
        log.debug("Trying to login");
        // login as admin
        testFunctions.login("admin", "default", true, true);

        // click on Admin area
        testFunctions.gotoAdminArea();
        // wait for the site to load
        testFunctions.waitUntilSiteLoaded();
    }

    /**
     * Tests assign/remove functionality and displayed job queue.
     *
     * @throws InterruptedException
     */
    @Test(priority = 301)
    public void testAssignRemoveData() throws InterruptedException {
        dataStock_assign("Process", "processes");
        dataStock_remove("Process", "processes");
    }

    /**
     * Tries to assign data stock. Checks after assigning by calling if all data stocks were assigned as expected and if no errors
     * occurred during assigning.
     *
     * @throws InterruptedException
     */
    public void dataStock_assign(String dataSetType, String dataSetType2) throws InterruptedException {
        Properties lang = TestContext.lang;
        String stockName = "SimpleStock1";
        log.debug("Going to 'Manage List ' page.");

        $(By.linkText(TestContext.lang.getProperty("common.stock"))).hover();
        // Click Manage Data Stocks
        $(By.linkText(TestContext.lang.getProperty("admin.stock.manageList"))).click();
        testFunctions.waitUntilSiteLoaded();

        // click assign data stock button in SimpleStock1 data stock
        $x(".//*[text()='SimpleStock1']/../../td[" + assignButton + "]/button[*]").click();

        if (!"Process".equals(dataSetType)) {
            // switch to another data set
            testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("common." + dataSetType2))).click();
        }
        // Test if data set is empty
        testFunctions.waitUntilSiteLoaded();
        //$x(".//*[@id='stockTabs:dataSetTabView:ct" + dataSetType + "DataTable_data']/tr[1]/td[2]").waitUntil(exist, TestContext.wait * 8);
        if (testFunctions.isElementNotPresent(
                By.xpath(".//*[@id='stockTabs:dataSetTabView:ct" + dataSetType + "DataTable_data']/tr[1]/td[3]")))
            org.testng.Assert.fail("logical data stock contains at least one element.");

        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("admin.tab.mainInfo"))).click();

        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:org_label']")).click();
        Thread.sleep(TestContext.wait);

        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:org_panel']/div/ul/li[" + 2 + "]")).click();

        // navigate into batch assign tab
        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("admin.stock.batchAssignRemove"))).click();

        // select 'select all' on data set type selection panel
        testFunctions.findAndWaitOnElement(
                By.xpath(".//*[@id='stockTabs:selectionRadioBtnTab']/tbody[1]/tr[1]/td[1]/div[1]")).click();

        // test whether data stocks list is not empty and whether
        // desired data set is occurs in assignable data set list
        testFunctions
                .findAndWaitOnElement(By.xpath(".//*[text()='RootStock1']/../../td[1]/div[*]"))
                .click();

        // Checking if all datasets types exist as a checkboxes
        for (DataSetsTypes dataSetsType : DataSetsTypes.values()) {
            String label = TestContext.lang.getProperty(dataSetsType.getDisplayName());
            testFunctions.findAndWaitOnElement(By.xpath("//td[text()='" + label + "']"));
        }

        // click on assign button to assign a data set
        testFunctions.findAndWaitOnElement(By.id("stockTabs:assignAllBtn")).click();

        if ("Process".equals(dataSetType))
            testFunctions.selectOptionInDataSetDialogue(4);
        else
            testFunctions.selectOptionInDataSetDialogue(1);

        testFunctions.goToPageByAdminMenu("admin.jobs", "admin.jobs.showList");
        log.debug("Going to 'Show jobs' page.");
        testFunctions.waitUntilSiteLoaded();
        String state = testFunctions.getFirstTableEntryAtColumn("showJobsTable", 11L);

        log.debug("Checking new job entry.");
        testFunctions.waitUntilSiteLoaded();
        if (!testFunctions.isLastTableEntryAtColumnEqual("showJobsTable", 2L, "admin")) {
            org.testng.Assert.fail("Incorrect user name is shown.");
        }
        if (!testFunctions.isLastTableEntryAtColumnEqual("showJobsTable", 3L, "assign")) {
            org.testng.Assert.fail("Incorrect job type is shown.");
        }

        if (log.isDebugEnabled())
            log.debug("state is: '" + state + "'");
        while (state.equals("RUNNING")) {
            testFunctions.clickButtonWithLabel("refresh");
            testFunctions.waitUntilSiteLoaded();
            state = testFunctions.getFirstTableEntryAtColumn("showJobsTable", 11L);
            log.debug("updating job page");
        }
        testFunctions.waitUntilSiteLoaded();
        log.debug("job is complete.");

        if (log.isDebugEnabled()) {
            // print run log of task
            testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='showJobsTable_data']/tr[1]/td[1]/div")).click();
            String jobLog = testFunctions.getSecondTableEntryAtColumn("showJobsTable", 1L);
            log.debug("jobLog is: " + jobLog);
        }

        log.debug("Checking amount of data sets that were assigned correctly.");
        // Waiting for and parsing the counters
        Supplier<String> errorCounter = () -> testFunctions.getFirstTableEntryAtColumn("showJobsTable", 8L);
        Supplier<String> infoCounter = () -> testFunctions.getFirstTableEntryAtColumn("showJobsTable", 9L);
        Supplier<String> successCounter = () -> testFunctions.getFirstTableEntryAtColumn("showJobsTable", 10L);
        long errorCount = Long.parseLong(testFunctions.getValidStringWaiting(errorCounter, testFunctions.STRING_NOT_BLANK,
                true, true, "Obtain error count"));
        long infoCount = Long.parseLong(testFunctions.getValidStringWaiting(infoCounter, testFunctions.STRING_NOT_BLANK,
                true, true, "Obtain info count"));
        long successCount = Long.parseLong(testFunctions.getValidStringWaiting(successCounter, testFunctions.STRING_NOT_BLANK,
                true, true, "Obtain success count"));

        int expectedErrorCount = 0;
        int expectedInfoCount = 0;
        int expectedSuccessCount = 56;
        String expectedState = "COMPLETE";
        if (errorCount != expectedErrorCount
                || infoCount != expectedInfoCount
                || successCount != expectedSuccessCount
                || !(state.equals(expectedState))) {

            log.info("Error count expected: " + expectedErrorCount + ", actually is: " + errorCount);
            log.info("Infos count expected: " + expectedInfoCount + ", actually is: " + infoCount);
            log.info("Success count expected: " + expectedSuccessCount + ", is: " + successCount);
            log.info("state expected: " + "COMPLETE" + ", is: " + state);
            org.testng.Assert.fail("Job did not complete correctly!");
        }

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.process.manageList");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.selectDataStock(stockName);
        testFunctions.targetTableContainsNumberDataSets("processTable", 5L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.lciaMethod.manageList");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.targetTableContainsNumberDataSets("lciamethodTable", 5L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.elementaryFlow.manageList");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.targetTableContainsNumberDataSets("flowTable", 7L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.productFlow.manageList");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.targetTableContainsNumberDataSets("flowTable", 7L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.flowProperty.manageList");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.targetTableContainsNumberDataSets("flowpropertyTable", 5L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.unitGroup.manageList");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.targetTableContainsNumberDataSets("unitgroupTable", 16L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.source.manageList");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.targetTableContainsNumberDataSets("sourceTable", 6L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.contact.manageList");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.targetTableContainsNumberDataSets("contactTable", 5L);

        login();
    }

    /**
     * Tries to remove data stock. Checks after removing by calling if all data stocks were removed as expected and if no errors
     * occurred during removing.
     *
     * @throws InterruptedException
     */
    public void dataStock_remove(String dataSetType, String dataSetType2) throws InterruptedException {
        Properties lang = TestContext.lang;
        String stockName = "SimpleStock1";
        log.debug("Going to 'Manage list' page.");
        $(By.linkText(TestContext.lang.getProperty("common.stock"))).hover();
        // Click Manage Data Stocks
        $(By.linkText(TestContext.lang.getProperty("admin.stock.manageList"))).click();
        // click assign data stock button in SimpleStock1 data stock
        testFunctions
                .findAndWaitOnElement(By.xpath(".//*[text()='SimpleStock1']/../../td[" + assignButton + "]/button[*]"))
                .click();

        if (!"Process".equals(dataSetType)) {
            // switch to another data set
            testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("common." + dataSetType2))).click();
        }
        // Test if data set is empty
        testFunctions.waitUntilSiteLoaded();

        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("admin.tab.mainInfo"))).click();

        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:org_label']")).click();
        testFunctions.waitUntilSiteLoaded();

        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:org_panel']/div/ul/li[" + 2 + "]")).click();

        testFunctions.findAndWaitOnElement(By.linkText(lang.getProperty("admin.stock.batchAssignRemove"))).click();

        testFunctions.findAndWaitOnElement(
                By.xpath(".//*[@id='stockTabs:selectionRadioBtnTab']/tbody[1]/tr[1]/td[1]/div[1]")).click();

        // test whether data stocks list is not empty and whether
        // desired data set is occurs in assignable data set list
        testFunctions.findAndWaitOnElement(
                By.xpath(".//*[@id='stockTabs:sourceStockTable_data']/tr[1]/td[1]")).click();
        testFunctions.findAndWaitOnElement(
                By.xpath(".//*[@id='stockTabs:sourceStockTable_data']/tr[2]/td[1]")).click();
        testFunctions.findAndWaitOnElement(
                By.xpath(".//*[@id='stockTabs:sourceStockTable_data']/tr[3]/td[1]")).click();
        testFunctions.findAndWaitOnElement(
                By.xpath(".//*[@id='stockTabs:sourceStockTable_data']/tr[4]/td[1]")).click();

        // Checking if all datasets types exist as a checkboxes
        for (DataSetsTypes dataSetsType : DataSetsTypes.values()) {
            String label = TestContext.lang.getProperty(dataSetsType.getDisplayName());
            testFunctions.findAndWaitOnElement(By.xpath("//td[text()='" + label + "']"));
        }

        // click on assign button to assign a data set
        testFunctions.findAndWaitOnElement(By.id("stockTabs:removeAllBtn")).click();

        testFunctions.findAndWaitOnElement(
                By.xpath(".//*[@id='stockTabs:adAssignAlldetachSelectedFromStockConfirmHeader']/legend[1]")).click();

        testFunctions.findAndWaitOnElement(By.xpath(
                        ".//*[@id='stockTabs:adAssignAlldetachSelectedFromStockConfirmHeader']/div[1]/table[1]/tbody[1]/tr[2]/td[1]/table[1]/tbody[1]/tr[4]/td[1]/div[1]"))
                .click();

        Thread.sleep(2000);
        testFunctions.findAndWaitOnElement(
                By.xpath(".//*[@id='stockTabs:adAssignAlldetachSelectedFromStockConfirmOkButton']")).click();
        Thread.sleep(2000);

        testFunctions.goToPageByAdminMenu("admin.jobs", "admin.jobs.showList");
        log.debug("Going to 'Show jobs' page.");
        String state = testFunctions.getFirstTableEntryAtColumn("showJobsTable", 11L);

        log.debug("Checking new job entry.");
        testFunctions.waitUntilSiteLoaded();
        if (!testFunctions.isLastTableEntryAtColumnEqual("showJobsTable", 2L, "admin")) {
            org.testng.Assert.fail("Incorrect user name is shown.");
        }

        if (!testFunctions.getFirstTableEntryAtColumn("showJobsTable", 3L).equals("remove")) {
            org.testng.Assert.fail("Incorrect job type is shown.");
        }

        if (log.isDebugEnabled())
            log.debug("state is: '" + state + "'");
        while (state.equals("RUNNING")) {
            testFunctions.clickButtonWithLabel("refresh");
            testFunctions.waitUntilSiteLoaded();
            state = testFunctions.getFirstTableEntryAtColumn("showJobsTable", 11L);
            log.debug("updating job page");
        }
        testFunctions.waitUntilSiteLoaded();
        log.debug("job is complete.");

        if (log.isDebugEnabled()) {
            // print run log of task
            testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='showJobsTable_data']/tr[1]/td[1]/div")).click();
            String jobLog = testFunctions.getSecondTableEntryAtColumn("showJobsTable", 1L);
            log.debug("jobLog is: " + jobLog);
        }

        // Wainting for and parsing the counters
        Supplier<String> errorCounter = () -> testFunctions.getFirstTableEntryAtColumn("showJobsTable", 8L);
        Supplier<String> infoCounter = () -> testFunctions.getFirstTableEntryAtColumn("showJobsTable", 9L);
        Supplier<String> successCounter = () -> testFunctions.getFirstTableEntryAtColumn("showJobsTable", 10L);

        long errorCount = Long.parseLong(testFunctions.getValidStringWaiting(errorCounter, testFunctions.STRING_NOT_BLANK,
                true, true, "Obtain error count"));
        long infoCount = Long.parseLong(testFunctions.getValidStringWaiting(infoCounter, testFunctions.STRING_NOT_BLANK,
                true, true, "Obtain info count"));
        long successCount = Long.parseLong(testFunctions.getValidStringWaiting(successCounter, testFunctions.STRING_NOT_BLANK,
                true, true, "Obtain success count"));

        log.debug("Checking amount of data sets that were removed correctly.");
        int expectedErrorCount = 0;
        int expectedInfoCount = 0;
        int expectedSuccessCount = 56;
        String expectedState = "COMPLETE";
        if (errorCount != expectedErrorCount
                || successCount != expectedSuccessCount
                || infoCount != expectedInfoCount
                || !(state.equals(expectedState))) {

            log.info("Error count expected: " + expectedErrorCount + ", actually is: " + errorCount);
            log.info("Success count expected: " + expectedInfoCount + ", is: " + infoCount);
            log.info("Success count expected: " + expectedSuccessCount + ", is: " + successCount);
            log.info("state expected: " + "COMPLETE" + ", is: " + state);
            org.testng.Assert.fail("Job did not complete correctly!");
        }

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.process.manageList");
        testFunctions.selectDataStock(stockName);
        testFunctions.targetTableContainsNumberDataSets("processTable", 0L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.lciaMethod.manageList");
        testFunctions.targetTableContainsNumberDataSets("lciamethodTable", 0L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.elementaryFlow.manageList");
        testFunctions.targetTableContainsNumberDataSets("flowTable", 0L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.productFlow.manageList");
        testFunctions.targetTableContainsNumberDataSets("flowTable", 0L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.flowProperty.manageList");
        testFunctions.targetTableContainsNumberDataSets("flowpropertyTable", 0L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.unitGroup.manageList");
        testFunctions.targetTableContainsNumberDataSets("unitgroupTable", 0L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.source.manageList");
        testFunctions.targetTableContainsNumberDataSets("sourceTable", 0L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.contact.manageList");
        testFunctions.targetTableContainsNumberDataSets("contactTable", 0L);

        login();
    }

}