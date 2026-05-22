package org.vladproj.client.voice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.client.connection.ClientUdpSender;

import javax.sound.sampled.*;
import java.util.Arrays;

public class VoiceCaptureThread extends Thread {
    private static final Logger log = LogManager.getLogger("org.vladproj.client.voice.VoiceCaptureThread");
    private static final int BUFFER_LENGTH = 1024;
    private final ClientUdpSender sender;
    private final String targetUsername;
    private final String srcUsername;
    private volatile boolean isRunning = true;
    private TargetDataLine microphone;

    public VoiceCaptureThread(ClientUdpSender sender, String targetUsername, String srcUsername) {
        this.sender = sender;
        this.targetUsername = targetUsername;
        this.srcUsername = srcUsername;
        setDaemon(true);
    }

    @Override
    public void run() {
        AudioFormat format = VoiceConfig.createFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        try {
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
        } catch (LineUnavailableException | IllegalArgumentException e) {
            log.fatal("Microphone is unavailable");
            throw new RuntimeException(e);
        }
        try {
            while (isRunning) {
                byte[] buffer = new byte[BUFFER_LENGTH];
                int count = microphone.read(buffer, 0, BUFFER_LENGTH);
                if (count > 0) {
                    sender.send(targetUsername, srcUsername, Arrays.copyOf(buffer, count));
                }
            }
        } finally {
            closeMicrophone();
        }
    }

    public void shutdown() {
        isRunning = false;
        closeMicrophone();
        interrupt();
    }

    private void closeMicrophone() {
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.close();
        }
    }
}
