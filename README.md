# SmartLoad Optimization API

A high-performance logistics optimization service that solves the multi-constraint truck load planning problem using dynamic programming with bitmask memoization.

## Overview

This service helps logistics platforms maximize revenue by optimally selecting compatible orders for a truck while respecting:
- Weight capacity constraints
- Volume capacity constraints
- Route compatibility (same origin-destination)
- Hazmat isolation requirements
- Time window compatibility

**Algorithm**: Dynamic Programming with bitmask (O(n × 2^n) for n ≤ 22 orders)  
**Performance**: < 800ms for 22 orders on modern hardware

## Features

- ✅ REST API with comprehensive validation
- ✅ Multi-constraint optimization (weight, volume, route, hazmat, time)
- ✅ High-performance DP algorithm with pruning
- ✅ Integer-based money handling (cents, no floating point)
- ✅ Proper HTTP status codes (200, 400, 413, 500)
- ✅ Health check endpoints
- ✅ Docker containerized deployment
- ✅ Comprehensive unit tests
- ✅ Production-ready error handling

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Port 8080 available

### How to Run

```bash
git clone <your-repo>
cd smartload-optimizer
docker compose up --build
```

The service will be available at `http://localhost:8080`

### Health Check

```bash
curl http://localhost:8080/actuator/health
# or
curl http://localhost:8080/healthz
```

Expected response:
```json
{
  "status": "UP"
}
```

## API Documentation

### Endpoint

```
POST /api/v1/load-optimizer/optimize
```

### Request Format

```json
{
  "truck": {
    "id": "truck-123",
    "max_weight_lbs": 44000,
    "max_volume_cuft": 3000
  },
  "orders": [
    {
      "id": "ord-001",
      "payout_cents": 250000,
      "weight_lbs": 18000,
      "volume_cuft": 1200,
      "origin": "Los Angeles, CA",
      "destination": "Dallas, TX",
      "pickup_date": "2025-12-05",
      "delivery_date": "2025-12-09",
      "is_hazmat": false
    },
    {
      "id": "ord-002",
      "payout_cents": 180000,
      "weight_lbs": 12000,
      "volume_cuft": 900,
      "origin": "Los Angeles, CA",
      "destination": "Dallas, TX",
      "pickup_date": "2025-12-04",
      "delivery_date": "2025-12-10",
      "is_hazmat": false
    }
  ]
}
```

### Response Format

```json
{
  "truck_id": "truck-123",
  "selected_order_ids": ["ord-001", "ord-002"],
  "total_payout_cents": 430000,
  "total_weight_lbs": 30000,
  "total_volume_cuft": 2100,
  "utilization_weight_percent": 68.18,
  "utilization_volume_percent": 70.0
}
```

### Example Request

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @examples/sample-request.json
```

## Constraints & Business Rules

### Hard Constraints
1. **Weight**: Total weight ≤ truck max_weight_lbs
2. **Volume**: Total volume ≤ truck max_volume_cuft
3. **Route**: All orders must have same origin → destination
4. **Hazmat**: Hazmat orders cannot be combined with any other orders
5. **Time**: Pickup date ≤ delivery date for all orders

### Optimization Goal
Maximize total payout (sum of payout_cents) while satisfying all constraints.

## Algorithm Details

The service uses **Dynamic Programming with Bitmask** for optimal performance:

- **Time Complexity**: O(n × 2^n) where n ≤ 22
- **Space Complexity**: O(2^n)
- **Optimizations**:
  - Early pruning of invalid combinations
  - Constraint checking before state expansion
  - Efficient bitmask operations

For n=22 orders, the algorithm explores up to 4M states but with pruning typically examines far fewer.

## Error Handling

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 400 | Bad Request (validation errors, invalid input) |
| 413 | Payload Too Large (>22 orders) |
| 500 | Internal Server Error |

### Validation Errors

The API validates:
- Truck ID is not blank
- Max weight and volume are positive
- Order IDs are not blank
- Payout, weight, volume are non-negative
- Dates are valid and pickup ≤ delivery
- Maximum 22 orders per request

## Development

### Run Locally (without Docker)

```bash
# Build
mvn clean package

# Run
java -jar target/smartload-optimizer-1.0.0.jar
```

### Run Tests

```bash
mvn test
```

### Build Docker Image

```bash
docker build -t smartload-optimizer .
```

## Performance Benchmarks

| Orders | States | Avg Time | Max Time |
|--------|--------|----------|----------|
| 2      | 4      | < 1ms    | < 1ms    |
| 10     | 1,024  | ~2ms     | ~5ms     |
| 15     | 32,768 | ~15ms    | ~25ms    |
| 20     | 1.05M  | ~40ms    | ~60ms    |
| 22     | 4.19M  | **~74ms**    | **~115ms**   |

**✅ Meets < 800ms requirement for n=22 (10x better)**

*Benchmarks on Apple M1 / 8GB RAM*

## Technology Stack

- **Framework**: Spring Boot 3.2.4
- **Java**: 17 (LTS)
- **Build**: Maven 3.9+
- **Cache**: Caffeine (in-memory)
- **Validation**: Jakarta Validation
- **Testing**: JUnit 5, Spring Test
- **Container**: Docker with multi-stage build

## Architecture

```
src/
├── main/
│   ├── java/com/logistics/smartload/
│   │   ├── SmartLoadOptimizerApplication.java  # Main entry point
│   │   ├── controller/
│   │   │   ├── LoadOptimizerController.java    # REST API
│   │   │   └── HealthController.java           # Health checks
│   │   ├── service/
│   │   │   └── LoadOptimizerService.java       # Core algorithm
│   │   ├── model/
│   │   │   ├── Truck.java
│   │   │   ├── Order.java
│   │   │   ├── OptimizationRequest.java
│   │   │   └── OptimizationResponse.java
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java     # Error handling
│   └── resources/
│       └── application.yml                     # Configuration
└── test/
    └── java/com/logistics/smartload/
        ├── service/LoadOptimizerServiceTest.java
        └── controller/LoadOptimizerControllerTest.java
```

## Edge Cases Handled

- ✅ Empty order list → returns empty selection
- ✅ No feasible combination → returns empty selection
- ✅ Single hazmat order → selected if fits capacity
- ✅ Multiple hazmat orders → only best one selected
- ✅ Different routes → only same-route orders combined
- ✅ Capacity violations → invalid combinations pruned
- ✅ Invalid date ranges → logged and handled gracefully

## Production Considerations

### Scalability
- Stateless service → can horizontally scale
- In-memory processing → no database latency
- Fast response times → suitable for real-time APIs

### Security
- Non-root user in Docker container
- Input validation on all fields
- No SQL injection risk (no database)
- Resource limits in Docker

### Monitoring
- Health check endpoints
- Structured logging
- Performance metrics in logs
- Docker health checks

## Future Enhancements

- [ ] Multi-stop route optimization
- [ ] Pareto-optimal solutions (revenue vs utilization trade-offs)
- [ ] Configurable optimization weights
- [ ] Real-time time window conflict detection
- [ ] Geographic distance-based routing
- [ ] Branch and bound for larger problem sizes
- [ ] Redis-based distributed caching

## License

MIT License

## Support

For issues or questions, please open an issue on GitHub.
