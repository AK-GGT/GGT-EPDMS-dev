package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.codeborne.selenide.Selenide.*;
import static org.junit.Assert.assertEquals;

@Listeners({ScreenShooter.class})
public class T072PushStockTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T072PushStockTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T071PushConfigTest.xml"), Arrays.asList("DB_pre_T015ImportExportTest.xml"));
    }

    @Override
    protected int getNumberOfConnections() {
        return 2;
    }

    /**
     * {@inheritDoc}
     * In this case (PushConfig is tested), the String "${nodeurl}" is replaced by the String targetSite set in TestContext.
     */
    @Override
    protected IDataSet preProcessDataSet(IDataSet dataSet) {
        log.debug("data set contains pushStockTest");
        ReplacementDataSet replacement = new ReplacementDataSet(dataSet);
        replacement.addReplacementObject("${nodeurl}", TestContext.SECONDARY_SITE_URL);
        return (IDataSet) replacement;
    }

    /**
     * Logs in.
     *
     * @throws InterruptedException
     */
    @BeforeClass
    public void login() throws InterruptedException {
        // import some data
        driver.manage().deleteAllCookies();
        log.debug("Trying to login");
        // login as admin
        testFunctions.login("admin", "default", true, true);

        // click on Admin area
        testFunctions.gotoAdminArea();
        // wait for the site to load
        testFunctions.waitUntilSiteLoaded();
    }


    /**
     * Tests push functionality of PushConfig and displayed job queue.
     *
     * @throws InterruptedException
     */
    @Test(priority = 301)
    public void testDataPush() throws InterruptedException {
        dataStock_push("test_config_changed", false);
        dataStock_push("test_config_changed", true);
    }

    /**
     * Tests display of imported data sets after last push in PushConfig.
     *
     * @throws InterruptedException
     */
    @Test(priority = 302, dependsOnMethods = {"testDataPush"})
    public void testDataPushImport() throws InterruptedException {
        log.debug("Going to data import page.");
        testFunctions.goToPageByAdminMenu("admin.dataImport", "admin.import");
        String path = this.getClass().getResource("/sample_data_min.zip").getPath();
        String push_name = "test_config_changed";
        // if we are on a windows machine, the first / before the C:/ should be removed
        if (File.separator.equals("\\"))
            path = path.substring(1);
        path = path.replace("/", java.io.File.separator);
        log.info("loading file " + path);
        // wait for the site to load
        testFunctions.waitUntilSiteLoaded();
        log.info("found footer, sending keys ");
        driver.findElement(By.xpath(".//*[@id='documentToUpload_input']")).sendKeys(path);

        // click Upload
        // it's not a link but a button. So a little bit diMain.getInstance().getDriver()erent code
        log.info("about to click 'upload'");
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.fileUpload.upload") + "')]")).click();

        Thread.sleep(TestContext.wait * 20L);

        // if uploaded successfully click 'Continue to step 2 (import)'
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.importUpload.goToStep2") + "')]")).click();
        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(TestContext.lang
                .getProperty("public.user.logout"))));

        // choose root data stock
        $x(".//*[@id='tableForm:importRootDataStock_label']").click(); // open stock selection menu
        Thread.sleep(TestContext.wait * 5L);
        $x(".//*[@id='tableForm:importRootDataStock_panel']/div/ul/li[contains(.,'RootStock1')]").click(); // click on RootStock1
        Thread.sleep(TestContext.wait * 5L);
        // next site, click on Import files
