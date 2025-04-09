package de.iai.ilcd.model.common.exception;

public class InvalidGroupException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>FormatException</code> without detail message.
     */
    public InvalidGroupException() {
    }

    /**
     * Constructs an instance of <code>FormatException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidGroupException(String msg) {
        super(msg);
    }
}
