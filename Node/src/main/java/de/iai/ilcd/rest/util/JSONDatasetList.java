package de.iai.ilcd.rest.util;

import de.fzk.iai.ilcd.service.model.IDataSetListVO;

import java.util.List;

/**
 * Wrapper for PrimeUI Data set list
 */
public class JSONDatasetList {

    /**
     * The actual data sets
     */
    private final List<? extends IDataSetListVO> dataSets;

    /**
     * Page size
     */
    private final int pageSize;

    /**
     * Start index
     */
    private final int startIndex;

    /**
     * Total count of elements
     */
    private final int totalCount;

    /**
     * Create the wrapper
     *
     * @param dataSets   the data sets
     * @param pageSize   pageSize
     * @param startIndex startIndex
     * @param totalCount totalCount
     */
    public JSONDatasetList(List<? extends IDataSetListVO> dataSets, int pageSize, int startIndex, int totalCount) {
        super();
        this.dataSets = dataSets;
        this.pageSize = pageSize;
        this.startIndex = startIndex;
        this.totalCount = totalCount;
    }

    /**
     * Get the data sets
     *
     * @return data sets
     */
    public List<? extends IDataSetListVO> getDataSets() {
        return this.dataSets;
    }

    /**
     * Get the data sets as array
     *
     * @return data sets as array
     */
    public IDataSetListVO[] getDataSetsAsArray() {
        IDataSetListVO[] arr = new IDataSetListVO[0];
        if (this.dataSets != null) {
            return this.dataSets.toArray(arr);
        }
        return arr;
    }

    /**
     * Get the page size
     *
     * @return page size
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * Get the start index
     *
     * @return start index
     */
    public int getStartIndex() {
        return this.startIndex;
    }

    /**
     * Get the total count
     *
     * @return total count
     */
    public int getTotalCount() {
        return this.totalCount;
    }

}
