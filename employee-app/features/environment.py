from selenium import webdriver

def before_all(context):
    context.driver = webdriver.Chrome()
    context.driver.implicitly_wait(10) # its closing too soon so i added a wait
    context.base_url = "http://localhost:5001"

def after_all(context):
    context.driver.quit()