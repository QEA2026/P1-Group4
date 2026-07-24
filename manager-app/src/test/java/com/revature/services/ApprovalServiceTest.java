package com.revature.services;


import com.revature.DAOs.ApprovalDAOInterface;
import com.revature.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ApprovalServiceTest {

    private ApprovalDAOInterface mockDao;
    private ApprovalService approvalService;

    @BeforeEach
    void setUp() {
        mockDao = mock(ApprovalDAOInterface.class);
        approvalService = new ApprovalService(mockDao);
    }

    //happy paths!

    @Test
    void reviewExpense_approved_returnsTrue() {
        when(mockDao.updateApproval(57, "approved", 7, "Looks good")).thenReturn(true);
        boolean result = approvalService.reviewExpense(57, "approved", 7, "Looks good");

        assertTrue(result);
        verify(mockDao).updateApproval(57, "approved", 7, "Looks good");
    }

    @Test
    void reviewExpense_denied_returnsTrue() {
        when(mockDao.updateApproval(57, "denied", 7, "Missing receipt")).thenReturn(true);
        boolean result = approvalService.reviewExpense(57, "denied", 7, "Missing receipt");

        assertTrue(result);
        verify(mockDao).updateApproval(57, "denied", 7, "Missing receipt");
    }

    @Test
    void reviewExpense_mixedCaseStatus_isLowercasedBeforeReachingDao() {
        when(mockDao.updateApproval(anyInt(), anyString(), anyInt(), anyString())).thenReturn(true);
        approvalService.reviewExpense(57, "APPROVED", 7, "Looks good");

        // the DAO MUST only see lowercase
        verify(mockDao).updateApproval(57, "approved", 7, "Looks good");
    }

    //sad paths: validation

    @Test
    void reviewExpense_nullStatus_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> approvalService.reviewExpense(57, null, 7, "comment"));

        // validation should fail
        verifyNoInteractions(mockDao);

    }

    @Test
    void reviewExpense_invalidStatus_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> approvalService.reviewExpense(57, "I dunno", 7, "comment"));
        verifyNoInteractions(mockDao);

    }

    @Test
    void reviewExpense_emptyStatus_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> approvalService.reviewExpense(57, "", 7, "comment"));
        verifyNoInteractions(mockDao);

    }

    //sad paths: DAO
    @Test
    void reviewExpense_daoReturnsFalse_returnsFalse() {
        when(mockDao.updateApproval(57, "approved", 7, "comment")).thenReturn(false);
        boolean result = approvalService.reviewExpense(57, "approved", 7, "comment");
        assertFalse(result);

    }

    @Test
    void reviewExpense_daoThrowsResourceNotFoundException_propagatesToCaller() {
        when(mockDao.updateApproval(999, "approved", 7, "comment"))
                .thenThrow(new ResourceNotFoundException("No approval found for expense id: 999"));

        assertThrows(ResourceNotFoundException.class, () ->
                approvalService.reviewExpense(999, "approved", 7, "comment"));

    }
}

