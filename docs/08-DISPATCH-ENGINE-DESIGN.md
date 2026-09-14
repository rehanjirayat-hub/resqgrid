# ResQGrid — Dispatch Engine Design

## 1. Purpose

This document defines the design of the ResQGrid Dispatch Engine.

The Dispatch Engine is responsible for selecting the most appropriate eligible emergency resource for an incident.

The engine must not simply select the first available resource.

It must:

1. Understand the incident requirements.
2. Identify possible candidates.
3. Filter candidates using mandatory eligibility rules.
4. Rank the remaining candidates.
5. Select the best candidate.
6. Perform final assignment verification.
7. Create the dispatch operation.
8. Ensure the resource cannot be assigned concurrently to conflicting incidents.

---

# 2. Design Principle

The primary principle is:

> **Eligibility first, optimization second.**

A resource that does not satisfy mandatory requirements must never become eligible merely because it has a better score in another category.

The engine therefore follows:

```text
Incident
   |
   v
Determine Requirements
   |
   v
Find Candidate Resources
   |
   v
Mandatory Eligibility Filtering
   |
   v
Ranking
   |
   v
Best Candidate
   |
   v
Final Assignment Verification
   |
   v
Dispatch
```

---

# 3. Dispatch Decision

The engine answers the following business question:

> Given an incident and the currently known operational state, which available resource is the most suitable valid resource to respond?

The answer must be based on documented business rules.

The engine must not make decisions based on arbitrary implementation convenience.

---

# 4. Input

The Dispatch Engine requires information about:

### Incident

Including:

* Incident identity.
* Severity.
* Status.
* Location.
* Response requirements.

### Resources

Including:

* Resource identity.
* Resource type.
* Resource status.
* Current location.
* Capabilities.
* Existing assignments.
* Operational constraints.

### Response Teams

Where the incident requires team participation:

* Team availability.
* Team capabilities.
* Existing assignments.
* Operational constraints.

---

# 5. Incident Requirements

Before resource selection begins, the system must determine what the incident requires.

Requirements may include:

* Required resource type.
* Required capabilities.
* Team requirement.
* Operational constraints.
* Other documented response requirements.

The Dispatch Engine must use the requirements determined by the business process.

It must not invent additional requirements during selection.

---

# 6. Candidate Identification

The first selection stage identifies resources that could potentially respond.

Candidate identification should retrieve resources relevant to the dispatch request rather than attempting to rank every resource in the system unnecessarily.

Potential candidates may be narrowed using information such as:

* Resource type.
* Operational status.
* Required capabilities.
* Location.

Candidate identification is not the same as final eligibility.

A candidate must still pass all mandatory eligibility rules.

---

# 7. Mandatory Eligibility Rules

A resource is eligible only when all mandatory requirements are satisfied.

The engine must evaluate factors including:

### Resource Status

The resource must be operationally available.

A resource in the following states is not normally dispatchable:

* BUSY.
* OFFLINE.
* MAINTENANCE.

Only an `AVAILABLE` resource is normally eligible.

Any documented exception must be explicitly defined before implementation.

---

# 8. Resource Type Eligibility

The resource must satisfy the required resource type for the incident.

For example:

```text
Required: AMBULANCE
```

does not permit an unrelated resource type to be selected merely because it is geographically closer.

Type compatibility is therefore a mandatory filtering rule.

---

# 9. Capability Eligibility

The resource must possess the capabilities required by the incident.

If an incident requires a capability that a resource does not possess, the resource is not eligible.

Capability filtering occurs before ranking.

A resource with excellent distance or workload characteristics cannot compensate for a missing mandatory capability.

---

# 10. Team Eligibility

When an incident requires a response team, the engine must verify that the required team conditions are satisfied.

This may include:

* Team availability.
* Required team capability.
* Existing active assignments.
* Other documented operational constraints.

A resource must not be selected if the required team cannot participate.

---

# 11. Existing Assignment Eligibility

A resource with a conflicting active dispatch must not be considered available for another conflicting assignment.

Historical completed dispatches do not make a resource unavailable.

The engine must distinguish between:

```text
Historical Dispatch
```

and

```text
Active Conflicting Dispatch
```

---

# 12. Operational Constraints

