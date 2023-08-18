package uk.gov.hmcts.darts.testutils.stubs;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.darts.common.entity.CourthouseEntity;
import uk.gov.hmcts.darts.common.entity.DailyListEntity;
import uk.gov.hmcts.darts.dailylist.repository.DailyListRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DailyListStub {
    private final DailyListRepository dailyListRepository;

    public void createEmptyDailyList(LocalDate date, CourthouseEntity courthouse) {
        DailyListEntity dailyListEntity = new DailyListEntity();
        dailyListEntity.setStartDate(date);
        dailyListEntity.setCourthouse(courthouse);
        dailyListRepository.saveAndFlush(dailyListEntity);
    }

}