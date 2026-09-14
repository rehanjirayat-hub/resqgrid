# ResQGrid — Business Rules

## 1. Purpose

This document defines the business rules governing how ResQGrid behaves during emergency incident management, resource management, resource selection, dispatch, and response completion.

These rules are authoritative for the implementation.

Business logic must follow these rules regardless of whether an operation originates from the REST API, web interface, or another application component.

If a business requirement changes, this document must be updated before the affected implementation is changed.

---

# 2. General Business Rules

### BR-01 — Valid Domain State

Every incident, resource, response team, and dispatch must remain in a valid documented state.

The system must reject operations that would create an invalid state.

### BR-02 — Business Rules Are Centralized

Core business decisions must be implemented in the business/service layer.

Controllers, JSP views, and persistence components must not independently implement dispatch decisions.

### BR-03 — No Undocumented Business Behavior

The implementation must not introduce business rules that are not defined in the project documentation.

---

# 3. Incident Rules

### BR-INC-01 — Incident Must Have Required Information

An incident cannot be created unless all information required by the domain model is provided and valid.

The exact required fields will be defined in the Domain Model document.

### BR-INC-02 — Severity Is Mandatory

Every active incident must have a severity.

Supported initial severity levels are:

* CRITICAL
* HIGH
* MEDIUM
* LOW

### BR-INC-03 — Severity Represents Response Priority

Incident severity influences the urgency and priority of resource-dispatch decisions.

Higher-severity incidents must receive higher operational priority than lower-severity incidents when the relevant dispatch conditions are otherwise comparable.

### BR-INC-04 — Incident State Must Be Valid

An incident can only move between states permitted by the documented incident lifecycle.

Arbitrary state changes are not permitted.

### BR-INC-05 — Resolved Incidents

A resolved incident must not be treated as an active incident requiring a new normal dispatch.

### BR-INC-06 — Incident Updates

Incident information may only be modified when the current incident state permits the requested modification.

---

# 4. Resource Rules

### BR-RES-01 — Resource Must Have Valid Operational State

Every resource must have one valid operational status.

Initial statuses are:

* AVAILABLE
* BUSY
* OFFLINE
* MAINTENANCE

### BR-RES-02 — Available Resource

A resource with status AVAILABLE may be considered for dispatch, subject to all other eligibility requirements.

AVAILABLE status alone does not guarantee dispatch eligibility.

### BR-RES-03 — Busy Resource

A resource with status BUSY must not be selected for a new dispatch that would conflict with its existing assignment.

### BR-RES-04 — Offline Resource

A resource with status OFFLINE must not be selected for normal dispatch.

### BR-RES-05 — Maintenance Resource

A resource with status MAINTENANCE must not be selected for normal dispatch.

### BR-RES-06 — Resource Status Must Reflect Operational Reality

Resource availability must remain consistent with its active dispatch assignments.

A resource must not remain AVAILABLE while simultaneously being assigned to an exclusive active dispatch.

---

# 5. Resource Capability Rules

### BR-CAP-01 — Capability Matching

When an incident requires specific capabilities, a resource must satisfy the required capabilities before it can be considered eligible.

### BR-CAP-02 — Mandatory Capability

Failure to satisfy a mandatory capability requirement makes a resource ineligible.

### BR-CAP-03 — Capability Is Part of Eligibility

Capability matching occurs during resource filtering and must occur before final resource ranking.

---

# 6. Resource Type Rules

### BR-TYPE-01 — Resource Type Must Be Considered

The dispatch engine must consider whether a resource's type is appropriate for the incident.

Initial resource types include:

* Ambulance
* Fire Unit
* Rescue Team

### BR-TYPE-02 — Incompatible Resource Types

A resource whose type cannot satisfy the incident's mandatory requirements must not be selected.

### BR-TYPE-03 — Type Matching Before Ranking

Mandatory resource-type requirements must be evaluated before a resource becomes a final ranking candidate.

---

# 7. Response Team Rules

### BR-TEAM-01 — Team Availability

Where a dispatch requires a response team, the team must be operationally available before assignment.

### BR-TEAM-02 — Team Capability

Where team capabilities are required, the team must satisfy the applicable capability requirements.

### BR-TEAM-03 — Team Assignment

A response team must not be assigned to conflicting active operations when the team's operational model permits only one assignment.

### BR-TEAM-04 — Team State Consistency

Team availability must remain consistent with active assignments.

---

# 8. Resource Eligibility Rules

A resource may proceed to ranking only when it satisfies all mandatory eligibility conditions.

At minimum, eligibility may consider:

1. Resource operational status.
2. Resource type.
3. Required capabilities.
4. Response-team requirements.
5. Existing assignments.
6. Operational constraints.

The dispatch engine must distinguish between:

**Eligibility** — whether the resource is allowed to participate.

and

**Ranking** — how suitable an eligible resource is compared with other eligible resources.

