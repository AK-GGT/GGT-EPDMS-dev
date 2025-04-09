package de.iai.ilcd.xml.zip.exceptions;

public class ZipInvalidException extends ZipException {

    public ZipInvalidException(String message) {
        super(message);
    }

    public ZipInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipInvalidException(Throwable cause) {
        super(cause);
    }
}
