package org.vladproj.exception;

public class VoiceUdpSerializerException extends RuntimeException {
    public VoiceUdpSerializerException() {
    }

    public VoiceUdpSerializerException(String message) {
        super(message);
    }

    public VoiceUdpSerializerException(String message, Throwable cause) {
        super(message, cause);
    }

}
