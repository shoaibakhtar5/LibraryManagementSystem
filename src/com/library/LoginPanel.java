package com.library;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JFrame parentFrame;
    private Runnable onLoginSuccess;
    private User loggedInUser;

    public LoginPanel(JFrame parentFrame, Runnable onLoginSuccess) {
        this.parentFrame = parentFrame;
        this.onLoginSuccess = onLoginSuccess;
        setOpaque(false);
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Main card panel
        JPanel cardPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Solid background with border
                g2d.setColor(new Color(40, 40, 40, 230));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Orange border
                g2d.setColor(new Color(255, 120, 0));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
            }
        };
        cardPanel.setPreferredSize(new Dimension(450, 500));
        cardPanel.setOpaque(false);

        // Form components
        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);

        // Title
        JLabel titleLabel = new JLabel("LIBRARY LOGIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 160, 0));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER); // 👈 Center the text

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 30, 0);
        cardPanel.add(titleLabel, gbc);

// Reset insets
        gbc.insets = new Insets(10, 20, 10, 20);

        // Username field
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        addFormField(cardPanel, gbc, "Username:", usernameField = new JTextField(15), labelFont, fieldFont);

        // Password field
        gbc.gridy = 2;
        addFormField(cardPanel, gbc, "Password:", passwordField = new JPasswordField(15), labelFont, fieldFont);

        // Role selection
        // Role selection
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;  // Make sure this is set to 1
        gbc.anchor = GridBagConstraints.LINE_START;  // Align label to the left
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(labelFont);
        roleLabel.setForeground(Color.WHITE);
        cardPanel.add(roleLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Allow combo box to expand
        gbc.weightx = 1.0;  // Give combo box more space
        String[] roles = {"Member", "Staff", "Admin"};
        roleComboBox = new JComboBox<>(roles);
        styleComboBox(roleComboBox);

// 🔧 Minimal fix: set background to make arrow visible
        roleComboBox.setBackground(new Color(50, 50, 50));  // Visible against dark backgrounds
        roleComboBox.setForeground(Color.ORANGE);           // Optional: makes text match theme

        cardPanel.add(roleComboBox, gbc);

// Reset fill and weight for next components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;


        // Show password checkbox
        gbc.gridy = 4;
        gbc.gridx = 1;
        JCheckBox showPassword = new JCheckBox("Show Password");
        styleCheckBox(showPassword);
        showPassword.addActionListener(e -> {
            passwordField.setEchoChar(showPassword.isSelected() ? (char)0 : '•');
        });
        cardPanel.add(showPassword, gbc);

        // Login button
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 20, 20, 20);
        JButton loginButton = new JButton("LOGIN");
        styleButton(loginButton);
        loginButton.addActionListener(e -> login());
        cardPanel.add(loginButton, gbc);

        add(cardPanel);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, Font labelFont, Font fieldFont) {
        gbc.gridx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(Color.WHITE);
        panel.add(label, gbc);

        gbc.gridx = 1;
        if (field instanceof JTextField) {
            ((JTextField)field).setFont(fieldFont);
            field.setBackground(new Color(60, 60, 60));
            field.setForeground(Color.WHITE);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(80, 80, 80)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        }
        panel.add(field, gbc);
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        combo.setBackground(new Color(60, 60, 60));
        combo.setForeground(Color.WHITE);
        combo.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(255, 120, 0) : new Color(60, 60, 60));
                setForeground(isSelected ? Color.BLACK : Color.WHITE);
                return this;
            }
        });
    }

    private void styleCheckBox(JCheckBox checkBox) {
        checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        checkBox.setForeground(Color.WHITE);
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setBackground(new Color(255, 120, 0));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 140, 0));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 120, 0));
            }
        });
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.authenticate(username, password, role);
            if (user != null) {
                loggedInUser = user;
                onLoginSuccess.run();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}