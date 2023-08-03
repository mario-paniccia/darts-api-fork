package uk.gov.hmcts.darts.cases.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.darts.common.exception.DartsApiError;

@Getter
@RequiredArgsConstructor
public enum CaseError implements DartsApiError {

    TOO_MANY_RESULTS(
        "100",
        HttpStatus.BAD_REQUEST,
        "Too many results have been returned. Please change search criteria."
    ),
    NO_CRITERIA_SPECIFIED(
        "101",
        HttpStatus.BAD_REQUEST,
        "No search criteria has been specified, please add at least 1 criteria to search for."
    ),
    CRITERIA_TOO_BROAD(
        "102",
        HttpStatus.BAD_REQUEST,
        "Search criteria is too broad, please add at least 1 more criteria to search for."
    ),
    INVALID_REQUEST(
        "103",
        HttpStatus.BAD_REQUEST,
        "The request is not valid.."
    );

    private static final String ERROR_TYPE_PREFIX = "CASE";

    private final String errorTypeNumeric;
    private final HttpStatus httpStatus;
    private final String title;

    @Override
    public String getErrorTypePrefix() {
        return ERROR_TYPE_PREFIX;
    }

}