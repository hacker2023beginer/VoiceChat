package org.vladproj.client.voice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.client.ClientUdpSender;

import javax.sound.sampled.*;
import java.util.Arrays;

public class VoiceCaptureThread extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static final int BUFFER_LENGTH = 2048;
    private ClientUdpSender sender;
    private String targetUsername;
    private String srcUsername;
    private boolean isRunning = true;

    public VoiceCaptureThread(ClientUdpSender sender, String targetUsername, String srcUsername) {
        this.sender = sender;
        this.targetUsername = targetUsername;
        this.srcUsername = srcUsername;
    }

    @Override
    public void run() {
        AudioFormat format = VoiceConfig.createFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine microphone;
        try {
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
        } catch (LineUnavailableException e) {
            log.fatal("Microphone is unavailable");
            throw new RuntimeException(e);
        }
        while (isRunning) {
            byte[] buffer = new byte[BUFFER_LENGTH];
            int count = microphone.read(buffer, 0, BUFFER_LENGTH);
            sender.send(targetUsername, srcUsername, Arrays.copyOf(buffer, count));
        }
    }
}
