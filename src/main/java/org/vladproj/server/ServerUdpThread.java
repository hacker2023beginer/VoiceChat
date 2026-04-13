package org.vladproj.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.ClientInfo;
import org.vladproj.parser.VoiceBufferParser;

import java.io.IOException;
import java.net.*;

//У пользователя есть уникальный никнейм
//Следовательно, поиск ведется по никнейму. Быстрый доступ по HashMap
//Не HashSet так как у него нет операций получения элемента, без использования цикла с итерациями
public class ServerUdpThread extends Server {
    private static final Logger log = LogManager.getLogger();
    private static final VoiceBufferParser parser = new VoiceBufferParser();
    private static final int UDP_SOCKET_PORT = 4445;
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
                InetAddress senderAddr = packet.getAddress();
                int senderPort = packet.getPort();
                log.info("Sender address: {}. Sender port: {}", senderAddr, senderPort);
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

    public void shutdown() {
        isRunning = false;
        socket.close();
    }

    private void handlePacket(DatagramPacket packet) {
        log.info("Method handlePacket started");
        byte[] voiceBuffer = packet.getData();
        int packetLength = packet.getLength();
        int bound = parser.findDataBound(voiceBuffer, packetLength);
        if (bound <= 0) {
            log.warn("Bad message from client");
            return;
        }
        byte[] audioData = parser.findAudioData(voiceBuffer, bound, packetLength);
        String targetUsername = parser.findUsername(voiceBuffer, bound);
        if (audioData.length == 0) {
            log.info("There is no voice data");
            return;
        }
        ClientInfo targetClientInfo = clients.get(targetUsername);
        if (targetClientInfo == null) {
            log.warn("There is no user in hashmap with username: {}", targetUsername);
            return;
        }
        DatagramPacket sendingPacket = new DatagramPacket(audioData, audioData.length, targetClientInfo.getAddress(), targetClientInfo.getUdpPort());
        try {
            socket.send(sendingPacket);
        } catch (IOException e) {
            if (!isRunning) {
                return;
            }
            e.printStackTrace();
        }
        log.info("Method handlePacket ended");
    }
}