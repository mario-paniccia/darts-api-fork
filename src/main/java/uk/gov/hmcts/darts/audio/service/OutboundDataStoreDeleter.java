package uk.gov.hmcts.darts.audio.service;

import uk.gov.hmcts.darts.common.entity.TransientObjectDirectoryEntity;

import java.util.List;

public interface OutboundDataStoreDeleter {

    List<TransientObjectDirectoryEntity> delete();
}