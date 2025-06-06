# Architecture
This application is a service that manages a study abroad application portal. It is a server side rendered web application written fully in Java 21.
The application uses the Spring Framework and uses a layered architecture. The layers are as follows:

Controller Layer: This layer is responsible for handling incoming HTTP requests and sending the response back to the client. It is the entry point of the application.

Service Layer: This layer contains the business logic of the application. It is responsible for processing the data and interacting with the repository layer.

Repository Layer: This layer is responsible for interacting with the database. It contains the logic for querying the database and performing CRUD operations.

# Database Schema
The following schema was generated from the Spring Data JPA entities. The schema is for a PostgreSQL database.

## Table: `local_users`
| Column       | Type         | Constraints                      |
|-------------|-------------|----------------------------------|
| `username`  | VARCHAR(255) | PRIMARY KEY                     |
| `password`  | VARCHAR(255) | NOT NULL                        |
| `email`     | VARCHAR(255) | NOT NULL                        |
| `role`      | VARCHAR(255) | NOT NULL, ENUM(`STUDENT`, `ADMIN`) |
| `displayName` | VARCHAR(255) | NOT NULL                        |
| `theme`     | VARCHAR(255) | NOT NULL, ENUM(...)             |

---

## Table: `sso_users`
| Column       | Type         | Constraints                      |
|-------------|-------------|----------------------------------|
| `username`  | VARCHAR(255) | PRIMARY KEY                     |
| `email`     | VARCHAR(255) | NOT NULL                        |
| `role`      | VARCHAR(255) | NOT NULL, ENUM(`STUDENT`, `ADMIN`) |
| `displayName` | VARCHAR(255) | NOT NULL                        |
| `theme`     | VARCHAR(255) | NOT NULL, ENUM(...)             |

---

## Table: `applications`
| Column        | Type         | Constraints                                      |
|--------------|-------------|--------------------------------------------------|
| `id`         | VARCHAR(255) | PRIMARY KEY                                     |
| `student`    | VARCHAR(255) | NOT NULL, FOREIGN KEY (`local_users.username`)  |
| `programId`  | INT          | NOT NULL, FOREIGN KEY (`programs.id`)          |
| `dateOfBirth`| DATE         | NOT NULL                                        |
| `gpa`        | DOUBLE       | NOT NULL                                        |
| `major`      | VARCHAR(255) | NOT NULL                                        |
| `status`     | VARCHAR(255) | NOT NULL, ENUM (`APPLIED`, `ENROLLED`, `CANCELLED`, `WITHDRAWN`, `ELIGIBLE`, `APPROVED`) |

---

## Table: `responses`
| Column        | Type         | Constraints                                      |
|--------------|-------------|--------------------------------------------------|
| `applicationId` | VARCHAR(255) | NOT NULL, FOREIGN KEY (`applications.id`)    |
| `question`   | VARCHAR(255) | NOT NULL, ENUM (`WHY_THIS_PROGRAM`, `ALIGN_WITH_CAREER`, `ANTICIPATED_CHALLENGES`, `ADAPTED_TO_ENVIRONMENT`, `UNIQUE_PERSPECTIVE`) |
| `response`   | TEXT         | NOT NULL (length = 10000)                       |
| **Primary Key** | (`applicationId`, `question`) | Composite Primary Key |

---

## Table: `documents`
| Column          | Type         | Constraints                                      |
|----------------|-------------|--------------------------------------------------|
| `applicationId` | VARCHAR(255) | NOT NULL, FOREIGN KEY (`applications.id`)      |
| `type`         | VARCHAR(255) | NOT NULL, ENUM (`ASSUMPTION_OF_RISK`, `CODE_OF_CONDUCT`, `MEDICAL_HISTORY`, `HOUSING`) |
| `timestamp`    | TIMESTAMP    | NOT NULL                                        |
| `file`         | BLOB         | NOT NULL                                        |
| **Primary Key** | (`applicationId`, `type`) | Composite Primary Key |

---

