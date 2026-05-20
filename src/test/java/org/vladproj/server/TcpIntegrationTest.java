package org.vladproj.server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.vladproj.client.connection.ClientTcp;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TcpIntegrationTest {
    private static final String USERNAME = "Vlados";
    private ServerTcpThread tcpThread;
    private ServerUdpThread udpThread;

    @BeforeAll
    void setup() {
        tcpThread = new ServerTcpThread("TCP");
        udpThread = new ServerUdpThread("UDP");

        tcpThread.start();
        udpThread.start();

        sleep(300);
    }

    @AfterAll
    void shutdown() {
        tcpThread.shutdown();
        udpThread.shutdown();
    }

    @Test
    void shouldClientRegisterSuccessfully(){
        ClientTcp client = new ClientTcp(
                InetAddress.getLoopbackAddress(),
                5000,
                USERNAME,
                6000
        );
        boolean result = client.register();

        assertTrue(result, "Client should register");

        assertNotNull(tcpThread.getClients().get(USERNAME));
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
