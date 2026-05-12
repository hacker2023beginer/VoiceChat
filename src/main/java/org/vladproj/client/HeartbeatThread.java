package org.vladproj.client;

import lombok.Getter;
import lombok.Setter;
import org.vladproj.exception.DatagramSocketException;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

@Getter
@Setter
public class HeartbeatThread extends Thread {
    private static boolean running = true;
    private final DatagramSocket SOCKET;
    private String srcUsername;
    private final int UDP_SERVER_PORT;
    private final InetAddress SERVER_ADDRESS;

    public HeartbeatThread(String srcUsername, int UDP_SERVER_PORT, InetAddress SERVER_ADDRESS) {
        try {
            this.SOCKET = new DatagramSocket();
        } catch (SocketException e) {
            throw new DatagramSocketException(e);
        }
        this.srcUsername = srcUsername;
        this.UDP_SERVER_PORT = UDP_SERVER_PORT;
        this.SERVER_ADDRESS = SERVER_ADDRESS;
    }

    @Override
    public void run() {
        while (running) {

        }
    }

    public void shutdown() {

    }
}
