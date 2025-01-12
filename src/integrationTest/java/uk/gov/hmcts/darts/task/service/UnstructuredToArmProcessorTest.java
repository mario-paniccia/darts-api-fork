package uk.gov.hmcts.darts.task.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.darts.arm.api.ArmDataManagementApi;
import uk.gov.hmcts.darts.arm.config.ArmDataManagementConfiguration;
import uk.gov.hmcts.darts.arm.service.UnstructuredToArmProcessor;
import uk.gov.hmcts.darts.arm.service.impl.UnstructuredToArmProcessorImpl;
import uk.gov.hmcts.darts.common.entity.ExternalObjectDirectoryEntity;
import uk.gov.hmcts.darts.common.entity.HearingEntity;
import uk.gov.hmcts.darts.common.entity.MediaEntity;
import uk.gov.hmcts.darts.common.enums.ExternalLocationTypeEnum;
import uk.gov.hmcts.darts.common.repository.ExternalLocationTypeRepository;
import uk.gov.hmcts.darts.common.repository.ExternalObjectDirectoryRepository;
import uk.gov.hmcts.darts.common.repository.ObjectDirectoryStatusRepository;
import uk.gov.hmcts.darts.common.repository.UserAccountRepository;
import uk.gov.hmcts.darts.datamanagement.api.DataManagementApi;
import uk.gov.hmcts.darts.testutils.IntegrationBase;
import uk.gov.hmcts.darts.testutils.data.MediaTestData;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.darts.common.enums.ObjectDirectoryStatusEnum.FAILURE_ARM_INGESTION_FAILED;
import static uk.gov.hmcts.darts.common.enums.ObjectDirectoryStatusEnum.MARKED_FOR_DELETION;
import static uk.gov.hmcts.darts.common.enums.ObjectDirectoryStatusEnum.STORED;

@SpringBootTest
@ActiveProfiles({"intTest", "h2db"})
@Transactional
class UnstructuredToArmProcessorTest extends IntegrationBase {

    public static final LocalDate HEARING_DATE = LocalDate.of(2023, 6, 10);
    UnstructuredToArmProcessor unstructuredToArmProcessor;
    @MockBean
    private ArmDataManagementApi armDataManagementApi;

    @Autowired
    private ExternalObjectDirectoryRepository externalObjectDirectoryRepository;
    @Autowired
    private ObjectDirectoryStatusRepository objectDirectoryStatusRepository;
    @Autowired
    private ExternalLocationTypeRepository externalLocationTypeRepository;
    @Autowired
    private DataManagementApi dataManagementApi;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private ArmDataManagementConfiguration armDataManagementConfiguration;


    @BeforeEach
    void setupData() {
        unstructuredToArmProcessor = new UnstructuredToArmProcessorImpl(externalObjectDirectoryRepository,
                                                                        objectDirectoryStatusRepository,
                                                                        externalLocationTypeRepository,
                                                                        dataManagementApi,
                                                                        armDataManagementApi,
                                                                        userAccountRepository,
                                                                        armDataManagementConfiguration);
    }

    @Test
    void movePendingDataFromUnstructuredToArmStorage() {
        HearingEntity hearing = dartsDatabase.createHearing(
            "NEWCASTLE",
            "Int Test Courtroom 2",
            "2",
            HEARING_DATE
        );

        MediaEntity savedMedia = dartsDatabase.save(
            MediaTestData.createMediaWith(
                hearing.getCourtroom(),
                OffsetDateTime.parse("2023-09-26T13:00:00Z"),
                OffsetDateTime.parse("2023-09-26T13:45:00Z"),
                1
            ));

        ExternalObjectDirectoryEntity unstructuredEod = dartsDatabase.getExternalObjectDirectoryStub().createExternalObjectDirectory(
            savedMedia,
            dartsDatabase.getObjectDirectoryStatusEntity(STORED),
            dartsDatabase.getExternalLocationTypeEntity(ExternalLocationTypeEnum.UNSTRUCTURED),
            UUID.randomUUID()
        );
        dartsDatabase.save(unstructuredEod);

        unstructuredToArmProcessor.processUnstructuredToArm();

        List<ExternalObjectDirectoryEntity> foundMediaList = dartsDatabase.getExternalObjectDirectoryRepository().findByMediaAndExternalLocationType(
            savedMedia,
            dartsDatabase.getExternalLocationTypeEntity(ExternalLocationTypeEnum.ARM)
        );

        assertEquals(1, foundMediaList.size());
        ExternalObjectDirectoryEntity foundMedia = foundMediaList.get(0);
        assertEquals(MARKED_FOR_DELETION.getId(), foundMedia.getStatus().getId());
    }

