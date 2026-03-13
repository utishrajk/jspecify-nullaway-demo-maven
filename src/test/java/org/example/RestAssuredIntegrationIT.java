package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.example.dto.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestAssuredIntegrationIT {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void shouldGetAllUsers() {
        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/hello")
        .then()
                .statusCode(200)
                .body("$", hasSize(4))
                .body("[0].fullName", equalTo("John Doe"));
    }

    @Test
    @DirtiesContext
    void shouldAddUser() {
        UserRequest request = new UserRequest("Michael", "Jordan", "mj@example.com", 23, 100000.0);

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/hello")
        .then()
                .statusCode(200)
                .body("fullName", equalTo("Michael Jordan"))
                .body("email", equalTo("mj@example.com"));

        // Verify it was actually added
        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/hello")
        .then()
                .statusCode(200)
                .body("$", hasSize(5))
                .body("[4].fullName", equalTo("Michael Jordan"));
    }
}
