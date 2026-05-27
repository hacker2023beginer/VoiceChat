package org.vladproj.entity;

import java.util.Optional;

public enum PacketType {
    VOICE(1),
    MESSAGE(2),
    PING(3),
    DISCONNECT(4),
    CALL_REQUEST(5),
    CALL_ACCEPT(6),
    CALL_REJECT(7),
    CALL_BUSY(8),
    CALL_END(9);

    private int value;

    PacketType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Optional<PacketType> fromValue(int value) {
        if (value < 1 || value > values().length) return Optional.empty();
        return Optional.of(values()[value - 1]);
    }
}
