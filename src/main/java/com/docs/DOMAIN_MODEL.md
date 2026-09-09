# ResQGrid — Domain Model

**Document:** Domain Model
**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Development Baseline

---

# 1. Overview

The domain model describes the main concepts ResQGrid works with and the relationships between them.

The initial domain is centered around four things:

* incidents that need help
* resources that can respond
* dispatches that connect resources to incidents
* history that records important changes

Other concepts such as location, capabilities, severity, and status support those core concepts.

The model should stay focused on the emergency-response problem. New domain objects should only be introduced when the business actually requires them.

---

# 2. Core Domain Objects

The initial model contains these main objects:

| Domain Object       | Responsibility                                                           |
| ------------------- | ------------------------------------------------------------------------ |
| `Incident`          | Represents an emergency that requires a response                         |
| `EmergencyResource` | Represents a resource that can respond to incidents                      |
| `ResponseTeam`      | Represents the personnel associated with a response operation            |
| `Dispatch`          | Represents the assignment of a resource and response team to an incident |
| `Location`          | Represents a geographical position used by incidents and resources       |
| `History`           | Represents an important event or state change                            |

`Capability` is currently modeled as an enum because the initial system only needs a fixed set of resource capabilities.

Supporting domain concepts include enums for statuses, severity, resource types, and dispatch state.

---

# 3. Incident

`Incident` is one of the central objects in the system.

It represents an emergency reported to ResQGrid.

## Main information

An incident currently contains:

* unique identifier
* title
* description
* location
* severity
* current status
* reported time

Additional timestamps or incident-specific information can be introduced later if the business requirements require them.

## Responsibility

`Incident` owns information and behavior that naturally belongs to an incident.

Examples:

* exposing its severity
* exposing its location
* changing its status through valid transitions
* maintaining its current lifecycle state
* protecting itself from invalid state changes

The current incident lifecycle is:

```text
REPORTED
    ↓
ASSESSED
    ↓
DISPATCHED
    ↓
IN_PROGRESS
    ↓
RESOLVED
```

An incident may also be cancelled while it is in a non-terminal state.

`RESOLVED` and `CANCELLED` are terminal states in the current model.

The incident should not be responsible for:

* finding the nearest resource
* querying PostgreSQL
* creating HTTP responses
* deciding database transaction boundaries
* coordinating multiple resources

Those responsibilities belong to application, service, or infrastructure layers.

---

# 4. EmergencyResource

`EmergencyResource` represents something that can be assigned to an emergency.

Examples include:

* ambulance
* fire unit
* rescue team
* police unit
* medical team
* helicopter

## Main information

A resource currently contains:

* unique identifier
* name
* resource type
* current status
* current location
* capabilities

## Responsibility

The resource owns state and behavior related to its own operational condition.

Examples:

* exposing its current status
* changing its operational status through valid rules
* exposing its capabilities
* exposing its current location

The resource should not be responsible for:

* searching all resources
* calculating the complete dispatch ranking
* directly updating PostgreSQL
* deciding incident priority
* coordinating the entire dispatch process

Those responsibilities belong to application or service components.

---

# 5. ResponseTeam

`ResponseTeam` represents the personnel involved in responding to an incident.

Examples include:

* medical response personnel
* firefighters
* rescue personnel

The first implementation intentionally keeps `ResponseTeam` simple.

## Main information

The current model contains:

* team identifier
* team name
* team status
* capabilities

The model does not currently attempt to implement employee or personnel management.

Potential future information could include team members, specializations, or other operational details if actual requirements justify them.

The exact relationship between `ResponseTeam` and `EmergencyResource` remains a design decision for the persistence and service layers.

---

# 6. Dispatch

`Dispatch` represents the actual assignment of an emergency resource and response team to an incident.

A dispatch is not simply a reference between objects. It has its own lifecycle and business meaning.

## Main information

A dispatch currently contains:

* unique identifier
* incident
* assigned emergency resource
* assigned response team
* dispatch status
* dispatch assignment timestamp

The resource and response team are not assigned when a dispatch is first created.

A newly created dispatch starts in the `PENDING` state.

## Responsibility

`Dispatch` owns information and behavior related to its own lifecycle.

