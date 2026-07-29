Feature: Manager Login

  Scenario: Manager successfully logs into the system
    Given I am on the manager login page
    When I enter username "testmanager" and password "MyNewStrongPassword123!"
    And I click the login button
    Then I should see a successful login message
    And I should be redirected to the manager dashboard

  Scenario: Manager enters invalid credentials
    Given I am on the manager login page
    When I enter username "Jamya" and password "4321password"
    And I click the login button
    Then I should see an invalid credentials message

  Scenario: Manager enters only a username
    Given I am on the manager login page
    When I enter username "testmanager" and password ""
    And I click the login button
    Then I should remain on the login page

  Scenario: Manager enters only a password
    Given I am on the manager login page
    When I enter username "" and password "MyNewStrongPassword123!"
    And I click the login button
    Then I should remain on the login page

  Scenario: Manager attempts login without entering credentials
    Given I am on the manager login page
    When I enter username "" and password ""
    And I click the login button
    Then I should remain on the login page