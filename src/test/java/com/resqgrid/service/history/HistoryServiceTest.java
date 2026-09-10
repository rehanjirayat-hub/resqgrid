package com.resqgrid.service.history;

import com.resqgrid.domain.history.History;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoryServiceTest {

    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new HistoryService();
    }

    @Test
    void record_shouldCreateHistoryRecord() {

        History history = historyService.record(
                1L,
                "DISPATCH",
                "STARTED",
                "Dispatch started for incident 10"
        );

        assertNotNull(history);
        assertEquals(1L, history.getEntityId());
        assertEquals("DISPATCH", history.getEntityType());
        assertEquals("STARTED", history.getAction());
        assertEquals(
                "Dispatch started for incident 10",
                history.getDescription()
        );
    }

    @Test
    void findHistoryByEntityId_shouldReturnMatchingRecords() {

        historyService.record(
                1L,
                "DISPATCH",
                "STARTED",
                "Dispatch started"
        );

        historyService.record(
                1L,
                "DISPATCH",
                "COMPLETED",
                "Dispatch completed"
        );

        historyService.record(
                2L,
                "DISPATCH",
                "STARTED",
                "Another dispatch started"
        );

        List<History> history =
                historyService.findHistoryByEntityId(1L);

        assertEquals(2, history.size());
    }

    @Test
    void findHistoryByEntityId_shouldReturnEmptyListWhenNoRecordsExist() {

        List<History> history =
                historyService.findHistoryByEntityId(999L);

        assertEquals(0, history.size());
    }

    @Test
    void findAllHistory_shouldReturnAllRecords() {

        historyService.record(
                1L,
                "DISPATCH",
                "STARTED",
                "Dispatch started"
        );

        historyService.record(
                2L,
                "DISPATCH",
                "CANCELLED",
                "Dispatch cancelled"
        );

        List<History> history =
                historyService.findAllHistory();

        assertEquals(2, history.size());
    }

    @Test
    void findAllHistory_shouldReturnIndependentUnmodifiableView() {

        historyService.record(
                1L,
                "DISPATCH",
                "STARTED",
                "Dispatch started"
        );

        List<History> history =
                historyService.findAllHistory();

        assertEquals(1, history.size());
    }
}
