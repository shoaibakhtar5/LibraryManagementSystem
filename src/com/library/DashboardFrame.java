package com.library;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private User user;
    private JPanel contentPanel;
    private JPanel sidebar;
    private boolean sidebarCollapsed = false;
    private final int SIDEBAR_WIDTH = 250;
    private final int SIDEBAR_COLLAPSED_WIDTH = 60;

    public DashboardFrame(User user) {
        this.user = user;
        initUI();
    }

    private void initUI() {
        setTitle("Library Management System - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Colors
        Color blue = new Color(0, 87, 183);
        Color orange = new Color(255, 98, 0);
        Color white = Color.WHITE;

        // Gradient Header
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, blue, 0, getHeight(), new Color(0, 48, 135));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        JLabel headerLabel = new JLabel("Welcome, " + user.getUsername() + " (" + user.getRole() + ")", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 32));
        headerLabel.setForeground(white);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Sidebar
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));

        // Toggle Button
        RoundedButton toggleButton = new RoundedButton("", orange, white, "menu.png");
        toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        toggleButton.addActionListener(e -> toggleSidebar());
        sidebar.add(toggleButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // Navigation Buttons
        if (user.hasRole("Admin")) {
            addNavButton(sidebar, "Manage Books", orange, white, "books.png", e -> showBookManagement());
            addNavButton(sidebar, "Manage Members", orange, white, "members.png", e -> showMemberManagement());
            addNavButton(sidebar, "Manage Users", orange, white, "users.png", e -> showPlaceholder("Users"));
            addNavButton(sidebar, "Manage Staff", orange, white, "staff.png", e -> showPlaceholder("Staff"));
        }
        if (user.hasRole("Admin") || user.hasRole("Staff")) {
            addNavButton(sidebar, "Issue/Return Books", orange, white, "transactions.png", e -> showPlaceholder("Transactions"));
            addNavButton(sidebar, "Reservations", orange, white, "reservations.png", e -> showReservations());
            addNavButton(sidebar, "Manage Fines", orange, white, "fines.png", e -> showFinesManagement());
        }
        if (user.hasRole("Member")) {
            addNavButton(sidebar, "View Books", orange, white, "books.png", e -> showBookManagement());
            addNavButton(sidebar, "My Reservations", orange, white, "reservations.png", e -> showReservations());
            addNavButton(sidebar, "My Fines", orange, white, "fines.png", e -> showFinesManagement());
            addNavButton(sidebar, "Submit Review", orange, white, "reviews.png", e -> showPlaceholder("Reviews"));
        }
        addNavButton(sidebar, "Logout", orange, white, "logout.png", e -> logout());

        add(sidebar, BorderLayout.WEST);

        // Content Panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(white);
        contentPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        add(contentPanel, BorderLayout.CENTER);

        // Default Content
        showPlaceholder("Dashboard");
    }

    private void addNavButton(JPanel panel, String text, Color bg, Color fg, String iconPath, java.awt.event.ActionListener listener) {
        RoundedButton button = new RoundedButton(text, bg, fg, iconPath);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.addActionListener(listener);
        panel.add(button);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        sidebar.setPreferredSize(new Dimension(sidebarCollapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_WIDTH, 0));
        for (Component comp : sidebar.getComponents()) {
            if (comp instanceof RoundedButton && !comp.equals(sidebar.getComponent(0))) {
                ((RoundedButton) comp).setText(sidebarCollapsed ? "" : ((RoundedButton) comp).getText());
            }
        }
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void showBookManagement() {
        contentPanel.removeAll();
        contentPanel.add(new BookManagementPanel(user));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showMemberManagement() {
        contentPanel.removeAll();
        contentPanel.add(new MemberManagementPanel(user));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showReservations() {
        contentPanel.removeAll();
        contentPanel.add(new ReservationsPanel(user));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showFinesManagement() {
        contentPanel.removeAll();
        contentPanel.add(new FinesPanel(user));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showPlaceholder(String section) {
        contentPanel.removeAll();
        JLabel label = new JLabel("Placeholder: " + section + " Management", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 24));
        contentPanel.add(label);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}