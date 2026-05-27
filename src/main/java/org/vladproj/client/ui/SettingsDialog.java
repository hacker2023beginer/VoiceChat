package org.vladproj.client.ui;

import org.vladproj.client.connection.ClientSettings;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SettingsDialog extends JDialog {
    private final JTextField usernameField = new JTextField();
    private final JTextField serverAddressField = new JTextField();
    private final JTextField tcpServerPortField = new JTextField();
    private final JTextField udpServerPortField = new JTextField();
    private final JTextField clientUdpPortField = new JTextField();
    private ClientSettings result;

    private SettingsDialog(Window owner, ClientSettings settings) {
        super(owner, "Настройки", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(createContent());
        fill(settings);
        pack();
        setMinimumSize(new Dimension(420, getHeight()));
        setLocationRelativeTo(owner);
    }

    public static ClientSettings showDialog(Component parent, ClientSettings settings) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        SettingsDialog dialog = new SettingsDialog(owner, settings);
        dialog.setVisible(true);
        return dialog.result;
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(Color.WHITE);
        root.add(createForm(), BorderLayout.CENTER);
        root.add(createButtons(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        addField(form, 0, "Имя", usernameField);
        addField(form, 1, "Адрес сервера", serverAddressField);
        addField(form, 2, "TCP порт сервера", tcpServerPortField);
        addField(form, 3, "UDP порт сервера", udpServerPortField);
        addField(form, 4, "Ваш UDP порт", clientUdpPortField);
        return form;
    }

    private JPanel createButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton cancel = new JButton("Отмена");
        JButton save = new JButton("Сохранить");
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> save());
        buttons.add(cancel);
        buttons.add(save);
        return buttons;
    }

    private void addField(JPanel form, int row, String labelText, JTextField field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(0, 0, 10, 12);

        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(101, 109, 118));
        form.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(0, 0, 10, 0);

        field.setColumns(20);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(208, 215, 222)),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)
        ));
        form.add(field, fieldConstraints);
    }

    private void fill(ClientSettings settings) {
        usernameField.setText(settings.getUsername());
        serverAddressField.setText(settings.getServerAddress().getHostAddress());
        tcpServerPortField.setText(String.valueOf(settings.getTcpServerPort()));
        udpServerPortField.setText(String.valueOf(settings.getUdpServerPort()));
        clientUdpPortField.setText(String.valueOf(settings.getClientUdpPort()));
    }

    private void save() {
        try {
            String username = usernameField.getText().trim();
            if (username.isBlank()) {
                throw new IllegalArgumentException("Введите имя пользователя.");
            }

            InetAddress serverAddress = InetAddress.getByName(serverAddressField.getText().trim());
            int tcpServerPort = parsePort(tcpServerPortField.getText(), "TCP порт сервера");
            int udpServerPort = parsePort(udpServerPortField.getText(), "UDP порт сервера");
            int clientUdpPort = parsePort(clientUdpPortField.getText(), "Ваш UDP порт");

            result = new ClientSettings(username, serverAddress, tcpServerPort, udpServerPort, clientUdpPort);
            dispose();
        } catch (UnknownHostException e) {
            showError("Адрес сервера не найден.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private int parsePort(String value, String fieldName) {
        try {
            int port = Integer.parseInt(value.trim());
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException(fieldName + " должен быть от 1 до 65535.");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " должен быть числом.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Настройки", JOptionPane.WARNING_MESSAGE);
    }
}