The engine must respect mandatory operational constraints documented by the project.

Examples include:

* Resource availability.
* Resource type.
* Required capability.
* Team availability.
* Existing active assignment.
* Incident-specific requirements.

Additional constraints must not be introduced without updating the appropriate documentation.

---

# 13. Eligibility Result

After filtering, the engine produces one of two outcomes:

### Eligible Candidates Exist

Continue to ranking.

### No Eligible Candidate Exists

The dispatch request cannot currently be assigned.

The system must not select an invalid resource simply to produce a successful dispatch.

The application must report an appropriate business outcome.

---

# 14. Ranking

Once mandatory filtering is complete, the remaining candidates are ranked.

Ranking is an optimization step.

The initial documented factors include:

* Incident severity.
* Distance.
* Workload.
* Resource suitability.
* Capability suitability where applicable.
* Operational considerations.

Mandatory requirements are already satisfied before ranking begins.

---

# 15. Incident Severity

Incident severity establishes the urgency of the response.

The documented priority order is:

```text
CRITICAL
HIGH
MEDIUM
LOW
```

Therefore:

```text
CRITICAL > HIGH > MEDIUM > LOW
```

Severity affects dispatch priority and operational decision-making.

Severity must not be treated as a substitute for mandatory resource eligibility.

---

# 16. Distance

Distance represents how far a candidate resource is from the incident.

When comparing otherwise eligible resources, a closer suitable resource should generally be preferred because it may provide a faster response.

The system must use the persisted location information defined by the database design.

Distance calculation must be consistent.

The exact mathematical distance calculation will be finalized during implementation design.

---

# 17. Workload

Workload represents the operational load associated with a resource.

A resource with lower current workload may be preferable to an otherwise comparable resource with a higher workload.

Workload must be derived from actual operational state and/or documented historical data.

The engine must not use arbitrary or manually invented workload values.

---

# 18. Resource Suitability

Resource suitability represents how well an eligible resource matches the incident requirements beyond basic type compatibility.

Relevant documented considerations may include:

* Capabilities.
* Operational state.
* Team compatibility.
* Existing workload.
* Other documented constraints.

Suitability must remain explainable.

The system should be able to identify why one eligible candidate ranked above another.

---

# 19. Ranking Principle

The ranking process must be deterministic where possible.

Given the same relevant operational state and the same dispatch request, the engine should produce the same ranking result.

This improves:

* Testability.
* Debugging.
* Predictability.
* Operational transparency.

---

# 20. Tie-Breaking

Two or more candidates may receive equivalent ranking outcomes.

A deterministic tie-breaking strategy must therefore exist.

Tie-breaking should use documented operational factors rather than arbitrary collection order.

The exact tie-breaking sequence must be finalized before implementation.

The implementation must never rely on accidental database ordering or unordered collection iteration to decide an assignment.

---

# 21. Ranking vs Filtering

Filtering and ranking must remain separate concepts.

### Filtering asks:

> Is this resource allowed to respond?

### Ranking asks:

> Among the resources allowed to respond, which is the most suitable?

Therefore:

```text
Invalid Candidate
      |
      X
   No Ranking
```

and:

```text
Eligible Candidates
      |
      v
    Rank
      |
      v
 Best Candidate
```

---

# 22. Candidate Ranking Process

The conceptual ranking flow is:

```text
All Relevant Resources
        |
        v
Status Filter
        |
        v
Type Filter
        |
        v
Capability Filter
        |
        v
Assignment Conflict Filter
        |
        v
Team / Operational Constraint Filter
        |
        v
Eligible Candidates
        |
        v
Compare Distance
        |
        v
Compare Workload
        |
        v
Compare Suitability
        |
        v
Apply Tie-Breaking
        |
        v
Best Candidate
```

The exact ordering of ranking factors must remain consistent with the finalized business rules.

---

# 23. No First-Available Rule

The Dispatch Engine must never implement:

```text
Find first AVAILABLE resource
→ Assign it
```

This would violate the core purpose of ResQGrid.

Instead:

```text
Find candidates
→ Filter
→ Rank
→ Select
```

---

# 24. Final Assignment Verification

Selecting the best candidate is not the end of the dispatch process.

Between candidate evaluation and final persistence, another concurrent operation may change the resource state.

