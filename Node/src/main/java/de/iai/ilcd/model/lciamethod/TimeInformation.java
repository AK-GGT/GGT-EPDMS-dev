package de.iai.ilcd.model.lciamethod;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.lciamethod.ITimeInformation;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Time information for {@link LCIAMethod LCIA methods}
 */
@Embeddable
public class TimeInformation implements ITimeInformation, Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -8055792015884241376L;

    /**
     * Duration information
     */
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lciamethod_ti_durationdescription", joinColumns = @JoinColumn(name = "lciamethod_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> duration = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter durationAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return TimeInformation.this.duration;
        }
    });

    /**
     * Reference year information
     */
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "lciamethod_ti_referenceyeardescription", joinColumns = @JoinColumn(name = "lciamethod_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> referenceYear = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter referenceYearAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return TimeInformation.this.referenceYear;
        }
    });

    /**
     * Get the duration information
     *
     * @return duration information
     */
    @Override
    public IMultiLangString getDuration() {
        return this.durationAdapter;
    }

    /**
     * Set the duration information
     *
     * @param duration the new duration information
     */
    public void setDuration(IMultiLangString duration) {
        this.durationAdapter.overrideValues(duration);
    }

    /**
     * Get the reference year information
     *
     * @return reference year information
     */
    @Override
    public IMultiLangString getReferenceYear() {
        return this.referenceYearAdapter;
    }

    /**
     * Set the reference year information
     *
     * @param referenceYear the new reference year information
     */
    public void setReferenceYear(IMultiLangString referenceYear) {
        this.referenceYearAdapter.overrideValues(referenceYear);
    }

}
