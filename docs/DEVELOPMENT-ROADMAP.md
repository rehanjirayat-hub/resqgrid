# ResQGrid — Development Roadmap

## 1. Purpose

This document defines the implementation roadmap for ResQGrid.

It establishes the order in which the system will be designed, implemented, tested, reviewed, and integrated.

The roadmap exists to prevent:

* Random implementation.
* Premature use of frameworks.
* Undocumented classes or fields.
* Mixing multiple learning topics unnecessarily.
* Building business logic before the domain is understood.
* Building APIs before the business layer is stable.
* Introducing concurrency before the normal dispatch flow works.

This roadmap must be followed throughout the project.

---

# 2. Development Philosophy

ResQGrid will be developed incrementally.

The implementation sequence is:

```text
Documentation
     |
     v
Project Foundation
     |
     v
Domain Model
     |
     v
Database Design Implementation
     |
     v
JDBC
     |
     v
JPA / Hibernate
     |
     v
Business Layer
     |
     v
Dispatch Engine
     |
     v
Concurrency
     |
     v
REST API
     |
     v
Servlet / JSP MVC
     |
     v
Testing
     |
     v
Final Validation
```

Each phase must produce a working and understandable result before the next major phase begins.

---

# 3. Source of Truth

The documentation directory is the project contract.

The following documents must guide implementation:

```text
01-PROJECT-OVERVIEW.md
02-REQUIREMENTS-SPECIFICATION.md
03-ACTORS-AND-USE-CASES.md
04-BUSINESS-RULES.md
05-DOMAIN-MODEL.md
06-SYSTEM-ARCHITECTURE.md
07-DATABASE-DESIGN.md
08-DISPATCH-ENGINE-DESIGN.md
09-CONCURRENCY-DESIGN.md
10-API-DESIGN.md
11-ERROR-HANDLING-DESIGN.md
12-TESTING-STRATEGY.md
13-DEVELOPMENT-ROADMAP.md
```

Implementation must not intentionally deviate from these documents.

If implementation reveals that a requirement or design is incomplete:

1. Stop implementation of the affected area.
2. Identify the affected document.
3. Review the requirement.
4. Update the documentation.
5. Check dependent documents for consistency.
6. Resume implementation only after the documentation is consistent.

---

# 4. Phase 0 — Documentation Completion

## Objective

Complete the entire project design before substantial implementation.

## Status

Completed.

All thirteen project documents must exist before implementation proceeds.

## Deliverables

* Project overview.
* Requirements.
* Use cases.
* Business rules.
* Domain model.
* Architecture.
* Database design.
* Dispatch engine design.
* Concurrency design.
* API design.
* Error handling design.
* Testing strategy.
* Development roadmap.

---

# 5. Phase 1 — Project Foundation

## Objective

Establish a clean Java/Maven project foundation.

## Technology

* Java 21.
* Maven.
* Git.
* GitHub.
* IntelliJ IDEA.

## Tasks

1. Verify project structure.
2. Verify Maven configuration.
3. Verify Java 21 configuration.
4. Verify Git configuration.
5. Verify `.gitignore`.
6. Verify GitHub remote.
7. Confirm the project builds successfully.
8. Establish the initial source/test directory structure.

## Learning Focus

* Maven project lifecycle.
* `pom.xml`.
* Dependencies.
* Java project structure.
* Maven standard directory layout.
* Git workflow.

## Completion Criteria

* Project builds successfully.
* Git repository is clean.
* Maven configuration is understood.
* No undocumented application classes have been created.

---

# 6. Phase 2 — Domain Model

## Objective

Transform the conceptual domain model into Java domain objects.

## Main Concepts

* Incident.
* Emergency Resource.
* Response Team.
* Dispatch.
* Location.
* Resource Capability.

## Tasks

1. Review the Domain Model document.
2. Decide the responsibility of each domain class.
3. Determine fields strictly from documented requirements.
4. Determine relationships.
5. Determine controlled values.
6. Define domain invariants.
7. Define state transitions.
8. Implement domain behavior.
9. Unit test domain behavior.

## Learning Focus

