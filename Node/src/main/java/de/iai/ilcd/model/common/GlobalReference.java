package de.iai.ilcd.model.common;

import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceTypeValues;
import de.fzk.iai.ilcd.api.binding.generated.common.STMultiLang;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "globalreference")
public class GlobalReference implements Serializable, IGlobalReference {

    private static final Logger LOGGER = LogManager.getLogger(GlobalReference.class);

    private static final long serialVersionUID = 1L;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "globalreference_shortdescription", joinColumns = @JoinColumn(name = "globalreference_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> shortDescription = new HashMap<>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter shortDescriptionAdapter = new MultiLangStringMapAdapter(
            () -> GlobalReference.this.shortDescription);
    DataSetVersion version;
    String uri;
    // URL to objects will be generated automatically
    @Transient
    String href = null;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // private MultiLanguageText shortDescription = new MultiLanguageText();
    @ElementCollection
    @CollectionTable(name = "globalreference_subreferences", joinColumns = @JoinColumn(name = "globalreference_id"))
    private List<String> subReferences = new LinkedList<>();

    @Enumerated(EnumType.STRING)
    private GlobalReferenceTypeValue type;

    @Embedded
    private Uuid uuid;

    public GlobalReference() {

    }

    public GlobalReference(GlobalReferenceType grt) {
        this.getSubReferences().addAll(
                Optional.ofNullable(grt).map(GlobalReferenceType::getSubReference).orElse(new ArrayList<>()));

        this.type = Optional.ofNullable(grt).map(GlobalReferenceType::getType)
                .map(GlobalReferenceTypeValues::getValue)
                .map(GlobalReferenceTypeValue::fromValue)
                .orElse(null);

        Optional.ofNullable(grt).map(GlobalReferenceType::getRefObjectId).ifPresent(this::setRefObjectId);

        this.version = Optional.ofNullable(grt).map(GlobalReferenceType::getVersion).map(v -> {
            try {
                return DataSetVersion.parse(v);
            } catch (FormatException e) {
                e.printStackTrace();
                LOGGER.warn("Invalid DataSetVersion Number");
                return null;
            }
        }).orElse(null);

        this.uri = Optional.ofNullable(grt).map(GlobalReferenceType::getUri).orElse(null);
        if (this.uri != null)
            this.setHref(this.uri);

        this.setShortDescription(Optional.ofNullable(grt).map(GlobalReferenceType::getShortDescription)
                .map(GlobalReference::adaptMultiLang).orElse(null));

    }

    public GlobalReference(it.jrc.lca.ilcd.common.GlobalReferenceType grt) {
        this.getSubReferences().addAll(
                Optional.ofNullable(grt).map(it.jrc.lca.ilcd.common.GlobalReferenceType::getSubReferences).orElse(new ArrayList<>()));

        this.type = Optional.ofNullable(grt).map(it.jrc.lca.ilcd.common.GlobalReferenceType::getType)
                .map(it.jrc.lca.ilcd.common.GlobalReferenceTypeValues::value)
                .map(GlobalReferenceTypeValue::fromValue)
                .orElse(null);

        Optional.ofNullable(grt).map(it.jrc.lca.ilcd.common.GlobalReferenceType::getRefObjectId).ifPresent(this::setRefObjectId);

        this.version = Optional.ofNullable(grt).map(it.jrc.lca.ilcd.common.GlobalReferenceType::getVersion).map(v -> {
            try {
                return DataSetVersion.parse(v);
            } catch (FormatException e) {
                e.printStackTrace();
                LOGGER.warn("Invalid DataSetVersion Number");
                return null;
            }
        }).orElse(null);

        this.uri = Optional.ofNullable(grt).map(it.jrc.lca.ilcd.common.GlobalReferenceType::getUri).orElse(null);
        if (this.uri != null)
            this.setHref(this.uri);

        Map<String, String> rawShortDescriptions = null;
        if (grt != null) {
            List<it.jrc.lca.ilcd.common.STMultiLang> sts = grt.getShortDescriptions();
            if (grt.getShortDescriptions() != null && !grt.getShortDescriptions().isEmpty()) {
                rawShortDescriptions = new HashMap<>();
                for (it.jrc.lca.ilcd.common.STMultiLang st : sts)
                    rawShortDescriptions.put(st.getLang(), st.getValue());
            }
        }
        this.setShortDescription(new MultiLangStringMapAdapter(rawShortDescriptions));

    }

