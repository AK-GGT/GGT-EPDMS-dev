package de.iai.ilcd.model.common;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.DataSetVO;
import de.fzk.iai.ilcd.service.model.IDataSetVO;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import de.iai.ilcd.webgui.controller.util.ExportMode;
import de.iai.ilcd.xml.zip.ZipArchiveBuilder;
import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author clemens.duepmeier
 */
@MappedSuperclass
public abstract class DataSet implements Serializable, IDataSetVO, ILongIdObject {

    private static final long serialVersionUID = 1L;
    // we use the field branch for optimistic concurrent locking;
    // i.e. collisions of concurrent updates will look for incremented branch numbers
    // branch will be incremented with every update of the data set
    @Version
    protected int branch = 0;
    /**
     * Visibility flag: The UI typically always filters for
     * <code>visible=true</code>.
     * <p>
     * This flag was originally introduced to flag data sets for deletion: Because
     * deletion takes time, users were able to interact with data sets that were
     * already in the process of deletion. Especially attempts to (re-)delete such
     * data sets resulted in misleading errors.
     * </p>
     */
    protected boolean visible = true;
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @MapKeyColumn(name = "lang")
    protected Map<String, String> name = new HashMap<String, String>();
    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    protected final MultiLangStringMapAdapter nameAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return DataSet.this.name;
        }
    });
    @Embedded
    protected Uuid uuid = new Uuid();
    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @MapKeyColumn(name = "lang")
    protected Map<String, String> description = new HashMap<String, String>();
    @Transient
    private final MultiLangStringMapAdapter descriptionAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return DataSet.this.description;
        }
    });
    protected String permanentUri;
    @Embedded
    protected DataSetVersion version;
    @Enumerated(EnumType.STRING)
    protected DataSetState releaseState = DataSetState.UNRELEASED;
    @ManyToMany(fetch = FetchType.LAZY)
    protected Set<Language> supportedLanguages = new HashSet<Language>();
    @Transient
    protected Set<DataSet> dependencies = null;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "XMLFILE_ID")
    XmlFile xmlFile;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // // if it's an remote data set node contains information about the remote network node
    // @Transient
    // private NetworkNode sourceNode=null;
    @Transient
    private String href;
    // we need this, if our data sets will be send to other servers, tools
    @Transient
    private String sourceId = ConfigurationService.INSTANCE.getNodeId();
    /**
     * Classifications
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable
    private List<Classification> classifications = new ArrayList<Classification>();
    @ManyToOne
    @JoinColumn(name = "root_stock_id")
    private RootDataStock rootDataStock;
    @Temporal(TemporalType.TIMESTAMP)
    private Date importDate = null;
    /**
     * Flag to indicate if this is the most recent version of this data set
     */
    @Basic
    private boolean mostRecentVersion;
    /**
     * Cache for the name column - used for filtering &amp; order in queries.
     * 255 character limit should be sufficient.
     */
    @Basic
    @Column(name = "name_cache", length = 255)
    private String nameCache;
    /**
     * Cache for the classification - used for filtering &amp; order in queries.
     * 100 character limit should be sufficient.
     */
    @Basic
    @Column(name = "classification_cache", length = 100)
    private String classificationCache;

    /**
     * Get all datasets that are linked from this dataset
     *
     * @return all datasets that are linked from this dataset
     */
    public Set<DataSet> getDependencies() {
        if (this.dependencies == null) {
            this.dependencies = new HashSet<DataSet>();
            return this.dependencies;
        } else return this.dependencies;
    }

    public void setDependencies(Set<DataSet> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Get all data stocks that this data set is contained in
     *
     * @return data stocks that this data set is contained in
     */
    public abstract Set<DataStock> getContainingDataStocks();

    /**
     * Get all data stocks that this data set is contained in as one String
     *
     * @return data stocks that this data set is contained in as one String
     */
    public String getContainingDataStocksAsString() {
        if (this.getContainingDataStocks() != null) {
            StringBuilder builder = new StringBuilder();
            for (DataStock stock : this.getContainingDataStocks()) {
                if (builder.length() != 0) {
                    builder.append(";");
                }
                builder.append(stock.getName());
            }
            return builder.toString();
        } else
            return null;
    }

    public List<DataStock> getContainingDataStocksSorted() {
        return new ArrayList<>(new TreeSet<>(getContainingDataStocks()));
    }

    /**
     * Add the data set to the data stock. Only do the <code>addXXXX</code> call,
     * everything else is done be {@link #addToDataStock(DataStock)}.
     *
     * @param stock stock to add to
     */
    protected abstract void addSelfToDataStock(DataStock stock);

    /**
     * Remove the data set from the data stock. Only do the <code>removeXXXX</code> call,
     * everything else is done be {@link #removeFromDataStock(DataStock)}.
     *
     * @param stock stock to add to
     */
    protected abstract void removeSelfFromDataStock(DataStock stock);

    /**
     * Get the type of this data set
     *
     * @return type of this data set
     */
    public abstract DataSetType getDataSetType();

    /**
     * Add to data stock
     *
     * @param stock stock to add to
     */
    public final boolean addToDataStock(DataStock stock) {
        final Set<DataStock> containingDataStocks = this.getContainingDataStocks();
        if (!containingDataStocks.contains(stock)) {
            containingDataStocks.add(stock);
            this.addSelfToDataStock(stock);
            return true;
        } else
            return false;
    }

    /**
     * Remove from data stock
     *
     * @param stock stock to remove from
     */
    public final boolean removeFromDataStock(DataStock stock) {
        final Set<DataStock> containingDataStocks = this.getContainingDataStocks();
        if (containingDataStocks.contains(stock)) {
            containingDataStocks.remove(stock);
            this.removeSelfFromDataStock(stock);
            return true;
        } else
            return false;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the root data stock
     *
     * @return the root data stock
     */
    public RootDataStock getRootDataStock() {
        return this.rootDataStock;
    }

    /**
     * Set the root data stock
     *
     * @param rootDataStock the root data stock to set
     */
    public void setRootDataStock(RootDataStock rootDataStock) {
        this.rootDataStock = rootDataStock;
    }

    // public NetworkNode getSourceNode() {
    // return sourceNode;
    // }
    //
    // public void setSourceNode(NetworkNode sourceNode) {
    // this.sourceNode = sourceNode;
    // }

    public String getRootDataStockId() {
        return this.rootDataStock.getUuidAsString();
    }

    /**
     * Write the {@link #getName() name} default value
     * to the cache field.
     */
    @PrePersist
    protected void applyDataSetCache() {
        this.nameCache = StringUtils.substring(this.getNameAsStringForCache(), 0, 255);
        if (CollectionUtils.isNotEmpty(this.classifications)) {
            this.classificationCache = StringUtils.substring(this.getClassificationHierarchyAsStringForCache(), 0, 100);
        } else {
            this.classificationCache = null;
        }

    }

    public int getBranch() {
        return this.branch;
    }

    public void setBranch(int branch) {
        this.branch = branch;
    }

    /**
     * Get the hierarchy of classification as string. May be overridden by
     * sub-classes if {@link #getClassification()} shall not represent
     * the hierarchy.
     *
     * @return delegates to {@link #getClassification()}.{@link Classification#getClassHierarchyAsString()
     * getClassHierarchyAsString()}
     */
    protected String getClassificationHierarchyAsStringForCache() {
        final Classification classification = this.getClassification();
        if (classification != null) {
            return classification.getClassHierarchyAsString();
        }
        return null;
    }

    /**
     * Get the name as string. May be overridden by sub-classes if {@link #getName()} shall not represent the name.
     *
     * @return delegates to {@link #getName()}.{@link MultiLanguageString#getDefaultValue() getDefaultValue()}
     */
    protected String getNameAsStringForCache() {
        if (this.name != null) {
            return this.nameAdapter.getDefaultValue();
        }
        return null;
    }

    /**
     * Determine if this is the most recent version of this data set
     *
     * @return <code>true</code> if this is the most recent version of this data set, else <code>false</code>
     */
    public boolean isMostRecentVersion() {
        return this.mostRecentVersion;
    }

    /**
     * Set the flag to indicate if this data set is the most recent version of the data set
     *
     * @param mostRecentVersion flag to set
     */
    public void setMostRecentVersion(boolean mostRecentVersion) {
        this.mostRecentVersion = mostRecentVersion;
    }

    /**
     * Get classification
     *
     * @param classificationSystem classification system
     * @return classification for classification system
     */
    @Override
    public Classification getClassification(String classificationSystem) {
        for (Classification c : this.classifications) {
            if (StringUtils.equalsIgnoreCase(c.getName(), classificationSystem)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Get all classifications
     *
     * @return classifications
     */
    @Override
    public List<Classification> getClassifications() {
        return this.classifications;
    }

    /**
     * Add a classification
     *
     * @param c classification to add
     */
    public void addClassification(Classification c) {
        if (!this.classifications.contains(c)) {
            this.classifications.add(c);
        }
    }

    /**
     * Get the default classification
     */
    @Override
    public Classification getClassification() {
        Classification tmp = null;
        if (CollectionUtils.isNotEmpty(this.classifications)) {
            tmp = this.getClassification(ConfigurationService.INSTANCE.getDefaultClassificationSystem());
            if (tmp == null) {
                tmp = this.getClassification("");
                if (tmp == null) {
                    tmp = (Classification) CollectionUtils.get(this.classifications, 0);
                }
            }
        }
        return tmp;
    }

    // @Override
    // public MultiLanguageText getDescription() {
    // return this.description;
    // }
    //
    // public void setDescription( MultiLanguageText description ) {
    // this.description = description;
    // }

    @Override
    public IMultiLangString getDescription() {
        return this.descriptionAdapter;
    }

    public void setDescription(IMultiLangString description) {
        this.descriptionAdapter.overrideValues(description);
    }

    @Override
    public IMultiLangString getName() {
        return this.nameAdapter;
    }

    public void setName(IMultiLangString name) {
        this.nameAdapter.overrideValues(name);
    }

    @Override
    public String getDefaultName() {
        return this.getName().getDefaultValue();
    }

    public Uuid getUuid() {
        return this.uuid;
    }

    public void setUuid(Uuid uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getUuidAsString() {
        return this.uuid.getUuid();
    }

    @Override
    public String getDataSetVersion() {
        return this.version != null ? this.version.getVersionString() : null;
    }

    public DataSetVersion getVersion() {
        return this.version;
    }

    public void setVersion(DataSetVersion version) {
        this.version = version;
    }

    @Override
    public String getPermanentUri() {
        return this.permanentUri;
    }

    public void setPermanentUri(String permanentUri) {
        this.permanentUri = permanentUri;
    }

    public DataSetState getReleaseState() {
        return this.releaseState;
    }

    public void setReleaseState(DataSetState releaseState) {
        this.releaseState = releaseState;
    }

    public XmlFile getXmlFile() {
        return this.xmlFile;
    }

    public void setXmlFile(XmlFile xmlFile) {
        this.xmlFile = xmlFile;
    }

    @Override
    public String getHref() {
        // @TODO - should be generated using the default host name specified in configuration
        // the access part for the dataset type and the UUID value
        return this.href;
    }

    @Override
    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public String getSourceId() {
        return this.sourceId;
    }

    @Override
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // object is null or no DataSet
        if (!(object instanceof DataSet)) {
            return false;
        }

        // check if same "subclass"
        if (!this.getClass().equals(object.getClass())) {
            return false;
        }

        DataSet other = (DataSet) object;
        // compare id, if set
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }

        // compare UUID, if set
        if ((this.uuid == null && other.uuid != null) || (this.uuid != null && !this.uuid.equals(other.uuid))) {
            return false;
        }

        // compare version, if set
        if ((this.version == null && other.version != null) || (this.version != null && !this.version.equals(other.version))) {
            return false;
        }

        // same class, same id, same UUID, same version [same means also: both null]
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getClass().getName() + "[id=" + this.id + "]";
    }

    public Set<Language> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(Set<Language> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    public List<String> getLanguages() {
        List<String> languages = new ArrayList<String>();
        for (Language l : this.supportedLanguages)
            languages.add(l.getLanguageCode());
        return languages;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    @Override
    public List<DataSetVO> getDuplicates() {
        return new ArrayList<DataSetVO>();
    }

    @Override
    public void setDuplicates(List<DataSetVO> list) {
    }


    /**
     * Eliminate the need for giant switch cases in the DataExportController by
     * implementing pseudo-Command Design pattern
     *
     * @author MK
     * @since soda4LCA 5.4.0
     */

    public abstract String getDirPathInZip();

    public abstract DataSetDao<?, ?, ?> getCorrespondingDSDao();

    /**
     * <p>
     * Super method every class that extend <code>DataSet</code> has the same
     * routine in order to dump XML files into a zip archive.
     * </p>
     *
     * <p>
     * The only special case at the time of writing is <code>Source</code>. It has
     * extra implementation to handle the digital files attached to it.
     * </p>
     *
     * <p>
     * Please provide the datasets in descending order of their version.
     * </p>
     *
     * @param zip to dump files into. assuming it has been initialized. this method
     *            will <b>NOT</b> close/flush the given zip after finish writing.
     * @see Source#writeInZip(ZipArchiveBuilder, ExportMode)
     */
    public void writeInZip(ZipArchiveBuilder zip, ExportMode em) {
        // ExportMode.ALL
//		String path = String.join("/",
//				this.getDirPathInZip(),
//				this.getUuid() + "_" + this.getVersion() + ".xml");

        String path = String.join("/",
                this.getDirPathInZip(),
                this.generateXMLname(zip, em));

        // adds file but skips already existing entries based on full path
        zip.addCompressed(this.getXmlFile().getCompressedContent(), path);
    }

    /**
     * <p>
     * Depending on the given export mode, the output xml name differs.
     * </p>
     * <p>
     * This methods works on the assumption that the zip archive skips adding
     * entries with duplicate names.
     *
     * @param zip that your are intending to write into. this method doesn't write
     *            but uses {@link ZipArchiveBuilder#containsEntry} to check for
     *            exisitng entries
     * @param em
     * @return full path of where to write the dataset in the zip archive.
     * @see ExportMode
     * @see ZipArchiveBuilder#containsEntry(String)
     */

    public String generateXMLname(ZipArchiveBuilder zip, ExportMode em) {
        String UUID = this.getUuidAsString(), ver = this.getDataSetVersion(), fileName;
        switch (em) {
            case ALL:
                fileName = UUID + "_" + ver + ".xml";
                break;

            case LATEST_ONLY:
                fileName = UUID + ".xml";
                if (!(this instanceof de.iai.ilcd.model.process.Process)
                        && zip.containsEntry(this.getDirPathInZip() + fileName))
                    fileName = UUID + "_" + ver + ".xml";
                break;

            case LATEST_ONLY_GLOBAL:
                fileName = UUID + ".xml";
                break;

            default:
                fileName = UUID + "_" + ver + ".xml";
        }
        return fileName;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
