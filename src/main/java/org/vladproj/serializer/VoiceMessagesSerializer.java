package org.vladproj.serializer;

import org.vladproj.entity.VoiceMessage;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class VoiceMessagesSerializer {

    public static byte[] serialize(Map<String, CopyOnWriteArrayList<VoiceMessage>> map) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(out);

        data.writeInt(map.size());

        for (var entry : map.entrySet()) {
            writeString(data, entry.getKey());

            CopyOnWriteArrayList<VoiceMessage> list = entry.getValue();
            data.writeInt(list.size());

            for (VoiceMessage msg : list) {
                writeVoiceMessage(data, msg);
            }
        }

        return out.toByteArray();
    }

    private static void writeString(DataOutputStream data, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        data.writeInt(bytes.length);
        data.write(bytes);
    }

    private static void writeVoiceMessage(DataOutputStream data, VoiceMessage msg) throws IOException {
        writeString(data, msg.getId().toString());
        writeString(data, msg.getSender());
        writeString(data, msg.getReceiver());

        data.writeInt(msg.getData().length);
        data.write(msg.getData());

        data.writeLong(msg.getTimestamp());
    }
}
