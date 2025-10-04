package com.example.catalog.controller;

import com.example.catalog.dto.ProductDto;
import com.example.catalog.dto.StockAdjustRequest;
import com.example.catalog.model.Product;
import com.example.catalog.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product sampleProduct;
    private ProductDto sampleProductDto;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        
        sampleProduct = new Product();
        sampleProduct.setId(productId);
        sampleProduct.setSku("CONTROLLER-TEST-SKU");
        sampleProduct.setName("Controller Test Product");
        sampleProduct.setDescription("Product for controller testing");
        sampleProduct.setPrice(new BigDecimal("79.99"));
        sampleProduct.setStock(30);

        sampleProductDto = new ProductDto();
        sampleProductDto.setId(productId);
        sampleProductDto.setSku("CONTROLLER-TEST-SKU");
        sampleProductDto.setName("Controller Test Product");
        sampleProductDto.setDescription("Product for controller testing");
        sampleProductDto.setPrice(new BigDecimal("79.99"));
        sampleProductDto.setStock(30);
    }

    @Nested
    @DisplayName("Product Creation Endpoint Tests")
    class ProductCreationEndpointTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() throws Exception {
            // Given
            when(productService.create(any(Product.class))).thenReturn(sampleProduct);
            when(productService.toDto(sampleProduct)).thenReturn(sampleProductDto);

            ProductDto createRequest = new ProductDto();
            createRequest.setSku("NEW-SKU");
            createRequest.setName("New Product");
            createRequest.setPrice(new BigDecimal("29.99"));
            createRequest.setStock(10);

            // When & Then
            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/products/")))
                    .andExpect(jsonPath("$.id").value(productId.toString()))
                    .andExpect(jsonPath("$.sku").value(sampleProductDto.getSku()))
                    .andExpect(jsonPath("$.name").value(sampleProductDto.getName()));

            verify(productService).create(any(Product.class));
            verify(productService).toDto(sampleProduct);
        }

        @Test
        @DisplayName("Should handle service exceptions during creation")
        void shouldHandleServiceExceptionsDuringCreation() throws Exception {
            // Given
            when(productService.create(any(Product.class))).thenThrow(new RuntimeException("Database error"));

            ProductDto createRequest = new ProductDto();
            createRequest.setSku("ERROR-SKU");
            createRequest.setName("Error Product");
            createRequest.setPrice(new BigDecimal("29.99"));

            // When & Then
            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());

            verify(productService).create(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Product Retrieval Endpoint Tests")
    class ProductRetrievalEndpointTests {

        @Test
        @DisplayName("Should retrieve all products")
        void shouldRetrieveAllProducts() throws Exception {
            // Given
            List<ProductDto> products = List.of(sampleProductDto);
            when(productService.listAll()).thenReturn(products);

            // When & Then
            mockMvc.perform(get("/api/v1/products"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(productId.toString()))
                    .andExpect(jsonPath("$[0].sku").value(sampleProductDto.getSku()));

            verify(productService).listAll();
        }

        @Test
        @DisplayName("Should retrieve empty list when no products exist")
        void shouldRetrieveEmptyListWhenNoProductsExist() throws Exception {
            // Given
            when(productService.listAll()).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/v1/products"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(productService).listAll();
        }

        @Test
        @DisplayName("Should retrieve product by ID successfully")
        void shouldRetrieveProductByIdSuccessfully() throws Exception {
            // Given
            when(productService.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(productService.toDto(sampleProduct)).thenReturn(sampleProductDto);

            // When & Then
            mockMvc.perform(get("/api/v1/products/{id}", productId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(productId.toString()))
                    .andExpect(jsonPath("$.sku").value(sampleProductDto.getSku()))
                    .andExpect(jsonPath("$.name").value(sampleProductDto.getName()));

            verify(productService).findById(productId);
            verify(productService).toDto(sampleProduct);
        }

        @Test
        @DisplayName("Should return 404 when product not found by ID")
        void shouldReturn404WhenProductNotFoundById() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(productService.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/v1/products/{id}", nonExistentId))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(productService).findById(nonExistentId);
            verify(productService, never()).toDto(any());
        }
    }

    @Nested
    @DisplayName("Product Update Endpoint Tests")
    class ProductUpdateEndpointTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() throws Exception {
            // Given
            when(productService.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(productService.update(any(Product.class))).thenReturn(sampleProduct);
            when(productService.toDto(sampleProduct)).thenReturn(sampleProductDto);

            ProductDto updateRequest = new ProductDto();
            updateRequest.setSku("UPDATED-SKU");
            updateRequest.setName("Updated Product");
            updateRequest.setPrice(new BigDecimal("99.99"));
            updateRequest.setStock(50);

            // When & Then
            mockMvc.perform(put("/api/v1/products/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(productId.toString()));

            verify(productService).findById(productId);
            verify(productService).update(any(Product.class));
            verify(productService).toDto(sampleProduct);
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent product")
        void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(productService.findById(nonExistentId)).thenReturn(Optional.empty());

            ProductDto updateRequest = new ProductDto();
            updateRequest.setSku("UPDATE-SKU");
            updateRequest.setName("Update Product");
            updateRequest.setPrice(new BigDecimal("99.99"));

            // When & Then
            mockMvc.perform(put("/api/v1/products/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(productService).findById(nonExistentId);
            verify(productService, never()).update(any());
            verify(productService, never()).toDto(any());
        }

        @Test
        @DisplayName("Should handle null stock in update request")
        void shouldHandleNullStockInUpdateRequest() throws Exception {
            // Given
            when(productService.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(productService.update(any(Product.class))).thenReturn(sampleProduct);
            when(productService.toDto(sampleProduct)).thenReturn(sampleProductDto);

            ProductDto updateRequest = new ProductDto();
            updateRequest.setSku("UPDATED-SKU");
            updateRequest.setName("Updated Product");
            updateRequest.setPrice(new BigDecimal("99.99"));
            updateRequest.setStock(null); // Null stock

            // When & Then
            mockMvc.perform(put("/api/v1/products/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(productService).findById(productId);
            verify(productService).update(argThat(product -> 
                product.getStock().equals(sampleProduct.getStock()) // Should keep existing stock
            ));
        }
    }

    @Nested
    @DisplayName("Stock Adjustment Endpoint Tests")
    class StockAdjustmentEndpointTests {

        @Test
        @DisplayName("Should adjust stock successfully")
        void shouldAdjustStockSuccessfully() throws Exception {
            // Given
            when(productService.adjustStock(eq(productId), eq(5))).thenReturn(true);

            StockAdjustRequest adjustRequest = new StockAdjustRequest();
            adjustRequest.setDelta(5);
            adjustRequest.setReason("Controller test adjustment");

            // When & Then
            mockMvc.perform(post("/api/v1/products/{id}/adjust-stock", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustRequest)))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(productService).adjustStock(productId, 5);
        }

        @Test
        @DisplayName("Should return conflict when stock adjustment fails")
        void shouldReturnConflictWhenStockAdjustmentFails() throws Exception {
            // Given
            when(productService.adjustStock(eq(productId), eq(-100))).thenReturn(false);

            StockAdjustRequest adjustRequest = new StockAdjustRequest();
            adjustRequest.setDelta(-100); // Large negative adjustment
            adjustRequest.setReason("Test conflict scenario");

            // When & Then
            mockMvc.perform(post("/api/v1/products/{id}/adjust-stock", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());

            verify(productService).adjustStock(productId, -100);
        }

        @Test
        @DisplayName("Should handle negative stock adjustments")
        void shouldHandleNegativeStockAdjustments() throws Exception {
            // Given
            when(productService.adjustStock(eq(productId), eq(-10))).thenReturn(true);

            StockAdjustRequest adjustRequest = new StockAdjustRequest();
            adjustRequest.setDelta(-10);
            adjustRequest.setReason("Sale");

            // When & Then
            mockMvc.perform(post("/api/v1/products/{id}/adjust-stock", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustRequest)))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(productService).adjustStock(productId, -10);
        }
    }

    @Nested
    @DisplayName("Product Deletion Endpoint Tests")
    class ProductDeletionEndpointTests {

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() throws Exception {
            // Given
            when(productService.delete(productId)).thenReturn(true);

            // When & Then
            mockMvc.perform(delete("/api/v1/products/{id}", productId))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(productService).delete(productId);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent product")
        void shouldReturn404WhenDeletingNonExistentProduct() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(productService.delete(nonExistentId)).thenReturn(false);

            // When & Then
            mockMvc.perform(delete("/api/v1/products/{id}", nonExistentId))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(productService).delete(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Invalid Request Handling Tests")
    class InvalidRequestHandlingTests {

        @Test
        @DisplayName("Should handle malformed JSON in create request")
        void shouldHandleMalformedJsonInCreateRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid json }"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productService, never()).create(any());
        }

        @Test
        @DisplayName("Should handle invalid UUID in path parameter")
        void shouldHandleInvalidUuidInPathParameter() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/products/invalid-uuid"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productService, never()).findById(any());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/products")
                            .content(objectMapper.writeValueAsString(sampleProductDto)))
                    .andDo(print())
                    .andExpect(status().is4xxClientError());

            verify(productService, never()).create(any());
        }
    }
}