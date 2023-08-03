package uk.gov.hmcts.darts.cases.helper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.darts.cases.model.GetCasesSearchRequest;
import uk.gov.hmcts.darts.common.entity.CourtCaseEntity;
import uk.gov.hmcts.darts.common.entity.CourtCaseEntity_;
import uk.gov.hmcts.darts.common.entity.CourthouseEntity;
import uk.gov.hmcts.darts.common.entity.CourthouseEntity_;
import uk.gov.hmcts.darts.common.entity.CourtroomEntity;
import uk.gov.hmcts.darts.common.entity.CourtroomEntity_;
import uk.gov.hmcts.darts.common.entity.DefendantEntity;
import uk.gov.hmcts.darts.common.entity.DefendantEntity_;
import uk.gov.hmcts.darts.common.entity.EventEntity;
import uk.gov.hmcts.darts.common.entity.EventEntity_;
import uk.gov.hmcts.darts.common.entity.HearingEntity;
import uk.gov.hmcts.darts.common.entity.HearingEntity_;
import uk.gov.hmcts.darts.common.entity.JudgeEntity;
import uk.gov.hmcts.darts.common.entity.JudgeEntity_;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@SuppressWarnings({"PMD.TooManyMethods"})
public class AdvancedSearchRequestHelper {
    @PersistenceContext
    private EntityManager entityManager;

    public List<CourtCaseEntity> getMatchingCourtCases(GetCasesSearchRequest request) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CourtCaseEntity> criteriaQuery = criteriaBuilder.createQuery(CourtCaseEntity.class);
        Root<CourtCaseEntity> caseRoot = criteriaQuery.from(CourtCaseEntity.class);
        List<Predicate> predicates = createPredicates(request, criteriaBuilder, caseRoot);