* OOP.
* Encapsulation.
* Composition.
* Relationships.
* Enums.
* Domain invariants.
* Object responsibility.
* Immutability where appropriate.
* Collections.
* Lambda/Streams only where they improve readability.

## Important Rule

No persistence annotations should be introduced merely because the domain classes exist.

The domain must first be understood independently.

## Completion Criteria

* Domain classes represent documented concepts.
* Responsibilities are clear.
* Important invariants are protected.
* Domain unit tests pass.
* No undocumented fields or methods exist.

---

# 7. Phase 3 — PostgreSQL Database Foundation

## Objective

Create the relational database structure described by the Database Design document.

## Technology

* PostgreSQL.
* SQL.

## Tasks

1. Install/verify PostgreSQL.
2. Create the ResQGrid database.
3. Design the actual schema from the documented database design.
4. Create tables.
5. Create primary keys.
6. Create foreign keys.
7. Create required constraints.
8. Create controlled-value enforcement where documented.
9. Create required indexes.
10. Insert controlled development/test data where necessary.
11. Verify relationships using SQL.

## Learning Focus

* Relational modeling.
* Primary keys.
* Foreign keys.
* Constraints.
* Joins.
* Indexes.
* Transactions.
* SQL queries.
* Referential integrity.

## Important Rule

The database must not be designed independently from `07-DATABASE-DESIGN.md`.

If the implementation requires a new table, column, relationship, or constraint not documented there, stop and update the documentation first.

## Completion Criteria

* PostgreSQL database exists.
* Schema matches documentation.
* Relationships work correctly.
* Constraints work correctly.
* Required indexes exist.
* SQL verification queries pass.

---

# 8. Phase 4 — JDBC Data Access

## Objective

Implement database access using JDBC before introducing ORM abstraction.

This phase is intentionally included to develop a strong understanding of SQL/database interaction.

## Tasks

1. Establish database connectivity.
2. Configure connection management.
3. Implement required JDBC data-access operations.
4. Execute parameterized SQL.
5. Map SQL results to appropriate application/domain representations.
6. Handle SQL exceptions.
7. Manage resources correctly.
8. Implement transaction handling where required.
9. Test JDBC operations against PostgreSQL.

## Learning Focus

* JDBC API.
* `Connection`.
* `PreparedStatement`.
* `ResultSet`.
* SQL parameter binding.
* Resource management.
* Transactions.
* Commit/rollback.
* SQL-to-object mapping.

## Important Rule

Do not hide JDBC behavior behind unnecessary abstraction before understanding what the abstraction is solving.

## Completion Criteria

* JDBC connection works.
* CRUD/read operations required by the phase work.
* SQL is parameterized.
* Resources are handled correctly.
* Transaction behavior is understood.
* JDBC integration tests pass.

---

# 9. Phase 5 — JPA and Hibernate

## Objective

Introduce ORM after understanding direct JDBC persistence.

## Technology

* JPA.
* Hibernate.

## Tasks

1. Introduce JPA/Hibernate configuration.
2. Map documented domain concepts to persistence entities where appropriate.
3. Define primary keys.
4. Define relationships.
5. Define ownership.
6. Define `JoinColumn` behavior.
7. Define `mappedBy` where applicable.
8. Configure cascade behavior only where justified.
9. Configure orphan removal only where justified.
10. Understand fetch strategies.
11. Implement repository/data-access operations.
12. Verify generated SQL.
13. Test persistence behavior.

## Learning Focus

* Entity lifecycle.
* Persistence context.
* First-level cache.
* Dirty checking.
* Lazy loading.
* Eager loading.
* N+1 queries.
* Relationships.
* Transactions.
* JPQL/HQL.
* Native SQL where justified.

## Important Rule

JPA/Hibernate configuration must follow the documented domain/database relationships.

Annotations must not be added mechanically.

Every relationship decision must be understood.

## Completion Criteria

* Entity mappings work.
* Relationships work.
* Persistence operations work.
* Transactions work.
* Fetch behavior is understood.
* Important ORM behavior has tests.
* No unexplained ORM annotations remain.

