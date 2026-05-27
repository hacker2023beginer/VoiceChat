package org.vladproj.exception;

public class DatagramSocketException extends RuntimeException {
    public DatagramSocketException(String message) {
        super(message);
    }

    public DatagramSocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatagramSocketException(Throwable cause) {
        super(cause);
    }
}
