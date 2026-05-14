package org.vladproj.server;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.vladproj.client.ClientHeartbeatThread;
import org.vladproj.client.ClientTcp;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerClientCleanThreadTest {
    private static final String USERNAME = "Vlados";
    private static final int CLIENT_UDP_PORT = 6000;
    private ServerTcpThread tcpThread;
    private ServerUdpThread udpThread;
    private ClientHeartbeatThread heartbeatThread;
    private ServerClientCleanThread cleanThread;

    @SneakyThrows
    @BeforeAll
    void setup() {
        tcpThread = new ServerTcpThread("TCP");
        udpThread = new ServerUdpThread("UDP");
        cleanThread = new ServerClientCleanThread("CLEAN");
        heartbeatThread = new ClientHeartbeatThread("HEARTBEAT", ServerUdpThread.UDP_SOCKET_PORT, InetAddress.getLocalHost(), CLIENT_UDP_PORT, USERNAME);
        tcpThread.start();
        udpThread.start();
        sleep(300);
        ClientTcp client = new ClientTcp(
                InetAddress.getLoopbackAddress(),
                ServerTcpThread.TCP_SOCKET_PORT,
                USERNAME,
                CLIENT_UDP_PORT
        );
        client.register();
        heartbeatThread.start();
        cleanThread.start();
    }

    @SneakyThrows
    @Test
    void cleanUserSuccessfully() {
        heartbeatThread.shutdown();
        sleep(20000);
        assertEquals(0, tcpThread.getClients().size());
    }

    @AfterAll
    void shutdown() {
        tcpThread.shutdown();
        udpThread.shutdown();
        cleanThread.shutdown();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}