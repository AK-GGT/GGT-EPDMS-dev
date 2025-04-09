package de.iai.ilcd.rest.util;

/**
 * The exception class for filed authorization of users.
 *
 * @author sarai
 */
public class NotAuthorizedException extends Exception {

    /*
     * The unique ID of serial version
     */
    private static final long serialVersionUID = -6444648467681144688L;

    /**
     * Throws new exception.
     */
    public NotAuthorizedException() {
    }

    /**
     * Throws new exception containing the cause in history.
     *
     * @param cause The cause of this exception
     */
    public NotAuthorizedException(String cause) {
        super(cause);
    }

}
