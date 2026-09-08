# ResQGrid — Dispatch Engine Design

**Document:** Dispatch Engine Design
**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Development Baseline

---

# 1. Purpose

The Dispatch Engine is the core decision-making component of ResQGrid.

Its responsibility is to determine which emergency resources are suitable for an incident and rank those resources so that the most appropriate candidate can be selected.

The engine must consider more than simple availability.

A resource may be available but still be unsuitable because of:

* resource type
* required capability
* distance
* workload
* operational status
* incident severity
* existing assignments
* other dispatch constraints

The Dispatch Engine is responsible for making the **dispatch decision**.

It is not responsible for directly managing HTTP requests or database connections.

---

# 2. Dispatch Engine Responsibilities

The Dispatch Engine is responsible for:

1. receiving an incident and available resource information
2. determining the requirements of the incident
3. filtering unsuitable resources
4. calculating candidate scores
5. ranking suitable candidates
6. selecting the best candidate or candidates
7. returning a dispatch decision
8. explaining why a candidate was selected when useful for debugging or reporting

The engine should not directly handle:

* HTTP requests
* JSP rendering
* SQL connection management
* transaction management
* authentication
* database-specific code

Those responsibilities belong to other layers.

---

# 3. Dispatch Decision Flow

The basic flow is:

```text
Incident
   |
   v
Determine requirements
   |
   v
Load candidate resources
   |
   v
Filter unsuitable resources
   |
   v
Calculate candidate scores
   |
   v
Rank candidates
   |
   v
Select best candidate(s)
   |
   v
Return dispatch decision
   |
   v
Dispatch Service performs assignment
```

An important distinction is made between:

**selection** and **assignment**.

The Dispatch Engine decides what should be assigned.

The Dispatch Service is responsible for performing the actual state-changing operation.

---

# 4. Candidate Selection vs Assignment

These two operations should not be treated as the same thing.

### Candidate selection

Answers:

> Which resource would be the best choice for this incident?

This is primarily business logic.

### Assignment

Answers:

> Can we safely assign that resource right now?

This requires:

* transaction handling
* current database state
* concurrency protection
* resource state update
* dispatch creation
* incident update
* history creation

Therefore:

```text
Dispatch Engine
    -> selects

Dispatch Service
    -> assigns safely
```

A resource selected by the engine may become unavailable before the assignment transaction completes.

The system must handle this situation rather than assuming the selection remains valid indefinitely.

---

# 5. Incident Requirements

The engine should derive the requirements used during candidate selection from the incident.

Important inputs may include:

* incident severity
* incident type
* incident location
* required resource type
* required capabilities
* number of resources required
* operational constraints

For example:

```text
Incident:
    Type: Medical Emergency
    Severity: CRITICAL
    Location: Point A
    Required Capability: ADVANCED_MEDICAL
    Required Resource Type: AMBULANCE
```

The engine should use these requirements when filtering candidates.

---

# 6. Resource Eligibility

A resource should only become a dispatch candidate if it satisfies the basic eligibility rules.

Initial eligibility checks include:

```text
Resource status = AVAILABLE
Resource type matches requirement
Required capabilities are present
Resource is not already actively assigned
Resource satisfies operational constraints
```

Resources that fail mandatory requirements should be removed before ranking.

There is little value in calculating a high score for a resource that cannot legally or operationally be dispatched.

---

# 7. Availability

Availability is a mandatory condition for normal dispatch.

The initial rule is:

```text
AVAILABLE -> candidate
BUSY -> reject
OFFLINE -> reject
MAINTENANCE -> reject
```

Availability should be checked both during candidate selection and again during the actual assignment process.

The second check is important because another request may have changed the resource state after candidate selection.

---

# 8. Resource Type Matching

Resource type is an important eligibility condition.

Examples:

```text
Medical incident
    -> AMBULANCE

Fire incident
    -> FIRE_UNIT

Specialized rescue incident
    -> RESCUE_TEAM
```

A resource with the wrong type should normally be rejected before ranking.

The design should allow additional resource types to be introduced later without requiring the Dispatch Engine to be completely rewritten.

---

# 9. Capability Matching

Resource capabilities provide a more detailed suitability check.

For example:

```text
Incident requires:
    ADVANCED_MEDICAL

Resource A:
    BASIC_MEDICAL
    ADVANCED_MEDICAL

Resource B:
    BASIC_MEDICAL
```

