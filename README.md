# Product Catalog Service

A comprehensive Spring Boot (Java 21) microservice for managing product catalog operations with full CRUD functionality, inventory management, and cloud-native features.

## 🚀 Features

- **REST API** with OpenAPI 3.0 specification and Swagger UI
- **PostgreSQL Database** with Flyway migrations for schema versioning
- **Optimistic Locking** for concurrent stock updates
- **Atomic Stock Operations** with transaction safety
- **Health Monitoring** via Spring Boot Actuator
- **Metrics Collection** with Prometheus integration
- **Containerization** with Docker and Kubernetes support
- **Comprehensive Testing** with unit and integration tests using Testcontainers

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │  Product Catalog │    │   PostgreSQL    │
│   (Optional)    │───▶│    Service       │───▶│    Database     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Prometheus    │
                       │   Monitoring    │
                       └─────────────────┘
```

### Tech Stack
- **Java 21** with Spring Boot 3.3.3
- **Spring Data JPA** for data persistence
- **PostgreSQL 15+** as primary database
- **Flyway 10.15.2** for database migrations
- **Docker & Kubernetes** for containerization
- **Maven** for build automation
- **JUnit 5 & Testcontainers** for testing

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker & Docker Compose**
- **PostgreSQL 15+** (if running without Docker)
- **Kubernetes cluster** (for K8s deployment)

## 🛠️ Local Development Setup

### Method 1: Docker Compose (Recommended)

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd product-catalog-service
   ```

2. **Build and run with Docker Compose:**
   ```bash
   mvn clean package -DskipTests
   docker-compose up --build
   ```

3. **Access the application:**
   - **API Base:** http://localhost:8080/api/v1/products
   - **Swagger UI:** http://localhost:8080/swagger-ui.html
   - **Health Check:** http://localhost:8080/actuator/health
   - **Metrics:** http://localhost:8080/actuator/prometheus

### Method 2: Manual Setup

1. **Start PostgreSQL:**
   ```bash
   docker run --name postgres-catalog -e POSTGRES_DB=catalog -e POSTGRES_USER=catalog -e POSTGRES_PASSWORD=catalog -p 5432:5432 -d postgres:15
   ```

2. **Build and run the application:**
   ```bash
   mvn clean package -DskipTests
   java -jar target/product-catalog-service-0.0.1-SNAPSHOT.jar
   ```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | Database username | `catalog` |
| `DB_PASSWORD` | Database password | `catalog` |
| `SPRING_DATASOURCE_URL` | Database URL | `jdbc:postgresql://localhost:5432/catalog` |
| `SERVER_PORT` | Application port | `8080` |

### Configuration Files

- `src/main/resources/application.yml` - Main application configuration
- `src/main/resources/db/migration/` - Flyway database migrations
- `k8s/` - Kubernetes deployment manifests

## 📚 API Documentation

### Core Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/products` | List all products |
| `GET` | `/api/v1/products/{id}` | Get product by ID |
| `POST` | `/api/v1/products` | Create new product |
| `PUT` | `/api/v1/products/{id}` | Update product |
| `DELETE` | `/api/v1/products/{id}` | Delete product |
| `POST` | `/api/v1/products/{id}/adjust-stock` | Adjust product stock |

### Example API Calls

**Create Product:**
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sample Product",
    "description": "A sample product",
    "price": 29.99,
    "sku": "SAMPLE-001",
    "stock": 100
  }'
```

**Adjust Stock:**
```bash
curl -X POST http://localhost:8080/api/v1/products/1/adjust-stock \
  -H "Content-Type: application/json" \
  -d '{
    "delta": -5,
    "reason": "Sale"
  }'
```

## 🗄️ Database Schema

The application uses the following main entities:

### Product Table
```sql
CREATE TABLE product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

## 🚀 Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (minikube, k3s, or cloud provider)
- kubectl configured

### Deploy to Kubernetes

1. **Apply Kubernetes manifests:**
   ```bash
   kubectl apply -f k8s/
   ```

2. **Check deployment status:**
   ```bash
   kubectl get pods
   kubectl get services
   ```

3. **Access the application:**
   ```bash
   kubectl port-forward service/product-catalog-service 8080:8080
   ```

### Kubernetes Resources

- `k8s/deployment.yaml` - Application deployment
- `k8s/service.yaml` - Service configuration
- `k8s/hpa.yaml` - Horizontal Pod Autoscaler
- ConfigMaps and Secrets for configuration

## 📊 Monitoring & Observability

### Health Checks
- **Liveness:** `/actuator/health/liveness`
- **Readiness:** `/actuator/health/readiness`

### Metrics
- **Prometheus endpoint:** `/actuator/prometheus`
- **Application metrics:** Custom business metrics included

## 🏗️ Project Structure

```
product-catalog-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/catalog/
│   │   │   ├── CatalogApplication.java
│   │   │   ├── controller/         # REST controllers
│   │   │   ├── service/            # Business logic
│   │   │   ├── repository/         # Data access layer
│   │   │   ├── model/              # Entity classes
│   │   │   └── dto/                # Data transfer objects
│   │   └── resources/
│   │       ├── application.yml     # App configuration
│   │       └── db/migration/       # Flyway migrations
│   └── test/                       # Test classes
├── k8s/                           # Kubernetes manifests
├── docker-compose.yml             # Local development setup
├── Dockerfile                     # Container image definition
├── pom.xml                        # Maven configuration
└── README.md                      # This file
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Related Documentation

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
