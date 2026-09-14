# ResQGrid — API Design

## 1. Purpose

This document defines the initial REST API design for ResQGrid.

The API provides external access to documented application capabilities while keeping business logic inside the application/service layer.

The API must support:

* Incident management.
* Resource management.
* Response-team management.
* Dispatch operations.
* Dispatch monitoring.
* History and reporting.
* Validation and error handling.

The API must not contain the core dispatch algorithm or persistence logic.

---

# 2. API Style

ResQGrid will expose a REST-style HTTP API.

The API will use:

* HTTP.
* JSON.
* Resource-oriented endpoints.
* Standard HTTP methods.
* Appropriate HTTP status codes.
* Consistent error responses.

The API is intended for both:

* External API clients.
* The ResQGrid web application.

---

# 3. API Base Path

The API will use a common versioned base path.

Initial design:

```text
/api/v1
```

Therefore, resource endpoints will follow a structure such as:

```text
/api/v1/incidents
/api/v1/resources
/api/v1/teams
/api/v1/dispatches
```

The exact final endpoint list must remain consistent with this document.

---

# 4. HTTP Methods

The API will use HTTP methods according to operation semantics.

### GET

Used for retrieving resources or information.

### POST

Used for creating resources or initiating documented operations.

### PUT

Used for replacing/updating a resource where full update semantics are appropriate.

### PATCH

May be used for partial updates where specifically justified.

### DELETE

Used only for documented deletion operations.

Deletion must not be introduced merely for convenience where historical records or business rules require preservation.

---

# 5. JSON

API request and response bodies will use JSON unless a documented endpoint requires another representation.

JSON field naming must remain consistent throughout the API.

The final naming convention must be selected before implementation and used consistently.

---

# 6. Incident API

The Incident API provides operations for managing emergency incidents.

## 6.1 Create Incident

Conceptual endpoint:

```text
POST /api/v1/incidents
```

Purpose:

Create a new incident.

The request must contain the information required by the documented incident model.

The server is responsible for assigning system-controlled state such as the initial incident status where appropriate.

---

## 6.2 Get Incident

```text
GET /api/v1/incidents/{id}
```

Purpose:

Retrieve a specific incident.

The response may include:

* Incident identity.
* Incident information.
* Severity.
* Status.
* Location.
* Relevant timestamps.
* Associated dispatch information where appropriate.

---

## 6.3 List Incidents

```text
GET /api/v1/incidents
```

Purpose:

Retrieve incidents.

Filtering may be supported using documented query parameters such as:

* Status.
* Severity.

Additional filters must be documented before implementation.

---

## 6.4 Update Incident

```text
PUT /api/v1/incidents/{id}
```

Purpose:

Update documented incident information.

The operation must respect incident lifecycle rules.

An API client must not be allowed to arbitrarily move an incident to an invalid lifecycle state.

---

## 6.5 Assess Incident

```text
POST /api/v1/incidents/{id}/assessment
```

Purpose:

Perform or record the documented incident assessment operation.

The exact request structure will be finalized during implementation.

---

## 6.6 Resolve Incident

```text
POST /api/v1/incidents/{id}/resolve
```

Purpose:

Resolve an incident according to the documented business rules.

The operation must not bypass required response/dispatch lifecycle rules.

---

# 7. Resource API

The Resource API provides management and operational access to emergency resources.

## 7.1 Register Resource

```text
POST /api/v1/resources
```

Purpose:

Register an emergency resource.

The request must contain the required resource information defined by the Domain Model and Database Design.

---

## 7.2 Get Resource

```text
GET /api/v1/resources/{id}
```

Purpose:

Retrieve a specific resource.

The response may include:

* Resource identity.
* Resource type.
* Current status.
* Location.
* Capabilities.
* Operational information.

---

## 7.3 List Resources

```text
GET /api/v1/resources
```

Purpose:

Retrieve resources.

Filtering may support documented criteria such as:

* Resource type.
* Resource status.
* Capability.

Additional filtering must be documented before implementation.

---

## 7.4 Update Resource

```text
PUT /api/v1/resources/{id}
```

Purpose:

Update documented resource information.

Updates must respect operational and business rules.

---

## 7.5 Change Resource Status

```text
PATCH /api/v1/resources/{id}/status
```

Purpose:

Change the operational status of a resource.

Valid statuses are:

```text
AVAILABLE
BUSY
OFFLINE
MAINTENANCE
```

The API must validate whether the requested state transition is allowed.

---

## 7.6 Resource History

```text
GET /api/v1/resources/{id}/dispatches
```

Purpose:

Retrieve dispatch history for a resource.

Historical records must remain available for reporting and operational analysis.

---

# 8. Response Team API

The Response Team API manages response teams.

## 8.1 Create Team

```text
POST /api/v1/teams
```

Purpose:

Register a response team.

---

## 8.2 Get Team

```text
GET /api/v1/teams/{id}
```

Purpose:

Retrieve a specific team.

---

## 8.3 List Teams

```text
GET /api/v1/teams
```

Purpose:

Retrieve response teams.

Filtering may be supported by documented criteria such as availability and capability.

---

