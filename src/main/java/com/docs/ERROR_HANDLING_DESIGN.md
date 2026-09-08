# ResQGrid — Testing Strategy

**Document:** Testing Strategy
**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Development Baseline

---

# 1. Purpose

Testing in ResQGrid is intended to verify both technical correctness and emergency-response business behavior.

The most important areas are:

* business rules
* incident lifecycle
* resource availability
* dispatch selection
* state transitions
* persistence
* transaction behavior
* concurrency
* API behavior
* error handling

Testing should give confidence that a change does not silently break an existing rule.

---

# 2. Testing Approach

The project will use several levels of testing:

```text
Unit Tests
    |
    v
Service / Integration Tests
    |
    v
Persistence Tests
    |
    v
API Tests
    |
    v
Concurrency Tests
```

Not every feature requires every level.

The appropriate test level depends on what is being verified.

---

# 3. Main Testing Tool

The automated test framework will be **JUnit 5**.

Maven will execute the automated test suite as part of the normal build lifecycle.

Manual API testing will also be performed using Postman once the REST layer is implemented.

---

# 4. What Should Be Unit Tested

Unit tests should focus on logic that can be tested without requiring a real PostgreSQL database.

Important candidates include:

* validation
* incident state transitions
* resource state transitions
* capability matching
* candidate filtering
* dispatch scoring
* resource ranking
* priority ordering
* exception behavior
* utility logic

The Dispatch Engine is a major unit-testing target.

---

# 5. Dispatch Engine Tests

The Dispatch Engine should be tested independently from the database.

Examples:

### Suitable resource

Given:

```text
Incident: CRITICAL medical emergency

Resource A:
AVAILABLE
AMBULANCE
required medical capability
near incident

Resource B:
BUSY
AMBULANCE
required capability
near incident
```

The engine should select Resource A.

---

### Capability mismatch

If a resource does not have the required capability, it should not become a valid candidate even if it is closer.

---

### Resource unavailable

A `BUSY`, `OFFLINE`, or `MAINTENANCE` resource should not be selected when the dispatch rules require an available resource.

---

### Ranking

If multiple resources are eligible, the engine should produce a deterministic ranking according to the defined dispatch criteria.

---

# 6. Incident Tests

Incident tests should verify:

* required data validation
* severity handling
* initial status
* valid status transitions
* invalid status transitions
* terminal-state behavior
* location requirements
* incident history behavior where applicable

Example scenarios:

```text
REPORTED -> TRIAGED
TRIAGED -> ASSIGNED
ASSIGNED -> IN_PROGRESS
IN_PROGRESS -> RESOLVED
```

Invalid transitions should be rejected.

The exact transition matrix remains governed by the business rules.

---

# 7. Resource Tests

Resource tests should verify:

* valid registration
* required fields
* resource type
* capabilities
* initial status
* status transitions
* active assignment restrictions
* availability rules

Important invariant:

```text
A resource involved in active work must not incorrectly appear AVAILABLE.
```

---

# 8. Dispatch Tests

Dispatch tests should cover:

* successful assignment
* no suitable resource
* invalid incident state
* unavailable resource
* capability mismatch
* duplicate dispatch attempt
* dispatch status transitions
* cancellation
* completion
* history creation

The tests should verify both the successful path and failure paths.

---

# 9. Exception Tests

Known business exceptions should be explicitly tested.

Examples:

```text
IncidentNotFoundException
ResourceNotFoundException
InvalidStateTransitionException
ResourceUnavailableException
NoSuitableResourceException
DispatchConflictException
```

The actual exception names will be finalized during implementation.

Tests should verify that the expected exception is thrown for the appropriate condition.

---

# 10. Repository and Database Tests

Once PostgreSQL is introduced, persistence behavior must also be tested.

These tests should verify:

* insert operations
* retrieval
* updates
* relationships
* foreign keys
* constraints
* transaction behavior
* status persistence
* dispatch persistence
* history persistence

These tests may use a dedicated test database.

---

# 11. JDBC Testing

During the JDBC phase, repository tests should verify the SQL implementation directly.

Examples:

