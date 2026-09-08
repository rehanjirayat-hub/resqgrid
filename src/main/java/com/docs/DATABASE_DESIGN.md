# ResQGrid — Database Design

**Document:** Database Design
**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Development Baseline

---

# 1. Overview

ResQGrid uses PostgreSQL as its primary persistent data store.

The database is responsible for storing the current operational state of the system as well as the historical information required for dispatch tracking and reporting.

The database design is relational because the core ResQGrid data has strong relationships:

* an incident can have dispatches
* a resource can participate in dispatches
* dispatches connect incidents and resources
* resources have capabilities
* incidents and resources have locations
* history records changes involving these entities

The database should enforce important integrity rules rather than relying entirely on Java code.

---

# 2. Database Goals

The database should provide:

* reliable persistence
* referential integrity
* transactional consistency
* prevention of invalid relationships
* support for concurrent operations
* efficient lookup of active incidents and resources
* efficient dispatch history queries
* support for reporting
* a clean foundation for JDBC
* a clean foundation for Hibernate/JPA

---

# 3. Database Technology

| Item                           | Decision                               |
| ------------------------------ | -------------------------------------- |
| Database                       | PostgreSQL                             |
| Query language                 | SQL                                    |
| Initial persistence technology | JDBC                                   |
| Later persistence technology   | JPA / Hibernate                        |
| Primary key strategy           | Database-generated numeric identifiers |
| Transaction model              | PostgreSQL transactions                |
| Schema style                   | Relational                             |

No NoSQL database will be used for the initial ResQGrid implementation.

---

# 4. Initial Tables

The initial schema is expected to contain:

```text
incidents
resources
locations
capabilities
resource_capabilities
dispatches
response_teams
history
```

Some tables may be adjusted during implementation if the final domain model exposes a better relational structure.

---

# 5. Entity Relationship Overview

The conceptual relationship is:

```text
                         +-------------+
                         |  locations  |
                         +------+------+
                                |
                    +-----------+-----------+
                    |                       |
                    v                       v
             +-------------+         +-------------+
             |  incidents  |         |  resources  |
             +------+------+         +------+------+
                    |                       |
                    |                       |
                    v                       v
             +-------------+         +----------------------+
             |  dispatches |         | resource_capabilities|
             +------+------+         +----------+-----------+
                    |                           |
                    |                           v
                    |                    +-------------+
                    |                    | capabilities|
                    |                    +-------------+
                    |
                    v
                history

             response_teams
```

The exact foreign-key relationships will be finalized before the schema is implemented.

---

# 6. `locations`

The `locations` table stores geographical information used by incidents and resources.

## Proposed columns

| Column      | Type    | Constraints | Purpose                         |
| ----------- | ------- | ----------- | ------------------------------- |
| `id`        | BIGINT  | PK          | Unique location identifier      |
| `latitude`  | NUMERIC | NOT NULL    | Latitude                        |
| `longitude` | NUMERIC | NOT NULL    | Longitude                       |
| `address`   | VARCHAR | NULL        | Optional human-readable address |

## Validation

Latitude should represent a valid latitude.

Valid range:

```text
-90 to +90
```

Longitude should represent a valid longitude.

Valid range:

```text
-180 to +180
```

PostgreSQL `CHECK` constraints should be considered for these ranges.

---

# 7. `incidents`

The `incidents` table stores reported emergencies.

## Proposed columns

| Column        | Type      | Constraints  | Purpose                 |
| ------------- | --------- | ------------ | ----------------------- |
| `id`          | BIGINT    | PK           | Incident identifier     |
| `type`        | VARCHAR   | NOT NULL     | Incident type           |
| `description` | TEXT      | NOT NULL     | Incident description    |
| `severity`    | VARCHAR   | NOT NULL     | Incident severity       |
| `status`      | VARCHAR   | NOT NULL     | Current incident status |
| `location_id` | BIGINT    | FK, NOT NULL | Incident location       |
| `created_at`  | TIMESTAMP | NOT NULL     | Creation timestamp      |
| `updated_at`  | TIMESTAMP | NOT NULL     | Last update timestamp   |

---

# 8. Incident Constraints

The database should enforce basic structural integrity.

Examples:

```text
id cannot be null
type cannot be null
description cannot be null
severity cannot be null
status cannot be null
location_id cannot be null
created_at cannot be null
updated_at cannot be null
```

