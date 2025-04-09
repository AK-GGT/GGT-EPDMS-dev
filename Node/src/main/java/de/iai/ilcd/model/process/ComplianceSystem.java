package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.ComplianceDeclarationsType;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.ComplianceType;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.ComplianceValue;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.iai.ilcd.model.common.GlobalReference;
import org.apache.commons.collections.comparators.NullComparator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "compliancesystem")
public class ComplianceSystem implements Serializable, IComplianceSystem, Comparable<IComplianceSystem> {

    @SuppressWarnings("unchecked")
    public static final Comparator<String> NULL_SAFE_COMPARATOR = new NullComparator(String.CASE_INSENSITIVE_ORDER);
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    private GlobalReference sourceReference;

    @Enumerated(EnumType.STRING)
    private ComplianceValue overallCompliance;

    @Enumerated(EnumType.STRING)
    private ComplianceValue nomenclatureCompliance;

    @Enumerated(EnumType.STRING)
    private ComplianceValue methodologicalCompliance;

    @Enumerated(EnumType.STRING)
    private ComplianceValue reviewCompliance;

    @Enumerated(EnumType.STRING)
    private ComplianceValue documentationCompliance;

    @Enumerated(EnumType.STRING)
    private ComplianceValue qualityCompliance;


    public ComplianceSystem() {

    }

    public ComplianceSystem(ComplianceDeclarationsType c) {

        this.setReviewCompliance(Optional.ofNullable(c).map(ComplianceDeclarationsType::getCompliance)
                .map(ComplianceType::getReviewCompliance).map(v -> ComplianceValue.fromValue(v.getValue()))
                .orElse(ComplianceValue.NOT_DEFINED));

        this.setOverallCompliance(Optional.ofNullable(c).map(ComplianceDeclarationsType::getCompliance)
                .map(ComplianceType::getApprovalOfOverallCompliance).map(t -> ComplianceValue.fromValue(t.getValue()))
                .orElse(ComplianceValue.NOT_DEFINED));

        this.setMethodologicalCompliance(Optional.ofNullable(c).map(ComplianceDeclarationsType::getCompliance)
                .map(ComplianceType::getMethodologicalCompliance).map(t -> ComplianceValue.fromValue(t.getValue()))
                .orElse(ComplianceValue.NOT_DEFINED));

        this.setNomenclatureCompliance(Optional.ofNullable(c).map(ComplianceDeclarationsType::getCompliance)
                .map(ComplianceType::getNomenclatureCompliance).map(t -> ComplianceValue.fromValue(t.getValue()))
                .orElse(ComplianceValue.NOT_DEFINED));

        this.setDocumentationCompliance(Optional.ofNullable(c).map(ComplianceDeclarationsType::getCompliance)
                .map(ComplianceType::getDocumentationCompliance).map(t -> ComplianceValue.fromValue(t.getValue()))
                .orElse(ComplianceValue.NOT_DEFINED));

        this.setQualityCompliance(Optional.ofNullable(c).map(ComplianceDeclarationsType::getCompliance)
                .map(ComplianceType::getQualityCompliance).map(t -> ComplianceValue.fromValue(t.getValue()))
                .orElse(ComplianceValue.NOT_DEFINED));

        this.setSourceReference(Optional.ofNullable(c).map(ComplianceDeclarationsType::getCompliance)
                .map(ComplianceType::getReferenceToComplianceSystem).map(r -> new GlobalReference(r)).orElse(null));
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
//		if ( this.sourceReference == null ) {
//			return "";
//		}
//		if ( this.sourceReference.getShortDescription() == null ) {
//			return "";
//		}
//		return this.sourceReference.getShortDescription().getDefaultValue();
//		
        return Optional.ofNullable(this.sourceReference).map(GlobalReference::getShortDescription)
                .map(IMultiLangString::getDefaultValue).orElse("");
    }

    @Override
    public ComplianceValue getDocumentationCompliance() {
        return this.documentationCompliance;
    }

    public void setDocumentationCompliance(ComplianceValue documentationCompliance) {
        this.documentationCompliance = documentationCompliance;
    }

    @Override
    public ComplianceValue getMethodologicalCompliance() {
        return this.methodologicalCompliance;
    }

    public void setMethodologicalCompliance(ComplianceValue methodologicalCompliance) {
        this.methodologicalCompliance = methodologicalCompliance;
    }

    @Override
    public ComplianceValue getNomenclatureCompliance() {
        return this.nomenclatureCompliance;
    }

    public void setNomenclatureCompliance(ComplianceValue nomenclatureCompliance) {
        this.nomenclatureCompliance = nomenclatureCompliance;
    }

    @Override
    public ComplianceValue getOverallCompliance() {
        return this.overallCompliance;
    }

    public void setOverallCompliance(ComplianceValue overallCompliance) {
        this.overallCompliance = overallCompliance;
    }

    @Override
    public ComplianceValue getQualityCompliance() {
        return this.qualityCompliance;
    }

    public void setQualityCompliance(ComplianceValue qualityCompliance) {
        this.qualityCompliance = qualityCompliance;
    }

    @Override
    public ComplianceValue getReviewCompliance() {
        return this.reviewCompliance;
    }

    public void setReviewCompliance(ComplianceValue reviewCompliance) {
        this.reviewCompliance = reviewCompliance;
    }

    @Override
    public GlobalReference getReference() {
        return this.sourceReference;
    }

    public void setSourceReference(GlobalReference sourceReference) {
        this.sourceReference = sourceReference;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.sourceReference == null) ? 0 : this.sourceReference.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ComplianceSystem)) {
            return false;
        }
        ComplianceSystem other = (ComplianceSystem) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.sourceReference == null) {
            if (other.sourceReference != null) {
                return false;
            }
        } else if (!this.sourceReference.equals(other.sourceReference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.process.ComplianceSystem[id=" + this.id + "]";
    }

    public int compareTo(IComplianceSystem other) {
        return NULL_SAFE_COMPARATOR.compare(this.getName(), other.getName());
    }

}