An ineligible resource must not become eligible merely because it has a favorable distance or workload score.

---

# 9. Dispatch Selection Rules

### BR-DIS-01 — No First-Available Selection

The dispatch engine must never use a simple "first available resource" strategy as its primary selection mechanism.

### BR-DIS-02 — Multi-Criteria Selection

The dispatch engine must evaluate multiple documented factors when selecting among eligible resources.

Factors include:

* Incident severity.
* Resource availability.
* Resource type.
* Required capabilities.
* Distance.
* Workload.
* Existing assignments.
* Team availability.
* Operational constraints.
* Expected response suitability.

### BR-DIS-03 — Filtering Before Ranking

The dispatch process must follow this conceptual sequence:

**Identify Candidates**

→ **Filter Ineligible Resources**

→ **Rank Eligible Resources**

→ **Select Best Candidate**

→ **Verify Assignment Availability**

→ **Create Dispatch**

### BR-DIS-04 — Best Candidate

The selected resource must be the best eligible candidate according to the documented dispatch-ranking strategy.

### BR-DIS-05 — No Eligible Resource

If no eligible resource exists, the system must not create an invalid dispatch.

The incident must remain in an appropriate operational state according to the dispatch workflow.

The exact failure representation will be defined in the Error Handling Design.

---

# 10. Distance Rules

### BR-DIST-01 — Distance May Influence Ranking

Distance between the incident and resource location may influence resource ranking.

### BR-DIST-02 — Distance Is Not Sole Selection Criterion

The closest resource must not automatically be selected when another eligible resource provides a better overall operational response.

### BR-DIST-03 — Distance Calculation

The exact distance representation and calculation method will be defined in the Dispatch Engine Design and Domain Model documents.

---

# 11. Workload Rules

### BR-WORK-01 — Existing Workload Must Be Considered

A resource's current workload may influence dispatch ranking.

### BR-WORK-02 — Workload Must Not Override Mandatory Eligibility

A favorable workload value cannot make an otherwise ineligible resource eligible.

### BR-WORK-03 — Workload Consistency

Workload information must be derived from or remain consistent with the resource's active assignments and documented operational state.

---

# 12. Dispatch Creation Rules

### BR-DISP-01 — Dispatch Requires an Eligible Resource

A dispatch cannot be created unless a valid eligible resource has been selected.

### BR-DISP-02 — Assignment Verification

The system must verify that the selected resource is still assignable immediately before completing the assignment operation.

This is required because resource availability may change between candidate selection and final assignment.

### BR-DISP-03 — Resource State Update

When an exclusive resource is successfully dispatched, its operational state must be updated according to the resource lifecycle.

### BR-DISP-04 — Dispatch State

Every dispatch must have a valid dispatch status.

The exact initial and subsequent statuses will be finalized in the Domain Model document.

### BR-DISP-05 — Related State Consistency

Creation of a dispatch and the corresponding resource-state change must not leave the system in an inconsistent state.

---

# 13. Concurrency Rules

### BR-CON-01 — Concurrent Requests Are Expected

The system must assume that multiple incidents may request resources concurrently.

### BR-CON-02 — No Double Assignment

Two concurrent dispatch operations must not successfully assign the same exclusive resource when only one active assignment is permitted.

### BR-CON-03 — Final Assignment Is Atomic

The final operation that claims a resource must protect the resource from concurrent conflicting assignments.

### BR-CON-04 — Failed Assignment

If a resource can no longer be assigned when final assignment is attempted, that dispatch operation must fail or retry according to the documented concurrency strategy.

### BR-CON-05 — Consistent State

Concurrency handling must preserve consistency among:

* Incident state.
* Resource state.
* Dispatch state.
* Response-team state where applicable.
* Database state.

The exact locking, transaction, synchronization, and isolation strategy belongs to `09-CONCURRENCY-DESIGN.md`.

---

# 14. Dispatch Lifecycle Rules

The conceptual dispatch workflow is:

**Incident Reported**

→ **Assessed**

→ **Requirements Determined**

→ **Resources Identified**

→ **Resources Filtered**

→ **Resources Ranked**

→ **Resource/Team Selected**

→ **Dispatch Created**

→ **Response In Progress**

→ **Response Completed**

→ **Incident Resolved**

Only documented transitions may occur.

---

# 15. Dispatch Completion Rules

### BR-COMP-01 — Active Dispatch Required

A dispatch cannot be completed unless it is currently in a state that permits completion.

### BR-COMP-02 — Completion Updates State

When a dispatch is completed, the system must update its dispatch state.

### BR-COMP-03 — Resource Availability

After completion, the associated resource must be returned to the appropriate operational state according to business rules.

### BR-COMP-04 — History

Dispatch completion must preserve relevant historical information.

---

# 16. Incident Resolution Rules

### BR-RESOLVE-01 — Valid Resolution

