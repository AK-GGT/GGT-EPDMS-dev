package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.testng.ScreenShooter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iai.ilcd.model.security.IUser;
import de.iai.ilcd.security.jwt.JWTController;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import eu.europa.ec.jrc.lca.commons.security.encryption.FileKeyLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import java.util.zip.CRC32;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

/**
 * Testing interaction with Node using Bearer Token. This replaced deprecated T110AuthTokenTest
 */
@Listeners({ScreenShooter.class})
@Test
public class T150JWTTokenTest extends AbstractUITest {

    protected final static Logger log = LogManager.getLogger(T150JWTTokenTest.class);
    private final static String ADMIN_USERNAME = "admin";
    private final static String ADMIN_PASSWORD = "default";
    // hardcoded in soda4LCA.properties
    private final static long TOKEN_TTL = 7884000L; // 3 months in seconds
    RestTemplate restTemplate;
    private JWTController jwtController, faultyJWTController;
    //TODO: proper page object pattern
    @FindBy(className = "ui-selectonemenu-label")
    private WebElement wSelectedDataStock;

    @FindBy(xpath = ".//*[@data-label='RootStock1']")
    private WebElement wRootStock1;

    /**
     * Generate a fresh Token for admin
     *
     * @return request entity with a token in the body
     */
    protected static ResponseEntity<String> _requestFrestJWT() {
        return _requestFrestJWT(ADMIN_USERNAME, ADMIN_PASSWORD, 0);
    }

    /**
     * Generate a fresh Token for a given user
     *
     * @param userName username of user as defined in the dbunit xmls (ex: admin)
     * @param password password of user as defined in the dbunit xmls (ex: default)
     * @return request entity with a token in the body
     */
    private static ResponseEntity<String> _requestFrestJWT(String userName, String password, long issueTime) {
        return _requestFrestJWT(TestContext.PRIMARY_SITE_URL, userName, password, issueTime);
    }

    private static ResponseEntity<String> _requestFrestJWT(String siteURL, String userName, String password, long issueTime) {
        var responseEntity = (new RestTemplate()).exchange(
                siteURL + "resource/authenticate/getToken?userName={USERNAME}&password={PASSWORD}&issueTime={issueTime}",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class,
                userName, password, issueTime);
        Assert.assertNotNull(responseEntity.getBody(), "Cannot generate a token from /getToken endpoint");
        return responseEntity;
    }

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return List.of(
                List.of("DB_post_T015ImportExportTest.xml")
        );
    }

    /**
     * Sets up the test by logging in and creating a network client.
     */
    @BeforeClass
    public void setup() throws Exception {
        super.setup(); // DBUnit stuff
        log.info("Begin of AuthToken test");
        driver.manage().deleteAllCookies();
        // open the main site
        driver.manage().deleteAllCookies();
        driver.get(TestContext.PRIMARY_SITE_URL);
        log.debug("Trying to login as admin");
        // login as admin
        testFunctions.login(ADMIN_USERNAME, ADMIN_PASSWORD, true, true);

        // click on Admin area
        testFunctions.gotoAdminArea();
        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='admin_footer']")));

//			client = testFunctions.createClient();
        restTemplate = new RestTemplate();

        // see cargo setting in the pom
        var keylocation = System.getProperty("user.home");
        log.info("Autowire JWTController where key path is set to: " + keylocation);
