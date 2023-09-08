package uk.gov.hmcts.darts.events;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.darts.FunctionalTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CourtlogsFunctionalTest extends FunctionalTest {


    public static final String ENDPOINT_URL = "/courtlogs";

    @Test
    @Disabled
    @Order(1)
    void postSuccess() {
        Response response = buildRequestWithAuth()
            .contentType(ContentType.JSON)
            .body("""
                      {
                        "log_entry_date_time": "1999-05-23T09:15:25Z",
                        "courthouse": "liverpool",
                        "courtroom": "1",
                        "case_numbers": [
                          "CASE1001"
                        ],
                        "text": "System : Start Recording : Record: Case Code:0008, New Case"
                      }""")
            .when()
            .baseUri(getUri(ENDPOINT_URL))
            .redirects().follow(false)
            .post()
            .then()
            .extract().response();

        assertEquals(201, response.statusCode());
    }

    @Test
    @Order(2)
    void postFail() {
        Response response = buildRequestWithAuth()
            .contentType(ContentType.JSON)
            .body("""
                      {
                        "log_entry_date_time": "2023-05-23T09:15:25Z",
                        "courthouse": "",
                        "courtroom": "1",
                        "case_numbers": [
                          "CASE1001"
                        ],
                        "text": "System : Start Recording : Record: Case Code:0008, New Case"
                      }""")
            .when()
            .baseUri(getUri(ENDPOINT_URL))
            .redirects().follow(false)
            .post()
            .then()
            .extract().response();

        assertEquals(400, response.statusCode());
    }

    @Test
    @Disabled
    @Order(3)
    void getSuccess() {
        Response response = buildRequestWithAuth()
            .contentType(ContentType.JSON)
            .param("courthouse", "liverpool")
            .param("case_number", "CASE1001")
            .param("start_date_time", "1999-05-23T09:15:25Z")
            .param("end_date_time", "1999-05-23T09:15:25Z")
            .when()
            .baseUri(getUri(ENDPOINT_URL))
            .redirects().follow(false)
            .get()
            .then()
            .extract().response();

        assertEquals("""
                         [
                             {
                                 "courthouse": "Liverpool",
                                 "caseNumber": "CASE1001",
                                 "timestamp": "1999-05-23T09:15:25Z",
                                 "eventText": "System : Start Recording : Record: Case Code:0008, New Case"
                             }
                         ]""", response.asPrettyString());
    }

    @Test
    @Order(4)
    void getFail() {
        Response response = buildRequestWithAuth()
            .contentType(ContentType.JSON)
            .param("courthouse", "liverpool")
            .param("case_number", "CASE1001")
            .param("start_date_time1", "2023-05-24T09:15:25Z")
            .param("end_date_time", "2023-05-23T09:15:25Z")
            .when()
            .baseUri(getUri(ENDPOINT_URL))
            .redirects().follow(false)
            .get()
            .then()
            .extract().response();

        assertEquals(400, response.statusCode());
        assertThat(response.asPrettyString()).contains(
            "Required request parameter 'start_date_time' for method parameter type OffsetDateTime is not present");

    }

}
