package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.util.SodaUtil;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;

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
@Table(name = "lciaresult")
public class LciaResult implements Serializable {

    private static final long serialVersionUID = 1L;
    @ManyToOne(cascade = CascadeType.ALL)
    protected GlobalReference methodReference = null;
    protected Double meanAmount = null;
    protected String uncertaintyDistribution = "";
    protected float standardDeviation = -1;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lciaresult_comment", joinColumns = @JoinColumn(name = "lciaresult_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> comment = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter commentAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return LciaResult.this.comment;
        }
    });
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * List of amounts
     */
    @ElementCollection
    @CollectionTable(name = "lciaresult_amounts", joinColumns = @JoinColumn(name = "lciaresult_id"))
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

    public IMultiLangString getComment() {
        return this.commentAdapter;
    }

    public void setComment(IMultiLangString comment) {
        this.commentAdapter.overrideValues(comment);
    }

    public Double getMeanAmount() {
        return this.meanAmount;
    }

    public void setMeanAmount(Double meanAmount) {
        this.meanAmount = meanAmount;
    }

    public GlobalReference getMethodReference() {
        return this.methodReference;
    }

    public void setMethodReference(GlobalReference methodReference) {
        this.methodReference = methodReference;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.methodReference == null) ? 0 : this.methodReference.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LciaResult)) {
            return false;
        }
        LciaResult other = (LciaResult) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.methodReference == null) {
            if (other.methodReference != null) {
                return false;
            }
        } else if (!this.methodReference.equals(other.methodReference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.process.LciaResult[id=" + this.id + "]";
    }

}
