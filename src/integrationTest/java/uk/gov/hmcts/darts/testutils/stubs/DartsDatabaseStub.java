package uk.gov.hmcts.darts.testutils.stubs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.darts.audio.entity.MediaRequestEntity;
import uk.gov.hmcts.darts.audiorequests.model.AudioRequestType;
import uk.gov.hmcts.darts.common.entity.CourtCaseEntity;
import uk.gov.hmcts.darts.common.entity.CourthouseEntity;
import uk.gov.hmcts.darts.common.entity.CourtroomEntity;
import uk.gov.hmcts.darts.common.entity.DailyListEntity;
import uk.gov.hmcts.darts.common.entity.EventEntity;
import uk.gov.hmcts.darts.common.entity.EventHandlerEntity;
import uk.gov.hmcts.darts.common.entity.ExternalLocationTypeEntity;
import uk.gov.hmcts.darts.common.entity.ExternalObjectDirectoryEntity;
import uk.gov.hmcts.darts.common.entity.HearingEntity;
import uk.gov.hmcts.darts.common.entity.JudgeEntity;
import uk.gov.hmcts.darts.common.entity.MediaEntity;
import uk.gov.hmcts.darts.common.entity.ObjectRecordStatusEntity;
import uk.gov.hmcts.darts.common.entity.TranscriptionCommentEntity;
import uk.gov.hmcts.darts.common.entity.TranscriptionEntity;
import uk.gov.hmcts.darts.common.entity.TranscriptionWorkflowEntity;
import uk.gov.hmcts.darts.common.entity.UserAccountEntity;
import uk.gov.hmcts.darts.common.enums.ExternalLocationTypeEnum;
import uk.gov.hmcts.darts.common.enums.ObjectDirectoryStatusEnum;
import uk.gov.hmcts.darts.common.repository.AuditRepository;
import uk.gov.hmcts.darts.common.repository.CaseRepository;
import uk.gov.hmcts.darts.common.repository.CourthouseRepository;
import uk.gov.hmcts.darts.common.repository.CourtroomRepository;
import uk.gov.hmcts.darts.common.repository.DailyListRepository;
import uk.gov.hmcts.darts.common.repository.DefenceRepository;
import uk.gov.hmcts.darts.common.repository.DefendantRepository;
import uk.gov.hmcts.darts.common.repository.EventHandlerRepository;
import uk.gov.hmcts.darts.common.repository.EventRepository;
import uk.gov.hmcts.darts.common.repository.ExternalLocationTypeRepository;
import uk.gov.hmcts.darts.common.repository.ExternalObjectDirectoryRepository;
import uk.gov.hmcts.darts.common.repository.HearingReportingRestrictionsRepository;
import uk.gov.hmcts.darts.common.repository.HearingRepository;
import uk.gov.hmcts.darts.common.repository.JudgeRepository;
import uk.gov.hmcts.darts.common.repository.MediaRepository;
import uk.gov.hmcts.darts.common.repository.MediaRequestRepository;
import uk.gov.hmcts.darts.common.repository.NodeRegistrationRepository;
import uk.gov.hmcts.darts.common.repository.NotificationRepository;
import uk.gov.hmcts.darts.common.repository.ObjectDirectoryStatusRepository;
import uk.gov.hmcts.darts.common.repository.ProsecutorRepository;
import uk.gov.hmcts.darts.common.repository.SecurityGroupRepository;
import uk.gov.hmcts.darts.common.repository.SecurityRoleRepository;
import uk.gov.hmcts.darts.common.repository.TranscriptionCommentRepository;
import uk.gov.hmcts.darts.common.repository.TranscriptionRepository;
import uk.gov.hmcts.darts.common.repository.TranscriptionWorkflowRepository;
import uk.gov.hmcts.darts.common.repository.TransformedMediaRepository;
import uk.gov.hmcts.darts.common.repository.TransientObjectDirectoryRepository;
import uk.gov.hmcts.darts.common.repository.UserAccountRepository;
import uk.gov.hmcts.darts.common.service.RetrieveCoreObjectService;
import uk.gov.hmcts.darts.dailylist.enums.SourceType;
import uk.gov.hmcts.darts.notification.entity.NotificationEntity;
import uk.gov.hmcts.darts.testutils.data.AudioTestData;
import uk.gov.hmcts.darts.testutils.data.CourthouseTestData;
import uk.gov.hmcts.darts.testutils.data.DailyListTestData;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDate.now;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static uk.gov.hmcts.darts.audio.enums.MediaRequestStatus.OPEN;
import static uk.gov.hmcts.darts.testutils.data.CourtroomTestData.createCourtRoomWithNameAtCourthouse;
import static uk.gov.hmcts.darts.testutils.data.MediaTestData.createMediaWith;

