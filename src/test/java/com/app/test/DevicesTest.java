package com.app.test;

import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.*;

public class DevicesTest {

    private static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");

    @BeforeClass
    public void setup() {
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
    }

    @Test(enabled = false)
    public void getDeviceList() {
        List<DeviceDetails> devices = get("devices.json").jsonPath().getList("", DeviceDetails.class);
        List<DeviceDetails> androidDevices = devices.stream().filter(d -> d.getOs().equals("android")).collect(Collectors.toList());
        List<DeviceDetails> iosDevices = devices.stream().filter(d -> d.getOs().equals("ios")).collect(Collectors.toList());
//        List<String> devices = get("devices.json").jsonPath().getList("device");
//        devices.forEach(System.out::println);
//        devices.forEach(System.out::println);
//        int randomNumber = ThreadLocalRandom.current().nextInt(0, androidDevices.size());
        Random r = new Random();
        int randomNumber = r.ints(0, androidDevices.size()).findFirst().getAsInt();
        System.out.println(androidDevices.get(randomNumber).getDevice());
    }

    @Test
    public void getAppList() {
        get("recent_apps").prettyPrint();
    }

}
