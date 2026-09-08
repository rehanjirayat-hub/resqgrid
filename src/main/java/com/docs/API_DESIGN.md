# ResQGrid — API & Application Flow Design

**Document:** API & Application Flow Design
**Project:** ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine
**Version:** 1.0
**Status:** Development Baseline

---

# 1. Purpose

ResQGrid will expose its application functionality through two presentation paths:

1. A web interface built with Servlets, JSP, and MVC.
2. REST APIs for programmatic access and integration testing.

Both paths must use the same service and business layers.

The presentation layer should not contain dispatch rules, database queries, or direct business decisions.

The basic structure is:

```text
Web / REST Client
       |
       v
Controller
       |
       v
Service
       |
       v
Domain / Dispatch Engine
       |
       v
Repository / DAO
       |
       v
PostgreSQL
```

---

# 2. API Design Goals

The API should provide operations for:

* incident management
* resource management
* dispatch operations
* status tracking
* history retrieval
* reporting

The API should also:

* use meaningful HTTP methods
* return appropriate HTTP status codes
* validate input
* return predictable responses
* expose business errors clearly
* avoid exposing persistence implementation details
* keep controllers thin

---

# 3. API Style

The initial REST API will follow resource-oriented URL design.

Examples:

```text
/api/incidents
/api/resources
/api/dispatches
```

Specific endpoints should represent operations on those resources rather than embedding implementation details in URLs.

---

# 4. Incident Endpoints

Initial incident API:

| Method  | Endpoint                      | Purpose                |
| ------- | ----------------------------- | ---------------------- |
| `POST`  | `/api/incidents`              | Create incident        |
| `GET`   | `/api/incidents`              | List incidents         |
| `GET`   | `/api/incidents/{id}`         | Get incident           |
| `PUT`   | `/api/incidents/{id}`         | Update incident        |
| `PATCH` | `/api/incidents/{id}/status`  | Change incident status |
| `GET`   | `/api/incidents/{id}/history` | View incident history  |

The exact endpoint set may be adjusted when implementation begins.

---

# 5. Create Incident

### Request

```text
POST /api/incidents
```

Example JSON:

```json
{
  "type": "MEDICAL_EMERGENCY",
  "description": "Person requires urgent medical assistance",
  "severity": "CRITICAL",
  "latitude": 12.2958,
  "longitude": 76.6394
}
```

The controller receives the request and converts it into an appropriate application/domain input.

The controller should not decide whether the incident is `CRITICAL` or determine which resource should be dispatched.

---

# 6. Create Incident Flow

```text
HTTP POST
    |
    v
IncidentController
    |
    v
Validate request
    |
    v
IncidentService
    |
    v
Create Incident
    |
    v
Repository
    |
    v
PostgreSQL
    |
    v
Response
```

The service layer owns the actual business operation.

---

# 7. Get Incident

### Request

```text
GET /api/incidents/{id}
```

The response should provide the information required by API clients without exposing internal persistence objects unnecessarily.

A response may conceptually contain:

```json
{
  "id": 101,
  "type": "MEDICAL_EMERGENCY",
  "description": "Person requires urgent medical assistance",
  "severity": "CRITICAL",
  "status": "ASSIGNED",
  "location": {
    "latitude": 12.2958,
    "longitude": 76.6394
  }
}
```

The final response DTO structure will be defined during implementation.

---

# 8. List Incidents

### Request

```text
GET /api/incidents
```

The initial implementation may return a collection of incidents.

Later, filtering can be added for useful operational queries such as:

```text
GET /api/incidents?status=REPORTED
GET /api/incidents?severity=CRITICAL
```

Filtering should be introduced when the underlying service and repository logic are ready.

---

# 9. Update Incident

### Request

```text
PUT /api/incidents/{id}
```

Updates should validate the current incident state before applying changes.

The API must not allow arbitrary state changes that violate the incident lifecycle.

For example, an already resolved incident should not silently return to `REPORTED`.

---

# 10. Incident Status Changes

A dedicated status operation may be exposed:

