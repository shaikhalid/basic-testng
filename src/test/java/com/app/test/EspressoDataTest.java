package com.app.test;

import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;

import static io.restassured.RestAssured.*;

public class EspressoDataTest {

    String appUrl, testSuiteUrl;

    @BeforeSuite
    public void setup() {
        PreemptiveBasicAuthScheme authenticationScheme = new PreemptiveBasicAuthScheme();
        authenticationScheme.setUserName(System.getenv("BROWSERSTACK_USERNAME"));
        authenticationScheme.setPassword(System.getenv("BROWSERSTACK_ACCESS_KEY"));
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://api-cloud.browserstack.com")
                .setBasePath("app-automate/espresso/v2")
                .setAuth(authenticationScheme)
                .build();
        responseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
    }

    @Test(enabled = true)
    public void espressoBuildDetails() {
        given()
                .header("Content-Type", "application/json")
                .get("builds/0daca02244f5843f725b0eb4db459d4983c53f7b")
                .prettyPrint();
    }

    @Test(enabled = false)
    public void espressoSessionDetails() {
        given()
                .header("Content-Type", "application/json")
                .get("builds/0daca02244f5843f725b0eb4db459d4983c53f7b/sessions/108db15e390457cedfc2820f6cb7a958285cc8c5")
                .prettyPrint();
    }

}
