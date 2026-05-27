package org.vladproj.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.ClientInfo;

import java.util.Map;

public class ServerClientCleanThread extends ServerRepository {
    private static final Logger log = LogManager.getLogger();
    private static final Long CLEAN_CHECK_TIME_MS = 5000L;
    private static final Long DISCONNECT_SUBTRACT_MS = 15000L;
    private boolean running = true;

    public ServerClientCleanThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        log.info("Clean thread {} started", getName());
        while (running) {
            try {
                Thread.sleep(CLEAN_CHECK_TIME_MS);
                for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
                    ClientInfo client = entry.getValue();
                    if (System.currentTimeMillis() - client.getLastSeen() > DISCONNECT_SUBTRACT_MS) {
                        clients.remove(entry.getKey());
                        log.info("User {} disconnect", client.getAddress());
                    }
                }
            } catch (InterruptedException e) {
                log.warn("Clean thread {} is interrupted immediately by another thread", getName());
                shutdown();
                Thread.currentThread().interrupt();
            }
        }
        log.info("Clean thread {} ended", getName());
    }

    public void shutdown() {
        log.info("Shutdown clean thread {}", getName());
        running = false;
    }
}
