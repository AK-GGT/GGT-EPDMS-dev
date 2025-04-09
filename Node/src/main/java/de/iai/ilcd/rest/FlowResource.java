package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.model.IDataSetListVO;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.webgui.controller.admin.export.DataExportController;
import de.iai.ilcd.webgui.controller.util.ExportMode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.view.ParameterTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Web Service for Flow
 */
@Component
@Path("flows")
public class FlowResource extends AbstractDataSetResource<Flow> {

    private static final Logger LOGGER = LogManager.getLogger(FlowResource.class);

    @Autowired
    private DataExportController dec;

    public FlowResource() {
        super(DataSetType.FLOW, ILCDTypes.FLOW);
    }

    /**
     * Retrieves all flows datasets (elementary, product, waste and other)
     *
     * @param search     search trigger
     * @param startIndex start index
     * @param pageSize   page size
     * @param format     format
     * @param callback   JSONP callback
     * @param stockName  name of data stock
     * @return a response object
     */
    @Override
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataSets(@PathParam("stockIdentifier") String stockIdentifier, @DefaultValue("false") @QueryParam("search") final boolean search,
                                @DefaultValue("false") @QueryParam("distributed") final boolean distributed,
                                @DefaultValue("0") @QueryParam("startIndex") final int startIndex, @DefaultValue("500") @QueryParam("pageSize") final int pageSize,
                                @QueryParam(AbstractResource.PARAM_FORMAT) String format, @DefaultValue("fn") @QueryParam("callback") String callback,
                                @QueryParam("lang") final String language, @DefaultValue("false") @QueryParam("langFallback") final boolean langFallback,
                                @DefaultValue("false") @QueryParam("allVersions") final boolean allVersions, @DefaultValue("false") @QueryParam("countOnly") final boolean countOnly,
                                @QueryParam("sortBy") final String sortBy, @DefaultValue("true") @QueryParam("sortOrder") final boolean sortOrder) {

        List<IDataSetListVO> dataSets = new ArrayList<IDataSetListVO>();
        Integer count;

        IDataStockMetaData[] stocks = getStocks(stockIdentifier);
        if (stocks == null)
            return Response.status(Response.Status.FORBIDDEN).type("text/plain").build();

        DataSetDao<ProductFlow, ?, ?> pfDaoObject = new ProductFlowDao();
        DataSetDao<ElementaryFlow, ?, ?> efDaoObject = new ElementaryFlowDao();

        ParameterTool params = null;
        if (search)
            params = new ParameterTool(this.getRequest());

        int efCount = getDataSetsCount(efDaoObject, stocks, params, search, language, langFallback, allVersions, countOnly);
        int pfCount = getDataSetsCount(pfDaoObject, stocks, params, search, language, langFallback, allVersions, countOnly);

        // To page through all flows, we simulate a big list: (all) elementary flows first, product flows second.
        // The start indices need to be interpreted accordingly, as do the page sizes.
        int efStartIndex = Math.max(0, startIndex); // start counting rows at the elementary flow rows
        int pfStartIndex = Math.max(0, startIndex - efCount); // respect offset for counting product flow rows
        int efPageSize = Math.max(0, Math.min(pageSize, efCount - efStartIndex)); // either pageSize or what's left of the elementary flows (or nothing)
        int pfPageSize = Math.max(0, Math.min(pageSize - efPageSize, pfCount - pfStartIndex)); // either what's left of the page or what's left of the product flows (or nothing)

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("ef count / startIndex / pageSize: " + efCount + " / " + efStartIndex + " / " + efPageSize);
            LOGGER.trace("pf count / startIndex / pageSize: " + pfCount + " / " + pfStartIndex + " / " + pfPageSize);
        }

        if (!countOnly) {

            // Only forward to the elementary flow dao if we're actually looking for elementary flows.
            if (efPageSize > 0) {
                List<? extends IDataSetListVO> efDataSets = getDataSets(efDaoObject, stocks, params, search, efStartIndex, efPageSize, format, callback, language, langFallback, allVersions, countOnly, sortBy, sortOrder, null);
                dataSets.addAll(efDataSets);
            }

            // Only forward to the product flow dao if we're actually looking for product flows.
            if (pfPageSize > 0) {
                List<? extends IDataSetListVO> pfDataSets = getDataSets(pfDaoObject, stocks, params, search, pfStartIndex, pfPageSize, format, callback, language, langFallback, allVersions, countOnly, sortBy, sortOrder, null);
                dataSets.addAll(pfDataSets);
            }
        }

