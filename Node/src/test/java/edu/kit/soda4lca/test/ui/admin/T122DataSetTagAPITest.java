package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import de.fzk.iai.ilcd.service.client.impl.vo.StringList;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple test to make sure the tagging endpoint
 * <p>
 * <code>resource/processes/{uuid}/tags</code>
 * </p>
 * works as expected.
 * <p>
 * There's an inner class storing expected results - partly initialised in the
 * <code>@BeforeClass</code> annotated setup. There's also a translator map that
 * "explains" test data to the log-readers.
 * <p>
 * So far we don't test the problem list, that gets handed back by the response.
 *
 * @author dev
 */
@Listeners({ScreenShooter.class})
public class T122DataSetTagAPITest extends AbstractUITest {

    protected static final Logger log = LogManager.getLogger(T122DataSetTagAPITest.class);
    private static final String ADMIN = "Admin1";
    private static final String USER_UNAUTHORISED = "User1";
    private static final String USER_AUTHORISED = "User7";
    private static final String ANONYMOUS = "anonymous";
    private static final String PASSWORD = "s3cr3t";
    private static final String VALID_TAG = "existingTag";
    private static final String INVALID_TAG = "nonexistingTag";
    private static final String VALID_UUID = "1a7da06d-e8b7-4ff1-920c-209e9009dbe0";
    private static final String INVALID_UUID = "INVALID-UUID-XXX-YYY-ZZZ-000";
    private static final String AUTHORISATION_HEADER = "Authorization";
    private static final String AUTHORISATION_TYPE = "Bearer";
    private static final List<String> originalTags = Stream.of("pre-existingTag").collect(Collectors.toList());
    /**
     * Map that explains our static final Strings to our (log-)readers.
     */
    private static final Map<String, String> babelFish = new HashMap<String, String>();
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    Client client;

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays
                .asList(Arrays.asList("DB_pre_T122DataSetTagAPITest_fragment.xml", "DB_post_T015ImportExportTest.xml"));
    }

    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        log.info("Starting to test API tag assignment functionality for data sets.");

        client = testFunctions.createClient();

        // Setting up expectations
        setExpectationsForGET();
        setExpectationsForPUT();
        setExpectationsForDELETE();
        setExpectationsForPUTWithCreate();
        setExpectationsForDELETEWithCreate();

        // Prepare map to translate test each setting for console readers.
        babelFish.put(ADMIN, "user with superadmin permissions");
        babelFish.put(USER_AUTHORISED, "user with tagging permission");
        babelFish.put(USER_UNAUTHORISED, "user without tagging permission");
        babelFish.put(ANONYMOUS, "unknown user");
        babelFish.put(VALID_TAG, "a valid tag parameter");
        babelFish.put(INVALID_TAG, "an invalid tag parameter");
        babelFish.put(VALID_UUID, "a valid uuid");
        babelFish.put(INVALID_UUID, "an invalid uuid");
    }

    @BeforeMethod
    public void startMessage() {
        log.info("And now..");
    }

    @AfterMethod
    public void successMessage() {
        log.info("Success: Everything works as expected.");
    }

    @Test(priority = 101)
    public void testGET() {
        log.info("... testing GET.");
        // Assemble test data
        List<String> testers = Stream.of(ADMIN, USER_AUTHORISED, USER_UNAUTHORISED, ANONYMOUS)
                .collect(Collectors.toList());
        Map<String, String> urls = Stream.of(VALID_UUID, INVALID_UUID)
                .collect(Collectors.toMap(uuid -> (String) uuid, uuid -> (String) generateURL(uuid)));

        // Do the testing
        for (String tester : testers) {
            for (String uuid : urls.keySet()) {
                log.info("Testing as " + babelFish.get(tester) + ", using " + babelFish.get(uuid) + ": GET "
                        + urls.get(uuid));
                Response response = request(urls.get(uuid), tester, GET);
                int status = assessStatus(response, Expectations.get(GET + uuid + tester));
                if (status == 200)
                    assessTagList(response, originalTags);
            }
        }
    }

    @Test(priority = 102)
    public void assignRemoveTestWithoutCreatingTags() {
        log.info("... testing PUT and DELETE with createTags=false.");

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.addAll(originalTags);
        expectedResult.add(VALID_TAG);

        assignRemoveTest(false, expectedResult);
    }

    @Test(priority = 103)
    public void assignRemoveTestCreatingTags() {
        log.info("testing PUT and DELETE with createTags=true.");

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.addAll(originalTags);
        expectedResult.add(VALID_TAG);
        expectedResult.add(INVALID_TAG);

        assignRemoveTest(true, expectedResult);
    }

    /**
     * Right now we're testing
     * <ol>
     * <li>tags: none, empty, duplicates</li>
     * <li>invalid version param</li>
     * <li>fishy bearer token</li>
     * </ol>
     */
    @Test(priority = 104)
    public void testRobustness() {
        log.info("... a confrontation with madness: Testing robustness..");

        List<String> tags;
        List<String> expectedResult;
        String url;
        Response response;

        // no tags
        tags = null;
        expectedResult = originalTags;

        log.info("Trying no Tags");

        url = generateURL(VALID_UUID, null, true);
        response = request(url, USER_AUTHORISED, PUT);
        assessStatus(response, Expectations.NO_TAGS);
        compareTagListOnProcessWith(expectedResult);

        // empty tags
        tags = Stream.of("", "").collect(Collectors.toList());
        expectedResult = originalTags;

        log.info("Trying empty Tags");

        url = this.generateURL(VALID_UUID, tags, true);
        response = request(url, USER_AUTHORISED, PUT);
        assessStatus(response, Expectations.EMPTY_TAGS);
        compareTagListOnProcessWith(expectedResult);

        // duplicate tags
        tags = Stream.of(VALID_TAG, VALID_TAG).collect(Collectors.toList());
        expectedResult = new ArrayList<String>();
        expectedResult.addAll(originalTags);
        expectedResult.add(VALID_TAG);

        log.info("Trying duplicate Tags");

        url = generateURL(VALID_UUID, tags, true);
        response = request(url, USER_AUTHORISED, PUT);
        assessStatus(response, Expectations.DUBLICATE_TAGS);
        compareTagListOnProcessWith(expectedResult);
        resetTagsOnProcess();

        // weird version
        tags = Stream.of(VALID_TAG, INVALID_TAG).collect(Collectors.toList());
        expectedResult = originalTags;

        log.info("Trying an invalid version param");

        url = generateURL(VALID_UUID, tags, true) + "&version=w.t.4";
        response = request(url, USER_AUTHORISED, PUT);
        assessStatus(response, Expectations.INVALID_VERSION);
        compareTagListOnProcessWith(expectedResult);

        // fishy bearer token
        tags = Stream.of(VALID_TAG, INVALID_TAG).collect(Collectors.toList());
        expectedResult = originalTags;

        log.info("Trying some fishy bearer token");

        url = generateURL(VALID_UUID, tags, true);
        response = client.target(url).request().header(AUTHORISATION_HEADER, AUTHORISATION_TYPE + " " + "imJustMakingThisUp")
                .put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        assessStatus(response, Expectations.INVALID_BEARER_TOKEN);
        compareTagListOnProcessWith(expectedResult);

        // broken bearer token
        tags = Stream.of(VALID_TAG, INVALID_TAG).collect(Collectors.toList());
        expectedResult = originalTags;

        log.info("Trying a broken bearer token");

        url = generateURL(VALID_UUID, tags, true);
        response = client.target(url).request().header(AUTHORISATION_HEADER, AUTHORISATION_TYPE).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        assessStatus(response, Expectations.INVALID_BEARER_TOKEN);
        compareTagListOnProcessWith(expectedResult);
    }

    private void assignRemoveTest(boolean createTags, List<String> expectedResult) {
        // Assemble testdata
        List<String> testers = Stream.of(ADMIN, USER_AUTHORISED, USER_UNAUTHORISED, ANONYMOUS)
                .collect(Collectors.toList());
        List<String> tags = Stream.of(VALID_TAG, INVALID_TAG).collect(Collectors.toList());
        Map<String, String> urls = Stream.of(VALID_UUID, INVALID_UUID)
                .collect(Collectors.toMap(uuid -> (String) uuid, uuid -> (String) generateURL(uuid, tags, createTags)));

        // Try to add tags, check results and then try to delete.
        for (String tester : testers) {
            for (String uuid : urls.keySet()) {
                log.info("Testing as " + babelFish.get(tester) + ", using " + babelFish.get(uuid) + ": PUT "
                        + urls.get(uuid));
                // Adding..
                Response response = request(urls.get(uuid), tester, PUT);
                int status = assessStatus(response, Expectations.get(PUT + uuid + tester + createTags));
                if (200 <= status && status < 300)
                    compareTagListOnProcessWith(expectedResult);

                // Deleting..
                Response deleteResponse = request(urls.get(uuid), tester, DELETE);
                if (log.isDebugEnabled())
                    log.debug("Testing as  " + babelFish.get(tester) + ", using " + babelFish.get(uuid) + ": DELETE "
                            + urls.get(uuid));
                int deleteStatus = assessStatus(deleteResponse, Expectations.get(DELETE + uuid + tester + createTags));
                if (200 <= deleteStatus && deleteStatus < 300)
                    compareTagListOnProcessWith(originalTags);
            }
        }
    }

    private void compareTagListOnProcessWith(List<String> expectedResult) {
        List<String> result = fetchTagsOn(VALID_UUID);
        if (!CollectionUtils.isEqualCollection(result, expectedResult))
            org.testng.Assert.fail("Expected tagList " + stringify(expectedResult, "'") + " but actually recieved "
                    + stringify(result, "'"));
    }

    private List<String> fetchTagsOn(String uuid) {
        return request(generateURL(uuid), USER_AUTHORISED, GET).readEntity(StringList.class).getStrings();
    }

	private int resetTagsOnProcess() {
		List<String> potentiallySetTags = Stream.of(VALID_TAG, INVALID_TAG).collect(Collectors.toList());
		return request(generateURL(VALID_UUID, potentiallySetTags, false), USER_AUTHORISED, DELETE).getStatus();
	}

    private void assessTagList(Response response, List<String> expectedResult) {
        List<String> result = response.readEntity(StringList.class).getStrings();
        if (!CollectionUtils.isEqualCollection(result, expectedResult))
            org.testng.Assert.fail("Expected tagList " + stringify(expectedResult, "'") + " but actually recieved "
                    + stringify(result, "'"));
    }

    private int assessStatus(Response response, int expectedStatus) {
        if (response == null)
            org.testng.Assert.fail("There was no response.");

        // Verify results.
        int responseStatus = response.getStatus();
        if (responseStatus != expectedStatus) {
            org.testng.Assert
                    .fail("Expected status " + expectedStatus + " but actually recieved " + responseStatus + ".");
        } else {
            if (log.isDebugEnabled())
                log.debug("Ok. Received response status as expected: " + responseStatus);
        }
        return responseStatus;
    }

    private Response request(String url, String userName, String httpMethod) {
        final Invocation.Builder builder;
        if (ANONYMOUS.equals(userName)) {
            builder = client.target(url).request();
        } else {
            builder = client.target(url).request().header(AUTHORISATION_HEADER, generateKey(userName));
        }

        final Response response;
        if (PUT.equals(httpMethod))
            response = builder.put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        else if (DELETE.equals(httpMethod))
            response = builder.delete(Response.class);
        else if (GET.equals(httpMethod))
            response = builder.get(Response.class);
        else
            throw new IllegalArgumentException(String.format("Unknown http method '%s'", httpMethod));
        return response;
    }

    private String generateURL(String uuid) {
        return generateURL(uuid, null, false);
    }

    private String generateURL(String uuid, List<String> tags, boolean createTags) {
        boolean tagsGiven = !(tags == null || tags.isEmpty());
        StringBuilder sb = new StringBuilder(TestContext.PRIMARY_SITE_URL + "resource/processes/" + uuid + "/tags");

        if (tagsGiven || createTags)
            sb.append("?");

        if (tagsGiven)
            sb.append("tag=" + StringUtils.join(tags, "&tag="));

        if (tagsGiven && createTags)
            sb.append("&");

        if (createTags)
            sb.append("createTags=" + createTags);

        return sb.toString();
    }

    /**
     * Warning: Do not call this method on unknown users. The method used sends a
     * request and expects 200 as response status.
     *
     * @param userName
     * @return
     */
    private String generateKey(String userName) {
        return AUTHORISATION_TYPE + " " + testFunctions.generateAPIKeyGET(userName, PASSWORD);
    }

    private String stringify(List<String> strings, String quote) {
        if (StringUtils.isNotBlank(quote))
            strings = strings.stream().map(s -> quote + s + quote).collect(Collectors.toList());
        return "[" + StringUtils.join(strings, ", ") + "]";
    }

    /* In the following we keep track of expected results */

    private void setExpectationsForGET() {
        Expectations.put(GET + VALID_UUID + ADMIN, 200);
        Expectations.put(GET + VALID_UUID + USER_AUTHORISED, 200);
        Expectations.put(GET + VALID_UUID + USER_UNAUTHORISED, 403);
        Expectations.put(GET + VALID_UUID + ANONYMOUS, 403);

        Expectations.put(GET + INVALID_UUID + ADMIN, 400);
        Expectations.put(GET + INVALID_UUID + USER_AUTHORISED, 400);
        Expectations.put(GET + INVALID_UUID + USER_UNAUTHORISED, 400);
        Expectations.put(GET + INVALID_UUID + ANONYMOUS, 400);
    }

    private void setExpectationsForPUT() {
        boolean createTags = false;
        Expectations.put(PUT + VALID_UUID + ADMIN + createTags, 200);
        Expectations.put(PUT + VALID_UUID + USER_AUTHORISED + createTags, 200);
        Expectations.put(PUT + VALID_UUID + USER_UNAUTHORISED + createTags, 403);
        Expectations.put(PUT + VALID_UUID + ANONYMOUS + createTags, 403);

        Expectations.put(PUT + INVALID_UUID + ADMIN + createTags, 400);
        Expectations.put(PUT + INVALID_UUID + USER_AUTHORISED + createTags, 400);
        Expectations.put(PUT + INVALID_UUID + USER_UNAUTHORISED + createTags, 400);
        Expectations.put(PUT + INVALID_UUID + ANONYMOUS + createTags, 400);
    }

    private void setExpectationsForPUTWithCreate() {
        boolean createTags = true;
        Expectations.put(PUT + VALID_UUID + ADMIN + createTags, 204);
        Expectations.put(PUT + VALID_UUID + USER_AUTHORISED + createTags, 204);
        Expectations.put(PUT + VALID_UUID + USER_UNAUTHORISED + createTags, 403);
        Expectations.put(PUT + VALID_UUID + ANONYMOUS + createTags, 403);

        Expectations.put(PUT + INVALID_UUID + ADMIN + createTags, 400);
        Expectations.put(PUT + INVALID_UUID + USER_AUTHORISED + createTags, 400);
        Expectations.put(PUT + INVALID_UUID + USER_UNAUTHORISED + createTags, 400);
        Expectations.put(PUT + INVALID_UUID + ANONYMOUS + createTags, 400);
    }

    private void setExpectationsForDELETE() {
        boolean createTags = false;
        Expectations.put(DELETE + VALID_UUID + ADMIN + createTags, 200);
        Expectations.put(DELETE + VALID_UUID + USER_AUTHORISED + createTags, 200);
        Expectations.put(DELETE + VALID_UUID + USER_UNAUTHORISED + createTags, 403);
        Expectations.put(DELETE + VALID_UUID + ANONYMOUS + createTags, 403);

        Expectations.put(DELETE + INVALID_UUID + ADMIN + createTags, 400);
        Expectations.put(DELETE + INVALID_UUID + USER_AUTHORISED + createTags, 400);
        Expectations.put(DELETE + INVALID_UUID + USER_UNAUTHORISED + createTags, 400);
        Expectations.put(DELETE + INVALID_UUID + ANONYMOUS + createTags, 400);
    }

    private void setExpectationsForDELETEWithCreate() {
        boolean createTags = true;
        Expectations.put(DELETE + VALID_UUID + ADMIN + createTags, 204);
        Expectations.put(DELETE + VALID_UUID + USER_AUTHORISED + createTags, 204);
        Expectations.put(DELETE + VALID_UUID + USER_UNAUTHORISED + createTags, 403);
        Expectations.put(DELETE + VALID_UUID + ANONYMOUS + createTags, 403);

        Expectations.put(DELETE + INVALID_UUID + ADMIN + createTags, 400);
        Expectations.put(DELETE + INVALID_UUID + USER_AUTHORISED + createTags, 400);
        Expectations.put(DELETE + INVALID_UUID + USER_UNAUTHORISED + createTags, 400);
        Expectations.put(DELETE + INVALID_UUID + ANONYMOUS + createTags, 400);
    }

    /**
     * Inner class to register generic and less generic expectations.
     *
     * @author dev
     */
    private static class Expectations {

        static final Map<String, Integer> generics = new HashMap<String, Integer>();

        static final int DUBLICATE_TAGS = 204;

        static final int NO_TAGS = 400;

        static final int EMPTY_TAGS = 400;

        static final int INVALID_BEARER_TOKEN = 403;

        static final int INVALID_VERSION = 400;

        static void put(String key, int value) {
            generics.put(key, value);
        }

        static int get(String key) {
            return generics.get(key);
        }
    }
}
