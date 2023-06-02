package uk.gov.hmcts.darts.authentication;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.darts.FunctionalTest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthenticationFunctionalTest extends FunctionalTest {

    @Test
    void shouldAllowAccessWhenUnprotectedEndpointIsCalledWithoutAuth() {
        Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .baseUri(getUri("/"))
            .get()
            .then()
            .extract().response();

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldReturnUnauthorizedWhenSecuredEndpointIsCalledWithoutAuth() {
        Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .baseUri(getUri("/dummy-secured-endpoint"))
            .redirects().follow(false)
            .get()
            .then()
            .extract().response();

        assertEquals(401, response.statusCode());
    }

    @Test
    void shouldAllowAccessWhenSecuredEndpointIsCalledWithAuth() {
        Response response = buildRequestWithAuth()
            .contentType(ContentType.JSON)
            .when()
            .baseUri(getUri("/dummy-secured-endpoint"))
            .redirects().follow(false)
            .get()
            .then()
            .extract().response();

        assertEquals(404, response.statusCode());
    }

}