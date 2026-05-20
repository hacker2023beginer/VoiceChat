package org.vladproj.client.connection;

import org.vladproj.client.voice.VoiceCaptureThread;
import org.vladproj.entity.VoiceUdpPacket;

import java.util.function.Consumer;

public class ClientSession {
    private final Consumer<VoiceUdpPacket> incomingPacketListener;
    private ClientSettings settings;
    private ClientHeartbeatThread heartbeatThread;
    private ClientUdpReceiver receiver;
    private ClientUdpSender sender;
    private VoiceCaptureThread captureThread;

    public ClientSession(Consumer<VoiceUdpPacket> incomingPacketListener) {
        this.incomingPacketListener = incomingPacketListener;
    }

    public boolean connect(ClientSettings newSettings) {
        disconnect();
        ClientTcp tcpClient = new ClientTcp(
                newSettings.getServerAddress(),
                newSettings.getTcpServerPort(),
                newSettings.getUsername(),
                newSettings.getClientUdpPort()
        );
        if (!tcpClient.register()) {
            return false;
        }

        settings = newSettings;
        heartbeatThread = new ClientHeartbeatThread(
                "heartbeat-" + settings.getUsername(),
                settings.getUdpServerPort(),
                settings.getServerAddress(),
                settings.getClientUdpPort(),
                settings.getUsername()
        );
        heartbeatThread.setDaemon(true);

        sender = new ClientUdpSender(
                settings.getUdpServerPort(),
                settings.getServerAddress(),
                settings.getClientUdpPort(),
                heartbeatThread.getSocket()
        );
        receiver = new ClientUdpReceiver(
                "udp-receiver-" + settings.getUsername(),
                settings.getClientUdpPort(),
                heartbeatThread.getSocket(),
                incomingPacketListener
        );
        receiver.setDaemon(true);

        heartbeatThread.start();
        receiver.start();
        return true;
    }

    public void startTalking(String targetUsername) {
        if (!isConnected() || isTalking()) {
            return;
        }
        captureThread = new VoiceCaptureThread(sender, targetUsername, settings.getUsername());
        captureThread.setDaemon(true);
        captureThread.start();
    }

    public void stopTalking() {
        if (captureThread != null) {
            captureThread.shutdown();
            captureThread = null;
        }
    }

    public void disconnect() {
        stopTalking();
        if (sender != null && settings != null) {
            try {
                sender.sendDisconnect(settings.getUsername());
            } catch (RuntimeException ignored) {
                // Socket may already be closed while the UI is shutting down.
            }
        }
        if (receiver != null) {
            receiver.shutdown();
            receiver = null;
        }
        if (heartbeatThread != null) {
            heartbeatThread.shutdown();
            heartbeatThread = null;
        }
        sender = null;
        settings = null;
    }

    public boolean isConnected() {
        return settings != null && sender != null;
    }

    public boolean isTalking() {
        return captureThread != null;
    }

    public ClientSettings getSettings() {
        return settings;
    }
}
