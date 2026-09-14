# ResQGrid — Requirements Specification

## 1. Purpose

This document defines the functional and non-functional requirements for the ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine.

It serves as the authoritative requirements reference for implementation.

All domain classes, business logic, database structures, APIs, services, repositories, controllers, and tests must be traceable to requirements defined in this documentation.

If a requirement changes during development, this document must be updated before implementation proceeds.

---

## 2. Functional Requirements

### FR-01 — Incident Management

The system shall allow authorized users or system actors to:

* Create an emergency incident.
* Record incident information.
* Associate an incident with a location.
* Assign an incident severity.
* Track the incident status.
* Update incident information when permitted.
* Retrieve incident details.
* Retrieve incident history.
* Mark an incident as resolved when the response process is completed.

The system shall support the following initial incident severity levels:

* CRITICAL
* HIGH
* MEDIUM
* LOW

The system shall support an incident lifecycle defined by the business rules document.

---

### FR-02 — Incident Classification

The system shall classify an incident according to its operational requirements.

Incident classification may be used by the dispatch engine to determine:

* Required resource type.
* Required capabilities.
* Required response priority.
* Other documented operational constraints.

The exact classification model and values shall be defined in the Domain Model and Business Rules documents.

---

### FR-03 — Emergency Resource Management

The system shall maintain emergency resources.

A resource shall have information necessary for the dispatch engine to determine whether it can participate in an incident response.

The system shall support:

* Resource registration.
* Resource retrieval.
* Resource status tracking.
* Resource availability determination.
* Resource type identification.
* Resource capability information.
* Resource operational state updates.

Initial resource types include:

* Ambulance
* Fire Unit
* Rescue Team

The exact resource attributes shall be defined in the Domain Model and Database Design documents.

---

### FR-04 — Resource Status Management

The system shall track the operational status of emergency resources.

Initial resource statuses are:

* AVAILABLE
* BUSY
* OFFLINE
* MAINTENANCE

The system shall prevent resources that are not operationally available from being selected for normal dispatch.

Status transitions shall follow the business rules defined for resource management.

---

### FR-05 — Response Team Management

The system shall support response teams associated with emergency resources where required by the operational model.

The system shall maintain information necessary to determine:

* Team availability.
* Team capability.
* Team assignment.
* Team participation in dispatch operations.

The exact team structure shall be defined in the Domain Model document.

---

### FR-06 — Resource Capability Management

The system shall support capability-based resource selection.

A resource shall be considered suitable only when it satisfies the capabilities required by an incident, where such requirements exist.

Capability matching shall be part of the dispatch decision process.

The exact capability model shall be defined in the Domain Model and Business Rules documents.

---

### FR-07 — Resource Selection

The system shall provide a dispatch engine capable of selecting an appropriate emergency resource for an incident.

The system shall NOT simply select the first available resource.

Resource selection shall consider documented factors including:

* Incident severity.
* Resource availability.
* Resource type.
* Required capabilities.
* Distance.
* Resource workload.
* Team availability.
* Existing assignments.
* Operational constraints.
* Expected response suitability.

The exact ranking and selection algorithm shall be defined in the Dispatch Engine Design and Business Rules documents.

---

### FR-08 — Resource Filtering

Before ranking resources, the dispatch engine shall eliminate resources that cannot satisfy mandatory operational requirements.

Filtering may consider:

* Resource status.
* Resource type.
* Required capabilities.
* Team availability.
* Operational restrictions.
* Existing assignments.

Only eligible resources shall proceed to the ranking stage.

---

### FR-09 — Resource Ranking

Eligible resources shall be ranked according to documented dispatch criteria.

The ranking mechanism shall produce a deterministic and explainable selection result where possible.

The system should make it possible to understand why a resource was preferred over another eligible resource.

The exact scoring, weighting, tie-breaking, and ranking rules shall be defined in the Dispatch Engine Design document.

---

### FR-10 — Dispatch Creation

The system shall create a dispatch when an appropriate resource is selected for an incident.

A dispatch shall associate the relevant:

* Incident.
* Resource.
* Response team where applicable.
* Dispatch information.
* Dispatch status.
* Relevant timestamps.

The exact dispatch structure shall be defined in the Domain Model and Database Design documents.

---

### FR-11 — Dispatch Lifecycle

The system shall track the lifecycle of a dispatch.

The dispatch process shall support operational state transitions.

The initial conceptual workflow is:

1. Incident reported.
2. Incident assessed.
3. Requirements determined.
4. Suitable resources identified.
5. Resources filtered.
6. Resources ranked.
7. Resource/team selected.
8. Dispatch created.
9. Response in progress.
10. Incident resolved.

