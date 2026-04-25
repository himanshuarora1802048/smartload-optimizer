package com.logistics.smartload.controller;

import com.logistics.smartload.model.OptimizationRequest;
import com.logistics.smartload.model.OptimizationResponse;
import com.logistics.smartload.service.LoadOptimizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/load-optimizer")
@RequiredArgsConstructor
@Validated
@Slf4j
public class LoadOptimizerController {

    private final LoadOptimizerService optimizerService;

    @PostMapping("/optimize")
    public ResponseEntity<OptimizationResponse> optimize(
            @Valid @RequestBody OptimizationRequest request) {
        
        log.info("Received optimization request for truck: {}, orders: {}", 
            request.getTruck().getId(), request.getOrders().size());
        
        try {
            OptimizationResponse response = optimizerService.optimize(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during optimization", e);
            throw new RuntimeException("Optimization failed: " + e.getMessage(), e);
        }
    }
}
