package de.iai.ilcd.xml.zip.exceptions;

public class ZipException extends Exception {

    public ZipException(String message) {
        super(message);
    }

    public ZipException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipException(Throwable cause) {
        super(cause);
    }

}
