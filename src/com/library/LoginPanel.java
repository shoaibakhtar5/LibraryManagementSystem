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
    private User loggedInUser; // Store the logged-in user for potential use

    public LoginPanel(JFrame parentFrame, Runnable onLoginSuccess) {
        this.parentFrame = parentFrame;
        this.onLoginSuccess = onLoginSuccess;
        initUI();
    }

    private void initUI() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Card-style panel
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setPreferredSize(new Dimension(400, 300));
        cardPanel.setOpaque(true);
        cardPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 50)));

        // Logo
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logo = new ImageIcon(getClass().getResource("/resources/library_logo.png"));
            logoLabel.setIcon(new ImageIcon(logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            logoLabel.setText("Library");
            logoLabel.setFont(new Font("Roboto", Font.BOLD, 24));
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        cardPanel.add(logoLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        cardPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Roboto", Font.PLAIN, 16));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        cardPanel.add(usernameField, gbc);

        // Password
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        cardPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Roboto", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        cardPanel.add(passwordField, gbc);

        // Role
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        cardPanel.add(roleLabel, gbc);

        gbc.gridx = 1;
        String[] roles = {"Member", "Staff", "Admin"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Roboto", Font.PLAIN, 16));
        roleComboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        cardPanel.add(roleComboBox, gbc);

        // Login Button
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        RoundedButton loginButton = new RoundedButton("Login", new Color(0, 107, 204), Color.WHITE, null);
        loginButton.setFont(new Font("Roboto", Font.BOLD, 16));
        loginButton.addActionListener(e -> login());
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(0, 127, 224));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(0, 107, 204));
            }
        });
        cardPanel.add(loginButton, gbc);

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
                loggedInUser = user; // Store the user
                parentFrame.getContentPane().removeAll();
                parentFrame.add(new DashboardPanel());
                parentFrame.revalidate();
                parentFrame.repaint();
                onLoginSuccess.run();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}