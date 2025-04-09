package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static org.junit.Assert.assertEquals;

/**
 * This test class test the functionality of push feature target. Therefore The creation, changing and deletion of
 * pushTarget entries are tested.
 *
 * @author sarai
 */
@Listeners({ScreenShooter.class})
public class T060PushTargetTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T060PushTargetTest.class);

    // node id of target node
    private String nodeId = "ACME2";

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T013GroupsTest.xml"));
    }

    ;

    /**
     * Sets up target node
     *
     * @param nodeExisting true if target node already exists
     * @param action       The action for retrieving page in admin menu
     * @throws Exception
     */
    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        log.info("Begin of create push test");
        driver.manage().deleteAllCookies();
        log.debug("Trying to login");
        // login as admin
        testFunctions.login("admin", "default", true, true);

        // click on Admin area
        testFunctions.gotoAdminArea();
        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='admin_footer']")));
    }

    /**
     * Tests creation of PushTarget with several (valid and invalid) settings.
     *
     * @throws InterruptedException
     */
    @Test(priority = 101)
    public void testBeginCreate() throws InterruptedException {
        createPushTarget("test_target", TestContext.SECONDARY_SITE_URL, "admin", "default", true, false, true, false, false);
        createPushTarget(null, TestContext.SECONDARY_SITE_URL, "admin", "default", false, false, true, false, false);
        createPushTarget("test_target_3", TestContext.SECONDARY_SITE_URL, "", "default", false, false, false, false, false);
        createPushTarget("test_target_4", TestContext.SECONDARY_SITE_URL, "admin", "", false, false, false, false, false);
        createPushTarget("test_target_5", TestContext.SECONDARY_SITE_URL, "admin", "default", false, false, true, false, false);
        createPushTarget("test_target_6", TestContext.SECONDARY_SITE_URL, "admin", "default", true, true, true, false, false);
        createPushTarget("test_target_7", null, "admin", "default", false, false, true, false, false);
        createPushTarget("test_target_8", "sdfdfgd", "admin", "default", false, false, true, false, true);
        createPushTarget("test_change_target", TestContext.SECONDARY_SITE_URL, "admin", "default", true, false, true, false, false);
        createPushTarget("test_delete_target", TestContext.SECONDARY_SITE_URL, "admin", "default", true, false, true, false, false);
        createPushTarget("test_target", TestContext.SECONDARY_SITE_URL, "admin", "default", true, false, true, true, false);
    }

    /**
     * Tests editing of PushTarget entry with several (valid and invalid) settings.
     *
     * @throws InterruptedException
     */
    @Test(priority = 102, dependsOnMethods = {"testBeginCreate"})
    public void testChange() throws InterruptedException {
        changeTarget("", "admin", "default", TestContext.SECONDARY_SITE_URL, true, true, true, false, false, true);
        changeTarget("test_target", "admin", "default", TestContext.SECONDARY_SITE_URL, true, true, true, false, false, true);
        changeTarget(null, "", "default", TestContext.SECONDARY_SITE_URL, true, false, false, false, false, true);
        changeTarget(null, "admin", "", TestContext.SECONDARY_SITE_URL, true, false, false, false, false, true);
        changeTarget(null, "admin", "default", "", false, true, false, false, false, true);
        changeTarget(null, "admin", "default", "sdfdfgdfg", false, true, false, false, true, true);
        changeTarget(null, "admin", "default", TestContext.SECONDARY_SITE_URL, true, true, false, false, false, true);
        changeTarget(null, "admin", "default", TestContext.SECONDARY_SITE_URL, true, true, false, true, false, true);
        changeTarget("test_new_name", "admin", "default", TestContext.SECONDARY_SITE_URL, true, true, false, false, false, false);
        changeTarget("test_new_name", "admin", "default", TestContext.SECONDARY_SITE_URL, true, true, false, false, false, true);
    }

    /**
     * Tests deletion of several PusTarget entries.
     *
     * @throws InterruptedException
     */
    @Test(priority = 103, dependsOnMethods = {"testBeginCreate"})//, "testChange"
    public void testDelete() throws InterruptedException {
        log.debug("Going to page 'Manage PushTarget list'.");
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushTarget.manageList");
        log.debug("Selecting 'test_delete_target' entry and deleting it.");
        testFunctions.selectTableEntryByContent("pushTable", "test_delete_target", 2);
        testFunctions.clickButtonWithI18nLabel("admin.deleteSelected");
        testFunctions.clickButtonWithI18nLabel("admin.ok");
        if (testFunctions.isElementNotPresent(By.xpath(".//(*[text()='test_delete_target']"))) {
            org.testng.Assert.fail("push target entry test_delete_target was not deleted correctly.");
        }
        log.debug("selecting all remaining entries in list and deleting them.");
        testFunctions.selectAllTableEntries("pushTable");
        testFunctions.clickButtonWithI18nLabel("admin.deleteSelected");
        testFunctions.clickButtonWithI18nLabel("admin.ok");

        if (!testFunctions.isTableEmpty("pushTable")) {
            org.testng.Assert.fail("All entries should have been deleted, but aren't.");
        }
    }

    /**
     * Tries to create a pushTarget entry and test whether pushTarget is created correctly or pushTarget is not created if at least one
     * input was incorrect
     *
     * @param name              The name of push target
     * @param node              The name of target node
     * @param login             The user name of target user
     * @param passw             The password of target user
     * @param selectTargetStock True if target stock is selected
     * @param nodeLaterChanged  True if target node is set to null after available target stcoks are shown
     * @param loginPasswCorrect True if the combination of user name and password is correct
     * @param nodeExisting      True if target node with given name already exists
     * @param targetExisting    True if pushTarget entry with given name already exists
     * @throws InterruptedException
     */
    public void createPushTarget(String name, String node, String login, String passw, boolean selectTargetStock,
                                 boolean nodeLaterChanged, boolean loginPasswCorrect, boolean targetExisting, boolean illegalNodeURL)
            throws InterruptedException {
        log.debug("Going to 'Create new PushTarget' page.");
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushTarget.new");
        log.debug("Filling in form.");
        testFunctions.fillInputText("pushNameIn", name);
        testFunctions.fillInputText("targetUrl", node);
//		if (node != null) {
//			testFunctions.selectItemInSelectBox("nodeSelectionIn", node);
//		}

        testFunctions.fillInputText("login", login);
        testFunctions.fillInputText("passw", passw);

        log.debug("Trying to login to target page for showing available target stocks.");
        testFunctions.findAndWaitOnElement(By.id("pushTargetShow")).click();
        Thread.sleep(TestContext.wait);

        if (name == null || name.isEmpty() || !loginPasswCorrect || node == null || illegalNodeURL) {
            if (!testFunctions.isTableEmpty("targetStockTable")) {
                org.testng.Assert.fail("PushTarget should have not enough information to show target stocks.");
            }
            if ((name == null || name.isEmpty()) && !testFunctions.isMessageShown("admin.pushTarget.nameRequired")) {
                org.testng.Assert.fail("push target name is efmpty but not corresponding error message is shown.");
            }
            if ((node == null || node.isEmpty()) && !testFunctions.isMessageShown("admin.pushTarget.targetURLRequired")) {
                org.testng.Assert.fail("node URL is empty but no corresponding error message is shown.");
            }
            if (illegalNodeURL && !testFunctions.isMessageShown("facesMsg.push.networkError")) {
                org.testng.Assert.fail("String in input for node URL is no valid URL but no corresponding error message is shown.");
            }
        } else if (selectTargetStock)
            testFunctions.selectTableEntryByContent("targetStockTable", "default", 2);
        if (nodeLaterChanged) {
            testFunctions.fillInputText("targetUrl", TestContext.SECONDARY_SITE_URL);
            testFunctions.fillInputText("login", "admin");
            testFunctions.fillInputText("passw", "default");
            testFunctions.findAndWaitOnElement(By.id("pushTargetShow")).click();
//			testFunctions.selectItemInSelectBox("nodeSelectionIn", TestContext.lang.getProperty("common.select.hint"));
        }
        log.debug("Saving entry.");
        testFunctions.clickButtonWithI18nLabel("admin.pushTarget.createNew");

        if (name == null || name.isEmpty() || node == null || targetExisting) {
            if ((name == null || name.isEmpty()) && !testFunctions.isMessageShown("admin.pushTarget.nameRequired")) {
                org.testng.Assert.fail("No name was entered but no error message is shown.");
            } else if ((node == null || node.isEmpty() || illegalNodeURL) && !testFunctions.isMessageShown("admin.pushTarget.targetURLRequired")) {
                org.testng.Assert.fail("node is illegaly set but no corresponding error messag is shown.");
            } else if (targetExisting && !testFunctions.isMessageShown("facesMsg.pushTarget.alreadyExists")) {
                org.testng.Assert
                        .fail("Name of an existing push target was chosen but no error (correct) message is shown.");
            }
        } else if (selectTargetStock && !nodeLaterChanged && !illegalNodeURL) {
            log.debug("Valid entry was saved and current page should be 'Manage PushTarget list.' Trying to go to new created entry.");
            if (!(testFunctions.findAndWaitOnElement(By.xpath(".//h1")).getText()
                    .equals(TestContext.lang.getProperty("admin.pushTarget.manageList"))))
                org.testng.Assert
                        .fail("Could not load web page for managing target stocks or could not save new target stock");
            if (!testFunctions.isTextShown(name))
                org.testng.Assert.fail("Created push target does not occur in push target list.");
            testFunctions.findAndWaitOnElement(By.xpath(".//*[text()='" + name + "']")).click();

            assertEquals("Incorrect push target name is shown.", name, $(By.id("pushNameOut")).getText());
            assertEquals("Incorrect target node is shown.", nodeId, $(By.id("nodeOut")).getText());
            assertEquals("Incorrect target data stock is shown.", "default", $(By.id("dsID")).getText());

        } else if (!testFunctions.isMessageShown("facesMsg.pushTarget.noTargetStock")) {
            org.testng.Assert.fail("No target stock was selected but no error message is shown");
        }

        if (name == null || name.isEmpty() || nodeLaterChanged || node == null || illegalNodeURL || !selectTargetStock) {
            log.debug("Going to 'Manage PushTarget list' page. Invalid entry should not occur in list.");
            testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushTarget.manageList");

            if (name != null && (testFunctions.isElementNotPresent(By.linkText(name)))) {
                org.testng.Assert.fail("Push target should not be created but appears with new name in manage list.");
            }
        }

        log.info("End of create push target");
    }

    /**
     * Logs in
     *
     * @throws InterruptedException
     */
    public void login() throws InterruptedException {

    }


    /**
     * Tries to change target. Tests whether pushTarget is changed correctly or not changed if at least one
     * input was incorrectly set.
     *
     * @param name               The new name of pushTarget entry
     * @param login              The user name of target node
     * @param passw              The password of target user
     * @param stockSelected      True if source stock is selected
     * @param loginPasswCorrect  True if the combination of user name and password is correct
     * @param nameIllegalChanged True if new name is not accepted as pushTarget name
     * @param nodeChanged        True if node is changed to null after target stock is selected
     * @throws InterruptedException
     */
    public void changeTarget(String name, String login, String passw, String node, boolean stockSelected, boolean loginPasswCorrect,
                             boolean nameIllegalChanged, boolean nodeChanged, boolean illegalNodeURL, boolean saveChanges) throws InterruptedException {

        log.debug("Going to 'Manage PushTarget list' page.");
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushTarget.manageList");
        log.debug("Goint to entry test_change_target.");
        testFunctions.findAndWaitOnElement(By.xpath(".//*[text()='test_change_target']")).click();
        log.debug("Editing pushTarget entry.");
        testFunctions.clickButtonWithI18nLabel("admin.pushTarget.edit");
        if (name != null)
            testFunctions.fillInputText("pushNameIn", name);

        testFunctions.fillInputText("targetUrl", node);

        testFunctions.fillInputText("login", login);
        testFunctions.fillInputText("passw", passw);

        log.debug("Trying to login to target site for showing available target stocks.");
        testFunctions.findAndWaitOnElement(By.id("pushTargetShow")).click();


        if ((name != null && name.isEmpty()) || !loginPasswCorrect || (node != null && node.isEmpty()) || illegalNodeURL) {
            if (!testFunctions.isTableEmpty("targetStockTable")) {
                org.testng.Assert.fail("PushTarget should have not enough information to show target stocks.");
            }
            if ((node != null && node.isEmpty()) && !testFunctions.isMessageShown("admin.pushTarget.targetURLRequired")) {
                org.testng.Assert.fail("node URL is empty but no corresponding error message is shown.");
            }
            if (illegalNodeURL && !testFunctions.isMessageShown("facesMsg.push.networkError")) {
                org.testng.Assert.fail("String in input for node URL is no valid URL but no corresponding error message is shown.");
            }
        } else if (stockSelected) {
            testFunctions.selectTableEntryByContent("targetStockTable", "default", 2);
        }
        if (nodeChanged) {
            testFunctions.fillInputText("targetUrl", TestContext.SECONDARY_SITE_URL);
            testFunctions.fillInputText("login", "admin");
            testFunctions.fillInputText("passw", "default");
            testFunctions.findAndWaitOnElement(By.id("pushTargetShow")).click();
        }
        if (saveChanges) {
            log.debug("Saving changes.");
            testFunctions.clickButtonWithI18nLabel("admin.pushTarget.changePushTargetInfo");
            if ((nameIllegalChanged || (name != null && name.isEmpty())) || (node != null && node.isEmpty()) || nodeChanged || illegalNodeURL || !loginPasswCorrect) {
                if ((name != null && name.isEmpty()) && !testFunctions.isMessageShown("admin.pushTarget.nameRequired")) {
                    org.testng.Assert.fail("No name was entered but no error message is shown.");
                } else if (nodeChanged && !testFunctions.isMessageShown("facesMsg.pushTarget.noTargetStock")) {
                    org.testng.Assert.fail("no node was selected but no error message is shown.");
                } else if (nameIllegalChanged && (name != null && !name.isEmpty())
                        && !testFunctions.isMessageShown("facesMsg.pushTarget.alreadyExists")) {
                    org.testng.Assert
                            .fail("Name of an existing push target was chosen but no error (correct) message is shown.");
                } else if (node != null && node.isEmpty() && !testFunctions.isMessageShown("admin.pushTarget.targetURLRequired")) {
                    org.testng.Assert.fail("No node url is entered but no corresponding error message is shown.");
                } else if ((nodeChanged || illegalNodeURL || !loginPasswCorrect) && !testFunctions.isMessageShown("facesMsg.pushTarget.noTargetStock")) {
                    org.testng.Assert.fail("No stock was selected but no corresponding error message is shown.");
                }
            } else {
                log.debug("Checking after save of correct change if current view is not editable and if all information is correct.");
                if ((testFunctions.findAndWaitOnElement(By.xpath(".//h1")).getText()
                        .contains(TestContext.lang.getProperty("admin.pushTarget.edit"))))
                    org.testng.Assert.fail("Form is still editable but should not.");
                if (!testFunctions.isMessageShown("facesMsg.pushTarget.changeSuccess")) {
                    org.testng.Assert.fail("Entry was not changed.");
                }

                assertEquals("Incorrect push target name is shown.",
                        name != null ? name : "test_change_target", $(By.id("pushNameOut")).getText());
                assertEquals("Incorrect target node is shown.", nodeId, $(By.id("nodeOut")).getText());
                assertEquals("Incorrect target data stock is shown.", "default",
                        $(By.id("dsID")).getText());
            }
        } else {
            log.debug("Quitting edit.");
            testFunctions.clickButtonWithI18nLabel("admin.pushConfig.cancel");
        }
        log.debug("Going to 'Manage PushTarget list' page.");
        testFunctions.goToPageByAdminMenu("admin.network", "admin.push", "admin.pushTarget.manageList");

        if (name == null || name.isEmpty() || !loginPasswCorrect || nodeChanged || nameIllegalChanged || !saveChanges || (node != null && node.isEmpty()) || illegalNodeURL) {
            log.debug("Checking if changed entry with new name occurs in list (should not since entry was changed incorrectly)");
            if (!(testFunctions.isElementNotPresent(By.linkText("test_change_target")))) {
                org.testng.Assert.fail("Push config should occur with old name in manage list but does not.");
            }

            if (!(name == null || name.isEmpty()) && !(name.equals("test_change_target")) && !nameIllegalChanged && (testFunctions.isElementNotPresent(By.linkText(name)))) {
                org.testng.Assert.fail("Push target should not be changed but appears with new name in manage list.");
            }
        } else {
            log.debug("Checking if changed entry with new name occurs in list (should appear since entry was changed correctly)");
            if (!(name.equals("test_change_config")) && testFunctions.isElementNotPresent(By.linkText("test_change_config"))) {
                org.testng.Assert.fail("The name of push target was changed but appears with old name in manage list.");
            }

            if (!(testFunctions.isElementNotPresent(By.linkText(name)))) {
                org.testng.Assert.fail("push target was successfully changed but does not occur under new name");
            }
        }
        log.info("End of change push target");
    }

}
