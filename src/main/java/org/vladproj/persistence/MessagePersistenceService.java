package org.vladproj.persistence;

import org.vladproj.entity.VoiceMessage;
import org.vladproj.serializer.VoiceMessagesDeserializer;
import org.vladproj.serializer.VoiceMessagesSerializer;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessagePersistenceService {
    private static final String FILE_NAME = "messages.dat";

    public synchronized void save(Map<String, CopyOnWriteArrayList<VoiceMessage>> messages) {
        try (
                FileOutputStream fos = new FileOutputStream(FILE_NAME)
        ) {

            byte[] data = VoiceMessagesSerializer.serialize(messages);

            fos.write(data);
            fos.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Map<String, CopyOnWriteArrayList<VoiceMessage>> load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }
        try (
                FileInputStream fis = new FileInputStream(file)
        ) {
            byte[] data = fis.readAllBytes();
            return VoiceMessagesDeserializer.deserialize(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}