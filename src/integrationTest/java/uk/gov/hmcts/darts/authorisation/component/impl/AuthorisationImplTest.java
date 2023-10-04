package uk.gov.hmcts.darts.authorisation.component.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.darts.authorisation.component.Authorisation;
import uk.gov.hmcts.darts.authorisation.component.UserIdentity;
import uk.gov.hmcts.darts.common.exception.DartsApiException;
import uk.gov.hmcts.darts.testutils.IntegrationBase;
import uk.gov.hmcts.darts.testutils.stubs.AuthorisationStub;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.darts.audio.exception.AudioApiError.MEDIA_NOT_FOUND;
import static uk.gov.hmcts.darts.audio.exception.AudioRequestsApiError.MEDIA_REQUEST_NOT_FOUND;
import static uk.gov.hmcts.darts.audio.exception.AudioRequestsApiError.MEDIA_REQUEST_NOT_VALID_FOR_USER;
import static uk.gov.hmcts.darts.authorisation.exception.AuthorisationError.USER_NOT_AUTHORISED_FOR_COURTHOUSE;
import static uk.gov.hmcts.darts.cases.exception.CaseApiError.CASE_NOT_FOUND;
import static uk.gov.hmcts.darts.common.enums.SecurityRoleEnum.APPROVER;
import static uk.gov.hmcts.darts.common.enums.SecurityRoleEnum.JUDGE;
import static uk.gov.hmcts.darts.common.enums.SecurityRoleEnum.REQUESTER;
import static uk.gov.hmcts.darts.hearings.exception.HearingApiError.HEARING_NOT_FOUND;
import static uk.gov.hmcts.darts.transcriptions.exception.TranscriptionApiError.TRANSCRIPTION_NOT_FOUND;

@Transactional
class AuthorisationImplTest extends IntegrationBase {

    @MockBean
    private UserIdentity mockUserIdentity;

    @Autowired
    private AuthorisationStub authorisationStub;

    @Autowired
    private Authorisation authorisationToTest;


    @BeforeEach
    void beforeEach() {
        authorisationStub.givenTestSchema();
        when(mockUserIdentity.getEmailAddress()).thenReturn(authorisationStub.getTestUser().getEmailAddress());
        when(mockUserIdentity.getUserAccount()).thenReturn(authorisationStub.getTestUser());
    }

    @Test
    void authoriseByCaseId() {
        assertDoesNotThrow(() -> authorisationToTest.authoriseByCaseId(
            authorisationStub.getCourtCaseEntity().getId(), Set.of(APPROVER, REQUESTER)));
    }

    @Test
    void authoriseByCaseIdShouldThrowAuthorisationErrorUserNotAuthorisedForCourthouse() {
        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseByCaseId(
                authorisationStub.getCourtCaseEntity().getId(), Set.of(JUDGE))
        );

