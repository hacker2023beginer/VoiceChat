package org.vladproj.serializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.PacketType;
import org.vladproj.entity.VoiceUdpPacket;
import org.vladproj.exception.VoiceUdpSerializerException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class VoiceUdpPacketSerializer {
    private static final Logger log = LogManager.getLogger();
    private static final int TYPE_BYTE_LENGTH = 1;
    private static final int USERNAME_LENGTH_BYTE_LENGTH = 4;

    public byte[] serialize(VoiceUdpPacket packet) {
        byte[] destUsernameInBytes = packet.getDestUsername().getBytes(StandardCharsets.UTF_8);
        byte[] srcUsernameInBytes = packet.getSrcUsername().getBytes(StandardCharsets.UTF_8);

        int packetLength = TYPE_BYTE_LENGTH + USERNAME_LENGTH_BYTE_LENGTH * 2 +
                destUsernameInBytes.length + srcUsernameInBytes.length
                + packet.getData().length;

        ByteBuffer buffer = ByteBuffer.allocate(packetLength);
        buffer.put((byte) packet.getPacketType().getValue());
        buffer.putInt(destUsernameInBytes.length);
        buffer.put(destUsernameInBytes);
        buffer.putInt(srcUsernameInBytes.length);
        buffer.put(srcUsernameInBytes);
        buffer.put(packet.getData());
        return buffer.array();
    }

    public VoiceUdpPacket deserialize(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        byte[] byteType = new byte[TYPE_BYTE_LENGTH];
        buffer.get(byteType);
        int numOfType = byteType[0];
        Optional<PacketType> optionalType = PacketType.fromValue(numOfType);
        if (optionalType.isEmpty()) {
            log.error("Incorrect type of packet");
            throw new VoiceUdpSerializerException("Incorrect num of packet: " + numOfType);
        }

        int destUsernameLength = buffer.getInt();
        byte[] byteDestUsername = new byte[destUsernameLength];
        buffer.get(byteDestUsername);
        String destUsername = new String(byteDestUsername, StandardCharsets.UTF_8);

        int srcUsernameLength = buffer.getInt();
        byte[] byteSrcUsername = new byte[srcUsernameLength];
        buffer.get(byteSrcUsername);
        String srcUsername = new String(byteSrcUsername, StandardCharsets.UTF_8);

        byte[] voiceData = new byte[buffer.remaining()];
        buffer.get(voiceData);
        return VoiceUdpPacket.builder()
                .packetType(optionalType.get())
                .destUsername(destUsername)
                .srcUsername(srcUsername)
                .data(voiceData)
                .build();
    }
}
