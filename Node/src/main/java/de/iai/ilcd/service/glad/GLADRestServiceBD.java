package de.iai.ilcd.service.glad;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.delegate.RestServiceBD;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.rest.util.InvalidGLADUrlException;
import eu.europa.ec.jrc.lca.commons.rest.dto.DataSetRegistrationResult;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.NodeIllegalStatusException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class GLADRestServiceBD extends RestServiceBD {


    private static final String REST_PATH = "api/v1";
    private static final String PATH_PREFIX = "/search/index";
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String AUTH_BEARER_PREFIX = "Bearer ";
    private final Logger log = LogManager.getLogger(GLADRestServiceBD.class);

    private GLADRestServiceBD(String serviceBasicUrl) {
        super(serviceBasicUrl);
    }

    static GLADRestServiceBD getInstance() {
        String urlPath = ConfigurationService.INSTANCE.getGladURL();
        if (!urlPath.endsWith("/")) {
            StringBuffer buffer = new StringBuffer(urlPath);
            buffer.append("/");
            urlPath = buffer.toString();
        }
        return new GLADRestServiceBD(urlPath + REST_PATH);
    }

    /**
     * Registers a given data set with given information whether data set is an
     * updated version of an existing data set.
     *
     * @param dataset The data set that shall be registered
     * @param update  The flag indication whether data set already exists and shall
     *                be updated in GLAD
     * @return A Result containing information whether data set could be
     * successfully registered, not registered since either data set
     * already exists or data set is invalid
     * @throws NodeIllegalStatusException
     * @throws AuthenticationException
     * @throws RestWSUnknownException
     * @throws InvalidGLADUrlException
     */
    public DataSetRegistrationResult registerDataSet(DataSet dataset, boolean update)
            throws NodeIllegalStatusException, AuthenticationException, RestWSUnknownException, InvalidGLADUrlException {

        try {
            if (log.isTraceEnabled()) {
                log.trace("GLAD API key: " + ConfigurationService.INSTANCE.getGladAPIKey());
            }
            Response response = getResource("/search?query=foo").request().header(AUTH_HEADER_NAME, AUTH_BEARER_PREFIX + ConfigurationService.INSTANCE.getGladAPIKey()).get(Response.class);
            log.debug("response: " + response.getStatus());
            if (response.getStatus() != 200) {
                log.error("invalid url, status: " + response.getStatus());
                throw new InvalidGLADUrlException();
            }
        } catch (ResponseProcessingException che) {
            throw new InvalidGLADUrlException(che.getMessage());
        }

        try {

            String refId = dataset.getUuid().getUuid();

            if (log.isDebugEnabled())
                log.debug("registering dataset " + refId + ", update=" + update);

            WebTarget wr;
            if (update)
                wr = getResource(PATH_PREFIX + "/" + refId);
            else
                wr = getResource(PATH_PREFIX);
            GLADMetaDataUtil datasetMdUtil = new GLADMetaDataUtil();
            GLADMetaData datasetMd = new GLADMetaData();
            datasetMdUtil.setGLADMetaData(datasetMd);
            datasetMd = datasetMdUtil.convertToGLADMetaData(dataset);
            Response cr;
            Invocation.Builder header = wr.request(MediaType.APPLICATION_JSON).header(AUTH_HEADER_NAME, AUTH_BEARER_PREFIX + ConfigurationService.INSTANCE.getGladAPIKey());

            if (!update) {
                cr = header.post(Entity.entity(datasetMd, MediaType.APPLICATION_JSON), Response.class);
                if (cr.getStatus() == 409) {
                    return DataSetRegistrationResult.REJECTED_NO_DIFFERENCE;
                }
            } else {
                cr = header.put(Entity.entity(datasetMd, MediaType.APPLICATION_JSON), Response.class);
            }
            if (log.isDebugEnabled())
                log.debug("finished with status code " + cr.getStatus());

            String responseBody = "(empty message)";
            try {
                responseBody = cr.readEntity(String.class);
            } catch (ResponseProcessingException | WebApplicationException e) {
            }

            if (cr.getStatus() == 422) {
                log.warn("registration rejected: " + responseBody);
                return DataSetRegistrationResult.REJECTED_COMPLIANCE;
            } else if (cr.getStatus() == 201 || cr.getStatus() == 204) {
                return DataSetRegistrationResult.ACCEPTED_PENDING;
            }
            log.error("registration failed: " + responseBody);
            log.error("status code: " + cr.getStatus());
            return DataSetRegistrationResult.ERROR;
        } catch (ResponseProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    /**
     * Deregisters data set with given refId/UUID
     *
     * @param refId The refID/UUID of data set that shall be deregistered from
     *              GLAD
     * @throws AuthenticationException
     * @throws RestWSUnknownException
     */
    public void deregisterDataSet(String refId) throws AuthenticationException, RestWSUnknownException {
        try {
            WebTarget wr = getResource(PATH_PREFIX + "/" + refId);
            Response cr = wr.request(MediaType.APPLICATION_JSON).header(AUTH_HEADER_NAME, AUTH_BEARER_PREFIX + ConfigurationService.INSTANCE.getGladAPIKey())
                    .delete(Response.class);
            if (cr.getStatus() == 401) {
                throw new AuthenticationException();
            }
        } catch (ResponseProcessingException ex) {
            throw new RestWSUnknownException(ex);
        }
    }
}
