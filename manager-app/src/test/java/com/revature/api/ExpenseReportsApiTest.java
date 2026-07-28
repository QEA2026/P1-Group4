package com.revature.api;

import com.revature.Main;
import io.javalin.Javalin;
import io.restassured.RestAssured;
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
import java.sql.Statement;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

// API tests for the read-only expense/report endpoints: GET /expenses/pending,
// GET /reports/employee/{userId}, GET /reports/category/{category},
// GET /reports/date/{date}, GET /reports/expense/{expenseId}.
// Boots the real Javalin app from Main against a disposable SQLite database
// created fresh per test.
@DisplayName("Expense Reports API Tests")
class ExpenseReportsApiTest {

    @TempDir
    Path tempDir;

    private Javalin app;
    private String jdbcUrl;

    @BeforeEach
    void setUp() throws Exception {
        Path dbFile = tempDir.resolve("expense-reports-api-test.db");
        System.setProperty("expense.db.path", dbFile.toAbsolutePath().toString());
        jdbcUrl = "jdbc:sqlite:" + dbFile.toAbsolutePath();

        Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE users (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    username TEXT NOT NULL," +
                "    password TEXT NOT NULL," +
                "    role TEXT NOT NULL" +
                ");");
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

        app = Main.createApp().start(0);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = app.port();
    }

    @AfterEach
    void tearDown() {
        app.stop();
        System.clearProperty("expense.db.path");
    }

    private int insertUser(String username, String role) throws Exception {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, 'x', ?);";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, role);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int insertExpense(int userId, double amount, String description, String date, String category) throws Exception {
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

    private void insertApproval(int expenseId, String status) throws Exception {
        String sql = "INSERT INTO approvals (expense_id, status) VALUES (?, ?);";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.setString(2, status);
            ps.executeUpdate();
        }
    }

    // ---- GET /expenses/pending ----

    @DisplayName("GET /expenses/pending returns only expenses awaiting review")
    @Test
    void getPendingExpenses_returnsOnlyPending() throws Exception {
        int pendingId = insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        int approvedId = insertExpense(11, 99.99, "hotel", "2026-07-21", "travel");
        insertApproval(pendingId, "pending");
        insertApproval(approvedId, "approved");

        given()
        .when()
            .get("/expenses/pending")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].id", equalTo(pendingId))
            .body("[0].description", equalTo("taxi"));
    }

    @DisplayName("GET /expenses/pending returns an empty list when none are pending")
    @Test
    void getPendingExpenses_whenNonePending_returnsEmptyList() {
        given()
        .when()
            .get("/expenses/pending")
        .then()
            .statusCode(200)
            .body("", empty());
    }

    // ---- GET /reports/employee/{userId} ----

    @DisplayName("GET /reports/employee/{userId} returns only that employee's expenses")
    @Test
    void getExpensesByEmployee_existingUser_returnsMatchingExpenses() throws Exception {
        int userId = insertUser("jsmith", "employee");
        insertExpense(userId, 25.50, "taxi", "2026-07-20", "travel");
        insertExpense(999, 12.00, "coffee", "2026-07-22", "food");

        given()
        .when()
            .get("/reports/employee/{userId}", userId)
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].userId", equalTo(userId));
    }

    @DisplayName("GET /reports/employee/{userId} returns an empty list when the user has no expenses")
    @Test
    void getExpensesByEmployee_existingUserNoExpenses_returnsEmptyList() throws Exception {
        int userId = insertUser("nospend", "employee");

        given()
        .when()
            .get("/reports/employee/{userId}", userId)
        .then()
            .statusCode(200)
            .body("", empty());
    }

    @DisplayName("GET /reports/employee/{userId} returns 404 for a user that doesn't exist")
    @Test
    void getExpensesByEmployee_unknownUser_returns404() {
        given()
        .when()
            .get("/reports/employee/{userId}", 999)
        .then()
            .statusCode(404);
    }

    // ---- GET /reports/category/{category} ----

    @DisplayName("GET /reports/category/{category} returns only expenses in that category")
    @Test
    void getExpensesByCategory_returnsMatchingCategory() throws Exception {
        insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        insertExpense(11, 12.00, "coffee", "2026-07-22", "food");

        given()
        .when()
            .get("/reports/category/{category}", "travel")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].category", equalTo("travel"));
    }

    @DisplayName("GET /reports/category/{category} returns an empty list for a category with no expenses")
    @Test
    void getExpensesByCategory_noMatches_returnsEmptyList() throws Exception {
        insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");

        given()
        .when()
            .get("/reports/category/{category}", "nonexistent")
        .then()
            .statusCode(200)
            .body("", empty());
    }

    // ---- GET /reports/date/{date} ----

    @DisplayName("GET /reports/date/{date} returns only expenses on that date")
    @Test
    void getExpenseByDate_returnsMatchingDate() throws Exception {
        insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        insertExpense(11, 12.00, "coffee", "2026-07-22", "food");

        given()
        .when()
            .get("/reports/date/{date}", "2026-07-20")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].description", equalTo("taxi"));
    }

    // ---- GET /reports/expense/{expenseId} ----

    @DisplayName("GET /reports/expense/{expenseId} returns the matching expense")
    @Test
    void getExpenseById_existing_returnsExpense() throws Exception {
        int id = insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");

        given()
        .when()
            .get("/reports/expense/{expenseId}", id)
        .then()
            .statusCode(200)
            .body("id", equalTo(id))
            .body("description", equalTo("taxi"));
    }

    @DisplayName("GET /reports/expense/{expenseId} returns 404 when the expense doesn't exist")
    @Test
    void getExpenseById_missing_returns404() {
        given()
        .when()
            .get("/reports/expense/{expenseId}", 999)
        .then()
            .statusCode(404);
    }
}
