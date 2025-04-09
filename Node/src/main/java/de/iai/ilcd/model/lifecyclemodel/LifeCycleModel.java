package de.iai.ilcd.model.lifecyclemodel;

import de.fzk.iai.ilcd.api.app.lifecyclemodel.LifeCycleModelDataSet;
import de.fzk.iai.ilcd.api.binding.generated.common.ClassificationType;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.*;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.ILifeCycleModelVO;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.fzk.iai.ilcd.service.model.process.IReview;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.LifeCycleModelDao;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.process.ComplianceSystem;
import de.iai.ilcd.model.process.Review;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

import static de.iai.ilcd.model.lifecyclemodel.LifeCycleModel.TABLE_NAME;

/**
 * @author MK
 * @since soda4LCA 5.7.0
 */

@Entity
@Table(name = TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = {"UUID", "MAJORVERSION",
        "MINORVERSION", "SUBMINORVERSION"}))
@AssociationOverrides({
        @AssociationOverride(name = "classifications", joinTable = @JoinTable(name = "lifecyclemodel_classifications"), joinColumns = @JoinColumn(name = "lifecyclemodel_id")),
        @AssociationOverride(name = "description", joinTable = @JoinTable(name = "lifecyclemodel_description"), joinColumns = @JoinColumn(name = "lifecyclemodel_id")),
        @AssociationOverride(name = "name", joinTable = @JoinTable(name = "lifecyclemodel_name"), joinColumns = @JoinColumn(name = "lifecyclemodel_id"))})
public class LifeCycleModel extends DataSet implements Serializable, ILifeCycleModelVO {

    private static final Logger LOGGER = LogManager.getLogger(LifeCycleModel.class);

    public static final String TABLE_NAME = "lifecyclemodel";

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 4574709727907073272L;
    @ManyToMany(mappedBy = "lifeCycleModels", fetch = FetchType.LAZY)
    protected Set<DataStock> containingDataStocks = new HashSet<DataStock>();
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lifecyclemodel_datasetinformation_basename", joinColumns = @JoinColumn(name = "lifecyclemodel_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> baseName = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter basePartAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return LifeCycleModel.this.baseName;
        }
    });

    // === DataSetInformation ===//
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lifecyclemodel_datasetinformation_treatmentstandardsroutes", joinColumns = @JoinColumn(name = "lifecyclemodel_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> treatmentStandardsRoutes = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter treatmentStandardsRoutesAdapter = new MultiLangStringMapAdapter(
            new IStringMapProvider() {

                @Override
                public Map<String, String> getMap() {
                    return LifeCycleModel.this.treatmentStandardsRoutes;
                }
            });
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lifecyclemodel_datasetinformation_mixandlocationTypes", joinColumns = @JoinColumn(name = "lifecyclemodel_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> mixAndLocationTypes = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter mixAndLocationTypesAdapter = new MultiLangStringMapAdapter(
            new IStringMapProvider() {

                @Override
                public Map<String, String> getMap() {
                    return LifeCycleModel.this.mixAndLocationTypes;
                }
            });
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lifecyclemodel_datasetinformation_functionalunitflowproperties", joinColumns = @JoinColumn(name = "lifecyclemodel_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> functionalUnitFlowProperties = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter functionalUnitFlowPropertiesAdapter = new MultiLangStringMapAdapter(
            new IStringMapProvider() {

                @Override
                public Map<String, String> getMap() {
                    return LifeCycleModel.this.functionalUnitFlowProperties;
                }
            });
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lifecyclemodel_referencetoresultingprocess", joinColumns = @JoinColumn(name = "lifecyclemodel_id"), inverseJoinColumns = @JoinColumn(name = "globalreferenceid"))
    protected List<GlobalReference> referenceToResultingProcess = new ArrayList<GlobalReference>();
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lifecyclemodel_datasetinformation_generalcomment", joinColumns = @JoinColumn(name = "lifecyclemodel_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> generalComment = new HashMap<String, String>();

    // classification information has been inherited
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lifecyclemodel_referencetoexternaldocmentation", joinColumns = @JoinColumn(name = "lifecyclemodel_id"), inverseJoinColumns = @JoinColumn(name = "globalreferenceid"))
    protected List<GlobalReference> referenceToExternalDocumentation = new ArrayList<GlobalReference>();
    @Column(name = "ref_ref_process", nullable = true)
    protected long referenceToReferenceProcess;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lifecyclemodel_technology_groups", joinColumns = @JoinColumn(name = "lifecyclemodel_id"), inverseJoinColumns = @JoinColumn(name = "techgroup_key"))
    @OrderBy("internalgroup_id ASC")
    protected List<TechnologyGroup> groups = new ArrayList<TechnologyGroup>();

    // === QuantitativeReference ===///

    //	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//	@JoinColumn(name = "referenceToProcess")
