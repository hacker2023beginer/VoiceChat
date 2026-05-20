package org.vladproj.client.ui;

import org.vladproj.client.connection.ClientSession;
import org.vladproj.client.connection.ClientSettings;
import org.vladproj.entity.VoiceUdpPacket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class VoiceChatUI extends JFrame {
    private static final Color BACKGROUND = new Color(246, 248, 250);
    private static final Color PANEL = Color.WHITE;
    private static final Color ACCENT = new Color(42, 111, 255);
    private static final Color DANGER = new Color(213, 68, 68);
    private static final Color TEXT = new Color(31, 35, 40);
    private static final Color MUTED = new Color(101, 109, 118);
    private static final long INCOMING_ROW_DELAY_MS = 1500L;
    private static final String APP_NAME = "Voice Chat";
    private static final String TALK_BUTTON_NAME = "Начать разговор";

    private final JPanel messagesPanel = new JPanel();
    private final JTextField targetField = new JTextField();
    private final JLabel statusLabel = new JLabel();
    private final JLabel settingsLabel = new JLabel();
    private final JButton settingsButton = new JButton("Настройки");
    private final JButton connectButton = new JButton("Подключиться");
    private final JToggleButton talkButton = new JToggleButton(TALK_BUTTON_NAME);
    private final ClientSession session = new ClientSession(this::handleIncomingPacket);
    private final Map<String, Long> lastIncomingRows = new HashMap<>();

    private ClientSettings settings = createDefaultSettings();

    public VoiceChatUI() {
        setTitle(APP_NAME);
        setMinimumSize(new Dimension(760, 560));
        setSize(880, 640);
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
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BACKGROUND);
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMessagesArea(), BorderLayout.CENTER);
        root.add(createControls(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent createHeader() {
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

    private JComponent createMessagesArea() {
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(BACKGROUND);
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JScrollPane scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        return scrollPane;
    }

    private JComponent createControls() {
        JPanel wrapper = new JPanel(new BorderLayout(14, 0));
        wrapper.setBackground(PANEL);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        JPanel targetPanel = new JPanel(new BorderLayout(8, 0));
        targetPanel.setOpaque(false);
        JLabel targetLabel = new JLabel("Собеседник");
        targetLabel.setForeground(MUTED);
        targetField.setPreferredSize(new Dimension(240, 38));
        targetField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        targetField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(208, 215, 222)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        targetPanel.add(targetLabel, BorderLayout.WEST);
        targetPanel.add(targetField, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        stylePrimaryButton(talkButton, DANGER);
        talkButton.setPreferredSize(new Dimension(170, 38));
        right.add(statusLabel);
        right.add(talkButton);

        wrapper.add(targetPanel, BorderLayout.CENTER);
        wrapper.add(right, BorderLayout.EAST);
        return wrapper;
    }

    private void bindActions() {
        settingsButton.addActionListener(e -> openSettings());
        connectButton.addActionListener(e -> toggleConnection());
        talkButton.addActionListener(e -> toggleTalking());
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

    private void toggleTalking() {
        if (talkButton.isSelected()) {
            String target = targetField.getText().trim();
            if (target.isBlank()) {
                JOptionPane.showMessageDialog(this, "Введите имя собеседника.", APP_NAME, JOptionPane.WARNING_MESSAGE);
                talkButton.setSelected(false);
                return;
            }
            session.startTalking(target);
            talkButton.setText("Остановить");
            statusLabel.setText("Идет передача");
            addOwnRow("Вы -> " + target, "разговор начат");
        } else {
            stopTalkingFromUi();
        }
    }

    private void stopTalkingFromUi() {
        session.stopTalking();
        talkButton.setText(TALK_BUTTON_NAME);
        if (session.isConnected()) {
            statusLabel.setText("Подключен");
        }
        addOwnRow("Вы", "передача остановлена");
    }

    private void handleIncomingPacket(VoiceUdpPacket packet) {
        SwingUtilities.invokeLater(() -> {
            String sender = packet.getSrcUsername();
            statusLabel.setText("Входящий голос: " + sender);
            long now = System.currentTimeMillis();
            Long lastRowAt = lastIncomingRows.get(sender);
            if (lastRowAt == null || now - lastRowAt > INCOMING_ROW_DELAY_MS) {
                addIncomingRow(sender, "входящий голос");
                lastIncomingRows.put(sender, now);
            }
        });
    }

    private void updateConnectionState(boolean connected, String statusText) {
        updateSettingsLabel();
        connectButton.setText(connected ? "Отключиться" : "Подключиться");
        talkButton.setEnabled(connected);
        if (!connected) {
            talkButton.setSelected(false);
            talkButton.setText(TALK_BUTTON_NAME);
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

    private void addOwnRow(String title, String text) {
        addRow(new VoiceMessagePanel(title, text, null, true));
    }

    private void addIncomingRow(String sender, String text) {
        addRow(new VoiceMessagePanel(sender, text, null, false));
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
