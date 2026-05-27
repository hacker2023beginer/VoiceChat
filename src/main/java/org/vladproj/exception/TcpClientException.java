package org.vladproj.exception;

public class TcpClientException extends RuntimeException {
    public TcpClientException(String message) {
        super(message);
    }

    public TcpClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public TcpClientException(Throwable cause) {
        super(cause);
    }
}
