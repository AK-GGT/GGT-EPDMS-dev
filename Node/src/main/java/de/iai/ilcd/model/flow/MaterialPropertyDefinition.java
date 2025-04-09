package de.iai.ilcd.model.flow;

import javax.persistence.*;

/**
 * Definition for a material property
 */
@Entity
@Table(name = "flow_product_material_property_definition")
public class MaterialPropertyDefinition {

    /**
     * Property definition ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Name
     */
    @Basic
    @Column(name = "name", length = 255)
    private String name;

    /**
     * Unit
     */
    @Basic
    @Column(name = "unit", length = 255)
    private String unit;

    /**
     * Unit description
     */
    @Lob
    @Basic
    @Column(name = "unitDescription")
    private String unitDescription;

    /**
     * Get the name
     *
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name
     *
     * @param name name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the unit
     *
     * @return unit
     */
    public String getUnit() {
        return this.unit;
    }

    /**
     * Set the unit
     *
     * @param unit unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Get the unit description
     *
     * @return unit description
     */
    public String getUnitDescription() {
        return this.unitDescription;
    }

    /**
     * Set the unit description
     *
     * @param unitDescription unit description to set
     */
    public void setUnitDescription(String unitDescription) {
        this.unitDescription = unitDescription;
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

    @Override
    public String toString() {
        return "MaterialPropertyDefinition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", unitDescription='" + unitDescription + '\'' +
                '}';
    }
}
