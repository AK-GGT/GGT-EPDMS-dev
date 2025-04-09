package de.iai.ilcd.model.flow;

import javax.persistence.*;

/**
 * Material property
 */
@Entity
@Table(name = "flow_product_material_property")
public class MaterialProperty {

    /**
     * Property definition ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The definition of the property
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_flow_id")
    private ProductFlow productFlow;

    /**
     * The definition of the property
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "definition_id")
    private MaterialPropertyDefinition definition;

    /**
     * Value
     */
    @Basic
    @Column(name = "value")
    private double value;

    /**
     * Get the definition
     *
     * @return definition
     */
    public MaterialPropertyDefinition getDefinition() {
        return this.definition;
    }

    /**
     * Set the definition
     *
     * @param definition definition to set
     */
    public void setDefinition(MaterialPropertyDefinition definition) {
        this.definition = definition;
    }

    /**
     * Get the value
     *
     * @return value
     */
    public double getValue() {
        return this.value;
    }

    /**
     * Set the value
     *
     * @param value value to set
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Get the ID
     *
     * @return ID
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the ID
     *
     * @param id ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the product flow
     *
     * @return product flow
     */
    public ProductFlow getProductFlow() {
        return this.productFlow;
    }

    /**
     * Set the product flow
     *
     * @param productFlow product flow to set
     */
    public void setProductFlow(ProductFlow productFlow) {
        if (this.productFlow != null) {
            this.productFlow.getMaterialProperties().remove(this);
        }
        this.productFlow = productFlow;
        if (productFlow != null) {
            productFlow.addMaterialProperty(this);
        }
    }

}
