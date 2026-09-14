# ResQGrid — Actors and Use Cases

## 1. Purpose

This document defines the actors that interact with ResQGrid and the use cases available to those actors.

It establishes the system-level interactions that later guide:

* API design.
* Controller responsibilities.
* Service responsibilities.
* Authorization decisions.
* Testing scenarios.
* User-interface operations.

This document does not define implementation details.

---

## 2. Actors

### 2.1 Emergency Operator

The Emergency Operator is the primary human user responsible for managing emergency incidents and coordinating response operations.

The Emergency Operator may:

* Report incidents.
* Review incident information.
* Assess incidents.
* Determine response requirements.
* View available resources.
* Initiate dispatch operations.
* Monitor dispatch status.
* Update operational information.
* Resolve incidents.
* Review relevant dispatch history.
* View operational reports.

---

### 2.2 Resource Coordinator

The Resource Coordinator is responsible for maintaining emergency-resource information and operational availability.

The Resource Coordinator may:

* Register resources.
* View resources.
* Update resource information.
* Update resource operational status.
* Manage resource capabilities.
* Review resource assignments.
* Review resource workload.
* Manage response-team information where applicable.

---

### 2.3 System Administrator

The System Administrator is responsible for system-level administration.

The System Administrator may:

* Manage system configuration where required.
* Manage operational data where authorized.
* Review system information.
* Support resource and team administration.
* Monitor system-level problems.

Detailed authentication and authorization behavior is outside the current use-case specification unless explicitly added to the requirements documentation.

---

### 2.4 Reporting User

The Reporting User consumes operational information for analysis and reporting.

The Reporting User may:

* View incident statistics.
* View dispatch statistics.
* View resource utilization.
* View resource workload information.
* Review historical dispatch information.

The Reporting User does not directly perform dispatch operations unless another role explicitly grants that capability.

---

### 2.5 ResQGrid System

The ResQGrid System itself acts as an automated participant in operational workflows.

The system shall:

* Validate incoming operations.
* Evaluate incident requirements.
* Identify eligible resources.
* Filter unsuitable resources.
* Rank eligible resources.
* Select an appropriate resource.
* Create dispatch records.
* Maintain resource and dispatch state.
* Enforce documented business rules.
* Protect shared resource state during concurrent operations.
* Maintain historical information.
* Generate operational reports from stored data.

---

## 3. Actor Responsibility Summary

| Actor                | Primary Responsibility                                                            |
| -------------------- | --------------------------------------------------------------------------------- |
| Emergency Operator   | Incident handling and response coordination                                       |
| Resource Coordinator | Resource and team management                                                      |
| System Administrator | System-level administration                                                       |
| Reporting User       | Operational reporting and analysis                                                |
| ResQGrid System      | Automated validation, selection, dispatch, state management, and rule enforcement |

---

# 4. Use Cases

## UC-01 — Report Incident

### Primary Actor

Emergency Operator

### Goal

Create a new emergency incident in the system.

### Preconditions

* The actor has permission to report an incident.
* Required incident information is available.

### Main Flow

1. Emergency Operator initiates incident reporting.
2. The system receives incident information.
3. The system validates the supplied information.
4. The system creates the incident.
5. The system assigns the appropriate initial incident state.
6. The incident becomes available for assessment and response processing.

### Alternative / Failure Conditions

* Required information is missing.
* Supplied information is invalid.
* Incident creation fails because of a persistence problem.

---

## UC-02 — Assess Incident

### Primary Actor

Emergency Operator

### Goal

Evaluate an incident and determine its response requirements.

### Main Flow

1. Operator selects an incident.
2. System displays incident information.
3. Operator assesses the incident.
4. Severity is determined or updated.
5. Required resource characteristics are determined.
6. Incident becomes eligible for resource-selection processing.

### Related Requirements

* Incident Management.
* Incident Classification.
* Validation.
* Dispatch Selection.

---

## UC-03 — View Incident

### Primary Actor

