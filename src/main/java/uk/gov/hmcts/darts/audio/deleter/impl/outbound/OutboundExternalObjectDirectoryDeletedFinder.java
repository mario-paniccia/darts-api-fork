package uk.gov.hmcts.darts.audio.deleter.impl.outbound;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.darts.audio.deleter.TransientObjectDirectoryDeletedFinder;
import uk.gov.hmcts.darts.common.repository.ObjectDirectoryStatusRepository;
import uk.gov.hmcts.darts.common.repository.TransientObjectDirectoryRepository;

@Service
public class OutboundExternalObjectDirectoryDeletedFinder extends TransientObjectDirectoryDeletedFinder {

    public OutboundExternalObjectDirectoryDeletedFinder(TransientObjectDirectoryRepository transientObjectDirectoryRepository,
                                                        ObjectDirectoryStatusRepository objectDirectoryStatusRepository) {
        super(transientObjectDirectoryRepository, objectDirectoryStatusRepository);
    }
}
