# ResQGrid — Business Rules

**Document:** Business Rules
**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Development Baseline

---

# 1. Purpose

This document defines the business rules that govern the behavior of ResQGrid.

Business rules represent decisions and constraints that must remain true regardless of whether the operation is triggered through:

* REST API
* Web interface
* Internal service
* Automated dispatch logic
* Future integrations

Business rules therefore belong to the business/domain layer rather than being tied exclusively to controllers, JSP pages, or database code.

---

# 2. Business Rule Categories

The rules are organized into:

1. Incident rules
2. Incident lifecycle rules
3. Resource rules
4. Resource lifecycle rules
5. Capability rules
6. Dispatch rules
7. Resource ranking rules
8. Concurrency rules
9. Transaction rules
10. History rules
11. Validation rules
12. Reporting rules

---

# 3. Incident Business Rules

## BR-INC-001 — Incident Must Be Identifiable

Every incident must have a unique identifier.

Two incidents must never share the same identifier.

---

## BR-INC-002 — Incident Requires Location

Every incident must have a valid location.

An incident without sufficient location information cannot participate in distance-based resource selection.

---

## BR-INC-003 — Incident Requires Severity

Every incident must have a severity.

Supported severity levels are:

* CRITICAL
* HIGH
* MEDIUM
* LOW

---

## BR-INC-004 — Incident Requires Valid Type

Every incident must have a valid incident type/category.

The type must be meaningful to the dispatch process where resource capability depends on it.

---

## BR-INC-005 — Incident Requires Initial Status

A newly created incident must receive an appropriate initial status.

The initial status is:

`REPORTED`

---

## BR-INC-006 — Incident Cannot Be Dispatched Without Valid State

Only incidents in an appropriate state may enter the resource dispatch process.

An incident that is already resolved or cancelled must not receive a new dispatch.

---

# 4. Incident Lifecycle Rules

The incident lifecycle is:

```text
REPORTED
    |
    v
ASSIGNED
    |
    v
IN_PROGRESS
    |
    v
RESOLVED
```

Cancellation may occur where business rules permit it:

```text
REPORTED -----> CANCELLED
ASSIGNED -----> CANCELLED
IN_PROGRESS --> CANCELLED
```

---

## BR-INC-LIFE-001 — Only Valid Transitions Are Allowed

An incident cannot move directly between arbitrary states.

The system must validate every requested state transition.

---

## BR-INC-LIFE-002 — Resolved Is Terminal

A `RESOLVED` incident cannot return to an active state through the normal workflow.

---

## BR-INC-LIFE-003 — Cancelled Is Terminal

A `CANCELLED` incident cannot return to an active state through the normal workflow.

---

## BR-INC-LIFE-004 — Assignment Must Be Meaningful

An incident should only move to `ASSIGNED` when an appropriate resource assignment exists.

---

## BR-INC-LIFE-005 — In-Progress Requires Active Response

An incident should only enter `IN_PROGRESS` when the associated response operation has progressed sufficiently to represent an active response.

---

# 5. Resource Business Rules

## BR-RES-001 — Resource Must Be Identifiable

Every emergency resource must have a unique identifier.

---

## BR-RES-002 — Resource Requires Type

Every resource must belong to a supported resource type.

Examples:

* Ambulance
* Fire Unit
* Rescue Team

---

## BR-RES-003 — Resource Requires Status

Every resource must have a valid operational status.

Supported statuses are:

* AVAILABLE
* BUSY
* OFFLINE
* MAINTENANCE

---

## BR-RES-004 — Only Available Resources Are Dispatch Candidates

Only resources with status:

`AVAILABLE`

may normally be considered for dispatch.

---

## BR-RES-005 — Busy Resources Cannot Be Assigned Again

A resource with status `BUSY` must not receive another incompatible active assignment.

---

## BR-RES-006 — Offline Resources Cannot Be Dispatched

A resource with status `OFFLINE` must not be selected by the Dispatch Engine.

---

## BR-RES-007 — Maintenance Resources Cannot Be Dispatched

A resource with status `MAINTENANCE` must not be selected by the Dispatch Engine.

---

# 6. Resource Lifecycle Rules

