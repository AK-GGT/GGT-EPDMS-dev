package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.testng.ScreenShooter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.fzk.iai.ilcd.service.client.ILCDServiceClientException;
import de.fzk.iai.ilcd.service.client.impl.ILCDNetworkClient;
import de.fzk.iai.ilcd.service.client.impl.vo.DataStockList;
import de.fzk.iai.ilcd.service.client.impl.vo.DataStockVO;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Selenide.$x;

/**
 * This test class tests the functionality of the auth token (a. k. a. API key).
 *
 * @author sarai
 */
@Listeners({ScreenShooter.class})
@Deprecated
public class T110AuthTokenTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T110AuthTokenTest.class);
    private static final String PASSWORD = "s3cr3t";

    private static final String USER_NAME = "User3";
    /*
     * The UUID of a data stock that is only visible to authenticated users
     */
    private static final String HIDDEN_STOCK = "9e2c6d39-daee-49d0-9d38-783100116aac";
    private static final String HEADER_NAME = "Authorization";
    /**
     * This value (waiting time for token expiration) must be set higher than value of 'feature.api.auth.token.ttl'
     * in soda4LCA.properties which is set to 10 seconds.
     */
    private static final int WAIT_FOR_TOKEN_TO_EXPIRE = 12000;
    RestTemplate restTemplate;

    /*
     * The network client to connect to the REST API.
     */
    WebDriver driver;

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T015ImportExportTest.xml"));
    }

    /**
     * Sets up the test by logging in and creating a network client.
     */
    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        log.info("Begin of AuthToken test");
        driver.manage().deleteAllCookies();
        driver = driver;
        // open the main site
        driver.manage().deleteAllCookies();
        driver.get(TestContext.PRIMARY_SITE_URL);
        log.debug("Trying to login");
        // login as admin
        testFunctions.login("admin", "default", true, true);

        // click on Admin area
        testFunctions.gotoAdminArea();
        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='admin_footer']")));

//			client = testFunctions.createClient();
        restTemplate = new RestTemplate();
    }

    /**
     * Tests whether user can generate an APPI key and authenticate with given API key via GET requests to REST API.
     *
     * @throws InterruptedException
     * @throws MessagingException
     * @throws IOException
     * @throws ILCDServiceClientException
     */
    @Test(priority = 101)
    public void testAllowedUser() throws InterruptedException, MessagingException, IOException, ILCDServiceClientException {
        log.info("Test user with valid API key.");
        String APIKey = changeUser(USER_NAME, true, true);
        Thread.sleep(10 * TestContext.wait);
        requestDataStockWithAPIKey(APIKey, 200, true);

        checkPublicDataStockRequest();
        log.info("Let API key expire.");
        Thread.sleep(WAIT_FOR_TOKEN_TO_EXPIRE);
        log.info("Testing API key after expiration");
        requestDataStockWithAPIKey(APIKey, 403, false);
        Thread.sleep(10 * TestContext.wait);
    }

    /**
     * Tests whether user cannot authenticate when API key use/generation disallowed.
     *
     * @throws InterruptedException
     * @throws MessagingException
     * @throws IOException
     */
    @Test(priority = 102)
    public void testAllowedUserLaterNot() throws InterruptedException, MessagingException, IOException {
        log.info("Generating an API key.");
        String APIKey = changeUser(USER_NAME, true, true);
        Thread.sleep(12 * TestContext.wait);
        requestDataStockWithAPIKey(APIKey, 200, true);
        Thread.sleep(5 * TestContext.wait);
        log.info("Disallow user to use API key.");
        changeUser(USER_NAME, false, false);
        Thread.sleep(10 * TestContext.wait);

        requestDataStockWithAPIKey(APIKey, 403, false);
        Thread.sleep(10 * TestContext.wait);
    }

    /**
     * Tests whether user can authenticate successfully with a first API key after they had generated twice an API key.
     *
     * @throws InterruptedException
     * @throws MessagingException
     * @throws IOException
     */
