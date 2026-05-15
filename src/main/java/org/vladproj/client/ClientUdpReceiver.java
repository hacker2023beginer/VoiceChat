package org.vladproj.client;


import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.client.voice.VoiceConfig;
import org.vladproj.entity.VoiceUdpPacket;
import org.vladproj.exception.DatagramSocketException;
import org.vladproj.serializer.VoiceUdpPacketSerializer;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Getter
@Setter
public class ClientUdpReceiver extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static final VoiceUdpPacketSerializer SERIALIZER = new VoiceUdpPacketSerializer();
    private static final int BUFFER_LENGTH = 2048;
    private final int udpClientPort;
    private final DatagramSocket socket;
    private boolean isRunning = true;
    private volatile String lastMessage;

    public ClientUdpReceiver(String name, int udpClientPort, DatagramSocket socket) {
        super(name);
        this.udpClientPort = udpClientPort;
        this.socket = socket;
        log.info("Client with udp port {} is created", this.udpClientPort);
    }

    @SneakyThrows
    @Override
    public void run() {
        AudioFormat format = VoiceConfig.createFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(info);
        speakers.open(format);
        speakers.start();
        while (isRunning) {
            byte[] voiceBuffer = new byte[BUFFER_LENGTH];
            DatagramPacket packet = new DatagramPacket(voiceBuffer, BUFFER_LENGTH);
            socket.receive(packet);
            byte[] raw = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), 0, raw, 0, packet.getLength());
            VoiceUdpPacket voicePacket = SERIALIZER.deserialize(voiceBuffer);
            speakers.write(
                    voicePacket.getData(),
                    0,
                    voicePacket.getData().length
            );
            int length = voicePacket.getData().length;
            String msg = new String(voicePacket.getData(), 0, length);
            setLastMessage(msg);
            log.info("Received: {}", msg);
        }
        speakers.drain();
        speakers.close();
    }

    public void shutdown() {
        isRunning = false;
        socket.close();
        log.info("Thread {} is interrupted", getName());
    }
}
