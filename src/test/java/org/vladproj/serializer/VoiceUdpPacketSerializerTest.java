package org.vladproj.serializer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.vladproj.entity.PacketType;
import org.vladproj.entity.VoiceUdpPacket;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VoiceUdpPacketSerializerTest {
    private VoiceUdpPacket packet = new VoiceUdpPacket(PacketType.VOICE, "Vlados", "Hello".getBytes(StandardCharsets.UTF_8));
    private VoiceUdpPacketSerializer serializer = new VoiceUdpPacketSerializer();

    @Test
    void shouldSerializeAndDeserializeSuccessfully(){
        byte[] array = serializer.serialize(packet);
        VoiceUdpPacket newPacket = serializer.deserialize(array);
        assertAll(
                () -> assertEquals(packet.getPacketType(), newPacket.getPacketType()),
                () -> assertEquals(packet.getUsername(), newPacket.getUsername()),
                () -> assertArrayEquals(packet.getData(), newPacket.getData())
        );
    }
}
