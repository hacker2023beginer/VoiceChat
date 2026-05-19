package org.vladproj.server;

import org.vladproj.entity.ClientInfo;
import org.vladproj.entity.VoiceMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ServerRepository extends Thread{
    protected static Map<String, ClientInfo> clients = new ConcurrentHashMap<>();
    protected static Map<String, VoiceMessage> messages = new ConcurrentHashMap<>();

    protected ServerRepository(String name) {
        super(name);
    }

    @Override
    public abstract void run();

    public abstract void shutdown();

    public Map<String, ClientInfo> getClients() {
        return clients;
    }
}
