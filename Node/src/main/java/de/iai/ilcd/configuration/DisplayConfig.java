package de.iai.ilcd.configuration;

import de.iai.ilcd.model.dao.DependenciesMode;

public class DisplayConfig {

    /**
     * Flag to indicate if the additional link for alternative rendering method of dataset detail should be shown in
     * data set
     * overview page
     */
    private boolean showAlternativeHtmlLink = false;

    /**
     * Flag to indicate if the back links at the bottom of the dataset detail view pages should be shown
     */
    private boolean showBackLinks = false;

    /**
     * Flag to indicate if the back button "Go back" at the top of the dataset detail view pages should be shown
     */
    private boolean showBackButton = true;

    /**
     * Flag to indicate if the back button "Close" at the top of the dataset detail view pages should be shown
     */
    private boolean showCloseButton = true;

    /**
     * Flag to indicate if the login link should be shown
     */
    private boolean showLoginLink = false;

    /**
     * Flag to indicate if the node id should be shown as link
     * search results
     */
    private boolean showNodeIdAsLink = false;

    /**
     * Flag to indicate whether the download-as-ZIP-link should be displayed
     * in the header
     */
    private boolean showDownloadAllLink = true;

    /**
     * Flag to indicate whether hidden datastocks should be shown by default
     */
    private boolean showHiddenDatastocksDefault = false;

    /**
     * indicates the default state of the "hide older versions" checkbox
     */
    private boolean defaultMostRecentVersionOnly = true;

    /**
     * Flag to indicate whether to show the options for dependency processing
     * in assign data stock dialogs
     */
    private boolean showDependenciesOptions = false;

    /**
     * show option 0 for dependency processing
     */
    private boolean showDependenciesOption0 = false;

    /**
     * show option 1 for dependency processing
     */
    private boolean showDependenciesOption1 = false;

    /**
     * show option 2 for dependency processing
     */
    private boolean showDependenciesOption2 = false;

    /**
     * show option 3 for dependency processing
     */
    private boolean showDependenciesOption3 = false;

    /**
     * sets the default dependency option
     */
    private DependenciesMode dependenciesOptionDefault = DependenciesMode.fromValue(0);

    /**
     * Flag to indicate whether to sort exchanges in process detail view
     */
    private String sortExchanges = "default";

    /**
     * Flag to indicate whether to sort indicators in process detail view
     */
    private String sortIndicators = "natural";

    /* Flags for identification of visibility of the columns in the public tables with contacts */
    private boolean showContactCategory = true;

    private boolean showContactEmail = true;

    private boolean showContactHomePage = true;

    private boolean showContactName = true;

    /* Flags for identification of visibility of the columns in the public tables with elementary flows */
    private boolean showElementaryFlowCategory = true;

    private boolean showElementaryFlowName = true;

    private boolean showElementaryFlowRefProperty = true;

    private boolean showElementaryFlowRefPropertyUnit = true;

    /* Flags for identification of visibility of the columns in the public tables with flow properties */
    private boolean showFlowPropertyCategory = true;

    private boolean showFlowPropertyDefaultUnit = true;

    private boolean showFlowPropertyDefaultUnitGroup = true;

    private boolean showFlowPropertyName = true;

    /* Flags for identification of visibility of the columns in the public tables with LCIA methods */
    private boolean showMethodDuration = true;

    private boolean showMethodName = true;

    private boolean showMethodReferenceYear = true;

    private boolean showMethodType = true;

    /* Flags for identification of visibility of the columns in the public tables with processes */
    private boolean showProcessClassification = true;

    private boolean showProcessLocation = true;

    private boolean showProcessType = true;

    private boolean showProcessSubType = false;

    private boolean showProcessName = true;

    private boolean showProcessNodeId = true;

    private boolean showProcessReferenceYear = true;

    private boolean showProcessValidUntil = true;

    private boolean showManageProcessType = true;

    private boolean showManageProcessSubType = false;

    private boolean showManageProcessLocation = true;

    private boolean showManageProcessReferenceYear = true;

    private boolean showManageProcessValidUntil = true;

    private boolean showManageProcessTags = false;

    private boolean showManageProcessCompliance = false;

    /* Flags for identification of visibility of the columns in the public tables with product flows */
    private boolean showProductFlowCategory = true;

    private boolean showProductFlowName = true;

    private boolean showProductFlowRefProperty = true;

    private boolean showProductFlowRefPropertyUnit = true;

    private boolean showProductFlowType = true;

    /* Flags for identification of visibility of the columns in the public tables with sources */
    private boolean showSourceCategory = true;

    private boolean showSourceName = true;

    private boolean showSourceType = true;

    /* Flags for identification of visibility of the columns in the public tables with unit groups */
    private boolean showUnitGroupCategory = true;

    private boolean showUnitGroupDefaultUnit = true;

    private boolean showUnitGroupName = true;

    private boolean showManageDatasetImportDate = true;

    private boolean showManageDatasetRootStock = true;

    private boolean showManageDatasetContainedIn = true;

    private boolean showManageDatasetVersion = true;

    private boolean showManageDatasetMostRecentVersion = true;