The application will also validate whether values such as severity and status are valid domain values.

Database-level checks may be introduced where they provide clear value.

---

# 9. Incident Status Storage

Incident status will initially be stored as a string value.

Expected values:

```text
REPORTED
ASSIGNED
IN_PROGRESS
RESOLVED
CANCELLED
```

Using strings keeps the database readable and avoids coupling the schema to Java enum ordinal positions.

We should not store Java enum ordinals such as:

```text
0
1
2
3
4
```

because changing enum ordering could corrupt the meaning of existing data.

---

# 10. Incident Severity Storage

Severity will also be stored as a string.

Expected values:

```text
CRITICAL
HIGH
MEDIUM
LOW
```

The Java domain model will provide the type-safe representation.

---

# 11. `resources`

The `resources` table stores emergency resources.

## Proposed columns

| Column        | Type      | Constraints  | Purpose                 |
| ------------- | --------- | ------------ | ----------------------- |
| `id`          | BIGINT    | PK           | Resource identifier     |
| `name`        | VARCHAR   | NOT NULL     | Resource name/code      |
| `type`        | VARCHAR   | NOT NULL     | Resource type           |
| `status`      | VARCHAR   | NOT NULL     | Current resource status |
| `location_id` | BIGINT    | FK, NOT NULL | Current location        |
| `created_at`  | TIMESTAMP | NOT NULL     | Creation timestamp      |
| `updated_at`  | TIMESTAMP | NOT NULL     | Last update timestamp   |

---

# 12. Resource Type

Initial resource types:

```text
AMBULANCE
FIRE_UNIT
RESCUE_TEAM
```

The database should store these as readable string values.

The Java application will represent them using an appropriate domain type, most likely an enum initially.

---

# 13. Resource Status

Initial resource statuses:

```text
AVAILABLE
BUSY
OFFLINE
MAINTENANCE
```

The database stores the current status.

The application controls valid state transitions.

---

# 14. `capabilities`

Capabilities are modeled separately because one resource may have multiple capabilities.

## Proposed columns

| Column        | Type    | Constraints      | Purpose                |
| ------------- | ------- | ---------------- | ---------------------- |
| `id`          | BIGINT  | PK               | Capability identifier  |
| `name`        | VARCHAR | UNIQUE, NOT NULL | Capability name        |
| `description` | TEXT    | NULL             | Capability description |

Example capability values:

```text
BASIC_MEDICAL
ADVANCED_MEDICAL
FIRE_SUPPRESSION
WATER_RESCUE
HEAVY_RESCUE
```

---

# 15. `resource_capabilities`

This table represents the many-to-many relationship between resources and capabilities.

## Proposed columns

| Column          | Type   | Constraints | Purpose    |
| --------------- | ------ | ----------- | ---------- |
| `resource_id`   | BIGINT | PK, FK      | Resource   |
| `capability_id` | BIGINT | PK, FK      | Capability |

The composite primary key:

```text
(resource_id, capability_id)
```

prevents the same capability from being assigned to the same resource more than once.

---

# 16. Resource Capability Relationship

Conceptually:

```text
Resource A
   |
   +---- BASIC_MEDICAL
   |
   +---- ADVANCED_MEDICAL
   |
   +---- WATER_RESCUE
```

Another resource may have a different set of capabilities.

This structure gives us a proper relational representation instead of storing multiple capability names in one column.

We should avoid a design such as:

```text
capabilities = 'MEDICAL,FIRE,WATER'
```

because that makes querying and enforcing relationships unnecessarily difficult.

---

# 17. `dispatches`

The `dispatches` table represents resource assignments to incidents.

## Proposed columns

| Column          | Type      | Constraints  | Purpose                   |
| --------------- | --------- | ------------ | ------------------------- |
| `id`            | BIGINT    | PK           | Dispatch identifier       |
| `incident_id`   | BIGINT    | FK, NOT NULL | Assigned incident         |
| `resource_id`   | BIGINT    | FK, NOT NULL | Assigned resource         |
| `status`        | VARCHAR   | NOT NULL     | Dispatch status           |
| `assigned_at`   | TIMESTAMP | NOT NULL     | Assignment time           |
| `dispatched_at` | TIMESTAMP | NULL         | Dispatch activation time  |
| `en_route_at`   | TIMESTAMP | NULL         | Resource began travelling |
| `arrived_at`    | TIMESTAMP | NULL         | Resource arrived          |
| `completed_at`  | TIMESTAMP | NULL         | Completion time           |
| `cancelled_at`  | TIMESTAMP | NULL         | Cancellation time         |
| `created_at`    | TIMESTAMP | NOT NULL     | Record creation time      |
| `updated_at`    | TIMESTAMP | NOT NULL     | Last update time          |

