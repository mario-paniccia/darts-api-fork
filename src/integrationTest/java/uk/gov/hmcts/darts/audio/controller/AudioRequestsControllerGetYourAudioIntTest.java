package uk.gov.hmcts.darts.audio.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.darts.common.entity.UserAccountEntity;
import uk.gov.hmcts.darts.testutils.IntegrationBase;

import java.net.URI;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.darts.audiorequests.model.MediaRequestStatus.EXPIRED;

@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
class AudioRequestsControllerGetYourAudioIntTest extends IntegrationBase {

    @Autowired
    private MockMvc mockMvc;

    private UserAccountEntity systemUser;

    @BeforeAll
    void beforeAll() {
        systemUser = dartsDatabase.createSystemUserAccountEntity();
    }

    @Test
    void getYourAudioCurrent() throws Exception {

        var requestor = dartsDatabase.createIntegrationTestUserAccountEntity(systemUser);
        dartsDatabase.createAndLoadCurrentMediaRequestEntity(requestor);

        var requestBuilder = get(URI.create(String.format("/audio-requests?expired=%s", FALSE)))
            .header(
                "user_id",
                requestor.getId()
            );

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        String actualJson = mvcResult.getResponse().getContentAsString();
        String expectedJson = """
            [
                {
                    "media_request_id": 1,
                    "case_number": "2",
                    "courthouse_name": "NEWCASTLE",
                    "hearing_date": "2023-06-10",
                    "media_request_start_ts": "2023-06-26T13:00:00Z",
                    "media_request_end_ts": "2023-06-26T13:45:00Z",
                    "media_request_status": "OPEN"
                }
            ]
            """;
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void getYourAudioExpired() throws Exception {

        var requestor = dartsDatabase.createIntegrationTestUserAccountEntity(systemUser);
        var currentMediaRequest = dartsDatabase.createAndLoadCurrentMediaRequestEntity(requestor);
        var expiredMediaRequest = dartsDatabase.createAndLoadExpiredMediaRequestEntity(
            currentMediaRequest.getHearing(),
            currentMediaRequest.getRequestor()
        );

        var requestBuilder = get(URI.create(String.format("/audio-requests?expired=%s", TRUE)))
            .header(
                "user_id",
                requestor.getId()
            );

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].media_request_id", is(expiredMediaRequest.getId())))
            .andExpect(jsonPath("$[0].case_number", is("2")))
            .andExpect(jsonPath("$[0].courthouse_name", is("NEWCASTLE")))
            .andExpect(jsonPath("$[0].media_request_start_ts").isString())
            .andExpect(jsonPath("$[0].media_request_end_ts").isString())
            .andExpect(jsonPath("$[0].media_request_expiry_ts").isString())
            .andExpect(jsonPath("$[0].media_request_status", is(EXPIRED.getValue())));
    }

    @Test
    void getYourAudioCurrentShouldReturnEmptyArrayInResponseBodyWhenNoCurrentMediaRequestExists() throws Exception {

        var requestor = dartsDatabase.createIntegrationTestUserAccountEntity(systemUser);

        var requestBuilder = get(URI.create(String.format("/audio-requests?expired=%s", FALSE)))
            .header(
                "user_id",
                requestor.getId()
            );

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        String actualJson = mvcResult.getResponse().getContentAsString();
        String expectedJson = """
            []
            """;
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void getYourAudioExpiredShouldReturnEmptyArrayInResponseBodyWhenNoExpiredMediaRequestExists() throws Exception {

        var requestor = dartsDatabase.createIntegrationTestUserAccountEntity(systemUser);
        dartsDatabase.createAndLoadCurrentMediaRequestEntity(requestor);

        var requestBuilder = get(URI.create(String.format("/audio-requests?expired=%s", TRUE)))
            .header(
                "user_id",
                requestor.getId()
            );

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        String actualJson = mvcResult.getResponse().getContentAsString();
        String expectedJson = """
            []
            """;
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void getYourAudioShouldReturnBadRequestWhenExpiredQueryParamIsNotPresent() throws Exception {

        var requestor = dartsDatabase.createIntegrationTestUserAccountEntity(systemUser);

        var requestBuilder = get(URI.create("/audio-requests"))
            .header(
                "user_id",
                requestor.getId()
            );

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andReturn();

        String actualJson = mvcResult.getResponse().getContentAsString();
        String expectedJson = """
            {
              "title": "Bad Request",
              "status": 400,
              "detail": "Required request parameter 'expired' for method parameter type Boolean is not present"
            }""";

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void getYourAudioShouldReturnBadRequestWhenUserIdHeaderIsNotPresent() throws Exception {

        var requestBuilder = get(URI.create(String.format("/audio-requests?expired=%s", FALSE)));

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andReturn();

        String actualJson = mvcResult.getResponse().getContentAsString();
        String expectedJson = """
            {
              "title": "Bad Request",
              "status": 400,
              "detail": "Required request header 'user_id' for method parameter type Integer is not present"
            }
            """;
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void getYourAudioShouldReturnEmptyArrayInResponseBodyWhenNoUserAccountExists() throws Exception {

        var requestBuilder = get(URI.create(String.format("/audio-requests?expired=%s", FALSE)))
            .header(
                "user_id",
                999
            );

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        String actualJson = mvcResult.getResponse().getContentAsString();
        String expectedJson = """
            []
            """;
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

}