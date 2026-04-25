package com.logistics.smartload.service;

import com.logistics.smartload.model.Order;
import com.logistics.smartload.model.OptimizationRequest;
import com.logistics.smartload.model.OptimizationResponse;
import com.logistics.smartload.model.Truck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class LoadOptimizerService {

    /**
     * Optimizes load selection using highly optimized dynamic programming with bitmask.
     * Optimizations:
     * - Precomputed weight/volume arrays for O(1) lookup
     * - Cached route and hazmat validation
     * - Early pruning of invalid states
     * - Incremental weight/volume tracking
     * Time complexity: O(n * 2^n) where n <= 22
     * Space complexity: O(2^n)
     */
    public OptimizationResponse optimize(OptimizationRequest request) {
        long startTime = System.nanoTime();
        
        Truck truck = request.getTruck();
        List<Order> orders = request.getOrders();
        
        if (orders == null || orders.isEmpty()) {
            return buildEmptyResponse(truck.getId(), truck.getMaxWeightLbs(), truck.getMaxVolumeCuft());
        }
        
        int n = orders.size();
        final int maxWeight = truck.getMaxWeightLbs();
        final int maxVolume = truck.getMaxVolumeCuft();
        
        // Precompute order properties for fast lookup
        int[] weights = new int[n];
        int[] volumes = new int[n];
        long[] payouts = new long[n];
        boolean[] isHazmat = new boolean[n];
        String[] routes = new String[n];
        
        for (int i = 0; i < n; i++) {
            Order order = orders.get(i);
            weights[i] = order.getWeightLbs();
            volumes[i] = order.getVolumeCuft();
            payouts[i] = order.getPayoutCents();
            isHazmat[i] = order.isHazmat();
            routes[i] = order.getRoute();
        }
        
        // Precompute incompatibility matrix for O(1) compatibility checks
        boolean[][] incompatible = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                // Orders are incompatible if different routes or either is hazmat
                incompatible[i][j] = !routes[i].equals(routes[j]) || isHazmat[i] || isHazmat[j];
                incompatible[j][i] = incompatible[i][j];
            }
        }
        
        // DP arrays
        long[] maxPayout = new long[1 << n];
        int[] maskWeight = new int[1 << n];
        int[] maskVolume = new int[1 << n];
        Arrays.fill(maxPayout, -1);
        maxPayout[0] = 0;
        
        // Dynamic programming with optimizations
        for (int mask = 0; mask < (1 << n); mask++) {
            if (maxPayout[mask] == -1) continue;
            
            long currentPayout = maxPayout[mask];
            int currentWeight = maskWeight[mask];
            int currentVolume = maskVolume[mask];
            
            // Try adding each order not in current mask
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) continue; // Already selected
                
                // Quick capacity check
                int newWeight = currentWeight + weights[i];
                int newVolume = currentVolume + volumes[i];
                if (newWeight > maxWeight || newVolume > maxVolume) continue;
                
                // Check compatibility with all selected orders
                boolean compatible = true;
                for (int j = 0; j < n; j++) {
                    if ((mask & (1 << j)) != 0 && incompatible[i][j]) {
                        compatible = false;
                        break;
                    }
                }
                
                if (!compatible) continue;
                
                int newMask = mask | (1 << i);
                long newPayout = currentPayout + payouts[i];
                
                if (newPayout > maxPayout[newMask]) {
                    maxPayout[newMask] = newPayout;
                    maskWeight[newMask] = newWeight;
                    maskVolume[newMask] = newVolume;
                }
            }
        }
        
        // Find the best combination
        long bestPayout = 0;
        int bestMask = 0;
        
        for (int mask = 0; mask < (1 << n); mask++) {
            if (maxPayout[mask] > bestPayout) {
                bestPayout = maxPayout[mask];
                bestMask = mask;
            }
        }
        
        // Build response
        List<String> selectedOrderIds = new ArrayList<>();
        int totalWeight = maskWeight[bestMask];
        int totalVolume = maskVolume[bestMask];
        
        for (int i = 0; i < n; i++) {
            if ((bestMask & (1 << i)) != 0) {
                selectedOrderIds.add(orders.get(i).getId());
            }
        }
        
        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        log.info("Optimization completed in {} ms. Selected {} orders with total payout ${}", 
            elapsed, selectedOrderIds.size(), bestPayout / 100.0);
        
        return OptimizationResponse.builder()
            .truckId(truck.getId())
            .selectedOrderIds(selectedOrderIds)
            .totalPayoutCents(bestPayout)
            .totalWeightLbs(totalWeight)
            .totalVolumeCuft(totalVolume)
            .utilizationWeightPercent(round((double) totalWeight / maxWeight * 100, 2))
            .utilizationVolumePercent(round((double) totalVolume / maxVolume * 100, 2))
            .build();
    }
    
    /**
     * Build empty response when no orders can be selected
     */
    private OptimizationResponse buildEmptyResponse(String truckId, int maxWeight, int maxVolume) {
        return OptimizationResponse.builder()
            .truckId(truckId)
            .selectedOrderIds(Collections.emptyList())
            .totalPayoutCents(0L)
            .totalWeightLbs(0)
            .totalVolumeCuft(0)
            .utilizationWeightPercent(0.0)
            .utilizationVolumePercent(0.0)
            .build();
    }
    
    /**
     * Round to specified decimal places
     */
    private double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}