```text
save incident
find incident
find available resources
save dispatch
update resource status
retrieve dispatch history
```

Tests should confirm that the application and database remain consistent.

SQL failures should not be hidden by tests that only check application objects.

---

# 12. Hibernate/JPA Testing

After moving persistence to Hibernate/JPA, tests should verify:

* entity mappings
* relationships
* persistence
* queries
* transaction boundaries
* lazy/eager behavior where relevant
* cascading behavior
* orphan removal where intentionally configured
* dirty checking
* locking behavior

JPA tests should not simply assume that correct Java objects mean correct database behavior.

---

# 13. Transaction Tests

Transactions are critical during dispatch.

A dispatch operation may modify several pieces of state:

```text
Incident
Resource
Dispatch
History
```

A failure during the operation should not leave the database partially updated.

A transaction test should verify behavior such as:

```text
Begin transaction
    |
    +--> create dispatch
    +--> update resource
    +--> update incident
    +--> create history
    |
    X failure
    |
Rollback
```

After rollback, the database should not contain a partial dispatch.

---

# 14. Concurrency Testing

Concurrency is one of the defining technical challenges of ResQGrid.

A key test scenario is:

```text
Incident A ----\
                >---- same resource
Incident B ----/
```

Both dispatch operations attempt to assign the same resource concurrently.

The expected result is:

```text
One dispatch succeeds
        +
One dispatch is rejected/retried safely
        =
No double assignment
```

The test must verify the invariant rather than merely checking that threads complete.

---

# 15. Java Concurrency Tools

Concurrency tests may use:

* `ExecutorService`
* `CountDownLatch`
* `CyclicBarrier`
* `AtomicInteger`
* concurrent collections where appropriate

These tools should be introduced when the corresponding concurrency implementation is developed.

They should not be added to tests without a clear reason.

---

# 16. Reproducing Race Conditions

A concurrency test should make it likely that competing operations overlap.

For example:

```text
Thread A -> check resource
Thread B -> check resource
Thread A -> attempt assignment
Thread B -> attempt assignment
```

A simple test that launches two threads without coordinating them does not reliably reproduce a race condition.

Synchronization tools can be used to create the required timing.

---

# 17. Concurrency Test Assertions

The important assertions include:

* only one active dispatch exists for the resource
* resource state is correct
* incident state is correct
* no duplicate assignment exists
* database constraints remain satisfied
* failed operations return controlled errors
* successful operations are committed correctly

The test should inspect the final system state, not just thread exceptions.

---

# 18. API Tests

REST endpoints should eventually be tested for:

* valid requests
* malformed requests
* missing fields
* invalid IDs
* invalid state transitions
* successful creation
* successful updates
* dispatch success
* dispatch conflict
* no suitable resource
* correct HTTP status codes
* correct response structure

Postman can be used during development for manual verification.

Automated API/integration tests should be added where practical.

---

# 19. MVC/Web Testing

The Servlet/JSP layer should verify:

* request routing
* form handling
* validation feedback
* service invocation
* successful redirects/forwards
* error handling
* correct data passed to JSP

Business rules should not be duplicated in JSP tests because those rules belong below the presentation layer.

---

# 20. Test Data

Tests should use controlled test data.

Examples:

```text
CRITICAL incident
HIGH incident
available ambulance
busy ambulance
offline ambulance
maintenance resource
resource with required capability
resource without required capability
multiple eligible resources
```

Test data should represent realistic combinations rather than only ideal cases.

---

# 21. Test Naming

Test names should describe behavior.

Prefer:

```text
shouldRejectDispatchWhenResourceIsUnavailable()
```

over:

```text
testDispatch1()
```

A test name should make its purpose understandable without opening the test body.

---

# 22. Arrange — Act — Assert

Tests should generally follow:

```text
Arrange
    |
    v
Act
    |
    v
Assert
```

Example conceptually:

```text
Arrange:
Create incident and unavailable resource.

Act:
Attempt dispatch.

Assert:
Dispatch is rejected.
```

This keeps tests readable and focused.

---

# 23. Test Independence

Tests should not depend on the execution order of other tests.

