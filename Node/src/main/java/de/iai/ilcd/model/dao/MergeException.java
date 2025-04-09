package de.iai.ilcd.model.dao;

/**
 * MergeException will be thrown if a merge operation will fail in a dao object
 */
public class MergeException extends Exception {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -4576474342174544246L;

    /**
     * Creates a new instance of <code>MergeException</code> without detail message.
     */
    public MergeException() {
    }

    /**
     * Constructs an instance of <code>MergeException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public MergeException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>MergeException</code> with the specified detail message.
     *
     * @param msg   the detail message.
     * @param cause cause of the exception
     */
    public MergeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