---

# 18. Dispatch Relationships

Each dispatch references:

* one incident
* one resource

Conceptually:

```text
Incident 1 ---- * Dispatch * ---- 1 Resource
```

This means:

* one incident can have multiple dispatch records over time
* one resource can have multiple dispatch records over time

Historical reuse of a resource is therefore supported.

---

# 19. Active Dispatch Problem

Historical dispatches must not prevent a resource from being reused later.

For example:

```text
Ambulance A
    |
    +-- Dispatch #1 COMPLETED
    +-- Dispatch #7 COMPLETED
    +-- Dispatch #15 ACTIVE
```

Dispatch #1 and #7 should not make Ambulance A unavailable.

Only an active conflicting dispatch should affect current availability.

---

# 20. Preventing Double Assignment

One of the most important database concerns is preventing two active dispatches from using the same resource simultaneously.

A simple unique constraint on:

```text
resource_id
```

would not work because historical dispatches must be allowed.

Instead, the final PostgreSQL implementation should consider a **partial unique index** covering only active dispatches.

Conceptually:

```text
One resource
+
active dispatch
=
at most one active assignment
```

The exact SQL will be written and tested during the JDBC/database implementation phase.

The active statuses must be clearly defined before creating this index.

---

# 21. Dispatch Status Storage

Initial dispatch statuses:

```text
CREATED
DISPATCHED
EN_ROUTE
ARRIVED
COMPLETED
CANCELLED
```

The database stores the current status.

The application controls valid transitions.

---

# 22. `response_teams`

The response team table exists to represent personnel/team information associated with emergency response operations.

## Initial proposed columns

| Column           | Type      | Constraints | Purpose             |
| ---------------- | --------- | ----------- | ------------------- |
| `id`             | BIGINT    | PK          | Team identifier     |
| `name`           | VARCHAR   | NOT NULL    | Team name           |
| `specialization` | VARCHAR   | NULL        | Team specialization |
| `status`         | VARCHAR   | NOT NULL    | Team status         |
| `created_at`     | TIMESTAMP | NOT NULL    | Creation time       |
| `updated_at`     | TIMESTAMP | NOT NULL    | Last update time    |

The exact relationship between teams and resources will be finalized before implementation.

We should not over-design personnel management at this stage.

---

# 23. `history`

The history table records important operational events.

## Proposed columns

| Column        | Type      | Constraints | Purpose                |
| ------------- | --------- | ----------- | ---------------------- |
| `id`          | BIGINT    | PK          | History identifier     |
| `incident_id` | BIGINT    | FK, NULL    | Related incident       |
| `resource_id` | BIGINT    | FK, NULL    | Related resource       |
| `dispatch_id` | BIGINT    | FK, NULL    | Related dispatch       |
| `event_type`  | VARCHAR   | NOT NULL    | Type of event          |
| `description` | TEXT      | NULL        | Human-readable context |
| `created_at`  | TIMESTAMP | NOT NULL    | Event time             |

Not every history event needs every foreign key.

For example:

```text
Incident created
-> incident_id populated
-> resource_id null
-> dispatch_id null
```

A dispatch status change might contain:

```text
dispatch_id populated
incident_id populated
resource_id populated
```

---

# 24. History Design Principle

History represents past events.

It should not be used to determine current state by repeatedly replaying all events.

Current state should remain directly available through:

* `incidents.status`
* `resources.status`
* `dispatches.status`

History exists primarily for:

* auditing
* troubleshooting
* operational timelines
* reporting
* understanding what happened

---

# 25. Foreign Keys

Foreign keys should enforce valid relationships.

Expected relationships include:

```text
incidents.location_id
        -> locations.id

resources.location_id
        -> locations.id

resource_capabilities.resource_id
        -> resources.id

resource_capabilities.capability_id
        -> capabilities.id

dispatches.incident_id
        -> incidents.id

dispatches.resource_id
        -> resources.id

history.incident_id
        -> incidents.id

history.resource_id
        -> resources.id

history.dispatch_id
        -> dispatches.id
```