@Service
@AllArgsConstructor
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.ExcessivePublicCount", "PMD.GodClass"})
@Getter
@Slf4j
public class DartsDatabaseStub {

    private final AuditRepository auditRepository;
    private final CaseRepository caseRepository;
    private final CourthouseRepository courthouseRepository;
    private final CourtroomRepository courtroomRepository;
    private final DailyListRepository dailyListRepository;
    private final DefenceRepository defenceRepository;
    private final DefendantRepository defendantRepository;
    private final EventRepository eventRepository;
    private final EventHandlerRepository eventHandlerRepository;
    private final ExternalLocationTypeRepository externalLocationTypeRepository;
    private final ExternalObjectDirectoryRepository externalObjectDirectoryRepository;
    private final HearingRepository hearingRepository;
    private final JudgeRepository judgeRepository;
    private final MediaRepository mediaRepository;
    private final MediaRequestRepository mediaRequestRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectDirectoryStatusRepository objectDirectoryStatusRepository;
    private final ProsecutorRepository prosecutorRepository;
    private final RetrieveCoreObjectService retrieveCoreObjectService;
    private final TranscriptionRepository transcriptionRepository;
    private final TranscriptionWorkflowRepository transcriptionWorkflowRepository;
    private final TranscriptionCommentRepository transcriptionCommentRepository;
    private final TransformedMediaRepository transformedMediaRepository;
    private final TransientObjectDirectoryRepository transientObjectDirectoryRepository;
    private final UserAccountRepository userAccountRepository;
    private final SecurityGroupRepository securityGroupRepository;
    private final SecurityRoleRepository securityRoleRepository;
    private final NodeRegistrationRepository nodeRegistrationRepository;
    private final HearingReportingRestrictionsRepository hearingReportingRestrictionsRepository;

    private final AuditStub auditStub;
    private final CourthouseStub courthouseStub;
    private final EventStub eventStub;
    private final ExternalObjectDirectoryStub externalObjectDirectoryStub;
    private final MediaRequestStub mediaRequestStub;
    private final TranscriptionStub transcriptionStub;
    private final TransformedMediaStub transformedMediaStub;
    private final UserAccountStub userAccountStub;

    private final List<EventHandlerEntity> eventHandlerBin = new ArrayList<>();
    private final List<UserAccountEntity> userAccountBin = new ArrayList<>();

    public void clearDatabaseInThisOrder() {
        auditRepository.deleteAll();
        transcriptionCommentRepository.deleteAll();
        transcriptionWorkflowRepository.deleteAll();
        transcriptionRepository.deleteAll();
        externalObjectDirectoryRepository.deleteAll();
        transientObjectDirectoryRepository.deleteAll();
        transformedMediaRepository.deleteAll();
        mediaRequestRepository.deleteAll();
        eventRepository.deleteAll();
        hearingRepository.deleteAll();
        mediaRepository.deleteAll();
        notificationRepository.deleteAll();
        nodeRegistrationRepository.deleteAll();
        courtroomRepository.deleteAll();
        defenceRepository.deleteAll();
        defendantRepository.deleteAll();
        prosecutorRepository.deleteAll();
        caseRepository.deleteAll();
        judgeRepository.deleteAll();
        dailyListRepository.deleteAll();
        courthouseRepository.deleteAll();
        eventHandlerRepository.deleteAll(eventHandlerBin);
        eventHandlerBin.clear();
        userAccountRepository.deleteAll(userAccountBin);
        userAccountBin.clear();
    }

    public List<EventHandlerEntity> findByHandlerAndActiveTrue(String handlerName) {
        return eventHandlerRepository.findByHandlerAndActiveTrue(handlerName);
    }

    public Optional<CourtCaseEntity> findByCaseByCaseNumberAndCourtHouseName(String someCaseNumber,
                                                                             String someCourthouse) {
        return caseRepository.findByCaseNumberIgnoreCaseAndCourthouse_CourthouseNameIgnoreCase(
            someCaseNumber,
            someCourthouse
        );
    }

