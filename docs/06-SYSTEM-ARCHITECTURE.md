# ResQGrid — System Architecture

## 1. Purpose

This document defines the high-level architecture of ResQGrid and establishes the responsibilities and communication boundaries between major application layers.

The architecture must support:

* Emergency incident management.
* Resource management.
* Response-team management.
* Multi-criteria resource dispatch.
* Concurrent dispatch operations.
* PostgreSQL persistence.
* JDBC.
* JPA/Hibernate.
* REST APIs.
* Servlet/JSP MVC.
* Automated testing.
* Maintainable object-oriented design.

This document defines architectural responsibilities but does not define individual Java implementation classes unless explicitly stated.

---

# 2. Architectural Principles

ResQGrid shall follow these principles:

### AP-01 — Separation of Responsibilities

Each architectural layer must have a clear responsibility.

### AP-02 — Business Logic Independence

Core business logic must not depend directly on presentation technology.

### AP-03 — Persistence Separation

Business logic must not directly manage low-level database operations.

### AP-04 — Dependency Direction

Higher-level application logic should depend on abstractions/responsibilities rather than unnecessary infrastructure details.

### AP-05 — Single Responsibility

A component should have a focused responsibility.

### AP-06 — Testability

Business logic must be structured so that important behavior can be tested independently.

### AP-07 — Documentation First

Implementation must follow the documented architecture.

---

# 3. High-Level Architecture

The initial architecture is:

```text
Client
  |
  +-----------------------+
  |                       |
REST API              Web Application
  |                       |
REST Controller       Servlet Controller
  |                       |
  +-----------+-----------+
              |
       Application / Service Layer
              |
      +-------+--------+
      |                |
Dispatch Engine   Reporting Engine
      |
Business Rules / Domain
      |
Repository / Data Access
      |
+-----+----------------+
|                      |
JDBC               JPA / Hibernate
|                      |
+----------+-----------+
           |
       PostgreSQL
```

The exact package structure and class names will be defined during implementation design.

---

# 4. Architectural Layers

## 4.1 Presentation Layer

The Presentation Layer is responsible for interaction with users or external clients.

Initial presentation mechanisms:

* REST API.
* Servlet/JSP web application.

Responsibilities include:

* Receiving user/client requests.
* Presenting responses.
* Request/response formatting.
* Delegating operations to appropriate application components.

The presentation layer must not contain core dispatch business rules.

---

# 5. REST API Layer

The REST API provides HTTP-based access to application functionality.

Responsibilities include:

* Receiving HTTP requests.
* Validating request structure.
* Translating external requests into application operations.
* Calling appropriate application services.
* Translating application results into HTTP responses.
* Returning appropriate HTTP status codes.
* Returning structured JSON responses.

The REST layer must not implement the dispatch algorithm itself.

---

# 6. Servlet/JSP MVC Layer

The web application shall use the Servlet/JSP MVC approach.

### Servlet Controllers

Servlets act as controllers.

Responsibilities include:

* Receiving web requests.
* Extracting request information.
* Calling application services.
* Preparing model information for views.
* Selecting the appropriate JSP view.

### JSP

JSP is responsible for presentation.

JSP pages must not contain core business logic such as:

* Resource ranking.
* Dispatch decisions.
* Concurrency handling.
* Database operations.

---

# 7. Controller Responsibility

Controllers are responsible for coordinating incoming requests.

A controller may:

1. Receive a request.
2. Validate basic request structure.
3. Call an application/service operation.
4. Receive the result.
5. Translate the result into the appropriate external response.

A controller must not:

* Directly execute complex SQL.
* Implement dispatch-ranking logic.
* Manage concurrency mechanisms.
* Directly manipulate database transactions without the persistence/application architecture requiring it.

---

# 8. Application / Service Layer

The Application/Service Layer coordinates use cases.

It acts as the boundary between controllers and business/domain operations.

Responsibilities include:

* Coordinating application workflows.
* Enforcing applicable business operations.
* Calling repositories.
* Coordinating domain objects.
* Managing transaction-oriented application operations where appropriate.
* Calling the dispatch engine.
* Coordinating reporting operations.

The service layer must not become a dumping ground for unrelated responsibilities.

---

# 9. Dispatch Engine

The Dispatch Engine is a core business component of ResQGrid.

Its responsibility is to determine the most appropriate eligible resource for an incident.

The dispatch engine shall conceptually perform:

```text
Incident Requirements
        |
Candidate Identification
        |
Eligibility Filtering
        |
Ranking
        |
Final Assignment Verification
        |
Selection
```

The engine must consider documented factors such as:

* Incident severity.
* Resource type.
* Resource capabilities.
* Availability.
* Distance.
* Workload.
* Existing assignments.
* Team availability.
* Operational constraints.

The exact algorithm is defined separately in:

`08-DISPATCH-ENGINE-DESIGN.md`

---

# 10. Reporting Engine

