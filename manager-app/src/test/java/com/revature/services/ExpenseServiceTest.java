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

        }
    }

    @Nested
    @DisplayName("Validation tests for getExpenseById")
    class getExpenseByIdTests{

        @Test
        @DisplayName("Happy path - returns expenses for a valid expense id")
        void testGetExpenseById_HappyPath(){

        }

        @Test
        @DisplayName("Sad path - returns empty list for when no theres no expense with that id")
        void testGetExpenseById_SadPath(){

        }
    }
}