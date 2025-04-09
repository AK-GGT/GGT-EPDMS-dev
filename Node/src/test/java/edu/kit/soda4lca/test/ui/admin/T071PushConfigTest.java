package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests the functionality of Configuration and push activity of push feature.
 * Therefore certain push configuration entries are created and changed (correctly as well as incorrectly).
 * Test method testDataPush tests if all given data sets are pushed correctly, therefore the data sets
 * "DB_pre_T071PushConfigTest_fragment" (contains pushTarget and target node entries) and "DB_post_T015ImportExportTest.xml"
 * (contains relevant test data stocks) have to be imported. The target node entry only contains the String "${nodeurl}" and will be replaced
 * by the target site URL while pre-processing (the target site may not be the same on every instance).
 * <p>
 * Lastly, deletion is tested.
 *
 * @author sarai
 */
@Listeners({ScreenShooter.class})
public class T071PushConfigTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T071PushConfigTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T071PushConfigTest_fragment.xml", "DB_post_T015ImportExportTest.xml"));
    }


    /**
     * {@inheritDoc}
     * In this case (PushConfig is tested), the String "${nodeurl}" is replaced by the String targetSite set in TestContext.
     */
    @Override
    protected IDataSet preProcessDataSet(IDataSet dataSet) {
        log.debug("data set contains pushConfigTest");
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
        log.info("Begin of create push test");
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
     * Tests the creation of PushConfig with severals (valid and invalid) settings.
     *
     * @throws InterruptedException
     */
    @Test(priority = 201)
    public void testBeginCreatePushConfig() throws InterruptedException {
        createPushConfig("test_config", null, "SimpleStock1", "test_target", false);
        createPushConfig("test_config", "admin.dependencies.ALL_FROM_DATASTOCK", "SimpleStock1", "test_target", false);
        createPushConfig(null, "admin.dependencies.ALL_FROM_DATASTOCK", "SimpleStock1", "test_target", false);

        createPushConfig("test_config_no_stock", "admin.dependencies.ALL_FROM_DATASTOCK", null, "test_target", false);
        createPushConfig("test_config_no_target", "admin.dependencies.ALL_FROM_DATASTOCK", "SimpleStock1", null, false);
        createPushConfig("test_config", "admin.dependencies.ALL_FROM_DATASTOCK", "SimpleStock1", "test_target", true);
        createPushConfig("test_change_config", "admin.dependencies.ALL_FROM_DATASTOCK", "SimpleStock1", "test_target",
                false);
        createPushConfig("test_delete_config", "admin.dependencies.ALL_FROM_DATASTOCK", "SimpleStock1", "test_target",
                false);

        log.debug("end of create push config test.");
    }

    /**
     * Tests editing of PushConfig with several (valid and invalid) settings.
     *
     * @throws InterruptedException
     */
    @Test(priority = 202, dependsOnMethods = {"testBeginCreatePushConfig"})
    public void testChange() throws InterruptedException {
        changePushConfig("test_config_changed", null, "RootStock1",
                "test_change_target", false);
        changePushConfig("", "admin.dependencies.ALL", "RootStock1", "test_change_target", false);
        changePushConfig("test_config_changed", "admin.dependencies.ALL", "RootStock1", null, false);
        changePushConfig("test_config", "admin.dependencies.ALL", "RootStock1", "test_change_target", true);
        changePushConfig("test_config_changed", "admin.dependencies.ALL", "RootStock1", "test_change_target", false);

        log.debug("End of create push config test.");
    }

    /**
     * Tests setting pushConfig as faourite
     *
     * @throws InterruptedException
     */
    @Test(priority = 203)
    public void testFavourite() throws InterruptedException {
        log.debug("Going to 'create new PushConfig' page.");
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.new");
        $x(".//h1").shouldHave(exactText(TestContext.lang.getProperty("admin.pushConfig.createNew")));
        log.debug("Filling in form.");
        testFunctions.fillInputText("pushNameIn", "not_favourite_first");
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='isFavouriteCheck']/div[2]")).click();
        testFunctions.selectItemInSelectBox("dependenciesModeIn", TestContext.lang.getProperty("admin.dependencies.ALL"));
        testFunctions.selectTableEntryByContent("sourceStockTable", "RootStock1", 2);
        testFunctions.selectItemInSelectBox("targetSelect", "test_change_target");
        log.debug("Saving new entry.");
        testFunctions.clickButtonWithI18nLabel("admin.pushConfig.createNew");
        $x(".//h1").shouldHave(text(TestContext.lang.getProperty("admin.pushConfig.manageList")));
        for (int i = 0; i < TestContext.wait / 2 && !$x(".//h1").getText().contains(TestContext.lang.getProperty("common.welcome")); i++)
            $(By.partialLinkText("soda4LCA Administration")).click();
        if (!testFunctions.isElementNotPresent(By.xpath(".//button/*[contains(.,'not_favourite_first')]"))) {
            org.testng.Assert.fail("favourite push button for first push config was set but is not visible.");
        }
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.manageList");
        testFunctions.waitUntilSiteLoaded();
        if (!testFunctions.isElementNotPresent(testFunctions.getColumnTableEntryByContent("pushTable", "not_favourite_first", 2, 7, "@class='fa fa-check'"))) {
            org.testng.Assert.fail("first pushConfig was set as favourite but is not shown as one in table.");
        }
        testFunctions.findAndWaitOnElement(By.linkText("not_favourite_first")).click();
        if (!testFunctions.isElementNotPresent(By.xpath(".//*[@id='pushConfigMetaData_content']/table/tbody/tr[2]/td[2]/*[@class='fa fa-check']"))) {
            org.testng.Assert.fail("first pushConfig was set as favourite but is not shown as one in pushConfig overview.");
        }
        for (int i = 0; i < TestContext.timeout && $x(".//*[@id='generalForm']").exists(); i++) {
            this.clickButtonWithI18nLabel("admin.pushConfig.edit");
        }
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='isFavouriteCheck']/div[2]")).click();
        testFunctions.clickButtonWithI18nLabel("admin.pushConfig.changePushConfigInfo");
        if (testFunctions.isElementNotPresent(By.xpath(".//*[@id='pushConfigMetaData_content']/table/tbody/tr[2]/td[2]/*[@class='fa fa-check']"))) {
            org.testng.Assert.fail("first pushConfig was not set as favourite but is shown as one in pushConfig overview.");
        }

        testFunctions.findAndWaitOnElement(By.partialLinkText("soda4LCA Administration")).click();
        if (testFunctions.isElementNotPresent(By.xpath(".//button/*[contains(.,'not_favourite_first')]"))) {
            org.testng.Assert.fail("favourite push button for first push config was not set but is visible.");
        }
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.manageList");
        if (testFunctions.isElementNotPresent(testFunctions.getColumnTableEntryByContent("pushTable", "not_favourite_first", 2, 7, "@class='fa fa-check'"))) {
            org.testng.Assert.fail("first pushConfig was not set as favourite but is shown as one in table.");
        }
    }


    /**
     * Tests deletion of seleral PushConfig entries.
     *
     * @throws InterruptedException
     */
    @Test(priority = 204, dependsOnMethods = {"testBeginCreatePushConfig", "testChange"})
    public void testDelete() throws InterruptedException {
        log.debug("begin of change");
        log.debug("Going to manage PushConfig list page.");
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.manageList");
        log.debug("Selecting and deleting entry 'tets_delete_config'.");
        testFunctions.selectTableEntryByContent("pushTable", "test_delete_config", 2);

        testFunctions.clickButtonWithI18nLabel("admin.deleteSelected");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.clickButtonWithI18nLabel("admin.ok");
        testFunctions.waitUntilSiteLoaded();

        if (testFunctions.isTextShown("test_delete_config")) {
            org.testng.Assert.fail("push target entry test_delete_target was not deleted correctly.");
        }
        log.debug("Selecting and deleting all remaining pushConfig entries.");
        testFunctions.selectAllTableEntries("pushTable");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.clickButtonWithI18nLabel("admin.deleteSelected");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.clickButtonWithI18nLabel("admin.ok");
        testFunctions.waitUntilSiteLoaded();
        if (!testFunctions.isTableEmpty("pushTable")) {
            org.testng.Assert.fail("All entries should have been deleted, but aren't.");
        }
    }

    /**
     * Tries to create a pushConfig entry and test whether pushConfig is created correctly or pushConfig is not created if at least one
     * input was incorrect
     *
     * @param name               The name of pushConfig entry
     * @param dependency         The dependency mode of data stock
     * @param stockName          The name of source stock
     * @param pushTarget         The name of push target
     * @param pushConfigExisting True if pushConfig with given name already exists
     * @throws InterruptedException
     */
    public void createPushConfig(String name, String dependency, String stockName, String pushTarget,
                                 boolean pushConfigExisting) throws InterruptedException {
        log.debug("Going to 'create new PushConfig' page.");
        testFunctions.waitUntilSiteLoaded();
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.new");
        $x(".//input[@id='pushNameIn']").shouldBe(visible, Duration.ofSeconds(TestContext.wait * 4));
        log.debug("Filling in form.");
        if (name != null) {
            for (int i = 0; i < TestContext.wait / 2 && !$x(".//input[@id='pushNameIn']").getValue().equals(name); i++)
                testFunctions.fillInputText("pushNameIn", name);
        }
        if (dependency != null) {
            testFunctions.selectItemInSelectBox("dependenciesModeIn", TestContext.lang.getProperty(dependency));
        }
        if (stockName != null) {
            testFunctions.selectTableEntryByContent("sourceStockTable", stockName, 2);
        }
        if (pushTarget != null) {
            testFunctions.selectItemInSelectBox("targetSelect", pushTarget);
        }
        log.debug("Saving new entry.");
        testFunctions.clickButtonWithI18nLabel("admin.pushConfig.createNew");
        testFunctions.waitUntilSiteLoaded();
        if (name == null || name.isEmpty() || dependency == null || stockName == null || pushTarget == null
                || pushConfigExisting) {
            if ((name == null || name.isEmpty()) && !testFunctions.isMessageShown("admin.pushConfig.nameRequired")) {
                org.testng.Assert.fail("No name was entered but no error message is shown.");
            } else if ((dependency == null) && !testFunctions.isMessageShown("admin.pushConfig.dependencyRequired")) {
                org.testng.Assert.fail("No dependency was selected but no error message is shown.");
            } else if ((stockName == null) && !testFunctions.isMessageShown("facesMsg.pushConfig.noSource")) {
                org.testng.Assert.fail("No source was selected but no error message is shown.");
            } else if ((pushTarget == null) && !testFunctions.isMessageShown("admin.pushConfig.targetRequired")) {
                org.testng.Assert.fail("No target was selected but no error message is shown.");
            } else if (pushConfigExisting && !testFunctions.isMessageShown("facesMsg.pushConfig.alreadyExists")) {
                org.testng.Assert
                        .fail("Name of an existing push target was chosen but no error (correct) message is shown.");
            }
            testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushTarget.manageList");
            testFunctions.waitUntilSiteLoaded();

            if (name != null && !pushConfigExisting && (testFunctions.isElementNotPresent(By.linkText(name)))) {
                org.testng.Assert.fail("Push config should not be created but appears with new name in manage list.");
            }
        } else {
            log.debug("After successful save manage push config list should appear. Trying to go to new created entry.");
            if (!(testFunctions.findAndWaitOnElement(By.xpath(".//h1")).getText()
                    .equals(TestContext.lang.getProperty("admin.pushConfig.manageList"))))
                org.testng.Assert
                        .fail("Could not load web page for managing target stocks or could not save new target stock");
            if (!testFunctions.isTextShown(name))
                org.testng.Assert.fail("Created push target does not occur in push target list.");
            for (int i = 0; i < TestContext.wait && $x(".//*[text()='" + name + "']").exists(); i++)
                $x(".//*[text()='" + name + "']").click();
            $x(".//h1").shouldHave(exactText(TestContext.lang.getProperty("admin.pushConfig.show") + ": " + name));

            final var actualPushConfigName = $(By.id("pushNameOut")).getText();
            assertEquals("Incorrect push config name is shown.", name, actualPushConfigName);

            assertEquals("Incorrect dependency mode is shown.", TestContext.lang.getProperty(dependency),
                    $(By.id("dependenciesModeOut")).getText());
            assertEquals("Incorrect target data stock is shown.", stockName,
                    $(By.id("showSourceID")).getText());
            assertEquals("Incorrect push target is shown.", pushTarget, $(By.id("targetName")).getText());
            assertEquals("Incorrect count of data sets is shown.", String.valueOf(0),
                    $(By.id("showSourceDataSetCount")).getText());

            final var expectedNewlyImportedDatasetsCount = 0;
            final var importedDatasetsAfterPushValue = testFunctions.findAndWaitOnElement(By.xpath(
                    ".//*[text()='"
                            + TestContext.lang.getProperty("admin.pushConfig.importedDataSetsAfterPush")
                            + "']/../../td[2]")).getText();
            if (!importedDatasetsAfterPushValue.equals(String.valueOf(expectedNewlyImportedDatasetsCount)))
                org.testng.Assert.fail("Incorrect count of imported data sets is shown. Expected: ''"
                        + ", Found text: '" + importedDatasetsAfterPushValue + "'");
        }

        log.info("End of create push config");
    }

    /**
     * Tries to change pushConfig entry. Tests whether pushConfig is changed correctly or not changed if at least one
     * input was incorrectly set.
     *
     * @param name               The new name of pushConfig entry
     * @param dependency         The new dependency node of source stock
     * @param stockName          The name of new selected source stock
     * @param pushTarget         The name of new selected pushTarget
     * @param pushConfigExisting True if pushConfig with given name already exists
     * @throws InterruptedException
     */
    public void changePushConfig(String name, String dependency, String stockName, String pushTarget,
                                 boolean pushConfigExisting) throws InterruptedException {
        log.debug("Trying to find manage push config page in menu");
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.manageList");
        testFunctions.waitUntilSiteLoaded();
        log.debug("Select entry 'test_change_config'");
        $(By.linkText("test_change_config")).click();
        $x(".//h1").shouldHave(text(TestContext.lang.getProperty("admin.pushConfig.show")));
        for (int i = 0; i < TestContext.wait / 2 && !$x(".//input[@id='pushNameIn']").exists(); i++)
            testFunctions.clickButtonWithI18nLabel("admin.pushConfig.edit");
        $x(".//input[@id='pushNameIn']").should(exist);
        log.debug("Trying to change configuration entry.");

        if (name != null) {
            for (int i = 0; i < TestContext.wait / 2 && !$x(".//input[@id='pushNameIn']").getValue().equals(name); i++) {
                testFunctions.fillInputText("pushNameIn", name);
            }
        }
        if (dependency == null) {
            testFunctions.selectItemInSelectBox("dependenciesModeIn", TestContext.lang.getProperty("common.select.hint"));
        } else {
            testFunctions.selectItemInSelectBox("dependenciesModeIn", TestContext.lang.getProperty(dependency));
        }
        if (stockName != null) {
            testFunctions.selectTableEntryByContent("sourceStockTable", stockName, 2);
        }
        if (pushTarget == null) {
            testFunctions.selectItemInSelectBox("targetSelect", TestContext.lang.getProperty("common.select.hint"));
        } else {
            testFunctions.selectItemInSelectBox("targetSelect", pushTarget);
        }
        log.debug("Save changes.");
        testFunctions.clickButtonWithI18nLabel("admin.pushConfig.changePushConfigInfo");
        testFunctions.waitUntilSiteLoaded();
        if (name == null || name.isEmpty() || dependency == null || pushTarget == null || pushConfigExisting) {
            if ((name == null || name.isEmpty()) && !testFunctions.isMessageShown("admin.pushConfig.nameRequired")) {
                org.testng.Assert.fail("No name was entered but no error message is shown.");
            } else if ((dependency == null) && !testFunctions.isMessageShown("amdin.pushConfig.dependencyRequired")) {
            } else if ((pushTarget == null) && !testFunctions.isMessageShown("admin.pushConfig.targetRequired")) {
                org.testng.Assert.fail("No target was selected but no error message is shown.");
            } else if (pushConfigExisting && !testFunctions.isMessageShown("facesMsg.pushConfig.alreadyExists")) {
                org.testng.Assert
                        .fail("Name of an existing push target was chosen but no error (correct) message is shown.");
            }
        } else {
            log.debug("Change of pushConfig was successfully changed. Entry changes will be now inspected.");
            if (!(testFunctions.findAndWaitOnElement(By.xpath(".//h1")).getText()
                    .contains(TestContext.lang.getProperty("admin.pushConfig.show"))))
                org.testng.Assert.fail("Could not save new target config");

            assertEquals("Incorrect push config name is shown.", name, $(By.id("pushNameOut")).getText());
            assertEquals("Incorrect dependency mode is shown.", TestContext.lang.getProperty(dependency),
                    $(By.id("dependenciesModeOut")).getText());
            assertEquals("Incorrect target data stock is shown.", stockName,
                    $(By.id("showSourceID")).getText());
            assertEquals("Incorrect count of data sets is shown.", String.valueOf(56),
                    $(By.id("showSourceDataSetCount")).getText());
            assertEquals("Incorrect count of imported data sets is shown.", String.valueOf(56),
                    testFunctions.findAndWaitOnElement(By.xpath(
                            ".//*[text()='"
                                    + TestContext.lang.getProperty("admin.pushConfig.importedDataSetsAfterPush")
                                    + "']/../../td[2]")).getText());
        }

        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushConfig.manageList");

        if (name == null || name.isEmpty() || dependency == null || pushTarget == null || pushConfigExisting) {
            log.debug("Invalid changes could not be saved. Hence the new name should not appear in manage list.");
            if (!(testFunctions.isElementNotPresent(By.linkText("test_change_config")))) {
                org.testng.Assert.fail("Push config should occur with old name in manage list but does not.");
            }

            if (!(name == null || name.isEmpty()) && !(name.equals("test_change_config")) && !pushConfigExisting && (testFunctions.isElementNotPresent(By.linkText(name)))) {
                org.testng.Assert.fail("Push config should not be changed but appears with new name in manage list.");
            }
        } else {
            log.debug("Valid changes were successfully saved. Hence new name should now appear in manage list.");
            if (!(name.equals("test_change_config")) && testFunctions.isElementNotPresent(By.linkText("test_change_config"))) {
                org.testng.Assert.fail("The name of push config was changed but appears with old name in manage list.");
            }

            if (!(testFunctions.isElementNotPresent(By.linkText(name)))) {
                org.testng.Assert.fail("push config was successfully changed but does not occur under new name");
            }
        }


        log.info("End of change push config");
    }

    /**
     * Extended method of the clickButtonWithI18nLabel method in TestFunctions, only clicks element if it is Displayed
     *
     * @param name
     */
    private void clickButtonWithI18nLabel(String name) {
        if ($x(".//button/span[text()='" + TestContext.lang.getProperty(name) + "']/..").isDisplayed())
            $x(".//button/span[text()='" + TestContext.lang.getProperty(name) + "']/..").click();
    }


}
