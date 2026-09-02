# Java Backend Labs

A milestone-based Java backend project that evolves the same application from Core Java to Spring Boot.

Instead of copying tutorial projects, each milestone upgrades the architecture and infrastructure of **EventFlow**, an event discovery and ticket booking application.

## EventFlow

Core features:

- Event search and sorting
- Ticket booking and cancellation
- Pricing and discount rules
- Inventory management
- Sales reporting
- CLI workflows
- Automated testing
- Concurrent booking protection

## Current Milestone

### M2 - PostgreSQL + JDBC 🚧

Replacing in-memory persistence with PostgreSQL using plain JDBC.

Current focus:

- PostgreSQL schema design
- `Connection`, `PreparedStatement`, and `ResultSet`
- JDBC repository implementations
- SQL constraints and foreign keys
- Manual transaction management
- Commit / rollback behavior

## Completed

### M1 - EventFlow Core ✅

Built the framework-free Java version of EventFlow with:

- Domain-oriented design
- Repository abstractions
- In-memory persistence
- Pricing policies
- Booking workflows
- Event search
- Sales reporting
- Command-line interface
- JUnit 5 tests
- Single-JVM concurrency protection using `synchronized`, `ConcurrentHashMap`, `ExecutorService`, and `CountDownLatch`

Release: `v0.1.0`

## Tech Stack

**Current**

- Java 21
- Maven
- JUnit 5
- PostgreSQL
- JDBC
- Git / GitHub

**Next**

- Hibernate / JPA
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
Domain
 ↓
Repository Interfaces
 ↓
In-Memory / JDBC Implementations
 ↓
PostgreSQL
```

The business layer depends on repository abstractions, allowing persistence to evolve without redesigning the core workflows.

## Project Evolution

```text
M1  Java + In-Memory + Testing + Concurrency
 ↓
M2  PostgreSQL + JDBC
 ↓
M3  Hibernate / JPA
 ↓
M4  Spring Core
 ↓
M5+ Spring Boot + REST + Security + Docker
```

Example database URL:

```text
jdbc:postgresql://localhost:5432/eventflow
```

Database credentials are not stored in the repository.

## Build and Test

Run the test suite with:

```bash
mvn clean verify
```

## Database Configuration

Database credentials are provided through environment variables and are never committed to Git:

```text
DB_URL
DB_USER
DB_PASSWORD
```

## Status

🚧 M2 - JDBC Persistence in progress
