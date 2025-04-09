package de.iai.ilcd.model.dao;

/**
 * This exception will be thrown when a lookup operation will fail to find the object to lookup
 *
 * @author clemens.duepmeier
 */
public class UnknownDBObjectException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 8393975248579060143L;

    /**
     * Creates a new instance of <code>UnknownDBObjectException</code> without detail message.
     */
    public UnknownDBObjectException() {
    }

    /**
     * Constructs an instance of <code>UnknownDBObjectException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnknownDBObjectException(String msg) {
        super(msg);
    }
}
