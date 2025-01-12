package uk.gov.hmcts.darts.transcriptions.controller;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.darts.testutils.IntegrationBase;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@TestInstance(PER_CLASS)
class TranscriptionControllerGetTranscriberTranscriptsIntTest extends IntegrationBase {

    private static final URI ENDPOINT_URI = URI.create("/transcriptions/transcriber-view");
    private static final String USER_ID_HEADER = "user_id";
    private static final String ASSIGNED_QUERY_PARAM = "assigned";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    @SuppressWarnings({"checkstyle.LineLengthCheck"})
    void beforeAll() {
        jdbcTemplate.update("""
                                INSERT INTO darts.courthouse (cth_id, courthouse_code, courthouse_name, created_ts, last_modified_ts, created_by,
                                last_modified_by, display_name)
                                VALUES (-1, NULL, 'Bristol', '2023-11-17 15:06:15.859244+00', '2023-11-17 15:06:15.859244+00', NULL, NULL, 'Bristol');
                                INSERT INTO darts.courtroom (ctr_id, cth_id, courtroom_name, created_ts, created_by)
                                VALUES (-1, -1, 'Court 1', NULL, NULL);
                                INSERT INTO darts.court_case (cas_id, cth_id, evh_id, case_object_id, case_number, case_closed, interpreter_used,
                                case_closed_ts, version_label, created_ts, created_by, last_modified_ts, last_modified_by, retention_applies_from_ts,
                                end_of_sentence_ts)
                                VALUES (-1, -1, NULL, NULL, 'T20231009-1', false, false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
                                INSERT INTO darts.hearing (hea_id, cas_id, ctr_id, hearing_date, scheduled_start_time, hearing_is_actual, judge_hearing_date,
                                created_ts, created_by, last_modified_ts, last_modified_by)
                                VALUES (-1, -1, -1, '2023-11-17', NULL, true, NULL, NULL, NULL, NULL, NULL);

                                INSERT INTO darts.user_account (usr_id, dm_user_s_object_id, user_name, user_email_address, description, is_active, created_ts,
                                last_modified_ts, last_login_ts, last_modified_by, created_by, account_guid, is_system_user)
                                VALUES (-10, NULL, 'Richard B', 'Richard.B@example.com', NULL, true, NULL, NULL, NULL, NULL, NULL, NULL, false);
                                INSERT INTO darts.security_group_user_account_ae (usr_id, grp_id)
                                VALUES (-10, -4);
                                INSERT INTO darts.security_group_courthouse_ae (grp_id, cth_id)
                                VALUES (-4, -1);

                                -- Transcript Requests: Approved
                                INSERT INTO darts.transcription (tra_id, cas_id, ctr_id, trt_id, hea_id, transcription_object_id, requestor, start_ts, end_ts,
                                created_ts, last_modified_ts, last_modified_by, version_label, created_by, tru_id, trs_id, hearing_date,
                                is_manual_transcription, hide_request_from_requestor)
                                VALUES (41, -1, NULL, 9, -1, NULL, NULL, '2023-11-23 09:00:00+00', '2023-11-23 09:30:00+00', '2023-11-23 16:25:55.297666+00',
                                '2023-11-23 16:26:20.451054+00', -10, NULL, -10, 1, 3, NULL, true, false);
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (41, 41, 1, -10, '2023-11-23 16:25:55.304517+00');
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (42, 41, 2, -10, '2023-11-23 16:25:55.338405+00');
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (43, 41, 3, -10, '2023-11-23 16:26:20.441633+00');

                                -- Your work > To do: With Transcriber
                                INSERT INTO darts.transcription (tra_id, cas_id, ctr_id, trt_id, hea_id, transcription_object_id, requestor, start_ts, end_ts,
                                created_ts, last_modified_ts, last_modified_by, version_label, created_by, tru_id, trs_id, hearing_date,
                                is_manual_transcription, hide_request_from_requestor)
                                VALUES (81, -1, NULL, 9, -1, NULL, NULL, '2023-11-23 09:20:00+00', '2023-11-23 09:30:00+00', '2023-11-23 17:45:14.938855+00',
                                '2023-11-23 17:45:51.1549+00', -10, NULL, -10, 1, 5, NULL, true, false);
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (81, 81, 1, -10, '2023-11-23 17:45:14.940936+00');
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (82, 81, 2, -10, '2023-11-23 17:45:14.948701+00');
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (101, 81, 3, -10, '2023-11-23 17:45:27.069655+00');
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (102, 81, 5, -10, '2023-11-23 17:45:51.151621+00');

                                -- This transcription would be hidden from Your work > Completed today (transcriber-view?assigned=true)
                                INSERT INTO darts.transcription (tra_id, cas_id, ctr_id, trt_id, hea_id, transcription_object_id, requestor, start_ts, end_ts,
                                created_ts, last_modified_ts, last_modified_by, version_label, created_by, tru_id, trs_id, hearing_date,
                                is_manual_transcription, hide_request_from_requestor)
                                VALUES (101, -1, NULL, 9, -1, NULL, NULL, '2023-11-24 09:00:00+00', '2023-11-24 09:30:00+00', '2023-11-24 12:37:00.782036+00',
                                '2023-11-24 12:53:42.870475+00', -10, NULL, -10, 1, 6, NULL, true, false);
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (121, 101, 1, -10, '2023-11-24 12:37:00.812692+00');
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (122, 101, 2, -10, '2023-11-24 12:37:00.846763+00');
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (123, 101, 3, -10, '2023-11-24 12:37:18.762383+00');
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (124, 101, 5, -10, '2023-11-24 12:37:34.976469+00');
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (141, 101, 6, -10, '2023-11-24 12:53:42.839577+00');

                                -- Your work > Completed today: Complete
                                INSERT INTO darts.transcription (tra_id, cas_id, ctr_id, trt_id, hea_id, transcription_object_id, requestor, start_ts, end_ts,
                                created_ts, last_modified_ts, last_modified_by, version_label, created_by, tru_id, trs_id, hearing_date,
                                is_manual_transcription, hide_request_from_requestor)
                                VALUES (121, -1, NULL, 9, -1, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, -10, NULL,
                                -10, 1, 6, NULL, true, false);
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (161, 121, 1, -10, CURRENT_TIMESTAMP);
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (162, 121, 2, -10, CURRENT_TIMESTAMP);
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (163, 121, 3, -10, CURRENT_TIMESTAMP);
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (164, 121, 5, -10, CURRENT_TIMESTAMP);
                                INSERT INTO darts.transcription_workflow (trw_id, tra_id, trs_id, workflow_actor, workflow_ts)
                                VALUES (165, 121, 6, -10, CURRENT_TIMESTAMP);
                                """);
    }