Resource A is eligible.

Resource B is not.

The engine should distinguish between:

* required capabilities
* optional capabilities

Required capabilities must be satisfied.

Optional capabilities may contribute to ranking.

---

# 10. Capability Matching Strategy

For the initial implementation, a resource should be considered fully eligible when it contains all mandatory capabilities required by the incident.

Conceptually:

```text
requiredCapabilities ⊆ resourceCapabilities
```

This is a natural use of Java `Set`.

For example:

```text
Required:
{ADVANCED_MEDICAL, BASIC_MEDICAL}

Resource:
{BASIC_MEDICAL, ADVANCED_MEDICAL, WATER_RESCUE}
```

The resource satisfies the requirement.

A resource with only:

```text
{BASIC_MEDICAL}
```

does not.

---

# 11. Distance

Distance is an important ranking factor.

When multiple resources are eligible, a closer resource should generally receive a better ranking than a much farther resource.

The engine can calculate the distance between:

```text
Incident location
        |
        v
Resource current location
```

For the initial implementation, the calculation can be based on latitude and longitude.

The exact geographic formula and implementation will be defined during the Java implementation phase.

The engine should treat distance as a ranking factor rather than automatically making the closest resource the winner.

---

# 12. Why Distance Is Not the Only Factor

Consider:

```text
Ambulance A
Distance: 2 km
Capability: BASIC_MEDICAL

Ambulance B
Distance: 5 km
Capability: ADVANCED_MEDICAL
```

For a critical incident requiring advanced medical capability:

```text
Ambulance A -> not eligible
Ambulance B -> eligible
```

A closer but unsuitable resource must not beat a suitable resource.

Eligibility therefore happens before ranking.

---

# 13. Severity and Priority

Incident severity influences dispatch priority.

Initial severity levels:

```text
CRITICAL
HIGH
MEDIUM
LOW
```

Critical incidents should receive stronger dispatch priority than lower-severity incidents.

Severity can influence:

* how quickly an incident is processed
* candidate scoring
* resource selection
* future scheduling behavior

The Dispatch Engine should not treat every incident as equally important.

---

# 14. Candidate Scoring

After filtering, eligible resources can be scored.

A conceptual score may consider:

```text
score =
    severity suitability
    + capability match
    + proximity
    + workload
    + operational preference
```

The exact numeric weights should be defined deliberately during implementation and testing.

We should avoid introducing arbitrary numbers without understanding how they affect real scenarios.

The important design decision is that ranking is multi-factor rather than based on one property.

---

# 15. Ranking Factors

Initial ranking factors are:

| Factor                  | Purpose                         |
| ----------------------- | ------------------------------- |
| Availability            | Mandatory eligibility condition |
| Resource type           | Mandatory eligibility condition |
| Required capabilities   | Mandatory eligibility condition |
| Distance                | Prefer faster response          |
| Workload                | Prefer less-loaded resources    |
| Severity                | Supports priority handling      |
| Operational constraints | Prevent unsuitable assignments  |

Some factors are filters.

Others are ranking criteria.

This distinction is important.

---

# 16. Filtering Before Ranking

The engine should follow this general pattern:

```text
All resources
      |
      v
Availability filter
      |
      v
Resource type filter
      |
      v
Capability filter
      |
      v
Operational constraint filter
      |
      v
Eligible candidates
      |
      v
Score candidates
      |
      v
Sort/rank
      |
      v
Best candidate
```

This keeps the ranking logic focused on resources that are actually dispatchable.

---

# 17. Java Collections

The Dispatch Engine naturally benefits from Java collections.

Possible structures include:

```text
List<EmergencyResource>
Set<Capability>
Map<ResourceType, List<EmergencyResource>>
PriorityQueue<ResourceCandidate>
```

The exact collection should be chosen based on the operation being performed.

We should not use collections simply because the project requires learning them.

Each collection should have a clear reason.

---

# 18. `Set` for Capabilities

A `Set` is appropriate for resource capabilities because duplicate capabilities have no useful meaning.

For example:

```text
{
    BASIC_MEDICAL,
    ADVANCED_MEDICAL,
    WATER_RESCUE
}
```

The engine can efficiently reason about whether required capabilities are contained in the resource's capability set.

