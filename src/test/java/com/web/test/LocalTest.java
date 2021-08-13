package com.web.test;

import com.browserstack.local.Local;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LocalTest {

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String URL = "http://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";
    private WebDriver driver;
    private Local local;

    @BeforeSuite
    public void setupLocal() throws Exception {
        System.out.println("Connecting local");
        local = new Local();
        Map<String, String> bsLocalArgs = new HashMap<>();
        bsLocalArgs.put("key", ACCESS_KEY);
        bsLocalArgs.put("v", "true");
        bsLocalArgs.put("logFile", "logs.txt");
        local.start(bsLocalArgs);
        System.out.println("Connected. Now testing...");
    }

    @BeforeTest(alwaysRun = true)
    public void setupDriver() throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("project", "BrowserStack");
        String buildName = System.getenv("BROWSERSTACK_BUILD_NAME");
        if (buildName != null) {
            caps.setCapability("build", buildName);
        } else {
            caps.setCapability("build", "Demo");
        }
        caps.setCapability("name", "Local Test - Chrome");

        caps.setCapability("os", "Windows");
        caps.setCapability("os_version", "10");
        caps.setCapability("browser", "Chrome");
        caps.setCapability("browser_version", "latest");

        caps.setCapability("browserstack.debug", "true");
        caps.setCapability("browserstack.local", "true");

        driver = new RemoteWebDriver(new URL(URL), caps);
    }

    @Test
    public void testLocalServer() {
        driver.get("http://localhost:8000");
        Assert.assertEquals(driver.getTitle(), "Local Server", "Incorrect title");
    }

    @AfterTest(alwaysRun = true)
    public void closeDriver() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"Local testing passed\"}}");
        driver.quit();
    }

    @AfterSuite(alwaysRun = true)
    public void closeLocal() throws Exception {
        local.stop();
        System.out.println("Binary stopped");
    }

}
