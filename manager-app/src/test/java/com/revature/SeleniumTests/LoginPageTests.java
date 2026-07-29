package com.revature.SeleniumTests;

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
public class LoginPageTests {
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
        @DisplayName("Enter only username")
        void sendKeys_enterOnlyUsername() {
            driver.get(BASE_URL + "/login.html");

            WebElement usernameInput = driver.findElement(By.id("username"));
            WebElement passwordInput = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(
                    By.xpath("//button[@type='submit']"));


            usernameInput.clear();
            usernameInput.sendKeys("testmanager");
            passwordInput.clear();

            assertEquals("testmanager", usernameInput.getAttribute("value"));

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
        @Order(4)
        @DisplayName("Enter only password")
        void sendKeys_enterOnlyPassword() {
            driver.get(BASE_URL + "/login.html");

            WebElement usernameInput = driver.findElement(By.id("username"));
            WebElement passwordInput = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(
                    By.xpath("//button[@type='submit']"));


            usernameInput.clear();
            passwordInput.clear();
            passwordInput.sendKeys("MyNewStrongPassword123!");


            assertEquals("MyNewStrongPassword123!", passwordInput.getAttribute("value"));

            loginButton.click();

            assertTrue(driver.getCurrentUrl().contains("login.html"),
                    "Form should not submit and URL should remain on login page");

            // Optional: check password input is still empty and focused
            assertEquals("", usernameInput.getAttribute("value"));
            assertTrue(usernameInput.equals(driver.switchTo().activeElement()),
                    "Username input should have focus after failed submit");

        }
        @Test
        @Order(5)
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

}
