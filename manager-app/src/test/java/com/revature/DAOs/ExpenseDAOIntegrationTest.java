package com.revature.DAOs;

import com.revature.exceptions.ResourceNotFoundException;
import com.revature.models.Expense;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

// Integration tests for ExpenseDAO: no mocking - each test runs the DAO's real SQL
// against a real, disposable SQLite database file created fresh per test via @TempDir.
// Relies on ConnectionUtil honoring the "expense.db.path" system property.
@DisplayName("ExpenseDAO Integration Tests")
class ExpenseDAOIntegrationTest {

    @TempDir
    Path tempDir;

    private ExpenseDAO expenseDAO;
    private String jdbcUrl;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        Path dbFile = tempDir.resolve("test-expense-manager.db");
        System.setProperty("expense.db.path", dbFile.toAbsolutePath().toString());
        jdbcUrl = "jdbc:sqlite:" + dbFile.toAbsolutePath();

        Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE expenses (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    user_id INTEGER NOT NULL," +
                "    amount REAL NOT NULL," +
                "    description TEXT NOT NULL," +
                "    date TEXT NOT NULL," +
                "    category TEXT" +
                ");");
            stmt.execute(
                "CREATE TABLE approvals (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    expense_id INTEGER NOT NULL," +
                "    status TEXT NOT NULL," +
                "    reviewer INTEGER," +
                "    comment TEXT," +
                "    review_date TEXT" +
                ");");
        }

        expenseDAO = new ExpenseDAO();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("expense.db.path");
    }

    private int insertExpense(int userId, double amount, String description, String date, String category) throws SQLException {
        String sql = "INSERT INTO expenses (user_id, amount, description, date, category) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setDouble(2, amount);
            ps.setString(3, description);
            ps.setString(4, date);
            ps.setString(5, category);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void insertApproval(int expenseId, String status) throws SQLException {
        String sql = "INSERT INTO approvals (expense_id, status) VALUES (?, ?);";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.setString(2, status);
            ps.executeUpdate();
        }
    }

    // ---- getPendingExpenses ----

    @DisplayName("getPendingExpenses returns only expenses joined to a pending approval")
    @Test
    void getPendingExpenses_returnsOnlyPendingJoinedExpenses() throws SQLException {
        int pendingId = insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        int approvedId = insertExpense(11, 99.99, "hotel", "2026-07-21", "travel");
        insertApproval(pendingId, "pending");
        insertApproval(approvedId, "approved");

        ArrayList<Expense> result = expenseDAO.getPendingExpenses();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(pendingId, result.get(0).getId());
        assertEquals("taxi", result.get(0).getDescription());
    }

    @DisplayName("getPendingExpenses returns empty list when no approvals are pending")
    @Test
    void getPendingExpenses_whenNonePending_returnsEmptyList() throws SQLException {
        int expenseId = insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        insertApproval(expenseId, "approved");

        ArrayList<Expense> result = expenseDAO.getPendingExpenses();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---- getExpensesByEmployee ----

    @DisplayName("getExpensesByEmployee returns only expenses for that user")
    @Test
    void getExpensesByEmployee_returnsOnlyMatchingUser() throws SQLException {
        insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        insertExpense(11, 99.99, "hotel", "2026-07-21", "travel");
        insertExpense(10, 12.00, "coffee", "2026-07-22", "food");

        ArrayList<Expense> result = expenseDAO.getExpensesByEmployee(10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getUserId() == 10));
    }

    @DisplayName("getExpensesByEmployee returns empty list when userId has no expenses")
    @Test
    void getExpensesByEmployee_whenNoMatch_returnsEmptyList() throws SQLException {
        insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");

        ArrayList<Expense> result = expenseDAO.getExpensesByEmployee(999);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---- getExpensesByCategory ----

    @DisplayName("getExpensesByCategory returns only expenses in that category")
    @Test
    void getExpensesByCategory_returnsOnlyMatchingCategory() throws SQLException {
        insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        insertExpense(11, 12.00, "coffee", "2026-07-22", "food");

        ArrayList<Expense> result = expenseDAO.getExpensesByCategory("travel");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("travel", result.get(0).getCategory());
    }

    @DisplayName("getExpensesByCategory returns empty list when category has no expenses")
    @Test
    void getExpensesByCategory_whenNoMatch_returnsEmptyList() throws SQLException {
        insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");

        ArrayList<Expense> result = expenseDAO.getExpensesByCategory("nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---- getExpenseByDate ----

    @DisplayName("getExpenseByDate returns only expenses on that date")
    @Test
    void getExpenseByDate_returnsOnlyMatchingDate() throws SQLException {
        insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        insertExpense(11, 12.00, "coffee", "2026-07-22", "food");

        ArrayList<Expense> result = expenseDAO.getExpenseByDate("2026-07-20");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("taxi", result.get(0).getDescription());
    }

    // ---- getExpenseById ----

    @DisplayName("getExpenseById returns the matching expense")
    @Test
    void getExpenseById_returnsExpense() throws SQLException {
        int id = insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");

        Expense result = expenseDAO.getExpenseById(id);

        assertEquals(id, result.getId());
        assertEquals(10, result.getUserId());
        assertEquals("taxi", result.getDescription());
        assertEquals("travel", result.getCategory());
    }

    @DisplayName("getExpenseById throws ResourceNotFoundException when id does not exist")
    @Test
    void getExpenseById_whenMissing_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> expenseDAO.getExpenseById(999));
    }
}
