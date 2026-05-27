package org.vladproj;

import org.vladproj.server.ServerUdpThread;
import org.vladproj.server.ServerTcpThread;
import org.vladproj.server.ServerClientCleanThread;

import java.util.Scanner;

public class App {
    private static final String STOP_WORD = "stop";

    public static void main(String[] args) {
        ServerTcpThread tcpServer = new ServerTcpThread("tcp-server");
        ServerUdpThread udpServer = new ServerUdpThread("udp-server");
        ServerClientCleanThread cleanThread = new ServerClientCleanThread("client-cleaner");
        tcpServer.start();
        udpServer.start();
        cleanThread.start();
        Scanner sc = new Scanner(System.in);
        String command = sc.nextLine();
        while (!command.equalsIgnoreCase(STOP_WORD)) {
            command = sc.nextLine();
        }
        cleanThread.shutdown();
        udpServer.shutdown();
        tcpServer.shutdown();
    }
}
