package com.example.catalog.service;

import com.example.catalog.dto.ProductDto;
import com.example.catalog.model.Product;
import com.example.catalog.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository repo;
    public ProductService(ProductRepository repo) { this.repo = repo; }

    public ProductDto toDto(Product p) {
        ProductDto d = new ProductDto();
        d.setId(p.getId());
        d.setSku(p.getSku());
        d.setName(p.getName());
        d.setDescription(p.getDescription());
        d.setPrice(p.getPrice());
        d.setStock(p.getStock());
        return d;
    }

    public Product create(Product p) {
        return repo.save(p);
    }

    public Optional<Product> findById(UUID id) { return repo.findById(id); }

    public List<ProductDto> listAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Product update(Product product) {
    return repo.save(product);
    }

    @Transactional
    public boolean adjustStock(UUID id, int delta) {
        int updated = repo.adjustStockNative(id.toString(), delta);
        return updated > 0;
    }

    public boolean delete(UUID id) {
    if (repo.existsById(id)) {
        repo.deleteById(id);
        return true;
    }
    return false;
}
}