Emergency Operator

### Goal

Retrieve information about an incident.

### Main Flow

1. Operator requests an incident.
2. System identifies the requested incident.
3. System retrieves persisted information.
4. System returns incident information.

The system shall handle requests for incidents that do not exist according to the Error Handling Design.

---

## UC-04 — Update Incident

### Primary Actor

Emergency Operator

### Goal

Modify incident information when the current incident state permits modification.

### Main Flow

1. Operator selects an incident.
2. System verifies that the incident exists.
3. System verifies that the requested update is permitted.
4. System validates the new information.
5. System persists the update.

Invalid state transitions or prohibited modifications shall be rejected.

---

## UC-05 — View Available Resources

### Primary Actor

Emergency Operator

### Goal

Determine which emergency resources are currently available.

### Main Flow

1. Operator requests available resources.
2. System evaluates resource operational status.
3. System identifies resources eligible for availability.
4. System returns the appropriate resource information.

Availability shall follow the documented business rules rather than relying only on a simple status check where additional constraints apply.

---

## UC-06 — Register Resource

### Primary Actor

Resource Coordinator

### Goal

Add an emergency resource to the system.

### Main Flow

1. Coordinator provides resource information.
2. System validates the information.
3. System creates the resource.
4. Resource becomes available for future operational use according to its initial state.

---

## UC-07 — Update Resource

### Primary Actor

Resource Coordinator

### Goal

Modify resource information.

### Main Flow

1. Coordinator selects a resource.
2. System retrieves the resource.
3. System validates the requested changes.
4. System updates the resource.
5. Updated resource information is persisted.

---

## UC-08 — Change Resource Status

### Primary Actor

Resource Coordinator

### Goal

Update the operational state of a resource.

### Main Flow

1. Coordinator selects a resource.
2. Coordinator requests a status change.
3. System validates the requested transition.
4. System updates the resource state.
5. Updated state is persisted.

The system shall prevent invalid status transitions defined by the Business Rules document.

---

## UC-09 — Manage Resource Capabilities

### Primary Actor

Resource Coordinator

### Goal

Maintain the capabilities associated with a resource.

### Main Flow

1. Coordinator selects a resource.
2. Coordinator views current capabilities.
3. Coordinator adds, removes, or updates capabilities where permitted.
4. System validates the operation.
5. System persists the updated capability information.

Capabilities shall be used by the dispatch engine when determining resource eligibility.

---

## UC-10 — Manage Response Team

### Primary Actor

Resource Coordinator

### Goal

Maintain response-team information required for emergency operations.

### Main Flow

1. Coordinator selects response-team management.
2. Coordinator creates or updates team information.
3. System validates the operation.
4. System persists the team information.
5. Team availability and capabilities become available to appropriate operational processes.

---

## UC-11 — Find Suitable Resources

### Primary Actor

Emergency Operator

### Supporting Actor

ResQGrid System

### Goal

Identify resources that can respond to an incident.

### Main Flow

1. Operator requests resource selection for an incident.
2. System retrieves incident requirements.
3. System identifies candidate resources.
4. System filters resources that fail mandatory requirements.
5. System evaluates eligible resources.
6. System ranks the eligible resources.
7. System produces the best available selection according to documented dispatch rules.

The system shall not simply select the first available resource.

---

## UC-12 — Dispatch Resource

### Primary Actor

Emergency Operator

### Supporting Actor

ResQGrid System

### Goal

Assign a suitable emergency resource to an incident.

### Main Flow

1. Operator initiates dispatch.
2. System evaluates incident requirements.
3. System identifies eligible resources.
4. System ranks eligible resources.
5. System selects the appropriate resource.
6. System verifies that the resource can still be assigned.
7. System creates the dispatch.
8. System updates the relevant resource state.
9. Dispatch enters its appropriate initial state.

The final assignment operation shall respect concurrency rules.

---

## UC-13 — Concurrent Dispatch Processing

### Primary Actor