The current dispatch lifecycle is:

```text
PENDING
    ↓
ASSIGNED
    ↓
IN_PROGRESS
    ↓
COMPLETED
```

A dispatch can also be cancelled from appropriate non-terminal states.

The dispatch should not:

* search the database for candidate resources
* decide which resource is best
* calculate the complete dispatch ranking
* manage HTTP requests

Those responsibilities belong to other layers.

---

# 7. Location

`Location` represents the geographical position of an incident or resource.

The current model supports:

* latitude
* longitude
* human-readable address

## Responsibility

`Location` is responsible for representing valid geographical data.

It validates:

* latitude between `-90` and `90`
* longitude between `-180` and `180`
* non-blank address

The object is immutable after creation.

Distance calculation is not currently part of `Location`.

The exact distance calculation strategy will be decided as part of Dispatch Engine implementation.

---

# 8. Capability

A `Capability` represents something that a resource is qualified or equipped to handle.

The current implementation uses a fixed enum.

Initial capabilities are:

```text
MEDICAL_RESPONSE
FIRE_RESPONSE
RESCUE
HAZARDOUS_MATERIALS
WATER_RESCUE
```

Capabilities are important during dispatch candidate selection.

For example, a resource with `FIRE_RESPONSE` capability may be preferred for an incident requiring fire-response capability.

The current model intentionally keeps capabilities simple.

A capability may become a separate domain object later if the business requires additional information such as:

* certification
* expiration
* specialization
* equipment requirements

There is no need to introduce that complexity in the initial implementation.

---

# 9. IncidentSeverity

Severity represents how serious an incident is.

Initial values:

```text
CRITICAL
HIGH
MEDIUM
LOW
```

Severity is a domain concept rather than a display-only value.

It affects:

* incident priority
* dispatch processing
* resource allocation decisions
* reporting

The order of severity must therefore remain meaningful to the application.

---

# 10. IncidentStatus

`IncidentStatus` represents the current stage of an incident.

Initial values:

```text
REPORTED
ASSESSED
DISPATCHED
IN_PROGRESS
RESOLVED
CANCELLED
```

The current lifecycle is:

```text
REPORTED
    ↓
ASSESSED
    ↓
DISPATCHED
    ↓
IN_PROGRESS
    ↓
RESOLVED
```

`CANCELLED` is available from non-terminal states.

The domain object enforces valid transitions and does not allow arbitrary changes such as:

```text
RESOLVED -> REPORTED
```

or:

```text
CANCELLED -> IN_PROGRESS
```

unless future business requirements explicitly introduce such behavior.

---

# 11. ResourceStatus

`ResourceStatus` represents the current operational condition of an emergency resource.

Initial values:

```text
AVAILABLE
BUSY
OFFLINE
MAINTENANCE
```

For normal dispatch:

```text
AVAILABLE   -> eligible
BUSY        -> not eligible
OFFLINE     -> not eligible
MAINTENANCE -> not eligible
```

The resource domain object controls valid status transitions.

---

# 12. ResourceType

`ResourceType` identifies the broad category of emergency resource.

Initial values include:

```text
AMBULANCE
FIRE_UNIT
RESCUE_TEAM
POLICE_UNIT
MEDICAL_TEAM
HELICOPTER
```

Resource type may influence dispatch eligibility.

For example, an incident requiring fire response should not normally be assigned an ambulance simply because the ambulance is geographically closer.

---

# 13. DispatchStatus

`DispatchStatus` represents the lifecycle of a resource assignment.

Initial values:

```text
PENDING
ASSIGNED
IN_PROGRESS
COMPLETED
CANCELLED
```

The current lifecycle is:

```text
PENDING
    ↓
ASSIGNED
    ↓
IN_PROGRESS
    ↓
COMPLETED
```

Cancellation is supported from appropriate non-terminal states.

More detailed operational states such as `EN_ROUTE` or `ARRIVED` are intentionally not part of the initial implementation.

They can be introduced later if the requirements demonstrate a real need for that level of tracking.

---

# 14. History

`History` represents an important event or state change that occurred during system operation.

Examples include:

* incident created
* incident status changed
* resource assigned
* dispatch created
* dispatch status changed
* resource released
* incident resolved
* dispatch cancelled