---

# 19. `List` for Candidate Resources

A `List` is appropriate when the engine has a collection of candidate resources that needs to be processed or transformed.

For example:

```text
List<EmergencyResource>
```

may represent the resources loaded for consideration.

The list can then be filtered and converted into candidate objects.

---

# 20. `PriorityQueue`

A `PriorityQueue` may be useful when the engine needs to repeatedly retrieve the highest-ranked candidate.

Conceptually:

```text
PriorityQueue<ResourceCandidate>
```

The queue ordering can be defined using a `Comparator`.

This is particularly useful if the engine eventually needs to select several resources rather than only one.

The final implementation should use `PriorityQueue` only if its behavior actually benefits the algorithm.

A normal sorted list may be simpler when all candidates must be ranked anyway.

---

# 21. `Comparator`

Candidate ranking should be represented using explicit comparison logic.

Possible ranking criteria include:

```text
higher suitability
    -> first

shorter distance
    -> preferred

lower workload
    -> preferred
```

Java's `Comparator` provides a clean way to express this ordering.

This also makes ranking rules easier to test and change.

---

# 22. Candidate Object

The engine should not necessarily rank raw `EmergencyResource` objects directly.

A separate candidate representation can contain calculated information.

For example, conceptually:

```text
ResourceCandidate

resource
distance
workload
score
capabilityMatch
```

This keeps calculated dispatch-specific information separate from the core resource entity.

The final Java type may be a record if its purpose remains a simple immutable data carrier.

---

# 23. Why Candidate Data Should Be Separate

`EmergencyResource` represents a real domain object.

It should not need fields such as:

```text
temporaryDispatchScore
calculatedDistanceForCurrentIncident
temporaryRanking
```

These values belong to the current dispatch decision, not to the resource itself.

Keeping them separate prevents temporary business calculations from polluting the domain model.

---

# 24. Streams

Java Streams may be useful for candidate processing.

A typical conceptual flow is:

```text
resources
    -> filter
    -> filter
    -> map to candidates
    -> sort
    -> collect
```

Streams should be used when they make the business logic clearer.

A complicated stream chain should not be preferred merely because it uses modern Java features.

Readable imperative code is acceptable when it communicates the business rule better.

---

# 25. Lambdas

The Dispatch Engine is a natural place to practice lambda expressions because:

* `Comparator` commonly uses lambdas
* stream filtering uses predicates
* mapping operations use functions
* sorting can use lambda-based comparison

For example, the engine may eventually express a rule conceptually as:

```text
candidate -> candidate.score()
```

The actual implementation should remain readable and should not hide important business rules inside overly complex lambdas.

---

# 26. Functional Interfaces

The engine may use functional interfaces when a ranking or filtering strategy needs to be configurable.

Examples include:

```text
Predicate<T>
Function<T, R>
Comparator<T>
```

These should only be introduced where they provide a genuine design benefit.

The Dispatch Engine is a good place to learn them through an actual business problem rather than isolated exercises.

---

# 27. Extensibility

The engine should not become tightly coupled to one resource type.

For example, adding:

```text
HAZMAT_UNIT
```

should primarily require:

* a new resource type
* its capabilities
* relevant business rules

rather than rewriting the complete dispatch algorithm.

This is one reason the engine should depend on domain abstractions and business rules rather than hard-coded resource-specific branches everywhere.

---

# 28. Multiple Resource Dispatch

Some incidents may require more than one resource.

For example:

```text
Large building fire
    -> multiple FIRE_UNIT resources
    -> RESCUE_TEAM
    -> additional support
```

The engine should therefore be designed so that selection can eventually return:

```text
List<ResourceCandidate>
```

rather than permanently assuming:

```text
one incident -> one resource
```

The first implementation may still support one-resource dispatch as the simplest operational path.

---

# 29. No Suitable Resource

The engine must handle situations where no resource satisfies the incident requirements.

Possible result:

```text
No suitable resource available
```

This should not be treated as an unexpected system crash.

It is a valid business outcome.

The service layer can then decide how the incident should remain queued or how the situation should be reported.

---

# 30. Resource Becomes Unavailable

Candidate selection does not reserve the resource.

Example:

```text
10:00:00
Engine selects Ambulance A

10:00:01
Another incident receives Ambulance A

10:00:02
Original dispatch attempts assignment
```

