package com.app.test;

import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayStoreTest {
    public static String userName = System.getenv("BROWSERSTACK_USERNAME");;
    public static String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
    public static final String URL = "https://" + userName + ":" + accessKey + "@hub-cloud.browserstack.com/wd/hub";
    private DesiredCapabilities caps;
    private AndroidDriver driver;

    @Test
    public void test() throws IOException, InterruptedException {
        caps = new DesiredCapabilities();

        caps.setCapability("device", "Google Pixel 3");
        caps.setCapability("os_version", "9.0");
        caps.setCapability("build", "MMT");
        caps.setCapability("name", "test");
        caps.setCapability("autoGrantPermissions", "true");

        //start the session with any random app that will just be used to initiate the test for eg: WikipediaSampleApp
        caps.setCapability("app", "DemoApp");

        //add your playstore credentials to skip the login process
        caps.setCapability("browserstack.appStoreConfiguration", new HashMap<String, String>() {{
            put("username", "playstore_username");
            put("password", "playstore_password");
        }});

        driver = new AndroidDriver(new URL(URL), caps);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        //launch the Google playstore app based on the platform
        try {
            if (caps.getCapability("device").toString().startsWith("Samsung")) {
                startApp("com.android.vending", "com.android.vending.AssetBrowserActivity");
            } else if (caps.getCapability("device").toString().startsWith("Google")) {
                startApp("com.android.vending", "com.google.android.finsky.activities.MainActivity");
            } else {
                startApp("com.android.vending", "com.android.vending.AssetBrowserActivity");
            }

            driver.rotate(ScreenOrientation.PORTRAIT);

            //checking for popups if any
            try {
                String noThanksButton = "//android.widget.Button[@text='No thanks']";
                driver.findElementByXPath(noThanksButton).click();
            } catch (Exception ex) {
                System.out.println("Not now element does not exist: " + ex.getMessage());
            }

            driver.findElementByXPath("//*[contains(@text, 'Search for apps ')]").click();
            //driver.findElementById("com.android.vending:id/search_bar_hint").click();
            driver.findElementByXPath("//android.widget.EditText").sendKeys("Air India");
            driver.pressKey(new KeyEvent(AndroidKey.ENTER));
            //Select the first search result to download the AirIndia App
            List<WebElement> items = driver.findElements(By.xpath("//android.view.View"));
            items.get(1).click();

            //checking for popups if any - can be uncommented if needed
            /*try {

                String continueButton = "//android.widget.Button[@text='Continue']";
                driver.findElementByXPath(continueButton).click();
            }
            catch (Exception ex) {
                System.out.println("Not now element does not exist: " + ex.getMessage());
            }*/

            //Installing the app
            driver.findElementByXPath("//android.widget.Button[@text='Install']").click();

            //Waiting for the app to get installed
            WebDriverWait wait = new WebDriverWait(driver, 300);
            String openButtonPath = "//android.widget.Button[@text='Open']";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(openButtonPath)));
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath(openButtonPath)));

            //Launching the app post installation
            driver.findElementByXPath(openButtonPath).click();

            /**Add in your test scripts here for the Air India App**/

            Thread.sleep(5000);

            JavascriptExecutor jse = (JavascriptExecutor) driver;
            jse.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"Successfully launched app post downloading from Play store\"}}");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(driver.getPageSource());
            JavascriptExecutor jse = (JavascriptExecutor) driver;
            jse.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"failed\", \"reason\": \"Could not launch app from Play store\"}}");
        }
        driver.quit();

    }

    private void startApp(String appPackage, String appActivity) {
        try {
            driver.startActivity(new Activity(appPackage, appActivity));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