## Main information

The current `History` model contains:

* unique identifier
* entity identifier
* entity type
* action
* occurrence timestamp
* description

For example:

```text
entityType = INCIDENT
entityId   = 101
action     = STATUS_CHANGED
description = Incident status changed to DISPATCHED
```

Another event could be:

```text
entityType = RESOURCE
entityId   = 25
action     = STATUS_CHANGED
description = Resource became BUSY
```

## Responsibility

History preserves operational context.

It should help answer questions such as:

* What happened?
* When did it happen?
* Which entity was involved?
* What action occurred?
* What changed?

History should not replace the current state stored in the main domain objects.

For example, the current resource status should be read from `EmergencyResource`, rather than reconstructed by scanning historical events.

---

# 15. Relationships

The initial conceptual relationships are:

```text
Incident
   |
   | 1
   |
   | 0..*
   v
Dispatch
   |
   +------------------+
   |                  |
   v                  v
EmergencyResource   ResponseTeam
   |
   +--------+
   |        |
   v        v
Location  Capability
```

An incident can have multiple dispatch records over its lifecycle.

A resource can participate in multiple dispatches over time.

However, under the normal operational model, a resource must not have multiple conflicting active dispatches at the same time.

---

# 16. Incident and Location

An incident has one relevant location.

Conceptually:

```text
Incident
   |
   | 1
   v
Location
```

The location is used by the Dispatch Engine when evaluating response distance.

---

# 17. Resource and Location

An emergency resource has a current location.

Conceptually:

```text
EmergencyResource
       |
       | 1
       v
    Location
```

The resource location may change over time.

The initial system models the current location directly.

More advanced location tracking can be considered later if the project requires historical movement or real-time tracking.

---

# 18. Resource and Capability

A resource may have multiple capabilities.

Conceptually:

```text
EmergencyResource
       |
       | 0..*
       v
   Capability
```

For example:

```text
Ambulance A
    |
    +-- MEDICAL_RESPONSE
    +-- RESCUE
```

Capability matching is an important part of dispatch candidate selection.

---

# 19. Incident and Dispatch

An incident may have multiple dispatches across its lifecycle.

For example:

```text
Incident #101
    |
    +-- Dispatch #5001 -> Ambulance A
    |
    +-- Dispatch #5002 -> Rescue Team B
```

Whether multiple resources can respond to the same incident simultaneously remains a business decision.

The initial implementation should not assume multi-resource dispatch unless the requirements require it.

---

# 20. Resource and Dispatch

A resource may participate in many dispatches over time.

Example:

```text
Ambulance A
    |
    +-- Dispatch #5001 -> Incident #101
    |
    +-- Dispatch #5014 -> Incident #118
    |
    +-- Dispatch #5032 -> Incident #126
```

These represent historical assignments.

At any given time, the normal operational rule is that a resource may have at most one conflicting active assignment.

This rule becomes especially important when concurrent dispatch requests are processed.

---

# 21. Dispatch and History

A dispatch can produce multiple historical events.

Example:

```text
Dispatch #5001
    |
    +-- CREATED
    +-- ASSIGNED
    +-- IN_PROGRESS
    +-- COMPLETED
```

History provides the timeline while the dispatch itself represents the current state of that operation.

The exact set of history actions will be defined as the service workflows are implemented.

---

# 22. ResponseTeam Relationship

The exact relationship between `ResponseTeam` and `EmergencyResource` is intentionally not fixed at the domain-model stage.

Possible future designs include:

* one team operating one resource
* one team operating multiple resources
* multiple personnel assigned to a resource

The first implementation should choose the simplest relationship that satisfies the actual business requirements.

A complete personnel-management model is outside the scope of the initial ResQGrid version.

---

# 23. Domain Responsibilities vs Service Responsibilities

A major design decision is to keep domain behavior separate from orchestration.

### Domain objects should handle

* their own state
* valid state transitions
* domain-level validation
* behavior directly related to themselves

### Services should handle

* coordinating multiple domain objects
* incident workflows
* dispatch workflows
* candidate selection
* resource ranking
* transaction-level operations
* orchestration between repositories

### Repositories / DAOs should handle

