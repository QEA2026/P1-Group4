package com.revature.DAOs;

import com.revature.models.User;
import com.revature.utils.ConnectionUtil;
import io.qameta.allure.Description;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class UserDAOIT {

    private UserDAO userDAO;
    private MockedStatic<ConnectionUtil> connectionUtilMock;

    private static final String H2_URL = "jdbc:h2:mem:userdb;DB_CLOSE_DELAY=-1";


    @BeforeEach
    void setup() throws SQLException {

        userDAO = new UserDAO();

        connectionUtilMock = mockStatic(ConnectionUtil.class);

        connectionUtilMock.when(ConnectionUtil::getConnection)
                .thenAnswer(invocation ->
                        DriverManager.getConnection(H2_URL)
                );

        try (Connection conn = DriverManager.getConnection(H2_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(255) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL
                )
            """);

            stmt.execute("DELETE FROM users");
        }
    }

    @AfterEach
    void tearDown() {

        connectionUtilMock.close();

    }


    @Test
    @Description("GetUserByUsername - Correct Local User Input")
    @DisplayName("GetUserByUsername - Correct Local User Input")
    void getUserByUsername_localUserInserted_ReturnsUserInstance() throws SQLException {

        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (id, username, password, role) VALUES (?, ?, ?, ?)"
             )) {

            ps.setInt(1, 1);
            ps.setString(2, "testuser");
            ps.setString(3, "password123");
            ps.setString(4, "manager");

            ps.executeUpdate();
        }

        User user = userDAO.getUserByUsername("testuser");

        assertNotNull(user, "User should not be null");
        assertEquals("testuser", user.getUsername(),
                "Username was not found or did not match");
    }


    @Test
    @Description("GetUserById - Correct Local User Input")
    @DisplayName("GetUserById - Correct Local User Input")
    void getUserById_localUserInserted_ReturnsUserInstance() throws SQLException {

        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (id, username, password, role) VALUES (?, ?, ?, ?)"
             )) {

            ps.setInt(1, 2);
            ps.setString(2, "testuser2");
            ps.setString(3, "password12345");
            ps.setString(4, "manager");

            ps.executeUpdate();
        }

        User user = userDAO.getUserById(2);

        assertNotNull(user, "User should not be null");
        assertEquals(2, user.getId(),
                "User ID was not found or did not match");
    }
}