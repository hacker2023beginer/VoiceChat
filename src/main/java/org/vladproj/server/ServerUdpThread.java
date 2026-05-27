package org.vladproj.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.ClientInfo;
import org.vladproj.entity.VoiceUdpPacket;
import org.vladproj.persistence.MessagePersistenceService;
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
    private static final MessagePersistenceService persistence = new MessagePersistenceService();
    public static final int UDP_SOCKET_PORT = 4445;
    private static final int BUFFER_LENGTH = 2048;
    private volatile boolean isRunning = true;
    private DatagramSocket socket;

    public ServerUdpThread(String name) {
        super(name);
        messages = persistence.load();
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
        log.info("UDP server started on port {}", UDP_SOCKET_PORT);
        byte[] voiceBuffer = new byte[BUFFER_LENGTH];
        while (isRunning) {
            try {
                DatagramPacket packet = new DatagramPacket(voiceBuffer, BUFFER_LENGTH);
                socket.receive(packet);
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
        byte[] raw = Arrays.copyOf(packet.getData(), packet.getLength());
        VoiceUdpPacket voiceUdpPacket = serializer.deserialize(raw);
        switch (voiceUdpPacket.getPacketType()) {
            case VOICE -> forwardPacket(voiceUdpPacket);
            case PING -> handlePingPacket(voiceUdpPacket);
            case DISCONNECT -> handleDisconnectPacket(voiceUdpPacket);
            case CALL_REQUEST, CALL_ACCEPT, CALL_REJECT, CALL_BUSY, CALL_END -> forwardPacket(voiceUdpPacket);
            default -> {}
        }
    }

    private void forwardPacket(VoiceUdpPacket voiceUdpPacket) {
        String targetUsername = voiceUdpPacket.getDestUsername();
        ClientInfo targetClientInfo = clients.get(targetUsername);
        if (targetClientInfo == null) {
            log.warn("There is no user in hashmap with username: {}", targetUsername);
            return;
        }
        byte[] data = serializer.serialize(voiceUdpPacket);
        DatagramPacket sendingPacket = new DatagramPacket(data, data.length, targetClientInfo.getAddress(), targetClientInfo.getUdpPort());
        try {
            socket.send(sendingPacket);
        } catch (IOException e) {
            if (!isRunning) {
                return;
            }
            e.printStackTrace();
        }
    }

    private void handlePingPacket(VoiceUdpPacket packet) {
        String srcUsername = packet.getSrcUsername();
        ClientInfo clientInfo = clients.get(srcUsername);
        if (clientInfo == null) {
            log.warn("User with username \"{}\" doesn't exist in map", srcUsername);
            return;
        }
        clientInfo.setLastSeen(System.currentTimeMillis());
    }

    private void handleDisconnectPacket(VoiceUdpPacket packet) {
        clients.remove(packet.getSrcUsername());
        log.info("User {} disconnected", packet.getSrcUsername());
    }

    public void shutdown() {
        isRunning = false;
        socket.close();
        persistence.save(messages);
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}
