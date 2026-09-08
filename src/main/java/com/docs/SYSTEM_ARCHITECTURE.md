# ResQGrid — System Architecture

**Document:** System Architecture
**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Development Baseline

---

# 1. Architecture Overview

ResQGrid will use a layered architecture.

The main goal is to keep:

* HTTP handling
* business logic
* domain behavior
* persistence
* database access

separated from each other.

The architecture should remain understandable and appropriate for the size of the project. We will not introduce layers or frameworks simply because they are common in enterprise applications.

The initial high-level structure is:

```text
                    +----------------------+
                    |   Web / REST API     |
                    | Servlets / JSP       |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |    Controller        |
                    | Request / Response   |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |      Service         |
                    | Business Workflows   |
                    +----------+-----------+
                               |
                 +-------------+-------------+
                 |                           |
                 v                           v
        +----------------+          +----------------+
        | Domain Model   |          | Dispatch       |
        | Business State |          | Engine         |
        +----------------+          +----------------+
                 |                           |
                 +-------------+-------------+
                               |
                               v
                    +----------------------+
                    | Repository / DAO     |
                    | JDBC / JPA            |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |     PostgreSQL       |
                    +----------------------+
```

---

# 2. Architectural Goals

The architecture should provide:

* clear separation of responsibilities
* testable business logic
* controlled database access
* transaction boundaries
* concurrency safety
* maintainability
* understandable code structure
* ability to evolve from JDBC to JPA
* ability to evolve from Servlets to Spring later

The architecture is designed for learning as well as implementation.

---

# 3. Main Layers

The initial application will be organized around the following responsibilities:

1. Presentation
2. Controller
3. Service
4. Domain
5. Repository / DAO
6. Persistence / Infrastructure
7. Database

These are logical responsibilities. They do not automatically mean every responsibility needs its own package or class immediately.

---

# 4. Presentation Layer

The presentation layer handles interaction with users and external clients.

Initial technologies:

* JSP
* HTML
* CSS
* basic JavaScript
* REST responses

The presentation layer should focus on presenting information.

It should not contain core dispatch rules.

For example, a JSP should not decide:

```text
Which ambulance is best?
```

That decision belongs to the business layer.

---

# 5. Controller Layer

Controllers translate external requests into application operations.

For the web application, this will primarily involve Servlets.

For REST, Servlet-based controllers/endpoints will initially be used before introducing Spring MVC.

Typical controller responsibilities:

* receive HTTP request
* extract parameters
* validate basic request format
* convert request data into appropriate input objects
* call a service
* translate the result into a response
* handle appropriate application exceptions

Controllers should remain thin.

---

# 6. Service Layer

The service layer coordinates business operations.

This is where application-level workflows will live.

Potential services include:

* `IncidentService`
* `ResourceService`
* `DispatchService`
* `ReportingService`

The exact classes will be finalized during implementation.

---

# 7. Incident Service

The Incident Service will coordinate operations involving incidents.

Potential responsibilities:

* create incident
* retrieve incident
* update incident
* change incident status
* validate incident-related business operations
* coordinate incident persistence
* record relevant history

It should not contain HTTP-specific code.

---

# 8. Resource Service

The Resource Service will coordinate operations involving emergency resources.

Potential responsibilities:

* register resource
* retrieve resource
* update resource
* change resource status
* manage capabilities
* coordinate resource persistence

It should not decide the complete dispatch ranking algorithm.

That belongs to the Dispatch Engine.

---

# 9. Dispatch Service

The Dispatch Service will coordinate the dispatch workflow.

This is expected to be one of the most important application services.

A dispatch operation may involve:

```text
Receive dispatch request
        |
        v
Validate incident
        |
        v
Find candidate resources
        |
        v
Dispatch Engine ranks candidates
        |
        v
Attempt assignment
        |
        v
Create dispatch
        |
        v
Update resource
        |
        v
Update incident
        |
        v
Record history
        |
        v
Commit transaction
```

The Dispatch Service coordinates this process.

