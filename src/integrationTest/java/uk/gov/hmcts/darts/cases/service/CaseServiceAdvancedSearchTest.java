package uk.gov.hmcts.darts.cases.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.darts.authorisation.component.UserIdentity;
import uk.gov.hmcts.darts.cases.model.AdvancedSearchResult;
import uk.gov.hmcts.darts.cases.model.GetCasesSearchRequest;
import uk.gov.hmcts.darts.common.entity.CourtCaseEntity;
import uk.gov.hmcts.darts.common.entity.CourthouseEntity;
import uk.gov.hmcts.darts.common.entity.CourtroomEntity;
import uk.gov.hmcts.darts.common.entity.EventEntity;
import uk.gov.hmcts.darts.common.entity.HearingEntity;
import uk.gov.hmcts.darts.common.entity.JudgeEntity;
import uk.gov.hmcts.darts.common.entity.UserAccountEntity;
import uk.gov.hmcts.darts.testutils.IntegrationBase;
import uk.gov.hmcts.darts.testutils.TestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.darts.common.enums.SecurityRoleEnum.JUDGE;
import static uk.gov.hmcts.darts.testutils.TestUtils.getContentsFromFile;
import static uk.gov.hmcts.darts.testutils.data.CaseTestData.createCaseAt;
import static uk.gov.hmcts.darts.testutils.data.CourthouseTestData.someMinimalCourthouse;
import static uk.gov.hmcts.darts.testutils.data.CourtroomTestData.createCourtRoomWithNameAtCourthouse;
import static uk.gov.hmcts.darts.testutils.data.DefendantTestData.createDefendantForCaseWithName;
import static uk.gov.hmcts.darts.testutils.data.EventTestData.createEventWith;
import static uk.gov.hmcts.darts.testutils.data.HearingTestData.createHearingWithDefaults;
import static uk.gov.hmcts.darts.testutils.data.JudgeTestData.createJudgeWithName;

@Slf4j
@SuppressWarnings({"PMD.VariableDeclarationUsageDistance", "PMD.NcssCount", "PMD.ExcessiveImports"})
class CaseServiceAdvancedSearchTest extends IntegrationBase {

    @Autowired
    CaseService service;
    @MockBean
    private UserIdentity mockUserIdentity;
    CourthouseEntity swanseaCourthouse;

