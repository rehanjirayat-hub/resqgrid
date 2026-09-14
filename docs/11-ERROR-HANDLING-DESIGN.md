# ResQGrid — Error Handling Design

## 1. Purpose

This document defines how ResQGrid detects, represents, handles, and communicates errors.

The goal is to ensure that failures are:

* Predictable.
* Consistent.
* Traceable.
* Safe.
* Understandable to API and web clients.
* Separated from business logic and persistence implementation details.

Error handling must not hide failures or silently produce incorrect system state.

---

# 2. Error Handling Principles

ResQGrid follows these principles:

1. Detect invalid input as early as practical.
2. Protect business rules inside the business/service layer.
3. Do not expose internal implementation details to clients.
4. Do not silently ignore failures.
5. Preserve database consistency.
6. Roll back failed transactional operations.
7. Distinguish client errors from server errors.
8. Use consistent API error responses.
9. Log unexpected technical failures appropriately.
10. Never allow an error to create an invalid domain state.

---

# 3. Error Categories

ResQGrid errors are divided into the following conceptual categories:

### 3.1 Validation Errors

The supplied input is invalid.

Examples:

* Required field missing.
* Invalid identifier format.
* Invalid severity.
* Invalid resource type.
* Invalid status value.
* Invalid location information.

---

### 3.2 Resource Not Found Errors

The requested domain object does not exist.

Examples:

* Incident does not exist.
* Resource does not exist.
* Team does not exist.
* Dispatch does not exist.

---

### 3.3 Business Rule Errors

The request is understood but violates a documented business rule.

Examples:

* Attempting an invalid incident status transition.
* Attempting to dispatch an unavailable resource.
* Required capability is missing.
* Required team is unavailable.
* Attempting to resolve an incident before required conditions are satisfied.

---

### 3.4 Conflict Errors

The requested operation conflicts with the current system state.

This category is especially important for concurrency.

Examples:

* Two incidents attempt to assign the same resource.
* Resource became BUSY before final assignment.
* Another transaction changed the resource state.
* Duplicate dispatch operation conflicts with an existing assignment.

---

### 3.5 Persistence Errors

The database or persistence layer fails.

Examples:

* Database connection failure.
* SQL execution failure.
* Transaction failure.
* Constraint violation.
* ORM persistence failure.

These errors must not be exposed to API clients as raw database exceptions.

---

### 3.6 Technical/System Errors

Unexpected application failures.

Examples:

* Unexpected runtime exception.
* Infrastructure failure.
* Serialization failure.
* Unexpected integration failure.

These should be logged and converted into safe client-facing responses.

---

# 4. Error Handling Layers

Error handling follows the system architecture.

Conceptual flow:

```text
Client
   |
Controller
   |
Service / Business Layer
   |
Domain
   |
Repository / Data Access
   |
Persistence
```

Each layer has different responsibilities.

---

# 5. Controller Layer

Controllers are responsible for handling transport-level concerns.

They should:

* Validate incoming request structure.
* Convert request data into application inputs.
* Invoke the appropriate service operation.
* Convert successful results into HTTP responses.
* Convert known application errors into appropriate HTTP responses.

Controllers must not contain complex business logic.

---

# 6. Service / Business Layer

The business layer is responsible for enforcing business rules.

It must detect conditions such as:

* Invalid lifecycle transition.
* Resource not eligible.
* Capability mismatch.
* Resource unavailable.
* Team unavailable.
* Dispatch conflict.
* Invalid operation according to business rules.

Business errors must be represented using application-level concepts rather than HTTP-specific implementation details wherever practical.

---

# 7. Domain Layer

The domain model must protect important invariants.

Examples include:

* Valid controlled values.
* Valid state transitions.
* Required relationships.
* Valid domain state.

The domain layer must not depend on HTTP concepts.

---

# 8. Repository / Data Access Layer

The repository/data-access layer is responsible for persistence failures.

It must not silently convert a failed database operation into a successful result.

Persistence errors should propagate to an appropriate higher-level handler.

The exact exception hierarchy and repository implementation will be finalized during implementation.

---

# 9. Transaction Failure

Operations involving multiple related state changes must be treated as atomic when required by the business rules.

For example, dispatch creation may involve:

```text
Verify Resource
      |
      v
Create Dispatch
      |
      v
Change Resource State
      |
      v
Commit
```

If a required operation fails:

```text
Rollback
```

The system must not leave a partially completed dispatch.

---

# 10. Dispatch Failure

