# ResQGrid — Database Design

## 1. Purpose

This document defines the relational database design for ResQGrid.

The database must support:

* Incident management.
* Emergency resource management.
* Response-team management.
* Resource capabilities.
* Locations.
* Dispatch records.
* Incident and resource status tracking.
* Dispatch history.
* Reporting and operational queries.
* Concurrent resource assignment.
* Data integrity.

The database will use **PostgreSQL**.

SQL is the required database language.

---

# 2. Database Technology

## 2.1 Database

PostgreSQL.

## 2.2 Database Access

The project will demonstrate:

* JDBC.
* JPA.
* Hibernate.

Both approaches must operate against the same PostgreSQL relational model.

## 2.3 Database Principle

The database is responsible for persistent data storage and relational integrity.

Business decisions such as resource ranking belong to the application/business layer unless a documented database operation specifically requires database-side logic.

---

# 3. Core Tables

The initial relational model contains the following core concepts:

```text
Location
Incident
Emergency Resource
Response Team
Capability
Resource Capability
Team Capability
Dispatch
```

The exact physical table names may use conventional SQL naming such as:

```text
locations
incidents
resources
response_teams
capabilities
resource_capabilities
team_capabilities
dispatches
```

Final physical naming must remain consistent across JDBC and JPA/Hibernate implementations.

---

# 4. Location

The Location concept represents a geographical location associated with incidents and resources.

A location may be referenced by:

* Multiple incidents.
* Multiple resources.

Conceptually:

```text
Location
   |
   +---- Incident
   |
   +---- Resource
```

The exact location fields must support the documented requirement for determining distance between incidents and resources.

The database design must therefore support geographical coordinates.

The exact coordinate representation and distance calculation approach will be finalized during implementation design.

---

# 5. Incident

The Incident table stores emergency incidents reported to ResQGrid.

An incident must support the domain requirements defined in the Domain Model and Business Rules documents.

Conceptually it contains information representing:

* Identity.
* Incident information.
* Severity.
* Status.
* Location.
* Relevant timestamps.

Controlled severity values are:

```text
CRITICAL
HIGH
MEDIUM
LOW
```

Controlled incident lifecycle states are:

```text
REPORTED
ASSESSED
REQUIREMENTS_DETERMINED
RESPONSE_IN_PROGRESS
RESOLVED
```

The database must prevent invalid values where appropriate.

---

# 6. Emergency Resource

The Resource table represents operational emergency resources.

Examples include:

* Ambulance.
* Fire Unit.
* Rescue Team.

Controlled resource types are:

```text
AMBULANCE
FIRE_UNIT
RESCUE_TEAM
```

Controlled resource statuses are:

```text
AVAILABLE
BUSY
OFFLINE
MAINTENANCE
```

A resource must contain enough persistent information to support:

* Identification.
* Resource type.
* Current status.
* Current location.
* Operational information required by documented dispatch rules.
* Historical dispatch relationships.

---

# 7. Response Team

The Response Team table represents teams that may participate in emergency response.

A team may participate in multiple historical dispatches.

Team information must support the documented requirement for determining whether the team is available for assignment.

The exact team fields will be finalized during implementation design.

---

# 8. Capability

Capabilities represent skills or operational capabilities that can be required by an incident and possessed by resources or teams.

Examples may include:

* Medical response.
* Fire suppression.
* Rescue capability.

The examples are illustrative.

The actual initial capability catalogue must be based on documented project requirements and should not be expanded arbitrarily during implementation.

---

# 9. Resource Capability

A resource may have zero or more capabilities.

A capability may belong to multiple resources.

Therefore the relationship is:

```text
Resource  * ---- *  Capability
```

This requires a junction/association table.

Conceptually:

```text
resource_capabilities
---------------------
resource_id
capability_id
```

The association must enforce appropriate relational integrity.

---

# 10. Team Capability

A response team may have zero or more capabilities.

A capability may belong to multiple teams.

Therefore:

```text
Response Team  * ---- *  Capability
```

This requires a junction/association table.

Conceptually:

```text
team_capabilities
-----------------
team_id
capability_id
```

The association must enforce appropriate relational integrity.

---

# 11. Dispatch

The Dispatch table is one of the most important tables in ResQGrid.

A dispatch represents a historical assignment/response relationship involving:

* An incident.
* A resource.
* A response team where applicable.
* Dispatch status.
* Relevant timestamps.

Conceptually:

```text
Incident
   |
   +---- Dispatch ---- Resource
              |
              +------ Response Team
```

One incident may have zero or more dispatch records.

