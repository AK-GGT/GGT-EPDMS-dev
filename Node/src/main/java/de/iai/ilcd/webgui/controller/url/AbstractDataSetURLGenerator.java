package de.iai.ilcd.webgui.controller.url;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.rest.ConvertResource;
import de.iai.ilcd.rest.ProcessResource;
import de.iai.ilcd.webgui.controller.ConfigurationBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 * Base implementation for URL generators. Please note that all generated
 * URLs are absolute URLs within context path (start with <code>/</code>).
 * If your element requires context path to be part of the URL, use the
 * context path provided by {@link ConfigurationBean}.
 *
 * @param <T> Type to generate URLs for
 */
public abstract class AbstractDataSetURLGenerator<T extends DataSet> extends AbstractURLGenerator {

    /**
     * Constant for REST-based directory
     */
    public final static String REST_BASE_DIR = "/resource/";
    /**
     * Type URL part for being first word
     */
    private final String typeUrlPartFW;
    /**
     * Type URL part for not being first word
     */
    private final String typeUrlPartNFW;
    /**
     * Resource URL virtual directory
     */
    private final String resourceDir;

    /**
     * Create the generator
     *
     * @param typeUrlPart type part in URL (upper/lower case first char will be done via conversion)
     */
    public AbstractDataSetURLGenerator(URLGeneratorBean urlBean, String typeUrlPart, String resourceDir) {
        this(urlBean, WordUtils.uncapitalize(typeUrlPart), WordUtils.capitalize(typeUrlPart), resourceDir);
    }

    /**
     * Create the generator
     *
     * @param typeUrlPartFW  type URL part for being first word
     * @param typeUrlPartNFW type URL part for not being first word
     * @param resourceDir    resource URL virtual directory
     */
    public AbstractDataSetURLGenerator(URLGeneratorBean urlBean, String typeUrlPartFW, String typeUrlPartNFW, String resourceDir) {
        super(urlBean);
        this.typeUrlPartFW = typeUrlPartFW;
        this.typeUrlPartNFW = typeUrlPartNFW;
        this.resourceDir = resourceDir;
    }

    /**
     * Create a matching DAO instance (required for cross linking of data sets from facelets)
     *
     * @return fresh DAO instance
     */
    public abstract DataSetDao<T, ?, ?> createDaoInstance();

    /**
     * Get the resource URL virtual directory
     *
     * @return the resource URL virtual directory
     */
    protected String getResourceDir() {
        return this.resourceDir;
    }

    /**
     * Get the detail URL with {@link #getCurrentStockName() current stock}
     *
     * @param object object to generate URL for
     * @return generated URL
     */
    public String getDetail(T object) {
        return this.getDetail(object, this.getCurrentStockName());
    }

    /**
     * Get the detail URL
     *
     * @param object    object to generate URL for
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getDetail(T object, String stockName) {
        if (object != null) {
            return "/show" + this.typeUrlPartNFW + ".xhtml?uuid=" + this.encodeURL(object.getUuidAsString())
                    + "&version=" + this.encodeURL(object.getDataSetVersion()) + "&"
                    + this.getStockURLParam(stockName);
        }
        return "/show" + this.typeUrlPartNFW + ".xhtml" + this.getStockURLParam(stockName, true);
    }

    /**
     * Get the detail URL
     *
     * @param object    object to generate URL for
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getDetail(String uuid, String version, String stockName) {
        StringBuilder buf = new StringBuilder("/show");
        if (uuid != null) {
            buf.append(this.typeUrlPartNFW);
            buf.append(".xhtml?uuid=");
            buf.append(this.encodeURL(uuid));
            buf.append("&version=");
            buf.append(this.encodeURL(version));
            buf.append("&");
            buf.append(this.getStockURLParam(stockName));
            return buf.toString();
        }

        buf.append(this.typeUrlPartNFW);
        buf.append(".xhtml");
        buf.append(this.getStockURLParam(stockName, true));
        return buf.toString();
    }

    /**
     * Get the listing URL with {@link #getCurrentStockName() current stock}
     *
     * @return generated URL
     */
    public String getList() {
        return this.getList(this.getCurrentStockName());

    }

    /**
     * Get the listing URL
     *
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getList(String stockName) {
        return "/" + this.typeUrlPartFW + "List.xhtml" + super.getStockURLParam(stockName, true);
    }

    /**
     * Get search form URL with {@link #getCurrentStockName() current stock}
     *
     * @return generated URL
     */
    public String getSearchForm() {
        return this.getSearchForm(this.getCurrentStockName());
    }

    /**
     * Get search form URL
     *
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getSearchForm(String stockName) {
        return "/" + this.typeUrlPartFW + "Search.xhtml" + super.getStockURLParam(stockName, true);
    }

    /* ======================= */
    /* ==== RESOURCE URLS ==== */
    /* ======================= */

    /**
     * Get resource URL for XML view
     *
     * @param object object to generate URL for
     * @return generated URL
     */
    public String getResourceDetailXml(T object) {
        return this.getResourceDetail(object, "xml", null);
    }


