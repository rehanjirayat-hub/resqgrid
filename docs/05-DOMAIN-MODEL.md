# ResQGrid — Domain Model

## 1. Purpose

This document defines the core domain model of ResQGrid.

It identifies the major business entities, value concepts, enumerations, responsibilities, attributes, and relationships required by the documented requirements and business rules.

This document is the authoritative source for the domain model.

Java classes, entity classes, database structures, JPA mappings, repositories, services, and APIs must be derived from this document.

No undocumented domain field, relationship, status, or entity may be introduced during implementation.

---

# 2. Core Domain Concepts

The initial ResQGrid domain consists of:

1. Incident
2. Emergency Resource
3. Response Team
4. Dispatch
5. Location
6. Resource Capability

The domain also uses controlled values for:

* Incident Severity
* Incident Status
* Resource Status
* Resource Type
* Resource Capability
* Dispatch Status

---

# 3. Incident

## 3.1 Responsibility

An Incident represents an emergency situation requiring assessment and potentially one or more emergency-response resources.

An Incident is responsible for representing information about the emergency and its operational lifecycle.

## 3.2 Required Information

An incident must contain information sufficient to:

* Identify the incident.
* Describe the emergency.
* Determine its severity.
* Identify its location.
* Track its operational status.
* Support response-resource requirements.
* Track relevant timestamps.

The exact technical representation of each attribute will be finalized during implementation design and database design.

## 3.3 Incident Characteristics

An incident may have:

* A unique identity.
* Description/information about the emergency.
* Severity.
* Status.
* Location.
* Required resource characteristics.
* Relevant timestamps.

## 3.4 Incident Responsibility Boundary

The Incident represents the emergency itself.

It must not contain:

* Database-access logic.
* HTTP request handling.
* JSP presentation logic.
* Dispatch-ranking implementation.
* Infrastructure logic.

---

# 4. Incident Severity

Incident severity represents the urgency and operational priority of an incident.

Initial values:

* `CRITICAL`
* `HIGH`
* `MEDIUM`
* `LOW`

Priority ordering:

**CRITICAL > HIGH > MEDIUM > LOW**

Severity influences dispatch priority but does not override mandatory resource eligibility requirements.

For example, a CRITICAL incident cannot use an incompatible resource simply because the incident has the highest priority.

---

# 5. Incident Status

Incident status represents the current operational stage of an incident.

The documented conceptual lifecycle is:

**REPORTED**

→ **ASSESSED**

→ **REQUIREMENTS_DETERMINED**

→ **RESPONSE_IN_PROGRESS**

→ **RESOLVED**

The final Java/database enum representation and any additional states required to represent documented failure or waiting conditions must be finalized before implementation in the relevant design documentation.

Invalid transitions must be rejected.

---

# 6. Emergency Resource

## 6.1 Responsibility

An Emergency Resource represents a physical or operational response resource that may be assigned to an incident.

Examples include:

* Ambulance
* Fire Unit
* Rescue Team

## 6.2 Resource Characteristics

A resource may have:

* A unique identity.
* Resource type.
* Operational status.
* Location.
* Capabilities.
* Current assignment information.
* Workload-related information.

## 6.3 Resource Responsibility Boundary

The Emergency Resource represents the resource and its operational state.

It must not contain:

* Database-access logic.
* HTTP handling.
* JSP logic.
* Dispatch-ranking algorithms.
* Infrastructure concerns.

---

# 7. Resource Type

Initial resource types are:

* `AMBULANCE`
* `FIRE_UNIT`
* `RESCUE_TEAM`

Resource type identifies the general category of an emergency resource.

Resource type is one factor used when determining whether a resource can respond to an incident.

Resource type alone does not determine final dispatch selection.

---

# 8. Resource Status

Initial resource statuses are:

* `AVAILABLE`
* `BUSY`
* `OFFLINE`
* `MAINTENANCE`

Meaning:

### AVAILABLE

The resource is operationally available for consideration.

### BUSY

The resource is currently engaged in an active assignment and cannot accept a conflicting assignment.

### OFFLINE

The resource is not operationally available.

### MAINTENANCE

The resource is unavailable because it is undergoing maintenance or is otherwise removed from normal operational use.

A resource's status must remain consistent with its active assignments.

---

# 9. Resource Capability

## 9.1 Responsibility

A Resource Capability represents an operational ability possessed by a resource.

