package de.iai.ilcd.rest;

import de.iai.ilcd.model.common.DigitalFile;
import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.source.Source;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;

/**
 * REST Web Service
 *
 * @author clemens.duepmeier
 */

@Component
@Path("external_docs")
public class FileResource {

    public static Logger logger = LogManager.getLogger(de.iai.ilcd.rest.FileResource.class);

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of FileResource
     */
    public FileResource() {
    }

    @GET
    @Path("{sourceId}/{fileName}")
    @Produces({"image/*", "application/*"})
    public Response getExternalFile(@PathParam("sourceId") String sourceId, @PathParam("fileName") String fileName) {

        SourceDao sourceDao = new SourceDao();
        Source source = sourceDao.getByDataSetId(sourceId);

        if (source == null)
            throw new WebApplicationException(404);

        DigitalFile requestedFile = null;
        for (DigitalFile file : source.getFiles()) {
            if (file.getFileName().equals(fileName)) {
                requestedFile = file;
                break;
            }

        }

        if (requestedFile == null)
            throw new WebApplicationException(404);

        // logger.trace("I am here with file: "
        // +requestedFile.getAbsoluteFileName());

        File file = new File(requestedFile.getAbsoluteFileName());
        if (!file.exists())
            throw new WebApplicationException(404);

        String mt = new MimetypesFileTypeMap().getContentType(file);
        return Response.ok(file, mt).build();
    }

    /**
     * PUT method for updating or creating an instance of FileResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/xml")
    public void putXml(String content) {
    }
}
