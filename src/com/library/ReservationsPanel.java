package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static java.awt.Color.orange;
import static java.awt.Color.white;

public class ReservationsPanel extends JPanel {
    private User user;
    private ReservationDAO reservationDAO;
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ReservationsPanel(User user) {
        this.user = user;
        this.reservationDAO = new ReservationDAO();
        this.bookDAO = new BookDAO();
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
        JLabel headerLabel = new JLabel("Reservations", SwingConstants.CENTER);
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
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchReservations(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchReservations(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchReservations(); }
        });
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH, 1);

        // Table
        String[] columns = {"ID", "Book ID", "Book Title", "Member ID", "Member Name", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        reservationTable = new JTable(tableModel);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationTable.setFont(new Font("Arial", Font.PLAIN, 16));
        reservationTable.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(white);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (user.hasRole("Admin") || user.hasRole("Staff") || user.hasRole("Member")) {
            RoundedButton addButton = new RoundedButton("Add", orange, white, "add.png");
            addButton.addActionListener(e -> showAddReservationDialog());
            buttonPanel.add(addButton);

            RoundedButton cancelButton = new RoundedButton("Cancel", orange, white, "cancel.png");
            cancelButton.addActionListener(e -> cancelReservation());
            buttonPanel.add(cancelButton);
        }
        RoundedButton refreshButton = new RoundedButton("Refresh", blue, white, "refresh.png");
        refreshButton.addActionListener(e -> refreshTable());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshTable();
    }

    private void searchReservations() {
        String searchText = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        try {
            List<Reservation> reservations = reservationDAO.getReservations(user);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Reservation reservation : reservations) {
                if (reservation.getBookTitle().toLowerCase().contains(searchText) ||
                        reservation.getMemberName().toLowerCase().contains(searchText)) {
                    tableModel.addRow(new Object[]{
                            reservation.getReservationId(),
                            reservation.getBookId(),
                            reservation.getBookTitle(),
                            reservation.getMemberId(),
                            reservation.getMemberName(),
                            sdf.format(reservation.getReservationDate()),
                            reservation.getStatus()
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading reservations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        searchField.setText("");
        searchReservations();
    }

    private void showAddReservationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Reservation", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Book> bookComboBox = new JComboBox<>();
        JComboBox<Member> memberComboBox = new JComboBox<>();
        bookComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        memberComboBox.setFont(new Font("Arial", Font.PLAIN, 16));

        JTextField bookSearchField = new JTextField(20);
        bookSearchField.setFont(new Font("Arial", Font.PLAIN, 16));
        List<Book> allBooks = new ArrayList<>();
        try {
            allBooks = bookDAO.getAllBooks(user);
            for (Book book : allBooks) {
                bookComboBox.addItem(book);
            }
            if (!user.hasRole("Member")) {
                List<Member> members = memberDAO.getAllMembers(user);
                for (Member member : members) {
                    memberComboBox.addItem(member);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Search bar for books
        List<Book> finalAllBooks = allBooks;
        bookSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterBooks(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterBooks(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterBooks(); }

            private void filterBooks() {
                String searchText = bookSearchField.getText().trim().toLowerCase();
                bookComboBox.removeAllItems();
                for (Book book : finalAllBooks) {
                    if (book.getTitle().toLowerCase().contains(searchText)) {
                        bookComboBox.addItem(book);
                    }
                }
            }
        });

        JTextField memberIdField = new JTextField(20);
        JTextField memberNameField = new JTextField(20);
        memberIdField.setFont(new Font("Arial", Font.PLAIN, 16));
        memberNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        memberIdField.setEditable(false);
        memberNameField.setEditable(false);

        // Update member fields based on user role
        if (user.hasRole("Member")) {
            memberIdField.setText(String.valueOf(user.getMemberId()));
            try {
                Member member = memberDAO.getMemberById(user, user.getMemberId());
                memberNameField.setText(member.getName());
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog, "Error loading member data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            memberComboBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Member selectedMember = (Member) e.getItem();
                    memberIdField.setText(String.valueOf(selectedMember.getMemberId()));
                    memberNameField.setText(selectedMember.getName());
                }
            });
        }

        // Select the first item in the combo boxes to populate fields initially
        if (bookComboBox.getItemCount() > 0) {
            bookComboBox.setSelectedIndex(0);
        }
        if (!user.hasRole("Member") && memberComboBox.getItemCount() > 0) {
            memberComboBox.setSelectedIndex(0);
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Search Book:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        dialog.add(bookSearchField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Select Book:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        dialog.add(bookComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Select Member:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        if (user.hasRole("Member")) {
            JTextField memberField = new JTextField(user.getUsername(), 20);
            memberField.setFont(new Font("Arial", Font.PLAIN, 16));
            memberField.setEditable(false);
            dialog.add(memberField, gbc);
        } else {
            dialog.add(memberComboBox, gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Member ID:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        dialog.add(memberIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("Member Name:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        dialog.add(memberNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        RoundedButton saveButton = new RoundedButton("Save", orange, white, "save.png");
        saveButton.addActionListener(e -> {
            Book selectedBook = (Book) bookComboBox.getSelectedItem();
            int memberId = user.hasRole("Member") ? user.getMemberId() : ((Member) memberComboBox.getSelectedItem()).getMemberId();
            if (selectedBook == null || (memberId == 0 && user.hasRole("Member"))) {
                JOptionPane.showMessageDialog(dialog, "Please select a book and member", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                reservationDAO.addReservation(user, selectedBook.getBookId(), memberId);
                if (user.hasRole("Member")) {
                    JOptionPane.showMessageDialog(dialog, "Reserved successfully, return the book after 1 week otherwise you will be fined.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Reservation added", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                refreshTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding reservation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(saveButton, gbc);

        dialog.setVisible(true);
    }

    private void cancelReservation() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a reservation to cancel", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 6);
        if (!status.equals("Active")) {
            JOptionPane.showMessageDialog(this, "Reservation already cancelled", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Cancel reservation ID " + reservationId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                reservationDAO.cancelReservation(user, reservationId);
                JOptionPane.showMessageDialog(this, "Reservation cancelled", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error cancelling reservation: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}