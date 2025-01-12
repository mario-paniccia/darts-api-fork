package uk.gov.hmcts.darts.usermanagement.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.darts.authorisation.api.AuthorisationApi;
import uk.gov.hmcts.darts.common.entity.SecurityGroupEntity;
import uk.gov.hmcts.darts.common.entity.UserAccountEntity;
import uk.gov.hmcts.darts.common.repository.SecurityGroupRepository;
import uk.gov.hmcts.darts.common.repository.UserAccountRepository;
import uk.gov.hmcts.darts.usermanagement.component.UserSearchQuery;
import uk.gov.hmcts.darts.usermanagement.component.validation.Validator;
import uk.gov.hmcts.darts.usermanagement.mapper.impl.UserAccountMapper;
import uk.gov.hmcts.darts.usermanagement.model.User;
import uk.gov.hmcts.darts.usermanagement.model.UserPatch;
import uk.gov.hmcts.darts.usermanagement.model.UserSearch;
import uk.gov.hmcts.darts.usermanagement.model.UserWithId;
import uk.gov.hmcts.darts.usermanagement.model.UserWithIdAndLastLogin;
import uk.gov.hmcts.darts.usermanagement.service.UserManagementService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserAccountMapper userAccountMapper;
    private final UserAccountRepository userAccountRepository;
    private final SecurityGroupRepository securityGroupRepository;
    private final AuthorisationApi authorisationApi;
    private final UserSearchQuery userSearchQuery;
    private final Validator<User> duplicateEmailValidator;
    private final Validator<Integer> userAccountExistsValidator;

    @Override
    @Transactional
    public UserWithId createUser(User user) {
        duplicateEmailValidator.validate(user);

        var userEntity = userAccountMapper.mapToUserEntity(user);
        if (isNull(userEntity.isActive())) {
            userEntity.setActive(true);
        }
        userEntity.setIsSystemUser(false);
        mapSecurityGroupsToUserEntity(user.getSecurityGroups(), userEntity);

        var currentUser = authorisationApi.getCurrentUser();
        userEntity.setCreatedBy(currentUser);
        userEntity.setLastModifiedBy(currentUser);

        var now = OffsetDateTime.now();
        userEntity.setCreatedDateTime(now);
        userEntity.setLastModifiedDateTime(now);

        var createdUserEntity = userAccountRepository.save(userEntity);

        UserWithId userWithId = userAccountMapper.mapToUserWithIdModel(createdUserEntity);
        List<Integer> securityGroupIds = mapSecurityGroupEntitiesToIds(createdUserEntity.getSecurityGroupEntities());
        userWithId.setSecurityGroups(securityGroupIds);

        return userWithId;
    }

    @Override
    @Transactional
    public UserWithIdAndLastLogin modifyUser(Integer userId, UserPatch userPatch) {
        userAccountExistsValidator.validate(userId);

        UserAccountEntity updatedUserEntity = userAccountRepository.findById(userId)
            .map(userEntity -> updatedUserAccount(userPatch, userEntity)).orElseThrow();

        UserWithIdAndLastLogin user = userAccountMapper.mapToUserWithIdAndLastLoginModel(updatedUserEntity);
        List<Integer> securityGroupIds = mapSecurityGroupEntitiesToIds(updatedUserEntity.getSecurityGroupEntities());
        user.setSecurityGroups(securityGroupIds);

        return user;
    }

    @Override
    public List<UserWithIdAndLastLogin> search(UserSearch userSearch) {
        List<UserWithIdAndLastLogin> userWithIdAndLastLoginList = new ArrayList<>();

        userSearchQuery.getUsers(userSearch.getFullName(), userSearch.getEmailAddress(), userSearch.getActive())
            .forEach(userAccountEntity -> {
                UserWithIdAndLastLogin userWithIdAndLastLogin = userAccountMapper.mapToUserWithIdAndLastLoginModel(userAccountEntity);
                userWithIdAndLastLogin.setSecurityGroups(mapSecurityGroupEntitiesToIds(userAccountEntity.getSecurityGroupEntities()));
                userWithIdAndLastLoginList.add(userWithIdAndLastLogin);
            });

        return userWithIdAndLastLoginList;
    }

    private UserAccountEntity updatedUserAccount(UserPatch userPatch, UserAccountEntity userEntity) {
        updateEntity(userPatch, userEntity);
        return userAccountRepository.save(userEntity);
    }

    private void updateEntity(UserPatch userPatch, UserAccountEntity userAccountEntity) {
        String name = userPatch.getFullName();
        if (name != null) {
            userAccountEntity.setUserName(name);
        }

        String description = userPatch.getDescription();
        if (description != null) {
            userAccountEntity.setUserDescription(description);
        }

        Boolean active = userPatch.getActive();
        if (active != null) {
            userAccountEntity.setActive(active);
        }

        if (BooleanUtils.isTrue(userAccountEntity.isActive())) {
            mapSecurityGroupsToUserEntity(userPatch.getSecurityGroups(), userAccountEntity);
        } else {
            userAccountEntity.setSecurityGroupEntities(Collections.emptySet());
        }

        userAccountEntity.setLastModifiedBy(authorisationApi.getCurrentUser());
        userAccountEntity.setLastModifiedDateTime(OffsetDateTime.now());
    }

    private void mapSecurityGroupsToUserEntity(List<Integer> securityGroups, UserAccountEntity userAccountEntity) {
        if (securityGroups != null) {
            Set<SecurityGroupEntity> securityGroupEntities = securityGroups.stream()
                .map(securityGroupRepository::findById)
                .map(Optional::orElseThrow)
                .collect(Collectors.toSet());
            userAccountEntity.setSecurityGroupEntities(securityGroupEntities);
        }
    }

    private List<Integer> mapSecurityGroupEntitiesToIds(Set<SecurityGroupEntity> securityGroupEntities) {
        return securityGroupEntities.stream()
            .map(SecurityGroupEntity::getId)
            .toList();
    }
}
