package com.revature;

import com.revature.DAOs.UserDAO;
import com.revature.models.User;
import com.revature.utils.ConnectionUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.transform.Result;import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

//import static com.revature.DAOs.UserDAO.logger;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDAO Test Parent Class")
class TestUserDAO {


    @Mock
    PreparedStatement ps;

    @Mock
    ConnectionUtil connectionUtil;

    private UserDAO userDAO = new UserDAO();

    //private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    @Nested
    class CreateUserTestDAO {

        @BeforeEach
        void cleanDatabase() throws SQLException {
            try (Connection conn = ConnectionUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM users")) {
                ps.executeUpdate();
            }
        }

        @Test
        @DisplayName("GetUserByUsername - Using Only Mocks")
        void getUserByUsername_mockingDependencies_ReturnsUserInstance() {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            try (MockedStatic<ConnectionUtil> mockedStatic = mockStatic(ConnectionUtil.class)) {
                mockedStatic.when(ConnectionUtil::getConnection).thenReturn(mockConn);

                when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
                when(mockPs.executeQuery()).thenReturn(mockRs);

                when(mockRs.next()).thenReturn(true);  // simulate one row found
                when(mockRs.getInt("id")).thenReturn(1);
                when(mockRs.getString("username")).thenReturn("testuser");
                when(mockRs.getString("password")).thenReturn("password123");
                when(mockRs.getString("role")).thenReturn("manager");

                // Call method under test
                User user = userDAO.getUserByUsername("testuser");

                // Verify results
                assertEquals("testuser", user.getUsername());
                assertEquals("manager", user.getRole());

            } catch (SQLException e) {
                System.out.println("Problem finding Username");
            }
        }

        @Test
        @DisplayName("GetUserByID - Mocking Entire Process")
        void getUserByID_mockedInfoInserted_returnUserInstance() {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            try(MockedStatic<ConnectionUtil> mockedStatic = mockStatic(ConnectionUtil.class)) {
                mockedStatic.when(ConnectionUtil::getConnection).thenReturn(mockConn);
                when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
                when(mockPs.executeQuery()).thenReturn(mockRs);

                when(mockRs.next()).thenReturn(true);
                when(mockRs.getInt("id")).thenReturn(2);
                when(mockRs.getString("username")).thenReturn("JamWhit08");
                when(mockRs.getString("password")).thenReturn("110802Jaw!");
                when(mockRs.getString("role")).thenReturn("employee");

                User newUser = userDAO.getUserById(2);

                assertEquals(2,newUser.getId());
            } catch (SQLException e) {
                System.out.println("User was not found with specified id");
            }
        }

//        @Test
//        @DisplayName("GetUserByUsername - Correct Local User Input")
//        void getUserByUsername_localUserInserted_ReturnsUserInstance() {
//            try (Connection conn = ConnectionUtil.getConnection();
//                 PreparedStatement ps = conn.prepareStatement("INSERT INTO users (id,username,password,role) VALUES (?,?,?,?)")) {
//                ps.setInt(1, 1);
//                ps.setString(2, "testuser");
//                ps.setString(3, "password123");
//                ps.setString(4, "manager");
//                ps.executeUpdate();
//            } catch (SQLException e) {
//                System.out.println("Trouble finding username");
//
//            }
//            User user = userDAO.getUserByUsername("testuser");
//            assertEquals("testuser", user.getUsername(), "Username was not found or did not match");
//        }
//
//        @Test
//        @DisplayName("GetUserByUsername - Correct Local User Input")
//        void getUserById_localUserInserted_ReturnsUserInstance() {
//            try (Connection conn = ConnectionUtil.getConnection();
//                 PreparedStatement ps = conn.prepareStatement("INSERT INTO users (id,username,password,role) VALUES (?,?,?,?)")) {
//                ps.setInt(1, 2);
//                ps.setString(2, "testuser2");
//                ps.setString(3, "password12345");
//                ps.setString(4, "manager");
//                ps.executeUpdate();
//            } catch (SQLException e) {
//                System.out.println("Trouble finding ID");
//
//            }
//            User user1 = userDAO.getUserById(2);
//            assertEquals(2, user1.getId(), "User was not found");
//        }
    }
}