The Reporting Engine is responsible for generating operational information from persisted system data.

It may provide:

* Incident statistics.
* Dispatch statistics.
* Resource utilization.
* Resource workload.
* Historical dispatch information.

Reporting logic must remain separate from core dispatch-selection logic.

---

# 11. Domain Layer

The Domain Layer represents the business concepts defined in the Domain Model.

Initial domain concepts include:

* Incident.
* Emergency Resource.
* Response Team.
* Dispatch.
* Location.
* Resource Capability.

The domain layer represents business state and relevant domain behavior.

Domain objects must not depend on:

* Servlet APIs.
* JSP.
* HTTP-specific concerns.
* Database connection management.

---

# 12. Repository / Data Access Layer

The Repository/Data Access Layer is responsible for communicating with persistent storage.

Responsibilities include:

* Persisting domain data.
* Retrieving domain data.
* Updating persistent state.
* Executing appropriate queries.
* Translating persistence results into domain/application representations.

Repositories must not contain high-level dispatch decisions.

---

# 13. JDBC Persistence

JDBC will be used to demonstrate direct relational database access.

JDBC responsibilities may include:

* Establishing database interaction.
* Executing SQL statements.
* Binding parameters.
* Reading query results.
* Managing appropriate JDBC resources.

JDBC must remain behind the data-access boundary.

Business services must not contain scattered JDBC operations.

---

# 14. JPA / Hibernate Persistence

JPA and Hibernate will be used to demonstrate ORM-based persistence.

JPA defines the persistence programming model.

Hibernate provides the ORM implementation.

The project will use this stage to demonstrate concepts including:

* Entity mapping.
* Relationships.
* Persistence context.
* Entity lifecycle.
* Dirty checking.
* First-level cache.
* Fetching strategies.
* Querying.
* Transactions.

Exact entity mappings and ORM decisions belong to the Database Design and implementation stages.

---

# 15. PostgreSQL

PostgreSQL is the primary persistent relational database.

All persistent operational data must ultimately be stored in PostgreSQL.

SQL remains the database language.

NoSQL databases are outside the initial project scope.

---

# 16. Transaction Boundary

Transactions shall be used when a business operation requires multiple related persistent changes to succeed or fail together.

A key example is dispatch assignment:

```text
Verify Resource
      |
Create Dispatch
      |
Update Resource State
      |
Commit
```

These operations must not leave the system in an inconsistent state.

The exact transaction mechanism and concurrency interaction are defined in:

`09-CONCURRENCY-DESIGN.md`

and refined in the implementation architecture.

---

# 17. Concurrency Boundary

Concurrency-sensitive operations must be protected at the point where shared resource state is changed.

The most important concurrency-sensitive operation is final resource assignment.

The architecture must prevent:

```text
Incident A ----\
                >---- Same Resource ---- Invalid Double Assignment
Incident B ----/
```

The system must instead ensure that only valid assignment operations succeed.

The exact mechanism is defined in:

`09-CONCURRENCY-DESIGN.md`

---

# 18. Request Flow — Incident Creation

The conceptual request flow is:

```text
Client
  |
Controller
  |
Application Service
  |
Incident Domain
  |
Repository
  |
PostgreSQL
```

The controller does not directly perform database operations.

The service coordinates the operation.

The repository handles persistence.

---

# 19. Request Flow — Resource Dispatch

The conceptual dispatch flow is:

```text
Client
  |
Controller
  |
Dispatch Application Service
  |
Dispatch Engine
  |
Candidate Resources
  |
Eligibility Filtering
  |
Ranking
  |
Final Assignment Verification
  |
Transaction / Concurrency Control
  |
Dispatch + Resource State Update
  |
Repository
  |
PostgreSQL
```

This flow is one of the most important architectural workflows in ResQGrid.

---

# 20. Request Flow — Reporting

The conceptual reporting flow is:

```text
Client
  |
Controller
  |
Reporting Service
  |
Reporting Engine
  |
Repository
  |
PostgreSQL
```

The reporting engine interprets persisted operational data and produces the requested report.

---

# 21. Validation Architecture

Validation exists at multiple levels.

### Request Validation

Ensures incoming external data has an acceptable structure.

### Domain Validation

Ensures domain state is valid.

### Business Validation

Ensures an operation follows documented business rules.

### Persistence Validation

Database constraints protect persistent data integrity.

Validation must occur at the appropriate boundary rather than being duplicated unnecessarily across every layer.

---

# 22. Error Handling Architecture

Errors should be handled according to their source.

Possible categories include:

* Invalid request.
* Validation failure.
* Business-rule violation.
* Resource unavailable.
* Concurrent assignment conflict.
* Resource not found.
* Incident not found.
* Persistence failure.
* Unexpected application failure.

Controllers/API boundaries are responsible for translating application failures into appropriate external responses.

The exact exception hierarchy and response model belong to:

`11-ERROR-HANDLING-DESIGN.md`

---

# 23. Dependency Rules

