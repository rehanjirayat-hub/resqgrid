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

The model should stay focused on the emergency-response problem. We will add new domain objects only when the business actually needs them.

---

# 2. Core Domain Objects

The initial model contains these main objects:

| Domain Object       | Responsibility                                                     |
| ------------------- | ------------------------------------------------------------------ |
| `Incident`          | Represents an emergency that requires a response                   |
| `EmergencyResource` | Represents a resource that can respond to incidents                |
| `ResponseTeam`      | Represents the personnel associated with a response operation      |
| `Dispatch`          | Represents the assignment of a resource to an incident             |
| `Location`          | Represents a geographical position used by incidents and resources |
| `History`           | Represents an important event or state change                      |
| `Capability`        | Represents something a resource is qualified or equipped to handle |

Supporting domain concepts will include enums for statuses, severity, and resource types.

---

# 3. Incident

`Incident` is one of the central objects in the system.

It represents an emergency reported to ResQGrid.

## Main information

An incident is expected to contain:

* unique identifier
* incident type
* description
* location
* severity
* current status
* creation time
* relevant timestamps

## Responsibility

`Incident` should own information and behavior that naturally belongs to an incident.

Examples:

* determining whether the incident is still active
* changing its status through valid transitions
* exposing its severity
* exposing its location
* determining whether it can receive another dispatch

The incident should not be responsible for:

* finding the nearest ambulance
* querying PostgreSQL
* creating HTTP responses
* deciding database transaction boundaries

Those responsibilities belong elsewhere.

---

# 4. EmergencyResource

`EmergencyResource` represents something that can be assigned to an emergency.

Examples include:

* ambulance
* fire unit
* rescue team

## Main information

A resource is expected to contain:

* unique identifier
* resource type
* current status
* current location
* capabilities
* operational information

## Responsibility

The resource should own state and behavior related to its own operational condition.

Examples:

* checking whether it is available
* changing its operational status through valid rules
* exposing its capabilities
* exposing its current location
* determining whether it can accept an assignment

The resource should not be responsible for:

* searching all resources
* calculating the complete dispatch ranking
* directly updating PostgreSQL
* deciding how an incident should be prioritized

Those are application/service or infrastructure concerns.

---

# 5. ResponseTeam

`ResponseTeam` represents the personnel involved in responding to an incident.

For example:

* medical response personnel
* firefighters
* rescue personnel

The first implementation does not need to make ResponseTeam unnecessarily complicated.

Its purpose is to give the domain a place for team-level information if the response operation needs it.

Potential information includes:

* team identifier
* team name
* members
* specialization
* availability

The exact relationship between `ResponseTeam` and `EmergencyResource` will be finalized during database design.

For the initial version, we should avoid building a complete employee-management system.

---

# 6. Dispatch

`Dispatch` represents the actual assignment of an emergency resource to an incident.

This is different from simply storing a reference between two objects.

A dispatch has its own lifecycle and business meaning.

## Main information

A dispatch is expected to contain:

* unique identifier
* incident
* assigned resource
* dispatch status
* assignment timestamp
* relevant timestamps
* completion information where applicable

## Responsibility

`Dispatch` should own information and behavior related to the dispatch lifecycle.

Examples:

* checking its current status
* changing dispatch status through valid transitions
* determining whether it is active
* recording relevant timestamps

The dispatch should not:

* search the database for candidate resources
* decide which resource is best
* manage HTTP requests

Those responsibilities belong to other layers.

---

# 7. Location

`Location` represents the geographical position of an incident or resource.

At minimum, the model should support coordinates that allow distance calculations.

Potential information:

* latitude
* longitude
* optional human-readable address

## Responsibility

Location should represent location data and provide appropriate domain-level behavior related to location.

For example:

* validating coordinate ranges
* providing coordinate information
* supporting distance calculations where appropriate

The exact distance calculation strategy will be decided during Dispatch Engine Design.

---

# 8. Capability

A capability represents something that a resource can handle.

Examples:

* BASIC_MEDICAL
* ADVANCED_MEDICAL
* FIRE_SUPPRESSION
* WATER_RESCUE
* HEAVY_RESCUE

The initial implementation should keep the capability model simple.

A capability may eventually become its own domain object if the business rules require richer information.

For example, if capabilities eventually need:

* certification
* expiration
* specialization
* equipment requirements

then a more detailed model can be introduced.

For the initial system, the model should avoid unnecessary complexity.

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

Severity is a domain concept, not simply a display value.

It affects:

* incident priority
* dispatch processing
* resource allocation decisions
* reporting

The order of severity must therefore be meaningful to the application.

---

# 10. IncidentStatus

Incident status represents the current stage of an incident.

Initial values:

```text
REPORTED
ASSIGNED
IN_PROGRESS
RESOLVED
CANCELLED
```

Status transitions are controlled by business rules.

The system should not allow arbitrary changes such as:

```text
RESOLVED -> REPORTED
```

unless a future business requirement explicitly introduces such behavior.

---

# 11. ResourceStatus

Resource status represents the current operational condition of an emergency resource.

Initial values:

```text
AVAILABLE
BUSY
OFFLINE
MAINTENANCE
```

The status is important because it directly affects resource eligibility.

