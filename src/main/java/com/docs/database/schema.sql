```sql
-- ============================================================
-- ResQGrid Database Schema
-- Database: resqgrid
-- Database Engine: PostgreSQL
-- ============================================================


-- ============================================================
-- 1. LOCATIONS
-- ============================================================

CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    address VARCHAR(500) NOT NULL,

    CONSTRAINT chk_location_latitude
        CHECK (latitude BETWEEN -90 AND 90),

    CONSTRAINT chk_location_longitude
        CHECK (longitude BETWEEN -180 AND 180)
);


-- ============================================================
-- 2. INCIDENTS
-- ============================================================

CREATE TABLE incidents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    location_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    reported_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_incident_location
        FOREIGN KEY (location_id)
        REFERENCES locations(id),

    CONSTRAINT chk_incident_type
        CHECK (
            type IN (
                'MEDICAL',
                'FIRE',
                'RESCUE',
                'WATER_RESCUE',
                'HAZARDOUS_MATERIAL'
            )
        ),

    CONSTRAINT chk_incident_severity
        CHECK (
            severity IN (
                'CRITICAL',
                'HIGH',
                'MEDIUM',
                'LOW'
            )
        ),

    CONSTRAINT chk_incident_status
        CHECK (
            status IN (
                'REPORTED',
                'ASSESSED',
                'DISPATCHED',
                'IN_PROGRESS',
                'RESOLVED',
                'CANCELLED'
            )
        )
);


-- ============================================================
-- 3. EMERGENCY RESOURCES
-- ============================================================

CREATE TABLE emergency_resources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    location_id BIGINT NOT NULL,

    CONSTRAINT fk_resource_location
        FOREIGN KEY (location_id)
        REFERENCES locations(id),

    CONSTRAINT chk_resource_type
        CHECK (
            type IN (
                'AMBULANCE',
                'FIRE_UNIT',
                'RESCUE_TEAM',
                'POLICE_UNIT',
                'MEDICAL_TEAM',
                'HELICOPTER'
            )
        ),

    CONSTRAINT chk_resource_status
        CHECK (
            status IN (
                'AVAILABLE',
                'BUSY',
                'OFFLINE',
                'MAINTENANCE'
            )
        )
);


-- ============================================================
-- 4. RESOURCE CAPABILITIES
-- ============================================================

CREATE TABLE resource_capabilities (
    resource_id BIGINT NOT NULL,
    capability VARCHAR(50) NOT NULL,

    PRIMARY KEY (resource_id, capability),

    CONSTRAINT fk_resource_capability_resource
        FOREIGN KEY (resource_id)
        REFERENCES emergency_resources(id),

    CONSTRAINT chk_resource_capability
        CHECK (
            capability IN (
                'MEDICAL_RESPONSE',
                'FIRE_RESPONSE',
                'RESCUE',
                'HAZARDOUS_MATERIALS',
                'WATER_RESCUE'
            )
        )
);


-- ============================================================
-- 5. RESPONSE TEAMS
-- ============================================================

CREATE TABLE response_teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL,

    CONSTRAINT chk_team_status
        CHECK (
            status IN (
                'AVAILABLE',
                'DEPLOYED',
                'OFFLINE'
            )
        )
);


-- ============================================================
-- 6. TEAM CAPABILITIES
-- ============================================================

CREATE TABLE team_capabilities (
    team_id BIGINT NOT NULL,
    capability VARCHAR(50) NOT NULL,

    PRIMARY KEY (team_id, capability),

    CONSTRAINT fk_team_capability_team
        FOREIGN KEY (team_id)
        REFERENCES response_teams(id),

    CONSTRAINT chk_team_capability
        CHECK (
            capability IN (
                'MEDICAL_RESPONSE',
                'FIRE_RESPONSE',
                'RESCUE',
                'HAZARDOUS_MATERIALS',
                'WATER_RESCUE'
            )
        )
);


-- ============================================================
-- 7. DISPATCHES
-- ============================================================

CREATE TABLE dispatches (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    resource_id BIGINT,
    team_id BIGINT,
    status VARCHAR(30) NOT NULL,
    dispatched_at TIMESTAMP,

    CONSTRAINT fk_dispatch_incident
        FOREIGN KEY (incident_id)
        REFERENCES incidents(id),

    CONSTRAINT fk_dispatch_resource
        FOREIGN KEY (resource_id)
        REFERENCES emergency_resources(id),

    CONSTRAINT fk_dispatch_team
        FOREIGN KEY (team_id)
        REFERENCES response_teams(id),

    CONSTRAINT chk_dispatch_status
        CHECK (
            status IN (
                'PENDING',
                'ASSIGNED',
                'IN_PROGRESS',
                'COMPLETED',
                'CANCELLED'
            )
        )
);


-- ============================================================
-- 8. HISTORY
-- ============================================================

CREATE TABLE history (
    id BIGSERIAL PRIMARY KEY,
    entity_id BIGINT NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    description TEXT NOT NULL
);


-- ============================================================
-- 9. INDEXES
-- ============================================================

CREATE INDEX idx_incidents_status
    ON incidents(status);

CREATE INDEX idx_incidents_severity
    ON incidents(severity);

CREATE INDEX idx_resources_status
    ON emergency_resources(status);

CREATE INDEX idx_resources_type
    ON emergency_resources(type);

CREATE INDEX idx_dispatches_incident
    ON dispatches(incident_id);

CREATE INDEX idx_dispatches_resource
    ON dispatches(resource_id);

CREATE INDEX idx_history_entity
    ON history(entity_id);
```