Ambulance A may no longer be available.

The assignment operation must detect this.

The system can then:

1. reject the stale selection
2. reload candidates
3. select another resource
4. retry according to defined limits

The retry policy will be handled by the Dispatch Service and concurrency design.

---

# 31. Dispatch Engine and Transactions

The Dispatch Engine itself should preferably remain independent of database transaction management.

The overall operation is:

```text
Dispatch Service
      |
      +--> load resources
      |
      +--> Dispatch Engine
      |       |
      |       +--> select candidate
      |
      +--> begin/continue transaction
      |
      +--> verify resource
      |
      +--> assign resource
      |
      +--> update states
      |
      +--> commit
```

This separation keeps business selection logic testable without requiring a database for every test.

---

# 32. Dispatch Engine and Persistence

The engine should not contain JDBC code such as:

```text
Connection
PreparedStatement
ResultSet
```

It should receive domain objects or suitable data structures from the service/repository layer.

This separation prevents database implementation details from leaking into business logic.

---

# 33. In-Memory Phase

The first version of the Dispatch Engine should work against in-memory data.

This allows us to focus on:

* filtering
* capability matching
* ranking
* collections
* comparators
* streams
* business rules

without introducing JDBC complexity at the same time.

Once the business logic is stable, the repository can provide data from PostgreSQL.

---

# 34. JDBC Phase

During the JDBC phase, repositories will load the required data.

The flow becomes:

```text
PostgreSQL
    |
    v
JDBC Repository
    |
    v
Domain objects
    |
    v
Dispatch Service
    |
    v
Dispatch Engine
```

The engine itself should not need to know whether the data came from PostgreSQL, a test fixture, or another repository implementation.

---

# 35. Hibernate/JPA Phase

When Hibernate/JPA is introduced, the same architectural responsibility should remain.

The persistence layer changes.

The business decision logic does not.

Conceptually:

```text
JDBC Repository
       \
        -> Dispatch Service -> Dispatch Engine
       /
JPA Repository
```

This demonstrates why separating business logic from persistence is important.

---

# 36. Error Handling

The engine should distinguish business outcomes from technical failures.

### Valid business outcome

```text
No suitable resource available
```

### Invalid input

```text
Incident requirements are incomplete
```

### Technical failure

```text
Unable to retrieve candidate resources
```

These should not all become generic `Exception` objects.

The application will define appropriate domain/application exceptions during implementation.

---

# 37. Logging and Diagnostics

Dispatch decisions should be explainable during development.

Useful diagnostic information may include:

```text
Incident ID
Candidate resource IDs
Rejected candidates and reason
Selected resource
Calculated score
Distance
Dispatch attempt
```

Logging must not expose unnecessary sensitive information.

The exact logging strategy will be defined later.

---

# 38. Example Decision

Consider:

```text
Incident:
    Severity: CRITICAL
    Required Type: AMBULANCE
    Required Capability: ADVANCED_MEDICAL
```

Available resources:

```text
Ambulance A
    AVAILABLE
    ADVANCED_MEDICAL
    3 km
    workload: low

Ambulance B
    AVAILABLE
    BASIC_MEDICAL
    1 km
    workload: low

Ambulance C
    AVAILABLE
    ADVANCED_MEDICAL
    7 km
    workload: high
```

Filtering produces:

```text
Ambulance A
Ambulance C
```

Ambulance B is rejected because it lacks the required capability.

The ranking stage then compares A and C using the defined scoring rules.

The engine selects the highest-ranked candidate.

---

# 39. Important Invariants

The Dispatch Engine must never intentionally select a resource that:

* is unavailable
* has the wrong resource type
* lacks a required capability
* violates a mandatory operational constraint

The engine should also avoid making decisions based on stale calculated information when the actual assignment occurs.

Final resource availability must be confirmed by the assignment process.

---

# 40. Testing Strategy

The Dispatch Engine should be highly testable because most of its logic is deterministic.

Important test cases include:

### Eligibility

* available resource is accepted
* busy resource is rejected
* offline resource is rejected
* maintenance resource is rejected
* wrong resource type is rejected
* missing required capability is rejected

### Ranking

* closer suitable resource ranks higher
* lower workload ranks appropriately
* severity affects priority as designed
* ranking ties are deterministic

