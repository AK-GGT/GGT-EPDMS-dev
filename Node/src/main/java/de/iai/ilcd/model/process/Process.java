package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.client.impl.vo.epd.ProcessSubType;
import de.fzk.iai.ilcd.service.model.IProcessVO;
import de.fzk.iai.ilcd.service.model.common.IClassification;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.CompletenessValue;
import de.fzk.iai.ilcd.service.model.enums.TypeOfProcessValue;
import de.fzk.iai.ilcd.service.model.enums.TypeOfQuantitativeReferenceValue;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.fzk.iai.ilcd.service.model.process.IQuantitativeReference;
import de.fzk.iai.ilcd.service.model.process.IReferenceFlow;
import de.fzk.iai.ilcd.service.model.process.IReview;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.flow.FlowPropertyDescription;
import de.iai.ilcd.model.process.contentdeclaration.ContentDeclaration;
import de.iai.ilcd.model.tag.Tag;
import de.iai.ilcd.util.SodaUtil;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import de.iai.ilcd.util.sort.CarbogenicsComparator;
import de.iai.ilcd.util.sort.ModuleOrderComparator;
import de.iai.ilcd.webgui.controller.ui.ComplianceUtilHandler;
import de.iai.ilcd.webgui.controller.ui.ComplianceUtilHandler.ComplianceSystemCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.persistence.*;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static de.iai.ilcd.model.process.Process.TABLE_NAME;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = {"UUID", "MAJORVERSION", "MINORVERSION", "SUBMINORVERSION"}))
@AssociationOverrides({
        @AssociationOverride(name = "classifications", joinTable = @JoinTable(name = "process_classifications"), joinColumns = @JoinColumn(name = "process_id")),
        @AssociationOverride(name = "description", joinTable = @JoinTable(name = "process_description"), joinColumns = @JoinColumn(name = "process_id")),
        @AssociationOverride(name = "name", joinTable = @JoinTable(name = "process_name"), joinColumns = @JoinColumn(name = "process_id"))})
public class Process extends DataSet implements Serializable, IProcessVO {

    public static final String TABLE_NAME = "process";

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 8332962245687030577L;

    /**
     * Comparator for {@link #getDeclaredModulesScenariosForExchanges()} to detect duplicate entries in String arrays
     */
    private final static Comparator<String[]> DECLARED_MODULES_SCENARIOS_FOR_EXCHANGES_COMPARATOR = new Comparator<String[]>() {

        @Override
        public int compare(String[] o1, String[] o2) {
            return ArrayUtils.isEquals(o1, o2) ? 0 : 1;
        }

    };

