# Product Catalog Service

Minimal Spring Boot (Java 21) microservice implementing CRUD for a product catalog.

Features included:
- REST API with OpenAPI (Swagger UI)
- PostgreSQL with Flyway migration
- Optimistic locking and atomic stock adjustment
- Actuator metrics (/actuator/prometheus)
- Dockerfile and docker-compose for local development
- Unit and integration test examples (Testcontainers)

## Run locally (dev)
1. Build:
   mvn -DskipTests package
2. Start with Docker Compose:
   docker-compose up --build

Swagger UI: http://localhost:8080/swagger-ui.html
API base: http://localhost:8080/api/v1/products
