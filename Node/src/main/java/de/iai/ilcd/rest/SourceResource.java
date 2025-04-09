package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.DigitalFile;
import de.iai.ilcd.model.common.GlobalRefUriAnalyzer;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.AbstractDigitalFileProvider;
import de.iai.ilcd.model.dao.CommonDataStockDao;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.util.SodaUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.AuthorizationException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * REST Web Service
 */
@Component
@Path("sources")
public class SourceResource extends AbstractDataSetResource<Source> {

    private static final Logger logger = LogManager.getLogger(SourceResource.class);

    /**
     * Create the source resource
     */
    public SourceResource() {
        super(DataSetType.SOURCE, ILCDTypes.SOURCE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataSetDao<Source, ?, ?> getFreshDaoInstance() {
        return new SourceDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXMLTemplatePath() {
        return "/xml/source.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDataSetTypeName() {
        return StringUtils.lowerCase(DataSetType.SOURCE.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Source> getDataSetType() {
        return Source.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHTMLDatasetDetailTemplatePath() {
        return "/html/source.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean userRequiresDatasetDetailRights() {
        return false;
    }

    @GET
    @Path("/complianceSystems")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response complianceSystems(@QueryParam(AbstractResource.PARAM_FORMAT) String format) {
        SourceDao dao = new SourceDao();
        List<Source> result = dao.getComplianceSystems();
        return super.getResponse(result, result.size(), 0, result.size(), format, null, null, false);
    }

    @GET
    @Path("/databases")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response databases(@QueryParam(AbstractResource.PARAM_FORMAT) String format) {
        SourceDao dao = new SourceDao();
        List<Source> result = dao.getDatabases();
        return super.getResponse(result, result.size(), 0, result.size(), format, null, null, false);
    }

    /**
     * Get an external file from the source
     *
     * @param uuid     UUID of the source
     * @param fileName name of the file to get
     * @return response for client
     */
    @GET
    @Path("{uuid}/{fileName}")
    @Produces({"image/*", "application/*"})
    public Response getExternalFile(@PathParam("uuid") String uuid, @Encoded @PathParam("fileName") String fileName, @QueryParam(AbstractDataSetResource.PARAM_VERSION) String versionStr) {
        logger.debug("getting external file (URL encoded) " + fileName);
        if (StringUtils.isNotBlank(fileName)) {
            fileName = SodaUtil.decode(fileName);
            logger.debug("getting external file (URL decoded) " + fileName);
        }
        return this.getFile(uuid, versionStr, fileName);
    }

    /**
     * Get the first external file from the source
     *
     * @param uuid UUID of the source
     * @return response for client
     */
    @GET
    @Path("{uuid}/digitalfile")
    @Produces({"image/*", "application/*"})
    public Response getExternalFile(@PathParam("uuid") String uuid, @QueryParam(AbstractDataSetResource.PARAM_VERSION) String versionStr) {
        return this.getFile(uuid, versionStr, null);
    }

    /**
     * POST method for importing new process data set sent by form
     *
     * @param multiPart multi part data
     * @return response for client
     */
    @POST
    @Path("/withBinaries")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes("multipart/form-data")
    public Response importByFileUpload(final FormDataMultiPart multiPart, @HeaderParam("stock") String stockUuid, @HeaderParam("lang") String language) {

        logger.debug("POSTing source with binaries to stock " + stockUuid);

        if (logger.isDebugEnabled()) {
            logger.debug("header param 'lang' is " + language);
        }

        // data stock will be taken from header, but my be overwritten by a parameter in the multipart
        FormDataBodyPart bodyStock = multiPart.getField("stock");
        if (bodyStock != null)
            stockUuid = bodyStock.getValueAs(String.class);

        if (logger.isDebugEnabled()) {
            logger.debug("multipart param 'stock' given? " + (bodyStock != null));
            logger.debug("using datastock UUID " + stockUuid);
        }

        // the actual XML
        FormDataBodyPart bodyDataset = multiPart.getField("file");
        if (bodyDataset == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No data set file parameter provided").type("text/plain").build();
        }
        InputStream dataSetInputStream = bodyDataset.getValueAs(InputStream.class);

        AbstractDigitalFileProvider digitFileProvider = new AbstractDigitalFileProvider() {

            @Override
            public boolean hasDigitalFile(String digitalFileReference) {
                final String name = this.getMatchingMultiPartKey(digitalFileReference);
                return (multiPart.getField(name) != null);
            }

            @Override
            public InputStream getInputStream(String digitalFileReference) {
                final String name = this.getMatchingMultiPartKey(digitalFileReference);
                FormDataBodyPart bodyPart = multiPart.getField(name);
                if (bodyPart != null) {
                    return bodyPart.getValueAs(InputStream.class);
                }
                return null;
            }

            /*
                As the key in our multipart could be either "filename.pdf" or "../external_docs/filename.pdf",
                we're detecting which one it is in order to use the correct one.
             */
            private String getMatchingMultiPartKey(String digitalFileReference) {
                if (multiPart.getField(digitalFileReference) != null)
                    return digitalFileReference;
                else if ((multiPart.getField(this.getBasename(digitalFileReference)) != null))
                    return this.getBasename(digitalFileReference);
                else
                    return null;
            }
        };

        try {
            final CommonDataStockDao dao = new CommonDataStockDao();
            AbstractDataStock ads;
            if (StringUtils.isBlank(stockUuid)) {
                logger.debug("no data stock specified, importing to default");
                ads = dao.getById(IDataStockMetaData.DEFAULT_DATASTOCK_ID);
            } else {
                ads = dao.getDataStockByUuid(stockUuid);
                if (!(ads instanceof RootDataStock)) {
                    throw new IllegalAccessException("Data sets can only be imported into root data stocks!");
                }
            }
            if (ads == null) {
                throw new IllegalArgumentException("Invalid root data stock UUID specified");
            }

            SecurityUtil.assertCanImport(ads);

            Source src = this.importByInputStream("POST import", DataSetType.SOURCE, dataSetInputStream, (RootDataStock) ads, digitFileProvider);

            return Response.ok(this.getStreamingOutput(src.getRootDataStock())).type(MediaType.APPLICATION_XML_TYPE).build();
        } catch (IllegalAccessException e) {
            return Response.status(Status.PRECONDITION_FAILED).type(MediaType.TEXT_PLAIN_TYPE).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE).entity(e.getMessage()).build();
        } catch (AuthorizationException e) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN_TYPE).entity(e.getMessage()).build();
        } catch (IOException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN_TYPE).entity(e.getMessage()).build();
        }

    }

    /**
     * Get an external file from the source
     *
     * @param uuid     UUID of the source
     * @param fileName name of the file to get or <code>null</code> to get the first
     * @return response for client
     */
    private Response getFile(String uuid, String versionStr, String fileName) {

        DataSetDao<Source, ?, ?> daoObject = this.getFreshDaoInstance();

        // fix uuid, if not in the right format
        GlobalRefUriAnalyzer analyzer = new GlobalRefUriAnalyzer(uuid);
        uuid = analyzer.getUuidAsString();

        DataSetVersion version = null;
        if (versionStr != null) {
            try {
                version = DataSetVersion.parse(versionStr);
            } catch (FormatException e) {
                // nothing
            }
        }

        Source source = null;

        if (version != null) {
            source = daoObject.getByUuidAndVersion(uuid, version);
        } else {
            source = daoObject.getByUuid(uuid);
        }

        if (source == null) {
            throw new WebApplicationException(404);
        }

        DigitalFile requestedFile = null;
        List<DigitalFile> digitalFiles = source.getFilesAsList();
        if (CollectionUtils.isNotEmpty(digitalFiles)) {
            if (fileName == null) {
                requestedFile = digitalFiles.get(0);
            } else {
                for (DigitalFile file : source.getFiles()) {
                    if (file.getFileName().equals(fileName)) {
                        requestedFile = file;
                        break;
                    }
                }
            }
        }

        if (requestedFile == null) {
            throw new WebApplicationException(404);
        }

        File file = new File(requestedFile.getAbsoluteFileName());
        if (!file.exists()) {
            throw new WebApplicationException(404);
        }

        String mt = new MimetypesFileTypeMap(SourceResource.class.getResourceAsStream("/mime.types")).getContentType(file);

        return Response.ok(file, mt).build();
    }

}
