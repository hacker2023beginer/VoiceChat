package org.vladproj.entity;

import java.net.InetAddress;
import java.util.Objects;

public class ClientInfo {
    private InetAddress address;
    private int udpPort;

    public ClientInfo() {
    }

    public ClientInfo(InetAddress address, int udpPort) {
        this.address = address;
        this.udpPort = udpPort;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientInfo)) return false;

        ClientInfo clientInfo = (ClientInfo) o;
        return udpPort == clientInfo.udpPort && Objects.equals(address, clientInfo.address);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(address);
        result = 31 * result + udpPort;
        return result;
    }

    @Override
    public String toString() {
        return "Client{" +
                "address=" + address +
                ", udpPort=" + udpPort +
                '}';
    }
}
