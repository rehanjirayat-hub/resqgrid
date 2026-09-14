# ResQGrid — Testing Strategy

## 1. Purpose

This document defines the testing strategy for ResQGrid.

Testing must verify not only that individual methods work, but that the complete system correctly enforces:

* Domain rules.
* Business rules.
* Resource eligibility.
* Dispatch selection.
* State transitions.
* Persistence behavior.
* Transaction boundaries.
* Concurrency guarantees.
* API behavior.
* Error handling.

The goal is to detect incorrect behavior before it reaches the final application.

---

# 2. Testing Principles

ResQGrid follows these principles:

1. Test behavior rather than implementation details.
2. Test business rules independently from infrastructure where practical.
3. Keep unit tests fast and deterministic.
4. Use integration tests where database behavior matters.
5. Test concurrency explicitly rather than assuming it works.
6. Test failure paths as seriously as successful paths.
7. Avoid tests that depend unnecessarily on execution order.
8. Keep tests maintainable and readable.
9. Every important business rule must have corresponding test coverage.
10. A feature is not complete until its required tests pass.

---

# 3. Testing Levels

ResQGrid will use multiple testing levels:

```text id="y4d2j0"
Unit Tests
    |
    v
Component / Service Tests
    |
    v
Persistence / Integration Tests
    |
    v
REST API Tests
    |
    v
Concurrency Tests
    |
    v
End-to-End Validation
```

Each level has a specific purpose.

---

# 4. Unit Testing

Unit tests verify individual units of behavior in isolation.

JUnit 5 will be the primary unit-testing framework.

Unit tests should cover:

* Domain behavior.
* Validation logic.
* State transitions.
* Business calculations.
* Eligibility rules.
* Ranking logic.
* Error conditions.

Unit tests should avoid requiring a real PostgreSQL database unless the behavior being tested specifically depends on persistence.

---

# 5. Domain Testing

Domain objects must be tested for important invariants.

Examples include:

* Valid incident severity.
* Valid incident status.
* Valid resource status.
* Valid dispatch status.
* Valid domain relationships.
* Valid lifecycle transitions.
* Invalid state transitions.

Tests must verify that invalid domain states cannot be created through supported domain operations.

---

# 6. Incident Testing

Incident tests must cover:

### Creation

* Valid incident creation.
* Required information.
* Initial status.

### Classification

* Valid severity values.
* Invalid severity values.

### Lifecycle

* Valid status transitions.
* Invalid status transitions.

### Resolution

* Valid resolution.
* Invalid resolution conditions.

---

# 7. Resource Testing

Resource tests must cover:

* Resource creation.
* Resource type.
* Resource status.
* Location.
* Capabilities.
* Status transitions.
* Availability rules.

Important scenarios include:

```text id="z0ph8u"
AVAILABLE -> BUSY
BUSY -> AVAILABLE
AVAILABLE -> OFFLINE
AVAILABLE -> MAINTENANCE
```

Only transitions permitted by the business rules should succeed.

---

# 8. Response Team Testing

Team tests must cover:

* Team creation.
* Team availability.
* Team capabilities.
* Valid updates.
* Invalid operations.
* Team participation in dispatch operations where applicable.

---

# 9. Capability Testing

Capability tests must verify:

* Capability creation/representation.
* Resource-capability association.
* Team-capability association.
* Capability matching.
* Missing capability rejection.

---

# 10. Dispatch Engine Testing

The Dispatch Engine is a critical part of ResQGrid and requires extensive testing.

Tests must verify the complete conceptual process:

```text id="6g5e9q"
Incident
   |
Determine Requirements
   |
Find Candidates
   |
Filter
   |
Rank
   |
Final Verification
   |
Assign
```

---

# 11. Eligibility Testing

The engine must reject resources that fail mandatory conditions.

Tests must cover:

* Resource is not AVAILABLE.
* Wrong resource type.
* Missing required capability.
* Required team unavailable.
* Existing conflicting assignment.
* Resource violates another documented operational constraint.

A resource failing mandatory eligibility must not reach the valid assignment result.

---

# 12. Ranking Testing

Ranking must be deterministic.

Tests must verify that eligible candidates are ordered according to the documented ranking rules.

Factors include:

* Severity.
* Distance.
* Workload.
* Suitability.
* Other documented operational factors.

The exact scoring formula and weights will be tested according to the final Dispatch Engine implementation design.

---

# 13. No First-Available Testing

