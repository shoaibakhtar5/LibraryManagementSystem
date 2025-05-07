package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class BookManagementPanel extends JPanel {
    private static final Color blue = new Color(0, 107, 204);
    private static final Color orange = new Color(255, 102, 0);
    private static final Color white = Color.WHITE;

    private User user;
    private BookDAO bookDAO;
    private CategoriesDAO categoriesDAO;
    private PublishersDAO publishersDAO;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public BookManagementPanel(User user) {
        this.user = user;
        this.bookDAO = new BookDAO();
        if (this.bookDAO == null) {
            JOptionPane.showMessageDialog(this, "Failed to instantiate BookDAO", "Error", JOptionPane.ERROR_MESSAGE);
        }
        this.categoriesDAO = new CategoriesDAO();
        this.publishersDAO = new PublishersDAO();
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
                GradientPaint gp = new GradientPaint(0, 0, blue, 0, getHeight(), new Color(0, 62, 138));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        JLabel headerLabel = new JLabel("Book Management", SwingConstants.CENTER);
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
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchBooks(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchBooks(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchBooks(); }
        });
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH, 1);

        // Table
        String[] columns = {"ID", "Title", "ISBN", "Category", "Publisher", "Year", "Total Copies", "Available"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setFont(new Font("Arial", Font.PLAIN, 16));
        bookTable.setRowHeight(30);
        bookTable.setShowGrid(false);
        bookTable.setIntercellSpacing(new Dimension(0, 0));
        bookTable.setBackground(white);
        bookTable.setSelectionBackground(new Color(230, 240, 255));
        bookTable.setSelectionForeground(Color.BLACK);

        // Table Header Styling
        bookTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        bookTable.getTableHeader().setBackground(new Color(240, 240, 240));
        bookTable.getTableHeader().setForeground(Color.BLACK);
        bookTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        // Alternating Row Colors
        bookTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
        TableColumnModel columnModel = bookTable.getColumnModel();
        columnModel.getColumn(0).setCellRenderer(centerRenderer); // ID
        columnModel.getColumn(5).setCellRenderer(centerRenderer); // Year
        columnModel.getColumn(6).setCellRenderer(centerRenderer); // Total Copies
        columnModel.getColumn(7).setCellRenderer(centerRenderer); // Available

        // Simplified Hover Effect
        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = bookTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    bookTable.setRowSelectionInterval(row, row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(white);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (user.hasRole("Admin")) {
            RoundedButton addButton = new RoundedButton("Add Book", orange, white, "add.png");
            addButton.addActionListener(e -> showAddBookDialog());
            buttonPanel.add(addButton);

            RoundedButton updateButton = new RoundedButton("Update Book", orange, white, "update.png");
            updateButton.addActionListener(e -> showUpdateBookDialog());
            buttonPanel.add(updateButton);

            RoundedButton deleteButton = new RoundedButton("Delete Book", orange, white, "delete.png");
            deleteButton.addActionListener(e -> deleteBook());
            buttonPanel.add(deleteButton);
        }
        RoundedButton refreshButton = new RoundedButton("Refresh", blue, white, "refresh.png");
        refreshButton.addActionListener(e -> refreshTable());
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshTable();
    }

    private void searchBooks() {
        String searchText = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        if (bookDAO != null) {
            try {
                List<Book> books = bookDAO.getAllBooks(user);
                for (Book book : books) {
                    if (book.getTitle().toLowerCase().contains(searchText) ||
                            book.getIsbn().toLowerCase().contains(searchText)) {
                        tableModel.addRow(new Object[]{
                                book.getBookId(),
                                book.getTitle(),
                                book.getIsbn(),
                                book.getCategoryName(),
                                book.getPublisherName(),
                                book.getPublicationYear(),
                                book.getTotalCopies(),
                                book.getAvailableCopies()
                        });
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "BookDAO is not initialized", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        searchField.setText("");
        searchBooks();
    }

    private boolean validateInputs(String title, String isbn, String year, String copies) {
        if (title.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!Pattern.matches("^[0-9]{10}$|^[0-9]{13}$", isbn)) {
            JOptionPane.showMessageDialog(this, "ISBN must be 10 or 13 digits", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int yearInt = Integer.parseInt(year);
            if (yearInt < 1800 || yearInt > 2025) {
                JOptionPane.showMessageDialog(this, "Publication year must be between 1800 and 2025", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Publication year must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int copiesInt = Integer.parseInt(copies);
            if (copiesInt <= 0) {
                JOptionPane.showMessageDialog(this, "Total copies must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Total copies must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Book", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JTextField isbnField = new JTextField(20);
        JComboBox<Category> categoryComboBox = new JComboBox<>();
        JComboBox<Publisher> publisherComboBox = new JComboBox<>();
        JTextField yearField = new JTextField(20);
        JTextField copiesField = new JTextField(20);

        titleField.setFont(new Font("Arial", Font.PLAIN, 16));
        isbnField.setFont(new Font("Arial", Font.PLAIN, 16));
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        publisherComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        yearField.setFont(new Font("Arial", Font.PLAIN, 16));
        copiesField.setFont(new Font("Arial", Font.PLAIN, 16));

        try {
            List<Category> categories = categoriesDAO.getAllCategories(user);
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
            List<Publisher> publishers = publishersDAO.getAllPublishers(user);
            for (Publisher publisher : publishers) {
                publisherComboBox.addItem(publisher);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading categories/publishers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(titleLabel, gbc);
        gbc.gridx = 1;
        dialog.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel isbnLabel = new JLabel("ISBN:");
        isbnLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(isbnLabel, gbc);
        gbc.gridx = 1;
        dialog.add(isbnField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(categoryLabel, gbc);
        gbc.gridx = 1;
        dialog.add(categoryComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel publisherLabel = new JLabel("Publisher:");
        publisherLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(publisherLabel, gbc);
        gbc.gridx = 1;
        dialog.add(publisherComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel yearLabel = new JLabel("Publication Year:");
        yearLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(yearLabel, gbc);
        gbc.gridx = 1;
        dialog.add(yearField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel copiesLabel = new JLabel("Total Copies:");
        copiesLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(copiesLabel, gbc);
        gbc.gridx = 1;
        dialog.add(copiesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        RoundedButton saveButton = new RoundedButton("Save", orange, white, "save.png");
        saveButton.addActionListener(e -> {
            String title = titleField.getText();
            String isbn = isbnField.getText();
            String year = yearField.getText();
            String copies = copiesField.getText();
            if (!validateInputs(title, isbn, year, copies)) {
                return;
            }
            if (bookDAO != null) {
                try {
                    Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
                    Publisher selectedPublisher = (Publisher) publisherComboBox.getSelectedItem();
                    if (selectedCategory == null || selectedPublisher == null) {
                        JOptionPane.showMessageDialog(dialog, "Please select a category and publisher", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    bookDAO.addBook(user, title, isbn, selectedCategory.getCategoryId(),
                            selectedPublisher.getPublisherId(), Integer.parseInt(year), Integer.parseInt(copies));
                    JOptionPane.showMessageDialog(dialog, "Book added successfully");
                    dialog.dispose();
                    refreshTable();
                } catch (SQLException | SecurityException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "BookDAO is not initialized", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(saveButton, gbc);

        dialog.setVisible(true);
    }

    private void showUpdateBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a book to update", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object bookIdObj = tableModel.getValueAt(selectedRow, 0);
        int bookId;
        try {
            bookId = Integer.parseInt(bookIdObj.toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid book ID format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String selectedTitle = tableModel.getValueAt(selectedRow, 1).toString();
        String selectedIsbn = tableModel.getValueAt(selectedRow, 2).toString();
        Object yearObj = tableModel.getValueAt(selectedRow, 5);
        int selectedYear;
        try {
            selectedYear = Integer.parseInt(yearObj.toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid year format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Object copiesObj = tableModel.getValueAt(selectedRow, 6);
        int selectedCopies;
        try {
            selectedCopies = Integer.parseInt(copiesObj.toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid copies format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Book", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(selectedTitle, 20);
        JTextField isbnField = new JTextField(selectedIsbn, 20);
        JComboBox<Category> categoryComboBox = new JComboBox<>();
        JComboBox<Publisher> publisherComboBox = new JComboBox<>();
        JTextField yearField = new JTextField(String.valueOf(selectedYear), 20);
        JTextField copiesField = new JTextField(String.valueOf(selectedCopies), 20);

        titleField.setFont(new Font("Arial", Font.PLAIN, 16));
        isbnField.setFont(new Font("Arial", Font.PLAIN, 16));
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        publisherComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        yearField.setFont(new Font("Arial", Font.PLAIN, 16));
        copiesField.setFont(new Font("Arial", Font.PLAIN, 16));

        try {
            List<Category> categories = categoriesDAO.getAllCategories(user);
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
            List<Publisher> publishers = publishersDAO.getAllPublishers(user);
            for (Publisher publisher : publishers) {
                publisherComboBox.addItem(publisher);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading categories/publishers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(titleLabel, gbc);
        gbc.gridx = 1;
        dialog.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel isbnLabel = new JLabel("ISBN:");
        isbnLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(isbnLabel, gbc);
        gbc.gridx = 1;
        dialog.add(isbnField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(categoryLabel, gbc);
        gbc.gridx = 1;
        dialog.add(categoryComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel publisherLabel = new JLabel("Publisher:");
        publisherLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(publisherLabel, gbc);
        gbc.gridx = 1;
        dialog.add(publisherComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel yearLabel = new JLabel("Publication Year:");
        yearLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(yearLabel, gbc);
        gbc.gridx = 1;
        dialog.add(yearField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel copiesLabel = new JLabel("Total Copies:");
        copiesLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dialog.add(copiesLabel, gbc);
        gbc.gridx = 1;
        dialog.add(copiesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        RoundedButton saveButton = new RoundedButton("Save", orange, white, "save.png");
        saveButton.addActionListener(e -> {
            String title = titleField.getText();
            String isbn = isbnField.getText();
            String year = yearField.getText();
            String copies = copiesField.getText();
            if (!validateInputs(title, isbn, year, copies)) {
                return;
            }
            if (bookDAO != null) {
                try {
                    Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
                    Publisher selectedPublisher = (Publisher) publisherComboBox.getSelectedItem();
                    if (selectedCategory == null || selectedPublisher == null) {
                        JOptionPane.showMessageDialog(dialog, "Please select a category and publisher", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    bookDAO.updateBook(user, bookId, title, isbn, selectedCategory.getCategoryId(),
                            selectedPublisher.getPublisherId(), Integer.parseInt(year), Integer.parseInt(copies));
                    JOptionPane.showMessageDialog(dialog, "Book updated successfully");
                    dialog.dispose();
                    refreshTable();
                } catch (SQLException | SecurityException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "BookDAO is not initialized", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(saveButton, gbc);

        dialog.setVisible(true);
    }

    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a book to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object bookIdObj = tableModel.getValueAt(selectedRow, 0);
        int bookId;
        try {
            bookId = Integer.parseInt(bookIdObj.toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid book ID format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (bookDAO != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete book ID " + bookId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    bookDAO.deleteBook(user, bookId);
                    JOptionPane.showMessageDialog(this, "Book deleted successfully");
                    refreshTable();
                } catch (SQLException | SecurityException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "BookDAO is not initialized", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}