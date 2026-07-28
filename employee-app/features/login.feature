Feature: Employee Login
  As a registered employee
  I want to log in with my credentials
  So that I can securely access my expense reports

  Scenario: Successful login with valid credentials
    Given I am on the login page
    When I enter username "marco"
    And I enter password "password123"
    And I click the login button
    Then I should see the title "Employee Expense Manager"
    And I should see welcome message "Welcome, marco"

  Scenario: Reject login with invalid credentials
    Given I am on the login page
    When I enter username "vanessa"
    And I enter password "password123"
    And I click the login button
    Then I should see an error message