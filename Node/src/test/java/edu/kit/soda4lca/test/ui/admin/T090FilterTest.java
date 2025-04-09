package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.testng.ScreenShooter;
import de.iai.ilcd.model.common.DataSetState;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.PathSelectors;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Tests filter in different data set views.
 *
 * @author sarai
 */
@Listeners({ScreenShooter.class})
public class T090FilterTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T090FilterTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    /**
     * Sets up test by logging in and navigating to admin area
     *
     * @throws Exception
     */
    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        log.info("Begin of filter test");
        WebDriverRunner.setWebDriver(driver);
        WebDriverRunner.clearBrowserCache();
        Configuration.fastSetValue = true;
        log.debug("Trying to login");
        // login as admin
        testFunctions.login("admin", "default", true, true);

        // click on Admin area
        testFunctions.gotoAdminArea();
        // wait for the site to load
        $x(".//*[@id='admin_footer']").shouldBe(visible);
        testFunctions.selectDataStock("RootStock1");
    }

    /**
     * Tests filter function of all data tables for all all kind of data set types
     * in the views Manage Data Sets, Assign Data Sets and publicly accessible view publicly accessible view iof
     * Browse Data Sets.
     *
     * @throws InterruptedException
     */
    @Test
    public void testFilters() throws InterruptedException {
        log.debug("Testing all filters in set view.");
        testFiltersInView(SetView.MANAGE);

        log.debug("Going to Manage stock list view.");
        testFunctions.goToPageByAdminMenu("common.stock", "admin.stock.manageList");
        for (int i = 0; i < TestContext.timeout && !$x(".//*[@id='stockTabs']/ul/li/a[text()='"
                + TestContext.lang.getProperty("admin.stock.assignedDataSets") + "']").exists(); i++)
            $(By.linkText("RootStock1")).click();
        $x(".//*[@id='stockTabs']/ul/li/a[text()='"
                + TestContext.lang.getProperty("admin.stock.assignedDataSets") + "']").click();
        log.debug("testing all filters in Manage Stock view.");
        testFiltersInView(SetView.ASSIGN);

        log.debug("Going to public index site.");
        $x(".//*[@id='leftFooter']/a[1]").click();

        // change DataStock
        $x(PathSelectors.DATASTOCK_SELECTOR);

        log.trace("Change DataStock");
        $x(PathSelectors.DATASTOCK_SELECTOR).click();
        $x(".//*[@data-label='RootStock1']").click();

        log.debug("Testing all public visible data set filters");
        testFiltersInView(SetView.PUBLIC);

    }

    /**
     * Tests all filters of given view.
     *
     * @param view The view whose data table filters are to test.
     * @throws InterruptedException
     */
    private void testFiltersInView(SetView view) throws InterruptedException {
        testDataSetList("processes", Arrays.asList("kao", "mat", "1999", "2016", "pe", "2016", "uuid:1a"),
                Arrays.asList(1, 2, 1, 1, 4, 5, 1), view);
        testDataSetList("lciaMethods", Arrays.asList("eu", "", "", "", "", "2016", "uuid:b7"),
                Arrays.asList(2, 0, 0, 0, 0, 5, 1), view);
        testDataSetList("elementaryFlows", Arrays.asList("bu", "", "", "", "", "2016", "uuid:0a"),
                Arrays.asList(2, 0, 0, 0, 0, 7, 6), view);
        testDataSetList("productFlows", Arrays.asList("slag", "", "", "", "", "2016", "uuid:7"),
                Arrays.asList(1, 0, 0, 0, 0, 5, 1), view);
        testDataSetList("flowProperties", Arrays.asList("area", "", "", "", "", "2016", "uuid:f"),
                Arrays.asList(1, 0, 0, 0, 0, 5, 0), view);
        testDataSetList("unitGroups", Arrays.asList("item", "", "", "", "", "2016", "uuid:93"),
                Arrays.asList(3, 0, 0, 0, 0, 16, 6), view);
        testDataSetList("sources", Arrays.asList("eco", "", "", "", "", "2016", "uuid:5"), Arrays.asList(1, 0, 0, 0, 0, 5, 2),
                view);
        testDataSetList("contacts", Arrays.asList("eu", "", "", "", "2016", "", "uuid:5"), Arrays.asList(1, 0, 0, 0, 0, 5, 1),
                view);

    }

    /**
     * Test the data table filters of given data set type.
     *
     * @param dataSet             The data set type whose data table in view is to be tested
     * @param filters             The filters to test
     * @param expectedFilteredNum The expected count of filtered data sets
     * @param view                The view in which the data set table is to be tested (either publicly visible data sets, data sets in admin view or data sets in assign view )
     * @throws InterruptedException
     */
    private void testDataSetList(String dataSet, List<String> filters, List<Integer> expectedFilteredNum, SetView view)
            throws InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Going to check filters of data set " + dataSet + ".");
        }
        String tableName = getStockTypeName(dataSet);
        switch (view) {
            case MANAGE:
                testFunctions.selectDataSet(dataSet);
                break;
            case ASSIGN:
                $x(".//*[@id='stockTabs:dataSetTabView']/ul/li/a[text()='"
                        + TestContext.lang.getProperty("common." + dataSet) + "']").click();
                break;
            case PUBLIC:
                $(By.linkText(TestContext.lang.getProperty("common." + dataSet))).click();
        }
        if (view != SetView.ASSIGN) {
            checkFiltersInDataSetList("common.name", filters.get(0), expectedFilteredNum.get(0), tableName, false);
            checkFiltersInDataSetList("common.dataset.classif", filters.get(1), expectedFilteredNum.get(1), tableName, false);
            checkFiltersInDataSetList("common.dataset.refYear", filters.get(2), expectedFilteredNum.get(2), tableName, false);
            checkFiltersInDataSetList("common.proc.validUntilYear", filters.get(3), expectedFilteredNum.get(3), tableName, false);
            checkFiltersInDataSetList("common.proc.owner", filters.get(4), expectedFilteredNum.get(4), tableName, false);
            checkFiltersInDataSetList("common.importDate", filters.get(5), expectedFilteredNum.get(5), tableName, false);
            checkFiltersInDataSetList("common.name", filters.get(6), expectedFilteredNum.get(6), tableName, false);
        } else {
            checkFiltersInDataSetList("common.name", filters.get(0), expectedFilteredNum.get(0), tableName, true);
            checkFiltersInDataSetList("common.dataset.classif", filters.get(1), expectedFilteredNum.get(1), tableName, true);
            checkFiltersInDataSetList("common.dataset.refYear", filters.get(2), expectedFilteredNum.get(2), tableName, true);
            checkFiltersInDataSetList("common.proc.validUntilYear", filters.get(3), expectedFilteredNum.get(3), tableName, true);
            checkFiltersInDataSetList("common.proc.owner", filters.get(4), expectedFilteredNum.get(4), tableName, true);
            checkFiltersInDataSetList("common.importDate", filters.get(5), expectedFilteredNum.get(5), tableName, true);
            checkFiltersInDataSetList("common.name", filters.get(6), expectedFilteredNum.get(6), tableName, true);
        }


    }

    /**
     * Checks each filter of each data type view.
     *
     * @param filterColumn        The to be filtered column
     * @param filter              The filter String the data sets shall be filtered by
     * @param expectedFilteredNum The expected number of resulting elements after filtering
     * @throws InterruptedException
     */
    private void checkFiltersInDataSetList(String filterColumn, String filter, int expectedFilteredNum, String tableName, boolean assign) {
        if (log.isDebugEnabled()) {
            log.debug("Testing filter column " + filterColumn + " with input '" + filter
                    + "' and expected filter count " + expectedFilteredNum);
        }

        //The xpath of column filter input in column header
        String filterColumnXPath = ".//*[@aria-label='" + TestContext.lang.getProperty(filterColumn)
                + ": activate to sort column ascending']/input";

        //The ID xpath of clear filter button
        String clearFilter = ".//button[contains(@id,'clearFilterBtn')]";

        String paginatorXPath = ".//div[contains(@id,'_paginator_top')]/span";
        if (assign) {
            filterColumnXPath = ".//thead[contains(@id,'" + tableName + "')]/tr/th[@aria-label='"
                    + TestContext.lang.getProperty(filterColumn) + ": activate to sort column ascending']/input";
            clearFilter = ".//button[@id='stockTabs:dataSetTabView:ct" + tableName + "DataTableclearFilterBtn']";
            paginatorXPath = ".//div[contains(@id, '" + tableName + "')]/div[contains(@id,'_paginator_top')]/span";
        }

//		String tableEntries = ".//div[contains(@id,'_paginator_top')]/span[contains(.,'"
//                + TestContext.lang.getProperty("common.list.currentEntries") + ": "
//                + String.valueOf(expectedFilteredNum) + "')]";

        // Ignore number of expected entries
        String tableEntries = ".//div[contains(@id,'_paginator_top')]/span[contains(.,'"
                + TestContext.lang.getProperty("common.list.currentEntries") + ": ')]";

        if ($x(filterColumnXPath).exists()) {
            testFunctions.waitUntilSiteLoaded();
            for (int i = 0; i < 5; i++) {
                if (!$x(filterColumnXPath).getValue().equals(filter) || !$x(tableEntries).exists()) {
                    // reference year input field cannot be clicked normally and needs a small offset
                    if (filterColumn.equals("common.dataset.refYear")) {
                        $x(filterColumnXPath).click();
                    } else {
                        $x(filterColumnXPath).click();
                    }
                    testFunctions.waitUntilSiteLoaded();
                    $x(filterColumnXPath).setValue(filter);
                    testFunctions.waitUntilSiteLoaded();
                } else {
                    break;
                }
                testFunctions.waitUntilSiteLoaded();
            }
            if (log.isTraceEnabled()) {
                log.trace("table paginator is: " + $x(paginatorXPath).innerText());
            }

            Selenide.sleep(1000);
            // fail the test if table was not updated
            $x(tableEntries).should(exist);
            $x(filterColumnXPath).clear();
            Selenide.sleep(1000);
            //click on clear filter button and check whether filter is cleared
            if ($x(clearFilter).exists()) {
                int totalEntries = testFunctions.countTotalTableEntries(tableName, assign);
                for (int i = 0; i < 5 && !$x(".//div[contains(@id,'_paginator_top')]/span[contains(.,'"
                        + TestContext.lang.getProperty("common.list.currentEntries") + ": "
                        + String.valueOf(totalEntries) + "')]").exists(); i++) {
                    $x(clearFilter).click();
                    testFunctions.waitUntilSiteLoaded();
                }
                totalEntries = testFunctions.countTotalTableEntries(tableName, assign);
                if (log.isTraceEnabled()) {
                    log.trace("table paginator after clear filter is: " + testFunctions.findAndWaitOnElement(By.xpath(paginatorXPath)).getText());
                    log.trace("total entries is: " + totalEntries);
                }
                if (!$x(".//div[contains(@id,'_paginator_top')]/span[contains(.,'"
                        + TestContext.lang.getProperty("common.list.currentEntries") + ": "
                        + String.valueOf(totalEntries) + "')]").exists()) {
                    org.testng.Assert.fail("table was not updated after clearing filter.");
                }
            }

        }
    }

    /**
     * Gets the name of data set type appearing in xpath ID of data set table in
     * stock view.
     *
     * @param type The data set type
     * @return The Name of data set type appearing in xpath ID of data set table
     * in stock view
     */
    public String getStockTypeName(String type) {
        String tableName = null;
        if (type.equals("processes")) {
            tableName = "Process";
        } else if (type.equals("lciaMethods")) {
            tableName = "LCIAM";
        } else if (type.equals("elementaryFlows")) {
            tableName = "EFlow";
        } else if (type.equals("productFlows")) {
            tableName = "PFlow";
        } else if (type.equals("flowProperties")) {
            tableName = "FlowProp";
        } else if (type.equals("unitGroups")) {
            tableName = "UnitGr";
        } else if (type.equals("sources")) {
            tableName = "Source";
        } else if (type.equals("contacts")) {
            tableName = "Contact";
        }

        return tableName;
    }

    /**
     * The different views the data sets can be filtered.
     * SET: Manage data set view
     * STOCK: manage data stock view with entry RootStock1, tab "Assign
     * data sets"
     * PUBLIC: the public visible data sets view
     *
     * @author sarai
     */
    enum SetView {
        ASSIGN("sets in assign view"), MANAGE("sets in data set view"), PUBLIC("public visible sets");

        private String value;

        SetView(String value) {
            this.value = value;
        }

        public static DataSetState fromValue(String value) {
            for (DataSetState enumValue : DataSetState.values()) {
                if (enumValue.getValue().equals(value))
                    return enumValue;
            }
            return null;
        }

        public String getValue() {
            return value;
        }
    }

}