    public List<HearingEntity> findByCourthouseCourtroomAndDate(String someCourthouse, String someRoom,
                                                                LocalDate toLocalDate) {
        return hearingRepository.findByCourthouseCourtroomAndDate(someCourthouse, someRoom, toLocalDate);
    }

    public List<EventEntity> getAllEvents() {
        return eventRepository.findAll();
    }

    public JudgeEntity createSimpleJudge(String name) {
        return retrieveCoreObjectService.retrieveOrCreateJudge(name);
    }

    public EventEntity createEvent(HearingEntity hearing, int eventHandlerId) {
        return eventStub.createEvent(hearing, eventHandlerId);
    }

    public EventEntity createEvent(HearingEntity hearing) {
        return eventStub.createEvent(hearing);
    }

    @Transactional
    public void givenTheCourtHouseHasRoom(CourthouseEntity courthouse, String roomName) {
        var courtroom = new CourtroomEntity();
        courtroom.setName(roomName);
        courtroom.setCourthouse(courthouseRepository.getReferenceById(courthouse.getId()));
        courtroomRepository.saveAndFlush(courtroom);
    }

    @Transactional
    public HearingEntity givenTheDatabaseContainsCourtCaseWithHearingAndCourthouseWithRoom(
        String caseNumber, String courthouseName, String courtroomName, LocalDate hearingDate) {
        createCourthouseUnlessExists(courthouseName);
        HearingEntity hearing = retrieveCoreObjectService.retrieveOrCreateHearing(
            courthouseName,
            courtroomName,
            caseNumber,
            hearingDate
        );
        hearing.setHearingIsActual(true);
        hearing.addJudge(createSimpleJudge(caseNumber + "judge1"));
        return hearingRepository.saveAndFlush(hearing);
    }

    @Transactional
    public CourtCaseEntity givenTheDatabaseContainsCourtCaseAndCourthouseWithRoom(String caseNumber,
                                                                                  String courthouseName,
                                                                                  String courtroomName) {
        givenTheDatabaseContainsCourthouseWithRoom(courthouseName, courtroomName);
        return retrieveCoreObjectService.retrieveOrCreateCase(courthouseName, caseNumber);
    }

    public CourtCaseEntity createCase(String courthouseName, String caseNumber) {
        courthouseStub.createCourthouseUnlessExists(courthouseName);
        return retrieveCoreObjectService.retrieveOrCreateCase(courthouseName, caseNumber);
    }

    public CourtroomEntity createCourtroomUnlessExists(String courthouseName, String courtroomName) {
        createCourthouseUnlessExists(courthouseName);
        return retrieveCoreObjectService.retrieveOrCreateCourtroom(courthouseName, courtroomName);
    }

    @Transactional
    public CourtroomEntity givenTheDatabaseContainsCourthouseWithRoom(String courthouseName, String courtroomName) {

        var persistedCourthouse = courthouseStub.createCourthouseUnlessExists(courthouseName);

        var courtroom = new CourtroomEntity();
        courtroom.setName(courtroomName);
        courtroom.setCourthouse(persistedCourthouse);
        courtroomRepository.saveAndFlush(courtroom);
        return courtroom;
    }

    public List<NotificationEntity> getNotificationsForCase(Integer caseId) {
        return notificationRepository.findByCourtCase_Id(caseId);
    }

    @Transactional
    public HearingEntity hasSomeHearing() {
        return createHearing("NEWCASTLE", "r1", "c1", now());
    }

    public HearingEntity createHearing(String courthouseName, String courtroomName, String caseNumber,
                                       LocalDate hearingDate) {
        createCourthouseUnlessExists(courthouseName);
        return retrieveCoreObjectService.retrieveOrCreateHearing(
            courthouseName,
            courtroomName,
            caseNumber,
            hearingDate
        );
    }

    public CourthouseEntity createCourthouseUnlessExists(String courthouseName) {
        return courthouseStub.createCourthouseUnlessExists(courthouseName);
    }

    public CourthouseEntity createCourthouseWithNameAndCode(String name, Integer code, String displayName) {
        var courthouse = CourthouseTestData.createCourthouse(name);
        courthouse.setCode(code);
        courthouse.setDisplayName(displayName);
        return courthouseRepository.save(courthouse);
    }

