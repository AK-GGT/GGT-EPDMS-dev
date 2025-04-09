package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.TypeOfQuantitativeReferenceValue;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "quantitativereference")
public class InternalQuantitativeReference implements Serializable {

    private static final long serialVersionUID = 1L;
    @Enumerated(EnumType.STRING)
    protected TypeOfQuantitativeReferenceValue type;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "process_quantref_otherreference", joinColumns = @JoinColumn(name = "internalquantitativereference_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> otherReference = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter otherReferenceAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return InternalQuantitativeReference.this.otherReference;
        }
    });
    @ElementCollection
    @CollectionTable(name = "process_quantref_referenceids", joinColumns = @JoinColumn(name = "internalquantitativereference_id"))
    @Column(name = "refId")
    Set<Integer> referenceIds = new HashSet<Integer>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IMultiLangString getOtherReference() {
        return this.otherReferenceAdapter;
    }

    public void setOtherReference(IMultiLangString otherReference) {
        this.otherReferenceAdapter.overrideValues(otherReference);
    }

    public Set<Integer> getReferenceIds() {
        return referenceIds;
    }

    protected void setReferenceIds(Set<Integer> referenceIds) {
        this.referenceIds = referenceIds;
    }

    public void addReferenceId(Integer referenceId) {
        // if (! referenceIds.contains(referenceId))
        referenceIds.add(referenceId);
    }

    public TypeOfQuantitativeReferenceValue getType() {
        return type;
    }

    public void setType(TypeOfQuantitativeReferenceValue type) {
        this.type = type;
    }

    // Completely ignored transient attributes from equals and hashcode

    @Override
    public int hashCode() {
        return Objects.hash(otherReference, referenceIds, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InternalQuantitativeReference other = (InternalQuantitativeReference) obj;
        return Objects.equals(otherReference, other.otherReference) && Objects.equals(referenceIds, other.referenceIds)
                && type == other.type;
    }
}
