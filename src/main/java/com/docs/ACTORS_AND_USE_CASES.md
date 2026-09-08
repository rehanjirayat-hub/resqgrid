# ResQGrid — Actors & Use Cases

**Document:** Actors & Use Cases
**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Development Baseline

---

# 1. Purpose

This document defines the actors that interact with ResQGrid and the major use cases supported by the system.

A use case describes a meaningful interaction between an actor and the system that produces an observable business result.

The purpose of this document is to establish system behavior before implementation.

---

# 2. Actors

ResQGrid has three primary human actors and one system actor.

## 2.1 Incident Operator

The Incident Operator manages emergency incident information.

Primary responsibilities:

* Register incidents
* Provide incident details
* Update incident information
* View incident information
* Monitor incident status

---

## 2.2 Dispatcher

The Dispatcher manages emergency resource allocation.

Primary responsibilities:

* View active incidents
* View resource availability
* Request resource dispatch
* Monitor dispatches
* Update dispatch-related operational information
* Review dispatch history

---

## 2.3 Operations Manager

The Operations Manager monitors the overall emergency response operation.

Primary responsibilities:

* Monitor incidents
* Monitor resources
* Review dispatch activity
* Review historical information
* View operational reports
* Analyze resource utilization

---

## 2.4 Dispatch Engine

The Dispatch Engine is an internal system actor.

It performs automated resource selection and dispatch decision logic.

Primary responsibilities:

* Find eligible resources
* Filter unsuitable resources
* Rank candidate resources
* Select the most appropriate resource
* Enforce dispatch rules
* Participate in concurrency-safe resource assignment

---

# 3. Use Case Overview

The major ResQGrid use cases are:

| ID     | Use Case                    | Primary Actor                   |
| ------ | --------------------------- | ------------------------------- |
| UC-001 | Register Incident           | Incident Operator               |
| UC-002 | View Incident               | Incident Operator / Dispatcher  |
| UC-003 | Update Incident             | Incident Operator               |
| UC-004 | Change Incident Status      | Dispatcher / Operations Manager |
| UC-005 | Register Resource           | Dispatcher                      |
| UC-006 | View Resource               | Dispatcher / Operations Manager |
| UC-007 | Update Resource Status      | Dispatcher                      |
| UC-008 | Find Suitable Resources     | Dispatcher / Dispatch Engine    |
| UC-009 | Dispatch Resource           | Dispatcher / Dispatch Engine    |
| UC-010 | Track Dispatch              | Dispatcher                      |
| UC-011 | Complete Dispatch           | Dispatcher                      |
| UC-012 | Cancel Dispatch             | Dispatcher                      |
| UC-013 | View Dispatch History       | Dispatcher / Operations Manager |
| UC-014 | View Incident History       | Operations Manager              |
| UC-015 | Generate Operational Report | Operations Manager              |

---

# 4. Use Case UC-001 — Register Incident

## Actor

Incident Operator

## Goal

Create a valid emergency incident in the system.

## Preconditions

* The system is available.
* The operator has access to incident registration.

## Main Flow

1. The operator provides incident information.
2. The system validates the supplied data.
3. The system validates the incident severity and other domain values.
4. The system creates an incident.
5. The system assigns the initial incident status.
6. The system persists the incident.
7. The system records the creation event where applicable.
8. The system returns the created incident.

## Alternative Flows

### Invalid Input

If required information is missing or invalid:

1. Validation fails.
2. The incident is not persisted.
3. The system reports the validation failure.

### Persistence Failure

If the incident cannot be persisted:

1. The operation fails.
2. The system must not report the incident as successfully created.
3. Appropriate error handling is performed.

## Postconditions

A valid incident exists in persistent storage.

---

# 5. Use Case UC-002 — View Incident

## Actors

Incident Operator, Dispatcher

## Goal

Retrieve information about an incident.

## Main Flow

1. Actor requests an incident.
2. System validates the incident identifier.
3. System retrieves the incident.
4. System returns incident information.

## Alternative Flow

If the incident does not exist:

* The system reports that the requested incident cannot be found.

---

# 6. Use Case UC-003 — Update Incident

## Actor

Incident Operator

## Goal

Update permitted incident information.

## Main Flow

1. Operator selects an existing incident.
2. System retrieves the incident.
3. Operator supplies updated information.
4. System validates the changes.
5. System verifies that the requested update is allowed.
6. System persists the changes.
7. System records relevant history where appropriate.

## Important Rule

Not every incident attribute should necessarily be editable after the incident has progressed through its lifecycle.

The final editability rules will be defined during domain design.

---

# 7. Use Case UC-004 — Change Incident Status

## Actors

Dispatcher, Operations Manager

## Goal

