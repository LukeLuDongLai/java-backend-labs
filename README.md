# Java Backend Labs

A milestone-based Java backend project built to practice backend development by evolving the same application step by step.

The current project is **EventFlow**, a simple event booking system. It started as an in-memory Java application and is gradually being extended with PostgreSQL, JDBC, Hibernate/JPA, Spring, REST APIs, security, and deployment.

## EventFlow

EventFlow currently supports:

- Event creation and publishing
- Event search and sorting
- Ticket booking and cancellation
- Pricing rules and discounts
- Ticket inventory management
- Sales reporting
- PostgreSQL persistence
- Transactional booking operations
- Basic concurrency protection for the in-memory implementation

## Current Milestone

### M3 - Hibernate / JPA 🚧

The next step is replacing the hand-written JDBC persistence layer with Hibernate/JPA while keeping the existing domain and service structure.

## Completed

### M2 - PostgreSQL + JDBC ✅

- Added PostgreSQL database schema
- Implemented JDBC repositories for events and bookings
- Added domain object rehydration from database records
- Added transaction handling for booking creation and cancellation
- Added JDBC integration tests
- Moved application persistence from memory to PostgreSQL
- Database configuration is provided through environment variables

Release: `v0.2.0`

### M1 - EventFlow Core ✅

- Core domain model for events and bookings
- Repository abstractions with in-memory implementations
- Pricing policy abstraction
- Event search and sorting
- Booking and cancellation workflows
- Sales reporting
- Command-line interface
- JUnit tests
- Single-JVM concurrency protection for ticket inventory

Release: `v0.1.0`

## Tech Stack

- Java 21
- Maven
- JUnit 5
- PostgreSQL
- JDBC
- Git / GitHub

Planned:

- Hibernate / JPA
- Spring Framework
- Spring Boot
- REST APIs
- Spring Data JPA
- Spring Security
- Docker

## Project Structure

```text
src/main/java/com/lukeludonglai/eventflow
├── app
├── cli
├── database
├── domain
├── exception
├── persistence
├── pricing
├── report
├── repository
├── search
└── service
```

The application is separated into domain, service, repository, persistence, and CLI layers so that infrastructure can change without rewriting the core business logic.

For example, EventFlow started with in-memory repositories and later switched to JDBC/PostgreSQL while keeping most of the service layer unchanged.

## Database

The PostgreSQL schema is stored in:

```text
db/schema.sql
```

The application expects the following environment variables:

```text
DB_URL
DB_USER
DB_PASSWORD
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

JDBC integration tests require a running PostgreSQL database and the database environment variables to be available to the Maven process.

## Project Evolution

```text
M1  Core Java + In-Memory
        ↓
M2  PostgreSQL + JDBC
        ↓
M3  Hibernate / JPA
        ↓
M4  Spring
        ↓
M5+ Spring Boot + REST + Security + Docker
```

The goal is to keep evolving the same application instead of creating a new tutorial project for every technology.