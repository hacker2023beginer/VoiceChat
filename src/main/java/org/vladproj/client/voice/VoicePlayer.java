package org.vladproj.client.voice;

import javax.sound.sampled.*;

public class VoicePlayer {

    public static void play(byte[] audioData) {
        try {
            AudioFormat format = VoiceConfig.createFormat();

            DataLine.Info info =
                    new DataLine.Info(SourceDataLine.class, format);

            SourceDataLine speakers =
                    (SourceDataLine) AudioSystem.getLine(info);

            speakers.open(format);
            speakers.start();

            speakers.write(audioData, 0, audioData.length);

            speakers.drain();
            speakers.stop();
            speakers.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}