//        jwtController = ApplicationContextHolder.getApplicationContext().getBean(JWTController.class);


        jwtController = new JWTController(new FileKeyLocation(keylocation));

        // expected to always throw exceptions when trying to parse any token
        // by pointing to a location that doesn't contain any public key
        faultyJWTController = new JWTController(new FileKeyLocation("nirvana")); // the band not the state of tranquility

        PageFactory.initElements(driver, this);
    }

    /**
     * Generate a couple of tokens with a small time difference between them
     *
     * @return 2d-array of object, first dimension size is number of invocations and the second dimension size
     * is the number of arguments that target method accept
     */
    @DataProvider(name = "API Keys")
    private Object[][] _api_keys() {

        var token1 = _requestFrestJWT().getBody();

        try {
            Thread.sleep(1000L); // wait 1 second
        } catch (InterruptedException e) {
            log.error("Cannot wait for the second token");
            e.printStackTrace();
        }
        var token2 = _requestFrestJWT().getBody();

        return new Object[][]{
                new Object[]{token1},
                new Object[]{token2},
        };
    }

    /**
     * Generate a token using the /resources/authenticate/getToken/
     *
     * <ol>
     *     <li>response status code is 200 OK</li>
     *     <li>sub keys has the correct admin username as value</li>
     *     <li>throw an AuthenticationException if zero public keys verify signature</li>
     * </ol>
     *
     * @see de.iai.ilcd.rest.AuthenticateResource
     */
    @Test(priority = 101,
            enabled = true,
            groups = {"JWTAuth", "JWTCreate", "JWTAPI"},
            description = "Create a JWT using REST endpoints")
    public void createTokenFromAPI() {
        var response = _requestFrestJWT();
        var token = response.getBody();
        var claims = jwtController.parseClaimsJws(token); // also verifies the signature
        log.info("Got this token (in base64) from resource endpoint " + token);

        Assert.assertEquals(200,
                response.getStatusCodeValue(),
                "Status code of HTTP response after generating token is NOT (200 OK)");
        Assert.assertEquals(ADMIN_USERNAME, claims.getSubject(), "Wrong Subject on the token");
        Assert.assertThrows(AuthenticationException.class, () -> faultyJWTController.parseClaimsJws(token));
    }

    /**
     * Generate a token using the UI (/showUser.xhtml)
     *
     * <ol>
     *     <li>sub keys has the correct admin username as value</li>
     *     <li>throw an AuthenticationException if zero public keys verify signature</li>
     *     <li>expiry date on token match the TTL with in a margin of 10 seconds</li>
     * </ol>
     */
    @Test(priority = 102,
            enabled = true,
            groups = {"JWTAuth", "JWTCreate", "JWTUI"},
            description = "Create a JWT using UI")
    public void createTokenFromUI() {
        testFunctions.findAndWaitOnElement(wSelectedDataStock).click();
        testFunctions.findAndWaitOnElement(wRootStock1).click();

        testFunctions.waitUntilSiteLoaded();

        testFunctions.selectMenuItem("admin.user", "admin.user.manageList");
        testFunctions.findAndWaitOnElement(By.xpath("//a[contains(.,'admin')]")).click();
        var generateBtn = By.xpath("//span[.='" + TestContext.lang.getProperty("admin.user.apikey.generate") + "']");
        var generateElement = testFunctions.findAndWaitOnConsoleElement(generateBtn);
        generateElement.click();
        // Generating an API key is an AJAX call (no page refresh)
        // wait for AJAX request to complete
        // seleinum thinks the element has been changed after the AJAX call
        new WebDriverWait(driver, Duration.ofMillis(10000))
                .ignoring(StaleElementReferenceException.class)
                .until((ExpectedCondition<Boolean>) driver -> driver.findElement(By.id("tokenOutput"))
                        .getAttribute("value").length() != 0);
        var token = testFunctions.findAndWaitOnElement(By.id("tokenOutput")).getAttribute("value");

        log.info("Got this token (in base64) from UI: " + token);
        var claims = jwtController.parseClaimsJws(token);

        Assert.assertEquals(ADMIN_USERNAME, claims.getSubject(), "Wrong Subject on the token");
        Assert.assertThrows(AuthenticationException.class, () -> faultyJWTController.parseClaimsJws(token));

        Assert.assertTrue( // Expiry date on token with 100 seconds margin of error
                (System.currentTimeMillis() + (TOKEN_TTL * 1000) // TTL is in seconds
                        - claims.getExpiration().getTime())
                        < 100 * 1000, // ≡ 100 seconds
                "Expiry date on token doesn't match configured TTL");
    }


    @DataProvider(name = "Sample datasets with totalCount")
    private Object[][] sample_datasets_count() {
        var token = _requestFrestJWT().getBody();
        var headers = new HttpHeaders();
        assert token != null;
        headers.setBearerAuth(token);

        return new Object[][]{
                new Object[]{headers, "processes", 5},
                new Object[]{headers, "flows", 14},
                new Object[]{headers, "lciamethods", 5},
                new Object[]{headers, "sources", 6},
                new Object[]{headers, "flowproperties", 5},
                new Object[]{headers, "unitgroups", 16},
                new Object[]{headers, "contacts", 5},
                new Object[]{headers, "lifecyclemodels", 0},

        };
    }

    /**
     * List datasets using REST endpoints (as json) and compare "totalCount"
     *
     * @throws JsonProcessingException may occur in case "totalCount" key was not found
     */

    @Test(priority = 103,
            enabled = true,
            groups = {"JWTAuth", "JWTAPI", "JWTList"},
//            dependsOnGroups = {"JWTCreate"},
            description = "Use token to list process/flow/etc.",
            dataProvider = "Sample datasets with totalCount")
    public void listDatasetByToken(HttpHeaders headers, String datasets_resource_path, int expectedTotalCount) throws JsonProcessingException {
        var responseEntity = restTemplate.exchange(
                TestContext.PRIMARY_SITE_URL + "resource/{datasets}?format=json",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class,
                datasets_resource_path);

        Assert.assertEquals(200, responseEntity.getStatusCodeValue(),
                "Status code of HTTP response after listing processes is NOT (200 OK)");

        var mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(responseEntity.getBody());
        log.trace(() -> datasets_resource_path + ": " + responseEntity.getBody());
        Assert.assertEquals(json.get("totalCount").asInt(), expectedTotalCount,
                "Number of " + datasets_resource_path + " in JSON response is wrong");
    }


    @DataProvider(name = "sample datasets with valid authz header and checksum")
    private Object[][] _sample_datasets() {
        var headers = new HttpHeaders();
        var token = _requestFrestJWT().getBody();
        assert token != null;
        headers.setBearerAuth(token);

        return new Object[][]{
                new Object[]{headers, "processes", "4a1ebe7c-6835-4a22-8b2e-3201f1cd32e8", 3927714700L},
                new Object[]{headers, "flows", "82b33e71-bfaa-49b4-9627-ee5963433f6e", 155406544L},
                new Object[]{headers, "lciamethods", "992c8e8d-769a-4930-9b0f-4fa323250738", 549957849L},
                new Object[]{headers, "sources", "7983f4c6-a355-4250-aaa8-5780a72cc1df", 1819043042L},
                new Object[]{headers, "flowproperties", "93a60a56-a3c8-14da-a746-0800200c9a66", 1247685893L},
                new Object[]{headers, "unitgroups", "93a60a57-a3c8-18da-a746-0800200c9a66", 3834145414L},
                new Object[]{headers, "contacts", "4b712d8f-9bdc-4aef-8ee5-455166d97637", 1906564700L},

        };
    }

    @Test(priority = 104,
            enabled = true,
            groups = {"JWTAuth", "JWTAPI", "JWTExport"},
//            dependsOnGroups = {"JWTList"},
            description = "Use token to download process/flow/etc.",
            dataProvider = "sample datasets with valid authz header and checksum")
    public void exportDatasetsByToken(HttpHeaders headers, String dataset_resource_path, String uuid, long correct_checksum) {

        log.info(() -> "Downloading from " + dataset_resource_path + " with uuid: " + uuid + " using a token");

        var responseEntity = restTemplate.exchange(
                TestContext.PRIMARY_SITE_URL + "resource/{dataset_resource}/{uuid}?format=xml",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class,
                dataset_resource_path, uuid);

        var ds = responseEntity.getBody();
        var crc32 = new CRC32();

        Assert.assertNotNull(ds, "Got an empty/null HTTP response on exporting dataset: " + uuid);
        crc32.update(ds.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(crc32.getValue(), correct_checksum,
                "Content of dataset " + uuid + " doesn't EXACTLY match what is expected. (Maybe some whitespaces got sneaked in the xml file?)");
        Assert.assertEquals(200, responseEntity.getStatusCodeValue(),
                "Status code of HTTP response after downloading" + dataset_resource_path + "is NOT (200 OK)");

    }

    @Test(priority = 105,
            enabled = true,
            groups = {"JWTAuth", "JWTAPI", "JWTImport"},
//            dependsOnGroups = {"JWTDownload", "JWTList"},
            description = "Use token to import ZIP")
    public void importZipByToken() throws IOException, InterruptedException {

        var token = _requestFrestJWT().getBody();

        var zipFileResource = this.getClass().getResource("/sample_epd_process_with_dependencies.zip");
        assert zipFileResource != null;
        InputStream zipFile = zipFileResource.openStream();

        var headers = new HttpHeaders();
        assert token != null;
        headers.setBearerAuth(token);
        listDatasetByToken(headers, "processes", 5);


        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new InputStreamResource(zipFile));
        body.add("zipFileName", "extra_datasets.zip");
        body.add("stock", "39be0b9b-683b-4bb8-b1f2-32320cb2cbfe"); // uuid of default datastock

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        var responseEntity = restTemplate.postForEntity(TestContext.PRIMARY_SITE_URL + "resource/processes/", entity, String.class);
        Assert.assertEquals(responseEntity.getStatusCodeValue(), 202, "Importing a zip should return a (202 Accepted)");
        Thread.sleep(5000L); // TODO: reduce wait
        listDatasetByToken(headers, "processes", 6);
    }

    /**
     * User 6 doesn't have permission to view "SimpleStock1" datastock
     */
    @Test(priority = 106,
            enabled = true,
            groups = {"JWTAuth", "JWTAPI", "JWTImport"},
//            dependsOnGroups = {"JWTDownload"},
            description = "Try to access datastock without having proper permission on token")
    public void attemptViolateDatastockPermission() {
        log.info("User 6 is trying to list SimpleStock1 without sufficient permission");
        var token = _requestFrestJWT("User6", "s3cr3t", 0).getBody();
        Assert.assertNotNull(token, "Failed to generate a token for User 6 (Alexander)");
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);

        Supplier<ResponseEntity<String>> responseLambda = () -> restTemplate.exchange(
                TestContext.PRIMARY_SITE_URL + "resource/datastocks/{SimpleStock1UUID}?format=xml",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class,
                "1e92f562-09c1-4828-8eea-eb7428a34fa");

        Assert.assertThrows(HttpClientErrorException.Unauthorized.class, responseLambda::get);
        log.info("User 6 couldn't to list SimpleStock1 (as intended)");
    }

    /**
     * Use internal {@link JWTController#generateToken(IUser, long)} to issue a token from the year 2000
     */
    @Test(priority = 107,
            enabled = true,
            groups = {"JWTAuth", "JWTAPI", "JWTImport"},
//            dependsOnGroups = {"JWTDownload"},
            description = "Use an expired token to view datasets")
    public void useExpiredToken() {
        var ancient_token = _requestFrestJWT("User6", "s3cr3t", 946684861L * 1000).getBody(); // 22-year-old token
        var headers = new HttpHeaders();
        assert ancient_token != null;
        headers.setBearerAuth(ancient_token);
        Supplier<ResponseEntity<String>> responseLambda = () -> restTemplate.exchange(
                TestContext.PRIMARY_SITE_URL + "resource/{datasets}?format=json",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class,
                "processes");

        log.info("Using an expired token to list datasets should result in a (403 Forbidden)");
        Assert.assertThrows(HttpClientErrorException.Forbidden.class, responseLambda::get);
        log.info("Expired token couldn't list datasets (as intended)");
    }

    /**
     * Generate an admin token from the PRIMARY testing node and use it against the SECONDARY testing node
     */
    @Test(priority = 108,
            enabled = true,
            groups = {"JWTAuth", "JWTAPI", "JWTImport"},
//            dependsOnGroups = {"JWTDownload"},
            description = "Use a token signed by someone else")
    public void invalidPublicKey() {
        var token = _requestFrestJWT(TestContext.PRIMARY_SITE_URL, ADMIN_USERNAME, ADMIN_PASSWORD, 0).getBody();
        var headers = new HttpHeaders();
        assert token != null;
        headers.setBearerAuth(token);

        Supplier<ResponseEntity<String>> responseLambda = () -> restTemplate.exchange(
                TestContext.SECONDARY_SITE_URL + "resource/datastocks/{SimpleStock1UUID}?format=xml",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class,
                "1e92f562-09c1-4828-8eea-eb7428a34fa");

        Assert.assertThrows(HttpClientErrorException.Unauthorized.class, responseLambda::get);
        log.info("Token from the secondary node cannot be used against primary node (as intended)");
    }

}
