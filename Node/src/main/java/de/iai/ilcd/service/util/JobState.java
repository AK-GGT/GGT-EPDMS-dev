package de.iai.ilcd.service.util;

import de.iai.ilcd.service.task.TaskStatus;

/**
 * This enum represents the states a job can have.
 *
 * @author sarai
 */
public enum JobState {
    WAITING("waiting"), RUNNING("running"), ERROR("error"), COMPLETE("complete"), NONE("none"), PROCESSING("processing"), INCOMPLETE("incomplete"), CANCELED("canceled");

    private String value;

    /**
     * Initializes job state with given value
     *
     * @param value The value of job state
     */
    JobState(String value) {
        this.value = value;
    }

    /**
     * Gets job state from its value name.
     *
     * @param value the value of job state
     * @return the corresponding job state or null
     */
    public static JobState fromValue(String value) {
        for (JobState enumValue : JobState.values()) {
            if (enumValue.getValue().equals(value))
                return enumValue;
        }
        return null;
    }

    public static JobState fromTaskStatus(TaskStatus taskStatus) {
        if (TaskStatus.SCHEDULED.equals(taskStatus))
            return JobState.WAITING;

        else if (TaskStatus.RUNNING.equals(taskStatus))
            return JobState.RUNNING;

        else if (TaskStatus.ERROR.equals(taskStatus))
            return JobState.ERROR;

        else if (TaskStatus.COMPLETE.equals(taskStatus))
            return JobState.COMPLETE;

        else if (TaskStatus.PROCESSING.equals(taskStatus))
            return JobState.PROCESSING;

        else if (TaskStatus.UNAUTHORISED.equals(taskStatus))
            return JobState.ERROR;

        else if (TaskStatus.INCOMPLETE.equals(taskStatus))
            return JobState.INCOMPLETE;

        else if (TaskStatus.CANCELED.equals(taskStatus))
            return JobState.CANCELED;

        return JobState.ERROR;
    }

    /**
     * Gets value of job state.
     *
     * @return Value of job state
     */
    public String getValue() {
        return value;
    }

}
