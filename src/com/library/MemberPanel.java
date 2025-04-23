package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MemberPanel extends JPanel {
    private final User user;
    private DefaultTableModel finesTableModel;
    private JTable finesTable;

    public MemberPanel(User user) {
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
        JLabel headerLabel = new JLabel("My Fines", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(white);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Fines Table
        String[] columns = {"Fine ID", "Amount", "Reason", "Date", "Status"};
        finesTableModel = new DefaultTableModel(columns, 0);
        finesTable = new JTable(finesTableModel);
        finesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(finesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(white);

        RoundedButton payFineButton = new RoundedButton("Pay Fine", orange, white, null);
        payFineButton.addActionListener(e -> payFine());
        buttonPanel.add(payFineButton);

        RoundedButton refreshButton = new RoundedButton("Refresh", orange, white, null);
        refreshButton.addActionListener(e -> loadFines());
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);

        loadFines();
    }

    private void loadFines() {
        try {
            FinesDAO finesDAO = new FinesDAO();
            List<Fine> fines = finesDAO.getFinesByMember(user.getMemberId());
            finesTableModel.setRowCount(0);
            for (Fine fine : fines) {
                finesTableModel.addRow(new Object[]{
                        fine.getFineId(),
                        fine.getAmount(),
                        fine.getReason(),
                        fine.getFineDate(),
                        fine.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading fines: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void payFine() {
        int selectedRow = finesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a fine to pay", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int fineId = (int) finesTableModel.getValueAt(selectedRow, 0);
        String status = (String) finesTableModel.getValueAt(selectedRow, 4);

        if ("Paid".equals(status)) {
            JOptionPane.showMessageDialog(this, "This fine is already paid", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to pay this fine?", "Confirm Payment", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                FinesDAO finesDAO = new FinesDAO();
                finesDAO.payFine(user, fineId);
                JOptionPane.showMessageDialog(this, "Fine paid successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadFines();
            } catch (SQLException | SecurityException e) {
                JOptionPane.showMessageDialog(this, "Error paying fine: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}