        Predicate finalAndPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        criteriaQuery.where(finalAndPredicate);
        TypedQuery<CourtCaseEntity> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }

    private List<Predicate> createPredicates(GetCasesSearchRequest request, CriteriaBuilder criteriaBuilder, Root<CourtCaseEntity> caseRoot) {
        List<Predicate> predicates = new ArrayList<>();
        CollectionUtils.addAll(predicates, createCaseCriteria(request, criteriaBuilder, caseRoot));
        CollectionUtils.addAll(predicates, addHearingDateCriteria(request, criteriaBuilder, caseRoot));
        CollectionUtils.addAll(predicates, addCourthouseCriteria(request, criteriaBuilder, caseRoot));
        CollectionUtils.addAll(predicates, addCourtroomCriteria(request, criteriaBuilder, caseRoot));
        CollectionUtils.addAll(predicates, addJudgeCriteria(request, criteriaBuilder, caseRoot));
        CollectionUtils.addAll(predicates, addDefendantCriteria(request, criteriaBuilder, caseRoot));
        CollectionUtils.addAll(predicates, addEventCriteria(request, criteriaBuilder, caseRoot));
        return predicates;

    }

    private List<Predicate> createCaseCriteria(GetCasesSearchRequest request, CriteriaBuilder criteriaBuilder, Root<CourtCaseEntity> caseRoot) {
        List<Predicate> predicateList = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getCaseNumber())) {
            predicateList.add(criteriaBuilder.like(
                criteriaBuilder.upper(caseRoot.get(CourtCaseEntity_.CASE_NUMBER)),
                surroundWithPercentagesUpper(request.getCaseNumber())
            ));
        }
        return predicateList;
    }

    private String surroundWithPercentagesUpper(String value) {
        return surroundValue(value.toUpperCase(Locale.ROOT), "%");
    }

    private String surroundValue(String value, String surroundWith) {
        return surroundWith + value + surroundWith;
    }

    private List<Predicate> addCourtroomCriteria(GetCasesSearchRequest request, CriteriaBuilder criteriaBuilder, Root<CourtCaseEntity> caseRoot) {
        List<Predicate> predicateList = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getCourtroom())) {
            Join<CourtCaseEntity, CourtroomEntity> courtroomJoin = joinCourtroom(caseRoot);

            predicateList.add(criteriaBuilder.like(
                criteriaBuilder.upper(courtroomJoin.get(CourtroomEntity_.NAME)),
                surroundWithPercentagesUpper(request.getCourtroom())
            ));
        }
        return predicateList;
    }

    private List<Predicate> addDefendantCriteria(GetCasesSearchRequest request, CriteriaBuilder criteriaBuilder, Root<CourtCaseEntity> caseRoot) {
        List<Predicate> predicateList = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getDefendantName())) {
            Join<CourtCaseEntity, DefendantEntity> defendantJoin = joinDefendantEntity(caseRoot);

            predicateList.add(criteriaBuilder.like(
                criteriaBuilder.upper(defendantJoin.get(DefendantEntity_.NAME)),
                surroundWithPercentagesUpper(request.getDefendantName())
            ));
        }
        return predicateList;
    }

    private List<Predicate> addEventCriteria(GetCasesSearchRequest request, CriteriaBuilder criteriaBuilder, Root<CourtCaseEntity> caseRoot) {
        List<Predicate> predicateList = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getEventTextContains())) {
            Join<CourtCaseEntity, EventEntity> eventJoin = joinEventEntity(caseRoot);

            predicateList.add(criteriaBuilder.like(
                criteriaBuilder.upper(eventJoin.get(EventEntity_.EVENT_TEXT)),
                surroundWithPercentagesUpper(request.getEventTextContains())
            ));
        }
        return predicateList;
    }

    private List<Predicate> addCourthouseCriteria(GetCasesSearchRequest request, CriteriaBuilder criteriaBuilder, Root<CourtCaseEntity> caseRoot) {
        List<Predicate> predicateList = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getCourthouse())) {
            Join<CourtCaseEntity, CourthouseEntity> courthouseJoin = joinCourthouse(caseRoot);
            predicateList.add(criteriaBuilder.like(
                criteriaBuilder.upper(courthouseJoin.get(CourthouseEntity_.COURTHOUSE_NAME)),
                surroundWithPercentagesUpper(request.getCourthouse())
            ));
        }
        return predicateList;
    }

    private List<Predicate> addJudgeCriteria(GetCasesSearchRequest request, CriteriaBuilder criteriaBuilder, Root<CourtCaseEntity> caseRoot) {
        List<Predicate> predicateList = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getJudgeName())) {
            Join<CourtCaseEntity, JudgeEntity> judgeJoin = joinJudge(caseRoot);
            predicateList.add(criteriaBuilder.like(
                criteriaBuilder.upper(judgeJoin.get(JudgeEntity_.NAME)),
                surroundWithPercentagesUpper(request.getJudgeName())
            ));
        }
        return predicateList;
    }

    private List<Predicate> addHearingDateCriteria(GetCasesSearchRequest request, CriteriaBuilder criteriaBuilder, Root<CourtCaseEntity> caseRoot) {
        List<Predicate> predicateList = new ArrayList<>();
        if (request.getDateFrom() != null || request.getDateTo() != null) {
            if (request.getDateFrom() != null) {
                Join<CourtCaseEntity, HearingEntity> hearingJoin = joinHearing(caseRoot);
                predicateList.add(criteriaBuilder.greaterThanOrEqualTo(
                    hearingJoin.get(HearingEntity_.HEARING_DATE),
                    request.getDateFrom()
                ));
            }
            if (request.getDateTo() != null) {
                Join<CourtCaseEntity, HearingEntity> hearingJoin = joinHearing(caseRoot);
                predicateList.add(criteriaBuilder.lessThanOrEqualTo(
                    hearingJoin.get(HearingEntity_.HEARING_DATE),
                    request.getDateTo()
                ));
            }
        }
        return predicateList;
    }


    private Join<CourtCaseEntity, HearingEntity> joinHearing(Root<CourtCaseEntity> caseRoot) {
        return caseRoot.join(CourtCaseEntity_.HEARINGS, JoinType.INNER);
    }

    private Join<CourtCaseEntity, JudgeEntity> joinJudge(Root<CourtCaseEntity> caseRoot) {
        return caseRoot.join(CourtCaseEntity_.JUDGES, JoinType.INNER);
    }

    private Join<CourtCaseEntity, CourtroomEntity> joinCourtroom(Root<CourtCaseEntity> caseRoot) {
        Join<CourtCaseEntity, HearingEntity> hearingJoin = joinHearing(caseRoot);
        return hearingJoin.join(HearingEntity_.COURTROOM, JoinType.INNER);
    }

    private Join<CourtCaseEntity, CourthouseEntity> joinCourthouse(Root<CourtCaseEntity> caseRoot) {
        Join<CourtCaseEntity, CourtroomEntity> courtroomJoin = joinCourtroom(caseRoot);
        return courtroomJoin.join(CourtroomEntity_.COURTHOUSE, JoinType.INNER);
    }

    private Join<CourtCaseEntity, DefendantEntity> joinDefendantEntity(Root<CourtCaseEntity> caseRoot) {
        return caseRoot.join(CourtCaseEntity_.DEFENDANT_LIST, JoinType.INNER);
    }

    private Join<CourtCaseEntity, EventEntity> joinEventEntity(Root<CourtCaseEntity> caseRoot) {
        Join<CourtCaseEntity, HearingEntity> hearingJoin = joinHearing(caseRoot);
        return hearingJoin.join(HearingEntity_.EVENT_LIST, JoinType.INNER);
    }
}