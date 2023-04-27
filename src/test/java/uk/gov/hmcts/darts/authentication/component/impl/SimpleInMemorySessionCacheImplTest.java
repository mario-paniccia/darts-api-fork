package uk.gov.hmcts.darts.authentication.component.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.darts.authentication.component.SessionCache;
import uk.gov.hmcts.darts.authentication.model.Session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleInMemorySessionCacheImplTest {

    private static final String DUMMY_SESSION_ID = "9D65049E1787A924E269747222F60CAA";

    private SessionCache sessionCache;

    @BeforeEach
    void setUp() {
        sessionCache = new SimpleInMemorySessionCacheImpl();
    }

    @Test
    void putShouldAddEntryToCache() {
        Session session = createSession();

        sessionCache.put(DUMMY_SESSION_ID, session);

        Session retrievedSession = sessionCache.get(DUMMY_SESSION_ID);
        assertEquals(retrievedSession, session);
    }

    @Test
    void putShouldThrowExceptionWhenProvidedWithNullKey() {
        assertThrows(NullPointerException.class, () -> sessionCache.put(null, createSession()));
    }

    @Test
    void putShouldThrowExceptionWhenProvidedWithNullValue() {
        assertThrows(IllegalArgumentException.class, () -> sessionCache.put(DUMMY_SESSION_ID, null));
    }

    @Test
    @SuppressWarnings("PMD.LinguisticNaming")
    void getShouldThrowExceptionWhenProvidedWithNullKey() {
        assertThrows(NullPointerException.class, () -> sessionCache.get(null));
    }

    private Session createSession() {
        return new Session(null, null, null);
    }

}