If the Dispatch Engine cannot assign a suitable resource, the operation must not create an invalid dispatch.

Possible outcomes include:

* Suitable resource found and assigned.
* No eligible resource available.
* Resource became unavailable during assignment.
* Concurrent assignment conflict.
* Technical failure.

Each outcome must be distinguishable.

---

# 11. No Suitable Resource

No suitable resource is not necessarily a technical error.

It is a valid business outcome.

The system should communicate that:

* The incident was processed.
* No eligible resource could currently be assigned.
* No invalid assignment was created.

The exact API status representation will follow the final API convention.

---

# 12. Concurrency Conflict

A concurrency conflict occurs when system state changes between candidate selection and final assignment.

Example:

```text
Transaction A
    |
Sees Ambulance A as AVAILABLE
    |
    |        Transaction B
    |             |
    |        Assigns Ambulance A
    |             |
    v             v
Final Verification
       |
       X
Conflict
```

The failed transaction must:

* Detect the conflict.
* Avoid creating an invalid dispatch.
* Roll back any incomplete transactional changes.
* Return a meaningful conflict result.

The exact concurrency mechanism is defined in:

`09-CONCURRENCY-DESIGN.md`

---

# 13. Invalid State Transition

Invalid state transitions must be rejected.

Examples:

```text
RESOLVED -> REPORTED
```

or an otherwise unsupported transition according to the documented lifecycle.

The error should explain that the requested transition is not permitted.

The client must not be allowed to bypass lifecycle rules.

---

# 14. Resource Availability Errors

A resource may become unavailable between:

* Candidate discovery.
* Ranking.
* Final assignment.

Therefore, availability must be verified at the final assignment boundary.

If the resource is no longer available, the assignment must fail safely or follow the documented retry behavior.

The system must never assume that an earlier candidate query guarantees current availability.

---

# 15. Validation Strategy

Validation should occur at multiple levels.

### Transport Validation

Checks request structure and basic input validity.

### Business Validation

Checks whether the requested operation is allowed according to business rules.

### Persistence Validation

Database constraints provide a final integrity boundary.

No single validation layer should be considered sufficient by itself.

---

# 16. Error Propagation

Errors should move through the architecture without losing their meaning.

Conceptually:

```text
Database / Domain Failure
        |
        v
Application / Service Error
        |
        v
Controller Error Mapping
        |
        v
HTTP Response
```

Low-level technical details should not be exposed directly to clients.

---

# 17. API Error Responses

The REST API must return a consistent JSON error structure.

The final structure will be defined during API implementation.

At minimum, an error response should communicate:

* That an error occurred.
* A machine-readable error classification/code.
* A human-readable message.
* Sufficient contextual information where appropriate.

Internal stack traces, SQL statements, database credentials, filesystem paths, and other sensitive implementation details must not be exposed.

---

# 18. HTTP Error Mapping

The API follows the status-code conventions defined in:

`10-API-DESIGN.md`

Initial conceptual mapping:

| Error Category            | HTTP Status |
| ------------------------- | ----------: |
| Invalid request           |         400 |
| Resource not found        |         404 |
| Business conflict         |         409 |
| Unexpected server failure |         500 |

Additional status codes may be introduced only when justified and documented.

---

# 19. Error Codes

Application-level error codes should be considered for important business errors.

Examples of conceptual categories:

```text
INCIDENT_NOT_FOUND
RESOURCE_NOT_FOUND
TEAM_NOT_FOUND
DISPATCH_NOT_FOUND
INVALID_STATE_TRANSITION
RESOURCE_UNAVAILABLE
RESOURCE_NOT_ELIGIBLE
NO_RESOURCE_AVAILABLE
DISPATCH_CONFLICT
VALIDATION_FAILED
PERSISTENCE_FAILURE
```

The final naming convention must be established before implementation.

---

# 20. Error Messages

Client-facing messages must be:

* Clear.
* Concise.
* Safe.
* Actionable where possible.

Messages should not expose internal technical details.

Bad example conceptually:

```text
Hibernate threw an OptimisticLockException on table resource...
```

Better conceptually:

```text
The resource is no longer available for this dispatch.
```

---

# 21. Logging

Unexpected technical failures must be logged appropriately.

Logs may contain diagnostic information required for troubleshooting.

However, logs must not expose unnecessary sensitive information.

Important events to log include:

* Unexpected exceptions.
* Persistence failures.
* Transaction failures.
* Concurrency conflicts where useful for diagnosis.
* Important dispatch failures.

The final logging framework and log format will be decided during implementation.

