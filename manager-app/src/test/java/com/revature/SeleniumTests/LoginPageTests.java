package com.revature.SeleniumTests;

import com.revature.DAOs.ExpenseDAO;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginPageTests {
    private WebDriver driver;
    private final String BASE_URL = "http://localhost:5500";
    private WebDriverWait wait;

    private void loginAsManager() {
        driver.get(BASE_URL + "/login.html");

        driver.findElement(By.id("username"))
                .sendKeys("vanessa");

        driver.findElement(By.id("password"))
                .sendKeys("password123");

        driver.findElement(By.xpath("//button[@type='submit']"))
                .click();

        wait.until(ExpectedConditions.urlContains("manager"));
    }

    @Nested
    class LoginTests {
        @BeforeEach
        void setUp() {
            driver = new ChromeDriver();
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        }

        @AfterEach
        void tearDown() {
            if (driver != null) {
                driver.quit();
            }
        }

        @Test
        @Order(1)
        @DisplayName("Navigate to website and verify title")
        void navigateToLoginPage_verifyTitle() {
            driver.get(BASE_URL + "/login.html");
            String title = driver.getTitle();
            assertNotNull(title);
            assertTrue(title.contains("Manager Login")
                    , "Title should contain 'Manager Login'");
            System.out.println("Title Identified: " + title);
        }

        @Test
        @Order(2)
        @DisplayName("Get current URL after navigation")
        void navigateToLoginPage_verifyURL() {
            driver.get(BASE_URL + "/login.html");
            String currentURL = driver.getCurrentUrl();
            assertTrue(currentURL.contains("login"),
                    "URL should contain 'login'");
            System.out.println("URL Identified: " + currentURL);
        }

        @Test
        @Order(3)
        @DisplayName("Enter Username and Password Successfully")
        void sendKeys_enterLoginCredentials() {
            driver.get(BASE_URL + "/login.html");

            WebElement usernameInput = driver.findElement(By.id("username"));
            WebElement passwordInput = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(
                    By.xpath("//button[@type='submit']"));

            assertTrue(usernameInput.isDisplayed());
            assertTrue(usernameInput.isEnabled());
            assertTrue(passwordInput.isDisplayed());
            assertTrue(passwordInput.isEnabled());
            assertTrue(loginButton.isDisplayed());
            assertTrue(loginButton.isEnabled());

            usernameInput.clear();
            passwordInput.clear();

            // Enter username and press tab to go to password textbox
            usernameInput.sendKeys("vanessa");

            // Enter password and press enter to submit credentials
            passwordInput.sendKeys("password123");

            assertEquals("vanessa", usernameInput.getAttribute("value"));
            assertEquals("password123", passwordInput.getAttribute("value"));

            loginButton.click();

            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("login-message"),
                    "Login successful! Redirecting"
            ));
            WebElement flash = driver.findElement(By.id("login-message"));
            String flashText = flash.getText();
            assertTrue(flashText.contains("Login successful! Redirecting..."));
            System.out.println("Flash message: " + flashText);

        }

        @Test
        @Order(4)
        @DisplayName("Enter Incorrect Username and Password")
        void sendKeys_enterIncorrectLoginCredentials() {
            driver.get(BASE_URL + "/login.html");

            WebElement usernameInput = driver.findElement(By.id("username"));
            WebElement passwordInput = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(
                    By.xpath("//button[@type='submit']"));

            assertTrue(usernameInput.isDisplayed());
            assertTrue(usernameInput.isEnabled());
            assertTrue(passwordInput.isDisplayed());
            assertTrue(passwordInput.isEnabled());
            assertTrue(loginButton.isDisplayed());
            assertTrue(loginButton.isEnabled());

            usernameInput.clear();
            passwordInput.clear();

            // Enter username and press tab to go to password textbox
            usernameInput.sendKeys("Jamya");

            // Enter password and press enter to submit credentials
            passwordInput.sendKeys("4321password");

            loginButton.click();

            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("login-message"),
                    "Invalid manager credentials"
            ));

            WebElement flash = driver.findElement(By.id("login-message"));
            String flashText = flash.getText();
            assertTrue(flashText.contains("Invalid manager credentials"));
            System.out.println("Flash message: " + flashText);

        }

        @Test
        @Order(4)
        @DisplayName("Enter only username")
        void sendKeys_enterOnlyUsername() {
            driver.get(BASE_URL + "/login.html");

            WebElement usernameInput = driver.findElement(By.id("username"));
            WebElement passwordInput = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(
                    By.xpath("//button[@type='submit']"));


            usernameInput.clear();
            usernameInput.sendKeys("vanessa");
            passwordInput.clear();

            assertEquals("vanessa", usernameInput.getAttribute("value"));

            loginButton.click();

            try {
                Thread.sleep(1000); // simple wait; replace with explicit wait if needed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(driver.getCurrentUrl().contains("login.html"),
                    "Form should not submit and URL should remain on login page");

            // Optional: check password input is still empty and focused
            assertEquals("", passwordInput.getAttribute("value"));
            assertTrue(passwordInput.equals(driver.switchTo().activeElement()),
                    "Password input should have focus after failed submit");

        }

        @Test
        @Order(5)
        @DisplayName("Enter only password")
        void sendKeys_enterOnlyPassword() {
            driver.get(BASE_URL + "/login.html");

            WebElement usernameInput = driver.findElement(By.id("username"));
            WebElement passwordInput = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(
                    By.xpath("//button[@type='submit']"));


            usernameInput.clear();
            passwordInput.clear();
            passwordInput.sendKeys("password123");


            assertEquals("password123", passwordInput.getAttribute("value"));

            loginButton.click();

            try {
                Thread.sleep(1000); // simple wait; replace with explicit wait if needed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(driver.getCurrentUrl().contains("login.html"),
                    "Form should not submit and URL should remain on login page");

            // Optional: check password input is still empty and focused
            assertEquals("", usernameInput.getAttribute("value"));
            assertTrue(usernameInput.equals(driver.switchTo().activeElement()),
                    "Username input should have focus after failed submit");

        }
        @Test
        @Order(6)
        @DisplayName("Enter Nothing")
        void sendKeys_enterNothing() {
            driver.get(BASE_URL + "/login.html");

            WebElement usernameInput = driver.findElement(By.id("username"));
            WebElement passwordInput = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(
                    By.xpath("//button[@type='submit']"));

            usernameInput.clear();
            passwordInput.clear();

            loginButton.click();

            try {
                Thread.sleep(1000); // simple wait; replace with explicit wait if needed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(driver.getCurrentUrl().contains("login.html"),
                    "Form should not submit and URL should remain on login page");

            // Optional: check password input is still empty and focused
            assertEquals("", usernameInput.getAttribute("value"));
            assertEquals("", passwordInput.getAttribute("value"));
            assertTrue(usernameInput.equals(driver.switchTo().activeElement()),
                    "Username input should have focus after failed submit");

        }
    }
    @Nested
    class ManagerPageTests {
        ExpenseDAO dao = new ExpenseDAO();

        @BeforeEach
        void setUp() {
            //Does successful login to access manager app
            driver = new ChromeDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            loginAsManager();
        }

        @AfterEach
        void teardown() {
            if (driver != null) {
                driver.quit();
            }
            dao.resetExpenseStatuses();
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
            WebElement reviewBtn = driver.findElement(By.xpath(
                    "//tr[" +
                            "td[text()='1'] and " +
                            "td[text()='2026-06-03'] and " +
                            "td[text()='$82.19'] and " +
                            "td[text()='meals'] and " +
                            "td[text()='Team lunch during sprint planning'] " +
                            "]//button[text()='Review']"
            ));
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
            WebElement reviewBtn = driver.findElement(By.xpath(
        "//tr[" +
                "td[text()='1'] and " +
                "td[text()='2026-06-03'] and " +
                "td[text()='$82.19'] and " +
                "td[text()='meals'] and " +
                "td[text()='Team lunch during sprint planning'] " +
                "]//button[text()='Review']"
            ));
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
                    By.xpath(
                            "//tr[" +
                                    "td[text()='1'] and " +
                                    "td[text()='2026-06-03'] and " +
                                    "td[text()='$82.19'] and " +
                                    "td[text()='meals'] and " +
                                    "td[text()='Team lunch during sprint planning']" +
                                    "]"
                    )
            ));
        }

        @Test
        @Order(6)
        @DisplayName("Review Expense, and deny it")
        void managerDashboard_reviewExpense_denyIt(){
            // find the expense review button based on the entire expense
            WebElement reviewBtn = driver.findElement(By.xpath(
                    "//tr[" +
                            "td[text()='1'] and " +
                            "td[text()='2026-06-01'] and " +
                            "td[text()='$135.42'] and " +
                            "td[text()='travel'] and " +
                            "td[text()='Airport rideshare to client site'] " +
                            "]//button[text()='Review']"
            ));
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
                    By.xpath(
                    "//tr[" +
                            "td[text()='1'] and " +
                            "td[text()='2026-06-01'] and " +
                            "td[text()='$135.42'] and " +
                            "td[text()='travel'] and " +
                            "td[text()='Airport rideshare to client site'] " +
                            "]//button[text()='Review']"
            )));
        }

    }
}
