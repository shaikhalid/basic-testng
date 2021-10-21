package com.app.test;

import io.appium.java_client.MobileDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import org.openqa.selenium.JavascriptExecutor;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import static io.restassured.RestAssured.*;

public class CameraInjectionPblTest {

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String URL = "http://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";
    private MobileDriver<MobileElement> driver;
    private String mediaUrl;

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
        List<String> customAppIds = get("recent_apps").jsonPath().getList("custom_id");
        if (customAppIds == null || !customAppIds.contains("CameraInjectionPblApp")) {
            given()
                    .header("Content-Type", "multipart/form-data")
                    .multiPart("file", new File("src/test/resources/app/appdata/az.apk"), "text/apk")
                    .param("custom_id", "CameraInjectionPblApp")
                    .post("upload");
            System.out.println("Uploaded app...");
        } else {
            System.out.println("Using previously uploaded app...");
        }
        mediaUrl = given()
                .header("Content-Type", "multipart/form-data")
                .multiPart("file", new File("src/test/resources/app/appdata/BarCode.png"), "text/apk")
                .param("custom_id", "SampleMedia")
                .post("upload-media")
                .jsonPath()
                .get("media_url");
        System.out.println("Uploaded image...");
        System.out.println("Media URL: " + mediaUrl);
    }

    @BeforeTest(alwaysRun = true)
    public void setup() throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("project", "BrowserStack");
        caps.setCapability("build", "Beta Features");
        caps.setCapability("name", "Camera Injection - Samsung Galaxy S20 Ultra");

        caps.setCapability("device", "Samsung Galaxy S20 Ultra");
        caps.setCapability("os_version", "10");
        caps.setCapability("app", "CameraInjectionPblApp");

        caps.setCapability("autoGrantPermissions", "true");

        caps.setCapability("browserstack.debug", "true");
        caps.setCapability("browserstack.enableCameraImageInjection", "true");
        caps.setCapability("browserstack.uploadMedia", new String[]{mediaUrl});
        driver = new AndroidDriver<>(new URL(URL), caps);
    }

    @Test
    public void testCameraInjection() {
        Wait<MobileDriver<MobileElement>> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NotFoundException.class);
        wait.until(d -> d.findElementByXPath("//android.widget.TextView[@text='SKIP']")).click();
        wait.until(d -> d.findElementByXPath("//android.widget.Button[@content-desc='undefined fab, button']")).click();
        wait.until(d -> d.findElementByXPath("//android.widget.TextView[@text='Email*']")).click();
        driver.findElementByXPath("//android.widget.EditText[@content-desc='undefined email input textInput, text']")
                .sendKeys("1raj@yopmail.com");
        driver.findElementByXPath("//android.widget.TextView[@text='Password*']").click();
        driver.findElementByXPath("//android.widget.EditText[@content-desc='undefined password input textInput, text']")
                .sendKeys("Test@1234");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("browserstack_executor: {\"action\": \"cameraImageInjection\", \"arguments\": {\"imageUrl\": \"" + mediaUrl + "\"}}");
        driver.findElementByXPath("//android.widget.TextView[@text='SIGN IN']").click();
        MobileElement scanResult = wait.until(d -> d.findElementByXPath("//android.widget.TextView[@text='Contact Lottery']"));
        Assert.assertEquals(scanResult.getText(), "Contact Lottery", "Incorrect message");
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"Camera Injection passed\"}}");
        driver.quit();
    }
}