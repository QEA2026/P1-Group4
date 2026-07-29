package com.revature.CucumberTests;

import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class ExpenseReviewSteps {
    private WebDriverWait wait;

    private final String BASE_URL = "http://localhost:5500";

    public ExpenseReviewSteps() {

        wait = new WebDriverWait(
                DriverManager.getDriver(),
                Duration.ofSeconds(5)
        );
    }

    private void loginAsManager() {
        DriverManager.getDriver().get(BASE_URL + "/login.html");

        DriverManager.getDriver()
                .findElement(By.id("username"))
                .sendKeys("testmanager");

        DriverManager.getDriver()
                .findElement(By.id("password"))
                .sendKeys("MyNewStrongPassword123!");

        DriverManager.getDriver()
                .findElement(By.xpath("//button[@type='submit']"))
                .click();

        wait.until(ExpectedConditions.urlContains("manager"));
    }

    @Given("I am logged in as a manager")
    public void i_am_logged_in_as_a_manager() {
        loginAsManager();
        assertEquals(BASE_URL + "/manager.html", DriverManager.getDriver().getCurrentUrl());
        String title = DriverManager.getDriver().getTitle();
        assertNotNull(title);
        assertTrue(title.contains("Manager Expense Dashboard"),
                "Title should contain Manager Expense Dashboard");
    }

    @And("there is a pending expense awaiting review")
    public void there_is_a_pending_expense() {
        WebElement reviewBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//tr[" +
                        "td[text()='1'] and " +
                        "td[text()='2026-06-03'] and " +
                        "td[text()='$82.19'] and " +
                        "td[text()='meals'] and " +
                        "td[text()='Team lunch during sprint planning'] " +
                        "]//button[text()='Review']"
                )));
        assertTrue(reviewBtn.isDisplayed());
    }

    @When("I open the expense review modal")
    public void open_review_expense_modal() {
        WebElement reviewBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//tr[" +
                        "td[text()='1'] and " +
                        "td[text()='2026-06-03'] and " +
                        "td[text()='$82.19'] and " +
                        "td[text()='meals'] and " +
                        "td[text()='Team lunch during sprint planning'] " +
                        "]//button[text()='Review']"
                )));
        reviewBtn.click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.xpath("//div[@id='review-modal']//h3"),
                "Review Expense"));
    }

    @And("I approve the expense")
    public void approve_expense() {
        WebElement approveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@id='approve-expense']")));
        approveBtn.click();
    }

    @And("I deny the expense")
    public void deny_expense() {
        WebElement denyBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@id='deny-expense']")));
        denyBtn.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("review-message"),
                "Expense denied successfully"
        ));

        WebElement successMessage = DriverManager.getDriver().findElement(By.id("review-message"));

        assertEquals("Expense denied successfully.",
                successMessage.getText());
    }

    @And("I cancel the review")
    public void cancel_review() {
        WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@id='cancel-review']")));
        cancelBtn.click();
    }

    @Then("I should see \"Expense approved successfully.\"")
    public void see_expense_approved_successfully_message() {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("review-message"),
                "Expense approved successfully"
        ));

        WebElement successMessage = DriverManager.getDriver().findElement(By.id("review-message"));

        assertEquals(
                "Expense approved successfully.",
                successMessage.getText()
        );
    }

    @Then("I should see \"Expense denied successfully.\"")
    public void see_expense_denied_successfully_message() {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("review-message"),
                "Expense denied successfully"
        ));

        WebElement successMessage = DriverManager.getDriver().findElement(By.id("review-message"));

        assertEquals(
                "Expense denied successfully.",
                successMessage.getText()
        );
    }

    @Then("the expense should remain pending")
    public void expense_cancelled_and_still_pending() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[" +
                        "td[text()='1'] and " +
                        "td[text()='2026-06-03'] and " +
                        "td[text()='$82.19'] and " +
                        "td[text()='meals'] and " +
                        "td[text()='Team lunch during sprint planning'] " +
                        "]//button[text()='Review']"
                )));
    }

    @And("the expense should no longer appear in pending expenses")
    public void show_expense_is_no_longer_there() {
        wait.until(ExpectedConditions.titleIs("Manager Expense Dashboard"));
        assertEquals("Manager Expense Dashboard",DriverManager.getDriver().getTitle(),
                "Title should be: " + DriverManager.getDriver().getTitle());

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

}
