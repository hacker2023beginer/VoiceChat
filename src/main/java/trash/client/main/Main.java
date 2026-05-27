package trash.client.main;

import trash.client.ui.VoiceChatUI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VoiceChatUI::new);
    }
}
