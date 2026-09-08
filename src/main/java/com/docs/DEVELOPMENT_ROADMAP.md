# ResQGrid — Development Roadmap & Implementation Plan

**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Development Baseline

---

# 1. Development Approach

ResQGrid will be developed incrementally.

The project will start with the core Java domain and business logic before introducing database and web technologies.

Each major stage should leave the project in a working state.

The development cycle is:

```text
Understand Requirement
        |
        v
Review Relevant Java Concept
        |
        v
Design
        |
        v
Implement
        |
        v
Test
        |
        v
Code Review
        |
        v
Refactor
        |
        v
Git Commit
        |
        v
Next Feature
```

The goal is not to finish the technologies one by one. The goal is to build the system while using each technology where it solves a real problem.

---

# 2. Phase 0 — Project Setup

### Current status

Completed.

The project currently contains:

* Maven project
* Java 21 configuration
* Git repository
* GitHub repository
* basic package structure
* project documentation

Initial project structure:

```text
resqgrid/
├── docs/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── resqgrid/
│   │               └── ResQGridApplication.java
│   └── test/
├── .gitignore
└── pom.xml
```

---

# 3. Phase 1 — Java Domain Foundation

This is the first implementation phase.

The focus is on building the domain model using plain Java before introducing persistence frameworks.

## Main concepts

* classes and objects
* encapsulation
* constructors
* enums
* validation
* exceptions
* relationships
* collections
* generics
* `equals()`
* `hashCode()`
* immutability where appropriate

## Initial domain types

The first domain types will include:

```text
Incident
EmergencyResource
Location
Capability
Dispatch
ResponseTeam
```

And the required enums:

```text
IncidentSeverity
IncidentStatus
ResourceStatus
ResourceType
DispatchStatus
```

Not every class will necessarily be implemented immediately. Types will be introduced when their responsibilities become clear.

---

# 4. Phase 1 Implementation Order

The initial implementation order will be approximately:

```text
Location
   |
   v
Capability
   |
   v
Enums
   |
   v
Incident
   |
   v
EmergencyResource
   |
   v
Dispatch
```

Relationships will be introduced gradually instead of creating a large object graph on the first day.

---

# 5. Phase 1 Testing

JUnit 5 will be introduced early.

Initial tests will cover:

* object creation
* validation
* enum behavior
* state transitions
* equality
* invalid input
* domain invariants

The goal is to learn how to test Java business logic before database and web complexity is introduced.

---

# 6. Phase 2 — In-Memory Resource Management

Once the domain model is stable, the application will manage incidents and resources in memory.

Possible components:

```text
IncidentService
ResourceService
DispatchService
DispatchEngine
```

Repositories may initially use in-memory collections.

Examples:

```text
List
Set
Map
Queue
PriorityQueue
```

Collections will be selected according to actual requirements.

---

# 7. Phase 2 — Collections and Generics

This phase will deliberately reinforce Java collections and generics.

ResQGrid provides real use cases for:

* `List` for ordered data
* `Set` for unique capabilities
* `Map` for indexed lookup
* `PriorityQueue` for candidate ranking
* generics for reusable components
* `Comparator` for ranking

The project should make the reason for each collection clear.

---

# 8. Phase 2 — Lambdas and Streams

Lambda expressions are a known weak area and will receive deliberate practice during the Dispatch Engine implementation.

Potential uses include:

* filtering candidates
* sorting resources
* calculating rankings
* grouping data
* transforming collections
* checking capabilities

For example, the Dispatch Engine may need to express logic such as:

```text
available resources
        |
        v
filter by type
        |
        v
filter by capability
        |
        v
calculate ranking
        |
        v
sort candidates
```

Streams and lambdas will be used where they improve clarity, not simply because the project needs to demonstrate them.

---

# 9. Phase 2 — Functional Interfaces

Functional interfaces will be introduced when the dispatch rules become sufficiently complex to benefit from configurable behavior.

Potential examples include strategies for:

* candidate scoring
* filtering
* prioritization

This will provide practical experience with functional programming concepts in Java.

---

# 10. Phase 3 — Dispatch Engine

The Dispatch Engine is the main business-logic milestone.

It will determine suitable resources for incidents.

The engine will consider factors such as:

* incident severity
* resource availability
* resource type
* required capabilities
* distance
* workload
* priority
* operational constraints

The engine will first filter invalid candidates and then rank the remaining candidates.

---

# 11. Phase 3 — Dispatch Algorithm

The initial conceptual flow is:

```text
Incident
   |
   v
Determine requirements
   |
   v
Get candidate resources
   |
   v
Filter unavailable resources
   |
   v
Filter incompatible resource types
   |
   v
Filter missing capabilities
   |
   v
Calculate candidate scores
   |
   v
Rank candidates
   |
   v
Select best candidate
```

The algorithm will remain testable independently from persistence.

---

# 12. Phase 3 — Concurrency

