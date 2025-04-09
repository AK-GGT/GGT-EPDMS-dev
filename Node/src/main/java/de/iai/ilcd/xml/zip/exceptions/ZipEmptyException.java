package de.iai.ilcd.xml.zip.exceptions;

public class ZipEmptyException extends ZipInvalidException {

    public ZipEmptyException(String message) {
        super(message);
    }

    public ZipEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipEmptyException(Throwable cause) {
        super(cause);
    }
}
