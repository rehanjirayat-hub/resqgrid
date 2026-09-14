# ResQGrid — Concurrency Design

## 1. Purpose

This document defines how ResQGrid handles concurrent operations that may attempt to modify the same operational resource simultaneously.

The primary concurrency problem is:

> Two or more incidents may attempt to assign the same emergency resource at the same time.

The system must prevent:

* Double assignment.
* Lost updates.
* Invalid resource state.
* Conflicting active dispatches.
* Partially completed assignments.
* Inconsistent incident/resource/dispatch state.

---

# 2. Concurrency Scenario

Consider one available ambulance:

```text id="d9w0w5"
             +--> Incident A
             |
Ambulance A -+
             |
             +--> Incident B
```

Both incidents may determine that Ambulance A is the best candidate.

Without concurrency protection:

```text id="4l2h7m"
Thread A: Resource AVAILABLE
Thread B: Resource AVAILABLE

Thread A: Assign Resource
Thread B: Assign Resource

Result:
Same Resource assigned twice
```

This state is invalid.

---

# 3. Core Concurrency Invariant

The following invariant must always hold:

> A resource must not have more than one conflicting active assignment at the same time.

Historical completed dispatches do not violate this rule.

Only currently conflicting active assignments are relevant.

---

# 4. Authoritative State

For concurrency-sensitive assignment, the persisted database state is authoritative.

An in-memory representation of resource availability must not be assumed to remain correct while another thread or application operation may modify the same resource.

Therefore, final assignment verification must consider current persistent state.

---

# 5. Concurrency Boundary

Concurrency protection is required at the point where the system changes shared resource state.

The critical operation is:

```text id="a8wz0c"
Resource Availability Verification
          +
Dispatch Creation
          +
Resource State Change
```

These operations must be coordinated so that another concurrent operation cannot create an invalid conflicting assignment between verification and update.

---

# 6. Candidate Selection Is Not Final Assignment

The Dispatch Engine may identify a resource as the best candidate.

However:

> Candidate selection does not guarantee final availability.

Between candidate selection and final assignment, another concurrent request may acquire the resource.

Therefore:

```text id="5i8t4q"
Candidate Selection
       ≠
Final Assignment
```

The system must perform final assignment verification.

---

# 7. Final Assignment Verification

The conceptual process is:

```text id="5p2k6h"
Find Candidates
      |
      v
Filter Candidates
      |
      v
Rank Candidates
      |
      v
Select Best Candidate
      |
      v
Final Availability Verification
      |
   +--+--+
   |     |
Valid   Invalid
   |     |
   v     v
Assign  Re-evaluate
```

The final verification must occur immediately before the resource is committed to the dispatch operation.

---

# 8. Transaction Boundary

The final assignment operation must be transactional.

The conceptual transaction is:

```text id="h4e8v2"
BEGIN
  |
  v
Verify Resource State
  |
  v
Create Dispatch
  |
  v
Change Resource State
  |
  v
COMMIT
```

If the operation cannot complete successfully, the transaction must not leave partial assignment state.

---

# 9. Atomicity Requirement

The following changes are logically part of one successful assignment:

1. Confirm resource availability.
2. Create the dispatch.
3. Change the resource's operational state.

These changes must be treated as one atomic business operation.

The system must avoid situations such as:

```text id="4x9g4v"
Dispatch created
+
Resource still AVAILABLE
```

or:

```text id="o9fj73"
Resource changed to BUSY
+
Dispatch was not created
```

unless the state is subsequently corrected by a documented recovery process.

---

# 10. Concurrency Control Strategy

The initial design will use database-backed concurrency control.

The exact mechanism will be selected during implementation design from appropriate relational transaction mechanisms.

Possible mechanisms include:

* Optimistic concurrency control.
* Pessimistic locking.
* Conditional state update.
* Database row locking.
* Transaction isolation behavior.

The final mechanism must be selected based on the documented requirements and learning objectives.

No mechanism should be introduced merely because it is familiar.

---

# 11. Preferred Design Principle

The concurrency mechanism must guarantee this property:

> At most one concurrent transaction can successfully transition a particular available resource into a conflicting assigned state.

This is more important than the specific locking API or framework mechanism used.

---

# 12. Conditional Assignment Principle

A safe assignment operation should conceptually behave like:

```text id="w9x1c3"
Current state is AVAILABLE
        |
        v
Attempt atomic state transition
        |
   +----+----+
   |         |
Success    Failure
   |         |
   v         v
Continue   Resource
assignment  already changed
```