//		@Test(priority = 103)
    public void testAPIKeyAfterNotTracked() throws InterruptedException, MessagingException, IOException {
        log.info("Generating two API keys with same user and checking whether" +
                " the first API key can be used for authentication");
        String APIKey = changeUser(USER_NAME, true, true);
        Thread.sleep(10 * TestContext.wait);
        changeUser(USER_NAME, true, true);
        Thread.sleep(10 * TestContext.wait);
        requestDataStockWithAPIKey(APIKey, 403, false);
        Thread.sleep(10 * TestContext.wait);
    }

    /**
     * Tests whether user can generate an API key and authenticate with given API key via GET requests to REST API.
     *
     * @throws InterruptedException
     * @throws MessagingException
     * @throws IOException
     * @throws ILCDServiceClientException
     */
    @Test(priority = 104)
    public void testAllowedUserGET() throws InterruptedException, MessagingException, IOException, ILCDServiceClientException {
        log.info("Test user with valid API key who generates API keys via GET requests on REST API.");
        changeUser(USER_NAME, true, false);
        Thread.sleep(10 * TestContext.wait);
        log.debug("Testing whether a valid user can generate an API key");
        String APIKey = testFunctions.generateAPIKeyGET(USER_NAME, PASSWORD, 200);
        Thread.sleep(10 * TestContext.wait);
        requestDataStockWithAPIKey(APIKey, 200, true);

        checkPublicDataStockRequest();

        log.debug("Let API key expire.");
        Thread.sleep(WAIT_FOR_TOKEN_TO_EXPIRE);
        log.debug("Testing API key after expiration");

        requestDataStockWithAPIKey(APIKey, 403, false);
        Thread.sleep(10 * TestContext.wait);
    }

    /**
     * Tests whether user can authenticate via GET request to REST API when user is later not allowed to use an API key.
     *
     * @throws InterruptedException
     * @throws MessagingException
     * @throws IOException
     */
    @Test(priority = 105)
    public void testAllowedUserLaterNotGET() throws InterruptedException, MessagingException, IOException {
        log.info("Generating an API key via GET request.");
        changeUser(USER_NAME, true, false);
        String APIKey = testFunctions.generateAPIKeyGET(USER_NAME, PASSWORD, 200);
        Thread.sleep(10 * TestContext.wait);
        requestDataStockWithAPIKey(APIKey, 200, true);

        log.info("Disallow user to use API key.");
        changeUser(USER_NAME, false, false);
        Thread.sleep(10 * TestContext.wait);

        requestDataStockWithAPIKey(APIKey, 403, false);
        Thread.sleep(10 * TestContext.wait);
        log.info("Checking whether user with no API key allowence can generate API key on their own.");
        testFunctions.generateAPIKeyGET(USER_NAME, PASSWORD, 401);
        Thread.sleep(10 * TestContext.wait);
    }

    /**
     * tests whether user can generate twice an API key and authenticate successfully with first (invalid) API key.
     *
     * @throws InterruptedException
     * @throws MessagingException
     * @throws IOException
     */