---

# 10. Phase 6 — Application and Business Layer

## Objective

Build the application/service layer that coordinates domain behavior and persistence.

## Main Responsibilities

* Incident operations.
* Resource operations.
* Team operations.
* Dispatch operations.
* Validation.
* Business-rule enforcement.
* Transaction coordination.

## Tasks

1. Define service responsibilities.
2. Define service inputs/outputs.
3. Connect domain behavior with persistence.
4. Implement documented business rules.
5. Implement validation.
6. Handle documented business failures.
7. Define transaction boundaries.
8. Unit test business behavior.
9. Integration test important operations.

## Learning Focus

* Separation of concerns.
* Service-layer design.
* Dependency direction.
* Transaction boundaries.
* Business-rule implementation.
* Clean code.
* Exception design.

## Completion Criteria

* Business rules are enforced.
* Services have clear responsibilities.
* Controllers are not required for business logic testing.
* Service tests pass.
* Integration tests pass.

---

# 11. Phase 7 — Dispatch Engine

## Objective

Implement the core ResQGrid decision-making engine.

This is one of the most important phases of the project.

## Process

```text
Incident
   |
Determine Requirements
   |
Identify Candidates
   |
Filter Ineligible Resources
   |
Rank Eligible Resources
   |
Select Best Candidate
   |
Final Assignment Verification
   |
Create Dispatch
```

## Tasks

1. Determine resource requirements.
2. Identify candidate resources.
3. Apply mandatory eligibility filters.
4. Evaluate capabilities.
5. Evaluate resource type.
6. Evaluate team requirements.
7. Evaluate operational availability.
8. Evaluate conflicting assignments.
9. Calculate documented ranking factors.
10. Rank candidates.
11. Apply deterministic tie-breaking.
12. Perform final assignment verification.
13. Create dispatch.
14. Update operational state.
15. Return an appropriate result.

## Learning Focus

* Algorithm design.
* Collections.
* Comparators.
* Streams.
* Lambda expressions.
* Multi-criteria ranking.
* Separation of filtering and optimization.
* Explainable business logic.

## Important Rule

The engine must never become:

```text
find first AVAILABLE resource
```

It must implement the documented eligibility and ranking process.

## Completion Criteria

* Mandatory filters work.
* Ranking works.
* Tie-breaking is deterministic.
* No first-available behavior exists.
* Final verification occurs.
* Dispatch creation works.
* Dispatch Engine tests pass.

---

# 12. Phase 8 — Concurrency and Transaction Safety

## Objective

Guarantee that concurrent dispatch requests cannot produce conflicting resource assignments.

## Primary Scenario

```text
Incident A --------\
                    \
                     Resource X
                    /
Incident B --------/
```

Both transactions attempt to assign Resource X.

## Tasks

1. Select the final concurrency mechanism documented for implementation.
2. Define the exact transaction boundary.
3. Implement final assignment protection.
4. Handle conflicting state changes.
5. Implement rollback behavior.
6. Implement documented retry behavior if required.
7. Test concurrent dispatch attempts.
8. Test transaction consistency.
9. Test failure scenarios.
10. Test behavior against PostgreSQL.

## Learning Focus

* Threads.
* Race conditions.
* Atomicity.
* Transactions.
* Database locking.
* Optimistic/pessimistic concurrency concepts.
* Isolation.
* Rollback.
* Lost updates.
* ORM persistence-context behavior.

## Critical Guarantee

At most one concurrent transaction may successfully create a conflicting active assignment for the same resource.

## Completion Criteria

* Double assignment is prevented.
* Resource state remains consistent.
* Dispatch state remains consistent.
* Rollback works.
* Concurrency tests pass reliably.

---

# 13. Phase 9 — REST API

## Objective

Expose documented application capabilities through REST APIs.

## Technology

* Java HTTP/REST stack selected according to the documented architecture.
* JSON.

## Tasks

1. Implement API controllers.
2. Implement request handling.
3. Implement response models.
4. Implement request validation.
5. Map business results to HTTP responses.
6. Implement error responses.
7. Implement documented endpoints.
8. Test successful requests.
9. Test invalid requests.
10. Test conflict responses.
11. Test not-found responses.

