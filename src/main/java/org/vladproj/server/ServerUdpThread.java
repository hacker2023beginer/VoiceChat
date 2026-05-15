package org.vladproj.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.ClientInfo;
import org.vladproj.entity.VoiceUdpPacket;
import org.vladproj.serializer.VoiceUdpPacketSerializer;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

//У пользователя есть уникальный никнейм
//Следовательно, поиск ведется по никнейму. Быстрый доступ по HashMap
//Не HashSet так как у него нет операций получения элемента, без использования цикла с итерациями
public class ServerUdpThread extends ServerRepository {
    private static final Logger log = LogManager.getLogger();
    private static final VoiceUdpPacketSerializer serializer = new VoiceUdpPacketSerializer();
    public static final int UDP_SOCKET_PORT = 4445;
    private static final int BUFFER_LENGTH = 2048;
    private volatile boolean isRunning = true;
    private DatagramSocket socket;

    public ServerUdpThread(String name) {
        super(name);
        try {
            socket = new DatagramSocket(UDP_SOCKET_PORT);
        } catch (SocketException e) {
            log.fatal("Port is unusable. Please, free the port for use or change in Server.class", e.getCause());
            Thread.currentThread().interrupt();
        }
        try {
            log.info("Server start with ip {} on port: {}", InetAddress.getLocalHost(), UDP_SOCKET_PORT);
        } catch (UnknownHostException e) {
            log.fatal("Cannot find ipv4 of server");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        log.info("Start sending voice message...");
        byte[] voiceBuffer = new byte[BUFFER_LENGTH];
        while (isRunning) {
            try {
                DatagramPacket packet = new DatagramPacket(voiceBuffer, BUFFER_LENGTH);
                socket.receive(packet);
                InetAddress senderAddr = packet.getAddress();
                int senderPort = packet.getPort();
                log.info("Sender address: {}. Sender port: {}", senderAddr, senderPort);
                handlePacket(packet);
            } catch (IOException e) {
                if (!isRunning) {
                    break;
                }
                e.printStackTrace();
            }
        }
        socket.close();
        log.info("End sending voice message. Thread {} is interrupted", Thread.currentThread().getName());
    }

    private void handlePacket(DatagramPacket packet) {
        log.info("Method handlePacket started");
        byte[] raw = Arrays.copyOf(packet.getData(), packet.getLength());
        VoiceUdpPacket voiceUdpPacket = serializer.deserialize(raw);
        switch (voiceUdpPacket.getPacketType()) {
            case VOICE -> handleVoicePacket(voiceUdpPacket);
            case PING -> handlePingPacket(voiceUdpPacket);
            case DISCONNECT -> handleDisconnectPacket(voiceUdpPacket);
            default -> {}
        }
        log.info("Method handlePacket ended");
    }

    private void handleVoicePacket(VoiceUdpPacket voiceUdpPacket) {
        log.info("UDP server handle method for voice packet is started");
        String targetUsername = voiceUdpPacket.getDestUsername();
        ClientInfo targetClientInfo = clients.get(targetUsername);
        if (targetClientInfo == null) {
            log.warn("There is no user in hashmap with username: {}", targetUsername);
            return;
        }
        byte[] audioData = serializer.serialize(voiceUdpPacket);
        DatagramPacket sendingPacket = new DatagramPacket(audioData, audioData.length, targetClientInfo.getAddress(), targetClientInfo.getUdpPort());
        try {
            socket.send(sendingPacket);
            log.info("Send data successfully");
        } catch (IOException e) {
            if (!isRunning) {
                return;
            }
            e.printStackTrace();
        }
        log.info("Handle method for voice packet is ended");
    }

    private void handlePingPacket(VoiceUdpPacket packet) {
        log.info("Handle heartbeat packet");
        String srcUsername = packet.getSrcUsername();
        ClientInfo clientInfo = clients.get(srcUsername);
        if (clientInfo == null) {
            log.warn("User with username \"{}\" doesn't exist in map", srcUsername);
            return;
        }
        clientInfo.setLastSeen(System.currentTimeMillis());
    }

    private void handleDisconnectPacket(VoiceUdpPacket packet) {
        log.info("Handle disconnect packet");
        clients.remove(packet.getSrcUsername());
        log.info("Disconnect user {}", packet.getSrcUsername());
    }

    public void shutdown() {
        isRunning = false;
        socket.close();
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}