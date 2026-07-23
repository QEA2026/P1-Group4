package com.revature.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// testing constructors, getters, setters, toString 
// from P1-Group4\manager-app\src\main\java\com\revature\models\Expense.java
@DisplayName("Expense Model Comprehensive Tests")
public class ExpenseTest {

    // Testing this constructor public Expense() {}
    @DisplayName("Default constructor with empty values test")
    @Test
    void defaultConstructor_shouldInitializeDefaultValues() {
        Expense expense = new Expense();

        assertAll(
                () -> assertEquals(0, expense.getId()),
                () -> assertEquals(0, expense.getUserId()),
                () -> assertEquals(0.0, expense.getAmount(), 0.001),
                () -> assertNull(expense.getDescription()),
                () -> assertNull(expense.getDate()),
                () -> assertNull(expense.getCategory()));
    }

    // Testing this constructor
    // public Expense(int id, int userId, double amount, String description, String date, String category) {}
    @DisplayName("Full constructor with values test")
    @Test
    void fullConstructor_shouldInitializeValues() {
        Expense expense = new Expense(
                1,
                1,
                9.99,
                "expense description",
                "2026-07-22",
                "expense category"
            );

        assertAll(
                () -> assertEquals(1, expense.getId()),
                () -> assertEquals(1, expense.getUserId()),
                () -> assertEquals(9.99, expense.getAmount(), 0.001),
                () -> assertEquals("expense description", expense.getDescription()),
                () -> assertEquals("2026-07-22", expense.getDate()),
                () -> assertEquals("expense category", expense.getCategory()));
    }

    // Testing this constructor
    // public Expense(int userId, double amount, String description, String date, String category) {}
    @DisplayName("Insert constructor with all values except id test")
    @Test
    void insertConstructor_shouldInitializeValues() {
        Expense expense = new Expense(
                1,
                9.99,
                "expense description",
                "2026-07-22",
                "expense category"
            );

        assertAll(
                () -> assertEquals(0, expense.getId()),
                () -> assertEquals(1, expense.getUserId()),
                () -> assertEquals(9.99, expense.getAmount(), 0.001),
                () -> assertEquals("expense description", expense.getDescription()),
                () -> assertEquals("2026-07-22", expense.getDate()),
                () -> assertEquals("expense category", expense.getCategory()));
    }

    // Testing setter methods
    @DisplayName("Expense setter methods should return the set values test")
    @Test
    void setterMethods_shouldSetValues() {

        // Arrange
        Expense expense = new Expense();

        // Act
        expense.setId(10);
        expense.setUserId(10);
        expense.setAmount(99.99);
        expense.setDescription("description updated");
        expense.setDate("2025-07-22");
        expense.setCategory("category updated");

        // Assert
        assertAll(
                () -> assertEquals(10, expense.getId()),
                () -> assertEquals(10, expense.getUserId()),
                () -> assertEquals(99.99, expense.getAmount(), 0.001),
                () -> assertEquals("description updated", expense.getDescription()),
                () -> assertEquals("2025-07-22", expense.getDate()),
                () -> assertEquals("category updated", expense.getCategory()));
    }

    // Testing toString method
    @DisplayName("toString Override method should return the formatted Expense string test")
    @Test
    void toString_shouldContainValues() {

        Expense expense = new Expense(
                100,
                100,
                999.99,
                "description string",
                "2026-07-22",
                "category string");

        String expected =
                "Expense{id=100, userId=100, amount=999.99, description='description string', date='2026-07-22', category='category string'}";

        assertEquals(expected, expense.toString());
    }
}