        int totalCount = efCount + pfCount; // total count of all available flow data sets
        return this.getResponse(dataSets, totalCount, startIndex, pageSize, format, callback, language, langFallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected URI getDatasetDetailUrl(Flow dataset, String uuid, String version, String stockName, String language) {
        try {
            return new URI("../datasetdetail/" + (dataset instanceof ElementaryFlow ? "elementary" : "product") + "Flow.xhtml?uuid=" + uuid
                    + (StringUtils.isNotBlank(version) ? "&version=" + version : "") + (StringUtils.isNotBlank(stockName) ? "&stock=" + stockName : "")
                    + (StringUtils.isNotBlank(language) ? "&lang=" + language : ""));
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get availability of producers for specified flow (processes with this flow as {@link ExchangeDirection#OUTPUT
     * output} flow)
     *
     * @param uuid UUID of the flow
     * @return empty {@link Response} with status {@link Status#OK OK} if producers for specified UUID are available,
     * else with status {@link Status#NO_CONTENT NO_CONTENT}
     */
    @HEAD
    @Path("{uuid}/producers")
    @Produces("text/plain")
    public Response getAvailabilityOfProducers(@PathParam("uuid") String uuid) {
        return this.getAvailabilityOfInOutputProcesses(uuid, ExchangeDirectionValues.OUTPUT);
    }

    /**
     * Get availability of consumers for specified flow (processes with this flow as {@link ExchangeDirection#INPUT
     * input} flow)
     *
     * @param uuid UUID of the flow
     * @return empty {@link Response} with status {@link Status#OK OK} if consumers for specified UUID are available,
     * else with status {@link Status#NO_CONTENT NO_CONTENT}
     */
    @HEAD
    @Path("{uuid}/consumers")
    @Produces("text/plain")
    public Response getAvailabilityOfConsumers(@PathParam("uuid") String uuid) {
        return this.getAvailabilityOfInOutputProcesses(uuid, ExchangeDirectionValues.INPUT);
    }

    /**
     * Get HEAD response for input (consumer) or output (producer) request
     *
     * @param flowUuid  UUID of the flow in question
     * @param direction the direction of the exchange
     * @return empty {@link Response} with status {@link Status#OK OK} if processes for specified UUID and direction are
     * available, else with status {@link Status#NO_CONTENT NO_CONTENT}
     * @see #getAvailabilityOfProducers(String)
     * @see #getAvailabilityOfConsumers(String)
     */
    private Response getAvailabilityOfInOutputProcesses(String flowUuid, ExchangeDirectionValues direction) {
        ProcessDao dao = new ProcessDao();
        Response.Status status;
        if (dao.getProcessesForExchangeFlowCount(flowUuid, direction) > 0) {
            status = Response.Status.OK;
        } else {
            status = Response.Status.NO_CONTENT;
        }
        return Response.status(status).type("text/plain").build();
    }

    /**
     * Get availability of producers for specified flow (processes with this flow as {@link ExchangeDirection#OUTPUT
     * output} flow)
     *
     * @param uuid       UUID of the flow
     * @param startIndex start index
     * @param pageSize   page size
     * @param format     format
     * @param callback   callback for JSONP
     * @return empty {@link Response} with status {@link Status#OK OK} if producers for specified UUID are available,
     * else with status {@link Status#NO_CONTENT NO_CONTENT}
     */
    @GET
    @Path("{uuid}/producers")
    @Produces({"application/xml", "application/javascript"})
    public Response getProducers(@PathParam("uuid") String uuid, @DefaultValue("0") @QueryParam("startIndex") final int startIndex,
                                 @DefaultValue("500") @QueryParam("pageSize") final int pageSize, @QueryParam(AbstractResource.PARAM_FORMAT) String format,
                                 @DefaultValue("fn") @QueryParam("callback") String callback, @QueryParam("lang") String language,
                                 @DefaultValue("false") @QueryParam("langFallback") final boolean langFallback) {

        return this.getInOutputProcesses(uuid, ExchangeDirectionValues.OUTPUT, startIndex, pageSize, format, callback, language, langFallback);
    }

    /**
     * Get availability of consumers for specified flow (processes with this flow as {@link ExchangeDirection#INPUT
     * input} flow)
     *
     * @param uuid       UUID of the flow
     * @param startIndex start index
     * @param pageSize   page size
     * @param format     format
     * @param callback   callback for JSONP
     * @return empty {@link Response} with status {@link Status#OK OK} if consumers for
     * specified UUID are available,
     * else with status {@link Status#NO_CONTENT NO_CONTENT}
     */
    @GET
    @Path("{uuid}/consumers")
    @Produces({"application/xml", "application/javascript"})
    public Response getConsumers(@PathParam("uuid") String uuid, @DefaultValue("0") @QueryParam("startIndex") final int startIndex,
                                 @DefaultValue("500") @QueryParam("pageSize") final int pageSize, @QueryParam(AbstractResource.PARAM_FORMAT) String format,
                                 @DefaultValue("fn") @QueryParam("callback") String callback, @QueryParam("lang") String language,
                                 @DefaultValue("false") @QueryParam("langFallback") final boolean langFallback) {

        return this.getInOutputProcesses(uuid, ExchangeDirectionValues.INPUT, startIndex, pageSize, format, callback, language, langFallback);
    }

    /**
     * Get GET response for input (consumer) or output (producer) request
     *
     * @param flowUuid   UUID of the flow
     * @param direction  the direction of the exchange
     * @param startIndex index of the first item (for pagination)
     * @param pageSize   pagination page size
     * @return created output
     * @see #getResponseForDatasetListVOList(List, int, int, int, String, String)
     */
    private Response getInOutputProcesses(String flowUuid, ExchangeDirectionValues direction, int startIndex, int pageSize, String format, String callback,
                                          String language, boolean langFallback) {
        ProcessDao dao = new ProcessDao();
        long count = dao.getProcessesForExchangeFlowCount(flowUuid, direction);
        List<Process> lst = dao.getProcessesForExchangeFlow(flowUuid, direction, startIndex, pageSize);
        if (CollectionUtils.isEmpty(lst)) {
            return Response.noContent().build();
        }
        return super.getResponse(lst, (int) count, startIndex, pageSize, format, callback, language, langFallback);
    }

    /**
     * Get the ancestor of specified product flow
     *
     * @param uuid        UUID of flow in question
     * @param productType <code>nothing | specific | generic</code>
     * @return ancestors of specified product flow
     */
    @GET
    @Path("{uuid}/ancestor")
    @Produces("application/xml")
    public Response getAncestor(@PathParam("uuid") String uuid, @QueryParam("productType") String productType) {
        ProductFlowDao pflowDao = new ProductFlowDao();
        Boolean specific = this.parseProductType(productType);
        ProductFlow pFlow = pflowDao.getAncestor(uuid, specific);

        if (pFlow == null) {
            return Response.status(Response.Status.NO_CONTENT).entity("No ancestor found for provided product flow UUID").type(MediaType.TEXT_PLAIN).build();
        } else {
            return Response.ok(this.generateOverview(pFlow, AbstractResource.FORMAT_XML), MediaType.APPLICATION_XML).build();
        }

    }

    /**
     * Get the ancestors of specified product flow
     *
     * @param uuid     UUID of flow in question
     * @param maxDepth max depth to follow
     * @param format   format
     * @param callback callback for JSONP
     * @return ancestors of specified product flow
     */
    @GET
    @Path("{uuid}/ancestors")
    @Produces({"application/xml", "application/javascript"})
    public Response getAncestors(@PathParam("uuid") String uuid, @DefaultValue("0") @QueryParam("depth") final int maxDepth,
                                 @QueryParam(AbstractResource.PARAM_FORMAT) String format, @DefaultValue("fn") @QueryParam("callback") String callback,
                                 @QueryParam("lang") String language, @DefaultValue("false") @QueryParam("langFallback") final boolean langFallback) {

        ProductFlowDao pflowDao = new ProductFlowDao();
        List<ProductFlow> lst = pflowDao.getAncestors(uuid, maxDepth);
        return super.getResponse(lst, (int) lst.size(), 0, lst.size(), format, callback, language, langFallback);
    }

    /**
     * Get the descendants of specified product flow
     *
     * @param uuid     UUID of flow in question
     * @param maxDepth max depth to follow
     * @param format   format
     * @param callback callback for JSONP
     * @return descendants of specified product flow
     */
    @GET
    @Path("{uuid}/descendants")
    @Produces({"application/xml", "application/javascript"})
    public Response getDescendants(@PathParam("uuid") String uuid, @DefaultValue("0") @QueryParam("depth") final int maxDepth,
                                   @QueryParam(AbstractResource.PARAM_FORMAT) String format, @DefaultValue("fn") @QueryParam("callback") String callback,
                                   @QueryParam("lang") String language, @DefaultValue("false") @QueryParam("langFallback") final boolean langFallback) {
        ProductFlowDao pflowDao = new ProductFlowDao();
        List<ProductFlow> lst = pflowDao.getDescendants(uuid, maxDepth);
        return super.getResponse(lst, (int) lst.size(), 0, lst.size(), format, callback, language, langFallback);
    }

    /**
     * Get the products
     *
     * @param startIndex  start index
     * @param pageSize    page size
     * @param productType <code>nothing | specific | generic</code>
     * @param format      format
     * @param callback    callback for JSONP
     * @return products
     */
    @GET
    @Path("products")
    @Produces("application/xml")
    public Response getProducts(@PathParam("stockIdentifier") String stockIdentifier, @QueryParam("startIndex") final int startIndex, @DefaultValue("500") @QueryParam("pageSize") final int pageSize,
                                @QueryParam("productType") String productType, @QueryParam(AbstractResource.PARAM_FORMAT) String format,
                                @DefaultValue("fn") @QueryParam("callback") String callback, @QueryParam("lang") String language,
                                @DefaultValue("false") @QueryParam("langFallback") final boolean langFallback) {

        IDataStockMetaData[] stocks = getStocks(stockIdentifier);
        if (stocks == null)
            return Response.status(Response.Status.FORBIDDEN).type("text/plain").build();

        ProductFlowDao dao = new ProductFlowDao();
        Boolean specific = this.parseProductType(productType);

        return super.getResponse(dao.getProducts(stocks, specific, startIndex, pageSize), dao.getProductsCount(stocks, specific).intValue(),
                startIndex, pageSize, format, callback, language, langFallback);
    }

    @GET
    @Path("products/export")
    @Produces("application/zip")
    @SuppressWarnings("unchecked")
    public Response exportProducts(@PathParam("stockIdentifier") String stockIdentifier) {

        IDataStockMetaData[] stocks = getStocks(stockIdentifier);
        if (stocks == null)
            return Response.status(Response.Status.FORBIDDEN).type("text/plain").build();

        ProductFlowDao dao = new ProductFlowDao();

        List<? extends DataSet> products = dao.getDataSets(stocks, null, true, 0, dao.getProductsCount(stocks, null).intValue());

        dec.setExportMode(ExportMode.LATEST_ONLY);

        try {
            File zip = dec.export((List<DataSet>) products).toFile();

            if (zip != null) {
                FileInputStream is = new FileInputStream(zip);

                StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream out) throws IOException, WebApplicationException {
                        IOUtils.copy(is, out);
                    }
                };

                return Response.ok(stream).header("Content-Disposition", "attachment; filename=products.zip").build();
            }
        } catch (Exception e) {
            LOGGER.error("error exporting products", e);
        }
        return null;
    }

    /**
     * Parse product type from URL (all ignoring case):
     * <ul>
     * <li><code>specific</code> &rArr; {@link Boolean#TRUE}</li>
     * <li><code>generic</code> &rArr; {@link Boolean#FALSE}</li>
     * <li><i>everything else</i> &rArr; <code>null</code></li>
     * <li></li>
     * </ul>
     *
     * @param productType product type to parse
     * @return parsed product type
     */
    private Boolean parseProductType(String productType) {
        productType = StringUtils.lowerCase(productType);
        if ("specific".equals(productType)) {
            return Boolean.TRUE;
        }
        if ("generic".equals(productType)) {
            return Boolean.FALSE;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataSetDao<Flow, ?, ?> getFreshDaoInstance() {
        return new FlowDaoWrapper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXMLTemplatePath() {
        return "/xml/flow.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDataSetTypeName() {
        return StringUtils.lowerCase(DataSetType.FLOW.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Flow> getDataSetType() {
        return Flow.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHTMLDatasetDetailTemplatePath() {
        return "/html/flow.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean userRequiresDatasetDetailRights() {
        return false;
    }

}
