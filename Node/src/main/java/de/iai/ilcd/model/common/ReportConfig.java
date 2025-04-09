package de.iai.ilcd.model.common;

/**
 * The object containing all information/configuration of customized reports.
 *
 * @author sarai
 */
public class ReportConfig implements Comparable<ReportConfig> {

    private String type;

    private String name;

    private String function;

    private Integer index;

    private String icon;

    private String fileName;

    private boolean timeStampSuffix = false;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFunction() {
        return this.function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Integer getIndex() {
        return this.index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isTimeStampSuffix() {
        return timeStampSuffix;
    }

    public void setTimeStampSuffix(boolean timeStampSuffix) {
        this.timeStampSuffix = timeStampSuffix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(ReportConfig anotherItem) {
        if ((index != null) && (anotherItem.index != null)) {
            return index.compareTo(anotherItem.index);
        } else if (index != null) {
            return -1;
        } else if (anotherItem.index != null) {
            return 1;
        } else {
            return 0;
        }
    }

}