The resource lifecycle is primarily:

```text
AVAILABLE
    |
    v
BUSY
    |
    v
AVAILABLE
```

A resource may also enter operational states such as:

```text
AVAILABLE <----> OFFLINE
AVAILABLE <----> MAINTENANCE
```

Exact administrative transition permissions will be refined when resource management is implemented.

---

## BR-RES-LIFE-001 — Dispatch Makes Resource Busy

When a resource is successfully assigned to an active dispatch, its operational state must become:

`BUSY`

---

## BR-RES-LIFE-002 — Completed Dispatch Releases Resource

When an active dispatch is successfully completed, the resource should become eligible for future assignments according to operational rules.

Normally this means:

`BUSY -> AVAILABLE`

---

## BR-RES-LIFE-003 — Cancelled Dispatch Handles Resource State

When a dispatch is cancelled, the system must determine whether the resource should become available again.

The state change must be consistent with the actual dispatch situation.

---

## BR-RES-LIFE-004 — Resource State Must Match Active Assignment

The system must not allow contradictory states such as:

```text
Resource = AVAILABLE
Active Dispatch = assigned to same resource
```

unless a future business rule explicitly permits such a state.

---

# 7. Capability Rules

## BR-CAP-001 — Resource Capability Must Match Incident Requirements

A resource must possess the capabilities required by the incident before it can be selected.

---

## BR-CAP-002 — Capability Is a Mandatory Filter

Capability matching is not merely a ranking preference.

A resource that lacks a mandatory capability must be removed from the candidate set.

---

## BR-CAP-003 — Resource Type May Affect Eligibility

Certain incidents may require specific resource types.

For example:

```text
Medical emergency -> Ambulance
Fire emergency -> Fire Unit
Special rescue -> Rescue Team
```

The exact mappings will be refined during dispatch engine design.

---

# 8. Dispatch Rules

## BR-DIS-001 — Dispatch Requires Existing Incident

A dispatch cannot be created for an incident that does not exist.

---

## BR-DIS-002 — Dispatch Requires Existing Resource

A dispatch cannot reference a resource that does not exist.

---

## BR-DIS-003 — Resource Must Be Eligible

A resource must satisfy all mandatory eligibility requirements before dispatch.

Eligibility includes, where applicable:

* Available status
* Required capability
* Appropriate resource type
* Operational suitability
* Valid location
* Acceptable workload

---

## BR-DIS-004 — One Resource Cannot Have Conflicting Active Assignments

A resource must not be assigned to multiple incompatible active dispatches simultaneously.

This is one of the most important invariants in the system.

---

## BR-DIS-005 — Dispatch Creates an Assignment Record

A successful resource assignment must create a persistent dispatch record.

---

## BR-DIS-006 — Dispatch Updates Resource State

A successful dispatch must update the resource's operational state.

Normally:

```text
AVAILABLE -> BUSY
```

---

## BR-DIS-007 — Dispatch Updates Incident State

A successful dispatch should update the incident state appropriately.

Normally:

```text
REPORTED -> ASSIGNED
```

---

## BR-DIS-008 — Dispatch Must Be Atomic

The core dispatch operation must behave as one business transaction.

The system must avoid states where:

* Dispatch exists but resource remains available
* Resource is busy but no dispatch exists
* Incident claims assignment but no dispatch exists
* History indicates success while the actual assignment failed

---

# 9. Dispatch Candidate Rules

The Dispatch Engine should process resources in two conceptual stages.

## Stage 1 — Eligibility Filtering

Remove resources that cannot respond.

Potential filters:

1. Resource status
2. Resource type
3. Required capability
4. Operational constraints
5. Existing active assignments
6. Location validity

---

## Stage 2 — Candidate Ranking

Rank the remaining eligible resources.

Potential ranking factors:

1. Incident severity
2. Capability suitability
3. Distance
4. Expected response time
5. Current workload
6. Operational priority
7. Existing assignments

The final scoring algorithm will be defined separately in the Dispatch Engine Design document.

---

# 10. Resource Ranking Rules

## BR-RANK-001 — Do Not Simply Select the First Resource

