package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class BookManagementPanel extends JPanel {
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
        this.categoriesDAO = new CategoriesDAO();
        this.publishersDAO = new PublishersDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 245)); // Soft gray background
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Colors
        Color primaryColor = new Color(26, 51, 102); // Dark blue
        Color accentColor = new Color(0, 120, 215); // Bright blue
        Color white = Color.WHITE;

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(white);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        JLabel headerLabel = new JLabel("Book Management");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(primaryColor);
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JLabel breadcrumbLabel = new JLabel("Dashboard > Books");
        breadcrumbLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        breadcrumbLabel.setForeground(new Color(100, 100, 100));
        headerPanel.add(breadcrumbLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Toolbar (Search + Buttons)
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(white);
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(white);
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        searchField.setPreferredSize(new Dimension(300, 35));
        searchField.setToolTipText("Search by title or ISBN");
        searchField.setText(" Search books...");
        searchField.setForeground(new Color(150, 150, 150));
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals(" Search books...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(" Search books...");
                    searchField.setForeground(new Color(150, 150, 150));
                }
            }
        });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchBooks(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchBooks(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchBooks(); }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);
        JLabel searchIcon = new JLabel(new ImageIcon(new ImageIcon(getClass().getResource("/resources/search.png"))
                .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        searchPanel.add(searchIcon, BorderLayout.WEST);
        toolbarPanel.add(searchPanel, BorderLayout.WEST);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(white);
        if (user.hasRole("Admin")) {
            RoundedButton addButton = new RoundedButton("Add", accentColor, white, "add.png");
            addButton.setToolTipText("Add a new book");
            addButton.setPreferredSize(new Dimension(100, 35));
            addButton.addActionListener(e -> showAddBookDialog());
            buttonPanel.add(addButton);

            RoundedButton updateButton = new RoundedButton("Update", accentColor, white, "update.png");
            updateButton.setToolTipText("Update selected book");
            updateButton.setPreferredSize(new Dimension(100, 35));
            updateButton.addActionListener(e -> showUpdateBookDialog());
            buttonPanel.add(updateButton);

            RoundedButton deleteButton = new RoundedButton("Delete", new Color(220, 53, 69), white, "delete.png");
            deleteButton.setToolTipText("Delete selected book");
            deleteButton.setPreferredSize(new Dimension(100, 35));
            deleteButton.addActionListener(e -> deleteBook());
            buttonPanel.add(deleteButton);
        }
        RoundedButton refreshButton = new RoundedButton("Refresh", primaryColor, white, "refresh.png");
        refreshButton.setToolTipText("Refresh book list");
        refreshButton.setPreferredSize(new Dimension(100, 35));
        refreshButton.addActionListener(e -> refreshTable());
        buttonPanel.add(refreshButton);
        toolbarPanel.add(buttonPanel, BorderLayout.EAST);
        add(toolbarPanel, BorderLayout.NORTH, 1);

        // Table
        String[] columns = {"ID", "Title", "ISBN", "Category", "Publisher", "Year", "Total Copies", "Available"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        bookTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row % 2 == 0) {
                    c.setBackground(new Color(240, 240, 240));
                } else {
                    c.setBackground(white);
                }
                return c;
            }
        };
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bookTable.setRowHeight(30);
        bookTable.setShowGrid(true); // Enable grid lines
        bookTable.setGridColor(new Color(200, 200, 200)); // Light gray grid lines
        bookTable.setIntercellSpacing(new Dimension(1, 1)); // Ensure grid lines are visible
        bookTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookTable.getTableHeader().setBackground(new Color(230, 230, 230));
        bookTable.getTableHeader().setForeground(primaryColor);
        bookTable.getTableHeader().setReorderingAllowed(false);

        // Adjust column widths
        TableColumnModel columnModel = bookTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50); // ID
        columnModel.getColumn(1).setPreferredWidth(200); // Title
        columnModel.getColumn(2).setPreferredWidth(120); // ISBN
        columnModel.getColumn(3).setPreferredWidth(100); // Category
        columnModel.getColumn(4).setPreferredWidth(150); // Publisher
        columnModel.getColumn(5).setPreferredWidth(60); // Year
        columnModel.getColumn(6).setPreferredWidth(80); // Total Copies
        columnModel.getColumn(7).setPreferredWidth(80); // Available

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getViewport().setBackground(white);
        add(scrollPane, BorderLayout.CENTER);

        // Load initial data
        refreshTable();
    }

    private void searchBooks() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.equals("search books...")) searchText = "";
        tableModel.setRowCount(0);
        try {
            List<Book> books = bookDAO.getAllBooks(user);
            // Sort books by bookId in ascending order
            books.sort(Comparator.comparingInt(Book::getBookId));
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
        }
    }

    private void refreshTable() {
        searchField.setText(" Search books...");
        searchField.setForeground(new Color(150, 150, 150));
        searchBooks();
    }

    private boolean validateInputs(String title, String isbn, String publisherName, String year, String copies) {
        if (title.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (publisherName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Publisher name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
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
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(245, 245, 245));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField titleField = new JTextField(20);
        JTextField isbnField = new JTextField(20);
        JComboBox<Category> categoryComboBox = new JComboBox<>();
        JTextField publisherField = new JTextField(20);
        JTextField yearField = new JTextField(20);
        JTextField copiesField = new JTextField(20);

        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        isbnField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        categoryComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        publisherField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        yearField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        copiesField.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Load all categories
        try {
            List<Category> categories = categoriesDAO.getAllCategories(user);
            if (categories.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No categories available. Please add categories first.", "Error", JOptionPane.ERROR_MESSAGE);
                dialog.dispose();
                return;
            }
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
            // Set the first category as the default selection
            categoryComboBox.setSelectedIndex(0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading categories: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            dialog.dispose();
            return;
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(titleLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel isbnLabel = new JLabel("ISBN:");
        isbnLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(isbnLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(isbnField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(categoryLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(categoryComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel publisherLabel = new JLabel("Publisher:");
        publisherLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(publisherLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(publisherField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel yearLabel = new JLabel("Publication Year:");
        yearLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(yearLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(yearField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel copiesLabel = new JLabel("Total Copies:");
        copiesLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(copiesLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(copiesField, gbc);

        dialog.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));
        RoundedButton saveButton = new RoundedButton("Save", new Color(0, 120, 215), Color.WHITE, "save.png");
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setToolTipText("Save book");
        saveButton.addActionListener(e -> {
            System.out.println("Add Book Save button clicked");
            String title = titleField.getText();
            String isbn = isbnField.getText();
            String year = yearField.getText();
            String copies = copiesField.getText();
            String publisherName = publisherField.getText().trim();
            System.out.println("Inputs: title=" + title + ", isbn=" + isbn + ", publisherName=" + publisherName + ", year=" + year + ", copies=" + copies);

            if (!validateInputs(title, isbn, publisherName, year, copies)) {
                System.out.println("Validation failed");
                return;
            }

            try {
                Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
                System.out.println("Selected category: " + (selectedCategory != null ? selectedCategory.getCategoryName() : "null"));
                if (selectedCategory == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select a category", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                System.out.println("Calling bookDAO.addBook...");
                bookDAO.addBook(user,
                        title,
                        isbn,
                        selectedCategory.getCategoryId(),
                        publisherName,
                        Integer.parseInt(year),
                        Integer.parseInt(copies)
                );
                System.out.println("Book added successfully");
                JOptionPane.showMessageDialog(dialog, "Book added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();
            } catch (SQLException | SecurityException ex) {
                System.out.println("Exception occurred: " + ex.getMessage());
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);

        RoundedButton cancelButton = new RoundedButton("Cancel", new Color(108, 117, 125), Color.WHITE, "cancel.png");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setToolTipText("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showUpdateBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a book to update", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        String selectedTitle = (String) tableModel.getValueAt(selectedRow, 1);
        String selectedIsbn = (String) tableModel.getValueAt(selectedRow, 2);
        int selectedYear = (int) tableModel.getValueAt(selectedRow, 5);
        int selectedCopies = (int) tableModel.getValueAt(selectedRow, 6);
        String publisherNameFromTable = (String) tableModel.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Book", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(245, 245, 245));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField titleField = new JTextField(selectedTitle, 20);
        JTextField isbnField = new JTextField(selectedIsbn, 20);
        JComboBox<Category> categoryComboBox = new JComboBox<>();
        JComboBox<Publisher> publisherComboBox = new JComboBox<>();
        JTextField yearField = new JTextField(String.valueOf(selectedYear), 20);
        JTextField copiesField = new JTextField(String.valueOf(selectedCopies), 20);

        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        isbnField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        categoryComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        publisherComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        yearField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        copiesField.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        try {
            List<Category> categories = categoriesDAO.getAllCategories(user);
            if (categories.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No categories available. Please add categories first.", "Error", JOptionPane.ERROR_MESSAGE);
                dialog.dispose();
                return;
            }
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
            categoryComboBox.setSelectedIndex(0);

            List<Publisher> publishers = publishersDAO.getAllPublishers(user);
            for (Publisher publisher : publishers) {
                publisherComboBox.addItem(publisher);
            }
            publisherComboBox.setSelectedItem(publishers.stream().filter(p -> p.getPublisherName().equals(publisherNameFromTable)).findFirst().orElse(null));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading categories/publishers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            dialog.dispose();
            return;
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(titleLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel isbnLabel = new JLabel("ISBN:");
        isbnLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(isbnLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(isbnField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(categoryLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(categoryComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel publisherLabel = new JLabel("Publisher:");
        publisherLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(publisherLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(publisherComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel yearLabel = new JLabel("Publication Year:");
        yearLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(yearLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(yearField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel copiesLabel = new JLabel("Total Copies:");
        copiesLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(copiesLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(copiesField, gbc);

        dialog.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));
        RoundedButton saveButton = new RoundedButton("Save", new Color(0, 120, 215), Color.WHITE, "save.png");
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setToolTipText("Save changes");
        saveButton.addActionListener(e -> {
            System.out.println("Update Book Save button clicked");
            String title = titleField.getText();
            String isbn = isbnField.getText();
            String year = yearField.getText();
            String copies = copiesField.getText();
            System.out.println("Inputs: title=" + title + ", isbn=" + isbn + ", publisherNameFromTable=" + publisherNameFromTable + ", year=" + year + ", copies=" + copies);

            if (!validateInputs(title, isbn, publisherNameFromTable, year, copies)) {
                System.out.println("Validation failed");
                return;
            }

            try {
                Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
                Publisher selectedPublisher = (Publisher) publisherComboBox.getSelectedItem();
                System.out.println("Selected category: " + (selectedCategory != null ? selectedCategory.getCategoryName() : "null"));
                System.out.println("Selected publisher: " + (selectedPublisher != null ? selectedPublisher.getPublisherName() : "null"));
                if (selectedCategory == null || selectedPublisher == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select a category and publisher", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                System.out.println("Calling bookDAO.updateBook...");
                bookDAO.updateBook(user,
                        bookId,
                        title,
                        isbn,
                        selectedCategory.getCategoryId(),
                        selectedPublisher.getPublisherId(),
                        Integer.parseInt(year),
                        Integer.parseInt(copies)
                );
                System.out.println("Book updated successfully");
                JOptionPane.showMessageDialog(dialog, "Book updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();
            } catch (SQLException | SecurityException ex) {
                System.out.println("Exception occurred: " + ex.getMessage());
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);

        RoundedButton cancelButton = new RoundedButton("Cancel", new Color(108, 117, 125), Color.WHITE, "cancel.png");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setToolTipText("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a book to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete book ID " + bookId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookDAO.deleteBook(user, bookId);
                JOptionPane.showMessageDialog(this, "Book deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}