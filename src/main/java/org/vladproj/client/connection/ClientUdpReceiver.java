package org.vladproj.client.connection;


import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.client.voice.VoiceConfig;
import org.vladproj.entity.VoiceUdpPacket;
import org.vladproj.serializer.VoiceUdpPacketSerializer;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Getter
@Setter
public class ClientUdpReceiver extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static final VoiceUdpPacketSerializer SERIALIZER = new VoiceUdpPacketSerializer();
    //public static final Map<String, List<VoiceMessage>> messages = ServerRepository.getMessages();
    private static final int BUFFER_LENGTH = 2048;
    private final int udpClientPort;
    private final DatagramSocket socket;
    private final Consumer<VoiceUdpPacket> packetListener;
    private volatile boolean isRunning = true;
    private volatile String lastMessage;

    public ClientUdpReceiver(String name, int udpClientPort, DatagramSocket socket) {
        this(name, udpClientPort, socket, null);
    }

    public ClientUdpReceiver(String name, int udpClientPort, DatagramSocket socket, Consumer<VoiceUdpPacket> packetListener) {
        super(name);
        this.udpClientPort = udpClientPort;
        this.socket = socket;
        this.packetListener = packetListener;
        log.info("Client with udp port {} is created", this.udpClientPort);
    }

    @Override
    public void run() {
        SourceDataLine speakers = openSpeakers();
        try {
            while (isRunning) {
                byte[] voiceBuffer = new byte[BUFFER_LENGTH];
                DatagramPacket packet = new DatagramPacket(voiceBuffer, BUFFER_LENGTH);
                socket.receive(packet);
                byte[] raw = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, raw, 0, packet.getLength());
                VoiceUdpPacket voicePacket = SERIALIZER.deserialize(raw);
                notifyPacketReceived(voicePacket);
                playPacket(speakers, voicePacket);
                String msg = new String(voicePacket.getData(), StandardCharsets.UTF_8);
                setLastMessage(msg);
                log.info("Received packet from {}", voicePacket.getSrcUsername());
            }
        } catch (SocketException e) {
            if (isRunning) {
                log.error("UDP receiver socket error", e);
            }
        } catch (IOException e) {
            log.error("UDP receiver IO error", e);
        } finally {
            closeSpeakers(speakers);
        }
    }

    private SourceDataLine openSpeakers() {
        try {
            AudioFormat format = VoiceConfig.createFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
            speakers.start();
            return speakers;
        } catch (LineUnavailableException | IllegalArgumentException e) {
            log.warn("Speakers are unavailable. Incoming packets will be received without playback.", e);
            return null;
        }
    }

    private void notifyPacketReceived(VoiceUdpPacket voicePacket) {
        if (packetListener != null) {
            packetListener.accept(voicePacket);
        }
    }

    private void playPacket(SourceDataLine speakers, VoiceUdpPacket voicePacket) {
        if (speakers != null && voicePacket.getData() != null) {
            speakers.write(voicePacket.getData(), 0, voicePacket.getData().length);
        }
    }

    private void closeSpeakers(SourceDataLine speakers) {
        if (speakers != null) {
            speakers.drain();
            speakers.stop();
            speakers.close();
        }
    }

    public void shutdown() {
        isRunning = false;
        socket.close();
        log.info("Thread {} is interrupted", getName());
    }
}
