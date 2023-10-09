package uk.gov.hmcts.darts.audio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.darts.audiorequests.model.AudioRequestType;
import uk.gov.hmcts.darts.common.entity.HearingEntity;
import uk.gov.hmcts.darts.common.exception.DartsApiException;
import uk.gov.hmcts.darts.notification.api.NotificationApi;
import uk.gov.hmcts.darts.notification.entity.NotificationEntity;
import uk.gov.hmcts.darts.notification.enums.NotificationStatus;
import uk.gov.hmcts.darts.testutils.IntegrationBase;
import uk.gov.hmcts.darts.testutils.stubs.SystemCommandExecutorStubImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.darts.audio.enums.AudioRequestStatus.COMPLETED;
import static uk.gov.hmcts.darts.audio.enums.AudioRequestStatus.FAILED;


@Import(SystemCommandExecutorStubImpl.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate")
class AudioTransformationServiceHandleKedaInvocationForMediaRequestsTest extends IntegrationBase {

    private static final String EMAIL_ADDRESS = "test@test.com";

    @Autowired
    private AudioTransformationServiceHandleKedaInvocationForMediaRequestsGivenBuilder given;

    @Autowired
    private AudioTransformationService audioTransformationService;

    @SpyBean
    private MediaRequestService mediaRequestService;

    private HearingEntity hearing;

    @BeforeEach
    void setUp() {
        dartsDatabase.getUserAccountStub().getSystemUserAccountEntity();
        hearing = given.aHearingWith("1", "some-courthouse", "some-courtroom");
    }

    @ParameterizedTest
    @EnumSource(names = {"DOWNLOAD", "PLAYBACK"})
    @Transactional
    @SuppressWarnings("PMD.LawOfDemeter")
    public void handleKedaInvocationForMediaRequestsShouldSucceedAndUpdateRequestStatusToCompletedAndScheduleSuccessNotificationFor(
        AudioRequestType audioRequestType) {
        given.aMediaEntityGraph();
        var userAccountEntity = given.aUserAccount(EMAIL_ADDRESS);
        given.aMediaRequestEntityForHearingWithRequestType(
            hearing,
            audioRequestType,
            userAccountEntity
        );

        Integer mediaRequestId = given.getMediaRequestEntity().getId();

        audioTransformationService.handleKedaInvocationForMediaRequests();

        var mediaRequestEntity = dartsDatabase.getMediaRequestRepository()
            .findById(mediaRequestId)
            .orElseThrow();
        assertEquals(COMPLETED, mediaRequestEntity.getStatus());

        List<NotificationEntity> scheduledNotifications = dartsDatabase.getNotificationRepository()
            .findAll();
        assertEquals(1, scheduledNotifications.size());

        var notificationEntity = scheduledNotifications.get(0);
        assertEquals(NotificationApi.NotificationTemplate.REQUESTED_AUDIO_AVAILABLE.toString(), notificationEntity.getEventId());
        assertNull(notificationEntity.getTemplateValues());
        assertEquals(NotificationStatus.OPEN, notificationEntity.getStatus());
        assertEquals(EMAIL_ADDRESS, notificationEntity.getEmailAddress());
    }

    @ParameterizedTest
    @EnumSource(names = {"DOWNLOAD", "PLAYBACK"})
    @Transactional
    @SuppressWarnings("PMD.LawOfDemeter")
    public void handleKedaInvocationForMediaRequestsShouldFailAndUpdateRequestStatusToFailedAndScheduleFailureNotificationFor(
        AudioRequestType audioRequestType) {
        var userAccountEntity = given.aUserAccount(EMAIL_ADDRESS);
        given.aMediaRequestEntityForHearingWithRequestType(
            hearing,
            audioRequestType,
            userAccountEntity
        );

        Integer mediaRequestId = given.getMediaRequestEntity().getId();
        var exception = assertThrows(
            DartsApiException.class,
            () -> audioTransformationService.handleKedaInvocationForMediaRequests()
        );

        assertEquals("Failed to process audio request", exception.getMessage());

        var mediaRequestEntity = dartsDatabase.getMediaRequestRepository()
            .findById(mediaRequestId)
            .orElseThrow();
        assertEquals(FAILED, mediaRequestEntity.getStatus());

        List<NotificationEntity> scheduledNotifications = dartsDatabase.getNotificationRepository()
            .findAll();
        assertEquals(1, scheduledNotifications.size());

        var notificationEntity = scheduledNotifications.get(0);
        assertEquals(NotificationApi.NotificationTemplate.ERROR_PROCESSING_AUDIO.toString(), notificationEntity.getEventId());
        assertNull(notificationEntity.getTemplateValues());
        assertEquals(NotificationStatus.OPEN, notificationEntity.getStatus());
        assertEquals(EMAIL_ADDRESS, notificationEntity.getEmailAddress());
    }

    @ParameterizedTest
    @EnumSource(names = {"DOWNLOAD", "PLAYBACK"})
    @Transactional
    public void handleKedaInvocationForMediaRequestsShouldNotInvokeProcessAudioRequestWhenNoOpenMediaRequestsExist(
        AudioRequestType audioRequestType) {
        given.aUserAccount(EMAIL_ADDRESS);

        audioTransformationService.handleKedaInvocationForMediaRequests();

        verify(mediaRequestService, never()).updateAudioRequestStatus(any(), any());
    }

}