One resource may have zero or more historical dispatch records.

One team may have zero or more historical dispatch records.

---

# 12. Dispatch Status

The initial conceptual dispatch states are:

```text
CREATED
IN_PROGRESS
COMPLETED
```

The database must represent dispatch status consistently with the Domain Model.

The application must enforce valid lifecycle transitions.

---

# 13. Primary Keys

Every persistent entity must have a stable primary key.

Initial persistent entities requiring identifiers include:

* Location.
* Incident.
* Resource.
* Response Team.
* Capability.
* Dispatch.

Primary-key generation strategy must be compatible with:

* PostgreSQL.
* JDBC.
* JPA/Hibernate.

The exact ID data type and generation strategy will be finalized before entity implementation.

No implementation should assume a generated ID is already available before persistence unless the documented ID strategy explicitly guarantees it.

---

# 14. Foreign Keys

Relationships between persistent entities must be represented using foreign keys.

Expected relationships include:

```text
incidents -> locations
resources -> locations

dispatches -> incidents
dispatches -> resources
dispatches -> response_teams

resource_capabilities -> resources
resource_capabilities -> capabilities

team_capabilities -> response_teams
team_capabilities -> capabilities
```

Foreign keys must protect referential integrity.

---

# 15. Relationship Cardinality

The relational model must reflect the Domain Model.

### Location → Incident

One location may be associated with many incidents.

### Location → Resource

One location may be associated with many resources.

### Incident → Dispatch

One incident may have zero or more dispatch records.

### Resource → Dispatch

One resource may have zero or more historical dispatch records.

### Response Team → Dispatch

One response team may have zero or more historical dispatch records.

### Resource ↔ Capability

Many-to-many.

### Response Team ↔ Capability

Many-to-many.

---

# 16. Nullability

Nullability must reflect domain requirements.

Fields that are mandatory according to business rules must not unnecessarily allow null values.

Fields that are genuinely optional may allow null values.

The final nullability rules must be documented before the corresponding database schema and entities are implemented.

---

# 17. Constraints

The database should enforce data integrity wherever the rule can be represented reliably at the database level.

Potential constraints include:

* Primary-key constraints.
* Foreign-key constraints.
* Not-null constraints.
* Unique constraints where required.
* Check constraints for controlled values where appropriate.

Application-level business rules must still be enforced by the application.

Database constraints are not a replacement for business logic.

---

# 18. Controlled Values

The following controlled values originate from the Domain Model.

## Incident Severity

```text
CRITICAL
HIGH
MEDIUM
LOW
```

Priority order:

```text
CRITICAL > HIGH > MEDIUM > LOW
```

## Incident Status

```text
REPORTED
ASSESSED
REQUIREMENTS_DETERMINED
RESPONSE_IN_PROGRESS
RESOLVED
```

## Resource Type

```text
AMBULANCE
FIRE_UNIT
RESCUE_TEAM
```

## Resource Status

```text
AVAILABLE
BUSY
OFFLINE
MAINTENANCE
```

## Dispatch Status

```text
CREATED
IN_PROGRESS
COMPLETED
```

The physical PostgreSQL representation of these controlled values will be decided consistently for both JDBC and JPA/Hibernate.

---

# 19. Resource Assignment Integrity

The database must support the core invariant:

> A resource must not be simultaneously assigned to conflicting active dispatches.

This is especially important for ambulances and other limited resources.

The application must perform final assignment verification as documented in the Concurrency Design.

Database-level protections must be used where appropriate.

The exact concurrency mechanism is intentionally deferred to:

`09-CONCURRENCY-DESIGN.md`

---

# 20. Dispatch History

Dispatch records must be retained as historical operational information.

Completed dispatches must not simply disappear when a resource becomes available again.

Historical dispatch information supports:

* Resource workload.
* Operational reporting.
* Incident history.
* Dispatch history.
* Performance analysis.

---

# 21. Current State vs Historical State

The database must distinguish between:

### Current operational state

Examples:

* Current resource status.
* Current resource location.
* Current incident status.
* Current team availability.

### Historical state

Examples:

* Previous dispatches.
* Completed responses.
* Historical resource assignments.
* Historical incident responses.

Historical information must not be lost merely because the current state changes.

---

# 22. Indexing Strategy

Indexes should be created for fields frequently used for:

* Resource lookup.
* Availability filtering.
* Incident lookup.
* Dispatch lookup.
* Foreign-key joins.
* Historical reporting.
* Concurrency-sensitive queries.

Initial indexing decisions must be based on actual query patterns.

Indexes must not be added randomly.

The final index list will be documented before database implementation.

