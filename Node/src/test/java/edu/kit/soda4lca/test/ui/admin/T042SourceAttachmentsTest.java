package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/*
	This test checks whether the attachments of the source datasets are actually there and have the correct size.
	It will consider source datasets imported via ZIP file as well as via the dedicated REST end point for source
	datasets with binary attachment.
 */
@Listeners({ScreenShooter.class})
public class T042SourceAttachmentsTest extends AbstractUITest {

    public static final String NODE_URL = TestContext.PRIMARY_SITE_URL;
    public static final String ATTACHMENT1 = "resource/sources/7983f4c6-a355-4250-aaa8-5780a72cc1df/Review-Report_LCI-Quicklime-and-Hydrated-Lime.pdf?version=03.00.000";
    public static final String ATTACHMENT2 = "resource/sources/8039a379-7a2b-4f51-a062-62b3cf1723ee/Model+Quicklime+EU+2007+UUID+34b6aca2-b139-422e-88cf-3bc4bd0bf303+version+20.00.000.jpg?version=03.00.000";
    public static final String ATTACHMENT3 = "resource/sources/dff34d61-54e9-4404-95ca-c09cc01b7f40/Model+Hydrate+UUID+a6b44172-a49d-471a-9e50-f37e7250caff+version+20.00.000.jpg?version=03.00.000";
    public static final String ATTACHMENT4 = "resource/sources/eff34d61-54e9-4404-95ca-c09cc01b7f40/Model+Hydrate+UUID+b6b44172-a49d-471a-9e50-f37e7250caff+version+20.00.000.jpg?version=03.00.000";
    public static final String ATTACHMENT4DATASET = "src/test/resources/datasets/sources/eff34d61-54e9-4404-95ca-c09cc01b7f40.xml";
    public static final String ATTACHMENT4FILE = "src/test/resources/datasets/external_docs/Model Hydrate UUID b6b44172-a49d-471a-9e50-f37e7250caff version 20.00.000.jpg";
    public static final String ATTACHMENT4KEY = "Model Hydrate UUID b6b44172-a49d-471a-9e50-f37e7250caff version 20.00.000.jpg";
    public static final String ATTACHMENT5 = "resource/sources/fff34d61-54e9-4404-95ca-c09cc01b7f40/Model+Hydrate+UUID+c6b44172-a49d-471a-9e50-f37e7250caff+version+20.00.000.jpg?version=03.00.000";
    public static final String ATTACHMENT5DATASET = "src/test/resources/datasets/sources/fff34d61-54e9-4404-95ca-c09cc01b7f40.xml";
    public static final String ATTACHMENT5FILE = "src/test/resources/datasets/external_docs/Model Hydrate UUID c6b44172-a49d-471a-9e50-f37e7250caff version 20.00.000.jpg";
    public static final String ATTACHMENT5KEY = "../external_docs/Model Hydrate UUID c6b44172-a49d-471a-9e50-f37e7250caff version 20.00.000.jpg";
    protected final static Logger log = LogManager.getLogger(T042SourceAttachmentsTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    @Test
    public void testSourceAttachment1() throws InterruptedException {
        //     Thread.sleep(60000);
        callUrl(NODE_URL + ATTACHMENT1, 200, 217681);
    }

    @Test
    public void testSourceAttachment2() throws InterruptedException {
        callUrl(NODE_URL + ATTACHMENT2, 200, 115132);
    }

    @Test
    public void testSourceAttachment3() throws InterruptedException {
        callUrl(NODE_URL + ATTACHMENT3, 200, 160477);
    }

    @Test
    public void testSourceAttachmentImportedByApi() throws InterruptedException {
        importSourceViaApi(NODE_URL + "resource/sources/withBinaries", ATTACHMENT4DATASET, ATTACHMENT4FILE, ATTACHMENT4KEY);
        callUrl(NODE_URL + ATTACHMENT4, 200, 160477);
    }

    @Test
    public void testSourceAttachmentImportedByApi2() throws InterruptedException {
        importSourceViaApi(NODE_URL + "resource/sources/withBinaries", ATTACHMENT5DATASET, ATTACHMENT5FILE, ATTACHMENT5KEY);
        callUrl(NODE_URL + ATTACHMENT5, 200, 160477);
    }

    private void importSourceViaApi(String url, String datasetPath, String attachmentPath, String multiPartKey) {
        RestTemplate restTemplate = new RestTemplate();

        log.debug("obtaining token");

        ResponseEntity<String> token = T150JWTTokenTest._requestFrestJWT();

        log.debug("uploading source dataset with attachment via API to {} with token {}", url, token.getBody());

        ResponseEntity<byte[]> response = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.getBody());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        try {
            body.add("file", new InputStreamResource(new FileInputStream(datasetPath)));
            body.add(multiPartKey, new InputStreamResource(new FileInputStream(attachmentPath)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        int status;
        try {
            response = restTemplate.postForEntity(url, requestEntity, byte[].class);
            status = response.getStatusCodeValue();
        } catch (HttpClientErrorException e) {
            status = e.getRawStatusCode();
            System.out.println(response);
        }

        log.debug("returned status code is {}", status);
        if (response != null && response.hasBody())
            log.debug("response body size is {}", response.getBody().length);

    }

    private void callUrl(String url, int expectedStatusCode, int expectedBodySize) {
        RestTemplate restTemplate = new RestTemplate();

        log.debug("calling URL {}", url);

        int status;
        ResponseEntity<byte[]> response = null;
        HttpHeaders headers = new HttpHeaders();

        try {
            response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
            status = response.getStatusCodeValue();
        } catch (HttpClientErrorException e) {
            status = e.getRawStatusCode();
        }

        log.debug("returned status code is {}", status);
        if (response != null && response.hasBody())
            log.debug("response body size is {}", response.getBody().length);

        org.testng.Assert.assertEquals(status, expectedStatusCode);
        org.testng.Assert.assertEquals(response.getBody().length, expectedBodySize);

    }

}