The exact `ON DELETE` behavior will be chosen carefully.

---

# 26. Delete Strategy

Operational data should generally not be casually deleted.

For example, deleting a resource that has historical dispatch records could damage reporting and historical accuracy.

Therefore, the initial system should prefer:

* status changes
* deactivation
* OFFLINE/MAINTENANCE states

over destructive deletion of operational records.

Where deletion is allowed, foreign-key behavior must be explicitly defined.

---

# 27. Primary Keys

Tables will use database-generated numeric primary keys.

The application should not assume that IDs are sequential without gaps.

For example:

```text
1
2
3
7
8
```

is perfectly valid.

Deleted records, failed transactions, and PostgreSQL sequence behavior can result in gaps.

The identifier's purpose is uniqueness, not counting.

---

# 28. Timestamps

Operational timestamps should be stored for important events.

Examples:

* incident creation
* incident update
* dispatch assignment
* dispatch activation
* arrival
* completion
* cancellation

The database should use an appropriate timestamp type and a consistent time-handling strategy.

The application should avoid mixing local server time and database time carelessly.

The final implementation will establish a consistent timestamp policy.

---

# 29. Indexing Strategy

Indexes should support actual query patterns.

Likely indexes include:

### Incidents

* status
* severity
* created_at

### Resources

* status
* type

### Dispatches

* incident_id
* resource_id
* status
* assigned_at

### History

* incident_id
* resource_id
* dispatch_id
* created_at

### Capabilities

* name

We should not create indexes on every column automatically.

Indexes will be added based on expected queries and measured needs.

---

# 30. Candidate Resource Query

The Dispatch Engine will eventually need data that allows it to identify suitable resources.

A database query may first narrow candidates based on conditions such as:

```text
resource.status = AVAILABLE
```

and potentially:

```text
resource.type = required type
```

and capability relationships.

More advanced ranking may remain in Java if it requires business logic that is clearer outside SQL.

The final division between SQL filtering and Java ranking will be decided during Dispatch Engine Design.

---

# 31. Database vs Application Responsibilities

Not every rule belongs exclusively in the database.

## Database should enforce:

* primary keys
* foreign keys
* required values
* uniqueness
* basic structural constraints
* appropriate indexes
* transaction atomicity
* concurrency-related integrity constraints

## Application should enforce:

* business workflows
* state transition rules
* resource suitability
* dispatch scoring
* capability policies
* operational decisions

Some rules may intentionally be enforced in both places when doing so improves safety.

---

# 32. Transaction Example

A dispatch operation may involve several SQL statements.

Conceptually:

```text
BEGIN

1. Verify incident
2. Verify resource
3. Verify resource can be assigned
4. Create dispatch
5. Update resource status
6. Update incident status
7. Insert history

COMMIT
```

If an important operation fails:

```text
ROLLBACK
```

The database must not be left with a partial dispatch.

---

# 33. Concurrency Considerations

The database is an important part of concurrency protection.

The application may have multiple Java threads processing requests simultaneously.

For example:

```text
Thread A -> Ambulance A
Thread B -> Ambulance A
```

Both may see the resource as available.

The database design must ensure that concurrent transactions cannot permanently create conflicting active assignments.

Potential mechanisms include:

* row-level locking
* appropriate transaction isolation
* conditional updates
* partial unique indexes
* transaction retries where appropriate

The exact strategy will be chosen after we understand the JDBC transaction model.

---

# 34. JDBC Development Strategy

The JDBC phase will intentionally expose the SQL involved in important operations.

We should be able to understand queries such as:

```text
INSERT incident
SELECT resource candidates
INSERT dispatch
UPDATE resource
UPDATE incident
INSERT history
```

before allowing Hibernate/JPA to abstract those operations.

This is important for understanding:

* SQL
* transactions
* joins
* indexes
* constraints
* database locking
* performance

---

# 35. JPA / Hibernate Mapping Considerations

Later, the relational model will be mapped to Java entities.

Important topics will include:

* `@Entity`
* `@Id`
* `@GeneratedValue`
* `@ManyToOne`
* `@OneToMany`
* `@ManyToMany`
* `@JoinColumn`
* `mappedBy`
* cascade
* orphan removal
* fetch strategies

