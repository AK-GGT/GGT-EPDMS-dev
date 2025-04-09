package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

/**
 * @author sarai
 */
@Listeners({ScreenShooter.class})
public class T017AssignDataStockTest extends AbstractUITest {

    protected final static Logger log = LogManager.getLogger(T017AssignDataStockTest.class);
    private int assignButton = 8;

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    @Test
    public void assignProcessDataTest() throws InterruptedException {
        assignData("4a1ebe7c-6835-4a22-8b2e-3201f1cd32e8", "Process", "processes");
    }

    @Test
    public void assignLCIAMethodsDataTest() throws InterruptedException {
        assignData("992c8e8d-769a-4930-9b0f-4fa323250738", "LCIAM", "lciaMethods");
    }

    @Test
    public void assignElementaryFlowsDataTest() throws InterruptedException {
        assignData("0a0ba345-bba1-411f-84cc-e72b4b7d1027", "EFlow", "elementaryFlows");
    }

    @Test
    public void assignProductFlowsDataTest() throws InterruptedException {
        assignData("82b33e71-bfaa-49b4-9627-ee5963433f6e", "PFlow", "productFlows");
    }

    @Test
    public void assignFlowPropertiesDataTest() throws InterruptedException {
        assignData("93a60a56-a3c8-19da-a746-0800200c9a66", "FlowProp", "flowProperties");
    }

    @Test
    public void assignUnitGroupsDataTest() throws InterruptedException {
        assignData("b4cac580-5ce8-11df-a08a-0800200c9a66", "UnitGr", "unitGroups");
    }

    @Test
    public void assignSourcesDataTest() throws InterruptedException {
        assignData("7983f4c6-a355-4250-aaa8-5780a72cc1df", "Source", "sources");
    }

    @Test
    public void assignContactsDataTest() throws InterruptedException {
        assignData("16e9b2c6-3c44-11dd-ae16-0800200c9a66", "Contact", "contacts");
    }

    public void assignData(String uuid, String dataSetType, String dataSetType2) throws InterruptedException {
        Properties lang = TestContext.lang;
        log.info("Assign " + dataSetType2 + " data test started");
        // assign some data stocks
        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true, 1, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        // wait for the site to load
        testFunctions.waitUntilSiteLoaded();
        // go to Manage Data Stocks
        testFunctions.goToPageByAdminMenu("common.stock", "admin.stock.manageList");

        // click assign data stock button in SimpleStock1 data stock
        $x(".//*[text()='SimpleStock1']/../../td[" + assignButton + "]/button[*]").click();
        if (!"Process".equals(dataSetType)) {
            // switch to another data set
            $(By.linkText(lang.getProperty("common." + dataSetType2))).click();
        }
        // Test if data set is empty
        if (testFunctions.isElementNotPresent(
                By.xpath(".//*[@id='stockTabs:dataSetTabView:ct" + dataSetType + "DataTable_data']/tr[1]/td[2]")))
            org.testng.Assert.fail("logical data stock contains at least one element.");

        // click on assign button to assign a data set
        Selenide.sleep(300);
        $(By.id("stockTabs:dataSetTabView:assignDataSetBtn" + dataSetType)).click();

        // test whether assignable data set list is not empty and whether
        // desired data set is occurs in assignable data set list
        testFunctions.findAndWaitOnElement(
                By.xpath(".//*[@id='stockTabs:dataSetTabView:ad" + dataSetType + "DataTable_data']/tr[1]/td[2]"));
        // if
        // (!testFunctions.isElementNotPresent(By.xpath(".//*[contains(text(),'"
        // + uuid + "')]/..")))
        // org.testng.Assert.fail( "To be assigned data set is not present." );
        if (!testFunctions.isElementNotPresent(By.xpath(".//*[@id='stockTabs:dataSetTabView:ad" + dataSetType
                + "DataTable']/descendant::*[contains(text(),'" + uuid + "')]")))
            org.testng.Assert.fail("To be assigned data set is not present.");

        testFunctions.selectDataSetFromDataTable(uuid);

        if (!testFunctions.isElementNotPresent(By.id("stockTabs:dataSetTabView:ad" + dataSetType + "DataTablebtn")))
            org.testng.Assert.fail("assign button is not present.");
        testFunctions.waitUntilSiteLoaded();

        // click on assign button
        $(By.id("stockTabs:dataSetTabView:ad" + dataSetType + "DataTablebtn")).click();
        Selenide.sleep(100);
        testFunctions.waitUntilSiteLoaded();

        if ("Process".equals(dataSetType))
            testFunctions.selectOptionInDataSetDialogue(4);
        else
            testFunctions.selectOptionInDataSetDialogue(1);


        // Test if assigned data set actually occurs in logical data stock
        if (!testFunctions.isElementNotPresent(
                By.xpath(".//*[@id='stockTabs:dataSetTabView:ct" + dataSetType + "DataTable_data']/tr[1]/td[2]"))) {
            $(By.id("stockTabs:dataSetTabView:assignDataSetBtn" + dataSetType)).click();
            Selenide.sleep(400);
            if (testFunctions.isElementNotPresent(By.xpath(".//*[@id='stockTabs:dataSetTabView:ad" + dataSetType
                    + "DataTable']/descendant::*[contains(text(),'" + uuid + "')]"))) {
                org.testng.Assert.fail("To be assigned element was not assigned.");
            } else {
                org.testng.Assert.fail("To be assigned element was assigned but does not occur.");
            }
        }
        testFunctions.findAndWaitOnElement(By.xpath(".//*[contains(text(),'" + uuid + "')]/.."));

        // test if assigned data set is still in assignable data set list
        testFunctions.findAndWaitOnElement(By.id("stockTabs:dataSetTabView:assignDataSetBtn" + dataSetType)).click();
        testFunctions.waitUntilSiteLoaded();
        if (testFunctions.isElementNotPresent(By.xpath(".//*[@id='stockTabs:dataSetTabView:ad" + dataSetType
                + "DataTable']/descendant::*[contains(text(),'" + uuid + "')]")))
            org.testng.Assert.fail("Assigned element is shown in to be assigned data sets.");
        log.info("Assign " + dataSetType2 + " data test finished.");
    }

}
