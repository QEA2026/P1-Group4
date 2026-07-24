package com.revature.DAOs;

import com.revature.exceptions.ResourceNotFoundException;
import com.revature.models.Approval;
import com.revature.utils.ConnectionUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ApprovalDAOTest {

    private ApprovalDAO approvalDAO;
    private MockedStatic<ConnectionUtil> connectionUtilMock;

    private Connection mockConn;
    private PreparedStatement mockPrepStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        approvalDAO = new ApprovalDAO();
        mockConn = mock(Connection.class);
        mockPrepStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        connectionUtilMock = mockStatic(ConnectionUtil.class);
        connectionUtilMock.when(ConnectionUtil::getConnection).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPrepStatement);
    }

    @AfterEach
    void tearDown() {
        connectionUtilMock.close();
    }

    //getApprovalByExpenseId tests
    @Test
    void getApprovalByExpenseId_happyPath_returnsApproval() throws SQLException {
        when(mockPrepStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getInt("expense_id")).thenReturn(46);
        when(mockResultSet.getString("status")).thenReturn("PENDING");
        when(mockResultSet.getInt("reviewer")).thenReturn(7);
        when(mockResultSet.getString("comment")).thenReturn("Looks fine");
        when(mockResultSet.getString("review_date")).thenReturn("2026-07-22");

        Approval result = approvalDAO.getApprovalByExpenseId(46);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(46, result.getExpenseId());
        assertEquals("PENDING", result.getStatus());
        assertEquals(7, result.getReviewer());
        assertEquals("Looks fine", result.getComment());
        assertEquals("2026-07-22", result.getReviewDate());

        verify(mockPrepStatement).setInt(1, 46);
    }

    @Test
    void getApprovalByExpenseId_notFound_returnsNull() throws SQLException {

        when(mockPrepStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Approval result = approvalDAO.getApprovalByExpenseId(999);

        assertNull(result);
    }

    @Test
    void getApprovalByExpenseId_sadPath_sqlException_returnsNull() throws SQLException {
        when(mockPrepStatement.executeQuery()).thenThrow(new SQLException("connection lost"));

        Approval result = approvalDAO.getApprovalByExpenseId(46);

        assertNull(result);
    }

    //updateApproval tests
    @Test
    void updateApproval_happyPath_returnsTrue() throws SQLException {
        when(mockPrepStatement.executeUpdate()).thenReturn(1);

        boolean result = approvalDAO.updateApproval(46, "APPROVED", 7, "Good to go");

        assertTrue(result);
        verify(mockPrepStatement).setString(1, "APPROVED");
        verify(mockPrepStatement).setInt(2, 7);
        verify(mockPrepStatement).setString(3, "Good to go");
        verify(mockPrepStatement).setInt(5, 46);
    }

    @Test
    void updateApproval_sadPath_noRowsAffected_throwsResourceNotFoundException() throws SQLException {
        when(mockPrepStatement.executeUpdate()).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () ->
                approvalDAO.updateApproval(999, "APPROVED", 7, "Good to go"));
    }

    @Test
    void updateApproval_sadPath_sqlException_returnsFalse() throws SQLException {
        when(mockPrepStatement.executeUpdate()).thenThrow(new SQLException("connection lost"));

        boolean result = approvalDAO.updateApproval(46, "APPROVED", 7, "Good to go");

        assertFalse(result);
    }
}