ResQGrid System

### Goal

Process multiple dispatch requests without assigning the same exclusive resource incorrectly.

### Scenario

Two incidents may request the same available resource at approximately the same time.

### Expected Behavior

1. Multiple dispatch operations may execute concurrently.
2. Each operation evaluates available resources.
3. Shared resource state is protected.
4. Only valid assignment operations succeed.
5. A resource cannot be successfully assigned to incompatible concurrent dispatches when only one assignment is permitted.
6. Database and application state remain consistent.

The exact synchronization, locking, and transaction strategy shall be defined in the Concurrency Design document.

---

## UC-14 — Monitor Dispatch

### Primary Actor

Emergency Operator

### Goal

Track the current state of a dispatch.

### Main Flow

1. Operator requests dispatch information.
2. System retrieves the dispatch.
3. System returns current dispatch state and relevant information.
4. Operator can determine the current stage of the response.

---

## UC-15 — Complete Dispatch

### Primary Actor

Emergency Operator

### Goal

Complete an active dispatch when the response operation has finished.

### Main Flow

1. Operator identifies the active dispatch.
2. Operator initiates completion.
3. System verifies that completion is permitted.
4. System updates dispatch state.
5. System updates the associated resource state according to business rules.
6. System records relevant completion information.
7. Incident state is updated when appropriate.

---

## UC-16 — Resolve Incident

### Primary Actor

Emergency Operator

### Goal

Mark an incident as resolved after the required response process is complete.

### Main Flow

1. Operator selects the incident.
2. System verifies the incident's current state.
3. System verifies required response operations are complete.
4. System changes the incident to its resolved state.
5. System records the relevant information.

The exact conditions for resolution shall be defined in the Business Rules document.

---

## UC-17 — View Dispatch History

### Primary Actor

Emergency Operator

### Supporting Actor

Reporting User

### Goal

Review historical dispatch operations.

### Main Flow

1. Actor requests dispatch history.
2. System retrieves relevant historical information.
3. System returns dispatch records and associated information.

---

## UC-18 — View Resource Workload

### Primary Actor

Resource Coordinator

### Supporting Actor

Reporting User

### Goal

Review resource utilization and workload.

### Main Flow

1. Actor requests workload information.
2. System analyzes relevant dispatch information.
3. System calculates or retrieves workload information.
4. System returns the result.

---

## UC-19 — Generate Operational Reports

### Primary Actor

Reporting User

### Goal

Obtain operational statistics and analysis.

### Main Flow

1. Reporting User selects a report.
2. System retrieves the required operational data.
3. System performs the required calculations.
4. System produces the requested report.

Initial reporting areas include:

* Incident statistics.
* Dispatch statistics.
* Resource utilization.
* Resource workload.
* Historical dispatch activity.

---

## UC-20 — Handle Invalid Operation

### Primary Actor

ResQGrid System

### Goal

Prevent invalid operations from corrupting system state.

### Main Flow

1. An operation is requested.
2. System validates the operation.
3. System detects invalid input or an invalid business operation.
4. System rejects the operation.
5. System returns an appropriate error representation.
6. System maintains valid application and database state.

---

# 5. Use-Case Relationships

The following relationships describe the high-level workflow.

### Incident Processing

`Report Incident`

→ `View Incident`

→ `Assess Incident`

→ `Find Suitable Resources`

→ `Dispatch Resource`

→ `Monitor Dispatch`

→ `Complete Dispatch`

→ `Resolve Incident`

---

### Resource Processing

`Register Resource`

→ `Update Resource`

→ `Manage Resource Capabilities`

→ `Change Resource Status`

→ Resource becomes eligible or ineligible for dispatch.

---

### Dispatch Processing

`Find Suitable Resources`

→ `Filter Resources`

→ `Rank Resources`

→ `Dispatch Resource`

→ `Monitor Dispatch`

→ `Complete Dispatch`

→ `Update Resource State`

→ `Record Dispatch History`

---