    private boolean showManageDatasetClassification = true;

    private boolean showManageProcessClassificationId = false;

    private boolean showManageProcessOwner = false;

    private boolean showManageProcessDatasource = false;

    private boolean showManageProcessRegistrationNumber = false;

    private boolean showManageProcessRegistrationAuthority = false;

    private boolean showDatasetDetailProcessType = true;

    /* Flags for showing tags in process detail view */
    private boolean showTagsHorizontally = true;

    private String tagsDelimiter = "; ";

    private boolean showTagName = true;

    /* Flags for identification of visibility of the columns in the process detail view */
    private boolean showProcessesDetailLciaResultsFirst = false;

    private boolean showProcessesDetailExchangesLocation = true;

    private boolean showProcessesDetailExchangesFunctionType = false;

    private boolean showProcessesDetailExchangesDataSourceType = false;

    private boolean showProcessesDetailExchangesDataDerivativeType = false;

    private boolean showProcessesDetailExchangesComment = false;

    private boolean showProcessesDetailExchangesUncertaintyDistType = false;

    private boolean showProcessesDetailExchangesRelativeStdDev = false;

    private boolean showProcessesDetailExchangesShowFunctionalUnit = false;

    private boolean showProcessesDetailLciaResultsShowFunctionalUnit = false;

    private boolean showProcessesDetailLciaResultsUncertaintyDistType = false;

    private boolean showProcessesDetailLciaResultsRelativeStdDev = false;

    private boolean showProcessDetailExchangesMinMaxValues = true;

    private boolean preloadFlowInOutLinks = false;

    private String showProcessesDetailActiveSections = "0,1,2,3,4";

    private boolean datasetDetailShortTitle = false;

    public boolean isShowDatasetDetailProcessType() {
        return showDatasetDetailProcessType;
    }

    public void setShowDatasetDetailProcessType(boolean showDatasetDetailProcessType) {
        this.showDatasetDetailProcessType = showDatasetDetailProcessType;
    }

    /**
     * Indicates if the additional link for alternative rendering method of dataset detail should be shown in data set
     * overview page
     *
     * @return <code>true</code> if the additional link for alternative rendering method of dataset detail should be
     * shown in
     * data set overview page, else <code>false</code>
     */
    public boolean isShowAlternativeHtmlLink() {
        return this.showAlternativeHtmlLink;
    }

    /**
     * Set if the additional link for alternative rendering method of dataset detail should be shown in data set
     * overview page
     *
     * @param showAlternativeHtmlLink the additional link for alternative rendering method of dataset detail to be set
     */
    public void setShowAlternativeHtmlLink(boolean showAlternativeHtmlLink) {
        this.showAlternativeHtmlLink = showAlternativeHtmlLink;
    }

    /**
     * Indicates if the back links on dataset detail view page should be shown
     *
     * @return <code>true</code> if the back links on dataset detail view page should be shown, else <code>false</code>
     */
    public boolean isShowBackLinks() {
        return this.showBackLinks;
    }

    /**
     * Set if the back links on dataset detail view page should be shown
     *
     * @param showBackLinks the back links on dataset detail view page should be shown to be set
     */
    public void setShowBackLinks(boolean showBackLinks) {
        this.showBackLinks = showBackLinks;
    }

    public boolean isShowContactCategory() {
        return showContactCategory;
    }

    public void setShowContactCategory(boolean showContactCategory) {
        this.showContactCategory = showContactCategory;
    }

    public boolean isShowContactEmail() {
        return showContactEmail;
    }

    public void setShowContactEmail(boolean showContactEmail) {
        this.showContactEmail = showContactEmail;
    }

    public boolean isShowContactHomePage() {
        return showContactHomePage;
    }

    public void setShowContactHomePage(boolean showContactHomePage) {
        this.showContactHomePage = showContactHomePage;
    }

    public boolean isShowContactName() {
        return showContactName;
    }

    public void setShowContactName(boolean showContactName) {
        this.showContactName = showContactName;
    }

    public boolean isShowElementaryFlowCategory() {
        return showElementaryFlowCategory;
    }

    public void setShowElementaryFlowCategory(boolean showElementaryFlowCategory) {
        this.showElementaryFlowCategory = showElementaryFlowCategory;
    }

    public boolean isShowElementaryFlowName() {
        return showElementaryFlowName;
    }

    public void setShowElementaryFlowName(boolean showElementaryFlowName) {
        this.showElementaryFlowName = showElementaryFlowName;
    }

    public boolean isShowElementaryFlowRefProperty() {
        return showElementaryFlowRefProperty;
    }

    public void setShowElementaryFlowRefProperty(boolean showElementaryFlowRefProperty) {
        this.showElementaryFlowRefProperty = showElementaryFlowRefProperty;
    }

    public boolean isShowElementaryFlowRefPropertyUnit() {
        return showElementaryFlowRefPropertyUnit;
    }

    public void setShowElementaryFlowRefPropertyUnit(boolean showElementaryFlowRefPropertyUnit) {
        this.showElementaryFlowRefPropertyUnit = showElementaryFlowRefPropertyUnit;
    }