Capabilities are used to determine whether a resource satisfies an incident's mandatory requirements.

Capability matching occurs during resource eligibility filtering before final resource ranking.

A capability is a controlled domain value.

## 9.2 Initial Capability Catalogue

The initial documented capability values are:

* `MEDICAL_RESPONSE`
* `FIRE_RESPONSE`
* `RESCUE_OPERATION`

These values represent the initial capability catalogue for the project.

## 9.3 Capability Meaning

### MEDICAL_RESPONSE

Represents the ability to perform emergency medical-response operations.

### FIRE_RESPONSE

Represents the ability to perform emergency fire-response operations.

### RESCUE_OPERATION

Represents the ability to perform emergency rescue operations.

## 9.4 Capability Rules

A resource may possess zero or more capabilities.

If an incident requires a mandatory capability, the resource must possess that capability before it can be considered eligible.

A resource that does not satisfy a mandatory capability requirement must not be considered eligible for that requirement.

Capability matching is an eligibility condition and must occur before final ranking.

Capability suitability may also be considered during ranking where the Dispatch Engine Design explicitly defines such behavior, but ranking must never make an otherwise ineligible resource eligible.

## 9.5 Capability Representation

Resource capabilities are represented as controlled domain values.

The initial Java representation will use an enum corresponding to the documented capability values.

The persistence representation will be defined consistently in the Database Design documentation.

## 9.6 Capability Catalogue Changes

The initial capability catalogue may be expanded later only when the corresponding documentation is updated before implementation.

No additional capability value may be invented during coding.

---

## 10. Response Team 

### 10.1 Responsibility

The Response Team represents a group of personnel or operational members that may participate in emergency response operations.

A Response Team is responsible for representing the team's operational state and capabilities relevant to emergency response.

A Response Team must not contain:

* Database access logic.
* HTTP or REST handling.
* Servlet/JSP logic.
* Dispatch-ranking logic.
* Persistence-specific operations.

---

### 10.2 Initial Characteristics

Each Response Team shall have the following domain characteristics:

* Identity.
* Operational status.
* Capabilities.
* Current assignments.

These characteristics represent the team's current operational state.

---

### 10.3 Team Identity

Each Response Team shall have a unique numeric identity.

The identity is used to distinguish one Response Team from another and to associate the team with operational records such as dispatches.

The exact persistence identifier strategy is defined separately by the Database Design and implementation architecture.

---

### 10.4 Team Operational Status

Response Team operational status is a controlled domain value.

The initial operational statuses are:

* AVAILABLE
* BUSY
* OFFLINE
* MAINTENANCE

#### AVAILABLE

The team is operational and may be considered for assignment when all other eligibility requirements are satisfied.

#### BUSY

The team is currently engaged in an assignment and must not be selected for a conflicting dispatch.

#### OFFLINE

The team is not currently available for normal dispatch operations.

#### MAINTENANCE

The team is temporarily unavailable for normal dispatch operations because of maintenance or an equivalent operational restriction.

The Java representation of Response Team operational status shall be an enum.

The exact persistence representation is defined by the Database Design and persistence implementation.

---

### 10.5 Team Capabilities

A Response Team may possess zero or more documented Resource Capabilities.

The initial capabilities are:

* MEDICAL_RESPONSE
* FIRE_RESPONSE
* RESCUE_OPERATION

Capabilities determine whether a Response Team satisfies capability requirements associated with an emergency response operation.

Capability matching must occur before ranking or final selection.

If a mandatory capability is required and the Response Team does not possess that capability, the team is not eligible for the operation.

The Java representation shall use the existing `ResourceCapability` enum defined by the domain model.

The relationship between Response Team and Resource Capability is many-to-many.

The persistence representation is defined by the Database Design.


# 11. Location

## 11.1 Responsibility

Location represents the geographic position relevant to an incident, resource, or other domain object where required.

Location exists primarily to support:

* Incident location.
* Resource location.
* Distance-based dispatch decisions.

## 11.2 Location Characteristics

The location model must contain sufficient geographic information to calculate distance between relevant locations.

The exact representation and coordinate system must be defined in the Database Design and Dispatch Engine Design documents.

## 11.3 Location Responsibility Boundary

Location represents geographic information.

Distance calculations and dispatch decisions must remain outside the basic location data representation unless the architecture documentation explicitly assigns such responsibility there.

---

# 12. Dispatch

