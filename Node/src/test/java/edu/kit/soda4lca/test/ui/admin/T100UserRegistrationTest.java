package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.testng.ScreenShooter;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Selenide.open;

@Listeners({ScreenShooter.class})
public class T100UserRegistrationTest extends AbstractUITest {

    public static final int MANAGE_USERS_COLUMN_INDEX_FIRST_ADDITIONAL_TERMS = 13;
    // initializing the log
    protected final static Logger log = LogManager.getLogger(T100UserRegistrationTest.class);
    private static final String siteName = "@APP-TITLE@";
    int mailCount = 0;
    private GreenMail mailServer;

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return List.of(List.of("DB_pre_T012UsersTest.xml"));
    }

    @AfterClass
    public void tearDownMailServer() {
        mailServer.stop();
    }

    /**
     *
     */
    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        ServerSetup setup = new ServerSetup(3025, "localhost", "smtp");
        mailServer = new GreenMail(setup);
        mailServer.start();
        log.info("Begin of UserRegistration test");
        WebDriverRunner.setWebDriver(driver);
        WebDriverRunner.clearBrowserCache();
        open(TestContext.PRIMARY_SITE_URL);
        Thread.sleep(TestContext.wait);
    }

    /**
     *
     */
    @Test(priority = 101)
    public void testRequired() throws InterruptedException, MessagingException, IOException {
        log.info("Begin to test required fields.");
        checkRequiredFields("user", "user@user.user", false, true, true);
        checkRequiredFields(null, "user@user.user", false, true, true);
        checkRequiredFields("user2", null, false, true, true);
        // picked up by client frontend's HTML5's type=email
        // checkRequiredFields("user3", "qwerty", false, false, true);
        checkRequiredFields("user", "user@user.user", true, true, true);
        checkRequiredFields("user5", "user@user.user", false, true, false);
        log.info("Finished testing required fields.");
    }

    /**
     *
     */
    @Test(priority = 102)
    public void testAllFields() throws InterruptedException, MessagingException, IOException {

        // Navigate to Registration form/view
        String name = "t€st üser";
        log.info("Begin to test all registration fields");
        log.debug("Going to 'Registration' page.");
        if (testFunctions.isElementNotPresent(By.linkText(TestContext.lang.getProperty("public.user.logout")))) {
            testFunctions.logout();
        }
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("public.user.register"))).click();

        // Register as John Doe
        log.debug("Filling in form as John Doe.");
        testFunctions.fillInputText("nameIn", name);
        testFunctions.fillInputText("email", "user@user.user");

        testFunctions.fillInputText("title", "PhD");
        testFunctions.fillInputText("firstName", "John");
        testFunctions.fillInputText("lastName", "Doe");
        testFunctions.findAndWaitOnElement(By.xpath(".//table[@id='gender']/tbody/tr[1]/td[2]/div")).click();
        testFunctions.fillInputText("jobposition", "chief");
        testFunctions.fillInputText("institution", "institute of sciences");
        testFunctions.fillInputText("phone", "+335557962375");
        testFunctions.findAndWaitOnElement(By.xpath(".//textarea[@id='dspurpose']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//textarea[@id='dspurpose']")).sendKeys("for data mining");

        testFunctions.selectItemInSelectBox("country", "Faroe Islands");
        testFunctions.fillInputText("zipCode", "96532");
        testFunctions.fillInputText("city", "London");
        testFunctions.findAndWaitOnElement(By.xpath(".//textarea[@id='streetAddr']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//textarea[@id='streetAddr']")).sendKeys("Baker Street 221b");

        // Check all additional terms
        int addTerms = driver
                .findElements(By.xpath(".//div[contains(@id,'additionalTerms') and contains(@id, 'selectBox')]")).size();

        for (int i = 0; i < addTerms; i++) {
            testFunctions.findAndWaitOnElement(By.xpath(".//div[@id='additionalTerms:" + i + ":selectBox']")).click();
        }

        // Accept privacy policy
        if (testFunctions
                .isElementNotPresent(By.xpath(".//*[@id='privacyPolicy']/tbody/tr/td[3]/label/span[contains(.,'*')]"))) {

            testFunctions.findAndWaitOnElement(By.id("acceptPrivacyPolicy")).click();
        }

        // save/register
        testFunctions.clickButtonWithI18nLabel("public.user.register");
        Thread.sleep(TestContext.wait);


        // Check confirmation message
        if (!(testFunctions.findAndWaitOnElement(By.xpath(".//h1")).getText()
                .equals(TestContext.lang.getProperty("public.user.registrated.msg")))) {
            org.testng.Assert.fail("Web page for showing successfull registration is not shown.");
        }

        checkMailLogin(name, driver, ++mailCount);

        List<Integer> addTermsList = new ArrayList<>();
        for (int i = 1; i <= addTerms; i++) {
            addTermsList.add(i);
        }
        checkExistsAndAddTerms(name, true, addTermsList);

        log.info("End of testing all fields");

    }

    /**
     * Tests deletion of several PusTarget entries.
     */
    @Test(priority = 103)
    public void testAddTerms() throws InterruptedException, MessagingException, IOException {
        log.info("Begin of testing additional terms.");
        checkAddTerms("firstUser", "email@email.com", List.of(1));
        checkAddTerms("secondUser", "email@email.com", List.of(2));
        checkAddTerms("thirdUser", "email@email.com", Arrays.asList(1, 2));
        checkAddTerms("fourthUser", "email@email.com", List.of());
        log.info("Finished testing additional terms.");
    }

    /**
     *
     */
    public void checkRequiredFields(String name, String email, boolean userAlreadyExisting, boolean emailCorrect,
                                    boolean privacyPolicyAccepted) throws InterruptedException, MessagingException, IOException {
        if (testFunctions.isElementNotPresent(By.linkText(TestContext.lang.getProperty("public.user.logout")))) {
            testFunctions.logout();
        }
        log.debug("Going to 'Registration' page.");
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("public.user.register"))).click();
        log.debug("Filling in form.");
        testFunctions.fillInputText("nameIn", name);
        testFunctions.fillInputText("email", email);

        if (privacyPolicyAccepted) {
            if (testFunctions
                    .isElementNotPresent(By.xpath(".//*[@id='privacyPolicy']/tbody/tr/td[3]/label/span[contains(.,'*')]"))) {
                testFunctions.findAndWaitOnElement(By.id("acceptPrivacyPolicy")).click();
            }
        }

        boolean privacyPolicyRequired = testFunctions
                .isElementNotPresent(By.xpath(".//*[@id='privacyPolicy']/tbody/tr/td[3]/label/span[contains(.,'*')]"));

        if (log.isDebugEnabled()) {
            log.debug("privacy policy required: " + privacyPolicyRequired);
        }

        log.debug("Trying to register new user.");
        testFunctions.clickButtonWithI18nLabel("public.user.register");
        Thread.sleep(TestContext.wait);

        if (name == null || name.isEmpty() || email == null || email.isEmpty() || !emailCorrect || userAlreadyExisting
                || (!privacyPolicyAccepted && privacyPolicyRequired)) {
            if ((name == null || name.isEmpty()) && !testFunctions.isMessageShown("admin.user.requiredMsg.loginName")) {
                org.testng.Assert.fail("name is empty but not corresponding error message is shown.");
            }
            if ((email == null || email.isEmpty()) && !testFunctions.isMessageShown("admin.user.requiredMsg.email")) {
                org.testng.Assert.fail("e-mail address is empty but no corresponding error message is shown.");
            }
            if (!emailCorrect && !testFunctions.isMessageShown("admin.user.validatorMsg.email")) {
                org.testng.Assert.fail("email address has not correct format but no erroro message is shown.");
            }

            if (userAlreadyExisting && !testFunctions.isMessageShown("facesMsg.user.alreadyExists"))
                if (!privacyPolicyAccepted && !testFunctions.isMessageShown("admin.user.acceptPrivacyPolicyRequired")) {
                    org.testng.Assert
                            .fail("Privacy policy was not accepted but no corresponding error message is shown.");
                }

        } else {

            if (!(testFunctions.findAndWaitOnElement(By.xpath(".//h1")).getText()
                    .equals(TestContext.lang.getProperty("public.user.registrated.msg"))))
                org.testng.Assert.fail("Web page for showing successfull registration is not shown.");

            checkMailLogin(name, driver, ++mailCount);

        }

        testFunctions.login("admin", "default", true, true);

        testFunctions.gotoAdminArea();

        testFunctions.goToPageByAdminMenu("admin.user", "admin.user.manageList");

        if (name != null && !name.isEmpty()) {
            boolean exists = !(email == null || email.isEmpty() || !emailCorrect || (!privacyPolicyAccepted && privacyPolicyRequired));
            checkExistsAndAddTerms(name, exists, null);
        }

        log.info("End of fill in required fields.");
    }

    /**
     *
     */
    public void checkAddTerms(String name, String email, List<Integer> addTerms)
            throws InterruptedException, MessagingException, IOException {
        if (testFunctions.isElementNotPresent(By.linkText(TestContext.lang.getProperty("public.user.logout")))) {
            testFunctions.logout();
        }
        log.debug("Going to 'Registration' page.");
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("public.user.register"))).click();
        log.debug("Filling in form.");
        testFunctions.fillInputText("nameIn", name);
        testFunctions.fillInputText("email", email);

        for (Integer addTerm : addTerms) {
            int position = addTerm - 1;
            testFunctions.findAndWaitOnElement(By.xpath(".//div[@id='additionalTerms:" + position + ":selectBox']")).click();
        }

        if (testFunctions
                .isElementNotPresent(By.xpath(".//*[@id='privacyPolicy']/tbody/tr/td[3]/label/span[contains(.,'*')]"))) {
            testFunctions.findAndWaitOnElement(By.id("acceptPrivacyPolicy")).click();
        }

        log.debug("Trying to login to target page for showing available target stocks.");
        testFunctions.clickButtonWithI18nLabel("public.user.register");
        Thread.sleep(TestContext.wait);

        // Check mail login
        checkMailLogin(name, driver, ++mailCount);

        checkExistsAndAddTerms(name, true, addTerms);

        log.info("End of checking additional terms.");

    }

    /**
     * Checks registration mail which was sent to registered user and checks
     * whether user can login with given credentials-
     *
     * @param name   The name of registered user
     * @param driver The WebDriver managing to navigate to given web page
     * @param count  The total count of messages in email box
     */
    private void checkMailLogin(String name, WebDriver driver, int count)
            throws MessagingException, IOException {
        MimeMessage[] messages = mailServer.getReceivedMessages();
        log.debug("Beginning to check sent mail.");

        if (messages.length != count) {
            org.testng.Assert.fail("E-mail server should get only " + count + " mail, but didn't (Got "
                    + messages.length + " mails).");
        }

        if (log.isDebugEnabled()) {
            log.debug("count " + count);
        }

        if (!messages[count - 1].getSubject().equals("User registration at " + siteName)) {
            org.testng.Assert.fail("Subject should be 'User registration at " + siteName + "', but is '"
                    + messages[0].getSubject() + "'.");
        }

        if (log.isDebugEnabled()) {
            log.debug("user name in mail: '" + testFunctions.extractUserFomMail(messages[count - 1])
                    + "', actual user name: '" + name + "'");
        }
        if (!testFunctions.extractUserFomMail(messages[count - 1]).equals(name)) {
            org.testng.Assert.fail("In e-mail is wrong login name shown.");
        }

        String passw = testFunctions.extractPasswordFomMail(messages[count - 1]);
        String link = testFunctions.extractLinkFomMail(messages[count - 1]);
        String newPassw = "S3cr3tPassword";

        driver.get(link);

        if (!testFunctions.isMessageShown("facesMsg.activation.accountActivated")) {
            org.testng.Assert.fail(
                    "The registrated account could not be activated! Error message is: " + testFunctions.getMessage());
        }

        log.debug("Trying to login with credentials given from registration mail.");
        testFunctions.login(name, passw, true, true);

        if (!testFunctions.isElementNotPresent(By.xpath(".//*[@id='leftFooter']/a[3]"))) {
            org.testng.Assert.fail("user was not correctly logged in!");
        }

    }

    /**
     * Checks whether user with given name exists and whether corresponding
     * additional terms are checked.
     *
     * @param name     The name of registered user.
     * @param exists   The flag indicating whether user was registered or not
     * @param addTerms The list of numbers which indicate which additional term was
     *                 checked
     */
    private void checkExistsAndAddTerms(String name, boolean exists, List<Integer> addTerms) {
        testFunctions.login("admin", "default", true, true);

        testFunctions.gotoAdminArea();

        testFunctions.goToPageByAdminMenu("admin.user", "admin.user.manageList");
        log.debug(
                "Checkiung whether user with given name exists in user list and whether correct additional terms are checked.");
        if (exists) {
            if (!testFunctions.isElementNotPresent(By.linkText(name))) {
                org.testng.Assert.fail("User " + name + " should be registred but seems to be not.");
            }

            if (addTerms != null) {

                for (Integer addTerm : addTerms) {
                    if (!testFunctions.isElementNotPresent(By.xpath(".//*[text()='" + name + "']/../../td["
                            + (MANAGE_USERS_COLUMN_INDEX_FIRST_ADDITIONAL_TERMS + addTerm) + "]/div/span[@class='fa fa-check']"))) {
                        org.testng.Assert.fail("AdditionalTerm no. " + addTerm + " for user " + name
                                + " should be checked but is not!");
                    }
                }
            }

        } else {
            if (testFunctions.isElementNotPresent(By.linkText(name))) {
                org.testng.Assert.fail("User " + name + " should not be registred but seems to be.");
            }
        }

    }

}
