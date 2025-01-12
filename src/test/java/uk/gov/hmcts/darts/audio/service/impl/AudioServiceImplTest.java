package uk.gov.hmcts.darts.audio.service.impl;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.darts.audio.component.AddAudioRequestMapper;
import uk.gov.hmcts.darts.audio.exception.AudioApiError;
import uk.gov.hmcts.darts.audio.model.AddAudioMetadataRequest;
import uk.gov.hmcts.darts.audio.model.AudioFileInfo;
import uk.gov.hmcts.darts.audio.service.AudioOperationService;
import uk.gov.hmcts.darts.audio.service.AudioService;
import uk.gov.hmcts.darts.audio.service.AudioTransformationService;
import uk.gov.hmcts.darts.audit.service.AuditService;
import uk.gov.hmcts.darts.authorisation.component.UserIdentity;
import uk.gov.hmcts.darts.common.entity.CourthouseEntity;
import uk.gov.hmcts.darts.common.entity.CourtroomEntity;
import uk.gov.hmcts.darts.common.entity.HearingEntity;
import uk.gov.hmcts.darts.common.entity.MediaEntity;
import uk.gov.hmcts.darts.common.exception.DartsApiException;
import uk.gov.hmcts.darts.common.repository.ExternalLocationTypeRepository;
import uk.gov.hmcts.darts.common.repository.ExternalObjectDirectoryRepository;
import uk.gov.hmcts.darts.common.repository.HearingRepository;
import uk.gov.hmcts.darts.common.repository.MediaRepository;
import uk.gov.hmcts.darts.common.repository.ObjectDirectoryStatusRepository;
import uk.gov.hmcts.darts.common.repository.TransientObjectDirectoryRepository;
import uk.gov.hmcts.darts.common.service.FileOperationService;
import uk.gov.hmcts.darts.common.service.RetrieveCoreObjectService;
import uk.gov.hmcts.darts.common.util.FileContentChecksum;
import uk.gov.hmcts.darts.datamanagement.api.DataManagementApi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports"})
class AudioServiceImplTest {

    public static final OffsetDateTime STARTED_AT = OffsetDateTime.now().minusHours(1);
    public static final OffsetDateTime ENDED_AT = OffsetDateTime.now();
    private static final String DUMMY_FILE_CONTENT = "DUMMY FILE CONTENT";

    private static final OffsetDateTime START_TIME = OffsetDateTime.parse("2023-01-01T12:00:00Z");
    private static final OffsetDateTime END_TIME = OffsetDateTime.parse("2023-01-01T13:00:00Z");
    @Mock
    AddAudioRequestMapper mapper;

    @Captor
    ArgumentCaptor<MediaEntity> mediaEntityArgumentCaptor;
    @Captor
    ArgumentCaptor<BinaryData> inboundBlobStorageArgumentCaptor;
    @Mock
    private AudioTransformationService audioTransformationService;

    @Mock
    private TransientObjectDirectoryRepository transientObjectDirectoryRepository;

    @Mock
    private AudioOperationService audioOperationService;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private FileOperationService fileOperationService;
    @Mock
    private RetrieveCoreObjectService retrieveCoreObjectService;
    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private UserIdentity userIdentity;
    @Mock
    private ExternalObjectDirectoryRepository externalObjectDirectoryRepository;
    @Mock
    private ExternalLocationTypeRepository externalLocationTypeRepository;
    @Mock
    private ObjectDirectoryStatusRepository objectDirectoryStatusRepository;
    @Mock
    private DataManagementApi dataManagementApi;
    @Mock
    private FileContentChecksum fileContentChecksum;
    private AudioService audioService;

