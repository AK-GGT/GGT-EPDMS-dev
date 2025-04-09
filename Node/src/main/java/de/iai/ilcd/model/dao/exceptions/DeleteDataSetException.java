package de.iai.ilcd.model.dao.exceptions;

/**
 * @author clemens.duepmeier
 */
public class DeleteDataSetException extends Exception {

    private static final long serialVersionUID = -4984877781421138010L;

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
}