    /**
     * Get resource URL for ZIP dependencies.
     * <p>
     * Returns a URL that points to a REST endpoint that generates a zip archive
     * containing the dependencies for a given process
     *
     * @param Process expecting a process object.
     * @return generated URL that reaches the REST endpoint defined in
     * ProcessResource.java
     * @see ProcessResource#getDependencies(String, String)
     */

    public String getProcessDependenciesZIP(Process object) {
        return String.format("/resource/processes/%s/zipexport?version=%s",
                object.getUuidAsString(),
                object.getDataSetVersion());
    }

    /**
     * Get resource URL for converting a XML to XLSX
     *
     * @param Process expecting a process object.
     * @return generated URL that reaches the REST endpoint defined in
     * ProcessResource.java
     * @see ConvertResource#XML2XLSX(String, String)
     * @see ConvertResource#askConversionService(String, String, String)
     */
    public String convert2XLSX(Process object) {
        return String.format("/resource/convert/%s/xlsx?format=XML&version=%s",
                object.getUuidAsString(),
                object.getDataSetVersion());
    }

    /**
     * Get resource URL for ZIP dependencies.
     * <p>
     * Returns a URL that points to a REST endpoint that generates a zip archive
     * containing the dependencies for a given process
     *
     * @param LifeCycleModel expecting a life-cycle model object.
     * @return generated URL that reaches the REST endpoint defined in
     * LifeCycleModelResource.java
     * @see LifeCycleModelResource#getDependencies(String, String)
     */

    public String getLCMDependenciesZIP(LifeCycleModel object) {
        return String.format("/resource/lifecyclemodels/%s/zipexport?version=%s",
                object.getUuidAsString(),
                object.getDataSetVersion());
    }

    /**
     * Get resource URL for HTML view
     *
     * @param object object to generate URL for
     * @return generated URL
     */
    public String getResourceDetailHtml(T object) {
        return this.getResourceDetail(object, "html", null);
    }

    /**
     * Get resource URL for HTML view
     *
     * @param object object to generate URL for
     * @return generated URL
     */
    public String getResourceDetailHtml(String uuid, String version) {
        return this.getResourceDetail(uuid, version, "html", null);
    }

    /**
     * Get resource URL for HTML view
     *
     * @param object object to generate URL for
     * @return generated URL
     */
    public String getResourceDetailHtmlWithStock(T object) {
        return this.getResourceDetail(object, "html", this.getCurrentStockName());
    }

    /**
     * Get resource URL for HTML view
     *
     * @param object object to generate URL for
     * @return generated URL
     */
    public String getResourceDetailHtmlWithStock(String uuid, String version, String stockName) {
        return this.getResourceDetail(uuid, version, "html", stockName);
    }

    /**
     * Get resource URL for <b>alternate</b> HTML view
     *
     * @param object object to generate URL for
     * @return generated URL
     */
    public String getResourceDetailHtmlAltWithStock(T object) {
        return this.getResourceDetail(object, "htmlalt", this.getCurrentStockName());
    }

    /**
     * Get resource URL for type listing
     *
     * @return generated URL
     */
    public String getResourceList() {
        return "/resource/" + this.resourceDir + "/";
    }

    /**
     * Get resource URL for type listing
     *
     * @param startIndex start index of listing
     * @param pageSize   page size of listing
     * @return generated URL
     */
    public String getResourceList(int startIndex, int pageSize) {
        return this.getResourceList() + "?startIndex=" + Integer.toString(startIndex) + "&amp;pageSize=" + Integer.toString(pageSize);
    }

    /* ==================== */
    /* ==== ADMIN URLS ==== */
    /* ===================== */

    /**
     * Get the URL to the list to manage a data set type (on current stock name)
     *
     * @return generated URL
     */
    public String getManageList() {
        return this.getManageList(this.getCurrentStockName());
    }

    /**
     * Get the URL to the list to manage a data set type
     *
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getManageList(String stockName) {
        return "/admin/datasets/manage" + this.typeUrlPartNFW + "List.xhtml" + super.getStockURLParam(stockName, true);
    }

    /* ========================== */
    /* ==== MISC AND HELPERS ==== */
    /* ========================== */

    /**
     * Get resource URL
     *
     * @param object    object to generate URL for
     * @param format    format (xml or html)
     * @param stockName include stock in URL (leave blank in order to not include stock in URL)
     * @return generated URL
     */
    private String getResourceDetail(T object, String format, String stockName) {
        if (object != null) {
            return getResourceDetail(object.getUuidAsString(), object.getDataSetVersion(), format, stockName);
        }
        return null;
    }

    private String getResourceDetail(String uuid, String version, String format, String stockName) {
        if (uuid != null) {
            return "/resource/" + this.resourceDir + "/" + uuid + "?format=" + (format == null ? "html" : format)
                    + (StringUtils.isNotBlank(version) ? "&amp;version=" + version : "")
                    + (StringUtils.isNotBlank(stockName) ? "&amp;" + super.getStockURLParam(stockName, false) : "");
        }
        return null;
    }

}
