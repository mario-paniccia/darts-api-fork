package uk.gov.hmcts.darts.authentication.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.darts.common.exception.DartsApiError;

@Getter
@RequiredArgsConstructor
public enum AuthenticationError implements DartsApiError {

    FAILED_TO_OBTAIN_ACCESS_TOKEN("100",
                                  HttpStatus.INTERNAL_SERVER_ERROR,
                                  "Failed to obtain access token"),

    FAILED_TO_VALIDATE_ACCESS_TOKEN("101",
                                  HttpStatus.INTERNAL_SERVER_ERROR,
                                  "Failed to validate access token"),

    LOGOUT_ATTEMPTED_FOR_INACTIVE_SESSION("102",
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Logout was attempted for a session that was inactive");

    private static final String ERROR_TYPE_PREFIX = "AUTHENTICATION";

    private final String errorTypeNumeric;
    private final HttpStatus httpStatus;
    private final String title;

    @Override
    public String getErrorTypePrefix() {
        return ERROR_TYPE_PREFIX;
    }

}