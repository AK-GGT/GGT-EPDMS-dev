package de.iai.ilcd.model.common;

/**
 * The model for Quality check events.
 * These are the elements that will be shown in the result table of the QuantitaticeQaulityAssurance view.
 *
 * @author sarai
 */
public class QualityCheckEvent {

    private String processName;

    private String referenceValue;

    private String processUuid;

    private String version;

    private String referenceUnit;

    private Double absValue;

    private Double minValue;

    private Double maxValue;

    private Double avgValue;

    private String category;

    private String indicatorName;

    private String indicatorUuid;

    private String message;

    private String module;

    private Long deviation;

    private String referenceCount;

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(String referenceValue) {
        this.referenceValue = referenceValue;
    }

    public String getProcessUuid() {
        return processUuid;
    }

    public void setProcessUuid(String processUuid) {
        this.processUuid = processUuid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public String getIndicatorUuid() {
        return indicatorUuid;
    }

    public void setIndicatorUuid(String indicatorUuid) {
        this.indicatorUuid = indicatorUuid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getReferenceUnit() {
        return referenceUnit;
    }

    public void setReferenceUnit(String referenceUnit) {
        this.referenceUnit = referenceUnit;
    }

    public Double getAbsValue() {
        return absValue;
    }

    public void setAbsValue(Double absValue) {
        this.absValue = absValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(Double avgValue) {
        this.avgValue = avgValue;
    }

    public Long getDeviation() {
        return deviation;
    }

    public void setDeviation(Long deviation) {
        this.deviation = deviation;
    }

    public String getReferenceCount() {
        return referenceCount;
    }

    public void setReferenceCount(String referenceCount) {
        this.referenceCount = referenceCount;
    }
}