Therefore the selected resource must be verified again immediately before final assignment.

Conceptually:

```text
Candidate Selected
       |
       v
Final State Verification
       |
   +---+---+
   |       |
Valid    Invalid
   |       |
   v       v
Assign   Retry / Fail
```

The exact concurrency mechanism is defined in:

`09-CONCURRENCY-DESIGN.md`

---

# 25. Concurrent Dispatch Scenario

Consider two incidents:

```text
Incident A ----\
                \
                 Ambulance A
                /
Incident B ----/
```

Both incidents may independently determine that Ambulance A is the best candidate.

The system must not allow both dispatch operations to successfully assign the same resource simultaneously.

Only one operation may win the final assignment.

The other operation must:

* Detect that the resource is no longer available.
* Avoid creating an invalid conflicting assignment.
* Follow the documented retry/failure behavior.

---

# 26. Dispatch Creation

After successful final verification, the system may create the dispatch.

The operation must establish the required relationship between:

* Incident.
* Resource.
* Response Team where applicable.

It must also establish:

* Dispatch status.
* Relevant timestamps.
* Required persistent state.

---

# 27. Resource State Transition

Successful assignment changes the operational state of the resource.

Conceptually:

```text
AVAILABLE
    |
    | successful dispatch
    v
BUSY
```

The resource must not remain incorrectly marked as `AVAILABLE` after successful assignment.

The state transition and dispatch creation must be treated consistently as one business operation.

---

# 28. Incident State Transition

When an incident receives a valid response, its state may progress according to the documented incident lifecycle.

The conceptual transition is:

```text
REQUIREMENTS_DETERMINED
          |
          v
RESPONSE_IN_PROGRESS
```

The exact transition conditions must follow the Business Rules document.

---

# 29. Dispatch Lifecycle

A dispatch follows the documented lifecycle:

```text
CREATED
   |
   v
IN_PROGRESS
   |
   v
COMPLETED
```

The engine is primarily responsible for creating the dispatch and initiating the response.

Completion is handled by the appropriate application/business operation.

---

# 30. Failed Assignment

A dispatch attempt may fail because:

* No eligible resource exists.
* The selected resource became unavailable.
* A concurrency conflict occurred.
* A required team became unavailable.
* A mandatory business rule was violated.
* A persistence/transaction failure occurred.

The system must not create a successful dispatch when final assignment conditions are not satisfied.

---

# 31. Retry Behavior

If the initially selected resource becomes unavailable during final assignment, the system may reevaluate the remaining candidates.

Conceptually:

```text
Select Best Candidate
       |
       v
Final Verification
       |
   Failed?
    /   \
  No     Yes
  |       |
Assign   Re-evaluate
          |
          v
   Remaining Candidates
```

The exact retry count and retry strategy must be defined in the Concurrency Design before implementation.

The system must avoid uncontrolled retry loops.

---

# 32. Explainability

The dispatch decision should be explainable.

For a selected resource, the system should be able to identify relevant decision information such as:

* Why the resource was eligible.
* Which required capabilities were satisfied.
* Its operational status.
* Its relative distance.
* Its workload.
* Why it ranked above other eligible candidates.

This supports:

* Debugging.
* Testing.
* Reporting.
* Operational transparency.

---

# 33. Separation of Responsibilities

The Dispatch Engine should focus on dispatch decision-making.

It should not directly handle:

* HTTP requests.
* JSP rendering.
* Servlet request parsing.
* Database connection management.
* Raw SQL scattered through business logic.
* UI presentation.

Persistence operations belong to the data-access layer.

Request handling belongs to controllers.

---

# 34. Lambda Expressions and Functional Operations

The implementation may use Java functional programming features where they improve readability and express the documented dispatch process.

Potentially useful operations include:

* Filtering candidates.
* Sorting/ranking candidates.
* Comparing candidate attributes.
* Transforming collections.

However, lambda expressions must not be introduced merely to make the implementation look advanced.

Readability and correctness take priority.

The implementation should make the filtering and ranking logic understandable to a Java developer.

---

# 35. Object-Oriented Design

The Dispatch Engine must follow object-oriented principles.

Responsibilities should be divided according to behavior rather than creating one excessively large method or class.

