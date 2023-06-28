package uk.gov.hmcts.darts.cases.service;

import uk.gov.hmcts.darts.cases.model.GetCasesRequest;
import uk.gov.hmcts.darts.cases.model.ScheduledCase;

import java.util.List;

public interface CaseService {

    List<ScheduledCase> getCases(GetCasesRequest request);

}