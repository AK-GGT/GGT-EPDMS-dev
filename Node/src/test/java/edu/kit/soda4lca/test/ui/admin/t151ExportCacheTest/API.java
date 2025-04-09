package edu.kit.soda4lca.test.ui.admin.t151ExportCacheTest;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.main.TestContext;
import edu.kit.soda4lca.test.ui.main.TestFunctions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.Listeners;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

@Listeners({ScreenShooter.class})
public class API {

    private static final Logger log = LogManager.getLogger(API.class);

    private final WebDriver driver;
    private final TestFunctions testFunctions;

    public API(WebDriver driver) {
        this.driver = driver;
        this.testFunctions = new TestFunctions();
        this.testFunctions.setDriver(this.driver);
    }

    String generateSuperAdminJWT() {
        // ARRANGE
        final var username = "admin";
        final var password = "default";
        final var method = HttpMethod.GET;
        final var url = TestContext.PRIMARY_SITE_URL + String.format("resource/authenticate/getToken?userName=%s&password=%s", username, password);
        final var restTemplate = new RestTemplate();

        // ACT
        log.debug("Requesting fresh JWT token for user '{}' ...", username);
        log.trace("Request: {} {}", method, url);
        final var responseEntity = restTemplate.exchange(url, method, new HttpEntity<>(new HttpHeaders()), String.class);

        return responseEntity.getBody();
    }

    ResponseEntity<String> importZip(@Nonnull final Path zipPath,
                                     @Nonnull final Stock rootTarget,
                                     @Nonnull final String token) {
        try {
            final var url = TestContext.PRIMARY_SITE_URL + "resource/processes/";
            final var headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            final var body = new LinkedMultiValueMap<>();
            body.add("file", new InputStreamResource(new BufferedInputStream(new FileInputStream(zipPath.toFile()))));
            body.add("zipFileName", "sample.zip");
            body.add("stock", Objects.requireNonNull(rootTarget.getUuid()));
            final var entity = new HttpEntity<>(body, headers);
            final var restTemplate = new RestTemplate();

            log.debug("Requesting import of zip archive '{}' to {} ...", zipPath, rootTarget);
            log.trace("Request: {} {}", HttpMethod.POST, url);
            return restTemplate.postForEntity(url, entity, String.class);
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Failed to import zip file '%s' (target %s).", zipPath, rootTarget), t);
        }
    }

    ResponseEntity<String> assignProcess(@Nonnull final UUID processUUID,
                                         @Nonnull final Stock logicalTarget,
                                         @Nonnull final String token) {
        try {
            final var url = TestContext.PRIMARY_SITE_URL + String.format("resource/datastocks/%s/processes/%s", logicalTarget.getUuid(), processUUID);
            final var headers = new HttpHeaders();
            headers.setBearerAuth(token);
            final var method = HttpMethod.PUT;
            final var restTemplate = new RestTemplate();

            log.debug("Requesting assignment of process '{}' ...", processUUID);
            log.trace("Request: {} {}", method, url);
            return restTemplate.exchange(url, method, new HttpEntity<>(null, headers), String.class);
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Failed to assign process '%s' to logical %s", processUUID, logicalTarget), t);
        }
    }

    ResponseEntity<String> unassignProcess(@Nonnull final UUID processUUID,
                                           @Nonnull final Stock logicalSource,
                                           @Nonnull final String token) {
        try {
            final var url = TestContext.PRIMARY_SITE_URL + String.format("resource/datastocks/%s/processes/%s", logicalSource.getUuid(), processUUID);
            final var headers = new HttpHeaders();
            headers.setBearerAuth(token);
            final var method = HttpMethod.DELETE;
            final var restTemplate = new RestTemplate();

            log.debug("Requesting un-assignment of process '{}'  ...", processUUID);
            log.trace("Request: {} {}", method, url);
            return restTemplate.exchange(url, method, new HttpEntity<>(null, headers), String.class);
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Failed to assign process '%s' to logical %s", processUUID, logicalSource), t);
        }
    }


    ResponseEntity<String> requestAsCsv(@Nonnull final Stock stock,
                                        @Nonnull final DecimalSeparator decimalSeparator,
                                        @Nonnull final String token) {
        final var url = String.format(TestContext.PRIMARY_SITE_URL + "resource/datastocks/%s/exportCSV?decimalSeparator=%s", stock.getUuid(), decimalSeparator.getParameterValue());
        final var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        final var method = HttpMethod.GET;
        final var restTemplate = new RestTemplate();

        log.debug("Requesting {}-csv export of {} ...", decimalSeparator.getParameterValue(), stock);
        log.trace("Request: {} {}", method, url);
        return restTemplate.exchange(url, method, new HttpEntity<>(headers), String.class);
    }

    ResponseEntity<String> requestStockAsZip(@Nonnull final Stock stock,
                                             @Nonnull final String token) {
        final var url = String.format(TestContext.PRIMARY_SITE_URL + "resource/datastocks/%s/export", stock.getUuid());
        final var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        final var method = HttpMethod.GET;
        final var restTemplate = new RestTemplate();

        log.debug("Requesting zip export of {} ...", stock);
        log.trace("Request: {} {}", method, url);
        return restTemplate.exchange(url, method, new HttpEntity<>(headers), String.class);
    }

}
