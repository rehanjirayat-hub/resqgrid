package com.resqgrid.service.history;

import com.resqgrid.domain.history.History;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoryService {

    private final List<History> historyRecords;

    public HistoryService() {
        this.historyRecords = new ArrayList<>();
    }

    public History record(
            long entityId,
            String entityType,
            String action,
            String description
    ) {

        History history = new History(
                System.currentTimeMillis(),
                entityId,
                entityType,
                action,
                LocalDateTime.now(),
                description
        );

        historyRecords.add(history);

        return history;
    }

    public List<History> findHistoryByEntityId(long entityId) {

        return historyRecords.stream()
                .filter(history -> history.getEntityId() == entityId)
                .toList();
    }

    public List<History> findAllHistory() {

        return List.copyOf(historyRecords);
    }
}
