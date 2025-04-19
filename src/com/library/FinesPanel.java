package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class FinesPanel extends JPanel {
    private User user;
    private FinesDAO finesDAO;
    private JTable finesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public FinesPanel(User user) {
        this.user = user;
        this.finesDAO = new FinesDAO();
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
        JLabel headerLabel = new JLabel(user.hasRole("Member") ? "My Fines" : "Fines", SwingConstants.CENTER);
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
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchFines(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchFines(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchFines(); }
        });
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH, 1);

        // Table
        String[] columns = {"ID", "Member ID", "Member Name", "Book ID", "Book Title", "Amount", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        finesTable = new JTable(tableModel);
        finesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        finesTable.setFont(new Font("Arial", Font.PLAIN, 16));
        finesTable.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(finesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(white);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (user.hasRole("Admin") || user.hasRole("Staff")) {
            RoundedButton payButton = new RoundedButton("Pay Fine", orange, white, "pay.png");
            payButton.addActionListener(e -> payFine());
            buttonPanel.add(payButton);
        }
        RoundedButton refreshButton = new RoundedButton("Refresh", blue, white, "refresh.png");
        refreshButton.addActionListener(e -> refreshTable());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshTable();
    }

    private void searchFines() {
        String searchText = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        try {
            List<Fine> fines = finesDAO.getFines(user);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Fine fine : fines) {
                if (fine.getMemberName().toLowerCase().contains(searchText) ||
                        fine.getBookTitle().toLowerCase().contains(searchText)) {
                    tableModel.addRow(new Object[]{
                            fine.getFineId(),
                            fine.getMemberId(),
                            fine.getMemberName(),
                            fine.getBookId(),
                            fine.getBookTitle(),
                            fine.getFineAmount(),
                            sdf.format(fine.getFineDate()),
                            fine.getStatus()
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading fines: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        searchField.setText("");
        searchFines();
    }

    private void payFine() {
        int selectedRow = finesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a fine to pay", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int fineId = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 7);
        if (status.equals("Paid")) {
            JOptionPane.showMessageDialog(this, "Fine already paid", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Pay fine ID " + fineId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                finesDAO.payFine(user, fineId);
                JOptionPane.showMessageDialog(this, "Fine paid successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error paying fine: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}