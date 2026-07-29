Feature: Manager reviews employee expenses

  Scenario: Manager approves a pending expense
    Given I am logged in as a manager
    And there is a pending expense awaiting review
    When I open the expense review modal
    And I approve the expense
    Then I should see "Expense approved successfully."
    And the expense should no longer appear in pending expenses


  Scenario: Manager denies a pending expense
    Given I am logged in as a manager
    And there is a pending expense awaiting review
    When I open the expense review modal
    And I deny the expense
    Then I should see "Expense denied successfully."
    And the expense should no longer appear in pending expenses


  Scenario: Manager cancels reviewing an expense
    Given I am logged in as a manager
    When I open the expense review modal
    And I cancel the review
    Then the expense should remain pending