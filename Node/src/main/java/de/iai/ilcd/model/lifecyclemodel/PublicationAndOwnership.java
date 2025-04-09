package de.iai.ilcd.model.lifecyclemodel;

import de.fzk.iai.ilcd.api.binding.generated.common.FTMultiLang;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.PublicationAndOwnershipType;
import de.fzk.iai.ilcd.service.model.enums.LicenseTypeValue;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.common.exception.FormatException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author MK
 * @since soda4LCA 5.7.0
 */

@Entity
@Table(name = "lcm_administrativeinformation_publicationandownership")
public class PublicationAndOwnership implements Serializable {

    private static final long serialVersionUID = -5005940714371619619L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PublicationAndOwnership_id")
    protected long id;
    protected String permanentUri;
    @Embedded
    protected DataSetVersion dataSetVersion;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lcm_publicationandownership_ref_datasetversion", joinColumns = @JoinColumn(name = "PublicationAndOwnership_id"), inverseJoinColumns = @JoinColumn(name = "ref_precedingDataSetVersion_id"))
    protected List<GlobalReference> referenceToPrecedingDataSetVersion = new ArrayList<GlobalReference>();
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    protected GlobalReference referenceToOwnershipOfDataSet;
    protected boolean copyright;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lcm_publicationandownership_ref_entities_w_exc_access", joinColumns = @JoinColumn(name = "PublicationAndOwnership_id"), inverseJoinColumns = @JoinColumn(name = "ref_entities_w_exc_access_id"))
    protected List<GlobalReference> referenceToEntitiesWithExclusiveAccess = new ArrayList<GlobalReference>();
    @Enumerated(EnumType.STRING)
    protected LicenseTypeValue licenseType;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lcm_publicationandownership_userestrictions", joinColumns = @JoinColumn(name = "PublicationAndOwnership_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> useRestrictions = new HashMap<String, String>();

    public PublicationAndOwnership() {
    }

    public PublicationAndOwnership(PublicationAndOwnershipType pub) {
        this.permanentUri = Optional.ofNullable(pub).map(PublicationAndOwnershipType::getPermanentDataSetURI)
                .orElse(null);

        this.dataSetVersion = Optional.ofNullable(pub).map(PublicationAndOwnershipType::getDataSetVersion).map(v -> {
            try {
                return DataSetVersion.parse(v);
            } catch (FormatException e) {
                e.printStackTrace();
                return null;
            }
        }).orElse(null);

        for (GlobalReferenceType ref : Optional.ofNullable(pub)
                .map(PublicationAndOwnershipType::getReferenceToPrecedingDataSetVersion)
                .orElse(new ArrayList<GlobalReferenceType>()))
            this.addReferenceToPrecedingDataSetVersion(new GlobalReference(ref));

        this.referenceToOwnershipOfDataSet = Optional.ofNullable(pub)
                .map(PublicationAndOwnershipType::getReferenceToOwnershipOfDataSet).map(r -> new GlobalReference(r))
                .orElse(null);

        this.copyright = Optional.ofNullable(pub).map(PublicationAndOwnershipType::isCopyright).orElse(false);

        this.licenseType = Optional.ofNullable(pub).map(PublicationAndOwnershipType::getLicenseType)
                .map(l -> l.getValue()).map(m -> LicenseTypeValue.fromValue(m)).orElse(null);

        for (FTMultiLang s : Optional.ofNullable(pub).map(PublicationAndOwnershipType::getAccessRestrictions)
                .orElse(new ArrayList<FTMultiLang>()))
            this.useRestrictions.put(s.getLang(), s.getValue());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPermanentUri() {
        return permanentUri;
    }

    public void setPermanentUri(String permanentUri) {
        this.permanentUri = permanentUri;
    }

    public DataSetVersion getDataSetVersion() {
        return dataSetVersion;
    }

    public void setDataSetVersion(DataSetVersion dataSetVersion) {
        this.dataSetVersion = dataSetVersion;
    }

    public List<GlobalReference> getReferenceToPrecedingDataSetVersion() {
        return referenceToPrecedingDataSetVersion;
    }

    public void addReferenceToPrecedingDataSetVersion(GlobalReference referenceToPrecedingDataSetVersion) {
        this.referenceToPrecedingDataSetVersion.add(referenceToPrecedingDataSetVersion);
    }

    public GlobalReference getReferenceToOwnershipOfDataSet() {
        return referenceToOwnershipOfDataSet;
    }

    public void setReferenceToOwnershipOfDataSet(GlobalReference referenceToOwnershipOfDataSet) {
        this.referenceToOwnershipOfDataSet = referenceToOwnershipOfDataSet;
    }

    public boolean isCopyright() {
        return copyright;
    }

    public void setCopyright(boolean copyright) {
        this.copyright = copyright;
    }

    public List<GlobalReference> getReferenceToEntitiesWithExclusiveAccess() {
        return referenceToEntitiesWithExclusiveAccess;
    }

    public LicenseTypeValue getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(LicenseTypeValue licenseType) {
        this.licenseType = licenseType;
    }

    public Map<String, String> getUseRestrictions() {
        return useRestrictions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(copyright, dataSetVersion, licenseType, permanentUri,
                referenceToEntitiesWithExclusiveAccess, referenceToOwnershipOfDataSet,
                referenceToPrecedingDataSetVersion, useRestrictions);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PublicationAndOwnership other = (PublicationAndOwnership) obj;
        return copyright == other.copyright && Objects.equals(dataSetVersion, other.dataSetVersion)
                && licenseType == other.licenseType && Objects.equals(permanentUri, other.permanentUri)
                && Objects.equals(referenceToEntitiesWithExclusiveAccess, other.referenceToEntitiesWithExclusiveAccess)
                && Objects.equals(referenceToOwnershipOfDataSet, other.referenceToOwnershipOfDataSet)
                && Objects.equals(referenceToPrecedingDataSetVersion, other.referenceToPrecedingDataSetVersion)
                && Objects.equals(useRestrictions, other.useRestrictions);
    }

    @Override
    public String toString() {
        return "PublicationAndOwnership [id=" + id + ", permanentUri=" + permanentUri + ", dataSetVersion="
                + dataSetVersion + ", referenceToPrecedingDataSetVersion=" + referenceToPrecedingDataSetVersion
                + ", referenceToOwnershipOfDataSet=" + referenceToOwnershipOfDataSet + ", copyright=" + copyright
                + ", referenceToEntitiesWithExclusiveAccess=" + referenceToEntitiesWithExclusiveAccess
                + ", licenseType=" + licenseType + ", useRestrictions=" + useRestrictions + "]";
    }

}