## Learning Focus

* HTTP.
* REST principles.
* JSON.
* Controllers.
* DTOs.
* HTTP status codes.
* API validation.
* API error handling.

## Important Rule

REST controllers must not contain Dispatch Engine logic.

The API delegates to the business/application layer.

## Completion Criteria

* Documented endpoints work.
* JSON responses are consistent.
* HTTP status codes are correct.
* Error handling is consistent.
* REST tests pass.

---

# 14. Phase 10 — Servlet/JSP MVC Web Application

## Objective

Build the server-rendered web interface required by the project architecture.

## Technology

* Servlets.
* JSP.
* MVC.
* HTML.
* CSS.
* Basic JavaScript.
* Tomcat.

## Tasks

1. Establish servlet configuration.
2. Define web controllers.
3. Connect controllers to application services.
4. Create JSP views.
5. Display incidents.
6. Display resources.
7. Display dispatch information.
8. Provide documented operational actions.
9. Display validation and business errors.
10. Test important web flows.

## Learning Focus

* Servlet lifecycle.
* Request/response.
* MVC.
* JSP.
* View rendering.
* Session/request scope where needed.
* Separation between controller and service.

## Important Rule

The JSP layer must not contain core business logic.

## Completion Criteria

* Main documented workflows are accessible through the web interface.
* Controllers delegate correctly.
* Business logic remains in the application layer.
* Error handling is user-friendly.

---

# 15. Phase 11 — Comprehensive Testing

## Objective

Run the complete testing strategy defined in `12-TESTING-STRATEGY.md`.

## Tasks

Run and verify:

* Unit tests.
* Domain tests.
* Business tests.
* JDBC tests.
* JPA/Hibernate tests.
* Repository tests.
* Integration tests.
* REST API tests.
* Error-handling tests.
* Transaction tests.
* Concurrency tests.
* End-to-end validation.

## Completion Criteria

* Required test suites pass.
* No known critical regression exists.
* Critical business rules have automated coverage.
* Concurrency guarantees are verified.

---

# 16. Phase 12 — Final Validation

## Objective

Validate the complete application against the documentation.

## Validation Areas

### Requirements

Verify every functional requirement.

### Business Rules

Verify every important business rule.

### Domain

Verify domain responsibilities and invariants.

### Database

Verify schema and relationships.

### Dispatch Engine

Verify filtering, ranking, and final assignment.

### Concurrency

Verify race-condition protection.

### API

Verify endpoints and error responses.

### Web

Verify important user workflows.

### Testing

Verify all required automated tests.

---

# 17. Documentation Audit

Before declaring the project complete:

1. Review all thirteen documents.
2. Compare documentation with implementation.
3. Identify undocumented behavior.
4. Identify implementation behavior that contradicts documentation.
5. Update documentation where the final design legitimately changed.
6. Confirm all documents remain internally consistent.

The final project must have traceability between:

```text
Requirement
   |
Use Case
   |
Business Rule
   |
Domain
   |
Implementation
   |
Test
```

---

# 18. Code Quality Review

Before final completion, review:

* Naming.
* Encapsulation.
* Class responsibilities.
* Method responsibilities.
* Duplicate logic.
* Exception handling.
* Transaction boundaries.
* SQL quality.
* ORM mappings.
* API consistency.
* Test quality.
* Documentation consistency.

No unnecessary abstraction should remain.

No dead code should remain.

No undocumented feature should remain.

---

# 19. Git Workflow

Development should use small, meaningful commits.

A commit should represent a coherent change.

Examples of commit categories:

```text
Project foundation
Domain model
Database schema
JDBC persistence
JPA mappings
Business services
Dispatch engine
Concurrency protection
REST API
Web MVC
Testing
Final cleanup
```

Before each important commit:

1. Run relevant tests.
2. Review changed files.
3. Check Git diff.
4. Confirm documentation consistency.
5. Commit with a meaningful message.

---

# 20. Learning Workflow

The project is also a structured Java backend learning exercise.

