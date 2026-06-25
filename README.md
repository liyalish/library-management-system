# Library Management System

A web-based Library Management System for managing book catalogs, book requests, physical book copies, users, and role-based workflows.

The application allows readers to browse and request books, librarians to issue, reject, and approve returns, and administrators to manage user accounts.

This is a final Java course project built with the required stack: **Servlets, Spring Core, Spring MVC, Spring Security, Thymeleaf, and plain JDBC**.
The project is deployed as a classic **WAR** application and does not use Spring Boot or ORM frameworks.

---

## Features

### Reader

* Browse the public book catalog
* Search books by title
* Filter books by author and genre
* View book descriptions and available copy count
* Request books for home use or reading room use
* Cancel pending requests
* Return issued books
* View request history and statuses
* Delete own account if there are no active requests

### Librarian

* Add, edit, and delete books
* View all book requests
* Issue pending requests
* Reject pending requests
* Approve book returns
* Manage copy availability through request workflows

### Administrator

* View and manage users
* Create librarian accounts
* Block and unblock readers and librarians
* Delete librarian accounts
* View all book requests in read-only mode

---

## Request Workflow

The system manages physical book copies using the `book_copies` table.

```text
Reader sends request
        ↓
Available copy becomes RESERVED
        ↓
Available copy counter decreases immediately
        ↓
Librarian can issue or reject the request
```

Main request statuses:

| Status           | Meaning                             |
| ---------------- | ----------------------------------- |
| `PENDING`        | Reader submitted a request          |
| `ISSUED`         | Librarian issued the book           |
| `PENDING_RETURN` | Reader requested to return the book |
| `RETURNED`       | Librarian approved the return       |
| `CANCELLED`      | Reader cancelled the request        |
| `REJECTED`       | Librarian rejected the request      |

Copy statuses:

| Status      | Meaning                                |
| ----------- | -------------------------------------- |
| `AVAILABLE` | Copy can be requested                  |
| `RESERVED`  | Copy is reserved for a pending request |
| `ISSUED`    | Copy is currently issued to a reader   |

---

## Tech Stack

* Java 17
* Spring Framework 6.2
* Spring Core
* Spring MVC
* Spring Security
* Thymeleaf
* Bootstrap 5
* Plain JDBC
* PostgreSQL
* Custom connection pool
* BCrypt password hashing through Spring Security `PasswordEncoder`
* Log4j2 via SLF4J
* JUnit 5
* Mockito
* JaCoCo
* Maven
* Apache Tomcat 10.1+

---

## Architecture

The project follows a layered MVC architecture:

```text
Controller  →  Service  →  DAO  →  PostgreSQL
```

### Main packages

| Package      | Purpose                                                                        |
| ------------ | ------------------------------------------------------------------------------ |
| `config`     | Spring MVC, root configuration, Spring Security configuration, web initializer |
| `controller` | Spring MVC controllers                                                         |
| `service`    | Business logic and service interfaces                                          |
| `dao`        | JDBC DAO interfaces and implementations                                        |
| `model`      | Domain models and form objects                                                 |
| `security`   | Spring Security user loading from the database                                 |
| `util`       | Connection pool, properties loader, helper utilities                           |
| `exception`  | Custom application exceptions                                                  |

---

## Security

Authentication and authorization are handled by **Spring Security**.

Implemented security features:

* Form-based login
* Role-based URL access through `SecurityFilterChain`
* Method-level authorization through `@PreAuthorize`
* User loading from the database through `CustomUserDetailsService`
* Password hashing with `BCryptPasswordEncoder`
* Blocked account handling
* Protected endpoints for reader, librarian, and administrator areas

Roles are stored in the database as:

```text
READER
LIBRARIAN
ADMIN
```

Spring Security maps them internally to:

```text
ROLE_READER
ROLE_LIBRARIAN
ROLE_ADMIN
```

---

## User Roles

| Role        | Capabilities                                                                                        |
| ----------- | --------------------------------------------------------------------------------------------------- |
| `READER`    | Browse catalog, request books, cancel own pending requests, return issued books, delete own account |
| `LIBRARIAN` | Manage books, issue requests, reject requests, approve returns                                      |
| `ADMIN`     | Manage users, create librarians, block/unblock users, delete librarians, view requests              |

---

## Database

The database is normalized and uses foreign keys to preserve data integrity.

Main tables:

| Table           | Purpose                                             |
| --------------- | --------------------------------------------------- |
| `users`         | Stores system users and roles                       |
| `authors`       | Stores book authors                                 |
| `genres`        | Stores book genres                                  |
| `books`         | Stores book catalog records                         |
| `book_authors`  | Many-to-many relationship between books and authors |
| `book_genres`   | Many-to-many relationship between books and genres  |
| `book_copies`   | Stores physical copies of books                     |
| `book_requests` | Stores reader requests and lending workflow         |

The available copy count is not stored directly in `books`. It is calculated from `book_copies` by counting copies with status `AVAILABLE`.

---

## Prerequisites

Before running the project, install:

* JDK 17 or higher
* Maven 3.9 or higher
* PostgreSQL 14 or higher
* Apache Tomcat 10.1 or higher

---

## Setup

### 1. Create database

```sql
CREATE DATABASE library_new ENCODING 'UTF8';
```

### 2. Run database scripts

```bash
psql -U postgres -d library_new -f db/schema.sql
psql -U postgres -d library_new -f db/data.sql
```

### 3. Configure database connection

Update:

```text
src/main/resources/application.properties
```

Example:

```properties
db.url=jdbc:postgresql://localhost:5432/library_new?characterEncoding=UTF-8
db.username=postgres
db.password=1234
db.driver=org.postgresql.Driver
db.pool.size=10
app.default.locale=en
```

---

## Build and Run

Build the project:

```bash
mvn clean package
```

Deploy the generated WAR file:

```text
target/library.war
```

to Apache Tomcat.

Then open:

```text
http://localhost:8080/library/
```

---

## Demo Accounts

Default password for all demo accounts:

```text
password
```

| Username    | Role        |
| ----------- | ----------- |
| `admin`     | `ADMIN`     |
| `librarian` | `LIBRARIAN` |
| `reader`    | `READER`    |

---

## Testing

Run all tests:

```bash
mvn test
```

Build with tests:

```bash
mvn clean package
```

JaCoCo coverage report is generated at:

```text
target/site/jacoco/index.html
```

The test suite covers:

* Book service logic
* Request service logic
* User service logic
* Spring Security user loading
* BCrypt password encoding
* Request status validation
* User role restrictions
* Account deletion rules
* Request cancellation, rejection, issuing, and return flow

---

## Internationalization

The interface supports English and Russian.

Language can be switched using:

```text
?lang=en
?lang=ru
```

Message files are stored in:

```text
src/main/resources/i18n/messages_en.properties
src/main/resources/i18n/messages_ru.properties
```

---

## UI Design

The project includes UI design documentation and screenshots in:

```text
docs/ui/
```

UI design materials include:

* Application purpose
* User objectives
* Basic and advanced scenarios
* Wireframes
* Mockups
* Implemented HTML/Thymeleaf web version

---

## Key Implementation Notes

* The project uses classic Spring MVC configuration and is deployed as a WAR file.
* Spring Security replaces manual authentication and authorization logic.
* `CustomUserDetailsService` loads users from the database for Spring Security.
* `SecurityConfig` defines URL-level access rules and login/logout behavior.
* Passwords are stored as BCrypt hashes.
* All database operations use `PreparedStatement`.
* DAO classes use plain JDBC without Hibernate or JPA.
* Book issuing, rejecting, cancelling, and returning use transactions where multiple rows must be updated atomically.
* The custom connection pool uses a thread-safe `BlockingQueue`.
* Connections are proxied so calling `close()` returns the connection to the pool instead of closing the real database connection.
* Thymeleaf escapes output by default, reducing XSS risk.
* State-changing actions use Post-Redirect-Get to prevent duplicate form submissions.

---

## Design Patterns Used

| Pattern              | Usage                                            |
| -------------------- | ------------------------------------------------ |
| MVC                  | Separates controllers, views, and business logic |
| DAO                  | Encapsulates database access                     |
| Service Layer        | Keeps business rules outside controllers         |
| Singleton            | Used for the custom connection pool              |
| Builder              | Used for creating `BookRequest` objects          |
| Dependency Injection | Spring injects DAO and service dependencies      |

---

## Project Structure

```text
src/
 ├── main/
 │   ├── java/com/library/
 │   │   ├── config/
 │   │   ├── controller/
 │   │   ├── dao/
 │   │   ├── exception/
 │   │   ├── model/
 │   │   ├── security/
 │   │   ├── service/
 │   │   └── util/
 │   ├── resources/
 │   │   ├── application.properties
 │   │   ├── i18n/
 │   │   └── log4j2.xml
 │   └── webapp/
 │       ├── css/
 │       └── WEB-INF/views/
 └── test/
     └── java/com/library/
```

---

## License

This project was created for educational purposes as a Java final course project.
