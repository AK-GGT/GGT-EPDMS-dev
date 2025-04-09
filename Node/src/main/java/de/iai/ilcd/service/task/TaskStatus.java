package de.iai.ilcd.service.task;

/**
 * The different states a task can be in. When the tasks use job meta data objects to track their progress in
 * the db, then this state is (converted to a JobState) used there, too.
 */
public enum TaskStatus {

    PROCESSING,
    SCHEDULED,
    RUNNING,
    COMPLETE,
    INCOMPLETE,
    ERROR,
    UNAUTHORISED,
    CANCELED;

    private static final String i18nPrefix = "task.status.";

    public String getI18nKey() {
        return i18nPrefix + name().toLowerCase();
    }

    // For Convenience

    public boolean isError() {
        return this.equals(ERROR);
    }

}