## 8.4 Update Team

```text
PUT /api/v1/teams/{id}
```

Purpose:

Update documented team information.

---

## 8.5 Team Dispatch History

```text
GET /api/v1/teams/{id}/dispatches
```

Purpose:

Retrieve historical dispatch information for a team.

---

# 9. Capability API

Capabilities are associated with resources and teams.

The API may expose capability management where required by the documented use cases.

Initial conceptual endpoints:

```text
GET /api/v1/capabilities
POST /api/v1/capabilities
```

Capability creation must not be exposed if the final application design establishes a fixed system-managed capability catalogue.

That decision must be finalized before implementation.

---

# 10. Dispatch API

The Dispatch API is the primary external interface for response assignment operations.

---

## 10.1 Find Suitable Resources

Conceptual endpoint:

```text
GET /api/v1/dispatches/candidates
```

Purpose:

Identify resources that are currently suitable candidates for an incident.

The operation may return information useful for:

* Eligibility.
* Ranking.
* Distance.
* Workload.
* Suitability.

This endpoint must not itself create a dispatch.

---

## 10.2 Create Dispatch

Conceptual endpoint:

```text
POST /api/v1/dispatches
```

Purpose:

Request a dispatch operation.

The server must execute the documented Dispatch Engine process:

```text
Determine Requirements
        |
        v
Identify Candidates
        |
        v
Filter
        |
        v
Rank
        |
        v
Final Assignment Verification
        |
        v
Create Dispatch
```

The client must not directly force an invalid resource assignment.

---

# 11. Dispatch Assignment Authority

The final assignment decision belongs to the server-side business logic.

The API must not allow a client to bypass:

* Eligibility checks.
* Capability checks.
* Resource status checks.
* Team requirements.
* Existing assignment checks.
* Concurrency protection.
* Business rules.

Even if a client supplies a resource ID, the server must verify that the resource is valid for the assignment.

---

# 12. Get Dispatch

```text
GET /api/v1/dispatches/{id}
```

Purpose:

Retrieve a dispatch.

The response may include:

* Dispatch identity.
* Incident.
* Resource.
* Response team where applicable.
* Dispatch status.
* Relevant timestamps.

---

# 13. List Dispatches

```text
GET /api/v1/dispatches
```

Purpose:

Retrieve dispatch records.

Documented filtering may include:

* Dispatch status.
* Incident.
* Resource.
* Team.

Additional filters must be documented before implementation.

---

# 14. Start Dispatch

```text
POST /api/v1/dispatches/{id}/start
```

Purpose:

Move a valid dispatch into the `IN_PROGRESS` state according to business rules.

---

# 15. Complete Dispatch

```text
POST /api/v1/dispatches/{id}/complete
```

Purpose:

Complete a dispatch according to documented lifecycle rules.

Completion must update the appropriate operational state.

For example, a successfully completed resource assignment may allow:

```text
BUSY
  |
  v
AVAILABLE
```

provided the business rules permit that transition.

---

# 16. Incident Dispatch History

```text
GET /api/v1/incidents/{id}/dispatches
```

Purpose:

Retrieve dispatch history associated with an incident.

This supports:

* Incident history.
* Operational monitoring.
* Reporting.

---

# 17. Reporting API

The Reporting API provides operational statistics and summaries.

Initial conceptual endpoints include:

```text
GET /api/v1/reports/incidents
GET /api/v1/reports/dispatches
GET /api/v1/reports/resources
```

Possible information includes:

### Incident Reports

* Incident counts.
* Severity distribution.
* Status distribution.

### Dispatch Reports

* Dispatch counts.
* Completion information.
* Dispatch status distribution.

### Resource Reports

* Resource utilization.
* Workload.
* Dispatch history.

The exact report fields must be defined before implementation.

---

# 18. Query Parameters

Query parameters may be used for filtering and retrieval.

Examples include:

```text
/api/v1/incidents?status=...
/api/v1/incidents?severity=...
/api/v1/resources?status=...
/api/v1/resources?type=...
```

Only documented filtering parameters should be implemented.

---

# 19. Pagination

List endpoints may eventually require pagination.

Pagination must be introduced when required by the documented performance and API requirements.

If pagination is implemented, its request and response format must be consistent across list endpoints.

---

# 20. HTTP Status Codes

The API will use standard HTTP status codes.

Initial mapping:

### 200 OK

Successful retrieval or successful operation with a response body.

### 201 Created

A new resource was successfully created.

### 204 No Content

A successful operation that intentionally returns no response body.

### 400 Bad Request

The request structure or supplied data is invalid.

### 404 Not Found

The requested resource does not exist.

### 409 Conflict

The request conflicts with current application state.

Examples include:

* Concurrent assignment conflict.
* Invalid state transition.
* Resource already assigned.

### 422 Unprocessable Entity

May be used when the request structure is valid but violates a documented business validation rule, if this status is selected for the final API convention.

### 500 Internal Server Error

Unexpected server-side failure.

The final status-code convention must remain consistent across endpoints.

---

# 21. API Error Responses

Error responses must use a consistent JSON structure.

The exact error response fields will be finalized in:

