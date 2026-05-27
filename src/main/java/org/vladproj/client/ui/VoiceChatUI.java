package org.vladproj.client.ui;

import org.vladproj.client.connection.ClientSession;
import org.vladproj.client.connection.ClientSettings;
import org.vladproj.entity.PacketType;
import org.vladproj.entity.VoiceUdpPacket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceChatUI extends JFrame {
    private static final Color BACKGROUND = new Color(246, 248, 250);
    private static final Color PANEL = Color.WHITE;
    private static final Color ACCENT = new Color(42, 111, 255);
    private static final Color DANGER = new Color(213, 68, 68);
    private static final Color TEXT = new Color(31, 35, 40);
    private static final Color MUTED = new Color(101, 109, 118);
    private static final String APP_NAME = "Voice Chat";
    private static final String CALL_BUTTON_NAME = "Позвонить";

    private final JPanel messagesPanel = new JPanel();
    private final JTextField targetField = new JTextField();
    private final JLabel statusLabel = new JLabel();
    private final JLabel settingsLabel = new JLabel();
    private final JButton settingsButton = new JButton("Настройки");
    private final JButton connectButton = new JButton("Подключиться");
    private final JButton searchButton = new JButton("🔍");
    private final JButton callButton = new JButton(CALL_BUTTON_NAME);
    private final JPopupMenu searchPopup = new JPopupMenu();
    private final ClientSession session = new ClientSession(this::handleIncomingPacket);
    private final Map<String, ByteArrayOutputStream> callRecordings = new HashMap<>();

    private ClientSettings settings = createDefaultSettings();
    private String pendingIncomingCaller;

    public VoiceChatUI() {
        setTitle(APP_NAME);
        setMinimumSize(new Dimension(780, 560));
        setSize(900, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setContentPane(createContent());
        bindActions();
        updateConnectionState(false, "Не подключен");
        addSystemRow("Готов к подключению");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                session.disconnect();
                dispose();
                System.exit(0);
            }
        });
        setVisible(true);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMessagesArea(), BorderLayout.CENTER);
        root.add(createControls(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(APP_NAME);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        title.setForeground(TEXT);

        settingsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        settingsLabel.setForeground(MUTED);
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(settingsLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        styleSecondaryButton(settingsButton);
        stylePrimaryButton(connectButton, ACCENT);
        actions.add(settingsButton);
        actions.add(connectButton);

        header.add(titles, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createMessagesArea() {
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(BACKGROUND);
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JScrollPane scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        return scrollPane;
    }

    private JPanel createControls() {
        JPanel wrapper = new JPanel(new BorderLayout(14, 0));
        wrapper.setBackground(PANEL);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        JPanel targetPanel = new JPanel(new BorderLayout(8, 0));
        targetPanel.setOpaque(false);
        JLabel targetLabel = new JLabel("Собеседник");
        targetLabel.setForeground(MUTED);
        targetField.setPreferredSize(new Dimension(260, 38));
        targetField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        targetField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(208, 215, 222)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        searchButton.setPreferredSize(new Dimension(42, 38));
        styleSecondaryButton(searchButton);
        targetPanel.add(targetLabel, BorderLayout.WEST);
        targetPanel.add(targetField, BorderLayout.CENTER);
        targetPanel.add(searchButton, BorderLayout.EAST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        stylePrimaryButton(callButton, DANGER);
        callButton.setPreferredSize(new Dimension(150, 38));
        right.add(statusLabel);
        right.add(callButton);

        wrapper.add(targetPanel, BorderLayout.CENTER);
        wrapper.add(right, BorderLayout.EAST);
        return wrapper;
    }

    private void bindActions() {
        settingsButton.addActionListener(e -> openSettings());
        connectButton.addActionListener(e -> toggleConnection());
        searchButton.addActionListener(e -> searchUsers());
        callButton.addActionListener(e -> toggleCall());
    }

    private void openSettings() {
        ClientSettings selected = SettingsDialog.showDialog(this, settings);
        if (selected != null) {
            settings = selected;
            updateSettingsLabel();
            addSystemRow("Настройки обновлены");
        }
    }

    private void toggleConnection() {
        if (session.isConnected()) {
            session.disconnect();
            pendingIncomingCaller = null;
            updateConnectionState(false, "Отключен");
            addSystemRow("Соединение закрыто");
            return;
        }
        connectButton.setEnabled(false);
        settingsButton.setEnabled(false);
        updateConnectionState(false, "Подключение...");
        Thread thread = new Thread(this::connectInBackground, "ui-connect");
        thread.setDaemon(true);
        thread.start();
    }

    private void connectInBackground() {
        boolean connected = false;
        String error = null;
        try {
            connected = session.connect(settings);
            if (!connected) {
                error = "Сервер отклонил регистрацию";
            }
        } catch (RuntimeException e) {
            error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }

        boolean finalConnected = connected;
        String finalError = error;
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(true);
            settingsButton.setEnabled(true);
            if (finalConnected) {
                updateConnectionState(true, "Подключен как " + settings.getUsername());
                addSystemRow("Подключено: " + settings.getUsername());
            } else {
                updateConnectionState(false, "Не подключен");
                addSystemRow("Ошибка подключения: " + finalError);
            }
        });
    }

    private void searchUsers() {
        if (!session.isConnected()) {
            showWarning("Сначала подключитесь к серверу.");
            return;
        }
        String prefix = targetField.getText().trim();
        searchButton.setEnabled(false);
        Thread thread = new Thread(() -> {
            List<String> users = session.searchUsers(prefix);
            SwingUtilities.invokeLater(() -> {
                searchButton.setEnabled(true);
                showSearchResults(users);
            });
        }, "ui-search-users");
        thread.setDaemon(true);
        thread.start();
    }

    private void showSearchResults(List<String> users) {
        searchPopup.removeAll();
        if (users.isEmpty()) {
            searchPopup.add(new JLabel("  Никого не найдено  "));
        } else {
            for (String username : users) {
                JButton item = new JButton(username);
                item.setHorizontalAlignment(JButton.LEFT);
                item.setFocusPainted(false);
                item.addActionListener(e -> {
                    targetField.setText(username);
                    searchPopup.setVisible(false);
                });
                searchPopup.add(item);
            }
        }
        searchPopup.show(targetField, 0, targetField.getHeight());
    }

    private void toggleCall() {
        if (!session.isConnected()) {
            showWarning("Сначала подключитесь к серверу.");
            return;
        }
        if (session.getCallState() == ClientSession.CallState.ACTIVE
                || session.getCallState() == ClientSession.CallState.OUTGOING) {
            String peer = session.getCallPeer();
            saveCallRecording(peer);
            session.endCall();
            pendingIncomingCaller = null;
            updateCallIdle("Звонок завершен");
            addSystemRow("Звонок с " + peer + " завершен");
            return;
        }

        String target = targetField.getText().trim();
        if (target.isBlank()) {
            showWarning("Введите имя собеседника или найдите его через поиск.");
            return;
        }
        if (target.equals(settings.getUsername())) {
            showWarning("Нельзя позвонить самому себе.");
            return;
        }
        if (session.requestCall(target)) {
            callButton.setText("Отменить");
            statusLabel.setText("Вызов " + target + "...");
            addSystemRow("Исходящий вызов: " + target);
        }
    }

    private void handleIncomingPacket(VoiceUdpPacket packet) {
        SwingUtilities.invokeLater(() -> {
            PacketType type = packet.getPacketType();
            if (type == PacketType.VOICE) {
                addVoiceRow(packet);
                return;
            }
            switch (type) {
                case CALL_REQUEST -> handleCallRequest(packet.getSrcUsername());
                case CALL_ACCEPT -> handleCallAccept(packet.getSrcUsername());
                case CALL_REJECT -> handleCallReject(packet.getSrcUsername());
                case CALL_BUSY -> handleCallBusy(packet.getSrcUsername());
                case CALL_END -> handleCallEnd(packet.getSrcUsername());
                default -> {}
            }
        });
    }

    private void handleCallRequest(String caller) {
        if (pendingIncomingCaller != null || session.hasActiveOrPendingCall()) {
            session.sendBusy(caller);
            addSystemRow(caller + " звонил, но вы уже в другом звонке");
            return;
        }
        pendingIncomingCaller = caller;
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Входящий звонок от " + caller + ". Принять?",
                APP_NAME,
                JOptionPane.YES_NO_OPTION
        );
        if (answer == JOptionPane.YES_OPTION) {
            session.acceptIncomingCall(caller);
            pendingIncomingCaller = null;
            callButton.setText("Завершить");
            statusLabel.setText("В звонке с " + caller);
            addSystemRow("Звонок с " + caller + " принят");
        } else {
            session.rejectIncomingCall(caller);
            pendingIncomingCaller = null;
            updateCallIdle("Звонок отклонен");
            addSystemRow("Звонок от " + caller + " отклонен");
        }
    }

    private void handleCallAccept(String peer) {
        session.markCallAccepted(peer);
        callButton.setText("Завершить");
        statusLabel.setText("В звонке с " + peer);
        addSystemRow(peer + " принял звонок");
    }

    private void handleCallReject(String peer) {
        saveCallRecording(peer);
        session.clearRemoteCall();
        updateCallIdle(peer + " отклонил звонок");
        addSystemRow(peer + " отклонил звонок");
    }

    private void handleCallBusy(String peer) {
        saveCallRecording(peer);
        session.clearRemoteCall();
        updateCallIdle(peer + " занят");
        addSystemRow(peer + " уже находится в другом звонке");
    }

    private void handleCallEnd(String peer) {
        saveCallRecording(peer);
        session.clearRemoteCall();
        updateCallIdle("Звонок завершен");
        addSystemRow(peer + " завершил звонок");
    }

    private void addVoiceRow(VoiceUdpPacket packet) {
        callRecordings
                .computeIfAbsent(packet.getSrcUsername(), key -> new ByteArrayOutputStream())
                .writeBytes(packet.getData());
        if (session.getCallState() == ClientSession.CallState.ACTIVE) {
            statusLabel.setText("Получено аудио от " + packet.getSrcUsername());
            return;
        }
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        addRow(new VoiceMessagePanel("Голос от " + packet.getSrcUsername(), time, packet.getData(), false));
        statusLabel.setText("Получено аудио от " + packet.getSrcUsername());
    }

    private void saveCallRecording(String peer) {
        if (peer == null) {
            return;
        }
        ByteArrayOutputStream recording = callRecordings.remove(peer);
        if (recording == null || recording.size() == 0) {
            return;
        }
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        addRow(new VoiceMessagePanel("Запись звонка: " + peer, time, recording.toByteArray(), false));
    }

    private void updateCallIdle(String status) {
        callButton.setText(CALL_BUTTON_NAME);
        statusLabel.setText(status);
    }

    private void updateConnectionState(boolean connected, String statusText) {
        updateSettingsLabel();
        connectButton.setText(connected ? "Отключиться" : "Подключиться");
        searchButton.setEnabled(connected);
        callButton.setEnabled(connected);
        if (!connected) {
            callButton.setText(CALL_BUTTON_NAME);
        }
        statusLabel.setText(statusText);
    }

    private void updateSettingsLabel() {
        settingsLabel.setText(settings.getUsername()
                + " | "
                + settings.getServerAddress().getHostAddress()
                + " TCP:"
                + settings.getTcpServerPort()
                + " UDP:"
                + settings.getUdpServerPort()
                + " | локальный UDP:"
                + settings.getClientUdpPort());
    }

    private void addSystemRow(String text) {
        addRow(new VoiceMessagePanel("Система", text, null, false));
    }

    private void addRow(VoiceMessagePanel panel) {
        messagesPanel.add(panel);
        messagesPanel.add(Box.createVerticalStrut(10));
        messagesPanel.revalidate();
        messagesPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            Container parent = messagesPanel.getParent();
            if (parent instanceof JViewport) {
                JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, messagesPanel);
                if (scrollPane != null) {
                    JScrollBar bar = scrollPane.getVerticalScrollBar();
                    bar.setValue(bar.getMaximum());
                }
            }
        });
    }

    private ClientSettings createDefaultSettings() {
        String username = System.getProperty("user.name", "user");
        return new ClientSettings(username, InetAddress.getLoopbackAddress(), 5000, 4445, 6000);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, APP_NAME, JOptionPane.WARNING_MESSAGE);
    }

    private void stylePrimaryButton(AbstractButton button, Color color) {
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    }

    private void styleSecondaryButton(AbstractButton button) {
        button.setFocusPainted(false);
        button.setForeground(TEXT);
        button.setBackground(new Color(240, 243, 246));
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    }
}
