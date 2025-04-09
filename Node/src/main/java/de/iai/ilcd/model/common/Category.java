package de.iai.ilcd.model.common;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "category")
// @Table(uniqueConstraints={@UniqueConstraint(columnNames={"catName", "catLevel"})})
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String catName;

    private String catId;

    private int catLevel;

    @Embedded
    private Uuid uuid;

    @Enumerated(EnumType.STRING)
    private DataSetType dataSetType;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCatId() {
        return this.catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public int getCatLevel() {
        return this.catLevel;
    }

    public void setCatLevel(int catLevel) {
        this.catLevel = catLevel;
    }

    public String getCatName() {
        return this.catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public DataSetType getDataSetType() {
        return this.dataSetType;
    }

    public void setDataSetType(DataSetType dataSetType) {
        this.dataSetType = dataSetType;
    }

    public Uuid getUuid() {
        return this.uuid;
    }

    public void setUuid(Uuid uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.common.Category[id=" + this.id + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.uuid == null) ? 0 : this.uuid.hashCode());
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
        if (!(obj instanceof Category)) {
            return false;
        }
        Category other = (Category) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!this.uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

}
