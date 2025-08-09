# SQL Sanitize API

A microservice that manages a list of "sensitive" words or phrases and can sanitize a given text by masking those words with asterisks.  

Please refer to: [Approach](docs/APPROACH.md) for more information of how I solve this assignment.

Please refer to: [Approach](docs/ProductionDeployment.txt) for more information about how I would deploy this to production.

<img width="1473" height="667" alt="image" src="https://github.com/user-attachments/assets/725eb6b3-40da-4d6c-a8a0-9a5736756f7b" />

---

## Features

- **CRUD endpoints** for managing sensitive words/phrases.
- **Sanitize endpoint** that replaces any configured sensitive word/phrase in a given string with asterisks.
- **Swagger/OpenAPI** documentation with clear request/response examples.
- **Unit tests** for controller, service, and utility classes.
- **MSSQL** for database etc.

---

## Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **MSSQL** (production)
- **H2** (test profile)
- **Flyway** (DB migrations)
- **Lombok**
- **Swagger / springdoc-openapi**
- **JUnit 5 + Mockito**

---

## Running the Application

### 1. Clone the repository
```bash
git clone https://github.com/<your-repo>/sql-sanitize.git
cd sql-sanitize
