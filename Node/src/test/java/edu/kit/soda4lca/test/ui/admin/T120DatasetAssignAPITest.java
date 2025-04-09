package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.DependenciesMode;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.codeborne.selenide.Selenide.$x;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the assign functionality for API.
 *
 * @author sarai
 */
@SuppressWarnings("SameParameterValue")
@Listeners({ScreenShooter.class})
public class T120DatasetAssignAPITest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T120DatasetAssignAPITest.class);
    private static final String WRITING_USER = "Admin1";
    private static final String READING_USER = "User3";
    private static final String STOCK_ID = "8e771302-b68e-4ce3-ba04-cb0e2095dc0e";
    private static final String ROOT_STOCK_ID = "9e2c6d39-daee-49d0-9d38-783100116aac";
    private static final String WRONG_VERION_FORMAT = "343eqe.6gfhfg.90sfs";
    private static final String PASSWORD = "s3cr3t";
    private static final String HEADER_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    
    /*
     * The network client to connect to the REST API.
     */
    Client client;
    private int correct_dependency = 3;
    private int wrong_dependency = 5;
    private JdbcTemplate jdbcTemplate;
    private DataSetType datasetType;

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T120DatasetAssignAPITest_fragment.xml", "DB_post_T015ImportExportTest.xml"));
    }

    /**
     * Sets up the test by logging in and creating a network client.
     */
    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        log.info("Begin of DatasetAssignAPI test");

        client = testFunctions.createClient();
        jdbcTemplate = super.newJDBCTemplate();
    }

    @Test(priority = 101)
    public void testProcesses() throws InterruptedException {
        log.info("begin to test assigning processes");
        String type = "processes";
        String assignedDataset = "1a7da06d-e8b7-4ff1-920c-209e9009dbe0";

        assignNotWrite(type, assignedDataset);
        assignAnonymous(type, assignedDataset);
        wrongDataStockID(type, assignedDataset);
        wrongDatasetID(type, "5296e2be-060b-4e50-b033-d45f85f6ac92");
        rootDataStock(type, assignedDataset);
        wrongVersion(type, assignedDataset, "02.00.000");
        wrongVersionFormat(type, assignedDataset);
        wrongDependency(type, assignedDataset);

        assign(type, assignedDataset);
        assignVersion(type, "4cffadc4-e2a2-4d34-9087-e54a2cfa4bd0", "03.00.000");

        String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
        String[] dependencyType = {"contacts"};
        assignDependencies(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f", dependencies, dependencyType);

        alreadyExisting(type, assignedDataset);
        log.info("end of test assigning processes");

    }

    @Test(priority = 102)
    public void testLICIAMethods() throws InterruptedException {
        log.info("begin to test assigning LCIA methods");
        String type = "lciamethods";
        String assignedDataset = "edabaa8b-89d0-4cb6-ba92-444ac5265422";

        assignAnonymous(type, assignedDataset);
        assignNotWrite(type, assignedDataset);
        wrongDataStockID(type, assignedDataset);
        wrongDatasetID(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, assignedDataset);
        wrongVersion(type, assignedDataset, "02.00.000");
        wrongVersionFormat(type, assignedDataset);
        wrongDependency(type, assignedDataset);

        assign(type, assignedDataset);
        assignVersion(type, "b7d61a6f-cb2f-4a46-b511-39c3e4cc31d3", "01.00.000");

//		Dependencies is currently not supported
//		String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
//		String[] dependencyType = {"contacts"};
//		assignDependencies(type, "992c8e8d-769a-4930-9b0f-4fa323250738", dependencies, dependencyType);
        alreadyExisting(type, assignedDataset);
        log.info("end of test assigning LCIA methods");
    }

    @Test(priority = 103)
    public void testElementaryFlows() throws InterruptedException {
        log.info("begin to test assigning elementary flows");
        String type = "flows";
        String assignedDataset = "0a2e688c-b9bd-416c-a25f-9446815aefa3";

        assignAnonymous(type, assignedDataset);
        assignNotWrite(type, assignedDataset);
        wrongDataStockID(type, assignedDataset);
        wrongDatasetID(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, assignedDataset);
        wrongVersion(type, assignedDataset, "02.00.000");
        wrongVersionFormat(type, assignedDataset);
        wrongDependency(type, assignedDataset);

        assign(type, assignedDataset);
        assignVersion(type, "0a2a71f2-4f59-4a67-b5f1-d633039ce0ea", "03.00.000");

//		Dependencies is currently not supported
//		String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
//		String[] dependencyType = {"contacts"};
//		assignDependencies(type, "0a0c1d5d-a396-49da-8ee0-9d4c2a06ea1f");
        alreadyExisting(type, assignedDataset);
        log.info("end of test assigning elementary flows");

    }

    @Test(priority = 104)
    public void testProductFlows() throws InterruptedException {
        log.info("begin to test assigning product flows");
        String type = "flows";
        String assignedDataset = "ffd105a3-1ae2-4af3-b7b0-ebceb224fd40";

        assignAnonymous(type, assignedDataset);
        assignNotWrite(type, assignedDataset);
        wrongDataStockID(type, assignedDataset);
        wrongDatasetID(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, assignedDataset);
        wrongVersion(type, assignedDataset, "02.00.000");
        wrongVersionFormat(type, assignedDataset);
        wrongDependency(type, assignedDataset);

//		Due to a not fixed bug, the following four test methods cannot be run correctly.
//		assign(type, assignedDataset);
//		assignVersion(type, "bc0d4a49-8840-45dd-ab3e-e18928012306", "01.00.000");

//		Dependencies is currently not supported
//		String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
//		String[] dependencyType = {"contacts"};
//		assignDependencies(type, "82b33e71-bfaa-49b4-9627-ee5963433f6e");

//		alreadyExisting(type, assignedDataset);
        log.info("end of test assigning product flows");
    }

    @Test(priority = 105)
    public void testFlowPorperties() throws InterruptedException {
        log.info("begin to test assigning flow properties");
        String type = "flowproperties";
        String assignedDataset = "93a60a56-a3c8-19da-a746-0800200c9a66";

        assignAnonymous(type, assignedDataset);
        assignNotWrite(type, assignedDataset);
        wrongDataStockID(type, assignedDataset);
        wrongDatasetID(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, assignedDataset);
        wrongVersion(type, assignedDataset, "02.00.000");
        wrongVersionFormat(type, assignedDataset);
        wrongDependency(type, assignedDataset);


        assign(type, assignedDataset);
        assignVersion(type, "93a60a56-a3c8-14da-a746-0800200c9a66", "03.00.000");

        String[] dependencies = {"838aaa22-0117-11db-92e3-0800200c9a66"};
        String[] dependencyType = {"unitgroups"};
        assignDependencies(type, "838aaa23-0117-11db-92e3-0800200c9a66", dependencies, dependencyType);

        alreadyExisting(type, assignedDataset);
        log.info("end of test assigning flow properties");

    }

    @Test(priority = 106)
    public void testSources() throws InterruptedException {
        log.info("begin to test assigning sources");
        String type = "sources";
        String assignedDataset = "dff34d61-54e9-4404-95ca-c09cc01b7f40";

        assignAnonymous(type, assignedDataset);
        assignNotWrite(type, assignedDataset);
        wrongDataStockID(type, assignedDataset);
        wrongDatasetID(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, assignedDataset);
        wrongVersion(type, assignedDataset, "02.00.000");
        wrongVersionFormat(type, assignedDataset);
        wrongDependency(type, assignedDataset);

        assign(type, assignedDataset);
        assignVersion(type, "8039a379-7a2b-4f51-a062-62b3cf1723ee", "03.00.000");

        String[] dependencies = {"d0f67f21-22d8-4e5e-914b-6f7dde923e33"};
        String[] dependencyType = {"contacts"};
        assignDependencies(type, "544205b8-bcc5-4850-a5b6-241434f23be6", dependencies, dependencyType);

        alreadyExisting(type, assignedDataset);
        log.info("end of test assigning sources");

    }

    @Test(priority = 107)
    public void testUnitGroups() throws InterruptedException {
        log.info("begin to test assigning unit groups");
        String type = "unitgroups";
        String assignedDataset = "ff8ed45d-bbfb-4531-8c7b-9b95e52bd41d";

        assignAnonymous(type, assignedDataset);
        assignNotWrite(type, assignedDataset);
        wrongDataStockID(type, assignedDataset);
        wrongDatasetID(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, assignedDataset);
        wrongVersion(type, assignedDataset, "02.00.000");
        wrongVersionFormat(type, assignedDataset);
        wrongDependency(type, assignedDataset);

        assign(type, assignedDataset);
        assignVersion(type, "f170abd3-f010-45f4-8e7c-9871a5c0421b", "03.00.000");

//		Datastock does not have any dependencies in Unit Groups
//		String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
//		String[] dependencyType = {"contacts"};
//		assignDependencies(type, "b4cac580-5ce8-11df-a08a-0800200c9a66");

        alreadyExisting(type, assignedDataset);
        log.info("end of test assigning unit groups");

    }

    @Test(priority = 108)
    public void testContacts() throws InterruptedException {
        log.info("begin to test assigning contacts");
        String type = "contacts";

        String assignedDataset = "5bb337b0-9a1a-11da-a72b-0800200c9a69";

        assignAnonymous(type, assignedDataset);
        assignNotWrite(type, assignedDataset);
        wrongDataStockID(type, assignedDataset);
        wrongDatasetID(type, "3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
        rootDataStock(type, assignedDataset);
        wrongVersion(type, assignedDataset, "02.00.000");
        wrongVersionFormat(type, assignedDataset);
        wrongDependency(type, assignedDataset);

        assign(type, assignedDataset);
        assignVersion(type, "aa0a2faf-0bcf-428f-9fdf-c74d7e6a5136", "03.00.000");

//		Datastock does not have any dependencies on contacts
//		String[] dependencies = {"16e9b2c6-3c44-11dd-ae16-0800200c9a66"};
//		String[] dependencyType = {"contacts"};
//		assignDependencies(type, "d0f67f21-22d8-4e5e-914b-6f7dde923e33");

        alreadyExisting(type, assignedDataset);
        log.info("end of test assigning contacts");

    }


    /**
     * Checks assigning a dataset as an anonymous user (not authenticated).
     *
     * @param type       The type of dataset to assign
     * @param dataset_id The UUID of dataset to assign
     */
    private void assignAnonymous(String type, String dataset_id) {
        log.debug("Assigning as anonymous user");

        testFunctions.checkDatasetExisting(null, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id).request().put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("You are not permitted to operate on this data stock.")) {
            org.testng.Assert.fail("After trying to assign as anonymous user, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(null, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

    }

    /**
     * Checks assigning a dataset as user without write permissions
     *
     * @param type       The type of dataset to assign
     * @param dataset_id The UUID of dataset to assign
     */
    private void assignNotWrite(String type, String dataset_id) {
        log.debug("Assigning as user without write permissions");

        String APIKey = testFunctions.generateAPIKeyGET(READING_USER, PASSWORD, 200);
        if (log.isTraceEnabled()) {
            log.trace("API key is: " + APIKey);
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id).request().header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("You are not permitted to operate on this data stock.")) {
            org.testng.Assert.fail("After trying to assign as user without writing permissions, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

    }

    /**
     * Checks assigning a plain dataset.
     *
     * @param type       The type of dataset to assign
     * @param dataset_id The UUID of dataset to assign
     */
    private void assign(String type, String dataset_id) {
        log.debug("Assigning as user without any other parameters");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id).request().header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (log.isDebugEnabled()) {
            log.debug("after assigning the status is: " + status);
            log.debug("after assigning the response message is: " + responseString);
        }
        if (status != 200 || !responseString.equals("Dataset has been assigned to data stock.")) {
            org.testng.Assert.fail("After trying to assign as user with the right permissions, the response status was either wrong (expected: 200, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }
        log.trace("API Key: " + APIKey);
        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

    }

    /**
     * Checks assigning a dataset with a given version
     *
     * @param type       The type of dataset to assign
     * @param dataset_id The UUID of datset to assign
     * @param version    The (correct) version of dataset to assign
     */
    private void assignVersion(String type, String dataset_id, String version) {
        log.debug("Assigning with version");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id + "?version=" + version);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id)
                .queryParam("version", version)
                .request().header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 200 || !responseString.equals("Dataset has been assigned to data stock.")) {
            org.testng.Assert.fail("After trying to assign with additional version parameter, the response status was either wrong (expected: 200, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

    }

    /**
     * Checks assigning a dataset with all its dependencies
     *
     * @param type           The type of dataset to assign
     * @param dataset_id     The UUID of dataset to assign
     * @param dependencies   The list of dependency UUIDs that should be assigned with
     * @param dependencyType The types of dependencies rthat should be assigned with
     */
    private void assignDependencies(String type, String dataset_id, String[] dependencies, String[] dependencyType) {
        log.debug("Assigning with dependencies");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        for (int i = 0; i < dependencies.length; i++) {
            testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dependencies[i], dependencyType[i], false, HEADER_NAME, client);
        }

        if (log.isDebugEnabled()) {
            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id)
                .queryParam("withDependencies", correct_dependency)
                .request().header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 200 || !responseString.equals("Dataset with its dependencies has been assigned to data stock.")) {
            org.testng.Assert.fail("After trying to assign as user with additional dependencies parameter, the response status was either wrong (expected: 200, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        for (int i = 0; i < dependencies.length; i++) {
            testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dependencies[i], dependencyType[i], true, HEADER_NAME, client);
        }

    }

    /**
     * Checks assigning a dataset to a not existing data stock.
     *
     * @param type       The type of dataset to assing
     * @param dataset_id The UUID of dataset to assing
     */
    private void wrongDataStockID(String type, String dataset_id) {
        log.debug("Assigning with wrong data stock ID");
        String stock_id = "7ztzqpuhfdgfd7te89rw";

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, stock_id, dataset_id, type, false, HEADER_NAME, client);
        if (log.isDebugEnabled()) {
            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + stock_id + "/" + type + "/" + dataset_id).request().header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("Data stock is not correctly set.")) {
            org.testng.Assert.fail("After trying to assign dataset to not exisiting data stock, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, stock_id, dataset_id, type, false, HEADER_NAME, client);

    }

    /**
     * Checks assigning a dataset that is not of given type or does not exist in datastock.
     *
     * @param type       The (wrong) type of dataset to assign
     * @param dataset_id The UUID of dataset to assign
     */
    private void wrongDatasetID(String type, String dataset_id) {
        log.debug("Assigning not existing dataset");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id).request().header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("Dataset with given UUID is not existing in database.")) {
            org.testng.Assert.fail("After trying to assign wrong dataset to " + STOCK_ID + ", the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

    }

    /**
     * Checking whether a dataset can be assigned to a root data stock.
     *
     * @param type       The type of the dataset to assign
     * @param dataset_id The UUID of dataset to assing
     */
    private void rootDataStock(String type, String dataset_id) {
        log.debug("Assigning to root data stock");

        String APIKey = testFunctions.generateAPIKeyGET("Admin1", PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + ROOT_STOCK_ID + "/" + type + "/" + dataset_id).request().header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        ;
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("Data stock must be a logical data stock.")) {
            org.testng.Assert.fail("After trying to assign dataset to root data stock, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, ROOT_STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

    }

    /**
     * Checks assigning a dataset with wrong version number.
     *
     * @param type       The type of dataset to assign
     * @param dataset_id The UUID of dataset to assign
     * @param version    An (incorrect) version of dataset
     */
    private void wrongVersion(String type, String dataset_id, String version) {
        log.debug("Assigning with wrong version");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id + "?version=" + version);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id)
                .queryParam("version", version)
                .request().header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("Dataset with given UUID and version is not existing in database.")) {
            org.testng.Assert.fail("After trying to assign dataset with incorrect version to " + STOCK_ID + ", the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: '" + responseString + "')");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

    }

    /**
     * Checks assigning a dataset with a version in wrong format.
     *
     * @param type       The type of dataset to assign
     * @param dataset_id The UUID of dataset to assign
     */
    private void wrongVersionFormat(String type, String dataset_id) {
        log.debug("Assigning with wrong version format");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        if (log.isDebugEnabled()) {
            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id + "?version=" + WRONG_VERION_FORMAT);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id)
                .queryParam("version", WRONG_VERION_FORMAT)
                .request().header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);
        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("Version is not in a correct format.")) {
            org.testng.Assert.fail("After trying to assign dataset to not exisiting data stock, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

    }

    /**
     * Checks assigning a dataset with not existing dependency mode.
     *
     * @param type       The type of dataset to assign
     * @param dataset_id Te UUID of dataset to assign
     */
    private void wrongDependency(String type, String dataset_id) {
        log.debug("Assigning with wrong dependency");

        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

        if (log.isDebugEnabled()) {

            log.debug("PUT URI is: '" + TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id + "?withDependencies=" + wrong_dependency);
        }
        Response response = client.target(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id + "?withDependencies=" + wrong_dependency).request().header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);


        int status = response.getStatus();
        String responseString = response.readEntity(String.class);
        if (status != 401 || !responseString.equals("Dependencies mode is not legal.")) {
            org.testng.Assert.fail("After trying to assign dataset to " + STOCK_ID + " with wrong dependencies, the response status was either wrong (expected: 401, got: " + status + ") or an unexpected response message was sent (message: " + responseString + ")");
        }

        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, false, HEADER_NAME, client);

    }

    /**
     * Checks an assigning an already assigned dataset.
     *
     * @param type       The type of dataset to assign
     * @param dataset_id The UUID of dataset to assign
     */
    private void alreadyExisting(String type, String dataset_id) {
        log.debug("Assigning already existing dataset");
        // Arrange
        String APIKey = testFunctions.generateAPIKeyGET(WRITING_USER, PASSWORD, 200);
        String url = TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + STOCK_ID + "/" + type + "/" + dataset_id;
        Response response;

        // Preconditions
        testFunctions.checkDatasetExisting(APIKey, STOCK_ID, dataset_id, type, true, HEADER_NAME, client);

        // Act
        response = client.target(url).request()
                .header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey)
                .put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);

        // Assert
        org.testng.Assert.assertEquals(response.getStatus(), 200, String.format("Response status when assigning already existing data set via '%s'", url));
        org.testng.Assert.assertEquals(response.readEntity(String.class), "Dataset is already assigned to data stock.",
                String.format("Response message when assigning already existing data set via '%s'", url));

    }

    @Test(priority = 109)
    public void unassign_previously_api_assigned_data_set_from_data_stock_via_api() throws MalformedURLException, FormatException {
        log.info("Un-assign previously assigned dataset via API (using uuid and version).");
        // ARRANGE
        var APIKey = testFunctions.generateAPIKeyGET("admin", "default"); // super admin
        var stockUuid = UUID.fromString("8856867f-2e5d-42f5-9dfe-bca46d3c4ad1");
        var processUuid = UUID.fromString("0dc3d65b-7ff8-4c92-a694-748fb28070a9");
        var processVersion = DataSetVersion.parse("03.00.000");
        var assignUnassignProcessUrl = new URL(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + stockUuid + "/processes/" + processUuid + "?version=" + processVersion.getVersionString());
        // We need this to verify the cross-reference within the database
        var stockId = getStockIdFromDBViaUuid(stockUuid);
        var processId = getProcessIdFromDBViaUuidAndVersion(processUuid, processVersion);

        assign_process_to_data_stock_via_api(APIKey, stockUuid, processUuid, processVersion, DependenciesMode.NONE);

        log.trace("Verifying that process (uuid='{}', version='{}', id={}) is among assigned data sets of data stock (uuid='{}', id={})...",
                processUuid, processVersion, processId, stockUuid, stockId);
        if (!testFunctions.isDataSetAmongAssignedDataSetsOfDataStock(processUuid, processVersion, DataSetType.PROCESS, stockUuid, client, APIKey)) {
            throw new IllegalStateException(String.format(
                    "Process (uuid:'%s', version:'%s') should have been among assigned data sets of stock '%s'.", processUuid, processVersion.getVersionString(), stockUuid));
        }

        if (!isProcessAssignedAccordingToDB(processId, stockId)) {
            throw new IllegalStateException(String.format(
                    "Assignment of process (id='%s', version='%s') to data stock (uuid='%s') should have produced entry in cross reference table, but has not.", processUuid, processVersion.getVersionString(), stockUuid));
        }

        // ACT
        log.info("Un-assigning process (uuid='{}', version='{}', id={}) to data stock (uuid='{}', id={}) via 'DELETE {}'...", processUuid, processVersion.getVersionString(), processId, stockUuid, stockId, assignUnassignProcessUrl);
        var unassignment = client.target(assignUnassignProcessUrl.toString()).request()
                .header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey)
                .delete();

        // ASSERT
        log.debug("Verifying response status...");
        var unassignmentStatus = unassignment.getStatus();
        log.trace("Status: {}", unassignmentStatus);
        org.testng.Assert.assertEquals(unassignmentStatus, 200,
                String.format("Response status when un-assigning already existing data set via '%s'", assignUnassignProcessUrl));

        log.debug("Verifying response message...");
        String unassignmentMessage = unassignment.readEntity(String.class); // Fun-fact: the entity can only be retrieved once from the client response object
        log.trace("Message: {}", unassignmentMessage);
        org.testng.Assert.assertEquals(unassignmentMessage, "Dataset has been removed from data stock.",
                String.format("Response message when un-assigning already existing data set via '%s'", assignUnassignProcessUrl));

        log.trace("Verifying that process (uuid='{}', version='{}', id={}) is among assigned data sets of data stock (uuid='{}', id={})...",
                processUuid, processVersion, processId, stockUuid, stockId);
        if (testFunctions.isDataSetAmongAssignedDataSetsOfDataStock(processUuid, processVersion, DataSetType.PROCESS, stockUuid, client, APIKey)) {
            Assert.fail(String.format(
                    "Process (uuid:'%s', version:'%s') should have been among unassigned data sets of stock '%s'.", processUuid, processVersion.getVersionString(), stockUuid));
        }

        if (isProcessAssignedAccordingToDB(processId, stockId)) {
            Assert.fail(String.format(
                    "Un-assignment of process (id='%s', version='%s') from data stock (uuid='%s') via API should have removed entry in cross reference table, but has not.", processUuid, processVersion.getVersionString(), stockUuid));
        }


    }

    @Test(priority = 110)
    public void unassign_previously_api_assigned_data_set_from_data_stock_via_ui() throws FormatException, MalformedURLException {
        log.info("Un-assign previously api assigned dataset via UI");
        // ARRANGE
        var APIKey = testFunctions.generateAPIKeyGET("admin", "default"); // super admin
        var stockUuid = UUID.fromString("8856867f-2e5d-42f5-9dfe-bca46d3c4ad1");
        var processUuid = UUID.fromString("0dc3d65b-7ff8-4c92-a694-748fb28070a9");
        var processVersion = DataSetVersion.parse("03.00.000");
        // We need this to verify the cross-reference within the database
        var stockId = getStockIdFromDBViaUuid(stockUuid);
        var processId = getProcessIdFromDBViaUuidAndVersion(processUuid, processVersion);

        assign_process_to_data_stock_via_api(APIKey, stockUuid, processUuid, processVersion, DependenciesMode.NONE);

        log.trace("Verifying that process (uuid='{}', version='{}', id={}) is among assigned data sets of data stock (uuid='{}', id={})...",
                processUuid, processVersion, processId, stockUuid, stockId);
        if (!testFunctions.isDataSetAmongAssignedDataSetsOfDataStock(processUuid, processVersion, DataSetType.PROCESS, stockUuid, client, APIKey)) {
            throw new IllegalStateException(String.format(
                    "Process (uuid:'%s', version:'%s') should have been among assigned data sets of stock '%s'.", processUuid, processVersion.getVersionString(), stockUuid));
        }

        if (!isProcessAssignedAccordingToDB(processId, stockId)) {
            throw new IllegalStateException(String.format(
                    "Assignment of process (id='%s', version='%s') to data stock (uuid='%s') should have produced entry in cross reference table, but has not.", processUuid, processVersion.getVersionString(), stockUuid));
        }

        // ACT
        log.info("Un-assigning process (uuid='{}', version='{}', id={}) to data stock (uuid='{}', id={}) via UI", processUuid, processVersion.getVersionString(), processId, stockUuid, stockId);
        testFunctions.login("admin", "default", true, true, 1, true);
        testFunctions.gotoAdminArea();
        testFunctions.waitUntilSiteLoaded();
        testFunctions.goToPageByAdminMenu("common.stock", "admin.stock.manageList");
        $x(".//*[text()='SimpleStock2']/../../td[8]/button[*]").click();
        testFunctions.waitUntilSiteLoaded();
        $x(".//*[text()='0dc3d65b-7ff8-4c92-a694-748fb28070a9']/../td[1]/div").click(); // click checkbox in dataset row.
        $x("//*[@id=\"stockTabs:dataSetTabView:ctProcessDataTablebtn\"]/span").click(); // click remove button.
        $x(".//*[@id='generalForm']/div[contains(@style,'display: block')]//button[contains(.,'OK')]").click(); // click OK in confirm dialog.

        // ASSERT
        testFunctions.waitUntilSiteLoaded();
        org.testng.Assert.assertTrue($x("//*[@id=\"stockTabs:dataSetTabView:ctProcessDataTable_data\"]/tr/td").getText().contains("No entries found")); // check if 'No entries found' is shown in the stock table

        log.trace("Verifying that process (uuid='{}', version='{}', id={}) is among assigned data sets of data stock (uuid='{}', id={})...",
                processUuid, processVersion, processId, stockUuid, stockId);
        if (testFunctions.isDataSetAmongAssignedDataSetsOfDataStock(processUuid, processVersion, DataSetType.PROCESS, stockUuid, client, APIKey)) {
            Assert.fail(String.format(
                    "Process (uuid:'%s', version:'%s') should have been among unassigned data sets of stock '%s'.", processUuid, processVersion.getVersionString(), stockUuid));
        }

        if (isProcessAssignedAccordingToDB(processId, stockId)) {
            Assert.fail(String.format(
                    "Un-assignment of process (id='%s', version='%s') from data stock (uuid='%s') via UI should have removed entry in cross reference table, but has not.", processUuid, processVersion.getVersionString(), stockUuid));
        }
    }

    @Test
    public void assert_publication_via_ui_assignment_works_as_expected() {
        // ARRANGE
        try {
            final var targetStockName = "SimpleStock1";
            final var targetStockUuid = UUID.fromString("8e771302-b68e-4ce3-ba04-cb0e2095dc0e");

            final var processUuid = UUID.fromString("3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
            final var processVersion = DataSetVersion.parse("03.00.000");

            final var dependencyContactUuid = UUID.fromString("16e9b2c6-3c44-11dd-ae16-0800200c9a66");
            final var dependencyContactVersion = DataSetVersion.parse("03.00.000");

            final var readingUser = "SimpleStock1_reader";
            final var readingUserAPIKey = testFunctions.generateAPIKeyGET(readingUser, readingUser);

            final var publishingUser = "admin";
            final var publishingUserPassword = "default";
            final var publishingUserAPIKey = testFunctions.generateAPIKeyGET(publishingUser, publishingUserPassword);

            final var unassignStatus = apiUnassignDatasetFromDataStock(publishingUserAPIKey, targetStockUuid, DataSetType.PROCESS, processUuid, processVersion, DependenciesMode.ALL);
            if (unassignStatus != 200) {

                throw new IllegalStateException("Failed to unassign dataset from target stock before assignment test");
            }
            if (401 != apiCheckDatasetAvailableForUser(readingUserAPIKey, DataSetType.PROCESS, processUuid, processVersion)) {

                throw new IllegalStateException("Unassigned dataset is (still) published to the user");
            }

            // ACT
            testFunctions.login(publishingUser, publishingUserPassword, true, true, 1, true);
            testFunctions.gotoAdminArea();
            testFunctions.waitUntilSiteLoaded();
            testFunctions.goToPageByAdminMenu("common.stock", "admin.stock.manageList");

            $x(".//*[text()=\"" + targetStockName + "\"]/../../td[8]/button[*]")
                    .click();

            testFunctions.waitUntilSiteLoaded();
            testFunctions.findAndWaitOnElement(By.id("stockTabs:dataSetTabView:assignDataSetBtnProcess"))
                    .click();

            $x(".//*[text()='3c0a1214-f4b2-4254-8fca-1d9e6ee9839f']/../td[1]/div")
                    .click();

            testFunctions.findAndWaitOnElement(By.id("stockTabs:dataSetTabView:adProcessDataTablebtn"))
                    .click();

            testFunctions.findAndWaitOnElement(By.xpath(
                    "//*[@id=\"stockTabs:dataSetTabView:adProcessDataTableattachSelectedToStockConfirmHeader\"]" +
                            "/legend[contains(concat(' ', normalize-space(@class), ' '), \"ui-fieldset-legend\")]"))
                    .click();

            testFunctions.findAndWaitOnElement(By.xpath(
                    "//*[@id=\"stockTabs:dataSetTabView:adProcessDataTableattachSelectedToStockConfirmHeader\"]" +
                            "//input[@value=\"" + DependenciesMode.ALL.name() + "\"]" +
                            "/../.."))
                    .click();

            testFunctions.findAndWaitOnElement(By.xpath(
                    "//span[@id=\"stockTabs:dataSetTabView:adProcessDataTableattachSelectedToStockpanel\"]" +
                            "/button"))
                    .click();

            testFunctions.findAndWaitOnElement(By.xpath(
                    "//span[contains(concat(' ', normalize-space(@class), ' '), \" ui-messages-info-summary \") and contains(text(), \"CEWEP\")]"));

            // ASSERT
            assertEquals("Dataset (generally) available to target stock read-authorized user (via API)", 200,
                    apiCheckDatasetAvailableForUser(publishingUserAPIKey, DataSetType.PROCESS, processUuid, processVersion));
            assertEquals("Dependency (generally) available to target stock read-authorized user (via API)", 200,
                    apiCheckDatasetAvailableForUser(publishingUserAPIKey, DataSetType.CONTACT, dependencyContactUuid, dependencyContactVersion));

            assertEquals("Dataset (generally) available to target stock read-authorized user (via API)", 200,
                    apiCheckDatasetAvailableForUser(readingUserAPIKey, DataSetType.PROCESS, processUuid, processVersion));
            assertEquals("Dependency (generally) available to target stock read-authorized user (via API)", 200,
                    apiCheckDatasetAvailableForUser(readingUserAPIKey, DataSetType.CONTACT, dependencyContactUuid, dependencyContactVersion));

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    @Test
    public void assert_retraction_via_ui_unassignment_works_as_expected() {
        try {
            // ARRANGE
            final var targetStockName = "SimpleStock1";
            final var targetStockUuid = UUID.fromString("8e771302-b68e-4ce3-ba04-cb0e2095dc0e");

            final var processUuid = UUID.fromString("3c0a1214-f4b2-4254-8fca-1d9e6ee9839f");
            final var processVersion = DataSetVersion.parse("03.00.000");

            final var dependencyContactUuid = UUID.fromString("16e9b2c6-3c44-11dd-ae16-0800200c9a66");
            final var dependencyContactVersion = DataSetVersion.parse("03.00.000");

            final var readingUser = "SimpleStock1_reader";
            final var readingUserAPIKey = testFunctions.generateAPIKeyGET(readingUser, readingUser);

            final var publishingUser = "admin";
            final var publishingUserPassword = "default";
            final var publishingUserAPIKey = testFunctions.generateAPIKeyGET(publishingUser, publishingUserPassword);

            if (200 != apiAssignDatasetToDataStock(publishingUserAPIKey, targetStockUuid, DataSetType.PROCESS, processUuid, processVersion, DependenciesMode.ALL)) {

                throw new IllegalStateException("Failed to assign dataset prior to retraction test");

            }
            if (200 != apiCheckDatasetAvailableForUser(readingUserAPIKey, DataSetType.PROCESS, processUuid, processVersion)) {

                throw new IllegalStateException("Assigned dataset not published to user");
            }

            // ACT
            testFunctions.login(publishingUser, publishingUserPassword, true, true, 1, true);
            testFunctions.gotoAdminArea();
            testFunctions.waitUntilSiteLoaded();
            testFunctions.goToPageByAdminMenu("common.stock", "admin.stock.manageList");

            $x(".//*[text()=\"" + targetStockName + "\"]/../../td[8]/button[*]")
                    .click();

            testFunctions.findAndWaitOnElement(By.xpath(
                    "//div[@id = 'stockTabs:dataSetTabView:ctProcessDataTable']" +
                            "//*[contains(@id, 'nameColumnContentUuid') and contains(text(), '3c0a1214-f4b2-4254-8fca-1d9e6ee9839f')]" +
                            "/ancestor::tr" +
                            "//td[contains(concat(' ', normalize-space(@class), ' '), ' ui-selection-column ')]" +
                            "//div[contains(concat(' ', normalize-space(@class), ' '), ' ui-chkbox')]"))
                    .click();

            testFunctions.findAndWaitOnElement(By.xpath(
                    "//button[@id = 'stockTabs:dataSetTabView:ctProcessDataTablebtn' and @aria-disabled = 'false']"))
                    .click();

            testFunctions.findAndWaitOnElement(By.xpath(
                    "//fieldset[@id = 'stockTabs:dataSetTabView:ctProcessDataTabledetachSelectedFromStockConfirmHeader']" +
                            "//legend[@role = 'button']"))
                    .click();

            testFunctions.findAndWaitOnElement(By.xpath("//fieldset[@id = 'stockTabs:dataSetTabView:ctProcessDataTabledetachSelectedFromStockConfirmHeader']" +
                    "//input[@type = 'radio' and @value = \"" + DependenciesMode.ALL.name() + "\"]" +
                    "//ancestor::div[contains(concat(' ', normalize-space(@class), ' '), 'ui-radiobutton')]")).click();

            testFunctions.findAndWaitOnElement(By.xpath("//span[@id = 'stockTabs:dataSetTabView:ctProcessDataTabledetachSelectedFromStockpanel']" +
                    "//button")).click();

            testFunctions.findAndWaitOnElement(By.xpath(
                    "//span[contains(concat(' ', normalize-space(@class), ' '), 'ui-messages-info-summary') and contains(text(), 'Waste incineration')]"));

            // ASSERT

            assertEquals("Dataset (generally) available to target stock read-authorized user (via API)", 401,
                    apiCheckDatasetAvailableForUser(readingUserAPIKey, DataSetType.PROCESS, processUuid, processVersion));
            assertEquals("Dependency (generally) available to target stock read-authorized user (via API)", 401,
                    apiCheckDatasetAvailableForUser(readingUserAPIKey, DataSetType.CONTACT, dependencyContactUuid, dependencyContactVersion));

        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
    }

    private int apiCheckDatasetAvailableForUser(String apiKey, DataSetType datasetType, UUID datasetUuid, DataSetVersion datasetVersion) {
        try {
            final var getDatasetRequestBuilder = client.target(new URL(TestContext.PRIMARY_SITE_URL + 
                            String.format("resource/%s/%s?version=%s&format=xml", datasetType.getStandardFolderName(), datasetUuid, datasetVersion)).toString())
                    .request()
                    .header(HEADER_NAME, BEARER_TOKEN_PREFIX + apiKey);

            try (final var getDatasetResponse = getDatasetRequestBuilder.get()) {

                return getDatasetResponse.getStatus();

            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private int apiCheckDatasetAvailableForUserInStock(String apiKey, UUID stockUuid, DataSetType datasetType, UUID datasetUuid, DataSetVersion datasetVersion) {
        try {
            final var getDatasetRequestBuilder = client.target(new URL(TestContext.PRIMARY_SITE_URL +
                            String.format("resource/datastocks/%s/%s/%s?version=%s", stockUuid, datasetType.getStandardFolderName(), datasetUuid, datasetVersion)).toString())
                    .request()
                    .header(HEADER_NAME, BEARER_TOKEN_PREFIX + apiKey);

            try (final var getDatasetResponse = getDatasetRequestBuilder.get()) {

                return getDatasetResponse.getStatus();

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int apiUnassignDatasetFromDataStock(String apiKey, UUID targetStockUuid, DataSetType datasetType, UUID datasetUuid, DataSetVersion datasetVersion, DependenciesMode dependenciesMode) {
        try {
            final var unassignDatasetRequestBuilder = client.target(new URL(TestContext.PRIMARY_SITE_URL +
                            "resource/datastocks/" + targetStockUuid + "/" + datasetType.getStandardFolderName() + "/" + datasetUuid + "?version=" + datasetVersion.getVersionString() + "&withDependencies=" + dependenciesMode.ordinal()).toString())
                    .request()
                    .header(HEADER_NAME, BEARER_TOKEN_PREFIX + apiKey);
            
            try (var assignmentResponse = unassignDatasetRequestBuilder.delete()) {

                return assignmentResponse.getStatus();

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int apiAssignDatasetToDataStock(String apiKey, UUID targetStockUuid, DataSetType dataSetType, UUID datasetUuid, DataSetVersion dataSetVersion, DependenciesMode dependenciesMode) {
        try {
            final var requestBuilder = client.target(new URL(TestContext.PRIMARY_SITE_URL + 
                            "resource/datastocks/" + targetStockUuid + "/" + dataSetType.getStandardFolderName() + "/" + datasetUuid + "?version=" + dataSetVersion.getVersionString() + "&withDependencies=" + dependenciesMode.ordinal()).toString())
                    .request()
                    .header(HEADER_NAME, BEARER_TOKEN_PREFIX + apiKey);

            try (var assignmentResponse = requestBuilder.put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE))) {

                return assignmentResponse.getStatus();
            }
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    private void assign_process_to_data_stock_via_api(String APIKey, UUID targetStockUuid, UUID processUuid, DataSetVersion processVersion, DependenciesMode depMode) throws MalformedURLException {
        var assignUnassignProcessUrl = new URL(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + targetStockUuid + "/processes/" + processUuid + "?version=" + processVersion.getVersionString() + "&withDependencies=" + depMode.ordinal());

        log.debug("Assigning process (uuid='{}', version='{}') to data stock (uuid='{}') via 'PUT {}'...", processUuid, processVersion.getVersionString(), targetStockUuid, assignUnassignProcessUrl);
        var assignment = client.target(assignUnassignProcessUrl.toString()).request()
                .header(HEADER_NAME, BEARER_TOKEN_PREFIX + APIKey)
                .put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), Response.class);

        int assignmentStatus = assignment.getStatus();
        String assignmentMessage = assignment.readEntity(String.class); // Fun-fact: the entity can only be retrieved once from the client response object
        log.trace("Response: {'status': '{}', 'message': '{}'}", assignmentStatus, assignmentMessage);

        log.debug("Verifying response ...");
        int expectedStatus = 200;
        if (assignmentStatus != expectedStatus) {
            throw new IllegalStateException(String.format(
                    "Unexpected response status when trying to assign process (uuid='%s', version='%s') to data stock '%s'" + System.getProperty("line.separator") +
                            "Request: 'PUT %s'" + System.getProperty("line.separator") +
                            "Expected status: '%d'" + System.getProperty("line.separator") +
                            "Actual status: '%d'" + System.getProperty("line.separator") +
                            "Response message: '%s'",
                    processUuid, processVersion.getVersionString(), targetStockUuid, assignUnassignProcessUrl, expectedStatus, assignmentStatus, assignmentMessage
            ));
        }
    }

    private boolean isProcessAssignedAccordingToDB(Long processId, Long stockId) {
        var queryAssignmentCrossReference = String.format("SELECT * FROM `datastock_process` WHERE `processes_ID`=%d AND `containingDataStocks_ID`=%d", processId, stockId);
        try {
            log.debug("Checking whether assignment cross reference for assigning process data set (id={}) to data stock (id={}) exists in db.", processId, stockId);
            log.trace("Query: '{}'", queryAssignmentCrossReference);
            Map<String, Object> singleQueryResult = jdbcTemplate.queryForMap(queryAssignmentCrossReference); // expected to throw EmptyResultDataAccessException

            log.trace("Found: " + singleQueryResult);

            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private Long getStockIdFromDBViaUuid(UUID stockUuid) {
        var queryStockId = String.format("SELECT `ID` FROM `datastock` WHERE `UUID`='%s'", stockUuid);
        log.debug("Determining stock id (using query '{}')...", queryStockId);
        var stockId = jdbcTemplate.queryForObject(queryStockId, Long.class);
        log.trace("Data stock id: " + stockId);
        return stockId;
    }

    private Long getProcessIdFromDBViaUuidAndVersion(UUID processUuid, DataSetVersion processVersion) {
        var queryProcessId = String.format("SELECT `ID` FROM `process` WHERE `UUID`='%s' AND `VERSION`=%d", processUuid, processVersion.getVersion());
        log.debug("Determining process id (using query '{}')...", queryProcessId);
        var processId = jdbcTemplate.queryForObject(queryProcessId, Long.class);
        log.trace("Process id: {}", processId);
        return processId;
    }

}
