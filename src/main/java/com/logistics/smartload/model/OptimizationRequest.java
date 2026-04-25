package com.logistics.smartload.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRequest {
    
    @NotNull(message = "Truck information is required")
    @Valid
    private Truck truck;
    
    @NotNull(message = "Orders list is required")
    @Size(max = 22, message = "Maximum 22 orders supported")
    @Valid
    private List<Order> orders;
}
