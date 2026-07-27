package com.revature.DAOs;

import com.revature.exceptions.ResourceNotFoundException;
import com.revature.models.Expense;
import com.revature.utils.ConnectionUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// testing getPendingExpenses, getExpensesByEmployee, getExpensesByCategory
// from P1-Group4\manager-app\src\main\java\com\revature\DAOs\ExpenseDAO.java
@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseDAO Unit Tests")
class ExpenseDAOTest {
    //Setup
    @Mock private Connection conn;
    @Mock private PreparedStatement ps;
    @Mock private ResultSet rs;

    private ExpenseDAO expenseDAO;

    //real instance, reset fresh before every test
    @BeforeEach
    void setUp() {
        expenseDAO = new ExpenseDAO();
    }

    //use MockedStatic to make a static method (ConnectionUtil.getConnection() is static)
    private MockedStatic<ConnectionUtil> mockConnectionUtil() {
        MockedStatic<ConnectionUtil> mocked = mockStatic(ConnectionUtil.class);
        mocked.when(ConnectionUtil::getConnection).thenReturn(conn);
        return mocked;
    }

    // Stubs rs.next()/rs.getX(...) to walk through the given rows in order,
    // matching however many times ExpenseDAO's while(rs.next()) loop calls them.
    private void stubResultSetForRows(List<Expense> rows) throws SQLException {
        if (rows.isEmpty()) {
            when(rs.next()).thenReturn(false);
            return;
        }
        Boolean[] more = new Boolean[rows.size()];
        Arrays.fill(more, true);
        when(rs.next()).thenReturn(true, Arrays.copyOfRange(more, 1, more.length)).thenReturn(false);

        when(rs.getInt("id")).thenReturn(rows.get(0).getId(),
                rows.stream().skip(1).mapToInt(Expense::getId).boxed().toArray(Integer[]::new));
        when(rs.getInt("user_id")).thenReturn(rows.get(0).getUserId(),
                rows.stream().skip(1).mapToInt(Expense::getUserId).boxed().toArray(Integer[]::new));
        when(rs.getDouble("amount")).thenReturn(rows.get(0).getAmount(),
                rows.stream().skip(1).mapToDouble(Expense::getAmount).boxed().toArray(Double[]::new));
        when(rs.getString("description")).thenReturn(rows.get(0).getDescription(),
                rows.stream().skip(1).map(Expense::getDescription).toArray(String[]::new));
        when(rs.getString("date")).thenReturn(rows.get(0).getDate(),
                rows.stream().skip(1).map(Expense::getDate).toArray(String[]::new));
        when(rs.getString("category")).thenReturn(rows.get(0).getCategory(),
                rows.stream().skip(1).map(Expense::getCategory).toArray(String[]::new));
    }

