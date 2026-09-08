# ResQGrid — Requirements Specification

**Document:** Requirements Specification
**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Draft / Development Baseline

---

# 1. Purpose

This document defines the functional and non-functional requirements of the ResQGrid system.

The purpose is to establish a clear and stable understanding of what the system must do before implementation begins.

ResQGrid is a backend-oriented emergency response and resource dispatch system that receives emergency incidents, evaluates their severity, identifies suitable emergency resources, dispatches resources, tracks their operational state, records dispatch history, and provides operational reporting.

The system is designed as a serious Java backend learning project with emphasis on:

* Object-oriented design
* Business logic
* Data persistence
* Database integrity
* Concurrency
* Thread safety
* Transaction management
* REST APIs
* Web interaction
* Testing
* Maintainable architecture

---

# 2. System Scope

ResQGrid covers the lifecycle of an emergency incident from registration through resource assignment and dispatch tracking.

The system must support:

1. Incident registration
2. Incident validation
3. Incident severity classification
4. Incident status management
5. Emergency resource registration
6. Resource availability management
7. Resource capability/type matching
8. Resource suitability evaluation
9. Resource dispatch
10. Concurrent dispatch requests
11. Prevention of double assignment
12. Dispatch status tracking
13. Dispatch history
14. Incident history
15. Operational reporting
16. REST API access
17. Web-based interaction
18. Persistent storage using PostgreSQL
19. Automated testing

---

# 3. Actors

## 3.1 Incident Operator

Responsible for registering and maintaining emergency incidents.

Typical responsibilities:

* Create incidents
* Provide incident location
* Provide incident description
* Provide incident type/category
* Update incident information
* View incident status

---

## 3.2 Dispatcher

Responsible for managing emergency resource allocation.

Typical responsibilities:

* View active incidents
* View available resources
* Initiate dispatch
* Monitor dispatch status
* Track resource assignments
* View dispatch history

---

## 3.3 Operations Manager

Responsible for operational monitoring and reporting.

Typical responsibilities:

* Monitor active incidents
* Monitor resource utilization
* Review dispatch history
* Review operational reports
* Analyze response activity
* Monitor resource availability

---

## 3.4 Dispatch Engine

The Dispatch Engine is a system component rather than a human actor.

It is responsible for automatically evaluating candidate resources and determining the most appropriate resource for an incident according to defined business rules.

---

# 4. Functional Requirements

## FR-001 — Incident Registration

The system shall allow an authorized user to register a new emergency incident.

An incident should contain information such as:

* Incident identifier
* Incident type
* Description
* Location
* Severity
* Status
* Creation timestamp

The system shall validate required information before storing the incident.

---

## FR-002 — Incident Validation

The system shall reject invalid incident data.

Validation should include appropriate checks for:

* Required fields
* Valid location information
* Valid severity
* Valid incident status
* Valid incident type
* Invalid or inconsistent values

Invalid data shall not be persisted.

---

## FR-003 — Incident Severity

The system shall support the following severity levels:

* CRITICAL
* HIGH
* MEDIUM
* LOW

Severity shall influence dispatch priority.

Critical incidents must receive higher dispatch priority than lower-severity incidents.

---

## FR-004 — Incident Status Management

The system shall maintain the lifecycle status of an incident.

The system shall support appropriate states such as:

* REPORTED
* ASSIGNED
* IN_PROGRESS
* RESOLVED
* CANCELLED

Only valid state transitions shall be permitted.

---

## FR-005 — Emergency Resource Registration

The system shall allow emergency resources to be registered.

Examples include:

* Ambulance
* Fire Unit
* Rescue Team

A resource shall contain information necessary for dispatch decisions, such as:

* Resource identifier
* Resource type
* Current status
* Location
* Capabilities
* Operational information

---

## FR-006 — Resource Status Management

The system shall track the operational state of every emergency resource.

Supported resource statuses shall include:

* AVAILABLE
* BUSY
* OFFLINE
* MAINTENANCE

Only appropriate resources may be considered for dispatch.

For example:

* AVAILABLE resources may be dispatched.
* BUSY resources shall not be assigned to another incident.
* OFFLINE resources shall not be assigned.
* MAINTENANCE resources shall not be assigned.

---

## FR-007 — Resource Capability Matching

The system shall evaluate whether a resource is capable of responding to a particular incident.

Resource selection shall not be based solely on availability.

The system shall consider appropriate operational constraints such as:

