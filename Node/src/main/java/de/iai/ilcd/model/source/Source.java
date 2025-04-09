package de.iai.ilcd.model.source;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.ISourceVO;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.fzk.iai.ilcd.service.model.enums.PublicationTypeValue;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.DigitalFile;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import de.iai.ilcd.webgui.controller.util.ExportMode;
import de.iai.ilcd.xml.zip.ZipArchiveBuilder;

import javax.persistence.*;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static de.iai.ilcd.model.source.Source.TABLE_NAME;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = {"UUID", "MAJORVERSION", "MINORVERSION", "SUBMINORVERSION"}))
@AssociationOverrides({
        @AssociationOverride(name = "classifications", joinTable = @JoinTable(name = "source_classifications"), joinColumns = @JoinColumn(name = "source_id")),
        @AssociationOverride(name = "description", joinTable = @JoinTable(name = "source_description"), joinColumns = @JoinColumn(name = "source_id")),
        @AssociationOverride(name = "name", joinTable = @JoinTable(name = "source_name"), joinColumns = @JoinColumn(name = "source_id"))})
public class Source extends DataSet implements Serializable, ISourceVO {

    public static final String TABLE_NAME = "source";

    /**
     *
     */
    private static final long serialVersionUID = 7897576329330145460L;

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "source_shortName", joinColumns = @JoinColumn(name = "source_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> shortName = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter shortNameAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Source.this.shortName;
        }
    });

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "source_citation", joinColumns = @JoinColumn(name = "source_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> citation = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter citationAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return Source.this.citation;
        }
    });

    @Enumerated(EnumType.STRING)
    protected PublicationTypeValue publicationType;
    /**
     * The data stocks this source is contained in
     */
    @ManyToMany(mappedBy = "sources", fetch = FetchType.LAZY)
    protected Set<DataStock> containingDataStocks = new HashSet<DataStock>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "source")
    Set<DigitalFile> files = new HashSet<DigitalFile>();
    @OneToMany(cascade = CascadeType.ALL)
    Set<GlobalReference> contacts = new HashSet<GlobalReference>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<DataStock> getContainingDataStocks() {
        return this.containingDataStocks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addSelfToDataStock(DataStock stock) {
        stock.addSource(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeSelfFromDataStock(DataStock stock) {
        stock.removeSource(this);
    }

    @Override
    public IMultiLangString getCitation() {
        return this.citationAdapter;
    }

    public void setCitation(IMultiLangString citation) {
        this.citationAdapter.overrideValues(citation);
    }

    @Override
    public PublicationTypeValue getPublicationType() {
        return this.publicationType;
    }

    public void setPublicationType(PublicationTypeValue publicationType) {
        this.publicationType = publicationType;
    }

    public IMultiLangString getShortName() {
        return this.shortNameAdapter;
    }

    public void setShortName(IMultiLangString shortName) {
        this.shortNameAdapter.overrideValues(shortName);
    }

    public Set<GlobalReference> getContacts() {
        return this.contacts;
    }

    protected void setContacts(Set<GlobalReference> contacts) {
        this.contacts = contacts;
    }

    /**
     * Convenience method for returning global references as List in order to user p:dataList (primefaces)
     *
     * @return List of global references
     */
    public List<GlobalReference> getContactsAsList() {
        return new ArrayList<GlobalReference>(this.getContacts());
    }

    @Override
    public List<IGlobalReference> getBelongsTo() {
        List<IGlobalReference> belongs = new ArrayList<IGlobalReference>();
        if (this.contacts == null) {
            this.contacts = new HashSet<GlobalReference>();
        }
        belongs.addAll(this.contacts);

        return belongs;
    }

    public void addContact(GlobalReference contact) {
        if (!this.contacts.contains(contact)) {
            this.contacts.add(contact);
        }
    }

    public Set<DigitalFile> getFiles() {
        return this.files;
    }

    protected void setFiles(Set<DigitalFile> files) {
        this.files = files;
    }

    /**
     * Convenience method for returning files as List in order to user p:dataList (primefaces)
     *
     * @return List of files
     */
    public List<DigitalFile> getFilesAsList() {
        return new ArrayList<DigitalFile>(this.getFiles());
    }

    @Override
    public List<IGlobalReference> getFileReferences() {
        List<IGlobalReference> fileRefs = new ArrayList<IGlobalReference>();

        for (DigitalFile file : this.files) {
            GlobalReference fileRef = new GlobalReference();
            fileRef.setType(GlobalReferenceTypeValue.OTHER_EXTERNAL_FILE);
            fileRef.setShortDescription(new MultiLangStringMapAdapter(file.getFileName()));
            fileRef.setRefObjectId(this.getUuidAsString());
            // @TODO : provide correct HREF attribute, maybe other too

            // add to list
            fileRefs.add(fileRef);
        }

        return fileRefs;
    }

    public void addFile(DigitalFile file) {
        if (!this.files.contains(file)) {
            this.files.add(file);
            file.setSource(this);
        }
    }

    public String getFilesDirectory() {
        String basePath = ConfigurationService.INSTANCE.getDigitalFileBasePath();
        return basePath + "/" + String.valueOf(this.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetType getDataSetType() {
        return DataSetType.SOURCE;
    }

    @Override
    public String getDirPathInZip() {
        return "ILCD/" + DatasetTypes.SOURCES.getValue();
    }

    @Override
    public DataSetDao<?, ?, ?> getCorrespondingDSDao() {
        return new SourceDao();
    }

    @Override
    public void writeInZip(ZipArchiveBuilder zip, ExportMode em) {
        super.writeInZip(zip, em); // dump source itself

        // dump digital files related to this source
        String externalDocsPathPrefix = String.join("/", "ILCD", "external_docs");
        for (DigitalFile file : this.getFiles()) {
            // skip if it's a URL
            if (file.getFileName() == null || file.getFileName().trim().toLowerCase().startsWith("https://") || file.getFileName().trim().toLowerCase().startsWith("http://"))
                continue;
            Path digitalFilePath = Paths.get(file.getAbsoluteFileName());
            if (Files.exists(digitalFilePath)) {
                String dst = String.join("/", externalDocsPathPrefix, digitalFilePath.getFileName().toString());
                zip.add(digitalFilePath.toFile(), dst);
            }
        }
    }
}
