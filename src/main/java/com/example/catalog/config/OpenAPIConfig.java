package com.example.catalog.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 Configuration for Product Catalog Service
 * 
 * This configuration provides comprehensive API documentation including:
 * - API metadata and descriptions
 * - Server configurations
 * - Reusable response components
 * - Error schema definitions
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .components(createComponents());
    }

    private Info createApiInfo() {
        String description = "## Product Catalog Service\n\n" +
                "A comprehensive REST API for managing product catalog operations including:\n\n" +
                "### Features\n" +
                "- **CRUD Operations**: Create, read, update, and delete products\n" +
                "- **Inventory Management**: Atomic stock adjustments with optimistic locking\n" +
                "- **Data Validation**: Input validation and business rule enforcement\n" +
                "- **Error Handling**: Comprehensive error responses with meaningful messages\n\n" +
                "### Business Rules\n" +
                "- Product SKUs must be unique\n" +
                "- Stock cannot go below zero\n" +
                "- Price must be positive\n" +
                "- Concurrent stock updates use optimistic locking\n\n" +
                "### Authentication\n" +
                "Currently, no authentication is required (development mode).";

        return new Info()
                .title("Product Catalog Service API")
                .version("v1.0")
                .description(description)
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"))
                .contact(new Contact()
                        .name("Product Catalog Team")
                        .email("catalog-team@example.com")
                        .url("https://github.com/IamSwaraj007/product-catalog-service"));
    }

    private List<Server> createServers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Development server"),
                new Server()
                        .url("https://api.product-catalog.example.com")
                        .description("Production server")
        );
    }

    private Components createComponents() {
        return new Components()
                .addResponses("BadRequest", createBadRequestResponse())
                .addResponses("NotFound", createNotFoundResponse())
                .addResponses("Conflict", createConflictResponse())
                .addResponses("InternalServerError", createInternalServerErrorResponse())
                .addSchemas("ErrorResponse", createErrorSchema())
                .addExamples("ProductExample", createProductExample())
                .addExamples("StockAdjustExample", createStockAdjustExample());
    }

    private ApiResponse createBadRequestResponse() {
        return new ApiResponse()
                .description("Bad Request - Invalid input data")
                .content(new Content()
                        .addMediaType("application/json", 
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    private ApiResponse createNotFoundResponse() {
        return new ApiResponse()
                .description("Resource not found")
                .content(new Content()
                        .addMediaType("application/json", 
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    private ApiResponse createConflictResponse() {
        return new ApiResponse()
                .description("Conflict - Resource already exists or operation conflicts with current state")
                .content(new Content()
                        .addMediaType("application/json", 
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    private ApiResponse createInternalServerErrorResponse() {
        return new ApiResponse()
                .description("Internal Server Error")
                .content(new Content()
                        .addMediaType("application/json", 
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    private Schema<?> createErrorSchema() {
        return new Schema<>()
                .type("object")
                .description("Error response format")
                .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("Error timestamp"))
                .addProperty("status", new Schema<>().type("integer").description("HTTP status code"))
                .addProperty("error", new Schema<>().type("string").description("Error type"))
                .addProperty("message", new Schema<>().type("string").description("Error message"))
                .addProperty("path", new Schema<>().type("string").description("Request path"));
    }

    private Example createProductExample() {
        return new Example()
                .summary("Sample Product")
                .description("Example of a complete product")
                .value("{\n" +
                        "  \"id\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                        "  \"sku\": \"LAPTOP-GAMING-001\",\n" +
                        "  \"name\": \"Gaming Laptop Pro\",\n" +
                        "  \"description\": \"High-performance gaming laptop with RTX graphics and RGB lighting\",\n" +
                        "  \"price\": 1299.99,\n" +
                        "  \"stock\": 25\n" +
                        "}");
    }

    private Example createStockAdjustExample() {
        return new Example()
                .summary("Stock Reduction")
                .description("Example of reducing stock after a sale")
                .value("{\n" +
                        "  \"delta\": -2,\n" +
                        "  \"reason\": \"Online sale\"\n" +
                        "}");
    }
}