Each test should establish the state it requires.

Avoid situations where:

```text
testA()
creates data

testB()
assumes testA() already ran
```

The test suite should remain reliable regardless of execution order.

---

# 24. Testing Invalid Scenarios

The test suite should not focus only on successful operations.

For ResQGrid, failure behavior is equally important.

Examples:

* missing incident
* missing resource
* invalid severity
* invalid status
* invalid capability
* resource already busy
* no suitable resource
* concurrent assignment conflict
* database constraint failure
* transaction failure
* malformed API request

---

# 25. Regression Testing

Whenever a bug is discovered, the preferred process is:

```text
Bug discovered
      |
      v
Create failing test
      |
      v
Fix implementation
      |
      v
Test passes
      |
      v
Keep test permanently
```

This prevents the same bug from silently returning later.

---

# 26. Test Coverage

Code coverage can be useful, but a high percentage alone does not mean the system is well tested.

For example, this test:

```text
execute every line
```

may still fail to verify the actual dispatch rules.

Priority should be given to meaningful scenarios and business invariants.

---

# 27. Important Invariants to Test

The following invariants should have explicit tests:

### Resource assignment

```text
A resource cannot have two conflicting active dispatches.
```

### Availability

```text
A resource assigned to active work cannot incorrectly remain AVAILABLE.
```

### Incident state

```text
A terminal incident cannot receive a new dispatch.
```

### Capability

```text
A resource without required capability cannot be selected.
```

### Transaction consistency

```text
A failed dispatch operation cannot leave partial state.
```

### History

```text
Important state changes create the required history records.
```

---

# 28. Testing Layers

The project should eventually have a structure similar to:

```text
src/
├── main/
│   └── java/
│
└── test/
    └── java/
```

Tests should generally mirror the main package structure where useful.

For example:

```text
com.resqgrid.dispatch
com.resqgrid.service
com.resqgrid.repository
```

can have corresponding test packages.

---

# 29. What Should Not Be Mocked Automatically

Mocking should be used deliberately.

Not every dependency needs to be mocked simply because a unit test can do so.

For example, repository interfaces may be mocked when testing service business logic in isolation.

But database behavior should be tested with an actual test database when database behavior itself is what needs verification.

---

# 30. Testing the Real Dispatch Workflow

At least one integration-level test should eventually exercise the complete dispatch workflow:

```text
Create incident
      |
      v
Find eligible resources
      |
      v
Rank candidates
      |
      v
Assign resource
      |
      v
Create dispatch
      |
      v
Update states
      |
      v
Create history
      |
      v
Verify database
```

This verifies that the individual pieces work together.

---

# 31. Test Execution

Tests should be executed regularly during development.

At minimum:

```text
mvn test
```

should pass before a feature is considered complete.

A clean build should also be periodically verified:

```text
mvn clean test
```

---

# 32. Definition of Done for a Feature

A feature should not be considered complete merely because the implementation compiles.

For a significant feature, the workflow is:

```text
Implementation
    |
    v
Unit tests
    |
    v
Integration tests where required
    |
    v
Failure scenarios
    |
    v
Concurrency tests where relevant
    |
    v
Manual API/web verification where relevant
    |
    v
Code review
    |
    v
Git commit
```

---

# 33. Testing Philosophy

Testing in ResQGrid should answer two questions:

### Does the code work?

This is basic correctness.

### Does the system protect its business rules under failure and concurrency?

This is more important for the dispatch engine.

A dispatch system that works correctly only when one request arrives at a time is not considered correct.

---

# 34. Initial Testing Priorities

The testing effort should initially focus on:

1. Domain validation
2. State transitions
3. Dispatch candidate selection
4. Resource ranking
5. Exception handling
6. Service workflows
7. JDBC persistence
8. Transaction behavior
9. JPA/Hibernate mappings
10. REST API behavior
11. Concurrency
12. End-to-end dispatch workflow

---

# 35. Document Status

**Document 12 — Testing Strategy**

Status: Completed as the initial testing baseline.

This document will guide test creation throughout the project rather than being treated as a final testing checklist.
