package com.web.parallel.test;

import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.*;

public class BaseTest {

    private static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String URL = "http://hub-cloud.browserstack.com/wd/hub";

    public static WebDriver getWebDriver() {
        return driverThread.get();
    }

    @BeforeSuite(alwaysRun = true)
    public void setupApp() {
        PreemptiveBasicAuthScheme authenticationScheme = new PreemptiveBasicAuthScheme();
        authenticationScheme.setUserName(USERNAME);
        authenticationScheme.setPassword(ACCESS_KEY);
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://api-cloud.browserstack.com")
                .setBasePath("automate")
                .setAuth(authenticationScheme)
                .build();
        responseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters("environment")
    public void setup(String environment, Method m) throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("project", "BrowserStack");
        caps.setCapability("build", "Demo");
        caps.setCapability("browserstack.user", USERNAME);
        caps.setCapability("browserstack.key", ACCESS_KEY);
        List<BrowserDetails> browsers = get("browsers.json")
                .jsonPath()
                .getList("", BrowserDetails.class);
        int randomNumber;
        BrowserDetails browserDetails;
        switch (environment) {
            case "desktop":
                List<BrowserDetails> desktopBrowsers = browsers.parallelStream()
                        .filter(details -> details.getDevice() == null)
                        .collect(Collectors.toList());
                randomNumber = ThreadLocalRandom.current().nextInt(0, desktopBrowsers.size());
                browserDetails = desktopBrowsers.get(randomNumber);
                caps.setCapability("name", m.getName() + " - " + browserDetails.getBrowser() + " " + browserDetails.getBrowser_version());
                caps.setCapability("os", browserDetails.getOs());
                caps.setCapability("os_version", browserDetails.getOs_version());
                caps.setCapability("browser", browserDetails.getBrowser());
                caps.setCapability("browser_version", browserDetails.getBrowser_version());
                break;
            case "mobile":
                List<BrowserDetails> mobileBrowsers = browsers.parallelStream()
                        .filter(BrowserDetails::isReal_mobile)
                        .collect(Collectors.toList());
                randomNumber = ThreadLocalRandom.current().nextInt(0, mobileBrowsers.size());
                browserDetails = mobileBrowsers.get(randomNumber);
                caps.setCapability("name", m.getName() + " - " + browserDetails.getDevice());
                caps.setCapability("os_version", browserDetails.getOs_version());
                caps.setCapability("device", browserDetails.getDevice());
                caps.setCapability("real_mobile", browserDetails.isReal_mobile());
                break;
            default:
                throw new IllegalArgumentException("Incorrect environment " + environment + " specified");
        }
        driverThread.set(new RemoteWebDriver(new URL(URL), caps));
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        JavascriptExecutor js = (JavascriptExecutor) driverThread.get();
        js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"BStackDemo login passed\"}}");
        driverThread.get().quit();
        driverThread.remove();
    }

}
