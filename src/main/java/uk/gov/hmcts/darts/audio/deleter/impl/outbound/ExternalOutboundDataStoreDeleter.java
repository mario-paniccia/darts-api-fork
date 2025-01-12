package uk.gov.hmcts.darts.audio.deleter.impl.outbound;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.darts.audio.deleter.impl.ExternalDataStoreDeleterImpl;
import uk.gov.hmcts.darts.common.entity.TransientObjectDirectoryEntity;
import uk.gov.hmcts.darts.common.helper.SystemUserHelper;
import uk.gov.hmcts.darts.common.repository.ObjectDirectoryStatusRepository;
import uk.gov.hmcts.darts.common.repository.TransientObjectDirectoryRepository;
import uk.gov.hmcts.darts.common.repository.UserAccountRepository;

@Service
public class ExternalOutboundDataStoreDeleter extends ExternalDataStoreDeleterImpl<TransientObjectDirectoryEntity> {


    public ExternalOutboundDataStoreDeleter(ObjectDirectoryStatusRepository objectDirectoryStatusRepository, UserAccountRepository userAccountRepository,
                                            TransientObjectDirectoryRepository repository,
                                            OutboundExternalObjectDirectoryDeletedFinder finder, OutboundDataStoreDeleter
                                                deleter, SystemUserHelper systemUserHelper) {
        super(objectDirectoryStatusRepository, userAccountRepository, repository, finder, deleter, systemUserHelper);
    }
}
