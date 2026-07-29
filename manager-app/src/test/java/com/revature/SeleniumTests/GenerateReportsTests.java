package com.revature.SeleniumTests;

import com.revature.CucumberTests.DriverManager;
import com.revature.DAOs.ExpenseDAO;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GenerateReportsTests {
    private WebDriver driver;
    private final String BASE_URL = "http://localhost:5500";
    private WebDriverWait wait;

    private void loginAsManager() {
        driver.get(BASE_URL + "/login.html");

        driver.findElement(By.id("username"))
                .sendKeys("testmanager");

        driver.findElement(By.id("password"))
                .sendKeys("MyNewStrongPassword123!");

        driver.findElement(By.xpath("//button[@type='submit']"))
                .click();

        wait.until(ExpectedConditions.urlContains("manager"));
    }

    @Nested
    class GenerateReports {
        ExpenseDAO dao = new ExpenseDAO();

        @BeforeEach
        void setUp() {
            DriverManager.initializeDriver();

            driver = DriverManager.getDriver();

            wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            loginAsManager();
        }

        @AfterEach
        void teardown() {
            DriverManager.quitDriver();
            dao.resetExpenseStatuses();
        }

        @Test
        @DisplayName("Generate Reports - Click Generate Report Button Then Pending Expense Button")
        void generateReport_pressGenerateReportButton_thenPendingExpenseButton() {

            // Click Generate Reports navigation button
            WebElement reportsNavBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-reports")
            ));
            reportsNavBtn.click();

            // Wait until reports section is visible
            WebElement reportsSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("reports-section")
            ));

            assertEquals("Generate Reports",
                    reportsSection.findElement(By.tagName("h3")).getText());


            // Click Generate Employee Report button
            WebElement generateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("generate-employee-report")
            ));
            generateBtn.click();


            // Click Pending Expenses button
            WebElement pendingBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-pending")
            ));
            pendingBtn.click();


            WebElement pendingSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("pending-expenses-section")
            ));

            assertEquals("Pending Expenses for Review",
                    pendingSection.findElement(By.tagName("h3")).getText());
        }

        @Test
        @DisplayName("Generate Employee Report by Employee ID")
        void generateEmployeeReportById() {

            // Click Generate Reports tab
            WebElement reportsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-reports")
            ));
            reportsBtn.click();

            // Wait until reports section is visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("reports-section")
            ));

            // Enter employee ID
            WebElement employeeIdInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("employee-report-id")
            ));

            employeeIdInput.sendKeys("1");

            // Click generate employee report
            WebElement generateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("generate-employee-report")
            ));

            generateBtn.click();

            // Verify success message
            WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-message")
            ));

            assertEquals(
                    "Report loaded successfully",
                    message.getText()
            );

            // Verify report heading
            WebElement reportHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@id='report-results']//h4")
            ));

            assertEquals(
                    "Expenses for employee 1",
                    reportHeading.getText()
            );

            // Verify results table exists
            WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@id='report-results']//table")
            ));

            assertTrue(table.isDisplayed());

            assertFalse(
                    driver.findElement(By.id("report-results")).getText().contains("No expenses found")
            );
        }

        @Test
        @DisplayName("Generate Employee Report with Invalid Employee ID")
        void generateEmployeeReportWithInvalidEmployeeId() {

            // Click Generate Reports tab
            WebElement reportsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-reports")
            ));
            reportsBtn.click();


            // Wait for reports section to appear
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("reports-section")
            ));


            // Enter invalid employee ID
            WebElement employeeIdInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("employee-report-id")
            ));

            employeeIdInput.sendKeys("999999");


            // Click generate employee report
            WebElement generateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("generate-employee-report")
            ));

            generateBtn.click();


            // Verify error message
            WebElement reportMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-message")
            ));

            assertEquals(
                    "No user found with id:999999",
                    reportMessage.getText()
            );
        }
        @Test
        @DisplayName("Generate Employee Report Without Employee ID")
        void generateEmployeeReportWithoutEmployeeId() {

            // Open reports section
            WebElement reportsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-reports")
            ));
            reportsBtn.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("reports-section")
            ));


            // Click generate without entering ID
            WebElement generateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("generate-employee-report")
            ));

            generateBtn.click();


            // Verify validation message
            WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-message")
            ));

            assertEquals(
                    "Please enter an employee ID",
                    message.getText()
            );
        }

        @Test
        @DisplayName("Generate Category Report with Existing Category")
        void generateCategoryReportWithExistingCategory() {

            // Open reports section
            WebElement reportsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-reports")
            ));
            reportsBtn.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("reports-section")
            ));


            // Enter category
            WebElement categoryInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("category-report")
            ));

            categoryInput.sendKeys("meals");


            // Click generate category report
            WebElement generateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("generate-category-report")
            ));

            generateBtn.click();


            // Verify success message
            WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-message")
            ));

            assertEquals(
                    "Report loaded successfully",
                    message.getText()
            );


            // Verify report heading
            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@id='report-results']//h4")
            ));

            assertEquals(
                    "Expenses in category meals",
                    heading.getText()
            );
        }

        @Test
        @DisplayName("Generate Category Report with Invalid Category")
        void generateCategoryReportWithInvalidCategory() {

            // Open reports section
            WebElement reportsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-reports")
            ));
            reportsBtn.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("reports-section")
            ));


            // Enter invalid category
            WebElement categoryInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("category-report")
            ));

            categoryInput.sendKeys("other");


            // Click generate category report
            WebElement generateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("generate-category-report")
            ));

            generateBtn.click();


            // Verify success response from frontend
            WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-message")
            ));

            assertEquals(
                    "Report loaded successfully",
                    message.getText()
            );


            // Verify no expenses returned
            WebElement results = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-results")
            ));

            assertTrue(
                    results.getText().contains("No expenses found."),
                    "Expected no expenses message for invalid category"
            );
        }
        @Test
        @DisplayName("Generate Category Report Without Category")
        void generateCategoryReportWithoutCategory() {

            // Open reports section
            WebElement reportsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-reports")
            ));
            reportsBtn.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("reports-section")
            ));


            // Click generate without entering category
            WebElement generateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("generate-category-report")
            ));

            generateBtn.click();


            // Verify validation message
            WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-message")
            ));

            assertEquals(
                    "Please enter a category",
                    message.getText()
            );
        }
        @Test
        @DisplayName("Generate Date Report with Existing Date")
        void generateDateReportWithExistingDate() {

            // Open reports section
            WebElement reportsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-reports")
            ));
            reportsBtn.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("reports-section")
            ));


            // Enter date
            WebElement dateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-date")
            ));

            dateInput.sendKeys("7-28-2026");


            // Click generate date report
            WebElement generateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("generate-date-report")
            ));

            generateBtn.click();


            // Verify success message
            WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-message")
            ));

            assertEquals(
                    "Report loaded successfully",
                    message.getText()
            );


            // Verify report heading
            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@id='report-results']//h4")
            ));

            assertEquals(
                    "Expenses for 2026-07-28",
                    heading.getText()
            );
        }

        @Test
        @DisplayName("Generate Date Report with Date That Has No Expenses")
        void generateDateReportWithInvalidDate() {

            // Open reports section
            WebElement reportsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-reports")
            ));
            reportsBtn.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("reports-section")
            ));


            // Enter date with no expenses
            WebElement dateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-date")
            ));

            dateInput.sendKeys("01-01-2026");


            // Click generate date report
            WebElement generateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("generate-date-report")
            ));

            generateBtn.click();


            // Verify message
            WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-message")
            ));

            assertEquals(
                    "Report loaded successfully",
                    message.getText()
            );


            // Verify no results message
            WebElement results = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-results")
            ));

            assertTrue(
                    results.getText().contains("No expenses found."),
                    "Expected no expenses message for date with no expenses"
            );
        }

        @Test
        @DisplayName("Generate Date Report Without Date")
        void generateDateReportWithoutDate() {

            // Open reports section
            WebElement reportsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("show-reports")
            ));
            reportsBtn.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("reports-section")
            ));


            // Click generate without entering date
            WebElement generateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("generate-date-report")
            ));

            generateBtn.click();


            // Verify validation message
            WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("report-message")
            ));

            assertEquals(
                    "Please select a date",
                    message.getText()
            );
        }
    }

}