    @BeforeEach
    void setupData() {
        swanseaCourthouse = someMinimalCourthouse();
        swanseaCourthouse.setCourthouseName("SWANSEA");
        swanseaCourthouse.setDisplayName("SWANSEA");

        CourtCaseEntity case1 = createCaseAt(swanseaCourthouse);
        case1.setCaseNumber("Case1");

        CourtCaseEntity case2 = createCaseAt(swanseaCourthouse);
        case2.setCaseNumber("Case2");
        case2.setDefendantList(Arrays.asList(createDefendantForCaseWithName(case2, "Defendant2")));

        CourtCaseEntity case3 = createCaseAt(swanseaCourthouse);
        case3.setCaseNumber("Case3");

        CourtCaseEntity case4 = createCaseAt(swanseaCourthouse);
        case4.setCaseNumber("Case4");

        CourtCaseEntity case5 = createCaseAt(swanseaCourthouse);
        case5.setCaseNumber("case5");

        CourtCaseEntity case6 = createCaseAt(swanseaCourthouse);
        case6.setCaseNumber("case6");

        CourtCaseEntity case7 = createCaseAt(swanseaCourthouse);
        case7.setCaseNumber("case7");

        CourtCaseEntity case8 = createCaseAt(swanseaCourthouse);
        case8.setCaseNumber("case8");

        JudgeEntity judge = createJudgeWithName("aJudge");
        CourtroomEntity courtroom1 = createCourtRoomWithNameAtCourthouse(swanseaCourthouse, "courtroom1");
        HearingEntity hearing1a = createHearingWithDefaults(case1, courtroom1, LocalDate.of(2023, 5, 20), judge);

        HearingEntity hearing1b = createHearingWithDefaults(case1, courtroom1, LocalDate.of(2023, 5, 21), judge);

        HearingEntity hearing1c = createHearingWithDefaults(case1, courtroom1, LocalDate.of(2023, 5, 22), judge);

        HearingEntity hearing2a = createHearingWithDefaults(case2, courtroom1, LocalDate.of(2023, 6, 20), judge);

        HearingEntity hearing2b = createHearingWithDefaults(case2, courtroom1, LocalDate.of(2023, 6, 21), judge);

        HearingEntity hearing2c = createHearingWithDefaults(case2, courtroom1, LocalDate.of(2023, 6, 22), judge);

        HearingEntity hearing3a = createHearingWithDefaults(case3, courtroom1, LocalDate.of(2023, 7, 20), judge);
        JudgeEntity judge3a = createJudgeWithName("Judge3a");
        hearing3a.addJudge(judge3a);

        HearingEntity hearing3b = createHearingWithDefaults(case3, courtroom1, LocalDate.of(2023, 7, 21), judge);

        HearingEntity hearing3c = createHearingWithDefaults(case3, courtroom1, LocalDate.of(2023, 7, 22), judge);

        CourtroomEntity courtroom2 = createCourtRoomWithNameAtCourthouse(swanseaCourthouse, "courtroom2");
        HearingEntity hearing4a = createHearingWithDefaults(case4, courtroom2, LocalDate.of(2023, 8, 20), judge);

        HearingEntity hearing4b = createHearingWithDefaults(case4, courtroom1, LocalDate.of(2023, 8, 21), judge);

        HearingEntity hearing4c = createHearingWithDefaults(case4, courtroom1, LocalDate.of(2023, 8, 22), judge);

        HearingEntity hearing5a = createHearingWithDefaults(case5, courtroom2, LocalDate.of(2023, 9, 20), judge);

        HearingEntity hearing5b = createHearingWithDefaults(case5, courtroom1, LocalDate.of(2023, 9, 21), judge);

        CourtroomEntity courtroom3 = createCourtRoomWithNameAtCourthouse(swanseaCourthouse, "courtroom3");
        HearingEntity hearing5c = createHearingWithDefaults(case5, courtroom3, LocalDate.of(2023, 9, 22), judge);

        HearingEntity hearing6a = createHearingWithDefaults(case6, courtroom2, LocalDate.of(2023, 9, 20), judge);

        HearingEntity hearing6b = createHearingWithDefaults(case6, courtroom3, LocalDate.of(2023, 9, 21), judge);
        hearing6b.addJudge(createJudgeWithName("Judge6b"));

        HearingEntity hearing6c = createHearingWithDefaults(case6, courtroom1, LocalDate.of(2023, 9, 22), judge);

        HearingEntity hearing7a = createHearingWithDefaults(case7, courtroom1, LocalDate.of(2023, 10, 21), judge);
        HearingEntity hearing7b = createHearingWithDefaults(case7, courtroom1, LocalDate.of(2023, 10, 23), judge);
        HearingEntity hearing8 = createHearingWithDefaults(case8, courtroom1, LocalDate.of(2023, 10, 22), judge);

        dartsDatabase.saveAll(hearing1a, hearing1b, hearing1c,
                              hearing2a, hearing2b, hearing2c,
                              hearing3a, hearing3b, hearing3c,
                              hearing4a, hearing4b, hearing4c,
                              hearing5a, hearing5b, hearing5c,
                              hearing6a, hearing6b, hearing6c,
                              hearing7a, hearing7b, hearing8

        );

        EventEntity event4a = createEventWith("eventName", "event4a", hearing4a, OffsetDateTime.now());
        EventEntity event5b = createEventWith("eventName", "event5b", hearing5b, OffsetDateTime.now());
        dartsDatabase.saveAll(event4a, event5b);
    }

    @Test
    void getWithCaseNumber() throws IOException {

        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .caseNumber("sE1")
            .build();

        setupUserAccountAndSecurityGroup();

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = TestUtils.removeIds(getContentsFromFile(
            "tests/cases/CaseServiceAdvancedSearchTest/getWithCaseNumber/expectedResponse.json"));

        compareJson(actualResponse, expectedResponse);
    }