After the normal dispatch flow works, concurrent dispatch will be introduced.

The main scenario is:

```text
Incident A ----\
                \
                 >---- Ambulance X
                /
Incident B ----/
```

Both requests may attempt to assign the same resource.

The implementation will address:

* check-then-act races
* shared mutable state
* critical sections
* synchronization
* thread-safe collections where appropriate
* atomic operations
* resource-level locking

The final protection will eventually move to the database.

---

# 13. Phase 3 — Concurrency Testing

Concurrency tests will deliberately create competing operations.

Possible tools:

```text
ExecutorService
CountDownLatch
CyclicBarrier
AtomicInteger
```

The important result is:

```text
At most one valid active assignment
```

rather than simply:

```text
Both threads completed
```

---

# 14. Phase 4 — PostgreSQL and JDBC

Once the in-memory business logic is understood, PostgreSQL will be introduced.

The JDBC phase is intentional.

Before relying on Hibernate/JPA, the project will directly experience:

* JDBC connections
* prepared statements
* SQL queries
* result sets
* transactions
* commits
* rollbacks
* resource management
* SQL exceptions

---

# 15. Phase 4 — Database Integration Order

The database work will progress approximately as follows:

```text
PostgreSQL setup
      |
      v
Schema creation
      |
      v
JDBC connection
      |
      v
Incident repository
      |
      v
Resource repository
      |
      v
Dispatch repository
      |
      v
History repository
      |
      v
Transactions
      |
      v
Concurrency protection
```

---

# 16. Phase 4 — SQL

SQL will remain important throughout the project.

The project will use PostgreSQL for:

* transactional data
* relationships
* constraints
* joins
* indexes
* reporting queries
* concurrency protection

SQL should not become invisible merely because Hibernate is introduced later.

---

# 17. Phase 5 — Hibernate and JPA

After JDBC persistence is understood and working, Hibernate/JPA will be introduced.

The goal is to understand what ORM solves and what it does not solve.

Topics will include:

* entities
* `@Entity`
* `@Id`
* `@GeneratedValue`
* `@ManyToOne`
* `@OneToMany`
* `@ManyToMany`
* `mappedBy`
* `@JoinColumn`
* cascade
* orphan removal
* JPQL/HQL
* native SQL
* persistence context
* entity lifecycle
* dirty checking
* first-level cache
* lazy loading
* eager loading
* N+1 queries
* transactions
* locking

---

# 18. Phase 5 — JPA Relationships

Relationships will be implemented based on the actual domain model.

The important relationships include:

```text
Incident -> Dispatch
EmergencyResource -> Dispatch
EmergencyResource -> Capability
Incident -> Location
EmergencyResource -> Location
```

Relationship mappings will be reviewed carefully instead of relying on generated annotations.

---

# 19. Phase 5 — ORM Problems

The project will deliberately investigate common ORM problems.

Examples:

* `LazyInitializationException`
* N+1 queries
* incorrect cascading
* unintended deletes
* excessive eager fetching
* detached entities
* incorrect transaction boundaries

These should be understood through actual ResQGrid scenarios.

---

# 20. Phase 6 — Servlet/JSP/MVC

After the service and persistence layers are stable, the web interface will be introduced.

Technologies:

* Servlets
* JSP
* MVC
* HTML
* CSS
* basic JavaScript
* Apache Tomcat

The web layer will consume the existing services.

It should not recreate business logic.

---

# 21. Phase 6 — Main Web Screens

Initial screens may include:

```text
Dashboard
Incident List
Incident Details
Incident Registration
Resource List
Resource Details
Dispatch Details
History
Operational Reports
```

Screens will be added according to actual implementation needs.

---

# 22. Phase 6 — MVC Flow

A typical operation should follow:

```text
Browser
   |
   v
Servlet
   |
   v
Service
   |
   v
Repository
   |
   v
Database
   |
   v
Service result
   |
   v
Servlet
   |
   v
JSP
```

JSP remains responsible for presentation.

---

# 23. Phase 7 — REST API

REST will be implemented after the service layer already contains working business logic.

Initial API groups:

```text
/api/incidents
/api/resources
/api/dispatches
/api/reports
```

The API will support:

* JSON requests
* JSON responses
* validation
* HTTP status codes
* error responses
* DTOs
* dispatch operations
* history retrieval

---

# 24. Phase 7 — REST Testing

REST operations will be tested using:

* Postman
* automated tests where appropriate
* integration tests

Important cases include:

* successful requests
* invalid requests
* missing resources
* invalid state transitions
* dispatch conflicts
* no suitable resource
* concurrent dispatch requests

---

# 25. Phase 8 — Reporting

Reporting functionality will be introduced after core incident, resource, and dispatch workflows are stable.

Possible reports:

* incidents by severity
* incidents by status
* dispatch counts
* resource utilization
* average response time
* completed vs cancelled dispatches

SQL will be important for reporting queries.

---

