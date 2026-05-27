package org.vladproj.exception;

public class ServerTcpException extends RuntimeException{
    public ServerTcpException(String message) {
        super(message);
    }

    public ServerTcpException(Throwable cause) {
        super(cause);
    }
}
