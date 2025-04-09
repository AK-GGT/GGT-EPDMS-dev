package de.iai.ilcd.model.flow;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.iai.ilcd.model.common.Classification;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.ElementaryFlowDao;
import de.iai.ilcd.model.datastock.DataStock;

import javax.persistence.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static de.iai.ilcd.model.flow.ElementaryFlow.TABLE_NAME;

/**
 * Elementary flow
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue(value = "e")
@Table(name = TABLE_NAME)
@AssociationOverrides({
        @AssociationOverride(name = "classifications", joinTable = @JoinTable(name = "flow_elementary_classifications"), joinColumns = @JoinColumn(name = "eflow_id")),
        @AssociationOverride(name = "propertyDescriptions", joinTable = @JoinTable(name = "flow_elementary_propertydescriptions")),
        @AssociationOverride(name = "synonyms", joinTable = @JoinTable(name = "flow_elementary_synonyms"), joinColumns = @JoinColumn(name = "eflow_id")),
        @AssociationOverride(name = "description", joinTable = @JoinTable(name = "flow_elementary_description"), joinColumns = @JoinColumn(name = "eflow_id")),
        @AssociationOverride(name = "name", joinTable = @JoinTable(name = "flow_elementary_name"), joinColumns = @JoinColumn(name = "eflow_id"))})
public class ElementaryFlow extends Flow {

    public static final String TABLE_NAME = "flow_elementary";

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -8622034936593695146L;

    /**
     * The data stocks this flow is contained in
     */
    @ManyToMany(mappedBy = "elementaryFlows", fetch = FetchType.LAZY)
    protected Set<DataStock> containingDataStocks = new HashSet<DataStock>();

    /**
     * flow categorization
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "categorization")
    protected Classification categorization = new Classification();

    /**
     * Get the flow categorization
     *
     * @return flow categorization
     */
    public Classification getCategorization() {
        return this.categorization;
    }

    /**
     * Set the flow categorization
     *
     * @param categorization flow categorization to set
     */
    public void setCategorization(Classification categorization) {
        this.categorization = categorization;
    }

    /**
     * Get the flow categorization
     *
     * @return flow categorization
     * @see #getCategorization()
     */
    @Override
    public Classification getFlowCategorization() {
        return this.getCategorization();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClassificationHierarchyAsStringForCache() {
        if (this.categorization != null) {
            return this.categorization.getClassHierarchyAsString();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<DataStock> getContainingDataStocks() {
        return this.containingDataStocks;
    }

    /**
     * Determine if this is an elementary flow
     *
     * @return <code>true</code>
     */
    public boolean isElementaryFlow() {
        return true;
    }

    /**
     * Determine if this is a product flow
     *
     * @return <code>false</code>
     */
    public boolean isProductFlow() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return statically {@link TypeOfFlowValue#ELEMENTARY_FLOW}
     */
    @Override
    public TypeOfFlowValue getType() {
        return TypeOfFlowValue.ELEMENTARY_FLOW;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addSelfToDataStock(DataStock stock) {
        stock.addElementaryFlow(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeSelfFromDataStock(DataStock stock) {
        stock.removeElementaryFlow(this);
    }


    @Override
    public String getDirPathInZip() {
        return "ILCD/" + DatasetTypes.FLOWS.getValue();
    }

    @Override
    public DataSetDao<?, ?, ?> getCorrespondingDSDao() {
        return new ElementaryFlowDao();
    }

}
