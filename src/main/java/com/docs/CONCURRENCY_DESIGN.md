# ResQGrid — Concurrency & Thread Safety Design

**Document:** Concurrency & Thread Safety Design
**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Development Baseline

---

# 1. Purpose

ResQGrid is expected to process multiple emergency operations at the same time.

Two incidents may be created simultaneously, multiple dispatch requests may be processed concurrently, and several users may attempt to update operational data at nearly the same time.

The most important concurrency problem is preventing the same emergency resource from being assigned to multiple active incidents.

For example:

```text
Incident A
    |
    +----> requests Ambulance 12
                         ^
                         |
Incident B -------------+
```

If both requests see Ambulance 12 as `AVAILABLE`, both must not successfully assign it.

Concurrency protection therefore applies to both:

* Java application code
* PostgreSQL database operations

Java synchronization alone is not sufficient once multiple application threads and database transactions are involved.

---

# 2. Main Concurrency Problem

The primary race condition is a check-then-act problem.

A dangerous sequence is:

```text
Thread A:
    check resource -> AVAILABLE

Thread B:
    check resource -> AVAILABLE

Thread A:
    assign resource

Thread B:
    assign same resource
```

Both threads made a valid decision based on the state they observed.

The problem occurs because the check and the assignment were not treated as one atomic operation.

The system must close this gap.

---

# 3. Concurrency Goals

The concurrency design should guarantee:

1. A resource cannot have two conflicting active assignments.
2. Resource state remains consistent with active dispatches.
3. Multi-step dispatch operations are atomic.
4. Failed dispatch operations do not leave partial state changes.
5. Concurrent requests do not silently overwrite important state.
6. Database integrity remains protected even when Java threads execute concurrently.
7. Synchronization is applied only where necessary.
8. The design remains testable.

---

# 4. Where Concurrency Exists

Concurrency can occur at several levels.

## Application level

Multiple Java threads may execute services simultaneously.

Example:

```text
Thread 1 -> DispatchService
Thread 2 -> DispatchService
Thread 3 -> IncidentService
```

## Database level

Multiple transactions may operate on the same PostgreSQL rows.

Example:

```text
Transaction A -> resource 12
Transaction B -> resource 12
```

## User level

Different users may submit operations against the same incident or resource.

The design therefore cannot assume that one operation completes before another begins.

---

# 5. Shared Mutable State

Shared mutable state is dangerous when accessed by multiple threads without proper coordination.

Examples in ResQGrid could include:

* in-memory resource collections
* in-memory incident collections
* current resource status
* active dispatch tracking
* counters
* caches

The system should minimize shared mutable state where practical.

Persistent operational state should ultimately be managed through PostgreSQL rather than relying on a Java collection as the authoritative source.

---

# 6. In-Memory Phase

During the initial in-memory implementation, resources and incidents may temporarily be stored in Java collections.

For example:

```text id="jv4c8x"
Map<Long, EmergencyResource>
```

If multiple threads modify such shared collections, thread safety becomes necessary.

The implementation must not assume that ordinary collections automatically become thread-safe.

For example:

```text id="6imx3v"
HashMap
ArrayList
HashSet
```

are not general-purpose thread-safe collections.

---

# 7. Java Thread Safety

The first concurrency exercises will demonstrate how simultaneous Java threads can interfere with shared state.

The project should make the race condition observable before adding synchronization.

The learning sequence is:

```text id="7djxar"
Shared state
    |
    v
Race condition
    |
    v
Identify critical section
    |
    v
Protect critical section
    |
    v
Test concurrent behavior
```

This makes synchronization a solution to a demonstrated problem rather than an arbitrary requirement.

---

# 8. Critical Section

A critical section is the part of an operation where shared state must not be changed concurrently in a conflicting way.

For resource assignment, the critical business operation is conceptually:

```text id="tq0q3g"
check resource availability
        +
reserve/assign resource
        +
update resource state
```

These operations must be coordinated so another request cannot successfully perform a conflicting assignment in the middle.

