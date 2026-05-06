package org.vladproj.exception;

public class UdpReceiverException extends RuntimeException {
    public UdpReceiverException(String message) {
        super(message);
    }

    public UdpReceiverException(String message, Throwable cause) {
        super(message, cause);
    }

    public UdpReceiverException(Throwable cause) {
        super(cause);
    }
}
