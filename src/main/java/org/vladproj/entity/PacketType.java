package org.vladproj.entity;

import java.util.Optional;

public enum PacketType {
    VOICE(1), TEXT(2), PING(3), DISCONNECT(4);

    private int value;

    PacketType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Optional<PacketType> fromValue(int value) {
        if (value < 1 || value > 4) return Optional.empty();
        return Optional.of(values()[value - 1]);
    }
}
