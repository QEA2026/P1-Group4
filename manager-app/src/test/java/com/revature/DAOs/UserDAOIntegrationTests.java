package com.revature.DAOs;

import com.revature.models.User;
import com.revature.utils.ConnectionUtil;
import io.qameta.allure.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserDAOIntegrationTests {
    private UserDAO userDAO = new UserDAO();
    private Connection mockConn;
    private PreparedStatement mockPs;
    private ResultSet mockRs;

    @BeforeAll
    static void setup() throws Exception {
        // Create a temp file for the test DB
        Path tempDbFile = Files.createTempFile("test-expense", ".db");
        tempDbFile.toFile().deleteOnExit();

        System.setProperty("expense.db.path", tempDbFile.toString());

        // Create schema and insert test data
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + tempDbFile);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT, password TEXT, role TEXT)");
            stmt.execute("INSERT INTO users (id, username, password, role) VALUES (1, 'testuser', 'pass', 'manager')");
        }
    }

    @Test
    @Description("GetUserByUsername - Correct Local User Input")
    @DisplayName("GetUserByUsername - Correct Local User Input")
    void getUserByUsername_localUserInserted_ReturnsUserInstance() {
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO users (id,username,password,role) VALUES (?,?,?,?)")) {
            ps.setInt(1, 1);
            ps.setString(2, "testuser");
            ps.setString(3, "password123");
            ps.setString(4, "manager");
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Trouble finding username");

        }
        User user = userDAO.getUserByUsername("testuser");
        assertEquals("testuser", user.getUsername(), "Username was not found or did not match");
    }

    @Test
    @Description("GetUserById - Correct Local User Input")
    @DisplayName("GetUserById - Correct Local User Input")
    void getUserById_localUserInserted_ReturnsUserInstance() {
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO users (id,username,password,role) VALUES (?,?,?,?)")) {
            ps.setInt(1, 2);
            ps.setString(2, "testuser2");
            ps.setString(3, "password12345");
            ps.setString(4, "manager");
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Trouble finding ID");

        }
        User user1 = userDAO.getUserById(2);
        assertEquals(2, user1.getId(), "User was not found");
    }
}

