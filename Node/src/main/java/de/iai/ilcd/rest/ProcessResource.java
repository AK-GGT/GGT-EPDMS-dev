package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.client.ILCDServiceClientException;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.client.impl.ILCDNetworkClient;
import de.fzk.iai.ilcd.service.client.impl.vo.DatasetVODAO;
import de.fzk.iai.ilcd.service.client.impl.vo.IntegerList;
import de.fzk.iai.ilcd.service.client.impl.vo.Result;
import de.fzk.iai.ilcd.service.client.impl.vo.StringList;
import de.fzk.iai.ilcd.service.client.impl.vo.dataset.DataSetList;
import de.fzk.iai.ilcd.service.client.impl.vo.dataset.FlowDataSetVO;
import de.fzk.iai.ilcd.service.model.*;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.adapter.DataSetListAdapter;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.nodes.NetworkNode;
import de.iai.ilcd.model.process.Exchange;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.tag.Tag;
import de.iai.ilcd.security.ProtectionType;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.webgui.controller.admin.export.DataExportController;
import de.iai.ilcd.xml.zip.ZipArchiveBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.view.ParameterTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.iai.ilcd.configuration.CachingConfig.FOREIGN_NODES_CACHEMANGER;

/**
 * REST web service for Processes
 */
@Component
@Path("processes")
public class ProcessResource extends AbstractDataSetResource<Process> {

    static final Logger LOGGER = LogManager.getLogger(ProcessResource.class);
    static final String DEFAULT_TIMEOUT = "5"; // in seconds
    @Autowired
    private DataExportController dataExportController;

    public ProcessResource() {
        super(DataSetType.PROCESS, ILCDTypes.PROCESS);
    }

