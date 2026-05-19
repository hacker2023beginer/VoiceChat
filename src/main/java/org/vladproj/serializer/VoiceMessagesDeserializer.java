package org.vladproj.serializer;

import org.vladproj.entity.VoiceMessage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoiceMessagesDeserializer {

    public static Map<String, VoiceMessage> deserialize(byte[] bytes) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));

        int size = data.readInt();
        Map<String, VoiceMessage> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            String key = readString(data);
            VoiceMessage msg = readVoiceMessage(data);
            map.put(key, msg);
        }

        return map;
    }

    private static String readString(DataInputStream data) throws IOException {
        int len = data.readInt();
        byte[] bytes = data.readNBytes(len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static VoiceMessage readVoiceMessage(DataInputStream data) throws IOException {
        UUID id = UUID.fromString(readString(data));
        String sender = readString(data);
        String receiver = readString(data);

        int dataLen = data.readInt();
        byte[] voice = data.readNBytes(dataLen);

        long timestamp = data.readLong();

        return VoiceMessage.builder()
                .id(id)
                .sender(sender)
                .receiver(receiver)
                .data(voice)
                .timestamp(timestamp)
                .build();
    }
}

