package trash.client.voice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class VoicePlayer {
    private static final Logger log = LogManager.getLogger();
    private static SourceDataLine speakers;

    static {
        initializeSpeakers();
    }

    /**
     * Инициализирует SourceDataLine один раз для переиспользования
     */
    private static void initializeSpeakers() {
        try {
            AudioFormat format = VoiceConfig.createFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
            speakers.start();
            log.info("Audio output initialized");
        } catch (Exception e) {
            log.error("Failed to initialize audio output", e);
            speakers = null;
        }
    }

    /**
     * Воспроизводит аудиоданные, переиспользуя существующую SourceDataLine
     */
    public static void play(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return;
        }

        if (speakers == null || !speakers.isOpen()) {
            try {
                initializeSpeakers();
            } catch (Exception e) {
                log.error("Cannot reinitialize speakers", e);
                return;
            }
        }

        try {
            synchronized (VoicePlayer.class) {
                speakers.write(audioData, 0, audioData.length);
            }
        } catch (Exception e) {
            log.error("Error playing audio", e);
        }
    }

    /**
     * Закрывает аудиовыход (вызывается при завершении приложения)
     */
    public static void shutdown() {
        if (speakers != null && speakers.isOpen()) {
            speakers.drain();
            speakers.stop();
            speakers.close();
            speakers = null;
            log.info("Audio output closed");
        }
    }
}