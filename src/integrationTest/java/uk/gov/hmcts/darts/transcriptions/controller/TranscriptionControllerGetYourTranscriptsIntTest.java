package uk.gov.hmcts.darts.transcriptions.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.darts.common.entity.TranscriptionEntity;
import uk.gov.hmcts.darts.common.entity.UserAccountEntity;
import uk.gov.hmcts.darts.testutils.IntegrationBase;
import uk.gov.hmcts.darts.testutils.stubs.AuthorisationStub;
import uk.gov.hmcts.darts.testutils.stubs.TranscriptionStub;

import java.net.URI;
import java.time.OffsetDateTime;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class TranscriptionControllerGetYourTranscriptsIntTest extends IntegrationBase {

    private static final URI ENDPOINT_URI = URI.create("/transcriptions");

    @Autowired
    private AuthorisationStub authorisationStub;

    @Autowired
    private TranscriptionStub transcriptionStub;

    @Autowired
    private MockMvc mockMvc;

    private TranscriptionEntity transcriptionEntity;
    private UserAccountEntity testUser;
    private UserAccountEntity systemUser;

    private static final OffsetDateTime YESTERDAY = now(UTC).minusDays(1).withHour(9).withMinute(0)
        .withSecond(0).withNano(0);

    @BeforeEach
    void beforeEach() {
        authorisationStub.givenTestSchema();

        transcriptionEntity = authorisationStub.getTranscriptionEntity();

        systemUser = authorisationStub.getSystemUser();
        testUser = authorisationStub.getTestUser();
    }

    @Test
    void getYourTranscriptsShouldReturnRequesterOnlyOk() throws Exception {
        var courtCase = authorisationStub.getCourtCaseEntity();
        var hearing = authorisationStub.getHearingEntity();
        transcriptionStub.createAndSaveCompletedTranscription(authorisationStub.getTestUser(), courtCase, hearing, YESTERDAY, true);

        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT_URI)
            .header(
                "user_id",
                testUser.getId()
            );

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requester_transcriptions", hasSize(1)))
            .andExpect(jsonPath("$.requester_transcriptions[0].transcription_id", is(transcriptionEntity.getId())))
            .andExpect(jsonPath("$.requester_transcriptions[0].case_id", is(courtCase.getId())))
            .andExpect(jsonPath(
                "$.requester_transcriptions[0].case_number",
                is(courtCase.getCaseNumber())
            ))
            .andExpect(jsonPath("$.requester_transcriptions[0].courthouse_name", is("Bristol")))
            .andExpect(jsonPath("$.requester_transcriptions[0].hearing_date").isString())
            .andExpect(jsonPath("$.requester_transcriptions[0].transcription_type", is("Specified Times")))
            .andExpect(jsonPath("$.requester_transcriptions[0].status", is("Awaiting Authorisation")))
            .andExpect(jsonPath("$.requester_transcriptions[0].urgency", is("Standard")))
            .andExpect(jsonPath("$.requester_transcriptions[0].requested_ts").isString())

            .andExpect(jsonPath("$.approver_transcriptions").isEmpty());
    }

    @Test
    void getYourTranscriptsShouldReturnOk() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT_URI)
            .header(
                "user_id",
                systemUser.getId()
            );

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requester_transcriptions").isEmpty())
            .andExpect(jsonPath("$.approver_transcriptions").isEmpty());
    }

    @Test
    void getYourTranscriptsShouldReturnRequesterAndApproverCombinedOk() throws Exception {
        var courtCase = authorisationStub.getCourtCaseEntity();
        var systemUserTranscription = dartsDatabase.getTranscriptionStub()
            .createAndSaveAwaitingAuthorisationTranscription(
                systemUser,
                courtCase,
                authorisationStub.getHearingEntity(), now(UTC)
            );

        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT_URI)
            .header(
                "user_id",
                testUser.getId()
            );

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requester_transcriptions[0].transcription_id", is(transcriptionEntity.getId())))
            .andExpect(jsonPath(
                "$.requester_transcriptions[0].case_id",
                is(courtCase.getId())
            ))
            .andExpect(jsonPath(
                "$.requester_transcriptions[0].case_number",
                is(courtCase.getCaseNumber())
            ))
            .andExpect(jsonPath("$.requester_transcriptions[0].courthouse_name", is("Bristol")))
            .andExpect(jsonPath("$.requester_transcriptions[0].hearing_date").isString())
            .andExpect(jsonPath("$.requester_transcriptions[0].transcription_type", is("Specified Times")))
            .andExpect(jsonPath("$.requester_transcriptions[0].status", is("Awaiting Authorisation")))
            .andExpect(jsonPath("$.requester_transcriptions[0].urgency", is("Standard")))
            .andExpect(jsonPath("$.requester_transcriptions[0].requested_ts").isString())

            .andExpect(jsonPath("$.approver_transcriptions[0].transcription_id", is(systemUserTranscription.getId())))
            .andExpect(jsonPath(
                "$.approver_transcriptions[0].case_id",
                is(courtCase.getId())
            ))
            .andExpect(jsonPath(
                "$.approver_transcriptions[0].case_number",
                is(courtCase.getCaseNumber())
            ))
            .andExpect(jsonPath("$.approver_transcriptions[0].courthouse_name", is("Bristol")))
            .andExpect(jsonPath("$.approver_transcriptions[0].hearing_date").isString())
            .andExpect(jsonPath("$.approver_transcriptions[0].transcription_type", is("Specified Times")))
            .andExpect(jsonPath("$.approver_transcriptions[0].status", is("Awaiting Authorisation")))
            .andExpect(jsonPath("$.approver_transcriptions[0].urgency", is("Standard")))
            .andExpect(jsonPath("$.approver_transcriptions[0].requested_ts").isString());
    }

}
