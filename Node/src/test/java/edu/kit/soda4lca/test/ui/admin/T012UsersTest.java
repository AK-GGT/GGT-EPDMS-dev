package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Add some users, then delete one and change an other one
 *
 * @author mark.szabo
 */
@Listeners({ScreenShooter.class})
public class T012UsersTest extends AbstractUITest {

    public static final int MANAGE_USERS_COLUMN_INDEX_CHECKBOX = 1;
    public static final int MANAGE_USERS_COLUMN_INDEX_USERNAME = 3;
    // initializing the log
    protected final static Logger log = LogManager.getLogger(T012UsersTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T012UsersTest.xml"));
    }

    ;

    /**
     * Add new users
     *
     * @throws InterruptedException
     */
    @Test(priority = 121)
    public void newUsers() throws InterruptedException {
        // create some new user
        log.info("'Create new users' test started");

        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        String password = "S3cr3tpassw";

        // add normal users
        createNewUser("User1", "john@iai.kit.edu", password, "Mr", "John", "Smith", "junior java developer", true, false, "Organization1");
        createNewUser("User2", "peter@iai.kit.edu", password, "Mr", "Peter", "Tailor", "java tester", true, false, "Organization1");
        createNewUser("User3", "whatever@iai.kit.edu", password, "Mr", "What", "Ever", "humorist", true, false, "Organization1");
        createNewUser("User4", "sara@iai.kit.edu", password, "Mrs", "Sara", "Smith", "junior java developer", false, false, "Organization1");
        createNewUser("User5", "alice@iai.kit.edu", password, "Ms", "Alice", "Regenkurt", "junior java developer", false, false, "Organization1");
        createNewUser("User6", "ceo@iai.kit.edu", password, "Dr", "Alexander", "Alexandrius", "senior java developer", true, false, "Organization2");
        createNewUser("User7", "noidea@iai.kit.edu", password, "Mr", "Idea", "No", "I've no idea what doing here", true, false, "Organization2");
        createNewUser("DeleteTestUser", "noidea2@iai.kit.edu", password, "M", "Idea", "No", "don't fire me!!!", true, false, "Organization3");

        // add some users with admin rights
        createNewUser("Admin1", "admin@iai.kit.edu", password, "Mr", "John", "Root", "admin", true, true, "Organization1");
        createNewUser("Admin2", "webmaster@iai.kit.edu", password, "Mr", "Java", "Script", "webmaster", true, true, "Organization1");
        createNewUser("Admin3", "designer@iai.kit.edu", password, "Ms", "Kathy", "Artist", "designer", true, true, "Organization2");

        log.info("'Create new users' test finished");
    }

