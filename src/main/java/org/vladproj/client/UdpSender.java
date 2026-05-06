package org.vladproj.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UdpSender {
    private static final byte[] DATAGRAM_SEPARATOR = "|".getBytes(StandardCharsets.UTF_8);
    private final int UDP_SERVER_PORT;
    private final InetAddress SERVER_ADDRESS;
    private final DatagramSocket SOCKET;

    public UdpSender(int udpServerPort, InetAddress address) {
        this.UDP_SERVER_PORT = udpServerPort;
        this.SERVER_ADDRESS = address;
        try {
            SOCKET = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void send (String targetUsername, byte[] audioData) {
        if (targetUsername == null || audioData == null) return;
        if (targetUsername.isBlank()) return;
        byte[] usernameInBytes = targetUsername.getBytes(StandardCharsets.UTF_8);
        int dataLength = usernameInBytes.length + audioData.length + DATAGRAM_SEPARATOR.length;
        byte[] data = new byte[dataLength];
        System.arraycopy(usernameInBytes, 0, data, 0, usernameInBytes.length);
        System.arraycopy(DATAGRAM_SEPARATOR, 0, data, usernameInBytes.length, DATAGRAM_SEPARATOR.length);
        System.arraycopy(audioData, 0, data, usernameInBytes.length + DATAGRAM_SEPARATOR.length, audioData.length);
        DatagramPacket packet = new DatagramPacket(data, data.length, SERVER_ADDRESS, UDP_SERVER_PORT);
        try {
            SOCKET.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
