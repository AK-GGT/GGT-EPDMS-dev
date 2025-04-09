package de.iai.ilcd.util;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.datastock.DataStockMetaData;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.webgui.util.PrimefacesUtil;
import org.primefaces.model.*;

import java.util.List;
import java.util.Map;

/**
 * Data Model with lazy support and selection feature
 *
 * @param <T> type of data set
 */
public abstract class DataSetSelectableDataModel<T extends DataSet> extends LazyDataModel<T> implements SelectableDataModel<T> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -4940798106696383971L;
    /**
     * Loader for content
     */
    private final IDataSetLoader<T> loader;
    /**
     * Data stock filter (may be <code>null</code>)
     */
    private DataStockMetaData dataStock;
    /**
     * Delete button visibility flag
     */
    private boolean showDeleteButton = false;
    /**
     * Selected items
     */
    private T[] selected;
    private boolean mostRecentVersionOnly = false;

    /**
     * Create the model
     *
     * @param loader loader for data
     */
    public DataSetSelectableDataModel(IDataSetLoader<T> loader) {
        this.loader = loader;
        this.loadRowCount();
    }

    public boolean isMostRecentVersionOnly() {
        return mostRecentVersionOnly;
    }

    public void setMostRecentVersionOnly(boolean mostRecentVersionOnly) {
        this.mostRecentVersionOnly = mostRecentVersionOnly;
    }

    /**
     * Checks whether selection is disabled.
     *
     * @return A flag indicating whether selection is disabled
     */
    public boolean isShowDeleteButton() {
        return showDeleteButton;
    }

    /**
     * Disable the visibility of delete button (meaning: {@link #isNothingSelected()} will always return <code>true</code>)
     *
     * @param showDeleteButton flag to set visibility of delete button
     */
    public void setShowDeleteButton(boolean showDeleteButton) {
        this.showDeleteButton = showDeleteButton;
    }

    /**
     * Get the data stock filter
     *
     * @return data stock filter
     */
    public DataStockMetaData getDataStock() {
        return this.dataStock;
    }

    /**
     * Set the data stock filter
     *
     * @param dataStock filter to set, may be <code>null</code>
     */
    public void setDataStock(DataStockMetaData dataStock) {
        this.dataStock = dataStock;
    }

    /**
     * Get selected, tunnel with typed delegate model because generic type will be reduced to {@link Object}
     *
     * @return selected items
     */
    public T[] getSelected() {
        return this.selected;
    }

    /**
     * Set selected, tunnel with typed delegate model because generic type will be reduced to {@link Object}
     *
     * @param selected selected items
     */
    public void setSelected(T[] selected) {
        this.selected = selected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortMeta, Map<String, FilterMeta> filterMeta) {

        // Normalize for legacy signature in lazyLoad method
        String sortField = (String) PrimefacesUtil.getLegacySortData(sortMeta).get("sortField");
        SortOrder sortOrder = (SortOrder) PrimefacesUtil.getLegacySortData(sortMeta).get("sortOrder");
        Map<String, Object> filters = PrimefacesUtil.getLegacyFilterData(filterMeta);

        this.loadRowCount(filters);
        return this.loader.load(first, pageSize, sortField, sortOrder, filters, this.dataStock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRowIndex(int rowIndex) {
        // workaround, see http://code.google.com/p/primefaces/issues/detail?id=1544
        if (this.getPageSize() != 0) {
            super.setRowIndex(rowIndex);
        }
    }

    /**
     * Load row count
     */
    private void loadRowCount() {
        this.setRowCount((int) this.loader.loadCount(this.dataStock, null));
    }

    /**
     * Load row count
     */
    private void loadRowCount(Map<String, Object> filters) {
        this.setRowCount((int) this.loader.loadCount(this.dataStock, filters));
    }

    /**
     * Determine if nothing is selected
     *
     * @return <code>true</code> if noting is selected, <code>false</code> otherwise
     */
    public boolean isNothingSelected() {
        return this.showDeleteButton || (this.selected == null || this.selected.length == 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPageSize() {
        return super.getPageSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPageSize(int pageSize) {
        super.setPageSize(pageSize);
    }

    public int loadTotalElementCount() {
        return this.getRowCount();
    }

    @Override
    public T getRowData(String rowKey) {

        List<T> l = (List<T>) getWrappedData();

        for (T o : l) {
            if (o.getId().toString().equals(rowKey))
                return (T) o;
        }

        return null;
    }

    @Override
    public Object getRowKey(T t) {
        return t.getId();
    }

    /**
     * Loader for lazy data for data sets
     *
     * @param <T> type of data set
     */
    public interface IDataSetLoader<T extends DataSet> {

        /**
         * Load the lazy data
         *
         * @param first     first index
         * @param pageSize  max count of elements to load
         * @param sortField field to sort by
         * @param sortOrder sort order
         * @param filters   filters for search etc.
         * @param dsMeta    data stock meta data filter (may be <code>null</code>!)
         * @return list with lazy loaded data
         */
        public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters, IDataStockMetaData dsMeta);

        /**
         * Load count of elements
         *
         * @param dsMeta data stock meta data filter (may be <code>null</code>!)
         * @return count of elements
         */
        public long loadCount(IDataStockMetaData dsMeta, Map<String, Object> filters);
    }

    /**
     * Data model with selection capabilities for {@link Process}
     */
    public static class ProcessSelectableDataModel extends DataSetSelectableDataModel<Process> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = -5338129763233588799L;

        /**
         * Create the model
         *
         * @param loader data loader
         */
        public ProcessSelectableDataModel(IDataSetLoader<Process> loader) {
            super(loader);
        }

        /**
         * Get the selected items
         *
         * @return selected items
         */
        public Process[] getSelectedItems() {
            return this.getSelected();
        }

        /**
         * Set the selected items
         *
         * @param items selected items
         */
        public void setSelectedItems(Process[] items) {
            super.setSelected(items);
        }

    }

    /**
     * Data model with selection capabilities for {@link LCIAMethod}
     */
    public static class LCIAMethodSelectableDataModel extends DataSetSelectableDataModel<LCIAMethod> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = -25866843798800048L;

        /**
         * Create the model
         *
         * @param loader data loader
         */
        public LCIAMethodSelectableDataModel(IDataSetLoader<LCIAMethod> loader) {
            super(loader);
        }

        /**
         * Get the selected items
         *
         * @return selected items
         */
        public LCIAMethod[] getSelectedItems() {
            return this.getSelected();
        }

        /**
         * Set the selected items
         *
         * @param items selected items
         */
        public void setSelectedItems(LCIAMethod[] items) {
            super.setSelected(items);
        }

    }

    /**
     * Data model with selection capabilities for {@link ProductFlow}
     */
    public static class ProductFlowSelectableDataModel extends DataSetSelectableDataModel<ProductFlow> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = 3383790123810846611L;

        /**
         * Create the model
         *
         * @param loader data loader
         */
        public ProductFlowSelectableDataModel(IDataSetLoader<ProductFlow> loader) {
            super(loader);
        }

        /**
         * Get the selected items
         *
         * @return selected items
         */
        public ProductFlow[] getSelectedItems() {
            return this.getSelected();
        }

        /**
         * Set the selected items
         *
         * @param items selected items
         */
        public void setSelectedItems(ProductFlow[] items) {
            super.setSelected(items);
        }

    }

    /**
     * Data model with selection capabilities for {@link ElementaryFlow}
     */
    public static class ElementaryFlowSelectableDataModel extends DataSetSelectableDataModel<ElementaryFlow> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = 3383790123810846611L;

        /**
         * Create the model
         *
         * @param loader data loader
         */
        public ElementaryFlowSelectableDataModel(IDataSetLoader<ElementaryFlow> loader) {
            super(loader);
        }

        /**
         * Get the selected items
         *
         * @return selected items
         */
        public ElementaryFlow[] getSelectedItems() {
            return this.getSelected();
        }

        /**
         * Set the selected items
         *
         * @param items selected items
         */
        public void setSelectedItems(ElementaryFlow[] items) {
            super.setSelected(items);
        }

    }

    /**
     * Data model with selection capabilities for {@link FlowProperty}
     */
    public static class FlowPropertySelectableDataModel extends DataSetSelectableDataModel<FlowProperty> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = -3455733938391343362L;

        /**
         * Create the model
         *
         * @param loader data loader
         */
        public FlowPropertySelectableDataModel(IDataSetLoader<FlowProperty> loader) {
            super(loader);
        }

        /**
         * Get the selected items
         *
         * @return selected items
         */
        public FlowProperty[] getSelectedItems() {
            return this.getSelected();
        }

        /**
         * Set the selected items
         *
         * @param items selected items
         */
        public void setSelectedItems(FlowProperty[] items) {
            super.setSelected(items);
        }

    }

    /**
     * Data model with selection capabilities for {@link UnitGroup}
     */
    public static class UnitGroupSelectableDataModel extends DataSetSelectableDataModel<UnitGroup> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = 8009685951105561608L;

        /**
         * Create the model
         *
         * @param loader data loader
         */
        public UnitGroupSelectableDataModel(IDataSetLoader<UnitGroup> loader) {
            super(loader);
        }

        /**
         * Get the selected items
         *
         * @return selected items
         */
        public UnitGroup[] getSelectedItems() {
            return this.getSelected();
        }

        /**
         * Set the selected items
         *
         * @param items selected items
         */
        public void setSelectedItems(UnitGroup[] items) {
            super.setSelected(items);
        }

    }

    /**
     * Data model with selection capabilities for {@link Contact}
     */
    public static class ContactSelectableDataModel extends DataSetSelectableDataModel<Contact> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = -2551320755608301233L;

        /**
         * Create the model
         *
         * @param loader data loader
         */
        public ContactSelectableDataModel(IDataSetLoader<Contact> loader) {
            super(loader);
        }

        /**
         * Get the selected items
         *
         * @return selected items
         */
        public Contact[] getSelectedItems() {
            return this.getSelected();
        }

        /**
         * Set the selected items
         *
         * @param items selected items
         */
        public void setSelectedItems(Contact[] items) {
            super.setSelected(items);
        }

    }

    /**
     * Data model with selection capabilities for {@link Source}
     */
    public static class SourceSelectableDataModel extends DataSetSelectableDataModel<Source> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = -3986950833614527784L;

        /**
         * Create the model
         *
         * @param loader data loader
         */
        public SourceSelectableDataModel(IDataSetLoader<Source> loader) {
            super(loader);
        }

        /**
         * Get the selected items
         *
         * @return selected items
         */
        public Source[] getSelectedItems() {
            return this.getSelected();
        }

        /**
         * Set the selected items
         *
         * @param items selected items
         */
        public void setSelectedItems(Source[] items) {
            super.setSelected(items);
        }

    }

    /**
     * Data model with selection capabilities for {@link LifeCycleModel}
     */
    public static class LifeCycleModelSelectableDataModel extends DataSetSelectableDataModel<LifeCycleModel> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = -3986950833614527784L;

        /**
         * Create the model
         *
         * @param loader data loader
         */
        public LifeCycleModelSelectableDataModel(IDataSetLoader<LifeCycleModel> loader) {
            super(loader);
        }

        /**
         * Get the selected items
         *
         * @return selected items
         */
        public LifeCycleModel[] getSelectedItems() {
            return this.getSelected();
        }

        /**
         * Set the selected items
         *
         * @param items selected items
         */
        public void setSelectedItems(LifeCycleModel[] items) {
            super.setSelected(items);
        }

    }
}
