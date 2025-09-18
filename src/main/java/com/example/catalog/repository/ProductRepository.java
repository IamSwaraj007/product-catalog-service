package com.example.catalog.repository;

import com.example.catalog.model.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySku(String sku);

    @Modifying
    @Transactional
    @Query("UPDATE product SET stock = stock + :delta, updated_at = now() WHERE id = CAST(:id AS uuid) AND (stock + :delta) >= 0", nativeQuery = true)
    int adjustStockNative(@Param("id") String id, @Param("delta") int delta);
}