---

# 22. Exception Handling

Exception handling must be deliberate.

The application must avoid:

* Catching every exception and ignoring it.
* Returning null to represent every failure.
* Printing stack traces as the primary error-handling mechanism.
* Converting all failures into generic success responses.
* Exposing raw exceptions through the REST API.

Exceptions should be handled at the layer responsible for making the appropriate decision.

---

# 23. Null and Missing Data

Missing required data must not silently become valid default values.

For example, if a required incident location is missing, the system must not silently assign an arbitrary location.

Required information must be validated according to the domain and requirements.

---

# 24. Database Constraint Failures

Database constraints provide an additional integrity boundary.

If a database constraint rejects an operation:

1. The transaction must fail safely.
2. The application must determine whether the failure represents a known business conflict or unexpected technical failure.
3. The client must receive an appropriate safe response.
4. Internal database details must not be exposed.

---

# 25. Rollback Requirements

A failed transactional operation must roll back all state changes that belong to the transaction.

For dispatch assignment, the system must not produce states such as:

```text
Dispatch created
+
Resource still AVAILABLE
```

or:

```text
Resource BUSY
+
No corresponding dispatch
```

when those states violate the documented business rules.

---

# 26. Retry Policy

Retries must not be added blindly.

Retries may be appropriate for specific transient failures, particularly concurrency-related conflicts.

Any retry mechanism must:

* Have a defined maximum.
* Preserve transaction correctness.
* Avoid duplicate dispatches.
* Avoid hiding persistent failures.
* Be documented before implementation.

The exact retry policy is intentionally deferred.

---

# 27. Client Responsibilities

Clients should:

* Validate basic input before submission where practical.
* Handle documented HTTP status codes.
* Handle conflict responses.
* Avoid assuming a resource remains available after a previous query.
* Avoid blindly repeating state-changing requests.

The server remains responsible for enforcing authoritative business rules.

---

# 28. Security Considerations

Error responses must not expose:

* Passwords.
* Credentials.
* Database connection information.
* Authentication tokens.
* Internal filesystem paths.
* SQL statements.
* Stack traces.
* Sensitive operational information not required by the client.

Security requirements will be expanded during the security implementation stage.

---

# 29. Web/MVC Error Handling

The JSP/Servlet web application must provide user-friendly error handling.

The web layer may present:

* Validation messages.
* Business-rule messages.
* Not-found messages.
* Conflict messages.
* General system-error messages.

The web interface must not expose raw exceptions to users.

---

# 30. REST vs Web Error Handling

REST clients and JSP/Servlet clients may present errors differently, but both must use the same underlying business rules.

Conceptually:

```text
                 Business Layer
                /              \
               /                \
        REST Controller      Web Controller
              |                    |
          JSON Error          Web Error View
```

Business logic must not be duplicated between these presentation paths.

---

# 31. Error Handling and Concurrency

Error handling must work together with the concurrency design.

A concurrency conflict is an expected operational possibility, not necessarily an application defect.

The system must:

* Detect it.
* Preserve consistency.
* Communicate it clearly.
* Allow an appropriate client response or retry where documented.

---

# 32. Testing Error Handling

Testing must verify:

* Invalid input.
* Missing resources.
* Invalid state transitions.
* Resource unavailable.
* No suitable resource.
* Capability mismatch.
* Team unavailable.
* Concurrent dispatch conflict.
* Persistence failure.
* Transaction rollback.
* Unexpected technical failure.
* Correct HTTP status codes.
* Correct error response format.

Detailed testing requirements are defined in:

`12-TESTING-STRATEGY.md`

---

# 33. Error Handling Boundaries

This document intentionally does not finalize:

* Exact exception class names.
* Exact exception inheritance hierarchy.
* Exact JSON error schema.
* Exact logging framework.
* Exact logging format.
* Exact retry implementation.
* Exact transaction annotations/configuration.
* Exact global exception-handler implementation.
* Exact security error handling.

These decisions must be made during implementation design and documented before coding them.

---

# 34. No Silent Error Handling

ResQGrid must never silently convert a failed operation into a successful operation.

Every failure must result in one of:

* A valid documented business outcome.
* A meaningful application error.
* A safe technical error response.

---

# 35. Documentation Consistency

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
* `12-TESTING-STRATEGY.md`
* `13-DEVELOPMENT-ROADMAP.md`

If a conflict is discovered, implementation must stop.

The relevant documentation must be reviewed and updated before implementation continues.

The documentation remains the source of truth for ResQGrid.
