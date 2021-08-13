package com.web.test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class FailTest {

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String URL = "http://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";
    private WebDriver driver;

    @BeforeTest(alwaysRun = true)
    public void setup() throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("project", "BrowserStack");
        String buildName = System.getenv("BROWSERSTACK_BUILD_NAME");
        if (buildName != null) {
            caps.setCapability("build", buildName);
        } else {
            caps.setCapability("build", "Demo");
        }
        caps.setCapability("name", "Failed Test - Chrome");

        caps.setCapability("os", "Windows");
        caps.setCapability("os_version", "10");
        caps.setCapability("browser", "Chrome");
        caps.setCapability("browser_version", "latest");

        driver = new RemoteWebDriver(new URL(URL), caps);
    }

    @Test
    public void testSearchBrowserStackFailure() {
        driver.get("http://www.google.com");
        driver.findElement(By.name("q")).sendKeys("BrowserStack");
        driver.findElement(By.name("q")).submit();
        driver.findElement(By.cssSelector("a[href='https://www.browserstack.com/']")).click();
        String title = "Incorrect | BrowserStack";
        Assert.assertEquals(driver.getTitle(), title, "Incorrect Title");
    }

    @AfterTest(alwaysRun = true)
    public void teardown() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"failed\", \"reason\": \"Incorrect Title\"}}");
        driver.quit();
    }

}
