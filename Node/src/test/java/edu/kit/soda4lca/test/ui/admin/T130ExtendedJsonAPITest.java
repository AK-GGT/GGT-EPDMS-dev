package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import com.fasterxml.jackson.databind.JsonNode;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

@Listeners({ScreenShooter.class})
public class T130ExtendedJsonAPITest extends AbstractUITest {

    private static final Logger logger = LogManager.getLogger(T130ExtendedJsonAPITest.class);
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String TEST_EPD_PROCESS_UUID = "da3bb9b5-ffef-4b54-8559-bb3a4b05af70";
    private static final String TEST_ILCD_PROCESS_UUID = "4a1ebe7c-6835-4a22-8b2e-3201f1cd32e8";
    private static final String TEST_EPD_PROCESS_VERSION = "00.03.000";
    private static final String TEST_ILCD_PROCESS_VERSION = "03.01.000";
    private static final String EXTENDED_JSON_URL_FOR_EPD_DATA_SET = TestContext.PRIMARY_SITE_URL +
            "resource/processes/" + TEST_EPD_PROCESS_UUID +
            "?version=" + TEST_EPD_PROCESS_VERSION +
            "&format=json&view=extended";
    private static final String EXTENDED_JSON_URL_FOR_LCIA_DATA_SET = TestContext.PRIMARY_SITE_URL +
            "resource/processes/" + TEST_ILCD_PROCESS_UUID +
            "?version=" + TEST_ILCD_PROCESS_VERSION +
            "&format=json&view=extended";
    private RestTemplate restTemplate;
    private HttpHeaders headers;

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return List.of(List.of("DB_pre_T130ExtendedJsonAPITest.xml"));
    }

    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        logger.info("Begin of ExtendedJsonAPI test");
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout(15000);
        httpRequestFactory.setReadTimeout(10000);
        restTemplate = new RestTemplate(httpRequestFactory);
        ResponseEntity<String> token = T150JWTTokenTest._requestFrestJWT();
        headers = new HttpHeaders();
        headers.setBearerAuth(Objects.requireNonNull(token.getBody()));

    }

    @Test(priority = 1)
    public void assertEPDExtendedJSONViewContainsCorrect_referencesToOriginalEPDs_materialProperties() {
        // Arrange
        logger.info("Checking whether extended json view exists. Calling URL {}", EXTENDED_JSON_URL_FOR_EPD_DATA_SET);
        final int expectedStatus = 200;
        final String url = EXTENDED_JSON_URL_FOR_EPD_DATA_SET;
        int receivedStatus;
        ResponseEntity<JsonNode> response;
        // Act
        JsonNode jsonRootNode = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            jsonRootNode = response.getBody();
            if (jsonRootNode == null)
                throw new IllegalStateException(String.format("Requested json view of test data set is null. URL: '%s'", url)); // No need to catch this one.
            receivedStatus = response.getStatusCodeValue();
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            receivedStatus = e.getRawStatusCode();
        }
        assert jsonRootNode != null;

        // Assert
        Assert.assertEquals(receivedStatus, expectedStatus, String.format("Wrong status in response to calling GET on '%s'", url));
        assertMaterialPropertiesAreCompleteAndCorrect(jsonRootNode);
        assertUrlForReferenceToOriginalEPDsAreResolved(jsonRootNode);
        logger.info("All good.");
    }

    public void assertUrlForReferenceToOriginalEPDsAreResolved(JsonNode jsonRoot) {
        // Arrange
        logger.info("Checking whether there are resolved URLs for referenced to original EPDs...");
        boolean errorDetected = false;
        boolean successDetected = false;
        // Act
        JsonNode anies = jsonRoot.at("/modellingAndValidation/dataSourcesTreatmentAndRepresentativeness/other/anies");
        for (JsonNode node : anies) {
            boolean isReferenceToOriginalEPD = false;
            try {
                isReferenceToOriginalEPD = node.get("name").textValue().toLowerCase(Locale.ROOT).equals("referenceToOriginalEPD".toLowerCase(Locale.ROOT));
            } catch (Exception e) {
                // Not a reference to original EPD => leave isReferenceToOriginalEPD at false
            }
            if (!isReferenceToOriginalEPD)
                continue;

            // Assert
            JsonNode resourceURLs;
            String tmpUrlTextValue = null;
            try {
                resourceURLs = node.at("/value/resourceURLs");
                for (JsonNode urlNode : resourceURLs) {
                    tmpUrlTextValue = urlNode.textValue();
                    new URL(tmpUrlTextValue).toURI().parseServerAuthority(); // Validate url, (parseServerAuthority will fail on URNs)
                }
            } catch (Exception e) {
                String problem;
                if (e instanceof NullPointerException)
                    problem = "Missing resourceURLs field or malformed json element inside resourceURLs (text value is null)";
                else if (e instanceof MalformedURLException || e instanceof URISyntaxException)
                    problem = String.format("Malformed resource url '%s'", tmpUrlTextValue);
                else
                    problem = "Unknown error";
                logger.error(String.format("%s when validation resource urls", problem), e);
                errorDetected = true;
                break;
            }
            if (resourceURLs.size() < 1)
                break;

            successDetected = true;
        }
        Assert.assertTrue(successDetected, "There are resolved URLs pointing to original EPDs");
        Assert.assertFalse(errorDetected, "There are references to an original EPD data sets that have malformed or missing resource URLs");
        logger.info("All good.");
    }

    public void assertMaterialPropertiesAreCompleteAndCorrect(JsonNode jsonRoot) {
        // Arrange
        final int testExchangeInternalID = 0;
        final int expectedMaterialPropertiesCount = 2;
        logger.info(String.format("Checking materialProperties for specific exchange (dataSetInternalID=%s)", testExchangeInternalID));
        // Act
        for (JsonNode exchangeNode : jsonRoot.at("/exchanges/exchange")) {
            int internalId = exchangeNode.get("dataSetInternalID").intValue();
            if (testExchangeInternalID == internalId) {
                JsonNode materialProperties;
                try {
                    materialProperties = exchangeNode.at("/materialProperties");
                    JsonNode flowReference = exchangeNode.get("referenceToFlowDataSet");
                    // Assert
                    Assert.assertNotNull(flowReference, String.format("Reference to flow should exist (dataSetInternalID=%s)", testExchangeInternalID));
                    Assert.assertNotNull(materialProperties, String.format("MaterialProperties should exist (dataSetInternalID=%s)", testExchangeInternalID));
                    Assert.assertEquals(materialProperties.size(), 2,
                            String.format("There should be exactly %d material properties (dataSetInternalID=%d)", expectedMaterialPropertiesCount, testExchangeInternalID));

                    validateMaterialPropertiesStructure(materialProperties);
                    validateMaterialPropertiesContent(materialProperties);
                } catch (Exception e) {
                    Assert.fail(String.format("Failed to obtain any material properties (dataSetInternalID=%d).", testExchangeInternalID), e);
                }
            }
        }
        logger.info("All good.");
    }

    private void validateMaterialPropertiesStructure(@Nonnull JsonNode materialProperties) {
        String propertyName = null;
        logger.info("Checking overall structure for each of the (flattened) material properties...");
        // Act
        for (JsonNode prop : materialProperties) {
            try {
                propertyName = prop.get("name").textValue();
                // Assert
                Assert.assertNotNull(propertyName, "Material properties should each have a name field");
                Assert.assertNotNull(prop.get("value"), String.format("Material property %s should have a value field", propertyName));
                Assert.assertNotNull(prop.get("unit"), String.format("Material property %s should have a unit field", propertyName));
                Assert.assertNotNull(prop.get("unitDescription"), String.format("Material property %s should have a unitDesctiption field", propertyName));
            } catch (Exception e) {
                propertyName = propertyName != null ? "'" + propertyName + "'" : "";
                Assert.fail(String.format("Failed to analyze structure of material property %s", propertyName), e);
            }
        }
    }

    private void validateMaterialPropertiesContent(JsonNode materialProperties) {
        String name, value, unit, unitDescription;
        // Arrange
        logger.info("Checking the content of the material properties...");
        Map<String, FlattenedMaterialProperty> expectationsByName = new HashMap<>();
        FlattenedMaterialProperty fmp = new FlattenedMaterialProperty("conversion factor to 1 kg", "0.0015", "-", "Without unit");
        expectationsByName.put(fmp.name, fmp);
        fmp = new FlattenedMaterialProperty("gross density", "660.0", "kg/m^3", "kilograms per cubic metre");
        expectationsByName.put(fmp.name, fmp);
        // Act
        for (JsonNode prop : materialProperties) {
            name = prop.get("name").textValue();
            value = prop.get("value").textValue();
            unit = prop.get("unit").textValue();
            unitDescription = prop.get("unitDescription").textValue();
            // Assert
            Assert.assertTrue(expectationsByName.containsKey(name), String.format("Retrieved name '%s' is part of expectations %s", name, expectationsByName.keySet()));
            Assert.assertEquals(value, expectationsByName.get(name).value, String.format("Retrieved value of material property '%s' should match expectation", name));
            Assert.assertEquals(unit, expectationsByName.get(name).unit, String.format("Retrieved unit of material property '%s' should match expectation", name));
            Assert.assertEquals(unitDescription, expectationsByName.get(name).unitDescription, String.format("Retrieved value of material property '%s' should match expectation", name));
        }
    }

    @Test(priority = 2)
    public void assertEPDExtendedJSONViewContainsCorrect_flowProperties_typeOfFlow_resultingflowAmount_referenceFlow() {
        final var testExchangeInternalID = 0;
        final var expectedFlowPropertiesCount = 1;
        final var expectedMeanValueOfFlow = 1.0;
        final var expectedLanguageOfFlowProperty = "de";
        final var expectedTypeOfFlow = "Product flow";
        final var expectedResultingFlowAmount = 1.0;
        final var expectedUuidOfFlow = "93a60a56-a3c8-22da-a746-0800200c9a66";
        final var expectedUnitNameOfFlow = "Kubikmeter";
        final var expectedReferenceFlowProperty = true;
        HashMap<String, String> m = new HashMap<>();
        m.put("lang", expectedLanguageOfFlowProperty);
        m.put("value", expectedUnitNameOfFlow);
        FlattenedFlowProperties expected = new FlattenedFlowProperties(List.of(m), expectedUuidOfFlow, expectedReferenceFlowProperty, expectedMeanValueOfFlow);

        // Act
        JsonNode epdJsonRootNode;
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(EXTENDED_JSON_URL_FOR_EPD_DATA_SET, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            epdJsonRootNode = response.getBody();
            if (epdJsonRootNode == null)
                throw new IllegalStateException(String.format("Requested json view of test data set is null. URL: '%s'", EXTENDED_JSON_URL_FOR_EPD_DATA_SET)); // No need to catch this one.
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            int expectedStatus = 200;
            int actualStatus = e.getRawStatusCode();
            throw new IllegalStateException(String.format(
                    "Unexpected response status when trying to call GET on '%s'" + LINE_SEPARATOR +
                            "\tExpected status: '%d'" + LINE_SEPARATOR +
                            "\tActual status: '%d'",
                    EXTENDED_JSON_URL_FOR_EPD_DATA_SET, expectedStatus, actualStatus));
        }

        // Assert
        logger.info(String.format("Checking typeOfFlow, referenceFlow, resultingflowAmount and flowProperties for specific exchange (dataSetInternalID=%s)", testExchangeInternalID));
        JsonNode exchangeNode = getExchangeByInternalId(testExchangeInternalID, epdJsonRootNode);
        try {
            JsonNode typeOfFlow = exchangeNode.get("typeOfFlow");
            JsonNode referenceFlow = exchangeNode.get("referenceFlow");
            JsonNode resultingFlowAmount = exchangeNode.get("resultingflowAmount");
            Assert.assertNotNull(typeOfFlow, String.format("typeOfFlow should exist (dataSetInternalID=%s)", testExchangeInternalID));
            Assert.assertEquals(typeOfFlow.textValue(), expectedTypeOfFlow,
                    String.format("Retrieved value of type of Flow '%s' should match expectation", expectedTypeOfFlow));
            Assert.assertTrue(referenceFlow.booleanValue(),
                    String.format("There is no reference to flow but there should be (dataSetInternalID=%s)", testExchangeInternalID));
            Assert.assertEquals(resultingFlowAmount.doubleValue(), expectedResultingFlowAmount,
                    String.format("Retrieved value of resultingflowAmount '%s' should match expectation", expectedResultingFlowAmount));

            JsonNode flowProperties = exchangeNode.get("flowProperties");
            Assert.assertNotNull(flowProperties, String.format("flowProperties should exist (dataSetInternalID=%s)", testExchangeInternalID));
            Assert.assertEquals(flowProperties.size(), expectedFlowPropertiesCount,
                    String.format("There should be exactly %d flow properties (dataSetInternalID=%d)", expectedFlowPropertiesCount, testExchangeInternalID));
            for (JsonNode prop : flowProperties) {
                String currUuid = prop.get("uuid").asText();
                String uuid = "93a60a56-a3c8-22da-a746-0800200c9a66";
                if (Objects.equals(currUuid, uuid))
                    assertFlowPropertiesContentAsExpected(prop, expected);
            }
        } catch (Exception e) {
            Assert.fail(String.format("Failed to obtain some fields of exchange (dataSetInternalID=%d).", testExchangeInternalID), e);
        }
        logger.info("All good.");
    }

    @Test(priority = 2)
    public void assertILCDExtendedJSONViewContainsCorrect_flowProperties_typeOfFlow_resultingflowAmount_referenceFlow_classification_location() {
        // Arrange
        final var url = EXTENDED_JSON_URL_FOR_LCIA_DATA_SET;
        final var testExchangeInternalID = 463;
        final var textExchangeInternalIDWithLocation = 54;
        final var expectedLocationOfFlow = "SY";
        final var expectedTypeOfFlow = "Product flow";
        final var expectedResultingFlowAmount = 1000.0;
        final var expectedLanguageOfFlowPropertyDE = "de";
        final var expectedLanguageOfFlowPropertyEN = "en";
        final var expectedUnitNameOfFlowDE = "Masse";
        final var expectedUnitNameOfFlowEN = "Mass";
        final var expectedUuidOfFlow = "93a60a56-a3c8-11da-a746-0800200b9a66";
        final var expectedUnitGroupUUIDOfFlow = "93a60a57-a4c8-11da-a746-0800200c9a66";
        final var expectedMeanValueOfFlow = 1000.0;
        final var expectedReferenceUnit = "kg";
        final var expectedReferenceFlowProperty = true;
        HashMap<String, String> flowPropertyNameDE = new HashMap<>();
        HashMap<String, String> flowPropertyNameEN = new HashMap<>();
        flowPropertyNameEN.put("lang", expectedLanguageOfFlowPropertyEN);
        flowPropertyNameEN.put("value", expectedUnitNameOfFlowEN);
        flowPropertyNameDE.put("lang", expectedLanguageOfFlowPropertyDE);
        flowPropertyNameDE.put("value", expectedUnitNameOfFlowDE);
        FlattenedFlowProperties expected = new FlattenedFlowProperties(Arrays.asList(flowPropertyNameEN, flowPropertyNameDE),
                expectedUuidOfFlow, expectedReferenceFlowProperty, expectedMeanValueOfFlow, expectedReferenceUnit, expectedUnitGroupUUIDOfFlow);

        // Act
        JsonNode jsonRootNode = null;
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            jsonRootNode = response.getBody();
            if (jsonRootNode == null)
                throw new IllegalStateException(String.format("Requested json view of test data set is null. URL: '%s'", url)); // No need to catch this one.
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            logger.error(String.format("Request to obtain extended json view failed. URL: '%s'", url));
        }
        assert jsonRootNode != null;

        // Assert
        logger.info(String.format("Checking location for specific exchange (dataSetInternalID=%s) in extended ILCD json view", textExchangeInternalIDWithLocation));
        JsonNode exchangeNodeWithLocation = getExchangeByInternalId(textExchangeInternalIDWithLocation, jsonRootNode);
        try {
            JsonNode location = exchangeNodeWithLocation.get("location");
            Assert.assertNotNull(location, String.format("location should exist (dataSetInternalID=%s)", textExchangeInternalIDWithLocation));
            Assert.assertEquals(location.textValue(), expectedLocationOfFlow,
                    String.format("Retrieved value of location of flow '%s' should match expectation", expectedLocationOfFlow));
        } catch (Exception e) {
            Assert.fail(String.format("Failed to obtain location. (dataSetInternalID=%d).", textExchangeInternalIDWithLocation), e);
        }

        logger.info(String.format("Checking flowProperties, typeOfFlow, resultingflowAmount and " +
                "classification for specific exchange (dataSetInternalID=%s) in extended ILCD json view", testExchangeInternalID));
        JsonNode exchangeNode = getExchangeByInternalId(testExchangeInternalID, jsonRootNode);
        try {
            JsonNode typeOfFlow = exchangeNode.get("typeOfFlow");
            JsonNode resultingFlowAmount = exchangeNode.get("resultingflowAmount");
            JsonNode referenceFlow = exchangeNode.get("referenceFlow");
            Assert.assertNotNull(typeOfFlow, String.format("typeOfFlow should exist (dataSetInternalID=%s)", testExchangeInternalID));
            Assert.assertEquals(typeOfFlow.textValue(), expectedTypeOfFlow,
                    String.format("typeOfFlow should exist (dataSetInternalID=%s)", testExchangeInternalID));
            Assert.assertEquals(resultingFlowAmount.doubleValue(), expectedResultingFlowAmount,
                    String.format("Retrieved value of resultingflowAmount '%s' should match expectation", expectedResultingFlowAmount));
            Assert.assertTrue(referenceFlow.booleanValue(),
                    String.format("There is no reference to flow but there should be (dataSetInternalID=%s)", testExchangeInternalID));

            JsonNode classification = exchangeNode.get("classification");
            assertClassificationContentAndStructureAsExpected(classification);

            JsonNode flowProperties = exchangeNode.get("flowProperties");
            for (JsonNode prop : flowProperties) {
                assertFlowPropertiesStructureAsExpected(prop);
                String currUuid = prop.get("uuid").asText();
                String uuid = "93a60a56-a3c8-11da-a746-0800200b9a66";
                if (Objects.equals(currUuid, uuid))
                    assertFlowPropertiesContentAsExpected(prop, expected);
            }

        } catch (Exception e) {
            Assert.fail(String.format("Failed to obtain some fields of exchange (dataSetInternalID=%d).", testExchangeInternalID), e);
        }
        logger.info("All good.");
    }

    private void assertClassificationContentAndStructureAsExpected(@Nonnull JsonNode classification) {
        // Arrange
        final var expectedClassHierarchy = "Materials production / Other mineralic materials";
        final var expectedClassificationName = "ilcd";
        // Assert
        Assert.assertNotNull(classification.get("name"), "name in classification should exist");
        Assert.assertNotNull(classification.get("classHierarchy"), "classHierarchy in classification should exist");
        Assert.assertEquals(classification.get("classHierarchy").textValue(), expectedClassHierarchy,
                String.format("Retrieved value of classHierarchy '%s' should match expectation", expectedClassHierarchy));
        Assert.assertEquals(classification.get("name").textValue(), expectedClassificationName,
                String.format("Retrieved value of classification name '%s' should match expectation", expectedClassificationName));
    }

    private void assertFlowPropertiesStructureAsExpected(@Nonnull JsonNode flowProperty) {
        logger.info("Checking the structure of the Flow properties...");
        Assert.assertNotNull(flowProperty, "flowProperties should exist");
        Assert.assertNotEquals(flowProperty.size(), 0, "flow properties count should be greater than Zero");
        Assert.assertNotNull(flowProperty.get("name"), "name in flowProperty should be exist");
        Assert.assertNotEquals(flowProperty.get("name").size(), 0, "name in flowProperty count should be greater than Zero");
        Assert.assertNotNull(flowProperty.get("uuid"), "uuid in flowProperty should be exist");
        Assert.assertNotNull(flowProperty.get("referenceFlowProperty"), "referenceFlowProperty in flowProperty should be exist");
        Assert.assertNotNull(flowProperty.get("meanValue"), "meanValue in flowProperty should be exist");
    }

    private void assertFlowPropertiesContentAsExpected(JsonNode flowPropertyActual, FlattenedFlowProperties expected) {
        logger.info("Checking the content of the Flow properties...");
        // Assert
        JsonNode name = flowPropertyActual.get("name");
        assertNameFieldAsExpected(expected.name, name);

        Assert.assertEquals(flowPropertyActual.get("uuid").textValue(), expected.uuid,
                String.format("Retrieved value of Uuid of Flow '%s' should match expectation", expected.uuid));
        Assert.assertEquals(flowPropertyActual.get("meanValue").doubleValue(), expected.meanValue,
                String.format("Retrieved value of meanValue of flow '%s' should match expectation", expected.meanValue));
        Assert.assertEquals(flowPropertyActual.get("referenceFlowProperty").booleanValue(), expected.referenceFlowProperty,
                String.format("There are no references to flow Property but there should be (referenceFlowProperty=%s)", expected.referenceFlowProperty));

        if (expected.unitGroupUUID != null)
            Assert.assertEquals(flowPropertyActual.get("unitGroupUUID").textValue(), expected.unitGroupUUID,
                    String.format("Retrieved value of unitGroupUUID of Flow '%s' should match expectation", expected.unitGroupUUID));

        if (expected.referenceUnit != null)
            Assert.assertEquals(flowPropertyActual.get("referenceUnit").textValue(), expected.referenceUnit,
                    String.format("Retrieved value of referenceUnit of flow '%s' should match expectation", expected.referenceUnit));
    }

    private JsonNode getExchangeByInternalId(int internalId,
                                             @Nonnull JsonNode jsonRootNode) {
        logger.trace("find exchange node with dataSetInternalID {} in the extended json response within exchanges node.", internalId);
        for (JsonNode exchangeNode : jsonRootNode.at("/exchanges/exchange")) {
            int currInternalId = exchangeNode.get("dataSetInternalID").intValue();
            if (currInternalId == internalId)
                return exchangeNode;
        }
        throw new NotFoundException(String.format("Exchange with dataSetInternalID '%d' can't be found.", internalId));
    }

    private void assertNameFieldAsExpected(@Nonnull final List<Map<String, String>> expected, @Nonnull final JsonNode actual) {
        Assert.assertEquals(actual.size(), expected.size(),
                String.format("Size of name node does not match expectation. %sActual: '%s' %sExpected: '%s'", LINE_SEPARATOR, actual, LINE_SEPARATOR, expected));
        for (Map<String, String> localizedName : expected)
            assertNameFieldContainsLangNamePair(localizedName.get("lang"), localizedName.get("value"), actual);
    }

    private void assertNameFieldContainsLangNamePair(@Nonnull final String lang,
                                                     @Nonnull final String name,
                                                     @Nonnull final JsonNode nameNode) {
        for (int i = 0; i < nameNode.size(); i++) {
            final var localizedName = nameNode.get(i);
            final var langValue = localizedName.get("lang").textValue();
            final var nameValue = localizedName.get("value").textValue();
            if (lang.equals(langValue) && name.equals(nameValue))
                return;
        }
        Assert.fail(String.format("Expected localized name {'lang': '%s', 'name': '%s'} not contained in name field: '%s'", lang, name, nameNode));
    }

    private static final class FlattenedMaterialProperty {
        final String name;
        final String value;
        final String unit;
        final String unitDescription;

        public FlattenedMaterialProperty(String name, String value, String unit, String unitDescription) {
            this.name = name;
            this.value = value;
            this.unit = unit;
            this.unitDescription = unitDescription;
        }
    }

    private static final class FlattenedFlowProperties {
        final List<Map<String, String>> name;
        final String uuid;
        final boolean referenceFlowProperty;
        final double meanValue;
        final String referenceUnit;
        final String unitGroupUUID;

        public FlattenedFlowProperties(List<Map<String, String>> name,
                                       String uuid,
                                       boolean referenceFlowProperty,
                                       double meanValue) {
            this(name, uuid, referenceFlowProperty, meanValue, null, null);
        }

        public FlattenedFlowProperties(List<Map<String, String>> name,
                                       String uuid,
                                       boolean referenceFlowProperty,
                                       double meanValue,
                                       String referenceUnit,
                                       String unitGroupUUID) {
            this.name = name;
            this.uuid = uuid;
            this.referenceFlowProperty = referenceFlowProperty;
            this.meanValue = meanValue;
            this.referenceUnit = referenceUnit;
            this.unitGroupUUID = unitGroupUUID;
        }
    }
}
