package de.iai.ilcd.rest;

import com.google.gson.Gson;
import de.fzk.iai.ilcd.service.client.impl.ServiceDAO;
import de.fzk.iai.ilcd.service.client.impl.vo.SimpleList;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractResource {

    /**
     * URL parameter key for view type
     */
    public static final String PARAM_VIEW = "view";
    /**
     * URL parameter key for view type
     */
    public static final String PARAM_GLAD_VIEW = "gladview";
    /**
     * URL parameter key for format type
     */
    public static final String PARAM_FORMAT = "format";
    /**
     * URL parameter key for count
     */
    public static final String PARAM_COUNT = "count";
    /**
     * URL parameter key for version
     */
    public static final String PARAM_VERSION = "version";
    /**
     * URL parameter value for HTML format type
     */
    public static final String FORMAT_HTML = "html";
    /**
     * URL parameter value for PrimeUI format type
     */
    public static final String FORMAT_JSON = "json";
    /**
     * URL parameter value for CSV format type
     */
    public static final String FORMAT_CSV = "csv";
    /**
     * URL parameter value for XLS format type
     */
    public static final String FORMAT_XLS = "xls";
    /**
     * URL parameter value for HTML format type
     */
    public static final String FORMAT_HTML_ALTERNATIVE = "htmlalt";
    /**
     * URL parameter value for XML format type
     */
    public static final String FORMAT_XML = "xml";
    /**
     * URL parameter value for overview view type
     */
    public static final String VIEW_OVERVIEW = "overview";
    /**
     * URL parameter value for dataset detail view type
     *
     * @deprecated use {@link #VIEW_DETAIL}
     */
    @Deprecated
    public static final String VIEW_FULL = "full";
    /**
     * URL parameter value for dataset detail view type
     */
    public static final String VIEW_DETAIL = "detail";
    /**
     * URL parameter value for dataset extended view type
     * <p>
     * At the time of writing, it's only used for JSON extended
     * view of the Exchanges.
     */
    public static final String VIEW_EXTENDED = "extended";
    /**
     * URL parameter value for natural sort
     */
    public static final String SORT_NATSORT = "nat";
    /**
     * URL parameter value for meta data view type
     */
    public static final String VIEW_METADATA = "metadata";
    /**
     * URL parameter key for indicating sort order
     */
    public static final String SORT = "sort";
    /**
     * URL parameter value for indicating sort order by id
     */
    public static final String SORT_ID = "id";
    /**
     * URL parameter value for indicating sort order by name
     */
    public static final String SORT_NAME = "name";
    /**
     * URL parameter value for indicating distributed search
     */
    public static final String DISTRIBUTED = "distributed";
    /**
     * URL parameter value for indicating max timeout in seconds
     */
    public static final String TIMEOUT = "timeout";
    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AbstractResource.class);

    // subsume both IntegerList and StringList
    protected Response getListResponse(SimpleList sl, String format) {
        if (StringUtils.equalsIgnoreCase(AbstractResource.FORMAT_JSON, format)) {
            return Response.ok(this.generateListJSONP(sl), "application/json;charset=UTF-8").build();
        } else {
            return Response.ok(this.generateListXML(sl), MediaType.APPLICATION_XML_TYPE).build();
        }
    }

    private StreamingOutput generateListXML(final SimpleList sl) {
        return new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                ServiceDAO dao = new ServiceDAO();
                try {
                    dao.marshal(sl, out);
                } catch (JAXBException e) {
                    if (e.getCause() instanceof SocketException) {
                        LOGGER.warn("exception occurred during marshalling - " + e);
                    } else {
                        LOGGER.error("error marshalling data", e);
                    }
                }
            }
        };
    }


    // TODO: leverage Gson's naming policy
    // TODO: do we REALLY need Gson here?
    private StreamingOutput generateListJSONP(final SimpleList sl) {
        return new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                Gson gson = new Gson();
                OutputStreamWriter w = new OutputStreamWriter(out, "UTF-8");

                Map<String, List<?>> result = new HashMap<String, List<?>>();
                result.put(sl.getIdentifier(), sl.getPayload());

                w.write(gson.toJson(result));
                w.flush();
                w.close();
            }
        };

    }
}
