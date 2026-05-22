package trash.client.connection;

import org.vladproj.entity.VoiceUdpPacket;
import trash.client.voice.VoiceCaptureThread;

import java.util.List;
import java.util.function.Consumer;

public class ClientSession {
    public enum CallState {
        IDLE, OUTGOING, INCOMING, ACTIVE
    }

    private final Consumer<VoiceUdpPacket> incomingPacketListener;
    private ClientSettings settings;
    private ClientHeartbeatThread heartbeatThread;
    private ClientUdpReceiver receiver;
    private ClientUdpSender sender;
    private VoiceCaptureThread captureThread;
    private CallState callState = CallState.IDLE;
    private String callPeer;

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

    public List<String> searchUsers(String prefix) {
        if (!isConnected()) {
            return List.of();
        }
        ClientTcp tcpClient = new ClientTcp(
                settings.getServerAddress(),
                settings.getTcpServerPort(),
                settings.getUsername(),
                settings.getClientUdpPort()
        );
        return tcpClient.searchUsers(prefix);
    }

    public boolean requestCall(String targetUsername) {
        if (!isConnected() || callState != CallState.IDLE || targetUsername == null || targetUsername.isBlank()) {
            return false;
        }
        callPeer = targetUsername;
        callState = CallState.OUTGOING;
        sender.sendCallRequest(targetUsername, settings.getUsername());
        return true;
    }

    public void acceptIncomingCall(String caller) {
        if (!isConnected() || caller == null || caller.isBlank()) {
            return;
        }
        callPeer = caller;
        callState = CallState.ACTIVE;
        sender.sendCallAccept(caller, settings.getUsername());
        startTalking(caller);
    }

    public void rejectIncomingCall(String caller) {
        if (!isConnected() || caller == null || caller.isBlank()) {
            return;
        }
        sender.sendCallReject(caller, settings.getUsername());
        clearCall();
    }

    public void sendBusy(String caller) {
        if (isConnected() && caller != null && !caller.isBlank()) {
            sender.sendCallBusy(caller, settings.getUsername());
        }
    }

    public void markCallAccepted(String peer) {
        if (!isConnected() || peer == null || peer.isBlank()) {
            return;
        }
        callPeer = peer;
        callState = CallState.ACTIVE;
        startTalking(peer);
    }

    public void clearRemoteCall() {
        clearCall();
    }

    public void endCall() {
        String peer = callPeer;
        if (isConnected() && peer != null && !peer.isBlank()) {
            sender.sendCallEnd(peer, settings.getUsername());
        }
        clearCall();
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
        String peer = callPeer;
        if (sender != null && settings != null && peer != null && !peer.isBlank()) {
            try {
                sender.sendCallEnd(peer, settings.getUsername());
            } catch (RuntimeException ignored) {
                // Socket may already be closed while the UI is shutting down.
            }
        }
        endCallSilently();
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
        clearCall();
    }

    private void endCallSilently() {
        stopTalking();
        callPeer = null;
        callState = CallState.IDLE;
    }

    private void clearCall() {
        stopTalking();
        callPeer = null;
        callState = CallState.IDLE;
    }

    public boolean isConnected() {
        return settings != null && sender != null;
    }

    public boolean isTalking() {
        return captureThread != null;
    }

    public boolean hasActiveOrPendingCall() {
        return callState != CallState.IDLE;
    }

    public CallState getCallState() {
        return callState;
    }

    public String getCallPeer() {
        return callPeer;
    }

    public ClientSettings getSettings() {
        return settings;
    }
}