    public boolean isShowFlowPropertyCategory() {
        return showFlowPropertyCategory;
    }

    public void setShowFlowPropertyCategory(boolean showFlowPropertyCategory) {
        this.showFlowPropertyCategory = showFlowPropertyCategory;
    }

    public boolean isShowFlowPropertyDefaultUnit() {
        return showFlowPropertyDefaultUnit;
    }

    public void setShowFlowPropertyDefaultUnit(boolean showFlowPropertyDefaultUnit) {
        this.showFlowPropertyDefaultUnit = showFlowPropertyDefaultUnit;
    }

    public boolean isShowFlowPropertyDefaultUnitGroup() {
        return showFlowPropertyDefaultUnitGroup;
    }

    public void setShowFlowPropertyDefaultUnitGroup(boolean showFlowPropertyDefaultUnitGroup) {
        this.showFlowPropertyDefaultUnitGroup = showFlowPropertyDefaultUnitGroup;
    }

    public boolean isShowFlowPropertyName() {
        return showFlowPropertyName;
    }

    public void setShowFlowPropertyName(boolean showFlowPropertyName) {
        this.showFlowPropertyName = showFlowPropertyName;
    }

    public boolean isShowLoginLink() {
        return showLoginLink;
    }

    public void setShowLoginLink(boolean showLoginLink) {
        this.showLoginLink = showLoginLink;
    }

    public boolean isShowMethodDuration() {
        return showMethodDuration;
    }

    public void setShowMethodDuration(boolean showMethodDuration) {
        this.showMethodDuration = showMethodDuration;
    }

    public boolean isShowMethodName() {
        return showMethodName;
    }

    public void setShowMethodName(boolean showMethodName) {
        this.showMethodName = showMethodName;
    }

    public boolean isShowMethodReferenceYear() {
        return showMethodReferenceYear;
    }

    public void setShowMethodReferenceYear(boolean showMethodReferenceYear) {
        this.showMethodReferenceYear = showMethodReferenceYear;
    }

    public boolean isShowMethodType() {
        return showMethodType;
    }

    public void setShowMethodType(boolean showMethodType) {
        this.showMethodType = showMethodType;
    }

    public boolean isShowNodeIdAsLink() {
        return showNodeIdAsLink;
    }

    public void setShowNodeIdAsLink(boolean showNodeIdAsLink) {
        this.showNodeIdAsLink = showNodeIdAsLink;
    }

    public boolean isShowProcessClassification() {
        return showProcessClassification;
    }

    public void setShowProcessClassification(boolean showProcessClassification) {
        this.showProcessClassification = showProcessClassification;
    }

    public boolean isShowProcessLocation() {
        return showProcessLocation;
    }

    public void setShowProcessLocation(boolean showProcessLocation) {
        this.showProcessLocation = showProcessLocation;
    }

    public boolean isShowProcessName() {
        return showProcessName;
    }

    public void setShowProcessName(boolean showProcessName) {
        this.showProcessName = showProcessName;
    }

    public boolean isShowProcessNodeId() {
        return showProcessNodeId;
    }

    public void setShowProcessNodeId(boolean showProcessNodeId) {
        this.showProcessNodeId = showProcessNodeId;
    }

    public boolean isShowProcessReferenceYear() {
        return showProcessReferenceYear;
    }

    public void setShowProcessReferenceYear(boolean showProcessReferenceYear) {
        this.showProcessReferenceYear = showProcessReferenceYear;
    }

    public boolean isShowProcessValidUntil() {
        return showProcessValidUntil;
    }

    public void setShowProcessValidUntil(boolean showProcessValidUntil) {
        this.showProcessValidUntil = showProcessValidUntil;
    }

    public boolean isShowProductFlowCategory() {
        return showProductFlowCategory;
    }

    public void setShowProductFlowCategory(boolean showProductFlowCategory) {
        this.showProductFlowCategory = showProductFlowCategory;
    }

    public boolean isShowProductFlowName() {
        return showProductFlowName;
    }

    public void setShowProductFlowName(boolean showProductFlowName) {
        this.showProductFlowName = showProductFlowName;
    }

    public boolean isShowProductFlowRefProperty() {
        return showProductFlowRefProperty;
    }

    public void setShowProductFlowRefProperty(boolean showProductFlowRefProperty) {
        this.showProductFlowRefProperty = showProductFlowRefProperty;
    }

    public boolean isShowProductFlowRefPropertyUnit() {
        return showProductFlowRefPropertyUnit;
    }

    public void setShowProductFlowRefPropertyUnit(boolean showProductFlowRefPropertyUnit) {
        this.showProductFlowRefPropertyUnit = showProductFlowRefPropertyUnit;
    }

    public boolean isShowProductFlowType() {
        return showProductFlowType;
    }

    public void setShowProductFlowType(boolean showProductFlowType) {
        this.showProductFlowType = showProductFlowType;
    }

    public boolean isShowSourceCategory() {
        return showSourceCategory;
    }

    public void setShowSourceCategory(boolean showSourceCategory) {
        this.showSourceCategory = showSourceCategory;
    }

    public boolean isShowSourceName() {
        return showSourceName;
    }

