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
import static org.junit.jupiter.api.Assertions.assertEquals;

// API tests for PUT /expenses/{id}/review: boots the real Javalin app from
// Main against a disposable SQLite database created fresh per test.
@DisplayName("Approval API Tests")
class ApprovalApiTest {

    @TempDir
    Path tempDir;

    private Javalin app;
    private String jdbcUrl;

    @BeforeEach
    void setUp() throws Exception {
        Path dbFile = tempDir.resolve("approval-api-test.db");
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

        app = Main.createApp().start(0);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = app.port();
    }

    @AfterEach
    void tearDown() {
        app.stop();
        System.clearProperty("expense.db.path");
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

    private void insertPendingApproval(int expenseId) throws Exception {
        String sql = "INSERT INTO approvals (expense_id, status) VALUES (?, 'pending');";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.executeUpdate();
        }
    }

    private String getApprovalStatus(int expenseId) throws Exception {
        String sql = "SELECT status FROM approvals WHERE expense_id = ?;";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString("status");
            }
        }
    }

    @DisplayName("Approving a pending expense returns 200 and persists status 'approved'")
    @Test
    void reviewExpense_approve_returns200AndPersists() throws Exception {
        int expenseId = insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        insertPendingApproval(expenseId);

        given()
            .contentType("application/json")
            .body("{\"status\":\"approved\",\"reviewer\":3,\"comment\":\"Looks good\"}")
        .when()
            .put("/expenses/{id}/review", expenseId)
        .then()
            .statusCode(200);

        assertEquals("approved", getApprovalStatus(expenseId));
    }

    @DisplayName("Denying a pending expense returns 200 and persists status 'denied'")
    @Test
    void reviewExpense_deny_returns200AndPersists() throws Exception {
        int expenseId = insertExpense(10, 40.00, "dinner", "2026-07-20", "meals");
        insertPendingApproval(expenseId);

        given()
            .contentType("application/json")
            .body("{\"status\":\"denied\",\"reviewer\":3,\"comment\":\"Missing receipt\"}")
        .when()
            .put("/expenses/{id}/review", expenseId)
        .then()
            .statusCode(200);

        assertEquals("denied", getApprovalStatus(expenseId));
    }

    @DisplayName("Status is normalized to lowercase regardless of request casing")
    @Test
    void reviewExpense_mixedCaseStatus_isNormalizedToLowercase() throws Exception {
        int expenseId = insertExpense(10, 15.00, "parking", "2026-07-20", "travel");
        insertPendingApproval(expenseId);

        given()
            .contentType("application/json")
            .body("{\"status\":\"Approved\",\"reviewer\":3,\"comment\":\"ok\"}")
        .when()
            .put("/expenses/{id}/review", expenseId)
        .then()
            .statusCode(200);

        assertEquals("approved", getApprovalStatus(expenseId));
    }

    @DisplayName("An invalid status value returns 400 and leaves the approval untouched")
    @Test
    void reviewExpense_invalidStatus_returns400() throws Exception {
        int expenseId = insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        insertPendingApproval(expenseId);

        given()
            .contentType("application/json")
            .body("{\"status\":\"maybe\",\"reviewer\":3,\"comment\":\"unsure\"}")
        .when()
            .put("/expenses/{id}/review", expenseId)
        .then()
            .statusCode(400);

        assertEquals("pending", getApprovalStatus(expenseId));
    }

    @DisplayName("Reviewing an expense with no approval row returns 404")
    @Test
    void reviewExpense_noApprovalForExpense_returns404() throws Exception {
        int expenseId = insertExpense(10, 25.50, "taxi", "2026-07-20", "travel");
        // deliberately no insertPendingApproval() call

        given()
            .contentType("application/json")
            .body("{\"status\":\"approved\",\"reviewer\":3,\"comment\":\"Looks good\"}")
        .when()
            .put("/expenses/{id}/review", expenseId)
        .then()
            .statusCode(404);
    }
}
