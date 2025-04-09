package de.iai.ilcd.service.glad;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.iai.ilcd.service.glad.model.*;

import java.util.List;

/**
 * The object representing the dataset metadata which are needed for correct GLAD registration.
 *
 * @author sarai
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GLADMetaData {

    public ReviewType reviewType;
    public ReviewSystem reviewSystem;
    public String contact;
    private String refId;
    private String name;
    private String category;
    private List<String> categories;
    private ProcessType processType;
    private RepresentativenessType representativenessType;
    private Double completeness;
    private Double amountDeviation;
    private Double representativenessValue;
    private Long validFrom;
    private Long validUntil;
    private Integer validFromYear;
    private Integer validUntilYear;
    private Double latitude;
    private Double longitude;
    private String technology;
    private AggregationType aggregationType;
    private String location;

    private String dataprovider;

    private List<String> supportedNomenclatures;

    private List<String> lciaMethods;

    private List<String> categoryPaths;

    private List<String> unspscPaths;

    private List<String> co2pePaths;

    private String unspscCode;

    private String co2peCode;

    private ModelingType modelingType;

    private MultifunctionalModeling multifunctionalModeling;

    private BiogenicCarbonModeling biogenicCarbonModeling;

    private EndOfLifeModeling endOfLifeModeling;

    private WaterModeling waterModeling;

    private InfrastructureModeling infrastructureModeling;

    private EmissionModeling emissionModeling;

    private CarbonStorageModeling carbonStorageModeling;

    private SourceReliability sourceReliability;

    private List<String> reviewers;

    private Boolean copyrightProtected;

    private String copyrightHolder;

    private LicenseType licenseType;

    private String license;

    private Boolean free;

    private Boolean publiclyAccessible;

    private String description;

    private String dataSetUrl;

    private DataFormat format;

    public GLADMetaData(String refId) {
        this.refId = refId;
        name = null;
        dataSetUrl = null;
        format = null;
    }

    public GLADMetaData() {
    }

    public String getRefId() {
        return this.refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public ProcessType getProcessType() {
        return processType;
    }

    public void setProcessType(ProcessType processType) {
        this.processType = processType;
    }

    public RepresentativenessType getRepresentativenessType() {
        return this.representativenessType;
    }

    public void setRepresentativenessType(RepresentativenessType representativenessType) {
        this.representativenessType = representativenessType;
    }

    public Double getCompleteness() {
        return completeness;
    }

    public void setCompleteness(Double completeness) {
        this.completeness = completeness;
    }

    public Double getAmountDeviation() {
        return this.amountDeviation;
    }

    public void setAmountDeviation(Double amountDeviation) {
        this.amountDeviation = amountDeviation;
    }

    public Double getRepresentativenessValue() {
        return this.representativenessValue;
    }

    public void setRepresentativenessValue(Double representativenessValue) {
        this.representativenessValue = representativenessValue;
    }

    public Long getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Long validFrom) {
        this.validFrom = validFrom;
    }

    public Long getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Long validUntil) {
        this.validUntil = validUntil;
    }

    public Integer getValidFromYear() {
        return this.validFromYear;
    }

    public void setValidFromYear(Integer validFromYear) {
        this.validFromYear = validFromYear;
    }

    public Integer getValidUntilYear() {
        return this.validUntilYear;
    }

    public void setValidUntilYear(Integer validUntilYear) {
        this.validUntilYear = validUntilYear;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDataprovider() {
        return this.dataprovider;
    }

    public void setDataprovider(String dataprovider) {
        this.dataprovider = dataprovider;
    }

    public String getTechnology() {
        return this.technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public List<String> getSupportedNomenclatures() {
        return this.supportedNomenclatures;
    }

    public void setSupportedNomenclatures(List<String> supportedNomenclatures) {
        this.supportedNomenclatures = supportedNomenclatures;
    }

    public List<String> getLciaMethods() {
        return this.lciaMethods;
    }

    public void setLciaMethods(List<String> lciaMethods) {
        this.lciaMethods = lciaMethods;
    }

    public List<String> getCategoryPaths() {
        return this.categoryPaths;
    }

    public void setCategoryPaths(List<String> categoryPaths) {
        this.categoryPaths = categoryPaths;
    }

    public List<String> getUnspscPaths() {
        return this.unspscPaths;
    }

    public void setUnspscPaths(List<String> unspscPaths) {
        this.unspscPaths = unspscPaths;
    }

    public List<String> getCo2pePaths() {
        return this.co2pePaths;
    }

    public void setCo2pePaths(List<String> co2pePaths) {
        this.co2pePaths = co2pePaths;
    }

    public String getUnspscCode() {
        return this.unspscCode;
    }

    public void setUnspscCode(String unspscCode) {
        this.unspscCode = unspscCode;
    }

    public String getCo2peCode() {
        return this.co2peCode;
    }

    public void setCo2peCode(String co2peCode) {
        this.co2peCode = co2peCode;
    }

    public ModelingType getModelingType() {
        return modelingType;
    }

    public void setModelingType(ModelingType modellingType) {
        this.modelingType = modellingType;
    }

    public MultifunctionalModeling getMultifunctionalModeling() {
        return this.multifunctionalModeling;
    }

    public void setMultifunctionalModeling(MultifunctionalModeling multifunctionalModeling) {
        this.multifunctionalModeling = multifunctionalModeling;
    }

    public BiogenicCarbonModeling getBiogenicCarbonModeling() {
        return this.biogenicCarbonModeling;
    }

    public void setBiogenicCarbonModeling(BiogenicCarbonModeling biogenicCarbonModeling) {
        this.biogenicCarbonModeling = biogenicCarbonModeling;
    }

    public EndOfLifeModeling getEndOfLifeModeling() {
        return this.endOfLifeModeling;
    }

    public void setEndOfLifeModeling(EndOfLifeModeling endOfLifeModeling) {
        this.endOfLifeModeling = endOfLifeModeling;
    }

    public WaterModeling getWaterModeling() {
        return this.waterModeling;
    }

    public void setWaterModeling(WaterModeling waterModeling) {
        this.waterModeling = waterModeling;
    }

    public InfrastructureModeling getInfrastructureModeling() {
        return this.infrastructureModeling;
    }

    public void setInfraStructureModeling(InfrastructureModeling infrastructureModeling) {
        this.infrastructureModeling = infrastructureModeling;
    }

    public EmissionModeling getEmissionModeling() {
        return this.emissionModeling;
    }

    public void setEmissionModeling(EmissionModeling emissionModeling) {
        this.emissionModeling = emissionModeling;
    }

    public CarbonStorageModeling getCarbonStorageModeling() {
        return this.carbonStorageModeling;
    }

    public void setCarbonStorageModeling(CarbonStorageModeling carbonStorageModeling) {
        this.carbonStorageModeling = carbonStorageModeling;
    }

    public SourceReliability getSourceReliability() {
        return this.sourceReliability;
    }

    public void setSourceReliability(SourceReliability sourceReliability) {
        this.sourceReliability = sourceReliability;
    }

    public AggregationType getAggregationType() {
        return this.aggregationType;
    }

    public void setAggregationType(AggregationType aggregationType) {
        this.aggregationType = aggregationType;
    }

    public ReviewType getReviewType() {
        return this.reviewType;
    }

    public void setReviewType(ReviewType reviewType) {
        this.reviewType = reviewType;
    }

    public ReviewSystem getreviewSystem() {
        return this.reviewSystem;
    }

    public void setReviewSystem(ReviewSystem reviewSystem) {
        this.reviewSystem = reviewSystem;
    }


    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public List<String> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<String> reviewers) {
        this.reviewers = reviewers;
    }

    public Boolean isCopyrightProtected() {
        return copyrightProtected;
    }

    public void setCopyrightProtected(Boolean copyrightProtected) {
        this.copyrightProtected = copyrightProtected;
    }

    public String getCopyrightHolder() {
        return copyrightHolder;
    }

    public void setCopyrightHolder(String copyrightHolder) {
        this.copyrightHolder = copyrightHolder;
    }

    public LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(LicenseType licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Boolean isFree() {
        return this.free;
    }

    public void setFree(Boolean free) {
        this.free = free;
    }

    public Boolean isPubliclyAccessible() {
        return this.publiclyAccessible;
    }

    public void setPubliclyAccessible(Boolean publiclyAccessible) {
        this.publiclyAccessible = publiclyAccessible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getdataSetUrl() {
        return this.dataSetUrl;
    }

    public void setDataSetUrl(String dataSetUrl) {
        this.dataSetUrl = dataSetUrl;
    }

    public DataFormat getFormat() {
        return this.format;
    }

    public void setFormat(DataFormat format) {
        this.format = format;
    }
}
