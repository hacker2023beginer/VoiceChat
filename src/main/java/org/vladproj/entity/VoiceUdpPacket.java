package org.vladproj.entity;

import lombok.*;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoiceUdpPacket {
    private PacketType packetType;
    private String destUsername;
    private String srcUsername;
    private byte[] data;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoiceUdpPacket)) return false;

        VoiceUdpPacket that = (VoiceUdpPacket) o;
        return packetType == that.packetType && Objects.equals(destUsername, that.destUsername) && Objects.equals(srcUsername, that.srcUsername) && Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(packetType);
        result = 31 * result + Objects.hashCode(destUsername);
        result = 31 * result + Objects.hashCode(srcUsername);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