//	protected InternalQuantitativeReference quantitativeReference;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lifecyclemodel_technology_processes", joinColumns = @JoinColumn(name = "lifecyclemodel_id"), inverseJoinColumns = @JoinColumn(name = "processinstance_key"))
    @OrderBy("dataSetInternalID ASC")
    protected List<ProcessInstance> processes = new ArrayList<ProcessInstance>();

    // === Technology ===//
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lifecyclemodel_diagram", joinColumns = @JoinColumn(name = "lifecyclemodel_id"), inverseJoinColumns = @JoinColumn(name = "globalreferenceid"))
    protected List<GlobalReference> referenceToDiagram = new ArrayList<GlobalReference>();
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lifecyclemodel_dataSourcestreatmentetc", joinColumns = @JoinColumn(name = "lifecyclemodel_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> useAdviceForDataSet = new HashMap<String, String>();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, targetEntity = Review.class)
    @JoinTable(name = "lifecyclemodel_reviews", joinColumns = @JoinColumn(name = "lifecyclemodel_id"), inverseJoinColumns = @JoinColumn(name = "review_id"))
    protected List<IReview> reviews = new ArrayList<IReview>();

    // === modellingAndValidation ===//
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, targetEntity = ComplianceSystem.class)
    protected Set<IComplianceSystem> complianceSystems = new TreeSet<IComplianceSystem>();
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lifecyclemodel_administrativeinformation", joinColumns = @JoinColumn(name = "lifecyclemodel_id"), inverseJoinColumns = @JoinColumn(name = "administrativeinformation_id"))
    protected AdministrativeInformation administrativeInformation;

    public LifeCycleModel() {
    }

    // === administrativeInformation == //

    /**
     * Constructs a LifeCycleModel entity (compatible with SQL) from a
     * LifeCycleModelDataSet object (compatible with XML)
     *
     * @param lcmds
     */

    public LifeCycleModel(LifeCycleModelDataSet lcmds) {

        this.uuid = Optional.ofNullable(lcmds).map(LifeCycleModelDataSet::getLifeCycleModelInformation)
                .map(LifeCycleModelInformationType::getDataSetInformation).map(DataSetInformationType::getUUID)
                .map(uuid -> new Uuid(uuid)).orElse(null);

        this.version = Optional.ofNullable(lcmds)
                .map(LifeCycleModelDataSet::getAdministrativeInformation)
                .map(AdministrativeInformationType::getPublicationAndOwnership)
                .map(PublicationAndOwnershipType::getDataSetVersion)
                .map(v -> {
                    try {
                        return DataSetVersion.parse(v);
                    } catch (FormatException e) {
                        return null;
                    }
                }).orElse(null);

        lcmds.safeGetBaseName().forEach((k, v) -> this.addBaseName(k, v));
        lcmds.safeGetTreatmentStandardsRoutes().forEach((k, v) -> this.addTreatmentStandardsRoutes(k, v));
        lcmds.safeGetMixAndLocationTypes().forEach((k, v) -> this.addMixAndLocationTypes(k, v));
        lcmds.safeGetFunctionalUnitFlowProperties().forEach((k, v) -> this.addFunctionalUnitFlowProperties(k, v));

        for (GlobalReferenceType grt : lcmds.safeGetReferenceToResultingProcess())
            this.getReferenceToResultingProcess().add(new GlobalReference(grt));

        this.getGeneralComment().putAll(lcmds.safeGetGeneralComment());

        for (GlobalReferenceType grt : lcmds.safeGetReferenceToExternalDocumentation())
            this.getReferenceToExternalDocumentation().add(new GlobalReference(grt));

        // ReferenceToReferenceProcess defaults to -1 if null
        this.referenceToReferenceProcess = Optional.ofNullable(lcmds)
                .map(LifeCycleModelDataSet::getLifeCycleModelInformation)
                .map(LifeCycleModelInformationType::getQuantitativeReference)
                .map(QuantitativeReferenceType::getReferenceToReferenceProcess).map(BigInteger::longValue).orElse(-1L);

        for (GroupType g : lcmds.safeGetGroups())
            this.getGroups().add(new TechnologyGroup(g));

        for (ProcessInstanceType p : lcmds.safeGetProcessInstance())
            this.getProcesses().add(new ProcessInstance(p));

        for (GlobalReferenceType grt : lcmds.safeGetReferenceToDiagram())
            this.getReferenceToDiagram().add(new GlobalReference(grt));

        this.getUseAdviceForDataSet().putAll(lcmds.safeGetUseAdviceForDataSet());

        for (ReviewType r : lcmds.safeGetReviews())
            this.getReviews().add(new Review(r));

        for (ComplianceDeclarationsType c : lcmds.safeGetComplianceDeclarations())
            this.addComplianceSystems(new ComplianceSystem(c));

        this.administrativeInformation = Optional.ofNullable(lcmds)
                .map(LifeCycleModelDataSet::getAdministrativeInformation).map(a -> new AdministrativeInformation(a))
                .orElse(null);

        for (ClassificationType c : lcmds.safeGetClassificationInformation())
            this.addClassification(new Classification(c));

        // set DataSetType for each ClClass
        for (ClClass c : Optional.ofNullable(this.getClassification()).map(Classification::getClasses)
                .orElse(new ArrayList<ClClass>()))
            c.setDataSetType(DataSetType.LIFECYCLEMODEL);

        this.setName(new MultiLangStringMapAdapter(lcmds.safeGetBaseName()));
        this.setPermanentUri(this.getAdministrativeInformation().getPublicationAndOwnership().getPermanentUri());

    }

    /**
     * Takes care of most scenarios for retrieving values from multilang maps
     *
     * @param map containing lang, value pair
     * @param key is the target lang you want the corresponding value for.
     * @return <ul>
     * <li>Get value for <b>requested key</b>, if not found...</li>
     * <li>Get value for <b>preferred language</b> from configuration, if
     * not found...</li>
     * <li>Get value for <b>the first key</b> in the map, if map is
     * empty...</li>
     * <li>Returns an empty string</li>
     * </ul>
     */

    public static String SafeLangMapValue(Map<String, String> map, String key) {
        String erd = map.getOrDefault(key, null);
        if (erd == null)
            erd = map.getOrDefault(ConfigurationService.INSTANCE.getPreferredLanguages(), null);
        if (erd == null)
            erd = map.values().stream().findFirst().orElse("");
        return erd;
    }

    public IMultiLangString getBaseName() {
        return basePartAdapter;
    }

    public void setBaseName(IMultiLangString basePart) {
        this.basePartAdapter.overrideValues(basePart);
    }

    public void addBaseName(String lang, String value) {
        this.baseName.put(lang, value);
    }

    public IMultiLangString getTreatmentStandardsRoutes() {
        return treatmentStandardsRoutesAdapter;
    }

    public void setTreatmentStandardsRoutes(IMultiLangString TreatmentStandardsPart) {
        this.treatmentStandardsRoutesAdapter.overrideValues(TreatmentStandardsPart);
    }

    public void addTreatmentStandardsRoutes(String lang, String value) {
        this.treatmentStandardsRoutes.put(lang, value);
    }

    public IMultiLangString getMixAndLocationTypes() {
        return mixAndLocationTypesAdapter;
    }

    public void setMixAndLocationTypes(IMultiLangString mixAndLocationPart) {
        this.mixAndLocationTypesAdapter.overrideValues(mixAndLocationPart);
    }

    public void addMixAndLocationTypes(String lang, String value) {
        this.mixAndLocationTypes.put(lang, value);
    }

    public IMultiLangString getFunctionalUnitFlowProperties() {
        return functionalUnitFlowPropertiesAdapter;
    }

    public void setFunctionalUnitFlowProperties(IMultiLangString functionalUnitFlowPart) {
        this.functionalUnitFlowPropertiesAdapter.overrideValues(functionalUnitFlowPart);
    }

    public void addFunctionalUnitFlowProperties(String lang, String value) {
        this.functionalUnitFlowProperties.put(lang, value);
    }

    public List<IGlobalReference> getReferenceToResultingProcess() {
        List<IGlobalReference> erg = new ArrayList<IGlobalReference>();
        if (referenceToResultingProcess != null)
            erg.addAll(referenceToResultingProcess);
        return erg;
    }

    public void addReferenceToResultingProcess(GlobalReference referenceToResultingProcess) {
        this.referenceToResultingProcess.add(referenceToResultingProcess);
    }

    public Map<String, String> getGeneralComment() {
        return generalComment;
    }

    public void addGeneralComment(String lang, String value) {
        this.generalComment.put(lang, value);
    }

    public List<IGlobalReference> getReferenceToExternalDocumentation() {
        List<IGlobalReference> erg = new ArrayList<IGlobalReference>();
        if (referenceToExternalDocumentation != null)
            erg.addAll(referenceToExternalDocumentation);
        return erg;
    }

    public void addReferenceToExternalDocumentation(GlobalReference referenceToExternalDocumentation) {
        this.referenceToExternalDocumentation.add(referenceToExternalDocumentation);
    }

    public long getReferenceToReferenceProcess() {
        return referenceToReferenceProcess;
    }

    public void setReferenceToReferenceProcess(long referenceToReferenceProcess) {
        this.referenceToReferenceProcess = referenceToReferenceProcess;
    }

    public List<TechnologyGroup> getGroups() {
        return groups;
    }

    public void addGroup(TechnologyGroup group) {
        this.groups.add(group);
    }

    public List<ProcessInstance> getProcesses() {
        return processes;
    }

    public void addProcesses(ProcessInstance processes) {
        this.processes.add(processes);
    }

    public List<IGlobalReference> getReferenceToDiagram() {
        List<IGlobalReference> erg = new ArrayList<IGlobalReference>();
        if (referenceToDiagram != null)
            erg.addAll(referenceToDiagram);
        return erg;
    }

    public void addReferenceToDiagram(GlobalReference referenceToDiagram) {
        this.referenceToDiagram.add(referenceToDiagram);
    }

    public Map<String, String> getUseAdviceForDataSet() {
        return useAdviceForDataSet;
    }

    public void addUseAdviceForDataSet(String lang, String value) {
        this.useAdviceForDataSet.put(lang, value);
    }

    public List<IReview> getReviews() {
        return reviews;
    }

    public void addReviews(Review review) {
        this.reviews.add(review);
    }

    public Set<IComplianceSystem> getComplianceSystems() {
        return complianceSystems;
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

    public void addComplianceSystems(ComplianceSystem complianceSystem) {
        this.complianceSystems.add(complianceSystem);
    }

    public AdministrativeInformation getAdministrativeInformation() {
        return administrativeInformation;
    }

    public void setAdministrativeInformation(AdministrativeInformation administrativeInformation) {
        this.administrativeInformation = administrativeInformation;
    }

    public ProcessInstance getProcessInstanceByID(long id) {
        for (ProcessInstance pi : this.getProcesses())
            if (pi.getDataSetInternalID() == id)
                return pi;
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(administrativeInformation, baseName, complianceSystems,
                containingDataStocks, functionalUnitFlowProperties, generalComment, groups, mixAndLocationTypes,
                processes, referenceToDiagram, referenceToExternalDocumentation, referenceToReferenceProcess,
                referenceToResultingProcess, reviews, treatmentStandardsRoutes, useAdviceForDataSet);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LifeCycleModel other = (LifeCycleModel) obj;
        return Objects.equals(administrativeInformation, other.administrativeInformation)
                && Objects.equals(baseName, other.baseName)
                && Objects.equals(complianceSystems, other.complianceSystems)
                && Objects.equals(containingDataStocks, other.containingDataStocks)
                && Objects.equals(functionalUnitFlowProperties, other.functionalUnitFlowProperties)
                && Objects.equals(generalComment, other.generalComment) && Objects.equals(groups, other.groups)
                && Objects.equals(mixAndLocationTypes, other.mixAndLocationTypes)
                && Objects.equals(processes, other.processes)
                && Objects.equals(referenceToDiagram, other.referenceToDiagram)
                && Objects.equals(referenceToExternalDocumentation, other.referenceToExternalDocumentation)
                && referenceToReferenceProcess == other.referenceToReferenceProcess
                && Objects.equals(referenceToResultingProcess, other.referenceToResultingProcess)
                && Objects.equals(reviews, other.reviews)
                && Objects.equals(treatmentStandardsRoutes, other.treatmentStandardsRoutes)
                && Objects.equals(useAdviceForDataSet, other.useAdviceForDataSet);
    }

    @Override
    public Set<DataStock> getContainingDataStocks() {
        return this.containingDataStocks;
    }

    @Override
    protected void addSelfToDataStock(DataStock stock) {
        stock.addLifeCycleModel(this);
    }

    @Override
    protected void removeSelfFromDataStock(DataStock stock) {
        stock.removeLifeCycleModel(this);
    }

    @Override
    public DataSetType getDataSetType() {
        return DataSetType.LIFECYCLEMODEL;
    }

    @Override
    public String getDirPathInZip() {
        return "ILCD/" + DatasetTypes.LIFECYCLEMODELS.getValue();
    }

    @Override
    public DataSetDao<?, ?, ?> getCorrespondingDSDao() {
        return new LifeCycleModelDao();
    }

    @Override
    public List<IGlobalReference> getReferenceToProcess() {

        List<IGlobalReference> erg = new ArrayList<IGlobalReference>();

        Optional.ofNullable(this)
                .map(LifeCycleModel::getProcesses)
                .orElse(new ArrayList<ProcessInstance>())
                .forEach(p -> erg.add(p.getReferenceToProcess()));

        return erg;
    }

    @Override
    public List<IGlobalReference> getReferenceToOwnershipOfDataSet() {

        List<IGlobalReference> erg = new ArrayList<IGlobalReference>();

        Optional.ofNullable(this)
                .map(LifeCycleModel::getAdministrativeInformation)
                .map(AdministrativeInformation::getPublicationAndOwnership)
                .map(PublicationAndOwnership::getReferenceToOwnershipOfDataSet)
                .map(l -> erg.add(l))
                .orElse(null);
        return erg;
    }

}