    public void setShowSourceName(boolean showSourceName) {
        this.showSourceName = showSourceName;
    }

    public boolean isShowSourceType() {
        return showSourceType;
    }

    public void setShowSourceType(boolean showSourceType) {
        this.showSourceType = showSourceType;
    }

    public boolean isShowUnitGroupCategory() {
        return showUnitGroupCategory;
    }

    public void setShowUnitGroupCategory(boolean showUnitGroupCategory) {
        this.showUnitGroupCategory = showUnitGroupCategory;
    }

    public boolean isShowUnitGroupDefaultUnit() {
        return showUnitGroupDefaultUnit;
    }

    public void setShowUnitGroupDefaultUnit(boolean showUnitGroupDefaultUnit) {
        this.showUnitGroupDefaultUnit = showUnitGroupDefaultUnit;
    }

    public boolean isShowUnitGroupName() {
        return showUnitGroupName;
    }

    public void setShowUnitGroupName(boolean showUnitGroupName) {
        this.showUnitGroupName = showUnitGroupName;
    }

    public boolean isShowProcessesDetailLciaResultsFirst() {
        return showProcessesDetailLciaResultsFirst;
    }

    public void setShowProcessesDetailLciaResultsFirst(boolean showProcessesDetailLciaResultsFirst) {
        this.showProcessesDetailLciaResultsFirst = showProcessesDetailLciaResultsFirst;
    }

    public boolean isShowProcessesDetailExchangesLocation() {
        return showProcessesDetailExchangesLocation;
    }

    public void setShowProcessesDetailExchangesLocation(boolean showProcessesDetailExchangesLocation) {
        this.showProcessesDetailExchangesLocation = showProcessesDetailExchangesLocation;
    }

    public boolean isShowProcessesDetailExchangesFunctionType() {
        return showProcessesDetailExchangesFunctionType;
    }

    public void setShowProcessesDetailExchangesFunctionType(boolean showProcessesDetailExchangesFunctionType) {
        this.showProcessesDetailExchangesFunctionType = showProcessesDetailExchangesFunctionType;
    }

    public boolean isShowProcessesDetailExchangesDataSourceType() {
        return showProcessesDetailExchangesDataSourceType;
    }

    public void setShowProcessesDetailExchangesDataSourceType(boolean showProcessesDetailExchangesDataSourceType) {
        this.showProcessesDetailExchangesDataSourceType = showProcessesDetailExchangesDataSourceType;
    }

    public boolean isShowProcessesDetailExchangesDataDerivativeType() {
        return showProcessesDetailExchangesDataDerivativeType;
    }

    public void setShowProcessesDetailExchangesDataDerivativeType(boolean showProcessesDetailExchangesDataDerivativeType) {
        this.showProcessesDetailExchangesDataDerivativeType = showProcessesDetailExchangesDataDerivativeType;
    }

    public boolean isShowProcessesDetailExchangesComment() {
        return showProcessesDetailExchangesComment;
    }

    public void setShowProcessesDetailExchangesComment(boolean showProcessesDetailExchangesComment) {
        this.showProcessesDetailExchangesComment = showProcessesDetailExchangesComment;
    }

    public boolean isShowProcessesDetailExchangesUncertaintyDistType() {
        return showProcessesDetailExchangesUncertaintyDistType;
    }

    public void setShowProcessesDetailExchangesUncertaintyDistType(
            boolean showProcessesDetailExchangesUncertaintyDistType) {
        this.showProcessesDetailExchangesUncertaintyDistType = showProcessesDetailExchangesUncertaintyDistType;
    }

    public boolean isShowProcessesDetailExchangesRelativeStdDev() {
        return showProcessesDetailExchangesRelativeStdDev;
    }

    public void setShowProcessesDetailExchangesRelativeStdDev(boolean showProcessesDetailExchangesRelativeStdDev) {
        this.showProcessesDetailExchangesRelativeStdDev = showProcessesDetailExchangesRelativeStdDev;
    }

    public boolean isShowProcessesDetailExchangesShowFunctionalUnit() {
        return showProcessesDetailExchangesShowFunctionalUnit;
    }

    public void setShowProcessesDetailExchangesShowFunctionalUnit(boolean showProcessesDetailExchangesShowFunctionalUnit) {
        this.showProcessesDetailExchangesShowFunctionalUnit = showProcessesDetailExchangesShowFunctionalUnit;
    }

    public boolean isShowProcessesDetailLciaResultsShowFunctionalUnit() {
        return showProcessesDetailLciaResultsShowFunctionalUnit;
    }

    public void setShowProcessesDetailLciaResultsShowFunctionalUnit(
            boolean showProcessesDetailLciaResultsShowFunctionalUnit) {
        this.showProcessesDetailLciaResultsShowFunctionalUnit = showProcessesDetailLciaResultsShowFunctionalUnit;
    }

    public boolean isShowProcessesDetailLciaResultsUncertaintyDistType() {
        return showProcessesDetailLciaResultsUncertaintyDistType;
    }