The exact statuses and allowed transitions shall be defined in the Business Rules document.

---

### FR-12 — Concurrent Dispatch Requests

The system shall support multiple incidents being processed concurrently.

The dispatch engine shall prevent the same resource from being successfully assigned to incompatible concurrent dispatch operations when the resource is only available for one assignment.

The system shall maintain consistent resource and dispatch state under concurrent operations.

Concurrency requirements shall be formally defined in the Concurrency Design document.

---

### FR-13 — Race Condition Prevention

The system shall prevent race conditions involving shared emergency resources.

Example scenario:

Two incidents simultaneously request the same available ambulance.

The system must ensure that both operations cannot independently conclude that the same single-use resource has been successfully assigned.

The implementation mechanism shall be defined in the Concurrency Design document.

---

### FR-14 — Location Management

The system shall maintain location information required for emergency incidents and resources.

Location information shall support dispatch decisions involving geographic distance.

The exact location representation and distance calculation strategy shall be defined in the Domain Model, Database Design, and Dispatch Engine Design documents.

---

### FR-15 — Dispatch History

The system shall maintain historical information about dispatch operations.

History shall allow the system to determine relevant information such as:

* Which resource was assigned.
* Which incident received the resource.
* Dispatch status.
* Relevant timestamps.
* Assignment and completion information.

The exact history model shall be defined in the Domain Model and Database Design documents.

---

### FR-16 — Resource Assignment Tracking

The system shall maintain sufficient information to determine:

* Current resource availability.
* Current assignments.
* Previous assignments.
* Dispatch completion.
* Resource workload.

This information shall support both operational dispatching and reporting.

---

### FR-17 — Reporting

The system shall provide reporting capabilities based on stored operational data.

Initial reporting requirements may include:

* Incident statistics.
* Resource utilization.
* Dispatch statistics.
* Response activity.
* Resource workload.
* Historical dispatch information.

Exact reports and calculations shall be defined in the Business Rules and future Reporting requirements.

---

### FR-18 — REST API

The system shall expose REST APIs for appropriate application operations.

The API layer shall provide structured request and response representations.

The API shall support appropriate:

* HTTP methods.
* HTTP status codes.
* Request validation.
* Response structures.
* Error responses.

The exact API endpoints and contracts shall be defined in the API Design document.

---

### FR-19 — Web Application

The system shall provide a web interface using the project's planned server-side web technology.

The initial architecture shall support:

* Servlet-based controllers.
* JSP-based views.
* MVC principles.

The web interface shall consume the application/business layer rather than implementing core dispatch logic directly.

---

### FR-20 — Persistence

The system shall persist operational data in PostgreSQL.

SQL shall be used as the database language.

The project shall demonstrate database access using:

* JDBC.
* JPA.
* Hibernate.

The exact progression and responsibilities of each persistence technology shall be defined in the Architecture and Development Roadmap documents.

---

### FR-21 — Data Retrieval

The system shall support retrieving persisted domain information through an appropriate repository/data-access layer.

Data access responsibilities shall remain separated from business logic.

The application shall not place database-specific operations directly inside domain objects or presentation components.

---

### FR-22 — Validation

The system shall validate incoming data and business operations.

Validation shall cover requirements such as:

* Required information.
* Valid domain values.
* Valid state transitions.
* Resource availability.
* Dispatch eligibility.
* Operational constraints.

Exact validation rules shall be defined in the Business Rules and Error Handling documents.

---

### FR-23 — Error Handling

The system shall provide controlled handling of invalid operations and application errors.

Errors shall be represented consistently at the appropriate application boundaries.

The system shall distinguish between:

* Invalid input.
* Invalid business operations.
* Resource conflicts.
* Persistence/data-access failures.
* Unexpected application failures.

The exact error model shall be defined in the Error Handling Design document.

---

### FR-24 — Automated Testing

The system shall include automated tests using JUnit 5.

Testing shall cover appropriate levels of the application, including:

* Domain behavior.
* Business rules.
* Dispatch selection.
* Resource eligibility.
* Concurrency-sensitive behavior.
* Persistence-related behavior where applicable.
* API behavior where applicable.

The exact testing strategy shall be defined in the Testing Strategy document.

---

## 3. Non-Functional Requirements

### NFR-01 — Maintainability

The system shall use clear separation of responsibilities.

Business logic shall not be unnecessarily mixed with:

* Database access.
* HTTP handling.
* JSP presentation.
* Infrastructure concerns.

---

### NFR-02 — Object-Oriented Design

The implementation shall demonstrate appropriate object-oriented principles including:

* Encapsulation.
* Abstraction.
* Inheritance where justified.
* Polymorphism where justified.
* Composition.
* Separation of responsibilities.

