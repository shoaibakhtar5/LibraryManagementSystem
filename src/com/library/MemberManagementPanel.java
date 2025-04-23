package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;

public class MemberManagementPanel extends JPanel {
    private final User user;
    private DefaultTableModel tableModel;
    private JTextField nameField, emailField, phoneField, addressField, usernameField;
    private JPasswordField passwordField;
    private JTable membersTable;

    public MemberManagementPanel(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Colors
        Color blue = new Color(0, 87, 183);
        Color orange = new Color(255, 98, 0);
        Color white = Color.WHITE;

        // Header
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
        JLabel headerLabel = new JLabel("Member Management", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(white);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels and Fields
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        inputPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        inputPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(20);
        inputPanel.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressField = new JTextField(20);
        inputPanel.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        inputPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        inputPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        inputPanel.add(passwordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(white);

        RoundedButton addButton = new RoundedButton("Add Member", orange, white, null);
        addButton.addActionListener(e -> addMember());
        buttonPanel.add(addButton);

        RoundedButton updateButton = new RoundedButton("Update Member", orange, white, null);
        updateButton.addActionListener(e -> updateMember());
        buttonPanel.add(updateButton);

        RoundedButton deleteButton = new RoundedButton("Delete Member", orange, white, null);
        deleteButton.addActionListener(e -> deleteMember());
        buttonPanel.add(deleteButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);

        add(inputPanel, BorderLayout.WEST);

        // Table
        String[] columns = {"ID", "Name", "Email", "Phone", "Address"};
        tableModel = new DefaultTableModel(columns, 0);
        membersTable = new JTable(tableModel);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = membersTable.getSelectedRow();
            if (selectedRow >= 0) {
                nameField.setText((String) tableModel.getValueAt(selectedRow, 1));
                emailField.setText((String) tableModel.getValueAt(selectedRow, 2));
                phoneField.setText((String) tableModel.getValueAt(selectedRow, 3));
                addressField.setText((String) tableModel.getValueAt(selectedRow, 4));
                usernameField.setText("");
                passwordField.setText("");
            }
        });
        JScrollPane scrollPane = new JScrollPane(membersTable);
        add(scrollPane, BorderLayout.CENTER);

        loadMembers();
    }

    private void loadMembers() {
        try {
            MemberDAO memberDAO = new MemberDAO();
            List<Member> members = memberDAO.getAllMembers(user);
            tableModel.setRowCount(0);
            for (Member member : members) {
                tableModel.addRow(new Object[]{
                        member.getMemberId(),
                        member.getName(),
                        member.getEmail(),
                        member.getPhone(),
                        member.getAddress()
                });
            }
        } catch (SQLException | SecurityException e) {
            JOptionPane.showMessageDialog(this, "Error loading members: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addMember() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            MemberDAO memberDAO = new MemberDAO();
            if (isUsernameTaken(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            memberDAO.addMember(user, name, email, phone, address, username, password);
            JOptionPane.showMessageDialog(this, "Member added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadMembers();
            clearFields();
        } catch (SQLException | SecurityException e) {
            JOptionPane.showMessageDialog(this, "Error adding member: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isUsernameTaken(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private void updateMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a member to update", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int memberId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            MemberDAO memberDAO = new MemberDAO();
            memberDAO.updateMember(user, memberId, name, email, phone, address);
            JOptionPane.showMessageDialog(this, "Member updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadMembers();
            clearFields();
        } catch (SQLException | SecurityException e) {
            JOptionPane.showMessageDialog(this, "Error updating member: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a member to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int memberId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this member?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                MemberDAO memberDAO = new MemberDAO();
                memberDAO.deleteMember(user, memberId);
                JOptionPane.showMessageDialog(this, "Member deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMembers();
                clearFields();
            } catch (SQLException | SecurityException e) {
                JOptionPane.showMessageDialog(this, "Error deleting member: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }
}