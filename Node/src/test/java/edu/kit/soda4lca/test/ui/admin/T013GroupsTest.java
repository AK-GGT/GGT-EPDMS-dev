package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

/**
 * Test Groups menu. Add new groups, then delete one. Add users to groups
 *
 * @author mark.szabo
 */
@Listeners({ScreenShooter.class})
public class T013GroupsTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T013GroupsTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T013GroupsTest.xml"));
    }

    /**
     * Add new groups
     *
     * @throws InterruptedException
     */
    @Test(priority = 131)
    public void newGroups() throws InterruptedException {
        // add new groups
        log.info("'Add new groups' test started");

        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        newGroup("Group1", "Organization1");
        newGroup("Group2", "Organization1");
        newGroup("Group3", "Organization2");
        newGroup("Group4", "Organization1");
        newGroup("Test Group to Delete", "Organization1");

        log.info("'Add new groups' test finished");
    }

    /**
     * Manage groups: delete a group, add some users to other groups
     *
     * @throws InterruptedException
     */
    @Test(priority = 132, dependsOnMethods = {"newGroups"})
    public void manageGroups() throws InterruptedException {
        // manage groups
        log.info("'Manage groups' test started");
        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        log.debug("Delete a group");
        // DELETE A GROUP
        $(By.linkText(TestContext.lang.getProperty("admin.user"))).hover();
        // Click the submenu 'Manage Group'
        for (int i = 0; i < TestContext.timeout && !$x(".//*[@id='tableForm']").exists(); i++)
            $(By.linkText(TestContext.lang.getProperty("admin.group.manageList"))).click();
        // find 'Test Group to Delete' and select it
        for (int i = 0; i < TestContext.timeout && !$x(".//*[@id='deleteBtn']").isEnabled(); i++)
            $x(".//*[@id='groupTable_data']/tr[7]/td[1]/div/div[2]/span").click();

        // Click 'Delete selected entries'
        WebElement deleteBtn = $x(".//*[@id='deleteBtn']");
        deleteBtn.sendKeys(Keys.RETURN);
        // Are you sure? - Click OK
        $x("//button[contains(.,'" + TestContext.lang.getProperty("admin.ok") + "')]").click();
        // Check the message
        if (!$x(".//*[@id='messages']/div/ul/li/span").innerText().contains(TestContext.lang.getProperty("facesMsg.removeSuccess").substring(0, 10)))
            org.testng.Assert.fail("Wrong message: " + $x(".//*[@id='messages']").getText());
        log.debug("Group deleted succesfull");

        log.debug("Add users to groups");
        // ADD USERS TO GROUPS
        String[] users = {"Admin1", "User1", "User3"};
        addUserToGroup("Group1", users, true);
        String[] users2 = {"Admin2", "User1", "User2", "User3"};
        addUserToGroup("Group2", users2, true);
        // delete someone from a group
        String[] users3 = {"User3"};
        addUserToGroup("Group2", users3, false);
        log.debug("Users added to groups succesfull");
        log.info("'Manage groups' test finished");
    }

    /**
     * Add new group
     *
     * @param name         Group name
     * @param organization Organization
     * @throws InterruptedException
     */
    public void newGroup(String name, String organization) throws InterruptedException {
        log.trace("Adding group " + name);
        // wait for the site to load
        $(By.linkText(TestContext.lang.getProperty("admin.globalConfig"))).shouldBe(visible);

        $(By.linkText(TestContext.lang.getProperty("admin.user"))).hover();
        // click the submenu 'New Group'
        for (int i = 0; i < TestContext.timeout &&
                !$x("//button[contains(.,'" + TestContext.lang.getProperty("admin.group.createGroup") + "')]").exists(); i++)
            $(By.linkText(TestContext.lang.getProperty("admin.group.new"))).click();

        // fill in the form
        // name
        $x(".//input[@id='groupName']").clear();
        while ($x(".//input[@id='groupName']").val().equals(""))
            $x(".//input[@id='groupName']").sendKeys(name);
        // organization
        // click on the list
        $x(".//*[@id='org_label']").click();
        // search for the specified element
        int i = 1;
        while (!$x(".//*[@id='org_items']/li[" + i + "]").innerText().contains(organization))
            i++;
        $x(".//*[@id='org_items']/li[" + i + "]").click();
        // Click 'Create a Group'
        testFunctions.
                findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.group.createGroup") + "')]"))
                .sendKeys(Keys.ENTER); // typically there's a drop-down obscuring the button, so we 'tab-select and enter'

        Thread.sleep(TestContext.wait);

        // check if any warning message shows up
        testFunctions.assertNoMessageOtherThan("facesMsg.group.createSuccess");
        // check, if the new site is manageGroupList.xhtml or not

        String expectedView = "manageGroupList.xhtml";
        Instant startOfWait = Instant.now();
        while (Duration.between(startOfWait, Instant.now()).getSeconds() < 10
                && !driver.getCurrentUrl().contains(expectedView))
            if (Thread.currentThread().isInterrupted())
                throw new RuntimeException("Interrupted.");

        String expectedPageTitle = TestContext.lang.getProperty("admin.group.manageList");
        String actualPageTitle = driver.getTitle();
        String actualURL = driver.getCurrentUrl();
        if (!expectedPageTitle.equals(expectedPageTitle))
            Assert.fail(String.format("Unexpected page title '%s', maybe we're in the wrong view '%s'? (expected title: '%s', expected view: '%s')", actualPageTitle, actualURL, expectedPageTitle, expectedView));

//		if ( !isManageView.getAsBoolean() )
//			org.testng.Assert.fail( "After adding a new group, it doesn't go to manageGroupList.xhtml but goes to "
//					+ driver.getCurrentUrl() );

        log.trace("Group " + name + " added succesfull");
    }

    /**
     * Add users to an existing group
     *
     * @param group Group name
     * @param users Name of users to add - array of String
     * @param add   boolean - if add user: true, if remove user from group: false
     * @throws InterruptedException
     */
    public void addUserToGroup(String group, String[] users, boolean add) throws InterruptedException {
        log.trace("Adding users to group " + group + " started");

        // wait for the site to load
        $(By.linkText(TestContext.lang.getProperty("admin.globalConfig"))).shouldBe(visible);
        $(By.linkText(TestContext.lang.getProperty("admin.user"))).hover();
        // Mouse over and click the submenu 'Manage Group'
        for (int i = 0; i < TestContext.timeout &&
                !$(By.linkText(TestContext.lang.getProperty("admin.globalConfig"))).exists(); i++)
            $(By.linkText(TestContext.lang.getProperty("admin.group.manageList"))).click();

        // click the group
        for (int i = 0; i < TestContext.timeout &&
                !$x(".//*[@id='generalForm']").exists(); i++)
            $(By.linkText(group)).click();

        // select users one by one
        for (String user : users) {
            int pickListSide = add ? 1 : 3;
            for (SelenideElement e : $$x(".//*[@id='generalForm']/div[2]/div[2]/div/div[" + pickListSide + "]/ul/li")) {
                if (e.innerText().equals(user))
                    e.doubleClick();
            }
        }
        // Click 'Change Group'

        $x("//button[contains(.,'" + TestContext.lang.getProperty("admin.group.changeGroup") + "')]").click();
        for (int i = 0; i < TestContext.timeout; i++) {
            if ($x(".//*[@id='messages']").isDisplayed())
                break;
        }
        // check the message
        if (!$x(".//*[@id='messages']").innerText().equals(TestContext.lang.getProperty("facesMsg.group.changeSuccess")))
            org.testng.Assert.fail("Error: " + $x(".//*[@id='messages']").val());
        // refresh page
        driver.navigate().refresh();
        log.trace("Adding users to group " + group + " finished succesfull");
    }

}
