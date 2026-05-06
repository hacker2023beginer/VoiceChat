package org.vladproj.parser;

import java.util.Optional;

public class ClientBufferParser {
    public Optional<String[]> parseClient(String buffer) {
        String[] data = buffer.split(" ");
        try {
            Integer.valueOf(data[2]);
        } catch (Exception e) {
            return Optional.empty();
        }
        return Optional.of(data);
    }
}