    public void setShowProcessesDetailLciaResultsUncertaintyDistType(
            boolean showProcessesDetailLciaResultsUncertaintyDistType) {
        this.showProcessesDetailLciaResultsUncertaintyDistType = showProcessesDetailLciaResultsUncertaintyDistType;
    }

    public boolean isShowProcessesDetailLciaResultsRelativeStdDev() {
        return showProcessesDetailLciaResultsRelativeStdDev;
    }

    public void setShowProcessesDetailLciaResultsRelativeStdDev(boolean showProcessesDetailLciaResultsRelativeStdDev) {
        this.showProcessesDetailLciaResultsRelativeStdDev = showProcessesDetailLciaResultsRelativeStdDev;
    }

    public String getShowProcessesDetailActiveSections() {
        return showProcessesDetailActiveSections;
    }

    public void setShowProcessesDetailActiveSections(String showProcessesDetailActiveSections) {
        this.showProcessesDetailActiveSections = showProcessesDetailActiveSections;
    }

    public boolean isPreloadFlowInOutLinks() {
        return preloadFlowInOutLinks;
    }

    public void setPreloadFlowInOutLinks(boolean preloadFlowInOutLinks) {
        this.preloadFlowInOutLinks = preloadFlowInOutLinks;
    }

    public boolean isDatasetDetailShortTitle() {
        return datasetDetailShortTitle;
    }

    public void setDatasetDetailShortTitle(boolean datasetDetailShortTitle) {
        this.datasetDetailShortTitle = datasetDetailShortTitle;
    }

    public void configure(ConfigurationService configurationService) {
        this.showAlternativeHtmlLink = configurationService.fileConfig.getBoolean("showAlternativeHtmlLink", false);
        this.showBackLinks = configurationService.fileConfig.getBoolean("showBackLinks", true);
        this.showBackButton = configurationService.fileConfig.getBoolean("display.show.datasetdetail.back.button", true);
        this.showCloseButton = configurationService.fileConfig.getBoolean("display.show.datasetdetail.close.button", true);
        this.showLoginLink = configurationService.fileConfig.getBoolean("showLoginLink", true);
        this.showNodeIdAsLink = configurationService.fileConfig.getBoolean("results.showNodeIdAsLink", true);
        this.showDatasetDetailProcessType = configurationService.fileConfig.getBoolean("processes.detail.type", true);
        this.showDownloadAllLink = configurationService.fileConfig.getBoolean("display.show.download.datastock", true);
        this.sortExchanges = configurationService.fileConfig.getString("display.sort.exchanges", "default");
        this.sortIndicators = configurationService.fileConfig.getString("display.sort.indicators", "natural");
        this.showHiddenDatastocksDefault = configurationService.fileConfig.getBoolean("display.datastocks.showhidden.default", false);
        this.defaultMostRecentVersionOnly = configurationService.fileConfig.getBoolean("display.hidepreviousversions.default", true);
        this.showDependenciesOptions = configurationService.fileConfig.getBoolean("display.show.dependencies.options", true);
        this.showDependenciesOption0 = configurationService.fileConfig.getBoolean("display.show.dependencies.option0", true);
        this.showDependenciesOption1 = configurationService.fileConfig.getBoolean("display.show.dependencies.option1", true);
        this.showDependenciesOption2 = configurationService.fileConfig.getBoolean("display.show.dependencies.option2", true);
        this.showDependenciesOption3 = configurationService.fileConfig.getBoolean("display.show.dependencies.option3", true);
        this.showTagsHorizontally = configurationService.fileConfig.getBoolean("display.tags.orientation.horizontal", true);
        this.tagsDelimiter = configurationService.fileConfig.getString("display.tags.delimiter", "; ");
        this.showTagName = configurationService.fileConfig.getBoolean("display.tags.show.name", true);
        this.dependenciesOptionDefault = DependenciesMode.fromValue(configurationService.fileConfig.getInteger("display.show.dependencies.default", 0));
        this.showProcessesDetailActiveSections = String.join(",", configurationService.fileConfig.getStringArray("processes.detail.activesections"));
        this.showProcessDetailExchangesMinMaxValues = configurationService.fileConfig.getBoolean("processes.detail.exchanges.minMaxValues", true);
        this.preloadFlowInOutLinks = configurationService.fileConfig.getBoolean("flows.overview.in-out-flows.links.preload", false);
        this.datasetDetailShortTitle = configurationService.fileConfig.getBoolean("dataset.detail.title.short", false);
    }

