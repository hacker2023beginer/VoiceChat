package trash.client.connection;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.PacketType;
import org.vladproj.entity.VoiceUdpPacket;
import org.vladproj.exception.DatagramSocketException;
import org.vladproj.serializer.VoiceUdpPacketSerializer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

@Getter
@Setter
public class ClientHeartbeatThread extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static final String EMPTY = "";
    private static final Long HEARTBEAT_TIME_MS = 5000L;
    private static final VoiceUdpPacketSerializer SERIALIZER = new VoiceUdpPacketSerializer();
    private final DatagramSocket socket;
    private final int udpServerPort;
    private final InetAddress serverAddress;
    private final int clientUdpPort;
    private final String srcUsername;
    private volatile boolean running = true;

    public ClientHeartbeatThread(String name, int udpServerPort, InetAddress serverAddress, int port, String srcUsername) {
        super(name);
        log.info("Heartbeat constructor is invoked");
        this.clientUdpPort = port;
        try {
            this.socket = new DatagramSocket(clientUdpPort);
            this.srcUsername = srcUsername;
            this.udpServerPort = udpServerPort;
            this.serverAddress = serverAddress;
        } catch (SocketException e) {
            throw new DatagramSocketException(e);
        }
    }

    @Override
    public void run() {
        log.info("Heartbeat method run is started");
        while (running) {
            VoiceUdpPacket pingPacket = VoiceUdpPacket.builder()
                    .packetType(PacketType.PING)
                    .destUsername(EMPTY)
                    .srcUsername(srcUsername)
                    .data(EMPTY.getBytes())
                    .build();
            byte[] data = SERIALIZER.serialize(pingPacket);
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, udpServerPort);
            try {
                socket.send(packet);
                Thread.sleep(HEARTBEAT_TIME_MS);
            } catch (IOException ioe) {
                if (!running) {
                    break;
                }
                throw new DatagramSocketException(ioe);
            } catch (InterruptedException ie) {
                log.warn("Thread {} is interrupted by some thread", getName());
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("Heartbeat method run is ended");
    }

    public void shutdown() {
        running = false;
        socket.close();
        log.info("Shutdown heartbeat thread {}", getName());
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}