# 6. System-Level Use-Case Rules

### Rule UC-R01

The system shall enforce documented business rules regardless of whether the operation originates from:

* REST API.
* Web interface.
* Internal application operation.

### Rule UC-R02

Core dispatch decisions shall be performed by the business/service layer rather than directly by controllers or JSP views.

### Rule UC-R03

A controller shall coordinate application requests and responses but shall not contain the core dispatch algorithm.

### Rule UC-R04

The dispatch engine shall not blindly trust a resource's previous availability state.

The final assignment must verify that the resource can still be assigned.

### Rule UC-R05

Concurrent dispatch operations must be handled according to the Concurrency Design document.

### Rule UC-R06

Use cases shall not introduce domain fields, statuses, or business rules that are not defined by the project's documentation.

---

# 7. Actor-to-Use-Case Matrix

| Use Case                       | Emergency Operator | Resource Coordinator | System Administrator | Reporting User | System |
| ------------------------------ | :----------------: | :------------------: | :------------------: | :------------: | :----: |
| Report Incident                |          ✓         |                      |                      |                |    ✓   |
| Assess Incident                |          ✓         |                      |                      |                |    ✓   |
| View Incident                  |          ✓         |                      |           ✓          |                |    ✓   |
| Update Incident                |          ✓         |                      |           ✓          |                |    ✓   |
| View Available Resources       |          ✓         |           ✓          |           ✓          |                |    ✓   |
| Register Resource              |                    |           ✓          |           ✓          |                |    ✓   |
| Update Resource                |                    |           ✓          |           ✓          |                |    ✓   |
| Change Resource Status         |                    |           ✓          |           ✓          |                |    ✓   |
| Manage Resource Capabilities   |                    |           ✓          |           ✓          |                |    ✓   |
| Manage Response Team           |                    |           ✓          |           ✓          |                |    ✓   |
| Find Suitable Resources        |          ✓         |                      |                      |                |    ✓   |
| Dispatch Resource              |          ✓         |                      |                      |                |    ✓   |
| Concurrent Dispatch Processing |                    |                      |                      |                |    ✓   |
| Monitor Dispatch               |          ✓         |                      |                      |                |    ✓   |
| Complete Dispatch              |          ✓         |                      |                      |                |    ✓   |
| Resolve Incident               |          ✓         |                      |                      |                |    ✓   |
| View Dispatch History          |          ✓         |                      |                      |        ✓       |    ✓   |
| View Resource Workload         |                    |           ✓          |                      |        ✓       |    ✓   |
| Generate Operational Reports   |                    |                      |                      |        ✓       |    ✓   |
| Handle Invalid Operation       |                    |                      |                      |                |    ✓   |

---

# 8. Scope Boundary

This document defines actor interactions and use cases at a system level.

It does not yet define:

* Exact database tables.
* Exact entity fields.
* Exact Java classes.
* Exact package structure.
* Exact API URLs.
* Exact SQL queries.
* Exact Hibernate mappings.
* Exact concurrency implementation.
* Exact dispatch scoring formula.

Those decisions belong to their respective project documents.

Any implementation decision that affects the use cases must remain consistent with this document.

---

# 9. Documentation Consistency Rule

This document must remain consistent with:

* `01-PROJECT-OVERVIEW.md`
* `02-REQUIREMENTS-SPECIFICATION.md`
* `04-BUSINESS-RULES.md`
* `05-DOMAIN-MODEL.md`
* `06-SYSTEM-ARCHITECTURE.md`
* `07-DATABASE-DESIGN.md`
* `08-DISPATCH-ENGINE-DESIGN.md`
* `09-CONCURRENCY-DESIGN.md`
* `10-API-DESIGN.md`
* `11-ERROR-HANDLING-DESIGN.md`
* `12-TESTING-STRATEGY.md`
* `13-DEVELOPMENT-ROADMAP.md`

If a conflict is discovered, implementation shall stop until the affected documentation is reviewed and updated.