```text
PATCH /api/incidents/{id}/status
```

Example:

```json
{
  "status": "IN_PROGRESS"
}
```

The service must verify that the requested transition is valid.

The controller should not contain the lifecycle rules.

---

# 11. Resource Endpoints

Initial resource API:

| Method  | Endpoint                      | Purpose                |
| ------- | ----------------------------- | ---------------------- |
| `POST`  | `/api/resources`              | Register resource      |
| `GET`   | `/api/resources`              | List resources         |
| `GET`   | `/api/resources/{id}`         | Get resource           |
| `PUT`   | `/api/resources/{id}`         | Update resource        |
| `PATCH` | `/api/resources/{id}/status`  | Change resource status |
| `GET`   | `/api/resources/{id}/history` | View resource history  |

---

# 12. Resource Registration

### Request

```text
POST /api/resources
```

Example:

```json
{
  "name": "Ambulance A",
  "type": "AMBULANCE",
  "status": "AVAILABLE",
  "latitude": 12.3001,
  "longitude": 76.6402,
  "capabilities": [
    "BASIC_MEDICAL",
    "ADVANCED_MEDICAL"
  ]
}
```

The service validates the resource and its capabilities before persistence.

---

# 13. Resource Status

Example:

```text
PATCH /api/resources/{id}/status
```

Request:

```json
{
  "status": "MAINTENANCE"
}
```

The service must ensure the transition is allowed.

A resource currently involved in an active dispatch should not simply be switched to an unrelated state if that would violate system invariants.

---

# 14. Dispatch Endpoints

Initial dispatch API:

| Method  | Endpoint                         | Purpose                            |
| ------- | -------------------------------- | ---------------------------------- |
| `POST`  | `/api/incidents/{id}/dispatch`   | Start dispatch decision/assignment |
| `GET`   | `/api/dispatches/{id}`           | Get dispatch                       |
| `GET`   | `/api/incidents/{id}/dispatches` | List incident dispatches           |
| `PATCH` | `/api/dispatches/{id}/status`    | Update dispatch status             |
| `POST`  | `/api/dispatches/{id}/cancel`    | Cancel dispatch                    |

The exact endpoint structure can be refined during implementation.

---

# 15. Dispatch Request

The dispatch operation is one of the most important API operations.

Example:

```text
POST /api/incidents/101/dispatch
```

The client does not normally select an arbitrary resource.

Instead, the server invokes the Dispatch Engine.

```text
Client
   |
   v
DispatchController
   |
   v
DispatchService
   |
   v
DispatchEngine
   |
   v
Select suitable resource
   |
   v
Safely assign resource
```

---

# 16. Why the Client Does Not Choose the Resource

The Dispatch Engine exists to make the resource-selection decision.

Allowing the client to simply submit:

```json
{
  "resourceId": 25
}
```

would bypass important business rules.

The server must determine whether a resource is:

* available
* suitable
* capable
* appropriately located
* operationally acceptable

The client may eventually request or influence dispatch behavior, but the server remains responsible for enforcing dispatch rules.

---

# 17. Dispatch Response

A successful dispatch response may conceptually contain:

```json
{
  "dispatchId": 501,
  "incidentId": 101,
  "resourceId": 25,
  "status": "DISPATCHED",
  "assignedAt": "2026-09-08T10:30:00"
}
```

The exact response model will be finalized during implementation.

---

# 18. No Suitable Resource

If no suitable resource is available, the API should return a controlled business response.

Possible HTTP status:

```text
409 Conflict
```

or another status chosen during implementation based on the exact semantics.

The response should clearly communicate that the dispatch could not be completed because no suitable resource was available.

This is different from a server failure.

---

# 19. Resource Already Taken

A resource may become unavailable between candidate selection and assignment.

Example:

```text
Dispatch A selects Ambulance 12
        |
        v
Dispatch B assigns Ambulance 12
        |
        v
Dispatch A attempts assignment
```

The service must detect the conflict.

The API should return a controlled result rather than incorrectly creating a duplicate assignment.

