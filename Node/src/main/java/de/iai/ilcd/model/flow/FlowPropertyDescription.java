package de.iai.ilcd.model.flow;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.flow.IReferenceFlowPropertyType;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "flowpropertydescription")
public class FlowPropertyDescription implements Serializable, IReferenceFlowPropertyType {

    private static final long serialVersionUID = 1L;
    protected int internalId;
    @ManyToOne(fetch = FetchType.LAZY)
    protected FlowProperty flowProperty;
    @ManyToOne(cascade = CascadeType.ALL)
    protected GlobalReference flowPropertyRef;
    protected double meanValue;
    protected double minValue;
    @Column(name = "maximumValue")
    protected double maxValue;
    protected String uncertaintyType;
    protected float standardDeviation;
    protected String derivationType;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "flowpropertydescription_description", joinColumns = @JoinColumn(name = "flowpropertydescription_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> description = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter descriptionAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return FlowPropertyDescription.this.description;
        }
    });
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDerivationType() {
        return this.derivationType;
    }

    public void setDerivationType(String derivationType) {
        this.derivationType = derivationType;
    }

    public IMultiLangString getDescription() {
        return this.descriptionAdapter;
    }

    public void setDescription(IMultiLangString description) {
        this.descriptionAdapter.overrideValues(description);
    }

    public FlowProperty getFlowProperty() {
        return this.flowProperty;
    }

    public void setFlowProperty(FlowProperty flowProperty) {
        this.flowProperty = flowProperty;
    }

    public IMultiLangString getFlowPropertyName() {
        IMultiLangString name = null;
        if (this.getFlowProperty() != null) {
            name = this.getFlowProperty().getName();
        } else {
            name = this.getFlowPropertyRef().getShortDescription();
        }

        return name;
    }

    @Override
    public IMultiLangString getName() {
        return this.getFlowPropertyName();
    }

    @Override
    public String getDefaultUnit() {
        if (this.getFlowProperty() != null) {
            return this.getFlowProperty().getDefaultUnit();
        } else {
            return null;
        }
    }

    public String getFlowPropertyUnit() {
        return this.getDefaultUnit();
    }

    public GlobalReference getFlowPropertyRef() {
        return this.flowPropertyRef;
    }

    public void setFlowPropertyRef(GlobalReference flowPropertyRef) {
        this.flowPropertyRef = flowPropertyRef;
    }

    @Override
    public GlobalReference getReference() {
        return this.getFlowPropertyRef();
    }

    public int getInternalId() {
        return this.internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getMeanValue() {
        return this.meanValue;
    }

    public void setMeanValue(double meanValue) {
        this.meanValue = meanValue;
    }

    public double getMinValue() {
        return this.minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public float getStandardDeviation() {
        return this.standardDeviation;
    }

    public void setStandardDeviation(float standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public String getUncertaintyType() {
        return this.uncertaintyType;
    }

    public void setUncertaintyType(String uncertaintyType) {
        this.uncertaintyType = uncertaintyType;
    }

    // TODO: generate it from configuration information
    @Override
    public String getHref() {
        return "";
    }

    public boolean isCarbonicProperty() {
        try {
            String referencedUuidString = StringUtils.lowerCase(this.getFlowPropertyRef().getRefObjectId());
            if (referencedUuidString.equals("62e503ce-544a-4599-b2ad-bcea15a7bf20") || referencedUuidString.equals("262a541b-209e-44cc-a426-33bce30de7b1"))
                return true;
        } catch (NullPointerException npe) {
            return false;
        }
        return false;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.flow.FlowPropertyDescription[id=" + this.id + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.flowPropertyRef == null) ? 0 : this.flowPropertyRef.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + this.internalId;
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
        if (!(obj instanceof FlowPropertyDescription)) {
            return false;
        }
        FlowPropertyDescription other = (FlowPropertyDescription) obj;
        if (this.flowPropertyRef == null) {
            if (other.flowPropertyRef != null) {
                return false;
            }
        } else if (!this.flowPropertyRef.equals(other.flowPropertyRef)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.internalId != other.internalId) {
            return false;
        }
        return true;
    }

}
