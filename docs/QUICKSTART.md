# Quick Start Guide

## Docker Deployment (Recommended)

### Prerequisites
- Docker and Docker Compose installed
- Port 8080 available

### Steps

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd smartload-optimizer
   ```

2. **Start the service**
   ```bash
   docker compose up --build
   ```
   
   The service will be available at `http://localhost:8080`

3. **Verify it's running**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

4. **Test with sample data**
   ```bash
   curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
     -H "Content-Type: application/json" \
     -d @examples/sample-request.json
   ```

5. **Stop the service**
   ```bash
   docker compose down
   ```

## Local Development (Without Docker)

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Steps

1. **Build the project**
   ```bash
   mvn clean package
   ```

2. **Run the application**
   ```bash
   java -jar target/smartload-optimizer-1.0.0.jar
   ```

3. **Test the API**
   ```bash
   ./test-api.sh
   ```

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
./test-api.sh
```

### Performance Benchmarks
The service includes a 22-order test case (`test-22-orders.json`) to verify performance:

```bash
time curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @examples/test-22-orders.json
```

Expected response time: < 2 seconds

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/load-optimizer/optimize` | Optimize load selection |
| GET | `/actuator/health` | Health check (detailed) |
| GET | `/healthz` | Health check (simple) |

## Sample Request

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
    }
  ]
}
```

## Sample Response

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

## Troubleshooting

### Port 8080 already in use
```bash
# Kill the process using port 8080
lsof -ti:8080 | xargs kill -9

# Or use a different port
java -jar target/smartload-optimizer-1.0.0.jar --server.port=8081
```

### Docker daemon not running
```bash
# Start Docker Desktop or Docker daemon
# macOS: Open Docker Desktop
# Linux: sudo systemctl start docker
```

### Build fails
```bash
# Clean Maven cache
mvn clean

# Rebuild
mvn package
```

## Next Steps

- See [README.md](README.md) for detailed documentation
- Review algorithm implementation in `LoadOptimizerService.java`
- Explore test cases in `src/test/`
- Modify `application.yml` for custom configuration