    /**
     * Get exchanges for process
     *
     * @param uuid      the UUID of the process
     * @param direction the direction, mapping is: &quot;in&quot; &rArr; {@link ExchangeDirection#INPUT}, &quot;out&quot;
     *                  &rArr; {@link ExchangeDirection#OUTPUT}. <code>null</code> is permitted (both directions matched)
     * @param type      the type of the flow, mapped via {@link TypeOfFlowValue#valueOf(String)}. <code>null</code> is
     *                  permitted (all types matched)
     * @return the list of the matched exchanges
     */
    @GET
    @Path("{uuid}/exchanges")
    @Produces("application/xml")
    public StreamingOutput getExchanges(@PathParam("uuid") String uuid, @QueryParam("direction") final String direction,
                                        @QueryParam("type") final String type, @QueryParam("lang") String language, @DefaultValue("false") @QueryParam("langFallback") final boolean langFallback) {

        ExchangeDirectionValues eDir = null;
        if ("in".equals(direction)) {
            eDir = ExchangeDirectionValues.INPUT;
        } else if ("out".equals(direction)) {
            eDir = ExchangeDirectionValues.OUTPUT;
        }

        TypeOfFlowValue fType = null;
        try {
            if (type != null) {
                fType = TypeOfFlowValue.valueOf(type);
            }
        } catch (Exception e) {
            // Nothing to do, null is already set before try
        }

        ExchangeDao eDao = new ExchangeDao();
        List<Exchange> exchanges = eDao.getExchangesForProcess(uuid, null, eDir, fType);
        List<IDataSetListVO> dataSets = new ArrayList<IDataSetListVO>();
        final String baseFlowUrl = ConfigurationService.INSTANCE.getNodeInfo().getBaseURL() + DatasetTypes.FLOWS.getValue() + "/";
        for (Exchange e : exchanges) {
            IFlowListVO f = e.getFlowWithSoftReference();
            if (f == null) {
                final GlobalReference ref = e.getFlowReference();
                f = new FlowDataSetVO();
                for (ILString lStr : ref.getShortDescription().getLStrings()) {
                    f.getName().setValue(lStr.getLang(), lStr.getValue());
                }
                f.setHref(ref.getHref());
            } else {
                f.setHref(baseFlowUrl + f.getUuidAsString());
            }
            dataSets.add(f);
        }

        final DataSetList dsList = new DataSetListAdapter(dataSets, language, langFallback);

        dsList.setTotalSize(dataSets.size());
        dsList.setPageSize(dataSets.size());
        dsList.setStartIndex(0);

        return new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                DatasetVODAO dao = new DatasetVODAO();
                try {
                    dao.marshal(dsList, out);
                } catch (JAXBException e) {
                    if (e.getCause().getCause() instanceof SocketException) {
                        LOGGER.warn("exception occurred during marshalling - " + e);
                    } else {
                        LOGGER.error("error marshalling data", e);
                    }
                }
            }
        };
    }


    /**
     * Produces the list of tags assigned to a process.
     *
     * @param uuid
     * @param format
     * @param versionString
     * @return
     */
    @GET
    @Path("{uuid}/tags")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessSpecificTags(@PathParam("uuid") String uuid,
                                           @QueryParam(AbstractResource.PARAM_FORMAT) String format,
                                           @QueryParam(AbstractResource.PARAM_VERSION) String versionString) {
        ProcessDao dao = new ProcessDao();
        StringList tagNames = new StringList();
        tagNames.setIdentifier("tags");

        DataSetVersion version = null;
        try {
            version = DataSetVersion.parse(versionString);
        } catch (FormatException fe) {
            // version remains null
        }

        Process process = dao.getByUuidAndOptionalVersion(uuid, version);
        if (process == null)
            return Response.status(400).entity("Invalid UUID and/or version.").type(MediaType.TEXT_PLAIN).build();

        try {
            SecurityUtil.assertCan(process, "No read permission for the given dataset.", ProtectionType.READ);
        } catch (AuthorizationException ae) {
            return Response.status(403).entity(ae.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

        Set<Tag> tags = process.getTags();
        tagNames.setString(tags.stream().map(Tag::getName).collect(Collectors.toList()));

        return this.getListResponse(tagNames, format);
    }

    /**
     * Endpoint to assign tags to a given process.
     * <p>
     * The tags are given as QueryParameters with key 'tag'.
     *
     * @param uuid
     * @param format
     * @param versionString
     * @return
     */
    @PUT
    @Path("{uuid}/tags")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setTag(@PathParam("uuid") String uuid, @QueryParam(AbstractResource.PARAM_FORMAT) String format,
                           @QueryParam(AbstractResource.PARAM_VERSION) String versionString) {
        return assignRemoveTags(uuid, format, versionString, this.getRequest());
    }

    /**
     * Endpoint to unassign tags from a given process.
     * <p>
     * The tags are given as QueryParameters with key 'tag'.
     *
     * @param uuid
     * @param format
     * @param versionString
     * @return
     */
    @DELETE
    @Path("{uuid}/tags")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response unassignTags(@PathParam("uuid") String uuid,
                                 @QueryParam(AbstractResource.PARAM_FORMAT) String format,
                                 @QueryParam(AbstractResource.PARAM_VERSION) String versionString) {
        return assignRemoveTags(uuid, format, versionString, this.getRequest());
    }

    /**
     * Processes a PUT/DELETE request to assign tags to processes.
     * <p>
     * A ParameterTool is used to retrieve a <code>String[]</code> for the key 'tag'
     * and an (optional) <code>boolean</code> for the key 'createTags'.
     *
     * @param uuid
     * @param format
     * @param versionString
     * @param request
     * @return
     */
    private Response assignRemoveTags(String uuid, String format, String versionString, HttpServletRequest request) {
        // Clarify http method
        String method = request.getMethod();
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Recieved " + method + " request at resource/processes/" + uuid + "/tags");

        // Extract relevant params
        ParameterTool params = new ParameterTool(request);
        boolean createTags = params.getBoolean("createTags", false);

        Set<String> tagParams = getValidParams(params, "tag");
        if (tagParams.size() == 0)
            return Response.status(400).entity("No Tag parameters provided.").type(MediaType.TEXT_PLAIN).build();
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Retrieved " + tagParams.size() + " tag parameters.");

        // Initialise version
        DataSetVersion version = null;
        try {
            version = DataSetVersion.parse(versionString);
        } catch (FormatException fe) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Appearently the version format was wrong. Exception message: " + fe.getMessage()
                        + ". Proceeding without version.");
            return Response.status(400).entity(fe.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

        // Initialise process
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Looking for process with UUID: " + uuid + "using (optional) version: "
                    + (version != null ? version.getVersionString() : "null"));
        ProcessDao pDao = new ProcessDao();
        Process process = pDao.getByUuidAndOptionalVersion(uuid, version);
        if (process == null)
            return Response.status(400).entity("Invalid UUID and/or version.").type(MediaType.TEXT_PLAIN).build();

        // Checking persmission..
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Checking permission..");
        try {
            SecurityUtil.assertCan(process, "No tagging permission.", ProtectionType.TAG);
        } catch (AuthorizationException ae) {
            return Response.status(403).entity(ae.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Access granted.");

        // Initialise tags
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Fetching tags from db..");
        TagDao tDao = new TagDao();
        List<Tag> tags = new ArrayList<Tag>();
        List<String> problems = new ArrayList<String>();

        for (String tagName : tagParams) {
            Tag tag = tDao.getTagByName(tagName);
            if (tag != null) {
                tags.add(tag);
            } else {
                problems.add(tagName);
            }
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Tags for " + problems.size() + " tagNames " + StringUtils.join(problems, ", ")
                    + " couldn't be fetched, because they appearently don't exist.");

        if (createTags && StringUtils.equals(method, "PUT")) {
            // We simply try to persist the not-yet-in-db-tags.
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Creating " + problems.size() + " new tags.");

            Set<Tag> newTags = new HashSet<Tag>();
            Set<Tag> problematicTags = new HashSet<Tag>();
            for (String tagName : problems) {
                Tag tag = new Tag();
                tag.setName(tagName);
                newTags.add(tag);
            }

            try {
                tDao.persist(newTags);
            } catch (PersistException pe) {
                // keep track of successes and failures
                problematicTags = findUnpersistedTags(tDao, newTags);
                newTags.removeAll(problematicTags);
            }
            problems = problematicTags.stream().map(Tag::getName).collect(Collectors.toList());
            tags.addAll(newTags);
        }

        // Assign/unassign tags to/from process
        if (StringUtils.equals(method, "PUT")) {
            for (Tag tag : tags)
                process.addTag(tag);
        } else if (StringUtils.equals(method, "DELETE")) {
            for (Tag tag : tags)
                process.removeTag(tag);
        }

        // Merge db
        try {
            pDao.merge(process);
        } catch (MergeException e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("The tags "
                    + StringUtils.join(tags.stream().map(Tag::getName).collect(Collectors.toList()), ", ")
                    + " were successfully " + (StringUtils.equals(method, "PUT") ? "assigned to" : "unassigned from")
                    + " process " + uuid + " and persisted in the database.");

        if (problems.size() != 0) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Adding the problematic tagName parameters as simple list to Response...");
            StringList problemsSL = new StringList();
            problemsSL.setIdentifier("Problems");
            problemsSL.setString(problems);

            return this.getListResponse(problemsSL, format); // status: 200
        }

        return Response.status(204).build();
    }

    /**
     * Checks which of the provided Tags are not yet persisted.
     *
     * @param tDao
     * @param tags
     * @return
     */
    private Set<Tag> findUnpersistedTags(TagDao tDao, Collection<Tag> tags) {
        List<Tag> existingTags = tDao.getAll();
        return tags.stream()
                .filter(t -> !t.containedInList(existingTags))
                .collect(Collectors.toSet());
    }

    /**
     * Getting rid of blanks and duplicates.
     *
     * @param paramCollection
     * @param key
     * @return
     */
    private Set<String> getValidParams(ValueParser paramCollection, String key) {
        Set<String> result = new HashSet<String>();
        String[] params = paramCollection.getStrings(key);

        if (params != null && params.length > 0) {
            int n = params.length;
            for (int i = 0; i < n; i++) {
                String param = params[i];
                if (StringUtils.isNotBlank(param))
                    result.add(param);
            }
        }
        return result;
    }


    /**
     * Get dependencies of a process in a zip archive.
     *
     * @param uuid    the UUID of the process
     * @param version the version of the process, if version not provided (null)
     *                latest is assumed
     * @return A zip archive contains the process and it's dependencies.
     */

    @GET
    @Path("{uuid}/zipexport")
    @Produces("application/zip")
    public Response getDependencies(@PathParam("uuid") String uuid, @QueryParam("version") String version) {
        ProcessDao pDao = (ProcessDao) getFreshDaoInstance();
        Process p;

        // if version not provided (null) or parser fails to read it properly, latest
        // version is assumed
        DataSetVersion dsv;
        try {
            dsv = DataSetVersion.parse(version);
            p = pDao.getByUuidAndVersion(uuid, dsv);
        } catch (FormatException | NullPointerException e) {
            p = pDao.getByUuid(uuid);
        }

        // The java community has been asking for import aliasing since 1998!
        java.nio.file.Path dir = Paths.get(ConfigurationService.INSTANCE.getZipFileDirectory());

        // Overwrite the same file to avoid post cleaning
        ZipArchiveBuilder zipArchiveBuilder = new ZipArchiveBuilder(dir.resolve("RESTtmpDEPexport.zip"), dir);

        dataExportController.writeDependencies(p, zipArchiveBuilder);
        zipArchiveBuilder.close();

        String filename = p.getUuidAsString() + "_dependencies.zip";
        return Response.ok(zipArchiveBuilder.getFile())
                .header("Content-Type", zipArchiveBuilder.MIME)
                .header("Content-Disposition", zipArchiveBuilder.getContentDisposition(filename))
                .build();
    }

    /**
     * Get the reference years
     *
     * @return reference years
     * <p>
     * TODO add support for datastock specific selection (like with language)
     */
    @GET
    @Path("referenceyears")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getReferenceYears(@PathParam("stockIdentifier") String datastock,
                                      @QueryParam(AbstractResource.PARAM_FORMAT) String format,
                                      @DefaultValue("false") @QueryParam(AbstractResource.DISTRIBUTED) Boolean distributed,
                                      @DefaultValue(DEFAULT_TIMEOUT) @QueryParam(AbstractResource.TIMEOUT) int timeout) { // 5 seconds default value
        ProcessDao pDao = new ProcessDao();

        final IntegerList il = new IntegerList();
        il.setIdentifier("referenceyears");

        il.setIntegers(pDao.getReferenceYears());

        if (distributed) {
            loadDistReferenceYears(il, timeout);
        }

        return getListResponse(il, format);
    }

    @Cacheable(FOREIGN_NODES_CACHEMANGER)
    public void loadDistReferenceYears(IntegerList il, int timeout) {
        // collect results from foreign nodes as well
        NetworkNodeDao nDao = new NetworkNodeDao();
        List<NetworkNode> nodes = nDao.getRemoteNetworkNodes();

        // Expires after 60 seconds
        ExecutorService ES = Executors.newCachedThreadPool();

        List<CompletableFuture<IIntegerList>> cfs = new ArrayList<CompletableFuture<IIntegerList>>();

        nodes.forEach(node -> cfs.add(CompletableFuture.supplyAsync(() -> {
            try {
                var client = new ILCDNetworkClient(node.getBaseUrl() + "resource/");
                client.setOrigin(ConfigurationService.INSTANCE.getNodeInfo().getBaseURLwithResource());
                return client.getReferenceYears();
            } catch (ILCDServiceClientException | IOException e) {
                LOGGER.error("error retrieving reference years from remote nodes", e);
                return null;
            }
        }, ES)));

        cfs.removeAll(Collections.singleton(null)); // Unreachable references years marked as null

        cfs.forEach(cf -> {
            try {
                IIntegerList result = cf.get(timeout, TimeUnit.SECONDS);
                if (result != null && result.getIntegers() != null)
                    for (Integer b : result.getIntegers())
                        if (b != null)
                            il.addInteger(b);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.info("A node took too long time to respond; ignoring...");
                LOGGER.debug("Exception is ", e);
            }
        });

        il.flush();
    }

    /**
     * Get the validUntil years
     *
     * @return validUntil years
     * <p>
     * TODO add support for datastock specific selection (like with language)
     */
    @GET
    @Path("validuntilyears")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getValidUntilYears(@PathParam("stockIdentifier") String datastock,
                                       @QueryParam(AbstractResource.PARAM_FORMAT) String format,
                                       @DefaultValue("false") @QueryParam(AbstractResource.DISTRIBUTED) Boolean distributed,
                                       @DefaultValue(DEFAULT_TIMEOUT) @QueryParam(AbstractResource.TIMEOUT) int timeout) {
        ProcessDao pDao = new ProcessDao();

        final IntegerList il = new IntegerList();
        il.setIdentifier("validuntilyears");

        il.setIntegers(pDao.getValidUntilYears());

        if (distributed) {
            loadDistValidUntilYears(il, timeout);
        }

        return getListResponse(il, format);
    }

    @Cacheable(FOREIGN_NODES_CACHEMANGER)
    public void loadDistValidUntilYears(final IntegerList il, int timeout) {
        // collect results from foreign nodes as well
        NetworkNodeDao nDao = new NetworkNodeDao();
        List<NetworkNode> nodes = nDao.getRemoteNetworkNodes();

        // Expires after 60 seconds
        ExecutorService ES = Executors.newCachedThreadPool();

        List<CompletableFuture<IIntegerList>> cfs = new ArrayList<CompletableFuture<IIntegerList>>();

        nodes.forEach(node -> cfs.add(CompletableFuture.supplyAsync(() -> {
            try {
                var client = new ILCDNetworkClient(node.getBaseUrl() + "resource/");
                client.setOrigin(ConfigurationService.INSTANCE.getNodeInfo().getBaseURLwithResource());
                return client.getValidUntilYears();
            } catch (ILCDServiceClientException | IOException e) {
                LOGGER.error("error retrieving valid until years from remote nodes", e);
                return null;
            }
        }, ES)));

        cfs.removeAll(Collections.singleton(null)); // Unreachable validyears marked as null

        cfs.forEach(cf -> {
            try {
                IIntegerList result = cf.get(timeout, TimeUnit.SECONDS);
                // TODO: introduce a cleaner addInteger(s) in service-api
                if (result != null && result.getIntegers() != null)
                    for (Integer b : result.getIntegers())
                        if (b != null)
                            il.addInteger(b);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.info("A node took too long time to respond; ignoring...");
                LOGGER.debug("Exception is ", e);
            }
        });
        il.flush();
    }

    /**
     * Get the locations
     *
     * @return the locations
     * <p>
     * TODO add support for datastock specific selection (like with language)
     */
    @GET
    @Path("locations")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getLocations(@PathParam("stockIdentifier") String datastock,
                                 @QueryParam(AbstractResource.PARAM_FORMAT) String format,
                                 @DefaultValue("false") @QueryParam(AbstractResource.DISTRIBUTED) Boolean distributed,
                                 @DefaultValue(DEFAULT_TIMEOUT) @QueryParam(AbstractResource.TIMEOUT) int timeout) {
        ProcessDao pDao = new ProcessDao();

        final StringList sl = new StringList();
        sl.setIdentifier("locations");

        List<GeographicalArea> locationsList = pDao.getUsedLocations();

        sl.setString(locationsList.stream().map(GeographicalArea::getAreaCode).collect(Collectors.toList()));

        if (distributed) {
            loadDistLocations(sl, timeout);
        }

        return getListResponse(sl, format);
    }

    @Cacheable(FOREIGN_NODES_CACHEMANGER)
    public void loadDistLocations(final StringList sl, int timeout) {
        // collect results from foreign nodes as well
        NetworkNodeDao nDao = new NetworkNodeDao();
        List<NetworkNode> nodes = nDao.getRemoteNetworkNodes();

        // Expires after 60 seconds
        ExecutorService ES = Executors.newCachedThreadPool();

        List<CompletableFuture<IStringList>> cfs = new ArrayList<CompletableFuture<IStringList>>();

        nodes.forEach(node -> cfs.add(CompletableFuture.supplyAsync(() -> {
            try {
                var client = new ILCDNetworkClient(node.getBaseUrl() + "resource/");
                client.setOrigin(ConfigurationService.INSTANCE.getNodeInfo().getBaseURLwithResource());
                return client.getLocations();
            } catch (ILCDServiceClientException | IOException e) {
                LOGGER.error("error retrieving locations from remote nodes", e);
                return null;
            }
        }, ES)));

        cfs.removeAll(Collections.singleton(null)); // Unreachable location marked as null

        cfs.forEach(cf -> {
            try {
                IStringList result = cf.get(timeout, TimeUnit.SECONDS);

                // TODO: introduce a cleaner addString(s) in service-api
                if (result != null && result.getStrings() != null)
                    for (String b : result.getStrings())
                        if (b != null)
                            sl.addString(b);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.info("A node took too long time to respond; ignoring...");
                LOGGER.debug("Exception is ", e);
            }
        });

        sl.flush();
    }

    /**
     * Get the languages
     *
     * @return the languages
     */
    @GET
    @Path("languages")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getLanguages(@PathParam("stockIdentifier") String datastock,
                                 @QueryParam(AbstractResource.PARAM_FORMAT) String format,
                                 @DefaultValue("false") @QueryParam(AbstractResource.DISTRIBUTED) Boolean distributed,
                                 @DefaultValue(DEFAULT_TIMEOUT) @QueryParam(AbstractResource.TIMEOUT) int timeout) {
        ProcessDao pDao = new ProcessDao();

        final StringList sl = new StringList();
        sl.setIdentifier("languages");

        List<String> languages = new ArrayList<String>();
        if (datastock != null) {
            CommonDataStockDao dsDao = new CommonDataStockDao();
            IDataStockMetaData[] ds = new IDataStockMetaData[]{dsDao.getDataStockByUuid(datastock)};
            if (ds[0] != null)
                languages = pDao.getLanguages(ds);
        } else
            languages = pDao.getLanguages();

        sl.setString(languages);

        if (distributed) {
            loadDistLanguages(sl, timeout);
        }

        return getListResponse(sl, format);
    }

    @Cacheable(FOREIGN_NODES_CACHEMANGER)
    public void loadDistLanguages(final StringList sl, int timeout) {
        // collect results from foreign nodes as well
        NetworkNodeDao nDao = new NetworkNodeDao();
        List<NetworkNode> nodes = nDao.getRemoteNetworkNodes();

        // Expires after 60 seconds
        ExecutorService ES = Executors.newCachedThreadPool();

        List<CompletableFuture<IStringList>> cfs = new ArrayList<CompletableFuture<IStringList>>();

        nodes.forEach(node -> cfs.add(CompletableFuture.supplyAsync(() -> {
            try {
                var client = new ILCDNetworkClient(node.getBaseUrl() + "resource/");
                client.setOrigin(ConfigurationService.INSTANCE.getNodeInfo().getBaseURLwithResource());
                return client.getLanguages();
            } catch (ILCDServiceClientException | IOException e) {
                LOGGER.error("error retrieving languages from remote nodes", e);
                return null;
            }
        }, ES)));

        cfs.removeAll(Collections.singleton(null)); // Unreachable languages marked as null

        cfs.forEach(cf -> {
            try {
                IStringList result = cf.get(timeout, TimeUnit.SECONDS);
                // TODO: introduce a cleaner addString(s) in service-api
                if (result != null && result.getStrings() != null)
                    for (String b : result.getStrings())
                        if (b != null)
                            sl.addString(b);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.info("A node took too long time to respond; ignoring...");
                LOGGER.debug("Exception is ", e);
            }
        });

        sl.flush();
    }

    @GET
    @Path("{uuid}/epd")
    //GET /processes/{uuid}/epd?version={version}
    @Produces({"application/pdf", MediaType.TEXT_HTML})
    public Response getOriginalEPD(@PathParam("uuid") String uuid, @QueryParam("version") String version) {
        SourceDao sourceDao = new SourceDao();
        ProcessDao processDao = new ProcessDao();
        DataSetVersion processVersion = null;
        try {
            processVersion = DataSetVersion.parse(version);
        } catch (FormatException | NullPointerException e) {
            LOGGER.warn("could not parse version number of process " + version, e);
        }

        // fetch the UUIDs of background databases
        Set<String> BKGDatabaseUUIDs = sourceDao.getDatabases()
                .stream().map(DataSet::getUuidAsString)
                .collect(Collectors.toSet());

        Process p = processDao.getByUuidAndOptionalVersion(uuid, processVersion);
        if (p == null)
            return poorMans404();


        IGlobalReference erg = null;

        //The reference we're looking for is easy to find for epds of format version 1.2...
        if (StringUtils.equals(p.getEpdFormatVersion(), "1.2")) {
            Set<GlobalReference> sources = p.getReferenceToOriginalEPD();
            if (sources != null && !sources.isEmpty()) {
                erg = sources.iterator().next();    //All references should be fine.
            } else {
                return poorMans404();
            }

            //...otherwise
        } else {

            // The EPD is hiding in one of these sources
            Set<IGlobalReference> Candidates = new HashSet<IGlobalReference>();

            // throw out any that match the list of background databases
            for (IGlobalReference ref : p.getDataSources())
                if (!BKGDatabaseUUIDs.contains(ref.getRefObjectId()))
                    Candidates.add(ref);

            switch (Candidates.size()) {
                case 0:
                    // sorry not found, display a 404
                    return poorMans404();
                case 1:
                    // we're good, this is the one
                    erg = Candidates.iterator().next();
                    break;

                default:
                    // ask oracle
                    erg = EPDOracle(Candidates);
                    break;
            }
        }

        DataSetVersion sourceVersion = null;

        try {
            sourceVersion = DataSetVersion.parse(erg.getVersionAsString());
        } catch (FormatException | NullPointerException e) {
            LOGGER.warn("could not parse version number of source " + sourceVersion, e);
        }

        DigitalFile df = null;
        for (DigitalFile d : Optional.ofNullable(sourceDao.getByUuidAndOptionalVersion(erg.getRefObjectId(), sourceVersion))
                .map(Source::getFiles)
                .orElseGet(() -> new HashSet<DigitalFile>())) // orElse always executes
            df = d;

        URI externalLink = null;
        try {
            externalLink = new URI(df.getFileName());
        } catch (URISyntaxException e) {
            LOGGER.warn("could not parse URI " + df.getFileName());
        } catch (NullPointerException e) {
            return poorMans404();
        }

        File f = new File(df.getAbsoluteFileName());
        if (f != null && f.exists())
            return Response.ok(f, "application/pdf").build();
        else if (externalLink != null)
            return Response.temporaryRedirect(externalLink).build();

        return poorMans404();

    }

    @GET
    @Path("/registrationAuthorities")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRegistrationAuthorities(@QueryParam(AbstractResource.PARAM_FORMAT) String format,
                                               @QueryParam("lang") String language,
                                               @DefaultValue("false") @QueryParam("langFallback") final boolean langFallback,
                                               @DefaultValue("false") @QueryParam(AbstractResource.DISTRIBUTED) Boolean distributed,
                                               @DefaultValue(DEFAULT_TIMEOUT) @QueryParam(AbstractResource.TIMEOUT) int timeout) {
        ProcessDao dao = new ProcessDao();
        List<IContactListVO> result = new ArrayList<>(dao.getAllRegistrationAuthorities());

        if (distributed) {
            loadDistRegistrationAuthorities(result, timeout);
        }

        return super.getResponse(result, result.size(), 0, result.size(), format, null, language, langFallback);
    }

    private void loadDistRegistrationAuthorities(final List<IContactListVO> contacts, int timeout) {
        // collect results from foreign nodes as well
        NetworkNodeDao nDao = new NetworkNodeDao();
        List<NetworkNode> nodes = nDao.getRemoteNetworkNodes();

        // Expires after 60 seconds
        ExecutorService ES = Executors.newCachedThreadPool();

        List<CompletableFuture<Result<IContactListVO>>> cfs = new ArrayList<CompletableFuture<Result<IContactListVO>>>();

        nodes.forEach(node -> cfs.add(CompletableFuture.supplyAsync(() -> {
            try {
                var client = new ILCDNetworkClient(node.getBaseUrl() + "resource/");
                client.setOrigin(ConfigurationService.INSTANCE.getNodeInfo().getBaseURLwithResource());
                return client.getRegistrationAuthorities();
            } catch (ILCDServiceClientException | IOException e) {
                LOGGER.error("error retrieving languages from remote nodes", e);
                return null;
            }
        }, ES)));

        cfs.removeAll(Collections.singleton(null)); // Unreachable items marked as null

        cfs.forEach(cf -> {
            try {
                Result<IContactListVO> result = cf.get(timeout, TimeUnit.SECONDS);
                // TODO: introduce a cleaner addString(s) in service-api
                if (result != null && result.getDataSets() != null)
                    for (IContactListVO b : result.getDataSets())
                        if (b != null)
                            contacts.add(b);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.info("A node took too long time to respond; ignoring...");
                LOGGER.debug("Exception is ", e);
            }
        });
    }

    /**
     * For EPD format extension 1.1, there is no deterministic way of getting the
     * original EPD PDF, so this method leverages a bunch of heuristics to find
     * the correct EPD.
     *
     * @return IGlobalReference of the Original EPD's PDF (correctness is not
     * guaranteed)
     */
    private IGlobalReference EPDOracle(Set<IGlobalReference> candidates) {

        if (LOGGER.isTraceEnabled())
            LOGGER.trace("we have to ask the oracle to decide between these candidates: ", candidates);

        IGlobalReference erg = candidates.iterator().next();
        int ergscore = 0; // slightly prioritize first element

        if (LOGGER.isTraceEnabled())
            LOGGER.trace("default selection is " + erg.getRefObjectId() + " " + erg.getVersionAsString() + ": " + 0);


        // If Java has a cleaner argmax, replace this
        for (IGlobalReference ref : candidates) {
            int score = EPDOracleHeuristicFn(ref);

            if (LOGGER.isTraceEnabled())
                LOGGER.trace("score of " + ref.getRefObjectId() + " " + ref.getVersionAsString() + " " + (ref.getShortDescription() != null ? ref.getShortDescription().getValueWithFallback("en") : "null") + ": " + score);

            if (score >= ergscore) {
                ergscore = score;
                erg = ref;

                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("selecting " + ref.getRefObjectId() + " " + ref.getVersionAsString() + " " + (ref.getShortDescription() != null ? ref.getShortDescription().getValueWithFallback("en") : "null") + ": " + score);
            }
        }

        if (LOGGER.isTraceEnabled())
            LOGGER.trace("the oracle has selected " + erg.getRefObjectId() + " " + erg.getVersionAsString() + " " + (erg.getShortDescription() != null ? erg.getShortDescription().getValueWithFallback("en") : "null"));

        return erg;
    }

    /**
     * Point-based system to assess the likelihood of given datasource reference
     * is the original PDF
     *
     * @param ref
     * @return score as an integer
     */
    private int EPDOracleHeuristicFn(IGlobalReference ref) {

        // Rules that the oracle uses to guess the correct EPD
        // Assuming input strings are in lowercase
        final List<Function<String, Integer>> RULES = Arrays.asList(
                (s -> s.contains("pdf") ? 5 : 0),
                (s -> s.endsWith("pdf") ? 5 : 0),
                (s -> s.contains("epd") ? 10 : 0),
                (s -> s.contains("jpg") ? -5 : 0),
                (s -> s.contains("png") ? -5 : 0),
                (s -> s.contains("database") ? -15 : 0),
                (s -> s.isEmpty() ? -100 : 0),
                (s -> s.contains("epd") && s.endsWith("pdf") ? 100 : 0) // most probable
        );

        // Look for EPD's PDF using using only shortDescription
        String normalized = ref.getShortDescription().getValueWithFallback("en").toLowerCase().trim();

        int erg = 0;
        // If Java has a cleaner reduce, replace this
        for (Function<String, Integer> r : RULES)
            erg += r.apply(normalized);

        return erg;
    }


    private Response poorMans404() {
        String LANDING_PAGE = ConfigurationService.INSTANCE.getLandingPageURL();
//		String REDIRECT_JS = String.format(
//				"<h3>redirecting in 5 seconds...</h3>" + 
//				"      <script>\n"
//				+ "         setTimeout(function(){\n"
//				+ "            window.location.href = '%s';\n"
//				+ "         }, 5000);\n"
//				+ "      </script>", LANDING_PAGE);

        String PAGE_404 = String.format(
                "<h1>We're sorry, but the document you're looking for could not be found.</h1>\n"
                        + "<a href='%s'>Back to home</a>\n", LANDING_PAGE);

        return Response.status(404).entity(PAGE_404).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataSetDao<Process, ?, ?> getFreshDaoInstance() {
        return new ProcessDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXMLTemplatePath() {
        return "/xml/process.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHTMLDatasetDetailTemplatePath() {
        return "/html/process.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDataSetTypeName() {
        return "process";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Process> getDataSetType() {
        return Process.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean userRequiresDatasetDetailRights() {
        return true;
    }

}