---

# 9. Synchronization in the In-Memory Phase

In the in-memory version, Java synchronization may be used to protect resource assignment.

Possible mechanisms include:

* `synchronized`
* synchronized blocks
* `Lock`
* `ReentrantLock`

The choice should be based on the actual design.

Synchronization should protect the smallest meaningful critical section rather than locking the entire dispatch system.

---

# 10. Avoid Global Locking

A design such as:

```text id="8nq6ta"
synchronized dispatchEverything()
```

would unnecessarily serialize all dispatch operations.

For example:

```text
Incident A -> Ambulance A
Incident B -> Ambulance B
Incident C -> Fire Unit A
```

These operations may not conflict with each other.

The system should avoid making every dispatch wait for every other dispatch.

The concurrency design should allow independent operations to proceed concurrently when they do not conflict.

---

# 11. Resource-Level Conflict

The actual conflict is normally associated with a particular resource.

For example:

```text id="6w0mhi"
Incident A -> Ambulance A
Incident B -> Ambulance A
```

is a conflict.

But:

```text id="k2zvpo"
Incident A -> Ambulance A
Incident B -> Ambulance B
```

does not represent the same resource conflict.

The design should therefore avoid unnecessarily locking unrelated resources.

---

# 12. Database as the Final Authority

Once PostgreSQL becomes the persistent store, Java memory cannot be treated as the final authority for resource availability.

The authoritative state is stored in PostgreSQL.

This matters because:

```text
Java Thread A
        |
        v
PostgreSQL

Java Thread B
        |
        v
PostgreSQL
```

Both requests ultimately interact with the same database state.

Application-level synchronization does not protect against every possible concurrent database operation.

---

# 13. Transaction Boundary

A dispatch operation should be treated as a transaction.

Conceptually:

```text id="qv3f2b"
BEGIN

load/verify incident
load/verify resource
verify resource can be assigned
create dispatch
update resource status
update incident status
create history

COMMIT
```

If any critical operation fails:

```text id="kq7x7g"
ROLLBACK
```

The system should not leave behind a partially completed dispatch.

---

# 14. Why Transactions Matter

Consider this sequence:

```text id="t4x9d1"
1. Dispatch created
2. Resource updated
3. Incident update fails
```

Without a transaction, the database may contain:

```text
dispatch = created
resource = BUSY
incident = still REPORTED
```

This is an inconsistent operational state.

With a transaction:

```text id="4bbykz"
BEGIN
    operation 1
    operation 2
    operation 3 -> failure
ROLLBACK
```

the changes can be undone as one unit.

---

# 15. Row-Level Locking

PostgreSQL supports row-level locking.

For resource assignment, the system can lock the relevant resource row while the transaction determines whether it can safely be assigned.

Conceptually:

```text id="nq0j5w"
Transaction A
    |
    +--> lock Resource 12
    |
    +--> verify AVAILABLE
    |
    +--> assign Resource 12
    |
    +--> commit
```

A concurrent transaction attempting to modify the same resource must respect the database lock.

This is significantly safer than relying only on a Java `synchronized` block.

---

# 16. Pessimistic Locking

Pessimistic locking assumes that conflicting access may occur and protects the row before making the change.

The conceptual approach is:

```text id="6eq1x5"
lock resource
    |
    v
read current state
    |
    v
validate
    |
    v
modify
    |
    v
commit
```

This can be useful for dispatch assignment because assigning the same resource concurrently is a serious conflict.

The exact SQL locking statement will be introduced during the JDBC implementation.

---

# 17. Optimistic Locking

Optimistic locking takes a different approach.

The system allows concurrent reads and detects whether the data changed before the update is committed.

A version field can be used conceptually:

```text id="4z3p5b"
resource version = 10
```

A transaction attempts to update only if the version is still `10`.

If another transaction already changed it:

```text id="d0g9kl"
expected version = 10
actual version = 11
```

the update fails.

Optimistic locking may become useful later, especially when JPA/Hibernate is introduced.

---

