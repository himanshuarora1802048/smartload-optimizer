package com.logistics.smartload.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @NotBlank(message = "Order ID is required")
    private String id;
    
    @JsonProperty("payout_cents")
    @PositiveOrZero(message = "Payout must be non-negative")
    private long payoutCents;
    
    @JsonProperty("weight_lbs")
    @PositiveOrZero(message = "Weight must be non-negative")
    private int weightLbs;
    
    @JsonProperty("volume_cuft")
    @PositiveOrZero(message = "Volume must be non-negative")
    private int volumeCuft;
    
    @NotBlank(message = "Origin is required")
    private String origin;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    @JsonProperty("pickup_date")
    @NotNull(message = "Pickup date is required")
    private LocalDate pickupDate;
    
    @JsonProperty("delivery_date")
    @NotNull(message = "Delivery date is required")
    private LocalDate deliveryDate;
    
    @JsonProperty("is_hazmat")
    private boolean isHazmat;
    
    public String getRoute() {
        return origin + " -> " + destination;
    }
    
    public boolean isCompatibleWith(Order other) {
        // Same route
        if (!this.getRoute().equals(other.getRoute())) {
            return false;
        }
        
        // Hazmat orders cannot be combined with other orders
        if (this.isHazmat || other.isHazmat) {
            return false;
        }
        
        // Time windows must be compatible (pickup before or on delivery)
        if (this.pickupDate.isAfter(this.deliveryDate) || 
            other.pickupDate.isAfter(other.deliveryDate)) {
            return false;
        }
        
        return true;
    }
}
