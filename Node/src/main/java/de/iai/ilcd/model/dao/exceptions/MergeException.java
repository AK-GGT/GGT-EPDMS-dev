package de.iai.ilcd.model.dao.exceptions;

/**
 * MergeException will be thrown if a merge operation will fail in a dao object
 *
 * @author clemens.duepmeier
 */
public class MergeException extends Exception {

    private static final long serialVersionUID = 4652389244326285272L;

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
}