For every implementation task, the development process will be:

```text
1. Read the requirement
        |
2. Identify the relevant documentation
        |
3. Understand the responsibility
        |
4. Decide the class
        |
5. Decide the fields
        |
6. Decide the methods
        |
7. Understand method behavior
        |
8. User implements
        |
9. Review implementation
        |
10. Fix/refactor
        |
11. Test
        |
12. Commit
```

The user is expected to write the implementation.

The mentor role is to explain:

* Why the class exists.
* Why the responsibility belongs there.
* Why a field is required.
* Why a method is required.
* How the method should behave.
* What mistakes to avoid.
* How to test the behavior.

---

# 21. No Blind Coding

Implementation must not begin with large amounts of generated code.

Before a class is implemented, its purpose must be understood.

Before a method is implemented, its behavior must be understood.

Before a relationship is implemented, its ownership and lifecycle must be understood.

Before a query is implemented, the required data and expected result must be understood.

Before a concurrency mechanism is implemented, the race condition and consistency guarantee must be understood.

---

# 22. No Undocumented Classes or Fields

Every important class must have a documented responsibility.

Every important field must have a documented reason to exist.

Every important method must support a documented behavior.

If a new class, field, method, endpoint, table, relationship, or feature becomes necessary:

```text
Stop
  |
Identify affected document
  |
Update documentation
  |
Review dependent documents
  |
Resume implementation
```

---

# 23. Technology Introduction Order

Technologies should be introduced in an intentional learning sequence.

```text
Java
  |
Maven
  |
OOP
  |
PostgreSQL / SQL
  |
JDBC
  |
JPA
  |
Hibernate
  |
Business Architecture
  |
Concurrency
  |
REST
  |
Servlet / JSP
  |
Testing
  |
Later: Spring
```

Spring is intentionally deferred.

---

# 24. Spring Learning Phase — Later

Spring must not be introduced into the initial ResQGrid implementation before the manual architecture has been understood.

After the core project is stable, Spring can be studied to understand what it solves.

Potential comparison areas:

```text
Manual Dependency Management
        vs
Spring Dependency Injection

Manual Web Configuration
        vs
Spring MVC

Manual Application Configuration
        vs
Spring Boot

Manual JDBC/JPA Integration
        vs
Spring Data JPA

Manual Security Configuration
        vs
Spring Security
```

The purpose is understanding rather than blindly replacing the existing architecture.

---

# 25. Deferred Technologies

The following technologies are intentionally outside the initial implementation roadmap:

* Microservices.
* Kafka.
* Redis.
* Kubernetes.
* Elasticsearch.
* Cloud infrastructure.
* GraphQL.
* NoSQL databases.
* React.
* Angular.

They may only be considered later if a documented requirement justifies them.

---

# 26. Definition of Done

ResQGrid is considered complete when:

* All required functionality is implemented.
* Documentation matches implementation.
* Domain rules are enforced.
* Database integrity is maintained.
* JDBC functionality works.
* JPA/Hibernate functionality works.
* Dispatch Engine works according to documented rules.
* Concurrent assignment is protected.
* REST API works.
* Servlet/JSP application works.
* Error handling is consistent.
* Required automated tests pass.
* Critical business rules are tested.
* No known critical regression remains.
* Git history is clean and understandable.

---

# 27. Final Project Quality Goal

The final result should demonstrate that the developer understands not only how to write Java code, but how to build a maintainable backend system.

The project should demonstrate understanding of:

* Object-oriented design.
* Domain modeling.
* SQL and relational databases.
* JDBC.
* JPA/Hibernate.
* Business logic.
* REST APIs.
* MVC.
* Transactions.
* Concurrency.
* Testing.
* Error handling.
* Clean architecture.
* Git/GitHub.
* Engineering trade-offs.

The goal is a project that can be explained technically from requirement to implementation.

---

# 28. Documentation Consistency

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
* `12-TESTING-STRATEGY.md`

If a conflict is discovered, implementation must stop.

The affected documentation must be reviewed and updated before implementation continues.

The documentation remains the source of truth for ResQGrid.
