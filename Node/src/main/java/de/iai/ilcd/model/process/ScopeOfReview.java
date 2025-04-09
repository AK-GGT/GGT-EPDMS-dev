package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.service.model.enums.MethodOfReviewValue;
import de.fzk.iai.ilcd.service.model.enums.ScopeOfReviewValue;
import de.fzk.iai.ilcd.service.model.process.IScope;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "scopeofreview")
public class ScopeOfReview implements Serializable, IScope {

    private static final long serialVersionUID = 1L;
    @Enumerated(EnumType.STRING)
    protected ScopeOfReviewValue name;
    @ElementCollection()
    @CollectionTable(name = "review_methods", joinColumns = @JoinColumn(name = "scopeofreview_id"))
    @Column(name = "method")
    @Enumerated(EnumType.STRING)
    Set<MethodOfReviewValue> methods = new HashSet<MethodOfReviewValue>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected ScopeOfReview() {

    }

    public ScopeOfReview(ScopeOfReviewValue name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Set<MethodOfReviewValue> getMethods() {
        return methods;
    }

    protected void setMethods(Set<MethodOfReviewValue> methods) {
        this.methods = methods;
    }

    public void addMethod(MethodOfReviewValue method) {
        if (!methods.contains(method))
            methods.add(method);
    }

    @Override
    public ScopeOfReviewValue getName() {
        return name;
    }

    public void setName(ScopeOfReviewValue name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((methods == null) ? 0 : methods.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScopeOfReview other = (ScopeOfReview) obj;
        if (methods == null) {
            if (other.methods != null)
                return false;
        } else if (!methods.equals(other.methods))
            return false;
        if (name != other.name)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.process.ScopeOfReview[name=" + name + "]";
    }

}
