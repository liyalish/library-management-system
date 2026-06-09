# Library Management System

A web application for managing a library. Readers browse the catalog and request books,
librarians process lending and returns, and administrators manage users and the catalog.

Final Java course project, built with the required stack: **Servlets, Spring Core,
Spring MVC, and plain JDBC** (no ORM, no Spring Boot).

## Tech stack

- Java 17
- Spring Framework 6.2 (Core + MVC) — classic configuration, deployed as a WAR
- Plain JDBC with a custom thread-safe connection pool (no Hibernate/JPA)
- PostgreSQL
- Thymeleaf (server-side templates) + Bootstrap 5 (responsive UI)
- BCrypt (password hashing)
- Log4j2 via SLF4J (centralized logging)
- JUnit 5 + Mockito (unit tests) + JaCoCo (coverage)
- Maven (build)

## Architecture

Layered architecture following the MVC pattern:

```
Controller (Spring MVC)  ->  Service (business logic)  ->  DAO (JDBC)  ->  PostgreSQL
```

Packages:
- `config`      — Spring configuration (root context, MVC, web initializer)
- `controller`  — Spring MVC controllers
- `service`     — business logic (interfaces + implementations)
- `dao`         — data access (interfaces + JDBC implementations, DAO factory)
- `model`       — domain entities and form objects
- `interceptor` — authorization interceptor (role-based access)
- `util`        — connection pool, password hashing, helpers
- `exception`   — custom exceptions

## User roles

| Role      | Capabilities                                                        |
|-----------|---------------------------------------------------------------------|
| READER    | browse catalog, request books, view/cancel own requests             |
| LIBRARIAN | view all requests, issue books (set return date), mark returned     |
| ADMIN     | manage users (block/delete), manage catalog (CRUD), manage copies   |

## Database

7 normalized tables (3NF): `users`, `authors`, `genres`, `books`, `book_genres`,
`book_copies`, `book_requests`. Foreign keys enforce integrity; each physical copy has a
unique inventory number and a status (AVAILABLE / ISSUED / RESERVED).

## Prerequisites

- JDK 17+
- Maven 3.9+
- PostgreSQL 14+
- Apache Tomcat 10.1+ (Jakarta EE 10 / Servlet 6.0)

## Setup

1. Create the database:
   ```sql
   CREATE DATABASE library_db ENCODING 'UTF8';
   ```
2. Run the scripts in `db/`:
   ```bash
   psql -U postgres -d library_db -f db/schema.sql
   psql -U postgres -d library_db -f db/data.sql
   ```
3. Set your database credentials in `src/main/resources/application.properties`
   (`db.url`, `db.username`, `db.password`).

## Build and run

```bash
mvn clean package
```

Deploy `target/library.war` to Tomcat (or run via the IDE Tomcat configuration), then open:
```
http://localhost:8080/library/
```

### Demo accounts (password for all = `password`)

| Username  | Role      |
|-----------|-----------|
| admin     | ADMIN     |
| librarian | LIBRARIAN |
| reader    | READER    |

## Run tests

```bash
mvn test
```

Coverage report is generated at `target/site/jacoco/index.html`.

## Internationalization

The interface supports English and Russian; switch with the `?lang=en` / `?lang=ru`
parameter. Localized strings live in `src/main/resources/i18n/messages*.properties`.

## Key implementation notes

- **Connection pool**: custom, thread-safe, built on a `BlockingQueue`; connections are
  proxied so `close()` returns them to the pool instead of closing.
- **Security**: passwords are stored as BCrypt hashes; all SQL uses `PreparedStatement`;
  Thymeleaf escapes output (XSS); access control is enforced by a Spring interceptor.
- **Transactions**: issuing and returning a book are wrapped in JDBC transactions
  (commit/rollback) because they update multiple rows atomically.
- **Design patterns**: Singleton (connection pool, DAO factory), Factory Method
  (`DaoFactory`), Builder (`BookRequest.builder()`).
- **Validation**: server-side via Bean Validation annotations + `BindingResult`,
  client-side via HTML5 attributes.
- **Post-Redirect-Get**: state-changing actions redirect after POST to prevent duplicate
  submissions on refresh (F5).