The service may retry with another candidate according to the concurrency design.

---

# 20. Dispatch Status

Example:

```text
PATCH /api/dispatches/{id}/status
```

Request:

```json
{
  "status": "EN_ROUTE"
}
```

Valid transitions must be enforced by the service/domain logic.

The controller should only translate the HTTP request into a service call.

---

# 21. Dispatch History

Incident history:

```text
GET /api/incidents/{id}/history
```

Resource history:

```text
GET /api/resources/{id}/history
```

Dispatch history:

```text
GET /api/dispatches/{id}/history
```

History responses should provide a useful operational timeline.

Example:

```json
{
  "eventType": "RESOURCE_ASSIGNED",
  "description": "Ambulance A assigned to incident 101",
  "createdAt": "2026-09-08T10:30:00"
}
```

---

# 22. Reporting Endpoints

Operational reporting may eventually expose endpoints such as:

```text
GET /api/reports/incidents
GET /api/reports/resources
GET /api/reports/dispatches
```

Potential report data includes:

* incident counts
* incidents by severity
* incidents by status
* resource utilization
* dispatch counts
* response times

Reporting logic belongs in a reporting service rather than inside controllers.

---

# 23. HTTP Methods

The initial API follows standard HTTP method semantics.

| Method   | Typical use                                   |
| -------- | --------------------------------------------- |
| `GET`    | Retrieve data                                 |
| `POST`   | Create or initiate an operation               |
| `PUT`    | Replace/update a resource                     |
| `PATCH`  | Partially update state                        |
| `DELETE` | Delete where deletion is explicitly supported |

Operational records such as incidents and dispatches should not be casually deleted.

---

# 24. HTTP Status Codes

Initial status-code guidelines:

| Status                      | Meaning                                                       |
| --------------------------- | ------------------------------------------------------------- |
| `200 OK`                    | Successful retrieval/update                                   |
| `201 Created`               | Resource successfully created                                 |
| `204 No Content`            | Successful operation with no response body                    |
| `400 Bad Request`           | Invalid request                                               |
| `404 Not Found`             | Requested entity does not exist                               |
| `409 Conflict`              | Business/concurrency conflict                                 |
| `422 Unprocessable Entity`  | Valid request structure but invalid business data, if adopted |
| `500 Internal Server Error` | Unexpected server failure                                     |

The final status-code policy will be kept consistent across endpoints.

---

# 25. Validation

Validation should happen at appropriate layers.

### Controller/API layer

Validate request structure.

Examples:

* required fields present
* valid JSON
* correct basic data types

### Service/domain layer

Validate business rules.

Examples:

* severity is valid
* status transition is valid
* resource can be assigned
* required capabilities are satisfied

### Database

Enforce structural integrity.

Examples:

* foreign keys
* unique constraints
* not-null requirements

Validation is therefore layered rather than concentrated in one class.

---

# 26. Error Response

API errors should have a consistent structure.

A possible format:

```json
{
  "status": 409,
  "error": "RESOURCE_UNAVAILABLE",
  "message": "Selected resource is no longer available",
  "timestamp": "2026-09-08T10:32:10"
}
```

The exact error DTO will be defined during implementation.

Internal exception details and stack traces should not be returned to API clients.

---

# 27. Controller Responsibilities

Controllers should:

* receive requests
* parse input
* perform basic request validation
* call services
* translate service results into HTTP responses
* translate known application errors into appropriate HTTP status codes

Controllers should not:

* calculate dispatch scores
* execute SQL
* directly manipulate database connections
* implement resource-ranking rules
* contain large business workflows

---

# 28. Service Responsibilities

Services coordinate business operations.

For example:

```text
DispatchService
    |
    +--> retrieve incident
    +--> retrieve candidate resources
    +--> invoke DispatchEngine
    +--> perform safe assignment
    +--> coordinate transaction
    +--> record history
```

The service is the boundary between presentation and business/persistence operations.

---

# 29. Repository Responsibilities

Repositories/DAOs handle persistence.

Examples:

```text
IncidentRepository
ResourceRepository
DispatchRepository
HistoryRepository
```

They should be responsible for operations such as:

* find incident
* save incident
* find available resources
* save dispatch
* update resource
* save history

They should not decide which resource is best.

---

# 30. REST and JSP Use the Same Services

A web page and REST request should not have separate implementations of the same business operation.

For example:

```text
JSP/Servlet
      |
      v
IncidentService
      ^
      |
REST Controller
```

Both presentation paths use the same business layer.

This avoids duplicating business rules.

---

# 31. Servlet/JSP MVC

The web interface will follow MVC principles.

Conceptually:

```text
Browser
   |
   v
Servlet Controller
   |
   v
Service
   |
   v
Domain / Repository
   |
   v
PostgreSQL
   |
   v
Service result
   |
   v
Servlet
   |
   v
JSP View
```

JSP should primarily render data rather than implement business logic.

---

# 32. JSP Responsibilities

JSP should handle presentation concerns such as:

* displaying incidents
* displaying resources
* displaying dispatch status
* displaying history
* presenting forms

JSP should not contain:

* SQL
* resource-ranking logic
* transaction handling
* concurrency logic
* complex business rules

---

# 33. Servlet Responsibilities

Servlets act as web controllers.

They should:

* receive HTTP requests
* determine the operation
* collect request parameters
* call the appropriate service
* place results into request/session scope where appropriate
* forward or redirect to the appropriate JSP

The servlet should remain thin.

---

# 34. REST Servlet Responsibilities

The initial REST implementation may also use Servlets before Spring MVC is introduced.

A REST servlet can:

* inspect HTTP method
* parse JSON
* validate request structure
* invoke services
* serialize response objects as JSON
* set HTTP status codes

This allows the project to understand what frameworks later automate.

---

# 35. JSON

JSON will be used for REST request and response bodies.

The project will eventually use a JSON library rather than manually building JSON strings.

The exact library will be selected when REST implementation begins.

The service and domain layers should not depend on JSON formatting.

---

# 36. DTOs

The API should use DTOs where appropriate rather than exposing JPA entities directly.

Examples:

```text
CreateIncidentRequest
IncidentResponse
CreateResourceRequest
ResourceResponse
DispatchResponse
ErrorResponse
```

This provides separation between:

* API representation
* domain model
* persistence model

It also prevents accidental exposure of internal fields.

---

# 37. Why Not Return Entities Directly

Returning persistence entities directly can create problems such as:

* exposing internal fields
* accidental lazy-loading behavior
* unwanted relationship traversal
* API coupling to database structure
* serialization problems
* difficulty changing persistence mappings

DTOs provide a stable API boundary.

---

# 38. Request Flow Example

Creating an incident:

```text
POST /api/incidents
        |
        v
IncidentController
        |
        v
CreateIncidentRequest
        |
        v
IncidentService
        |
        v
Incident domain object
        |
        v
IncidentRepository
        |
        v
PostgreSQL
        |
        v
IncidentResponse
        |
        v
HTTP 201
```

---

# 39. Dispatch Request Flow

A dispatch request follows a more complex path:

```text
POST /api/incidents/{id}/dispatch
        |
        v
DispatchController
        |
        v
DispatchService
        |
        v
Load incident/resources
        |
        v
DispatchEngine
        |
        v
Candidate selection
        |
        v
Safe assignment
        |
        v
Transaction
        |
        +--> Dispatch
        +--> Resource
        +--> Incident
        +--> History
        |
        v
Commit
        |
        v
DispatchResponse
```

---

# 40. Authentication and Authorization

Authentication and authorization are not part of the first API implementation.

The architecture should nevertheless keep these concerns outside business logic.

Later, security can be introduced without rewriting the Dispatch Engine.

Potential future roles include:

```text
OPERATOR
DISPATCHER
MANAGER
ADMIN
```

The current implementation will focus on business functionality first.

---

# 41. API Versioning

The first API can use:

```text
/api/...
```

A versioning strategy such as:

```text
/api/v1/...
```

may be introduced if the API evolves significantly.