A critical requirement is that ResQGrid must not simply select the first available resource.

Tests must create multiple eligible resources where:

* One resource appears first.
* Another resource has better suitability.

The engine must select according to the documented ranking rules rather than collection order.

---

# 14. Tie-Breaking Testing

When candidates have equivalent ranking values, the documented deterministic tie-breaking strategy must be applied.

Tests must verify:

* Equal-score candidates.
* Deterministic selection.
* Repeatable results.

The final tie-breaking sequence must be tested exactly as documented when it is finalized.

---

# 15. Distance Testing

Distance-related behavior must be tested using controlled locations.

Tests should cover:

* Same location.
* Nearby resource.
* Farther resource.
* Multiple candidates with different distances.
* Boundary conditions relevant to the final distance calculation.

The exact distance algorithm will be tested according to the final Dispatch Engine design.

---

# 16. Workload Testing

Workload calculation must be tested against controlled dispatch history.

Tests should cover:

* No previous workload.
* Low workload.
* Higher workload.
* Multiple historical dispatches.
* Equal workload candidates.

The exact workload calculation will follow the finalized Dispatch Engine design.

---

# 17. Final Assignment Verification Testing

Candidate selection does not guarantee final availability.

Tests must verify that the system performs final verification immediately before assignment.

A candidate that becomes unavailable after ranking must not be assigned.

---

# 18. Dispatch Lifecycle Testing

Dispatch lifecycle tests must verify valid transitions such as:

```text id="0ww3o8"
CREATED
   |
   v
IN_PROGRESS
   |
   v
COMPLETED
```

Invalid transitions must be rejected.

---

# 19. Resource State During Dispatch

Tests must verify the relationship between dispatch lifecycle and resource operational state.

For example:

```text id="4rmz31"
Resource AVAILABLE
        |
        v
Dispatch Assignment
        |
        v
Resource BUSY
```

After valid dispatch completion:

```text id="i5q7mp"
Resource BUSY
        |
        v
Dispatch Completed
        |
        v
Resource AVAILABLE
```

The exact transition behavior must follow the business rules.

---

# 20. Concurrency Testing

Concurrency testing is mandatory because preventing double assignment is a core requirement.

The primary scenario is:

```text id="awxkfg"
Incident A ----\
                \
                 > Same Resource
                /
Incident B ----/
```

Both operations attempt to assign the same resource concurrently.

The expected result is:

* At most one assignment succeeds.
* The other operation detects the conflict or follows the documented retry behavior.
* No duplicate conflicting active dispatch exists.
* Resource state remains consistent.
* Database state remains consistent.

---

# 21. Concurrent Test Design

Concurrency tests should use controlled synchronization so that competing operations overlap intentionally.

The test must avoid relying on timing assumptions such as:

```text id="czx7ll"
Thread.sleep(...)
```

as the primary synchronization mechanism.

Where practical, explicit coordination mechanisms should be used to make the race reproducible.

---

# 22. Transaction Testing

Transactional behavior must be tested.

For a dispatch operation involving multiple state changes, tests must verify:

### Successful transaction

All required changes are committed.

### Failed transaction

All required changes are rolled back.

The system must not leave partial state.

---

# 23. Rollback Testing

A test should intentionally cause a failure after one or more changes have begun.

The expected result is that the transaction returns the database to its previous consistent state.

For example, the test must detect and reject situations such as:

```text id="zkm7vy"
Dispatch exists
+
Resource state was not updated
```

when both operations are required to be atomic.

---

# 24. Persistence Testing

Persistence tests verify actual interaction with PostgreSQL.

They should cover:

* Insert.
* Retrieval.
* Update.
* Relationships.
* Constraints.
* Transactions.
* Queries.
* State changes.

Persistence tests should use a controlled test database/schema.

---

# 25. JDBC Testing

Because JDBC is an explicit project learning requirement, JDBC-based data access must be tested independently where applicable.

Tests should verify:

* Correct SQL behavior.
* Parameter binding.
* Result mapping.
* Resource cleanup.
* Transaction behavior.
* Database errors.

SQL must not be assumed correct merely because the Java code compiles.

---

# 26. JPA/Hibernate Testing

JPA/Hibernate persistence must be tested for:

* Entity mapping.
* Relationships.
* Foreign keys.
* Persistence operations.
* Fetching behavior.
* Transaction boundaries.
* Entity lifecycle.
* Dirty checking.
* First-level cache behavior where relevant.
* Query behavior.
* N+1 risks where relevant.