## Table: `notes`
| Column          | Type         | Constraints                                      |
|----------------|-------------|--------------------------------------------------|
| `id`          | INT          | PRIMARY KEY, AUTO_INCREMENT (SEQUENCE)         |
| `applicationId` | VARCHAR(255) | NOT NULL, FOREIGN KEY (`applications.id`)      |
| `username`    | VARCHAR(255) | NOT NULL, FOREIGN KEY (`local_users.username`)  |
| `content`     | TEXT         | NOT NULL (length = 10000)                       |
| `timestamp`   | TIMESTAMP    | NOT NULL                                        |

---

## Table: `programs`
| Column              | Type         | Constraints                                     |
|---------------------|-------------|-----------------------------------------------|
| `id`               | INT          | PRIMARY KEY, AUTO_INCREMENT (SEQUENCE)       |
| `title`            | VARCHAR(255) | NOT NULL                                     |
| `year`             | INT          | NOT NULL (Stored as numeric year, e.g., 2025) |
| `semester`         | VARCHAR(255) | NOT NULL, ENUM (`FALL`, `SPRING`, `SUMMER`)  |
| `applicationOpen`  | DATE         | NOT NULL                                     |
| `applicationClose` | DATE         | NOT NULL                                     |
| `documentDeadline` | DATE         | NOT NULL                                     |
| `startDate`        | DATE         | NOT NULL                                     |
| `endDate`          | DATE         | NOT NULL                                     |
| `description`      | TEXT         | NOT NULL (length = 10000)                    |

---

## Table: `faculty_leads`
| Column       | Type         | Constraints                                      |
|-------------|-------------|--------------------------------------------------|
| `programId` | INT          | NOT NULL, FOREIGN KEY (`programs.id`)           |
| `username`  | VARCHAR(255) | NOT NULL, FOREIGN KEY (`local_users.username`)  |
| **Primary Key** | (`programId`, `username`) | Composite Primary Key |



# Technologies Used
* Database: PostgreSQL
* Database Access: Spring Data JPA
    * Spring Data JPA allows us to write repository interfaces that contain the query methods. Spring Data JPA will automatically generate the implementation of these query methods at runtime.
* Build Tool: Gradle
    * Gradle is a build automation tool that allows us to define the dependencies and build tasks in a Groovy-based DSL.
    * We can run the build tasks using the `gradlew` script.
* Testing: JUnit, Mockito
    * JUnit is a unit testing framework for Java. We can write test cases using JUnit to test the application logic.
    * Mockito is a mocking framework that allows us to mock the dependencies of the class under test.
* HTTP Server: Spring Web
    * Spring Web is a module of the Spring Framework that provides support for building web applications.
* HTML Templating: Thymeleaf
    * Thymeleaf is a modern server-side Java template engine for web and standalone environments.
* Interactivity: HTMX
    * HTMX is a library that allows us to create web applications with interactivity without writing JavaScript.
    * HTMX uses HTML attributes to define the behavior of the elements.
* Styling: TailwindCSS, DaisyUI
    * TailwindCSS is a utility-first CSS framework for rapidly building custom designs.
    * DaisyUI is a component library for TailwindCSS that provides additional components and utilities.
* SSO: Shibboleth
    * Shibboleth provides Single Sign-On (SSO) capabilities, allowing users to authenticate once and access multiple applications seamlessly.
	* It is integrated via Apache using the Shibboleth module, ensuring that SSO endpoints (e.g., /Shibboleth.sso) are securely handled

# How to Run

- Test API and services
    - `./gradlew clean test`

- Reset Database (delete all data and create empty database 'abroad')
    - `./gradlew resetDb`

- Fresh Build (reset database and fill it with sample data) and run
    - `./gradlew resetDb clean bootJar`
    - `java -jar build/libs/abroad-0.0.1-SNAPSHOT.jar --fillDb=true`
    - Can also run `java -jar build/libs/abroad-0.0.1-SNAPSHOT.jar --fillDb=true --resetDb=true` to reset database and fill it with sample data.
        - No gradle involved, but it requires the "abroad" database to be created and running.
    - Sample data can be found in `src/main/resources/data/`. The code responsible
      for reading CSV and saving to the database is in `com.abroad.services.DataInitializerService`.

- Normal Build and run (without modifying existing database)
    - `./gradlew clean bootJar`
    - `java -jar build/libs/abroad-0.0.1-SNAPSHOT.jar`