package uk.gov.hmcts.darts.event.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.darts.common.entity.CourtCaseEntity;
import uk.gov.hmcts.darts.common.entity.EventHandlerEntity;
import uk.gov.hmcts.darts.common.repository.CaseRepository;
import uk.gov.hmcts.darts.common.repository.EventRepository;
import uk.gov.hmcts.darts.common.repository.HearingRepository;
import uk.gov.hmcts.darts.common.service.RetrieveCoreObjectService;
import uk.gov.hmcts.darts.event.model.CreatedHearing;
import uk.gov.hmcts.darts.event.model.DartsEvent;
import uk.gov.hmcts.darts.event.service.handler.base.EventHandlerBase;

@Slf4j
@Service
public class SetReportingRestrictionEventHandler extends EventHandlerBase {

    public SetReportingRestrictionEventHandler(RetrieveCoreObjectService retrieveCoreObjectService,
                           EventRepository eventRepository,
                           HearingRepository hearingRepository,
                           CaseRepository caseRepository,
                           ApplicationEventPublisher eventPublisher) {
        super(retrieveCoreObjectService, eventRepository, hearingRepository, caseRepository, eventPublisher);
    }

    @Transactional
    @Override
    public void handle(DartsEvent dartsEvent, EventHandlerEntity eventHandler) {
        CreatedHearing createdHearing = createHearingAndSaveEvent(dartsEvent, eventHandler);
        CourtCaseEntity courtCaseEntity = createdHearing.getHearingEntity().getCourtCase();
        courtCaseEntity.setReportingRestrictions(eventHandler);
        caseRepository.saveAndFlush(courtCaseEntity);
    }
}
