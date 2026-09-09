package com.resqgrid.domain.history;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import com.resqgrid.domain.history.History;

import static org.junit.jupiter.api.Assertions.*;

class HistoryTest {

    @Test
    void shouldCreateHistoryWithValidData() {
        LocalDateTime occurredAt = LocalDateTime.now();

        History history = new History(
                1,
                101,
                "INCIDENT",
                "CREATED",
                occurredAt,
                "Incident was created"
        );

        assertNotNull(history);
        assertEquals(1, history.getId());
        assertEquals(101, history.getEntityId());
        assertEquals("INCIDENT", history.getEntityType());
        assertEquals("CREATED", history.getAction());
        assertEquals(occurredAt, history.getOccurredAt());
        assertEquals("Incident was created", history.getDescription());
    }

    @Test
    void shouldRejectInvalidHistoryId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new History(
                        0,
                        101,
                        "INCIDENT",
                        "CREATED",
                        LocalDateTime.now(),
                        "Incident was created"
                )
        );
    }

    @Test
    void shouldRejectInvalidEntityId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new History(
                        1,
                        0,
                        "INCIDENT",
                        "CREATED",
                        LocalDateTime.now(),
                        "Incident was created"
                )
        );
    }

    @Test
    void shouldRejectNullEntityType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new History(
                        1,
                        101,
                        null,
                        "CREATED",
                        LocalDateTime.now(),
                        "Incident was created"
                )
        );
    }

    @Test
    void shouldRejectBlankEntityType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new History(
                        1,
                        101,
                        "   ",
                        "CREATED",
                        LocalDateTime.now(),
                        "Incident was created"
                )
        );
    }

    @Test
    void shouldRejectNullAction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new History(
                        1,
                        101,
                        "INCIDENT",
                        null,
                        LocalDateTime.now(),
                        "Incident was created"
                )
        );
    }

    @Test
    void shouldRejectBlankAction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new History(
                        1,
                        101,
                        "INCIDENT",
                        "   ",
                        LocalDateTime.now(),
                        "Incident was created"
                )
        );
    }

    @Test
    void shouldRejectNullOccurredAt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new History(
                        1,
                        101,
                        "INCIDENT",
                        "CREATED",
                        null,
                        "Incident was created"
                )
        );
    }

    @Test
    void shouldRejectNullDescription() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new History(
                        1,
                        101,
                        "INCIDENT",
                        "CREATED",
                        LocalDateTime.now(),
                        null
                )
        );
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new History(
                        1,
                        101,
                        "INCIDENT",
                        "CREATED",
                        LocalDateTime.now(),
                        "   "
                )
        );
    }
}