* Resource type
* Required capability
* Incident severity
* Resource status
* Existing workload
* Location
* Distance
* Operational suitability

---

## FR-008 — Resource Candidate Selection

The system shall identify suitable resources for an incident.

Candidate resources shall be filtered before ranking.

A resource that fails mandatory requirements shall not be considered a valid candidate even if it is nearby.

---

## FR-009 — Resource Ranking

The Dispatch Engine shall rank suitable resources according to business rules.

Ranking may consider:

1. Incident severity
2. Resource availability
3. Capability match
4. Distance
5. Current workload
6. Operational priority
7. Existing assignments
8. Expected response time

The exact ranking algorithm will be defined in the Dispatch Engine Design document.

---

## FR-010 — Resource Dispatch

The system shall allow a suitable resource to be dispatched to an incident.

A successful dispatch shall:

1. Identify a valid incident.
2. Identify a valid candidate resource.
3. Confirm that the resource can still be assigned.
4. Create a dispatch record.
5. Update the resource state.
6. Update the incident state when appropriate.
7. Record relevant timestamps and history.

These operations must maintain system consistency.

---

## FR-011 — Prevent Double Assignment

The system shall prevent the same resource from being simultaneously assigned to multiple incompatible incidents.

This requirement is especially important when multiple incidents attempt to dispatch the same resource concurrently.

The system must protect the resource assignment operation against race conditions.

---

## FR-012 — Concurrent Dispatch Handling

The system shall support concurrent dispatch requests.

When multiple operations attempt to allocate the same resource:

* Only valid assignment(s) may succeed according to business rules.
* The same resource must not become assigned to conflicting incidents.
* Resource state must remain consistent.
* Dispatch records must remain consistent.
* Database state must remain valid.

Concurrency handling shall be implemented deliberately rather than relying on timing assumptions.

---

## FR-013 — Dispatch Status

The system shall track the lifecycle of a dispatch.

Appropriate dispatch states may include:

* CREATED
* DISPATCHED
* EN_ROUTE
* ARRIVED
* COMPLETED
* CANCELLED

Only valid transitions shall be permitted.

---

## FR-014 — Dispatch History

The system shall maintain historical information about resource dispatches.

History should provide information such as:

* Incident
* Resource
* Dispatch status
* Assignment timestamp
* Completion timestamp
* Relevant operational events

Historical records shall remain available for reporting and auditing.

---

## FR-015 — Resource Availability Tracking

The system shall maintain the current availability state of resources.

When a resource is dispatched, its status shall be updated appropriately.

When a dispatch is completed or otherwise releases the resource, the resource shall become eligible for future assignments according to business rules.

---

## FR-016 — Incident History

The system shall maintain relevant historical changes to incidents.

The history may include:

* Status changes
* Assignment events
* Dispatch events
* Resolution events
* Cancellation events

The exact history model will be defined during domain and database design.

---

## FR-017 — Reporting

The system shall provide operational reporting capabilities.

Reports may include:

* Number of active incidents
* Incidents by severity
* Incidents by status
* Resource utilization
* Available resources
* Busy resources
* Dispatch counts
* Dispatch completion statistics
* Response activity
* Historical dispatch information

---

## FR-018 — REST API

The system shall expose REST endpoints for core operations.

The API shall provide appropriate operations for resources such as:

* Incidents
* Emergency resources
* Dispatches
* Reports

The API shall use appropriate HTTP methods and status codes.

---

## FR-019 — Web Interface

The system shall provide a basic web interface using:

* Servlets
* JSP
* MVC
* HTML
* CSS
* Basic JavaScript where appropriate

The web interface should allow operational users to interact with core system functionality.

---

## FR-020 — Persistent Storage

The system shall persist application data in PostgreSQL.

The system shall use SQL for relational database operations.

JDBC shall be used to establish understanding of direct database interaction before higher-level persistence abstraction is introduced.

Hibernate/JPA shall later be used where appropriate for ORM-based persistence.

---

## FR-021 — Transaction Management

Operations that modify multiple related pieces of data shall maintain transactional consistency.

For example, a dispatch operation may involve:

* Creating a dispatch
* Updating resource status
* Updating incident status
* Recording history

These operations shall not leave the system in a partially updated state.

---

## FR-022 — Input and Business Validation

The system shall distinguish between:

* Invalid input
* Invalid business operations
* Persistence failures
* Concurrency conflicts
* Unexpected system failures

Appropriate exceptions and error handling shall be used.

---

