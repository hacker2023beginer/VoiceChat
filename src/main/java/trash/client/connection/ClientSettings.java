package trash.client.connection;

import java.net.InetAddress;

public class ClientSettings {
    private final String username;
    private final InetAddress serverAddress;
    private final int tcpServerPort;
    private final int udpServerPort;
    private final int clientUdpPort;

    public ClientSettings(String username, InetAddress serverAddress, int tcpServerPort, int udpServerPort, int clientUdpPort) {
        this.username = username;
        this.serverAddress = serverAddress;
        this.tcpServerPort = tcpServerPort;
        this.udpServerPort = udpServerPort;
        this.clientUdpPort = clientUdpPort;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public int getTcpServerPort() {
        return tcpServerPort;
    }

    public int getUdpServerPort() {
        return udpServerPort;
    }

    public int getClientUdpPort() {
        return clientUdpPort;
    }
}
