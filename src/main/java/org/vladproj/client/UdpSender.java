package org.vladproj.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.PacketType;
import org.vladproj.entity.VoiceUdpPacket;
import org.vladproj.serializer.VoiceUdpPacketSerializer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpSender {
    private static final Logger log = LogManager.getLogger();
    private static final PacketType DEFAULT_PACKET_TYPE = PacketType.VOICE;
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
        VoiceUdpPacket voiceUdpPacket = VoiceUdpPacket.builder()
                .packetType(DEFAULT_PACKET_TYPE)
                .destUsername(targetUsername)
                .data(audioData)
                .build();
        VoiceUdpPacketSerializer serializer = new VoiceUdpPacketSerializer();
        byte[] bytes = serializer.serialize(voiceUdpPacket);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, SERVER_ADDRESS, UDP_SERVER_PORT);
        try {
            SOCKET.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
