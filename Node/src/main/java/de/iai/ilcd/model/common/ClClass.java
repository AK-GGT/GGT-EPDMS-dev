package de.iai.ilcd.model.common;

import de.fzk.iai.ilcd.api.binding.generated.common.ClassType;
import de.fzk.iai.ilcd.service.model.common.IClass;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Optional;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "clclass")
// @Table(uniqueConstraints={@UniqueConstraint(columnNames={"catName", "catLevel"})})
public class ClClass implements Serializable, IClass {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clName")
    private String name;

    private String clId;

    @Column(name = "clLevel")
    private int level;

    @Embedded
    private Uuid uuid;

    @ManyToOne()
    private Category category;

    @Enumerated(EnumType.STRING)
    private DataSetType dataSetType;

    protected ClClass() {

    }

    public ClClass(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public ClClass(String name, int level, String id) {
        this.name = name;
        this.level = level;
        this.clId = id;
    }

    public ClClass(ClassType cl) {
        this.name = Optional.ofNullable(cl).map(ClassType::getValue).orElse(null);
        this.level = Optional.ofNullable(cl).map(ClassType::getLevel).map(BigInteger::intValue).orElse(null);
        this.clId = Optional.ofNullable(cl).map(ClassType::getClassId).orElse(null);
        // FIXME: Treat classID as UUID?

    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getClId() {
        return this.clId;
    }

    public void setClId(String clId) {
        this.clId = clId;
    }

    @Override
    public Integer getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uuid getUuid() {
        return this.uuid;
    }

    public void setUuid(Uuid uuid) {
        this.uuid = uuid;
    }

    public Category getCategory() {
        return this.category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public DataSetType getDataSetType() {
        return this.dataSetType;
    }

    public void setDataSetType(DataSetType dataSetType) {
        this.dataSetType = dataSetType;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + this.level;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
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
        if (!(obj instanceof ClClass)) {
            return false;
        }
        ClClass other = (ClClass) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.level != other.level) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
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

    @Override
    public String toString() {
        return this.getClass().getName() + "[id=" + this.id + "]";
    }

}