The tests must verify actual persistence behavior rather than relying solely on annotations appearing correct.

---

# 27. Relationship Testing

Tests must verify important relationships such as:

```text id="a7x1iz"
Incident -> Dispatch
Resource -> Dispatch
Team -> Dispatch
Resource -> Capability
Team -> Capability
Incident -> Location
Resource -> Location
```

Tests must verify that relationships behave consistently with the Database Design.

---

# 28. REST API Testing

REST API tests must verify:

* Endpoint availability.
* Request validation.
* Successful responses.
* HTTP status codes.
* JSON structure.
* Not-found behavior.
* Business-rule failures.
* Conflict behavior.
* Error response structure.

---

# 29. API Success Testing

Each implemented endpoint must have successful-path tests.

Examples include:

* Create incident.
* Retrieve incident.
* Register resource.
* Retrieve resource.
* Find suitable candidates.
* Create dispatch.
* Retrieve dispatch.
* Complete dispatch.

Only endpoints actually implemented according to the documentation require tests.

---

# 30. API Error Testing

API tests must verify:

* Invalid request.
* Missing required data.
* Invalid identifier.
* Resource not found.
* Invalid state transition.
* Business-rule violation.
* Dispatch conflict.
* Persistence failure where practical.

The API must return the documented status code and error representation.

---

# 31. Error Handling Testing

Error-handling tests must verify that:

* Known errors are mapped correctly.
* Unexpected errors do not expose internal details.
* Error responses remain consistent.
* Transactions are rolled back when required.
* Failures do not silently become successes.

---

# 32. Validation Testing

Validation tests must cover boundary conditions.

Examples:

* Missing required field.
* Empty value.
* Invalid controlled value.
* Invalid identifier.
* Invalid state.
* Invalid combination of values.

Tests should cover both valid and invalid boundaries.

---

# 33. Reporting Testing

Reporting operations must be tested against controlled data.

Tests should verify:

* Correct incident counts.
* Correct severity distribution.
* Correct dispatch counts.
* Correct resource utilization information.
* Correct historical information.

The exact report calculations will follow the finalized reporting design.

---

# 34. Integration Testing

Integration tests verify that multiple layers work together.

Examples:

```text id="4uxu8x"
Controller
    |
Service
    |
Repository
    |
PostgreSQL
```

Integration tests are particularly important for:

* Transactions.
* Persistence.
* Queries.
* Entity relationships.
* Dispatch assignment.
* Concurrency.

---

# 35. End-to-End Testing

End-to-end validation should verify important business flows through the externally accessible application interface.

A primary flow is:

```text id="1z0nsi"
Report Incident
      |
      v
Assess Incident
      |
      v
Determine Requirements
      |
      v
Find Suitable Resource
      |
      v
Dispatch
      |
      v
Resource Becomes BUSY
      |
      v
Dispatch In Progress
      |
      v
Dispatch Completed
      |
      v
Resource Becomes AVAILABLE
      |
      v
Incident Resolved
```

The exact flow must follow the final business rules.

---

# 36. Test Data Strategy

Tests must use controlled and understandable data.

Test data should clearly represent:

* Different incident severities.
* Different resource types.
* Different resource statuses.
* Different capabilities.
* Different locations.
* Different workloads.
* Different dispatch states.

Tests should avoid unexplained magic values.

---

# 37. Test Isolation

Tests should not depend on another test running first.

Each test must establish the state it requires.

Database tests should clean up or reset their test data according to the selected test-database strategy.

---

# 38. Deterministic Testing

Tests must produce predictable results.

Avoid unnecessary dependencies on:

* Current time.
* Random values.
* Thread scheduling.
* Database record ordering.
* Network availability.

Where such factors are part of the behavior being tested, they must be controlled deliberately.

---

# 39. Time-Based Testing

Dispatch and incident timestamps are important.

Tests involving time should avoid relying on arbitrary real-world timing where possible.

Where appropriate, time-related behavior should use controlled test conditions.

The exact time abstraction strategy will be determined during implementation design.

---

# 40. Lambda and Functional Logic Testing

Where lambda expressions, streams, or functional-style logic are used in the implementation, tests must focus on the resulting behavior.

The project should avoid using complex lambda expressions merely to demonstrate syntax.

Readability and correctness take priority.

---

# 41. Test Naming

Test names must clearly communicate the behavior being verified.

