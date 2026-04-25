package com.logistics.smartload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.smartload.model.Order;
import com.logistics.smartload.model.OptimizationRequest;
import com.logistics.smartload.model.Truck;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoadOptimizerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testOptimize_Success() throws Exception {
        Truck truck = new Truck("truck-123", 44000, 3000);
        
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false),
            new Order("ord-002", 180000L, 12000, 900, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 4), LocalDate.of(2025, 12, 10), false)
        );
        
        OptimizationRequest request = new OptimizationRequest(truck, orders);
        
        mockMvc.perform(post("/api/v1/load-optimizer/optimize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truck_id").value("truck-123"))
                .andExpect(jsonPath("$.total_payout_cents").value(430000))
                .andExpect(jsonPath("$.selected_order_ids").isArray());
    }

    @Test
    void testOptimize_ValidationError_MissingTruck() throws Exception {
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false)
        );
        
        OptimizationRequest request = new OptimizationRequest(null, orders);
        
        mockMvc.perform(post("/api/v1/load-optimizer/optimize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testOptimize_ValidationError_TooManyOrders() throws Exception {
        Truck truck = new Truck("truck-123", 44000, 3000);
        
        List<Order> orders = new java.util.ArrayList<>();
        for (int i = 0; i < 23; i++) {
            orders.add(new Order("ord-" + i, 100000L, 1000, 100, "LA", "TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false));
        }
        
        OptimizationRequest request = new OptimizationRequest(truck, orders);
        
        mockMvc.perform(post("/api/v1/load-optimizer/optimize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