## 12.1 Responsibility

A Dispatch represents the operational assignment of a response resource to an incident.

It records the relationship between an emergency incident and the resource selected to respond.

Where required, it also represents the response-team assignment.

## 12.2 Dispatch Characteristics

A dispatch may contain:

* Unique identity.
* Incident association.
* Resource association.
* Response-team association where applicable.
* Dispatch status.
* Creation timestamp.
* Start timestamp where applicable.
* Completion timestamp where applicable.
* Other operational information required for dispatch history.

## 12.3 Dispatch Responsibility Boundary

Dispatch represents an actual assignment.

It must not independently decide which resource should be selected.

Resource filtering and ranking belong to the dispatch/business engine.

---

# 13. Dispatch Status

Dispatch status represents the lifecycle of an assignment.

The initial conceptual states are:

* `CREATED`
* `IN_PROGRESS`
* `COMPLETED`

The exact final status model must remain consistent with the Dispatch Lifecycle and Business Rules documentation.

Invalid status transitions must be rejected.

---

# 14. Domain Relationships

The initial conceptual relationships are:

### Incident → Location

An incident has a location representing where the emergency occurred.

### Emergency Resource → Location

An emergency resource has a location representing its current operational position.

### Emergency Resource → Resource Capability

A resource may have one or more capabilities.

### Response Team → Resource Capability

A response team may have capabilities relevant to emergency operations.

### Incident → Dispatch

An incident may have dispatch operations associated with it.

### Emergency Resource → Dispatch

A resource may participate in dispatch operations over its operational history.

### Response Team → Dispatch

A response team may participate in dispatch operations where required.

These relationships must be refined into exact cardinalities and ownership rules in the Database Design and System Architecture documents.

---

# 15. Relationship Cardinality — Initial Model

The following represents the initial conceptual model.

| Relationship               | Initial Cardinality                                            |
| -------------------------- | -------------------------------------------------------------- |
| Incident → Location        | Many incidents may reference a location                        |
| Resource → Location        | Many resources may reference a location                        |
| Incident → Dispatch        | One incident may have zero or more dispatch records            |
| Resource → Dispatch        | One resource may have zero or more historical dispatch records |
| Response Team → Dispatch   | One team may have zero or more historical dispatch records     |
| Resource → Capability      | One resource may have zero or more capabilities                |
| Response Team → Capability | One team may have zero or more capabilities                    |

These are conceptual cardinalities.

Exact JPA ownership, `mappedBy`, `JoinColumn`, join tables, foreign keys, cascade behavior, and orphan-removal behavior must not be decided from this document alone.

They must be explicitly defined in the Database Design and System Architecture documents.

---

# 16. Assignment Model

The Dispatch entity represents the historical assignment between an Incident and an Emergency Resource.

The dispatch relationship allows the system to determine:

* Which resource responded.
* Which incident received the response.
* When the assignment was created.
* When the response started.
* When the response completed.
* What the dispatch status became.

A dispatch record must remain historically meaningful after the resource becomes available for another incident.

---

# 17. Resource Availability Model

Resource availability is determined using operational state and assignment information.

A resource should be considered dispatchable only when:

1. Its operational status permits dispatch.
2. It satisfies the incident's resource-type requirements.
3. It satisfies mandatory capability requirements.
4. Required response-team conditions are satisfied.
5. It has no conflicting active assignment.
6. Other mandatory operational constraints are satisfied.

Therefore:

**AVAILABLE status ≠ automatically eligible resource.**

Availability is one part of eligibility.

---

# 18. Resource Selection Model

Resource selection consists conceptually of:

### Stage 1 — Candidate Identification

Identify resources that may potentially respond.

### Stage 2 — Eligibility Filtering

Remove resources that fail mandatory requirements.

### Stage 3 — Ranking

Compare eligible resources using documented optimization criteria.

### Stage 4 — Final Verification

Verify that the selected resource remains assignable.

### Stage 5 — Assignment

Create the dispatch and update the relevant operational state.

The exact algorithm belongs to:

`08-DISPATCH-ENGINE-DESIGN.md`

---

# 19. Domain Invariants

The following invariants must always hold.

### DI-01

An incident must have a valid severity.

### DI-02

An incident must have a valid status.

### DI-03

A resource must have a valid resource type.

### DI-04

A resource must have a valid operational status.

### DI-05

A dispatch must reference a valid incident.

