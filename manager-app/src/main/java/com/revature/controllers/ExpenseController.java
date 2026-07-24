package com.revature.controllers;
import com.revature.services.ExpenseService;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import com.revature.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * Handles all HTTP requests related to viewing expenses for the
 * Manager App. This is the "web" layer that sits between Postman
 * (or any future frontend) and the service layer. It doesn't contain
 * any business logic itself, it just parses requests, calls the
 * service, and sends a JSON response back.
 *
 * Covers these manager user stories:
 *  - View all pending expenses awaiting review
 *  - Generate reports by employee, category, or date
 */

public class ExpenseController {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);
    ExpenseService expenseService = new ExpenseService();

    // Returns every expense currently awaiting manager review.
    public Handler getPendingExpensesHandler = (ctx) -> {
        try {
            var pendingExpenses = expenseService.getPendingExpenses();
            ctx.json(pendingExpenses);
            ctx.status(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving expenses: {}", e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("An unexpected error occurred.");
        }
    };

    // {userId} comes from the URL itself, /reports/employee/3
    public Handler getExpensesByEmployeeHandler = (ctx) -> {
        int userId = 0;
        try {
            userId = Integer.parseInt(ctx.pathParam("userId"));

            // service checks the user exists and throws ResourceNotFoundException if not
            var expenses = expenseService.getExpensesByEmployee(userId);
            ctx.json(expenses);
            ctx.status(HttpStatus.OK);

        } catch (ResourceNotFoundException e) {
            logger.warn("User {} not found during retrieval of expenses: {}", userId, e.getMessage());
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving expenses for employee {} : {}", userId, e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("An unexpected error occurred.");
        }
    };

    // e.g. /reports/category/travel
    public Handler getExpensesByCategoryHandler = (ctx) -> {
        String category = "";
        try {
            category = ctx.pathParam("category");
            ctx.json(expenseService.getExpensesByCategory(category));
            ctx.status(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Unexpected error occurred during retrieval of expense by category {} : {}", category, e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("An unexpected error occurred.");
        }
    };

    public Handler getExpenseByDateHandler = (ctx) -> {
        String date = "";
        try {
            date = ctx.pathParam("date");
            ctx.json(expenseService.getExpenseByDate(date));
            ctx.status(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Unexpected error occurred during retrieval of expense by date {} : {}", date, e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("An unexpected error occurred.");
        }
    };

    // we need this before approving or denying it.
    public Handler getExpenseByIdHandler = (ctx) -> {
        int id = 0;
        try {
            id = Integer.parseInt(ctx.pathParam("expenseId"));

            // service throws ResourceNotFoundException if the expense doesn't exist
            var expense = expenseService.getExpenseById(id);
            ctx.json(expense);
            ctx.status(HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            logger.warn("Expense not found with id: {}", id);
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving expense by id {} : {}", id, e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("An unexpected error occurred.");
        }
    };


}
