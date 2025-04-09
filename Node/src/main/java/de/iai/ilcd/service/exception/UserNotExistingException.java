package de.iai.ilcd.service.exception;

/**
 * The exception class for not existing users.
 *
 * @author sarai
 */
public class UserNotExistingException extends Exception {

    /*
     * The unique id of serial version
     */
    private static final long serialVersionUID = -6444648467681144688L;

    /**
     * Throws a new exception.
     */
    public UserNotExistingException() {
    }

    /**
     * Throws a new exception containing the cause in history.
     *
     * @param cause The cause of this exception
     */
    public UserNotExistingException(String cause) {
        super(cause);
    }

}
