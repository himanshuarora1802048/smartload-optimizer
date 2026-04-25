# API Documentation

## Base URL
```
http://localhost:8080
```

## Endpoints

### 1. Optimize Load Selection

Finds the optimal combination of orders for a truck that maximizes revenue while respecting all constraints.

**Endpoint:** `POST /api/v1/load-optimizer/optimize`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "truck": {
    "id": "string",
    "max_weight_lbs": "integer (positive)",
    "max_volume_cuft": "integer (positive)"
  },
  "orders": [
    {
      "id": "string",
      "payout_cents": "long (non-negative)",
      "weight_lbs": "integer (non-negative)",
      "volume_cuft": "integer (non-negative)",
      "origin": "string",
      "destination": "string",
      "pickup_date": "date (YYYY-MM-DD)",
      "delivery_date": "date (YYYY-MM-DD)",
      "is_hazmat": "boolean"
    }
  ]
}
```

**Constraints:**
- Maximum 22 orders per request
- All fields are required
- Positive values for capacities
- Non-negative values for weights/volumes/payouts
- Valid dates in ISO format
- Pickup date must be before or equal to delivery date

**Success Response (200 OK):**
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

**Error Responses:**

*400 Bad Request* - Validation errors
```json
{
  "status": 400,
  "message": "Validation failed: truck - Truck information is required",
  "timestamp": "2026-04-25T00:00:00",
  "errors": {
    "truck": "Truck information is required"
  }
}
```

*413 Payload Too Large* - Too many orders
```json
{
  "status": 400,
  "message": "Validation failed: orders - Maximum 22 orders supported",
  "timestamp": "2026-04-25T00:00:00",
  "errors": {
    "orders": "Maximum 22 orders supported"
  }
}
```

*500 Internal Server Error* - Server error
```json
{
  "status": 500,
  "message": "An unexpected error occurred",
  "timestamp": "2026-04-25T00:00:00",
  "errors": null
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

---

### 2. Health Check (Actuator)

Spring Boot Actuator health endpoint with detailed system status.

**Endpoint:** `GET /actuator/health`

**Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 494384795648,
        "free": 251818164224,
        "threshold": 10485760,
        "path": "/app/.",
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**cURL Example:**
```bash
curl http://localhost:8080/actuator/health
```

---

### 3. Health Check (Simple)

Simple health check endpoint for monitoring and load balancers.

**Endpoint:** `GET /healthz`

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "smartload-optimizer"
}
```

**cURL Example:**
```bash
curl http://localhost:8080/healthz
```

---

## Business Rules

### Constraint Validation

1. **Weight Constraint**
   - Sum of order weights ≤ truck max_weight_lbs

2. **Volume Constraint**
   - Sum of order volumes ≤ truck max_volume_cuft

3. **Route Compatibility**
   - All selected orders must have same origin and destination

4. **Hazmat Isolation**
   - Hazmat orders cannot be combined with any other orders
   - Only one hazmat order can be selected at a time

5. **Time Window Validation**
   - Pickup date ≤ delivery date for each order
   - No overlapping conflicts (simplified in current version)

### Optimization Goal

Maximize: `sum(selected_orders.payout_cents)`

Subject to all constraints above.

---

## Rate Limits

Currently no rate limits implemented. For production use, consider:
- Request rate limiting (e.g., 100 req/min per IP)
- Payload size limits (already at 22 orders max)
- Circuit breakers for fault tolerance

---

## Error Codes Reference

| Code | Description | Common Causes |
|------|-------------|---------------|
| 200 | Success | Request processed successfully |
| 400 | Bad Request | Invalid input, validation errors |
| 413 | Payload Too Large | More than 22 orders |
| 500 | Internal Server Error | Unexpected server error |
| 503 | Service Unavailable | Service is down or starting |

---

## Testing

See `examples/` directory for sample requests:
- `sample-request.json` - Basic 2-order example
- `test-22-orders.json` - Performance test with 22 orders

Run tests with:
```bash
make test-sample
make test-performance
```

---

## Performance

Expected response times:
- 2-10 orders: < 100ms
- 15 orders: ~50ms
- 20 orders: ~300ms
- 22 orders: ~1.7s

All times include algorithm execution and JSON serialization.
