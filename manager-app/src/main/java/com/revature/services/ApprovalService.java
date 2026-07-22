package com.revature.services;

import com.revature.DAOs.ApprovalDAO;
import com.revature.DAOs.ApprovalDAOInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *
 * Owns the review rules:
 *  - a review can only ever result in "approved" or "denied"; anything else
 *    would be written straight to the approvals table and make the expense
 *    invisible to both the pending list and the history report
 *  - status is stored lowercase so it always matches the queries that
 *    filter on status
 *
 * Depends on the DAO *interface* so unit tests can pass in a Mockito mock
 * instead of hitting the real database.
 */
public class ApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalService.class);

    private final ApprovalDAOInterface approvalDAO;

    // used by the real app
    public ApprovalService() {
        this(new ApprovalDAO());
    }

    // used by unit tests to inject a mock DAO
    public ApprovalService(ApprovalDAOInterface approvalDAO) {
        this.approvalDAO = approvalDAO;
    }

    // Throws IllegalArgumentException for an invalid status; returns whether
    // the update actually changed a row.
    public boolean reviewExpense(int expenseId, String status, int reviewer, String comment) {
        if (status == null
                || !(status.equalsIgnoreCase("approved") || status.equalsIgnoreCase("denied"))) {
            logger.warn("Rejected review of expense {} with invalid status: {}", expenseId, status);
            throw new IllegalArgumentException("Status must be either 'approved' or 'denied'.");
        }

        boolean success = approvalDAO.updateApproval(expenseId, status.toLowerCase(), reviewer, comment);

        if (success) {
            logger.info("Expense {} successfully updated to status: {}", expenseId, status.toLowerCase());
        } else {
            logger.warn("Failed to update expense {}", expenseId);
        }
        return success;
    }
}