For normal dispatch:

```text
AVAILABLE -> eligible
BUSY -> not eligible
OFFLINE -> not eligible
MAINTENANCE -> not eligible
```

---

# 12. ResourceType

Resource type identifies the broad category of emergency resource.

Initial examples:

```text
AMBULANCE
FIRE_UNIT
RESCUE_TEAM
```

Resource type may influence dispatch eligibility.

For example, an incident requiring fire suppression should not normally be assigned an ambulance simply because the ambulance is closer.

---

# 13. DispatchStatus

Dispatch status represents the lifecycle of a resource assignment.

Initial values:

```text
CREATED
DISPATCHED
EN_ROUTE
ARRIVED
COMPLETED
CANCELLED
```

The valid transition rules will be defined and enforced as part of the dispatch domain logic.

---

# 14. History

`History` represents an important event or state change that occurred during system operation.

Examples:

* incident created
* incident status changed
* resource assigned
* dispatch created
* dispatch status changed
* resource released
* incident resolved
* dispatch cancelled

## Responsibility

History exists to preserve operational context.

It should answer questions such as:

* What happened?
* When did it happen?
* Which incident was involved?
* Which resource was involved?
* What changed?

History should not replace the current state stored in the main domain objects.

For example, the current resource status should be read from the resource state rather than reconstructed by scanning historical events.

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
   ^
   |
   | 1
   |
EmergencyResource
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

The initial system can model the current location directly. More advanced location tracking can be considered later if the project requires it.

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
    +-- BASIC_MEDICAL
    +-- ADVANCED_MEDICAL
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

Whether multiple active resources can respond to the same incident simultaneously is a business decision that will be finalized later.

The initial implementation should not assume multi-resource dispatch unless required.

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

These are historical assignments.

At any given time, the normal rule is that the resource may have at most one conflicting active assignment.

---

# 21. Dispatch and History

A dispatch can produce multiple historical events.

Example:

```text
Dispatch #5001
    |
    +-- CREATED
    +-- DISPATCHED
    +-- EN_ROUTE
    +-- ARRIVED
    +-- COMPLETED
```

History provides the timeline while the dispatch itself represents the current state of that operation.

---

# 22. ResponseTeam Relationship

The conceptual relationship is:

```text
ResponseTeam
      |
      v
EmergencyResource
```

The exact cardinality is intentionally not fixed yet.

A future design may allow:

* one team operating one resource
* one team operating multiple resources
* multiple personnel assigned to a resource

The first implementation should choose the simplest relationship that satisfies the actual requirements.

---

# 23. Domain Responsibilities vs Service Responsibilities

A major design decision is to keep domain behavior separate from orchestration.

### Domain objects should handle:

* their own state
* valid state transitions
* domain-level validation
* behavior directly related to themselves

### Services should handle:

* coordinating multiple domain objects
* dispatch workflows
* candidate selection
* transaction-level operations
* orchestration between repositories

### Repositories/DAOs should handle:

* persistence
* queries
* database interaction

### Controllers should handle:

* HTTP requests
* request parsing
* response creation
* interaction with services

This separation will help prevent "god classes" and keep the project maintainable.

---

# 24. Dispatch Engine and Domain Model

The Dispatch Engine is not itself a domain entity.

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

The engine should not own the permanent state of incidents or resources.

---

# 25. Domain Model and Persistence

The domain model and database model are related but should not be treated as identical by default.

For example:

* A Java object may contain behavior that does not map directly to a database column.
* A database table may contain technical metadata that does not belong directly in the domain API.
* Relationships may require ORM-specific mapping decisions.
* Hibernate/JPA mapping should support the domain rather than dictate the entire domain design.

The final persistence model will be defined in the Database Design document.

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
EmergencyResource
   |
   | selected
   v
Dispatch
   |
   +--> Incident updated
   |
   +--> Resource updated
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

The following decisions are intentionally deferred to later design documents:

### Resource selection

* Exact scoring formula
* Distance calculation
* Workload calculation
* Tie-breaking

### Response teams

* Exact relationship with resources
* Whether teams can operate multiple resources

### Multiple resources

* Whether one incident can have several active resources
* How resource allocation changes when that happens

### Location

* Coordinate precision
* Address representation
* Location update strategy

### Capabilities

* Enum vs entity
* Many-to-many mapping
* Capability requirements per incident

These decisions should be made when their surrounding design is clear rather than prematurely.

---

# 29. Initial Domain Model Summary

The first implementation will revolve around:

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
                            |
                            | *
                            |
                            v
                 +----------------------+
                 | EmergencyResource    |
                 +----------------------+
                    |              |
                    |              |
                    v              v
               Location       Capability

Supporting concepts:

IncidentSeverity
IncidentStatus
ResourceStatus
ResourceType
DispatchStatus
History
ResponseTeam
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
5. What other object should it interact with?
6. Why should it exist separately?

If those questions cannot be answered clearly, the concept probably does not need to be a separate domain object yet.

---

# 31. Document Status

**Document 05 — Domain Model**

Status: Completed as the initial domain baseline.

This document will guide:

* Java class design
* Interfaces
* Service responsibilities
* Repository design
* JPA relationships
* Database schema
* Dispatch Engine design
* Unit testing