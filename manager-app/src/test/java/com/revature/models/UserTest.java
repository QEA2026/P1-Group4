package com.revature.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// testing constructors, getters, setters, toString
// from P1-Group4\manager-app\src\main\java\com\revature\models\User.java
@DisplayName("User Model Comprehensive Tests")
public class UserTest {

    // Testing this constructor public User() {}
    @DisplayName("Default constructor with empty values test")
    @Test
    void defaultConstructor_shouldInitializeDefaultValues() {
        User user = new User();

        assertAll(
                () -> assertEquals(0, user.getId()),
                () -> assertNull(user.getUsername()),
                () -> assertNull(user.getPassword()),
                () -> assertNull(user.getRole()));
    }

    // Testing this constructor public User(int id, String username, String password, String role) {}
    @DisplayName("Full constructor with values test")
    @Test
    void fullConstructor_shouldInitializeValues() {
        User user = new User(
                1,
                "test username",
                "test password",
                "manager"
            );

        assertAll(
                () -> assertEquals(1, user.getId()),
                () -> assertEquals("test username", user.getUsername()),
                () -> assertEquals("test password", user.getPassword()),
                () -> assertEquals("manager", user.getRole()));
    }

    // Testing this constructor public User(String username, String password, String role) {}
    @DisplayName("Insert constructor with all values except id test")
    @Test
    void insertConstructor_shouldInitializeValues() {
        User user = new User(
                "test username",
                "test password",
                "manager"
            );

        assertAll(
                () -> assertEquals(0, user.getId()),
                () -> assertEquals("test username", user.getUsername()),
                () -> assertEquals("test password", user.getPassword()),
                () -> assertEquals("manager", user.getRole()));
    }

    // Testing setter methods
    @DisplayName("User setter methods should return the set values test")
    @Test
    void setterMethods_shouldSetValues() {

        // Arrange
        User user = new User();

        // Act
        user.setId(10);
        user.setUsername("new name");
        user.setPassword("newPassword");
        user.setRole("manager");

        // Assert
        assertAll(
                () -> assertEquals(10, user.getId()),
                () -> assertEquals("new name", user.getUsername()),
                () -> assertEquals("newPassword", user.getPassword()),
                () -> assertEquals("manager", user.getRole()));
    }

    // Testing toString method
    @DisplayName("toString Override method should return the formatted User string test")
    @Test
    void toString_shouldContainValues() {

        User user = new User(
                100,
                "username1",
                "newPassword1",
                "manager");

        String expected =
                "User{id=100, username='username1', role='manager'}";

        assertEquals(expected, user.toString());
    }
}