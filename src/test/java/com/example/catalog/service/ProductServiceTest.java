package com.example.catalog.service;

import com.example.catalog.dto.ProductDto;
import com.example.catalog.model.Product;
import com.example.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        sampleProduct = new Product();
        sampleProduct.setId(productId);
        sampleProduct.setSku("TEST-SKU-001");
        sampleProduct.setName("Test Product");
        sampleProduct.setDescription("Test Description");
        sampleProduct.setPrice(new BigDecimal("29.99"));
        sampleProduct.setStock(100);
    }

    @Nested
    @DisplayName("Product Creation Tests")
    class ProductCreationTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Given
            Product productToCreate = new Product();
            productToCreate.setSku("NEW-SKU-001");
            productToCreate.setName("New Product");
            productToCreate.setPrice(new BigDecimal("19.99"));
            productToCreate.setStock(50);

            when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

            // When
            Product created = productService.create(productToCreate);

            // Then
            assertNotNull(created);
            assertEquals(sampleProduct.getId(), created.getId());
            assertEquals(sampleProduct.getSku(), created.getSku());
            verify(productRepository, times(1)).save(productToCreate);
        }

        @Test
        @DisplayName("Should set default stock when creating product without stock")
        void shouldSetDefaultStockWhenCreatingProduct() {
            // Given
            Product productWithoutStock = new Product();
            productWithoutStock.setSku("NO-STOCK-SKU");
            productWithoutStock.setName("Product Without Stock");
            productWithoutStock.setPrice(new BigDecimal("9.99"));
            // stock is null

            when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

            // When
            Product created = productService.create(productWithoutStock);

            // Then
            assertNotNull(created);
            verify(productRepository).save(argThat(product -> 
                product.getStock() != null && product.getStock() >= 0
            ));
        }
    }

    @Nested
    @DisplayName("Product Retrieval Tests")
    class ProductRetrievalTests {

        @Test
        @DisplayName("Should find product by ID successfully")
        void shouldFindProductByIdSuccessfully() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

            // When
            Optional<Product> found = productService.findById(productId);

            // Then
            assertTrue(found.isPresent());
            assertEquals(sampleProduct.getId(), found.get().getId());
            assertEquals(sampleProduct.getSku(), found.get().getSku());
            verify(productRepository, times(1)).findById(productId);
        }

        @Test
        @DisplayName("Should return empty when product not found")
        void shouldReturnEmptyWhenProductNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When
            Optional<Product> found = productService.findById(nonExistentId);

            // Then
            assertFalse(found.isPresent());
            verify(productRepository, times(1)).findById(nonExistentId);
        }

        @Test
        @DisplayName("Should find product by SKU successfully")
        void shouldFindProductBySkuSuccessfully() {
            // Given
            String sku = "TEST-SKU-001";
            when(productRepository.findBySku(sku)).thenReturn(Optional.of(sampleProduct));

            // When
            Optional<Product> found = productService.findBySku(sku);

            // Then
            assertTrue(found.isPresent());
            assertEquals(sku, found.get().getSku());
            verify(productRepository, times(1)).findBySku(sku);
        }

        @Test
        @DisplayName("Should return all products")
        void shouldReturnAllProducts() {
            // Given
            List<Product> products = List.of(sampleProduct);
            when(productRepository.findAll()).thenReturn(products);

            // When
            List<ProductDto> result = productService.listAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(sampleProduct.getSku(), result.get(0).getSku());
            verify(productRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Product Update Tests")
    class ProductUpdateTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            // Given
            Product updatedProduct = new Product();
            updatedProduct.setId(productId);
            updatedProduct.setSku("UPDATED-SKU");
            updatedProduct.setName("Updated Product");
            updatedProduct.setPrice(new BigDecimal("39.99"));
            updatedProduct.setStock(75);

            when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

            // When
            Product result = productService.update(updatedProduct);

            // Then
            assertNotNull(result);
            assertEquals(updatedProduct.getSku(), result.getSku());
            assertEquals(updatedProduct.getName(), result.getName());
            verify(productRepository, times(1)).save(updatedProduct);
        }
    }

    @Nested
    @DisplayName("Stock Adjustment Tests")
    class StockAdjustmentTests {

        @Test
        @DisplayName("Should adjust stock successfully when update affects rows")
        void shouldAdjustStockSuccessfully() {
            // Given
            UUID productId = UUID.randomUUID();
            int delta = 5;
            when(productRepository.adjustStockNative(productId.toString(), delta)).thenReturn(1);

            // When
            boolean result = productService.adjustStock(productId, delta);

            // Then
            assertTrue(result);
            verify(productRepository, times(1)).adjustStockNative(productId.toString(), delta);
        }

        @Test
        @DisplayName("Should fail stock adjustment when no rows affected")
        void shouldFailStockAdjustmentWhenNoRowsAffected() {
            // Given
            UUID productId = UUID.randomUUID();
            int delta = -5;
            when(productRepository.adjustStockNative(productId.toString(), delta)).thenReturn(0);

            // When
            boolean result = productService.adjustStock(productId, delta);

            // Then
            assertFalse(result);
            verify(productRepository, times(1)).adjustStockNative(productId.toString(), delta);
        }

        @Test
        @DisplayName("Should handle negative stock adjustment")
        void shouldHandleNegativeStockAdjustment() {
            // Given
            UUID productId = UUID.randomUUID();
            int negativeDelta = -10;
            when(productRepository.adjustStockNative(productId.toString(), negativeDelta)).thenReturn(1);

            // When
            boolean result = productService.adjustStock(productId, negativeDelta);

            // Then
            assertTrue(result);
            verify(productRepository, times(1)).adjustStockNative(productId.toString(), negativeDelta);
        }
    }

    @Nested
    @DisplayName("Product Deletion Tests")
    class ProductDeletionTests {

        @Test
        @DisplayName("Should delete existing product successfully")
        void shouldDeleteExistingProductSuccessfully() {
            // Given
            when(productRepository.existsById(productId)).thenReturn(true);
            doNothing().when(productRepository).deleteById(productId);

            // When
            boolean result = productService.delete(productId);

            // Then
            assertTrue(result);
            verify(productRepository, times(1)).existsById(productId);
            verify(productRepository, times(1)).deleteById(productId);
        }

        @Test
        @DisplayName("Should fail to delete non-existent product")
        void shouldFailToDeleteNonExistentProduct() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(productRepository.existsById(nonExistentId)).thenReturn(false);

            // When
            boolean result = productService.delete(nonExistentId);

            // Then
            assertFalse(result);
            verify(productRepository, times(1)).existsById(nonExistentId);
            verify(productRepository, never()).deleteById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("DTO Conversion Tests")
    class DtoConversionTests {

        @Test
        @DisplayName("Should convert Product to ProductDto correctly")
        void shouldConvertProductToProductDtoCorrectly() {
            // When
            ProductDto dto = productService.toDto(sampleProduct);

            // Then
            assertNotNull(dto);
            assertEquals(sampleProduct.getId(), dto.getId());
            assertEquals(sampleProduct.getSku(), dto.getSku());
            assertEquals(sampleProduct.getName(), dto.getName());
            assertEquals(sampleProduct.getDescription(), dto.getDescription());
            assertEquals(sampleProduct.getPrice(), dto.getPrice());
            assertEquals(sampleProduct.getStock(), dto.getStock());
        }

        @Test
        @DisplayName("Should handle null product in DTO conversion")
        void shouldHandleNullProductInDtoConversion() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                productService.toDto(null);
            });
        }
    }
}
