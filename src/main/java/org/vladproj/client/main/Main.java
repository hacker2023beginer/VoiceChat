package org.vladproj.client.main;

import org.vladproj.client.ui.VoiceChatUI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VoiceChatUI::new);
    }
}