    private final static Comparator<String[]> STRING_ARRAY_SORT_COMPARATOR = new Comparator<String[]>() {

        @Override
        public int compare(String[] o1, String[] o2) {
            if (o1.length == 0) {
                return o2.length == 0 ? 0 : -1;
            }
            if (o2.length == 0) {
                return 1;
            }
            if (o1[0].equals(o2[0]) && (o1.length > 1) && (o2.length > 1) && (o1[1] != null) && (o2[1] != null))
                return o1[1].compareTo(o2[1]);
            else
                return o1[0].compareTo(o2[0]);
        }
    };

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "processname_base", joinColumns = @JoinColumn(name = "process_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> baseName = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter basePartAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Process.this.baseName;
        }
    });

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "processname_route", joinColumns = @JoinColumn(name = "process_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> nameRoute = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter routePartAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Process.this.nameRoute;
        }
    });

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "processname_location", joinColumns = @JoinColumn(name = "process_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> nameLocation = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter locationPartAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Process.this.nameLocation;
        }
    });

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "processname_unit", joinColumns = @JoinColumn(name = "process_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> nameUnit = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter unitPartAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Process.this.nameUnit;
        }
    });

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "process_synonyms", joinColumns = @JoinColumn(name = "process_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> synonyms = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter synonymsAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Process.this.synonyms;
        }
    });

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected List<Scenario> scenarios = new ArrayList<Scenario>();

    @Enumerated(EnumType.STRING)
    protected TypeOfProcessValue type;

    protected boolean parameterized;

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "process_useadvice", joinColumns = @JoinColumn(name = "process_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> useAdvice = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter useAdviceAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Process.this.useAdvice;
        }
    });

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "process_technicalpurpose", joinColumns = @JoinColumn(name = "process_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> technicalPurpose = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter technicalPurposeAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Process.this.technicalPurpose;
        }
    });

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "process_technologydescription", joinColumns = @JoinColumn(name = "process_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> technologyDescription = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter technologyDescriptionAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Process.this.technologyDescription;
        }
    });

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected TimeInformation timeInformation;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected Geography geography;

    protected String format;

    @ManyToOne(cascade = CascadeType.ALL)
    protected GlobalReference ownerReference;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "process_preceding_versions",
            joinColumns = @JoinColumn(name = "process_ID"),
            inverseJoinColumns = @JoinColumn(name = "reference_ID"))
    protected Set<GlobalReference> precedingDataSetVersions;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    protected LCIMethodInformation lCIMethodInformation;

    @Enumerated(EnumType.STRING)
    protected CompletenessValue completeness;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = Review.class)
    protected List<IReview> reviews = new ArrayList<IReview>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected AccessInformation accessInformation;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, targetEntity = ComplianceSystem.class)
    protected Set<IComplianceSystem> complianceSystems = new TreeSet<IComplianceSystem>();

    @ManyToOne(cascade = CascadeType.ALL)
    protected GlobalReference approvedBy;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("internalId")
    protected List<Exchange> exchanges = new ArrayList<Exchange>();
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    protected InternalQuantitativeReference internalReference;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected List<LciaResult> lciaResults = new ArrayList<LciaResult>();
    protected boolean resultsIncluded;
    protected boolean exchangesIncluded;
    /**
     * The data stocks this process is contained in
     */
    @ManyToMany(mappedBy = "processes", fetch = FetchType.LAZY)
    protected Set<DataStock> containingDataStocks = new HashSet<DataStock>();
    /**
     * The tags this process is tagged with
     */
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "process_tag",
            joinColumns = @JoinColumn(name = "process_ID"),
            inverseJoinColumns = @JoinColumn(name = "tag_ID"))
    protected Set<Tag> tags = new HashSet<Tag>();
    @Transient
    boolean accessRestricted = false;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "process_datasource",
            joinColumns = @JoinColumn(name = "process_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "datasource_ID", referencedColumnName = "ID"))
    List<GlobalReference> dataSources = new ArrayList<GlobalReference>();
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "process_datasetgenerator",
            joinColumns = @JoinColumn(name = "process_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "contact_ID", referencedColumnName = "ID"))
    Set<GlobalReference> datasetGenerator = new HashSet<GlobalReference>();
    /**
     * Exchanges excluding reference flow(s)
     * EPD indicators for life cycle
     */
    @Transient
    private List<Exchange> exchangesExcludingRefFlows = null;
    @Transient
    private List<LciaResult> lciaResultsCache = null;
    @Embedded
    private SafetyMargins safetyMargins = new SafetyMargins();
    /**
     * Flag to indicate if product model is contained
     */
    @Basic
    @Column(name = "containsProductModel")
    private boolean containsProductModel;
    @Temporal(TemporalType.DATE)
    private Date publicationDateOfEPD = null;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "registration_authority_reference_ID")
    private GlobalReference referenceToRegistrationAuthority = null;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "process_reference_to_original_epd",
            joinColumns = @JoinColumn(name = "process_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "globalreference_ID", referencedColumnName = "ID"))
    private Set<GlobalReference> referenceToOriginalEPD = new HashSet<GlobalReference>();
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "process_reference_to_publisher",
            joinColumns = @JoinColumn(name = "process_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "globalreference_ID", referencedColumnName = "ID"))
    private Set<GlobalReference> referenceToPublisher = new HashSet<GlobalReference>();
    private String registrationNumber = null;
    /**
     * Version of the epd format extension
     */
    private String epdFormatVersion = null;
    /**
     * Cache for the LCI method information - used for filtering &amp; order in queries.
     * 20 character limit should be sufficient.
     */
    // only for query efficiency
    @Basic
    @Column(name = "lciMethodInformation_cache", length = 20)
    private String lciMethodInformationCache;
    /**
     * sub type of process
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "subtype")
    private ProcessSubType subType;
    /**
     * Cache for the LCI method information - used for filtering &amp; order in queries.
     * 1 character limit is sufficient (due to {@link ComplianceSystemCode#toString()}).
     * Used for JSF display as well, because no truncation is being risked.
     */
    @Basic
    @Column(name = "complianceSystem_cache", length = 1)
    private String complianceSystemCache;
    @Transient
    private ContentDeclaration contentDeclaration;
    private Boolean metaDataOnly;
    @Transient
    private List<Exchange> inputs = null;
    @Transient
    private List<Exchange> outputs = null;

    public Process() {
    }

    // TODO this is VERY ugly
    public Process(IProcessVO voProcess) {
        this.setUuid(new Uuid(voProcess.getUuidAsString()));
        this.setPermanentUri(voProcess.getPermanentUri());
        this.setHref(voProcess.getHref());
        String[] nameParts = voProcess.getDefaultName().split(";");
        if (nameParts.length > 0) {
            this.setBaseName(new MultiLangStringMapAdapter(nameParts[0]));
        }
        if (nameParts.length > 1) {
            this.setNameRoute(new MultiLangStringMapAdapter(nameParts[1]));
        }
        if (nameParts.length > 2) {
            this.setNameLocation(new MultiLangStringMapAdapter(nameParts[2]));
        }
        if (nameParts.length > 3) {
            this.setNameUnit(new MultiLangStringMapAdapter(nameParts[3]));
        }
        if (voProcess.getSynonyms() != null) {
            this.setSynonyms(voProcess.getSynonyms());
        }
        List<? extends IClassification> classifications = voProcess.getClassifications();
        if (CollectionUtils.isNotEmpty(classifications)) {
            for (IClassification entry : classifications) {
                this.addClassification(new Classification(entry));
            }
        }
        if (voProcess.getType() != null) {
            this.setType(voProcess.getType());
        }
        this.setParameterized(voProcess.getParameterized());
        if (voProcess.getDescription() != null) {
            this.setDescription(voProcess.getDescription());
        }
        if (voProcess.getUseAdvice() != null) {
            this.setUseAdvice(voProcess.getUseAdvice());
        }
        if (voProcess.getLocation() != null) {
            Geography poGeography = new Geography();
            poGeography.setLocation(voProcess.getLocation());
            this.setGeography(poGeography);
        }
        TimeInformation poTimeInformation = new TimeInformation();
        if (voProcess.getTimeInformation() != null && voProcess.getTimeInformation().getReferenceYear() != null) {
            poTimeInformation.setReferenceYear(voProcess.getTimeInformation().getReferenceYear());
        }
        if (voProcess.getTimeInformation() != null && voProcess.getTimeInformation().getValidUntil() != null) {
            poTimeInformation.setValidUntil(voProcess.getTimeInformation().getValidUntil());
        }
        this.setTimeInformation(poTimeInformation);
        if (voProcess.getDataSetVersion() != null) {
            DataSetVersion dataSetVersion = new DataSetVersion();
            try {
                dataSetVersion = DataSetVersion.parse(voProcess.getDataSetVersion());
            } catch (FormatException ex) {
                // ignore; this should not happen
            }
            this.setVersion(dataSetVersion);
        }
        if (voProcess.getReviews() != null && voProcess.getReviews().size() > 0) {
            this.setReviews(voProcess.getReviews());
            // this.setFormat("ILCD format");
        }
    }

    /**
     * Apply cache fields for process, those are cached values for:
     * <ul>
     * <li>{@link #getLCIMethodInformation()}</li>
     * </ul>
     */
    @Override
    @PrePersist
    protected void applyDataSetCache() {
        super.applyDataSetCache();
        if (this.lCIMethodInformation != null && this.lCIMethodInformation.getMethodPrinciple() != null) {
            this.lciMethodInformationCache = StringUtils.substring(this.lCIMethodInformation.getMethodPrinciple().value(), 0, 20);
        } else {
            this.lciMethodInformationCache = null;
        }
        // TODO: use dependency injection later
        ComplianceUtilHandler compHandler = new ComplianceUtilHandler();
        this.complianceSystemCache = StringUtils.substring(compHandler.getComplianceCodeAsString(this.complianceSystems), 0, 1);
    }

    /**
     * Get process name for the cache instead of {@link DataSet#getName()}.
     *
     * @return the {@link #getProcessName() process name}
     */
    @Override
    protected String getNameAsStringForCache() {
        return this.getName().getValue();
    }

    /**
     * Get the cached value for the highest compliance system
     *
     * @return cached value for the highest compliance system
     */
    public String getComplianceSystemCache() {
        return this.complianceSystemCache;
    }

    @Override
    public IMultiLangString getBaseName() {
        return this.basePartAdapter;
    }

    public void setBaseName(IMultiLangString basePart) {
        this.basePartAdapter.overrideValues(basePart);
    }

    public IMultiLangString getNameLocation() {
        return this.locationPartAdapter;
    }

    public void setNameLocation(IMultiLangString locationPart) {
        this.locationPartAdapter.overrideValues(locationPart);
    }

    public IMultiLangString getNameRoute() {
        return this.routePartAdapter;
    }

    public void setNameRoute(IMultiLangString routePart) {
        this.routePartAdapter.overrideValues(routePart);
    }

    public IMultiLangString getNameUnit() {
        return this.unitPartAdapter;
    }

    public void setNameUnit(IMultiLangString unitPart) {
        this.unitPartAdapter.overrideValues(unitPart);
    }

    public IMultiLangString getFullName() {

        Map<String, String> tmp = new HashMap<String, String>();
        for (ILString lString : this.basePartAdapter.getLStrings()) {
            String language = lString.getLang();
            if (language == null) {
                continue;
            }
            String baseValue = lString.getValue();
            StringBuilder joinedValue = new StringBuilder();

            if (StringUtils.isNotBlank(baseValue)) {
                joinedValue.append(baseValue);
            }
            if (StringUtils.isNotBlank(this.routePartAdapter.getValue(language))) {
                joinedValue.append("; ").append(this.routePartAdapter.getValue(language));
            }
            if (StringUtils.isNotBlank(this.locationPartAdapter.getValue(language))) {
                joinedValue.append("; ").append(this.locationPartAdapter.getValue(language));
            }
            if (StringUtils.isNotBlank(this.unitPartAdapter.getValue(language))) {
                joinedValue.append("; ").append(this.unitPartAdapter.getValue(language));
            }
            tmp.put(language, joinedValue.toString());
        }

        return new MultiLangStringMapAdapter(tmp);
    }

    @Override
    public AccessInformation getAccessInformation() {
        return this.accessInformation;
    }

    public void setAccessInformation(AccessInformation accessInformation) {
        this.accessInformation = accessInformation;
    }

    public CompletenessValue getCompleteness() {
        return this.completeness;
    }

    public void setCompleteness(CompletenessValue completeness) {
        this.completeness = completeness;
    }

    @Override
    public CompletenessValue getCompletenessProductModel() {
        return this.completeness;
    }

    public List<Exchange> getExchanges() {
        return this.exchanges;
    }

    protected void setExchanges(List<Exchange> exchanges) {
        this.exchanges = exchanges;
    }

    /**
     * Get the exchanges filtered by direction (Input/Output)
     *
     * @param direction the exchange direction (Input/Output)
     * @return exchanges filtered by direction
     */
    public List<Exchange> getExchanges(String direction) {
        if (StringUtils.isNotBlank(direction)) {
            List<Exchange> filtered = new ArrayList<Exchange>();
            for (Exchange exchange : this.exchanges) {
                if (String.valueOf(exchange.getExchangeDirection()).equals(direction)) {
                    filtered.add(exchange);
                }
            }
            return filtered;
        }
        return this.exchanges;
    }

    public List<Exchange> getInputs() {
        if (this.inputs == null)
            this.inputs = getExchanges("INPUT");
        return this.inputs;
    }

    public List<Exchange> getOutputs() {
        if (this.outputs == null)
            this.outputs = getExchanges("OUTPUT");
        return this.outputs;
    }

    /**
     * Get the exchanges, excluding reference flow(s)
     *
     * @return the exchanges, excluding reference flow(s)
     */
    @SuppressWarnings("unchecked")
    public List<Exchange> getExchangesExcludingRefFlows() {
        if (this.exchangesExcludingRefFlows == null) {
            this.exchangesExcludingRefFlows = ListUtils.removeAll(this.exchanges, this.getReferenceExchanges());
        }
        return this.exchangesExcludingRefFlows;
    }

    /**
     * Get the exchanges filtered by indicator
     *
     * @param indicator the indicator's UUID
     * @return exchanges filtered by indicator
     */
    public List<Exchange> getExchangesByIndicator(String indicatorUUID) {
        if (StringUtils.isNotBlank(indicatorUUID)) {
            List<Exchange> filtered = new ArrayList<Exchange>();
            for (Exchange exchange : this.exchanges) {
                if (exchange.getFlowReference() != null && exchange.getFlowReference().getUuid() != null
                        && exchange.getFlowReference().getUuid().getUuid().equals(indicatorUUID)) {
                    filtered.add(exchange);
                }
            }
            return filtered;
        }
        return this.exchanges;
    }

    /**
     * Get the exchanges filtered by indicator
     *
     * @param indicator the indicator's UUID
     * @return exchanges filtered by indicator
     */
    public List<LciaResult> getLciaResultsByIndicator(String indicatorUUID) {
        if (StringUtils.isNotBlank(indicatorUUID)) {
            List<LciaResult> filtered = new ArrayList<LciaResult>();
            for (LciaResult lciaResult : this.lciaResults) {
                if (lciaResult.getMethodReference() != null && lciaResult.getMethodReference().getUuid() != null
                        && lciaResult.getMethodReference().getUuid().getUuid().equals(indicatorUUID)) {
                    filtered.add(lciaResult);
                }
            }
            return filtered;
        }
        return this.lciaResults;
    }

    /**
     * Get a list of all modules declared for exchanges and LCIA results
     *
     * @return the list of modules
     */
    @SuppressWarnings("unchecked")
    public List<String> getDeclaredModules() {
        List<String> modules = new ArrayList<String>();
        for (Exchange exchange : this.exchanges) {
            for (Amount a : exchange.getAmounts()) {
                String currentModule = a.getModule();
                if (StringUtils.isNotBlank(currentModule) && !modules.contains(currentModule)) {
                    modules.add(currentModule);
                }
            }
        }
        for (LciaResult result : this.lciaResults) {
            for (Amount a : result.getAmounts()) {
                String currentModule = a.getModule();
                if (StringUtils.isNotBlank(currentModule) && !modules.contains(currentModule)) {
                    modules.add(currentModule);
                }
            }
        }

        Collections.sort(modules, new ModuleOrderComparator());
        return modules;
    }

    /**
     * Get a list of all modules declared for exchanges
     *
     * @return the list of modules for exchanges
     */
    public List<String> getDeclaredModulesForExchanges() {
        List<String> modules = new ArrayList<String>();
        for (Exchange exchange : this.exchanges) {
            for (Amount a : exchange.getAmounts()) {
                String currentModule = a.getModule();
                if (StringUtils.isNotBlank(currentModule) && !modules.contains(currentModule)) {
                    modules.add(currentModule);
                }
            }
        }
        return modules;
    }

    /**
     * Get a list of all modules and scenarios declared for exchanges
     *
     * @return the list of modules and scenarios for exchanges
     */
    public List<String[]> getDeclaredModulesScenariosForExchanges() {
        List<String[]> modulesScenarios = new ArrayList<String[]>();
        for (Exchange exchange : this.exchanges) {
            for (Amount a : exchange.getAmounts()) {
                String[] lineStr = new String[2];
                String currentModule = a.getModule();
                String currentScenario = a.getScenario(); // may be null
                lineStr[0] = currentModule;
                lineStr[1] = currentScenario;

                if (StringUtils.isNotBlank(currentModule) &&
                        !SodaUtil.contains(modulesScenarios, Process.DECLARED_MODULES_SCENARIOS_FOR_EXCHANGES_COMPARATOR, lineStr)) {
                    modulesScenarios.add(lineStr);
                }
            }
        }
        Collections.sort(modulesScenarios, STRING_ARRAY_SORT_COMPARATOR);
        return modulesScenarios;
    }

    /**
     * Get a list of all modules declared for LCIA results
     *
     * @return the list of modules for LCIA results
     */
    public List<String> getDeclaredModulesForLciaResults() {
        List<String> modules = new ArrayList<String>();
        for (LciaResult result : this.lciaResults) {
            for (Amount a : result.getAmounts()) {
                String currentModule = a.getModule();
                if (StringUtils.isNotBlank(currentModule) && !modules.contains(currentModule)) {
                    modules.add(currentModule);
                }
            }
        }
        return modules;
    }

    /**
     * Get a list of all modules and scenarios declared for LCIA results
     *
     * @return the list of modules and scenarios for LCIA results
     */
    public List<String[]> getDeclaredModulesScenariosForLciaResults() {
        List<String[]> modulesScenarios = new ArrayList<String[]>();
        for (LciaResult result : this.lciaResults) {
            for (Amount a : result.getAmounts()) {
                String[] lineStr = new String[2];
                String currentModule = a.getModule();
                String currentScenario = a.getScenario(); // may be null
                lineStr[0] = currentModule;
                lineStr[1] = currentScenario;

                if (StringUtils.isNotBlank(currentModule) &&
                        !SodaUtil.contains(modulesScenarios, Process.DECLARED_MODULES_SCENARIOS_FOR_EXCHANGES_COMPARATOR, lineStr)) {
                    modulesScenarios.add(lineStr);
                }
            }
        }
        Collections.sort(modulesScenarios, STRING_ARRAY_SORT_COMPARATOR);
        return modulesScenarios;
    }

    /**
     * Provides the two biogenic carbon content flow properties.
     */
    public List<FlowPropertyDescription> getCarbogenics() {
        List<FlowPropertyDescription> carbogenics = new ArrayList<FlowPropertyDescription>();
        List<Exchange> referenceExchanges = this.getReferenceExchanges();

        for (Exchange exchange : referenceExchanges) {
            if (exchange != null && exchange.getFlowWithSoftReference() != null && exchange.getFlowWithSoftReference().getPropertyDescriptions() != null) {
                for (FlowPropertyDescription propertyDescription : exchange.getFlowWithSoftReference().getPropertyDescriptions()) {
                    if (propertyDescription.isCarbonicProperty())
                        carbogenics.add(propertyDescription);
                }
            }
        }
        carbogenics.sort(new CarbogenicsComparator());
        return carbogenics;
    }

    public void addExchange(Exchange exchange) {
        this.exchanges.add(exchange);
    }

    @Override
    public Set<IComplianceSystem> getComplianceSystems() {
        return new TreeSet<IComplianceSystem>(this.complianceSystems);
    }

    protected void setComplianceSystems(Set<IComplianceSystem> compliances) {
        this.complianceSystems = compliances;
    }

    /**
     * Convenience method for returning compliance systems as List in order to user p:dataList (primefaces)
     *
     * @return List of compliance systems
     */
    public List<IComplianceSystem> getComplianceSystemsAsList() {
        List<IComplianceSystem> l = new ArrayList<IComplianceSystem>(this.getComplianceSystems());
        Collections.sort(l);
        return l;
    }

    public void addComplianceSystem(ComplianceSystem compliance) {
        this.complianceSystems.add(compliance);
    }

    @Override
    public GlobalReference getApprovedBy() {
        return this.approvedBy;
    }

    public void setApprovedBy(GlobalReference approvedByReference) {
        this.approvedBy = approvedByReference;
    }

    @Override
    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Geography getGeography() {
        return this.geography;
    }

    public void setGeography(Geography geography) {
        this.geography = geography;
    }

    @Override
    public String getLocation() {
        if (this.geography != null) {
            return this.geography.getLocation();
        } else {
            return null;
        }
    }

    /**
     * Only lists the tags that are set specifically for this process data set.
     * E.g. tags associated with this process data set as a member of certain
     * data stocks are not included. <br>
     * (Use <code>getAssociatedTagsList()</code>
     * if you're interested in all tags associated with this process data set.)
     *
     * @return set of process specific tags
     */
    public Set<Tag> getTags() {
        return this.tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    /**
     * (sorted) list of tags used in frontend, includes tags from containing datastock and
     * root datastock
     *
     * @return
     */
    public List<Tag> getAssociatedTagsList() {

        Set<Tag> allAssociatedTags = new HashSet<Tag>();

        // Collect process specific tags
        allAssociatedTags.addAll(this.tags);

        // Collect tags from containing data stocks
        this.containingDataStocks.stream().map(DataStock::getTags).forEach(allAssociatedTags::addAll);

        // Collect tags from root stock
        allAssociatedTags.addAll(this.getRootDataStock().getTags());

        // Sort by name and return as list. Sorting is null-safe on tag names.
        return allAssociatedTags.stream().sorted(Comparator.nullsLast(Comparator.comparing(Tag::getName))).collect(Collectors.toList());
    }

    public List<Tag> getVisibleTagsList() {
        return this.getAssociatedTagsList().stream().filter(Tag::isVisible).collect(Collectors.toList());
    }

    @Override
    public Boolean getParameterized() {
        return this.parameterized;
    }

    public void setParameterized(boolean parameterized) {
        this.parameterized = parameterized;
    }

    public boolean isResultsIncluded() {
        return this.resultsIncluded;
    }

    public void setResultsIncluded(boolean resultsIncluded) {
        this.resultsIncluded = resultsIncluded;
    }

    @Override
    public Boolean getHasResults() {
        return this.resultsIncluded;
    }

    public boolean isExchangesIncluded() {
        return this.exchangesIncluded;
    }

    public void setExchangesIncluded(boolean exchangesIncluded) {
        this.exchangesIncluded = exchangesIncluded;
    }

    @Override
    public LCIMethodInformation getLCIMethodInformation() {
        return this.lCIMethodInformation;
    }

    public void setLCIMethodInformation(LCIMethodInformation lciMethodInformation) {
        this.lCIMethodInformation = lciMethodInformation;
    }

    public List<LciaResult> getLciaResults() {
        return this.lciaResults;
    }

    protected void setLciaResults(List<LciaResult> lciaResults) {
        this.lciaResults = lciaResults;
    }

    @SuppressWarnings("unchecked")
    public List<LciaResult> getLciaResultsCopy() {
        if (this.lciaResultsCache == null) {
            this.lciaResultsCache = ListUtils.removeAll(this.lciaResults, new ArrayList<LciaResult>());
        }
        return this.lciaResultsCache;
    }

    public void addLciaResult(LciaResult result) {
        this.lciaResults.add(result);
    }

    @Override
    public GlobalReference getOwnerReference() {
        return this.ownerReference;
    }

    public void setOwnerReference(GlobalReference ownerReference) {
        this.ownerReference = ownerReference;
    }

    public Set<GlobalReference> getPrecedingDataSetVersions() {
        return precedingDataSetVersions;
    }

    public void setPrecedingDataSetVersions(Set<GlobalReference> precedingDataSetVersions) {
        this.precedingDataSetVersions = precedingDataSetVersions;
    }

    public void addPrecedingDataSetVersion(GlobalReference precedingDataSetVersion) {
        this.precedingDataSetVersions.add(precedingDataSetVersion);
    }

    @Override
    public IMultiLangString getName() {
        return this.getFullName();
    }

    public InternalQuantitativeReference getInternalReference() {
        return this.internalReference;
    }

    public void setInternalReference(InternalQuantitativeReference internalReference) {
        this.internalReference = internalReference;
    }

    @Override
    public IQuantitativeReference getQuantitativeReference() {
        return new QuantitativeReference(this.internalReference, this.exchanges);
    }

    public List<Exchange> getReferenceExchanges() {
        return getReferenceExchanges(null);
    }

    public List<Exchange> getReferenceExchanges(ExchangeDirectionValues direction) {
        List<Exchange> refExchanges = new ArrayList<Exchange>();

        if (this.internalReference == null || this.internalReference.referenceIds == null) {
            return refExchanges;
        }
        if (this.internalReference.referenceIds.size() <= 0) {
            return refExchanges;
        }

        ProcessDao dao = new ProcessDao();
        return dao.getReferenceExchanges(this, direction);
    }

    public IMultiLangString getOtherReference() {
        if (this.internalReference == null) {
            return null;
        }
        return this.internalReference.getOtherReference();
    }

    @Override
    public List<IReview> getReviews() {
        return this.reviews;
    }

    protected void setReviews(List<IReview> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        if (!this.reviews.contains(review)) {
            this.reviews.add(review);
        }
    }

    @Override
    public IMultiLangString getSynonyms() {
        return this.synonymsAdapter;
    }

    public void setSynonyms(IMultiLangString synonyms) {
        this.synonymsAdapter.overrideValues(synonyms);
    }

    @Override
    public TimeInformation getTimeInformation() {
        return this.timeInformation;
    }

    public void setTimeInformation(TimeInformation timeInformation) {
        this.timeInformation = timeInformation;
    }

    @Override
    public TypeOfProcessValue getType() {
        return this.type;
    }

    public void setType(TypeOfProcessValue type) {
        this.type = type;
    }

    @Override
    public IMultiLangString getUseAdvice() {
        return this.useAdviceAdapter;
    }

    public void setUseAdvice(IMultiLangString useAdvice) {
        this.useAdviceAdapter.overrideValues(useAdvice);
    }

    @Override
    public IMultiLangString getTechnicalPurpose() {
        return this.technicalPurposeAdapter;
    }

    public void setTechnicalPurpose(IMultiLangString technicalPurpose) {
        this.technicalPurposeAdapter.overrideValues(technicalPurpose);
    }

    @Override
    public IMultiLangString getTechnologyDescription() {
        return this.technologyDescriptionAdapter;
    }

    public void setTechnologyDescription(IMultiLangString technologyDescription) {
        this.technologyDescriptionAdapter.overrideValues(technologyDescription);
    }

    @Override
    public String getOverallQuality() {
        String overallQuality = null;

        return overallQuality;
    }

    @Override
    public boolean isAccessRestricted() {
        return this.accessRestricted;
    }

    @Override
    public void setAccessRestricted(boolean restricted) {
        this.accessRestricted = restricted;
    }

    @Override
    public Set<DataStock> getContainingDataStocks() {
        return this.containingDataStocks;
    }

    protected void setContainingDataStocks(Set<DataStock> containingDataStocks) {
        this.containingDataStocks = containingDataStocks;
    }

    /**
     * Get the sub type
     *
     * @return sub type
     */
    public ProcessSubType getSubType() {
        return this.subType;
    }

    /**
     * Set the sub type
     *
     * @param subType sub type to set
     */
    public void setSubType(ProcessSubType subType) {
        this.subType = subType;
    }

    /**
     * Get the safety margins
     *
     * @return safety margins
     */
    public SafetyMargins getSafetyMargins() {
        return this.safetyMargins;
    }

    /**
     * Set safety margins
     *
     * @param safetyMargins safety margins to set
     */
    public void setSafetyMargins(SafetyMargins safetyMargins) {
        this.safetyMargins = safetyMargins;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addSelfToDataStock(DataStock stock) {
        stock.addProcess(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeSelfFromDataStock(DataStock stock) {
        stock.removeProcess(this);
    }

    /**
     * Get the flag to indicate if product model is contained
     *
     * @return <code>true</code> if product model contained, <code>false</code> otherwise
     */
    public boolean isContainsProductModel() {
        return this.containsProductModel;
    }

    /**
     * Get the flag to indicate if product model is contained
     *
     * @return <code>{@link Boolean#TRUE}</code> if product model contained, <code>{@link Boolean#FALSE}</code>
     * otherwise
     */
    @Override
    public Boolean getContainsProductModel() {
        return this.containsProductModel ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Set the flag to indicate if product model is contained
     *
     * @param containsProductModel new value
     */
    public void setContainsProductModel(boolean containsProductModel) {
        this.containsProductModel = containsProductModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetType getDataSetType() {
        return DataSetType.PROCESS;
    }

    public List<Scenario> getScenarios() {
        return this.scenarios;
    }

    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public Scenario getScenario(String key) {
        for (Scenario s : getScenarios()) {
            if (s.getName().equals(key))
                return s;

        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<IGlobalReference> getDataSources() {
        return (List<IGlobalReference>) (List) this.dataSources;
    }

    public Set<GlobalReference> getDatasetGenerator() {
        return datasetGenerator;
    }

    public void setDatasetGenerator(Set<GlobalReference> datasetGenerator) {
        this.datasetGenerator = datasetGenerator;
    }

    public List<IGlobalReference> getDatasetGeneratorAsList() {
        List<IGlobalReference> ds = new ArrayList<IGlobalReference>();
        if (this.datasetGenerator != null)
            ds.addAll(this.datasetGenerator);
        return ds;
    }

    @Override
    public String getDirPathInZip() {
        return "ILCD/" + DatasetTypes.PROCESSES.getValue();
    }

    @Override
    public DataSetDao<?, ?, ?> getCorrespondingDSDao() {
        return new ProcessDao();
    }

    public Set<GlobalReference> getReferenceToOriginalEPD() {
        return referenceToOriginalEPD;
    }

    public void setReferenceToOriginalEPD(Set<GlobalReference> referenceToOriginalEPD) {
        this.referenceToOriginalEPD = referenceToOriginalEPD;
    }

    public Date getPublicationDateOfEPD() {
        return publicationDateOfEPD;
    }

    public void setPublicationDateOfEPD(Date publicationDateOfEPD) {
        this.publicationDateOfEPD = publicationDateOfEPD;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Set<GlobalReference> getReferenceToPublisher() {
        return referenceToPublisher;
    }

    public void setReferenceToPublisher(Set<GlobalReference> referenceToPublisher) {
        this.referenceToPublisher = referenceToPublisher;
    }

    public GlobalReference getReferenceToRegistrationAuthority() {
        return referenceToRegistrationAuthority;
    }

    public void setReferenceToRegistrationAuthority(GlobalReference referenceToRegistrationAuthority) {
        this.referenceToRegistrationAuthority = referenceToRegistrationAuthority;
    }

    public String getEpdFormatVersion() {
        return epdFormatVersion;
    }

    public void setEpdFormatVersion(String epdFormatVersion) {
        this.epdFormatVersion = epdFormatVersion;
    }

    @Override
    public IGlobalReference getRegistrationAuthority() {
        return this.referenceToRegistrationAuthority;
    }

    public ContentDeclaration getContentDeclaration() {
        return contentDeclaration;
    }

    public void setContentDeclaration(ContentDeclaration contentDeclaration) {
        this.contentDeclaration = contentDeclaration;
    }

    public Boolean getMetaDataOnly() {
        return metaDataOnly == null ? false : metaDataOnly;
    }

    public void setMetaDataOnly(Boolean metaDataOnly) {
        this.metaDataOnly = metaDataOnly;
    }


    public boolean addTag(Tag tag) {
        boolean added = tags.add(tag);
        if (added)
            tag.getProcesses().add(this);
        return added;
    }

    public void addAll(Collection<Tag> tags) {
        this.tags.addAll(tags);
        tags.forEach(t -> t.getProcesses().add(this));
    }

    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }

    // for being able to show the list of reference flows in the service API
    @Override
    public List<IReferenceFlow> getReferenceFlows() {
        ProcessDao dao = new ProcessDao();
        List<Exchange> refExchanges = dao.getReferenceExchanges(this);

        List<IReferenceFlow> refFlows = new ArrayList<>();
        refFlows.addAll(refExchanges);
        return refFlows;
    }

}

class QuantitativeReference implements Serializable, IQuantitativeReference {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    List<Exchange> exchanges;

    InternalQuantitativeReference internalReference;

    public QuantitativeReference(InternalQuantitativeReference internalReference, List<Exchange> exchanges) {
        this.exchanges = exchanges;
        this.internalReference = internalReference;
    }


    @Override
    public List<IReferenceFlow> getReferenceFlows() {
        List<IReferenceFlow> refExchanges = new ArrayList<IReferenceFlow>();

        if (this.internalReference == null || this.internalReference.referenceIds == null) {
            return refExchanges;
        }
        if (this.internalReference.referenceIds.size() <= 0) {
            return refExchanges;
        }
        for (Integer intObject : this.internalReference.referenceIds) {
            for (Exchange exchange : this.exchanges) {
                // because we cannot guarantee that internalIds are running from 0 and are continuously allocated, we
                // have to search for them
                if (exchange.getInternalId() == intObject.intValue()) {
                    refExchanges.add(exchange);
                    break;
                }
            }
        }
        return refExchanges;
    }

    @Override
    public IMultiLangString getFunctionalUnit() {
        if (this.internalReference != null) {
            return this.internalReference.getOtherReference();
        } else {
            return null;
        }
    }

    @Override
    public TypeOfQuantitativeReferenceValue getType() {
        if (this.internalReference != null) {
            return this.internalReference.getType();
        } else {
            return null;
        }
    }
}
