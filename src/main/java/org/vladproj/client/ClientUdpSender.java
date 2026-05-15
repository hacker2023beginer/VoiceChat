package org.vladproj.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.PacketType;
import org.vladproj.entity.VoiceUdpPacket;
import org.vladproj.exception.DatagramSocketException;
import org.vladproj.serializer.VoiceUdpPacketSerializer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientUdpSender {
    private static final Logger log = LogManager.getLogger();
    private static final PacketType DEFAULT_PACKET_TYPE = PacketType.VOICE;
    private static final VoiceUdpPacketSerializer SERIALIZER = new VoiceUdpPacketSerializer();
    private final int udpServerPort;
    private final InetAddress serverAddress;
    private final DatagramSocket socket;
    private final int clientUdpPort;

    public ClientUdpSender(int udpServerPort, InetAddress address, int port, DatagramSocket socket) {
        this.udpServerPort = udpServerPort;
        this.serverAddress = address;
        this.clientUdpPort = port;
        this.socket = socket;
    }

    public void send(String targetUsername, String srcUsername, byte[] audioData) {
        if (targetUsername == null || audioData == null) return;
        if (targetUsername.isBlank()) return;
        VoiceUdpPacket voiceUdpPacket = VoiceUdpPacket.builder()
                .packetType(DEFAULT_PACKET_TYPE)
                .destUsername(targetUsername)
                .srcUsername(srcUsername)
                .data(audioData)
                .build();
        byte[] bytes = SERIALIZER.serialize(voiceUdpPacket);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, serverAddress, udpServerPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new DatagramSocketException(e);
        }
    }
}
