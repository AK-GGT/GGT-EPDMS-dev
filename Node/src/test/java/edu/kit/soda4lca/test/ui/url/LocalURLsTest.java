package edu.kit.soda4lca.test.ui.url;

import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.util.url.LocalURLs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LocalURLsTest {

    private static final Logger logger = LogManager.getLogger(LocalURLsTest.class);

    private static UUID VALID_UUID;
    private static LocalURLs.FORMAT_KEY VALID_FORMAT;
    private static DataSetVersion VALID_VERSION;
    private static DataSetType VALID_TYPE;
    private static URL VALID_BASE_URL;
    private static URL VALID_BASE_URL_WITHOUT_TRAILING_SLASH;
    private static String VALID_RESOURCE_FILE_NAME;
    private static String EXPECTED_DATA_SET_DETAILS_RESULT;
    private static String EXPECTED_DATA_SET_DETAILS_RESULT_WITHOUT_VERSION;
    private static String EXPECTED_RESOURCE_FILE_RESULT;
    private static String EXPECTED_RESOURCE_FILE_RESULT_WITHOUT_VERSION;

    @Before
    public void setUp() throws Exception {
        // Inputs (data set related)
        VALID_UUID = UUID.fromString("c124d1b8-7017-475a-a8c0-5cadc85da497");
        VALID_VERSION = DataSetVersion.parse("00.00.001");
        VALID_TYPE = DataSetType.PROCESS;
        VALID_RESOURCE_FILE_NAME = "shiny.pdf";
        // Inputs (URL related)
        VALID_FORMAT = LocalURLs.FORMAT_KEY.JSON;
        String baseUrlWithoutTrailingSlash = "https://localhost:8080/Node";
        VALID_BASE_URL_WITHOUT_TRAILING_SLASH = new URL(baseUrlWithoutTrailingSlash);
        VALID_BASE_URL = new URL(baseUrlWithoutTrailingSlash + "/");
        // Expectations
        EXPECTED_DATA_SET_DETAILS_RESULT = "https://localhost:8080/Node/resource/processes/c124d1b8-7017-475a-a8c0-5cadc85da497?version=00.00.001&amp;format=json";
        EXPECTED_DATA_SET_DETAILS_RESULT_WITHOUT_VERSION = "https://localhost:8080/Node/resource/processes/c124d1b8-7017-475a-a8c0-5cadc85da497?format=json";
        EXPECTED_RESOURCE_FILE_RESULT = "https://localhost:8080/Node/resource/sources/c124d1b8-7017-475a-a8c0-5cadc85da497/shiny.pdf?version=00.00.001";
        EXPECTED_RESOURCE_FILE_RESULT_WITHOUT_VERSION = "https://localhost:8080/Node/resource/sources/c124d1b8-7017-475a-a8c0-5cadc85da497/shiny.pdf";
    }

    @Test
    public void getDateSetDetailsURL() {
        // Act
        URL resultAllParametersPerfect = LocalURLs.getDateSetDetailsURL(VALID_BASE_URL, () -> VALID_TYPE, () -> VALID_UUID, () -> VALID_VERSION, VALID_FORMAT);
        URL resultTrailingSlashOfBaseUrlMissing = LocalURLs.getDateSetDetailsURL(VALID_BASE_URL_WITHOUT_TRAILING_SLASH, () -> VALID_TYPE, () -> VALID_UUID, () -> VALID_VERSION, VALID_FORMAT);
        // Assert
        Assert.assertEquals("Obtaining details, all parameters perfect", EXPECTED_DATA_SET_DETAILS_RESULT, resultAllParametersPerfect.toString());
        Assert.assertEquals("Obtaining details, base url without trailing slash", EXPECTED_DATA_SET_DETAILS_RESULT, resultTrailingSlashOfBaseUrlMissing.toString());
        try {
            resultAllParametersPerfect.toURI().parseServerAuthority();
        } catch (URISyntaxException e) {
            logger.error(e);
            Assert.fail(String.format("Malformed result (all params where perfect) %s", resultAllParametersPerfect));
        }
        try {
            resultTrailingSlashOfBaseUrlMissing.toURI().parseServerAuthority();
        } catch (URISyntaxException e) {
            logger.error(e);
            Assert.fail(String.format("Expected valid URL (all params perfect but base url without trailing slash), but found malformed result '%s'", resultTrailingSlashOfBaseUrlMissing));
        }
    }

    @Test
    public void getResourceFileURL() {
        // Act
        URL resultAllParametersPerfect = LocalURLs.getResourceFileURL(VALID_BASE_URL, () -> VALID_UUID, () -> VALID_VERSION, () -> VALID_RESOURCE_FILE_NAME);
        URL resultTrailingSlashOfBaseUrlMissing = LocalURLs.getResourceFileURL(VALID_BASE_URL_WITHOUT_TRAILING_SLASH, () -> VALID_UUID, () -> VALID_VERSION, () -> VALID_RESOURCE_FILE_NAME);
        // Assert
        Assert.assertEquals("Obtaining resource file, all parameters perfect", EXPECTED_RESOURCE_FILE_RESULT, resultAllParametersPerfect.toString());
        Assert.assertEquals("Obtaining resource file, base url without trailing slash", EXPECTED_RESOURCE_FILE_RESULT, resultTrailingSlashOfBaseUrlMissing.toString());
    }

    @Test
    public void getResourceFileURLWithBadVersion() {
        // Arrange
        Function<Supplier<DataSetVersion>, URL> method = version -> LocalURLs.getResourceFileURL(VALID_BASE_URL, () -> VALID_UUID, version, () -> VALID_RESOURCE_FILE_NAME);
        final Supplier<DataSetVersion> brokenVersion = () -> {
            throw new RuntimeException("BANG");
        };
        // Act
        URL resultWithoutVersion = method.apply(null);
        URL resultWithBrokenVersion = method.apply(brokenVersion);
        // Assert
        Assert.assertEquals("Obtaining resource file url (version missing)", EXPECTED_RESOURCE_FILE_RESULT_WITHOUT_VERSION, resultWithoutVersion.toString());
        Assert.assertEquals("Obtaining resource file (version broken)", EXPECTED_RESOURCE_FILE_RESULT_WITHOUT_VERSION, resultWithBrokenVersion.toString());
    }

    @Test
    public void getResourceFileURLWithBadFileName() {
        // Arrange
        Consumer<Supplier<String>> method = fileName -> LocalURLs.getResourceFileURL(VALID_BASE_URL, () -> VALID_UUID, () -> VALID_VERSION, fileName);
        final Supplier<String> brokenFileName = () -> {
            throw new RuntimeException("BANG");
        };
        // Act + Assert
        Assert.assertThrows(IllegalArgumentException.class, () -> method.accept(null));
        Assert.assertThrows(IllegalArgumentException.class, () -> method.accept(brokenFileName));
    }

    @Test
    public void getResourceFileURLWithBadUuid() {
        // Arrange
        Consumer<Supplier<UUID>> method = uuid -> LocalURLs.getResourceFileURL(VALID_BASE_URL, uuid, () -> VALID_VERSION, () -> VALID_RESOURCE_FILE_NAME);
        final Supplier<UUID> brokenUuid = () -> {
            throw new RuntimeException("BANG");
        };
        // Act + Assert
        Assert.assertThrows(IllegalArgumentException.class, () -> method.accept(null));
        Assert.assertThrows(IllegalArgumentException.class, () -> method.accept(brokenUuid));
    }

    @Test
    public void getDateSetDetailsURLWithBadVersion() {
        // Arrange
        Function<Supplier<DataSetVersion>, URL> method = version -> LocalURLs.getDateSetDetailsURL(VALID_BASE_URL, () -> VALID_TYPE, () -> VALID_UUID, version, VALID_FORMAT);
        final Supplier<DataSetVersion> brokenVersion = () -> {
            throw new RuntimeException("BANG");
        };
        // Act
        String urlWithNullVersion = method.apply(null).toString();
        String urlWithBrokenVersion = method.apply(brokenVersion).toString();
        // Assert
        Assert.assertEquals("Obtaining details url (version missing)", EXPECTED_DATA_SET_DETAILS_RESULT_WITHOUT_VERSION, urlWithNullVersion);
        Assert.assertEquals("Obtaining details (version broken)", EXPECTED_DATA_SET_DETAILS_RESULT_WITHOUT_VERSION, urlWithBrokenVersion);
    }

    @Test
    public void getDateSetDetailsURLWithoutFormat() {
        // Arrange
        Consumer<LocalURLs.FORMAT_KEY> method = format -> LocalURLs.getDateSetDetailsURL(VALID_BASE_URL, () -> VALID_TYPE, () -> VALID_UUID, () -> VALID_VERSION, format);
        // Act + Assert
        Assert.assertThrows(IllegalArgumentException.class, () -> method.accept(null));
    }

    @Test
    public void getDateSetDetailsURLWithBadType() {
        // Given
        Consumer<Supplier<DataSetType>> method = type -> LocalURLs.getDateSetDetailsURL(VALID_BASE_URL, type, () -> VALID_UUID, () -> VALID_VERSION, VALID_FORMAT);
        final Supplier<DataSetType> brokenType = () -> {
            throw new RuntimeException("BANG");
        };
        // Act + Assert
        Assert.assertThrows(IllegalArgumentException.class, () -> method.accept(null));
        Assert.assertThrows(IllegalArgumentException.class, () -> method.accept(brokenType));
    }

    @Test
    public void getDateSetDetailsURLWithBadUuid() {
        // Given
        Consumer<Supplier<UUID>> method = uuid -> LocalURLs.getDateSetDetailsURL(VALID_BASE_URL, () -> VALID_TYPE, uuid, () -> VALID_VERSION, VALID_FORMAT);
        final Supplier<UUID> brokenUuid = () -> {
            throw new RuntimeException("BANG");
        };
        // Act + Assert
        Assert.assertThrows(IllegalArgumentException.class, () -> method.accept(null));
        Assert.assertThrows(IllegalArgumentException.class, () -> method.accept(brokenUuid));
    }

}