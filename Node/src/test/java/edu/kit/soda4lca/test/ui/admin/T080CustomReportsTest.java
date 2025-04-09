package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.FileTestFunctions;
import edu.kit.soda4lca.test.ui.main.TestContext;
import edu.kit.soda4lca.test.ui.main.WorkbookTestFunctions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Listeners({ScreenShooter.class})
public class T080CustomReportsTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T080CustomReportsTest.class);

    private int count = 2;

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
        driver.manage().deleteAllCookies();
        log.debug("Trying to login");
        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();
        // wait for the site to load
        testFunctions.waitUntilSiteLoaded();
        // Choose data stock RootStock1

    }

    /**
     * Tests if all in config file defined custom reports are shown and executed
     * correctly.
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testCustomReports() throws InterruptedException, IOException {
        log.debug("start of testing custom reports.");
        List<String> firstHeaders = Arrays.asList("Type", "Count");
        List<String> secondHeaders = Arrays.asList("Type", "Methods");
        customReport("Retrieve count of processes", "retrieve_process_counts", firstHeaders, 1, 4, 2);
        testFunctions.findAndWaitOnElement(By.partialLinkText("soda4LCA Administration")).click();
        customReport("Retrieve count of LCIA methods", "retrieve_lcia_counts", secondHeaders, 2, 4, 2);
        log.debug("custom reports test finished. ");
    }

    /**
     * Checks if a custom report behaves as expected. Therefore the custom
     * report will be checked if it appears with given name and given order in
     * custom reports view and if custom report is executed correctly.
     *
     * @param name            The expected name of custom report shown in custom report view
     * @param fileName        The expected file name of downloaded custom report
     * @param headers         The list of expected headers of custom report result
     * @param order           The order the custom report should appear
     * @param entries         The number of expected table entries in resulting table
     * @param expectedColumns The number of expected columns in resulting table
     * @throws InterruptedException
     * @throws IOException
     */
    public void customReport(String name, String fileName, List<String> headers, int order, int entries, int expectedColumns)
            throws InterruptedException, IOException {

        log.debug("Started testing custom report with name " + name + ".");
        FileTestFunctions.tryDeleteFile(fileName, this.getClass());

        setupTest();

        int customReportsCount = driver
                .findElements(By.xpath(".//*[@id='generalForm']/div/div/div/div/button")).size();
        if (customReportsCount != count) {
            org.testng.Assert.fail("Custom reports seems to have more or less than " + count
                    + " buttons. Custom reports has actually " + customReportsCount);
        }
        if (!testFunctions.isElementNotPresent(By.xpath(".//*[text()='" + name + "']"))) {
            org.testng.Assert.fail("Custom report with name " + name + " does not appear.");
        }
        if (!testFunctions
                .findAndWaitOnElement(By.xpath(".//*[@id='generalForm']/div/div/div/div[" + order + "]/button/span"))
                .getText().equals(name)) {
            org.testng.Assert.fail("custom report with name " + name + " does not appear as button no. " + order
                    + " in custom reports view.");
        }
        log.debug("Executing custom report and checking its result.");
        testFunctions.clickButtonWithLabel(name);
        String resultFileName = fileName + ".csv";
        Thread.sleep(TestContext.wait * 20);

        WorkbookTestFunctions.compareCSVEntries(resultFileName, this.getClass(), entries, headers, expectedColumns);
        log.debug("finished testing custom report " + name + ".");
    }

    /**
     * Sets up test by navigating to custom reports view and loading correct stock if needed.
     *
     * @throws InterruptedException
     */
    public void setupTest() throws InterruptedException {
        boolean customReportEnabled = testFunctions.isElementNotPresent(
                By.xpath(".//*[text()='" + TestContext.lang.getProperty("admin.customReports") + "']"));
        if (!customReportEnabled) {
            org.testng.Assert.fail("Custom reports function does not seem to be enabled.");
        }
        testFunctions.clickButtonWithI18nLabel("admin.customReports");
        testFunctions.waitUntilSiteLoaded();

        if (testFunctions.isElementNotPresent(By.xpath(".//*[@id='admin_header']/div[2]//table"))) {
//			testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='admin_header']/div[2]//table/tbody/tr[1]/td[2]/div")).click();
            testFunctions.findAndWaitOnElement(By.xpath("//*[contains(@class, 'selectDataStockHTMLClass')]")).click();
            testFunctions.findAndWaitOnElement(By.xpath(".//*[@data-label='RootStock1']")).click();
            testFunctions.waitUntilSiteLoaded();
        }
    }

}
