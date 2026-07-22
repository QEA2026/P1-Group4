package com.revature.services;

import java.util.ArrayList;

import com.revature.DAOs.ExpenseDAO;
import com.revature.DAOs.ExpenseDAOInterface;
import com.revature.DAOs.UserDAO;
import com.revature.DAOs.UserDAOInterface;
import com.revature.exceptions.ResourceNotFoundException;
import com.revature.models.Expense;
import com.revature.models.User;

/*
 * Owns the rules around viewing expenses and reports:
 *  - you can only pull an employee report for a user that exists
 *  - asking for an expense that doesn't exist is an error, not a null
 *
 * Depends on the DAO *interfaces* so unit tests can pass in Mockito mocks
 * instead of hitting the real database.
 */
public class ExpenseService {

    private final ExpenseDAOInterface expenseDAO;
    private final UserDAOInterface userDAO;

    // used by the real app
    public ExpenseService() {
        this(new ExpenseDAO(), new UserDAO());
    }

    // used by unit tests to inject mock DAOs
    public ExpenseService(ExpenseDAOInterface expenseDAO, UserDAOInterface userDAO) {
        this.expenseDAO = expenseDAO;
        this.userDAO = userDAO;
    }

    public ArrayList<Expense> getPendingExpenses() {
        return expenseDAO.getPendingExpenses();
    }

    // Report by employee: the user has to exist, but an empty expense list is valid
    public ArrayList<Expense> getExpensesByEmployee(int userId) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("No user found with id:" + userId);
        }
        return expenseDAO.getExpensesByEmployee(userId);
    }

    public ArrayList<Expense> getExpensesByCategory(String category) {
        return expenseDAO.getExpensesByCategory(category);
    }

    public ArrayList<Expense> getExpenseByDate(String date) {
        return expenseDAO.getExpenseByDate(date);
    }

    // Needed before approving or denying an expense
    public Expense getExpenseById(int expenseId) {
        Expense expense = expenseDAO.getExpenseById(expenseId);
        if (expense == null) {
            throw new ResourceNotFoundException("No expense found with id:" + expenseId);
        }
        return expense;
    }
}
