package de.iai.ilcd.model.dao;

/**
 * @author clemens.duepmeier
 */
public class DeleteDataSetException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -7369354707177956265L;

    /**
     * Creates a new instance of <code>DeleteDataSetException</code> without detail message.
     */
    public DeleteDataSetException() {
    }

    /**
     * Constructs an instance of <code>DeleteDataSetException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public DeleteDataSetException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>DeleteDataSetException</code> with the specified detail
     * and parent exception.
     *
     * @param msg the detail message.
     * @param e   original exception.
     */
    public DeleteDataSetException(String msg, Exception e) {
        super(msg, e);
    }
}