    public void configureColumns(ConfigurationService configurationService) {
        this.showProcessName = configurationService.fileConfig.getBoolean("processes.name", true);
        this.showProcessLocation = configurationService.fileConfig.getBoolean("processes.location", true);
        this.showProcessType = configurationService.fileConfig.getBoolean("processes.type", true);
        this.showProcessSubType = configurationService.fileConfig.getBoolean("processes.subtype", false);
        this.showProcessClassification = configurationService.fileConfig.getBoolean("processes.classification", true);
        this.showProcessReferenceYear = configurationService.fileConfig.getBoolean("processes.referenceYear", true);
        this.showProcessValidUntil = configurationService.fileConfig.getBoolean("processes.validUntil", true);
        this.showProcessNodeId = configurationService.fileConfig.getBoolean("processes.nodeId", true);
        this.showProcessesDetailLciaResultsFirst = configurationService.fileConfig.getBoolean("processes.detail.lciaresultsfirst", false);
        this.showProcessesDetailExchangesLocation = configurationService.fileConfig.getBoolean("processes.detail.exchanges.location", true);
        this.showProcessesDetailExchangesFunctionType = configurationService.fileConfig.getBoolean("processes.detail.exchanges.functionType", false);
        this.showProcessesDetailExchangesDataSourceType = configurationService.fileConfig.getBoolean("processes.detail.exchanges.dataSourceType", false);
        this.showProcessesDetailExchangesDataDerivativeType = configurationService.fileConfig.getBoolean("processes.detail.exchanges.dataSourceType", false);
        this.showProcessesDetailExchangesComment = configurationService.fileConfig.getBoolean("processes.detail.exchanges.comment", false);
        this.showProcessesDetailExchangesUncertaintyDistType = configurationService.fileConfig.getBoolean("processes.detail.exchanges.uncertaintyDistType", false);
        this.showProcessesDetailExchangesRelativeStdDev = configurationService.fileConfig.getBoolean("processes.detail.exchanges.relativeStdDev", false);
        this.showProcessesDetailExchangesShowFunctionalUnit = configurationService.fileConfig.getBoolean("processes.detail.exchanges.showFunctionalUnit", false);
        this.showProcessesDetailLciaResultsShowFunctionalUnit = configurationService.fileConfig.getBoolean("processes.detail.lciaresults.showFunctionalUnit", false);
        this.showProcessesDetailLciaResultsUncertaintyDistType = configurationService.fileConfig.getBoolean("processes.detail.lciaresults.uncertaintyDistType", false);
        this.showProcessesDetailLciaResultsRelativeStdDev = configurationService.fileConfig.getBoolean("processes.detail.lciaresults.relativeStdDev", false);
        this.showMethodName = configurationService.fileConfig.getBoolean("methods.name", true);
        this.showMethodType = configurationService.fileConfig.getBoolean("methods.type", true);
        this.showMethodReferenceYear = configurationService.fileConfig.getBoolean("methods.referenceYear", true);
        this.showMethodDuration = configurationService.fileConfig.getBoolean("methods.duration", true);
        this.showElementaryFlowName = configurationService.fileConfig.getBoolean("elementaryFlows.name", true);
        this.showElementaryFlowCategory = configurationService.fileConfig.getBoolean("elementaryFlows.category", true);
        this.showElementaryFlowRefProperty = configurationService.fileConfig.getBoolean("elementaryFlows.referenceProperty", true);
        this.showElementaryFlowRefPropertyUnit = configurationService.fileConfig.getBoolean("elementaryFlows.referencePropertyUnit", true);
        this.showProductFlowName = configurationService.fileConfig.getBoolean("productFlows.name", true);
        this.showProductFlowType = configurationService.fileConfig.getBoolean("productFlows.type", true);
        this.showProductFlowCategory = configurationService.fileConfig.getBoolean("productFlows.category", true);
        this.showProductFlowRefProperty = configurationService.fileConfig.getBoolean("productFlows.referenceProperty", true);
        this.showProductFlowRefPropertyUnit = configurationService.fileConfig.getBoolean("productFlows.referencePropertyUnit", true);
        this.showFlowPropertyName = configurationService.fileConfig.getBoolean("flowProperties.name", true);
        this.showFlowPropertyCategory = configurationService.fileConfig.getBoolean("flowProperties.category", true);
        this.showFlowPropertyDefaultUnit = configurationService.fileConfig.getBoolean("flowProperties.defaultUnit", true);
        this.showFlowPropertyDefaultUnitGroup = configurationService.fileConfig.getBoolean("flowProperties.defaultUnitGroup", true);
        this.showUnitGroupName = configurationService.fileConfig.getBoolean("unitGroups.name", true);
        this.showUnitGroupCategory = configurationService.fileConfig.getBoolean("unitGroups.category", true);
        this.showUnitGroupDefaultUnit = configurationService.fileConfig.getBoolean("unitGroups.defaultUnit", true);
        this.showSourceName = configurationService.fileConfig.getBoolean("sources.name", true);
        this.showSourceCategory = configurationService.fileConfig.getBoolean("sources.category", true);
        this.showSourceType = configurationService.fileConfig.getBoolean("sources.type", true);
        this.showContactName = configurationService.fileConfig.getBoolean("contacts.name", true);
        this.showContactCategory = configurationService.fileConfig.getBoolean("contacts.category", true);
        this.showContactEmail = configurationService.fileConfig.getBoolean("contacts.email", true);
        this.showContactHomePage = configurationService.fileConfig.getBoolean("contacts.homePage", true);
        this.showManageDatasetImportDate = configurationService.fileConfig.getBoolean("datasets.manage.importdate", true);
        this.showManageDatasetRootStock = configurationService.fileConfig.getBoolean("datasets.manage.rootdatastock", true);
        this.showManageDatasetContainedIn = configurationService.fileConfig.getBoolean("datasets.manage.containedin", true);
        this.showManageDatasetVersion = configurationService.fileConfig.getBoolean("datasets.manage.version", true);
        this.showManageDatasetMostRecentVersion = configurationService.fileConfig.getBoolean("datasets.manage.mostrecent", true);
        this.showManageDatasetClassification = configurationService.fileConfig.getBoolean("datasets.manage.classification", true);
        this.showManageProcessType = configurationService.fileConfig.getBoolean("processes.manage.type", true);
        this.showManageProcessSubType = configurationService.fileConfig.getBoolean("processes.manage.subtype", false);
        this.showManageProcessLocation = configurationService.fileConfig.getBoolean("processes.manage.location", true);
        this.showManageProcessReferenceYear = configurationService.fileConfig.getBoolean("processes.manage.referenceyear", true);
        this.showManageProcessValidUntil = configurationService.fileConfig.getBoolean("processes.manage.validuntil", true);
        this.showManageProcessClassificationId = configurationService.fileConfig.getBoolean("processes.manage.classificationid", false);
        this.showManageProcessOwner = configurationService.fileConfig.getBoolean("processes.manage.owner", false);
        this.showManageProcessDatasource = configurationService.fileConfig.getBoolean("processes.manage.datasource", false);
        this.showManageProcessRegistrationAuthority = configurationService.fileConfig.getBoolean("processes.manage.registrationAuthority", false);
        this.showManageProcessRegistrationNumber = configurationService.fileConfig.getBoolean("processes.manage.registrationNumber", false);
        this.showManageProcessTags = configurationService.fileConfig.getBoolean("processes.manage.tags", false);
        this.showManageProcessCompliance = configurationService.fileConfig.getBoolean("processes.manage.compliance", false);
    }

