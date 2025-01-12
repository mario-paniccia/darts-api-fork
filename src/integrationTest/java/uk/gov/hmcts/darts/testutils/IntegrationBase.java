package uk.gov.hmcts.darts.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.darts.testutils.stubs.DartsDatabaseStub;
import uk.gov.hmcts.darts.testutils.stubs.wiremock.DartsGatewayStub;

@AutoConfigureWireMock
@SpringBootTest
@ActiveProfiles({"intTest", "h2db", "in-memory-caching"})
public class IntegrationBase {

    protected DartsGatewayStub dartsGateway = new DartsGatewayStub();

    @Autowired
    protected DartsDatabaseStub dartsDatabase;
    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void clearDbAndStubs() {
        dartsDatabase.clearDatabaseInThisOrder();
        dartsGateway.clearStubs();
    }
}
