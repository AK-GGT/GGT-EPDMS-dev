package de.iai.ilcd.model.common.exception;

public class PrivacyPolicyNotAcceptedException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -308624641260405949L;

    /**
     * Creates a new instance of <code>PrivacyPolicyNotAcceptedException</code> without detail message.
     */
    public PrivacyPolicyNotAcceptedException() {
    }

    /**
     * Constructs an instance of <code>PrivacyPolicyNotAcceptedException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public PrivacyPolicyNotAcceptedException(String msg) {
        super(msg);
    }
}
