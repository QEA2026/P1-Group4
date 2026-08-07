from behave import given, when, then
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

@given('I am on the login page')
def step_on_login_page(context):
    context.driver.get(context.base_url + "/login")

@when('I enter username "{username}"')
def step_enter_username(context, username):
    context.driver.find_element(By.ID, "username").send_keys(username)

@when('I enter password "{password}"')
def step_enter_password(context, password):
    context.driver.find_element(By.ID, "password").send_keys(password)

@when('I click the login button')
def step_click_login(context):
    context.driver.find_element(By.XPATH, "//button[text()='Login']").click()

@then('I should see the title "{message}"')
def step_see_dashboard(context, message):
    heading = context.driver.find_element(By.TAG_NAME, "h1")
    assert message in heading.text, \
        f"Expected title '{message}', but got '{heading.text}'"


@then('I should see welcome message "{message}"')
def step_see_welcome(context, message):

    username = WebDriverWait(context.driver, 15).until(
        EC.text_to_be_present_in_element(
            (By.ID, "username-display"),
            "marco"
        )
    )

    assert username

@then('I should see an error message')
def step_see_error(context):
    # selenium is checking it too fast before the error message can show
    WebDriverWait(context.driver, 10).until(
        lambda d: d.find_element(By.ID, "login-message").text.strip() != ""
    )