It should not place all dispatch logic into one enormous method.

---

# 10. Dispatch Engine

The Dispatch Engine is a dedicated business component responsible for resource selection.

Its responsibilities include:

* finding eligible candidates
* filtering unsuitable resources
* evaluating capabilities
* considering resource status
* evaluating distance
* considering workload
* ranking candidates
* applying tie-breaking rules

Conceptually:

```text
Incident
   |
   v
Dispatch Engine
   |
   +--> Candidate filtering
   |
   +--> Capability matching
   |
   +--> Distance evaluation
   |
   +--> Workload evaluation
   |
   +--> Ranking
   |
   v
Best Candidate
```

The engine should not directly manipulate HTTP requests or database connections.

---

# 11. Domain Layer

The domain layer contains the core business concepts.

Examples:

* `Incident`
* `EmergencyResource`
* `Dispatch`
* `ResponseTeam`
* `Location`
* `History`

Supporting enums may include:

* `IncidentSeverity`
* `IncidentStatus`
* `ResourceStatus`
* `ResourceType`
* `DispatchStatus`

The domain layer should represent meaningful business state and behavior.

---

# 12. Domain Behavior

Where appropriate, domain objects should protect their own valid state.

For example, an `Incident` should not expose completely unrestricted status mutation if that would allow invalid lifecycle transitions.

Instead, domain behavior should make invalid states harder to create.

Conceptually:

```text
Incident
   |
   +--> current status
   |
   +--> severity
   |
   +--> location
   |
   +--> valid status transition
```

The exact implementation will be decided during coding.

---

# 13. Repository / DAO Layer

Repositories or DAOs isolate persistence operations from business logic.

Examples:

* `IncidentRepository`
* `ResourceRepository`
* `DispatchRepository`
* `HistoryRepository`

Initially, JDBC will be used to understand direct SQL and database interaction.

Later, JPA/Hibernate will be introduced where appropriate.

---

# 14. Repository Responsibilities

Repositories should handle persistence concerns such as:

* saving entities
* finding entities
* updating entities
* deleting entities where appropriate
* querying resources
* retrieving dispatch history
* retrieving reporting data

Repositories should not decide:

```text
Which resource is operationally best?
```

That is a business decision.

---

# 15. JDBC Phase

Before relying on ORM abstraction, ResQGrid will use JDBC directly.

This phase is intentional.

We want to understand:

* JDBC connections
* `PreparedStatement`
* `ResultSet`
* SQL queries
* transactions
* commit
* rollback
* resource management
* SQL/database errors

The goal is to understand what ORM eventually abstracts.

---

# 16. JPA / Hibernate Phase

After the JDBC implementation and database concepts are understood, Hibernate/JPA will be introduced.

The JPA phase will focus on understanding:

* entities
* relationships
* `@OneToMany`
* `@ManyToOne`
* `@OneToOne` where justified
* `mappedBy`
* `@JoinColumn`
* cascade behavior
* orphan removal
* entity lifecycle
* persistence context
* dirty checking
* first-level cache
* lazy/eager fetching
* N+1 queries
* JPQL/HQL
* transactions

JPA should solve persistence problems rather than hide a lack of understanding of the database.

---

# 17. Persistence / Infrastructure Layer

Infrastructure contains technical components required by the application.

Potential responsibilities include:

* database connection management
* transaction management
* Hibernate configuration
* JDBC configuration
* application configuration
* logging
* utility infrastructure

Infrastructure should support the business application rather than contain business decisions.

---

# 18. Database Layer

PostgreSQL is the persistent storage system.

The database will store information such as:

* incidents
* resources
* capabilities
* dispatches
* locations
* history
* response teams where required

Database design will be documented separately before implementation.

---

# 19. Request Flow

A typical REST request will follow a path similar to:

```text
HTTP Request
     |
     v
REST Servlet
     |
     v
Controller
     |
     v
Service
     |
     +--------> Domain
     |
     +--------> Dispatch Engine
     |
     v
Repository / DAO
     |
     v
PostgreSQL
     |
     v
Repository Result
     |
     v
Service Result
     |
     v
Controller
     |
     v
HTTP Response
```