The database schema remains the relational source of truth.

JPA mappings should accurately represent the intended relationships rather than introducing accidental relationships.

---

# 36. Expected Cardinalities

The initial conceptual cardinalities are:

```text
Location
    |
    +---- 1 Incident
    |
    +---- 1 Resource

Incident
    |
    +---- 0..* Dispatch

Resource
    |
    +---- 0..* Dispatch

Resource
    |
    +---- 0..* Capability
              |
              +---- 0..* Resource

Incident
    |
    +---- 0..* History

Resource
    |
    +---- 0..* History

Dispatch
    |
    +---- 0..* History
```

The many-to-many resource/capability relationship is implemented through:

```text
resource_capabilities
```

---

# 37. Normalization

The database should avoid unnecessary duplication.

For example, capability names should not be repeated inside every resource row.

Instead:

```text
resources
    |
resource_capabilities
    |
capabilities
```

Similarly, location data should not be unnecessarily duplicated across incident and resource records when a reusable location representation makes sense.

The final schema should balance normalization with practical query requirements.

---

# 38. Seed Data

Development seed data should eventually include enough variety to test the dispatch engine.

Example resources:

```text
Ambulance A
    AVAILABLE
    BASIC_MEDICAL
    ADVANCED_MEDICAL

Ambulance B
    BUSY
    BASIC_MEDICAL

Fire Unit A
    AVAILABLE
    FIRE_SUPPRESSION

Rescue Team A
    AVAILABLE
    HEAVY_RESCUE
    WATER_RESCUE
```

Example incidents should cover different severities and required capabilities.

Seed data should be used for development/testing, not treated as production data.

---

# 39. Reporting Requirements

The schema should support queries such as:

### Incident statistics

```text
Count incidents by severity
Count incidents by status
Count incidents over a time range
```

### Resource statistics

```text
Count resources by status
Count resources by type
```

### Dispatch statistics

```text
Count dispatches by status
Count dispatches by resource
Count dispatches over time
Calculate response durations
```

The reporting layer will define the actual SQL queries later.

---

# 40. Schema Evolution

The database schema will evolve through deliberate migrations.

Schema changes should be:

* documented
* reproducible
* version-controlled
* tested

We should not rely on manually changing the development database without recording the change.

A migration approach will be selected before the database implementation grows beyond the initial schema.

---

# 41. Database Integrity Principles

The database should make invalid states difficult to represent.

Important examples:

```text
No incident without valid location
No dispatch without incident
No dispatch without resource
No duplicate resource-capability relationship
No duplicate active resource assignment
```

Application validation remains necessary, but the database should provide a second layer of protection for important integrity rules.

---

# 42. Initial Schema Summary

The initial relational model is:

```text
locations
    |
    +---- incidents
    |
    +---- resources

resources
    |
    +---- resource_capabilities ---- capabilities
    |
    +---- dispatches
    |
    +---- history

incidents
    |
    +---- dispatches
    |
    +---- history

dispatches
    |
    +---- history

response_teams
```

---

# 43. Deferred Database Decisions

The following are intentionally not finalized yet:

* exact PostgreSQL DDL
* exact foreign-key `ON DELETE` actions
* exact active-dispatch status set
* exact partial unique index definition
* timestamp/time-zone strategy
* response-team/resource relationship
* migration tooling
* advanced geographic database features

These will be resolved before the relevant implementation phase.

---

# 44. Database Design Principles

The ResQGrid database should follow these principles:

1. PostgreSQL remains the source of persistent relational data.
2. Relationships are represented using proper foreign keys.
3. Many-to-many relationships use junction tables.
4. Operational history is preserved.
5. Current state is stored directly.
6. Important integrity rules are enforced by the database where practical.
7. Transactions protect multi-step business operations.
8. Indexes are driven by real query patterns.
9. SQL should remain understandable to the developer.
10. JPA should map to a deliberate relational design rather than replace database understanding.

---

# 45. Document Status

**Document 07 — Database Design**

Status: Completed as the initial relational database baseline.

This document will be used when we begin:

* PostgreSQL setup
* SQL schema creation
* JDBC implementation
* transaction design
* concurrency implementation
* Hibernate/JPA mapping
* reporting queries