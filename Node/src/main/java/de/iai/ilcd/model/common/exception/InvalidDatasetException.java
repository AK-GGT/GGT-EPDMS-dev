package de.iai.ilcd.model.common.exception;

/**
 * Exception if invalid data set identifier provided
 */
public class InvalidDatasetException extends RuntimeException {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 3428402580593503803L;

    /**
     * Create exception
     *
     * @param msg message
     */
    public InvalidDatasetException(String msg) {
        super(msg);
    }

}