//		testFunctions.findAndWaitOnElement( By.xpath( "//button[contains(.,'" + TestContext.lang.getProperty( "admin.importFiles" ) + "')]" ) ).click();
        $x("//button[contains(.,'" + TestContext.lang.getProperty("admin.importFiles") + "')]").click();
        Thread.sleep(TestContext.wait * 5L);
        log.debug("Import started, it could take a while");
        // then it starts importing. Status log is on an iframe, so change the driver
        switchTo().frame("console");

        testFunctions.findAndWaitOnConsoleElement(By.xpath("html/body[contains(.,'------ import of files finished ------')]"));

        switchTo().defaultContent();

        log.debug("Going to 'Manage PushConfig list' page.");

        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.manageList");

        Thread.sleep(TestContext.wait * 5L);

        log.debug("Going to " + push_name + " entry.");
        testFunctions.findAndWaitOnElement(By.linkText(push_name)).click();

        log.debug("Checking entry and selecting push putton.");

        assertEquals("Incorrect count of  total data sets is shown.", String.valueOf(65),
                $(By.id("showSourceDataSetCount")).getText());
        assertEquals("Incorrect count of not pushed data sets is shown.", String.valueOf(12),
                $(By.id("showSourceImportedDataSets")).getText());

        testFunctions.clickButtonWithI18nLabel("admin.pushConfig.pushStocks");
        testFunctions.fillInputText("pass", "default");
        testFunctions.clickButtonWithI18nLabel("admin.ok");

        testFunctions.goToPageByAdminMenu("admin.jobs", "admin.jobs.showList");
        String state = testFunctions.getFirstTableEntryAtColumn("showJobsTable", 11L);
        if (log.isDebugEnabled())
            log.debug("state is: '" + state + "'");
        while (state.equals("RUNNING")) {
            testFunctions.clickButtonWithLabel("refresh");
            testFunctions.waitUntilSiteLoaded();
            state = testFunctions.getFirstTableEntryAtColumn("showJobsTable", 11L);
            log.debug("updating job page");
        }
        log.debug("job is complete.");

        //reloading page
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.manageList");

        Thread.sleep(TestContext.wait * 5L);

        log.debug("Going to " + push_name + " entry.");
        testFunctions.findAndWaitOnElement(By.linkText(push_name)).click();

        Thread.sleep(TestContext.wait * 5L);

        assertEquals("Incorrect count of unpushed data sets is shown.", String.valueOf(0),
                $(By.id("showSourceImportedDataSets")).getText());
    }

    /**
     * Tries to push data stock. Checks after sending push call if all data stocks were transferred as expected and if no errors
     * occurred during push.
     *
     * @param pushConfig    The name of push config entry that shall be pushed
     * @param alreadyPushed True if given push config entry was already pushed
     * @throws InterruptedException
     */
    private void dataStock_push(String pushConfig, boolean alreadyPushed) throws InterruptedException {
        log.info("Going to push. Already pushed? " + alreadyPushed);
        String dependency = "admin.dependencies.ALL";
        String stockName = "RootStock1";
        log.debug("Going to 'Manage PushConfig list' page.");
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.manageList");
        testFunctions.waitUntilSiteLoaded();
        log.debug("Clicking push button.");
        testFunctions.findAndWaitOnElement(By.linkText(pushConfig)).click();
        Thread.sleep(TestContext.wait * 5L);
        testFunctions.clickButtonWithI18nLabel("admin.pushConfig.pushStocks");
        testFunctions.fillInputText("pass", "default");
        testFunctions.clickButtonWithI18nLabel("admin.ok");

        testFunctions.waitUntilSiteLoaded();
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.manageList");
        testFunctions.findAndWaitOnElement(By.linkText(pushConfig)).click();
        testFunctions.waitUntilSiteLoaded();

        assertEquals("Incorrect push config name is shown.", pushConfig, $(By.id("pushNameOut")).getText());
        assertEquals("Incorrect dependency mode is shown.", TestContext.lang.getProperty(dependency),
                $(By.id("dependenciesModeOut")).getText());
        assertEquals("Incorrect target data stock is shown.", stockName, $(By.id("showSourceID")).getText());
        assertEquals("Incorrect count of data sets is shown.", String.valueOf(53),
                $(By.id("showSourceDataSetCount")).getText());

        testFunctions.goToPageByAdminMenu("admin.jobs", "admin.jobs.showList");
        log.debug("Going to 'Show jobs' page.");
        String state = testFunctions.getFirstTableEntryAtColumn("showJobsTable", 11L);

        log.debug("Checking new job entry.");
        Thread.sleep(TestContext.wait * 5L);
        if (!testFunctions.isLastTableEntryAtColumnEqual("showJobsTable", 2L, "admin")) {
            org.testng.Assert.fail("Incorrect user name is shown.");
        }
        if (!testFunctions.isLastTableEntryAtColumnEqual("showJobsTable", 3L, "push")) {
            org.testng.Assert.fail("Incorrect job type is shown.");
        }
        String description = "Pushing from data stock 'RootStock1' (1e92f562-09c1-4828-8eea-eb7428a34fa6) to data stock 'default' (39be0b9b-683b-4bb8-b1f2-32320cb2cbfe) in ACME2";

//        if (!testFunctions.isLastTableEntryAtColumnEqual("showJobsTable", 4L, description)) {
//            org.testng.Assert.fail("Incorrect description is shown.");
//        }
        if (log.isDebugEnabled())
            log.debug("state is: '" + state + "'");
        while (state.equals("RUNNING")) {
            testFunctions.clickButtonWithLabel("refresh");
            testFunctions.waitUntilSiteLoaded();
            state = testFunctions.getFirstTableEntryAtColumn("showJobsTable", 11L);
            log.debug("updating job page");
        }
        log.debug("job is complete.");
        Supplier<String> errorCounter = () -> testFunctions.getFirstTableEntryAtColumn("showJobsTable", 8L);
        Supplier<String> infoCounter = () -> testFunctions.getFirstTableEntryAtColumn("showJobsTable", 9L);
        Supplier<String> successCounter = () -> testFunctions.getFirstTableEntryAtColumn("showJobsTable", 10L);
        long errorCount = Long.parseLong(testFunctions.getValidStringWaiting(errorCounter, testFunctions.STRING_NOT_BLANK,
                true, true, "Obtain error count"));
        long infoCount = Long.parseLong(testFunctions.getValidStringWaiting(infoCounter, testFunctions.STRING_NOT_BLANK,
                true, true, "Obtain info count"));
        long successCount = Long.parseLong(testFunctions.getValidStringWaiting(successCounter, testFunctions.STRING_NOT_BLANK,
                true, true, "Obtain success count"));

        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='showJobsTable_data']/tr[1]/td[1]/div")).click();
        String jobLog = testFunctions.getSecondTableEntryAtColumn("showJobsTable", 1L);

        if (log.isTraceEnabled()) {
            log.trace("jobLog is: " + System.getProperty("line.separator") + jobLog);
        }

        // evaluating counters
        if (!alreadyPushed) {
            int expectedErrorsCount = 0;
            int expectedInfosCount = 0;
            int expectedSuccessesCount = 53;
            String expectedState = "COMPLETE";
            log.debug("Checking amount of data sets that were already pushed.");
            if (errorCount != expectedErrorsCount || infoCount != expectedInfosCount || successCount != expectedSuccessesCount || !(state.equals(expectedState))) {
                log.info("Error is: " + errorCount + ". Expected: " + expectedErrorsCount);
                log.info("infosCount is: " + infoCount + ". Expected: " + expectedInfosCount);
                log.info("successCount is: " + successCount + ". Expected: " + expectedSuccessesCount);
                log.info("state is: " + state + ". Expected: " + expectedState);
                org.testng.Assert.fail("Job did not complete correctly!");

            }

        } else {
            int expectedErrorsCount = 0;
            int expectedInfosCount = 0;
            int expectedSuccessesCount = 53;
            String expectedState = "COMPLETE";
            log.debug("Checking amount of data sets that were already pushed.");
            if (errorCount != expectedErrorsCount || infoCount != expectedInfosCount || successCount != expectedSuccessesCount || !(state.equals(expectedState))) {
                log.info("Error is: " + errorCount + ". Expected: " + expectedErrorsCount);
                log.info("infosCount is: " + infoCount + ". Expected: " + expectedInfosCount);
                log.info("successCount is: " + successCount + ". Expected: " + expectedSuccessesCount);
                log.info("state is: " + state + ". Expected: " + expectedState);
                org.testng.Assert.fail("Job did not complete correctly!");

            }

        }

        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.manageList");
        testFunctions.findAndWaitOnElement(By.linkText(pushConfig)).click();
        testFunctions.waitUntilSiteLoaded();

        assertEquals("Incorrect count of data sets is shown.", String.valueOf(0),
                $(By.id("showSourceImportedDataSets")).getText());

        log.debug("Checking on target node whether all pushed datasets appear in correct data stock.");
        testFunctions.login("admin", "default", true, true, 0, false, TestContext.SECONDARY_SITE_URL, null);

        testFunctions.gotoAdminArea();
        testFunctions.waitUntilSiteLoaded();
        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.process.manageList");

        testFunctions.targetTableContainsNumberDataSets("processTable", 5L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.lciaMethod.manageList");
        testFunctions.targetTableContainsNumberDataSets("lciamethodTable", 5L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.elementaryFlow.manageList");
        testFunctions.targetTableContainsNumberDataSets("flowTable", 7L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.productFlow.manageList");
        testFunctions.targetTableContainsNumberDataSets("flowTable", 5L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.flowProperty.manageList");
        testFunctions.targetTableContainsNumberDataSets("flowpropertyTable", 5L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.unitGroup.manageList");
        testFunctions.targetTableContainsNumberDataSets("unitgroupTable", 16L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.source.manageList");
        testFunctions.targetTableContainsNumberDataSets("sourceTable", 5L);

        testFunctions.goToPageByAdminMenu("admin.dataset.manageList", "admin.contact.manageList");
        testFunctions.targetTableContainsNumberDataSets("contactTable", 5L);

        login();
    }

}