### Edge cases

* no resources
* no eligible resources
* one eligible resource
* many eligible resources
* duplicate capabilities
* multiple required capabilities
* equal scores
* incomplete incident requirements

---

# 41. Deterministic Ranking

If two resources have exactly the same ranking score, the engine should have a deterministic tie-breaking rule.

For example:

```text
1. score
2. distance
3. resource ID
```

The exact ordering will be finalized during implementation.

Deterministic behavior is important for:

* predictable production behavior
* repeatable tests
* debugging
* easier incident investigation

---

# 42. Performance Considerations

The initial implementation should prioritize correctness and clarity.

For a moderate number of resources, a straightforward approach is sufficient:

```text
filter -> calculate -> rank
```

If the number of resources grows significantly, optimization can be considered later.

Potential optimization areas include:

* filtering at repository level
* reducing unnecessary database queries
* indexed resource queries
* avoiding repeated capability loading
* avoiding repeated distance calculations
* caching carefully where appropriate

Premature optimization should be avoided.

---

# 43. Concurrency Boundary

The Dispatch Engine itself can perform selection logic without owning the global concurrency lock.

Concurrency becomes critical when the selected resource is actually assigned.

Therefore:

```text
Selection
    -> Dispatch Engine

State transition
    -> Dispatch Service + transaction + concurrency controls
```

This separation prevents the engine from becoming a large synchronized component.

The detailed concurrency strategy is defined in the separate Concurrency Design document.

---

# 44. Design Principles

The Dispatch Engine should follow these principles:

1. Filter before ranking.
2. Mandatory requirements must be satisfied before scoring.
3. Business selection should be separate from state-changing assignment.
4. Database access should stay outside the engine.
5. Ranking rules should be explicit and testable.
6. Candidate calculations should not pollute domain entities.
7. Collections should be selected according to actual use.
8. Streams and lambdas should improve readability, not reduce it.
9. Selection must tolerate the possibility of stale resource state.
10. Assignment must revalidate the selected resource.
11. Ranking should be deterministic.
12. The design should allow additional resource types and dispatch rules later.

---

# 45. Planned Java Responsibilities

The eventual implementation is expected to separate responsibilities approximately like this:

```text
DispatchService
    |
    +-- coordinates dispatch operation
    |
    +-- obtains resource data
    |
    +-- invokes DispatchEngine
    |
    +-- performs assignment
    |
    +-- manages transaction boundary

DispatchEngine
    |
    +-- filters candidates
    |
    +-- evaluates suitability
    |
    +-- calculates ranking
    |
    +-- selects candidate(s)

ResourceCandidate
    |
    +-- represents dispatch-specific calculated data

Repository
    |
    +-- loads and persists resources/incidents/dispatches

Domain objects
    |
    +-- represent core business state
```

These names are the initial design baseline and may be refined during implementation if a better responsibility boundary becomes clear.

---

# 46. Future Extensions

The design should leave room for future features such as:

* multiple-resource dispatch
* specialized response teams
* estimated response time
* road/network distance
* resource capacity
* equipment requirements
* geographic zones
* operational priorities
* dynamic workload
* dispatch escalation
* fallback resource selection

These are extension points, not requirements for the first implementation.

---

# 47. Final Dispatch Flow

The complete initial decision process is:

```text
Incident received
       |
       v
Determine incident requirements
       |
       v
Retrieve possible resources
       |
       v
Filter by availability
       |
       v
Filter by resource type
       |
       v
Filter by required capabilities
       |
       v
Apply operational constraints
       |
       v
Create dispatch candidates
       |
       v
Calculate distance/workload/score
       |
       v
Rank candidates
       |
       v
Select best candidate(s)
       |
       v
Return decision
       |
       v
Dispatch Service validates current state
       |
       v
Perform transactional assignment
```

---

# 48. Document Status

**Document 08 — Dispatch Engine Design**

Status: Completed as the initial dispatch-engine baseline.

This document will guide the implementation of:

* candidate filtering
* capability matching
* ranking
* `Comparator`
* collections
* `PriorityQueue` where justified
* streams
* lambdas
* candidate modeling
* dispatch selection
* unit testing

Concurrency, locking, and race-condition handling are intentionally covered separately in **Document 09 — Concurrency & Thread Safety Design**.