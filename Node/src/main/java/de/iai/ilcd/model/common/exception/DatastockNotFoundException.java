package de.iai.ilcd.model.common.exception;

/**
 * Exception if invalid data set identifier provided
 */
public class DatastockNotFoundException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -309285075021508917L;

    /**
     * Create exception
     *
     * @param msg message
     */
    public DatastockNotFoundException(String msg) {
        super(msg);
    }

}
