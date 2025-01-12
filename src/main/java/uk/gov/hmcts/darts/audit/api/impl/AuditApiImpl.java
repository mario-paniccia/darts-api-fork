package uk.gov.hmcts.darts.audit.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.darts.audit.api.AuditActivity;
import uk.gov.hmcts.darts.audit.api.AuditApi;
import uk.gov.hmcts.darts.audit.service.AuditService;
import uk.gov.hmcts.darts.common.entity.CourtCaseEntity;
import uk.gov.hmcts.darts.common.entity.UserAccountEntity;

@Service
@RequiredArgsConstructor
public class AuditApiImpl implements AuditApi {

    private final AuditService auditService;

    @Override
    public void recordAudit(AuditActivity activity, UserAccountEntity userAccountEntity, CourtCaseEntity courtCase) {
        auditService.recordAudit(activity, userAccountEntity, courtCase);
    }
}
