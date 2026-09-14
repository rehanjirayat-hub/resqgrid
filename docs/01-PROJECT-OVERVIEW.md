# ResQGrid — Real-Time Emergency Response & Resource Dispatch Engine

## 1. Project Overview

ResQGrid is a backend-focused emergency response and resource dispatch system designed to simulate how emergency incidents can be received, assessed, prioritized, and handled using available response resources.

The system manages incidents such as medical emergencies, fires, rescue operations, water rescues, and hazardous-material situations. Based on the nature and severity of an incident, ResQGrid determines what type of resources and capabilities are required and selects suitable available resources for dispatch.

The primary focus of the project is the backend system and its business logic. The project emphasizes object-oriented design, database persistence, concurrency control, testing, and maintainable architecture rather than building a complex frontend.

## 2. Problem Statement

Emergency response systems need to make decisions quickly while dealing with limited resources and multiple incidents at the same time.

A simple system that assigns the first available resource is not sufficient. A useful dispatch engine needs to consider factors such as:

* Incident severity
* Incident type
* Required capabilities
* Resource type
* Resource availability
* Distance from the incident
* Existing workload
* Response team availability
* Operational constraints
* Concurrent dispatch requests

The system must also prevent the same resource from being assigned to multiple incidents at the same time.

ResQGrid provides a controlled environment for implementing and testing these problems using Java and a relational database.

## 3. Project Objectives

The main objectives of ResQGrid are:

1. Build a realistic Java backend application using object-oriented design.
2. Model emergency incidents, resources, response teams, locations, dispatches, and history.
3. Implement business rules for incident assessment and resource dispatch.
4. Select resources using multiple ranking criteria instead of simple first-match selection.
5. Handle concurrent dispatch requests safely.
6. Persist application data using PostgreSQL and SQL.
7. Implement database access using JDBC before introducing higher-level persistence with JPA/Hibernate.
8. Expose application functionality through REST APIs.
9. Build a web layer using Servlets, JSP, and MVC concepts.
10. Write automated tests using JUnit 5.
11. Apply clean code, maintainable architecture, validation, exception handling, and proper separation of responsibilities.
12. Later compare manually implemented backend concerns with the solutions provided by Spring and Spring Boot.

## 4. Scope

### 4.1 In Scope

ResQGrid will include:

* Incident creation and management
* Incident severity and status handling
* Incident type classification
* Location management
* Emergency resource management
* Resource types and capabilities
* Resource availability tracking
* Response team management
* Resource selection and ranking
* Dispatch creation
* Dispatch lifecycle management
* Dispatch history
* Concurrent dispatch handling
* PostgreSQL database persistence
* JDBC-based data access
* Hibernate/JPA-based persistence
* REST APIs
* Servlet/JSP-based web interface
* Validation and exception handling
* Unit and integration testing
* Reporting and basic operational analytics
* Git/GitHub-based version control

### 4.2 Out of Scope Initially

The initial implementation will not focus on:

* Microservices
* Kubernetes
* Kafka
* Redis
* Elasticsearch
* Cloud infrastructure
* GraphQL
* NoSQL databases
* Complex frontend frameworks such as React or Angular

These technologies may only be considered later if the project requirements justify them.

## 5. Major Capabilities

### 5.1 Incident Management

The system manages emergency incidents.

An incident contains information such as:

* Incident type
* Description
* Severity
* Location
* Current status
* Reported time

### 5.2 Resource Management

The system manages emergency resources such as:

* Ambulances
* Fire units
* Rescue teams
* Police units
* Medical teams
* Helicopters

Each resource has a status and one or more capabilities.

### 5.3 Response Team Management

Response teams represent personnel responsible for operating or supporting emergency resources.

Teams have their own availability status and capabilities.

### 5.4 Resource Selection

The dispatch engine identifies resources that satisfy the requirements of an incident.

Selection can consider:

* Required resource type
* Required capability
* Availability
* Distance
* Priority
* Workload
* Operational constraints

### 5.5 Dispatch Management

A dispatch connects an incident with the resources and response team assigned to handle it.

The dispatch has its own lifecycle and status.

### 5.6 History

Important operational actions are recorded so that the system can maintain a history of changes and dispatch activity.

### 5.7 Reporting

The system provides information useful for understanding:

* Incident activity
* Resource utilization
* Dispatch activity
* Response performance
* Operational history

## 6. High-Level Workflow

A typical emergency response flow is:

```text
Incident Reported
       |
       v
Incident Assessed
       |
       v
Determine Requirements
       |
       v
Find Available Resources
       |
       v
Filter by Type and Capability
       |
       v
Rank Suitable Resources
       |
       v
Select Resource and Response Team
       |
       v
Create Dispatch
       |
       v
Dispatch In Progress
       |
       v
Incident Resolved
```

If no suitable resource is available, the system must handle that situation according to the defined business rules instead of incorrectly assigning an unavailable resource.

## 7. Technology Direction

The project will be developed primarily using:

* Java 21
* Maven
* PostgreSQL
* SQL
* JDBC
* Hibernate
* JPA
* Servlets
* JSP
* MVC
* REST APIs
* JSON
* JUnit 5
* Apache Tomcat
* Git
* GitHub
* HTML, CSS, and basic JavaScript

Spring and Spring Boot will be introduced later in the project after the underlying concepts have been implemented manually.

This allows the project to demonstrate what problems frameworks solve rather than using frameworks without understanding the underlying mechanisms.

## 8. Architectural Direction

The application will follow a layered architecture with responsibilities separated between major areas such as:

```text
Presentation
     |
Controller
     |
Service / Business Logic
     |
Domain
     |
Repository / Data Access
     |
Persistence
     |
PostgreSQL
```

The exact package and class structure will be defined during implementation rather than creating unnecessary layers in advance.

## 9. Expected Outcome

The final result should be a maintainable backend system that demonstrates practical Java development rather than a collection of isolated examples.

By completing ResQGrid, the project should demonstrate the ability to:

* Design domain models
* Apply object-oriented programming
* Implement business rules
* Work with Java collections
* Use streams and functional programming where appropriate
* Handle exceptions and validation
* Implement thread-safe operations
* Work with PostgreSQL and SQL
* Use JDBC
* Work with JPA/Hibernate
* Design REST APIs
* Build MVC applications
* Write automated tests
* Reason about concurrency and database consistency
* Design maintainable backend architecture
* Understand the problems solved by Spring and Spring Boot

The project should ultimately be something that can be explained, tested, maintained, and extended like a real backend system.
