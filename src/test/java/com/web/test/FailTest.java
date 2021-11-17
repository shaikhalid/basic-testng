package com.web.test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.openqa.selenium.Keys.TAB;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class FailTest {

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String URL = "http://hub-cloud.browserstack.com/wd/hub";
    private WebDriver driver;

    @BeforeTest(alwaysRun = true)
    public void setup() throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("project", "BrowserStack");
        caps.setCapability("build", "Demo");
        caps.setCapability("name", "Failed Test - Chrome");

        caps.setCapability("os", "Windows");
        caps.setCapability("os_version", "10");
        caps.setCapability("browser", "Chrome");
        caps.setCapability("browser_version", "latest");

        caps.setCapability("browserstack.user", USERNAME);
        caps.setCapability("browserstack.key", ACCESS_KEY);
        caps.setCapability("browserstack.debug", "true");

        driver = new RemoteWebDriver(new URL(URL), caps);
    }

    @Test
    public void testSearchBrowserStackFailure() {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        driver.get("https://bstackdemo.com");
        wait.until(elementToBeClickable(By.id("signin"))).click();
        wait.until(elementToBeClickable(By.cssSelector("#username input"))).sendKeys("fav_user" + TAB);
        driver.findElement(By.cssSelector("#password input")).sendKeys("testingisfun99" + TAB);
        driver.findElement(By.id("login-btn")).click();
        String username = wait.until(presenceOfElementLocated(By.className("username"))).getText();
        Assert.assertEquals(username, "incorrect_user", "Incorrect username");
    }

    @AfterTest(alwaysRun = true)
    public void teardown() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"failed\", \"reason\": \"Incorrect username\"}}");
        driver.quit();
    }

}