Potential responsibilities include:

* Candidate identification.
* Eligibility evaluation.
* Ranking.
* Assignment coordination.

The exact classes and interfaces must be decided during implementation design.

No class should be created simply because it sounds architecturally appropriate without a documented responsibility.

---

# 36. Persistence Interaction

The Dispatch Engine obtains operational information through the application/data-access architecture.

It must not assume that in-memory state alone represents the authoritative current resource state.

For concurrency-sensitive final assignment, persistent state must be considered authoritative.

---

# 37. Transaction Interaction

Final dispatch assignment requires transactional consistency.

The conceptual operation is:

```text
Verify Current Resource State
        |
        v
Create Dispatch
        |
        v
Update Resource State
        |
        v
Commit
```

If the required operation cannot be completed consistently, the transaction must not leave an invalid partial assignment.

The exact transaction mechanism belongs to the Concurrency Design.

---

# 38. Business Rule Traceability

The Dispatch Engine must implement the documented business rules, including:

* No first-available assignment.
* Mandatory eligibility before ranking.
* Resource availability requirements.
* Type compatibility.
* Capability compatibility.
* Team requirements.
* Existing assignment checks.
* Severity priority.
* Distance considerations.
* Workload considerations.
* Deterministic tie-breaking.
* Final assignment verification.
* Prevention of double assignment.

---

# 39. Performance Considerations

The engine should avoid unnecessary processing.

Potential optimizations include:

* Filtering candidates early.
* Retrieving only relevant resources.
* Avoiding unnecessary database queries.
* Avoiding repeated loading of the same data.
* Avoiding unnecessary sorting of ineligible resources.

Optimization must not compromise correctness.

The project will use actual query and application behavior to guide later performance improvements.

---

# 40. Failure Transparency

The system must distinguish between:

### No Eligible Resource

The system successfully evaluated candidates but none satisfied the requirements.

### Concurrent Assignment Conflict

A candidate was eligible earlier but became unavailable before final assignment.

### Technical Failure

A database, persistence, or infrastructure failure prevented completion.

These outcomes must not be incorrectly reported as the same business condition.

---

# 41. Dispatch Engine Output

The dispatch operation should provide an application-level result representing the outcome.

Possible conceptual outcomes include:

```text
SUCCESS
NO_ELIGIBLE_RESOURCE
ASSIGNMENT_CONFLICT
VALIDATION_FAILURE
TECHNICAL_FAILURE
```

The exact result representation will be finalized during implementation and error-handling design.

---

# 42. Algorithm Boundaries

This document defines the dispatch decision process but intentionally does not finalize:

* Exact scoring formula.
* Exact numeric weights.
* Exact distance formula.
* Exact workload calculation.
* Exact capability scoring.
* Exact tie-breaking sequence.
* Exact retry count.
* Exact concurrency mechanism.
* Exact locking strategy.
* Exact Java classes.
* Exact method signatures.

These must be finalized in the appropriate documentation before implementation.

---

# 43. Design Rule — No Undocumented Algorithm Changes

During implementation, if a developer believes that an additional ranking factor, score, constraint, retry behavior, or selection rule is required, implementation must stop.

The developer must:

1. Identify the proposed change.
2. Identify which rule/document it affects.
3. Update the appropriate documentation.
4. Review consistency with related documents.
5. Only then implement the change.

No undocumented dispatch behavior may be introduced.

---

# 44. Documentation Consistency

This document must remain consistent with:

* `01-PROJECT-OVERVIEW.md`
* `02-REQUIREMENTS-SPECIFICATION.md`
* `03-ACTORS-AND-USE-CASES.md`
* `04-BUSINESS-RULES.md`
* `05-DOMAIN-MODEL.md`
* `06-SYSTEM-ARCHITECTURE.md`
* `07-DATABASE-DESIGN.md`
* `09-CONCURRENCY-DESIGN.md`
* `10-API-DESIGN.md`
* `11-ERROR-HANDLING-DESIGN.md`
* `12-TESTING-STRATEGY.md`
* `13-DEVELOPMENT-ROADMAP.md`

If a conflict is discovered, implementation must stop.

The conflicting documentation must be reviewed and updated before coding continues.

The documentation remains the source of truth for the project.