    /**
     * Manage users. First delete one (named: DeleteTestUser), then change the zip code of an other one (named User1)
     *
     * @throws InterruptedException
     */
    @Test(priority = 122, dependsOnMethods = {"newUsers"})
    public void manageUsers() throws InterruptedException {
        // manage users
        log.info("'Manage users - delete and change users' test started");

        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        log.debug("Delete user");
        // DELETE A USER
        // Mouse over the menu 'User'
        $(By.linkText(TestContext.lang.getProperty("admin.user"))).hover();
        // Mouse over the submenu 'Manage Users'
        $(By.linkText(TestContext.lang.getProperty("admin.user.manageList"))).click();
        // wait for the site to load
        $(By.linkText(TestContext.lang.getProperty("public.user.logout"))).shouldBe(visible);
        int i = 1;
        while (!$x(".//*[@id='userTable_data']/tr[" + Integer.toString(i) + "]/td[" + MANAGE_USERS_COLUMN_INDEX_USERNAME + "]/a").getText().contains(
                "DeleteTestUser"))
            i++;
        // select it
        $x(".//*[@id='userTable_data']/tr[" + Integer.toString(i) + "]/td[" + MANAGE_USERS_COLUMN_INDEX_CHECKBOX + "]/div/div[2]").click();
        // wait for the clientside script
        // Click 'Delete selected entries'
        $x(".//*[@id='deleteBtn']").click();
        // Are you sure? - Click OK
        $x("//button[contains(.,'" + TestContext.lang.getProperty("admin.ok") + "')]").click();
        // Check the message
        if (!$x(".//*[@id='messages']").getText().contains(
                TestContext.lang.getProperty("facesMsg.removeSuccess").substring(0, 10)))
            org.testng.Assert.fail("Wrong message: " + $x(".//*[@id='messages']").getText());
        log.debug("User deleted succesfull");

        log.debug("Change a zipCode of a user");
        // CHANGE USER INFORMATION
        // Click on TestUser
        while (!$x(".//input[@id='zipCode']").exists())
            $(By.linkText("User1")).click();
        // Change ZIP Code
        $x(".//input[@id='zipCode']").clear();
        $x(".//input[@id='zipCode']").setValue("1234");

        if (testFunctions.isElementNotPresent(By.xpath(".//*[text()='" + TestContext.lang.getProperty("common.privacyPolicyTitle") + "']"))) {
            WebElement privacyPolicyCheckbox = driver.findElement(By.id("acceptPrivacyPolicy_input"));
            WebElement privacyPolicyCheckboxVisible = testFunctions.findAndWaitOnElement(By.id("acceptPrivacyPolicy"));
            if (!privacyPolicyCheckbox.isSelected()) {
                privacyPolicyCheckboxVisible.click();
            }
        }

        // Click Change user information
        $x("//button[contains(.,'" + TestContext.lang.getProperty("admin.user.changeUserInfo") + "')]").click();
        // wait for a message
        for (int j = 0; j < TestContext.timeout; j++) {
            if ($x(".//*[@id='messages']").isDisplayed())
                break;
        }
        // Check the message
        if (!$x(".//*[@id='messages']").innerText().contains(
                TestContext.lang.getProperty("facesMsg.user.accountChanged")))
            org.testng.Assert.fail("Wrong message: " + $x(".//*[@id='messages']").innerText());
        log.debug("zipCode of a user changed succesfull");

        log.info("'Manage users - delete and change users' test finished");
    }

