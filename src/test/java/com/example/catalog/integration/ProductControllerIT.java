package com.example.catalog.integration;

import com.example.catalog.dto.ProductDto;
import com.example.catalog.dto.StockAdjustRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Product Controller Integration Tests")
class ProductControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("catalog_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDto sampleProductDto;

    @BeforeAll
    static void configureProperties() {
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        sampleProductDto = new ProductDto();
        sampleProductDto.setSku("INTEGRATION-TEST-SKU-" + System.currentTimeMillis());
        sampleProductDto.setName("Integration Test Product");
        sampleProductDto.setDescription("Product for integration testing");
        sampleProductDto.setPrice(new BigDecimal("99.99"));
        sampleProductDto.setStock(50);
    }

    @Nested
    @DisplayName("Product Creation Integration Tests")
    class ProductCreationIntegrationTests {

        @Test
        @DisplayName("Should create product successfully via API")
        void shouldCreateProductSuccessfullyViaApi() throws Exception {
            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleProductDto)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.sku").value(sampleProductDto.getSku()))
                    .andExpect(jsonPath("$.name").value(sampleProductDto.getName()))
                    .andExpect(jsonPath("$.description").value(sampleProductDto.getDescription()))
                    .andExpect(jsonPath("$.price").value(sampleProductDto.getPrice()))
                    .andExpect(jsonPath("$.stock").value(sampleProductDto.getStock()));
        }

        @Test
        @DisplayName("Should reject product with duplicate SKU")
        void shouldRejectProductWithDuplicateSku() throws Exception {
            // First, create a product
            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleProductDto)))
                    .andExpect(status().isCreated());

            // Try to create another product with the same SKU
            ProductDto duplicateSkuProduct = new ProductDto();
            duplicateSkuProduct.setSku(sampleProductDto.getSku()); // Same SKU
            duplicateSkuProduct.setName("Duplicate SKU Product");
            duplicateSkuProduct.setPrice(new BigDecimal("49.99"));
            duplicateSkuProduct.setStock(25);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateSkuProduct)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should reject product with invalid data")
        void shouldRejectProductWithInvalidData() throws Exception {
            ProductDto invalidProduct = new ProductDto();
            // Missing required fields (name, SKU, price)

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidProduct)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Product Retrieval Integration Tests")
    class ProductRetrievalIntegrationTests {

        @Test
        @DisplayName("Should retrieve all products")
        void shouldRetrieveAllProducts() throws Exception {
            // Create a product first
            MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleProductDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Retrieve all products
            mockMvc.perform(get("/api/v1/products"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[*].sku", hasItem(sampleProductDto.getSku())));
        }

        @Test
        @DisplayName("Should retrieve product by ID")
        void shouldRetrieveProductById() throws Exception {
            // Create a product first
            MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleProductDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Extract the created product ID
            String responseContent = createResult.getResponse().getContentAsString();
            ProductDto createdProduct = objectMapper.readValue(responseContent, ProductDto.class);

            // Retrieve the product by ID
            mockMvc.perform(get("/api/v1/products/{id}", createdProduct.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdProduct.getId().toString()))
                    .andExpect(jsonPath("$.sku").value(sampleProductDto.getSku()))
                    .andExpect(jsonPath("$.name").value(sampleProductDto.getName()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent product")
        void shouldReturn404ForNonExistentProduct() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/products/{id}", nonExistentId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Product Update Integration Tests")
    class ProductUpdateIntegrationTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() throws Exception {
            // Create a product first
            MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleProductDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Extract the created product
            String responseContent = createResult.getResponse().getContentAsString();
            ProductDto createdProduct = objectMapper.readValue(responseContent, ProductDto.class);

            // Update the product
            ProductDto updateDto = new ProductDto();
            updateDto.setSku("UPDATED-SKU-" + System.currentTimeMillis());
            updateDto.setName("Updated Product Name");
            updateDto.setDescription("Updated description");
            updateDto.setPrice(new BigDecimal("149.99"));
            updateDto.setStock(75);

            mockMvc.perform(put("/api/v1/products/{id}", createdProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdProduct.getId().toString()))
                    .andExpect(jsonPath("$.sku").value(updateDto.getSku()))
                    .andExpect(jsonPath("$.name").value(updateDto.getName()))
                    .andExpect(jsonPath("$.price").value(updateDto.getPrice()));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent product")
        void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(put("/api/v1/products/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleProductDto)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Stock Adjustment Integration Tests")
    class StockAdjustmentIntegrationTests {

        @Test
        @DisplayName("Should adjust stock successfully")
        void shouldAdjustStockSuccessfully() throws Exception {
            // Create a product first
            MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleProductDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Extract the created product
            String responseContent = createResult.getResponse().getContentAsString();
            ProductDto createdProduct = objectMapper.readValue(responseContent, ProductDto.class);

            // Adjust stock
            StockAdjustRequest adjustRequest = new StockAdjustRequest();
            adjustRequest.setDelta(-10);
            adjustRequest.setReason("Integration test stock reduction");

            mockMvc.perform(post("/api/v1/products/{id}/adjust-stock", createdProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustRequest)))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should reject stock adjustment that would result in negative stock")
        void shouldRejectStockAdjustmentResultingInNegativeStock() throws Exception {
            // Create a product with low stock
            ProductDto lowStockProduct = new ProductDto();
            lowStockProduct.setSku("LOW-STOCK-SKU-" + System.currentTimeMillis());
            lowStockProduct.setName("Low Stock Product");
            lowStockProduct.setPrice(new BigDecimal("19.99"));
            lowStockProduct.setStock(5); // Only 5 in stock

            MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(lowStockProduct)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Extract the created product
            String responseContent = createResult.getResponse().getContentAsString();
            ProductDto createdProduct = objectMapper.readValue(responseContent, ProductDto.class);

            // Try to reduce stock by more than available
            StockAdjustRequest adjustRequest = new StockAdjustRequest();
            adjustRequest.setDelta(-10); // Trying to reduce by 10 when only 5 available
            adjustRequest.setReason("Test negative stock scenario");

            mockMvc.perform(post("/api/v1/products/{id}/adjust-stock", createdProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 404 when adjusting stock for non-existent product")
        void shouldReturn404WhenAdjustingStockForNonExistentProduct() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            StockAdjustRequest adjustRequest = new StockAdjustRequest();
            adjustRequest.setDelta(5);

            mockMvc.perform(post("/api/v1/products/{id}/adjust-stock", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Product Deletion Integration Tests")
    class ProductDeletionIntegrationTests {

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() throws Exception {
            // Create a product first
            MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleProductDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Extract the created product
            String responseContent = createResult.getResponse().getContentAsString();
            ProductDto createdProduct = objectMapper.readValue(responseContent, ProductDto.class);

            // Delete the product
            mockMvc.perform(delete("/api/v1/products/{id}", createdProduct.getId()))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // Verify the product is deleted
            mockMvc.perform(get("/api/v1/products/{id}", createdProduct.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent product")
        void shouldReturn404WhenDeletingNonExistentProduct() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/products/{id}", nonExistentId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
