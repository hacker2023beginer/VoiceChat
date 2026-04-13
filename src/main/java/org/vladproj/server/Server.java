package org.vladproj.server;

import org.vladproj.entity.ClientInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Server extends Thread{
    protected Map<String, ClientInfo> clients = new ConcurrentHashMap<>();

    public Server(String name) {
        super(name);
    }

    @Override
    public abstract void run();

    public abstract void shutdown();
}
