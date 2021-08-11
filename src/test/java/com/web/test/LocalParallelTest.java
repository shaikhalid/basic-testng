package com.web.test;

import com.browserstack.local.Local;
import io.restassured.path.json.JsonPath;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LocalParallelTest {

    private static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();
    private Local local;

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String URL = "http://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    @BeforeSuite(alwaysRun = true)
    public void before() throws Exception {
        local = new Local();
        Map<String, String> bsLocalArgs = new HashMap<>();
        bsLocalArgs.put("key", ACCESS_KEY);
        bsLocalArgs.put("v", "true");
        bsLocalArgs.put("logFile", "logs.txt");
        local.start(bsLocalArgs);
    }

    @BeforeTest(alwaysRun = true)
    @Parameters({"config", "environment"})
    public void setup(String configFile, String environment) throws MalformedURLException {
        JsonPath jsonPath = JsonPath.from(new File("src/test/resources/web/config/" + configFile));
        Map<String, String> capDetails = new HashMap<>();
        capDetails.putAll(jsonPath.getMap("capabilities"));
        capDetails.putAll(jsonPath.getMap("environments." + environment));
        DesiredCapabilities caps = new DesiredCapabilities(capDetails);
        caps.setCapability("browserstack.local", "true");

        driverThread.set(new RemoteWebDriver(new URL(URL), caps));
    }

    @Test
    public void testSearchBrowserStack() {
        WebDriver driver = driverThread.get();
        driver.get("http://localhost:8000");
        Assert.assertEquals(driver.getTitle(), "Local Server", "Incorrect title");
    }

    @AfterTest(alwaysRun = true)
    public void teardown() {
        JavascriptExecutor js = (JavascriptExecutor) driverThread.get();
        js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"BrowserStack search passed\"}}");
        driverThread.get().quit();
        driverThread.remove();
    }

    @AfterSuite
    public void after() throws Exception {
        local.stop();
    }

}
