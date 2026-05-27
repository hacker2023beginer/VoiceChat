package org.vladproj.parser;

import java.util.Optional;

public class ClientBufferParser {
    public Optional<String[]> parseClient(String buffer) {
        String[] data = buffer.split(" ", 3);
        if (data.length < 3) {
            return Optional.empty();
        }
        return Optional.of(data);
    }
}
