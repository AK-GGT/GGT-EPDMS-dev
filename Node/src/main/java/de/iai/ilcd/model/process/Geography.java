package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "process_geography")
public class Geography implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 1488250906328293747L;
    protected String location;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "process_locationrestriction", joinColumns = @JoinColumn(name = "process_geography_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> description = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter descriptionAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Geography.this.description;
        }
    });
    /**
     * ID of Geography
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Get the ID of Geography
     *
     * @return the ID of Geography
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the ID of Geography
     *
     * @param id the ID of Geography to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    public IMultiLangString getDescription() {
        return this.descriptionAdapter;
    }

    public void setDescription(IMultiLangString description) {
        this.descriptionAdapter.overrideValues(description);
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
