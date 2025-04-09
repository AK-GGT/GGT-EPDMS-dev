package de.iai.ilcd.service.task;

public class TaskRunException extends TaskException {

    private static final String messagePrefix = "Error running ";

    public TaskRunException(Class<?> taskClass, Throwable cause) {
        super(messagePrefix + taskClass.getTypeName(), cause);
    }

    public TaskRunException(String i18nKey, Class<?> taskClass, Throwable cause) {
        super(i18nKey, messagePrefix + taskClass.getTypeName(), cause);
    }

    public TaskRunException(String message) {
        super(message);
    }

}
