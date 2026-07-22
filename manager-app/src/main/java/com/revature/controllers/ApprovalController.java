package com.revature.controllers;

import com.revature.exceptions.ResourceNotFoundException;
import com.revature.models.Approval;
import com.revature.services.ApprovalService;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * ApprovalController
 *
 * Handles all HTTP requests related to reviewing expenses.
 * Managers use this controller to approve or deny pending
 * expense reports submitted by employees. The actual review
 * rules live in ApprovalService; this class only translates
 * HTTP requests and responses.
 *
 * Covers this manager user story:
 *  - As a manager, I want to approve or deny submitted expenses
 *    so that I can manage reimbursements appropriately.
 *  - As a manager, I want to add comments to expense decisions
 *    so that employees understand the reasoning behind approvals or denials.
 */

public class ApprovalController {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalController.class);
    ApprovalService approvalService = new ApprovalService();

    // PUT /expenses/{id}/review
    // Body: { "status": "approved", "reviewer": 3, "comment": "Looks good" }
    public Handler reviewExpenseHandler = (ctx) -> {
        int expenseId = 0;
        try {
            // get the expense id from the url path
            expenseId = Integer.parseInt(ctx.pathParam("id"));

            // then deserialize the JSON request body into an Approval object
            Approval reviewRequest = ctx.bodyAsClass(Approval.class);

            // service validates the status and throws IllegalArgumentException if invalid
            boolean success = approvalService.reviewExpense(
                    expenseId,
                    reviewRequest.getStatus(),
                    reviewRequest.getReviewer(),
                    reviewRequest.getComment()
            );

            if (success) {
                ctx.status(HttpStatus.OK);
                ctx.result("Expense " + reviewRequest.getStatus().toLowerCase() + " successfully.");
            } else {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.result("Could not update expense.");
            }

        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result(e.getMessage());
        } catch (ResourceNotFoundException e) {
            logger.warn("No approval found for expense id: {}", expenseId);
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error reviewing expense {} : {}", expenseId, e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("An unexpected error occurred.");
        }
    };
}
