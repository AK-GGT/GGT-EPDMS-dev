package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

@Listeners({ScreenShooter.class})
public class T017StockTest_hidden extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T017StockTest_hidden.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T014StockTest.xml"));
    }

    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        log.info("Begin of hidden stock test");
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

    private void setupStock(Actions action, String oldID, String newID, String stockType, boolean visible)
            throws InterruptedException {
        testFunctions.goToPageByAdminMenu("common.stock", "admin.stock.manageList");
        testFunctions.waitUntilSiteLoaded();
        if (oldID != null) {
            testFunctions.findAndWaitOnCheckBox(TestContext.lang.getProperty("admin.stock.showHidden")).click();
            Thread.sleep(TestContext.wait * 5);
            if (!testFunctions
                    .isElementNotPresent(By.xpath(".//*[@id='stockTable_data']/tr/td[3]/*[text()='" + oldID + "']"))) {
                org.testng.Assert.fail("Element with ID " + oldID + " should now be visible but is not.");
            }
            testFunctions
                    .findAndWaitOnElement(By.xpath(".//*[@id='stockTable_data']/tr/td[3]/*[text()='" + oldID + "']"))
                    .click();
        } else if (stockType != null) {
            testFunctions.clickButtonWithI18nLabel("admin." + stockType + ".new");
        }
        testFunctions.fillInputText("stockTabs:name", newID);
        if (!visible) {
            testFunctions
                    .findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:hiddenChoice']/tbody/tr/td[2]/div[1]/div[2]"))
                    .click();
        } else {
            testFunctions
                    .findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:hiddenChoice']/tbody/tr/td[1]/div[1]/div[2]"))
                    .click();
        }
        testFunctions.selectItemInSelectBox("stockTabs:org_label", "Default Organization");
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.saveNew"))).click();
    }

    @Test
    public void testHiddenStocks() throws InterruptedException {

        Actions action = new Actions(driver);
        setupStock(action, null, "first_visible", "rootStock", true);
        setupStock(action, null, "second_visible", "stock", true);
        setupStock(action, null, "third_invisible", "rootStock", false);
        setupStock(action, null, "fourth_visible", "rootStock", true);
        setupStock(action, null, "fifth_invisible", "stock", false);
        checkHiddenStocks(4);
        setupStock(action, "first_visible", "first_invisible", null, false);
        setupStock(action, "second_visible", "second_invisible", null, false);
        setupStock(action, "fourth_visible", "fourth_invisible", null, false);
        checkHiddenStocks(1);
        setupStock(action, "first_invisible", "first_visible", null, true);
        setupStock(action, "second_invisible", "second_visible", null, true);
        setupStock(action, "third_invisible", "third_visible", null, true);
        setupStock(action, "fourth_invisible", "fourth_visible", null, true);
        setupStock(action, "fifth_invisible", "fifth_visible", null, true);
        checkHiddenStocks(6);

    }

    private void checkHiddenStocks(int expectedVisibleEntries) throws InterruptedException {
        testFunctions.goToPageByAdminMenu("common.stock", "admin.stock.manageList");
        int countVisibleEntries = driver
                .findElements(By.xpath(".//*[@id='stockTable_data']/tr")).size();
        if (countVisibleEntries != expectedVisibleEntries) {
            org.testng.Assert.fail("Inncorrect count of visible data stocks is shown. Expected: "
                    + expectedVisibleEntries + ", shown: " + countVisibleEntries);
            for (int i = 1; i <= countVisibleEntries; i++) {
                String currentElement = testFunctions
                        .findAndWaitOnElement(By.xpath(".//*[@id='stockTable_data']/tr[" + i + "]/td[2]/a")).getText();
                if (currentElement.contains("invisible")) {
                    org.testng.Assert.fail("Invisible element " + currentElement + " is unexpectedly shown.");
                }
            }
        }
        testFunctions.findAndWaitOnCheckBox(TestContext.lang.getProperty("admin.stock.showHidden")).click();
        Thread.sleep(TestContext.wait * 5);
        int countAllEntries = driver
                .findElements(By.xpath(".//*[@id='stockTable_data']/tr")).size();
        if (countAllEntries != 6) {
            org.testng.Assert.fail("Inncorrect count of all data is shown. Expected: 6, shown: " + countAllEntries);
        }
    }

}
