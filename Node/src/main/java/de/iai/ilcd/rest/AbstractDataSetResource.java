package de.iai.ilcd.rest;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.client.impl.ServiceDAO;
import de.fzk.iai.ilcd.service.client.impl.vo.CategoryList;
import de.fzk.iai.ilcd.service.client.impl.vo.DataStockVO;
import de.fzk.iai.ilcd.service.client.impl.vo.DatasetVODAO;
import de.fzk.iai.ilcd.service.client.impl.vo.dataset.DataSetList;
import de.fzk.iai.ilcd.service.client.impl.vo.dataset.DataSetVO;
import de.fzk.iai.ilcd.service.client.impl.vo.types.common.ClassType;
import de.fzk.iai.ilcd.service.model.*;
import de.fzk.iai.ilcd.service.model.enums.TypeOfProcessValue;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.jaxb.DataSetJAXB;
import de.iai.ilcd.model.adapter.ClClassAdapter;
import de.iai.ilcd.model.adapter.DataSetListAdapter;
import de.iai.ilcd.model.adapter.DataStockListAdapter;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.datastock.*;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.rest.augmentations.ResourceReference;
import de.iai.ilcd.rest.json.ExchangeTypeSerializer;
import de.iai.ilcd.rest.json.JAXBElementMixIn;
import de.iai.ilcd.rest.json.QNameSerializer;
import de.iai.ilcd.rest.util.JSONDatasetList;
import de.iai.ilcd.rest.util.JSONDatasetListSerializer;
import de.iai.ilcd.security.ProtectionType;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.util.*;
import de.iai.ilcd.util.sort.ClClassIdComparator;
import de.iai.ilcd.webgui.controller.ConfigurationBean;
import de.iai.ilcd.webgui.controller.DirtyFlagBean;
import de.iai.ilcd.webgui.controller.ui.AvailableStockHandler;
import de.iai.ilcd.webgui.controller.util.csv.CSVFormatter;
import de.iai.ilcd.webgui.controller.util.csv.DecimalSeparator;
import de.iai.ilcd.xml.read.DataSetImporter;
import de.iai.ilcd.xml.read.DataSetZipImporter;
import eu.europa.ec.jrc.lca.commons.util.ApplicationContextHolder;
import it.jrc.lca.ilcd.common.GlobalReferenceType;
import it.jrc.lca.ilcd.process.ExchangeType;
import it.jrc.lca.ilcd.process.ProcessDataSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.view.ParameterTool;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.*;

/**
 * REST web service for data sets. In order to support data stock awareness, sub-classes must have a {@link PathParam
 * path parameter} <code>stockIdentifier</code>, e.g.:
 *
 * <pre>
 * &#64;Path( "datastocks/{stockIdentifier}/processes" )
 * public class ProcessStockAwareResource extends ProcessResource {
 *    (...)
 * }
 * </pre>
 *
 * @param <T> type of data set
 */
