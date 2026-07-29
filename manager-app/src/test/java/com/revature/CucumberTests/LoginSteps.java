package com.revature.CucumberTests;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class LoginSteps {

    private final String BASE_URL = "http://localhost:5500";

    private WebDriverWait wait;

    public LoginSteps() {
        wait = new WebDriverWait(
                DriverManager.getDriver(),
                Duration.ofSeconds(5)
        );
    }

    @Given("I am on the manager login page")
    public void i_am_on_the_manager_login_page() {
        DriverManager.getDriver()
                .get(BASE_URL + "/login.html");
    }


    @When("I enter username {string} and password {string}")
    public void enterCredentials(String username, String password) {

        WebElement usernameInput = DriverManager.getDriver()
                .findElement(By.id("username"));

        WebElement passwordInput = DriverManager.getDriver()
                .findElement(By.id("password"));

        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
    }

    @And("I click the login button")
    public void i_click_the_login_button() {
        DriverManager.getDriver()
                .findElement(By.xpath("//button[@type='submit']"))
                .click();
    }


    @Then("I should see a successful login message")
    public void i_should_see_a_successful_login_message() {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("login-message"),
                "Login successful! Redirecting"
        ));

        assertTrue(
                DriverManager.getDriver()
                        .findElement(By.id("login-message"))
                        .getText()
                        .contains("Login successful! Redirecting")
        );
    }

    @Then("I should see an invalid credentials message")
    public void i_should_see_an_invalid_credentails_message() {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("login-message"),
                "Invalid manager credentials"
        ));

        assertTrue(
                DriverManager.getDriver()
                        .findElement(By.id("login-message"))
                        .getText()
                        .contains("Invalid manager credentials")
        );
    }

    @Then("I should be redirected to the manager dashboard")
    public void i_should_be_redirected_to_the_manager_dashboard() {

        wait.until(ExpectedConditions.urlContains("manager"));

        assertTrue(
                DriverManager.getDriver()
                        .getCurrentUrl()
                        .contains("manager")
        );
    }


    @Then("I should remain on the login page")
    public void i_remain_on_the_login_page() {

        assertTrue(
                DriverManager.getDriver()
                        .getCurrentUrl()
                        .contains("login.html")
        );
    }
}