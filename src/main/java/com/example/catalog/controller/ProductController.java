package com.example.catalog.controller;

import com.example.catalog.dto.ProductDto;
import com.example.catalog.dto.StockAdjustRequest;
import com.example.catalog.model.Product;
import com.example.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "APIs for managing product catalog")
public class ProductController {
    private final ProductService svc;
    public ProductController(ProductService svc) { this.svc = svc; }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product in the catalog")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "409", description = "Product with SKU already exists", content = @Content)
    })
    public ResponseEntity<ProductDto> create(
            @Parameter(description = "Product data to create", required = true)
            @RequestBody ProductDto dto) {
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
    @Operation(summary = "Get all products", description = "Retrieves a list of all products in the catalog")
    @ApiResponse(responseCode = "200", description = "List of products retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class)))
    public List<ProductDto> list() {
        return svc.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<ProductDto> get(
            @Parameter(description = "Product ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        return svc.findById(id).map(p -> ResponseEntity.ok(svc.toDto(p))).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    public ResponseEntity<ProductDto> update(
            @Parameter(description = "Product ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(description = "Updated product data", required = true)
            @RequestBody ProductDto dto) {
        return svc.findById(id).map(existing -> {
            existing.setSku(dto.getSku());
            existing.setName(dto.getName());
            existing.setDescription(dto.getDescription());
            existing.setPrice(dto.getPrice());
            existing.setStock(dto.getStock() == null ? existing.getStock() : dto.getStock());
        
            Product updated = svc.update(existing);
            return ResponseEntity.ok(svc.toDto(updated));
        })
        .orElse(ResponseEntity.notFound().build());
}


    @PostMapping("/{id}/adjust-stock")
    @Operation(summary = "Adjust product stock", description = "Adjusts the stock quantity of a product (add or subtract)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Stock adjusted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
        @ApiResponse(responseCode = "409", description = "Insufficient stock or concurrent modification", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid stock adjustment request", content = @Content)
    })
    public ResponseEntity<Void> adjustStock(
            @Parameter(description = "Product ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(description = "Stock adjustment request", required = true)
            @RequestBody StockAdjustRequest req) {
        boolean ok = svc.adjustStock(id, req.getDelta());
        if (!ok) return ResponseEntity.status(HttpStatus.CONFLICT).build();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Deletes a product from the catalog")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Product ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
    boolean deleted = svc.delete(id);
    if (!deleted) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
}
}