---

# 23. Query Requirements

The database design must support queries required by the application.

Important query categories include:

### Incident Queries

* Find incident by ID.
* Find incidents by status.
* Find incidents by severity.
* Retrieve incident history.

### Resource Queries

* Find resource by ID.
* Find available resources.
* Filter resources by type.
* Find resources by capability.
* Retrieve resource dispatch history.
* Determine resource workload.

### Team Queries

* Find available teams.
* Find teams by capability.
* Retrieve team dispatch history.

### Dispatch Queries

* Find dispatches for an incident.
* Find dispatches for a resource.
* Find active dispatches.
* Retrieve completed dispatch history.

### Reporting Queries

* Incident statistics.
* Dispatch statistics.
* Resource utilization.
* Workload information.

---

# 24. Distance and Location Queries

The dispatch engine requires distance information when ranking eligible resources.

The database must store sufficient location information to support this requirement.

The initial design does not require a specialized geospatial database extension.

Distance calculation may be performed by application logic using persisted coordinates unless later documentation explicitly introduces a PostgreSQL geospatial capability.

No additional geospatial technology is to be introduced without documentation approval.

---

# 25. Transaction Requirements

Database operations that modify related state must be transactionally consistent.

Examples include:

```text
Create Dispatch
+
Change Resource Status
```

and concurrency-sensitive assignment operations.

A successful business operation must not leave only part of its required persistent changes committed.

A failed operation must not leave invalid partial state.

Exact transaction boundaries will be defined together with the concurrency design.

---

# 26. JDBC Requirements

JDBC implementation must:

* Use parameterized SQL.
* Avoid SQL injection vulnerabilities.
* Properly manage database resources.
* Handle transactions correctly.
* Map query results consistently.
* Respect the relational model defined in this document.

JDBC SQL must not be scattered throughout unrelated business classes.

---

# 27. JPA/Hibernate Requirements

JPA/Hibernate implementation must map the documented relational model.

The project will explicitly study:

* Entity relationships.
* One-to-many relationships.
* Many-to-many relationships.
* Relationship ownership.
* `mappedBy`.
* `JoinColumn`.
* Join tables.
* Cascade behavior.
* Orphan removal.
* Fetching strategies.
* Persistence context.
* Entity lifecycle.
* Dirty checking.
* First-level cache.
* Querying.
* Transactions.

Exact mapping decisions must be documented before implementation.

---

# 28. ORM Performance Requirements

The persistence design must consider:

* N+1 query problems.
* Lazy loading.
* Eager loading.
* Fetch joins.
* Query count.
* Transaction boundaries.
* Persistence-context behavior.

Performance optimization must be based on actual access patterns rather than blindly making all relationships eager.

---

# 29. Database Security

Database access must follow secure practices.

Requirements include:

* No credentials committed to Git.
* No hard-coded production credentials.
* Parameterized JDBC queries.
* Appropriate database user permissions.
* Environment-based configuration for sensitive values.

---

# 30. Schema Evolution

Database schema changes must be deliberate and documented.

A schema change that affects domain relationships, business rules, or application behavior must first be reflected in the relevant project documentation.

No undocumented database column should be introduced merely because it appears convenient during implementation.

---

# 31. Database Design Boundaries

This document intentionally does not finalize:

* Exact SQL `CREATE TABLE` statements.
* Exact column names for every field.
* Exact column data types.
* Exact primary-key generation mechanism.
* Exact enum storage strategy.
* Exact JPA annotations.
* Exact Hibernate mappings.
* Exact indexes.
* Exact transaction implementation.
* Exact locking mechanism.
* Exact dispatch scoring data storage.

These decisions must be finalized before their implementation stage and must remain consistent with all project documentation.

---

# 32. Documentation Consistency

This document must remain consistent with:

* `01-PROJECT-OVERVIEW.md`
* `02-REQUIREMENTS-SPECIFICATION.md`
* `03-ACTORS-AND-USE-CASES.md`
* `04-BUSINESS-RULES.md`
* `05-DOMAIN-MODEL.md`
* `06-SYSTEM-ARCHITECTURE.md`
* `08-DISPATCH-ENGINE-DESIGN.md`
* `09-CONCURRENCY-DESIGN.md`
* `10-API-DESIGN.md`
* `11-ERROR-HANDLING-DESIGN.md`
* `12-TESTING-STRATEGY.md`
* `13-DEVELOPMENT-ROADMAP.md`

If a conflict is discovered, implementation must stop.

The relevant documentation must be reviewed and updated before coding continues.

The documentation remains the source of truth for the project.