A test should make it understandable:

* What scenario is being tested.
* What condition is involved.
* What result is expected.

The exact naming convention will be applied consistently during implementation.

---

# 42. Test Organization

Tests should be organized according to the project structure and responsibility of the component being tested.

The project will use the standard Maven test structure:

```text id="1z1bkj"
src/
└── test/
    └── java/
```

Additional test resources may be placed under:

```text id="x1u8n0"
src/
└── test/
    └── resources/
```

The exact package structure will follow the final application package structure.

---

# 43. Test Dependencies

JUnit 5 is required.

Additional testing libraries may be introduced when they provide clear value for:

* REST testing.
* Database integration testing.
* Concurrency testing.
* Mocking.
* Test utilities.

No dependency should be added solely for convenience without understanding its purpose.

---

# 44. Mocking Strategy

Mocks may be used where isolation is valuable.

However, mocking must not replace tests that require real behavior.

For example:

* Business logic can be unit tested independently.
* Actual PostgreSQL behavior requires database integration tests.
* ORM mapping requires actual persistence testing.
* Concurrency guarantees require real concurrent execution against the relevant persistence mechanism.

---

# 45. Test Coverage

Coverage metrics may be used as supporting information.

High code coverage alone does not prove correctness.

Priority must be given to:

* Critical business rules.
* Dispatch logic.
* State transitions.
* Concurrency guarantees.
* Transaction boundaries.
* Error handling.
* Persistence correctness.

---

# 46. Regression Testing

When a defect is discovered:

1. Reproduce the defect.
2. Add a test that demonstrates the defect.
3. Fix the implementation.
4. Confirm the new test passes.
5. Run the relevant regression test suite.

A fixed defect should remain protected by automated testing where practical.

---

# 47. Feature Completion Criteria

A feature is considered test-complete only when:

* Required unit tests pass.
* Required integration tests pass.
* Required API tests pass.
* Relevant failure cases are tested.
* Relevant business rules are tested.
* No known regression exists.
* Documentation and implementation remain consistent.

---

# 48. Pre-Commit Validation

Before committing a completed feature, the developer should:

1. Run the relevant tests.
2. Run the complete test suite when appropriate.
3. Confirm no unexpected failures.
4. Review changed behavior.
5. Confirm documentation consistency.
6. Commit only when the project is in a known-good state.

---

# 49. Testing and Documentation Contract

Every implemented requirement must be traceable to testing.

Conceptually:

```text id="y4c3n8"
Requirement
    |
    v
Business Rule
    |
    v
Implementation
    |
    v
Test
```

If a requirement changes, the affected tests must be reviewed.

If a test reveals that the documented behavior is incorrect or incomplete, implementation must stop and the relevant documentation must be reviewed first.

---

# 50. Deferred Testing Decisions

This document intentionally does not finalize:

* Exact test library versions beyond required JUnit 5.
* Exact mocking framework.
* Exact PostgreSQL test-database strategy.
* Exact test container strategy.
* Exact REST testing framework.
* Exact concurrency-test utilities.
* Exact test naming convention.
* Exact test-data reset strategy.
* Exact code-coverage target.

These decisions must be made before the corresponding implementation is introduced.

---

# 51. No Untested Critical Business Logic

The following areas are considered critical and must receive explicit automated tests:

* Incident lifecycle.
* Resource lifecycle.
* Resource eligibility.
* Capability matching.
* Dispatch ranking.
* Final assignment verification.
* Dispatch lifecycle.
* Resource state changes.
* Transactional assignment.
* Concurrency protection.
* Error handling.

---

# 52. Documentation Consistency

This document must remain consistent with:

* `01-PROJECT-OVERVIEW.md`
* `02-REQUIREMENTS-SPECIFICATION.md`
* `03-ACTORS-AND-USE-CASES.md`
* `04-BUSINESS-RULES.md`
* `05-DOMAIN-MODEL.md`
* `06-SYSTEM-ARCHITECTURE.md`
* `07-DATABASE-DESIGN.md`
* `08-DISPATCH-ENGINE-DESIGN.md`
* `09-CONCURRENCY-DESIGN.md`
* `10-API-DESIGN.md`
* `11-ERROR-HANDLING-DESIGN.md`
* `13-DEVELOPMENT-ROADMAP.md`

If a conflict is discovered, implementation must stop.

The relevant documentation must be reviewed and updated before implementation continues.

The documentation remains the source of truth for ResQGrid.
