package com.revature.SeleniumTests;

import com.revature.utils.ConnectionUtil;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManagerDashboardSeleniumTest {
    private WebDriver driver;
    private final String BASE_URL = "http://localhost:5500";
    private WebDriverWait wait;

    private void loginAsManager() {
        driver.get(BASE_URL + "/login.html");

        driver.findElement(By.id("username"))
                .sendKeys("testmanager");

        driver.findElement(By.id("password"))
                .sendKeys("newPassword123!");

        driver.findElement(By.xpath("//button[@type='submit']"))
                .click();

        wait.until(ExpectedConditions.urlContains("manager"));
    }

    @Nested
    class ManagerPageTests {
        private static final double SEEDED_AMOUNT = 82.19;
        private static final String SEEDED_CATEGORY = "meals";
        private static final String SEEDED_DATE = "2026-06-03";
        private Integer seededExpenseId;
        private int seededEmployeeId;
        private String seededDescription;

        @BeforeEach
        void setUp(TestInfo testInfo) throws SQLException {
            if (testInfo.getTestMethod()
                    .map(method -> method.getName().startsWith("managerDashboard_reviewExpense_"))
                    .orElse(false)) {
                seedPendingExpense();
            }

            //Does successful login to access manager app
            driver = new ChromeDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            loginAsManager();
        }

        @AfterEach
        void teardown() throws SQLException {
            try {
                if (driver != null) {
                    driver.quit();
                }
            } finally {
                deleteSeededExpense();
            }
        }

        private void seedPendingExpense() throws SQLException {
            try (Connection conn = ConnectionUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement employee = conn.prepareStatement(
                            "SELECT id FROM users WHERE LOWER(role) = 'employee' ORDER BY id LIMIT 1");
                         ResultSet result = employee.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("No employee exists for the Selenium test expense");
                        }
                        seededEmployeeId = result.getInt("id");
                    }

                    try (PreparedStatement expense = conn.prepareStatement(
                            "INSERT INTO expenses (user_id, amount, description, date, category) " +
                                    "VALUES (?, ?, ?, ?, ?) RETURNING id")) {
                        expense.setInt(1, seededEmployeeId);
                        expense.setDouble(2, SEEDED_AMOUNT);
                        expense.setString(3, "Selenium dashboard test expense");
                        expense.setString(4, SEEDED_DATE);
                        expense.setString(5, SEEDED_CATEGORY);
                        try (ResultSet result = expense.executeQuery()) {
                            result.next();
                            seededExpenseId = result.getInt("id");
                        }
                    }

                    seededDescription = "Selenium dashboard test expense " + seededExpenseId;
                    try (PreparedStatement description = conn.prepareStatement(
                            "UPDATE expenses SET description = ? WHERE id = ?")) {
                        description.setString(1, seededDescription);
                        description.setInt(2, seededExpenseId);
                        description.executeUpdate();
                    }

                    try (PreparedStatement approval = conn.prepareStatement(
                            "INSERT INTO approvals (expense_id, status, reviewer, comment) " +
                                    "VALUES (?, 'pending', NULL, NULL)")) {
                        approval.setInt(1, seededExpenseId);
                        approval.executeUpdate();
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    seededExpenseId = null;
                    throw e;
                }
            }
        }

        private void deleteSeededExpense() throws SQLException {
            if (seededExpenseId == null) {
                return;
            }

            try (Connection conn = ConnectionUtil.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement approval = conn.prepareStatement(
                             "DELETE FROM approvals WHERE expense_id = ?");
                     PreparedStatement expense = conn.prepareStatement(
                             "DELETE FROM expenses WHERE id = ?")) {
                    approval.setInt(1, seededExpenseId);
                    approval.executeUpdate();
                    expense.setInt(1, seededExpenseId);
                    expense.executeUpdate();
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } finally {
                seededExpenseId = null;
            }
        }

        private String seededExpenseRowXpath() {
            return "//tr[" +
                    "td[text()='" + seededEmployeeId + "'] and " +
                    "td[text()='" + SEEDED_DATE + "'] and " +
                    "td[text()='$" + String.format("%.2f", SEEDED_AMOUNT) + "'] and " +
                    "td[text()='" + SEEDED_CATEGORY + "'] and " +
                    "td[text()='" + seededDescription + "']" +
                    "]";
        }

        @Test
        @Order(1)
        @DisplayName("Manager Page - Verify title")
        void managerPage_verifyTitle() {
            driver.get(BASE_URL + "/manager.html");
            String title = driver.getTitle();
            assertNotNull(title);
            assertTrue(title.contains("Manager Expense Dashboard"),
                    "Title should contain Manager Expense Dashboard");
            System.out.println("Found Title: " + title);
        }

        @Test
        @Order(2)
        @DisplayName("Get current URL after navigation")
        void navigateToLoginPage_verifyURL() {
            driver.get(BASE_URL + "/manager.html");
            String currentURL = driver.getCurrentUrl();
            assertNotNull(currentURL);
            assertTrue(currentURL.contains("manager"),
                    "URL should contain 'manager'");
            System.out.println("URL Identified: " + currentURL);
        }

        @Test
        @Order(3)
        @DisplayName(("Logout of Manager Dashboard"))
        void managerDashboard_logoutOfManagerDashboard() {
            //waits for page to load in before finding logout button
            WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@id='logout-btn']")));
            logoutButton.click();
            wait.until(ExpectedConditions.urlMatches(BASE_URL + "/login.html"));
            String currentURL = driver.getCurrentUrl();
            assertEquals(BASE_URL + "/login.html", currentURL,
                    "URL Should be :" + BASE_URL + "/login.html");
            System.out.println("You have logged out and are now at URL: " + currentURL);
        }

        @Test
        @Order(4)
        @DisplayName("Review expense then go back to dashboard")
        void managerDashboard_reviewExpense_navigateBackToDashboard() {
            WebElement reviewBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath(seededExpenseRowXpath() + "//button[text()='Review']")));
            reviewBtn.click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.xpath("//div[@id='review-modal']//h3"),
                    "Review Expense"));

            WebElement heading = driver.findElement(By.xpath("//div[@id='review-modal']//h3"));
            assertEquals("Review Expense", heading.getText());

            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@id='cancel-review']")));

            String title = driver.getTitle();
            assertEquals("Manager Expense Dashboard",title,
                    "Title should be: " + title);
        }

        @Test
        @Order(5)
        @DisplayName("Review Expense, and approve it")
        void managerDashboard_reviewExpense_approveIt(){
            // find the expense review button based on the entire expense
            WebElement reviewBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath(seededExpenseRowXpath() + "//button[text()='Review']")));
            reviewBtn.click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.xpath("//div[@id='review-modal']//h3"),
                    "Review Expense"));

            WebElement heading = driver.findElement(By.xpath("//div[@id='review-modal']//h3"));
            assertEquals("Review Expense", heading.getText());

            WebElement approveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@id='approve-expense']")));
            approveBtn.click();

            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("review-message"),
                    "Expense approved successfully"
            ));

            WebElement successMessage = driver.findElement(By.id("review-message"));

            assertEquals(
                    "Expense approved successfully.",
                    successMessage.getText()
            );

            wait.until(ExpectedConditions.titleIs("Manager Expense Dashboard"));
            assertEquals("Manager Expense Dashboard",driver.getTitle(),
                    "Title should be: " + driver.getTitle());

            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath(seededExpenseRowXpath())
            ));
        }

        @Test
        @Order(6)
        @DisplayName("Review Expense, and deny it")
        void managerDashboard_reviewExpense_denyIt(){
            // find the expense review button based on the entire expense
            WebElement reviewBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath(seededExpenseRowXpath() + "//button[text()='Review']")));
            reviewBtn.click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.xpath("//div[@id='review-modal']//h3"),
                    "Review Expense"));

            WebElement heading = driver.findElement(By.xpath("//div[@id='review-modal']//h3"));
            assertEquals("Review Expense", heading.getText());

            WebElement denyBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@id='deny-expense']")));
            denyBtn.click();

            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("review-message"),
                    "Expense denied successfully"
            ));

            WebElement successMessage = driver.findElement(By.id("review-message"));

            assertEquals("Expense denied successfully.", successMessage.getText());

            wait.until(ExpectedConditions.titleIs("Manager Expense Dashboard"));
            assertEquals("Manager Expense Dashboard",driver.getTitle(),
                    "Title should be: " + driver.getTitle());

            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath(seededExpenseRowXpath())));
        }

    }
}