* persistence
* database queries
* database interaction
* mapping persisted data to domain objects

### Controllers should handle

* HTTP requests
* request parsing
* response creation
* interaction with services

This separation helps prevent large classes with unrelated responsibilities and keeps the project maintainable.

---

# 24. Dispatch Engine and Domain Model

The Dispatch Engine is not a domain entity.

It is a business component that operates on domain information.

Conceptually:

```text
Incident
   |
   | requirements
   v
Dispatch Engine
   |
   +----> EmergencyResource candidates
   |
   +----> Capability matching
   |
   +----> Distance evaluation
   |
   +----> Workload evaluation
   |
   +----> Ranking
   |
   v
Selected Resource
```

The engine should not permanently own the state of incidents or resources.

Its responsibility is to evaluate candidates and determine the most suitable resource according to the dispatch rules.

---

# 25. Domain Model and Persistence

The domain model and database model are related but should not be treated as identical by default.

For example:

* a Java object may contain behavior that does not map directly to a database column
* a database table may contain technical metadata that does not belong directly in the domain API
* relationships may require ORM-specific mapping decisions
* Hibernate/JPA mapping should support the domain rather than dictate the entire domain design

The final persistence model will be defined during database and JPA implementation.

---

# 26. Initial Object Interaction

A typical dispatch operation conceptually looks like this:

```text
Incident
   |
   | requires response
   v
Dispatch Service
   |
   v
Dispatch Engine
   |
   | finds eligible resources
   v
EmergencyResource candidates
   |
   | selected
   v
Dispatch
   |
   +--> Incident updated
   |
   +--> Resource updated
   |
   +--> ResponseTeam updated
   |
   +--> History recorded
```

The actual Java implementation will introduce services and repositories around this interaction.

---

# 27. Objects We Are Deliberately Not Adding Yet

The following concepts are intentionally not separate domain objects at this stage:

* User
* AuthenticationSession
* Notification
* Vehicle
* Equipment
* Address
* AuditLog
* Region
* Organization
* EmergencyCenter

They may become necessary later.

For now, adding them would increase complexity without solving a current ResQGrid requirement.

---

# 28. Important Design Questions for Later

The following decisions are intentionally deferred until the surrounding implementation makes the requirements clearer.

### Resource selection

* exact scoring formula
* distance calculation
* workload calculation
* tie-breaking

### Response teams

* exact relationship with resources
* whether teams can operate multiple resources
* whether team capability affects resource selection

### Multiple resources

* whether one incident can have several active resources
* how resource allocation changes when that happens

### Location

* coordinate precision
* address representation
* location update strategy
* distance calculation strategy

### Capabilities

* whether the enum remains sufficient
* many-to-many database mapping
* capability requirements per incident

These decisions should be made when their surrounding design is clear rather than prematurely.

---

# 29. Initial Domain Model Summary

The first implementation revolves around:

```text
                     +----------------+
                     |    Incident    |
                     +----------------+
                             |
                             | 1..*
                             |
                             v
                     +----------------+
                     |    Dispatch    |
                     +----------------+
                       |            |
                       |            |
                       v            v
              +----------------+  +----------------+
              | Emergency      |  | ResponseTeam   |
              | Resource       |  +----------------+
              +----------------+
                   |       |
                   |       |
                   v       v
              Location  Capability
```

Supporting concepts:

```text
IncidentSeverity
IncidentStatus
ResourceStatus
ResourceType
DispatchStatus
History
```

---

# 30. Implementation Principle

The domain model should evolve from actual business behavior.

We will not create Java classes simply because a noun appears in the requirements.

Before introducing a new class, we should be able to answer:

1. What business concept does it represent?
2. What responsibility does it own?
3. What state does it manage?
4. What behavior belongs inside it?
5. What other objects should it interact with?
6. Why should it exist separately?

If those questions cannot be answered clearly, the concept probably does not need to be a separate domain object yet.

---

# 31. Document Status

**Document 05 — Domain Model**

Status: Updated to match the implemented domain baseline.

This document will guide:

* Java class design
* interfaces
* service responsibilities
* repository design
* JPA relationships
* database schema
* Dispatch Engine design
* unit testing
* future domain evolution