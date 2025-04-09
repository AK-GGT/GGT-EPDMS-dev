package de.iai.ilcd.service.task;

public abstract class TaskException extends Exception {

    protected String i18nKey;

    public TaskException(String i18nKey, String message) {
        super(message);
        this.i18nKey = i18nKey;
    }

    public TaskException(String i18nKey, String message, Throwable cause) {
        super(message, cause);
        this.i18nKey = i18nKey;
    }

    public TaskException(String message) {
        super(message);
        this.i18nKey = null;
    }

    public TaskException(String message, Throwable cause) {
        super(message, cause);
        this.i18nKey = null;
    }

    public String getI18nKey() {
        return i18nKey;
    }

}
