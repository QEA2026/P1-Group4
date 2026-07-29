Feature: Manager Generate Reports

  Background:
    Given I log in as a manager
    And I navigate to the generate reports section


  Scenario: Manager opens the generate reports section
    Then I should see the generate reports section


  Scenario: Manager generates an employee report by employee ID
    When I enter employee ID "1"
    And I click generate employee report
    Then I should see the message "Report loaded successfully"
    And I should see employee report heading "Expenses for employee 1"


  Scenario: Manager tries to generate employee report with invalid employee ID
    When I enter employee ID "999999"
    And I click generate employee report
    Then I should see the message "No user found with id:999999"


  Scenario: Manager generates a category report
    When I enter category "meals"
    And I click generate category report
    Then I should see the message "Report loaded successfully"
    And I should see category report heading "Expenses in category meals"


  Scenario: Manager generates a date report
    When I enter date "2026-07-28"
    And I click generate date report
    Then I should see the message "Report loaded successfully"
    And I should see date report heading "Expenses for 2026-07-28"