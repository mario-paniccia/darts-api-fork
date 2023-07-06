package uk.gov.hmcts.darts.audio.service.impl;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.darts.common.entity.MediaEntity;
import uk.gov.hmcts.darts.common.repository.MediaRepository;
import uk.gov.hmcts.darts.datamanagement.config.DataManagementConfiguration;
import uk.gov.hmcts.darts.datamanagement.service.DataManagementService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AudioTransformationServiceImplTest {

    private static final UUID BLOB_LOCATION = UUID.randomUUID();
    private static final String TEST_BINARY_STRING = "Test String to be converted to binary!";
    private static final BinaryData BINARY_DATA = BinaryData.fromBytes(TEST_BINARY_STRING.getBytes());

    @Mock
    private DataManagementService dataManagementService;
    @Mock
    private DataManagementConfiguration dataManagementConfiguration;

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private AudioTransformationServiceImpl audioTransformationService;

    @Test
    void testGetAudioBlobData() {
        when(dataManagementService.getBlobData(dataManagementConfiguration.getUnstructuredContainerName(), BLOB_LOCATION)).thenReturn(BINARY_DATA);
        BinaryData binaryData = audioTransformationService.getAudioBlobData(BLOB_LOCATION);
        assertEquals(BINARY_DATA, binaryData);
    }

    @Test
    void getMediaMetadataShouldReturnRepositoryResultsUnmodifiedWhenRepositoryHasResult() {
        List<MediaEntity> expectedResults = Collections.singletonList(new MediaEntity());

        when(mediaRepository.findAllByHearingId(any()))
            .thenReturn(expectedResults);

        List<MediaEntity> mediaEntities = audioTransformationService.getMediaMetadata(1);

        assertEquals(expectedResults, mediaEntities);
    }

    @Test
    void getMediaMetadataShouldReturnRepositoryResultsUnmodifiedWhenRepositoryResultIsEmpty() {
        List<MediaEntity> expectedResults = Collections.emptyList();

        when(mediaRepository.findAllByHearingId(any()))
            .thenReturn(expectedResults);

        List<MediaEntity> mediaEntities = audioTransformationService.getMediaMetadata(1);

        assertEquals(expectedResults, mediaEntities);
    }

}