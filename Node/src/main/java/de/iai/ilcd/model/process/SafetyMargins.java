package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Safety margins
 */
@Embeddable
public class SafetyMargins {

    /**
     * Description
     */
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "process_safetymargins_description", joinColumns = @JoinColumn(name = "process_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> description = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter descriptionAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return SafetyMargins.this.description;
        }
    });
    /**
     * Margins value
     */
    @Basic
    @Column(name = "margins")
    private int margins;

    /**
     * Get the margins
     *
     * @return margins
     */
    public int getMargins() {
        return this.margins;
    }

    /**
     * Set the margins
     *
     * @param margins margins to set
     */
    public void setMargins(int margins) {
        this.margins = margins;
    }

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

}