    private static IMultiLangString adaptMultiLang(List<STMultiLang> ls) {
        Map<String, String> erd = new HashMap<>();
        if (ls != null)
            for (STMultiLang s : ls)
                erd.put(s.getLang(), s.getValue());
        return new MultiLangStringMapAdapter(erd);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public GlobalReferenceTypeValue getType() {
        return this.type;
    }

    @Override
    public void setType(GlobalReferenceTypeValue refType) {
        this.type = refType;
    }

    @Override
    public IMultiLangString getShortDescription() {
        return this.shortDescriptionAdapter;
    }

    public void setShortDescription(IMultiLangString shortDescription) {
        this.shortDescriptionAdapter.overrideValues(shortDescription);
    }

    public List<String> getSubReferences() {
        return this.subReferences;
    }

    protected void setSubReferences(List<String> subReferences) {
        this.subReferences = subReferences;
    }

    public void addSubReference(String subReference) {
        this.subReferences.add(subReference);
    }

    @Override
    public String getUri() {
        return this.uri;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }

    public Uuid getUuid() {
        if (this.uuid != null) {
            return this.uuid;
        }
        Uuid uuidFromUri = this.getUuidFromUri();
        return uuidFromUri;
    }

    public void setUuid(Uuid uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getRefObjectId() {
        if (this.uuid != null) {
            return this.uuid.getUuid();
        }
        Uuid uuidFromUri = this.getUuidFromUri();
        if (uuidFromUri != null) {
            return uuidFromUri.getUuid();
        }
        return null;
    }

    @Override
    public void setRefObjectId(String value) {
        this.uuid = new Uuid(value);
    }

    private Uuid getUuidFromUri() {
        if (this.uri == null) {
            return null;
        }
        try {
            GlobalRefUriAnalyzer analyzer = new GlobalRefUriAnalyzer(this.uri);
            return analyzer.getUuid();
        } catch (Exception e) {
            // we do nothing here
        }
        return null;
    }

    public DataSetVersion getVersion() {
        return this.version;
    }

    public void setVersion(DataSetVersion version) {
        this.version = version;
    }

    @Override
    public void setVersion(String versionString) {
        DataSetVersion newVersion = null;
        try {
            newVersion = DataSetVersion.parse(versionString);
        } catch (FormatException ex) {
            // we do nothing here
        }
        if (newVersion != null) {
            this.version = newVersion;
        }
    }

    @Override
    public String getVersionAsString() {
        if (this.version != null) {
            return this.version.getVersionString();
        }
        return null;
    }

    @Override
    public String getHref() {
        // @TODO: include HREF generation code here later
        return this.href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public String toString() {
        return "GlobalReference [id=" + id + ", shortDescription=" + shortDescription + ", shortDescriptionAdapter="
                + shortDescriptionAdapter + ", subReferences=" + subReferences + ", type=" + type + ", uuid=" + uuid
                + ", version=" + version + ", uri=" + uri + ", href=" + href + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.uri == null) ? 0 : this.uri.hashCode());
        result = prime * result + ((this.uuid == null) ? 0 : this.uuid.hashCode());
        result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GlobalReference)) {
            return false;
        }
        GlobalReference other = (GlobalReference) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!this.uri.equals(other.uri)) {
            return false;
        }
        if (this.uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!this.uuid.equals(other.uuid)) {
            return false;
        }
        if (this.version == null) {
            return other.version == null;

        } else
            return this.version.equals(other.version);
    }

}