# 18. Pessimistic vs Optimistic

For the core resource-assignment operation, the initial design favors database-level protection appropriate for a high-conflict assignment operation.

| Approach             | Advantage                                        | Concern                              |
| -------------------- | ------------------------------------------------ | ------------------------------------ |
| Pessimistic locking  | Directly protects the resource during assignment | Can increase waiting                 |
| Optimistic locking   | Less blocking during reads                       | Requires conflict detection/retry    |
| Java synchronization | Simple for in-memory state                       | Does not protect database-wide state |
| Database constraint  | Strong final integrity guarantee                 | Does not replace transaction design  |

The implementation may use more than one mechanism where they solve different problems.

---

# 19. Database Constraint as Final Protection

The database should provide a final integrity barrier against duplicate active assignments.

The design from the Database Design document proposes a PostgreSQL partial unique index covering active dispatches.

Conceptually:

```text id="2c7oqc"
resource_id
    +
active dispatch
    =
unique
```

This means even if application logic contains a concurrency bug, PostgreSQL can reject a conflicting active assignment.

This is an important defense-in-depth strategy.

---

# 20. Application Logic and Database Integrity

The application should not rely exclusively on the database constraint.

The preferred approach is:

```text id="m7y3zt"
Application validation
        +
Transaction control
        +
Database locking where required
        +
Database constraints
```

Each layer protects against different classes of failure.

---

# 21. Conditional Updates

Another useful technique is an atomic conditional update.

Conceptually:

```text id="49y5ps"
UPDATE resources
SET status = 'BUSY'
WHERE id = ?
  AND status = 'AVAILABLE';
```

The important result is the number of affected rows.

If:

```text id="twg4n9"
affected rows = 1
```

the resource was successfully transitioned.

If:

```text id="avp2e7"
affected rows = 0
```

the resource was no longer available or did not exist.

This can make the state transition itself atomic.

The exact combination of conditional updates, locking, and dispatch insertion will be decided during JDBC implementation.

---

# 22. Candidate Selection and Race Conditions

Candidate selection happens before assignment.

For example:

```text id="4z9e4m"
10:00:00
Dispatch Engine selects Ambulance A

10:00:01
Another request assigns Ambulance A

10:00:02
Original request attempts assignment
```

The original candidate is now stale.

The system must not blindly trust the earlier selection.

Assignment must revalidate current state.

---

# 23. Safe Assignment Flow

The safe flow is:

```text id="5xxq4x"
1. Find suitable candidates
        |
        v
2. Select best candidate
        |
        v
3. Start assignment transaction
        |
        v
4. Re-check current resource state
        |
        v
5. Safely reserve/assign resource
        |
        v
6. Create dispatch
        |
        v
7. Update related state
        |
        v
8. Commit
```

If step 4 or 5 fails because another request won the resource:

```text id="v4m3db"
do not create a conflicting dispatch
```

The service can then select another candidate if the retry policy permits.

---

# 24. Retry Strategy

A concurrency conflict can be treated as a recoverable business/operational condition.

For example:

```text id="0j7dkn"
Candidate A selected
    |
    v
Assignment conflict
    |
    v
Candidate A rejected
    |
    v
Try Candidate B
```

Retries must be bounded.

The system should not retry indefinitely.

The initial implementation should use a small, explicit retry limit.

---

# 25. Why Infinite Retries Are Dangerous

If every conflict causes another immediate retry:

```text id="q0l2kp"
retry
retry
retry
retry
retry
...
```

the system can waste CPU and database resources.

It can also make an already overloaded system worse.

Retry behavior should therefore be:

* limited
* observable
* predictable
* tested

---

# 26. Deadlocks

A deadlock can occur when transactions hold locks that the other transaction needs.

Example:

```text id="5o7y1r"
Transaction A:
    locks Resource 1
    waits for Resource 2

Transaction B:
    locks Resource 2
    waits for Resource 1
```

Neither can continue.

The application should reduce deadlock risk by keeping transactions short and acquiring multiple locks in a consistent order.

