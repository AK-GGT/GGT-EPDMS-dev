package de.iai.ilcd.model.common;

import de.fzk.iai.ilcd.api.binding.generated.common.ClassType;
import de.fzk.iai.ilcd.api.binding.generated.common.ClassificationType;
import de.fzk.iai.ilcd.service.model.common.IClass;
import de.fzk.iai.ilcd.service.model.common.IClassification;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "classification")
public class Classification implements Serializable, IClassification {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clname")
    private String name;

    @ManyToOne
    @JoinColumn(name = "catSystem")
    private CategorySystem categorySystem;

    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("level")
    private List<ClClass> classes = new ArrayList<ClClass>();

    public Classification() {

    }

    public Classification(String name) {
        this.name = name;
    }

    public Classification(IClassification second) {
        this.name = second.getName();
        for (IClass iclass : second.getIClassList()) {
            ClClass clClass = new ClClass(iclass.getName(), iclass.getLevel());
            this.classes.add(clClass);
        }
    }

    public Classification(ClassificationType ct) {
        this.name = Optional.ofNullable(ct).map(ClassificationType::getName).orElse("ilcd");
        for (ClassType cl : Optional.ofNullable(ct).map(ClassificationType::getClazz)
                .orElse(new ArrayList<ClassType>()))
            this.addClass(new ClClass(cl));
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CategorySystem getCategorySystem() {
        return this.categorySystem;
    }

    public void setCategorySystem(CategorySystem categorySystem) {
        this.categorySystem = categorySystem;
    }

    public List<ClClass> getClasses() {
        return this.classes;
    }

    protected void setClasses(List<ClClass> classes) {
        this.classes = classes;
    }

    @Override
    public List<IClass> getIClassList() {
        List<IClass> iClasses = new ArrayList<IClass>();
        for (IClass iClass : this.classes) {
            iClasses.add(iClass);
        }
        return iClasses;
    }

    public void addClass(ClClass clClass) {
        if (!this.classes.contains(clClass)) {
            this.classes.add(clClass);
        }
    }

    public void removeClass(ClClass clClass) {
        if (this.classes.contains(clClass)) {
            this.classes.remove(clClass);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getClassHierarchyAsString() {
        return getClassHierarchyAsString(false);
    }

    public String getClassHierarchyAsString(boolean withId) {
        StringBuilder buffer = new StringBuilder();

        int numberOfClasses = this.classes.size();
        int i = 0;
        for (ClClass clClass : this.classes) {
            if (withId)
                buffer.append(clClass.getClId()).append(" ");
            buffer.append(clClass.getName());
            if (i++ < numberOfClasses - 1) {
                buffer.append(" / ");
            }
        }
        return buffer.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.categorySystem == null) ? 0 : this.categorySystem.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
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
        if (!(obj instanceof Classification)) {
            return false;
        }
        Classification other = (Classification) obj;
        if (this.categorySystem == null) {
            if (other.categorySystem != null) {
                return false;
            }
        } else if (!this.categorySystem.equals(other.categorySystem)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.common.Classification[id=" + this.id + "]";
    }

}