    @AfterAll
    void afterAll() {
        jdbcTemplate.update("""
                                DELETE FROM darts.transcription_workflow WHERE tra_id IN (41, 81, 101, 121);
                                DELETE FROM darts.transcription WHERE tra_id IN (41, 81, 101, 121);

                                DELETE FROM darts.security_group_courthouse_ae WHERE grp_id=-4 AND cth_id=-1;
                                DELETE FROM darts.security_group_user_account_ae WHERE usr_id=-10 AND grp_id=-4;
                                DELETE FROM darts.user_account WHERE usr_id=-10;

                                DELETE FROM darts.hearing WHERE hea_id=-1;
                                DELETE FROM darts.court_case WHERE cas_id=-1;
                                DELETE FROM darts.courtroom WHERE ctr_id=-1;
                                DELETE FROM darts.courthouse WHERE cth_id=-1;
                                """);
    }

    @Test
    void givenAssignedQueryParamOnly_thenReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT_URI)
            .queryParam(ASSIGNED_QUERY_PARAM, TRUE.toString());

        final MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        String actualResponse = mvcResult.getResponse().getContentAsString();

        String expectedResponse = """
            {
              "title": "Bad Request",
              "status": 400,
              "detail": "Required request header 'user_id' for method parameter type Integer is not present"
            }
            """;
        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void givenUserIdHeaderOnly_thenReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT_URI)
            .header(
                USER_ID_HEADER,
                -10
                   );

        final MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        String actualResponse = mvcResult.getResponse().getContentAsString();

        String expectedResponse = """
            {
              "title": "Bad Request",
              "status": 400,
              "detail": "Required request parameter 'assigned' for method parameter type Boolean is not present"
            }
            """;
        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void givenSomeOtherUserAndTranscriptRequestsViewRequested_thenReturnEmptyArray() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT_URI)
            .header(
                USER_ID_HEADER,
                -1
                   )
            .queryParam(ASSIGNED_QUERY_PARAM, FALSE.toString());

        final MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        String actualResponse = mvcResult.getResponse().getContentAsString();
        String expectedResponse = "[]";
        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void givenSomeOtherUserAndYourWorkViewRequested_thenReturnEmptyArray() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT_URI)
            .header(
                USER_ID_HEADER,
                -1
                   )
            .queryParam(ASSIGNED_QUERY_PARAM, TRUE.toString());

        final MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        String actualResponse = mvcResult.getResponse().getContentAsString();
        String expectedResponse = "[]";
        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void givenTranscriberUserAndTranscriptRequestsViewRequested_thenReturnApprovedTranscription() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT_URI)
            .header(
                USER_ID_HEADER,
                -10
                   )
            .queryParam(ASSIGNED_QUERY_PARAM, FALSE.toString());

        final MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        String actualResponse = mvcResult.getResponse().getContentAsString();
        String expectedResponse = """
            [
              {
                "transcription_id": 41,
                "case_id": -1,
                "case_number": "T20231009-1",
                "courthouse_name": "Bristol",
                "hearing_date": "2023-11-17",
                "transcription_type": "Specified Times",
                "status": "Approved",
                "urgency": "Standard",
                "requested_ts": "2023-11-23T16:25:55.304517Z",
                "state_change_ts": "2023-11-23T16:26:20.441633Z",
                "is_manual": true
              }
            ]
            """;
        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    @SuppressWarnings({"checkstyle.LineLengthCheck"})
    void givenTranscriberAndYourWorkViewRequested_thenReturnAssignedAndCompletedTranscriptionTodayAndDoNotReturnCompletedTranscriptionFromBeforeToday()
        throws Exception {
        // This test expects the "Complete" (trs_id=6) transcription (tra_id=101) to be hidden from "Your work > Completed today" view
        // because the workflow_ts is BEFORE TODAY (workflow_ts='2023-11-24 12:53:42.839577+00').
        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT_URI)
            .header(
                USER_ID_HEADER,
                -10
                   )
            .queryParam(ASSIGNED_QUERY_PARAM, TRUE.toString());

        final MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].transcription_id").isArray())
            .andExpect(jsonPath("$.[0].transcription_id").value(121))
            .andExpect(jsonPath("$.[0].status").value("Complete"))
            .andExpect(jsonPath("$.[0].state_change_ts").isNotEmpty())
            .andExpect(jsonPath("$.[1].transcription_id").value(81))
            .andExpect(jsonPath("$.[1].status").value("With Transcriber"))
            .andExpect(jsonPath("$.[1].state_change_ts").value("2023-11-23T17:45:51.151621Z"))
            .andReturn();

        String actualResponse = mvcResult.getResponse().getContentAsString();
        DocumentContext documentContext = JsonPath.parse(actualResponse);
        List<Integer> transcriptionIds = documentContext.read("$..transcription_id");
        assertEquals(List.of(121, 81), transcriptionIds);
        OffsetDateTime completeTimestamp = OffsetDateTime.parse(documentContext.read("$.[0].state_change_ts"));
        OffsetDateTime now = OffsetDateTime.now(UTC);
        assertTrue(completeTimestamp.isAfter(now.truncatedTo(ChronoUnit.DAYS))); // today (CURRENT_DATE)
    }

}
