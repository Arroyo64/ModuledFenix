package org.ies.fenix.client.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionManagerTest {

    @Test
    void saveSession_storesUserDataAndMarksUserAsLoggedIn() {
        SessionManager sessionManager = new SessionManager();

        sessionManager.saveSession("token-123", 5, "david");

        assertTrue(sessionManager.isLoggedIn());
        assertEquals("token-123", sessionManager.getToken());
        assertEquals(5, sessionManager.getClientId());
        assertEquals("david", sessionManager.getUsername());
        assertEquals("Bearer token-123", sessionManager.getAuthorizationHeader());
    }

    @Test
    void isLoggedIn_returnsFalseWhenTokenIsNullOrBlank() {
        SessionManager sessionManager = new SessionManager();
        assertFalse(sessionManager.isLoggedIn());

        sessionManager.saveSession("   ", 5, "david");
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    void clearSession_removesStoredUserData() {
        SessionManager sessionManager = new SessionManager();
        sessionManager.saveSession("token-123", 5, "david");

        sessionManager.clearSession();

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getToken());
        assertNull(sessionManager.getClientId());
        assertNull(sessionManager.getUsername());
    }
}