    private void assertExpenseListEquals(List<Expense> expected, List<Expense> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Expense exp = expected.get(i);
            Expense act = actual.get(i);
            int index = i;
            assertAll(
                    "row " + index,
                    () -> assertEquals(exp.getId(), act.getId()),
                    () -> assertEquals(exp.getUserId(), act.getUserId()),
                    () -> assertEquals(exp.getAmount(), act.getAmount(), 0.001),
                    () -> assertEquals(exp.getDescription(), act.getDescription()),
                    () -> assertEquals(exp.getDate(), act.getDate()),
                    () -> assertEquals(exp.getCategory(), act.getCategory()));
        }
    }

    private void assertExpenseEquals(Expense expected, Expense actual) {
        assertAll(
                () -> assertEquals(expected.getId(), actual.getId()),
                () -> assertEquals(expected.getUserId(), actual.getUserId()),
                () -> assertEquals(expected.getAmount(), actual.getAmount(), 0.001),
                () -> assertEquals(expected.getDescription(), actual.getDescription()),
                () -> assertEquals(expected.getDate(), actual.getDate()),
                () -> assertEquals(expected.getCategory(), actual.getCategory()));
    }

    // ---- getPendingExpenses ----

    @DisplayName("getPendingExpenses should return populated list when pending rows exist")
    @Test
    void getPendingExpenses_whenRowsExist_returnsPopulatedList() throws SQLException {
        List<Expense> expected = List.of(
                new Expense(1, 10, 25.50, "taxi", "2026-07-20", "travel"),
                new Expense(2, 11, 99.99, "hotel", "2026-07-21", "travel"));

        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            stubResultSetForRows(expected);

            ArrayList<Expense> result = expenseDAO.getPendingExpenses();

            assertNotNull(result);
            assertExpenseListEquals(expected, result);
        }
    }

    @DisplayName("getPendingExpenses should return empty list when no pending rows match")
    @Test
    void getPendingExpenses_whenNoRowsMatch_returnsEmptyList() throws SQLException {
        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            stubResultSetForRows(List.of());

            ArrayList<Expense> result = expenseDAO.getPendingExpenses();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @DisplayName("getPendingExpenses should return null when a SQLException is thrown")
    @Test
    void getPendingExpenses_whenSqlExceptionThrown_returnsNull() {
        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            ignored.when(ConnectionUtil::getConnection).thenThrow(new SQLException("connection failed"));

            ArrayList<Expense> result = expenseDAO.getPendingExpenses();

            assertNull(result);
        }
    }

    // ---- getExpensesByEmployee ----

    @DisplayName("getExpensesByEmployee should bind userId and return matching expenses")
    @Test
    void getExpensesByEmployee_whenRowsExist_returnsMatchingListAndBindsUserId() throws SQLException {
        int userId = 10;
        List<Expense> expected = List.of(
                new Expense(1, userId, 25.50, "taxi", "2026-07-20", "travel"),
                new Expense(3, userId, 12.00, "coffee", "2026-07-22", "food"));

        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            stubResultSetForRows(expected);

            ArrayList<Expense> result = expenseDAO.getExpensesByEmployee(userId);

            assertNotNull(result);
            assertExpenseListEquals(expected, result);
            verify(ps).setInt(1, userId);
        }
    }

    @DisplayName("getExpensesByEmployee should return empty list when no rows match the userId")
    @Test
    void getExpensesByEmployee_whenNoRowsMatch_returnsEmptyList() throws SQLException {
        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            stubResultSetForRows(List.of());

            ArrayList<Expense> result = expenseDAO.getExpensesByEmployee(99);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @DisplayName("getExpensesByEmployee should return null when a SQLException is thrown")
    @Test
    void getExpensesByEmployee_whenSqlExceptionThrown_returnsNull() {
        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            ignored.when(ConnectionUtil::getConnection).thenThrow(new SQLException("connection failed"));

            ArrayList<Expense> result = expenseDAO.getExpensesByEmployee(10);

            assertNull(result);
        }
    }

    // ---- getExpensesByCategory ----

    @DisplayName("getExpensesByCategory should bind category and return matching expenses")
    @Test
    void getExpensesByCategory_whenRowsExist_returnsMatchingListAndBindsCategory() throws SQLException {
        String category = "travel";
        List<Expense> expected = List.of(
                new Expense(1, 10, 25.50, "taxi", "2026-07-20", category),
                new Expense(2, 11, 99.99, "hotel", "2026-07-21", category));

        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            stubResultSetForRows(expected);

            ArrayList<Expense> result = expenseDAO.getExpensesByCategory(category);

            assertNotNull(result);
            assertExpenseListEquals(expected, result);
            verify(ps).setString(1, category);
        }
    }

    @DisplayName("getExpensesByCategory should return empty list when no rows match the category")
    @Test
    void getExpensesByCategory_whenNoRowsMatch_returnsEmptyList() throws SQLException {
        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            stubResultSetForRows(List.of());

            ArrayList<Expense> result = expenseDAO.getExpensesByCategory("nonexistent");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @DisplayName("getExpensesByCategory should return null when a SQLException is thrown")
    @Test
    void getExpensesByCategory_whenSqlExceptionThrown_returnsNull() {
        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            ignored.when(ConnectionUtil::getConnection).thenThrow(new SQLException("connection failed"));

            ArrayList<Expense> result = expenseDAO.getExpensesByCategory("travel");

            assertNull(result);
        }
    }

    // ---- getExpenseByDate ----

    @DisplayName("getExpenseByDate should bind date and return matching expenses")
    @Test
    void getExpenseByDate_whenRowsExist_returnsMatchingListAndBindsDate() throws SQLException {
        String date = "2026-07-20";
        List<Expense> expected = List.of(
                new Expense(1, 10, 25.50, "taxi", date, "travel"),
                new Expense(4, 12, 40.00, "lunch", date, "food"));

        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            stubResultSetForRows(expected);

            ArrayList<Expense> result = expenseDAO.getExpenseByDate(date);

            assertNotNull(result);
            assertExpenseListEquals(expected, result);
            verify(ps).setString(1, date);
        }
    }

    @DisplayName("getExpenseByDate should return empty list when no rows match the date")
    @Test
    void getExpenseByDate_whenNoRowsMatch_returnsEmptyList() throws SQLException {
        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            stubResultSetForRows(List.of());

            ArrayList<Expense> result = expenseDAO.getExpenseByDate("2026-01-01");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @DisplayName("getExpenseByDate should return null when a SQLException is thrown")
    @Test
    void getExpenseByDate_whenSqlExceptionThrown_returnsNull() {
        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            ignored.when(ConnectionUtil::getConnection).thenThrow(new SQLException("connection failed"));

            ArrayList<Expense> result = expenseDAO.getExpenseByDate("2026-07-20");

            assertNull(result);
        }
    }

    // ---- getExpenseById ----

    @DisplayName("getExpenseById should bind id and return the matching expense")
    @Test
    void getExpenseById_whenFound_returnsExpense() throws SQLException {
        Expense expected = new Expense(1, 10, 25.50, "taxi", "2026-07-20", "travel");

        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            stubResultSetForRows(List.of(expected));

            Expense result = expenseDAO.getExpenseById(1);

            assertExpenseEquals(expected, result);
            verify(ps).setInt(1, 1);
        }
    }

    // No row found: rs.next() returns false, no exception is thrown from the try block,
    // so execution falls through to the DAO's final throw of ResourceNotFoundException.
    @DisplayName("getExpenseById should throw ResourceNotFoundException when no expense matches the id")
    @Test
    void getExpenseById_whenNotFound_throwsResourceNotFoundException() throws SQLException {
        int missingId = 999;

        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            stubResultSetForRows(List.of());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> expenseDAO.getExpenseById(missingId));

            assertEquals("Expense not found with id: " + missingId, ex.getMessage());
        }
    }

    // A SQLException is caught and logged internally, but getExpenseById has no early
    // return on that path either - it falls through to the same final throw as "not found".
    @DisplayName("getExpenseById should throw ResourceNotFoundException when a SQLException is thrown")
    @Test
    void getExpenseById_whenSqlExceptionThrown_throwsResourceNotFoundException() {
        int id = 5;

        try (MockedStatic<ConnectionUtil> ignored = mockConnectionUtil()) {
            ignored.when(ConnectionUtil::getConnection).thenThrow(new SQLException("connection failed"));

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> expenseDAO.getExpenseById(id));

            assertEquals("Expense not found with id: " + id, ex.getMessage());
        }
    }
}
