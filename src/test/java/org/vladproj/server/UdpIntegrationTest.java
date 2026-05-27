package org.vladproj.server;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.vladproj.client.connection.ClientTcp;
import org.vladproj.client.connection.ClientUdpReceiver;
import org.vladproj.client.connection.ClientUdpSender;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UdpIntegrationTest {
    private static final String USERNAME = "Vlados";
    private static final String MESSAGE = "hello";
    private ServerUdpThread udpThread;
    private ServerTcpThread tcpThread;
    private ClientUdpSender sender;
    private ClientUdpReceiver receiver;

    @SneakyThrows
    @BeforeAll
    void setup() {
        tcpThread = new ServerTcpThread("TCP");
        udpThread = new ServerUdpThread("UDP");
        tcpThread.start();
        udpThread.start();
        sleep(300);
        ClientTcp client = new ClientTcp(
                InetAddress.getLoopbackAddress(),
                5000,
                USERNAME,
                6000
        );
        client.register();
        try {
            DatagramSocket socket1 = new DatagramSocket(6001);
            DatagramSocket socket2 = new DatagramSocket(6000);
            sender = new ClientUdpSender(4445, InetAddress.getLocalHost(), 6001, socket1);
            receiver = new ClientUdpReceiver(USERNAME, 6000, socket2);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    void shutdown() {
        udpThread.shutdown();
        tcpThread.shutdown();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void shouldSendMessageSuccessful() {
        receiver.start();
        sleep(300);
        sender.send(USERNAME, "test", MESSAGE.getBytes(StandardCharsets.UTF_8));
        sleep(300);
        assertEquals(MESSAGE, receiver.getLastMessage());
        receiver.shutdown();
    }
}