## FR-023 — Testing

The system shall contain automated tests using JUnit 5.

Tests shall cover:

* Domain behavior
* Validation
* Dispatch logic
* Resource selection
* Status transitions
* Concurrency-sensitive behavior
* Repository/database behavior where appropriate
* REST/API behavior where appropriate

---

# 5. Non-Functional Requirements

## NFR-001 — Maintainability

The system shall use clear separation of responsibilities.

Classes should have focused responsibilities and avoid unnecessary coupling.

---

## NFR-002 — Object-Oriented Design

The system shall apply appropriate object-oriented principles including:

* Encapsulation
* Abstraction
* Polymorphism
* Composition
* Interface-based design
* Appropriate inheritance where justified

Object-oriented concepts shall be used because they improve the design, not merely to satisfy a checklist.

---

## NFR-003 — Thread Safety

Shared mutable state shall be carefully controlled.

The system shall avoid unsafe concurrent modifications and race conditions.

---

## NFR-004 — Data Integrity

The database shall enforce appropriate integrity rules.

The application and database together shall protect against:

* Invalid references
* Invalid states
* Duplicate assignments
* Inconsistent relationships
* Partial updates

---

## NFR-005 — Performance

The system should efficiently handle:

* Resource candidate filtering
* Resource ranking
* Database queries
* Reporting queries
* Concurrent dispatch requests

Performance optimization shall be evidence-based rather than premature.

---

## NFR-006 — Scalability of Design

The architecture should allow future expansion of:

* Resource types
* Incident types
* Dispatch rules
* Reporting capabilities
* API endpoints
* Authentication/authorization
* Additional operational workflows

---

## NFR-007 — Testability

Business logic should be designed so that important behavior can be tested independently from:

* HTTP
* JSP
* Database infrastructure
* External systems

---

## NFR-008 — Security Awareness

The system shall follow reasonable backend security practices.

At minimum, database interaction shall avoid unsafe SQL construction and user input shall be validated.

Security will be expanded later when authentication and authorization are introduced.

---

## NFR-009 — Observability

Important operational events should be identifiable through appropriate application logging.

Examples include:

* Incident creation
* Resource dispatch
* Dispatch failure
* Resource state changes
* Concurrency conflicts
* Unexpected errors

---

## NFR-010 — Documentation

Important architectural and business decisions shall be documented.

Documentation shall evolve together with the implementation.

---

# 6. Business Constraints

The following constraints apply throughout the project:

1. PostgreSQL is the primary database.
2. SQL is the database language.
3. No NoSQL database will be used.
4. Maven is the build system.
5. Java 21 is the target Java version.
6. JDBC will be learned and used before relying completely on ORM.
7. Hibernate/JPA will be introduced after direct JDBC understanding.
8. Servlets/JSP/MVC will be introduced before moving to Spring-based abstractions.
9. Spring will be introduced later as an evolution of the manually built architecture.
10. Concurrency must be treated as a real business requirement.
11. Business logic must not be placed indiscriminately inside controllers or persistence classes.
12. Database transactions must protect multi-step state changes.
13. Automated testing is part of the development process, not a final step.

---

# 7. Requirements Traceability

Major requirements will eventually be mapped to:

* Domain objects
* Services
* Repositories/DAOs
* Database tables
* REST endpoints
* Web pages
* Automated tests

This traceability will help ensure that implementation decisions remain connected to actual business requirements.

---

# 8. Out-of-Scope Requirements

The following are intentionally outside the initial implementation scope:

* Microservices
* Kafka/event streaming infrastructure
* Redis
* Kubernetes
* Elasticsearch
* Cloud deployment
* React/Angular frontend
* GraphQL
* Advanced distributed systems
* Real GPS tracking
* Real emergency-service integrations
* Real SMS/email notification infrastructure

These may be considered later only if they provide meaningful learning or architectural value.

---

# 9. Requirement Completion Criteria

A requirement is considered implemented only when:

1. The behavior is clearly defined.
2. The appropriate domain/application logic exists.
3. Validation and edge cases are considered.
4. Persistence behavior is correct where applicable.
5. Concurrency implications are considered where applicable.
6. Automated tests exist for important behavior.
7. The implementation follows the project's architecture.
8. The implementation has been reviewed and refactored where necessary.

---

# 10. Document Status

**Document 02 — Requirements Specification**

Status: Completed as the initial requirements baseline.

Future requirement changes must be deliberate and documented rather than silently changing system behavior.