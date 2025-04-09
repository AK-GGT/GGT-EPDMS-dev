package de.iai.ilcd.model.process;

import org.eclipse.persistence.annotations.Index;

import javax.persistence.*;

/**
 * Statistics for a classification
 */
@Entity
@Table(name = "process_lciaresult_clclass_statistics")
public class LciaResultClClassStats {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Classification
     */
    @Basic
    @Index
    @Column(name = "clid")
    private String clid;

    /**
     * UUID of the indicator
     */
    @Basic
    @Index
    @Column(name = "method_uuid")
    private String methodUuid;

    /**
     * UUID of the indicator's flow property
     */
    @Basic
    @Index
    @Column(name = "reference_flowproperty_uuid")
    private String referenceFlowpropertyUuid;

    /**
     * Unit cache of the indicator's flow property unit
     */
    @Basic
    @Index
    @Column(name = "ref_unit")
    private String refUnit;

    /**
     * Module
     */
    @Basic
    @Index
    @Column(name = "module")
    private String module;

    /**
     * Minimum value
     */
    @Basic
    @Column(name = "val_min")
    private Double min;

    /**
     * Mean value
     */
    @Basic
    @Column(name = "val_mean")
    private Double mean;

    /**
     * Maximum value
     */
    @Basic
    @Column(name = "val_max")
    private Double max;

    /**
     * Timestamp of calculation
     */
    @Basic
    @Column(name = "ts_calculated")
    private long tsCalculated;

    /**
     * Timestamp of calculation
     */
    @Basic
    @Column(name = "ts_last_change")
    private long tsLastChange;

    /**
     * IDs of all reference data stocks
     */
    @Basic
    @Column(name = "stockIds")
    private String stockIds;

    /**
     * Count of all data sets in reference stocks
     */
    @Basic
    @Column(name = "reference_count")
    private Integer referenceCount;

    /**
     * Get the ID
     *
     * @return ID
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the ID
     *
     * @param id ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the min value
     *
     * @return min value
     */
    public Double getMin() {
        return this.min;
    }

    /**
     * Set the min value
     *
     * @param min value to set
     */
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     * Get the mean value
     *
     * @return mean value
     */
    public Double getMean() {
        return this.mean;
    }

    /**
     * Set the mean value
     *
     * @param mean value to set
     */
    public void setMean(Double mean) {
        this.mean = mean;
    }

    /**
     * Get the max value
     *
     * @return max value
     */
    public Double getMax() {
        return this.max;
    }

    /**
     * Set the max value
     *
     * @param max value to set
     */
    public void setMax(Double max) {
        this.max = max;
    }

    /**
     * Get the time stamp of value calculation
     *
     * @return time stamp of value calculation
     */
    public long getTsCalculated() {
        return this.tsCalculated;
    }

    /**
     * Set the time stamp of value calculation
     *
     * @param tsCalculated time stamp of value calculation to set
     */
    public void setTsCalculated(long tsCalculated) {
        this.tsCalculated = tsCalculated;
    }

    /**
     * Get the time stamp of last value change
     *
     * @return time stamp of last value change
     */
    public long getTsLastChange() {
        return this.tsLastChange;
    }

    /**
     * Set the time stamp of last value change
     *
     * @param tsLastChange time stamp of last value change to set
     */
    public void setTsLastChange(long tsLastChange) {
        this.tsLastChange = tsLastChange;
    }

    /**
     * Set the CLID of the stats entry
     *
     * @return CLID of the stats entry
     */
    public String getClid() {
        return this.clid;
    }

    /**
     * Set the CLID of the stats entry
     *
     * @param clid CLID of the stats entry to set
     */
    public void setClid(String clid) {
        this.clid = clid;
    }

    /**
     * Get the unit group UUID of the stats entry
     *
     * @return unit group UUID of the stats entry
     */
    public String getMethodUuid() {
        return this.methodUuid;
    }

    /**
     * Set the unit group UUID of the stats entry
     *
     * @param ugUuid unit group UUID of the stats entry to set
     */
    public void setMethodUuid(String ugUuid) {
        this.methodUuid = ugUuid;
    }

    /**
     * Get UUID of reference flow property
     *
     * @return UUID of reference flow property
     */
    public String getReferenceFlowpropertyUuid() {
        return referenceFlowpropertyUuid;
    }

    /**
     * Set UUID of reference flow property
     *
     * @param referenceFlowPropertyUuid the UUID of reference flow property
     */
    public void setReferenceFlowpropertyUuid(String referenceFlowPropertyUuid) {
        this.referenceFlowpropertyUuid = referenceFlowPropertyUuid;
    }

    /**
     * Get reference unit of stats entry
     *
     * @return reference unit
     */
    public String getRefUnit() {
        return refUnit;
    }

    /**
     * Set reference unit of stats entry
     *
     * @param referencePropertyUnit_cache the reference unit of stats entry
     */
    public void setRefUnit(String referencePropertyUnit_cache) {
        this.refUnit = referencePropertyUnit_cache;
    }

    /**
     * Get the module of the stats entry
     *
     * @return module of the stats entry
     */
    public String getModule() {
        return this.module;
    }

    /**
     * Set the module of the stats entry
     *
     * @param module module of the stats entry to set
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Get stock ID in stats entry
     *
     * @return the stock ID in stats entry
     */
    public String getStockIds() {
        return stockIds;
    }

    /**
     * Set sock ID in stats entry
     *
     * @param stockIds the stock ID to set
     */
    public void setStockIds(String stockIds) {
        this.stockIds = stockIds;
    }

    /**
     * Get count of reference processes in stats entry
     *
     * @return the count of reference processes in stats entry
     */
    public Integer getReferenceCount() {
        return referenceCount;
    }

    /**
     * Set count of reference processes in stats entry
     *
     * @param referenceCount the count of reference processes to set
     */
    public void setReferenceCount(Integer referenceCount) {
        this.referenceCount = referenceCount;
    }

}