`11-ERROR-HANDLING-DESIGN.md`

The API must distinguish between:

* Validation failure.
* Not found.
* Business-rule failure.
* Conflict.
* Technical failure.

---

# 22. Validation

API requests must be validated before the corresponding business operation is executed.

Validation must include:

* Required fields.
* Valid values.
* Valid identifiers.
* Valid state transitions.
* Business-specific requirements where applicable.

API validation must not replace deeper business validation.

The service/business layer remains responsible for protecting business rules.

---

# 23. Concurrency Errors

A concurrent dispatch conflict must be represented as a business/API conflict rather than as a successful dispatch.

Conceptually:

```text
Client
  |
POST /dispatches
  |
Dispatch Engine
  |
Concurrent Conflict
  |
409 Conflict
```

The response should provide enough information for the client to understand that the assignment could not be completed because the operational state changed.

---

# 24. API and Dispatch Engine Separation

The REST controller must not implement:

* Candidate filtering.
* Resource scoring.
* Distance calculations.
* Workload calculations.
* Locking.
* Transaction coordination.

The API layer delegates these responsibilities to the appropriate application/business components.

---

# 25. API and Persistence Separation

Controllers must not directly:

* Execute SQL.
* Open JDBC connections.
* Manage Hibernate sessions/entity managers.
* Perform database transactions directly.

Persistence responsibilities belong to the data-access/application architecture.

---

# 26. Resource Representation

API responses should represent resources in a stable external format.

The external representation should not unnecessarily expose:

* Internal persistence implementation details.
* Database-specific details.
* Internal ORM state.
* Sensitive configuration.

The final response model will be defined before implementation.

---

# 27. Entity IDs

API resource identifiers must be represented consistently.

The exact identifier type and JSON representation must follow the final database/domain design.

The API must not assume an identifier is available before persistence unless the documented ID strategy guarantees it.

---

# 28. State Transition Protection

Clients must not arbitrarily manipulate lifecycle states.

For example, an incident should not be allowed to jump directly from:

```text
REPORTED
```

to:

```text
RESOLVED
```

unless the documented business rules explicitly permit that transition.

The API must delegate lifecycle validation to the appropriate business layer.

---

# 29. Idempotency Considerations

Operations that create or trigger important state changes must be considered for duplicate requests.

This is especially important for:

```text
POST /api/v1/dispatches
```

A client retry caused by a network problem must not accidentally create duplicate conflicting dispatches.

The final idempotency strategy must be documented before implementation.

---

# 30. API Security Boundary

The initial API design acknowledges security as a system requirement.

Security mechanisms such as:

* Authentication.
* Authorization.
* Role-based access.
* Request protection.

will be introduced according to the project roadmap.

Security implementation must not bypass the documented application/business authorization boundaries.

---

# 31. Actor-to-API Responsibility

The API must support documented actors without embedding actor-specific business logic inside controllers.

Relevant operations include:

### Emergency Operator

* Report incident.
* Assess incident.
* View/update incident.
* Request dispatch.
* Monitor dispatch.

### Resource Coordinator

* Register resource.
* Update resource.
* Change resource status.
* Manage resource capabilities.
* Manage teams.

### Reporting User

* View operational reports.
* View historical information.

### System Administrator

* Manage system-level configuration where documented.

Authorization details are intentionally deferred to the security stage.

---

# 32. API Versioning

The initial API version is:

```text
v1
```

The version is included in the URL:

```text
/api/v1/...
```

Breaking API changes should require a documented versioning decision.

---

# 33. API Documentation

The API should eventually be documented using an industry-standard API documentation approach.

The exact documentation technology is not finalized in this document.

API documentation must remain consistent with the actual implementation and this design document.

---

# 34. API Testing

API testing must verify:

* Successful requests.
* Invalid requests.
* Missing resources.
* Invalid state transitions.
* Business-rule violations.
* Dispatch conflicts.
* Concurrent assignment behavior.
* Correct HTTP status codes.
* Correct JSON responses.

The complete testing strategy is defined in:

`12-TESTING-STRATEGY.md`

---

# 35. API Design Boundaries

This document intentionally does not finalize:

* Exact request JSON fields.
* Exact response JSON fields.
* Exact DTO class names.
* Exact validation annotations.
* Exact authentication mechanism.
* Exact authorization model.
* Exact pagination format.
* Exact idempotency mechanism.
* Exact report response structures.
* Exact error response schema.
* Exact API documentation technology.

These must be finalized before implementation.

---

# 36. No Undocumented Endpoints

A new API endpoint must not be introduced merely because it is convenient during implementation.

If a new endpoint is required:

1. Identify the requirement.
2. Identify the affected use case.
3. Update this document.
4. Check consistency with the relevant business, architecture, and error-handling documents.
5. Only then implement the endpoint.

---

# 37. Documentation Consistency

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
* `11-ERROR-HANDLING-DESIGN.md`
* `12-TESTING-STRATEGY.md`
* `13-DEVELOPMENT-ROADMAP.md`

If a conflict is discovered, implementation must stop.

The conflicting documentation must be reviewed and updated before coding continues.

The documentation remains the source of truth for the project.
