package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DispatchStatusTest {

    @Test
    void shouldContainAllDocumentedStatuses() {
        assertEquals(3, DispatchStatus.values().length);
        assertEquals(DispatchStatus.CREATED, DispatchStatus.valueOf("CREATED"));
        assertEquals(DispatchStatus.IN_PROGRESS, DispatchStatus.valueOf("IN_PROGRESS"));
        assertEquals(DispatchStatus.COMPLETED, DispatchStatus.valueOf("COMPLETED"));
    }
}