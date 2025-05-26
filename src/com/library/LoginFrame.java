package com.library;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private LoginPanel loginPanel;

    public LoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Library Management System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set size and center
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(20, 20, 20),
                        getWidth(), getHeight(), new Color(50, 25, 0)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Login Panel
        loginPanel = new LoginPanel(this, () -> {
            User loggedInUser = loginPanel.getLoggedInUser();
            if (loggedInUser != null) {
                dispose();
                new DashboardFrame(loggedInUser).setVisible(true);
            }
        });

        mainPanel.add(loginPanel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }
}