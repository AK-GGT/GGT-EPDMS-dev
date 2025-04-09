package de.iai.ilcd.delegate;

import de.iai.ilcd.model.registry.Registry;
import eu.europa.ec.jrc.lca.commons.domain.NodeCredentials;
import eu.europa.ec.jrc.lca.commons.rest.CustomStatus;
import eu.europa.ec.jrc.lca.commons.rest.dto.*;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.NodeIllegalStatusException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import eu.europa.ec.jrc.lca.registry.domain.DataSet;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class DataSetRestServiceBD extends RestServiceBD {

    private static final String REST_PATH = "rest";

    private DataSetRestServiceBD(String serviceBasicUrl) {
        super(serviceBasicUrl);
    }

    public static DataSetRestServiceBD getInstance(Registry registry) {
        return new DataSetRestServiceBD(registry.getBaseUrl() + REST_PATH);
    }

    public List<DataSetRegistrationResult> registerDataSets(List<DataSet> datasets, NodeCredentials credentials) throws NodeIllegalStatusException,
            AuthenticationException, RestWSUnknownException {
        try {
            WebTarget wr = getResource("/datasetservice/register");

            Response cr = wr.request(MediaType.APPLICATION_XML).post(Entity.entity(new SecuredRequestWrapper(credentials, new JaxbBaseList<DataSet>(datasets)), MediaType.APPLICATION_XML), Response.class);
            if (cr.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
                throw new NodeIllegalStatusException();
            } else if (cr.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException();
            }
            JaxbBaseList<DataSetRegistrationResult> list = cr.readEntity(new GenericType<JaxbBaseList<DataSetRegistrationResult>>() {
            });
            return list.getList();
        } catch (ResponseProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    public List<DataSetRegistrationAcceptanceDecision> checkStatus(List<DataSetDTO> dataSets, NodeCredentials credentials) throws AuthenticationException,
            RestWSUnknownException {
        try {
            WebTarget wr = getResource("/datasetservice/checkStatus");

            Response cr = wr.request(MediaType.APPLICATION_XML).post(Entity.entity(new SecuredRequestWrapper(credentials, new JaxbBaseList<DataSetDTO>(dataSets)), MediaType.APPLICATION_XML), Response.class);
            if (cr.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException();
            }
            JaxbBaseList<DataSetRegistrationAcceptanceDecision> list = cr.readEntity(new GenericType<JaxbBaseList<DataSetRegistrationAcceptanceDecision>>() {
            });
            return list.getList();
        } catch (ResponseProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    public DataSet getDataSetData(DataSet dataset) throws RestWSUnknownException {
        try {
            Response cr = getResponse("/datasetservice/dataset/" + dataset.getId());
            if (cr.getStatus() == CustomStatus.ENTITY_NOT_FOUND.getCode()) {
                return null;
            }
            return (DataSet) cr.getEntity();
        } catch (ResponseProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    public List<DataSetDeregistrationResult> deregisterDataSets(List<DataSetDTO> datasets, String reason, NodeCredentials credentials)
            throws AuthenticationException, RestWSUnknownException {
        DataSetDeregistrationRequest dsDeregistrationReq = new DataSetDeregistrationRequest(reason, datasets);
        try {
            WebTarget wr = getResource("/datasetservice/deregister");

            Response cr = wr.request(MediaType.APPLICATION_XML).post(Entity.entity(new SecuredRequestWrapper(credentials, dsDeregistrationReq), MediaType.APPLICATION_XML), Response.class);
            if (cr.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException();
            }
            JaxbBaseList<DataSetDeregistrationResult> list = cr.readEntity(new GenericType<JaxbBaseList<DataSetDeregistrationResult>>() {
            });
            return list.getList();
        } catch (ResponseProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    public ValidationResult verify(DataSet dataset) {
        try {
            WebTarget wr = getResource("/datasetservice/verify");

            Response cr = wr.request(MediaType.APPLICATION_XML).post(Entity.entity(dataset, MediaType.APPLICATION_XML), Response.class);
            if (cr.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                return ValidationResult.INVALID;
            } else if (cr.getStatus() == Response.Status.OK.getStatusCode()) {
                return ValidationResult.VALID;
            } else if (cr.getStatus() == CustomStatus.ENTITY_NOT_FOUND.getCode()) {
                return ValidationResult.CANT_VALIDATE_NOT_REGISTERED;
            }
            return ValidationResult.CANT_VALIDATE;
        } catch (ResponseProcessingException ex) {
            return ValidationResult.CANT_VALIDATE;
        }
    }

}
