package uk.gov.hmcts.darts.authorisation.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.darts.common.entity.SecurityRoleEnum.COURT_CLERK;
import static uk.gov.hmcts.darts.common.entity.SecurityRoleEnum.COURT_MANAGER;

class UserStateTest {

    @Test
    void builder() {
        Set<Role> newRoles = new HashSet<>();
        newRoles.add(Role.builder()
                         .roleId(COURT_MANAGER.getId())
                         .roleName(COURT_MANAGER.toString())
                         .permissions(new HashSet<>())
                         .build());
        newRoles.add(Role.builder()
                         .roleId(COURT_CLERK.getId())
                         .roleName(COURT_CLERK.toString())
                         .permissions(new HashSet<>())
                         .build());

        UserState userState = UserState.builder()
            .userId(123)
            .userName("UserName")
            .roles(newRoles)
            .build();

        assertEquals(123, userState.getUserId());
        assertEquals("UserName", userState.getUserName());
        Set<Role> roles = userState.getRoles();
        assertEquals(newRoles, roles);
        assertEquals(2, roles.size());
    }

}