---

# 27. Keep Transactions Short

A dispatch transaction should not contain unnecessary work.

Avoid doing things such as:

```text id="9j6j5z"
HTTP calls
long calculations
user interaction
slow external operations
large report generation
```

while holding database locks.

The transaction should focus on the state changes that need atomicity.

---

# 28. Lock Ordering

If a future operation needs to lock multiple resources, the system should use a consistent ordering.

For example:

```text id="6m6s0m"
lock resource with smaller ID first
lock resource with larger ID second
```

This reduces the chance that two transactions acquire the same locks in opposite orders.

The exact rule will be finalized if multi-resource dispatch requires multiple simultaneous resource locks.

---

# 29. Incident-Level Concurrency

Resources are not the only entities that can experience concurrent updates.

Two requests may attempt to update the same incident.

Examples:

```text id="m0h8f3"
Dispatcher -> ASSIGNED
Operator   -> CANCELLED
```

The system must define valid state transitions and prevent invalid overwrites.

The application should verify the current state before applying important transitions.

---

# 30. Resource Status Consistency

A resource's status should remain consistent with its active work.

Important invariant:

```text id="9v7t6p"
Active dispatch
    =>
resource cannot remain AVAILABLE
```

Similarly:

```text id="y9ec4w"
No active dispatch
    =>
resource may become AVAILABLE
```

subject to other operational conditions such as maintenance or offline status.

---

# 31. Incident and Dispatch Consistency

An incident should not become `ASSIGNED` if no dispatch was successfully created.

Likewise, a successful active dispatch should result in the appropriate incident/resource state changes.

These operations belong inside the same transaction where they represent one logical business operation.

---

# 32. History and Transactions

History records are part of the operational state transition.

For example:

```text id="u0f3s7"
Resource assigned
    |
    +--> dispatch created
    +--> resource status changed
    +--> incident status changed
    +--> history recorded
```

If the dispatch transaction rolls back, the corresponding history record should not incorrectly claim that the assignment succeeded.

Therefore, the history insert should normally participate in the same transaction.

---

# 33. Thread Safety of Services

Service classes should avoid storing request-specific mutable state in instance fields.

Unsafe conceptual design:

```text id="z0fs7r"
class DispatchService {
    private Incident currentIncident;
    private EmergencyResource selectedResource;
}
```

If the same service instance is shared between requests, different threads could overwrite these fields.

Instead, request-specific state should normally remain local to the method invocation.

Conceptually:

```text id="5u0c6f"
dispatch(incident)
    |
    +--> local variables
    +--> local candidate data
    +--> transaction
```

---

# 34. Stateless Services

The preferred service design is therefore mostly stateless.

For example:

```text id="w0xk0d"
DispatchService
    |
    +--> methods
    |
    +--> dependencies
```

rather than:

```text id="vyn7jf"
DispatchService
    |
    +--> currentIncident
    +--> currentResource
    +--> currentCandidates
```

Stateless services are easier to reason about under concurrent requests.

---

# 35. Thread-Safe Collections

Where shared in-memory state is genuinely required, appropriate concurrent collections may be considered.

Examples include:

```text id="7vq4rq"
ConcurrentHashMap
CopyOnWriteArrayList
BlockingQueue
```

Each has different characteristics.

They should not be treated as universal replacements for ordinary collections.

For example, `ConcurrentHashMap` provides thread-safe map operations, but a sequence of multiple operations may still require additional coordination.

---

# 36. Atomicity vs Thread Safety

Thread-safe collection methods do not automatically make an entire business operation atomic.

For example:

```text id="2q4y8x"
if resource is available
    assign resource
```

Even if the underlying map is thread-safe, the overall sequence can still race.

Thread safety must therefore be considered at the business-operation level.

---

# 37. `volatile`

`volatile` may be useful for certain shared state where visibility is the main concern.

It does not automatically make compound operations atomic.

For example:

```text id="8fbrqk"
counter++
```

is not made safely atomic merely because `counter` is volatile.

