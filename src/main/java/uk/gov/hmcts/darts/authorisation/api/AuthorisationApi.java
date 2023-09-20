package uk.gov.hmcts.darts.authorisation.api;

import uk.gov.hmcts.darts.authorisation.model.UserState;
import uk.gov.hmcts.darts.common.entity.CourthouseEntity;
import uk.gov.hmcts.darts.common.enums.SecurityRoleEnum;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AuthorisationApi {

    Optional<UserState> getAuthorisation(String emailAddress);

    void checkAuthorisation(List<CourthouseEntity> courthouses, Set<SecurityRoleEnum> securityRoles);

}
