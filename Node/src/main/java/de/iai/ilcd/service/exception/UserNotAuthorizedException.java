package de.iai.ilcd.service.exception;

public class UserNotAuthorizedException extends Exception {

    /*
     * The unique id of serial version
     */
    private static final long serialVersionUID = -6444648467681144688L;

    /**
     * Throws a new exception.
     */
    public UserNotAuthorizedException() {
    }

    /**
     * Throws a new exception containing the cause in history.
     *
     * @param cause The cause of this exception
     */
    public UserNotAuthorizedException(String cause) {
        super(cause);
    }

}
