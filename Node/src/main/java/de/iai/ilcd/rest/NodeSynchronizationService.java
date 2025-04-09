package de.iai.ilcd.rest;

import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import eu.europa.ec.jrc.lca.registry.domain.DataSet;
import eu.europa.ec.jrc.lca.registry.domain.NodeChangeLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Component
@Path("/synchronization")
public class NodeSynchronizationService {

    private static final Logger LOGGER = LogManager.getLogger(NodeSynchronizationService.class);

    @Autowired
    private RegistryService registryService;

    @Autowired
    private DataSetDeregistrationService dataSetDeregistrationService;

    /**
     * Informs node about changes in registry
     *
     * @param registryId
     * @param changeLog
     * @return
     */
    @PUT
    @Path("/inform/{identity}")
    @Produces(MediaType.APPLICATION_XML)
    public Response synchronizeNode(@PathParam("identity") String registryId, NodeChangeLog changeLog) {
        LOGGER.info("=============================Node synchronization signal received");
        try {
            registryService.applyChange(registryId, changeLog);
        } catch (RestWSUnknownException e) {
            throw new WebApplicationException(e, Status.BAD_REQUEST);
        }
        return Response.ok().build();
    }

    /**
     * Informs node about changes in registry
     *
     * @param registryId
     * @param changedDS
     * @return
     */
    @PUT
    @Path("/informAboutDSDeregistration/{identity}")
    @Produces(MediaType.APPLICATION_XML)
    public Response synchronizeDS(@PathParam("identity") String registryId, DataSet changedDS) {
        LOGGER.info("=============================DS synchronization signal received");
        try {
            dataSetDeregistrationService.applyDeregistration(registryId, changedDS);
        } catch (RestWSUnknownException e) {
            throw new WebApplicationException(e, Status.BAD_REQUEST);
        }
        return Response.ok().build();
    }

}
