package com.revature.api;

import at.favre.lib.crypto.bcrypt.BCrypt;
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
import java.sql.Statement;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

// API tests for POST /login: boots the real Javalin app from Main against a
// disposable SQLite database created fresh per test, so these exercise the
// full stack (routing -> AuthController -> UserService -> UserDAO) rather
// than mocks.
@DisplayName("Login API Tests")
class LoginApiTest {

    @TempDir
    Path tempDir;

    private Javalin app;
    private String jdbcUrl;

    @BeforeEach
    void setUp() throws Exception {
        Path dbFile = tempDir.resolve("login-api-test.db");
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

    private void insertUser(String username, String rawPassword, String role) throws Exception {
        String hashed = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashed);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    @DisplayName("Correct manager credentials return 200 with the user and no password")
    @Test
    void login_validManagerCredentials_returns200() throws Exception {
        insertUser("mgr1", "correct-horse", "manager");

        given()
            .contentType("application/json")
            .body("{\"username\":\"mgr1\",\"password\":\"correct-horse\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .body("username", equalTo("mgr1"))
            .body("role", equalTo("manager"))
            .body("password", nullValue());
    }

    @DisplayName("Wrong password returns 401")
    @Test
    void login_wrongPassword_returns401() throws Exception {
        insertUser("mgr2", "correct-horse", "manager");

        given()
            .contentType("application/json")
            .body("{\"username\":\"mgr2\",\"password\":\"wrong-password\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(401);
    }

    @DisplayName("Unknown username returns 401, not 404 (no user enumeration)")
    @Test
    void login_unknownUsername_returns401() {
        given()
            .contentType("application/json")
            .body("{\"username\":\"ghost\",\"password\":\"whatever\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(401);
    }

    @DisplayName("Non-manager role returns 401 even with the correct password")
    @Test
    void login_employeeRole_returns401() throws Exception {
        insertUser("emp1", "correct-horse", "employee");

        given()
            .contentType("application/json")
            .body("{\"username\":\"emp1\",\"password\":\"correct-horse\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(401);
    }

    @DisplayName("Blank username returns 400")
    @Test
    void login_blankUsername_returns400() {
        given()
            .contentType("application/json")
            .body("{\"username\":\"\",\"password\":\"whatever\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(400);
    }

    @DisplayName("Missing password field returns 400")
    @Test
    void login_missingPassword_returns400() {
        given()
            .contentType("application/json")
            .body("{\"username\":\"mgr1\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(400);
    }
}
