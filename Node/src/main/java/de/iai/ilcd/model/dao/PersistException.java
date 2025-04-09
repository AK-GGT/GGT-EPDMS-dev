package de.iai.ilcd.model.dao;

/**
 * PersistException will be thrown if a dao persist operation will fail
 *
 * @author clemens.duepmeier
 */
public class PersistException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 347749911021604308L;

    /**
     * Creates a new instance of <code>PersistException</code> without detail message.
     */
    public PersistException() {
    }

    /**
     * Constructs an instance of <code>PersistException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public PersistException(String msg) {
        super(msg);
    }

    public PersistException(String msg, Throwable e) {
        super(msg, e);
    }
}
