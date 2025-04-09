package de.iai.ilcd.service.exception;

/**
 * The exception class for jobs that could not be scheduled.
 *
 * @author sarai
 */
public class JobNotScheduledException extends Exception {

    /**
     * The unique ID
     */
    private static final long serialVersionUID = -6444648467681144688L;

    /**
     * Throws a new exception
     */
    public JobNotScheduledException() {
    }

    /**
     * Throws a new exception containing the history of causing exception
     *
     * @param cause The cause of this thrown exception
     */
    public JobNotScheduledException(String cause) {
        super(cause);
    }

}
