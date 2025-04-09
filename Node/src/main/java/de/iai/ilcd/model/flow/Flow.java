package de.iai.ilcd.model.flow;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.IFlowVO;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.iai.ilcd.model.flow.Flow.TABLE_NAME;

/**
 * Common Flow representation
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = {"UUID", "MAJORVERSION", "MINORVERSION", "SUBMINORVERSION"}))
@DiscriminatorColumn(name = "flow_object_type", discriminatorType = DiscriminatorType.CHAR)
public abstract class Flow extends DataSet implements Serializable, IFlowVO {

    public static final String TABLE_NAME = "flow_common";

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 320361630947897226L;
    /**
     * Synonyms map
     */
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "flow_synonyms", joinColumns = @JoinColumn(name = "flow_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> synonyms = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter synonymsAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {
        @Override
        public Map<String, String> getMap() {
            return Flow.this.synonyms;
        }
    });
    /**
     * LocationOfSupply map
     */
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "flow_locationofsupply", joinColumns = @JoinColumn(name = "flow_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> locationOfSupply = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter locationOfSupplyAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {
        @Override
        public Map<String, String> getMap() {
            return Flow.this.locationOfSupply;
        }
    });
    /**
     * The CAS number
     */
    protected String casNumber;

    /**
     * The EC number
     */
    @Column(name = "ecnumber")
    protected String ecNumber;

    /**
     * The sum formula
     */
    protected String sumFormula;

    /**
     * Reference property description
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reference_property_description_id")
    protected FlowPropertyDescription referencePropertyDescription;

    /**
     * Property descriptions
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "flow_propertydescriptions")
    private Set<FlowPropertyDescription> propertyDescriptions = new HashSet<FlowPropertyDescription>();

    /**
     * Cache for the reference property.
     * 20 character limit should be sufficient
     */
    // only for query efficiency
    @Basic
    @Column(name = "referenceProperty_cache", length = 20)
    private String referencePropertyCache;

    /**
     * Cache for the reference property unit.
     * 10 character limit should be sufficient
     */
    // only for query efficiency
    @Basic
    @Column(name = "referencePropertyUnit_cache", length = 10)
    private String referencePropertyUnitCache;


    /**
     * Get the CAS number
     *
     * @return CAS number
     */
    @Override
    public String getCasNumber() {
        return this.casNumber;
    }

    /**
     * Set the CAS number
     *
     * @param casNumber CAS number to set
     */
    public void setCasNumber(String casNumber) {
        this.casNumber = casNumber;
    }

    /**
     * Get the property descriptions
     *
     * @return property descriptions
     */
    public Set<FlowPropertyDescription> getPropertyDescriptions() {
        return this.propertyDescriptions;
    }

    /**
     * Set the property descriptions
     *
     * @param propertyDescriptions property descriptions to set
     */
    protected void setPropertyDescriptions(Set<FlowPropertyDescription> propertyDescriptions) {
        this.propertyDescriptions = propertyDescriptions;
    }

    /**
     * Add a property description
     *
     * @param propertyDescription property description to add
     */
    public void addPropertDesription(FlowPropertyDescription propertyDescription) {
        if (!this.propertyDescriptions.contains(propertyDescription)) {
            this.propertyDescriptions.add(propertyDescription);
        }
    }

    /**
     * Get the reference property description.
     *
     * @return reference property description
     */
    public FlowPropertyDescription getReferencePropertyDescription() {
        return this.referencePropertyDescription;
    }

    /**
     * Get the reference flow property description. Delegates to {@link #getReferencePropertyDescription()}.
     *
     * @return reference flow property description
     */
    @Override
    public FlowPropertyDescription getReferenceFlowProperty() {
        return this.getReferencePropertyDescription();
    }

    /**
     * Set the reference flow property description
     *
     * @param referencePropertyDescription reference property description to set
     */
    public void setReferenceProperty(FlowPropertyDescription referencePropertyDescription) {
        this.referencePropertyDescription = referencePropertyDescription;
    }

    /**
     * Get the sum formula
     *
     * @return sum formula
     */
    @Override
    public String getSumFormula() {
        return this.sumFormula;
    }

    /**
     * Set the sum formula
     *
     * @param sumFormula sum formula to set
     */
    public void setSumFormula(String sumFormula) {
        this.sumFormula = sumFormula;
    }

    /**
     * Get the synonyms
     *
     * @return adapter to synonyms map
     */
    @Override
    public IMultiLangString getSynonyms() {
        return this.synonymsAdapter;
    }

    /**
     * Set the synonyms
     *
     * @param synonyms synonyms to set (adapter writes values to map)
     */
    public void setSynonyms(IMultiLangString synonyms) {
        this.synonymsAdapter.overrideValues(synonyms);
    }

    /**
     * Apply cache fields for flow, those are:
     * <ul>
     * <li>{@link #getReferencePropertyDescription()}</li>
     * <li>{@link FlowPropertyDescription#getDefaultUnit() default unit} of {@link #getReferencePropertyDescription()}</li>
     * </ul>
     */
    @Override
    @PrePersist
    protected void applyDataSetCache() {
        super.applyDataSetCache();
        if (this.referencePropertyDescription != null) {
            if (this.referencePropertyDescription.getFlowPropertyName() != null) {
                this.referencePropertyCache = StringUtils.substring(this.referencePropertyDescription.getFlowPropertyName().getDefaultValue(), 0, 20);
            } else {
                this.referencePropertyCache = null;
            }
            this.referencePropertyUnitCache = StringUtils.substring(this.referencePropertyDescription.getDefaultUnit(), 0, 10);
        } else {
            this.referencePropertyCache = null;
            this.referencePropertyUnitCache = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetType getDataSetType() {
        return DataSetType.FLOW;
    }

    public abstract TypeOfFlowValue getType();

    public String getEcNumber() {
        return ecNumber;
    }

    public void setEcNumber(String ecNumber) {
        this.ecNumber = ecNumber;
    }

    /**
     * Get the location of supply
     *
     * @return adapter to location of supply
     */
    @Override
    public IMultiLangString getLocationOfSupply() {
        return this.locationOfSupplyAdapter;
    }

    /**
     * Set the location of supply
     *
     * @param locationOfSupply location of supply to set (adapter writes values to map)
     */
    public void setLocationOfSupply(IMultiLangString locationOfSupply) {
        this.locationOfSupplyAdapter.overrideValues(locationOfSupply);
    }

    public String getDirPathInZip() {
        return "ILCD/" + DatasetTypes.FLOWS.getValue();
    }
}