public abstract class AbstractDataSetResource<T extends DataSet> extends AbstractResource {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AbstractDataSetResource.class);
    /**
     * The type in API definition, required for {@link #generateDataSetDetailAsHtml(DataSet)}
     */
    private final ILCDTypes apiType;
    /**
     * The type in model definition, required for
     */
    private final DataSetType modelType;
    /**
     * Context, required for the velocity rendering
     */
    @Context
    private UriInfo context;
    /**
     * Headers, currently unused
     */
    @Context
    private HttpHeaders headers;
    /**
     * The request, required for request parameter evaluation
     */
    @Context
    private HttpServletRequest request;

    /**
     * Create an abstract data set resource
     *
     * @param modelType model type value
     * @param apiType   API type value
     */
    public AbstractDataSetResource(DataSetType modelType, ILCDTypes apiType) {
        if (apiType == null) {
            throw new IllegalArgumentException("apiType must not be null!");
        }
        if (modelType == null) {
            throw new IllegalArgumentException("modelType must not be null!");
        }

        this.apiType = apiType;
        this.modelType = modelType;
    }

    /**
     * Create a fresh data access object to work with
     *
     * @return fresh data access object
     */
    protected abstract DataSetDao<T, ?, ?> getFreshDaoInstance();

    protected abstract Class<T> getDataSetType();

    /**
     * Get the path to the template for the XML single data set view This is typically something like
     * <code>/xml/<i>$datasettype$</i>.vm</code>
     *
     * @return path to the template for the XML single data set view
     * @deprecated obsolete
     */
    @Deprecated
    protected abstract String getXMLTemplatePath();

    /**
     * Get the path to the template for the HTML dataset detail view page This is typically something like
     * <code>/html/<i>$datasettype$</i>.vm</code>
     *
     * @return path to the template for the HTML dataset detail view page
     */
    protected abstract String getHTMLDatasetDetailTemplatePath();

    /**
     * Get the name of the data set type for the creation of error/info messages
     *
     * @return name of the data set type for the creation of error/info messages
     */
    protected abstract String getDataSetTypeName();

    /**
     * Flag to set, if dataset detail view rights shall be checked in user bean
     *
     * @return <code>true</code> if dataset detail view rights shall be checked, else <code>false</code>
     */
    protected abstract boolean userRequiresDatasetDetailRights();

    /**
     * Retrieves representation of an instance of data set
     *
     * @param search     search trigger
     * @param startIndex start index
     * @param pageSize   page size
     * @param format     format
     * @param callback   JSONP callback
     * @return a response object
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataSets(@PathParam("stockIdentifier") String stockIdentifier, @DefaultValue("false") @QueryParam("search") final boolean search,
                                @DefaultValue("false") @QueryParam("distributed") final boolean distributed,
                                @DefaultValue("0") @QueryParam("startIndex") final int startIndex, @DefaultValue("500") @QueryParam("pageSize") final int pageSize,
                                @QueryParam(AbstractResource.PARAM_FORMAT) String format, @DefaultValue("fn") @QueryParam("callback") String callback,
                                @QueryParam("lang") final String language, @DefaultValue("false") @QueryParam("langFallback") final boolean langFallback,
                                @DefaultValue("false") @QueryParam("allVersions") final boolean allVersions, @DefaultValue("false") @QueryParam("countOnly") final boolean countOnly,
                                @QueryParam("sortBy") final String sortBy, @DefaultValue("true") @QueryParam("sortOrder") final boolean sortOrder) {

        List<? extends IDataSetListVO> dataSets = new ArrayList<IDataSetListVO>();
        Integer count;

        IDataStockMetaData[] stocks = getStocks(stockIdentifier);
        if (stocks == null)
            return Response.status(Response.Status.FORBIDDEN).type("text/plain").build();

        DataSetDao<T, ?, ?> daoObject = this.getFreshDaoInstance();

        ParameterTool params = null;
        if (search)
            params = new ParameterTool(this.request);

        count = getDataSetsCount(daoObject, stocks, params, search, language, langFallback, allVersions, countOnly);

        SearchResultCount searchResultCount = new SearchResultCount();

        if (!countOnly)
            dataSets = getDataSets(daoObject, stocks, params, search, startIndex, pageSize, format, callback, language, langFallback, allVersions, countOnly, sortBy, sortOrder, searchResultCount);

        return this.getResponse(dataSets, distributed ? searchResultCount.getValue() : count, startIndex, pageSize, format, callback, language, langFallback);
    }

    protected List<? extends IDataSetListVO> getDataSets(DataSetDao<? extends T, ?, ?> daoObject, IDataStockMetaData[] stocks, ParameterTool params, final boolean search, final int startIndex, final int pageSize, String format,
                                                         String callback, final String language, final boolean langFallback, final boolean allVersions,
                                                         final boolean countOnly, final String sortBy, final boolean sortOrder, SearchResultCount searchResultCount) {

        List<? extends IDataSetListVO> dataSets = new ArrayList<IDataSetListVO>();

        if (search) {
            dataSets = daoObject.search(getDataSetType(), params, startIndex, pageSize, sortBy, sortOrder, language, langFallback, !allVersions, stocks, searchResultCount);
        } else {
            dataSets = daoObject.search(getDataSetType(), null, startIndex, pageSize, sortBy, sortOrder, language, langFallback, !allVersions, stocks, searchResultCount);
        }

        return dataSets;
    }

    protected int getDataSetsCount(DataSetDao<? extends T, ?, ?> daoObject, IDataStockMetaData[] stocks, ParameterTool params, final boolean search, final String language,
                                   final boolean langFallback, final boolean allVersions, final boolean countOnly) {

        if (search) {
            return (int) daoObject.searchResultCount(params, !allVersions, stocks);
        } else
            return daoObject.getCount(stocks, language, langFallback, !allVersions).intValue();
    }

    /**
     * Create a streaming output for a list of data sets
     *
     * @param list       the list with the data
     * @param totalCount the total count of result items (for pagination)
     * @param startIndex index of the first item in the passed list in the total data (for pagination)
     * @param pageSize   pagination page size
     * @param format     output format
     * @param callback   JSONP callback (may be <code>null</code> if format does not trigger JSON output)
     * @param language   the language to filter for
     * @return output for the client (which will be marshaled via JAXB)
     */
    protected Response getResponse(final List<? extends IDataSetListVO> list, final int totalCount, final int startIndex,
                                   final int pageSize, final String format, final String callback, final String language, final boolean langFallback) {

        if (StringUtils.equalsIgnoreCase(AbstractResource.FORMAT_JSON, format)) {
            return Response.ok(this.generateJSONP(list, totalCount, startIndex, pageSize, language, langFallback),
                    "application/json;charset=UTF-8").build();
        } else {
            return Response.ok(this.generateXML(list, totalCount, startIndex, pageSize, language, langFallback),
                    MediaType.APPLICATION_XML_TYPE).build();
        }
    }

    /**
     * Create a streaming output for a list of data sets
     *
     * @param list       the list with the data
     * @param totalCount the total count of result items (for pagination)
     * @param startIndex index of the first item in the passed list in the total data (for pagination)
     * @param pageSize   pagination page size
     * @return output for the client (which will be marshaled via JAXB)
     */
    private StreamingOutput generateJSONP(final List<? extends IDataSetListVO> list, final int totalCount,
                                          final int startIndex, final int pageSize, final String language, final boolean langFallback) {

        return new StreamingOutput() {

            @Override
            public void write(OutputStream outStream) throws IOException, WebApplicationException {

                GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
                JSONDatasetListSerializer.register(gsonBuilder, language, langFallback);

                OutputStreamWriter w = new OutputStreamWriter(outStream, "UTF-8");
                gsonBuilder.create().toJson(new JSONDatasetList(list, pageSize, startIndex, totalCount), w);
                w.flush();
            }
        };
    }

    /**
     * Create a streaming output for a list of data sets in ILCD data set format
     *
     * @param list       the list with the data
     * @param totalCount the total count of result items (for pagination)
     * @param startIndex index of the first item in the passed list in the total data (for pagination)
     * @param pageSize   pagination page size
     * @param language   the language to filter for
     * @return output for the client (which will be marshaled via JAXB)
     */
    private StreamingOutput generateXML(final List<? extends IDataSetListVO> list, final int totalCount,
                                        final int startIndex, final int pageSize, final String language, final boolean langFallback) {

        return new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                DataSetList resultList = new DataSetListAdapter(list, language, langFallback);

                String baseUrl = ConfigurationService.INSTANCE.getNodeInfo().getBaseURL();

                // set xlink:href attribute for each dataset
                for (DataSetVO ds : resultList.getDataSet()) {
                    // do not overwrite if already set
                    if (StringUtils.isNotBlank(ds.getHref()))
                        continue;
                    StringBuffer buf = new StringBuffer(baseUrl);
                    buf.append(AbstractDataSetResource.this.getURLSuffix(ds));
                    buf.append("/");
                    buf.append(ds.getUuidAsString());
                    if (StringUtils.isNotBlank(ds.getDataSetVersion())) {
                        buf.append("?version=");
                        buf.append(ds.getDataSetVersion());
                    }
                    ds.setHref(buf.toString());
                }

                resultList.setTotalSize(totalCount);
                resultList.setPageSize(pageSize);
                resultList.setStartIndex(startIndex);

                DatasetVODAO dao = new DatasetVODAO();

                try {
                    dao.marshal(resultList, out);
                } catch (JAXBException e) {
                    if (e.getCause() != null && e.getCause().getCause() instanceof SocketException) {
                        LOGGER.warn("exception occurred during marshalling - " + e);
                    } else {
                        LOGGER.error("error marshalling data", e);
                    }
                }
            }
        };
    }

    /**
     * @param dataset   data set instance
     * @param version   version of the data set (may be blank)
     * @param stockName stock name (may be blank)
     * @param uuid      UUID of data set
     */
    protected URI getDatasetDetailUrl(T dataset, String uuid, String version, String stockName, String language) {
        try {
            return new URI("../datasetdetail/" + this.modelType.name().toLowerCase() + ".xhtml?uuid=" + uuid + (StringUtils.isNotBlank(version) ? "&version=" + version
                    : "") + (StringUtils.isNotBlank(stockName) ? "&stock=" + stockName : "") + (StringUtils.isNotBlank(language) ? "&lang=" + language : ""));
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a data set by UUID string
     *
     * @param uuid       UUID string
     * @param versionStr version string (may be blank / omitted)
     * @return XML or HTML response for client
     */
    @GET
    @Path("{uuid}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public Response getDataSet(
            @Context HttpHeaders headers,
            @PathParam("stockIdentifier") String stockIdentifier,
            @PathParam("uuid") String uuid,
            @QueryParam(AbstractResource.PARAM_VERSION) String versionStr,
            @QueryParam("stock") String stockNameLegacy,
            @QueryParam("lang") String language,
            @QueryParam(AbstractResource.PARAM_FORMAT) @DefaultValue(AbstractResource.FORMAT_HTML) String format,
            @QueryParam(AbstractResource.PARAM_VIEW) @DefaultValue(AbstractResource.VIEW_DETAIL) String view,
            @QueryParam(AbstractResource.PARAM_GLAD_VIEW) String gladView) {

        if (stockIdentifier == null && stockNameLegacy != null) {
            stockIdentifier = stockNameLegacy;
        }

        IDataStockMetaData stock = this.getStockByIdentifier(stockIdentifier);

        IDataStockMetaData[] stocksToCheck = getStocks(stockIdentifier);
        if (stocksToCheck == null)
            return Response.status(Response.Status.FORBIDDEN).type("text/plain").build();

        try {
            // if request coming from GLAD requesting view, overide
            if ("true".equalsIgnoreCase(gladView)) {
                view = AbstractResource.VIEW_DETAIL;
                format = AbstractResource.FORMAT_HTML;
            }

            DataSetVersion version = null;
            if (versionStr != null) {
                try {
                    version = DataSetVersion.parse(versionStr);
                } catch (FormatException e) {
                    // nothing
                }
            }

            // fix uuid, if not in the right format
            GlobalRefUriAnalyzer analyzer = new GlobalRefUriAnalyzer(uuid);
            uuid = analyzer.getUuidAsString();

            DataSetDao<T, ?, ?> daoObject = this.getFreshDaoInstance();

            T dataset;
            if (version != null) {
                dataset = daoObject.getByUuidAndVersion(uuid, version);
            } else {
                dataset = daoObject.getByUuid(uuid);
            }

            // check the data stocks
            if (dataset != null && !this.isDatasetInStocks(dataset, stocksToCheck) && version == null) {
                // if the dataset has been retrieved without specifiying a specific version number and
                // there are no permissions for the very dataset returned, maybe a previous version
                // has matching permissions
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("no version was given and no permissions for the latest version " + dataset.getDataSetVersion()
                            + " of dataset " + uuid + "exist , looking for possible previous versions with matching permissions");

                boolean foundPrevious = false;

                if (daoObject.getOtherVersions(dataset) != null) {
                    for (T ds : daoObject.getOtherVersions(dataset)) {
                        if (this.isDatasetInStocks(ds, stocksToCheck)) {
                            dataset = ds;
                            versionStr = ds.getDataSetVersion();
                            version = ds.getVersion();
                            foundPrevious = true;
                            if (LOGGER.isTraceEnabled())
                                LOGGER.trace("found version " + versionStr);
                            break;
                        }
                    }
                }
                if (!foundPrevious) {
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("no alternative version was found");
                    LOGGER.debug("FORBIDDEN");
                    return Response.status(Response.Status.FORBIDDEN).type("text/plain").build();
                }
            }

            // TODO: replace velocity
            if (dataset == null) {
                String errorTitle = this.getDataSetTypeName() + " data set not found";
                String errorMessage = "A " + this.getDataSetTypeName() + " data set with the uuid " + uuid + " cannot be found in the database";
                if (format.equals(AbstractResource.FORMAT_HTML)) {
                    return Response.status(Response.Status.NOT_FOUND).entity(VelocityUtil.errorPage(this.context, errorTitle, errorMessage)).type(
                            "text/html").build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity(errorMessage).type("text/plain").build();
                }
            }

            // Local variable uuid defined in an enclosing scope must be final or effectively final
            final String uuid_final = uuid,
                    // httpHeader is removed if value is null
                    httpHeaderSupersedingKey = "X-soda4LCA-dataset-superseding",
                    httpHeaderSupersedingValue = Optional.ofNullable(dataset)
                            .map(DataSet::getCorrespondingDSDao)
                            .map(dao -> dao.getSupersedingDataSetVersion(uuid_final))
                            .map(DataSet::getDataSetVersion)
                            .orElse(null);

            /** Response switch board **/
            SecurityUtil.assertCanRead(dataset);
            switch (format.toLowerCase()) {
                case AbstractResource.FORMAT_JSON:
                    // SecurityUtil.assertCanExport(dataset);
                    return responseAsJSON(dataset, view)
                            .header(httpHeaderSupersedingKey, httpHeaderSupersedingValue)
                            .build();

                case AbstractResource.FORMAT_CSV:
                    // return the file as CSV format
                    SecurityUtil.assertCanExport(dataset);
                    return responseAsCSV(dataset, view)
                            .header(httpHeaderSupersedingKey, httpHeaderSupersedingValue)
                            .build();

                case AbstractResource.FORMAT_HTML:
                case AbstractResource.FORMAT_HTML_ALTERNATIVE:
                    return Response.seeOther(this.getDatasetDetailUrl(dataset, uuid, versionStr,
                            (stock == null ? null : stock.getName()), language)).build();

                case AbstractResource.FORMAT_XML:
                    // handle detail and metadata mode by returning the XML file itself
                    return responseAsXML(dataset, view)
                            .header(httpHeaderSupersedingKey, httpHeaderSupersedingValue)
                            .build();
                default:
                    // in all other cases we return the overview view as XML file
                    // velocity-generated overview
                    return Response
                            .ok(this.generateOverview(dataset, AbstractResource.FORMAT_XML), MediaType.APPLICATION_XML)
                            .header(httpHeaderSupersedingKey, httpHeaderSupersedingValue)
                            .build();
            }
        } catch (AuthorizationException e) {
            LOGGER.debug("UNAUTHORIZED ", e);
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

    }

    /**
     * <p>
     * Handle the different cases of views when asking for dataset in JSON format
     * </p>
     *
     * <p>
     * Only used in {@link #getDataSet(HttpHeaders, String, String, String, String, String, String, String, String)}
     * </p>
     *
     * @param dataset
     * @param view
     * @return Generate a JSON body based on the provided view parameters
     */
    private ResponseBuilder responseAsJSON(T dataset, String view) {
        DataSetJAXB dataSetJAXB;
        Object ds;
        ObjectMapper jsonMapper;


        switch (view) {
            case AbstractResource.VIEW_DETAIL:
            case AbstractResource.VIEW_FULL:
                // de.fzk.iai.ilcd.api.dataset.DataSet ds = new UnmarshalHelper().unmarshal(dataset);
                dataSetJAXB = (DataSetJAXB) ApplicationContextHolder.getApplicationContext().getBean("dataSetJAXB");
                ds = dataSetJAXB.unmarshal(dataset.getXmlFile().getContent());

                jsonMapper = newJsonMapper();
                try {
                    String jsonSTR = jsonMapper.writeValueAsString(ds);

                    return Response.ok(jsonSTR, MediaType.APPLICATION_JSON);
                } catch (JsonProcessingException e) {
                    LOGGER.debug("JsonProcessingException", e);
                    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN);
                }

            case AbstractResource.VIEW_EXTENDED:
                // de.fzk.iai.ilcd.api.dataset.DataSet ds = new UnmarshalHelper().unmarshal(dataset);
                dataSetJAXB = (DataSetJAXB) ApplicationContextHolder.getApplicationContext().getBean("dataSetJAXB");
                ds = dataSetJAXB.unmarshal(dataset.getXmlFile().getContent());

                jsonMapper = newJsonMapper();

                if (ds instanceof ProcessDataSet) {
                    ProcessDataSet pds = (ProcessDataSet) ds;
                    if (TypeOfProcessValue.EPD.equals(((Process) dataset).getType()))
                        augmentReferencesToOriginalEpds(pds);

                    SimpleModule exchangemodule = new SimpleModule();
                    exchangemodule.addSerializer(ExchangeType.class, new ExchangeTypeSerializer((ProcessDataSet) ds));
                    jsonMapper.registerModule(exchangemodule);
                }

                try {
                    String jsonSTR = jsonMapper.writeValueAsString(ds);

                    return Response.ok(jsonSTR, MediaType.APPLICATION_JSON);
                } catch (JsonProcessingException e) {
                    LOGGER.debug("JsonProcessingException", e);
                    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN);
                }

            case AbstractResource.VIEW_OVERVIEW:
                JsonObject json = new JsonObject();
                json.addProperty("uuid", dataset.getUuidAsString());
                json.addProperty("dataSetVersion", dataset.getDataSetVersion());
                json.addProperty("permanentUri", dataset.getPermanentUri());

                if (dataset instanceof Process)
                    json.addProperty("technologyDescription",
                            ((Process) dataset).getTechnologyDescription().getDefaultValue());

                return Response.ok(json.toString(), MediaType.APPLICATION_JSON);
            default:
                return Response.status(400).entity("No JSON representation for view: " + view);
        }
    }

    private void augmentReferencesToOriginalEpds(ProcessDataSet pds) {
        List<Object> anies = null;
        try {
            anies = pds.getModellingAndValidation().getDataSourcesTreatmentAndRepresentativeness().getOther().getAnies();
        } catch (NullPointerException npe) {
            LOGGER.trace(String.format("Failed to access anies of Process data set %s", pds.getProcessInformation().getDataSetInformation().getUUID()), npe);
            return;
        }
        if (anies == null || anies.size() < 1) {
            return; // returning quietly seems fine for now. This only works for epds anyway.
        }

        List<Object> aniesCopy = new ArrayList<>(anies);
        for (Object o : aniesCopy) {
            if (!(o instanceof JAXBElement))
                continue;
            JAXBElement<?> el = (JAXBElement<?>) o;
            if (el.getName() == null
                    || el.getName().getLocalPart() == null
                    || !el.getName().getLocalPart().toLowerCase(Locale.ROOT).contains("referenceToOriginalEpd".toLowerCase(Locale.ROOT)))
                continue;

            GlobalReferenceType grt = null;
            try {
                grt = (GlobalReferenceType) el.getValue();
                if (grt == null)
                    continue;
            } catch (ClassCastException | NullPointerException e) {
                continue;
            }
            JAXBElement<ResourceReference> elAugmented = new JAXBElement<>(el.getName(), ResourceReference.class, new ResourceReference(grt));
            anies.remove(el);
            anies.add(elAugmented);
        }
    }

    /**
     * Why this is not a singleton?
     *
     * <ol>
     * <li>I am not sure that conditionally injecting custom serializer is thread-safe</li>
     * <li>Exchange module need the current process as parameter for it's constructor</li>
     * <li>You can add a module to a ObjectMapper but you cannot remove it.</li>
     * </ol>
     * <p>
     * What you can do is reset/override all serializers inside a specific module by calling
     * <code>setSerializers</code>. I am not 100% certain that is suitable in this environment.
     *
     * @return
     */
    private ObjectMapper newJsonMapper() {
        SimpleModule qnameModule = new SimpleModule();
        qnameModule.addSerializer(QName.class, new QNameSerializer());


        ObjectMapper jsonMapper = JsonMapper.builder()
//				.annotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance(), true))
                .addModule(new JaxbAnnotationModule())
                .addModule(qnameModule)
//				.addModule(exchangemodule)
                .serializationInclusion(Include.NON_NULL)
                .serializationInclusion(Include.NON_EMPTY)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .addMixIn(JAXBElement.class, JAXBElementMixIn.class)
                .defaultDateFormat(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN))
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
//				.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
//				.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false)
                .build();
        return jsonMapper;
    }

    /**
     * <p>
     * Handle the different cases of views when asking for dataset in CSV format
     * </p>
     *
     * <p>
     * Only used in {@link #getDataSet(HttpHeaders, String, String, String, String, String, String, String, String)}
     * </p>
     *
     * @param dataset
     * @param view    not used, kept for consistency
     * @return CSV file
     */
    private ResponseBuilder responseAsCSV(T dataset, String view) {
        // StringCSV
//		final InputStream inputStream = generateSingleCSV(dataset);
        final StringWriter sw = generateSingleCSV(dataset);

        return Response.ok(sw.toString(), MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=" + dataset.getUuidAsString() + ".csv");
    }


    /**
     * <p>
     * Handle the different cases of views when asking for dataset in XML format.
     * </p>
     *
     * <p>
     * Returns an overview generate by a velocity as a fallback for absence of EXPORT permission.
     * </p>
     *
     * <p>
     * Only used in {@link #getDataSet(HttpHeaders, String, String, String, String, String, String, String, String)}
     * </p>
     *
     * @param dataset
     * @param view
     * @return XML file or velocity-generated overview
     */
    private ResponseBuilder responseAsXML(T dataset, String view) {

        String xmlSTR;
        switch (view) {
            case AbstractResource.VIEW_DETAIL:
            case AbstractResource.VIEW_FULL:
            case AbstractResource.VIEW_OVERVIEW:
            default:

                xmlSTR = dataset.getXmlFile().getContent();
                return Response.ok(xmlSTR, MediaType.APPLICATION_XML).header("Content-Disposition",
                        "attachment; filename=" + dataset.getUuidAsString() + ".xml");

//		case AbstractResource.VIEW_OVERVIEW:
//			de.fzk.iai.ilcd.api.dataset.DataSet ds = new UnmarshalHelper().unmarshal(dataset);
//
//			XmlMapper xmlMapper = XmlMapper.builder()
//					.addModule(new JaxbAnnotationModule())
//					.serializationInclusion(Include.NON_NULL)
//					.serializationInclusion(Include.NON_EMPTY)
//					.enable(SerializationFeature.INDENT_OUTPUT)
//					.build();
//			try {
//				xmlSTR = xmlMapper.writeValueAsString(ds);
//			} catch (JsonProcessingException e) {
//				LOGGER.debug("JsonProcessingException (XmlMapper)", e);
//				return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN);
//			}
//
//			if (SecurityUtil.hasExportPermission(dataset))
//				return Response.ok(xmlSTR, MediaType.APPLICATION_XML).header("Content-Disposition",
//						"attachment; filename=" + dataset.getUuidAsString() + ".xml");
//			// break;
//		default:
//			// if a browser is requesting a dataset that has no EXPORT
//			// permission, let's show at least the meta data
//			return Response.ok(this.generateOverview(dataset, AbstractResource.FORMAT_XML), MediaType.APPLICATION_XML);
        }
    }


    @HEAD
    @Path("{uuid}")
    @Produces({"application/xml", "text/html"})
    public Response existsDataSet(@PathParam("stockIdentifier") String stockIdentifier, @PathParam("uuid") String uuid,
                                  @QueryParam(AbstractResource.PARAM_VERSION) String versionStr, @QueryParam("stock") String stockNameLegacy,
                                  @QueryParam("lang") String language) {

        IDataStockMetaData stock = null;
        if (stockIdentifier == null && stockNameLegacy != null) {
            stockIdentifier = stockNameLegacy;
        }
        if (stockIdentifier != null) {
            stock = this.getStockByIdentifier(stockIdentifier);
            if (stock == null) {
                return Response.status(Response.Status.NOT_FOUND).type("text/plain").build();
            }
        }

        DataSetVersion version = null;
        if (versionStr != null) {
            try {
                version = DataSetVersion.parse(versionStr);
            } catch (FormatException e) {
                // nothing
            }
        }

        // fix uuid, if not in the right format
        GlobalRefUriAnalyzer analyzer = new GlobalRefUriAnalyzer(uuid);
        uuid = analyzer.getUuidAsString();

        DataSetDao<T, ?, ?> daoObject = this.getFreshDaoInstance();

        boolean exists;
        if (version != null) {
            exists = daoObject.existsByUuidAndVersion(uuid, version);
        } else {
            exists = daoObject.existsByUuid(uuid);
        }

        if (exists)
            return Response.ok().build();
        else
            return Response.noContent().build();
    }

    /**
     * Determine if data set is in one of the specified data stocks
     *
     * @param dataset       data set to check
     * @param stocksToCheck stocks to check for
     * @return <code>true</code> if data set is in one of the specified data stocks, <code>false</code> otherwise
     */
    private boolean isDatasetInStocks(T dataset, IDataStockMetaData[] stocksToCheck) {
        if (dataset != null && ArrayUtils.isNotEmpty(stocksToCheck)) {
            // first: root data stock
            for (IDataStockMetaData m : stocksToCheck) {
                if (m.getName().equals(dataset.getRootDataStock().getName())) {
                    return true;
                }
            }
            // second: non-root data stock
            for (IDataStockMetaData m : stocksToCheck) {
                for (DataStock ds : dataset.getContainingDataStocks()) {
                    if (m.getName().equals(ds.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get URL suffix for data set
     *
     * @param vo value object instance
     * @return URL suffix
     */
    protected String getURLSuffix(DataSetVO vo) {
        if (vo instanceof IProcessListVO) {
            return DatasetTypes.PROCESSES.getValue();
        } else if (vo instanceof IFlowListVO) {
            return DatasetTypes.FLOWS.getValue();
        } else if (vo instanceof IFlowPropertyListVO) {
            return DatasetTypes.FLOWPROPERTIES.getValue();
        } else if (vo instanceof IUnitGroupListVO) {
            return DatasetTypes.UNITGROUPS.getValue();
        } else if (vo instanceof ILCIAMethodListVO) {
            return DatasetTypes.LCIAMETHODS.getValue();
        } else if (vo instanceof ISourceListVO) {
            return DatasetTypes.SOURCES.getValue();
        } else if (vo instanceof IContactListVO) {
            return DatasetTypes.CONTACTS.getValue();
        } else if (vo instanceof ILifeCycleModelListVO) {
            return DatasetTypes.LIFECYCLEMODELS.getValue();
        } else {
            return null;
        }
    }

    /**
     * Generate overview. currently called statically with <code>type == xml</code>.
     *
     * @param dataset data set to generate overview for
     * @param type    type of data set
     * @return overview source to return to client
     */
    @Deprecated // Velocity is gradually being phased-out
    protected String generateOverview(T dataset, String type) {
        LOGGER.warn("Velocity's generateOverview is @Deprecated");
        VelocityContext velocityContext = VelocityUtil.getServicesContext(this.context);
        velocityContext.put("dataset", dataset);

        return VelocityUtil.parseTemplate(this.getXMLTemplatePath(), velocityContext);
    }

    /**
     * Generate data set detail view as HTML view
     *
     * @param dataset data set to generate HTML view for
     * @return HTML code to return to client
     */
    @Deprecated // Velocity is gradually being phased-out
    private String generateDataSetDetailAsHtml(T dataset) {
        LOGGER.warn("Velocity's generateDataSetDetailAsHtml is @Deprecated");
        VelocityContext velocityContext = VelocityUtil.getServicesContext(this.context);
        velocityContext.put("dataset", dataset);

        de.fzk.iai.ilcd.api.dataset.DataSet xmlDataset = new UnmarshalHelper().unmarshal(dataset);

        velocityContext.put("xmlDataset", xmlDataset);

        return VelocityUtil.parseTemplate(this.getHTMLDatasetDetailTemplatePath(), velocityContext);
    }

    /**
     * Generate data set detail view as CSV view
     *
     * @param dataset data set to generate CSV view for
     * @return CSV code to return to client
     */
    private StringWriter generateSingleCSV(T dataset) {

        StringWriter w = new StringWriter();
        CSVFormatter f = new CSVFormatter(DecimalSeparator.DOT);
        List<Process> dataSetList = new ArrayList<Process>();

        try {
            f.writeHeader(w);
            Process process = (Process) dataset;
            dataSetList.add(process);

            Map<String, ProductFlow> flowProperties = new HashMap<String, ProductFlow>();
            String uuid = process.getReferenceExchanges().get(0).getReference().getRefObjectId();
            ProductFlowDao pfdao = new ProductFlowDao();
            ProductFlow productFlow = pfdao.getByUuid(uuid);
            flowProperties.put(process.getUuid().getUuid(), productFlow);

            f.formatCSV(dataSetList, flowProperties, w);
//			in = org.apache.commons.io.IOUtils.toInputStream(w.toString(), "UTF-8");
        } catch (IOException e) {
            AbstractDataSetResource.LOGGER.error("Error in generating the CSV file. ", e);
        }
        return w;
    }

    /**
     * PUT method for updating or creating an instance of ContactResource
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes("application/xml")
    public void putXml(String content) {
    }

    /**
     * POST method for importing new process data set sent by form
     *
     * @param inputStream XML data to import (from form)
     * @param stockUuidFromHeader  UUID of the root stock to import to (or <code>null</code> for default), via request header
     * @param stockUuidFromForm  UUID of the root stock to import to (or <code>null</code> for default), via form
     * @return response for client
     */
    @POST
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes("multipart/form-data")
    public Response importByFileUpload(@HeaderParam("stock") String stockUuidFromHeader, @FormDataParam("file") InputStream inputStream, @FormDataParam("stock") String stockUuidFromForm,
                                       @FormDataParam("zipFileName") String zipFileName, @FormDataParam("lang") String language) {

        String stockUuid = StringUtils.isNotBlank(stockUuidFromHeader) ? stockUuidFromHeader : stockUuidFromForm;

        LOGGER.debug("trying to import into data stock {}", stockUuid);

        if (StringUtils.isNotBlank(zipFileName)) {
            CommonDataStockDao dsDao = new CommonDataStockDao();
            AbstractDataStock ads = null;

            if (StringUtils.isBlank(stockUuid))
                ads = dsDao.getById((new ConfigurationBean(ConfigurationService.INSTANCE.getContextPath())).getDefaultDataStockId());
            else
                ads = dsDao.getDataStockByUuid(stockUuid);

            if (ads == null)
                return Response.status(404).entity("Specified data stock not found").build();

            if (!(ads instanceof RootDataStock)) {
                return Response.serverError().build();
            }

            final RootDataStock rds = (RootDataStock) ads;

            try {
                SecurityUtil.assertCanImport(ads);
            } catch (AuthorizationException e) {
                return Response.status(403).build();
            }

            // TODO
            // - clean up, refactor dedicated code for ZIP upload
            // - use Java 8 lambda expression for starting new thread, requires upgrade to Spring 4

//			Runnable task2 = () -> { 
//				try {
//					importZipByInputStream(null, zipFileName, fileInputStream, rds );
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			};
//			 
//			new Thread(task2).start();

            // store ZIP locally first
            String fileName = ConfigurationService.INSTANCE.getUploadDirectory() + File.separator + zipFileName + "_" + System.currentTimeMillis() + ".zip";
            File targetFile = new File(fileName);

            try {
                if (!Files.exists(targetFile.toPath().getParent()))
                    Files.createDirectories(targetFile.toPath().getParent());
                java.nio.file.Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e1) {
                LOGGER.error("could not copy temporary ZIP file to {}", fileName, e1);
            }

            IOUtils.closeQuietly(inputStream);

            Runnable task1 = new Runnable() {

                @Override
                public void run() {
                    try {
                        importZipFile(null, zipFileName, targetFile.getAbsolutePath(), rds);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            Thread thread1 = new Thread(task1);
            thread1.start();

            return Response.status(202).build();
        } else
            return this.importByXml(inputStream, stockUuid, language);
    }

    /**
     * POST method for importing new process data set sent directly
     *
     * @param fileInputStream XML data to import
     * @param stockUuid       UUID of the root stock to import to (or <code>null</code> for default
     * @return response for client
     */
    @POST
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes("application/xml")
    public Response importByXml(InputStream fileInputStream, @HeaderParam("stock") String stockUuid, @HeaderParam("lang") String language) {
        try {
            T ds = this.importXml("POST import", fileInputStream, stockUuid);

            return Response.ok(this.getStreamingOutput(ds.getRootDataStock())).type(MediaType.APPLICATION_XML_TYPE).build();
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
     * Process uploaded XML as input stream
     *
     * @param inputStream
     *            stream with XML
     * @param stockUuid
     *            UUID of the root stock to import to (or <code>null</code> for default
     * @return response for client
     */

    /**
     * Get streaming output for data stock
     *
     * @param ads data stock instance
     * @return streaming output
     */
    protected StreamingOutput getStreamingOutput(AbstractDataStock ads) {
        final DataStockVO dsVo = DataStockListAdapter.getServiceApiVo(ads);
        final StreamingOutput sout = new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                ServiceDAO sDao = new ServiceDAO();
                try {
                    sDao.marshal(dsVo, out);
                } catch (JAXBException e) {
                    if (e.getCause().getCause() instanceof SocketException) {
                        AbstractDataSetResource.LOGGER.warn("exception occurred during marshalling - " + e);
                    } else {
                        AbstractDataSetResource.LOGGER.error("error marshalling data", e);
                    }
                }
            }
        };

        return sout;
    }

    /**
     * Process uploaded XML as input stream
     *
     * @param desc        description (mainly for logging)
     * @param inputStream stream with XML
     * @param stockUuid   UUID of the root stock to import to (or <code>null</code> for default
     * @return created data set
     * @throws IllegalAccessException on illegal access of resources
     * @throws IOException            on I/O errors
     */
    protected T importXml(String desc, InputStream inputStream, String stockUuid) throws IllegalAccessException, IOException {
        final CommonDataStockDao dao = new CommonDataStockDao();
        AbstractDataStock ads;

        if (StringUtils.isBlank(stockUuid))
            ads = dao.getById((new ConfigurationBean(ConfigurationService.INSTANCE.getContextPath())).getDefaultDataStockId());
        else {
            ads = dao.getDataStockByUuid(stockUuid);
            if (ads == null)
                throw new NotFoundException("Specified data stock not found");
            if (!(ads instanceof RootDataStock)) {
                throw new IllegalAccessException("Data sets can only be imported into root data stocks!");
            }
        }
        if (ads == null) {
            throw new IllegalArgumentException("Invalid root data stock UUID specified");
        }
        SecurityUtil.assertCanImport(ads);

        return this.importByInputStream(desc, this.modelType, inputStream, (RootDataStock) ads, null);
    }

    /**
     * Get the category systems
     *
     * @return list of category systems
     */
    @GET
    @Path("categorysystems")
    @Produces("application/xml")
    public StreamingOutput getCategorySystems() {
        ClassificationDao cDao = new ClassificationDao();

        final CategoryList cats = new CategoryList();
        for (String name : cDao.getCategorySystemNames()) {
            cats.addCategory(new ClassType(name));
        }
        return this.getCategoriesStreamingOutput(cats);
    }

    /**
     * Get the top categories
     *
     * @param stockIdentifier name of data stock
     * @param catSystem name of category system
     * @param sort      sort field
     * @return list of categories
     */
    @GET
    @Path("categories")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCategories(@PathParam("stockIdentifier") String stockIdentifier, @QueryParam("catSystem") String catSystem,
                                  @QueryParam("sort") final String sort, @QueryParam(AbstractResource.PARAM_FORMAT) String format, @QueryParam("lang") String language) {

        if (catSystem == null)
            catSystem = ConfigurationService.INSTANCE.getDefaultClassificationSystem();

        if (language == null)
            language = ConfigurationService.INSTANCE.getDefaultLanguage();

        IDataStockMetaData[] stocksToCheck = getStocks(stockIdentifier);
        if (stocksToCheck == null)
            return Response.status(Response.Status.FORBIDDEN).type("text/plain").build();

        DataSetDao<T, ?, ?> daoObject = this.getFreshDaoInstance();

        final CategoryList catList = new CategoryList();

        ParameterTool params = new ParameterTool(this.request);
        List<ClClass> cats = daoObject.getTopClasses(catSystem, params, stocksToCheck);

        if (sort != null && sort.equalsIgnoreCase(SORT_ID)) {
            Collections.sort(cats, new ClClassIdComparator());
        }

        for (ClClass clazz : cats) {
            catList.addCategory(new ClClassAdapter(clazz));
        }

        if (ConfigurationService.INSTANCE.isTranslateClassification() && !StringUtils.equalsIgnoreCase(language, ConfigurationService.INSTANCE
                .getDefaultLanguage())) {
            CategoryTranslator t = new CategoryTranslator(this.modelType, catSystem);
            for (ClassType cat : catList.getCategories()) {
                cat.setValue(t.translateTo(cat.getClassId(), language));
            }
        }

        if (StringUtils.equalsIgnoreCase(AbstractResource.FORMAT_JSON, format)) {
            return Response.ok(catList, MediaType.APPLICATION_JSON_TYPE)
                    .build();
        } else {
            return Response.ok(this.getCategoriesStreamingOutput(catList), MediaType.APPLICATION_XML_TYPE)
                    .build();
        }
    }

    /**
     * Get data sets in category or sub-categories
     *
     * @param stockIdentifier  name of data stock
     * @param category   list of categories
     * @param catSystem  category system
     * @param sort       sort field
     * @param startIndex start index (only valid for data set query)
     * @param pageSize   page size (only valid for data set query)
     * @param format     format for output
     * @param callback   JSONP callback
     * @return data sets in category or sub-categories
     */
    @GET
    @Path("categories/{category:.+}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDatasetsInCategoryOrSubcategories(@PathParam("stockIdentifier") String stockIdentifier, @DefaultValue("false") @QueryParam("search") final boolean search,
                                                         @PathParam("category") @Encoded String category, @QueryParam("catSystem") String catSystem, @QueryParam("sort") final String sort,
                                                         @DefaultValue("0") @QueryParam("startIndex") final int startIndex, @DefaultValue("500") @QueryParam("pageSize") final int pageSize,
                                                         @QueryParam(AbstractResource.PARAM_FORMAT) String format, @DefaultValue("fn") @QueryParam("callback") String callback,
                                                         @QueryParam("lang") String language, @DefaultValue("false") @QueryParam("langFallback") final boolean langFallback,
                                                         @DefaultValue("false") @QueryParam("allVersions") final boolean allVersions, @DefaultValue("false") @QueryParam("countOnly") final boolean countOnly,
                                                         @QueryParam("sortBy") final String sortBy, @DefaultValue("true") @QueryParam("sortOrder") final boolean sortOrder) {

        if (catSystem == null)
            catSystem = ConfigurationService.INSTANCE.getDefaultClassificationSystem();

        if (language == null)
            language = ConfigurationService.INSTANCE.getDefaultLanguage();

        IDataStockMetaData stock = null;
        if (stockIdentifier != null) {
            stock = this.getStockByIdentifier(stockIdentifier);
            if (stock == null) {
                return Response.status(Response.Status.FORBIDDEN).type("text/plain").build();
            }
        }

        ParameterTool params = null;
        params = new ParameterTool(this.request);

        IDataStockMetaData[] stocksToCheck = stock != null ? new IDataStockMetaData[]{stock} : this.getAvailableDataStocks();

        StringTokenizer tokenizer = new StringTokenizer(category, "/");

        List<String> categories = new ArrayList<String>();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("processing category '" + token + "' (raw), '" + SodaUtil.decode(token) + "' (decoded)" + SodaUtil.decode(SodaUtil.decode(token)) + "' (double decoded)");
            String cat = (ConfigurationService.INSTANCE.isClassificationEncodingFix() ? SodaUtil.decode(SodaUtil.decode(token)) : SodaUtil.decode(token));
            categories.add(cat);
        }

        if (categories.get(categories.size() - 1).equals("subcategories")) {
            categories.remove(categories.size() - 1);
            if (StringUtils.equalsIgnoreCase(AbstractResource.FORMAT_JSON, format)) {
                return Response.ok(this.getSubCategoriesList(categories, catSystem, params, (!allVersions), sort, language, stocksToCheck), "application/json;charset=UTF-8")
                        .build();
            } else {
                return Response.ok(this.getSubCategories(categories, catSystem, params, (!allVersions), sort, language, stocksToCheck), MediaType.APPLICATION_XML_TYPE)
                        .build();
            }
        } else {
            return this.getDatasetsInSubcategory(startIndex, pageSize, categories, catSystem, search, params, format, callback, language, langFallback, !allVersions, sortBy, sortOrder, stocksToCheck);
        }
    }

    /**
     * Get the sub categories
     *
     * @param categories            categories path
     * @param catSystem             category system name
     * @param mostRecentVersionOnly
     * @return sub categories
     */
    private StreamingOutput getSubCategories(List<String> categories, String catSystem, ValueParser params, boolean mostRecentVersionOnly, String sort, String language,
                                             IDataStockMetaData... stocks) {
        final CategoryList cats = getSubCategoriesList(categories, catSystem, params, mostRecentVersionOnly, sort, language,
                stocks);

        return this.getCategoriesStreamingOutput(cats);
    }

    /**
     * @param categories
     * @param catSystem
     * @param mostRecentVersionOnly
     * @param sort
     * @param language
     * @param stocks
     * @return
     */
    private CategoryList getSubCategoriesList(List<String> categories, String catSystem, ValueParser params, boolean mostRecentVersionOnly,
                                              String sort, String language, IDataStockMetaData... stocks) {
        DataSetDao<T, ?, ?> daoObject = this.getFreshDaoInstance();

        stocks = stocks != null ? stocks : this.getAvailableDataStocks();

        // translate incoming
        categories = translateCategories(categories, catSystem, language);

        final CategoryList cats = new CategoryList();
        List<ClClass> classes = daoObject.getSubClasses(catSystem, categories, params, mostRecentVersionOnly, stocks);

        if (sort != null && sort.equalsIgnoreCase(SORT_ID)) {
            Collections.sort(classes, new ClClassIdComparator());
        }

        for (ClClass clazz : classes) {
            cats.addCategory(new ClClassAdapter(clazz));
        }

        // translate outgoing
        if (ConfigurationService.INSTANCE.isTranslateClassification() && !StringUtils.equalsIgnoreCase(language, ConfigurationService.INSTANCE
                .getDefaultLanguage())) {
            CategoryTranslator t = new CategoryTranslator(this.modelType, catSystem);
            for (ClassType cat : cats.getCategories()) {
                cat.setValue(t.translateTo(cat.getClassId(), language));
            }
        }
        return cats;
    }

    private List<String> translateCategories(List<String> categories, String catSystem, String fromLanguage) {
        if (ConfigurationService.INSTANCE.isTranslateClassification() && !StringUtils.equalsIgnoreCase(fromLanguage, ConfigurationService.INSTANCE
                .getDefaultLanguage())) {
            CategoryTranslator t = new CategoryTranslator(this.modelType, catSystem);
            List<String> translatedCategories = new ArrayList<String>();
            for (String cat : categories) {
                translatedCategories.add(t.translateFrom(cat, fromLanguage));
            }
            categories = translatedCategories;
        }
        return categories;
    }

    /**
     * Get all data sets in specified sub category path
     *
     * @param startIndex  start index
     * @param pageSize    page size
     * @param categories  categories path
     * @param catSystem   name of category system
     * @param language    the language to filter for
     * @return all data sets in specified sub category path
     * @see #getDatasetsInCategoryOrSubcategories(String, boolean, String, String, String, int, int, String, String, String, boolean, boolean, boolean, String, boolean)
     */
    private Response getDatasetsInSubcategory(int startIndex, int pageSize, List<String> categories, String catSystem,
                                              final boolean search, ParameterTool params, String format, String callback, String language,
                                              boolean langFallback, boolean mostRecentVersionOnly, final String sortBy, final boolean sortOrder,
                                              IDataStockMetaData... stocks) {
        stocks = stocks != null ? stocks : this.getAvailableDataStocks();

        DataSetDao<T, ?, ?> daoObject = this.getFreshDaoInstance();
        List<T> dataSets = new ArrayList<T>();

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("processing categories '" + categories + "'");

        if (ConfigurationService.INSTANCE.isTranslateClassification()) {
            // translate incoming
            categories = translateCategories(categories, catSystem, language);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("translated to '" + categories + "'");
        }

        int count = (int) daoObject.getNumberByClass(catSystem, categories, search, params, stocks, language, langFallback, mostRecentVersionOnly);
        dataSets = daoObject.getByClass(catSystem, categories, search, params, sortBy, sortOrder, stocks, language, langFallback, mostRecentVersionOnly, startIndex, pageSize);

        return this.getResponse(dataSets, count, startIndex, pageSize, format, callback, language, langFallback);
    }

    /**
     * Get streaming output for category list
     *
     * @param cats category list
     * @return streaming output for category list
     */
    private StreamingOutput getCategoriesStreamingOutput(final CategoryList cats) {
        return new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                ServiceDAO dao = new ServiceDAO();
                try {
                    dao.marshal(cats, out);
                } catch (JAXBException e) {

                    LOGGER.error("error marshalling data", e);
                }
            }
        };
    }

    /**
     * Get current request
     *
     * @return current request
     */
    protected HttpServletRequest getRequest() {
        return this.request;
    }

    /**
     * Get all available data stocks
     *
     * @return available data stocks
     */
    protected IDataStockMetaData[] getAvailableDataStocks() {
        AvailableStockHandler availableStocksHandler = new AvailableStockHandler();
        availableStocksHandler.setDirty(new DirtyFlagBean());

        return availableStocksHandler.getAllStocksMeta().toArray(new IDataStockMetaData[0]);
    }

    /**
     * Get data stock by identifier (name or UUID)
     *
     * @param stockIdentifier identifier
     * @return available data stock or <code>null</code>
     */
    protected IDataStockMetaData getStockByIdentifier(String stockIdentifier) {
        AvailableStockHandler availableStocksHandler = new AvailableStockHandler();
        availableStocksHandler.setDirty(new DirtyFlagBean());

        return availableStocksHandler.getStockByIdentifier(stockIdentifier);
    }

    /**
     * Get the context / URI information
     *
     * @return context / URI information
     */
    protected UriInfo getContext() {
        return this.context;
    }

    /**
     * Import data set from input stream
     *
     * @param desc              description (mainly for logging)
     * @param type              type of data set
     * @param inStream          the input stream
     * @param rds               the root data stock to import to
     * @param digitFileProvider digital file provider, may be <code>null</code> for all non-{@link Source} data sets
     * @return created data set
     * @throws IOException on I/O errors
     */
    @SuppressWarnings("unchecked")
    protected T importByInputStream(String desc, DataSetType type, InputStream inStream, RootDataStock rds, AbstractDigitalFileProvider digitFileProvider)
            throws IOException {

        DataSetImporter importer = new DataSetImporter();
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);

        try {
            T foo = (T) importer.importDataSet(type, desc, inStream, out, rds, digitFileProvider);
            if (foo != null) {
                LOGGER.info("data set successfully imported.");
                LOGGER.info("{}", writer.getBuffer());
                out.println("data set successfully imported.");
                out.flush();
                return foo;
            } else {
                LOGGER.error("Cannot import data set");
                LOGGER.error("output is: {}", writer.getBuffer());
                throw new IllegalStateException(writer.getBuffer().toString());
            }
        } catch (Exception e) {
            LOGGER.error("cannot import data set");
            LOGGER.error("exception is: ", e);
            throw new IllegalArgumentException(writer.getBuffer().toString());
        }
    }

    /**
     * Import ZIO from input stream
     *
     * @param desc        description (mainly for logging)
     * @param zipFileName name of the ZIP file
     * @param zipFile    the ZIP file
     * @param rds         the root data stock to import to
     * @throws IOException on I/O errors
     */
    protected void importZipFile(String desc, String zipFileName, String zipFile, RootDataStock rds) throws IOException {
        DataSetZipImporter importer = new DataSetZipImporter(new DataSetImporter());
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);

        try {
            long start = System.currentTimeMillis();

            importer.importZipFile(zipFile, out, rds);

            long stop = System.currentTimeMillis();

            long duration = (stop - start) / 1000;

            LOGGER.info("ZIP file successfully imported in " + duration + "s");
            LOGGER.info("{}", writer.getBuffer());
            out.println("data set successfully imported.");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Cannot import data set");
            LOGGER.error("output is: {}", writer.getBuffer());
            throw new IllegalStateException(writer.getBuffer().toString());
        }
    }

    /**
     * Return the eligible data stock(s) for a given stock identifier (UUID).
     * <p>
     * Returns null if there's no such data stock.
     *
     * @param stockIdentifier
     * @return
     */
    protected IDataStockMetaData[] getStocks(String stockIdentifier) {
        IDataStockMetaData stock = null;
        if (stockIdentifier != null) {
            stock = this.getStockByIdentifier(stockIdentifier);
            if (stock == null) {
                return null;
            }
        }

        IDataStockMetaData[] stocks = stock != null ? new IDataStockMetaData[]{stock} : this.getAvailableDataStocks();
        return stocks;
    }

    /**
     * Assigns a dataset with given dataset UUID to logical stock with given UUID.
     *
     * @param stockIdentifier the UUID of the data stock
     * @param datasetId       The UUID of the dataset
     * @param dependencies    The dependencies mode for assigning
     * @param versionStr         The version of the dataset
     * @return A response containing a HTTP code and message string
     */
    protected Response assignDataSetToStock(String stockIdentifier, String datasetId, Integer dependencies, String versionStr) {
        CommonDataStockDao dao = new CommonDataStockDao();

        AbstractDataStock stock = dao.getDataStockByUuid(stockIdentifier);

        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();

        DataSetDao<T, ?, ?> dsDao = this.getFreshDaoInstance();

        DataSet ds;

        DependenciesUtil dependenciesUtil = new DependenciesUtil();

        if (stock == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Data stock is not correctly set.").type("text/plain").build();
        }

        if (stock.isRoot()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Data stock must be a logical data stock.").type("text/plain").build();
        }

        try {
            DataStockMetaData dsm = new DataStockMetaData(stock);
            SecurityUtil.assertCan(dsm, ProtectionType.WRITE);

        } catch (AuthorizationException ae) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("You are not permitted to operate on this data stock.").type("text/plain").build();
        }
        DataSetVersion version = null;
        if (versionStr != null) {
            try {
                version = DataSetVersion.parse(versionStr);
                ds = dsDao.getByUuidAndVersion(datasetId, version);

                if (ds == null) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity("Dataset with given UUID and version is not existing in database.").type("text/plain").build();
                }
            } catch (FormatException e) {
                if (LOGGER.isDebugEnabled()) {
                    e.printStackTrace();
                }
                return Response.status(Response.Status.UNAUTHORIZED).entity("Version is not in a correct format.").type("text/plain").build();
            }
        } else {
            ds = dsDao.getByUuid(datasetId);
            if (ds == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Dataset with given UUID is not existing in database.").type("text/plain").build();
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("dataset is contained in: " + ds.getContainingDataStocksAsString());
        }

        if (ds.getContainingDataStocks().contains(stock)) {
            return Response.ok("Dataset is already assigned to data stock.").build();
        }

        if (dependencies != null && (dependencies < 0 || dependencies >= 4)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Dependencies mode is not legal.").type("text/plain").build();
        }

        boolean added = false;
        try {
            t.begin();

            ds.addToDataStock((DataStock) stock);
            em.merge(ds);

            if (dependencies != null) {
                DependenciesMode mode = DependenciesMode.fromValue(dependencies);
                if (mode != null && mode != DependenciesMode.NONE) {
                    LOGGER.debug("Dependencies mode is set.");
                    for (DataSet dependency : dependenciesUtil.getDependencyCluster(ds, mode, true)) {

                        LOGGER.debug("adding dependency to stock.");
                        boolean oldAdded = added;
                        added = dependency.addToDataStock((DataStock) stock);

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("dependency is added: " + added);
                        }

                        //We want to check whether any dependency was assigned.
                        if (added == false) {
                            added = oldAdded;
                        }
                        // save new state
                        em.merge(dependency);
                    }
                }
            }

            stock.setModified(true);
            em.merge(stock);
            t.commit();
        } catch (Exception e) {
            LOGGER.warn("dataset could not be added to data stock!");
            t.rollback();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Dataset could not be assigned to data stock.").type("text/plain").build();
        }

        try {
            dao.merge(stock);
        } catch (MergeException e) {
            if (LOGGER.isDebugEnabled()) {
                e.printStackTrace();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Dataset could not be assigned to data stock.").type("text/plain").build();
        }

        if (added) {
            return Response.ok("Dataset with its dependencies has been assigned to data stock.").build();
        }
        return Response.ok("Dataset has been assigned to data stock.").build();
    }


    /**
     * Removes a given dataset that has been assigned to given datastock.
     *
     * @param stockIdentifier The UUID of the datastock to operate on
     * @param datasetId       The UUID of the dataset to be removed
     * @param dependencies    The dependencies mode for the dataset represented as an Integer
     * @param versionStr      The version of the dataset represented as String
     * @return
     */
    protected Response removeDataSetFromStock(String stockIdentifier, String datasetId, Integer dependencies, String versionStr) {
        CommonDataStockDao dao = new CommonDataStockDao();

        AbstractDataStock stock = dao.getDataStockByUuid(stockIdentifier);

        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();
        DataStockMetaData dsm;

        DataSetDao<T, ?, ?> dsDao = this.getFreshDaoInstance();

        DataSet ds = null;
        DataSetVersion version = null;

        DependenciesUtil dependenciesUtil = new DependenciesUtil();

        if (stock == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Data stock is not correctly set.").type("text/plain").build();
        }

        if (stock.isRoot()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Data stock must be a logical data stock.").type("text/plain").build();
        }

        try {
            dsm = new DataStockMetaData(stock);
            SecurityUtil.assertCan(dsm, ProtectionType.WRITE);

        } catch (AuthorizationException ae) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("You are not permitted to operate on this data stock.").type("text/plain").build();
        }

        if (versionStr != null) {
            try {
                version = DataSetVersion.parse(versionStr);
                ds = dsDao.getByUuidAndVersion(datasetId, version);

                if (ds == null || !ds.getContainingDataStocks().contains(stock)) {
                    return Response.ok("Dataset with given UUID and version is not assigned to data stock.").build();
                }

            } catch (FormatException e) {
                if (LOGGER.isDebugEnabled()) {
                    e.printStackTrace();
                }
                return Response.status(Response.Status.UNAUTHORIZED).entity("Version is not in a correct format.").type("text/plain").build();
            }
        } else {
            ds = dsDao.getByUuid(datasetId);
            if (ds == null || !ds.getContainingDataStocks().contains(stock)) {
                return Response.ok("Dataset with given UUID is not assigned to data stock.").build();
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("dataset is contained in: " + ds.getContainingDataStocksAsString());
        }

        if (dependencies != null && (dependencies < 0 || dependencies >= 4)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Dependencies mode is not legal.").type("text/plain").build();
        }

        boolean removed = false;
        try {
            t.begin();

            ds.removeFromDataStock((DataStock) stock);
            em.merge(ds); // save new state

            // detach dependencies

            if (dependencies != null) {
                DependenciesMode mode = DependenciesMode.fromValue(dependencies);
                if (mode != null && mode != DependenciesMode.NONE) {
                    LOGGER.debug("Dependencies mode is set.");
                    for (DataSet dependency : DependenciesUtil.getDependencyCluster(ds, mode, true)) {
                        LOGGER.trace("Dependencies exist.");
                        try {
                            if (LOGGER.isDebugEnabled())
                                LOGGER.debug("detaching dependency: " + dependency.getUuidAsString() + " "
                                        + dependency.getDefaultName());
                        } catch (Exception e) {
                        }

                        boolean oldRemoved = removed;
                        removed = dependency.removeFromDataStock((DataStock) stock);

                        em.merge(dependency); // save new state

                        //We want to check whether any dependency was removed
                        if (removed == false) {
                            removed = oldRemoved;
                        }
                    }
                }
            }
            stock.setModified(true);
            t.commit();
        } catch (Exception e) {

            t.rollback();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Dataset could not be removed from data stock.").type("text/plain").build();
        }

        try {
            dao.merge(stock);
        } catch (MergeException e) {
            if (LOGGER.isDebugEnabled()) {
                e.printStackTrace();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Dataset could not be removed from data stock.").type("text/plain").build();
        }

        if (removed) {
            return Response.ok("Dataset with its dependencies has been removed from data stock.").build();
        }
        return Response.ok("Dataset has been removed from data stock.").build();
    }
}