# 26. Phase 9 — Refactoring and Code Quality

Once the major functionality exists, the codebase will receive a dedicated refactoring pass.

Review areas:

* class responsibilities
* package structure
* naming
* duplication
* exception handling
* validation
* dependency direction
* collection choices
* generic types
* thread safety
* database interaction
* transaction boundaries
* test quality
* API design

The goal is to improve the existing implementation rather than blindly rewriting it.

---

# 27. Phase 10 — Spring Introduction

Spring will be introduced only after the underlying architecture is understood.

The project will compare:

```text
Manual Java / Servlet approach
            |
            v
Spring Core
            |
            v
Spring MVC
            |
            v
Spring Boot
            |
            v
Spring Data JPA
            |
            v
Spring Security
```

The purpose is to understand what Spring provides rather than treating Spring annotations as magic.

---

# 28. Technology Evolution

The overall technology progression is:

```text
Java
 |
 v
In-Memory Application
 |
 v
Concurrency
 |
 v
PostgreSQL + JDBC
 |
 v
Hibernate/JPA
 |
 v
Servlets/JSP/MVC
 |
 v
REST
 |
 v
Testing / Refactoring
 |
 v
Spring
```

Each step builds on the previous one.

---

# 29. Git Strategy

Git will be used throughout development.

Commits should represent meaningful milestones.

Examples:

```text
Initial project setup
Add project documentation
Implement incident domain model
Add resource domain model
Implement dispatch candidate selection
Add dispatch concurrency protection
Add PostgreSQL schema
Implement JDBC repositories
Add JPA mappings
Implement servlet MVC flow
Add REST API
Add dispatch integration tests
Refactor dispatch engine
```

Commit messages should describe what changed rather than using vague messages such as:

```text
update
changes
done
final
```

---

# 30. Branching Strategy

The initial development can remain on `main` while the project is small.

Feature branches may be introduced when a feature becomes large enough to justify isolated work.

For example:

```text
main
 |
 +-- feature/dispatch-engine
 |
 +-- feature/jdbc-persistence
 |
 +-- feature/rest-api
```

The project should not create branches merely to follow a process.

---

# 31. Definition of Done

A feature is considered complete when:

* requirements are understood
* design is clear
* implementation is complete
* relevant tests pass
* error cases are handled
* concurrency is considered where relevant
* persistence behavior is verified where relevant
* code has been reviewed
* unnecessary complexity has been removed
* documentation is updated if required
* changes are committed to Git

---

# 32. Learning Strategy

The project is also a Java backend learning environment.

When a concept is needed, the approach will be:

```text
Concept
   |
   v
Definition
   |
   v
Why ResQGrid needs it
   |
   v
Small example
   |
   v
Implement in project
   |
   v
Review understanding
```

Previously learned topics will be revisited when they naturally appear.

Weak areas will receive additional exercises rather than being skipped.

---

# 33. Concepts to Reinforce

Throughout development, particular attention will be given to:

### Java

* OOP
* inheritance
* interfaces
* abstraction
* encapsulation
* exceptions
* collections
* generics
* lambdas
* streams
* functional interfaces
* `Optional`
* records
* enums
* annotations

### Concurrency

* threads
* synchronization
* locks
* atomic operations
* concurrent collections
* race conditions
* transactions
* database locking

### Persistence

* SQL
* JDBC
* transactions
* JPA
* Hibernate
* entity lifecycle
* fetching
* caching
* locking

### Web

* HTTP
* Servlets
* JSP
* MVC
* REST
* JSON
* HTTP status codes

### Testing

* JUnit 5
* unit tests
* integration tests
* database tests
* API tests
* concurrency tests

---

# 34. Implementation Discipline

The project should avoid implementing everything at once.

For each feature:

```text
Requirement
    |
    v
Design
    |
    v
Implementation
    |
    v
Test
    |
    v
Review
    |
    v
Refactor
    |
    v
Commit
```

Moving to the next feature before the current feature is understood should be avoided.

---

# 35. Current Milestone

The documentation and project setup phase is complete.

The next implementation milestone is:

**Phase 1 — Java Domain Foundation**

The first implementation focus is:

```text
Incident
Location
Enums
Validation
Domain exceptions
```

After the first domain objects are implemented and tested, `EmergencyResource` and the remaining relationships will be introduced.

---

# 36. Final Project Progression

The intended progression is:

```text
                RESQGRID

Documentation
      |
      v
Java Domain
      |
      v
In-Memory Business Logic
      |
      v
Dispatch Engine
      |
      v
Concurrency
      |
      v
PostgreSQL + JDBC
      |
      v
Hibernate/JPA
      |
      v
Servlet/JSP MVC
      |
      v
REST API
      |
      v
Testing + Refactoring
      |
      v
Spring
      |
      v
Production-Level Review
```

The final objective is a working backend system whose architecture, business rules, persistence behavior, concurrency handling, and tests can all be explained and defended by the developer who built it.