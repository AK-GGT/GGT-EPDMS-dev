package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "scenario")
public class Scenario {

    /**
     * Description
     */
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "scenario_description", joinColumns = @JoinColumn(name = "scenario_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> description = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter descriptionAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Scenario.this.description;
        }
    });
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "group_")
    private String group;
    @Basic
    @Column(name = "default_")
    private Boolean ddefault;

    /**
     * Get the description
     *
     * @return description
     */
    public IMultiLangString getDescription() {
        return this.descriptionAdapter;
    }

    /**
     * Set the description
     *
     * @param mls description to set
     */
    public void setDescription(IMultiLangString mls) {
        this.descriptionAdapter.overrideValues(mls);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Boolean getDefault() {
        return ddefault;
    }

    public void setDefault(Boolean ddefault) {
        this.ddefault = ddefault;
    }

}