If the transition fails because another transaction already changed the resource state, the current operation must not create a successful conflicting dispatch.

---

# 13. Lost Update Prevention

The system must prevent lost updates.

Example:

```text id="8p5p8x"
Thread A reads resource as AVAILABLE
Thread B reads resource as AVAILABLE

Thread A changes resource
Thread B overwrites state using stale information
```

This must not result in an invalid final state.

Concurrency control must ensure that stale state cannot silently overwrite a newer valid state.

---

# 14. Double Assignment Prevention

The following sequence must be impossible as a successful final outcome:

```text id="4q9a5j"
Incident A -> Resource X -> ACTIVE
Incident B -> Resource X -> ACTIVE
```

when both assignments conflict in time.

One assignment may succeed.

The competing assignment must detect the conflict and follow the documented failure/retry behavior.

---

# 15. Resource State Transition

The important operational transition is:

```text id="z9q6os"
AVAILABLE
    |
    | successful assignment
    v
BUSY
```

The transition must be protected from concurrent conflicting transitions.

A resource already in `BUSY`, `OFFLINE`, or `MAINTENANCE` state must not become successfully assigned through stale availability information.

---

# 16. Dispatch Creation and Resource Update

Dispatch creation and resource-state modification must remain consistent.

Successful operation:

```text id="1w8h1a"
Incident
   |
   +---- Dispatch CREATED / IN_PROGRESS
                |
                +---- Resource BUSY
```

Failure must not leave only one side of the relationship committed.

---

# 17. Isolation Considerations

Database transaction isolation affects what concurrent operations can observe.

The project will study how PostgreSQL transaction isolation interacts with:

* Concurrent reads.
* Concurrent updates.
* Row locking.
* Lost updates.
* Assignment conflicts.
* Transaction commits and rollbacks.

The exact isolation level must be explicitly selected before implementation.

It must not be chosen randomly.

---

# 18. Row-Level Concurrency

Resource assignment is naturally associated with a particular resource record.

Therefore database-level row-level concurrency mechanisms may be appropriate.

The implementation must evaluate whether locking the relevant resource row provides the required protection.

The final decision must be documented before coding.

---

# 19. Pessimistic Locking

Pessimistic locking may be used when the design requires a transaction to prevent other transactions from modifying a resource while the assignment operation is being completed.

Conceptually:

```text id="9t3j7q"
Transaction A
     |
Lock Resource X
     |
Verify
     |
Assign
     |
Commit
     |
Release Lock
```

A concurrent transaction attempting to modify the same resource must respect the database locking behavior.

Whether pessimistic locking is selected will be determined after evaluating the complete assignment workflow.

---

# 20. Optimistic Concurrency Control

Optimistic concurrency control may be considered when the system can detect that the resource changed after it was originally read.

Conceptually:

```text id="qx4f0f"
Read Resource
     |
Remember Version / State
     |
Perform Assignment Attempt
     |
Verify Version / State
     |
   +--+--+
   |     |
Same   Changed
   |     |
   v     v
Assign  Conflict
```

If the resource changed, the operation must not overwrite the newer state.

---

# 21. Conditional State Transition

Another valid concurrency approach is an atomic conditional update based on the current resource state.

Conceptually:

```text id="1v9gk4"
AVAILABLE
   |
   | conditional update
   v
BUSY
```

The operation succeeds only if the resource is still in the expected state.

A failure indicates that another operation changed the resource first.

The exact SQL/JDBC/JPA implementation is intentionally deferred.

---

# 22. Final Mechanism Selection

Before implementing concurrency, the project must explicitly select the mechanism that will be used.

The decision must consider:

* Correctness.
* PostgreSQL behavior.
* JDBC behavior.
* JPA/Hibernate behavior.
* Transaction boundaries.
* Testability.
* Understandability.
* Project learning objectives.

The selected mechanism must be recorded in the documentation before implementation begins.

---

# 23. Concurrent Dispatch Flow

The complete conceptual flow is:

```text id="7skm8v"
Incident A
   |
   v
Determine Requirements
   |
   v
Find / Filter / Rank
   |
   v
Select Resource X
   |
   v
Begin Assignment Transaction
   |
   v
Final Verification
   |
   +----------------------+
   |                      |
   | Resource Available   | Resource Unavailable
   |                      |
   v                      v
Assign Resource           Conflict
Create Dispatch             |
Change Resource State       |
   |                        v
Commit                  Re-evaluate / Fail
```

---

# 24. Competing Transaction Flow

For two simultaneous requests:

```text id="z3c5h0"
Transaction A                 Transaction B
-------------                 -------------
Select Resource X             Select Resource X
       |                             |
       v                             v
Final Verification            Final Verification
       |                             |
       v                             |
Acquire / Update X            Wait / Detect Conflict
       |                             |
       v                             v
Create Dispatch A             Assignment Fails
       |                             |
       v                             v
Resource BUSY                 No conflicting Dispatch
       |
       v
Commit
```

The exact behavior depends on the selected concurrency mechanism.

---

# 25. Retry Behavior

A concurrency conflict may cause the selected resource to become unavailable.

The system may then re-evaluate remaining eligible candidates.

Conceptually:

```text id="5d6qg4"
Conflict
   |
   v
Check Remaining Candidates
   |
   +---- Candidate Available ----> Retry Assignment
   |
   +---- No Candidate ------------> Report No Available Resource
```

Retry behavior must be bounded.

The system must not retry indefinitely.

The exact retry count and strategy must be defined before implementation.

---

# 26. Retry and Transaction Separation

A failed transaction must be handled cleanly before another assignment attempt is made.

A retry must not accidentally reuse invalid transactional state.

Conceptually:

```text id="h8g1x5"
Transaction 1
     |
Conflict
     |
Rollback / End
     |
     v
Re-evaluate
     |
     v
Transaction 2
```

The implementation must respect the transaction semantics of the chosen persistence mechanism.

---

# 27. Deadlock Considerations

Concurrency mechanisms may introduce deadlocks when multiple transactions lock resources in inconsistent orders.

The design must therefore avoid unnecessary multi-resource locking.

If multiple resources or teams must be locked, a consistent ordering strategy must be considered.

The exact strategy must be documented if such a workflow is introduced.

---

# 28. Transaction Rollback

If any required operation fails inside the assignment transaction, the transaction must be rolled back as appropriate.

Potential failures include:

* Resource state update failure.
* Dispatch creation failure.
* Constraint violation.
* Concurrency conflict.
* Persistence failure.

The system must not treat a failed partial transaction as a successful dispatch.

---

# 29. Database Constraints and Application Logic

Concurrency correctness must not depend entirely on application-level checks.

Application checks are necessary:

```text
Is resource available?
```

But the final state change must also be protected against another concurrent operation.

Therefore:

```text id="i5e5c7"
Application Validation
        +
Database Concurrency Protection
        =
Safe Assignment
```

---

# 30. In-Memory Synchronization

Java synchronization mechanisms such as:

* `synchronized`.
* Java locks.
* In-memory mutexes.

must not be assumed to provide complete protection for database-backed resource assignment.

For example, multiple application instances would not share the same JVM lock.

The authoritative concurrency mechanism must therefore operate at the persistent/shared-state boundary.

In-memory synchronization may only be introduced for a documented reason.

---

# 31. Multiple Application Instances

The design must remain correct if more than one application process is operating against the same PostgreSQL database.

Concurrency protection must therefore not depend exclusively on a single JVM instance.

The database transaction/concurrency strategy is the primary protection boundary.

---

# 32. JDBC Concurrency

When JDBC is used, the implementation must explicitly manage:

* Connection transaction state.
* Commit.
* Rollback.
* Appropriate isolation behavior.
* Resource locking/conditional updates where selected.
* Statement execution.

JDBC operations must not accidentally commit partial assignment changes.

---

# 33. JPA/Hibernate Concurrency

When JPA/Hibernate is used, the project must study how ORM behavior interacts with:

* Persistence context.
* Entity state.
* Dirty checking.
* Transactions.
* Optimistic locking.
* Pessimistic locking.
* Database isolation.
* Concurrent updates.

The project must not assume that JPA/Hibernate automatically solves concurrency.

---

# 34. Persistence Context and Stale Data

A Hibernate persistence context may contain an entity state that is no longer current in the database.

Therefore the assignment process must be designed carefully around transaction boundaries and current persistent state.

The implementation must understand when an entity is:

* Managed.
* Detached.
* Refreshed.
* Updated.
* Flushed.

This is especially important during concurrent assignment.

---

# 35. Flush and Commit

The project must distinguish between:

```text id="b1u8az"
Application change
```

and:

```text id="m7r3s5"
Database commit
```

A change being present in the persistence context does not necessarily mean that another transaction has observed the committed state.

Concurrency reasoning must therefore consider the complete transaction lifecycle.

---

# 36. Concurrency Testing

Concurrency behavior must be tested explicitly.

A concurrency test should simulate at least:

