package de.iai.ilcd.model.common.exception;

/**
 * Exception if data set is selected but not in the current context's data stock
 */
public class DatasetNotInSelectedStockException extends RuntimeException {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 3485738940593503803L;

    /**
     * Create exception
     *
     * @param msg message
     */
    public DatasetNotInSelectedStockException(String msg) {
        super(msg);
    }

}
