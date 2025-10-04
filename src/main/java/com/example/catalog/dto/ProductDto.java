package com.example.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Product information")
public class ProductDto {
    @Schema(description = "Product unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "Product SKU (Stock Keeping Unit)", example = "LAPTOP-001", required = true)
    private String sku;
    
    @Schema(description = "Product name", example = "Gaming Laptop", required = true)
    private String name;
    
    @Schema(description = "Product description", example = "High-performance gaming laptop with RGB lighting")
    private String description;
    
    @Schema(description = "Product price", example = "1299.99", required = true)
    private BigDecimal price;
    
    @Schema(description = "Available stock quantity", example = "50")
    private Integer stock;

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
