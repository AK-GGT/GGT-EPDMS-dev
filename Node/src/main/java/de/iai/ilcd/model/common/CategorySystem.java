package de.iai.ilcd.model.common;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "categorysystem")
public class CategorySystem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "csname")
    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Category> categories = new HashSet<Category>();

    @OneToMany(cascade = CascadeType.ALL)
    private Set<CategoryDefinition> categoryDefinitions = new HashSet<CategoryDefinition>();

    @Transient
    private CategoryDefinition currentCategoryDefinition = null;

    protected CategorySystem() {
    }

    public CategorySystem(String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Category> getCategories() {
        return this.categories;
    }

    protected void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public void addCategory(Category cat) {
        if (!this.categories.contains(cat)) {
            this.categories.add(cat);
        }
    }

    public void removeCategory(Category cat) {
        if (this.categories.contains(cat)) {
            this.categories.remove(cat);
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        if (!(obj instanceof CategorySystem)) {
            return false;
        }
        CategorySystem other = (CategorySystem) obj;
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
        return "de.iai.ilcd.model.common.ClassificationSystem[name=" + this.name + "]";
    }

    public Set<CategoryDefinition> getCategoryDefinitions() {
        return categoryDefinitions;
    }

    public void setCategoryDefinitions(Set<CategoryDefinition> categoryDefinitions) {
        this.categoryDefinitions = categoryDefinitions;
    }

    public CategoryDefinition getCurrentCategoryDefinition() {

        // for now, always return the latest as long as the required implicit
        // functionality in the DAO has not been added
        CategoryDefinition cd = null;
        Date importDate = null;

        Iterator<CategoryDefinition> it = this.categoryDefinitions.iterator();

        while (it.hasNext()) {
            CategoryDefinition cd2 = it.next();
            if (importDate == null || cd2.getImportDate().after(importDate)) {
                importDate = cd2.getImportDate();
                cd = cd2;
            }
        }

        return cd;
        // return currentCategoryDefinition;
    }

    public void setCurrentCategoryDefinition(CategoryDefinition currentCategoryDefinition) {
        this.currentCategoryDefinition = currentCategoryDefinition;
    }

}
