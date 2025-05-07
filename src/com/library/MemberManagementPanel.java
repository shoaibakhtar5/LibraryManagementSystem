package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

public class MemberManagementPanel extends JPanel {
    private static final Color blue = new Color(0, 87, 183);
    private static final Color orange = new Color(255, 98, 0);
    private static final Color white = Color.WHITE;

    private User user;
    private MemberDAO memberDAO;
    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public MemberManagementPanel(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
        this.memberDAO = new MemberDAO();
        if (this.memberDAO == null) {
            JOptionPane.showMessageDialog(this, "Failed to instantiate MemberDAO", "Error", JOptionPane.ERROR_MESSAGE);
        }
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
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
        JLabel searchLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/search.png"));
            searchLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            searchLabel.setText("🔍");
        }
        searchPanel.add(searchLabel);
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
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
        membersTable = new JTable(tableModel);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersTable.setFont(new Font("Arial", Font.PLAIN, 16));
        membersTable.setRowHeight(30);
        membersTable.setShowGrid(true); // Enable grid lines
        membersTable.setGridColor(Color.LIGHT_GRAY); // Set grid color
        membersTable.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Add border
        membersTable.setIntercellSpacing(new Dimension(1, 1)); // Small spacing
        membersTable.setBackground(white);
        membersTable.setSelectionBackground(new Color(230, 240, 255));
        membersTable.setSelectionForeground(Color.BLACK);

        // Table Header Styling
        membersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        membersTable.getTableHeader().setBackground(new Color(240, 240, 240));
        membersTable.getTableHeader().setForeground(Color.BLACK);
        membersTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        // Alternating Row Colors
        membersTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? white : new Color(245, 245, 245));
                }
                return c;
            }
        });

        // Column Alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        TableColumnModel columnModel = membersTable.getColumnModel();
        columnModel.getColumn(0).setCellRenderer(centerRenderer); // ID
        columnModel.getColumn(3).setCellRenderer(centerRenderer); // Phone

        // Sort by Member ID
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        membersTable.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING))); // Default sort by ID
        sorter.sort();

        // Selection Listener
        membersTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = membersTable.getSelectedRow();
            if (selectedRow >= 0 && !e.getValueIsAdjusting()) {
                // No input fields to populate here, as we align with BookManagementPanel style
            }
        });

        // Mouse Click for Selection
        membersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = membersTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    membersTable.setRowSelectionInterval(row, row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(white);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (user.hasRole("Admin")) {
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
        if (memberDAO != null) {
            try {
                List<Member> members = memberDAO.getAllMembers(user);
                // Sort members by memberId before adding to the table
                members.sort(Comparator.comparingInt(Member::getMemberId));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                for (Member member : members) {
                    if (member.getName().toLowerCase().contains(searchText) ||
                            member.getEmail().toLowerCase().contains(searchText) ||
                            member.getPhone().toLowerCase().contains(searchText)) {
                        tableModel.addRow(new Object[]{
                                member.getMemberId(),
                                member.getName(),
                                member.getEmail(),
                                member.getPhone(),
                                dateFormat.format(member.getJoinDate()),
                                member.getAddress()
                        });
                    }
                }
            } catch (SQLException | SecurityException e) {
                JOptionPane.showMessageDialog(this, "Error loading members: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "MemberDAO is not initialized", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        searchField.setText("");
        searchMembers();
    }

    private void showAddMemberDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Member", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField newNameField = new JTextField(20);
        JTextField newEmailField = new JTextField(20);
        JTextField newPhoneField = new JTextField(20);
        JTextField newAddressField = new JTextField(20);
        JTextField newUsernameField = new JTextField(20);
        JPasswordField newPasswordField = new JPasswordField(20);

        newNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        newEmailField.setFont(new Font("Arial", Font.PLAIN, 16));
        newPhoneField.setFont(new Font("Arial", Font.PLAIN, 16));
        newAddressField.setFont(new Font("Arial", Font.PLAIN, 16));
        newUsernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 16));

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        dialog.add(newNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        dialog.add(newEmailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        dialog.add(newPhoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        dialog.add(newAddressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        dialog.add(newUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        RoundedButton saveButton = new RoundedButton("Save", orange, white, "save.png");
        saveButton.addActionListener(e -> {
            String newName = newNameField.getText().trim();
            String newEmail = newEmailField.getText().trim();
            String newPhone = newPhoneField.getText().trim();
            String newAddress = newAddressField.getText().trim();
            String newUsername = newUsernameField.getText().trim();
            String newPassword = new String(newPasswordField.getPassword()).trim();

            if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty() || newAddress.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(dialog, "Invalid email format", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 6 characters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (memberDAO != null) {
                    if (isUsernameTaken(newUsername)) {
                        JOptionPane.showMessageDialog(dialog, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    memberDAO.addMember(user, newName, newEmail, newPhone, newAddress, newUsername, newPassword);
                    JOptionPane.showMessageDialog(dialog, "Member added successfully");
                    dialog.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "MemberDAO is not initialized", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        dialog.add(saveButton, gbc);

        dialog.setVisible(true);
    }

    private void showUpdateMemberDialog() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a member to update", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int memberId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String email = (String) tableModel.getValueAt(selectedRow, 2);
        String phone = (String) tableModel.getValueAt(selectedRow, 3);
        String address = (String) tableModel.getValueAt(selectedRow, 5);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Member", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField updatedNameField = new JTextField(name, 20);
        JTextField updatedEmailField = new JTextField(email, 20);
        JTextField updatedPhoneField = new JTextField(phone, 20);
        JTextField updatedAddressField = new JTextField(address, 20);

        updatedNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        updatedEmailField.setFont(new Font("Arial", Font.PLAIN, 16));
        updatedPhoneField.setFont(new Font("Arial", Font.PLAIN, 16));
        updatedAddressField.setFont(new Font("Arial", Font.PLAIN, 16));

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        dialog.add(updatedNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        dialog.add(updatedEmailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        dialog.add(updatedPhoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        dialog.add(updatedAddressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        RoundedButton saveButton = new RoundedButton("Save", orange, white, "save.png");
        saveButton.addActionListener(e -> {
            String updatedName = updatedNameField.getText().trim();
            String updatedEmail = updatedEmailField.getText().trim();
            String updatedPhone = updatedPhoneField.getText().trim();
            String updatedAddress = updatedAddressField.getText().trim();

            if (updatedName.isEmpty() || updatedEmail.isEmpty() || updatedPhone.isEmpty() || updatedAddress.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!updatedEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(dialog, "Invalid email format", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (memberDAO != null) {
                    memberDAO.updateMember(user, memberId, updatedName, updatedEmail, updatedPhone, updatedAddress);
                    JOptionPane.showMessageDialog(dialog, "Member updated successfully");
                    dialog.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "MemberDAO is not initialized", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        dialog.add(saveButton, gbc);

        dialog.setVisible(true);
    }

    private void deleteMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a member to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int memberId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete member ID " + memberId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (memberDAO != null) {
                    memberDAO.deleteMember(user, memberId);
                    JOptionPane.showMessageDialog(this, "Member deleted successfully");
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(this, "MemberDAO is not initialized", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private boolean isUsernameTaken(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}