package org.vladproj.client.voice;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.vladproj.client.connection.ClientHeartbeatThread;
import org.vladproj.client.connection.ClientTcp;
import org.vladproj.client.connection.ClientUdpReceiver;
import org.vladproj.client.connection.ClientUdpSender;
import org.vladproj.server.ServerClientCleanThread;
import org.vladproj.server.ServerTcpThread;
import org.vladproj.server.ServerUdpThread;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VoiceCaptureThreadTest {
    private static final String USERNAME1 = "Vlados";
    private static final int CLIENT_UDP_PORT1 = 6000;
    private static final String USERNAME2 = "Misha";
    private static final int CLIENT_UDP_PORT2 = 6001;
    private ServerTcpThread tcpThread;
    private ServerUdpThread udpThread;
    private ClientHeartbeatThread heartbeatThread1;
    private ClientHeartbeatThread heartbeatThread2;
    private ServerClientCleanThread cleanThread;
    private ClientUdpReceiver receiver1;
    private ClientUdpSender sender2;

    @SneakyThrows
    @BeforeAll
    void setup() {
        tcpThread = new ServerTcpThread("TCP");
        udpThread = new ServerUdpThread("UDP");
        cleanThread = new ServerClientCleanThread("CLEAN");
        heartbeatThread1 = new ClientHeartbeatThread("HEARTBEAT1", ServerUdpThread.UDP_SOCKET_PORT, InetAddress.getLocalHost(), CLIENT_UDP_PORT1, USERNAME1);
        heartbeatThread2 = new ClientHeartbeatThread("HEARTBEAT2", ServerUdpThread.UDP_SOCKET_PORT, InetAddress.getLocalHost(), CLIENT_UDP_PORT2, USERNAME2);
        receiver1 = new ClientUdpReceiver("RECEIVER1", CLIENT_UDP_PORT1, heartbeatThread1.getSocket());
        sender2 = new ClientUdpSender(ServerUdpThread.UDP_SOCKET_PORT,  InetAddress.getLocalHost(), CLIENT_UDP_PORT2, heartbeatThread2.getSocket());
        tcpThread.start();
        udpThread.start();
        Thread.sleep(300);
        ClientTcp client1 = new ClientTcp(
                InetAddress.getLoopbackAddress(),
                ServerTcpThread.TCP_SOCKET_PORT,
                USERNAME1,
                CLIENT_UDP_PORT1
        );
        client1.register();
        ClientTcp client2 = new ClientTcp(
                InetAddress.getLoopbackAddress(),
                ServerTcpThread.TCP_SOCKET_PORT,
                USERNAME2,
                CLIENT_UDP_PORT2
        );
        client2.register();
        heartbeatThread1.start();
        heartbeatThread2.start();
        cleanThread.start();
        receiver1.start();
    }

    @SneakyThrows
    @Test
    void testSendingVoiceData() {
        VoiceCaptureThread voiceCaptureThread = new VoiceCaptureThread(sender2, USERNAME1, USERNAME2);
        voiceCaptureThread.start();
        Thread.sleep(10000);
        assertNotEquals(null, receiver1.getLastMessage());
    }

    @AfterAll
    void shutdown() {
        tcpThread.shutdown();
        udpThread.shutdown();
        heartbeatThread1.shutdown();
        heartbeatThread2.shutdown();
        cleanThread.shutdown();
        receiver1.shutdown();
    }
}