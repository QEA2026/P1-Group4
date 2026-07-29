package com.revature.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


// testing constructors, getters, setters, toString 
// from P1-Group4\manager-app\src\main\java\com\revature\models\Approval.java
@DisplayName("Approval Model Comprehensive Tests")
public class ApprovalTest {

    // Testing this constructor public Approval() {} 
    @DisplayName("Default constructor with empty values test")
    @Test
    void defaultConstructor_shouldInitializeDefaultValues() {
        Approval approval = new Approval();

        assertAll(
                () -> assertEquals(0, approval.getId()),
                () -> assertEquals(0, approval.getExpenseId()),
                () -> assertNull(approval.getStatus()),
                () -> assertEquals(0, approval.getReviewer()),
                () -> assertNull(approval.getComment()),
                () -> assertNull(approval.getReviewDate()));
    }

    // Testing this constructor public Approval(int id, int expenseId, String status, int reviewer, String comment, String reviewDate){} 
    @DisplayName("Full constructor with values test")
    @Test
    void fullConstructor_shouldInitializeValues() {
        Approval approval = new Approval(
                1, 1, "pending", 1, "comment exists", "2026-07-22");

        assertAll(
                () -> assertEquals(1, approval.getId()),
                () -> assertEquals(1, approval.getExpenseId()),
                () -> assertEquals("pending", approval.getStatus()),
                () -> assertEquals(1, approval.getReviewer()),
                () -> assertEquals("comment exists", approval.getComment()),
                () -> assertEquals("2026-07-22", approval.getReviewDate()));
    }

    // Testing this constructor public Approval(int expenseId, String status, int reviewer, String comment, String reviewDate) {}
    @DisplayName("Insert constructor with all values execpt id test")
    @Test
    void insertConstructor_shouldInitializeValues() {
        Approval approval = new Approval(
                1, "pending", 1, "comment exists", "2026-07-22");

        assertAll(
                () -> assertEquals(0, approval.getId()),
                () -> assertEquals(1, approval.getExpenseId()),
                () -> assertEquals("pending", approval.getStatus()),
                () -> assertEquals(1, approval.getReviewer()),
                () -> assertEquals("comment exists", approval.getComment()),
                () -> assertEquals("2026-07-22", approval.getReviewDate()));
    }

    // Testing setter methods
    @DisplayName("Approval setter methods should return the set values test")
    @Test
    void setterMethods_shouldSetValues() {

        // Arrange
        Approval approval = new Approval();

        // Act
        approval.setId(10);
        approval.setExpenseId(10);
        approval.setStatus("approved");
        approval.setReviewer(10);
        approval.setComment("comment updated");
        approval.setReviewDate("2025-07-22");

        // Assert
        assertAll(
                () -> assertEquals(10, approval.getId()),
                () -> assertEquals(10, approval.getExpenseId()),
                () -> assertEquals("approved", approval.getStatus()),
                () -> assertEquals(10, approval.getReviewer()),
                () -> assertEquals("comment updated", approval.getComment()),
                () -> assertEquals("2025-07-22", approval.getReviewDate()));
    }

    // Testing toString method
    @DisplayName("toString Override method should return the formatted Approval string test")
    @Test
    void toString_shouldContainValues() {

        Approval approval = new Approval(
                100,
                100,
                "approved",
                100,
                "comment string",
                "2026-07-22");

        String expected = "Approval{id=100, expenseId=100, status='approved', reviewer=100, comment='comment string', reviewDate='2026-07-22'}";

        assertEquals(expected, approval.toString());

    }
}
