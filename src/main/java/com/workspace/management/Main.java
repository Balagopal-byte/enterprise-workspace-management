package com.workspace.management;

import com.formdev.flatlaf.FlatLightLaf;
import com.workspace.management.ui.LoginFrame;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * Application entry point.
 * Initializes FlatLaf Look and Feel and launches LoginFrame on the EDT.
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Configure FlatLaf modern Look and Feel
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
        } catch (Exception e) {
            LOGGER.warning("Could not set FlatLaf L&F: " + e.getMessage());
        }

        // Launch on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
