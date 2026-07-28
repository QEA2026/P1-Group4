from behave import given, when, then
from selenium.webdriver.common.by import By

# Shared precondition — runs before each scenario
@given('I am logged in as "{username}"')
def step_logged_in(context, username):
    # Go to login, authenticate, and land on the dashboard.
    context.driver.get(context.base_url + "/login")
    context.driver.find_element(By.ID, "username").send_keys(username)
    context.driver.find_element(By.ID, "password").send_keys("password123")
    context.driver.find_element(By.CSS_SELECTOR, "#login-form button").click()


@when('I submit an expense with amount "{amount}" and description "{description}"')
def step_submit_expense(context, amount, description):
    context.driver.find_element(By.ID, "show-submit").click()
    context.driver.find_element(By.ID, "amount").send_keys(amount)
    context.driver.find_element(By.ID, "description").send_keys(description)
    # category is optional, so it's left out here
    context.driver.find_element(By.CSS_SELECTOR, "#expense-form button").click()


@then('I should see the expense in my expense list')
def step_see_expense_in_list(context, description="flight"):
    # the submitted expense should now appear in the rendered table
    context.driver.find_element(By.ID, "refresh-expenses").click()
    table = context.driver.find_element(By.ID, "expenses-list")
    assert "flight" in table.text, \
        f"Expected the expense in the list, got: {table.text}"

@then('I should see a validation error on the amount field')
def step_amount_validation(context):
    amount = context.driver.find_element(By.ID, "amount")   # confirm the real id
    is_valid = context.driver.execute_script(
        "return arguments[0].checkValidity();", amount
    )
    assert is_valid is False, "Expected the amount field to be invalid"

@when('I submit an expense with amount "{amount}" and an empty description')
def step_submit_empty_description(context, amount):
    # reveal the form first, same as the valid-submit step
    context.driver.find_element(By.ID, "show-submit").click()
    context.driver.find_element(By.ID, "amount").send_keys(amount)
    # leave description empty on purpose
    context.driver.find_element(By.CSS_SELECTOR, "#expense-form button").click()

# This is for when the browser blocks the empty fields
@then('I should see a validation error on the description field')
def step_description_validation(context):
    description = context.driver.find_element(By.ID, "description")
    is_valid = context.driver.execute_script(
        "return arguments[0].checkValidity();", description
    )
    assert is_valid is False, "Expected the description field to be invalid"

