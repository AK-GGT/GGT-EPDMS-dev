package de.iai.ilcd.xml.zip.exceptions;

public class ZipUnreadableException extends ZipInvalidException {

    public ZipUnreadableException(String message) {
        super(message);
    }

    public ZipUnreadableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipUnreadableException(Throwable cause) {
        super(cause);
    }
}