    private static void compareJson(String actualResponse, String expectedResponse) {
        try {
            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.NON_EXTENSIBLE);
        } catch (AssertionError ae) {
            log.error("expected\r\n{}to match\r\n{}", expectedResponse, actualResponse);
            throw ae;
        }
    }

    @Test
    void getWithDateRangeFrom() throws IOException {
        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .dateFrom(LocalDate.of(2023, 7, 21))
            .build();

        setupUserAccountAndSecurityGroup();

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = TestUtils.removeIds(getContentsFromFile(
            "tests/cases/CaseServiceAdvancedSearchTest/getWithDateRangeFrom/expectedResponse.json"));
        compareJson(actualResponse, expectedResponse);
    }

    @Test
    void getWithDateRangeTo() throws IOException {

        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .dateTo(LocalDate.of(2023, 6, 21))
            .build();

        setupUserAccountAndSecurityGroup();

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = TestUtils.removeIds(getContentsFromFile(
            "tests/cases/CaseServiceAdvancedSearchTest/getWithDateRangeTo/expectedResponse.json"));
        compareJson(actualResponse, expectedResponse);
    }

    @Test
    void getWithDateRangeFromTo() throws IOException {

        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .dateFrom(LocalDate.of(2023, 6, 21))
            .dateTo(LocalDate.of(2023, 7, 21))
            .build();

        setupUserAccountAndSecurityGroup();

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = TestUtils.removeIds(getContentsFromFile(
            "tests/cases/CaseServiceAdvancedSearchTest/getWithDateRangeFromTo/expectedResponse.json"));
        compareJson(actualResponse, expectedResponse);
    }

    @Test
    void getWithDateRangeFromToSameDate() throws IOException {

        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .dateFrom(LocalDate.of(2023, 10, 22))
            .dateTo(LocalDate.of(2023, 10, 22))
            .build();

        setupUserAccountAndSecurityGroup();

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = TestUtils.removeIds(getContentsFromFile(
            "tests/cases/CaseServiceAdvancedSearchTest/getWithDateRangeFromToSameDate/expectedResponse.json"));
        compareJson(actualResponse, expectedResponse);
    }

    @Test
    void getWithJudge() throws IOException {

        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .judgeName("3A")
            .build();

        setupUserAccountAndSecurityGroup();

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = TestUtils.removeIds(getContentsFromFile(
            "tests/cases/CaseServiceAdvancedSearchTest/getWithJudge/expectedResponse.json"));
        compareJson(actualResponse, expectedResponse);
    }

    @Test
    void getWithCourtroom() throws IOException {

        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .courtroom("roOm2")
            .build();

        setupUserAccountAndSecurityGroup();

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = TestUtils.removeIds(getContentsFromFile(
            "tests/cases/CaseServiceAdvancedSearchTest/getWithCourtroom/expectedResponse.json"));
        compareJson(actualResponse, expectedResponse);
    }

    @Test
    void getWithEventText() throws IOException {

        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .eventTextContains("nT5b")
            .build();

        setupUserAccountAndSecurityGroup();

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = TestUtils.removeIds(getContentsFromFile(
            "tests/cases/CaseServiceAdvancedSearchTest/getWithEventText/expectedResponse.json"));
        compareJson(actualResponse, expectedResponse);
    }


    @Test
    void getWithCourtroomJudge() throws IOException {

        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .courtroom("roOm3")
            .judgeName("dGe6B")
            .build();

        setupUserAccountAndSecurityGroup();

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = TestUtils.removeIds(getContentsFromFile(
            "tests/cases/CaseServiceAdvancedSearchTest/getWithCourtroomJudge/expectedResponse.json"));
        compareJson(actualResponse, expectedResponse);
    }

    @Test
    void whenAdvancedSearchIsRunForUserWithGlobalAccessWithNoCourthouseAccess_thenShouldReturnResultsOk() throws IOException {

        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .caseNumber("sE1")
            .build();

        UserAccountEntity testUser = dartsDatabase.getUserAccountStub().createAuthorisedIntegrationTestUserWithoutCourthouse();
        when(mockUserIdentity.getUserAccount()).thenReturn(testUser);
        when(mockUserIdentity.userHasGlobalAccess(Set.of(JUDGE))).thenReturn(true);

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = TestUtils.removeIds(getContentsFromFile(
            "tests/cases/CaseServiceAdvancedSearchTest/getWithCaseNumber/expectedResponse.json"));

        compareJson(actualResponse, expectedResponse);
    }

    @Test
    void whenAdvancedSearchIsRunForUserWithoutGlobalAccessWithNoCourthouseAccess_thenShouldReturnEmptyArray() throws IOException {

        GetCasesSearchRequest request = GetCasesSearchRequest.builder()
            .caseNumber("sE1")
            .build();

        UserAccountEntity testUser = dartsDatabase.getUserAccountStub().createAuthorisedIntegrationTestUserWithoutCourthouse();
        when(mockUserIdentity.getUserAccount()).thenReturn(testUser);

        List<AdvancedSearchResult> resultList = service.advancedSearch(request);
        String actualResponse = TestUtils.removeIds(objectMapper.writeValueAsString(resultList));
        String expectedResponse = "[]";

        compareJson(actualResponse, expectedResponse);
    }

    private void setupUserAccountAndSecurityGroup() {
        UserAccountEntity testUser = dartsDatabase.getUserAccountStub()
            .createAuthorisedIntegrationTestUser(swanseaCourthouse);
        when(mockUserIdentity.getUserAccount()).thenReturn(testUser);
    }

}