The project should use `volatile` only where its memory-visibility semantics are actually appropriate.

---

# 38. Atomic Classes

Atomic classes may be useful for simple shared counters or state transitions.

Examples include:

```text id="2l7m6y"
AtomicInteger
AtomicLong
AtomicBoolean
```

They are not a replacement for database transactions or resource-level locking.

Their use should be limited to problems they actually solve.

---

# 39. Synchronization Boundary

The system should synchronize the smallest operation necessary.

The preferred conceptual boundary is:

```text id="q1g6we"
candidate selection
    -> normally not globally synchronized

resource assignment
    -> protected

database state transition
    -> transaction + database protection
```

This preserves concurrency for independent work.

---

# 40. Concurrency Testing

Concurrency bugs are often difficult to reproduce with ordinary sequential tests.

ResQGrid should include tests that deliberately create contention.

A test can conceptually:

```text id="j9zv3f"
Create one available ambulance

Thread A -> dispatch Incident A
Thread B -> dispatch Incident B

Run both concurrently

Verify:
    only one assignment succeeds
    one incident receives the ambulance
    no duplicate active dispatch exists
    resource state is consistent
```

---

# 41. Synchronizing Test Threads

Java concurrency utilities can help coordinate test execution.

Potential tools include:

* `CountDownLatch`
* `CyclicBarrier`
* `ExecutorService`

For example, a test can make multiple threads wait until all participants are ready before starting the assignment attempt.

This increases the chance of exposing the race condition.

---

# 42. Concurrency Test Assertions

A successful concurrency test should verify more than "no exception occurred."

Important assertions include:

```text id="7x8i2s"
Exactly one active dispatch for the resource
Exactly one assignment succeeded
Resource status is correct
Incident statuses are correct
No duplicate active dispatch records exist
History is consistent
Database transaction did not leave partial state
```

---

# 43. Database Concurrency Test

Once PostgreSQL is introduced, concurrency tests should operate against a real test database where possible.

The test should simulate:

```text id="r8n9n0"
Transaction A
Transaction B
       |
       v
same resource
```

This is important because in-memory synchronization does not reproduce PostgreSQL locking behavior.

---

# 44. Isolation Level

PostgreSQL transaction isolation affects what concurrent transactions can observe.

The initial system should use PostgreSQL's normal transactional behavior unless a specific business requirement requires a different isolation level.

Isolation levels should not be changed casually.

If a higher isolation level is considered later, it must be justified with:

* the consistency problem it solves
* the performance cost
* expected contention
* test results

---

# 45. Hibernate/JPA Considerations

When JPA/Hibernate is introduced, concurrency protection will need to be mapped appropriately.

Potential mechanisms include:

* `@Version` for optimistic locking
* pessimistic lock modes
* transactional service methods
* database constraints

Hibernate does not eliminate concurrency problems.

The underlying PostgreSQL rules still matter.

---

# 46. JDBC vs JPA Concurrency

During JDBC development, concurrency behavior should be understood directly through SQL.

The developer should understand:

```text id="mby0fk"
BEGIN
SELECT ... FOR UPDATE
UPDATE ...
INSERT ...
COMMIT
```

before relying on JPA abstractions.

Later, Hibernate can provide higher-level mechanisms while PostgreSQL continues to enforce the final database behavior.

---

# 47. Failure Scenarios

The concurrency design must handle failures such as:

### Resource already assigned

```text id="sm8m0g"
Assignment rejected
```

### Transaction rollback

```text id="9fglh3"
No partial state should remain
```

### Database constraint violation

```text id="9om9oc"
Conflicting assignment rejected
```

### Concurrent incident update

```text id="3n4c0h"
Current state revalidated
```

### Deadlock or transient database failure

```text id="0q3f5x"
Transaction fails
Retry only where appropriate
```

---

# 48. Concurrency and REST Requests

When REST APIs are introduced, each incoming request may be handled concurrently by the web container.

For example:

