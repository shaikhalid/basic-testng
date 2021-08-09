package com.app.test;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;

public class AppiumParallelTest {

    private static final ThreadLocal<AndroidDriver<AndroidElement>> driverThread = new ThreadLocal<>();

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String URL = "http://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    @BeforeSuite(alwaysRun = true)
    public void setupApp() {
        PreemptiveBasicAuthScheme authenticationScheme = new PreemptiveBasicAuthScheme();
        authenticationScheme.setUserName(USERNAME);
        authenticationScheme.setPassword(ACCESS_KEY);
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://api-cloud.browserstack.com")
                .setBasePath("app-automate")
                .setAuth(authenticationScheme)
                .build();
        responseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
        List<String> customIds = get("recent_apps").jsonPath().getList("custom_id");
        if (customIds == null || !customIds.contains("DemoApp")) {
            System.out.println("Uploading app...");
            given()
                    .header("Content-Type", "multipart/form-data")
                    .multiPart("file", new File("src/test/resources/app/appdata/WikipediaSample.apk"), "text/apk")
                    .param("custom_id", "DemoApp")
                    .post("upload");
        } else {
            System.out.println("Using previously uploaded app...");
        }
    }

    @BeforeTest(alwaysRun = true)
    @Parameters({"config", "environment"})
    public void setup(String configFile, String environment) throws MalformedURLException {
        JsonPath jsonPath = JsonPath.from(new File("src/test/resources/app/config/" + configFile));
        Map<String, String> capDetails = new HashMap<>();
        capDetails.putAll(jsonPath.getMap("capabilities"));
        capDetails.putAll(jsonPath.getMap("environments." + environment));
        DesiredCapabilities caps = new DesiredCapabilities(capDetails);
        driverThread.set(new AndroidDriver<>(new URL(URL), caps));
    }

    @Test
    public void searchWikipedia() {
        Wait<AndroidDriver<AndroidElement>> wait = new FluentWait<>(driverThread.get())
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NotFoundException.class);
        AndroidElement searchElement = wait.until(d -> d.findElementByAccessibilityId("Search Wikipedia"));
        searchElement.click();
        AndroidElement insertTextElement = wait.until(d -> d.findElementById("org.wikipedia.alpha:id/search_src_text"));
        insertTextElement.sendKeys("BrowserStack");
        List<AndroidElement> allProductName = wait.until(d -> d.findElementsByClassName("android.widget.TextView"));
        Assert.assertTrue(allProductName.size() > 0, "Products are not present");
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        driverThread.get().executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"Wikipedia search passed\"}}");
        driverThread.get().quit();
        driverThread.remove();
    }

}
