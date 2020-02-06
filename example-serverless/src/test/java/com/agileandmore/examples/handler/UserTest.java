package com.agileandmore.examples.handler;

import com.agileandmore.slsemulator.SlsEmulator;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This unit test is actually an integration test : it goes through API gateway,
 * lambda, and down database or dependencies if you want to.
 */
public class UserTest {

    static SlsEmulator emulator = null;

    /**
     * Typically you would have a base test class with the logic to start and
     * stop the emulation, and you would inherit this class in your test
     * classes.
     */
    @BeforeClass
    public static void setUpClass() {
        emulator = new SlsEmulator();

        // if the endpoint.url property is defined, we use it to override
        // the default value. This property can be used either to run on
        // a custom port locally, or to play tests against deployed APIs.
        String endpointUrl = System.getProperty("endpoint.url");
        if (endpointUrl == null) {
            endpointUrl = "http://localhost:7070";
        }

        RestAssured.baseURI = endpointUrl;
        RestAssured.urlEncodingEnabled = true;
    }

    @AfterClass
    public static void tearDownClass() {
        emulator.stop();
    }

    @Before
    public void setUp() {
        // here you would for example clean up tables before each test
        // make sure tests can be run in any order
    }

    @After
    public void after() {
        // do something after each test
    }

    @Test
    public void should_create_user() {

        User user = new User();
        user.setFirstName("firstName");
        user.setLastName("lastName");

        // call the API. This is going through the serverless emulation if run locally.
        given().contentType("application/json").body(user)
                .when().post("/api/v1/users")
                .then().statusCode(201);

        // in a real test you would have assertions here
        // depends if the operation returns a body or not
    }

    @Test
    public void should_find_user() {

        User user = given()
                .pathParam("phone", "0901020102")
                .contentType("application/json")
                .when().get("/api/v1/users/{phone}")
                .then()
                .statusCode(200).extract().response().as(User.class);

        // in a real test you would have assertions here
        // depends if the operation returns a body or not
        assertThat(user.getFirstName()).isEqualTo("My First Name");
        assertThat(user.getLastName()).isEqualTo("My Last Name");
    }

    @Test
    public void should_delete_user() {

        given()
                .pathParam("phone", "0901020102")
                .contentType("application/json")
                .when().delete("/api/v1/users/{phone}")
                .then()
                .statusCode(204);

        // in a real test you would have assertions here
        // depends if the operation returns a body or not
        // you may test also if the user has really been deleted in the database
    }

    @Test
    public void should_update_user() {

        User user = new User();
        user.setFirstName("firstName");
        user.setLastName("lastName");

        // call the API. This is going through the serverless emulation if run locally.
        given().contentType("application/json").body(user)
                .pathParam("phone", "0901020102")
                .when().put("/api/v1/users/{phone}")
                .then().statusCode(200);

        // in a real test you would have assertions here
        // depends if the operation returns a body or not
    }

}
