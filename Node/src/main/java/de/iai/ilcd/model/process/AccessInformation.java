package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.LicenseTypeValue;
import de.fzk.iai.ilcd.service.model.process.IAccessInformation;
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
@Table(name = "process_accessinformation")
public class AccessInformation implements Serializable, IAccessInformation {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 6587910669693621030L;
    protected boolean copyright = false;
    @Enumerated(EnumType.STRING)
    protected LicenseTypeValue licenseType;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "process_userestrictions", joinColumns = @JoinColumn(name = "process_accessinformation_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> useRestrictions = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter useRestrictionsAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return AccessInformation.this.useRestrictions;
        }
    });
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Get the ID of Access information
     *
     * @return the ID of Access information
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the ID of Access information
     *
     * @param id the ID of Access information to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean isCopyright() {
        return this.copyright;
    }

    public void setCopyright(boolean copyright) {
        this.copyright = copyright;
    }

    @Override
    public LicenseTypeValue getLicenseType() {
        return this.licenseType;
    }

    public void setLicenseType(LicenseTypeValue licenseType) {
        this.licenseType = licenseType;
    }

    @Override
    public IMultiLangString getUseRestrictions() {
        return this.useRestrictionsAdapter;
    }

    public void setUseRestrictions(IMultiLangString useRestrictions) {
        this.useRestrictionsAdapter.overrideValues(useRestrictions);
    }
}