```text id="s4s5wp"
HTTP Request A
        |
        v
DispatchService

HTTP Request B
        |
        v
DispatchService
```

The application must therefore be designed under the assumption that service methods can execute concurrently.

Servlet container threading behavior must not be treated as sequential request processing.

---

# 49. Concurrency and JSP/MVC

The same principle applies to the JSP/Servlet layer.

Servlet instances may handle multiple requests concurrently.

Servlets should therefore avoid storing request-specific mutable data in instance fields.

Request data should remain within the request scope or method-local variables.

---

# 50. Defense in Depth

ResQGrid's concurrency protection should follow multiple layers:

```text id="x1e0oh"
HTTP / Controller
       |
       v
Stateless Service
       |
       v
Dispatch Engine
       |
       v
Transaction
       |
       v
Database locking / conditional update
       |
       v
Database constraints
```

No single mechanism should be expected to solve every concurrency problem.

---

# 51. What Java Protects

Java-level concurrency mechanisms are primarily responsible for:

* protecting shared in-memory state
* coordinating Java threads
* controlling critical sections
* preventing unsafe access to shared objects

They are not the final authority over persistent database state.

---

# 52. What PostgreSQL Protects

PostgreSQL is responsible for:

* transaction atomicity
* row-level locking
* isolation
* persistent state integrity
* foreign-key enforcement
* unique constraints
* database-level consistency

The database therefore provides the final protection for persistent operational state.

---

# 53. Initial Strategy

The initial ResQGrid strategy is:

```text id="6a8j3c"
In-memory phase
    -> demonstrate race conditions
    -> use appropriate Java synchronization

JDBC phase
    -> PostgreSQL transactions
    -> conditional updates / row locking
    -> database constraints

JPA/Hibernate phase
    -> preserve database guarantees
    -> introduce appropriate JPA locking/versioning
```

The implementation should evolve without changing the fundamental business rule:

> One resource must not have conflicting active assignments.

---

# 54. Concurrency Principles

ResQGrid should follow these principles:

1. Treat resource assignment as a concurrency-sensitive operation.
2. Do not rely on check-then-act logic without protection.
3. Keep service classes stateless where possible.
4. Avoid global locks.
5. Protect only the required critical section.
6. Use PostgreSQL transactions for multi-step persistent operations.
7. Revalidate resource state during assignment.
8. Use database constraints as a final integrity barrier.
9. Keep transactions short.
10. Avoid unnecessary database locks.
11. Make retries bounded and explicit.
12. Test concurrent behavior deliberately.
13. Do not assume thread-safe collections make compound business operations atomic.
14. Do not treat Java synchronization as a replacement for database concurrency control.

---

# 55. Final Dispatch Concurrency Flow

The expected production-style flow is:

```text id="p0t4gm"
Request A                    Request B
    |                            |
    v                            v
DispatchService             DispatchService
    |                            |
    +-------- candidates --------+
                 |
                 v
          Dispatch Engine
                 |
          select candidates
                 |
        +--------+--------+
        |                 |
        v                 v
   Assignment A      Assignment B
        |                 |
        v                 v
   Transaction        Transaction
        |                 |
        +--------+--------+
                 |
                 v
        PostgreSQL protection
                 |
        +--------+--------+
        |                 |
        v                 v
   Resource A        Resource A
   assignment        conflict
        |
        v
      COMMIT
```

Only one transaction should successfully obtain the conflicting resource.

The losing operation should receive a controlled result and may attempt another eligible resource according to the retry policy.

---

# 56. Document Status

**Document 09 — Concurrency & Thread Safety Design**

Status: Completed as the initial concurrency baseline.

This document will guide the implementation of:

* Java thread-safety exercises
* synchronized critical sections
* concurrent dispatch testing
* service thread safety
* PostgreSQL transactions
* resource locking
* conditional updates
* database constraints
* retry handling
* JPA/Hibernate concurrency mechanisms
* race-condition tests

The exact SQL locking statements and Java synchronization implementation will be introduced during the implementation phases rather than being hard-coded into this design document.