    @Transactional
    public CourthouseEntity createCourthouseWithTwoCourtrooms() {
        CourthouseEntity swanseaCourtEntity = createCourthouseWithNameAndCode("SWANSEA", 457, "Swansea");
        courtroomRepository.saveAndFlush(createCourtRoomWithNameAtCourthouse(swanseaCourtEntity, "1"));
        courtroomRepository.saveAndFlush(createCourtRoomWithNameAtCourthouse(swanseaCourtEntity, "2"));
        return swanseaCourtEntity;

    }

    @Transactional
    public void createDailyLists(CourthouseEntity courthouseEntity) throws IOException {
        DailyListEntity xhbDailyList = DailyListTestData.createDailyList(
            LocalTime.of(13, 0),
            String.valueOf(SourceType.XHB),
            courthouseEntity,
            "tests/dailyListProcessorTest/dailyListXHB.json"
        );

        DailyListEntity cppDailyList = DailyListTestData.createDailyList(
            LocalTime.of(13, 0),
            String.valueOf(SourceType.CPP),
            courthouseEntity,
            "tests/dailyListProcessorTest/dailyListCPP.json"
        );

        dailyListRepository.saveAllAndFlush(List.of(xhbDailyList, cppDailyList));
    }

    public MediaEntity createMediaEntity(OffsetDateTime startTime, OffsetDateTime endTime, int channel) {
        return mediaRepository.saveAndFlush(createMediaWith(startTime, endTime, channel));
    }


    public CourtroomEntity findCourtroomBy(String courthouseName, String courtroomName) {
        return courtroomRepository.findByCourthouseNameAndCourtroomName(courthouseName, courtroomName).orElse(null);
    }

    public CourthouseEntity findCourthouseWithName(String name) {
        return courthouseRepository.findByCourthouseNameIgnoreCase(name).get();
    }

    public ExternalLocationTypeEntity getExternalLocationTypeEntity(ExternalLocationTypeEnum externalLocationTypeEnum) {
        return externalLocationTypeRepository.getReferenceById(externalLocationTypeEnum.getId());
    }

    public ObjectRecordStatusEntity getObjectDirectoryStatusEntity(
        ObjectDirectoryStatusEnum objectDirectoryStatusEnum) {
        return objectDirectoryStatusRepository.getReferenceById(objectDirectoryStatusEnum.getId());
    }

    @Transactional
    public MediaRequestEntity createAndLoadOpenMediaRequestEntity(UserAccountEntity requestor, AudioRequestType audioRequestType) {

        HearingEntity hearing = createHearing("NEWCASTLE", "Int Test Courtroom 2", "2", LocalDate.of(2023, 6, 10));

        return save(
            AudioTestData.createCurrentMediaRequest(
                hearing,
                requestor,
                OffsetDateTime.parse("2023-06-26T13:00:00Z"),
                OffsetDateTime.parse("2023-06-26T13:45:00Z"),
                OffsetDateTime.parse("2023-06-30T13:00:00Z"),
                audioRequestType, OPEN
            ));
    }

    @Transactional
    public MediaRequestEntity createAndLoadNonAccessedCurrentMediaRequestEntity(UserAccountEntity requestor,
                                                                                AudioRequestType audioRequestType) {

        HearingEntity hearing = createHearing("NEWCASTLE", "Int Test Courtroom 2", "2", LocalDate.of(2023, 6, 10));

        MediaRequestEntity completedMediaRequest = AudioTestData.createCompletedMediaRequest(
            hearing,
            requestor,
            OffsetDateTime.parse("2023-06-26T13:00:00Z"),
            OffsetDateTime.parse("2023-06-26T14:00:00Z"), audioRequestType
        );
        save(completedMediaRequest);

        OffsetDateTime expiryTime = OffsetDateTime.of(2023, 7, 2, 13, 0, 0, 0, UTC);
        OffsetDateTime lastAccessed = OffsetDateTime.of(2023, 6, 30, 13, 0, 0, 0, UTC);
        transformedMediaStub.createTransformedMediaEntity(completedMediaRequest, "T20231010_0", expiryTime, lastAccessed);

        return completedMediaRequest;
    }

    public MediaRequestEntity createAndLoadExpiredMediaRequestEntity(HearingEntity hearing,
                                                                     UserAccountEntity requestor,
                                                                     AudioRequestType audioRequestType) {
        OffsetDateTime now = OffsetDateTime.now(UTC);
        return save(
            AudioTestData.createExpiredMediaRequest(
                hearing,
                requestor,
                now.minusDays(5),
                now.minusDays(4),
                audioRequestType
            ));
    }

