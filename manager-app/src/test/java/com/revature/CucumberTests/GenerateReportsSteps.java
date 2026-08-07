package com.revature.CucumberTests;

import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenerateReportsSteps {

    private final String BASE_URL = "http://localhost:5500";


    private WebDriverWait getWait() {
        return new WebDriverWait(
                DriverManager.getDriver(),
                Duration.ofSeconds(5)
        );
    }


    @Given("I log in as a manager")
    public void i_am_logged_in_as_a_manager() {

        DriverManager.getDriver()
                .get(BASE_URL + "/login.html");


        DriverManager.getDriver()
                .findElement(By.id("username"))
                .sendKeys("testmanager");


        DriverManager.getDriver()
                .findElement(By.id("password"))
                .sendKeys("newPassword123!");


        DriverManager.getDriver()
                .findElement(By.xpath("//button[@type='submit']"))
                .click();


        getWait().until(
                ExpectedConditions.urlContains("manager")
        );
    }


    @Given("I navigate to the generate reports section")
    public void navigate_to_generate_reports() {

        WebElement reportsBtn =
                getWait().until(
                        ExpectedConditions.elementToBeClickable(
                                By.id("show-reports")
                        )
                );

        reportsBtn.click();


        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("reports-section")
                )
        );
    }


    @Then("I should see the generate reports section")
    public void verify_reports_section() {

        WebElement section =
                DriverManager.getDriver()
                        .findElement(By.id("reports-section"));


        assertEquals(
                "Generate Reports",
                section.findElement(By.tagName("h3")).getText()
        );
    }


    @When("I enter employee ID {string}")
    public void enter_employee_id(String id) {

        DriverManager.getDriver()
                .findElement(By.id("employee-report-id"))
                .sendKeys(id);
    }


    @When("I click generate employee report")
    public void click_employee_report() {

        DriverManager.getDriver()
                .findElement(By.id("generate-employee-report"))
                .click();
    }


    @When("I enter category {string}")
    public void enter_category(String category) {

        DriverManager.getDriver()
                .findElement(By.id("category-report"))
                .sendKeys(category);
    }


    @When("I click generate category report")
    public void click_category_report() {

        DriverManager.getDriver()
                .findElement(By.id("generate-category-report"))
                .click();
    }


    @When("I enter date {string}")
    public void enter_date(String date) {

        WebElement dateInput = DriverManager.getDriver()
                .findElement(By.id("report-date"));

        ((org.openqa.selenium.JavascriptExecutor) DriverManager.getDriver())
                .executeScript(
                        """
                        arguments[0].value = arguments[1];
                        arguments[0].dispatchEvent(new Event('change'));
                        """,
                        dateInput,
                        date
                );
    }


    @When("I click generate date report")
    public void click_date_report() {

        DriverManager.getDriver()
                .findElement(By.id("generate-date-report"))
                .click();
    }


    @Then("I should see the message {string}")
    public void verify_message(String expected) {

        WebElement message =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("report-message")
                        )
                );


        assertEquals(
                expected,
                message.getText()
        );
    }


    @Then("I should see employee report heading {string}")
    public void verify_employee_heading(String expected) {

        WebElement heading =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[@id='report-results']//h4")
                        )
                );


        assertEquals(
                expected,
                heading.getText()
        );
    }


    @Then("I should see category report heading {string}")
    public void verify_category_heading(String expected) {

        WebElement heading =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[@id='report-results']//h4")
                        )
                );


        assertEquals(
                expected,
                heading.getText()
        );
    }


    @Then("I should see date report heading {string}")
    public void verify_date_heading(String expected) {

        WebElement heading =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[@id='report-results']//h4")
                        )
                );


        assertEquals(
                expected,
                heading.getText()
        );
    }
}