package com.example.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Stock adjustment request")
public class StockAdjustRequest {
    @Schema(description = "Stock change amount (positive to add, negative to subtract)", 
            example = "-5", required = true)
    private int delta;
    
    @Schema(description = "Reason for stock adjustment", example = "Sale")
    private String reason;
    
    public int getDelta() { return delta; }
    public void setDelta(int delta) { this.delta = delta; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
