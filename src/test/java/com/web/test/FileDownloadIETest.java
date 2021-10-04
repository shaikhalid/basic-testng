package com.web.test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

public class FileDownloadIETest {

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String URL = "http://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";
    private WebDriver driver;

    @BeforeTest(alwaysRun = true)
    public void setup() throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("project", "BrowserStack");
        caps.setCapability("build", "Demo");
        caps.setCapability("name", "Single Test - Chrome");

        caps.setCapability("os", "Windows");
        caps.setCapability("os_version", "10");
        caps.setCapability("browser", "IE");
        caps.setCapability("browser_version", "11.0");

        caps.setCapability("browserstack.debug", "true");

        driver = new RemoteWebDriver(new URL(URL), caps);
    }

    @Test
    public void testFileDownloadIe() throws IOException, InterruptedException {
        JavascriptExecutor jse = (JavascriptExecutor) driver;

        driver.get("https://rubygems.org/gems/selenium-webdriver");
        Thread.sleep(2000);
        WebElement Element = driver.findElement(By.id("download"));

        // This will scroll the page till the element is found
        jse.executeScript("arguments[0].scrollIntoView();", Element);
        jse.executeScript("window.scrollBy(0,-100)");
        Thread.sleep(1000);

        // Click on the element to download the file
        Element.click();
        Thread.sleep(2000);

        jse.executeScript("browserstack_executor: {\"action\": \"saveFile\"}");
        Thread.sleep(10000);

        // Check if file exists
        System.out.println(jse.executeScript("browserstack_executor: {\"action\": \"fileExists\", \"arguments\": {\"fileName\": \"selenium-webdriver-3.142.7.gem\"}}"));

        // Get file properties
        System.out.println(jse.executeScript("browserstack_executor: {\"action\": \"getFileProperties\", \"arguments\": {\"fileName\": \"selenium-webdriver-3.142.7.gem\"}}"));

        // Get file content. The content is Base64 encoded
        String base64EncodedFile = (String) jse.executeScript("browserstack_executor: {\"action\": \"getFileContent\", \"arguments\": {\"fileName\": \"selenium-webdriver-3.142.7.gem\"}}");

        // Decode the content to Base64
        byte[] data = Base64.getDecoder().decode(base64EncodedFile);
        OutputStream stream = new FileOutputStream("selenium-webdriver-3.142.7.gem");
        stream.write(data);
        stream.close();
    }

    @AfterTest(alwaysRun = true)
    public void teardown() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"BrowserStack search passed\"}}");
        driver.quit();
    }

}
