package de.iai.ilcd.model.datastock;

import de.iai.ilcd.webgui.controller.util.ExportMode;

/**
 * Utility class to store {@link DataStock} meta data in order to prevent having a list of all data stocks where
 * accidentally lazy loading of one or more lists
 * of data sets (40k flows on ELCD max) was triggered.
 */
public interface IDataStockMetaData extends Comparable<IDataStockMetaData> {

    /**
     * ID of default data stock
     */
    public final static long DEFAULT_DATASTOCK_ID = 1L;

    /**
     * Get the ID of the represented stock
     *
     * @return the id of the represented stock
     */
    public Long getId();

    /**
     * Get the name of the represented stock
     *
     * @return the name of the represented stock
     */
    public String getName();

    /**
     * Get the long title of the represented stock
     *
     * @return the long title of the represented stock
     */
    public String getLongTitleValue();

    /**
     * Determine if stock is a root data stock
     *
     * @return <code>true</code> if stock is a root data stock, <code>false</code> otherwise
     */
    public boolean isRoot();

    /**
     * Get the UUID of the data stock as string
     *
     * @return UUID of the data stock as string
     */
    public String getUuidAsString();

    /**
     * Get the ExportTag of the data stock for the specified export type and mode
     *
     * @return the ExportTag
     */
    public ExportTag getExportTag(ExportType type, ExportMode mode);

    /**
     * Get the DisplayProperties of the data stock
     *
     * @return the DisplayProperties
     */
    public DisplayProperties getDisplayProperties();


}