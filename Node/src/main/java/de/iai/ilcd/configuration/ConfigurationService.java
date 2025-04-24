package de.iai.ilcd.configuration;

import com.okworx.ilcd.validation.exception.InvalidProfileException;
import com.okworx.ilcd.validation.profile.Profile;
import com.okworx.ilcd.validation.profile.ProfileManager;
import de.fzk.iai.ilcd.service.client.impl.vo.nodeinfo.NodeInfo;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

/**
 * @author clemens.duepmeier
 */
public enum ConfigurationService {

    INSTANCE;

    /**
     * Legacy (Velocity templates) engine used for rendering HTML output (dataset detail)
     */
    public final static String RENDER_LEGACY = "legacy";
    /**
     * JSF (Facelets) engine used for rendering HTML output (dataset detail), used as default
     */
    public final static String RENDER_JSF = "JSF";
    public static final String AUGMENTATION_MODE_OVERRIDE = "override";
    public static final String AUGMENTATION_MODE_NO_OVERRIDE = "no override";
    private final Logger logger = LogManager.getLogger(ConfigurationService.class);
    // initialize basePath while loading class
    private final String basePath;
    private final NodeInfo nodeInfo = new NodeInfo();
    private final String defaultPropertiesFile = System.getProperty("catalina.base") + File.separator + "conf" + File.separator + "soda4LCA.properties";
    private Map<String, String[]> augmentRegistrationAuthorityMap = new HashMap<>();
    Configuration fileConfig;
    long searchDistTimeout = 0;
    private URI baseURI = null;
    private String landingPageURL = null;
    private String contextPath = null;
    private String versionTag = null;
    private String schemaVersion = null;
    private Configuration appConfig;
    private DisplayConfig displayConfig;
    private String featureNetworking;
    private boolean featureGlad;
    private String featureGladURL;
    private String featureGladAPIKey;
    private boolean tokenOnly;
    private Long tokenTTL;
    private String defaultLanguage;
    private boolean tls;
    private String propertiesFilePath = null;
    private List<String> preferredLanguages = null;
    private boolean translateClassification = false;
    private List<String> translateClassificationCSVlanguages = null;
    private Map<String, String> translateClassifications = new HashMap<String, String>();
    private boolean acceptPrivacyPolicy;
    private boolean qqaEnabled;
    private Integer qqaThreshold;
    private Integer qqaWarnThreshold;
    private List<Profile> registeredProfiles = new ArrayList<Profile>();
    /**
     * Engine used for rendering HTML output (dataset detail)
     */
    private String htmlRenderEngine = null;
    /**
     * Default classification system
     */
    private String defaultClassificationSystem;
    private boolean classificationEncodingFix = false;
    private boolean enableCSVExport;
    private boolean adminOnlyExport;
    private List<String> csvProfiles;
    private boolean dependenciesIgnoreProcesses;
    private boolean enableDatasetdetailsView;
    private boolean honorPermissionsForSupersedingDatasets;
    private String cleanCacheCron;
    /**
     * The time in milliseconds that a cache entry will be valid from the time
     * it is stored
     */
    private long searchDistCacheTTL;

    private File dirProfiles;

    private boolean proxyMode;

    private String convertXLSXAPI;

    private byte[] APISigningKey;

    /**
     * Comma seperated list of datstocks UUIDs.
     * Every token user will have access to.
     * Doesn't affect any datastocks the user already has access to.
     */
    private List<String> APIPublicDataStocks;

    private String oidcBaseURI;
    private String oidcRealm;
    private String oidcDiscoveryURI;
    private String oidcClientID;
    private String oidcSecret;
    private String oidcClientAuthenticationMethod;
    private String oidcPreferredJwsAlgorithm;
    private String oidcScope; // plus separated (ex: openid+profile+email)
    private String oidcCallbackUrl;
    private String oidcCallbackFilterDefaultUrl;
    private String oidcExternalRolesUrl;
    // env var friendly
    // oidc.roleMappings = CM_XL, 1, READ, EXPORT; CM_M, 23, READ, EXPORT;...
    private String oidcRoleMappings;

    /**
     * Flag to decide if <code>flyway.setValidateOnMigrate(boolean)</code>.
     */
    private boolean flywayValidate = true;

    private boolean developerMode = false;

    private boolean showReferenceProducts = false;

    /**
     * These tags will be set for each newly imported data set.
     */
    private List<String> importTagNames;

