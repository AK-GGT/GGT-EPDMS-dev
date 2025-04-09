package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.api.app.common.IMultiLang;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.model.IDataSetVO;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.GlobalRefUriAnalyzer;
import de.iai.ilcd.model.common.exception.DatasetNotInSelectedStockException;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.common.exception.InvalidDatasetException;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.util.SodaUtil;
import de.iai.ilcd.util.UnmarshalHelper;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.ConfigurationBean;
import de.iai.ilcd.webgui.controller.admin.LoginHandler;
import de.iai.ilcd.webgui.controller.url.AbstractDataSetURLGenerator;
import de.iai.ilcd.webgui.controller.url.SourceURLGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.AuthorizationException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handler for DataSet view
 *
 * @param <I> Type of value object that this handler provides
 * @param <T> Type of model object that this handler provides
 * @param <D> Type of the Data Access Object that is being used by this handler
 * @param <A> API type that is being used by this handler
 */
public abstract class AbstractDataSetHandler<I extends IDataSetVO, T extends DataSet, D extends DataSetDao<T, ?, I>, A extends de.fzk.iai.ilcd.api.dataset.DataSet> extends AbstractHandler {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -3151934201431439509L;

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AbstractDataSetHandler.class);
    /**
     * DAO for data retrieval
     */
    private final D daoInstance;
    /**
     * The resource directory
     */
    private final String resourceDir;
    /**
     * ILCD API Type for current handler
     */
    private final ILCDTypes apiType;
    /**
     * Instance to be hold
     */
    private I dataSet;
    /**
     * {@link DataSet#getId() Data set id} as string
     */
    private String dataSetIdString = null;
    /**
     * {@link DataSet#getUuid() Data set uuid} as string
     */
    private String dataSetUuidString = null;
    /**
     * {@link DataSet#getVersion() Data set version} as string
     */
    private String dataSetVersionString = null;
    /**
     * Source node as string
     */
    private String sourceNodeIdString = null;
    /**
     * All other versions
     */
    private List<T> otherVersions;
    /**
     * Unmarshalled XML data set (only loaded if unmarshallXml set to true when
     * {@link #postViewParamInit(boolean unmarshallXml)} is called)
     */
    private A xmlDataset;

    /**
     * Configuration bean
     */
    @ManagedProperty(value = "#{conf}")
    private ConfigurationBean conf;

    /**
     * Stock selection managed bean
     */
    @ManagedProperty(value = "#{stockSelection}")
    private StockSelectionHandler stockSelection;

    @ManagedProperty(value = "#{loginHandler}")
    private LoginHandler loginHandler;

    /**
     * ID of the registry
     */
    private Long registryId;

    /**
     * UUID of the registry
     */
    private String registryUUID;

    /**
     * Initialize handler
     *
     * @param daoInstance matching DAO
     * @param resourceDir the directory for the RESTful service (e.g. <code>processes</code>)
     * @param apiType     ILCD API Type for current handler
     */
    public AbstractDataSetHandler(D daoInstance, String resourceDir, ILCDTypes apiType) {
        super();
        if (daoInstance == null) {
            throw new IllegalArgumentException("daoInstance must not be null!");
        }
        if (resourceDir == null) {
            throw new IllegalArgumentException("resourceDir must not be null!");
        }
        if (apiType == null) {
            throw new IllegalArgumentException("apiType must not be null!");
        }
        /*
         * Java-7-style:
         * Objects.requireNonNull( daoInstance, "daoInstance must not be null!" );
         * Objects.requireNonNull( resourceDir, "resourceDir must not be null!" );
         * Objects.requireNonNull( apiType, "apiType must not be null!" );
         */
        this.daoInstance = daoInstance;
        this.resourceDir = resourceDir;
        this.apiType = apiType;
    }

    /**
     * Called when a local data set was loaded successfully by {@link #postViewParamInit()}.
     * Override this method when this information is required in sub-classes.
     *
     * @param dataset loaded data set
     */
    protected void datasetLoaded(T dataset) {
        // No operation
    }

    /**
     * @throws AbortProcessingException if processing shall be aborted
     * @see #postViewParamInit(boolean)
     */
    public final void postViewParamInit() throws AbortProcessingException {
        this.postViewParamInit(false);
    }

    /**
     * Method to be called after view parameters have been initialized. This is actually loading the data set! What
     * happens:
     * <ul>
     * <li>{@link #getDataSetIdString() database ID} specified: local data set loaded by database ID (all other
     * parameter are being ignored!)</li>
     * <li>only {@link #getDataSetUuidString() UUID} specified: local data set loaded by UUID, most recent version</li>
     * <li>only {@link #getDataSetUuidString() UUID} and {@link #getDataSetVersionString() version} specified: local
     * data set loaded by UUID and given version</li>
     * <li>only {@link #getDataSetUuidString() UUID} and {@link #getSourceNodeIdString() source node} specified: remote
     * data set loaded by UUID, most recent version</li>
     * <li style="color: gray;"><i>only {@link #getDataSetUuidString() UUID} and {@link #getDataSetVersionString()
     * version} and {@link #getSourceNodeIdString()
     * source node} specified: remote data set loaded by UUID and given version (once ServiceAPI supports this)</i></li>
     * </ul>
     *
     * @param unmarshallXml indicates if XML object shall be unmarshalled
     * @throws AbortProcessingException in case of errors
     */
    @SuppressWarnings("unchecked")
    public final void postViewParamInit(boolean unmarshallXml) throws AbortProcessingException {
        if (this.dataSet == null) {
            this.otherVersions = null;

            T tmp = null;
            DataSetVersion version = null;

            // ID specified => load local
            if (!StringUtils.isBlank(this.dataSetIdString)) {
                tmp = this.daoInstance.getByDataSetId(this.dataSetIdString);
                version = tmp.getVersion();
            } else {
                // no database ID, so ensure UUID is present
                if (StringUtils.isNotBlank(this.dataSetUuidString)) {

                    if (StringUtils.isNotBlank(this.dataSetVersionString)) {
                        try {
                            version = DataSetVersion.parse(this.dataSetVersionString);
                        } catch (FormatException e) {
                            this.addI18NFacesMessage("facesMsg.illegalVersion", FacesMessage.SEVERITY_ERROR);
                        }
                    }

                    // no source node => local
                    if (StringUtils.isBlank(this.sourceNodeIdString)) {
                        if (version != null) {
                            tmp = this.daoInstance.getByUuidAndVersion(this.dataSetUuidString, version);
                        } else {
                            tmp = this.daoInstance.getByUuid(this.dataSetUuidString);
                            version = tmp.getVersion();
                        }
                    }

                    // remote loading
                    else {
                        if (version != null) {
                            // TODO: include version
                            this.dataSet = this.daoInstance.getForeignDataSet(this.sourceNodeIdString, this.dataSetUuidString, this.registryId);
                        } else {
                            this.dataSet = this.daoInstance.getForeignDataSet(this.sourceNodeIdString, this.dataSetUuidString, this.registryId);
                        }
                        // xml unmarshalling and loading of other versions is nonsense
                        // (and would trigger exception because tmp is still null ;-)
                        return;
                    }
                }
            }

            if (tmp == null) {
                throw new InvalidDatasetException(this.getI18n().getString("facesMsg.noValidIdentifier"));
            } else {
                // only check assignment to current data stock if a stock parameter was explicitly provided
                if (!this.stockSelection.isAutoDefault()) {
                    // check if the selected data stock is valid for the current data set
                    if (!StringUtils.isBlank(this.dataSetIdString)) {
                        if (!this.daoInstance.isInDatastockById(this.dataSetIdString, this.stockSelection.getCurrentStock())) {
                            throw new DatasetNotInSelectedStockException(this.getI18n().getString("facesMsg.dataSetNotInSelectedStock"));
                        }
                    } else {
                        if (!this.daoInstance.isInDatastockByUuid(this.dataSetUuidString, version, this.stockSelection.getCurrentStock())) {
//							throw new DatasetNotInSelectedStockException( this.getI18n().getString( "facesMsg.dataSetNotInSelectedStock" ) );
                            this.addI18NFacesMessage("facesMsg.stock.rerouted", FacesMessage.SEVERITY_INFO, tmp.getRootDataStock().getName());
                        }
                    }
                }

                // check read privileges (will throw exception if not present)
                try {
                    SecurityUtil.assertCanRead(tmp);

                } catch (Exception notAuthorizedException) {
                    // If we're logged in - fine let's throw the notAuthorizedException.
                    // Otherwise redirect to login page.
                    try {
                        SecurityUtil.assertIsNotLoggedIn();

                        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();

                        // get the current url
                        String currentUrl = null;
                        Object requestObject = extContext.getRequest();
                        if (requestObject instanceof ServletRequest) {
                            HttpServletRequest request = (HttpServletRequest) requestObject;
                            String servletPath = request.getServletPath();
                            String queryString = request.getQueryString();
                            currentUrl = servletPath + "?" + queryString;
                        }

                        // set loginHandler source - to be send back here after login.
                        loginHandler.setSource(currentUrl);

                        try { // redirect to login page (and display a message asking to log in)
                            extContext.redirect(conf.getContextPath() + "/login.xhtml?" + LoginHandler.LOGIN_REQUIRED_PARAM_KEY + "=" + LoginHandler.LOGIN_REQUIRED_RESOURCE);
                        } catch (Exception ioe) {
                            throw new AbortProcessingException(ioe);
                        }

                    } catch (IllegalArgumentException iae) {
                        // User was logged in but is not authorized.
                        throw notAuthorizedException;
                    }
                }

                // assign data set
                this.dataSet = (I) tmp;
                // load xml data via JAXB if required
                if (unmarshallXml) {
                    this.xmlDataset = (A) new UnmarshalHelper().unmarshal(tmp);
                }

                // load other versions (currently only for local data sets)
                List<T> allOtherVersions = this.daoInstance.getOtherVersions(tmp);
                this.otherVersions = new ArrayList<T>();

                for (T d : allOtherVersions) {
                    try {
                        SecurityUtil.assertCanRead(d);
                        this.otherVersions.add(d);
                    } catch (AuthorizationException e) {
                    }
                }

                // let the subclasses know
                this.datasetLoaded(tmp);
            }
        }
    }

    /**
     * Try to fix given www address (checks only for prefix http://), then calls UrlValidator.isValid()
     *
     * @param www address to check
     * @return either fixed www adress or blank String otherwise
     */
    public String fixWwwAddress(String www) {
        if (StringUtils.isNotBlank(www)) {
            if (!StringUtils.startsWithIgnoreCase(www, "http://") && !StringUtils.startsWithIgnoreCase(www, "https://")) {
                www = ConfigurationService.INSTANCE.getHttpPrefix() + www;
            }
            UrlValidator urlValidator = new UrlValidator();
            if (urlValidator.isValid(www)) {
                return www;
            } else {
                return "";
            }
        }
        return www;
    }

    /**
     * Helper method to return the last element of given list
     *
     * @param <E>     element type
     * @param anyList the list
     * @return the last element of given list
     */
    public <E> E getLastElementOfList(List<E> anyList) {
        if (CollectionUtils.isNotEmpty(anyList)) {
            return anyList.get(anyList.size() - 1);
        }
        return null;
    }

    /**
     * Helper method to return a given Collection as List
     *
     * @param <E>           element type
     * @param anyCollection the collection
     * @return the Collection as List
     */
    public <E> List<E> getAsList(Collection<E> anyCollection) {
        if (CollectionUtils.isNotEmpty(anyCollection)) {
            return new ArrayList<E>(anyCollection);
        }
        return null;
    }

    /**
     * Determine if
     *
     * @return <code>true</code> if ..., else <code>false</code>
     */
    public boolean isUrlWithStock() {
        return false;
    }

    /**
     * Get value for provided language with fall back to default for XML API types
     *
     * @param lang       desired language
     * @param multilangs list of {@link IMultiLang}
     * @return value for provided language (or fall back)
     */
    public IMultiLang getMultilangWithFallback(String lang, List<? extends IMultiLang> multilangs) {
        return SodaUtil.getMultilangWithFallback(lang, multilangs);
    }

    /**
     * Get reference URL for arbitrary data set global reference
     *
     * @param <E>       type of data set
     * @param globalRef global reference to link to
     * @param generator URL generator
     * @return URL that was determined (or {@link IGlobalReference#getUri() URI} if not in database
     */
    public <E extends DataSet> String getReferenceUrl(IGlobalReference globalRef, AbstractDataSetURLGenerator<E> generator) {
        if (globalRef == null) {
            return null;
        }
        return this.getReferenceUrl(globalRef.getRefObjectId(), globalRef.getUri(), globalRef.getVersionAsString(), generator);
    }

    /**
     * Get reference URL for arbitrary data set global reference (XML)
     * Note: Different name because EL doesn't support overloaded methods
     *
     * @param <E>       type of data set
     * @param globalRef global reference to link to
     * @param generator URL generator
     * @return URL that was determined (or {@link GlobalReferenceType#getUri() URI} if not in database
     */
    public <E extends DataSet> String getReferenceUrlXml(GlobalReferenceType globalRef, AbstractDataSetURLGenerator<E> generator) {
        if (globalRef == null) {
            return null;
        }
        return this.getReferenceUrl(globalRef.getRefObjectId(), globalRef.getUri(), globalRef.getVersion(), generator);
    }

    /**
     * Get reference URL for arbitrary data set global reference
     *
     * @param <E>           type of data set
     * @param uuidStr       UUID as string (ref object id)
     * @param uri           URI of global reference
     * @param versionString version as string
     * @param generator     URL generator
     * @return URL that was determined (or {@link IGlobalReference#getUri() URI} if not in database
     */
    private <E extends DataSet> String getReferenceUrl(String uuidStr, String uri, String versionString, AbstractDataSetURLGenerator<E> generator) {
        DataSetDao<E, ?, ?> dao = generator.createDaoInstance();

        /*
         * better use uri value first??
         */

        if (StringUtils.isBlank(uuidStr)) {
            GlobalRefUriAnalyzer uriAnalyzer = new GlobalRefUriAnalyzer(uri);
            uuidStr = uriAnalyzer.getUuidAsString();
        }

        if (StringUtils.isNotBlank(uuidStr)) {
            DataSetVersion version = null;
            if (StringUtils.isNotBlank(versionString)) {
                try {
                    version = DataSetVersion.parse(versionString);
                } catch (Exception e) {
                    // don't care
                }
            }

            E dataset = null;

            if (DataSetVersion.isNotBlank(version)) {
                dataset = dao.getByUuidAndVersion(uuidStr, version);
            } else {
                dataset = dao.getByUuid(uuidStr);
            }

            if (dataset != null) {
                return this.conf.getContextPath() + generator.getResourceDetailHtml(dataset);
            }
        }

        if (StringUtils.isNotBlank(uri)) {
            if (!StringUtils.startsWithIgnoreCase(uri, "http://") && !StringUtils.startsWithIgnoreCase(uri, "https://")) {
                uri = this.conf.getContextPath() + "/" + uri.replace("..", "resource");
            }
        }
        return uri;
    }

    /**
     * Get digital file URL for arbitrary data set global reference
     *
     * @param globalRef global reference to link to
     * @param generator Source URL generator
     * @return URL that was determined (or {@link IGlobalReference#getUri() URI} if not in database
     */
    public String getDigitalFileUrl(IGlobalReference globalRef, SourceURLGenerator generator) {
        if (globalRef == null) {
            return null;
        }
        return this.getDigitalFileUrl(globalRef.getRefObjectId(), globalRef.getUri(), generator);
    }

    /**
     * Get digital file URL for arbitrary data set global reference (XML)
     * Note: Different name because EL doesn't support overloaded methods
     *
     * @param globalRef global reference to link to
     * @param generator Source URL generator
     * @return URL that was determined (or {@link GlobalReferenceType#getUri() URI} if not in database
     */
    public String getDigitalFileUrlXml(GlobalReferenceType globalRef, SourceURLGenerator generator) {
        if (globalRef == null) {
            return null;
        }
        return this.getDigitalFileUrl(globalRef.getRefObjectId(), globalRef.getUri(), generator);
    }

    /**
     * Get resource URL for digital file of a source data set for arbitrary data set global reference
     *
     * @param uuidStr   UUID as string (ref object id)
     * @param uri       URI of global reference
     * @param generator Source URL generator
     * @return URL that was determined (or {@link IGlobalReference#getUri() URI} if not in database
     */
    private String getDigitalFileUrl(String uuidStr, String uri, SourceURLGenerator generator) {
        DataSetDao<Source, ?, ?> dao = generator.createDaoInstance();

        if (StringUtils.isBlank(uuidStr)) {
            GlobalRefUriAnalyzer uriAnalyzer = new GlobalRefUriAnalyzer(uri);
            uuidStr = uriAnalyzer.getUuidAsString();
        }

        if (StringUtils.isNotBlank(uuidStr)) {
            Source source = null;
            source = dao.getByUuid(uuidStr);

            if (source != null) {
                return generator.getResourceDigitalFile(source);
            }
        }
        return uri;
    }

    /**
     * Get the unmarshalled XML data set
     *
     * @return unmarshalled XML data set
     */
    public A getXmlDataset() {
        return this.xmlDataset;
    }

    /**
     * Determine if the data set is a foreign data set
     *
     * @return <code>true</code> if data set is a foreign data set, else <code>false</code>
     * @deprecated will be removed once data stocks are fully implemented due to lack of foreign data sets
     */
    @Deprecated
    public boolean isForeignDataSet() {
        return !StringUtils.isBlank(this.sourceNodeIdString);
    }

    /**
     * Get the foreign resource URL for XML of data set
     *
     * @return foreign resource URL for XML of data set or <code>null</code> if this is not
     * @deprecated will be removed once data stocks are fully implemented due to lack of foreign data sets
     */
    @Deprecated
    public String getForeignXMLResourceURL() {
        return this.getForeignResourceUrl("xml");
    }

    /**
     * Get the foreign resource URL for HTML view of data set
     *
     * @return foreign resource URL for HTML view of data set
     * @deprecated will be removed once data stocks are fully implemented due to lack of foreign data sets
     */
    @Deprecated
    public String getForeignHTMLResourceURL() {
        return this.getForeignResourceUrl("html");
    }

    /**
     * Get foreign resource URL
     *
     * @param format format (xml or html)
     * @return created resource URL
     * @deprecated will be removed once data stocks are fully implemented due to lack of foreign data sets
     */
    @Deprecated
    private String getForeignResourceUrl(String format) {
        return this.getResourceUrl(this.dataSet.getHref(), format);
    }

    /**
     * Get resource URL
     *
     * @param pathPrefix prefix (context path, foreign URL,...) before <code>resource/...</code> of the resource URL
     * @param format     format (xml or html)
     * @return created resource URL
     */
    private String getResourceUrl(String pathPrefix, String format) {
        return pathPrefix + "resource/" + this.resourceDir + "/" + this.dataSet.getUuidAsString() + "?format=" + format
                + (StringUtils.isBlank(this.dataSetVersionString) ? "" : "&amp;version=" + this.dataSet.getDataSetVersion());
    }

    /**
     * Setter for managed property for configuration
     *
     * @param conf configuration bean to set
     */
    public void setConf(ConfigurationBean conf) {
        this.conf = conf;
    }

    /**
     * Get the data set
     *
     * @return data set
     */
    public I getDataSet() {
        return this.dataSet;
    }

    /**
     * Set the data set
     *
     * @param dataSet data set to set
     */
    public void setDataSet(I dataSet) {
        this.dataSet = dataSet;
    }

    /**
     * Get the data set id as string
     *
     * @return data set id as string
     */
    public String getDataSetIdString() {
        return this.dataSetIdString;
    }

    /**
     * Set the data set id as string
     *
     * @param dataSetIdString id to set
     */
    public void setDataSetIdString(String dataSetIdString) {
        this.dataSetIdString = dataSetIdString;
    }

    /**
     * Determine if at least one other version is present
     *
     * @return <code>true</code> if at least one other version is present, else <code>false</code>
     */
    public boolean isOtherVersionPresent() {
        return this.otherVersions != null && !this.otherVersions.isEmpty();
    }

    /**
     * Get the data set uuid as string
     *
     * @return data set uuid as string
     */
    public String getDataSetUuidString() {
        return this.dataSetUuidString;
    }

    /**
     * Set the data set uuid as string
     *
     * @param dataSetUuidString uuid to set
     */
    public void setDataSetUuidString(String dataSetUuidString) {
        this.dataSetUuidString = dataSetUuidString;
    }

    /**
     * Get the data set version as string
     *
     * @return data set version as string
     */
    public String getDataSetVersionString() {
        return this.dataSetVersionString;
    }

    /**
     * Set the data set version as string
     *
     * @param dataSetVersionString version to set
     */
    public void setDataSetVersionString(String dataSetVersionString) {
        this.dataSetVersionString = dataSetVersionString;
    }

    /**
     * Get the source node id as string
     *
     * @return source node id as string
     */
    public String getSourceNodeIdString() {
        return this.sourceNodeIdString;
    }

    /**
     * Set the source node id as string
     *
     * @param sourceNodeIdString id to set
     */
    public void setSourceNodeIdString(String sourceNodeIdString) {
        this.sourceNodeIdString = sourceNodeIdString;
    }

    /**
     * Get the stock selection managed bean
     *
     * @return stock selection managed bean
     */
    public StockSelectionHandler getStockSelection() {
        return this.stockSelection;
    }

    /**
     * Set the stock selection managed bean
     *
     * @param stockSelection stock selection managed bean to set
     */
    public void setStockSelection(StockSelectionHandler stockSelection) {
        this.stockSelection = stockSelection;
    }

    /**
     * Get the other versions for this data set
     *
     * @return other versions for this data set
     */
    public List<T> getOtherVersions() {
        return this.otherVersions;
    }

    /**
     * Get the DAO instance of this handler
     *
     * @return DAO instance of this handler
     */
    protected final D getDaoInstance() {
        return this.daoInstance;
    }

    /**
     * Get the registry ID
     *
     * @return registry ID
     */
    public Long getRegistryId() {
        return this.registryId;
    }

    /**
     * Set the registry ID
     *
     * @param registryId registry ID to set
     */
    public void setRegistryId(Long registryId) {
        this.registryId = registryId;
    }

    /**
     * Get the registry UUID
     *
     * @return registry UUID
     */
    public String getRegistryUUID() {
        return this.registryUUID;
    }

    /**
     * Set the registry UUID
     *
     * @param registryUUID registry UUID to set
     */
    public void setRegistryUUID(String registryUUID) {
        this.registryUUID = registryUUID;
    }

    /**
     * @see SodaUtil#getValueFromMatPropKey(String)
     */
    public String getValueFromMatPropKey(String matPropDefName) {
        return SodaUtil.getValueFromMatPropKey(matPropDefName);
    }

    public LoginHandler getLoginHandler() {
        return loginHandler;
    }

    public void setLoginHandler(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
    }
}