    @Mock
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        audioService = new AudioServiceImpl(
            audioTransformationService,
            externalObjectDirectoryRepository,
            objectDirectoryStatusRepository,
            externalLocationTypeRepository,
            mediaRepository,
            audioOperationService,
            fileOperationService,
            retrieveCoreObjectService,
            hearingRepository,
            mapper,
            dataManagementApi,
            userIdentity,
            fileContentChecksum
        );
    }

    @Test
    void previewShouldReturnExpectedData() throws IOException, ExecutionException, InterruptedException {

        MediaEntity mediaEntity = new MediaEntity();
        mediaEntity.setId(1);
        mediaEntity.setStart(START_TIME);
        mediaEntity.setEnd(END_TIME);
        mediaEntity.setChannel(1);

        Path mediaPath = Path.of("/path/to/audio/sample2-5secs.mp2");
        when(mediaRepository.findById(1)).thenReturn(Optional.of(mediaEntity));
        when(audioTransformationService.saveMediaToWorkspace(mediaEntity)).thenReturn(mediaPath);

        AudioFileInfo audioFileInfo = new AudioFileInfo(START_TIME.toInstant(), END_TIME.toInstant(), "test", 1);
        when(audioOperationService.reEncode(anyString(), any())).thenReturn(audioFileInfo);

        byte[] testStringInBytes = DUMMY_FILE_CONTENT.getBytes(StandardCharsets.UTF_8);
        BinaryData data = BinaryData.fromBytes(testStringInBytes);
        when(fileOperationService.saveFileToBinaryData(any())).thenReturn(data);

        try (InputStream inputStream = audioService.preview(mediaEntity.getId())) {
            byte[] bytes = inputStream.readAllBytes();
            assertEquals(DUMMY_FILE_CONTENT, new String(bytes));
        }
    }

    @Test
    void previewShouldThrowExceptionWhenMediaIdCannotBeFound() {

        var mediaRequestId = 1;

        MediaEntity mediaEntity = new MediaEntity();
        mediaEntity.setId(mediaRequestId);
        mediaEntity.setStart(START_TIME);
        mediaEntity.setEnd(END_TIME);
        mediaEntity.setChannel(1);

        when(mediaRepository.findById(mediaRequestId)).thenReturn(Optional.empty());

        var exception = assertThrows(
            DartsApiException.class,
            () -> audioService.preview(mediaRequestId)
        );

        assertEquals(AudioApiError.REQUESTED_DATA_CANNOT_BE_LOCATED, exception.getError());
    }


    @Test
    void addAudio() throws IOException {
        OffsetDateTime startedAt = OffsetDateTime.now().minusHours(1);
        OffsetDateTime endedAt = OffsetDateTime.now();

        AddAudioMetadataRequest addAudioMetadataRequest = createAddAudioRequest(startedAt, endedAt);
        HearingEntity hearingEntity = new HearingEntity();
        when(retrieveCoreObjectService.retrieveOrCreateHearing(
            anyString(),
            anyString(),
            anyString(),
            any()
        )).thenReturn(hearingEntity);
        MediaEntity mediaEntity = createMediaEntity(startedAt, endedAt);

        MockMultipartFile audioFile = new MockMultipartFile(
            "addAudio",
            "audio_sample.mp2",
            "audio/mpeg",
            DUMMY_FILE_CONTENT.getBytes()
        );

        when(mapper.mapToMedia(any())).thenReturn(mediaEntity);
        audioService.addAudio(audioFile, addAudioMetadataRequest);

        verify(dataManagementApi).saveBlobDataToInboundContainer(inboundBlobStorageArgumentCaptor.capture());
        var binaryData = inboundBlobStorageArgumentCaptor.getValue();
        assertEquals(BinaryData.fromStream(audioFile.getInputStream()).toString(),binaryData.toString());


        verify(mediaRepository).save(mediaEntityArgumentCaptor.capture());
        verify(hearingRepository, times(3)).saveAndFlush(any());
        MediaEntity savedMedia = mediaEntityArgumentCaptor.getValue();
        assertEquals(startedAt, savedMedia.getStart());
        assertEquals(endedAt, savedMedia.getEnd());
        assertEquals(1, savedMedia.getChannel());
        assertEquals(2, savedMedia.getTotalChannels());
        assertEquals("SWANSEA", savedMedia.getCourtroom().getCourthouse().getCourthouseName());
        assertEquals("1", savedMedia.getCourtroom().getName());

    }

    private MediaEntity createMediaEntity(OffsetDateTime startedAt, OffsetDateTime endedAt) {
        MediaEntity mediaEntity = new MediaEntity();
        mediaEntity.setStart(startedAt);
        mediaEntity.setEnd(endedAt);
        mediaEntity.setChannel(1);
        mediaEntity.setTotalChannels(2);
        CourthouseEntity courthouse = new CourthouseEntity();
        courthouse.setCourthouseName("SWANSEA");
        mediaEntity.setCourtroom(new CourtroomEntity(1, "1", courthouse));
        return mediaEntity;
    }

    private AddAudioMetadataRequest createAddAudioRequest(OffsetDateTime startedAt, OffsetDateTime endedAt) {
        AddAudioMetadataRequest addAudioMetadataRequest = new AddAudioMetadataRequest();
        addAudioMetadataRequest.startedAt(startedAt);
        addAudioMetadataRequest.endedAt(endedAt);
        addAudioMetadataRequest.setChannel(1);
        addAudioMetadataRequest.totalChannels(2);
        addAudioMetadataRequest.format("mp3");
        addAudioMetadataRequest.filename("test");
        addAudioMetadataRequest.courthouse("SWANSEA");
        addAudioMetadataRequest.courtroom("1");
        addAudioMetadataRequest.cases(List.of("1", "2", "3"));
        return addAudioMetadataRequest;
    }

    @Test
    void linkAudioAndHearing() {
        AddAudioMetadataRequest addAudioMetadataRequest = createAddAudioRequest(STARTED_AT, ENDED_AT);
        MediaEntity mediaEntity = createMediaEntity(STARTED_AT, ENDED_AT);

        HearingEntity hearing = new HearingEntity();
        when(retrieveCoreObjectService.retrieveOrCreateHearing(
            anyString(),
            anyString(),
            anyString(),
            any()
        )).thenReturn(hearing);
        audioService.linkAudioAndHearing(addAudioMetadataRequest, mediaEntity);
        verify(hearingRepository, times(3)).saveAndFlush(any());
        assertEquals(3, hearing.getMediaList().size());
    }
}
