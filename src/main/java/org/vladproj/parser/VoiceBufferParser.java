package org.vladproj.parser;

import java.nio.charset.StandardCharsets;

public class VoiceBufferParser {
    public int findDataBound(byte[] voiceBuffer, int bufferLength) {
        int usernameBound = -1;
        for (int i = 0; i < bufferLength; i++) {
            if (voiceBuffer[i] == '|') {
                usernameBound = i;
                break;
            }
        }
        return usernameBound;
    }

    public byte[] findAudioData(byte[] voiceBuffer, int bound, int bufferLength) {
        byte[] audioData = new byte[bufferLength - bound - 1];
        System.arraycopy(voiceBuffer, bound + 1, audioData, 0, audioData.length);
        return audioData;
    }

    public String findUsername(byte[] voiceBuffer, int bound) {
        return new String(voiceBuffer, 0, bound, StandardCharsets.UTF_8);
    }
}