    /**
     * Add user
     *
     * @param user         Username
     * @param email        email
     * @param password     password
     * @param title        title (Mr, Mrs, Dr)
     * @param firstname    First name
     * @param surname      Last name
     * @param job          Professional Position
     * @param gender       Gender, boolean: true-male, false-female
     * @param admin        Super admin right, boolean
     * @param Organization Organization
     * @throws InterruptedException
     */
    public void createNewUser(String user, String email, String password, String title, String firstname, String surname, String job, boolean gender,
                              boolean admin, String Organization) throws InterruptedException {
        log.trace("Adding user " + user);

        testFunctions.waitOnAdminArea();

        $(By.linkText(TestContext.lang.getProperty("admin.user"))).hover();

        // Mouse over the submenu 'New User' and click
        for (int i = 0; i < TestContext.timeout && !$x("//button[contains(.,'" + TestContext.lang.getProperty("admin.user.createUser") + "')]").exists(); i++)
            $(By.linkText(TestContext.lang.getProperty("admin.user.new"))).click();

        // FILL IN THE FORM
        // Name
        $x(".//input[@id='nameIn']").clear();
        for (int i = 0; i < TestContext.timeout && $x(".//input[@id='nameIn']").val().equals(""); i++)
            $x(".//input[@id='nameIn']").setValue(user);
        // email
        //$x( ".//input[@id='email']" ).click();
        $x(".//input[@id='email']").clear();
        $x(".//input[@id='email']").setValue(email);

        // password
        //$x( ".//input[@id='passw']" ).click();
        $x(".//input[@id='passw']").clear();
        $x(".//input[@id='passw']").setValue(password);

        // password again
        $x(".//input[@id='repeatPassw']").clear();
        $x(".//input[@id='repeatPassw']").setValue(password);

        // title
        $x(".//input[@id='title']").clear();
        $x(".//input[@id='title']").setValue(title);

        // first name
        $x(".//input[@id='firstName']").clear();
        $x(".//input[@id='firstName']").setValue(firstname);

        // last name
        $x(".//input[@id='lastName']").clear();
        $x(".//input[@id='lastName']").setValue(surname);

        // Professional Position
        $x(".//input[@id='jobposition']").clear();
        $x(".//input[@id='jobposition']").setValue(job);
        // gender //true-male, false-female
        if (gender)
            $x(".//*[@id='gender']/tbody/tr/td[2]/div/div[2]").click();
        else
            $x(".//*[@id='gender']/tbody/tr/td[1]/div/div[2]").click();
        // comments - textarea
        $(By.id("dspurpose")).clear();
        $(By.id("dspurpose")).setValue("Just for test");
        // access right
        if (admin) {
            $x(".//*[@id='admin_content']/div/form/div[4]/div[2]/div/span").scrollTo().click();
        }
        // Organization
        // click on the list
        $(By.id("orgSelect_label")).scrollTo().click();

        Thread.sleep(TestContext.wait * 8);
        // click on given Organization
        $x(".//*[@id='orgSelect_panel']/div/ul/li[contains(., '" + Organization + "')]").click();
        Thread.sleep(TestContext.wait * 8);

        // Country
        $x(".//*[@id='country_label']").scrollTo().click();
        $x(".//*[@id='country_panel']/div/ul/li[7]").click();
        $x(".//*[@id='country_panel']/div/ul/li[7]").click();

        // Zip code
        $x(".//input[@id='zipCode']").clear();
        $x(".//input[@id='zipCode']").setValue("1111");
        // City
        $x(".//input[@id='city']").clear();
        $x(".//input[@id='city']").setValue("Budapest");
        // Street address - textarea
        $(By.id("streetAddr")).clear();
        $(By.id("streetAddr")).setValue(" Irinyi u. 1-17.");

        if ($(By.xpath(".//*[text()='" + TestContext.lang.getProperty("common.privacyPolicyTitle") + "']")).exists()) {
            WebElement privacyPolicyCheckbox = $(By.id("acceptPrivacyPolicy_input"));
            WebElement privacyPolicyCheckboxVisible = $(By.id("acceptPrivacyPolicy"));
            if (!privacyPolicyCheckbox.isSelected()) {
                privacyPolicyCheckboxVisible.click();
            }
        }

        // Click Create user
        $x("//button[child::span[text()='" + TestContext.lang.getProperty("admin.user.createUser") + "']]").click();

        Thread.sleep(TestContext.wait);

        // wait for the validator script (tests username, email etc.)
        // check for error message
        testFunctions.assertNoMessageOtherThan("facesMsg.user.createAccountSuccess");

//		Instant start = Instant.now();
//		while (Duration.between(start, Instant.now()).getSeconds() < 60)
//			if (Thread.currentThread().isInterrupted())
//				throw new RuntimeException("Interrupted.");

        String expectedView = "manageUserList.xhtml";
        Instant startOfWait = Instant.now();
        while (Duration.between(startOfWait, Instant.now()).getSeconds() < 10
                && !driver.getCurrentUrl().contains(expectedView))
            if (Thread.currentThread().isInterrupted())
                throw new RuntimeException("Interrupted.");
        String expectedPageTitle = TestContext.lang.getProperty("admin.user.manageList");
        String actualPageTitle = driver.getTitle();
        String actualURL = driver.getCurrentUrl();
        if (!expectedPageTitle.equals(expectedPageTitle))
            Assert.fail(String.format("Unexpected page title '%s', maybe we're in the wrong view '%s'? (expected title: '%s', expected view: '%s')", actualPageTitle, actualURL, expectedPageTitle, expectedView));

        $x(".//*[@id='tableForm']").shouldBe(visible);

        // check the url, it should be manageUserList.xhtml
//		if ( !driver.getCurrentUrl().contains( "manageUserList.xhtml" ) )
//			org.testng.Assert.fail( "After adding a new user, it doesn't go to manageUserList.xhtml but goes to "
//					+ driver.getCurrentUrl() );

        log.trace("User " + user + " added succesfull");
    }

}
