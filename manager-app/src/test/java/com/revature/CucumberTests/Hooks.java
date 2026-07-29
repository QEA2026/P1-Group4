package com.revature.CucumberTests;

import com.revature.DAOs.ExpenseDAO;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class Hooks {

    @Before
    public void setUp() {
        DriverManager.initializeDriver();
    }


    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }

    @After("@expense")
    public void resetExpenses() {
        ExpenseDAO dao = new ExpenseDAO();
        dao.resetExpenseStatuses();
    }
}