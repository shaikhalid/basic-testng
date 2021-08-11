package com.web.test;

import io.restassured.path.json.JsonPath;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ParallelTest {

    private static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String URL = "http://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    @BeforeTest(alwaysRun = true)
    @Parameters({"config", "environment"})
    public void setup(String configFile, String environment) throws MalformedURLException {
        JsonPath jsonPath = JsonPath.from(new File("src/test/resources/web/config/" + configFile));
        Map<String, String> capDetails = new HashMap<>();
        capDetails.putAll(jsonPath.getMap("capabilities"));
        capDetails.putAll(jsonPath.getMap("environments." + environment));
        DesiredCapabilities caps = new DesiredCapabilities(capDetails);
        driverThread.set(new RemoteWebDriver(new URL(URL), caps));
    }

    @Test
    public void testSearchBrowserStack() {
        WebDriver driver = driverThread.get();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        driver.get("http://www.google.com");
        driver.findElement(By.name("q")).sendKeys("BrowserStack");
        driver.findElement(By.name("q")).submit();
        Assert.assertTrue(wait.until(ExpectedConditions.titleIs("BrowserStack - Google Search")), "Incorrect Title");
    }

    @AfterTest(alwaysRun = true)
    public void teardown() {
        JavascriptExecutor js = (JavascriptExecutor) driverThread.get();
        js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"BrowserStack search passed\"}}");
        driverThread.get().quit();
        driverThread.remove();
    }

}
