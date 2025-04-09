package de.iai.ilcd.model.common.exception;

/**
 * @author clemens.duepmeier
 */
public class FormatException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 4360649931931671629L;

    /**
     * Creates a new instance of <code>FormatException</code> without detail message.
     */
    public FormatException() {
    }

    /**
     * Constructs an instance of <code>FormatException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public FormatException(String msg) {
        super(msg);
    }
}
