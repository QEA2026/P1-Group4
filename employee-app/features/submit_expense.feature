Feature: Submit Expense
  As a registered employee
  I want to submit expense reports
  So that they can be reviewed by a manager

  Scenario: Successfully submit a valid expense
      Given I am logged in as "marco"
      When I submit an expense with amount "150" and description "flight"
      Then I should see the expense in my expense list

  Scenario: Reject an expense with a negative amount
      Given I am logged in as "marco"
      When I submit an expense with amount "-50" and description "flight"
      Then I should see a validation error on the amount field

  Scenario: Reject an expense with an empty description
      Given I am logged in as "marco"
      When I submit an expense with amount "150" and an empty description
      Then I should see a validation error on the description field

