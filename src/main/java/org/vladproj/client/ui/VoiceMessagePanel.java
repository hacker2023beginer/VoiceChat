package org.vladproj.client.ui;

import org.vladproj.client.voice.VoicePlayer;
import org.vladproj.entity.VoiceMessage;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VoiceMessagePanel extends JPanel {
    private static final Color OWN_BACKGROUND = new Color(232, 240, 255);
    private static final Color INCOMING_BACKGROUND = Color.WHITE;
    private static final Color BORDER = new Color(208, 215, 222);
    private static final Color TEXT = new Color(31, 35, 40);
    private static final Color MUTED = new Color(101, 109, 118);

    public VoiceMessagePanel(VoiceMessage message) {
        this(
                "Голос от " + message.getSender(),
                new SimpleDateFormat("HH:mm:ss").format(new Date(message.getTimestamp())),
                message.getData(),
                false
        );
    }

    public VoiceMessagePanel(String title, String details, byte[] audioData, boolean own) {
        setLayout(new BorderLayout(12, 0));
        setBackground(own ? OWN_BACKGROUND : INCOMING_BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        JLabel detailsLabel = new JLabel(details);
        detailsLabel.setForeground(MUTED);
        detailsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(detailsLabel);
        add(textPanel, BorderLayout.CENTER);

        if (audioData != null && audioData.length > 0) {
            JButton playButton = new JButton("PLAY");
            playButton.setFocusPainted(false);
            playButton.addActionListener(e -> VoicePlayer.play(audioData));
            add(playButton, BorderLayout.EAST);
        }
    }
}