Object-oriented design decisions must be justified by the project requirements rather than added only to demonstrate a language feature.

---

### NFR-03 — Thread Safety

Operations involving shared resources shall be designed to remain consistent when executed concurrently.

The implementation shall explicitly address:

* Shared state.
* Atomicity.
* Race conditions.
* Resource locking or equivalent concurrency control.
* Transaction boundaries where applicable.

---

### NFR-04 — Data Consistency

The system shall maintain consistent domain and database state.

Operations that modify related data shall use appropriate transaction boundaries.

Database constraints and application-level validation shall complement each other.

---

### NFR-05 — Reliability

The system shall fail in a controlled manner when an operation cannot be completed.

An unsuccessful dispatch operation must not leave a resource or incident in an invalid intermediate state.

---

### NFR-06 — Performance

The system shall avoid unnecessary database operations and inefficient data-access patterns.

The implementation shall consider:

* Query efficiency.
* Appropriate indexing.
* Fetching strategy.
* N+1 query problems.
* Resource-selection performance.
* Transaction scope.

Performance optimization shall not be introduced without an identifiable requirement or measured problem.

---

### NFR-07 — Security

The application shall be designed with security considerations appropriate to a backend application.

Security requirements shall include appropriate protection of application operations and validation of externally supplied data.

Detailed authentication and authorization requirements shall be defined separately if required by the project scope.

---

### NFR-08 — Testability

Application components shall be designed so that business behavior can be tested independently where practical.

Dependencies and responsibilities should support isolated unit testing.

---

### NFR-09 — API Consistency

REST APIs shall follow consistent conventions for:

* Resource naming.
* HTTP methods.
* Status codes.
* Request validation.
* Response structures.
* Error responses.

---

### NFR-10 — Documentation Traceability

Every significant implementation decision shall be traceable to one or more project documents.

Undocumented fields, methods, classes, database columns, endpoints, or business rules shall not be introduced arbitrarily.

---

## 4. Technology Requirements

The initial project shall use:

| Area               | Technology      |
| ------------------ | --------------- |
| Language           | Java 21         |
| Build Tool         | Maven           |
| Database           | PostgreSQL      |
| Database Language  | SQL             |
| Database Access    | JDBC            |
| ORM                | JPA / Hibernate |
| Web                | Servlets / JSP  |
| API                | REST            |
| Data Format        | JSON            |
| Testing            | JUnit 5         |
| Application Server | Tomcat          |
| Version Control    | Git / GitHub    |

Spring and Spring Boot shall be studied later and shall not replace the initial architecture unless the documentation is explicitly updated.

---

## 5. Project Constraints

The initial implementation shall NOT require:

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
* Other complex frontend frameworks.

These technologies may be considered later only when a documented requirement justifies their introduction.

---

## 6. Development Constraints

The project shall follow this development process:

1. Define requirements.
2. Understand the requirement.
3. Design the domain responsibility.
4. Define required fields.
5. Define required methods.
6. Define method behavior.
7. Implement.
8. Review implementation.
9. Refactor where required.
10. Test.
11. Commit.

Implementation shall not precede the relevant design/documentation decision.

---

## 7. Requirement Traceability

The following documents provide detailed specifications for the requirements defined here:

* `01-PROJECT-OVERVIEW.md` — Project purpose and scope.
* `03-ACTORS-AND-USE-CASES.md` — Actors and system interactions.
* `04-BUSINESS-RULES.md` — Domain and operational rules.
* `05-DOMAIN-MODEL.md` — Domain objects and relationships.
* `06-SYSTEM-ARCHITECTURE.md` — Application architecture and responsibilities.
* `07-DATABASE-DESIGN.md` — PostgreSQL schema and persistence design.
* `08-DISPATCH-ENGINE-DESIGN.md` — Resource selection and dispatch algorithm.
* `09-CONCURRENCY-DESIGN.md` — Concurrency and race-condition strategy.
* `10-API-DESIGN.md` — REST API contracts.
* `11-ERROR-HANDLING-DESIGN.md` — Error and exception handling.
* `12-TESTING-STRATEGY.md` — Testing approach and coverage.
* `13-DEVELOPMENT-ROADMAP.md` — Implementation sequence.

These documents must remain consistent with this Requirements Specification.

---

## 8. Requirement Change Policy

If a new requirement is discovered during implementation:

1. Stop implementation of the affected functionality.
2. Identify which requirement or design document is affected.
3. Update the relevant documentation.
4. Review whether other documents are affected.
5. Resolve any documentation conflicts.
6. Only then continue implementation.

The documentation is the source of truth for the project.
