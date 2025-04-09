package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.fzk.iai.ilcd.service.model.process.IReferenceFlow;
import de.iai.ilcd.model.common.Classification;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.FlowDao;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.flow.FlowPropertyDescription;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.util.SodaUtil;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "exchange")
public class Exchange implements Serializable, IReferenceFlow {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(Exchange.class);
    protected int internalId;
    @ManyToOne(cascade = CascadeType.ALL)
    protected GlobalReference flowReference = null;
    @ManyToOne(fetch = FetchType.LAZY)
    protected Flow flow;
    @Transient
    protected Flow flowSoftReference;
    protected String location = "";

    //	@Transient
//	protected boolean flowSoftReferenceNotResolvable = false;
    protected String functionType = "";
    @Enumerated(EnumType.STRING)
    protected ExchangeDirectionValues exchangeDirection;
    protected String referenceToVariable;
    protected Double meanAmount = null;
    protected Double resultingAmount = null;
    protected Double minimumAmount = null;
    protected Double maximumAmount = null;
    protected String uncertaintyDistribution = "";
    protected float standardDeviation = -1;
    protected String allocation = "";
    protected String dataSource = "";
    protected String derivationType = "";
    @ManyToOne
    protected GlobalReference refToDataSource = null;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "exchange_comment", joinColumns = @JoinColumn(name = "exchange_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> comment = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter commentAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Exchange.this.comment;
        }
    });
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * List of amounts
     */
    @ElementCollection
    @CollectionTable(name = "exchange_amounts", joinColumns = @JoinColumn(name = "exchange_id"))
    private List<Amount> amounts = new ArrayList<Amount>();
    /**
     * Reference to a unit group
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "unitgroup_reference")
    private GlobalReference unitGroupReference = null;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAllocation() {
        return this.allocation;
    }

    public void setAllocation(String allocation) {
        this.allocation = allocation;
    }

    public IMultiLangString getComment() {
        return this.commentAdapter;
    }

    public void setComment(IMultiLangString comment) {
        this.commentAdapter.overrideValues(comment);
    }

    public String getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDerivationType() {
        return this.derivationType;
    }

    public void setDerivationType(String derivationType) {
        this.derivationType = derivationType;
    }

    public ExchangeDirectionValues getExchangeDirection() {
        return this.exchangeDirection;
    }

    public void setExchangeDirection(ExchangeDirectionValues exchangeDirection) {
        this.exchangeDirection = exchangeDirection;
    }

    public GlobalReference getFlowReference() {
        return this.flowReference;
    }

    public void setFlowReference(GlobalReference flowReference) {
        if (flowReference != null && !GlobalReferenceTypeValue.FLOW_DATA_SET.equals(flowReference.getType())) {
            throw new IllegalArgumentException("Provided reference is no flow reference!");
        }
        this.flowReference = flowReference;
    }

    @Override
    public IGlobalReference getReference() {
        return this.getFlowReference();
    }

    @Deprecated // in favor of getFlowWithSoftReference()
    public Flow getFlow() {
        return this.flow;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    public Flow getFlowWithSoftReference() {
        if (this.flow != null) {
            LOGGER.debug(() -> "associated flow is " + this.flow.getUuidAsString() + " version " + this.flow.getDataSetVersion());
            return this.flow;
        } else if (this.flowSoftReference != null)
            return this.flowSoftReference;

        LOGGER.debug(() -> "no flow is associated, need to look it up in the local database");
        try {
            LOGGER.debug(() -> "looking up flow " + this.getFlowReference().getRefObjectId() + " version " + this.getFlowReference().getVersionAsString());

            Flow resultExact = FlowDao.lookupFlow(this.getFlowReference().getRefObjectId(), this.getFlowReference().getVersion());
            if (resultExact != null) {
                LOGGER.debug(() -> "result is " + resultExact.getUuidAsString() + " version " + resultExact.getDataSetVersion());

                this.flowSoftReference = resultExact;
                return this.flowSoftReference;
            }

            Flow resultLatest = FlowDao.lookupFlow(this.getFlowReference().getRefObjectId(), null);
            if (resultLatest != null) {
                LOGGER.debug(() -> "result is " + resultLatest.getUuidAsString() + " version " + resultLatest.getDataSetVersion());

                this.flowSoftReference = resultLatest;
                return this.flowSoftReference;
            }
        } catch (NullPointerException e) {
            return null;
        }
        return null;
    }

    public String getFunctionType() {
        return this.functionType;
    }

    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }

    public int getInternalId() {
        return this.internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getMaximumAmount() {
        return this.maximumAmount;
    }

    public void setMaximumAmount(Double maximumAmount) {
        this.maximumAmount = maximumAmount;
    }

    public Double getMeanAmount() {
        return this.meanAmount;
    }

    public void setMeanAmount(Double meanAmount) {
        this.meanAmount = meanAmount;
    }

    @Override
    public Double getMeanValue() {
        return this.getMeanAmount();
    }

    public Double getMinimumAmount() {
        return this.minimumAmount;
    }

    public void setMinimumAmount(Double minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public GlobalReference getRefToDataSource() {
        return this.refToDataSource;
    }

    public void setRefToDataSource(GlobalReference refToDataSource) {
        this.refToDataSource = refToDataSource;
    }

    public String getReferenceToVariable() {
        return this.referenceToVariable;
    }

    public void setReferenceToVariable(String referenceToVariable) {
        this.referenceToVariable = referenceToVariable;
    }

    public Double getResultingAmount() {
        return this.resultingAmount;
    }

    public void setResultingAmount(Double resultingAmount) {
        this.resultingAmount = resultingAmount;
    }

    public float getStandardDeviation() {
        return this.standardDeviation;
    }

    public void setStandardDeviation(float standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public String getUncertaintyDistribution() {
        return this.uncertaintyDistribution;
    }

    public void setUncertaintyDistribution(String uncertaintyDistribution) {
        this.uncertaintyDistribution = uncertaintyDistribution;
    }

    /**
     * Get the amounts
     *
     * @return amounts
     */
    public List<Amount> getAmounts() {
        return this.amounts;
    }

    /**
     * Set the amounts
     *
     * @param amounts amounts to set
     */
    public void setAmounts(List<Amount> amounts) {
        this.amounts = amounts;
    }

    /**
     * @see SodaUtil#getAmountByModule(List, String)
     */
    public Amount getAmountByModule(String module) {
        return SodaUtil.getAmountByModule(this.amounts, module);
    }

    /**
     * @see SodaUtil#getAmountByModuleScenario(List, String, String)
     */
    public Amount getAmountByModuleScenario(String module, String scenario) {
        if (scenario != null)
            return SodaUtil.getAmountByModuleScenario(this.amounts, module, scenario);
        else
            return SodaUtil.getAmountByModule(this.amounts, module);
    }

    /**
     * Get the unit group reference
     *
     * @return unit group reference
     */
    public GlobalReference getUnitGroupReference() {
        return this.unitGroupReference;
    }

    /**
     * Set the unit group reference
     *
     * @param unitGroupReference unit group reference to set
     */
    public void setUnitGroupReference(GlobalReference unitGroupReference) {
        if (unitGroupReference != null && !GlobalReferenceTypeValue.UNIT_GROUP_DATA_SET.equals(unitGroupReference.getType())) {
            throw new IllegalArgumentException("Provided reference is no unit group reference!");
        }
        this.unitGroupReference = unitGroupReference;
    }

    @Override
    public IMultiLangString getFlowName() {
        if (this.getFlowWithSoftReference() != null) {
            return this.getFlowWithSoftReference().getName();
        } else if (this.flowReference != null) {
            return this.flowReference.getShortDescription();
        } else
            return null;
    }

    public String getFlowType() {
        if (this.getFlowWithSoftReference() != null) {
            return this.getFlowWithSoftReference().getType().getValue();
        } else {
            return null;
        }
    }

    public Classification getClassification() {
        if (this.getFlowWithSoftReference() != null) {
            if (this.getFlowWithSoftReference() instanceof ElementaryFlow) {
                return ((ElementaryFlow) this.getFlowWithSoftReference()).getCategorization();
            } else {
                return this.getFlowWithSoftReference().getClassification();
            }
        }
        return null;
    }

    public String getClassificationAsString() {

        Classification classification = this.getClassification();
        if (classification != null) {
            return classification.getClassHierarchyAsString();
        }

        return null;
    }

    /**
     * Get the name of the reference flow property (fallback: get short description of flow's global reference)
     *
     * @return the name of the reference flow property
     */
    public IMultiLangString getReferenceFlowPropertyName() {
        if (this.getFlowWithSoftReference() != null) {
            final FlowPropertyDescription fpDesc = this.getFlowWithSoftReference().getReferencePropertyDescription();
            if (fpDesc != null) {
                final FlowProperty refProp = fpDesc.getFlowProperty();
                if (refProp != null) {
                    return refProp.getName();
                } else {
                    final GlobalReference refPropGlobalRef = fpDesc.getFlowPropertyRef();
                    if (refPropGlobalRef != null) {
                        return refPropGlobalRef.getShortDescription();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public IMultiLangString getFlowPropertyName() {
        return this.getReferenceFlowPropertyName();
    }

    /**
     * Get the reference unit as String
     *
     * @return the reference unit as String
     */
    public String getReferenceUnit() {
        // TODO: review consistency check
        if (this.getFlowWithSoftReference() != null) {
            final FlowPropertyDescription fpDesc = this.getFlowWithSoftReference().getReferencePropertyDescription();
            if (fpDesc != null) {
                final FlowProperty refProp = fpDesc.getFlowProperty();
                if (refProp != null) {
                    final UnitGroup unitGroup = refProp.getUnitGroup();
                    if (unitGroup != null) {
                        return unitGroup.getDefaultUnit();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getUnit() {
        return this.getReferenceUnit();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.exchangeDirection == null) ? 0 : this.exchangeDirection.hashCode());
        result = prime * result + ((this.flowReference == null) ? 0 : this.flowReference.hashCode());
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
        if (!(obj instanceof Exchange)) {
            return false;
        }
        Exchange other = (Exchange) obj;
        if (this.exchangeDirection != other.exchangeDirection) {
            return false;
        }
        if (this.flowReference == null) {
            if (other.flowReference != null) {
                return false;
            }
        } else if (!this.flowReference.equals(other.flowReference)) {
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

    @Override
    public String toString() {
        return "de.iai.ilcd.model.process.Exchange[id=" + this.id + "]";
    }

}
