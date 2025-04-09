package de.iai.ilcd.model.lifecyclemodel;

import de.fzk.iai.ilcd.api.binding.generated.common.StringMultiLang;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.GroupType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author MK
 * @since soda4LCA 5.7.0
 */

@Entity
@Table(name = "technology_group")
public class TechnologyGroup implements Serializable {

    private static final long serialVersionUID = 7199571444844453126L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "techgroup_key")
    protected long techgroup_key;
    @Column(name = "internalgroup_id")
    protected long internalgroup_id;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "technology_groups_details", joinColumns = @JoinColumn(name = "techgroupkey"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> groupdetails = new HashMap<String, String>();

    public TechnologyGroup() {
    }

    public TechnologyGroup(long internalgroup_id, Map<String, String> groupdetails) {
        super();
        this.internalgroup_id = internalgroup_id;
        this.groupdetails.putAll(groupdetails);
        ;
    }

    public TechnologyGroup(GroupType g) {
        this.internalgroup_id = g.getId().longValue();
        this.addGroupdetails(g.getGroupName());
    }

    public long getTechgroup_key() {
        return techgroup_key;
    }

    public void setTechgroup_key(long techgroup_key) {
        this.techgroup_key = techgroup_key;
    }

    public long getGroupid() {
        return internalgroup_id;
    }

    public void setGroupid(long groupid) {
        this.internalgroup_id = groupid;
    }

    public Map<String, String> getGroupdetails() {
        return groupdetails;
    }

    public void addGroupdetails(Map<String, String> groupdetails) {
        this.groupdetails.putAll(groupdetails);
    }

    public void addGroupdetails(List<StringMultiLang> groupdetails) {
        for (StringMultiLang s : groupdetails)
            this.groupdetails.put(s.getLang(), s.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupdetails, internalgroup_id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TechnologyGroup other = (TechnologyGroup) obj;
        return Objects.equals(groupdetails, other.groupdetails) && internalgroup_id == other.internalgroup_id;
    }

}
