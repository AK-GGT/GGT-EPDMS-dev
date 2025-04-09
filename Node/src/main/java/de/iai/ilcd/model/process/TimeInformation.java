package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.process.ITimeInformation;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author clemens.duepmeier
 */
@Entity(name = "process_timeinformation")
@Table(name = "process_timeinformation")
public class TimeInformation implements Serializable, ITimeInformation {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 3919984048290994958L;
    protected Integer referenceYear = null;
    protected Integer validUntil = null;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "process_timedescription", joinColumns = @JoinColumn(name = "process_timeinformation_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> description = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter descriptionAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return TimeInformation.this.description;
        }
    });
    /**
     * ID of Time information
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Get the ID of Time information
     *
     * @return the ID of Time information
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the ID of Time information
     *
     * @param id the ID of Time information to set
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

    @Override
    public Integer getReferenceYear() {
        return this.referenceYear;
    }

    public void setReferenceYear(Integer referenceYear) {
        this.referenceYear = referenceYear;
    }

    @Override
    public Integer getValidUntil() {
        return this.validUntil;
    }

    public void setValidUntil(Integer validUntil) {
        this.validUntil = validUntil;
    }
}
