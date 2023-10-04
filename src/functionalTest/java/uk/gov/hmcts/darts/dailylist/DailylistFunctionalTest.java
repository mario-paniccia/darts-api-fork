package uk.gov.hmcts.darts.dailylist;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.darts.FunctionalTest;
import uk.gov.hmcts.darts.dailylist.enums.SourceType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailylistFunctionalTest extends FunctionalTest {

    public static final String POST_DAILYLIST_URL = "/dailylists";

    @ParameterizedTest
    @EnumSource(names = {"CPP", "XHB"})
    void postDailyList(SourceType sourceType) throws IOException {
        String todayDateString = LocalDate.now().toString();
        String tomorrowDateString = LocalDate.now().plusDays(1).toString();

        String xmlDocument = getContentsFromFile("DailyList-Document.xml");

        Response response = buildRequestWithExternalAuth()
            .contentType(ContentType.JSON)
            .queryParam("source_system", sourceType)
            .queryParam("courthouse", "Swansea")
            .queryParam("hearing_date", tomorrowDateString)
            .queryParam("unique_id", "1111111")
            .queryParam("published_ts", todayDateString + "T23:30:52.123Z")
            .header("xml_document", xmlDocument)
            .when()
            .baseUri(getUri(POST_DAILYLIST_URL))
            .redirects().follow(false)
            .post().then().extract().response();

        assertEquals(200, response.getStatusCode());

        Integer dalId = response.jsonPath().get("dal_id");

        String jsonDocument = getJsonDocumentWithValues(todayDateString, tomorrowDateString);

        //then patch it with JSON
        response = buildRequestWithExternalAuth()
            .contentType(ContentType.JSON)
            .queryParam("dal_id", dalId)
            .header("json_document", jsonDocument)
            .when()
            .baseUri(getUri(POST_DAILYLIST_URL))
            .redirects().follow(false)
            .patch().then().extract().response();

        assertEquals(200, response.getStatusCode());
    }

    private String getJsonDocumentWithValues(String todayDateString, String tomorrowDateString) throws IOException {
        String jsonDocument = getContentsFromFile("DailyListRequest.json");

        jsonDocument = jsonDocument.replace("<<TODAY>>", todayDateString);
        jsonDocument = jsonDocument.replace("<<TOMORROW>>", tomorrowDateString);
        jsonDocument = jsonDocument.replace("<<CASENUMBER>>", UUID.randomUUID().toString());
        jsonDocument = jsonDocument.replace("<<JUDGENAME>>", UUID.randomUUID().toString());
        return jsonDocument;
    }

    @Test
    void postInvalidCourthouse() throws IOException {

        String todayDateString = LocalDate.now().toString();
        String tomorrowDateString = LocalDate.now().plusDays(1).toString();

        String xmlDocument = getContentsFromFile("DailyList-Document.xml");

        Response response = buildRequestWithExternalAuth()
            .contentType(ContentType.JSON)
            .queryParam("source_system", "XHB")
            .queryParam("courthouse", "doesnotexist")
            .queryParam("hearing_date", tomorrowDateString)
            .queryParam("unique_id", "1111111")
            .queryParam("published_ts", todayDateString + "T23:30:52.123Z")
            .header("xml_document", xmlDocument)
            .when()
            .baseUri(getUri(POST_DAILYLIST_URL))
            .redirects().follow(false)
            .post().then().extract().response();

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void postNoDocument() throws IOException {

        String todayDateString = LocalDate.now().toString();
        String tomorrowDateString = LocalDate.now().plusDays(1).toString();

        Response response = buildRequestWithExternalAuth()
            .contentType(ContentType.JSON)
            .queryParam("source_system", "XHB")
            .queryParam("courthouse", "Swansea")
            .queryParam("hearing_date", tomorrowDateString)
            .queryParam("unique_id", "1111111")
            .queryParam("published_ts", todayDateString + "T23:30:52.123Z")
            .when()
            .baseUri(getUri(POST_DAILYLIST_URL))
            .redirects().follow(false)
            .post().then().extract().response();

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("title").contains("xml_document"));
        assertTrue(response.getBody().jsonPath().getString("title").contains("json_document"));
    }
}