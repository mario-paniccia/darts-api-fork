package uk.gov.hmcts.darts.audio.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.darts.audio.component.AudioRequestSummaryMapper;
import uk.gov.hmcts.darts.audio.service.MediaRequestService;
import uk.gov.hmcts.darts.audiorequests.api.AudioRequestsApi;
import uk.gov.hmcts.darts.audiorequests.model.AudioRequestSummary;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AudioRequestsController implements AudioRequestsApi {

    private final MediaRequestService mediaRequestService;
    private final AudioRequestSummaryMapper audioRequestSummaryMapper;

    @Override
    public ResponseEntity<List<AudioRequestSummary>> getYourAudio(Integer userId, Boolean expired) {

        return new ResponseEntity<>(audioRequestSummaryMapper.mapToAudioRequestSummary(
            mediaRequestService.viewAudioRequests(userId, expired)), HttpStatus.OK);
    }

}