```text id="4h8k4e"
Multiple concurrent dispatch attempts
        |
        v
Same incident/resource candidates
        |
        v
Concurrent assignment
        |
        v
Verify only valid assignment succeeds
```

Tests must verify that:

* A resource is not double-assigned.
* Conflicting transactions are detected.
* Resource state remains valid.
* Dispatch records remain consistent.
* Failed transactions do not leave partial state.

---

# 37. Reproducibility of Concurrency Tests

Concurrency tests must be designed carefully because thread scheduling is nondeterministic.

Tests should create controlled contention rather than relying on random timing.

The test strategy should make it possible to demonstrate the race condition and verify the protection mechanism.

---

# 38. Failure Scenarios to Test

At minimum, concurrency testing should consider:

### Scenario 1 — Same Resource

Two incidents compete for one resource.

### Scenario 2 — Multiple Resources

Two incidents have multiple eligible resources.

### Scenario 3 — Resource Becomes Busy

A resource changes state between candidate selection and final assignment.

### Scenario 4 — Transaction Failure

Dispatch creation fails after resource state processing begins.

### Scenario 5 — Concurrent State Change

Another operation changes the resource before final assignment.

### Scenario 6 — Retry

The initially selected resource becomes unavailable and another eligible resource exists.

---

# 39. Consistency Requirements

After every successful dispatch:

```text id="j0s6y4"
Incident State
     +
Dispatch State
     +
Resource State
     +
Team State where applicable
```

must be mutually consistent.

After every failed dispatch attempt, the database must remain in a valid state.

---

# 40. Concurrency and History

Concurrency protection must not delete or corrupt historical dispatch information.

If a dispatch attempt fails because another transaction won the resource, no successful conflicting historical dispatch should be recorded.

Completed historical dispatches must remain intact.

---

# 41. Observability

Concurrency conflicts should be identifiable during development and testing.

The application should be able to distinguish:

* Normal assignment.
* No eligible resource.
* Concurrency conflict.
* Persistence failure.

Logging may be used to understand concurrent operations without exposing sensitive information.

---

# 42. Performance Considerations

Concurrency protection should prevent invalid assignments without unnecessarily blocking unrelated resources.

For example:

```text id="w9qf3n"
Transaction A -> Ambulance A
Transaction B -> Ambulance B
```

should not be unnecessarily blocked merely because both operations are dispatching resources.

Locking should be as narrow as practical.

---

# 43. Concurrency Design Boundaries

This document intentionally does not finalize:

* Exact locking API.
* Exact PostgreSQL SQL statement.
* Exact transaction isolation level.
* Exact JPA annotation.
* Exact optimistic-locking field.
* Exact pessimistic-locking query.
* Exact retry count.
* Exact retry delay.
* Exact exception hierarchy.
* Exact Java implementation classes.
* Exact method signatures.

These decisions must be finalized before implementation.

---

# 44. Required Concurrency Decision

Before concurrency-related implementation begins, the project must explicitly document:

1. Concurrency mechanism.
2. Transaction boundary.
3. Resource state transition mechanism.
4. Conflict detection mechanism.
5. Retry behavior.
6. Rollback behavior.
7. Concurrency test strategy.

Only after these decisions are documented may concurrency code be implemented.

---

# 45. No Undocumented Concurrency Changes

If implementation reveals a new concurrency problem or suggests a different locking/retry mechanism:

1. Stop implementation.
2. Identify the affected rule.
3. Update this document.
4. Check consistency with:

    * `04-BUSINESS-RULES.md`
    * `06-SYSTEM-ARCHITECTURE.md`
    * `07-DATABASE-DESIGN.md`
    * `08-DISPATCH-ENGINE-DESIGN.md`
5. Only then continue implementation.

No concurrency behavior may be silently changed during coding.

---

# 46. Documentation Consistency

This document must remain consistent with:

* `01-PROJECT-OVERVIEW.md`
* `02-REQUIREMENTS-SPECIFICATION.md`
* `03-ACTORS-AND-USE-CASES.md`
* `04-BUSINESS-RULES.md`
* `05-DOMAIN-MODEL.md`
* `06-SYSTEM-ARCHITECTURE.md`
* `07-DATABASE-DESIGN.md`
* `08-DISPATCH-ENGINE-DESIGN.md`
* `10-API-DESIGN.md`
* `11-ERROR-HANDLING-DESIGN.md`
* `12-TESTING-STRATEGY.md`
* `13-DEVELOPMENT-ROADMAP.md`

If a conflict is discovered, implementation must stop.

The conflicting documentation must be reviewed and updated before coding continues.

The documentation remains the source of truth for the project.
