package de.iai.ilcd.service.queue;

import de.iai.ilcd.service.task.TaskException;

public class TaskProcessingException extends TaskException {

    private static final String messagePrefix = "Error processing ";

    public TaskProcessingException(Class<?> taskClass, Throwable cause) {
        super(messagePrefix + taskClass.getTypeName(), cause);
    }

    public TaskProcessingException(String i18nKey, Class<?> taskClass, Throwable cause) {
        super(i18nKey, messagePrefix + taskClass.getTypeName(), cause);
    }

    public TaskProcessingException(String message) {
        super(message);
    }

}
