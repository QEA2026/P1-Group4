package com.revature.CucumberTests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverManager {

    private static WebDriver driver;

    public static void initializeDriver() {
        if (driver == null) {

            ChromeOptions options = new ChromeOptions();

            options.addArguments(
                    "--user-data-dir=C:/selenium/chrome-test-profile-"
                            + System.currentTimeMillis()
            );

            options.addArguments("--disable-save-password-bubble");
            options.addArguments("--disable-features=PasswordLeakDetection");
            options.addArguments("--disable-features=PasswordCheck");

            options.setExperimentalOption(
                    "prefs",
                    java.util.Map.of(
                            "credentials_enable_service", false,
                            "profile.password_manager_enabled", false
                    )
            );

            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
        }
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static void quitDriver() {
        if(driver != null) {
            driver.quit();
            driver = null;
        }
    }
}