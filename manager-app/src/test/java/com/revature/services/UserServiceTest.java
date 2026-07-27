package com.revature.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.revature.DAOs.UserDAOInterface;
import com.revature.exceptions.InvalidInputException;
import com.revature.exceptions.ResourceNotFoundException;
import com.revature.models.User;
import com.revature.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import at.favre.lib.crypto.bcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserDAOInterface mockUserDAO;

    @BeforeEach
    void createMocks() {
        mockUserDAO = mock(UserDAOInterface.class);
    }

    @Nested
    class UserServiceConstructorTest {

        @Test
        void userService_noArgConstructor_createsInstance() {
            UserService userService = new UserService();
            assertNotNull(userService);
        }
    }

    @Nested
    class UserServiceGetUsersByUsernameTest {
        private UserDAOInterface mockUserDAO;
        private UserService userService;

        @BeforeEach
        void setUp() {
            mockUserDAO = mock(UserDAOInterface.class);
            userService = new UserService(mockUserDAO);
        }

        @Test
        @DisplayName("getUserByUsername - Username is null")
        void getUserByUsername_usernameIsNull_returnsNull() {
            assertThrows(InvalidInputException.class,
                    () -> userService.getUserByUsername(null));
        }

        @Test
        void getUserByUsername_emptyUsername_throwsInvalidInputException() {
            assertThrows(InvalidInputException.class, () -> userService.getUserByUsername("  "));
        }

        @Test
        @DisplayName("getUserByUsername - User is null")
        void getUserByUsername_userNotFound_throwsResourceNotFoundException() {
            when(mockUserDAO.getUserByUsername("nonexistent")).thenReturn(null);
            assertThrows(ResourceNotFoundException.class,
                    () -> userService.getUserByUsername("nonexistent"));
        }

        @Test
        @DisplayName("getUserByUsername - Return User")
        void getUserByUsername_userFound_returnUser() {
            User user = new User(1, "goodUser", "goodPassword", "manager");
            when(mockUserDAO.getUserByUsername("goodUser")).thenReturn(user);

            User result = userService.getUserByUsername("goodUser");
            assertNotNull(result);
            assertEquals("goodUser", result.getUsername());
        }
    }


    @Nested
    class UserServiceGetUserByIdTest {

        private UserDAOInterface mockUserDAO;
        private UserService userService;

        @BeforeEach
        void setUp() {
            mockUserDAO = mock(UserDAOInterface.class);
            userService = new UserService(mockUserDAO);
        }

        @Test
        @DisplayName("getUserById - Id is less than 0")
        void getUserById_invalidId_throwsException() {
            assertThrows(InvalidInputException.class,
                    () -> userService.getUserById(-1));
        }

        @Test
        @DisplayName("getUserById - User is null")
        void getUserById_userIsNull_throwsException() {
            User user = new User(1, "badUser", "badPassword", "manager");
            when(mockUserDAO.getUserById(1)).thenReturn(null);
            assertThrows(ResourceNotFoundException.class,
                    () -> userService.getUserById(user.getId()));
        }

        @Test
        @DisplayName("getUserById - Return User")
        void getUserByUseId_userFound_returnUser() {
            User user = new User(1, "goodUser", "goodPassword", "manager");
            when(mockUserDAO.getUserById(1)).thenReturn(user);

            User result = userService.getUserById(1);
            assertNotNull(result);
            assertEquals(1, result.getId());
        }
    }

    @Nested
    class UserServiceLoginTest {

        private UserDAOInterface mockUserDAO;
        private UserService userService;

        @BeforeEach
        void setUp() {
            mockUserDAO = mock(UserDAOInterface.class);
            userService = new UserService(mockUserDAO);
        }

        @Test
        @DisplayName("login - user object is null")
        void login_userIsNull_throwsInvalidInputException() {
            when(mockUserDAO.getUserByUsername("unknownUser")).thenReturn(null);
            User result = userService.login("unknownUser", "anyPassword");
            assertNull(result);
        }

        @Test
        @DisplayName("login - Username is null")
        void login_usernameIsNull_throwsException() {
            assertThrows(InvalidInputException.class,
                    () -> userService.login(null, "somepassword"));
        }

        @Test
        @DisplayName("login - Password is null")
        void login_passwordIsNull_returnsNull() {
            assertThrows(InvalidInputException.class,
                    () -> userService.login("someUser", null));
        }

        @Test
        @DisplayName("login - role does not equal manager")
        void login_roleIsNotManager_returnsNull() {
            User user = new User(1, "unknownUser", "12345password", "employee");
            when(mockUserDAO.getUserByUsername("unknownUser")).thenReturn(user);
            User result = userService.login("unknownUser", "12345password");
            assertNull(result);

        }

        @Test
        @DisplayName("login - password verification fails returns null")
        void login_passwordVerificationFails_returnsNull() {

            // Hash a real password to set in the user object
            String hashedPassword = BCrypt.withDefaults().hashToString(12, "correctpassword".toCharArray());
            User user = new User(1, "newUser", hashedPassword, "manager");
            when(mockUserDAO.getUserByUsername("newUser")).thenReturn(user);

            // Get the real verifyer OUTSIDE the static mocking block
            BCrypt.Verifyer realVerifyer = BCrypt.verifyer();

            // Get a real Result by verifying a WRONG password
            BCrypt.Result realResult = realVerifyer.verify("wrongpassword".toCharArray(), user.getPassword());

            try (MockedStatic<BCrypt> mockedBCrypt = mockStatic(BCrypt.class)) {
                BCrypt.Verifyer mockVerifyer = mock(BCrypt.Verifyer.class);
                when(mockVerifyer.verify(any(char[].class), anyString())).thenReturn(realResult);

                // Stub the static method to return our mockVerifyer
                mockedBCrypt.when(BCrypt::verifyer).thenReturn(mockVerifyer);

                User result = userService.login("newUser", "wrongpassword");

                assertNull(result);
            }
        }

        @Test
        @DisplayName("login - password verification succeeds returns user")
        void login_passwordVerificationSucceeds_returnsUser() {
            // Create a real hashed password for the correct password
            String hashedPassword = BCrypt.withDefaults().hashToString(12, "correctpassword".toCharArray());
            User user = new User(1, "newUser", hashedPassword, "manager");
            when(mockUserDAO.getUserByUsername("newUser")).thenReturn(user);

            // Get the real verifyer BEFORE static mocking
            BCrypt.Verifyer realVerifyer = BCrypt.verifyer();

            // Get a real Result by verifying the CORRECT password
            BCrypt.Result realResult = realVerifyer.verify("correctpassword".toCharArray(), user.getPassword());

            try (MockedStatic<BCrypt> mockedBCrypt = mockStatic(BCrypt.class)) {
                BCrypt.Verifyer mockVerifyer = mock(BCrypt.Verifyer.class);
                when(mockVerifyer.verify(any(char[].class), anyString())).thenReturn(realResult);

                // Stub the static method to return our mockVerifyer
                mockedBCrypt.when(BCrypt::verifyer).thenReturn(mockVerifyer);

                User result = userService.login("newUser", "correctpassword");

                assertNotNull(result, "User should not be null on successful login");
                assertEquals("newUser", result.getUsername());
                assertNull(result.getPassword(), "Password should be null after login");
            }
        }
    }

}