    public boolean isShowProcessType() {
        return showProcessType;
    }

    public void setShowProcessType(boolean showProcessType) {
        this.showProcessType = showProcessType;
    }

    public boolean isShowManageDatasetImportDate() {
        return showManageDatasetImportDate;
    }

    public void setShowManageDatasetImportDate(boolean showManageDatasetImportDate) {
        this.showManageDatasetImportDate = showManageDatasetImportDate;
    }

    public boolean isShowManageDatasetRootStock() {
        return showManageDatasetRootStock;
    }

    public void setShowManageDatasetRootStock(boolean showManageDatasetRootStock) {
        this.showManageDatasetRootStock = showManageDatasetRootStock;
    }

    public boolean isShowManageDatasetContainedIn() {
        return showManageDatasetContainedIn;
    }

    public void setShowManageDatasetContainedIn(boolean showManageDatasetContainedIn) {
        this.showManageDatasetContainedIn = showManageDatasetContainedIn;
    }

    public boolean isShowManageDatasetVersion() {
        return showManageDatasetVersion;
    }

    public void setShowManageDatasetVersion(boolean showManageDatasetVersion) {
        this.showManageDatasetVersion = showManageDatasetVersion;
    }

    public boolean isShowManageDatasetMostRecentVersion() {
        return showManageDatasetMostRecentVersion;
    }

    public void setShowManageDatasetMostRecentVersion(boolean showManageDatasetMostRecentVersion) {
        this.showManageDatasetMostRecentVersion = showManageDatasetMostRecentVersion;
    }

    public boolean isShowManageDatasetClassification() {
        return showManageDatasetClassification;
    }

    public void setShowManageDatasetClassification(boolean showManageDatasetClassification) {
        this.showManageDatasetClassification = showManageDatasetClassification;
    }

    public boolean isShowManageProcessType() {
        return showManageProcessType;
    }

    public void setShowManageProcessType(boolean showManageProcessType) {
        this.showManageProcessType = showManageProcessType;
    }

    public boolean isShowManageProcessLocation() {
        return showManageProcessLocation;
    }

    public void setShowManageProcessLocation(boolean showManageProcessLocation) {
        this.showManageProcessLocation = showManageProcessLocation;
    }

    public boolean isShowManageProcessReferenceYear() {
        return showManageProcessReferenceYear;
    }

    public void setShowManageProcessReferenceYear(boolean showManageProcessReferenceYear) {
        this.showManageProcessReferenceYear = showManageProcessReferenceYear;
    }

    public boolean isShowManageProcessValidUntil() {
        return showManageProcessValidUntil;
    }

    public void setShowManageProcessValidUntil(boolean showManageProcessValidUntil) {
        this.showManageProcessValidUntil = showManageProcessValidUntil;
    }

    public boolean isShowManageProcessSubType() {
        return showManageProcessSubType;
    }

    public void setShowManageProcessSubType(boolean showManageProcessSubType) {
        this.showManageProcessSubType = showManageProcessSubType;
    }

    public boolean isShowProcessSubType() {
        return showProcessSubType;
    }

    public void setShowProcessSubType(boolean showProcessSubType) {
        this.showProcessSubType = showProcessSubType;
    }

    public boolean isShowManageProcessClassificationId() {
        return showManageProcessClassificationId;
    }

    public void setShowManageProcessClassificationId(boolean showClassificationId) {
        this.showManageProcessClassificationId = showClassificationId;
    }

