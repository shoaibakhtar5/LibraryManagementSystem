package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReservationsPanel extends JPanel {
    private User user;
    private ReservationDAO reservationDAO;
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private static final Color blue = new Color(0, 87, 183);
    private static final Color orange = new Color(255, 98, 0);
    private static final Color white = Color.WHITE;

    public ReservationsPanel(User user) {
        this.user = user;
        this.reservationDAO = new ReservationDAO();
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
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
        reservationTable.setShowGrid(true);
        reservationTable.setGridColor(Color.LIGHT_GRAY);
        reservationTable.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        reservationTable.setIntercellSpacing(new Dimension(1, 1));
        reservationTable.setBackground(white);
        reservationTable.setSelectionBackground(new Color(230, 240, 255));
        reservationTable.setSelectionForeground(Color.BLACK);

        // Table Header Styling
        reservationTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        reservationTable.getTableHeader().setBackground(new Color(240, 240, 240));
        reservationTable.getTableHeader().setForeground(Color.BLACK);
        reservationTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        // Alternating Row Colors
        reservationTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
        TableColumnModel columnModel = reservationTable.getColumnModel();
        columnModel.getColumn(0).setCellRenderer(centerRenderer); // ID
        columnModel.getColumn(1).setCellRenderer(centerRenderer); // Book ID
        columnModel.getColumn(3).setCellRenderer(centerRenderer); // Member ID

        // Sort by Reservation ID
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        reservationTable.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        sorter.sort();

        // Mouse Click for Selection
        reservationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = reservationTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    reservationTable.setRowSelectionInterval(row, row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
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
        if (user.hasRole("Admin") || user.hasRole("Staff")) {
            RoundedButton clearAllButton = new RoundedButton("Clear All", new Color(200, 50, 50), white, null);
            clearAllButton.addActionListener(e -> clearAllReservations());
            buttonPanel.add(clearAllButton);
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
            reservations.sort(Comparator.comparingInt(Reservation::getReservationId));
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
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(white);

        // Gradient Header for Dialog
        JPanel dialogHeaderPanel = new JPanel() {
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
        dialogHeaderPanel.setLayout(new BorderLayout());
        JLabel dialogHeaderLabel = new JLabel("Add New Reservation", SwingConstants.CENTER);
        dialogHeaderLabel.setFont(new Font("Arial", Font.BOLD, 24));
        dialogHeaderLabel.setForeground(white);
        dialogHeaderLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        dialogHeaderPanel.add(dialogHeaderLabel, BorderLayout.CENTER);
        dialog.add(dialogHeaderPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(white);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Book Selection Section
        JPanel bookPanel = new JPanel(new GridBagLayout());
        bookPanel.setBackground(white);
        bookPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(blue), "Book Details", 0, 0, new Font("Arial", Font.BOLD, 16), blue));
        GridBagConstraints bookGbc = new GridBagConstraints();
        bookGbc.insets = new Insets(5, 5, 5, 5);
        bookGbc.fill = GridBagConstraints.HORIZONTAL;
        bookGbc.anchor = GridBagConstraints.WEST;

        JComboBox<Book> bookComboBox = new JComboBox<>();
        bookComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        bookComboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        bookComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Book) {
                    Book book = (Book) value;
                    if (book.getAvailableCopies() == 0) {
                        setText(book.getTitle() + " (Unavailable)");
                        setForeground(Color.RED);
                    } else {
                        setText(book.getTitle());
                        setForeground(Color.BLACK);
                    }
                }
                return this;
            }
        });

        JTextField bookSearchField = new JTextField(20);
        bookSearchField.setFont(new Font("Arial", Font.PLAIN, 16));
        bookSearchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        bookSearchField.setToolTipText("Search books by title");

        List<Book> allBooks = new ArrayList<>();
        try {
            allBooks = bookDAO.getAllBooks(user);
            for (Book book : allBooks) {
                bookComboBox.addItem(book);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        JLabel bookSearchLabel = new JLabel("Search Book:");
        bookSearchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        bookSearchLabel.setForeground(blue);
        bookGbc.gridx = 0;
        bookGbc.gridy = 0;
        bookPanel.add(bookSearchLabel, bookGbc);

        bookGbc.gridx = 1;
        bookPanel.add(bookSearchField, bookGbc);

        JLabel selectBookLabel = new JLabel("Select Book:");
        selectBookLabel.setFont(new Font("Arial", Font.BOLD, 14));
        selectBookLabel.setForeground(blue);
        bookGbc.gridx = 0;
        bookGbc.gridy = 1;
        bookPanel.add(selectBookLabel, bookGbc);

        bookGbc.gridx = 1;
        bookPanel.add(bookComboBox, bookGbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(bookPanel, gbc);

        // Member Selection Section
        JPanel memberPanel = new JPanel(new GridBagLayout());
        memberPanel.setBackground(white);
        memberPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(blue), "Member Details", 0, 0, new Font("Arial", Font.BOLD, 16), blue));
        GridBagConstraints memberGbc = new GridBagConstraints();
        memberGbc.insets = new Insets(5, 5, 5, 5);
        memberGbc.fill = GridBagConstraints.HORIZONTAL;
        memberGbc.anchor = GridBagConstraints.WEST;

        JComboBox<Member> memberComboBox = new JComboBox<>();
        memberComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        memberComboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JTextField memberIdField = new JTextField(20);
        JTextField memberNameField = new JTextField(20);
        memberIdField.setFont(new Font("Arial", Font.PLAIN, 16));
        memberIdField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        memberIdField.setEditable(false);
        memberIdField.setToolTipText("Member ID");
        memberNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        memberNameField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        memberNameField.setEditable(false);
        memberNameField.setToolTipText("Member Name");

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
            try {
                List<Member> members = memberDAO.getAllMembers(user);
                for (Member member : members) {
                    memberComboBox.addItem(member);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog, "Error loading members: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            memberComboBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Member selectedMember = (Member) e.getItem();
                    memberIdField.setText(String.valueOf(selectedMember.getMemberId()));
                    memberNameField.setText(selectedMember.getName());
                }
            });
        }

        int row = 0;
        if (!user.hasRole("Member")) {
            JLabel selectMemberLabel = new JLabel("Select Member:");
            selectMemberLabel.setFont(new Font("Arial", Font.BOLD, 14));
            selectMemberLabel.setForeground(blue);
            memberGbc.gridx = 0;
            memberGbc.gridy = row++;
            memberPanel.add(selectMemberLabel, memberGbc);

            memberGbc.gridx = 1;
            memberPanel.add(memberComboBox, memberGbc);
        }

        JLabel memberIdLabel = new JLabel("Member ID:");
        memberIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        memberIdLabel.setForeground(blue);
        memberGbc.gridx = 0;
        memberGbc.gridy = row++;
        memberPanel.add(memberIdLabel, memberGbc);

        memberGbc.gridx = 1;
        memberPanel.add(memberIdField, memberGbc);

        JLabel memberNameLabel = new JLabel("Member Name:");
        memberNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        memberNameLabel.setForeground(blue);
        memberGbc.gridx = 0;
        memberGbc.gridy = row++;
        memberPanel.add(memberNameLabel, memberGbc);

        memberGbc.gridx = 1;
        memberPanel.add(memberNameField, memberGbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        contentPanel.add(memberPanel, gbc);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(white);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        RoundedButton cancelButton = new RoundedButton("Cancel", new Color(150, 150, 150), white, null);
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        RoundedButton saveButton = new RoundedButton("Save", orange, white, "save.png");
        saveButton.setPreferredSize(new Dimension(120, 40));
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.addActionListener(e -> {
            Book selectedBook = (Book) bookComboBox.getSelectedItem();
            int memberId = user.hasRole("Member") ? user.getMemberId() : ((Member) memberComboBox.getSelectedItem()).getMemberId();
            if (selectedBook == null || (memberId == 0 && user.hasRole("Member"))) {
                JOptionPane.showMessageDialog(dialog, "Please select a book and member", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selectedBook.getAvailableCopies() == 0) {
                JOptionPane.showMessageDialog(dialog, "Selected book is unavailable", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Disable the Save button to prevent multiple clicks
            saveButton.setEnabled(false);

            // Show a loading message
            JOptionPane loadingPane = new JOptionPane("Adding reservation, please wait...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
            JDialog loadingDialog = loadingPane.createDialog(dialog, "Processing");
            loadingDialog.setModal(false);
            loadingDialog.setVisible(true);

            // Perform the database operation in a background thread with timeout
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        reservationDAO.addReservation(user, selectedBook.getBookId(), memberId);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    // Close the loading dialog
                    loadingDialog.dispose();

                    try {
                        get(30, TimeUnit.SECONDS); // 30-second timeout
                        // Show success message
                        if (user.hasRole("Member")) {
                            JOptionPane.showMessageDialog(dialog, "Reserved successfully, return the book after 1 week otherwise you will be fined.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Reservation added", "Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                        dialog.dispose();
                        refreshTable();
                    } catch (TimeoutException ex) {
                        JOptionPane.showMessageDialog(dialog, "Operation timed out after 30 seconds. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                        System.out.println("Operation timed out: " + ex.getMessage());
                    } catch (ExecutionException ex) {
                        // Extract the root cause if it's a wrapped exception
                        Throwable cause = ex.getCause();
                        if (cause == null) cause = ex;
                        JOptionPane.showMessageDialog(dialog, "Error adding reservation: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        cause.printStackTrace();
                    } catch (InterruptedException ex) {
                        JOptionPane.showMessageDialog(dialog, "Operation interrupted. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        System.out.println("Operation interrupted: " + ex.getMessage());
                    } finally {
                        // Re-enable the Save button
                        saveButton.setEnabled(true);
                    }
                }
            };
            worker.execute();
        });
        buttonPanel.add(saveButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Select the first item in the combo boxes to populate fields initially
        if (bookComboBox.getItemCount() > 0) {
            bookComboBox.setSelectedIndex(0);
        }
        if (!user.hasRole("Member") && memberComboBox.getItemCount() > 0) {
            memberComboBox.setSelectedIndex(0);
        }

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
                e.printStackTrace();
            }
        }
    }

    private void clearAllReservations() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel all active reservations? This cannot be undone.", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                reservationDAO.clearAllReservations(user);
                JOptionPane.showMessageDialog(this, "All active reservations have been cancelled", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error clearing reservations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}