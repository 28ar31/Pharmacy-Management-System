package week9;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class PharmacyManagementGUI extends JFrame {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "pharmacydb";
    private static final String USER = "root";
    private static final String PASS = "ayushan2831"; // <-- Change to your MySQL password
    private static final String TIMEZONE_FIX = "?serverTimezone=UTC";

    // Swing components
    private final JTable medicineTable;
    private final DefaultTableModel tableModel;
    private final JTextField nameField = new JTextField(15);
    private final JTextField stockField = new JTextField(5);
    private final JTextField priceField = new JTextField(7);
    private final JTextField chargeField = new JTextField(7);
    private final JButton addButton = new JButton("Add");
    private final JButton updateButton = new JButton("Update");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton clearButton = new JButton("Clear");

    public PharmacyManagementGUI() {
        // Frame setup
        setTitle("Pharmacy Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Table setup
        String[] columnNames = {"ID", "Name", "Stock", "Price", "Home Delivery Charge"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        medicineTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(medicineTable);
        add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Manage Medicine"));

        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Stock:"));
        inputPanel.add(stockField);
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(priceField);
        inputPanel.add(new JLabel("Delivery Charge:"));
        inputPanel.add(chargeField);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // Combine panels
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        addListeners();

        // Initialize DB
        setupDatabase();
        loadMedicines();
    }

    private void addListeners() {
        // Populate input fields on row select
        medicineTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && medicineTable.getSelectedRow() != -1) {
                int selectedRow = medicineTable.getSelectedRow();
                nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                stockField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                priceField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                chargeField.setText(tableModel.getValueAt(selectedRow, 4).toString());
            }
        });

        addButton.addActionListener(e -> addMedicine());
        updateButton.addActionListener(e -> updateMedicine());
        deleteButton.addActionListener(e -> deleteMedicine());
        clearButton.addActionListener(e -> clearFields());
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL + DB_NAME + TIMEZONE_FIX, USER, PASS);
    }

    private void setupDatabase() {
        // Create database if it doesn’t exist
        try (Connection conn = DriverManager.getConnection(DB_URL + TIMEZONE_FIX, USER, PASS);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        } catch (SQLException e) {
            showError("Could not create database. Please ensure MySQL is running.", e);
            System.exit(1);
        }

        // Create table if not exists
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String createTableSQL = "CREATE TABLE IF NOT EXISTS Medicines (" +
                    "Med_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "Med_Name VARCHAR(100) NOT NULL UNIQUE, " +
                    "Stock INT NOT NULL DEFAULT 0, " +
                    "Price DECIMAL(10,2) NOT NULL, " +
                    "Home_LoCharge DECIMAL(10,2) NOT NULL DEFAULT 0)";
            stmt.executeUpdate(createTableSQL);

            // Insert sample data if table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Medicines")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertInitialData(stmt);
                }
            }

        } catch (SQLException e) {
            showError("Database table setup failed.", e);
        }
    }

    private void insertInitialData(Statement stmt) throws SQLException {
        String[] inserts = {
                "INSERT INTO Medicines (Med_Name, Stock, Price, Home_LoCharge) VALUES ('Vitamin C', 200, 30.00, 5.00)",
                "INSERT INTO Medicines (Med_Name, Stock, Price, Home_LoCharge) VALUES ('Paracetamol', 150, 15.00, 3.00)",
                "INSERT INTO Medicines (Med_Name, Stock, Price, Home_LoCharge) VALUES ('Ibuprofen', 100, 25.75, 4.50)",
                "INSERT INTO Medicines (Med_Name, Stock, Price, Home_LoCharge) VALUES ('Amoxicillin', 80, 45.00, 6.50)",
                "INSERT INTO Medicines (Med_Name, Stock, Price, Home_LoCharge) VALUES ('Cetirizine', 120, 20.00, 2.50)"
        };
        for (String sql : inserts) {
            stmt.executeUpdate(sql);
        }
    }

    private void loadMedicines() {
        tableModel.setRowCount(0);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Medicines ORDER BY Med_ID")) {

            while (rs.next()) {
                int id = rs.getInt("Med_ID");
                String name = rs.getString("Med_Name");
                int stock = rs.getInt("Stock");
                BigDecimal price = rs.getBigDecimal("Price");
                BigDecimal charge = rs.getBigDecimal("Home_LoCharge");
                tableModel.addRow(new Object[]{id, name, stock, price, charge});
            }

        } catch (SQLException e) {
            showError("Could not load medicines from the database.", e);
        }
    }

    private void addMedicine() {
        String sql = "INSERT INTO Medicines (Med_Name, Stock, Price, Home_LoCharge) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nameField.getText());
            pstmt.setInt(2, Integer.parseInt(stockField.getText()));
            pstmt.setBigDecimal(3, new BigDecimal(priceField.getText()));
            pstmt.setBigDecimal(4, new BigDecimal(chargeField.getText()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                loadMedicines();
                clearFields();
                JOptionPane.showMessageDialog(this, "Medicine added successfully!");
            }

        } catch (NumberFormatException nfe) {
            showError("Invalid number format. Please check Stock, Price, and Charge fields.", nfe);
        } catch (SQLException e) {
            showError("Could not add the medicine.", e);
        }
    }

    private void updateMedicine() {
        int selectedRow = medicineTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int medId = (int) tableModel.getValueAt(selectedRow, 0);
        String sql = "UPDATE Medicines SET Med_Name=?, Stock=?, Price=?, Home_LoCharge=? WHERE Med_ID=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nameField.getText());
            pstmt.setInt(2, Integer.parseInt(stockField.getText()));
            pstmt.setBigDecimal(3, new BigDecimal(priceField.getText()));
            pstmt.setBigDecimal(4, new BigDecimal(chargeField.getText()));
            pstmt.setInt(5, medId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                loadMedicines();
                clearFields();
                JOptionPane.showMessageDialog(this, "Medicine updated successfully!");
            }

        } catch (NumberFormatException nfe) {
            showError("Invalid number format. Please check Stock, Price, and Charge fields.", nfe);
        } catch (SQLException e) {
            showError("Could not update the medicine.", e);
        }
    }

    private void deleteMedicine() {
        int selectedRow = medicineTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int medId = (int) tableModel.getValueAt(selectedRow, 0);
        String medName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + medName + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Medicines WHERE Med_ID=?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, medId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    loadMedicines();
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Medicine deleted successfully!");
                }

            } catch (SQLException e) {
                showError("Could not delete the medicine.", e);
            }
        }
    }

    private void clearFields() {
        medicineTable.clearSelection();
        nameField.setText("");
        stockField.setText("");
        priceField.setText("");
        chargeField.setText("");
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this,
                message + "\nError: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PharmacyManagementGUI().setVisible(true));
    }
}
