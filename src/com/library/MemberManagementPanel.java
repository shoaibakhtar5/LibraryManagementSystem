package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

import static java.awt.Color.orange;
import static java.awt.Color.white;

public class MemberManagementPanel extends JPanel {
    private User user;
    private MemberDAO memberDAO;
    private JTable memberTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public MemberManagementPanel(User user) {
        this.user = user;
        this.memberDAO = new MemberDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        Color blue = new Color(0, 87, 183);
        Color orange = new Color(255, 98, 0);
        Color white = Color.WHITE;
        setBackground(white);

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
        JLabel headerLabel = new JLabel("Member Management", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(white);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(white);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel searchLabel = new JLabel(new ImageIcon(new ImageIcon(getClass().getResource("/resources/search.png")).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        searchPanel.add(searchLabel);
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchMembers(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchMembers(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchMembers(); }
        });
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH, 1);

        // Table
        String[] columns = {"ID", "Name", "Email", "Phone", "Join Date", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        memberTable = new JTable(tableModel);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setFont(new Font("Arial", Font.PLAIN, 16));
        memberTable.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(white);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (user.hasRole("Admin") || user.hasRole("Staff")) {
            RoundedButton addButton = new RoundedButton("Add Member", orange, white, "add.png");
            addButton.addActionListener(e -> showAddMemberDialog());
            buttonPanel.add(addButton);

            RoundedButton updateButton = new RoundedButton("Update Member", orange, white, "update.png");
            updateButton.addActionListener(e -> showUpdateMemberDialog());
            buttonPanel.add(updateButton);

            RoundedButton deleteButton = new RoundedButton("Delete Member", orange, white, "delete.png");
            deleteButton.addActionListener(e -> deleteMember());
            buttonPanel.add(deleteButton);
        }
        RoundedButton refreshButton = new RoundedButton("Refresh", blue, white, "refresh.png");
        refreshButton.addActionListener(e -> refreshTable());
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshTable();
    }

    private void searchMembers() {
        String searchText = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        try {
            List<Member> members = memberDAO.getAllMembers(user);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Member member : members) {
                if (member.getName().toLowerCase().contains(searchText) ||
                        member.getEmail().toLowerCase().contains(searchText)) {
                    tableModel.addRow(new Object[]{
                            member.getMemberId(),
                            member.getName(),
                            member.getEmail(),
                            member.getPhone(),
                            sdf.format(member.getJoinDate()),
                            member.getAddress()
                    });
                }
            }
        } catch (SQLException | SecurityException e) {
            JOptionPane.showMessageDialog(this, "Error loading members: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        searchField.setText("");
        searchMembers();
    }

    private boolean validateInputs(String name, String email, String phone) {
        if (name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (phone != null && !phone.isEmpty() && !Pattern.matches("^[0-9]{10}$", phone)) {
            JOptionPane.showMessageDialog(this, "Phone must be 10 digits", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void showAddMemberDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Member", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField addressField = new JTextField(20);

        nameField.setFont(new Font("Arial", Font.PLAIN, 16));
        emailField.setFont(new Font("Arial", Font.PLAIN, 16));
        phoneField.setFont(new Font("Arial", Font.PLAIN, 16));
        addressField.setFont(new Font("Arial", Font.PLAIN, 16));

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(nameLabel, gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(emailLabel, gbc);
        gbc.gridx = 1;
        dialog.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(phoneLabel, gbc);
        gbc.gridx = 1;
        dialog.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(addressLabel, gbc);
        gbc.gridx = 1;
        dialog.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        RoundedButton saveButton = new RoundedButton("Save", orange, white, "save.png");
        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();
            if (!validateInputs(name, email, phone)) {
                return;
            }
            try {
                memberDAO.addMember(user, name, email, phone, address);
                JOptionPane.showMessageDialog(dialog, "Member added successfully");
                dialog.dispose();
                refreshTable();
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(saveButton, gbc);

        dialog.setVisible(true);
    }

    private void showUpdateMemberDialog() {
        int selectedRow = memberTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a member to update", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int memberId = (int) tableModel.getValueAt(selectedRow, 0);
        String selectedName = (String) tableModel.getValueAt(selectedRow, 1);
        String selectedEmail = (String) tableModel.getValueAt(selectedRow, 2);
        String selectedPhone = (String) tableModel.getValueAt(selectedRow, 3);
        String selectedAddress = (String) tableModel.getValueAt(selectedRow, 5);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Member", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(selectedName, 20);
        JTextField emailField = new JTextField(selectedEmail, 20);
        JTextField phoneField = new JTextField(selectedPhone, 20);
        JTextField addressField = new JTextField(selectedAddress, 20);

        nameField.setFont(new Font("Arial", Font.PLAIN, 16));
        emailField.setFont(new Font("Arial", Font.PLAIN, 16));
        phoneField.setFont(new Font("Arial", Font.PLAIN, 16));
        addressField.setFont(new Font("Arial", Font.PLAIN, 16));

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(nameLabel, gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(emailLabel, gbc);
        gbc.gridx = 1;
        dialog.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(phoneLabel, gbc);
        gbc.gridx = 1;
        dialog.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(addressLabel, gbc);
        gbc.gridx = 1;
        dialog.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        RoundedButton saveButton = new RoundedButton("Save", orange, white, "save.png");
        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();
            if (!validateInputs(name, email, phone)) {
                return;
            }
            try {
                memberDAO.updateMember(user, memberId, name, email, phone, address);
                JOptionPane.showMessageDialog(dialog, "Member updated successfully");
                dialog.dispose();
                refreshTable();
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(saveButton, gbc);

        dialog.setVisible(true);
    }

    private void deleteMember() {
        int selectedRow = memberTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a member to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int memberId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete member ID " + memberId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                memberDAO.deleteMember(user, memberId);
                JOptionPane.showMessageDialog(this, "Member deleted successfully");
                refreshTable();
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}