    @Test
    void movePreviousFailedAttemptFromUnstructuredToArmStorage() {
        HearingEntity hearing = dartsDatabase.createHearing(
            "NEWCASTLE",
            "Int Test Courtroom 2",
            "2",
            HEARING_DATE
        );

        MediaEntity savedMedia = dartsDatabase.save(
            MediaTestData.createMediaWith(
                hearing.getCourtroom(),
                OffsetDateTime.parse("2023-09-26T13:00:00Z"),
                OffsetDateTime.parse("2023-09-26T13:45:00Z"),
                1
            ));

        ExternalObjectDirectoryEntity unstructuredEod = dartsDatabase.getExternalObjectDirectoryStub().createExternalObjectDirectory(
            savedMedia,
            dartsDatabase.getObjectDirectoryStatusEntity(STORED),
            dartsDatabase.getExternalLocationTypeEntity(ExternalLocationTypeEnum.UNSTRUCTURED),
            UUID.randomUUID()
        );
        dartsDatabase.save(unstructuredEod);

        ExternalObjectDirectoryEntity armEod = dartsDatabase.getExternalObjectDirectoryStub().createExternalObjectDirectory(
            savedMedia,
            dartsDatabase.getObjectDirectoryStatusEntity(FAILURE_ARM_INGESTION_FAILED),
            dartsDatabase.getExternalLocationTypeEntity(ExternalLocationTypeEnum.ARM),
            UUID.randomUUID()
        );

        armEod.setTransferAttempts(1);
        dartsDatabase.save(armEod);

        unstructuredToArmProcessor.processUnstructuredToArm();

        List<ExternalObjectDirectoryEntity> foundMediaList = dartsDatabase.getExternalObjectDirectoryRepository().findByMediaAndExternalLocationType(
            savedMedia,
            dartsDatabase.getExternalLocationTypeEntity(ExternalLocationTypeEnum.ARM)
        );

        assertEquals(1, foundMediaList.size());
        ExternalObjectDirectoryEntity foundMedia = foundMediaList.get(0);
        assertEquals(MARKED_FOR_DELETION.getId(), foundMedia.getStatus().getId());
    }

    @Test
    void updateTransferAttemptIfUnableToFindUnstructuredRecordFromFailedArmEod() {
        HearingEntity hearing = dartsDatabase.createHearing(
            "NEWCASTLE",
            "Int Test Courtroom 2",
            "2",
            HEARING_DATE
        );

        MediaEntity savedMedia = dartsDatabase.save(
            MediaTestData.createMediaWith(
                hearing.getCourtroom(),
                OffsetDateTime.parse("2023-09-26T13:00:00Z"),
                OffsetDateTime.parse("2023-09-26T13:45:00Z"),
                1
            ));

        ExternalObjectDirectoryEntity armEod = dartsDatabase.getExternalObjectDirectoryStub().createExternalObjectDirectory(
            savedMedia,
            dartsDatabase.getObjectDirectoryStatusEntity(STORED),
            dartsDatabase.getExternalLocationTypeEntity(ExternalLocationTypeEnum.ARM),
            UUID.randomUUID()
        );
        armEod.setStatus(dartsDatabase.getObjectDirectoryStatusRepository().getReferenceById(FAILURE_ARM_INGESTION_FAILED.getId()));
        armEod.setTransferAttempts(1);
        dartsDatabase.save(armEod);

        unstructuredToArmProcessor.processUnstructuredToArm();

        List<ExternalObjectDirectoryEntity> foundMediaList = dartsDatabase.getExternalObjectDirectoryRepository().findByMediaAndExternalLocationType(
            savedMedia,
            dartsDatabase.getExternalLocationTypeEntity(ExternalLocationTypeEnum.ARM)
        );

        assertEquals(1, foundMediaList.size());
        ExternalObjectDirectoryEntity foundMedia = foundMediaList.get(0);
        assertEquals(FAILURE_ARM_INGESTION_FAILED.getId(), foundMedia.getStatus().getId());
        assertEquals(2, foundMedia.getTransferAttempts());
    }
}
