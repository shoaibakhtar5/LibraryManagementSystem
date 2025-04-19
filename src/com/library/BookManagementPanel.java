package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import static java.awt.Color.orange;
import static java.awt.Color.white;

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
        JLabel searchLabel = new JLabel(new ImageIcon(new ImageIcon(getClass().getResource("/resources/search.png")).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        searchPanel.add(searchLabel);
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
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
        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
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
        dialog.getContentPane().setBackground(Color.WHITE);
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
            try {
                Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
                Publisher selectedPublisher = (Publisher) publisherComboBox.getSelectedItem();
                if (selectedCategory == null || selectedPublisher == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select a category and publisher", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                bookDAO.addBook(user,
                        title,
                        isbn,
                        selectedCategory.getCategoryId(),
                        selectedPublisher.getPublisherId(),
                        Integer.parseInt(year),
                        Integer.parseInt(copies)
                );
                JOptionPane.showMessageDialog(dialog, "Book added successfully");
                dialog.dispose();
                refreshTable();
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        String selectedTitle = (String) tableModel.getValueAt(selectedRow, 1);
        String selectedIsbn = (String) tableModel.getValueAt(selectedRow, 2);
        int selectedYear = (int) tableModel.getValueAt(selectedRow, 5);
        int selectedCopies = (int) tableModel.getValueAt(selectedRow, 6);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Book", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
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
            try {
                Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
                Publisher selectedPublisher = (Publisher) publisherComboBox.getSelectedItem();
                if (selectedCategory == null || selectedPublisher == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select a category and publisher", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                bookDAO.updateBook(user,
                        bookId,
                        title,
                        isbn,
                        selectedCategory.getCategoryId(),
                        selectedPublisher.getPublisherId(),
                        Integer.parseInt(year),
                        Integer.parseInt(copies)
                );
                JOptionPane.showMessageDialog(dialog, "Book updated successfully");
                dialog.dispose();
                refreshTable();
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete book ID " + bookId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookDAO.deleteBook(user, bookId);
                JOptionPane.showMessageDialog(this, "Book deleted successfully");
                refreshTable();
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}