---

# 20. Dispatch Request Flow

Dispatch is more complex than a normal CRUD operation.

The conceptual flow is:

```text
Dispatcher
    |
    v
Dispatch Controller
    |
    v
Dispatch Service
    |
    +--> Load Incident
    |
    +--> Load Candidate Resources
    |
    v
Dispatch Engine
    |
    +--> Filter
    +--> Match capabilities
    +--> Evaluate distance
    +--> Evaluate workload
    +--> Rank
    |
    v
Selected Resource
    |
    v
Concurrency-Safe Assignment
    |
    +--> Create Dispatch
    +--> Update Resource
    +--> Update Incident
    +--> Record History
    |
    v
Commit
```

The concurrency-safe assignment step is critical.

---

# 21. Transaction Boundary

A transaction should represent a meaningful business operation.

For the core dispatch operation, the transaction boundary will likely cover:

```text
resource availability check
        +
resource assignment
        +
dispatch creation
        +
incident update
        +
history creation
```

The exact transaction implementation will be determined during JDBC and JPA phases.

---

# 22. Concurrency Boundary

Concurrency protection must exist around the operation where shared resource state can change.

The dangerous sequence is:

```text
Thread A: read AVAILABLE
Thread B: read AVAILABLE
Thread A: assign
Thread B: assign
```

The architecture must prevent this from producing two conflicting active assignments.

The final strategy may involve a combination of:

* Java concurrency control
* transaction boundaries
* database constraints
* row-level locking or equivalent database mechanisms

The final decision will be documented separately.

---

# 23. Error Handling

Errors should be handled according to their responsibility.

Examples:

### Controller-level

* malformed request
* invalid HTTP input
* unsupported request format

### Service/domain-level

* invalid business operation
* invalid status transition
* no suitable resource
* resource unavailable

### Repository/infrastructure-level

* SQL failure
* database connection failure
* persistence failure

The system should avoid catching every exception in one generic location and silently continuing.

---

# 24. Dependency Direction

The preferred dependency direction is:

```text
Presentation
     |
     v
Controller
     |
     v
Service
     |
     +----> Domain
     |
     +----> Repository interfaces
              |
              v
       Persistence implementation
```

The domain should not depend on Servlets or JSP.

The domain should not know about HTTP.

The domain should not create database connections.

---

# 25. Interfaces and Implementations

Interfaces will be introduced where they provide a meaningful abstraction.

For example:

```text
DispatchEngine
      |
      +---- DefaultDispatchEngine
```

or:

```text
IncidentRepository
      |
      +---- JdbcIncidentRepository
      |
      +---- JpaIncidentRepository
```

The goal is not to create interfaces for every class.

An abstraction should exist because:

* behavior has multiple implementations
* testing benefits from substitution
* the boundary represents a meaningful architectural contract
* future evolution is reasonably expected

---

# 26. MVC Approach

The web application will initially follow MVC concepts:

```text
Model
  |
  v
Service / Domain
  ^
  |
Controller
  |
  v
JSP View
```

Servlets will act as controllers.

JSP will be used for presentation.

Business logic should remain outside JSP.

---

# 27. REST Approach

The REST API will expose business operations rather than directly exposing database operations.

For example:

```text
POST   /api/incidents
GET    /api/incidents/{id}
PUT    /api/incidents/{id}
GET    /api/resources
POST   /api/dispatches
GET    /api/dispatches/{id}
GET    /api/reports/...
```

Exact endpoint design will be defined in the API & Web Design document.

---

# 28. Spring Evolution

Spring will not be introduced at the beginning.

The initial architecture will deliberately implement important responsibilities manually.

Later, Spring will be introduced to understand what it solves.

Examples include:

* dependency injection
* MVC request handling
* configuration
* transaction management
* REST support
* data access abstraction

