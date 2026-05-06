package org.vladproj;

import org.vladproj.server.ServerUdpThread;

import java.util.Scanner;

public class App {
    private static final String STOP_WORD = "stop";

    public static void main(String[] args) {
        ServerUdpThread server = new ServerUdpThread("server1");
        server.start();
        Scanner sc = new Scanner(System.in);
        String command = sc.nextLine();
        while (!command.equalsIgnoreCase(STOP_WORD)) {
            command = sc.nextLine();
        }
        server.shutdown();
    }
}
