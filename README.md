# Java Backend Labs

A milestone-based Java backend project that evolves the application from Core Java to Spring Boot.

**EventFlow** is an event discovery and ticket booking application. Each milestone replaces or extends part of the architecture instead of starting a new tutorial project.

## EventFlow

Main features:

- Event search and sorting
- Ticket booking and cancellation
- Pricing and discounts
- Inventory management
- Sales reporting
- CLI workflows
- Automated testing
- Concurrency protection

## Current Milestone

### M4 - Spring Core 🚧

Moving application wiring and transaction management to Spring.

## Completed Milestones

### M3 - Hibernate / JPA ✅

- Replaced manual JDBC persistence with Hibernate/JPA
- Added JPA entity mappings and relationships
- Implemented Hibernate repositories
- Added transactional booking persistence
- Added optimistic locking with `@Version`
- Added Hibernate integration and concurrency tests

Tag: `v0.3.0`

### M2 - PostgreSQL + JDBC ✅

- Added PostgreSQL persistence
- Implemented JDBC repositories
- Added schema constraints and foreign keys
- Implemented manual transaction management with commit/rollback
- Added JDBC integration tests

Tag: `v0.2.0`

### M1 - EventFlow Core ✅

- Domain and service layers
- Repository abstractions with in-memory persistence
- Pricing, search, booking, cancellation, and reporting
- CLI application
- JUnit 5 tests
- Single-JVM concurrency protection

Tag: `v0.1.0`

## Tech Stack

**Current**

- Java 21
- Maven
- JUnit 5
- PostgreSQL
- Hibernate / JPA
- Git / GitHub

**Next**

- Spring Framework
- Spring Boot
- REST APIs
- Spring Data JPA
- Spring Security
- Docker

## Architecture

```text
CLI
 ↓
Services
 ↓
Domain / JPA Entities
 ↓
Repository Interfaces
 ↓
Hibernate / JPA
 ↓
PostgreSQL
```

The persistence layer has evolved from in-memory storage to JDBC and then Hibernate/JPA while keeping the core booking workflows largely independent from infrastructure.

## Project Evolution

```text
M1  Core Java + In-Memory              v0.1.0
 ↓
M2  PostgreSQL + JDBC                  v0.2.0
 ↓
M3  Hibernate / JPA                    v0.3.0
 ↓
M4  Spring Core
 ↓
M5+ Spring Boot + REST + Security + Docker
```

Previous milestones can be checked out directly by tag:

```bash
git checkout v0.1.0
git checkout v0.2.0
git checkout v0.3.0
```

For example, the complete JDBC version is available at:

```bash
git checkout v0.2.0
```

## Build and Test

```bash
mvn clean verify
```

Database-backed integration tests require PostgreSQL and the following environment variables:

```text
DB_URL
DB_USER
DB_PASSWORD
```

Database credentials are not committed to Git.

## Status

🚧 M4 - Spring Core in progress