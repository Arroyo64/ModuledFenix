package org.ies.fenix.server.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlreadyPurchasedExceptionTest {

    @Test
    void constructor_storesMessage() {
        AlreadyPurchasedException exception = new AlreadyPurchasedException("already purchased");

        assertEquals("already purchased", exception.getMessage());
    }
}
