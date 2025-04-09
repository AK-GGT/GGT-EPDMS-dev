package de.iai.ilcd.model.datastock;

import de.iai.ilcd.webgui.controller.util.ExportMode;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to store {@link DataStock} meta data in order to prevent having a list of all data stocks where
 * accidentally lazy loading of one or more lists
 * of data sets (40k flows on ELCD max) was triggered.
 */
public final class DataStockMetaData implements IDataStockMetaData, Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -7566700077543772015L;

    /**
     * ID of the data stock
     */
    private final Long id;

    /**
     * Name of the data stock
     */
    private final String name;

    /**
     * Long title of the data stock
     */
    private final String longTitle;

    /**
     * Is root data stock flag
     */
    private final boolean root;

    /**
     * UUID of the data stock
     */
    private final String uuid;

    /**
     * export tag
     */
    private List<ExportTag> exportTags = new ArrayList<>();

    private DisplayProperties displayProperties;

    private Class<? extends AbstractDataStock> dataStockClass;

    /**
     * Create a meta data storage object for data stocks
     *
     * @param ads data stock to store meta data from
     */
    public DataStockMetaData(AbstractDataStock ads) {
        this.id = ads.getId();
        this.name = ads.getName();
        this.longTitle = ads.getLongTitle().getDefaultValue();
        this.root = ads.isRoot();
        this.uuid = ads.getUuidAsString();
        this.exportTags.addAll(ads.getExportTags());
        this.displayProperties = ads.getDisplayProperties();
        this.dataStockClass = ads.getClass();
    }

    public Class<? extends AbstractDataStock> getDataStockClass() {
        return dataStockClass;
    }

    /**
     * Get the ID of the represented stock
     *
     * @return the id of the represented stock
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Get the name of the represented stock
     *
     * @return the name of the represented stock
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Get the long title of the represented stock
     *
     * @return the long title of the represented stock
     */
    @Override
    public String getLongTitleValue() {
        return this.longTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUuidAsString() {
        return this.uuid;
    }

    /**
     * Determine if stock is a root data stock
     *
     * @return <code>true</code> if stock is a root data stock, <code>false</code> otherwise
     */
    @Override
    public boolean isRoot() {
        return this.root;
    }

    /**
     * Get the ExportTag of the data stock
     *
     * @return the ExportTag
     */
    public ExportTag getExportTag(ExportType type, ExportMode mode) {
        if (exportTags == null)
            this.exportTags = new ArrayList<>();
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

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(IDataStockMetaData o) {
        return Collator.getInstance().compare(this.getName(), o.getName());
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
        result = prime * result + (this.root ? 1231 : 1237);
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
        if (!(obj instanceof DataStockMetaData)) {
            return false;
        }
        DataStockMetaData other = (DataStockMetaData) obj;
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
        return this.root == other.root;
    }

    public DisplayProperties getDisplayProperties() {
        return displayProperties;
    }

    public void setDisplayProperties(DisplayProperties displayProperties) {
        this.displayProperties = displayProperties;
    }

}