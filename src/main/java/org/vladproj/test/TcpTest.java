package org.vladproj.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.client.TcpClient;
import org.vladproj.server.ServerTcpThread;
import org.vladproj.server.ServerUdpThread;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TcpTest {
    private static final Logger log = LogManager.getLogger();

    public static void main(String[] args) throws UnknownHostException {
        ServerTcpThread tcpServer = new ServerTcpThread("TCP");
        tcpServer.start();
        ServerUdpThread udpServer = new ServerUdpThread("UDP");
        udpServer.start();
        TcpClient tcpClient = new TcpClient(InetAddress.getLocalHost(), 5000, "Vlados", 6000);
        if (tcpClient.register()) {
            log.info("Зарегался");
            log.info(tcpServer.getClients().get("Vlados"));
        } else {
            log.info("Не зарегался");
        }
    }
}
