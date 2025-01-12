package uk.gov.hmcts.darts.audio.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.darts.audiorequests.model.AudioRequestType;
import uk.gov.hmcts.darts.audit.api.AuditActivity;
import uk.gov.hmcts.darts.audit.model.AuditSearchQuery;
import uk.gov.hmcts.darts.audit.service.AuditService;
import uk.gov.hmcts.darts.authorisation.component.Authorisation;
import uk.gov.hmcts.darts.authorisation.component.UserIdentity;
import uk.gov.hmcts.darts.common.entity.AuditEntity;
import uk.gov.hmcts.darts.common.entity.UserAccountEntity;
import uk.gov.hmcts.darts.datamanagement.service.DataManagementService;
import uk.gov.hmcts.darts.testutils.IntegrationBase;
import uk.gov.hmcts.darts.testutils.stubs.AuthorisationStub;
import uk.gov.hmcts.darts.testutils.stubs.TransientObjectDirectoryStub;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.darts.audiorequests.model.AudioRequestType.PLAYBACK;
import static uk.gov.hmcts.darts.common.enums.ObjectDirectoryStatusEnum.STORED;
import static uk.gov.hmcts.darts.common.enums.SecurityRoleEnum.TRANSCRIBER;

@AutoConfigureMockMvc
@SuppressWarnings({"PMD.ExcessiveImports"})
class AudioRequestsControllerDownloadIntTest extends IntegrationBase {

    private static final URI ENDPOINT = URI.create("/audio-requests/download");

    private static final Integer DOWNLOAD_AUDIT_ACTIVITY_ID = AuditActivity.EXPORT_AUDIO.getId();
    @MockBean
    private Authorisation authorisation;

    @MockBean
    private UserIdentity mockUserIdentity;

    @Autowired
    protected TransientObjectDirectoryStub transientObjectDirectoryStub;

    @Autowired
    private AuthorisationStub authorisationStub;

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private DataManagementService dataManagementService;

    @Autowired
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        UserAccountEntity testUser = dartsDatabase.getUserAccountStub().getIntegrationTestUserAccountEntity();
        when(mockUserIdentity.getUserAccount()).thenReturn(testUser);
    }

    @Test
    void audioRequestDownloadShouldDownloadFromOutboundStorageAndReturnSuccess() throws Exception {
        var blobId = UUID.randomUUID();

        var requestor = dartsDatabase.getUserAccountStub().getIntegrationTestUserAccountEntity();
        var mediaRequestEntity = dartsDatabase.createAndLoadOpenMediaRequestEntity(requestor, AudioRequestType.DOWNLOAD);
        var objectDirectoryStatusEntity = dartsDatabase.getObjectDirectoryStatusEntity(STORED);

        dartsDatabase.getTransientObjectDirectoryRepository()
            .saveAndFlush(transientObjectDirectoryStub.createTransientObjectDirectoryEntity(
                mediaRequestEntity,
                objectDirectoryStatusEntity,
                blobId
            ));

        doNothing().when(authorisation)
            .authoriseByMediaRequestId(mediaRequestEntity.getId(), Set.of(TRANSCRIBER));

        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT)
            .queryParam("media_request_id", String.valueOf(mediaRequestEntity.getId()));

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk());

        verify(dataManagementService).getBlobData(eq("darts-outbound"), any());

        verify(authorisation, times(1)).authoriseByMediaRequestId(
            mediaRequestEntity.getId(),
            Set.of(TRANSCRIBER)
        );

        AuditSearchQuery searchQuery = new AuditSearchQuery();
        searchQuery.setCaseId(mediaRequestEntity.getHearing().getCourtCase().getId());
        searchQuery.setFromDate(OffsetDateTime.now().minusDays(1));
        searchQuery.setToDate(OffsetDateTime.now().plusDays(1));
        searchQuery.setAuditActivityId(DOWNLOAD_AUDIT_ACTIVITY_ID);

        List<AuditEntity> auditEntities = auditService.search(searchQuery);
        assertEquals("2", auditEntities.get(0).getCourtCase().getCaseNumber());
        assertEquals(1, auditEntities.size());

    }

    @Test
    @Transactional
    void audioRequestDownloadGetShouldReturnBadRequestWhenMediaRequestEntityIsPlayback() throws Exception {
        authorisationStub.givenTestSchema();

        var mediaRequestEntity = authorisationStub.getMediaRequestEntity();
        mediaRequestEntity.setRequestType(PLAYBACK);
        dartsDatabase.save(mediaRequestEntity);

        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT)
            .queryParam("media_request_id", String.valueOf(authorisationStub.getMediaRequestEntity().getId()));

        doNothing().when(authorisation)
            .authoriseByMediaRequestId(authorisationStub.getMediaRequestEntity().getId(), Set.of(TRANSCRIBER));

        mockMvc.perform(requestBuilder)
            .andExpect(header().string("Content-Type", "application/problem+json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("AUDIO_REQUESTS_102"));

        verify(authorisation, times(1)).authoriseByMediaRequestId(
            authorisationStub.getMediaRequestEntity().getId(),
            Set.of(TRANSCRIBER)
        );
    }

    @Test
    @Transactional
    void audioRequestDownloadGetShouldReturnErrorWhenNoRelatedTransientObjectExistsInDatabase() throws Exception {
        authorisationStub.givenTestSchema();

        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT)
            .queryParam("media_request_id", String.valueOf(authorisationStub.getMediaRequestEntity().getId()));

        doNothing().when(authorisation)
            .authoriseByMediaRequestId(authorisationStub.getMediaRequestEntity().getId(), Set.of(TRANSCRIBER));

        mockMvc.perform(requestBuilder)
            .andExpect(header().string("Content-Type", "application/problem+json"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.type").value("AUDIO_101"));

        verify(authorisation, times(1)).authoriseByMediaRequestId(
            authorisationStub.getMediaRequestEntity().getId(),
            Set.of(TRANSCRIBER)
        );
    }

    @Test
    void audioDownloadGetShouldReturnBadRequestWhenNoRequestBodyIsProvided() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(ENDPOINT);

        mockMvc.perform(requestBuilder)
            .andExpect(header().string("Content-Type", "application/problem+json"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authorisation);
    }

}
