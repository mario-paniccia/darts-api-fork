package uk.gov.hmcts.darts.audio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.darts.audio.entity.MediaRequestEntity;

@Repository
public interface MediaRequestRepository extends JpaRepository<MediaRequestEntity, Integer> {

}