    /**
     * If this flag is set true, all data sets referenced by an import request will be tagged by the
     * configured {@link #importTagNames} (Including the data sets that are not imported, because they already
     * exist in the database).
     */
    private boolean tagExistingOnImport;

    ConfigurationService() {
        try {
            this.appConfig = new PropertiesConfiguration("app.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("FATAL ERROR: application properties could not be initialized", e);
        }

        this.displayConfig = new DisplayConfig();

        // log application version message
        this.versionTag = this.appConfig.getString("version.tag");
        this.logger.info(this.versionTag);

        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource("log4j2.properties");
        Path decodedPath = null;
        // now extract path and decode it
        try {
            // note, that URLs getPath() method does not work, because it don't
            // decode encoded Urls, but URI's does this
            decodedPath = Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException ex) {
            this.logger.error("Cannot extract base path from resource files", ex);
        }

        // base path it relative to web application root directory
        // this.basePath = decodedPath.replace( "/WEB-INF/classes/log4j2.properties", "" );
        this.basePath = decodedPath.getParent().getParent().getParent().toString();
        this.logger.info("base path of web application: {}", this.basePath);

        // Obtain our environment naming context
        Context initCtx;
        Context envCtx;

        try {
            initCtx = new InitialContext();
            envCtx = (Context) initCtx.lookup("java:comp/env");
            propertiesFilePath = (String) envCtx.lookup("soda4LCAProperties");
        } catch (NamingException e1) {
            this.logger.error(e1.getMessage());
        }

        if (propertiesFilePath == null) {
            this.logger.info("using default application properties at {}", this.defaultPropertiesFile);
            propertiesFilePath = this.defaultPropertiesFile;
        } else {
            this.logger.info("reading application configuration properties from {}", propertiesFilePath);
        }

        initSodaProperties();

        // validate/migrate database schema
        // - has to happen after initializing this.flywayValidate flag which is
        // initialized from soda4lca.properties
        this.migrateDatabaseSchema();

        this.configureProfiles();
    }

    private void initSodaProperties() {
        try {
            this.fileConfig = new PropertiesConfiguration(propertiesFilePath);

            this.featureNetworking = this.fileConfig.getString("feature.networking");
            this.featureGlad = this.fileConfig.getBoolean("feature.glad", false);
            if (this.featureGlad) {
                this.featureGladURL = this.fileConfig.getString("feature.glad.url");
                this.featureGladAPIKey = this.fileConfig.getString("feature.glad.apikey");
            }

            this.acceptPrivacyPolicy = this.fileConfig.getBoolean("user.registration.privacypolicy.accept", false);
            this.tokenOnly = this.fileConfig.getBoolean("feature.api.auth.token.only", false);
            this.tokenTTL = this.fileConfig.getLong("feature.api.auth.token.ttl", 7884000L); // 3 months in seconds
            this.setAPISigningKey(DatatypeConverter.parseBase64Binary(fileConfig.getString("feature.api.auth.token.APISigningKey", "secret"))); // TODO: rm old key config
            this.setAPIPublicDataStocks(this.fileConfig.getList("feature.api.public.datastocks", List.of()));

            this.enableCSVExport = this.fileConfig.getBoolean("feature.export.csv", false);
            this.adminOnlyExport = this.fileConfig.getBoolean("feature.export.csv.adminonly", false);
            this.csvProfiles = this.fileConfig.getList("feature.export.csv.profiles");
            this.honorPermissionsForSupersedingDatasets = this.fileConfig.getBoolean("display.superseding.honorstockpermissions", true);
            this.dependenciesIgnoreProcesses = this.fileConfig
                    .getBoolean("feature.export.dependencies.ignoreincludedprocesses", true);
            this.setEnableDatasetdetailsView(this.fileConfig.getBoolean("feature.view.datasetdetails", false));
            this.proxyMode = this.fileConfig.getBoolean("feature.networking.proxy", false);
            this.configureLanguages();
            this.htmlRenderEngine = this.fileConfig.getString("htmlRenderEngine", RENDER_JSF);

            this.initDisplayConfig();

            this.qqaEnabled = this.fileConfig.getBoolean("feature.qqa", false);
            this.qqaThreshold = this.fileConfig.getInteger("feature.qqa.threshold", 100);
            this.qqaWarnThreshold = this.fileConfig.getInteger("feature.qqa.warnThreshold", 150);
            this.classificationEncodingFix = this.fileConfig.getBoolean("classification.encodingfix", false);
            this.searchDistCacheTTL = this.fileConfig.getLong("search.dist.cache.ttl", 3600000); // 60 minutes

            // spring > 5.1, use '-' as a cron disable pattern
            this.cleanCacheCron = this.fileConfig.getString("files.cacheClean.schedule.cronjob", "-");

            this.setConvertXLSXAPI(this.fileConfig.getString("feature.convertXLSXAPI.URL", "")); // "http://localhost/api"));
            this.nodeInfo.setBaseURL(this.fileConfig.getString("service.node.baseURL", ""));

            if (!this.fileConfig.subset("feature.registrationAuthority.augment").isEmpty()) {
                this.configureRegistrationAuthorityAugmentation();
            }

            this.nodeInfo.setBaseURL(this.fileConfig.getString("service.node.baseURL", ""));

            this.setDefaultClassificationSystem(this.fileConfig.getString("defaultClassificationSystem", "ILCD"));

            this.searchDistTimeout = fileConfig.getLong("search.dist.timeout", 5000);

            this.flywayValidate = fileConfig.getBoolean("flyway.validate", true);

            this.developerMode = fileConfig.getBoolean("feature.developerMode", false);

            this.showReferenceProducts = fileConfig.getBoolean("feature.api.processes.referenceproducts", false);

            //oidc
            this.oidcBaseURI = fileConfig.getString("feature.oidc.baseURI", "");
            this.oidcRealm = fileConfig.getString("feature.oidc.realm", "");
            this.oidcDiscoveryURI = fileConfig.getString("feature.oidc.discoveryURI", this.oidcBaseURI + "/realms/" + this.oidcRealm + "/.well-known/openid-configuration"); // optional
            this.oidcClientID = fileConfig.getString("feature.oidc.clientID", "");
            this.oidcSecret = fileConfig.getString("feature.oidc.secret", "");
            this.oidcClientAuthenticationMethod = fileConfig.getString("feature.oidc.clientAuthenticationMethod", "client_secret_basic"); // optional
            this.oidcPreferredJwsAlgorithm = fileConfig.getString("feature.oidc.preferredJwsAlgorithm", "RS256"); // optional
            this.oidcScope = fileConfig.getString("feature.oidc.scope", "openid"); // plus separated (ex: openid+profile+email)
            this.oidcCallbackUrl = fileConfig.getString("feature.oidc.callbackUrl", this.getNodeInfo().getBaseURLnoResource().toString()); // optional
            this.oidcCallbackFilterDefaultUrl = fileConfig.getString("feature.oidc.callbackFilterDefaultUrl", this.getNodeInfo().getBaseURLnoResource() + "postauth"); // optional
            this.oidcExternalRolesUrl = fileConfig.getString("feature.oidc.externalRolesUrl", null); // optional
            // env var friendly
            // oidc.roleMappings = CM_XL, 1, READ, EXPORT; CM_M, 23, READ, EXPORT;...
            var arr = fileConfig.getStringArray("feature.oidc.roleMappings");
            if (arr != null && arr.length != 0)
                this.oidcRoleMappings = String.join(",", arr);

            this.importTagNames = fileConfig.getList("feature.tags.setonimport", new ArrayList());
            this.tagExistingOnImport = fileConfig.getBoolean("feature.tags.setonimport.tagexisting", false);

        } catch (ConfigurationException ex) {
            this.logger.error(
                    "Cannot find application configuration properties file under {}, either put it there or set soda4LCAProperties environment entry via JNDI.",
                    propertiesFilePath, ex);
            throw new RuntimeException("application configuration properties not found", ex);
        }
    }

    public Configuration readSodaProperties() throws ConfigurationException {
        this.initSodaProperties();
        return this.fileConfig;
    }

    private void initDisplayConfig() {
        this.displayConfig.configure(this);
        this.displayConfig.configureColumns(this);
    }


    private void configureProfiles() {

        File directory = new File(this.getProfileDirectory());

        File[] fList = directory.listFiles((dir1, name) -> name.endsWith(".jar"));

        // init ProfileManager
        new ProfileManager.ProfileManagerBuilder().registerDefaultProfiles(true, false).build();

        if (fList != null) {
            for (File file : fList) {
                if (file.isFile()) {

                    try {
                        Profile profile = ProfileManager.getInstance().registerProfile(file.toURI().toURL());
                        this.logger.info("Profile " + profile.getName() + " registered");
                    } catch (MalformedURLException e1) {
                        this.logger.error("invalid URL when trying to register profile " + file.getAbsolutePath());
                    } catch (InvalidProfileException e1) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid profile.", null));
                    } catch (Exception e1) {
                        this.logger.error("Error registering profile " + file.getAbsolutePath(), e1);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void configureLanguages() {
        this.preferredLanguages = this.fileConfig.getList("preferredlanguages");
        if (this.preferredLanguages == null || this.preferredLanguages.isEmpty()) {
            this.preferredLanguages = new ArrayList<String>();
            this.preferredLanguages.add("en");
            this.preferredLanguages.add("de");
            this.preferredLanguages.add("es");
        }
        this.defaultLanguage = this.preferredLanguages.get(0);

        this.translateClassification = this.fileConfig.getBoolean("classification.translate", false);

        this.translateClassificationCSVlanguages = this.fileConfig.getList("classification.translate.csv");
        if (this.translateClassificationCSVlanguages == null)
            this.translateClassificationCSVlanguages = new ArrayList<String>(); // No translation then

        if (this.translateClassification) {
            for (Iterator<String> iter = this.fileConfig.getKeys("classification.translate.system"); iter.hasNext(); ) {
                String key = iter.next();
                String catSystem = StringUtils.substringAfter(key, "classification.translate.system.");
                String path = this.fileConfig.getString(key);
                if (logger.isTraceEnabled())
                    logger.trace(" registering classification i18n for " + catSystem + " : " + path);
                this.translateClassifications.put(catSystem.toUpperCase(), path);
            }
        }
    }

    private void configureRegistrationAuthorityAugmentation() {
        if (this.fileConfig.getBoolean("feature.registrationAuthority.augment", true)) { // Deprecated flag. It can still be used to turn the feature off (preferred: commenting out the config)
            this.augmentRegistrationAuthorityMap = new HashMap<>();
            final var isDefaultModeOverride = this.fileConfig.getBoolean("feature.registrationAuthority.augment.override", false);
            this.configureDefaultRAA(isDefaultModeOverride);
            this.configureStockSpecificRAA(isDefaultModeOverride);
        }
    }

    private void configureDefaultRAA(boolean isDefaultModeOverride) {
        if (this.fileConfig.getBoolean("feature.registrationAuthority.augment.useGlobalAsDefault", true)) { // Deprecated flag. It can still be used to turn off global augmentation (preferred: commenting out the config)
            final var contactData = this.extractContactAugmentationData(this.fileConfig.getList("feature.registrationAuthority.augment.global"), isDefaultModeOverride);
            if (contactData != null)
                this.augmentRegistrationAuthorityMap.put(null, contactData);
        }
    }

    private void configureStockSpecificRAA(boolean isDefaultModeOverride) {
        final var datastockSpecificAugmentations = this.fileConfig.subset("feature.registrationAuthority.augment.datastock");
        for (Iterator<?> it = datastockSpecificAugmentations.getKeys(); it.hasNext(); ) {
            final var dataStockUuid = (String) it.next();
            if (dataStockUuid != null) {
                String[] contactData = this.extractContactAugmentationData(datastockSpecificAugmentations.getList(dataStockUuid), isDefaultModeOverride);
                if (contactData != null)
                    this.augmentRegistrationAuthorityMap.put(dataStockUuid, contactData);
            }
        }
    }

    /**
     * Converts the provided parameter list into a contact tuple, while
     * applying basic checks on the parameters.
     *
     * @param contactParams List having uuid as first entry and name as second.
     * @return contact tuple (or null). Note: tuple[0] holds an uuid, tuple[1] a name
     */
    private String[] extractContactAugmentationData(List<?> contactParams, boolean isDefaultModeOverride) {
        if (contactParams == null || contactParams.isEmpty()) {
            return null;
        }
        if (contactParams.size() < 2) {
            logger.warn("Invalid augmentation config (params: {})", contactParams);
            return null;
        }

        final var tuple = new String[3];
        final var uuidParameter = contactParams.get(0);
        final var uuidString = uuidParameter != null ? String.valueOf(uuidParameter).trim() : (String) null;
        // Let's check the integrity of the uuid
        if (!isUuidString(uuidString))
            return null;
        else
            tuple[0] = uuidString;

        final var nameParameter = contactParams.get(1);
        final var nameString = nameParameter != null ? String.valueOf(nameParameter).trim() : "";
        tuple[1] = nameString;

        final var modeParam = contactParams.size() >= 3 ? contactParams.get(2) : null;
        final var declaredMode = modeParam != null ? String.valueOf(modeParam).trim() : (String) null;
        if ("override".equalsIgnoreCase(declaredMode) || "overwrite".equalsIgnoreCase(declaredMode)) {
            tuple[2] = AUGMENTATION_MODE_OVERRIDE;
        } else {
            tuple[2] = isDefaultModeOverride ? AUGMENTATION_MODE_OVERRIDE : AUGMENTATION_MODE_NO_OVERRIDE;
            if (declaredMode != null && !declaredMode.isBlank()) {
                logger.warn("Unknown augmentation mode in augmentation tuple {}. Using {} instead", contactParams, tuple);
            }
        }

        return tuple;
    }

    private boolean isUuidString(Object o) {
        if (!(o instanceof String))
            return false;

        String s = (String) o;
        try {
            return UUID.fromString(s).toString().equals(s);
        } catch (Exception e) {
            logger.warn("Expected to find uuid parameter, but found: '{}'.", s);
            return false;
        }
    }

    public void configureNodeInfo(String ctxPath) {
        if (this.contextPath == null) {
            this.contextPath = ctxPath;
            this.confNodeInfo();
        }
    }

    private void confNodeInfo() {
        String detectedHostName = null;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            detectedHostName = inetAddress.getHostName();
        } catch (UnknownHostException e) {
            this.logger.error("Could not detect hostname", e);
        }

        this.tls = this.fileConfig.getBoolean("service.url.tls", false);

        String configuredHostName = this.fileConfig.getString("service.url.hostname");
        int port = this.fileConfig.getInteger("service.url.port", 8081);

        String hostName;

        if (configuredHostName != null) {
            hostName = configuredHostName;
        } else {
            hostName = detectedHostName;
        }

        try {
            URI newUri = new URI((this.tls ? "https" : "http"), null, hostName, (port == 8081 ? -1 : port), this.contextPath, null, null);
            this.logger.info("application base URI: " + newUri.toString());
            this.baseURI = newUri;
            this.nodeInfo.setBaseURL(newUri.toString() + "/resource/");
            this.landingPageURL = this.fileConfig.getString("service.node.landingpage", newUri.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException("FATAL ERROR: could not determine base URL for service interface", e);
        }

        try {
            this.nodeInfo.setNodeID(this.fileConfig.getString("service.node.id"));
        } catch (Exception e) {
            this.logger.error("Cannot set nodeid from configuration file", e);
        }
        try {
            this.nodeInfo.setName(this.fileConfig.getString("service.node.name"));
        } catch (Exception e) {
            this.logger.error("Cannot set nodename from configuration file", e);
        }

        this.nodeInfo.setOperator(this.fileConfig.getString("service.node.operator"));
        this.nodeInfo.setDescription(this.fileConfig.getString("service.node.description"));

        // override baseURL only if it is explicitly set in the configuration file
        if (this.fileConfig.getString("service.node.baseURL") != null) {
            String url = this.fileConfig.getString("service.node.baseURL");
            if (!url.endsWith("/")) {
                url += "/";
            }
            this.nodeInfo.setBaseURL(url);
        }

        this.nodeInfo.setAdminName(this.fileConfig.getString("service.admin.name"));
        this.nodeInfo.setAdminEMail(this.fileConfig.getString("service.admin.email"));
        this.nodeInfo.setAdminPhone(this.fileConfig.getString("service.admin.phone"));
        this.nodeInfo.setAdminWWW(this.fileConfig.getString("service.admin.www"));

    }

    private void migrateDatabaseSchema() {
        DataSource dataSource = null;
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(this.appConfig.getString("persistence.dbConnection"));

            logger.info("Database connection: " + dataSource.getConnection().getMetaData().getURL());

            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("de.iai.ilcd.db.migrations", "sql/migrations")
                    .table("schema_version")
                    .outOfOrder(false)
                    // validate db status if not bypassed by property in soda4lca.properties
                    .validateOnMigrate(flywayValidate)
                    .load();
            this.logSchemaStatus(flyway);

            int migrations = flyway.migrate();

            if (migrations > 0) {
                this.logger.info("database schema: successfully migrated");
                this.logSchemaStatus(flyway);
            }

            this.schemaVersion = flyway.info().current().getVersion().getVersion();

        } catch (FlywayException e) {
            this.logger.error("error migrating database schema", e);
            throw new RuntimeException("FATAL ERROR: database schema is not properly initialized", e);
        } catch (NamingException e) {
            this.logger.error("error looking up datasource", e);
            throw new RuntimeException("FATAL ERROR: could not lookup datasource", e);
        } catch (SQLException e) {
            this.logger.error("SQL Exception occurred, data source: {}", dataSource, e);
            throw new RuntimeException("SQL Exception occurred, data source: {}", e);
        }

    }

    private void logSchemaStatus(Flyway flyway) {
        if (flyway.info().current() != null) {
            this.logger.info("database schema: current version is " + flyway.info().current().getVersion());
        } else {
            this.logger.info("database schema: no migration has been applied yet.");
        }
    }

    public String getHttpPrefix() {
        if (this.tls)
            return "https://";
        else
            return "http://";
    }

    public String getVersionTag() {
        return this.versionTag;
    }

    public String getSchemaVersion() {
        return this.schemaVersion;
    }

    public String getNodeId() {
        return this.nodeInfo.getNodeID();
    }

    public String getNodeName() {
        return this.nodeInfo.getName();
    }

    public Configuration getProperties() {
        return this.fileConfig;
    }

    public Configuration getConfigUpdate() throws ConfigurationException {
        INSTANCE.readSodaProperties();
        return INSTANCE.getProperties();
    }

    public NodeInfo getNodeInfo() {
        return this.nodeInfo;
    }

    public String getBasePath() {
        return this.basePath;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public String getZipFileDirectory() {
        String zipPath = this.fileConfig.getString("files.location.zipfiles", this.getBasePath() + "/WEB-INF/var/zips");
        File dir = new File(zipPath);
        try {
            if (!dir.exists()) {
                FileUtils.forceMkdir(dir);
            }
        } catch (IOException e) {
            this.logger.error("could not create zip files directory at ", zipPath);
        }

        return zipPath;
    }

    public String getProfileDirectory() {
        String profilePath = this.fileConfig.getString("files.location.validationprofiles", this.getBasePath() + "/WEB-INF/var/validation_profiles");
        dirProfiles = new File(profilePath);
        try {
            if (!dirProfiles.exists()) {
                FileUtils.forceMkdir(dirProfiles);
            }
        } catch (IOException e) {
            this.logger.error("could not create validation profile files directory", profilePath);
        }

        return profilePath;
    }

    public String getDigitalFileBasePath() {
        return this.fileConfig.getString("files.location.datafiles", this.getBasePath() + "/WEB-INF/var/files");
    }


    public String getUploadDirectory() {
        return this.fileConfig.getString("files.location.uploads", this.getBasePath() + "/WEB-INF/var/uploads");
    }

    public String getUniqueUploadFileName(String prefix, String extension) {
        StringBuilder buffer = new StringBuilder();
        Date date = new Date();
        buffer.append(prefix).append(date.getTime()).append(UUID.randomUUID()).append(".").append(extension);

        return this.getUploadDirectory() + "/" + buffer.toString();
    }

    public URI getBaseURI() {
        return this.baseURI;
    }

    public Configuration getAppConfig() {
        return this.appConfig;
    }

    public String getGladURL() {
        return this.featureGladURL;
    }

    public String getGladAPIKey() {
        return this.featureGladAPIKey;
    }

    public boolean isGladEnabled() {
        return this.featureGlad;
    }

    public boolean isAcceptPrivacyPolicy() {
        return this.acceptPrivacyPolicy;
    }

    public boolean isQqaEnabled() {
        return this.qqaEnabled;
    }

    public boolean isTokenOnly() {
        return this.tokenOnly;
    }

    public Integer getQqaThreshold() {
        return this.qqaThreshold;
    }

    public Integer getQqaWarnThreshold() {
        return this.qqaWarnThreshold;
    }

    public boolean isTokenTTL() {
        return (tokenTTL != null) && (tokenTTL > 0);
    }

    public Long getTokenTTL() {
        if (tokenTTL != null && tokenTTL <= 0) {
            return null;
        }
        return tokenTTL;
    }

    public boolean isRegistryBasedNetworking() {
        if (this.featureNetworking == null) {
            return true;
        } else {
            if ("nodes".equals(this.featureNetworking)) {
                return false;
            } else {
                return true;
            }
        }
    }

    public List<String> getPreferredLanguages() {
        return this.preferredLanguages;
    }

    public String getDefaultLanguage() {
        return this.defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        logger.info("changing default language to " + defaultLanguage);
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * Get the default classification system
     *
     * @return default classification system
     */
    public String getDefaultClassificationSystem() {
        return this.defaultClassificationSystem;
    }

    /**
     * Set the default classification system
     *
     * @param defaultClassificationSystem the default classification system to set
     */
    public void setDefaultClassificationSystem(String defaultClassificationSystem) {
        this.defaultClassificationSystem = defaultClassificationSystem;
    }

    /**
     * Get the engine used for rendering HTML output (dataset detail)
     *
     * @return the engine used for rendering HTML output (dataset detail)
     */
    public String getHtmlRenderEngine() {
        return this.htmlRenderEngine;
    }

    /**
     * Set the engine used for rendering HTML output (dataset detail)
     *
     * @return the engine used for rendering HTML output (dataset detail) to be set
     */
    public void setHtmlRenderEngine(String htmlRenderEngine) {
        this.htmlRenderEngine = htmlRenderEngine;
    }

    public File getDirProliles() {
        return dirProfiles;
    }

    public void setDirProliles(File dirProliles) {
        this.dirProfiles = dirProliles;
    }

    public long getSearchDistTimeout() {
        return searchDistTimeout;
    }

    public void setSearchDistTimeout(long searchDistTimeout) {
        this.searchDistTimeout = searchDistTimeout;
    }

    public DisplayConfig getDisplayConfig() {
        return displayConfig;
    }

    public void setDisplayConfig(DisplayConfig displayConfig) {
        this.displayConfig = displayConfig;
    }

    public List<Profile> getRegisteredProfiles() {
        return registeredProfiles;
    }

    public void setRegisteredProfiles(List<Profile> registeredProfiles) {
        this.registeredProfiles = registeredProfiles;
    }

    public boolean isTranslateClassification() {
        return translateClassification;
    }

    public void setTranslateClassification(boolean translateClassification) {
        this.translateClassification = translateClassification;
    }

    public Map<String, String> getTranslateClassifications() {
        return translateClassifications;
    }

    public void setTranslateClassifications(Map<String, String> translateClassifications) {
        this.translateClassifications = translateClassifications;
    }


    public boolean isEnableCSVExport() {
        return enableCSVExport;
    }

    public void setEnableCSVExport(boolean enableCSVExport) {
        this.enableCSVExport = enableCSVExport;
    }

    public boolean isDependenciesIgnoreProcesses() {
        return dependenciesIgnoreProcesses;
    }

    public void setDependenciesIgnoreProcesses(boolean dependenciesIgnoreProcesses) {
        this.dependenciesIgnoreProcesses = dependenciesIgnoreProcesses;
    }

    public boolean isAdminOnlyExport() {
        return adminOnlyExport;
    }


    public void setAdminOnlyExport(boolean adminOnlyExport) {
        this.adminOnlyExport = adminOnlyExport;
    }

    public List<String> getCsvProfiles() {
        return csvProfiles;
    }

    public void setCsvProfiles(List<String> csvProfiles) {
        this.csvProfiles = csvProfiles;
    }

    public boolean isTls() {
        return tls;
    }


    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public String getLandingPageURL() {
        return landingPageURL;
    }


    public boolean isProxyMode() {
        return proxyMode;
    }


    public boolean isEnableDatasetdetailsView() {
        return enableDatasetdetailsView;
    }


    public void setEnableDatasetdetailsView(boolean enableDatasetdetailsView) {
        this.enableDatasetdetailsView = enableDatasetdetailsView;
    }


    public boolean isClassificationEncodingFix() {
        return classificationEncodingFix;
    }


    public void setClassificationEncodingFix(boolean classificationEncodingFix) {
        this.classificationEncodingFix = classificationEncodingFix;
    }

    public String getCleanCacheCron() {
        return cleanCacheCron;
    }

    public void setCleanCacheCron(String cleanCacheCron) {
        this.cleanCacheCron = cleanCacheCron;
    }

    public long getSearchDistCacheTTL() {
        return searchDistCacheTTL;
    }


    public String getConvertXLSXAPI() {
        return convertXLSXAPI;
    }

    public void setConvertXLSXAPI(String convertXLSXAPI) {
        this.convertXLSXAPI = convertXLSXAPI;
    }

    public List<String> getTranslateClassificationCSVlanguages() {
        return translateClassificationCSVlanguages;
    }

    public boolean isHonorPermissionsForSupersedingDatasets() {
        return honorPermissionsForSupersedingDatasets;
    }

    public void setHonorPermissionsForSupersedingDatasets(boolean honorPermissionsForSupersedingDatasets) {
        this.honorPermissionsForSupersedingDatasets = honorPermissionsForSupersedingDatasets;
    }

    public boolean isAugmentRegistrationAuthority() {
        return this.augmentRegistrationAuthorityMap.size() > 0;
    }

    public Map<String, String[]> getAugmentRegistrationAuthorityMap() {
        return augmentRegistrationAuthorityMap;
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }

    public byte[] getAPISigningKey() {
        return APISigningKey;
    }

    public void setAPISigningKey(byte[] aPISigningKey) {
        APISigningKey = aPISigningKey;
    }

    public String getOidcBaseURI() {
        return oidcBaseURI;
    }

    public void setOidcBaseURI(String oidcBaseURI) {
        this.oidcBaseURI = oidcBaseURI;
    }

    public String getOidcRealm() {
        return oidcRealm;
    }

    public void setOidcRealm(String oidcRealm) {
        this.oidcRealm = oidcRealm;
    }

    public String getOidcDiscoveryURI() {
        return oidcDiscoveryURI;
    }

    public void setOidcDiscoveryURI(String oidcDiscoveryURI) {
        this.oidcDiscoveryURI = oidcDiscoveryURI;
    }

    public String getOidcClientID() {
        return oidcClientID;
    }

    public void setOidcClientID(String oidcClientID) {
        this.oidcClientID = oidcClientID;
    }

    public String getOidcSecret() {
        return oidcSecret;
    }

    public void setOidcSecret(String oidctSecret) {
        this.oidcSecret = oidctSecret;
    }

    public String getOidcClientAuthenticationMethod() {
        return oidcClientAuthenticationMethod;
    }

    public void setOidcClientAuthenticationMethod(String oidcClientAuthenticationMethod) {
        this.oidcClientAuthenticationMethod = oidcClientAuthenticationMethod;
    }

    public String getOidcPreferredJwsAlgorithm() {
        return oidcPreferredJwsAlgorithm;
    }

    public void setOidcPreferredJwsAlgorithm(String oidcPreferredJwsAlgorithm) {
        this.oidcPreferredJwsAlgorithm = oidcPreferredJwsAlgorithm;
    }

    public String getOidcScope() {
        return oidcScope;
    }

    public void setOidcScope(String oidcScope) {
        this.oidcScope = oidcScope;
    }

    public String getOidcCallbackUrl() {
        return oidcCallbackUrl;
    }

    public void setOidcCallbackUrl(String oidcCallbackUrl) {
        this.oidcCallbackUrl = oidcCallbackUrl;
    }

    public String getOidcCallbackFilterDefaultUrl() {
        return oidcCallbackFilterDefaultUrl;
    }

    public void setOidcCallbackFilterDefaultUrl(String oidcCallbackFilterDefaultUrl) {
        this.oidcCallbackFilterDefaultUrl = oidcCallbackFilterDefaultUrl;
    }

    public String getOidcExternalRolesUrl() {
        return oidcExternalRolesUrl;
    }

    public void setOidcExternalRolesUrl(String oidcExternalRolesUrl) {
        this.oidcExternalRolesUrl = oidcExternalRolesUrl;
    }

    public String getOidcRoleMappings() {
        return oidcRoleMappings;
    }

    public void setOidcRoleMappings(String oidcRoleMappings) {
        this.oidcRoleMappings = oidcRoleMappings;
    }

    public boolean isShowReferenceProducts() {
        return showReferenceProducts;
    }

    public List<String> getAPIPublicDataStocks() {
        return APIPublicDataStocks;
    }

    public void setAPIPublicDataStocks(List<String> APIPublicDataStocks) {
        this.APIPublicDataStocks = APIPublicDataStocks;
    }

    public boolean isTagExistingOnImport() {
        return tagExistingOnImport;
    }

    public TreeSet<String> getImportTagNames() {
        TreeSet<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        set.addAll(importTagNames);
        return set;
    }

    public boolean isTagOnImport() {
        return importTagNames.size() > 0;
    }
}