Move an incident through its valid lifecycle.

## Main Flow

1. Actor selects an incident.
2. System retrieves the current status.
3. Actor requests a new status.
4. System validates the transition.
5. If valid, the system changes the status.
6. The system records the status change.
7. The updated incident is persisted.

## Alternative Flow

If the transition is invalid:

* The status must not change.
* The system reports a business rule violation.

Example:

An incident should not be moved directly from `REPORTED` to `RESOLVED` if the defined lifecycle requires intermediate states.

---

# 8. Use Case UC-005 — Register Resource

## Actor

Dispatcher

## Goal

Add an emergency resource to the system.

## Main Flow

1. Dispatcher provides resource information.
2. System validates the resource.
3. System validates its resource type and capabilities.
4. System assigns an appropriate initial status.
5. System persists the resource.

## Example Resources

* Ambulance
* Fire Unit
* Rescue Team

---

# 9. Use Case UC-006 — View Resource

## Actors

Dispatcher, Operations Manager

## Goal

View information about an emergency resource.

Information may include:

* Resource identifier
* Resource type
* Current status
* Current location
* Capabilities
* Current assignments
* Operational information

---

# 10. Use Case UC-007 — Update Resource Status

## Actor

Dispatcher

## Goal

Change the operational state of a resource.

## Main Flow

1. Dispatcher selects a resource.
2. System retrieves its current state.
3. Dispatcher requests a status change.
4. System validates the transition.
5. System updates the resource.
6. System records relevant history.

## Supported Resource States

* AVAILABLE
* BUSY
* OFFLINE
* MAINTENANCE

## Important Rule

A resource that is currently assigned to an active dispatch must not be arbitrarily changed to `AVAILABLE` if doing so would violate dispatch consistency.

---

# 11. Use Case UC-008 — Find Suitable Resources

## Actors

Dispatcher, Dispatch Engine

## Goal

Identify resources capable of responding to an incident.

## Main Flow

1. Dispatcher selects an incident.
2. System retrieves incident requirements.
3. Dispatch Engine retrieves potential resources.
4. Engine filters unavailable resources.
5. Engine filters resources that lack required capabilities.
6. Engine evaluates operational constraints.
7. Engine evaluates candidate suitability.
8. Engine ranks valid candidates.
9. System returns the ranked candidates.

## Candidate Factors

The selection process may consider:

* Resource availability
* Resource type
* Capability
* Incident severity
* Distance
* Current workload
* Existing assignments
* Expected response time
* Operational priority

## Important Rule

The nearest resource is not automatically the best resource.

A resource must first satisfy mandatory requirements before distance and other ranking factors can be considered.

---

# 12. Use Case UC-009 — Dispatch Resource

## Actors

Dispatcher, Dispatch Engine

## Goal

Assign a suitable resource to an incident.

## Main Flow

1. Dispatcher requests dispatch for an incident.
2. System validates the incident.
3. Dispatch Engine identifies eligible resources.
4. Engine ranks candidates.
5. A candidate resource is selected.
6. System verifies that the resource is still assignable.
7. System creates the dispatch.
8. Resource state is updated.
9. Incident state is updated where appropriate.
10. Relevant history is recorded.
11. Transaction is completed successfully.

## Concurrency Requirement

The resource verification and assignment operation must be concurrency-safe.

If two requests attempt to assign the same resource concurrently, the system must prevent an invalid double assignment.

## Failure Flow

If no suitable resource exists:

* No invalid dispatch is created.
* Resource states remain unchanged.
* The system reports that no suitable resource is currently available.

## Concurrency Failure Flow

If the selected resource becomes unavailable before assignment:

* The dispatch operation must not blindly continue.
* The system must re-evaluate or fail safely according to the dispatch strategy.

---

# 13. Use Case UC-010 — Track Dispatch

## Actor

Dispatcher

## Goal

Monitor the progress of an active dispatch.

## Main Flow

1. Dispatcher requests a dispatch.
2. System retrieves dispatch information.
3. System returns:

    * Incident
    * Resource
    * Current dispatch status
    * Assignment time
    * Relevant timestamps
    * Other operational information

---

# 14. Use Case UC-011 — Complete Dispatch

## Actor

Dispatcher

## Goal

Mark a completed response operation.

## Main Flow

1. Dispatcher selects an active dispatch.
2. System validates that completion is allowed.
3. Dispatch status changes to `COMPLETED`.
4. Completion timestamp is recorded.
5. Resource state is updated appropriately.
6. Incident state is updated if required.
7. Relevant history is recorded.
8. Transaction completes.

## Important Rule

Completing a dispatch and releasing a resource are related operations and must remain consistent.

---

# 15. Use Case UC-012 — Cancel Dispatch

