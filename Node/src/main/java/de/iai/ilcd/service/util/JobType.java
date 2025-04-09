package de.iai.ilcd.service.util;

/**
 * This enumeration represents the different types a job can be of.
 *
 * @author sarai
 */
public enum JobType {
    PUSH("push"), EXPORT("export"), ATTACH("assign"), DETACH("remove"), DELETE("delete"), OTHER("other");

    /*
     * The value of job type
     */
    private String value;

    /**
     * Initializes job tape with given value.
     *
     * @param value The value of job state
     */
    JobType(String value) {
        this.value = value;
    }

    /**
     * Gets the job type of given value String.
     *
     * @param value The value of job type
     * @return The job type of given value
     */
    public static JobType fromValue(String value) {
        for (JobType enumValue : JobType.values()) {
            if (enumValue.getValue().equals(value))
                return enumValue;
        }
        return null;
    }

    /**
     * Gets value of given job type.
     *
     * @return The value of given job type
     */
    public String getValue() {
        return value;
    }

}