        assertEquals(USER_NOT_AUTHORISED_FOR_COURTHOUSE.getTitle(), exception.getMessage());
        assertEquals(USER_NOT_AUTHORISED_FOR_COURTHOUSE, exception.getError());
    }

    @Test
    void authoriseByCaseIdShouldThrowCaseApiErrorCaseNotFound() {
        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseByCaseId(
                -1, Set.of(JUDGE))
        );

        assertEquals(CASE_NOT_FOUND.getTitle(), exception.getMessage());
        assertEquals(CASE_NOT_FOUND, exception.getError());
    }

    @Test
    void authoriseByHearingId() {
        assertDoesNotThrow(() -> authorisationToTest.authoriseByHearingId(
            authorisationStub.getHearingEntity().getId(), Set.of(APPROVER, REQUESTER)));
    }

    @Test
    void authoriseByHearingIdShouldThrowAuthorisationErrorUserNotAuthorisedForCourthouse() {
        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseByHearingId(
                authorisationStub.getHearingEntity().getId(), Set.of(JUDGE))
        );

        assertEquals(USER_NOT_AUTHORISED_FOR_COURTHOUSE.getTitle(), exception.getMessage());
        assertEquals(USER_NOT_AUTHORISED_FOR_COURTHOUSE, exception.getError());
    }

    @Test
    void authoriseByHearingIdShouldThrowHearingApiErrorHearingNotFound() {
        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseByHearingId(
                -1, Set.of(JUDGE))
        );

        assertEquals(HEARING_NOT_FOUND.getTitle(), exception.getMessage());
        assertEquals(HEARING_NOT_FOUND, exception.getError());
    }

    @Test
    void authoriseByMediaRequestId() {
        assertDoesNotThrow(() -> authorisationToTest.authoriseByMediaRequestId(
            authorisationStub.getMediaRequestEntity().getId(), Set.of(APPROVER, REQUESTER)));
    }

    @Test
    void authoriseByMediaRequestIdShouldThrowAuthorisationErrorUserNotAuthorisedForCourthouse() {
        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseByMediaRequestId(
                authorisationStub.getMediaRequestEntity().getId(), Set.of(JUDGE))
        );

        assertEquals(USER_NOT_AUTHORISED_FOR_COURTHOUSE.getTitle(), exception.getMessage());
        assertEquals(USER_NOT_AUTHORISED_FOR_COURTHOUSE, exception.getError());
    }

    @Test
    void authoriseByMediaRequestIdShouldThrowAudioRequestsApiErrorMediaRequestNotFound() {
        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseByMediaRequestId(
                -1, Set.of(JUDGE))
        );

        assertEquals(MEDIA_REQUEST_NOT_FOUND.getTitle(), exception.getMessage());
        assertEquals(MEDIA_REQUEST_NOT_FOUND, exception.getError());
    }

    @Test
    void authoriseMediaRequestAgainstUser() {
        assertDoesNotThrow(() -> authorisationToTest.authoriseMediaRequestAgainstUser(
            authorisationStub.getMediaRequestEntity().getId()));
    }

    @Test
    void authoriseMediaRequestAgainstUserShouldThrowAudioRequestsApiErrorMediaRequestNotValidForUser() {

        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseMediaRequestAgainstUser(
                authorisationStub.getMediaRequestEntitySystemUser().getId())
        );

        assertEquals(MEDIA_REQUEST_NOT_VALID_FOR_USER.getTitle(), exception.getMessage());
        assertEquals(MEDIA_REQUEST_NOT_VALID_FOR_USER, exception.getError());
    }

    @Test
    void authoriseByMediaId() {
        assertDoesNotThrow(() -> authorisationToTest.authoriseByMediaId(
            authorisationStub.getMediaEntity().getId(), Set.of(APPROVER, REQUESTER)));
    }

    @Test
    void authoriseByMediaIdShouldThrowAuthorisationErrorUserNotAuthorisedForCourthouse() {
        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseByMediaId(
                authorisationStub.getMediaEntity().getId(), Set.of(JUDGE))
        );

        assertEquals(USER_NOT_AUTHORISED_FOR_COURTHOUSE.getTitle(), exception.getMessage());
        assertEquals(USER_NOT_AUTHORISED_FOR_COURTHOUSE, exception.getError());
    }

    @Test
    void authoriseByMediaIdShouldThrowAudioApiErrorMediaNotFound() {
        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseByMediaId(
                -1, Set.of(JUDGE))
        );

        assertEquals(MEDIA_NOT_FOUND.getTitle(), exception.getMessage());
        assertEquals(MEDIA_NOT_FOUND, exception.getError());
    }

    @Test
    void authoriseByTranscriptionId() {
        assertDoesNotThrow(() -> authorisationToTest.authoriseByTranscriptionId(
            authorisationStub.getTranscriptionEntity().getId(), Set.of(APPROVER, REQUESTER)));
    }

    @Test
    void authoriseByTranscriptionIdShouldThrowAuthorisationErrorUserNotAuthorisedForCourthouse() {
        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseByTranscriptionId(
                authorisationStub.getTranscriptionEntity().getId(), Set.of(JUDGE))
        );

        assertEquals(USER_NOT_AUTHORISED_FOR_COURTHOUSE.getTitle(), exception.getMessage());
        assertEquals(USER_NOT_AUTHORISED_FOR_COURTHOUSE, exception.getError());
    }

    @Test
    void authoriseByTranscriptionIdShouldThrowTranscriptionApiErrorTranscriptionNotFound() {
        var exception = assertThrows(
            DartsApiException.class,
            () -> authorisationToTest.authoriseByTranscriptionId(
                -1, Set.of(JUDGE))
        );

        assertEquals(TRANSCRIPTION_NOT_FOUND.getTitle(), exception.getMessage());
        assertEquals(TRANSCRIPTION_NOT_FOUND, exception.getError());
    }

}