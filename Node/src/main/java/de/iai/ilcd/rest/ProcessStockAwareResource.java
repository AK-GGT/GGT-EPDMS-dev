package de.iai.ilcd.rest;

import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Stock aware version of process resource
 */
@Component
@Path("datastocks/{stockIdentifier}/processes")
public class ProcessStockAwareResource extends ProcessResource {

    @PUT
    @Path("{datasetId}")
    @Produces({"application/xml", "text/xml"})
    public Response assignDataset(@PathParam("stockIdentifier") String stockIdentifier, @PathParam("datasetId") String datasetId, @QueryParam("withDependencies") Integer dependencies, @QueryParam("version") String version) {

        return assignDataSetToStock(stockIdentifier, datasetId, dependencies, version);
    }

    @DELETE
    @Path("{datasetId}")
    @Produces({"application/xml", "text/xml"})
    public Response removeAssignedDataset(@PathParam("stockIdentifier") String stockIdentifier, @PathParam("datasetId") String datasetId, @QueryParam("withDependencies") Integer dependencies, @QueryParam("version") String version) {
        return removeDataSetFromStock(stockIdentifier, datasetId, dependencies, version);
    }


}
