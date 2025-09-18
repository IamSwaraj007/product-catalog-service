package com.example.catalog.controller;

import com.example.catalog.dto.ProductDto;
import com.example.catalog.dto.StockAdjustRequest;
import com.example.catalog.model.Product;
import com.example.catalog.service.ProductService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService svc;
    public ProductController(ProductService svc) { this.svc = svc; }

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody ProductDto dto) {
        Product p = new Product();
        p.setSku(dto.getSku());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock() == null ? 0 : dto.getStock());
        Product created = svc.create(p);
        ProductDto out = svc.toDto(created);
        return ResponseEntity.created(URI.create("/api/v1/products/" + out.getId())).body(out);
    }

    @GetMapping
    public List<ProductDto> list() {
        return svc.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> get(@PathVariable UUID id) {
        return svc.findById(id).map(p -> ResponseEntity.ok(svc.toDto(p))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/adjust-stock")
    public ResponseEntity<Void> adjustStock(@PathVariable UUID id, @RequestBody StockAdjustRequest req) {
        boolean ok = svc.adjustStock(id, req.getDelta());
        if (!ok) return ResponseEntity.status(HttpStatus.CONFLICT).build();
        return ResponseEntity.noContent().build();
    }
}
