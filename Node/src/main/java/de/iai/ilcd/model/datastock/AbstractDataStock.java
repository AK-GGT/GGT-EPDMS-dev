package de.iai.ilcd.model.datastock;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.Uuid;
import de.iai.ilcd.model.security.Organization;
import de.iai.ilcd.model.tag.Tag;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import de.iai.ilcd.webgui.controller.util.ExportMode;
import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;

import javax.persistence.*;
import java.text.Collator;
import java.util.*;

/**
 * Base implementation of a data stock
 */

/**
 * @author oliver.kusche
 */
@Entity
@Table(name = "datastock", uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "datastock_type", length = 3, discriminatorType = DiscriminatorType.STRING)
public abstract class AbstractDataStock implements IDataStockMetaData, ILongIdObject {

    /**
     * UUID for global identification
     */
    @Embedded
    protected Uuid uuid = new Uuid();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "datastock_id")
    protected List<ExportTag> exportTags = new ArrayList<ExportTag>();
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "display_props_id")
    protected DisplayProperties displayProperties;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "datastock_tag",
            joinColumns = {@JoinColumn(name = "datastock_ID")},
            inverseJoinColumns = {@JoinColumn(name = "tag_ID")})
    protected Set<Tag> tags = new HashSet<Tag>();
    @Transient
    protected boolean empty = false;
    /**
     * Database ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Name (may only contain <code>[A-Za-z0-9_]</code>)
     */
    @Basic
    @Column(name = "name")
    private String name;
    /**
     * Long title
     */
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "datastock_longtitle", joinColumns = @JoinColumn(name = "datastock_id"))
    @MapKeyColumn(name = "lang")
    private Map<String, String> longTitle = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    // Anonyme Providerinstanz die immer aktuelle Referenz zurueckgibt ist noetig, falls sich die referenz durch JPA mal
    // aendert!!
    @Transient
    // wichtig, dass JPA nicht verwirrt wird ;)
    private final MultiLangStringMapAdapter longTitleAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {
        @Override
        public Map<String, String> getMap() {
            return AbstractDataStock.this.longTitle;
        }
    });
    // /**
    // * Description
    // */
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "datastock_description", joinColumns = @JoinColumn(name = "datastock_id"))
    @MapKeyColumn(name = "lang")
    private Map<String, String> description = new HashMap<String, String>();
    @Transient
    private final MultiLangStringMapAdapter descriptionAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return AbstractDataStock.this.description;
        }
    });
    /**
     * Owner organization
     */
    @ManyToOne
    @JoinColumn(name = "owner_organization")
    private Organization ownerOrganization;

    /**
     * Constructor
     */
    public AbstractDataStock() {
    }

    /**
     * Convenience method to determine if stock is a {@link RootDataStock}
     *
     * @return <code>true</code> for {@link RootDataStock}s, <code>false</code> otherwise
     */
    @Override
    public abstract boolean isRoot();

    /**
     * Get the owner organization
     *
     * @return owner organization
     */
    public Organization getOwnerOrganization() {
        return this.ownerOrganization;
    }

    /**
     * Set the owner organization
     *
     * @param owner owner organization to set
     */
    public void setOwnerOrganization(Organization owner) {
        this.ownerOrganization = owner;
    }

    /**
     * Get the database ID
     *
     * @return database ID
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Set the database ID
     *
     * @param id database ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the name of the data stock
     *
     * @return name of the data stock
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Set the name of the data stock
     *
     * @param name name of the data stock to set, (may only contain <code>[A-Za-z0-9_]+</code>)
     * @throws IllegalArgumentException if name contains other characters than <code>[A-Za-z0-9_]+</code> or is <code>null</code>
     */
    public void setName(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException(
                    "Data stock name may contain only letters, numbers and underscores, no white spaces or anything else and must not be null!");
        }
        this.name = name;
    }

    /**
     * For use in UI, where multi-language approach is not convenient.
     *
     * @return title
     */
    public String getTitle() {
        return getLongTitle().getValue(ConfigurationService.INSTANCE.getDefaultLanguage());
    }

    /**
     * For use in UI, where multi-language approach is not convenient.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.longTitle.put(ConfigurationService.INSTANCE.getDefaultLanguage(), title);
    }

    /**
     * Get the long title of the data stock
     *
     * @return long title of the data stock
     */
    // neuer Ruckgabetyp: das interface, nicht mehr die impl ==> hatten wir ja so abgesprochen
    public IMultiLangString getLongTitle() {
        return this.longTitleAdapter; // hier den adapter rausgeben
    }

    /**
     * Set the long title of the data stock
     *
     * @param longTitle long title of the data stock to set
     */
    // Argumenttyp: auch hier interface, nicht impl
    public void setLongTitle(IMultiLangString longTitle) {
        this.longTitleAdapter.overrideValues(longTitle); // Die Werte innerhalb der Map werden damit ueberschrieben
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLongTitleValue() {
        return this.getLongTitle().getValue();
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
     * @param description description to set
     */
    public void setDescription(IMultiLangString description) {
        this.descriptionAdapter.overrideValues(description);
    }

    /**
     * Get the UUID
     *
     * @return the UUID
     */
    public Uuid getUuid() {
        return this.uuid;
    }

    /**
     * Set the UUID
     *
     * @param uuid the UUID to set
     */
    public void setUuid(Uuid uuid) {
        this.uuid = uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUuidAsString() {
        if (this.uuid != null) {
            return this.uuid.getUuid();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractDataStock)) {
            return false;
        }
        AbstractDataStock other = (AbstractDataStock) obj;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(IDataStockMetaData o) {
        return Collator.getInstance().compare(this.getName(), o.getName());
    }

    public List<ExportTag> getExportTags() {
        return this.exportTags;
    }

    public ExportTag getExportTag(ExportType type, ExportMode mode) {
        if (exportTags == null)
            this.exportTags = new ArrayList<ExportTag>();
        if (exportTags.isEmpty()) {
            return createExportTag(type, mode);
        }
        for (ExportTag et : this.exportTags)
            if (type.equals(et.getType()) && mode.equals(et.getMode())) {
                return et;
            }
        return createExportTag(type, mode);
    }

    private ExportTag createExportTag(ExportType type, ExportMode mode) {
        ExportTag newExportTag = new ExportTag();
        newExportTag.setType(type);
        newExportTag.setMode(mode);
        this.exportTags.add(newExportTag);
        return newExportTag;
    }

    public void setExportTag(ExportTag exportTag) {
        for (ExportTag et : this.exportTags)
            if (exportTag.getType().equals(et.getType()) && exportTag.getMode().equals(et.getMode())) {
                et = exportTag;
                return;
            }
        this.exportTags.add(exportTag);
    }

    public List<Tag> getTags() {
        return new ArrayList<Tag>(this.tags);
    }

    public void setTags(List<Tag> tags) {
        this.tags = new HashSet<Tag>(tags);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getDs().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getDs().remove(this);
    }

    public void removeTagByName(Tag tag) {
        for (Tag t : tags) {
            if (tag.getName().equals(t.getName())) {
                tags.remove(t);
                break;
            }
        }
    }

    public DisplayProperties getDisplayProperties() {
        if (displayProperties == null)
            displayProperties = new DisplayProperties();
        return displayProperties;
    }

    public void setDisplayProperties(DisplayProperties displayProperties) {
        this.displayProperties = displayProperties;
    }

    public void setModified(boolean modified) {
        for (ExportTag et : this.exportTags)
            et.setModified(modified);
    }

    public boolean isSetModified() {
        for (ExportTag et : this.exportTags)
            if (!et.isModified())
                return false;

        return true;
    }

}
