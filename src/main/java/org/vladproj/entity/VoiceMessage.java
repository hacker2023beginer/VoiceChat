package org.vladproj.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class VoiceMessage {
    private UUID id;
    private String sender;
    private String receiver;
    private byte[] data;
    private Long timestamp;
}