//		@Test(priority = 106)
    public void testAPIKeyAfterNotTrackedGET() throws InterruptedException, MessagingException, IOException {
        log.info("Generating two API keys via GET request with same user and checking whether" +
                " the first API key can be used for authentication");
        try {
            changeUser(USER_NAME, true, true);
            Thread.sleep(10 * TestContext.wait);
            String APIKey = testFunctions.generateAPIKeyGET(USER_NAME, PASSWORD, 200);
            Thread.sleep(10 * TestContext.wait);
            // This API key will not be recorded by user
            testFunctions.generateAPIKeyGET(USER_NAME, PASSWORD, 200);
            Thread.sleep(10 * TestContext.wait);
            requestDataStockWithAPIKey(APIKey, 403, false);
            Thread.sleep(10 * TestContext.wait);
        } catch (AssertionError ae) {
            log.info("trying a second time");
            changeUser(USER_NAME, true, true);
            Thread.sleep(10 * TestContext.wait);
            String APIKey = testFunctions.generateAPIKeyGET(USER_NAME, PASSWORD, 200);
            Thread.sleep(10 * TestContext.wait);
            // This API key will not be recorded by user
            testFunctions.generateAPIKeyGET(USER_NAME, PASSWORD, 200);
            Thread.sleep(10 * TestContext.wait);
            requestDataStockWithAPIKey(APIKey, 403, false);
            Thread.sleep(10 * TestContext.wait);
        }
    }


    /**
     * Tests whether one can authenticate with validly generated API key if user is not allowed to use API key.
     *
     * @throws InterruptedException
     * @throws MessagingException
     * @throws IOException
     */
    @Test(priority = 107)
    public void testNotAllowedUser() throws InterruptedException, MessagingException, IOException {
        String APIKey = changeUser(USER_NAME, false, true);
        Thread.sleep(10 * TestContext.wait);
        requestDataStockWithAPIKey(APIKey, 403, false);
        Thread.sleep(10 * TestContext.wait);
        log.info("Testing whether user wo is not allowed to generate API keys can generate API key on its own.");
        testFunctions.generateAPIKeyGET(USER_NAME, PASSWORD, 401);
        Thread.sleep(10 * TestContext.wait);
    }


    /**
     * Tests the data base request with an invalid String as API key and tests the
     * API key generation request for invalid principal/credential combination
     *
     * @throws InterruptedException
     * @throws MessagingException
     * @throws IOException
     */
    @Test(priority = 108)
    public void testrandomAPIKey() throws InterruptedException, MessagingException, IOException {
        String randomName = "wrwherwofsj";
        String randomPassword = "smfdsffdfdlkassfhf";
        String randomAPIKey = "hgdhfgd.sjgfdhgf.fjgasrlfgalrfzalfgdfh";
        log.debug("Testing whether an invalid user can generate an API key");
        testFunctions.generateAPIKeyGET(randomName, randomPassword, 401);
        Thread.sleep(10 * TestContext.wait);
        requestDataStockWithAPIKey(randomAPIKey, 403, false);
        Thread.sleep(10 * TestContext.wait);
    }

    /**
     * Changes User entry with given name in web interface by toggling the flag for API key allowance
     * and generates an API key if API key shall be generated.
     *
     * @param name             the name of user to change
     * @param authTokenAllowed The flag indicating whether auth token shall be allowed when user change is saved
     * @param generateAPIKey   The flag indicating whether an API key shall be generated
     * @return An API key (as String) - if an API key shall be generated, else <code> null </code>
     * @throws InterruptedException
     */
    private String changeUser(String name, boolean authTokenAllowed, boolean generateAPIKey) throws InterruptedException {
        log.info("Going to user entry with name " + name + ", setting the auth token allowed option as wished.");

        String APIKey = null;
        testFunctions.goToPageByAdminMenu("admin.user", "admin.user.manageList");
        testFunctions.waitUntilSiteLoaded();
        $x(".//*[text()='" + name + "']").click();
        testFunctions.waitUntilSiteLoaded();

        if ($x(".//*[text()='" + TestContext.lang.getProperty("admin.user.apikey.allowed") + "']").exists()) {
            boolean expiryVisible = $x(".//*[contains(.,'" + TestContext.lang.getProperty("admin.user.apikey.expiry") + "')]").exists();
            if (log.isDebugEnabled()) {
                log.debug("expiration date visible: " + expiryVisible);
                log.debug("auth token allowed: " + authTokenAllowed);
            }
            // Checks whether flag is set when it should be and toggles checkbox if flag is not according to auth token allowence or generateAPIKey.
            // this used to be a while loop before for some reason
            if ((!(authTokenAllowed || generateAPIKey) && expiryVisible) || ((authTokenAllowed || generateAPIKey) && !expiryVisible)) {
                testFunctions.findAndWaitOnCheckBox(TestContext.lang.getProperty("admin.user.apikey.allowed")).click();
                log.debug("flag for API key allowed was changed.");
                testFunctions.waitUntilSiteLoaded();
                expiryVisible = testFunctions.isElementNotPresent(By.xpath(".//*[contains(.,'" + TestContext.lang.getProperty("admin.user.apikey.expiry") + "')]"));
                log.debug("expiryDate is now visible: " + expiryVisible);
                testFunctions.waitUntilSiteLoaded();
                Selenide.sleep(TestContext.wait * 2);
            }

            if (generateAPIKey) {
                expiryVisible = $x(".//*[contains(.,'" + TestContext.lang.getProperty("admin.user.apikey.expiry") + "')]").exists();
                if (!expiryVisible) {
                    org.testng.Assert.fail("Auth token should be enabled but seems not to be since expiration date output is not visible.");
                } else {
//						testFunctions.clickButtonWithI18nLabel("admin.user.apikey.generate");
                    testFunctions.clickButtonHidden("generateApiKeyOLD");
                    // Generating an API key is an AJAX call (no page refresh)
                    // wait for AJAX request to complete
                    // seleinum thinks the element has been changed after the AJAX call
                    new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofMillis(10000))
                            .ignoring(StaleElementReferenceException.class)
                            .until((ExpectedCondition<Boolean>) driver -> driver.findElement(By.id("tokenOutput"))
                                    .getAttribute("value").length() != 0);
                    APIKey = testFunctions.findAndWaitOnElement(By.id("tokenOutput")).getAttribute("value");
                    if (log.isTraceEnabled()) {
                        log.trace("API key is: " + APIKey);
                    }
                }

                log.info("Setting option tokenAllowed to false if auth token shall be not allowed and the tokenAllowed" +
                        "option is currently set.");
                if (log.isDebugEnabled()) {
                    log.debug("authTokenAllowed: " + authTokenAllowed);
                    log.debug("expiryVisible: " + expiryVisible);
                }
                while (!authTokenAllowed && expiryVisible) {
                    testFunctions.findAndWaitOnCheckBox(TestContext.lang.getProperty("admin.user.apikey.allowed")).click();
                    Thread.sleep(TestContext.wait * 2);
                    expiryVisible = testFunctions.isElementNotPresent(By.xpath(".//*[contains(.,'" + TestContext.lang.getProperty("admin.user.apikey.expiry") + "')]"));

                    if (log.isDebugEnabled()) {
                        log.debug("expiry is visible: " + expiryVisible);
                    }
                }

            }

            testFunctions.clickButtonWithI18nLabel("admin.user.changeUserInfo");
            Thread.sleep(TestContext.wait * 2);

            if (authTokenAllowed && !expiryVisible) {
                org.testng.Assert.fail("Auth token should be allowed but allowence seems not to be enabled");
            }

            if (!authTokenAllowed && expiryVisible) {
                org.testng.Assert.fail("Auth token should not be allowed but allowence seems not to be disabled");
            }
        } else {
            org.testng.Assert.fail("AuthToken/API key should be enabled but seems not to be.");
        }

        return APIKey;
    }

    /**
     * Requests the lists of data stocks from rest API Key with given API
     *
     * @param APIKey         The API key for authorization
     * @param expectedStatus The expected response status code
     * @param authenticated  The flag whether the user should be authenticated with given API key or not
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    private void requestDataStockWithAPIKey(String APIKey, int expectedStatus, boolean authenticated) throws JsonMappingException, JsonProcessingException {
        if (log.isDebugEnabled()) {
            log.debug("Testing whether GET request with API key " + APIKey + "returns expected status code " + expectedStatus + ".");
        }
        if (log.isTraceEnabled())
            log.trace("token is " + APIKey);

        int status = -1;
        ResponseEntity<DataStockList> response = null;
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_NAME, "Bearer " + APIKey);


        try {
            response = restTemplate.exchange(TestContext.PRIMARY_SITE_URL + "resource/datastocks", HttpMethod.GET,
                    new HttpEntity<>(headers), DataStockList.class);

            status = response.getStatusCodeValue();
        } catch (HttpClientErrorException e) {
            status = e.getRawStatusCode();
        }

        if (log.isDebugEnabled()) {
            log.debug("response status:" + status);
        }

        if (status != expectedStatus) {
            org.testng.Assert.fail("The expected status code after retrieving data stocks should be " + expectedStatus + " but is actually " + status + ".");
        }

        if (log.isDebugEnabled()) {
            if (authenticated) {
                log.debug("Checks whether hidden stock appears in returned stock list as expected.");
            } else {
                log.debug("Check whether hidden stock does not appear in returned stock list as expected. ");
            }
        }
        if (status == 200) {

            //Constructing the list of visible stocks out of response
            Object result = response.getBody();

            JAXBElement<DataStockList> e = (JAXBElement<DataStockList>) result;
            DataStockList list = e.getValue();


            boolean hiddenStockVisible = isHiddenStockVisible(list);

            if (authenticated && !hiddenStockVisible) {
                org.testng.Assert.fail(USER_NAME + " should be authenticated with API key but seems to be not since data stock with UUID " + HIDDEN_STOCK + " is not visible.");
            }

            if (!authenticated && hiddenStockVisible) {
                org.testng.Assert.fail(USER_NAME + " should be not authenticated with API key but seems to be since data stock with UUID " + HIDDEN_STOCK + " is visible.");
            }

        }
    }

    /**
     * Checks whether the restricted stock appears in a public request without authentication.
     *
     * @throws ILCDServiceClientException
     * @throws IOException
     */
    private void checkPublicDataStockRequest() throws ILCDServiceClientException, IOException {
        log.info("Checking whether the hidden stock appears in returned stock list after unauthenticated GET request.");
        ILCDNetworkClient client = new ILCDNetworkClient(TestContext.PRIMARY_SITE_URL + "resource/");
        DataStockList list = client.getDataStocks();
        boolean hiddenStockVisible = isHiddenStockVisible(list);

        if (hiddenStockVisible) {
            org.testng.Assert.fail("Guest should be not authenticated but seems to be since data stock with UUID " + HIDDEN_STOCK + " is visible.");
        }

    }

    /**
     * Checks whether stock (which is only visible for certain authorized users) is visible in given stock list.
     *
     * @param list The list to check whether wanted stock is contained in it
     * @return true if it contains the wanted restricted stock
     */
    private boolean isHiddenStockVisible(DataStockList list) {
        List<DataStockVO> dsList = list.getDataStocks();
        if (log.isDebugEnabled()) {
            log.debug("list size is: " + dsList.size());
        }

        boolean hiddenStockVisible = false;

        //Going through stock list and check whether stock UUID equals UUID of restricted stock
        for (DataStockVO dsVO : dsList) {
            if (log.isDebugEnabled()) {
                log.debug("short name of current stock is: " + dsVO.getShortName());
            }
            if (dsVO.getUuid().equals(HIDDEN_STOCK)) {
                hiddenStockVisible = true;
                continue;
            }
            log.debug("end of while loop.");

        }

        return hiddenStockVisible;
    }


}
