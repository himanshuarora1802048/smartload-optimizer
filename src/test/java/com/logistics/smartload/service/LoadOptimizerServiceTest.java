package com.logistics.smartload.service;

import com.logistics.smartload.model.Order;
import com.logistics.smartload.model.OptimizationRequest;
import com.logistics.smartload.model.OptimizationResponse;
import com.logistics.smartload.model.Truck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoadOptimizerServiceTest {

    private LoadOptimizerService service;

    @BeforeEach
    void setUp() {
        service = new LoadOptimizerService();
    }

    @Test
    void testOptimize_BasicScenario() {
        Truck truck = new Truck("truck-123", 44000, 3000);
        
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false),
            new Order("ord-002", 180000L, 12000, 900, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 4), LocalDate.of(2025, 12, 10), false)
        );
        
        OptimizationRequest request = new OptimizationRequest(truck, orders);
        OptimizationResponse response = service.optimize(request);
        
        assertNotNull(response);
        assertEquals("truck-123", response.getTruckId());
        assertEquals(2, response.getSelectedOrderIds().size());
        assertTrue(response.getSelectedOrderIds().contains("ord-001"));
        assertTrue(response.getSelectedOrderIds().contains("ord-002"));
        assertEquals(430000L, response.getTotalPayoutCents());
        assertEquals(30000, response.getTotalWeightLbs());
        assertEquals(2100, response.getTotalVolumeCuft());
    }

    @Test
    void testOptimize_WithHazmat() {
        Truck truck = new Truck("truck-456", 44000, 3000);
        
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false),
            new Order("ord-003", 320000L, 30000, 1800, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 6), LocalDate.of(2025, 12, 8), true)
        );
        
        OptimizationRequest request = new OptimizationRequest(truck, orders);
        OptimizationResponse response = service.optimize(request);
        
        assertNotNull(response);
        assertEquals(1, response.getSelectedOrderIds().size());
        assertEquals("ord-003", response.getSelectedOrderIds().get(0));
        assertEquals(320000L, response.getTotalPayoutCents());
    }

    @Test
    void testOptimize_WeightConstraint() {
        Truck truck = new Truck("truck-789", 20000, 3000);
        
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false),
            new Order("ord-002", 180000L, 12000, 900, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 4), LocalDate.of(2025, 12, 10), false)
        );
        
        OptimizationRequest request = new OptimizationRequest(truck, orders);
        OptimizationResponse response = service.optimize(request);
        
        assertNotNull(response);
        assertEquals(1, response.getSelectedOrderIds().size());
        assertEquals("ord-001", response.getSelectedOrderIds().get(0));
        assertEquals(250000L, response.getTotalPayoutCents());
    }

    @Test
    void testOptimize_VolumeConstraint() {
        Truck truck = new Truck("truck-999", 44000, 1500);
        
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false),
            new Order("ord-002", 180000L, 12000, 900, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 4), LocalDate.of(2025, 12, 10), false)
        );
        
        OptimizationRequest request = new OptimizationRequest(truck, orders);
        OptimizationResponse response = service.optimize(request);
        
        assertNotNull(response);
        assertEquals(1, response.getSelectedOrderIds().size());
        assertEquals("ord-001", response.getSelectedOrderIds().get(0));
    }

    @Test
    void testOptimize_DifferentRoutes() {
        Truck truck = new Truck("truck-111", 44000, 3000);
        
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false),
            new Order("ord-004", 200000L, 15000, 1000, "New York, NY", "Boston, MA",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false)
        );
        
        OptimizationRequest request = new OptimizationRequest(truck, orders);
        OptimizationResponse response = service.optimize(request);
        
        assertNotNull(response);
        assertEquals(1, response.getSelectedOrderIds().size());
        assertEquals("ord-001", response.getSelectedOrderIds().get(0));
    }

    @Test
    void testOptimize_EmptyOrders() {
        Truck truck = new Truck("truck-222", 44000, 3000);
        List<Order> orders = new ArrayList<>();
        
        OptimizationRequest request = new OptimizationRequest(truck, orders);
        OptimizationResponse response = service.optimize(request);
        
        assertNotNull(response);
        assertEquals("truck-222", response.getTruckId());
        assertEquals(0, response.getSelectedOrderIds().size());
        assertEquals(0L, response.getTotalPayoutCents());
        assertEquals(0.0, response.getUtilizationWeightPercent());
    }

    @Test
    void testOptimize_NoFeasibleCombination() {
        Truck truck = new Truck("truck-333", 10000, 500);
        
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false),
            new Order("ord-002", 180000L, 12000, 900, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 4), LocalDate.of(2025, 12, 10), false)
        );
        
        OptimizationRequest request = new OptimizationRequest(truck, orders);
        OptimizationResponse response = service.optimize(request);
        
        assertNotNull(response);
        assertEquals(0, response.getSelectedOrderIds().size());
        assertEquals(0L, response.getTotalPayoutCents());
    }

    @Test
    void testOptimize_UtilizationCalculation() {
        Truck truck = new Truck("truck-444", 50000, 2000);
        
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 25000, 1000, "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false)
        );
        
        OptimizationRequest request = new OptimizationRequest(truck, orders);
        OptimizationResponse response = service.optimize(request);
        
        assertNotNull(response);
        assertEquals(50.0, response.getUtilizationWeightPercent(), 0.01);
        assertEquals(50.0, response.getUtilizationVolumePercent(), 0.01);
    }
}
