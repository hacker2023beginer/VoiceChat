package trash.client.voice;

import javax.sound.sampled.AudioFormat;

public class VoiceConfig {
    public static AudioFormat createFormat() {
        float sampleRate = 16000.0f;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(
                sampleRate,
                sampleSizeInBits,
                channels,
                signed,
                bigEndian
        );
    }
}
