package com.app.test;

import com.browserstack.local.Local;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;

public class AppiumLocalTest {

    private static AndroidDriver<AndroidElement> driver;
    private static Local local;

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
        if (customIds == null || !customIds.contains("LocalApp")) {
            System.out.println("Uploading app...");
            given()
                    .header("Content-Type", "multipart/form-data")
                    .multiPart("file", new File("src/test/resources/app/appdata/LocalSample.apk"), "text/apk")
                    .param("custom_id", "LocalApp")
                    .post("upload");
        } else {
            System.out.println("Using previously uploaded app...");
        }
    }

    @BeforeTest(alwaysRun = true)
    public void setup() throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("project", "BrowserStack");
        caps.setCapability("build", "Demo");
        caps.setCapability("name", "Local Testing - Google Pixel 3");

        caps.setCapability("device", "Google Pixel 3");
        caps.setCapability("os_version", "9.0");
        caps.setCapability("real_mobile", "true");
        caps.setCapability("app", "LocalApp");

        caps.setCapability("browserstack.local", "true");

        local = new Local();
        Map<String, String> options = new HashMap<>();
        options.put("key", ACCESS_KEY);
        local.start(options);

        driver = new AndroidDriver<>(new URL(URL), caps);
    }

    @Test
    public void test() {
        Wait<AndroidDriver<AndroidElement>> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NotFoundException.class);
        AndroidElement searchElement = wait.until(d -> d.findElementById("com.example.android.basicnetworking:id/test_action"));
        searchElement.click();
        List<AndroidElement> allTextViewElements = wait.until(d -> d.findElementsByClassName("android.widget.TextView"));
        boolean textPresent = allTextViewElements.stream().anyMatch(e -> e.getText().contains("The active connection is wifi."));
        Assert.assertTrue(textPresent, "Text is not present");
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"Local testing passed\"}}");
        driver.quit();
        local.stop();
    }
}