The project should make the comparison meaningful because we will already understand the underlying mechanisms.

---

# 29. Package Organization

The initial package structure should reflect responsibilities rather than arbitrary technical layers.

A possible starting point is:

```text
com.resqgrid
├── domain
├── service
├── repository
├── controller
├── dispatch
├── persistence
├── exception
└── config
```

This is a starting point, not a rigid requirement.

Packages will be created when the corresponding implementation responsibility actually appears.

We should avoid creating dozens of empty packages at the beginning.

---

# 30. Architecture Principles

The following principles apply throughout implementation.

## Principle 1 — Keep Controllers Thin

Controllers coordinate HTTP interaction.

They should not contain core business rules.

---

## Principle 2 — Keep Services Focused

Services coordinate business workflows.

They should not become containers for every unrelated operation.

---

## Principle 3 — Keep Domain Objects Meaningful

Domain objects should represent real business concepts and protect important state.

---

## Principle 4 — Keep Repositories About Persistence

Repositories retrieve and persist data.

They should not decide business policy.

---

## Principle 5 — Keep Infrastructure Replaceable Where Reasonable

Database and framework details should not leak unnecessarily into every part of the application.

---

## Principle 6 — Business Rules Must Have One Clear Home

A rule should not be duplicated across:

* controller
* service
* repository
* JSP

unless different layers are enforcing different aspects of the same constraint.

---

# 31. Architecture Trade-Offs

This architecture intentionally accepts some additional structure in exchange for:

* easier testing
* clearer responsibilities
* better separation
* easier technology evolution
* stronger understanding of backend architecture

At the same time, we will avoid unnecessary enterprise complexity.

ResQGrid is a learning project, but it should still resemble a well-designed backend application.

---

# 32. Architecture Evolution

The architecture will evolve through several stages.

### Stage 1

```text
Java
  |
  v
In-Memory Domain
```

Focus:

* OOP
* collections
* business rules
* dispatch logic

---

### Stage 2

```text
Java
  |
  v
JDBC
  |
  v
PostgreSQL
```

Focus:

* SQL
* JDBC
* transactions
* persistence

---

### Stage 3

```text
Java
  |
  v
JPA / Hibernate
  |
  v
PostgreSQL
```

Focus:

* ORM
* relationships
* persistence context
* entity lifecycle

---

### Stage 4

```text
Servlets
   |
   v
MVC / JSP
   |
   v
Services
   |
   v
Persistence
```

Focus:

* web architecture
* MVC
* request/response handling

---

### Stage 5

```text
REST API
   |
   v
Services
   |
   v
Persistence
```

Focus:

* API design
* HTTP semantics
* JSON
* REST principles

---

### Stage 6

```text
Spring
   |
   v
Spring MVC / REST
   |
   v
Spring Data JPA
   |
   v
PostgreSQL
```

Focus:

* dependency injection
* framework architecture
* comparing manual architecture with Spring

---

# 33. What This Architecture Does Not Promise

The architecture does not attempt to provide:

* microservices
* distributed transactions
* event-driven infrastructure
* horizontal scaling
* cloud-native deployment
* real-time GPS infrastructure

Those concerns are outside the initial scope.

The architecture should remain strong without pretending ResQGrid is a massive production platform.

---

# 34. Architecture Success Criteria

The architecture is successful if:

1. Controllers remain focused on external interaction.
2. Services coordinate business workflows.
3. Domain objects represent meaningful business concepts.
4. Dispatch logic remains independently testable.
5. Persistence code is isolated.
6. Database operations are transactional where required.
7. Concurrent dispatch cannot corrupt resource state.
8. The system can move from JDBC to JPA without rewriting the entire business layer.
9. Spring can later be introduced without losing understanding of the underlying architecture.
10. The code remains understandable to another developer.

---

# 35. Document Status

**Document 06 — System Architecture**

Status: Completed as the initial architectural baseline.

This document will guide package structure, class responsibilities, dependency direction, transaction boundaries, persistence implementation, web development, REST development, and later Spring migration.