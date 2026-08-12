from selenium import webdriver
from selenium.webdriver.chrome.options import Options


def before_all(context):
    options = Options()

    # Required for AWS/Jenkins Linux
    options.add_argument("--headless=new")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--window-size=1920,1080")

    context.driver = webdriver.Chrome(options=options)

    context.driver.implicitly_wait(10)

    context.base_url = "http://localhost:5001"


def before_scenario(context, scenario):
    # Start every scenario clean
    context.driver.delete_all_cookies()
    context.driver.get(context.base_url + "/login")


def after_all(context):
    if hasattr(context, "driver") and context.driver:
        context.driver.quit()