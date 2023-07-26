package uk.gov.hmcts.darts.cases.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GetCasesSearchRequest {

    String caseNumber;
    String courthouse;
    String courtroom;
    String judgeName;
    String defendantName;
    LocalDate dateFrom;
    LocalDate dateTo;
    String eventTextContains;

}