The following dependency rules apply.

### DR-01

Presentation components may depend on application/service components.

### DR-02

Application/service components may depend on domain and required application abstractions.

### DR-03

Repositories provide data-access responsibilities to the application layer.

### DR-04

Domain objects must not depend on controllers.

### DR-05

Domain objects must not depend on JSP.

### DR-06

Domain objects must not directly manage database connections.

### DR-07

Controllers must not bypass the application/service layer to directly execute business operations.

### DR-08

Dispatch ranking must not be implemented inside controllers or JSP pages.

---

# 24. Domain and Persistence Separation

The project must clearly distinguish between:

**Domain responsibility**

and

**Persistence responsibility**.

The domain model describes business concepts.

JPA/Hibernate mappings describe how those concepts are persisted.

Persistence-specific concerns must not unnecessarily leak into business decision-making.

The exact degree of separation will be documented during database and implementation design.

---

# 25. JDBC and JPA/Hibernate Learning Strategy

The project is intended to teach both direct SQL persistence and ORM persistence.

The development roadmap shall determine the exact sequence.

The learning progression should allow understanding of:

```text
SQL
  ↓
JDBC
  ↓
Repository/Data Access
  ↓
JPA Concepts
  ↓
Hibernate Implementation
  ↓
ORM Relationships
  ↓
Persistence Context
  ↓
Transactions
  ↓
Performance / Fetching
```

Spring Data JPA will be studied later rather than being used to hide these concepts during the initial implementation.

---

# 26. Testing Architecture

Testing shall exist at appropriate architectural levels.

Examples include:

* Domain tests.
* Business-service tests.
* Dispatch-engine tests.
* Repository tests.
* Integration tests.
* REST/API tests.
* Concurrency tests.

Tests must verify behavior defined by the documentation rather than implementation details that have no business significance.

The complete testing strategy is defined in:

`12-TESTING-STRATEGY.md`

---

# 27. Configuration and Infrastructure

Infrastructure concerns may include:

* Database configuration.
* JDBC configuration.
* Hibernate configuration.
* Servlet container configuration.
* Application configuration.
* Logging configuration.

Infrastructure configuration must remain separate from core business rules.

Sensitive configuration such as credentials must not be committed to Git.

---

# 28. Technology Constraints

The initial architecture uses:

* Java 21.
* Maven.
* PostgreSQL.
* SQL.
* JDBC.
* JPA.
* Hibernate.
* Servlets.
* JSP.
* REST.
* JSON.
* JUnit 5.
* Tomcat.
* Git/GitHub.

The initial architecture does not require:

* Microservices.
* Kafka.
* Redis.
* Kubernetes.
* Elasticsearch.
* GraphQL.
* NoSQL.
* React.
* Angular.
* Cloud infrastructure.

These may only be introduced if project documentation is explicitly changed.

---

# 29. Architecture Decision Boundaries

This document intentionally does not finalize:

* Exact Java packages.
* Exact class names.
* Exact interfaces.
* Exact repository implementation structure.
* Exact entity mappings.
* Exact SQL schema.
* Exact REST endpoints.
* Exact exception classes.
* Exact transaction annotations/configuration.
* Exact concurrency mechanism.
* Exact dispatch scoring formula.

Those decisions belong to their respective design documents.

---

# 30. Architecture Quality Requirements

The architecture should make it possible to:

* Understand where a responsibility belongs.
* Test business logic independently.
* Replace or compare persistence approaches.
* Prevent controllers from becoming business-logic containers.
* Prevent repositories from becoming business-rule containers.
* Isolate concurrency-sensitive operations.
* Maintain clear domain boundaries.
* Extend functionality without unnecessary architectural coupling.

---

# 31. Architecture Decision Rule

Before creating a new class or package, the development process must answer:

1. Which architectural responsibility does it serve?
2. Which documented requirement requires it?
3. Which layer owns that responsibility?
4. What other components should it depend on?
5. What components should depend on it?
6. Is an existing component already responsible for this behavior?

If these questions cannot be answered from the documentation, implementation must stop and the relevant documentation must be reviewed.

---

# 32. Documentation Consistency

This document must remain consistent with:

* `01-PROJECT-OVERVIEW.md`
* `02-REQUIREMENTS-SPECIFICATION.md`
* `03-ACTORS-AND-USE-CASES.md`
* `04-BUSINESS-RULES.md`
* `05-DOMAIN-MODEL.md`
* `07-DATABASE-DESIGN.md`
* `08-DISPATCH-ENGINE-DESIGN.md`
* `09-CONCURRENCY-DESIGN.md`
* `10-API-DESIGN.md`
* `11-ERROR-HANDLING-DESIGN.md`
* `12-TESTING-STRATEGY.md`
* `13-DEVELOPMENT-ROADMAP.md`

If a conflict is discovered, implementation must stop.

The conflicting documentation must be reviewed and updated before coding continues.

The documentation remains the source of truth for the project.
