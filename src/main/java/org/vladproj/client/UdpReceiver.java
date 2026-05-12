package org.vladproj.client;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.exception.DatagramSocketException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpReceiver extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static final int BUFFER_LENGTH = 2048;
    private final int UDP_CLIENT_PORT;
    private DatagramSocket socket;
    private boolean isRunning = true;
    private volatile String lastMessage;

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public UdpReceiver(String name, int udpClientPort) {
        super(name);
        UDP_CLIENT_PORT = udpClientPort;
        try {
            socket = new DatagramSocket(UDP_CLIENT_PORT);
            log.info("Client with udp port {} is created", UDP_CLIENT_PORT);
        } catch (Exception e) {
            log.fatal("Cannot find localhost address or port is not available");
            throw new DatagramSocketException(e);
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            byte[] voiceBuffer = new byte[BUFFER_LENGTH];
            DatagramPacket packet = new DatagramPacket(voiceBuffer, BUFFER_LENGTH);
            try {
                socket.receive(packet);
                voiceBuffer = packet.getData();
                int length = packet.getLength();
                String msg = new String(voiceBuffer, 0, length);
                setLastMessage(msg);
                log.info("Received: {}", msg);
            } catch (IOException e) {
                if (!isRunning) {
                    break;
                }
                log.error("Error while receiving packet", e);
            }
        }
    }

    public void shutdown() {
        isRunning = false;
        socket.close();
        log.info("Thread {} is interrupted", getName());
    }
}
