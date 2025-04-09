package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the remove functionality for API.
 *
 * @author sarai
 */
@Listeners({ScreenShooter.class})
public class T121DatasetRemoveAPITest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T121DatasetRemoveAPITest.class);
    private static final String WRITING_USER = "Admin1";
    private static final String READING_USER = "User3";
    private static final String STOCK_ID = "8e771302-b68e-4ce3-ba04-cb0e2095dc0e";
    private static final String ROOT_STOCK_ID = "1e92f562-09c1-4828-8eea-eb7428a34fa6";
    private static final String WRONG_VERION_FORMAT = "343eqe.6gfhfg.90sfs";
    private static final String PASSWORD = "s3cr3t";
    private static final String HEADER_NAME = "Authorization";
    /*
     * The network client to connect to the REST API.
     */
    Client client;
    WebDriver driver;
    private int correct_dependency = 3;
    private int wrong_dependency = 5;

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T121DatasetRemoveAPITest_fragment.xml", "DB_post_T015ImportExportTest.xml"));
    }

    /**
     * Sets up the test by logging in and creating a network client.
     */
    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        log.info("Begin of DatasetRemoveAPI test");

        client = testFunctions.createClient();
    }

    @Test(priority = 101)
    public void testProcesses() {
        log.info("begin to test removing processes");
        String type = "processes";
        String removedDataset = "1a7da06d-e8b7-4ff1-920c-209e9009dbe0";

        removeAnonymous(type, removedDataset);
        removeNotWrite(type, removedDataset);
        wrongDataStockID(type, removedDataset);
        notExisting(type, "5296e2be-060b-4e50-b033-d45f85f6ac92");
        rootDataStock(type, removedDataset);
        wrongVersion(type, removedDataset, "02.00.000");
        wrongVersionFormat(type, removedDataset);
        wrongDependency(type, removedDataset);
        remove(type, removedDataset);
        removeVersion(type, "4cffadc4-e2a2-4d34-9087-e54a2cfa4bd0", "03.00.000");

        String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
        String[] dependencyType = {"contacts"};
        removeDependencies(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f", dependencies, dependencyType);

        notExisting(type, removedDataset);
        log.info("end of test removing processes");
    }

    @Test(priority = 102)
    public void testLICIAMethods() {
        log.info("begin to test removing LCIA methods");
        String type = "lciamethods";
        String removedDataset = "edabaa8b-89d0-4cb6-ba92-444ac5265422";

        removeAnonymous(type, removedDataset);
        removeNotWrite(type, removedDataset);
        wrongDataStockID(type, removedDataset);
        notExisting(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, removedDataset);
        wrongVersion(type, removedDataset, "02.00.000");
        wrongVersionFormat(type, removedDataset);
        wrongDependency(type, removedDataset);

        remove(type, removedDataset);
        removeVersion(type, "b7d61a6f-cb2f-4a46-b511-39c3e4cc31d3", "01.00.000");

//		Dependencies is currently not supported
//		String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
//		String[] dependencyType = {"contacts"};
//		removeDependencies(type, "992c8e8d-769a-4930-9b0f-4fa323250738", dependencies, dependencyType);
        notExisting(type, removedDataset);
        log.info("end of test removing LCIA methods");
    }

    @Test(priority = 103)
    public void testElementaryFlows() {
        log.info("begin to test removing elementary flows");
        String type = "flows";
        String removedDataset = "0a2e688c-b9bd-416c-a25f-9446815aefa3";

        removeAnonymous(type, removedDataset);
        removeNotWrite(type, removedDataset);
        wrongDataStockID(type, removedDataset);
        notExisting(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, removedDataset);
        wrongVersion(type, removedDataset, "02.00.000");
        wrongVersionFormat(type, removedDataset);
        wrongDependency(type, removedDataset);

        remove(type, removedDataset);
        removeVersion(type, "0a2a71f2-4f59-4a67-b5f1-d633039ce0ea", "03.00.000");

//		Dependencies is currently not supported
//		String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
//		String[] dependencyType = {"contacts"};
//		removeDependencies(type, "0a0c1d5d-a396-49da-8ee0-9d4c2a06ea1f");

        notExisting(type, removedDataset);
        log.info("end of test removing elementary flows");
    }

    // This test does currently not work since there seems to be a bug in which
    // product flows are not shown in flows list of root data stocks
//	@Test(priority=104)
    public void testProductFlows() {
        log.info("begin to test removing product flows");
        String type = "flows";
        String removedDataset = "ffd105a3-1ae2-4af3-b7b0-ebceb224fd40";

        removeAnonymous(type, removedDataset);
        removeNotWrite(type, removedDataset);
        wrongDataStockID(type, removedDataset);
        notExisting(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, removedDataset);
        wrongVersion(type, removedDataset, "02.00.000");
        wrongVersionFormat(type, removedDataset);
        wrongDependency(type, removedDataset);

//		Due to a not fixed bug, the following four test methods cannot be run correctly.
//		remove(type, removedDataset);
//		removeVersion(type, "bc0d4a49-8840-45dd-ab3e-e18928012306", "01.00.000");

//		Dependencies is currently not supported
//		String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
//		String[] dependencyType = {"contacts"};
//		removeDependencies(type, "82b33e71-bfaa-49b4-9627-ee5963433f6e");

//		notExisting(type, removedDataset);
        log.info("end of test removing product flows");
    }

    @Test(priority = 105)
    public void testFlowPorperties() {
        log.info("begin to test removing flow properties");
        String type = "flowproperties";
        String removedDataset = "93a60a56-a3c8-19da-a746-0800200c9a66";

        removeAnonymous(type, removedDataset);
        removeNotWrite(type, removedDataset);
        wrongDataStockID(type, removedDataset);
        notExisting(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, removedDataset);
        wrongVersion(type, removedDataset, "02.00.000");
        wrongVersionFormat(type, removedDataset);
        wrongDependency(type, removedDataset);


        remove(type, removedDataset);
        removeVersion(type, "93a60a56-a3c8-14da-a746-0800200c9a66", "03.00.000");

        String[] dependencies = {"838aaa22-0117-11db-92e3-0800200c9a66"};
        String[] dependencyType = {"unitgroups"};
        removeDependencies(type, "838aaa23-0117-11db-92e3-0800200c9a66", dependencies, dependencyType);

        notExisting(type, removedDataset);
        log.info("end of test removing flow properties");
    }

    @Test(priority = 106)
    public void testSources() {
        log.info("begin to test removing sources");
        String type = "sources";
        String removedDataset = "dff34d61-54e9-4404-95ca-c09cc01b7f40";

        removeAnonymous(type, removedDataset);
        removeNotWrite(type, removedDataset);
        wrongDataStockID(type, removedDataset);
        notExisting(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, removedDataset);
        wrongVersion(type, removedDataset, "02.00.000");
        wrongVersionFormat(type, removedDataset);
        wrongDependency(type, removedDataset);

        remove(type, removedDataset);
        removeVersion(type, "8039a379-7a2b-4f51-a062-62b3cf1723ee", "03.00.000");

        String[] dependencies = {"d0f67f21-22d8-4e5e-914b-6f7dde923e33"};
        String[] dependencyType = {"contacts"};
        removeDependencies(type, "544205b8-bcc5-4850-a5b6-241434f23be6", dependencies, dependencyType);

        notExisting(type, removedDataset);
        log.info("end of test removing sources");
    }

    @Test(priority = 107)
    public void testUnitGroups() {
        log.info("begin to test removing unit groups");
        String type = "unitgroups";
        String removedDataset = "ff8ed45d-bbfb-4531-8c7b-9b95e52bd41d";

        removeAnonymous(type, removedDataset);
        removeNotWrite(type, removedDataset);
        wrongDataStockID(type, removedDataset);
        notExisting(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, removedDataset);
        wrongVersion(type, removedDataset, "02.00.000");
        wrongVersionFormat(type, removedDataset);
        wrongDependency(type, removedDataset);

        remove(type, removedDataset);
        removeVersion(type, "f170abd3-f010-45f4-8e7c-9871a5c0421b", "03.00.000");

//		Datastock does not have any dependencies in Unit Groups
//		String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
//		String[] dependencyType = {"contacts"};
//		removeDependencies(type, "b4cac580-5ce8-11df-a08a-0800200c9a66");

        notExisting(type, removedDataset);
        log.info("end of test removing unit groups");
    }

    @Test(priority = 108)
    public void testContacts() {
        log.info("begin to test removing contacts");
        String type = "contacts";

        String removedDataset = "5bb337b0-9a1a-11da-a72b-0800200c9a69";

        removeAnonymous(type, removedDataset);
        removeNotWrite(type, removedDataset);
        wrongDataStockID(type, removedDataset);
        notExisting(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, removedDataset);
        wrongVersion(type, removedDataset, "02.00.000");
        wrongVersionFormat(type, removedDataset);
        wrongDependency(type, removedDataset);
        remove(type, removedDataset);
        removeVersion(type, "aa0a2faf-0bcf-428f-9fdf-c74d7e6a5136", "03.00.000");
        notExisting(type, removedDataset);
        log.info("end of test removing contacts");
    }


    /**
     * Checks removing a dataset as an anonymous user (not authenticated) from stock.
     *
     * @param type       The type of dataset to remove
     * @param dataset_id The UUID of dataset to remove
     */
    private void removeAnonymous(String type, String dataset_id) {
        log.debug("Removing as anonymous user");
        String APIKey = testFunctions.generateAPIKeyGET(READING_USER, PASSWORD, 200);
        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id).request().delete(Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        log.debug("response is: HTTP " + status + " - " + responseString);
        if (status != 401 || !responseString.equals("You are not permitted to operate on this data stock.")) {
            org.testng.Assert.fail("After trying to removing as anonymous user, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

    }

    /**
     * Checks removing a dataset as user without write permissions from stock.
     *
     * @param type       The type of dataset to remove
     * @param dataset_id The UUID of dataset to remove
     */
    private void removeNotWrite(String type, String dataset_id) {
        log.debug("Removing as user without write permissions");

        String APIKey = testFunctions.generateAPIKeyGET(READING_USER, PASSWORD, 200);
        if (log.isTraceEnabled()) {
            log.trace("API key is: " + APIKey);
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id).request().header(HEADER_NAME, "Bearer " + APIKey).delete(Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("You are not permitted to operate on this data stock.")) {
            org.testng.Assert.fail("After trying to remove as user without writing permissions, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

    }

    /**
     * Checks removing a plain dataset.
     *
     * @param type       The type of dataset to remove
     * @param dataset_id The UUID of dataset to remove
     */
    private void remove(String type, String dataset_id) {
        log.debug("Removing as user without any other parameters");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id).request().header(HEADER_NAME, "Bearer " + APIKey).delete(Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (log.isDebugEnabled()) {
            log.debug("after removing the status is: " + status);
            log.debug("after removing the response message is: " + responseString);
        }
        if (status != 200 || !responseString.equals("Dataset has been removed from data stock.")) {
            org.testng.Assert.fail("After trying to remove as user with the right permissions, the response status was either wrong (expected: 200, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        if (log.isDebugEnabled()) {
            log.trace("API Key: " + APIKey);
        }
        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

    }

    /**
     * Checks removing a dataset with a given version.
     *
     * @param type       The type of dataset to remove
     * @param dataset_id The UUID of dateset to remove
     * @param version    The (correct) version of dataset to remove
     */
    private void removeVersion(String type, String dataset_id, String version) {
        log.debug("Removing with version");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id + "?version=" + version);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id)
                .queryParam("version", version)
                .request().header(HEADER_NAME, "Bearer " + APIKey).delete(Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        int expectedStatus = 200;
        String expectedResponseMessage = "Dataset has been removed from data stock.";
        if (status != 200 || !expectedResponseMessage.equals(responseString)) {
            org.testng.Assert.fail("After trying to remove with additional version parameter, the response status was either wrong (expected: " + expectedStatus + ", got: " + status + ") or an unexpected response message was sent (expected: '" + expectedResponseMessage + "', got: '" + responseString + "')");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

    }

    /**
     * Checks removing a dataset with all its dependencies.
     *
     * @param type           The type of dataset to remove
     * @param dataset_id     The UUID of dataset to remove
     * @param dependencies   The list of dependency UUIDs that should be removed with
     * @param dependencyType The types of dependencies rthat should be removed with
     */
    private void removeDependencies(String type, String dataset_id, String[] dependencies, String[] dependencyType) {
        log.debug("Removing with dependencies");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        for (int i = 0; i < dependencies.length; i++) {
            testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dependencies[i], dependencyType[i], true, HEADER_NAME, client);
        }

        log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id)
                .queryParam("withDependencies", correct_dependency)
                .request().header(HEADER_NAME, "Bearer " + APIKey).delete(Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 200 || !responseString.equals("Dataset with its dependencies has been removed from data stock.")) {
            org.testng.Assert.fail("After trying to removing as user with additional dependencies parameter, the response status was either wrong (expected: 200, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        for (int i = 0; i < dependencies.length; i++) {
            testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dependencies[i], dependencyType[i], false, HEADER_NAME, client);
        }

    }

    /**
     * Checks removing a dataset from a not existing data stock.
     *
     * @param type       The type of dataset to assing
     * @param dataset_id The UUID of dataset to assing
     */
    private void wrongDataStockID(String type, String dataset_id) {
        log.debug("Removing with wrong data stock ID");
        String stock_id = "7ztzqpuhfdgfd7te89rw";

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, stock_id, dataset_id, type, false, HEADER_NAME, client);

        log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + stock_id + "/" + type + "/" + dataset_id).request().header(HEADER_NAME, "Bearer " + APIKey).delete(Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("Data stock is not correctly set.")) {
            org.testng.Assert.fail("After trying to remove dataset from not exisiting data stock, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, stock_id, dataset_id, type, false, HEADER_NAME, client);

    }

    /**
     * Checks removing a dataset that is not of given type or does not exist in data stock.
     *
     * @param type       The (wrong) type of dataset to remove
     * @param dataset_id The UUID of dataset to remove
     */
    private void notExisting(String type, String dataset_id) {
        log.debug("removing not existing dataset");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id).request().header(HEADER_NAME, "Bearer " + APIKey).delete(Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 200 || !responseString.equals("Dataset with given UUID is not assigned to data stock.")) {
            org.testng.Assert.fail("After trying to remove wrong dataset from " + STOCK_ID + ", the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }


    }

    /**
     * Checking whether a dataset can be removed from a root data stock.
     *
     * @param type       The type of the dataset to remove
     * @param dataset_id The UUID of dataset to remove
     */
    private void rootDataStock(String type, String dataset_id) {
        log.debug("Removing from root data stock");

        String APIKey = testFunctions.generateAPIKeyGET("Admin1", PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + ROOT_STOCK_ID + "/" + type + "/" + dataset_id).request().header(HEADER_NAME, "Bearer " + APIKey).delete(Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("Data stock must be a logical data stock.")) {
            org.testng.Assert.fail("After trying to remove dataset from root data stock, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
    }

    /**
     * Checks removing a dataset with wrong version number.
     *
     * @param type       The type of dataset to remove
     * @param dataset_id The UUID of dataset to remove
     * @param version    An (incorrect) version of dataset
     */
    private void wrongVersion(String type, String dataset_id, String version) {
        log.debug("Removing with wrong version");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id + "?version=" + version);
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id)
                .queryParam("version", version)
                .request().header(HEADER_NAME, "Bearer " + APIKey).delete(Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 200 || !responseString.equals("Dataset with given UUID and version is not assigned to data stock.")) {
            org.testng.Assert.fail("After trying to removing dataset with incorrect version to " + STOCK_ID + ", the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: '" + responseString + "')");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

    }

    /**
     * Checks removing a dataset with a version in wrong format.
     *
     * @param type       The type of dataset to remove
     * @param dataset_id The UUID of dataset to remove
     */
    private void wrongVersionFormat(String type, String dataset_id) {
        log.debug("Removing with wrong version format");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id + "?version=" + WRONG_VERION_FORMAT);
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id)
                .queryParam("version", WRONG_VERION_FORMAT)
                .request().header(HEADER_NAME, "Bearer " + APIKey).delete(Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("Version is not in a correct format.")) {
            org.testng.Assert.fail("After trying to remove dataset with wrong version format, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

    }

    /**
     * Checks removing a dataset with not existing dependency mode.
     *
     * @param type       The type of dataset to remove
     * @param dataset_id Te UUID of dataset to remove
     */
    private void wrongDependency(String type, String dataset_id) {
        log.debug("Removing with wrong dependency");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        log.debug("DELETE URI is: " + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id + "?withDependencies=" + wrong_dependency);
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id)
                .queryParam("withDependencies", wrong_dependency)
                .request().header(HEADER_NAME, "Bearer " + APIKey).delete(Response.class);


        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("Dependencies mode is not legal.")) {
            org.testng.Assert.fail("After trying to remove dataset from " + STOCK_ID + " with wrong dependencies, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);
        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

    }


}
