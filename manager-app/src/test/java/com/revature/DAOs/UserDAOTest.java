package com.revature.DAOs;

import com.revature.models.User;
import com.revature.utils.ConnectionUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import io.qameta.allure.Description;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

//import static com.revature.DAOs.UserDAO.logger;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDAO Test Parent Class")
class UserDAOTest {

    private UserDAO userDAO = new UserDAO();
    private Connection mockConn;
    private PreparedStatement mockPs;
    private ResultSet mockRs;

    @Nested
    class MockedUnitTests {

        @BeforeEach
        @DisplayName("Creating Mocks for UserDAO tests")
        void createMocks() {
            mockConn = mock(Connection.class);
            mockPs = mock(PreparedStatement.class);
            mockRs = mock(ResultSet.class);
        }

        @Test
        @Description("GetUserByUsername - Using Only Mocks")
        @DisplayName("GetUserByUsername - Using Only Mocks")
        void getUserByUsername_mockingDependencies_ReturnsUserInstance() {
            try (MockedStatic<ConnectionUtil> mockedStatic = mockStatic(ConnectionUtil.class)) {
                mockedStatic.when(ConnectionUtil::getConnection).thenReturn(mockConn);

                when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
                when(mockPs.executeQuery()).thenReturn(mockRs);

                when(mockRs.next()).thenReturn(true);  // simulate one row found
                when(mockRs.getInt("id")).thenReturn(1);
                when(mockRs.getString("username")).thenReturn("testuser");
                when(mockRs.getString("password")).thenReturn("password123");
                when(mockRs.getString("role")).thenReturn("manager");

                User user = userDAO.getUserByUsername("testuser");

                assertEquals("testuser", user.getUsername());
                assertEquals("manager", user.getRole());

            } catch (SQLException e) {
                System.out.println("Problem finding Username");
            }
        }

        @Test
        @Description("GetUserByID - Mocking Entire Process")
        @DisplayName("GetUserByID - Mocking Entire Process")
        void getUserByID_mockedInfoInserted_returnUserInstance() {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            try (MockedStatic<ConnectionUtil> mockedStatic = mockStatic(ConnectionUtil.class)) {
                mockedStatic.when(ConnectionUtil::getConnection).thenReturn(mockConn);
                when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
                when(mockPs.executeQuery()).thenReturn(mockRs);

                when(mockRs.next()).thenReturn(true);
                when(mockRs.getInt("id")).thenReturn(2);
                when(mockRs.getString("username")).thenReturn("JamWhit08");
                when(mockRs.getString("password")).thenReturn("110802Jaw!");
                when(mockRs.getString("role")).thenReturn("employee");

                User newUser = userDAO.getUserById(2);

                assertEquals(2, newUser.getId());
            } catch (SQLException e) {
                System.out.println("User was not found with specified id");
            }
        }

        @Test
        @Description("getUserByUsername - returns null for null username")
        @DisplayName("getUserByUsername - returns null for null username")
        void getUserByUsername_nullUsername_returnsNull() {
            User result = userDAO.getUserByUsername(null);
            Assertions.assertNull(result);
        }

        @Test
        @Description("getUserByUsername - Connection fails exception thrown")
        @DisplayName("getUserByUsername - Connection fails exception thrown")
        void getUserByUsername_connectionFails_throwsException() {
            try(MockedStatic<ConnectionUtil> mocked = mockStatic(ConnectionUtil.class)) {
                mocked.when(ConnectionUtil::getConnection).thenThrow(new SQLException ("Forced Exception"));
                User user = userDAO.getUserByUsername("someUsername");

                assertNull(user);
            }

        }
        @Test
        @Description("getUserById - Connection fails exception thrown")
        @DisplayName("getUserById - Connection fails exception thrown")
        void getUserById_connectionFails_throwsException() {
            try(MockedStatic<ConnectionUtil> mocked = mockStatic(ConnectionUtil.class)) {
                mocked.when(ConnectionUtil::getConnection).thenThrow(new SQLException ("Forced Exception"));
                User user = userDAO.getUserById(1);

                assertNull(user);
            }
        }
    }
}

