package org.vladproj.server;

import org.vladproj.entity.ClientInfo;
import org.vladproj.entity.VoiceMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ServerRepository extends Thread{
    protected static Map<String, ClientInfo> clients = new ConcurrentHashMap<>();
    protected static Map<String, CopyOnWriteArrayList<VoiceMessage>> messages = new ConcurrentHashMap<>();

    protected ServerRepository(String name) {
        super(name);
    }

    @Override
    public abstract void run();

    public abstract void shutdown();

    public Map<String, ClientInfo> getClients() {
        return clients;
    }

    public static Map<String, CopyOnWriteArrayList<VoiceMessage>> getMessages() {
        return messages;
    }
}
