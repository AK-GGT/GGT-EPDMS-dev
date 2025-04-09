package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.common.JobMetaData;
import de.iai.ilcd.model.dao.JobMetaDataDao;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.util.PrimefacesUtil;
import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The job list handler for managing {@link JobMetaData} list
 *
 * @author sarai
 */
@Component
@Scope("view")
public class JobsHandler extends AbstractHandler {

    /*
     * The unique ID of serial version
     */
    private static final long serialVersionUID = 7489739875966195376L;
    /*
     * The logger for logging
     */
    private static Logger log = LogManager.getLogger(JobsHandler.class);
    /*
     * The Data Access Object (DAO) for getting {@link JobMetaData}
     */
    private JobMetaDataDao jobMetaDataDao = new JobMetaDataDao();
    /**
     * Lazy model
     */
    private LazyDataModel<JobMetaData> lazyModel = new JobLazyDataModel();

    /**
     * Toggle the autoupdate feature of the jobs table
     */
    private boolean autoUpdate = false;

    /**
     * Load count of elements
     *
     * @return the count of elements
     */
    protected long loadElementCount() {
        return this.jobMetaDataDao.getAllCount();
    }

    /**
     * Do everything here that depends on injected beans
     */
    protected void postConstruct() {
    }

    /**
     * Do the lazy loading
     *
     * @param first     first index
     * @param pageSize  page size
     * @param sortField sort field
     * @param sortOrder sort oder
     * @param filters   filters
     * @return loaded data
     */
    public List<JobMetaData> lazyLoad(int first, int pageSize, String sortField, SortOrder sortOrder,
                                      Map<String, Object> filters) {
        List<JobMetaData> jobsList;
        if (sortOrder == SortOrder.DESCENDING) {
            jobsList = this.jobMetaDataDao.get(first, pageSize);
        } else {
            // We need to count backwards..
            jobsList = getLazyDecrementalJobList(first, pageSize);
        }
        return jobsList;
    }

    /**
     * Method to hack the dao into providing a lazy job meta data list,
     * that's sorted from recent to old.
     *
     * @param UIRowNumber
     * @param pageSize
     * @return
     */
    private List<JobMetaData> getLazyDecrementalJobList(int UIRowNumber, int pageSize) {
        List<JobMetaData> result;

        long rowCount = this.jobMetaDataDao.getAllCount();
        if (rowCount < UIRowNumber) //Hopefully the UI is more responsible than that, but let's be careful anyway.
            return null;

        //Index 'magic' to find the correct subset of jobs needed for the lazy model.
        long theoreticalIndex = rowCount - UIRowNumber - pageSize; //This (possibly below zero) number increments to (rowCount - first), alias it increments to the first entry from the back that's redundant.
        long firstDBRowToBeConsidered = Math.max(0, theoreticalIndex); //Obviously we just care for positive values to start a query with.
        long securePageSize = Math.min(pageSize, pageSize + theoreticalIndex); //This actually only does anything when the theoretical index is below zero AND the provided pageSize was not already adjusted.

        //Fetch the jobs' meta data and sort from recent to old.
        result = this.jobMetaDataDao.get((int) firstDBRowToBeConsidered, (int) securePageSize);
        Collections.reverse(result);

        return result;
    }

    /**
     * Reload count for model
     */
    protected final void reloadCount() {
        long rowCount = this.loadElementCount();
        this.lazyModel.setRowCount((int) rowCount);
    }

    /**
     * Called after dependency injection
     */
    @PostConstruct
    public final void init() {
        this.postConstruct();
        this.reloadCount();
    }

    /**
     * Get the lazy model
     *
     * @return lazy model
     */
    public final LazyDataModel<JobMetaData> getLazyModel() {
        return this.lazyModel;
    }

    /**
     * Set the lazy model
     *
     * @param lazyModel model to set
     */
    public final void setLazyModel(LazyDataModel<JobMetaData> lazyModel) {
        this.lazyModel = lazyModel;
    }

    /**
     * Converts all newline commands in given String into html
     * <code><br/></code> tags to invoke new lines in web page.
     *
     * @param lines The given String of which newline command shall be converted
     * @return A String containing html <code><br/></code> tags instead of
     * newline command
     */
    public String convertLines(String lines) {
        return lines.replace("\n", "<br/>");
    }

    /**
     * Converts the given run time into String of format hh:mm:ss,millis
     *
     * @param jobRunTime The run time that shall be formatted
     * @return A formatted String of run time
     */
    public String formatRunTime(Long jobRunTime) {
        PeriodFormatter pf = new PeriodFormatterBuilder().printZeroAlways().minimumPrintedDigits(2).appendHours()
                .appendSuffix(":").appendMinutes().appendSuffix(":").appendSeconds().toFormatter();
        Duration duration = new Duration(jobRunTime);
        Period period = duration.toPeriod();

        return pf.print(period);
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    /**
     * Lazy model
     */
    public class JobLazyDataModel extends LazyDataModel<JobMetaData> {

        /**
         * serial ID
         */
        private static final long serialVersionUID = -36355927090061383L;

        /**
         * {@inheritDoc}
         */
        @Override
        public List<JobMetaData> load(int first, int pageSize, Map<String, SortMeta> sortMeta, Map<String, FilterMeta> filterMeta) {

            // Normalize for legacy signature in lazyLoad method
            String sortField = (String) PrimefacesUtil.getLegacySortData(sortMeta).get("sortField");
            SortOrder sortOrder = (SortOrder) PrimefacesUtil.getLegacySortData(sortMeta).get("sortOrder");
            Map<String, Object> filters = PrimefacesUtil.getLegacyFilterData(filterMeta);

            if (JobsHandler.log.isDebugEnabled()) {
                JobsHandler.log.debug("Loading the lazy data between {0} and {1} " + sortField,
                        new Object[]{first, (first + pageSize)});
                for (String key : filters.keySet())
                    JobsHandler.log.debug(key + " : " + filters.get(key));
            }
            List<JobMetaData> lazyData = JobsHandler.this.lazyLoad(first, pageSize, sortField, sortOrder, filters);

            long rowCount = JobsHandler.this.loadElementCount();
            this.setRowCount((int) rowCount);
            return lazyData;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JobMetaData getRowData(String rowKey) {
            List<JobMetaData> l = (List<JobMetaData>) getWrappedData();

            for (JobMetaData o : l) {
                if (((ILongIdObject) o).getId().toString().equals(rowKey))
                    return (JobMetaData) o;
            }

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getRowKey(JobMetaData t) {
            return ((ILongIdObject) t).getId();
        }
    }

}
