package com.library;

import javax.swing.*;
import java.awt.*;

public class LibraryApp {
    private JFrame frame;

    public LibraryApp() {
        frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // Center window

        // Show login panel
        frame.add(new LoginPanel(this::showMainPanel));
        frame.setVisible(true);
    }

    private void showMainPanel() {
        frame.getContentPane().removeAll();
        JLabel tempLabel = new JLabel("Welcome to the Main App! (To be implemented)", SwingConstants.CENTER);
        frame.add(tempLabel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryApp::new);
    }
}