package org.vladproj.client.voice;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class VoiceRecorder {
    private TargetDataLine microphone;
    private ByteArrayOutputStream outputStream;
    private boolean recording;

    public void startRecording() {
        try {
            AudioFormat format = VoiceConfig.createFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            outputStream = new ByteArrayOutputStream();
            recording = true;
            Thread thread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (recording) {
                    int count =
                            microphone.read(
                                    buffer,
                                    0,
                                    buffer.length
                            );
                    if (count > 0) {
                        outputStream.write(buffer, 0, count);
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] stopRecording() {
        if (!recording) {
            return new byte[0];
        }
        recording = false;
        microphone.stop();
        microphone.close();
        return outputStream.toByteArray();
    }
}
