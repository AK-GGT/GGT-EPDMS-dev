package de.iai.ilcd.model.lciamethod;

import de.fzk.iai.ilcd.api.binding.generated.common.DataDerivationTypeStatusValues;
import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Characterisation factor representation for {@link LCIAMethod LCIAMethods}
 */
@Entity
@Table(name = "lciamethodcharacterisationfactor")
public class LCIAMethodCharacterisationFactor {

    /**
     * The short description
     */
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lciamethodcharfactor_description", joinColumns = @JoinColumn(name = "lciamethodcharacterisationfactor_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> shortDescription = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter shortDescriptionAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return LCIAMethodCharacterisationFactor.this.shortDescription;
        }
    });
    /**
     * Synthetic ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = null;
    /**
     * The referenced flow as real instance
     */
    @ManyToOne(cascade = {})
    private ElementaryFlow referencedFlowInstance;
    /**
     * GlobalReference type for the referenced flow
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private GlobalReference flowGlobalReference;

    /**
     * The exchange direction
     */
    @Enumerated(EnumType.STRING)
    private ExchangeDirectionValues exchangeDirection;

    /**
     * The mean value
     */
    @Basic
    private Double meanValue;

    /**
     * The location
     */
    @Basic
    private String location;

    /**
     * The data derivation type status
     */
    @Enumerated(EnumType.STRING)
    private DataDerivationTypeStatusValues dataDerivationTypeStatus;

    /**
     * Get the id value
     *
     * @return id value
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the id
     *
     * @param id id
     */
    protected void setId(Long id) {
        this.id = id;
    }

    /**
     * <p>
     * Get the flow instance that is referenced by this characterisation
     * </p>
     * <p>
     * <b>Please note</b>: this is only available for flows that were in the database during import of characterisation
     * method! So this characterisation might return a <code>null</code>! value. You can use
     * {@link #hasReferencedFlowInstance()} to check.
     * </p>
     *
     * @return flow that is referenced by this characterisation
     */
    public ElementaryFlow getReferencedFlowInstance() {
        return this.referencedFlowInstance;
    }

    /**
     * Set the new referenced flow instance
     *
     * @param referencedFlowInstance the new referenced flow instance
     */
    public void setReferencedFlowInstance(ElementaryFlow referencedFlowInstance) {
        this.referencedFlowInstance = referencedFlowInstance;
    }

    /**
     * Check if a {@link #getReferencedFlowInstance() referenced flow instance} is available
     *
     * @return <code>true</code> if available, else <code>false</code>
     */
    public boolean hasReferencedFlowInstance() {
        return this.getReferencedFlowInstance() != null;
    }

    /**
     * Get the short description
     *
     * @return short description
     */
    public IMultiLangString getShortDescription() {
        return this.shortDescriptionAdapter;
    }

    /**
     * Set the new short description
     *
     * @param shortDescription new short description to set
     */
    public void setShortDescription(IMultiLangString shortDescription) {
        this.shortDescriptionAdapter.overrideValues(shortDescription);
    }

    /**
     * <p>
     * Get the global reference for the flow
     * </p>
     * <p>
     * <b>Please note:</b> If the flow was in the database during import, it can be accessed via
     * {@link #getReferencedFlowInstance()}. You can check the availability of the flow via
     * {@link #hasReferencedFlowInstance()}.
     * </p>
     *
     * @return global reference for the flow
     */
    public GlobalReference getFlowGlobalReference() {
        return this.flowGlobalReference;
    }

    /**
     * Set the new global reference for the flow
     *
     * @param flowGlobalReference new global reference for the flow to set
     */
    public void setFlowGlobalReference(GlobalReference flowGlobalReference) {
        this.flowGlobalReference = flowGlobalReference;
    }

    /**
     * Get the exchange direction
     *
     * @return exchange direction
     */
    public ExchangeDirectionValues getExchangeDirection() {
        return this.exchangeDirection;
    }

    /**
     * Set the new exchange direction
     *
     * @param exchangeDirection new exchange direction to set
     */
    public void setExchangeDirection(ExchangeDirectionValues exchangeDirection) {
        this.exchangeDirection = exchangeDirection;
    }

    /**
     * Get the mean value
     *
     * @return mean value
     */
    public Double getMeanValue() {
        return this.meanValue;
    }

    /**
     * Set the new mean value
     *
     * @param meanValue new mean value to set
     */
    public void setMeanValue(Double meanValue) {
        this.meanValue = meanValue;
    }

    /**
     * Get the data derivation type status
     *
     * @return data derivation type status
     */
    public DataDerivationTypeStatusValues getDataDerivationTypeStatus() {
        return this.dataDerivationTypeStatus;
    }

    /**
     * Set the new data derivation type status
     *
     * @param dataDerivationTypeStatus the new data derivation type status to set
     */
    public void setDataDerivationTypeStatus(DataDerivationTypeStatusValues dataDerivationTypeStatus) {
        this.dataDerivationTypeStatus = dataDerivationTypeStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return LCIAMethodCharacterisationFactor.class.getName() + "[id=" + this.getId() + "]";
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


}
