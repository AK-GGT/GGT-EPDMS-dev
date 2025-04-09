package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks the export of a data set by counting the number of entries in download
 * excel file and comparing it with the number of data set entries
 *
 * @author Sarai Eilebrecht
 */
@Listeners({ScreenShooter.class})
public class T018Export_XLSTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T018Export_XLSTest.class);
    private Map<String, String> idMap = new HashMap<String, String>();

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    @BeforeClass
    public void login() throws InterruptedException, JsonGenerationException, JsonMappingException, IOException {
        driver.manage().deleteAllCookies();
        log.debug("all Cookies deleted.");
        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        // wait for the site to load
        log.debug("Went to admin area");
        testFunctions.waitUntilSiteLoaded();
        // select RootStock 1
        testFunctions.selectDataStock("RootStock1");

        initIdMap();
    }

    private void initIdMap() {
        this.idMap.clear();
        this.idMap.put("pageOnly", "exportSinglePageAsXLSButton");
        this.idMap.put("all", "exportAllPagesAsXLSButton");
        this.idMap.put("menuButton", "exportMenuButton");
        this.idMap.put("processes", "processTable");
        this.idMap.put("lciaMethods", "lciamethodTable");
        this.idMap.put("elementaryFlows", "flowTable");
        this.idMap.put("productFlows", "flowTable");
        this.idMap.put("flowProperties", "flowpropertyTable");
        this.idMap.put("unitGroups", "unitgroupTable");
        this.idMap.put("sources", "sourceTable");
        this.idMap.put("contacts", "contactTable");

    }

    /**
     * Checks number of exported xls file of Process data sets that are visible on
     * current page
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportProcessesPageDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("processes", "pageOnly");
    }

    /**
     * Checks number of exported xls file of all Process data sets
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportProcessesAllDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("processes", "all");
    }

    /**
     * Checks number of exported xls file of LCIA Methods data sets that are visible
     * on current page
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportLCIAMethodsPageDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("lciaMethods", "pageOnly");
    }

    /**
     * Checks number of exported xls file of all LCIA Methods data sets
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportLCIAMethodsAllDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("lciaMethods", "all");
    }

    /**
     * Checks number of exported xls file of Elementary Flows data sets that are
     * visible on current page
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportElementaryFlowsPageDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("elementaryFlows", "pageOnly");
    }

    /**
     * Checks number of exported xls file of all Elementary Flows data sets
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportElementaryFlowsAllDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("elementaryFlows", "all");
    }

    /**
     * Checks number of exported xls file of Poduct Flows data sets that are visible
     * on current page
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportProductFlowsPageDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("productFlows", "pageOnly");
    }

    /**
     * Checks number of exported xls file of all Product Flows data sets
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportProductFlowsAllDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("productFlows", "all");
    }

    /**
     * Checks number of exported xls file of Flow Properties data sets that are
     * visible on current page
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportFlowPropertiesPageDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("flowProperties", "pageOnly");
    }

    /**
     * Checks number of exported xls file of all Flow Properties data sets
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportFlowPropertiesAllDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("flowProperties", "all");
    }

    /**
     * Checks number of exported xls file of Unit Groups data sets that are visible
     * on current page
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportUnitGroupsPageDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("unitGroups", "pageOnly");
    }

    /**
     * Checks number of exported xls file of all Unit Groups data sets
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportUnitGroupsAllDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("unitGroups", "all");
    }

    /**
     * Checks number of exported xls file of Sources data sets that are visible on
     * current page
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportSourcesPageDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("sources", "pageOnly");
    }

    /**
     * Checks number of exported xls file of all Sources data sets
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportSourcesAllDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("sources", "all");
    }

    /**
     * Checks number of exported xls file of Contacts data sets that are visible on
     * current page
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportContactsPageDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("contacts", "pageOnly");
    }

    /**
     * Checks number of exported xls file of all Contacts data sets
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void exportContactsAllDataAsXLS() throws InterruptedException, IOException {
        exportDataAsXLS("contacts", "all");
    }

    /**
     * Checks the number of entries in download excel file against count of data
     * sets on current page or all data sets
     *
     * @throws InterruptedException
     * @throws IOException
     */
    public void exportDataAsXLS(String data, String exportType) throws InterruptedException, IOException {
        log.info("Export " + data + exportType + " data set to xls test started");

        // Export data as XLS file
        String stockName = TestContext.lang.getProperty("common." + data).replaceAll(" ", "_");
        String fileName = ("Node_RootStock1_" + stockName + "_"
                + TestContext.lang.getProperty("admin.export.datasets." + exportType + ".suffix") + ".xls")
                .replaceAll("\\+", "_");
        FileTestFunctions.tryDeleteFile(fileName, this.getClass());

        testFunctions.selectDataSet(data);

        // Detect number of data sets on current page
        int dataSetEntries = testFunctions.countTotalDatasetEntries();

        // If only entries of current page shall be exported and number of entries is
        // bigger than the number of visible entries,
        // take number of visible entries as reference
        if (exportType == "pageOnly") {
            int pageCount = testFunctions.countDatasetEntriesOnPage();
            if (pageCount < dataSetEntries) {
                dataSetEntries = pageCount;
            }
        }

        // Click on Export As XLS file button
        String contextId = idMap.get(data);
        String menuButtonId = contextId + ":" + idMap.get("menuButton");
        String exportTypeButtonId = contextId + ":" + idMap.get(exportType);
        testFunctions.findAndWaitOnElement(By.id(menuButtonId)).click();

        // Select given export option in drop-down menu
        testFunctions.findAndWaitOnElement(By.id(exportTypeButtonId)).click();
        Thread.sleep(TestContext.wait * 20);

        // Count number of entries in download excel file and compare result with number
        // of data set entries
        WorkbookTestFunctions.countAndCompareEntries(fileName, this.getClass(), dataSetEntries);

        log.info("Export " + data + exportType + " data set to xls test finished");
    }

}
