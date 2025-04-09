package de.iai.ilcd.model.lifecyclemodel;

import de.fzk.iai.ilcd.api.binding.generated.common.CommissionerAndGoalType;
import de.fzk.iai.ilcd.api.binding.generated.common.FTMultiLang;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.binding.generated.common.StringMultiLang;
import de.iai.ilcd.model.common.GlobalReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author MK
 * @since soda4LCA 5.7.0
 */

@Entity
@Table(name = "lcm_administrativeinformation_commissionerandgoal")
public class CommissionerAndGoal implements Serializable {

    private static final long serialVersionUID = -5975775752893698104L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CommissionerAndGoal_id")
    protected long id;
    //	 _____________________________
//	 < its a long 'useless' story >
//	  -----------------------------
//	         \   ^__^
//	          \  (oo)\_______
//	             (__)\       )\/\
//	                 ||----w |
//	                 ||     ||
//
    @Column(name = "USELESSCOLUMN", nullable = true)
    protected boolean useless = false;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lcm_commissionerandgoal_referencetocommissioner", joinColumns = @JoinColumn(name = "CommissionerAndGoal_id"), inverseJoinColumns = @JoinColumn(name = "ref_contact_id"))
    protected List<GlobalReference> referenceToCommissioner = new ArrayList<GlobalReference>();
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lcm_commissionerandgoal_project", joinColumns = @JoinColumn(name = "CommissionerAndGoal_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> project = new HashMap<String, String>();
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lcm_commissionerandgoal_intendedapplications", joinColumns = @JoinColumn(name = "CommissionerAndGoal_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> intendedApplications = new HashMap<String, String>();

    public CommissionerAndGoal() {
    }

    public CommissionerAndGoal(CommissionerAndGoalType comm) {
        for (GlobalReferenceType ref : Optional.ofNullable(comm)
                .map(CommissionerAndGoalType::getReferenceToCommissioner).orElse(new ArrayList<>()))
            this.getReferenceToCommissioner().add(new GlobalReference(ref));

        for (StringMultiLang s : Optional.ofNullable(comm).map(CommissionerAndGoalType::getProject)
                .orElse(new ArrayList<>()))
            this.addProject(s);

        for (FTMultiLang s : Optional.ofNullable(comm).map(CommissionerAndGoalType::getIntendedApplications)
                .orElse(new ArrayList<>()))
            this.addIntendedApplications(s);

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<GlobalReference> getReferenceToCommissioner() {
        return referenceToCommissioner;
    }

    public void addReferenceToCommissioner(GlobalReference referenceToCommissioner) {
        this.referenceToCommissioner.add(referenceToCommissioner);
    }

    public Map<String, String> getProject() {
        return project;
    }

    public void addProject(StringMultiLang s) {
        this.project.put(s.getLang(), s.getValue());
    }

    public Map<String, String> getIntendedApplications() {
        return intendedApplications;
    }

    public void addIntendedApplications(FTMultiLang s) {
        this.intendedApplications.put(s.getLang(), s.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(intendedApplications, project, referenceToCommissioner);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CommissionerAndGoal other = (CommissionerAndGoal) obj;
        return Objects.equals(intendedApplications, other.intendedApplications)
                && Objects.equals(project, other.project)
                && Objects.equals(referenceToCommissioner, other.referenceToCommissioner);
    }

    @Override
    public String toString() {
        return "CommissionerAndGoal [id=" + id + ", referenceToCommissioner=" + referenceToCommissioner + ", project="
                + project + ", intendedApplications=" + intendedApplications + "]";
    }
}
