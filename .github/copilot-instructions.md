# Copilot Instructions for Product Catalog Service

## Project Overview
- **Type:** Spring Boot microservice (Java 21)
- **Purpose:** CRUD operations for product catalog with stock management
- **Key Features:**
  - REST API (OpenAPI spec in `openapi.yaml`)
  - PostgreSQL database (schema migrations via Flyway)
  - Optimistic locking for stock updates
  - Metrics via Actuator (`/actuator/prometheus`)
  - Containerized for local/dev use (Dockerfile, docker-compose)
  - Unit and integration tests (Testcontainers)

## Architecture & Structure
- **Entry Point:** `src/main/java/com/example/catalog/CatalogApplication.java`
- **API Layer:** `controller/ProductController.java` exposes endpoints for product CRUD and stock adjustment
- **DTOs:** `dto/ProductDto.java`, `dto/StockAdjustRequest.java` for request/response payloads
- **Domain Model:** `model/Product.java` (includes optimistic locking via `@Version`)
- **Persistence:** `repository/ProductRepository.java` (Spring Data JPA)
- **Business Logic:** `service/ProductService.java` (atomic stock adjustment logic)
- **Database Migration:** `src/main/resources/db/migration/V1__init.sql` (managed by Flyway)
- **Configuration:** `application.yml` (Spring Boot config)
- **API Spec:** `openapi.yaml` (used for Swagger UI)

## Developer Workflows
- **Build:** `mvn -DskipTests package`
- **Run (local/dev):** `docker-compose up --build`
- **Test:**
  - Unit: `ProductServiceTest.java`
  - Integration: `ProductControllerIT.java` (uses Testcontainers for ephemeral DB)
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API Base Path:** `/api/v1/products`

## Patterns & Conventions
- **Optimistic Locking:** `@Version` field in `Product.java` ensures atomic stock updates
- **Stock Adjustment:** Use `StockAdjustRequest` DTO and dedicated endpoint for atomic operations
- **Migration Scripts:** Place new DB migrations in `db/migration/` with Flyway naming (`V{N}__desc.sql`)
- **Testing:** Integration tests use Testcontainers for isolated DB; unit tests mock repository
- **Configuration:** All config in `application.yml`; override via environment variables for Docker
- **OpenAPI:** Keep `openapi.yaml` in sync with controller endpoints

## Integration Points
- **Database:** PostgreSQL (see `docker-compose.yml` for service definition)
- **Metrics:** Prometheus scrape endpoint at `/actuator/prometheus`
- **Swagger:** OpenAPI spec exposed via Swagger UI

## Examples
- **Add Product:** `POST /api/v1/products` with `ProductDto`
- **Adjust Stock:** `POST /api/v1/products/{id}/adjust-stock` with `StockAdjustRequest`
- **Get Metrics:** `GET /actuator/prometheus`

## Key Files
- `CatalogApplication.java` (main)
- `ProductController.java` (API)
- `ProductService.java` (logic)
- `Product.java` (model)
- `ProductRepository.java` (persistence)
- `application.yml` (config)
- `openapi.yaml` (API spec)
- `V1__init.sql` (DB migration)
- `Dockerfile`, `docker-compose.yml` (containerization)

---
_If any section is unclear or missing, please provide feedback for further refinement._
