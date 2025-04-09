package de.iai.ilcd.model.common.exception;

public class ExpiredPasswordNotChangedException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -2902399689997163477L;

    /**
     * Creates a new instance of <code>ExpiredPasswordNotChangedException</code> without detail message.
     */
    public ExpiredPasswordNotChangedException() {
    }

    /**
     * Constructs an instance of <code>ExpiredPasswordNotChangedException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ExpiredPasswordNotChangedException(String msg) {
        super(msg);
    }

}
