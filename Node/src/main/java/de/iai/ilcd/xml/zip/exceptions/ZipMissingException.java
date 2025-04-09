package de.iai.ilcd.xml.zip.exceptions;

public class ZipMissingException extends ZipException {

    public ZipMissingException(String message) {
        super(message);
    }

    public ZipMissingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipMissingException(Throwable cause) {
        super(cause);
    }
}
