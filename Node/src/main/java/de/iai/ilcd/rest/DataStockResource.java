package de.iai.ilcd.rest;

import com.google.gson.GsonBuilder;
import de.fzk.iai.ilcd.service.client.impl.ServiceDAO;
import de.fzk.iai.ilcd.service.client.impl.vo.DataStockVO;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.adapter.DataStockListAdapter;
import de.iai.ilcd.model.dao.CommonDataStockDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.DataStockMetaData;
import de.iai.ilcd.model.datastock.ExportType;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.webgui.controller.admin.export.DataExportController;
import de.iai.ilcd.webgui.controller.util.ExportMode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.List;

/**
 * REST web service for data stocks
 */
@Component
@Path("datastocks")
public class DataStockResource {

    public final static String PARAM_FORMAT = "format";
    /**
     * URL parameter value for PrimeUI format type
     */
    public final static String FORMAT_JSON = "json";
    /**
     * URL parameter value for XML format type
     */
    public final static String FORMAT_XML = "xml";
    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(DataStockResource.class);
    @Autowired
    private DataExportController exportController;

    /**
     * Get data stocks in both formats
     *
     * @param format can be XML or JSON
     * @return a response object
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataStocks(@DefaultValue(FORMAT_XML) @QueryParam(AbstractResource.PARAM_FORMAT) String format) {

        if (StringUtils.equalsIgnoreCase(AbstractResource.FORMAT_JSON, format)) {
            return Response.ok(this.getDataStocksJSONP(), "application/json;charset=UTF-8").build();
        } else {
            return Response.ok(this.getDataStocksXML(), MediaType.APPLICATION_XML_TYPE).build();
        }

    }

    /**
     * Get a data stock in XML or JSON format
     *
     * @param stockIdentifier stock identifier (name or UUID)
     * @param format          format of the response (XML or JSON)
     * @return a response object
     */
    @GET
    @Path("{stockIdentifier}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataStock(@PathParam("stockIdentifier") String stockIdentifier, @DefaultValue(FORMAT_XML) @QueryParam(PARAM_FORMAT) String format) {
        try {
            if (StringUtils.equalsIgnoreCase(FORMAT_JSON, format)) {
                return Response.ok(this.getDataStockJSON(stockIdentifier), "application/json;charset=UTF-8").build();
            } else {
                return Response.ok(this.getDataStockXML(stockIdentifier), MediaType.APPLICATION_XML).build();
            }
        } catch (AuthorizationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("permission denied").type("text/plain").build();
        }
    }