An incident may only be resolved when its current state permits resolution.

### BR-RESOLVE-02 — Required Response Completion

Where the incident requires an active response, required response operations must be completed before the incident can be resolved.

### BR-RESOLVE-03 — No Invalid Resolution

The system must reject attempts to resolve an incident that still has an unresolved required response operation.

---

# 17. History Rules

### BR-HIST-01 — Dispatch History

Completed dispatch operations must remain available as historical records.

### BR-HIST-02 — Historical Accuracy

Historical records must represent what actually occurred and must not be silently overwritten merely because the current resource or incident state has changed.

### BR-HIST-03 — Operational Traceability

The system should provide sufficient historical information to understand resource assignment and dispatch activity.

---

# 18. Reporting Rules

### BR-REPORT-01 — Reports Use Persisted Operational Data

Operational reports must be based on the system's stored operational information.

### BR-REPORT-02 — Resource Utilization

Resource-utilization reporting must reflect documented resource and dispatch history.

### BR-REPORT-03 — Dispatch Statistics

Dispatch statistics must be calculated from valid dispatch records.

### BR-REPORT-04 — Incident Statistics

Incident statistics must be based on valid incident information and documented status/severity rules.

---

# 19. Validation Rules

### BR-VAL-01 — Validate Before State Change

Input and business validation must occur before an operation changes persistent domain state.

### BR-VAL-02 — Invalid Values

Unsupported enum/status/severity/type values must be rejected.

### BR-VAL-03 — Invalid Transitions

State transitions not permitted by the documented lifecycle must be rejected.

### BR-VAL-04 — Missing Required Data

Required domain information must be present before the operation is accepted.

---

# 20. Transaction Rules

### BR-TX-01 — Atomic Business Operations

Operations that require multiple related state changes must be treated as one logical business operation where consistency requires it.

### BR-TX-02 — Dispatch Transaction

The final resource assignment and corresponding dispatch creation must be consistent.

### BR-TX-03 — Rollback on Failure

If a transactionally related operation fails, the system must not leave partially applied state where the affected operation requires atomicity.

The exact transaction mechanism will be defined in the System Architecture and Concurrency Design documents.

---

# 21. Priority Rules

Incident severity contributes to response priority.

Initial priority ordering is:

**CRITICAL > HIGH > MEDIUM > LOW**

Priority does not mean that a resource can ignore mandatory eligibility requirements.

For example, a CRITICAL incident must receive higher priority, but an incompatible resource must not be dispatched merely because the incident is critical.

---

# 22. Tie-Breaking Rules

When multiple eligible resources have equivalent ranking according to the primary selection criteria, the dispatch engine must use a deterministic tie-breaking strategy.

The exact tie-breaking mechanism shall be defined in:

`08-DISPATCH-ENGINE-DESIGN.md`

No arbitrary or implementation-order-dependent tie-breaking behavior shall be relied upon.

---

# 23. Failure Rules

The system must fail safely when an operation cannot be completed.

Examples include:

* No eligible resource.
* Resource became unavailable.
* Invalid state transition.
* Invalid input.
* Persistence failure.
* Concurrent assignment conflict.

A failure must not result in an invalid resource, incident, or dispatch state.

---

# 24. Business Rule Priority

When multiple business considerations apply, mandatory constraints take precedence over optimization criteria.

The conceptual order is:

1. Safety and mandatory operational constraints.
2. Resource eligibility.
3. Incident priority.
4. Capability/type suitability.
5. Availability.
6. Distance and response suitability.
7. Workload and other ranking factors.
8. Deterministic tie-breaking.

The exact mathematical ranking strategy is intentionally not defined here and belongs to the Dispatch Engine Design document.

---

# 25. Business Rule Boundaries

This document defines business behavior but does not define implementation details such as:

* Java classes.
* Java fields.
* Java methods.
* Package structure.
* SQL queries.
* Database table structure.
* JPA annotations.
* Hibernate mappings.
* Synchronization primitives.
* API URLs.
* HTTP request/response DTO structures.

Those details must be defined by their respective project documents.

---

# 26. Documentation Consistency

This document must remain consistent with:

* `01-PROJECT-OVERVIEW.md`
* `02-REQUIREMENTS-SPECIFICATION.md`
* `03-ACTORS-AND-USE-CASES.md`
* `05-DOMAIN-MODEL.md`
* `06-SYSTEM-ARCHITECTURE.md`
* `07-DATABASE-DESIGN.md`
* `08-DISPATCH-ENGINE-DESIGN.md`
* `09-CONCURRENCY-DESIGN.md`
* `10-API-DESIGN.md`
* `11-ERROR-HANDLING-DESIGN.md`
* `12-TESTING-STRATEGY.md`
* `13-DEVELOPMENT-ROADMAP.md`

If a conflict is found between documents, implementation must stop and the conflict must be resolved in documentation before coding continues.