    public MediaRequestEntity createAndLoadCompletedMediaRequestEntity(HearingEntity hearing,
                                                                       UserAccountEntity requestor,
                                                                       AudioRequestType audioRequestType) {
        MediaRequestEntity completedMediaRequest = AudioTestData.createCompletedMediaRequest(
            hearing,
            requestor,
            OffsetDateTime.parse("2023-06-26T13:00:00Z"),
            OffsetDateTime.parse("2023-06-26T13:45:00Z"),
            audioRequestType
        );
        return save(completedMediaRequest);
    }

    public MediaEntity addMediaToHearing(HearingEntity hearing, MediaEntity mediaEntity) {
        mediaRepository.save(mediaEntity);
        hearing.addMedia(mediaEntity);
        hearingRepository.save(hearing);
        return mediaEntity;
    }

    public ExternalObjectDirectoryEntity save(ExternalObjectDirectoryEntity externalObjectDirectoryEntity) {
        return externalObjectDirectoryRepository.save(externalObjectDirectoryEntity);
    }

    public CourtCaseEntity save(CourtCaseEntity courtCaseEntity) {
        return caseRepository.save(courtCaseEntity);
    }

    public CourthouseEntity save(CourthouseEntity courthouseEntity) {
        return courthouseRepository.save(courthouseEntity);
    }

    public CourtroomEntity save(CourtroomEntity courtroom) {
        return courtroomRepository.save(courtroom);
    }

    public MediaRequestEntity save(MediaRequestEntity mediaRequestEntity) {
        return mediaRequestRepository.saveAndFlush(mediaRequestEntity);
    }

    public MediaEntity save(MediaEntity media) {
        return mediaRepository.save(media);
    }

    public JudgeEntity save(JudgeEntity judge) {
        return judgeRepository.save(judge);
    }

    @Transactional
    public HearingEntity save(HearingEntity hearingEntity) {
        return hearingRepository.saveAndFlush(hearingEntity);
    }

    public TranscriptionEntity save(TranscriptionEntity transcriptionEntity) {
        return transcriptionRepository.saveAndFlush(transcriptionEntity);
    }

    public TranscriptionWorkflowEntity save(TranscriptionWorkflowEntity transcriptionWorkflowEntity) {
        return transcriptionWorkflowRepository.saveAndFlush(transcriptionWorkflowEntity);
    }

    public void save(TranscriptionWorkflowEntity... transcriptionWorkflowEntity) {
        transcriptionWorkflowRepository.saveAllAndFlush(asList(transcriptionWorkflowEntity));
    }

    public void save(TranscriptionCommentEntity... transcriptionCommentEntities) {
        transcriptionCommentRepository.saveAllAndFlush(asList(transcriptionCommentEntities));
    }

    public void saveAll(HearingEntity... hearingEntities) {
        hearingRepository.saveAllAndFlush(asList(hearingEntities));
    }

    public void saveAll(EventEntity... eventEntities) {
        eventRepository.saveAll(asList(eventEntities));
    }

    public void saveAll(EventHandlerEntity... eventHandlerEntities) {
        eventHandlerRepository.saveAll(asList(eventHandlerEntities));
    }

    public void saveAll(UserAccountEntity... testUsers) {
        stream(testUsers).forEach(user -> {
            UserAccountEntity systemUser = userAccountRepository.getReferenceById(0);
            user.setCreatedBy(systemUser);
            user.setLastModifiedBy(systemUser);
        });
        userAccountRepository.saveAll(asList(testUsers));
    }

    public void addToTrash(EventHandlerEntity... eventHandlerEntities) {
        this.eventHandlerBin.addAll(asList(eventHandlerEntities));
    }

    public void addToUserAccountTrash(String... emailAddresses) {
        stream(emailAddresses)
            .flatMap(email -> userAccountRepository.findByEmailAddressIgnoreCase(email).stream())
            .forEach(userAccountBin::add);
    }

    public void createTestUserAccount() {
        if (userAccountRepository.findByEmailAddressIgnoreCase("test.user@example.com").isEmpty()) {
            UserAccountEntity testUser = new UserAccountEntity();
            testUser.setEmailAddress("test.user@example.com");
            testUser.setUserName("testuser");
            testUser.setAccountGuid(UUID.randomUUID().toString());
            testUser.setIsSystemUser(false);
            testUser.setActive(true);
            userAccountRepository.saveAndFlush(testUser);
        }
    }
}
