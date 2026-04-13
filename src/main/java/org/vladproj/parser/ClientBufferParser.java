package org.vladproj.parser;

public class ClientBufferParser {
    public String[] parseClient(String buffer) {
        String[] data = buffer.split(" ");
        try {
            Integer.valueOf(data[2]);
        } catch (Exception e) {
            return null;
        }
        return data;
    }
}