The system must not use a simplistic rule such as:

```text
find first AVAILABLE resource
```

---

## BR-RANK-002 — Mandatory Requirements Come First

A resource that fails a mandatory requirement must not outrank a resource that satisfies it.

---

## BR-RANK-003 — Distance Is Not the Only Factor

The closest resource is not automatically the best resource.

Example:

```text
Ambulance A
Distance = 2 km
Capability = insufficient

Ambulance B
Distance = 5 km
Capability = required

Result:
Ambulance B is eligible.
Ambulance A is not.
```

---

## BR-RANK-004 — Ranking Must Be Deterministic

Given the same system state and same dispatch criteria, the ranking process should produce a predictable ordering.

This makes the system easier to:

* Test
* Debug
* Explain
* Maintain

---

## BR-RANK-005 — Tie-Breaking Must Be Defined

If two resources have equivalent ranking scores, the system must apply a deterministic tie-breaker.

Potential tie-breakers include:

1. Shorter distance
2. Lower workload
3. Earlier availability
4. Stable resource identifier

The exact order will be finalized during Dispatch Engine Design.

---

# 11. Severity and Priority Rules

## BR-PRI-001 — Critical Incidents Have Highest Priority

`CRITICAL` incidents must receive higher dispatch priority than lower-severity incidents.

---

## BR-PRI-002 — Severity Influences Dispatch Decisions

Severity should influence:

* Incident processing order
* Candidate evaluation
* Resource allocation
* Reporting

---

## BR-PRI-003 — Severity Alone Does Not Override Eligibility

A critical incident cannot justify assigning an unsuitable resource.

Example:

```text
Critical incident
+
Unavailable resource
=
Invalid dispatch
```

---

# 12. Concurrency Rules

Concurrency is a first-class business concern in ResQGrid.

---

## BR-CON-001 — Shared Resource State Must Be Protected

Operations that read and modify resource assignment state must be designed to handle concurrent access.

---

## BR-CON-002 — Check-Then-Assign Must Be Atomic

The following sequence is unsafe if performed independently:

```text
1. Check resource is AVAILABLE
2. Another thread assigns resource
3. First thread assigns resource
```

The system must ensure that eligibility verification and assignment cannot produce an invalid double assignment.

---

## BR-CON-003 — Only One Conflicting Assignment May Succeed

If two concurrent operations attempt to assign the same resource, the system must ensure that conflicting assignments do not both succeed.

---

## BR-CON-004 — Database and Application Concurrency Must Agree

Thread synchronization alone is not sufficient protection once multiple application instances or database transactions are involved.

The final design must consider both:

* Application-level concurrency
* Database-level concurrency

---

## BR-CON-005 — Failed Concurrent Assignment Must Leave Consistent State

If a concurrent dispatch loses the assignment race:

* It must not leave partial data.
* It must not incorrectly mark the resource as busy.
* It must not create a false dispatch record.
* It must return an appropriate business outcome.

---

# 13. Transaction Rules

## BR-TXN-001 — Related State Changes Must Be Transactional

Operations that represent one business action must be committed or rolled back together.

---

## BR-TXN-002 — Dispatch Transaction

The core dispatch transaction may include:

```text
Validate incident
        |
Find candidate
        |
Verify resource availability
        |
Create dispatch
        |
Update resource
        |
Update incident
        |
Record history
        |
Commit
```

If a critical operation fails, the transaction should roll back as appropriate.

---

## BR-TXN-003 — No Partial Dispatch

The database must not permanently contain a partially completed dispatch operation.

---

# 14. History Rules

## BR-HIS-001 — Important State Changes Should Be Traceable

Important operational events should be recorded.

Examples:

* Incident created
* Incident status changed
* Resource dispatched
* Dispatch status changed
* Resource released
* Incident resolved
* Dispatch cancelled

---

## BR-HIS-002 — History Should Preserve Operational Context

A historical event should contain enough information to understand what happened and when it happened.

---

## BR-HIS-003 — History Is Not Current State

Historical records describe past events.

Current state should remain represented by the appropriate current domain entities.

History should not be used as a substitute for current resource or incident state.

---

# 15. Validation Rules