## Actor

Dispatcher

## Goal

Cancel a dispatch that should no longer continue.

## Main Flow

1. Dispatcher selects a dispatch.
2. System verifies that cancellation is allowed.
3. Dispatch status changes to `CANCELLED`.
4. Relevant timestamps and history are recorded.
5. Resource availability is updated according to business rules.
6. Incident state is updated if required.

---

# 16. Use Case UC-013 — View Dispatch History

## Actors

Dispatcher, Operations Manager

## Goal

Review historical dispatch activity.

Information may include:

* Incident
* Resource
* Dispatch status
* Assignment time
* Completion time
* Cancellation information
* Historical events

The history must remain useful for operational analysis and reporting.

---

# 17. Use Case UC-014 — View Incident History

## Actor

Operations Manager

## Goal

Review the lifecycle of an incident.

The system should provide relevant historical events such as:

* Incident creation
* Status changes
* Resource assignment
* Dispatch events
* Resolution
* Cancellation

---

# 18. Use Case UC-015 — Generate Operational Report

## Actor

Operations Manager

## Goal

Obtain useful operational information from the system.

Possible reports include:

### Incident Reports

* Total incidents
* Incidents by severity
* Incidents by status
* Active incidents
* Resolved incidents

### Resource Reports

* Total resources
* Available resources
* Busy resources
* Offline resources
* Maintenance resources
* Resource utilization

### Dispatch Reports

* Total dispatches
* Completed dispatches
* Cancelled dispatches
* Active dispatches
* Dispatch activity over time

The reporting design will be refined later.

---

# 19. Cross-Cutting Use Case Rules

The following rules apply to multiple use cases.

## 19.1 Validation

User-provided input must be validated before business operations are executed.

---

## 19.2 Business Rule Validation

Valid input does not necessarily mean a valid operation.

For example:

A request to mark a busy resource as available may be syntactically valid but operationally invalid.

---

## 19.3 Persistence Consistency

Related database changes must be handled transactionally when they represent one business operation.

---

## 19.4 Concurrency Safety

Operations involving shared resources must be safe under concurrent execution.

---

## 19.5 History

Important state changes should be traceable through appropriate historical records.

---

## 19.6 Error Handling

The system should distinguish between:

* Validation errors
* Resource-not-found errors
* Invalid state transitions
* Dispatch failures
* Concurrency conflicts
* Persistence failures
* Unexpected system failures

---

# 20. High-Level User Journey

A typical emergency response workflow is:

```text
Incident Operator
       |
       v
Register Incident
       |
       v
Incident = REPORTED
       |
       v
Dispatcher Reviews Incident
       |
       v
Dispatch Engine Finds Candidates
       |
       v
Filter + Rank Resources
       |
       v
Select Suitable Resource
       |
       v
Concurrency-Safe Assignment
       |
       v
Create Dispatch
       |
       +----------------------+
       |                      |
       v                      v
Resource = BUSY       Incident = ASSIGNED
       |                      |
       +----------+-----------+
                  |
                  v
             Dispatch Active
                  |
                  v
           Resource Responds
                  |
                  v
          Dispatch Completed
                  |
                  +-------> Resource Released
                  |
                  +-------> Incident Updated
                  |
                  v
             History Stored
                  |
                  v
             Reporting
```

---

# 21. Use Case Dependencies

Several use cases depend on others.

For example:

```text
Register Incident
        |
        v
View Incident
        |
        v
Find Suitable Resources
        |
        v
Dispatch Resource
        |
        +----> Track Dispatch
        |
        +----> Complete Dispatch
        |
        +----> Cancel Dispatch
        |
        v
Dispatch History
        |
        v
Operational Reporting
```

Similarly:

```text
Register Resource
        |
        v
Update Resource Status
        |
        v
Find Suitable Resources
        |
        v
Dispatch Resource
```

---

# 22. Future Expansion

The use-case model should allow future support for:

* Authentication
* Authorization
* Multiple dispatcher roles
* Notifications
* External emergency systems
* Real-time location updates
* More advanced dispatch policies
* Additional reporting
* Audit logging
* Administrative management

These are not required for the initial implementation.

---

# 23. Acceptance Principle

A use case should be considered implemented only when:

1. Its normal flow works.
2. Invalid input is handled.
3. Invalid business operations are rejected.
4. Relevant persistence behavior works.
5. Transactional consistency is maintained.
6. Concurrency implications are handled where applicable.
7. Important behavior is covered by tests.
8. The implementation follows the defined architecture.

---

# 24. Document Status

**Document 03 — Actors & Use Cases**

Status: Completed as the initial behavioral baseline.

This document will be used as a reference during domain modeling, architecture, API design, implementation, and testing.