package trash.client.ui;

import org.vladproj.entity.VoiceMessage;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VoiceMessagePanel extends JPanel {
    private static final Color INCOMING_BACKGROUND = Color.WHITE;
    private static final Color BORDER = new Color(208, 215, 222);
    private static final Color TEXT = new Color(31, 35, 40);
    private static final Color MUTED = new Color(101, 109, 118);

    public VoiceMessagePanel(VoiceMessage message) {
        this(
                "Голос от " + message.getSender(),
                new SimpleDateFormat("HH:mm:ss").format(new Date(message.getTimestamp()))
        );
    }

    public VoiceMessagePanel(String title, String details) {
        setLayout(new BorderLayout(12, 0));
        setBackground(INCOMING_BACKGROUND);
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
    }
}
