package uk.gov.hmcts.darts.testutils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.darts.cases.repository.CaseRepository;
import uk.gov.hmcts.darts.cases.repository.ReportingRestrictionsRepository;
import uk.gov.hmcts.darts.common.entity.CaseEntity;
import uk.gov.hmcts.darts.common.entity.CourthouseEntity;
import uk.gov.hmcts.darts.common.entity.CourtroomEntity;
import uk.gov.hmcts.darts.common.entity.EventEntity;
import uk.gov.hmcts.darts.common.entity.HearingEntity;
import uk.gov.hmcts.darts.common.repository.CourtroomRepository;
import uk.gov.hmcts.darts.common.repository.EventRepository;
import uk.gov.hmcts.darts.common.repository.ExternalObjectDirectoryRepository;
import uk.gov.hmcts.darts.common.repository.HearingRepository;
import uk.gov.hmcts.darts.common.repository.MediaRepository;
import uk.gov.hmcts.darts.courthouse.CourthouseRepository;
import uk.gov.hmcts.darts.notification.entity.NotificationEntity;
import uk.gov.hmcts.darts.notification.repository.NotificationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.darts.common.util.CommonTestDataUtil.createCase;
import static uk.gov.hmcts.darts.common.util.CommonTestDataUtil.createCourtroom;
import static uk.gov.hmcts.darts.common.util.CommonTestDataUtil.createHearing;

@Service
@AllArgsConstructor
@SuppressWarnings("PMD.TooManyMethods")
public class DartsDatabaseStub {

    private final CaseRepository caseRepository;
    private final EventRepository eventRepository;
    private final ReportingRestrictionsRepository reportingRestrictionsRepository;
    private final CourthouseRepository courthouseRepository;
    private final HearingRepository hearingRepository;
    private final CourtroomRepository courtroomRepository;
    private final MediaRepository mediaRepository;
    private final ExternalObjectDirectoryRepository externalObjectDirectoryRepository;
    private final NotificationRepository notificationRepository;

    public void clearDatabase() {
        notificationRepository.deleteAll();
        hearingRepository.deleteAll();
        eventRepository.deleteAll();
        externalObjectDirectoryRepository.deleteAll();
        mediaRepository.deleteAll();
        courtroomRepository.deleteAll();
        caseRepository.deleteAll();
        courthouseRepository.deleteAll();
    }

    public void save(CaseEntity minimalCaseEntity) {
        caseRepository.save(minimalCaseEntity);
    }

    public Optional<CaseEntity> findByCaseByCaseNumberAndCourtHouseName(String someCaseNumber, String someCourthouse) {
        return caseRepository.findByCaseNumberAndCourthouse_CourthouseName(someCaseNumber, someCourthouse);
    }

    public List<HearingEntity> findByCourthouseCourtroomAndDate(String someCourthouse, String someRoom, LocalDate toLocalDate) {
        return hearingRepository.findByCourthouseCourtroomAndDate(someCourthouse, someRoom, toLocalDate);
    }

    public List<EventEntity> getAllEvents() {
        return eventRepository.findAll();
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
        var caseEntity = givenTheDatabaseContainsCourtCaseAndCourthouseWithRoom(caseNumber, courthouseName, courtroomName);
        var courtroomEntity = courtroomRepository.findByNames(courthouseName, courtroomName);
        var hearingEntity = new HearingEntity();
        hearingEntity.setHearingIsActual(true);
        hearingEntity.setHearingDate(hearingDate);
        hearingEntity.setCourtCase(caseEntity);
        hearingEntity.setCourtroom(courtroomEntity);
        return hearingRepository.saveAndFlush(hearingEntity);
    }

    @Transactional
    public CaseEntity givenTheDatabaseContainsCourtCaseAndCourthouseWithRoom(String caseNumber, String courthouseName, String courtroomName) {
        var courtroom = givenTheDatabaseContainsCourthouseWithRoom(courthouseName, courtroomName);
        var caseEntity = minimalCaseEntity();
        caseEntity.setCaseNumber(caseNumber);
        caseEntity.setCourthouse(courtroom.getCourthouse());
        return caseRepository.saveAndFlush(caseEntity);
    }

    @Transactional
    public CourtroomEntity givenTheDatabaseContainsCourthouseWithRoom(String courthouseName, String courtroomName) {
        var courthouse = new CourthouseEntity();
        courthouse.setCourthouseName(courthouseName);
        var persistedCourthouse = courthouseRepository.saveAndFlush(courthouse);

        var courtroom = new CourtroomEntity();
        courtroom.setName(courtroomName);
        courtroom.setCourthouse(persistedCourthouse);
        courtroomRepository.saveAndFlush(courtroom);
        return courtroom;
    }

    public static CaseEntity minimalCaseEntity() {
        var caseEntity = new CaseEntity();
        caseEntity.setCaseNumber("1");
        caseEntity.setCourthouse(minimalCourtHouse());
        return caseEntity;
    }

    public static CourthouseEntity minimalCourtHouse() {
        var courtHouse = new CourthouseEntity();
        courtHouse.setCourthouseName("some-courthouse");
        return courtHouse;
    }

    public static HearingEntity minimalHearing() {
        var courtroom = minimalCourtroom();
        var caseEntity = minimalCaseEntity();
        caseEntity.setCourthouse(courtroom.getCourthouse());

        HearingEntity hearing = new HearingEntity();
        hearing.setCourtCase(caseEntity);
        hearing.setCourtroom(courtroom);
        hearing.setHearingDate(now());
        return hearing;
    }

    public static CourtroomEntity minimalCourtroom() {
        CourtroomEntity courtroom = new CourtroomEntity();
        courtroom.setCourthouse(minimalCourtHouse());
        courtroom.setName("some-court-room");
        return courtroom;
    }

    public CaseEntity hasSomeCourtCase() {
        return caseRepository.save(minimalCaseEntity());
    }

    public List<NotificationEntity> getNotificationsForCase(Integer caseId) {
        return notificationRepository.findByCourtCase_Id(caseId);
    }

    @Transactional
    public HearingEntity hasSomeHearing() {
        return hearingRepository.saveAndFlush(createHearing(createCase("c1"), createCourtroom("r1"), now()));
    }
}