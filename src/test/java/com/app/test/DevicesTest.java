package com.app.test;

import com.app.parallel.test.DeviceDetails;
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

    @Test
    public void getDeviceList() {
        List<DeviceDetails> devices = get("devices.json")
                .jsonPath()
                .getList("", DeviceDetails.class);
        List<DeviceDetails> androidDevices = devices.stream()
                .filter(d -> d.getOs().equals("android"))
                .collect(Collectors.toList());
        androidDevices.forEach(System.out::println);
    }

}
