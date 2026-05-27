package org.vladproj.entity;

import lombok.*;

import java.net.InetAddress;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ClientInfo {
    private InetAddress address;
    private int udpPort;
    private volatile long lastSeen;

    public ClientInfo(InetAddress address, int udpPort) {
        this.address = address;
        this.udpPort = udpPort;
    }
}
