package com.revature.services;

import com.revature.DAOs.ExpenseDAOInterface;
import com.revature.DAOs.UserDAOInterface;
import com.revature.exceptions.ResourceNotFoundException;
import com.revature.models.Expense;
import com.revature.models.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    ExpenseDAOInterface expenseDAO;

    @Mock
    UserDAOInterface userDAO;

    // Creates a real ExpenseService object
    @InjectMocks
    ExpenseService expenseService;

    private Expense buildFakeExpense(int id, String category, String date){
        return new Expense(id, 1, 100.00, "just for testing", date, category);
    }

    /*
    * What im testing:
        * The service correctly delegates to the DAO
        * The service returns whatever the DAO returns without modifying it
     */
    @Nested
    @DisplayName("Tests for getPendingExpenses")
    class getPendingExpensesTests{

        @Test
        @DisplayName("Happy path - returns all pending expenses from the DAO")
        void testGetPendingExpenses_HappyPath(){
            ArrayList<Expense> fakeExpenses = new ArrayList<>();
            fakeExpenses.add(buildFakeExpense(1, "travel", "2026-06-01"));
            fakeExpenses.add(buildFakeExpense(2, "meals", "2026-06-03"));

            when(expenseDAO.getPendingExpenses()).thenReturn(fakeExpenses);

            ArrayList<Expense> result = expenseService.getPendingExpenses();

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(expenseDAO).getPendingExpenses();
        }

        @Test
        @DisplayName("Sad path - returns empty list when nothing is pending")
        void testGetPendingExpenses_SadPath(){
            when(expenseDAO.getPendingExpenses()).thenReturn(new ArrayList<>());

            ArrayList<Expense> result = expenseService.getPendingExpenses();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    /*
    * What im testing:
        * The user-exists business rule: you can only pull a report for a real user
        * A user with no expenses is valid (empty list), a missing user is an error
     */
    @Nested
    @DisplayName("Tests for getExpensesByEmployee")
    class getExpensesByEmployeeTests{

        @Test
        @DisplayName("Happy path - user exists, returns their expenses")
        void testGetExpensesByEmployee_HappyPath(){
            int userId = 1;
            User fakeUser = new User(userId, "frank", "hashed", "employee");
            ArrayList<Expense> fakeExpenses = new ArrayList<>();
            fakeExpenses.add(buildFakeExpense(1, "travel", "2026-06-01"));

            when(userDAO.getUserById(userId)).thenReturn(fakeUser);
            when(expenseDAO.getExpensesByEmployee(userId)).thenReturn(fakeExpenses);

            ArrayList<Expense> result = expenseService.getExpensesByEmployee(userId);

            assertEquals(1, result.size());
            verify(expenseDAO).getExpensesByEmployee(userId);
        }

        @Test
        @DisplayName("Happy path - user exists but has no expenses, empty list is valid")
        void testGetExpensesByEmployee_NoExpenses(){
            int userId = 1;
            User fakeUser = new User(userId, "vanessa", "hashed", "employee");

            when(userDAO.getUserById(userId)).thenReturn(fakeUser);
            when(expenseDAO.getExpensesByEmployee(userId)).thenReturn(new ArrayList<>());

            ArrayList<Expense> result = expenseService.getExpensesByEmployee(userId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Sad path - user does not exist, throws ResourceNotFoundException")
        void testGetExpensesByEmployee_UserNotFound(){
            int userId = 999;
            when(userDAO.getUserById(userId)).thenReturn(null);

            assertThrows(ResourceNotFoundException.class,
                    () -> expenseService.getExpensesByEmployee(userId));

            // the expense DAO must never be hit for a user that doesn't exist
            verify(expenseDAO, never()).getExpensesByEmployee(anyInt());
        }
    }

    /*
    * What im testing:
        * The service correctly delegates to the DAO
        * The service returns whatever the DAO returns without modifying it
     */
    @Nested
    @DisplayName("Tests for getExpensesByCategory")
    class getExpensesByCategoryTests{

        @Test
        @DisplayName("Happy path - returns expenses for a valid category")
        void testGetExpensesByCategory_HappyPath(){
            String category = "travel";
            ArrayList<Expense> fakeExpenses = new ArrayList<>();
            fakeExpenses.add(buildFakeExpense(1, category, "2026-06-01"));

            when(expenseDAO.getExpensesByCategory(category)).thenReturn(fakeExpenses);

            ArrayList<Expense> result = expenseService.getExpensesByCategory(category);

            assertEquals(1, result.size());
            assertEquals(category, result.get(0).getCategory());
            verify(expenseDAO).getExpensesByCategory(category);
        }

        @Test
        @DisplayName("Sad path - returns empty list for a category with no expenses")
        void testGetExpensesByCategory_SadPath(){
            when(expenseDAO.getExpensesByCategory("does-not-exist")).thenReturn(new ArrayList<>());

            ArrayList<Expense> result = expenseService.getExpensesByCategory("does-not-exist");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    /*
    * What im testing:
        * The service correctly delegates to the DAO
        * The service returns whatever the DAO returns without modifying it
     */
    @Nested
    @DisplayName("Tests for getExpenseByDate")
    class getExpenseByDateTests{

        @Test
        @DisplayName("Happy path - returns expenses for a valid date")
        void testGetExpenseByDate_HappyPath(){
            String testDate = "2026-06-01";
            ArrayList<Expense> fakeExpenses = new ArrayList<>();
            fakeExpenses.add(buildFakeExpense(1, "travel", testDate));
            fakeExpenses.add(buildFakeExpense(2, "meals", testDate));

            // Tell the mock what to return when called with the date
            when(expenseDAO.getExpenseByDate(testDate)).thenReturn(fakeExpenses);

            // Act
            ArrayList<Expense> result = expenseService.getExpenseByDate(testDate);

            //Assert
            assertNotNull(result);
            assertEquals(2,result.size());
            assertEquals(testDate, result.get(0).getDate());

            //verify the serice actually called the DAO with the right date
            verify(expenseDAO).getExpenseByDate(testDate);
        }

        @Test
        @DisplayName("Sad Path - returns empty list when no expenses on that date")
        void testGetExpenseByDate_SadPath(){
            String testDate = "1999-01-01";
            when(expenseDAO.getExpenseByDate(testDate)).thenReturn(new ArrayList<>());

            ArrayList<Expense> result = expenseService.getExpenseByDate(testDate);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    /*
    * What im testing:
        * The "asking for an expense that doesn't exist is an error, not a null"
          business rule - this lookup happens right before approving or denying
     */
    @Nested
    @DisplayName("Validation tests for getExpenseById")
    class getExpenseByIdTests{

        @Test
        @DisplayName("Happy path - returns the expense for a valid expense id")
        void testGetExpenseById_HappyPath(){
            int expenseId = 1;
            Expense fakeExpense = buildFakeExpense(expenseId, "travel", "2026-06-01");

            when(expenseDAO.getExpenseById(expenseId)).thenReturn(fakeExpense);

            Expense result = expenseService.getExpenseById(expenseId);

            assertNotNull(result);
            assertEquals(expenseId, result.getId());
            verify(expenseDAO).getExpenseById(expenseId);
        }

        @Test
        @DisplayName("Sad path - throws ResourceNotFoundException when no expense with that id")
        void testGetExpenseById_SadPath(){
            int expenseId = 999;
            when(expenseDAO.getExpenseById(expenseId)).thenReturn(null);

            ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
                    () -> expenseService.getExpenseById(expenseId));

            assertEquals("No expense found with id:" + expenseId, e.getMessage());
        }
    }
}