    /**
     * Get list of data stocks
     *
     * @return an instance of javax.ws.rs.core.StreamingOutput
     */
    protected StreamingOutput getDataStocksXML() {
//		UserAccessBean user = new UserAccessBean();
        List<AbstractDataStock> stocks = SecurityUtil.getCurrentUser().getReadableStocks();

        final DataStockListAdapter adapter = new DataStockListAdapter(stocks);
        return new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws WebApplicationException {
                ServiceDAO dao = new ServiceDAO();
                dao.setRenderSchemaLocation(true);
                try {
                    dao.marshal(adapter, out);
                } catch (JAXBException e) {
                    DataStockResource.LOGGER.error("error marshalling data", e);
                    // if ( e.getCause().getCause() instanceof SocketException )
                    // {
                    // DataStockResource.LOGGER.warn( "exception occurred during
                    // marshalling - " + e );
                    // }
                    // else {
                    // DataStockResource.LOGGER.error( "error marshalling data",
                    // e );
                    // }
                }
            }
        };

    }

    /**
     * Get data stock in JSON format
     *
     * @return an instance of javax.ws.rs.core.StreaminOutput
     */
    protected StreamingOutput getDataStocksJSONP() {

//		UserAccessBean user = new UserAccessBean();
        List<AbstractDataStock> stocks = SecurityUtil.getCurrentUser().getReadableStocks();

        final DataStockListAdapter adapter = new DataStockListAdapter(stocks);
        return new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                GsonBuilder gsonBuilder = new GsonBuilder();

                OutputStreamWriter w = new OutputStreamWriter(out);
                gsonBuilder.create().toJson(adapter, w);
                w.flush();
            }
        };
    }

    /**
     * Get data stock
     *
     * @param stockIdentifier stock identifier (name or UUID)
     * @return an instance of javax.ws.rs.core.StreamingOutput
     */
    protected StreamingOutput getDataStockXML(String stockIdentifier) {

        CommonDataStockDao stockDao = new CommonDataStockDao();
        AbstractDataStock ads = stockDao.getDataStockByIdentifier(stockIdentifier);

        SecurityUtil.assertCanRead(ads);

        final DataStockVO dsVo = DataStockListAdapter.getServiceApiVo(ads);

        return new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws WebApplicationException {
                ServiceDAO dao = new ServiceDAO();
                dao.setRenderSchemaLocation(true);
                try {
                    dao.marshal(dsVo, out);
                } catch (JAXBException e) {
                    DataStockResource.LOGGER.error("error marshalling data", e);
                }
            }
        };
    }

    /**
     * Get a data stock
     *
     * @param stockIdentifier stock identifier (name or UUID)
     * @return an instance of javax.ws.rs.core.StreamingOutput
     */
    protected StreamingOutput getDataStockJSON(String stockIdentifier) {

        CommonDataStockDao stockDao = new CommonDataStockDao();
        AbstractDataStock ads = stockDao.getDataStockByIdentifier(stockIdentifier);

        SecurityUtil.assertCanRead(ads);

        final DataStockVO dsVo = DataStockListAdapter.getServiceApiVo(ads);

        return new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                GsonBuilder gsonBuilder = new GsonBuilder();

                OutputStreamWriter w = new OutputStreamWriter(out);
                gsonBuilder.create().toJson(dsVo, w);
                w.flush();

            }
        };
    }

    /**
     * Export data stock as ZIP file
     *
     * @param stockIdentifier stock identifier (name or UUID)
     * @return an instance of javax.ws.rs.core.StreamingOutput
     */
    @GET
    @Path("{stockIdentifier}/export")
    @Produces("application/zip")
    public Response export(@PathParam("stockIdentifier") String stockIdentifier) {
        return export(stockIdentifier, ExportType.ZIP, ExportMode.LATEST_ONLY);
    }

    /**
     * Export data stock as ZIP file
     *
     * @param stockIdentifier stock identifier (name or UUID)
     * @return an instance of javax.ws.rs.core.StreamingOutput
     */
    @GET
    @Path("{stockIdentifier}/exportCSV")
    @Produces("text/plain")
    public Response exportCSV(@PathParam("stockIdentifier") String stockIdentifier, @QueryParam("decimalSeparator") String decimalSeparator) {
        if (ConfigurationService.INSTANCE.isEnableCSVExport()) {
            if (!StringUtils.isBlank(decimalSeparator) && decimalSeparator.equals("comma")) {
                return export(stockIdentifier, ExportType.CSV_EPD_C, ExportMode.LATEST_ONLY_GLOBAL);
            } else
                return export(stockIdentifier, ExportType.CSV_EPD, ExportMode.LATEST_ONLY_GLOBAL);
        } else
            return Response.status(501).type("text/plain").build();
    }

    private Response export(String stockIdentifier, ExportType type, ExportMode mode) {

        CommonDataStockDao stockDao = new CommonDataStockDao();

        AbstractDataStock ads = stockDao.getDataStockByIdentifier(stockIdentifier);

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("exporting datastock " + ads.getName() + " as " + type.getValue() + " " + type.ordinal());

        try {
            SecurityUtil.assertCanExport(ads);
        } catch (AuthorizationException e1) {
            return Response.status(Response.Status.FORBIDDEN).type("text/plain").build();
        }

        InputStream in = null;

        if (!ads.getExportTag(type, mode).isModified()) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("serving cached file " + ads.getExportTag(type, mode).getFile());
            try {
                in = new FileInputStream(ads.getExportTag(type, mode).getFile());
            } catch (IOException e) {
                LOGGER.info("cached file not found, generating fresh one");
                in = doExport(ads, type);
            }
        } else {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("cached file is not up-to-date, generating fresh one");
            in = doExport(ads, type);
        }

        final InputStream inputStream = in;

        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                IOUtils.copy(inputStream, out);
            }
        };

        return Response.ok(stream).header("Content-Disposition", "attachment; filename=" + ads.getName() + DataExportController.timeStampSuffix() + type.getValue())
                .build();
    }

    private InputStream doExport(AbstractDataStock ads, ExportType type) {
        exportController.setExportMode(ExportMode.LATEST_ONLY);
        exportController.setStock(new DataStockMetaData(ads));
        switch (type) {
            case ZIP:
                return exportController.getFile().getStream();
            case CSV_EPD:
            case CSV_EPD_C: {
                return exportController.getCSVFile(type).getStream();
            }
            default:
                return null;
        }

    }

}
