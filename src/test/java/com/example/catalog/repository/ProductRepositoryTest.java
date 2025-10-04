package com.example.catalog.repository;

import com.example.catalog.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("ProductRepository Tests")
class ProductRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("catalog_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setSku("REPO-TEST-SKU-" + System.currentTimeMillis());
        sampleProduct.setName("Repository Test Product");
        sampleProduct.setDescription("Product for repository testing");
        sampleProduct.setPrice(new BigDecimal("59.99"));
        sampleProduct.setStock(25);
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Should save and retrieve product successfully")
        void shouldSaveAndRetrieveProductSuccessfully() {
            // When
            Product saved = productRepository.save(sampleProduct);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getSku()).isEqualTo(sampleProduct.getSku());
            assertThat(saved.getName()).isEqualTo(sampleProduct.getName());
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();

            // Verify retrieval
            Optional<Product> retrieved = productRepository.findById(saved.getId());
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getSku()).isEqualTo(sampleProduct.getSku());
        }

        @Test
        @DisplayName("Should find all products")
        void shouldFindAllProducts() {
            // Given
            Product product1 = productRepository.save(sampleProduct);
            
            Product product2 = new Product();
            product2.setSku("SECOND-SKU-" + System.currentTimeMillis());
            product2.setName("Second Product");
            product2.setPrice(new BigDecimal("39.99"));
            product2.setStock(15);
            productRepository.save(product2);

            entityManager.flush();

            // When
            List<Product> allProducts = productRepository.findAll();

            // Then
            assertThat(allProducts).hasSize(2);
            assertThat(allProducts).extracting(Product::getSku)
                    .contains(sampleProduct.getSku(), product2.getSku());
        }

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            // Given
            Product saved = productRepository.save(sampleProduct);
            entityManager.flush();
            entityManager.clear();

            // When
            saved.setName("Updated Product Name");
            saved.setPrice(new BigDecimal("79.99"));
            Product updated = productRepository.save(saved);
            entityManager.flush();

            // Then
            assertThat(updated.getName()).isEqualTo("Updated Product Name");
            assertThat(updated.getPrice()).isEqualTo(new BigDecimal("79.99"));
            assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
        }

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() {
            // Given
            Product saved = productRepository.save(sampleProduct);
            UUID productId = saved.getId();
            entityManager.flush();

            // When
            productRepository.deleteById(productId);
            entityManager.flush();

            // Then
            Optional<Product> deleted = productRepository.findById(productId);
            assertThat(deleted).isEmpty();
        }
    }

    @Nested
    @DisplayName("SKU-based Operations")
    class SkuBasedOperations {

        @Test
        @DisplayName("Should find product by SKU")
        void shouldFindProductBySku() {
            // Given
            Product saved = productRepository.save(sampleProduct);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Product> found = productRepository.findBySku(sampleProduct.getSku());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
            assertThat(found.get().getName()).isEqualTo(sampleProduct.getName());
        }

        @Test
        @DisplayName("Should return empty when SKU not found")
        void shouldReturnEmptyWhenSkuNotFound() {
            // When
            Optional<Product> found = productRepository.findBySku("NON-EXISTENT-SKU");

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should enforce unique SKU constraint")
        void shouldEnforceUniqueSkuConstraint() {
            // Given
            productRepository.save(sampleProduct);
            entityManager.flush();

            // When & Then
            Product duplicateSkuProduct = new Product();
            duplicateSkuProduct.setSku(sampleProduct.getSku()); // Same SKU
            duplicateSkuProduct.setName("Different Name");
            duplicateSkuProduct.setPrice(new BigDecimal("99.99"));
            duplicateSkuProduct.setStock(10);

            assertThatThrownBy(() -> {
                productRepository.save(duplicateSkuProduct);
                entityManager.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Stock Adjustment Operations")
    class StockAdjustmentOperations {

        @Test
        @DisplayName("Should adjust stock using native query when product exists")
        void shouldAdjustStockUsingNativeQueryWhenProductExists() {
            // Given
            Product saved = productRepository.save(sampleProduct);
            entityManager.flush();
            entityManager.clear();

            int initialStock = sampleProduct.getStock();
            int delta = 10;

            // When
            int affectedRows = productRepository.adjustStockNative(saved.getId().toString(), delta);

            // Then
            assertThat(affectedRows).isEqualTo(1);

            // Verify the stock was actually updated
            entityManager.clear();
            Optional<Product> updated = productRepository.findById(saved.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getStock()).isEqualTo(initialStock + delta);
        }

        @Test
        @DisplayName("Should not adjust stock when resulting in negative stock")
        void shouldNotAdjustStockWhenResultingInNegativeStock() {
            // Given
            sampleProduct.setStock(5); // Low stock
            Product saved = productRepository.save(sampleProduct);
            entityManager.flush();
            entityManager.clear();

            int negativeDelta = -10; // Would result in negative stock

            // When
            int affectedRows = productRepository.adjustStockNative(saved.getId().toString(), negativeDelta);

            // Then
            assertThat(affectedRows).isEqualTo(0); // No rows affected due to constraint

            // Verify the stock was not changed
            entityManager.clear();
            Optional<Product> unchanged = productRepository.findById(saved.getId());
            assertThat(unchanged).isPresent();
            assertThat(unchanged.get().getStock()).isEqualTo(5); // Original stock
        }

        @Test
        @DisplayName("Should return 0 affected rows when product does not exist")
        void shouldReturn0AffectedRowsWhenProductDoesNotExist() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            int delta = 5;

            // When
            int affectedRows = productRepository.adjustStockNative(nonExistentId.toString(), delta);

            // Then
            assertThat(affectedRows).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle large positive stock adjustments")
        void shouldHandleLargePositiveStockAdjustments() {
            // Given
            Product saved = productRepository.save(sampleProduct);
            entityManager.flush();

            int largeDelta = 1000;

            // When
            int affectedRows = productRepository.adjustStockNative(saved.getId().toString(), largeDelta);

            // Then
            assertThat(affectedRows).isEqualTo(1);

            // Verify the stock was updated
            entityManager.clear();
            Optional<Product> updated = productRepository.findById(saved.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getStock()).isEqualTo(sampleProduct.getStock() + largeDelta);
        }
    }

    @Nested
    @DisplayName("Data Validation Tests")
    class DataValidationTests {

        @Test
        @DisplayName("Should handle products with null description")
        void shouldHandleProductsWithNullDescription() {
            // Given
            sampleProduct.setDescription(null);

            // When
            Product saved = productRepository.save(sampleProduct);
            entityManager.flush();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getDescription()).isNull();
        }

        @Test
        @DisplayName("Should handle products with zero stock")
        void shouldHandleProductsWithZeroStock() {
            // Given
            sampleProduct.setStock(0);

            // When
            Product saved = productRepository.save(sampleProduct);
            entityManager.flush();

            // Then
            assertThat(saved.getStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should enforce not null constraints")
        void shouldEnforceNotNullConstraints() {
            // Given
            Product invalidProduct = new Product();
            invalidProduct.setSku(null); // Required field
            invalidProduct.setName("Valid Name");
            invalidProduct.setPrice(new BigDecimal("10.00"));

            // When & Then
            assertThatThrownBy(() -> {
                productRepository.save(invalidProduct);
                entityManager.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Optimistic Locking Tests")
    class OptimisticLockingTests {

        @Test
        @DisplayName("Should increment version on update")
        void shouldIncrementVersionOnUpdate() {
            // Given
            Product saved = productRepository.save(sampleProduct);
            entityManager.flush();
            Long initialVersion = saved.getVersion();

            // When
            saved.setName("Updated Name");
            Product updated = productRepository.save(saved);
            entityManager.flush();

            // Then
            assertThat(updated.getVersion()).isEqualTo(initialVersion + 1);
        }

        @Test
        @DisplayName("Should handle concurrent updates with optimistic locking")
        void shouldHandleConcurrentUpdatesWithOptimisticLocking() {
            // Given
            Product saved = productRepository.save(sampleProduct);
            entityManager.flush();
            entityManager.clear();

            // Simulate two concurrent updates
            Product product1 = productRepository.findById(saved.getId()).orElseThrow();
            Product product2 = productRepository.findById(saved.getId()).orElseThrow();

            // When & Then
            product1.setName("Updated by User 1");
            productRepository.save(product1);
            entityManager.flush();

            product2.setName("Updated by User 2");
            
            // The second update should fail due to optimistic locking
            assertThatThrownBy(() -> {
                productRepository.save(product2);
                entityManager.flush();
            }).isInstanceOf(Exception.class); // OptimisticLockException or similar
        }
    }
}