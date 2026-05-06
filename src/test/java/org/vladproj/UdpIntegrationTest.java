package org.vladproj;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.vladproj.client.TcpClient;
import org.vladproj.client.UdpReceiver;
import org.vladproj.client.UdpSender;
import org.vladproj.server.ServerTcpThread;
import org.vladproj.server.ServerUdpThread;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UdpIntegrationTest {
    private static final String USERNAME = "Vlados";
    private static final String MESSAGE = "hello";
    private ServerUdpThread udpThread;
    private ServerTcpThread tcpThread;

    @BeforeAll
    void setup() {
        tcpThread = new ServerTcpThread("TCP");
        udpThread = new ServerUdpThread("UDP");
        tcpThread.start();
        udpThread.start();
        sleep(300);
        TcpClient client = new TcpClient(
                InetAddress.getLoopbackAddress(),
                5000,
                USERNAME,
                6000
        );
        client.register();

    }

    @AfterAll
    void shutdown() {
        udpThread.shutdown();
        tcpThread.shutdown();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    @Test
    void shouldSendMessageSuccessful() throws UnknownHostException {
        UdpSender sender = new UdpSender(4445, InetAddress.getLocalHost());
        UdpReceiver receiver = new UdpReceiver(USERNAME, 6000);
        receiver.start();
        sleep(300);
        sender.send(USERNAME, MESSAGE.getBytes(StandardCharsets.UTF_8));
        sleep(300);
        assertEquals(MESSAGE, receiver.getLastMessage());
        receiver.shutdown();
    }
}
