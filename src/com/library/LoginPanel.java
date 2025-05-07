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
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(18, 18, 18)); // Black background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(new Color(30, 30, 30)); // Dark gray card
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setPreferredSize(new Dimension(450, 420));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 111, 0), 2), // Orange border
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        Font font = new Font("Segoe UI", Font.PLAIN, 16);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);

        // Logo or title
        JLabel logoLabel = new JLabel("Library Login");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        cardPanel.add(logoLabel, gbc);

        // Username Label + Field
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        usernameLabel.setForeground(Color.WHITE);
        cardPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.setFont(font);
        usernameField.setBackground(new Color(50, 50, 50));
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        usernameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cardPanel.add(usernameField, gbc);

        // Password Label + Field
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(Color.WHITE);
        cardPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(font);
        passwordField.setBackground(new Color(50, 50, 50));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setEchoChar('•');
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cardPanel.add(passwordField, gbc);

        // Role Label + ComboBox
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(labelFont);
        roleLabel.setForeground(Color.WHITE);
        cardPanel.add(roleLabel, gbc);

        gbc.gridx = 1;
        String[] roles = {"Member", "Staff", "Admin"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(font);
        roleComboBox.setBackground(new Color(50, 50, 50));
        roleComboBox.setForeground(Color.WHITE);
        roleComboBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cardPanel.add(roleComboBox, gbc);

        // Show Password Checkbox
        gbc.gridy = 4;
        gbc.gridx = 1;
        JCheckBox showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        showPasswordCheckBox.setForeground(Color.ORANGE);
        showPasswordCheckBox.setBackground(new Color(30, 30, 30));
        showPasswordCheckBox.setFocusPainted(false);
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        cardPanel.add(showPasswordCheckBox, gbc);

        // Login Button
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loginButton.setBackground(new Color(255, 111, 0));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(255, 143, 0));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(255, 111, 0));
            }
        });

        loginButton.addActionListener(e -> login());
        cardPanel.add(loginButton, gbc);

        // Add to main panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(cardPanel, gbc);
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
                parentFrame.getContentPane().removeAll();
                parentFrame.add(new DashboardPanel());
                parentFrame.revalidate();
                parentFrame.repaint();
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
