package de.iai.ilcd.webgui.controller;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.configuration.DisplayConfig;
import de.iai.ilcd.model.common.ConfigurationItem;
import de.iai.ilcd.model.common.JobMetaData;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.service.util.JobState;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Bean to access configuration, is in application scope, all values loaded only once
 */
@ManagedBean(name = "conf")
@ApplicationScoped
public class ConfigurationBean extends AbstractHandler implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8874445582176430124L;
    /**
     * All available primefaces themes
     */
    private final static List<String> AVAILABLE_THEMES = Arrays.asList("afterdark", "afternoon", "afterwork", "aristo", "black-tie", "blitzer", "bluesky", "bootstrap", "casablanca", "cruze", "cupertino", "dark-hive", "delta", "dot-luv", "eggplant", "excite-bike", "flick", "glass-x", "home", "hot-sneaks", "humanity", "le-frog", "midnight", "mint-choc", "overcast", "pepper-grinder", "redmond", "rocket", "sam", "smoothness", "south-street", "start", "sunny", "swanky-purse", "trontastic", "ui-darkness", "ui-lightness", "vader");
    private static Logger logger = LogManager.getLogger(ConfigurationBean.class);
    private static Configuration conf = ConfigurationService.INSTANCE.getProperties();
    private static DisplayConfig displayConf = ConfigurationService.INSTANCE.getDisplayConfig();
    /**
     * Application version tag
     */
    private final String appVersion;
    /**
     * Path to templates
     */
    private final String templatePath;
    /**
     * Context path
     */
    private final String contextPath;
    private final String contextPath2;
    /**
     * Base URI
     */
    private final URI baseUri;
    /**
     * Flag whether to display a "Back" button in dataset detail views
     */
    private final boolean showDatasetDetailBackOption;
    /**
     * Flag whether to display a "Close" button in dataset detail views
     */
    private final boolean showDatasetDetailCloseOption;
    private String themeName;
    /**
     * Name of jQuery UI theme
     */
    private ThemeResolver themeResolver;
    /**
     * Flag if registration is activated
     */
    private boolean registrationActivated;
    /**
     * Flag if the registration settings allow the user himself to activate his new user acccount
     */
    private boolean selfActivation;
    /**
     * (absolute) URL to an HTML fragment to be included in the index page
     */
    private String customIndexPageContentURL;
    /**
     * Flag if acceptance of terms is required
     */
    private boolean requireTermsAcceptance;
    /**
     * Link to terms document/page
     */
    private String termsMessage;
    /**
     * Link to terms document/page
     */
    private String termsLink;
    /**
     * Flag if additional terms should be displayed
     */
    private boolean renderAdditionalTerms;
    /**
     * Flag if acceptance of additional terms is required
     */
    private boolean additionalTermsRequireAcceptance;
    /**
     * Title for additional terms
     */
    private String additionalTermsTitle;
    /**
     * Additional terms message
     */
    private String additionalTermsMessage;
    /**
     * Additional terms: message to be displayed if acceptance not given but required
     */
    private String additionalTermsRequiredMessage;
    /**
     * Flag if real name for users is required
     */
    private boolean requireNameForRegistration;
    /**
     * Flag if affiliation for users is required
     */
    private boolean requireAffiliationForRegistration;
    /**
     * Flag if affiliation for users is required
     */
    private boolean requireAddressForRegistration;
    /**
     * Flag if purpose is required
     */
    private boolean requirePurposeForRegistration;
    /**
     * Flag whether to show sectors
     */
    private boolean sectors;
    /**
     * Flag if captcha is to be used in the registration process
     */
    private boolean spamProtection;
    /**
     * Flag if slider is to be used in the registration process
     */
    private boolean spamProtectionSlider;
    /**
     * Application title
     */
    private String applicationTitle;
    /**
     * Application subtitle
     */
    private String applicationSubTitle;
    /**
     * Flag to indicate whether the CSV download should be enabled
     */
    private boolean enableCSVExport;

    /**
     * Flag to indicate whether the admin only can  download CSV or not
     */
    private boolean adminOnlyExport;

    /**
     * Flag to indicate whether the user can view the dataset details
     */
    private boolean enableDatasetdetailsView = false;

    /**
     * Path to logo
     */
    private String logoPath;

    /**
     * Path to logo for dataset detail
     */
    private String datasetDetailLogoPath;

    /**
     * Indicates whether high resolution logo images are available ("@2x" suffix)
     */
    private boolean logoHighRes = false;

    /**
     * Path to background for dataset detail
     */
    private String datasetDetailBackgroundPath;

    /**
     * Indicates whether push feature is available
     */
    private boolean pushShown;

    /**
     * Flag wo indicate whether developer mode is enabled
     */
    private boolean developerMode;

    /**
     * ID of the default data stock
     */
    private long defaultDataStockId;

    /**
     * Flag to indicate if default data stock is a root data stock
     */
    private boolean defaultDataStockRoot;

    /**
     * Flag to indicate whether GLAD is enabled
     */
    private boolean gladEnabled;

    private String gladUrl;

    private String privacyPolicyURL;

    private String imprintURL;

    private String landingPageURL;

    private boolean privacyPolicyURLFragment = false;

    private boolean imprintURLFragment = false;


    private boolean acceptPrivacyPolicy;

    private boolean qqaEnabled;

    private boolean oidcEnabled;

    private String convertXLSXAPI;

    private boolean dbOwner;


    /**
     * Initialize configuration bean
     */
    public ConfigurationBean() {
        this(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath());
    }

    /**
     * Use this constructor if not in the context of JSF; i.e. initialize context path by yourself
     *
     * @param request the request to get the environment from
     */
    public ConfigurationBean(HttpServletRequest request) {
        this(request.getContextPath());
    }

    /**
     * Use this constructor if not in the context of JSF; i.e. initialize context path by yourself
     *
     * @param contextPath the contextPath of application
     */
    public ConfigurationBean(String contextPath) {
        this.contextPath = contextPath;
        this.contextPath2 = StringUtils.isNotBlank(contextPath) ? contextPath.substring(1) : "ROOT";

        this.templatePath = this.buildTemplatePath();

        this.appVersion = ConfigurationService.INSTANCE.getAppConfig().getString("version.tag");
        this.showDatasetDetailBackOption = displayConf.isShowBackButton();
        this.showDatasetDetailCloseOption = displayConf.isShowCloseButton();
        this.enableCSVExport = ConfigurationService.INSTANCE.isEnableCSVExport();
        this.adminOnlyExport = ConfigurationService.INSTANCE.isAdminOnlyExport();
        this.setEnableDatasetdetailsView(ConfigurationService.INSTANCE.isEnableDatasetdetailsView());

        this.baseUri = ConfigurationService.INSTANCE.getBaseURI();
        this.landingPageURL = ConfigurationService.INSTANCE.getLandingPageURL();

        try {
            initialiseSodaProperties();
        } catch (ConfigurationException e1) {
            logger.error("error initializing soda4LCA.properties", e1);
        }

        // Config from DB for default stock:
        try {
            ConfigurationItemDao confItemDao = new ConfigurationItemDao();
            ConfigurationItem defaultStockId = confItemDao.getConfigurationItem("datastock.default");
            Long id = defaultStockId.getIntvalue();
            ConfigurationItem defaultStockRoot = confItemDao.getConfigurationItem("datastock.default_isroot");
            Long root = defaultStockRoot.getIntvalue();

            if (id != null) {
                this.defaultDataStockId = id;
            }
            if (root != null) {
                this.defaultDataStockRoot = root.equals(1L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initialiseSodaProperties() throws ConfigurationException {
        try {
            conf = ConfigurationService.INSTANCE.readSodaProperties();

            this.applicationTitle = conf.getString("title");
            this.applicationSubTitle = conf.getString("subtitle");
            this.registrationActivated = conf.getBoolean("user.registration.activated", false);
            this.selfActivation = conf.getBoolean("user.registration.selfActivation", false);
            this.spamProtection = conf.getBoolean("user.registration.spam.protection", false);
            this.spamProtectionSlider = conf.getBoolean("user.registration.spam.protection.slider", false);
            this.requireTermsAcceptance = conf.getBoolean("user.registration.acceptterms.require", false);
            this.termsLink = conf.getString("user.registration.acceptterms.link", null);
            this.termsMessage = conf.getString("user.registration.acceptterms.message", null);
            this.customIndexPageContentURL = conf.getString("content.static.index.custom.url", null);
            this.additionalTermsRequireAcceptance = conf.getBoolean("user.registration.additionalterms.1.require", false);
            this.additionalTermsRequiredMessage = conf.getString("user.registration.additionalterms.1.requiredMessage", null);
            this.additionalTermsMessage = conf.getString("user.registration.additionalterms.1.message", null);
            this.additionalTermsTitle = conf.getString("user.registration.additionalterms.1.title", null);
            this.renderAdditionalTerms = this.additionalTermsTitle != null;
            this.requireNameForRegistration = conf.getBoolean("user.registration.name.require", false);
            this.requireAffiliationForRegistration = conf.getBoolean("user.registration.affiliation.require", false);
            this.requireAddressForRegistration = conf.getBoolean("user.registration.address.require", false);
            this.requirePurposeForRegistration = conf.getBoolean("user.registration.purpose.require", false);
            this.sectors = conf.getBoolean("user.registration.sectors", false);
            this.pushShown = conf.getBoolean("feature.push", false);
            this.developerMode = conf.getBoolean("feature.developerMode", false);
            this.privacyPolicyURL = conf.getString("content.static.privacypolicy.url", null);
            this.privacyPolicyURLFragment = conf.getBoolean("content.static.privacypolicy.url.fragment", false);
            this.imprintURL = conf.getString("content.static.imprint.url", null);
            this.imprintURLFragment = conf.getBoolean("content.static.imprint.url.fragment", false);
            this.acceptPrivacyPolicy = conf.getBoolean("user.registration.privacypolicy.accept", false);
            this.qqaEnabled = conf.getBoolean("feature.qqa", false);
            this.gladEnabled = conf.getBoolean("feature.glad", false);
            if (gladEnabled) {
                this.gladUrl = conf.getString("feature.glad.url");
            }
            this.oidcEnabled = StringUtils.isNotEmpty(ConfigurationService.INSTANCE.getOidcBaseURI());
            themeInit();
            logoInit();
            displayConf = ConfigurationService.INSTANCE.getDisplayConfig();

            try {
                final var preconfiguredTags = ConfigurationService.INSTANCE.getImportTagNames();
                if (!preconfiguredTags.isEmpty()) {
                    new TagDao().persistUnknownsByNames(ConfigurationService.INSTANCE.getImportTagNames());
                }
            } catch (PersistException e) {
                logger.error("Failed to persist import tags from config file: {}", ConfigurationService.INSTANCE.getImportTagNames());
            }


            updateJobTable();

        } catch (ConfigurationException e) {
            throw new ConfigurationException("Couldn't read soda4LCA.properties.", e);
        }
    }

    private void themeInit() {
        this.themeName = conf.getString("theme");
        if (!this.getTemplatePath().endsWith("default")) {
            this.themeResolver = new ThemeResolver("cupertino");
        } else if ("random".equalsIgnoreCase(themeName)) {
            Random rand = new Random();
            this.themeResolver = new ThemeResolver(ConfigurationBean.AVAILABLE_THEMES.get(rand.nextInt(ConfigurationBean.AVAILABLE_THEMES.size())));
        } else if ("crazy".equalsIgnoreCase(themeName)) {
            this.themeResolver = new CrazyThemeResolver();
        } else {
            this.themeResolver = new ThemeResolver(themeName);
        }
    }

    private void logoInit() {

        String logoPath = conf.getString("logo");
        if (logoPath == null || logoPath.toLowerCase().trim().equals("false")) {
            logoPath = null;
        } else if (logoPath.contains("%contextPath%")) {
            logoPath = logoPath.replace("%contextPath%", this.contextPath);
        }
        this.logoPath = logoPath;

        String datasetDetailLogoPath = conf.getString("logo.datasetdetail");
        if (datasetDetailLogoPath == null || datasetDetailLogoPath.toLowerCase().trim().equals("false")) {
            datasetDetailLogoPath = null;
        } else {
            datasetDetailLogoPath = datasetDetailLogoPath.replace("%contextPath%", this.contextPath);
        }
        this.datasetDetailLogoPath = datasetDetailLogoPath;

        String datasetDetailBackgroundPath = conf.getString("logo.datasetdetail.background");
        if (datasetDetailBackgroundPath == null || datasetDetailBackgroundPath.toLowerCase().trim().equals("false")) {
            datasetDetailBackgroundPath = null;
        } else {
            datasetDetailBackgroundPath = datasetDetailBackgroundPath.replace("%contextPath", this.contextPath);
        }
        this.datasetDetailBackgroundPath = datasetDetailBackgroundPath;

        this.logoHighRes = conf.getBoolean("logo.highres", false);
    }

    public void reReadConfigs() {
        try {
            initialiseSodaProperties();
        } catch (ConfigurationException e) {
            e.printStackTrace();
            this.addI18NFacesMessage("facesMsg.error", e);
        }
    }

    /**
     * Build up the template path
     *
     * @return created template path
     */
    private String buildTemplatePath() {
        String themeName = conf.getString("template");
        return "/templates/" + themeName;
    }

    /**
     * Get the template path
     *
     * @return template path
     */
    public String getTemplatePath() {
        return this.templatePath;
    }

    /**
     * Get the jQuery UI Theme name
     *
     * @return jQuery UI Theme name
     */
    public String getThemeName() {
        return this.themeResolver.toString();
    }

    /**
     * Get the context path
     *
     * @return context path
     */
    public String getContextPath() {
        return this.contextPath;
    }

    public String getContextPath2() {
        return this.contextPath2;
    }

    /**
     * Get the base URI
     *
     * @return base URI
     */
    public URI getBaseUri() {
        return this.baseUri;
    }

    /**
     * Get the flag if registration is activated
     *
     * @return <code>true</code> if registration shall be allowed, else <code>false</code>
     */
    public boolean isRegistrationActivated() {
        return this.registrationActivated;
    }

    /**
     * Convenience method for JSF, delegates to {@link #isRegistrationActivated()}
     *
     * @return {@link #isRegistrationActivated()}
     */
    public boolean getRegistrationActivated() {
        return this.isRegistrationActivated();
    }

    /**
     * Get the flag if the user is allowed to activate himself a newly created user account
     *
     * @return <code>true</code> if self activation is allowed, else <code>false</code>
     */
    public boolean isSelfActivation() {
        return this.selfActivation;
    }

    /**
     * Convenience method for JSF, delegates to {@link #isSelfActivation()}
     *
     * @return <code>true</code> if self activation is allowed, else <code>false</code>
     */
    public boolean getSelfActivation() {
        return this.isSelfActivation();
    }

    /**
     * Determine, if logo was provided (<code>{@link #getLogoPath()} != null</code>)
     *
     * @return <code>true</code> if logo provided, else <code>false</code>
     */
    public boolean isLogoProvided() {
        return this.logoPath != null;
    }

    /**
     * Get the logo path
     *
     * @return logo path
     */
    public String getLogoPath() {
        return this.logoPath;
    }

    /**
     * Determine, if logo was provided (<code>{@link #getLogoPath()} != null</code>)
     *
     * @return <code>true</code> if logo provided, else <code>false</code>
     */
    public boolean isDatasetDetailLogoProvided() {
        return this.datasetDetailLogoPath != null;
    }

    /**
     * Get the logo path
     *
     * @return logo path
     */
    public String getDatasetDetailLogoPath() {
        return this.datasetDetailLogoPath;
    }

    /**
     * Determine, if background was provided (<code>{@link #getDatasetDetailBackgroundPath()} != null</code>)
     *
     * @return is data set detail background provided
     */
    public boolean isDatasetDetailBackgroundProvided() {
        return this.datasetDetailBackgroundPath != null;
    }

    /**
     * Get the background logo path
     *
     * @return background logo path
     */
    public String getDatasetDetailBackgroundPath() {
        return datasetDetailBackgroundPath;
    }

    /**
     * Determines whether push feature is provided.
     *
     * @return true if push feature is provided
     */
    public boolean isPushShown() {
        return pushShown;
    }

    /**
     * Determines whether developer mode is enabled.
     *
     * @return True if developer mode is enabled
     */
    public boolean isDeveloperMode() {
        return developerMode;
    }

    /**
     * Get the title of the application
     *
     * @return title of the application
     */
    public String getApplicationTitle() {
        return this.applicationTitle;
    }

    /**
     * Get the subtitle of the application
     *
     * @return subtitle of the application
     */
    public String getApplicationSubTitle() {
        return this.applicationSubTitle;
    }

    /**
     * Get ID of the default data stock
     *
     * @return ID of the default data stock
     */
    public long getDefaultDataStockId() {
        return this.defaultDataStockId;
    }

    /**
     * Set ID of the default data stock
     *
     * @param defaultDataStockId the ID of the default data stock to set
     */
    public void setDefaultDataStockId(long defaultDataStockId) {
        this.defaultDataStockId = defaultDataStockId;
    }

    /**
     * Indicates if the default data stock is a {@link RootDataStock}
     *
     * @return <code>true</code> if default data stock is a {@link RootDataStock}, else <code>false</code>
     */
    public boolean isDefaultDataStockRoot() {
        return this.defaultDataStockRoot;
    }

    /**
     * Set if the default data stock is a {@link RootDataStock}
     *
     * @param defaultDataStockRoot <code>true</code> if default data stock is a {@link RootDataStock}, else <code>false</code>
     */
    public void setDefaultDataStockRoot(boolean defaultDataStockRoot) {
        this.defaultDataStockRoot = defaultDataStockRoot;
    }

    /**
     * Get the default classification system
     *
     * @return default classification system
     */
    public String getDefaultClassificationSystem() {
        return ConfigurationService.INSTANCE.getDefaultClassificationSystem();
    }

    /**
     * Set the default classification system
     *
     * @param defaultClassificationSystem the default classification system to set
     */
    public void setDefaultClassificationSystem(String defaultClassificationSystem) {
        ConfigurationService.INSTANCE.setDefaultClassificationSystem(defaultClassificationSystem);
    }

    public String getAppVersion() {
        return this.appVersion;
    }

    public DisplayConfig getDisplayConfig() {
        return ConfigurationService.INSTANCE.getDisplayConfig();
    }

    public boolean isGladEnabled() {
        return gladEnabled;
    }

    public String getGladUrl() {
        return gladUrl;
    }

    public boolean isSpamProtection() {
        return spamProtection;
    }

    public boolean isRequireTermsAcceptance() {
        return requireTermsAcceptance;
    }

    public String getTermsMessage() {
        return termsMessage;
    }

    public String getTermsLink() {
        return termsLink;
    }

    public boolean isSpamProtectionSlider() {
        return spamProtectionSlider;
    }

    public boolean isShowDatasetDetailBackOption() {
        return showDatasetDetailBackOption;
    }

    public boolean isShowDatasetDetailCloseOption() {
        return showDatasetDetailCloseOption;
    }

    public boolean isEnableCSVExport() {
        return enableCSVExport;
    }

    public void setEnableCSVExport(boolean enableCSVExport) {
        this.enableCSVExport = enableCSVExport;
    }

    public boolean isAdminOnlyExport() {
        return adminOnlyExport;
    }

    public void setAdminOnlyExport(boolean adminOnlyExport) {
        this.adminOnlyExport = adminOnlyExport;
    }

    public String getCustomIndexPageContentURL() {
        return customIndexPageContentURL;
    }

    public boolean isCustomIndexPageContentURLMultiLang() {
        if (customIndexPageContentURL != null) {
            return customIndexPageContentURL.contains("$lang");
        }
        return false;
    }

    public boolean isLogoHighRes() {
        return logoHighRes;
    }

    public String getPrivacyPolicyURL() {
        return privacyPolicyURL;
    }

    public boolean isPrivacyPolicyURLMultiLang() {
        if (privacyPolicyURL != null) {
            return privacyPolicyURL.contains("$lang");
        }
        return false;
    }

    public boolean isAcceptPrivacyPolicy() {
        return acceptPrivacyPolicy;
    }

    public boolean isQqaEnabled() {
        return qqaEnabled;
    }

    public String getImprintURL() {
        return imprintURL;
    }

    public boolean isImprintURLMultiLang() {
        if (imprintURL != null) {
            return imprintURL.contains("$lang");
        }
        return false;
    }

    public boolean isAdditionalTermsRequireAcceptance() {
        return additionalTermsRequireAcceptance;
    }

    public String getAdditionalTermsTitle() {
        return additionalTermsTitle;
    }

    public String getAdditionalTermsMessage() {
        return additionalTermsMessage;
    }

    public boolean isRenderAdditionalTerms() {
        return renderAdditionalTerms;
    }

    public String getAdditionalTermsRequiredMessage() {
        return additionalTermsRequiredMessage;
    }

    public boolean isRequireNameForRegistration() {
        return requireNameForRegistration;
    }

    public boolean isRequireAffiliationForRegistration() {
        return requireAffiliationForRegistration;
    }

    public boolean isRequireAddressForRegistration() {
        return requireAddressForRegistration;
    }

    public boolean isRequirePurposeForRegistration() {
        return requirePurposeForRegistration;
    }

    public boolean isSectors() {
        return sectors;
    }

    public boolean isPrivacyPolicyURLFragment() {
        return privacyPolicyURLFragment;
    }

    public void setPrivacyPolicyURLFragment(boolean privacyPolicyURLFragment) {
        this.privacyPolicyURLFragment = privacyPolicyURLFragment;
    }

    public boolean isImprintURLFragment() {
        return imprintURLFragment;
    }

    public void setImprintURLFragment(boolean imprintURLFragment) {
        this.imprintURLFragment = imprintURLFragment;
    }

    public boolean isEnableDatasetdetailsView() {
        return enableDatasetdetailsView;
    }

    public void setEnableDatasetdetailsView(boolean enableDatasetdetailsView) {
        this.enableDatasetdetailsView = enableDatasetdetailsView;
    }

    public String getLandingPageURL() {
        return landingPageURL;
    }

    public void setLandingPageURL(String landingPageURL) {
        this.landingPageURL = landingPageURL;
    }

    public String getConvertXLSXAPI() {
        return ConfigurationService.INSTANCE.getConvertXLSXAPI();
    }

    /**
     * We expect that jobs/tasks that have JobState.RUNNING or JobState.PROCESSING on start-up
     * actually are incomplete. Furthermore, we assume JobState.WAITING actually means
     * JobState.CANCELED.
     */
    private void updateJobTable() {
        JobMetaDataDao jmdDao = new JobMetaDataDao();
        List<JobMetaData> allJmds = jmdDao.getAll();

        // Running and processing -> incomplete
        allJmds.stream()
                .filter(jmd -> JobState.RUNNING.equals(jmd.getJobState())
                        || JobState.PROCESSING.equals(jmd.getJobState()))
                .sequential()
                .forEach(jmd -> {
                    jmd.setJobState(JobState.INCOMPLETE);
                    try {
                        jmdDao.merge(jmd);
                    } catch (MergeException e) {
                        logger.warn("Job " + jmd.getJobId() + " is considererd incomplete but still has state: " + jmd.getJobState().name(), e);
                    }
                });

        // waiting -> canceled
        allJmds.stream()
                .filter(jmd -> JobState.WAITING.equals(jmd.getJobState()))
                .sequential()
                .forEach(jmd -> {
                    jmd.setJobState(JobState.CANCELED);
                    try {
                        jmdDao.merge(jmd);
                    } catch (MergeException e) {
                        logger.warn("Job " + jmd.getJobId() + " is considererd incomplete but still has state: " + jmd.getJobState().name(), e);
                    }
                });
    }

    public boolean isOidcEnabled() {
        return oidcEnabled;
    }

    public void setOidcEnabled(boolean oidcEnabled) {
        this.oidcEnabled = oidcEnabled;
    }

    /**
     * Theme resolver helper class
     */
    private class ThemeResolver implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = -9000736094793926791L;

        /**
         * Name of the theme
         */
        private final String theme;

        /**
         * Create theme resolver
         *
         * @param theme theme to use
         */
        public ThemeResolver(String theme) {
            if (theme != null) {
                this.theme = theme;
            } else {
                this.theme = "cupertino"; // default
            }
        }

        /**
         * Get theme name to use
         *
         * @return theme name
         */
        @Override
        public String toString() {
            return this.theme;
        }
    }

    /**
     * Crazy theme resolver helper class
     */
    private class CrazyThemeResolver extends ThemeResolver {

        /**
         *
         */
        private static final long serialVersionUID = -822796936628484888L;

        /**
         * Crazy theme resolver
         */
        public CrazyThemeResolver() {
            super(null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            Random rand = new Random();
            return ConfigurationBean.AVAILABLE_THEMES.get(rand.nextInt(ConfigurationBean.AVAILABLE_THEMES.size()));
        }

    }
}