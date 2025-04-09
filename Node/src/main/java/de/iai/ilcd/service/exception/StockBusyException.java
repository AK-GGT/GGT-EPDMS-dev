package de.iai.ilcd.service.exception;

/**
 * StockBusyException will be thrown when Assign/reomve operation is started and the stock is busy
 */
public class StockBusyException extends Exception {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -4533474342174644249L;

    /**
     * Creates a new instance of <code>StockBusyException</code> without detail message.
     */
    public StockBusyException() {
    }

    /**
     * Constructs an instance of <code>StockBusyException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public StockBusyException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>StockBusyException</code> with the specified detail message.
     *
     * @param msg   the detail message.
     * @param cause cause of the exception
     */
    public StockBusyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
