package com.example.catalog.service;

import com.example.catalog.model.Product;
import com.example.catalog.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {
    @Test
    void createAndFind() {
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        ProductService svc = new ProductService(repo);
        Product p = new Product();
        p.setSku("SKU-1");
        p.setName("Name");
        p.setPrice(new BigDecimal("9.99"));
        when(repo.save(any())).thenReturn(p);
        Product created = svc.create(p);
        assertNotNull(created);
        verify(repo, times(1)).save(p);
    }

    @Test
    void adjustStockSuccess() {
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        when(repo.adjustStockNative(anyString(), anyInt())).thenReturn(1);
        ProductService svc = new ProductService(repo);
        boolean ok = svc.adjustStock(UUID.randomUUID(), 5);
        assertTrue(ok);
    }
}
