package uk.gov.hmcts.darts.dailylist.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.darts.common.entity.CourthouseEntity;
import uk.gov.hmcts.darts.common.entity.DailyListEntity;
import uk.gov.hmcts.darts.courthouse.api.CourthouseApi;
import uk.gov.hmcts.darts.courthouse.exception.CourthouseCodeNotMatchException;
import uk.gov.hmcts.darts.courthouse.exception.CourthouseNameNotFoundException;
import uk.gov.hmcts.darts.dailylist.exception.DailyListException;
import uk.gov.hmcts.darts.dailylist.mapper.DailyListMapper;
import uk.gov.hmcts.darts.dailylist.model.CourtHouse;
import uk.gov.hmcts.darts.dailylist.model.DailyList;
import uk.gov.hmcts.darts.dailylist.model.DailyListPostRequest;
import uk.gov.hmcts.darts.dailylist.repository.DailyListRepository;
import uk.gov.hmcts.darts.dailylist.service.DailyListService;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyListServiceImpl implements DailyListService {

    private final DailyListRepository dailyListRepository;
    private final CourthouseApi courthouseApi;
    private final DailyListMapper dailyListMapper;

    @Value("${darts.daily-list.housekeeping.days-to-keep:30}")
    private int housekeepingDays;

    @Value("${darts.daily-list.housekeeping.enabled:false}")
    private boolean housekeepingEnabled;

    @Override
    /*
    Retrieve the new Daily List, and store it in the database.
     */
    public void processIncomingDailyList(DailyListPostRequest postRequest) {
        DailyList dailyList = postRequest.getDailyList();

        CourthouseEntity courthouse = retrieveCourtHouse(dailyList);
        String uniqueId = dailyList.getDocumentId().getUniqueId();
        Optional<DailyListEntity> existingRecordOpt = dailyListRepository.findByUniqueId(uniqueId);
        if (existingRecordOpt.isPresent()) {
            //update the record
            DailyListEntity existingRecord = existingRecordOpt.get();
            dailyListMapper.mapToExistingDailyListEntity(postRequest, courthouse, existingRecord);
            dailyListRepository.saveAndFlush(existingRecord);
        } else {
            //insert new record
            DailyListEntity dailyListEntity = dailyListMapper.mapToDailyListEntity(
                postRequest,
                courthouse
            );
            dailyListRepository.saveAndFlush(dailyListEntity);
        }
    }

    @Override
    @SchedulerLock(name = "DailyListService_Housekeeping",
        lockAtLeastFor = "PT1M", lockAtMostFor = "PT5M")
    @Scheduled(cron = "${darts.daily-list.housekeeping.cron}")
    @Transactional
    public void runHouseKeeping() {
        if (housekeepingEnabled) {
            LocalDate dateToDeleteBefore = LocalDate.now().minusDays(housekeepingDays);
            log.info("Starting DailyList housekeeping, deleting anything before {}", dateToDeleteBefore);
            List<DailyListEntity> deletedEntities = dailyListRepository.deleteByStartDateBefore(dateToDeleteBefore);
            log.info("Finished DailyList housekeeping. Deleted {} rows.", deletedEntities.size());
        }
    }

    private CourthouseEntity retrieveCourtHouse(DailyList dailyList) {
        CourtHouse crownCourt = dailyList.getCrownCourt();
        Integer courthouseCode = crownCourt.getCourtHouseCode().getCode();
        String courthouseName = crownCourt.getCourtHouseName();
        try {
            return courthouseApi.retrieveAndUpdateCourtHouse(courthouseCode, courthouseName);
        } catch (CourthouseCodeNotMatchException ccnme) {
            log.warn(
                "Courthouse in database {} Does not match that received by dailyList, {} {}",
                ccnme.getDatabaseCourthouse(),
                courthouseCode,
                courthouseName
            );
            return ccnme.getDatabaseCourthouse();
        } catch (CourthouseNameNotFoundException e) {
            String message = MessageFormat.format(
                "DailyList with uniqueId {0} received with an invalid courthouse ''{1}''",
                dailyList.getDocumentId().getUniqueId(),
                crownCourt.getCourtHouseName()
            );
            throw new DailyListException(message, e);
        }
    }

}
