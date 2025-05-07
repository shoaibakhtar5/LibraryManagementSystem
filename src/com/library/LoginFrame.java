package com.library;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private LoginPanel loginPanel; // Instance field to hold the login panel

    public LoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Library Management System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        // Gradient Background Panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 107, 204), 0, getHeight(), new Color(0, 62, 138));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        add(backgroundPanel);

        // Login Panel with callback to open DashboardFrame
        loginPanel = new LoginPanel(this, () -> {
            User loggedInUser = loginPanel.getLoggedInUser();
            if (loggedInUser != null) {
                dispose(); // Close LoginFrame
                new DashboardFrame(loggedInUser).setVisible(true); // Open DashboardFrame with the logged-in user
            }
        });
        backgroundPanel.add(loginPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}