Versioning should not be added simply for appearance.

The important requirement is to avoid breaking existing consumers when meaningful API evolution eventually occurs.

---

# 42. Idempotency Considerations

Some operations may be accidentally submitted more than once.

For example:

```text
POST /api/incidents/101/dispatch
```

could be triggered twice by a client.

The service must ensure that repeated requests do not create conflicting active assignments.

Database constraints and dispatch-state checks provide important protection.

More advanced idempotency keys can be considered later if required.

---

# 43. Transactions and HTTP Requests

An HTTP request should not directly control database transactions.

Instead:

```text
Controller
    |
    v
Service
    |
    v
Transaction boundary
```

The service layer coordinates the business operation and its transactional requirements.

This keeps transaction management independent of the presentation technology.

---

# 44. API and Concurrency

REST requests may execute concurrently.

Therefore:

```text
Request A -> DispatchService
Request B -> DispatchService
```

must be safe.

The controller itself should not attempt to solve the resource race condition.

The concurrency controls defined in the Concurrency Design document apply inside the service/persistence operation.

---

# 45. API Testing

The REST API will eventually be tested using:

* Postman for manual API testing
* JUnit 5 for automated tests
* integration tests for persistence operations

Important scenarios include:

* valid incident creation
* invalid incident creation
* missing resource
* invalid status transition
* successful dispatch
* no suitable resource
* concurrent dispatch
* duplicate dispatch attempt
* resource becoming unavailable

---

# 46. API Design Principles

ResQGrid APIs should follow these principles:

1. Controllers remain thin.
2. Business logic belongs in services/domain components.
3. Dispatch decisions belong to the Dispatch Engine.
4. Database access belongs to repositories/DAOs.
5. REST and JSP/MVC use the same service layer.
6. DTOs separate API contracts from internal entities.
7. HTTP status codes communicate operation results.
8. Validation is layered.
9. Errors have predictable responses.
10. Internal implementation details are not exposed.
11. Concurrent requests must be treated as normal.
12. API design should remain simple until real requirements justify additional complexity.

---

# 47. Initial API Summary

The initial resource groups are:

```text
/api/incidents
/api/resources
/api/dispatches
/api/reports
```

The most important operations are:

```text
Create incident
View incident
Update incident
Change incident status

Register resource
View resource
Update resource
Change resource status

Dispatch resource to incident
View dispatch
Update dispatch status
Cancel dispatch

View history
Generate operational reports
```

---

# 48. Application Layer Summary

The complete application flow is:

```text
                    +------------------+
                    |   Browser / API  |
                    +--------+---------+
                             |
                             v
                    +------------------+
                    |    Controller    |
                    | Servlet / REST  |
                    +--------+---------+
                             |
                             v
                    +------------------+
                    |     Service      |
                    +--------+---------+
                             |
             +---------------+---------------+
             |                               |
             v                               v
      +-------------+                +---------------+
      |   Domain    |                | Dispatch      |
      |   Objects   |                | Engine        |
      +------+------+                +-------+-------+
             |                               |
             +---------------+---------------+
                             |
                             v
                    +------------------+
                    | Repository / DAO |
                    +--------+---------+
                             |
                             v
                    +------------------+
                    |   PostgreSQL     |
                    +------------------+
```

---

# 49. Evolution Toward Spring

The initial implementation intentionally uses Servlets and straightforward controllers.

Later, Spring MVC/REST can replace much of the manual infrastructure:

```text
Current:

Servlet
   -> Service
   -> Repository

Later:

Spring Controller
   -> Service
   -> Repository
```

The business layer should remain recognizable.

This gives the project a useful before-and-after comparison of what Spring actually provides.

---

# 50. Document Status

**Document 10 — API & Application Flow Design**

Status: Completed as the initial application/API baseline.

This document will guide the implementation of:

* Servlet controllers
* JSP/MVC flow
* REST endpoints
* request/response DTOs
* HTTP status handling
* validation
* error responses
* service integration
* JSON handling
* API testing
* later Spring MVC/REST migration