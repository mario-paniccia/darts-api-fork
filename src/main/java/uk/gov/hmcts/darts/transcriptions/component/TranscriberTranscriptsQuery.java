package uk.gov.hmcts.darts.transcriptions.component;

import uk.gov.hmcts.darts.transcriptions.model.TranscriberViewSummary;

import java.util.List;

public interface TranscriberTranscriptsQuery {

    List<TranscriberViewSummary> getTranscriptRequests(Integer userId);

    List<TranscriberViewSummary> getTranscriberTranscriptions(Integer userId);

}