package de.iai.ilcd.model.flow;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.common.IClassification;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.ProductFlowDao;
import de.iai.ilcd.model.datastock.DataStock;

import javax.persistence.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static de.iai.ilcd.model.flow.ProductFlow.TABLE_NAME;

/**
 * Models a flow of type "product", "waste" or "other"
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue(value = "p")
@Table(name = TABLE_NAME)
@AssociationOverrides({
        @AssociationOverride(name = "classifications", joinTable = @JoinTable(name = "flow_product_classifications"), joinColumns = @JoinColumn(name = "pflow_id")),
        @AssociationOverride(name = "propertyDescriptions", joinTable = @JoinTable(name = "flow_product_propertydescriptions")),
        @AssociationOverride(name = "synonyms", joinTable = @JoinTable(name = "flow_product_synonyms"), joinColumns = @JoinColumn(name = "pflow_id")),
        @AssociationOverride(name = "description", joinTable = @JoinTable(name = "flow_product_description"), joinColumns = @JoinColumn(name = "pflow_id")),
        @AssociationOverride(name = "name", joinTable = @JoinTable(name = "flow_product_name"), joinColumns = @JoinColumn(name = "pflow_id"))})
public class ProductFlow extends Flow {

    public static final String TABLE_NAME = "flow_product";

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -2256171450431294913L;
    /**
     * The data stocks this flow is contained in
     */
    @ManyToMany(mappedBy = "productFlows", fetch = FetchType.LAZY)
    protected Set<DataStock> containingDataStocks = new HashSet<DataStock>();
    /**
     * The material properties
     */
    @OneToMany(mappedBy = "productFlow", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    protected Set<MaterialProperty> materialProperties = new HashSet<MaterialProperty>();
    /**
     * Flag to indicate if this is a specific product
     */
    @Basic
    @Column(name = "specific_product")
    private boolean specificProduct = false;
    /**
     * Reference to vendor
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vendor_reference")
    private GlobalReference vendorReference;
    /**
     * Reference to source
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "source_reference")
    private GlobalReference sourceReference;
    /**
     * Type of the flow
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TypeOfFlowValue type;
    /**
     * &quot;is a&quot; reference
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "is_a_reference")
    private GlobalReference isAReference;

    /**
     * Determine if this is a specific product
     *
     * @return <code>true</code> for specific products, <code>false</code> otherwise
     */
    public boolean isSpecificProduct() {
        return this.specificProduct;
    }

    /**
     * Set specific product flag
     *
     * @param specificProduct specific product flag
     */
    public void setSpecificProduct(boolean specificProduct) {
        this.specificProduct = specificProduct;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<DataStock> getContainingDataStocks() {
        return this.containingDataStocks;
    }

    /**
     * {@inheritDoc}
     *
     * @return type of flow
     */
    @Override
    public TypeOfFlowValue getType() {
        return this.type;
    }

    /**
     * Set the type of flow
     *
     * @param type flow type
     * @throws IllegalStateException if type is {@link TypeOfFlowValue#ELEMENTARY_FLOW elementary flow}
     */
    public void setType(TypeOfFlowValue type) {
        if (TypeOfFlowValue.ELEMENTARY_FLOW.equals(type)) {
            throw new IllegalStateException(this.getClass().getName() + " cannot be of type " + type.toString());
        }
        this.type = type;
    }

    /**
     * Get all &quot;is a&quot; reference
     *
     * @return all &quot;is a&quot; reference
     */
    public GlobalReference getIsAReference() {
        return this.isAReference;
    }

    /**
     * Set &quot;is a&quot; reference
     *
     * @param isAReference &quot;is a&quot; reference to set
     */
    public void setIsAReference(GlobalReference isAReference) {
        this.isAReference = isAReference;
    }

    /**
     * Determine if &quot;is a&quot; reference is present
     *
     * @return <code>true</code> if <code>{@link #getIsAReference()} != null</code>, <code>false</code> otherwise
     */
    public boolean hasIsAReference() {
        return this.isAReference != null;
    }

    /**
     * Determine if this is an elementary flow
     *
     * @return <code>false</code>
     */
    public boolean isElementaryFlow() {
        return false;
    }

    /**
     * Determine if this is a product flow
     *
     * @return <code>true</code>
     */
    public boolean isProductFlow() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return statically <code>null</code> (only {@link ElementaryFlow elementary flows} have a categorization)
     */
    @Override
    public IClassification getFlowCategorization() {
        return null;
    }

    /**
     * Get the vendor reference
     *
     * @return vendor reference
     */
    public GlobalReference getVendorReference() {
        return this.vendorReference;
    }

    /**
     * Set the vendor reference
     *
     * @param vendorReference vendor reference to set
     */
    public void setVendorReference(GlobalReference vendorReference) {
        this.vendorReference = vendorReference;
    }

    /**
     * Get the source reference
     *
     * @return source reference
     */
    public GlobalReference getSourceReference() {
        return this.sourceReference;
    }

    /**
     * Set the source reference
     *
     * @param sourceReference source reference to set
     */
    public void setSourceReference(GlobalReference sourceReference) {
        this.sourceReference = sourceReference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addSelfToDataStock(DataStock stock) {
        stock.addProductFlow(this);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeSelfFromDataStock(DataStock stock) {
        stock.removeProductFlow(this);
    }

    /**
     * Get the material properties
     *
     * @return the material properties
     */
    public Set<MaterialProperty> getMaterialProperties() {
        return this.materialProperties;
    }

    /**
     * Add a material property
     *
     * @param property property to add
     */
    public void addMaterialProperty(MaterialProperty property) {
        if (!this.materialProperties.contains(property)) {
            this.materialProperties.add(property);
            property.setProductFlow(this);
        }
    }

    @Override
    public String getDirPathInZip() {
        return "ILCD/" + DatasetTypes.FLOWS.getValue();
    }

    @Override
    public DataSetDao<?, ?, ?> getCorrespondingDSDao() {
        return new ProductFlowDao();
    }
}