    public boolean isShowManageProcessOwner() {
        return this.showManageProcessOwner;
    }

    public void setShowManageProcessOwner(boolean showManageProcessOwner) {
        this.showManageProcessOwner = showManageProcessOwner;
    }

    public boolean isShowManageProcessDatasource() {
        return showManageProcessDatasource;
    }

    public void setShowManageProcessDatasource(boolean showManageProcessDatasource) {
        this.showManageProcessDatasource = showManageProcessDatasource;
    }

    public boolean isShowTagsHorizontally() {
        return showTagsHorizontally;
    }

    public void setShowTagsHorizontally(boolean showTagsHorizontally) {
        this.showTagsHorizontally = showTagsHorizontally;
    }

    public String getTagsDelimiter() {
        return tagsDelimiter;
    }

    public void setTagsDelimiter(String tagsDelimiter) {
        this.tagsDelimiter = tagsDelimiter;
    }

    public boolean isShowTagName() {
        return showTagName;
    }

    public void setShowTagName(boolean showTagName) {
        this.showTagName = showTagName;
    }

    public boolean isShowDownloadAllLink() {
        return showDownloadAllLink;
    }

    public void setShowDownloadAllLink(boolean showDownloadAllLink) {
        this.showDownloadAllLink = showDownloadAllLink;
    }

    public String getSortIndicators() {
        return sortIndicators;
    }

    public void setSortIndicators(String sortIndicators) {
        this.sortIndicators = sortIndicators;
    }

    public boolean isShowHiddenDatastocksDefault() {
        return showHiddenDatastocksDefault;
    }

    public void setShowHiddenDatastocksDefault(boolean showHiddenDatastocksDefault) {
        this.showHiddenDatastocksDefault = showHiddenDatastocksDefault;
    }

    public boolean isDefaultMostRecentVersionOnly() {
        return defaultMostRecentVersionOnly;
    }

    public void setDefaultMostRecentVersionOnly(boolean defaultMostRecentVersionOnly) {
        this.defaultMostRecentVersionOnly = defaultMostRecentVersionOnly;
    }

    public boolean isShowDependenciesOptions() {
        return showDependenciesOptions;
    }

    public void setShowDependenciesOptions(boolean showDependenciesOptions) {
        this.showDependenciesOptions = showDependenciesOptions;
    }

    public boolean isShowDependenciesOption0() {
        return showDependenciesOption0;
    }

    public void setShowDependenciesOption0(boolean showDependenciesOption0) {
        this.showDependenciesOption0 = showDependenciesOption0;
    }

    public boolean isShowDependenciesOption1() {
        return showDependenciesOption1;
    }

    public void setShowDependenciesOption1(boolean showDependenciesOption1) {
        this.showDependenciesOption1 = showDependenciesOption1;
    }

    public boolean isShowDependenciesOption2() {
        return showDependenciesOption2;
    }

    public void setShowDependenciesOption2(boolean showDependenciesOption2) {
        this.showDependenciesOption2 = showDependenciesOption2;
    }

    public boolean isShowDependenciesOption3() {
        return showDependenciesOption3;
    }

    public void setShowDependenciesOption3(boolean showDependenciesOption3) {
        this.showDependenciesOption3 = showDependenciesOption3;
    }

    public DependenciesMode getDependenciesOptionDefault() {
        return dependenciesOptionDefault;
    }

    public void setDependenciesOptionDefault(DependenciesMode dependenciesOptionDefault) {
        this.dependenciesOptionDefault = dependenciesOptionDefault;
    }

    public boolean isShowBackButton() {
        return showBackButton;
    }

    public void setShowBackButton(boolean showBackButton) {
        this.showBackButton = showBackButton;
    }

    public boolean isShowCloseButton() {
        return showCloseButton;
    }

    public void setShowCloseButton(boolean showCloseButton) {
        this.showCloseButton = showCloseButton;
    }

    public String getSortExchanges() {
        return sortExchanges;
    }

    public void setSortExchanges(String sortExchanges) {
        this.sortExchanges = sortExchanges;
    }

    public boolean isShowManageProcessRegistrationNumber() {
        return showManageProcessRegistrationNumber;
    }

    public void setShowManageProcessRegistrationNumber(boolean showManageProcessRegistrationNumber) {
        this.showManageProcessRegistrationNumber = showManageProcessRegistrationNumber;
    }

    public boolean isShowManageProcessRegistrationAuthority() {
        return showManageProcessRegistrationAuthority;
    }

    public void setShowManageProcessRegistrationAuthority(boolean showManageProcessRegistrationAuthority) {
        this.showManageProcessRegistrationAuthority = showManageProcessRegistrationAuthority;
    }

    public boolean isShowManageProcessTags() {
        return showManageProcessTags;
    }

    public void setShowManageProcessTags(boolean showManageProcessTags) {
        this.showManageProcessTags = showManageProcessTags;
    }

    public boolean isShowProcessDetailExchangesMinMaxValues() {
        return showProcessDetailExchangesMinMaxValues;
    }

    public boolean isShowManageProcessCompliance() {
        return showManageProcessCompliance;
    }

}
