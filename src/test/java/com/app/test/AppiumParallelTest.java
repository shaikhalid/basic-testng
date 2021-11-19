package com.app.test;

import com.app.parallel.test.DeviceDetails;
import io.appium.java_client.MobileDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import org.openqa.selenium.JavascriptExecutor;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.*;

public class AppiumParallelTest {

    private static final ThreadLocal<MobileDriver<MobileElement>> driverThread = new ThreadLocal<>();

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String URL = "http://hub-cloud.browserstack.com/wd/hub";

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
    @Parameters("config")
    public void setupDriver(String configFile) throws MalformedURLException {
        JsonPath jsonPath = JsonPath.from(new File("src/test/resources/app/config/" + configFile));
        Map<String, String> capDetails = new HashMap<>(jsonPath.getMap("capabilities"));
        DesiredCapabilities caps = new DesiredCapabilities(capDetails);
        caps.setCapability("browserstack.user", USERNAME);
        caps.setCapability("browserstack.key", ACCESS_KEY);
        List<DeviceDetails> androidDevices = get("devices.json")
                .jsonPath()
                .getList("", DeviceDetails.class)
                .stream()
                .filter(d -> d.getOs().equals("android"))
                .collect(Collectors.toList());
        int randomNumber = ThreadLocalRandom.current().nextInt(0, androidDevices.size());
        DeviceDetails deviceDetails = androidDevices.get(randomNumber);
        System.out.println(deviceDetails);
        caps.setCapability("os", deviceDetails.getOs());
        caps.setCapability("os_version", deviceDetails.getOs_version());
        caps.setCapability("device", deviceDetails.getDevice());
        caps.setCapability("name", "Wikipedia Search Function - " + deviceDetails.getDevice());
        caps.setCapability("app", "DemoApp");

        driverThread.set(new AndroidDriver<>(new URL(URL), caps));
    }

    @Test
    public void searchWikipedia() {
        Wait<MobileDriver<MobileElement>> wait = new FluentWait<>(driverThread.get())
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NotFoundException.class);
        MobileElement searchElement = wait.until(d -> d.findElementByAccessibilityId("Search Wikipedia"));
        searchElement.click();
        MobileElement insertTextElement = wait.until(d -> d.findElementById("org.wikipedia.alpha:id/search_src_text"));
        insertTextElement.sendKeys("BrowserStack");
        List<MobileElement> allProductName = wait.until(d -> d.findElementsByClassName("android.widget.TextView"));
        Assert.assertTrue(allProductName.size() > 0, "Products are not present");
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        JavascriptExecutor js = (JavascriptExecutor) driverThread.get();
        js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"Wikipedia search passed\"}}");
        driverThread.get().quit();
        driverThread.remove();
    }

}