## BR-VAL-001 — Required Data Must Be Present

Mandatory information must be provided before an operation proceeds.

---

## BR-VAL-002 — Enum Values Must Be Valid

Values representing:

* Severity
* Status
* Resource type
* Dispatch status

must correspond to supported domain values.

---

## BR-VAL-003 — Invalid Business Operations Must Be Rejected

The system must reject operations that violate domain rules even when the input format itself is valid.

---

## BR-VAL-004 — Validation Must Be Consistent

The same business rule should not be implemented differently by different controllers.

Business validation belongs in an appropriate shared business/domain layer.

---

# 16. Reporting Rules

## BR-REP-001 — Reports Must Reflect Persisted State

Operational reports should be based on reliable system data.

---

## BR-REP-002 — Current and Historical Data Must Be Distinguishable

Reports must clearly distinguish between:

* Current resource state
* Current incident state
* Historical dispatch activity

---

## BR-REP-003 — Aggregations Must Be Correct

Counts and statistics must correctly account for:

* Incident status
* Incident severity
* Resource status
* Dispatch status
* Relevant time periods

---

# 17. Core System Invariants

The following invariants are especially important.

## Invariant 1 — No Double Assignment

```text
A resource cannot have two conflicting active assignments.
```

---

## Invariant 2 — Busy Resource Has Active Work

Under the normal dispatch model:

```text
BUSY resource -> associated active dispatch
```

---

## Invariant 3 — Successful Dispatch Has Resource

```text
Successful dispatch -> valid resource
```

---

## Invariant 4 — Successful Dispatch Has Incident

```text
Successful dispatch -> valid incident
```

---

## Invariant 5 — Resolved Incident Is Not Dispatchable

```text
RESOLVED incident -> cannot receive normal new dispatch
```

---

## Invariant 6 — Cancelled Incident Is Not Dispatchable

```text
CANCELLED incident -> cannot receive normal new dispatch
```

---

## Invariant 7 — Unavailable Resource Is Not Dispatchable

```text
BUSY/OFFLINE/MAINTENANCE -> not eligible for normal dispatch
```

---

## Invariant 8 — Dispatch State and Resource State Must Agree

The system must avoid contradictory operational states.

---

# 18. Business Rule Priority

When multiple rules appear to conflict, the system should generally prioritize:

1. Data integrity
2. Safety and eligibility
3. Concurrency correctness
4. Mandatory capability requirements
5. Incident severity
6. Response efficiency
7. Distance and optimization
8. Tie-breaking rules

The exact precedence will be finalized during Dispatch Engine Design.

---

# 19. Implementation Principle

Business rules must not be duplicated unnecessarily across:

* JSP
* Servlets
* REST controllers
* Repositories
* SQL scripts

The system should establish a clear business layer responsible for enforcing domain behavior.

Database constraints may additionally enforce integrity where appropriate.

---

# 20. Testing Implications

Every important business rule should eventually have one or more tests.

Examples:

```text
Given an AVAILABLE ambulance
When it is dispatched
Then it becomes BUSY
```

```text
Given a BUSY ambulance
When a dispatch is requested
Then the ambulance is rejected
```

```text
Given two concurrent dispatch requests
When both attempt to assign the same ambulance
Then conflicting assignments cannot both succeed
```

```text
Given a RESOLVED incident
When a dispatch is requested
Then the dispatch is rejected
```

```text
Given a resource without the required capability
When candidate resources are evaluated
Then the resource is excluded
```

---

# 21. Future Rule Extensions

Future versions may introduce:

* Multiple resources per incident
* Resource teams
* Specialized equipment
* Maximum response distance
* Resource fatigue/workload limits
* Regional dispatch boundaries
* Escalation rules
* Priority aging
* SLA-based dispatching
* Emergency override policies

These are not part of the initial baseline unless deliberately added later.

---

# 22. Document Status

**Document 04 — Business Rules**

Status: Completed as the initial business-rule baseline.

These rules will directly influence:

* Domain model
* Service design
* Dispatch Engine
* Database constraints
* Transaction boundaries
* Concurrency strategy
* Unit tests
* Integration tests
* REST behavior