package uk.gov.hmcts.darts.transcriptions.mapper;

import lombok.experimental.UtilityClass;
import uk.gov.hmcts.darts.common.entity.CourtCaseEntity;
import uk.gov.hmcts.darts.common.entity.EventHandlerEntity;
import uk.gov.hmcts.darts.common.entity.TranscriptionDocumentEntity;
import uk.gov.hmcts.darts.common.entity.TranscriptionEntity;
import uk.gov.hmcts.darts.common.entity.TranscriptionTypeEntity;
import uk.gov.hmcts.darts.common.entity.TranscriptionUrgencyEntity;
import uk.gov.hmcts.darts.common.exception.DartsApiException;
import uk.gov.hmcts.darts.transcriptions.enums.TranscriptionStatusEnum;
import uk.gov.hmcts.darts.transcriptions.exception.TranscriptionApiError;
import uk.gov.hmcts.darts.transcriptions.model.GetTranscriptionByIdResponse;
import uk.gov.hmcts.darts.transcriptions.model.TranscriptionTypeResponse;
import uk.gov.hmcts.darts.transcriptions.model.TranscriptionUrgencyResponse;
import uk.gov.hmcts.darts.transcriptions.util.TranscriptionUtil;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@UtilityClass
public class TranscriptionResponseMapper {

    public List<TranscriptionTypeResponse> mapToTranscriptionTypeResponses(List<TranscriptionTypeEntity> transcriptionTypeEntities) {
        return emptyIfNull(transcriptionTypeEntities).stream()
            .map(TranscriptionResponseMapper::mapToTranscriptionTypeResponse)
            .collect(Collectors.toList());
    }

    TranscriptionTypeResponse mapToTranscriptionTypeResponse(TranscriptionTypeEntity transcriptionTypeEntity) {
        TranscriptionTypeResponse transcriptionTypeResponse = new TranscriptionTypeResponse();
        transcriptionTypeResponse.setTranscriptionTypeId(transcriptionTypeEntity.getId());
        transcriptionTypeResponse.setDescription(transcriptionTypeEntity.getDescription());
        return transcriptionTypeResponse;
    }

    public List<TranscriptionUrgencyResponse> mapToTranscriptionUrgencyResponses(
        List<TranscriptionUrgencyEntity> transcriptionUrgencyEntities) {
        return emptyIfNull(transcriptionUrgencyEntities).stream()
            .map(TranscriptionResponseMapper::mapToTranscriptionUrgencyResponse)
            .collect(Collectors.toList());
    }

    TranscriptionUrgencyResponse mapToTranscriptionUrgencyResponse(TranscriptionUrgencyEntity transcriptionUrgencyEntity) {
        TranscriptionUrgencyResponse transcriptionUrgencyResponse = new TranscriptionUrgencyResponse();
        transcriptionUrgencyResponse.setTranscriptionUrgencyId(transcriptionUrgencyEntity.getId());
        transcriptionUrgencyResponse.setDescription(transcriptionUrgencyEntity.getDescription());
        transcriptionUrgencyResponse.setPriorityOrder(transcriptionUrgencyEntity.getPriorityOrder());
        return transcriptionUrgencyResponse;
    }

    public static GetTranscriptionByIdResponse mapToTranscriptionResponse(TranscriptionEntity transcriptionEntity) {

        GetTranscriptionByIdResponse transcriptionResponse = new GetTranscriptionByIdResponse();
        try {
            CourtCaseEntity courtCase = transcriptionEntity.getCourtCase();
            transcriptionResponse.setTranscriptionId(transcriptionEntity.getId());
            transcriptionResponse.setCaseId(courtCase.getId());
            EventHandlerEntity reportingRestrictions = courtCase.getReportingRestrictions();
            if (reportingRestrictions != null) {
                transcriptionResponse.setReportingRestriction(reportingRestrictions.getEventName());
            }
            transcriptionResponse.setCaseNumber(courtCase.getCaseNumber());
            transcriptionResponse.setCourthouse(courtCase.getCourthouse().getCourthouseName());
            transcriptionResponse.setDefendants(courtCase.getDefendantStringList());
            transcriptionResponse.setJudges(courtCase.getJudgeStringList());

            if (transcriptionEntity.getTranscriptionStatus() != null) {
                transcriptionResponse.setStatus(transcriptionEntity.getTranscriptionStatus().getDisplayName());
            }

            transcriptionResponse.setFrom(getRequestorName(transcriptionEntity));
            transcriptionResponse.setReceived(transcriptionEntity.getCreatedDateTime());
            transcriptionResponse.setRequestorComments(TranscriptionUtil.getTranscriptionCommentAtStatus(
                transcriptionEntity,
                TranscriptionStatusEnum.REQUESTED
            ));
            transcriptionResponse.setRejectionReason(TranscriptionUtil.getTranscriptionCommentAtStatus(transcriptionEntity, TranscriptionStatusEnum.REJECTED));

            final var latestTranscriptionDocumentEntity = transcriptionEntity.getTranscriptionDocumentEntities()
                .stream()
                .max(comparing(TranscriptionDocumentEntity::getUploadedDateTime));
            latestTranscriptionDocumentEntity.ifPresent(
                transcriptionDocumentEntity -> transcriptionResponse.setTranscriptFileName(transcriptionDocumentEntity.getFileName()));

            if (transcriptionEntity.getHearing() != null) {
                transcriptionResponse.setHearingId(transcriptionEntity.getHearing().getId());
                transcriptionResponse.setHearingDate(transcriptionEntity.getHearing().getHearingDate());
            } else {
                transcriptionResponse.setHearingDate(transcriptionEntity.getHearingDate());
            }
            if (transcriptionEntity.getTranscriptionUrgency() != null) {
                transcriptionResponse.setUrgency(transcriptionEntity.getTranscriptionUrgency().getDescription());
            }
            transcriptionResponse.setRequestType(transcriptionEntity.getTranscriptionType().getDescription());
            transcriptionResponse.setTranscriptionStartTs(transcriptionEntity.getStartTime());
            transcriptionResponse.setTranscriptionEndTs(transcriptionEntity.getEndTime());
            transcriptionResponse.setIsManual(transcriptionEntity.getIsManualTranscription());
        } catch (Exception exception) {
            throw new DartsApiException(TranscriptionApiError.TRANSCRIPTION_NOT_FOUND);
        }
        return transcriptionResponse;

    }

    private String getRequestorName(TranscriptionEntity transcriptionEntity) {
        if (transcriptionEntity.getCreatedBy() != null) {
            return transcriptionEntity.getCreatedBy().getUserName();
        } else {
            return transcriptionEntity.getRequestor();
        }
    }
}