### DI-06

A dispatch must reference a valid resource.

### DI-07

A resource must not have conflicting exclusive active assignments.

### DI-08

A resource that is unavailable for operational reasons must not be successfully dispatched.

### DI-09

A mandatory resource capability requirement must be satisfied before a resource can be selected.

### DI-10

Historical dispatch records must remain valid after the associated resource's current state changes.

### DI-11

Invalid lifecycle transitions must not be allowed.

### DI-12

Domain state must remain consistent after successful business operations.

---

# 20. Domain vs Business-Service Responsibilities

The domain model represents business concepts and their state.

Business services/engines are responsible for coordinating operations involving multiple domain concepts.

For example:

### Domain Responsibility

A resource represents:

* Its type.
* Its status.
* Its capabilities.
* Its location.

### Business-Service Responsibility

The dispatch engine determines:

* Which resources are eligible.
* How eligible resources are ranked.
* Which resource should be selected.
* How the dispatch operation is coordinated.

This separation prevents domain objects from becoming responsible for the entire application workflow.

---

# 21. Persistence Considerations

The domain model will eventually be persisted using PostgreSQL.

The project will demonstrate:

* JDBC-based persistence.
* JPA.
* Hibernate.

Persistence mapping decisions must preserve the domain relationships defined here.

However, this document does not define:

* Table names.
* Column names.
* Primary-key generation strategy.
* Foreign-key constraints.
* Join-table structure.
* Indexes.
* JPA annotations.
* Hibernate configuration.

Those belong to:

`07-DATABASE-DESIGN.md`

and the relevant architecture documentation.

---

# 22. Domain Model and Concurrency

The domain model must support concurrent dispatch operations without permitting invalid shared-resource state.

The important domain invariant is:

> A resource that permits only one conflicting active assignment must not be successfully assigned to multiple conflicting dispatches at the same time.

The exact concurrency mechanism is intentionally outside this document.

It belongs to:

`09-CONCURRENCY-DESIGN.md`

---

# 23. Domain Model and Reporting

The domain model must retain sufficient operational information to support:

* Incident statistics.
* Dispatch statistics.
* Resource utilization.
* Resource workload.
* Historical dispatch reporting.

Reporting calculations belong to the reporting/business layer rather than being embedded into basic domain state.

---

# 24. Explicitly Deferred Domain Decisions

The following decisions are intentionally deferred until their respective documents are created:

* Exact Java package structure.
* Exact implementation details beyond the documented domain concepts.
* Entity inheritance strategy.
* Exact relationship ownership.
* `mappedBy` decisions.
* `JoinColumn` decisions.
* Cascade behavior.
* Orphan-removal behavior.
* Lazy/eager fetching decisions.
* Database table design.
* Primary-key generation.
* Exact database column types.
* Exact distance representation.
* Exact dispatch scoring formula.
* Exact concurrency mechanism.

The following decisions are **not** deferred:

* Initial Resource Capability catalogue.
* Resource Capability controlled-value meaning.
* Resource Capability as a controlled domain value.
* Initial Java representation of Resource Capability as an enum.

These decisions are now part of the documented domain model and must be followed during implementation.

---

# 25. Domain Design Rule

Every Java domain class must have a clear responsibility derived from this document.

Before creating a domain class, the development process must answer:

1. What domain concept does this class represent?
2. Why does the concept exist?
3. What state belongs to it?
4. What behavior belongs to it?
5. What relationships does it have?
6. Which document defines that requirement?

If these questions cannot be answered from the documentation, implementation must stop and the documentation must be reviewed first.

---

# 26. Documentation Consistency

This document must remain consistent with:

* `01-PROJECT-OVERVIEW.md`
* `02-REQUIREMENTS-SPECIFICATION.md`
* `03-ACTORS-AND-USE-CASES.md`
* `04-BUSINESS-RULES.md`
* `06-SYSTEM-ARCHITECTURE.md`
* `07-DATABASE-DESIGN.md`
* `08-DISPATCH-ENGINE-DESIGN.md`
* `09-CONCURRENCY-DESIGN.md`
* `10-API-DESIGN.md`
* `11-ERROR-HANDLING-DESIGN.md`
* `12-TESTING-STRATEGY.md`
* `13-DEVELOPMENT-ROADMAP.md`

Any conflict between documents must be identified and resolved before implementation.

The documentation remains the source of truth for the project.
