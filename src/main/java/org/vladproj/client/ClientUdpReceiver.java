package org.vladproj.client;


import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.exception.DatagramSocketException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Getter
@Setter
public class ClientUdpReceiver extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static final int BUFFER_LENGTH = 2048;
    private final int udpClientPort;
    private final DatagramSocket socket;
    private boolean isRunning = true;
    private volatile String lastMessage;

    public ClientUdpReceiver(String name, int udpClientPort) {
        super(name);
        try {
            this.udpClientPort = udpClientPort;
            socket = new DatagramSocket(this.udpClientPort);
            log.info("Client with udp port {} is created", this.udpClientPort);
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
