package uk.gov.hmcts.darts.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.darts.common.entity.JudgeEntity;

import java.util.Optional;

@Repository
public interface JudgeRepository extends JpaRepository<JudgeEntity, Integer> {

    Optional<JudgeEntity> findByNameIgnoreCase(String name);

    default JudgeEntity createJudge(String judgeName) {
        JudgeEntity judge = new JudgeEntity();
        judge.setName(judgeName);
        this.saveAndFlush(judge);
        return judge;
    }
}
