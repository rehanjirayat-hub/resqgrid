package com.resqgrid.domain.history;

import java.time.LocalDateTime;

public class History {

    private final long id;
    private final long entityId;
    private final String entityType;
    private final String action;
    private final LocalDateTime occurredAt;
    private final String description;

    public History(
            long id,
            long entityId,
            String entityType,
            String action,
            LocalDateTime occurredAt,
            String description
    ) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "History ID must be greater than zero"
            );
        }

        if (entityId <= 0) {
            throw new IllegalArgumentException(
                    "Entity ID must be greater than zero"
            );
        }

        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException(
                    "Entity type cannot be blank"
            );
        }

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "Action cannot be blank"
            );
        }

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "Occurred time cannot be null"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be blank"
            );
        }

        this.id = id;
        this.entityId = entityId;
        this.entityType = entityType;
        this.action = action;
        this.occurredAt = occurredAt;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public long getEntityId() {
        return entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", entityId=" + entityId +
                ", entityType='" + entityType + '\'' +
                ", action='" + action + '\'' +
                ", occurredAt=" + occurredAt +
                ", description='" + description + '\'' +
                '}';
    }
}