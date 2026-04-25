#!/bin/bash
# SmartLoad Optimizer API Test Script

BASE_URL="http://localhost:8080"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "==================================="
echo "SmartLoad Optimizer API Test Suite"
echo "==================================="
echo ""

# Test 1: Health Check (Actuator)
echo "Test 1: Health Check (Actuator)"
echo "GET /actuator/health"
curl -s "${BASE_URL}/actuator/health" | python3 -m json.tool
echo ""
echo "-----------------------------------"
echo ""

# Test 2: Health Check (Custom)
echo "Test 2: Custom Health Check"
echo "GET /healthz"
curl -s "${BASE_URL}/healthz" | python3 -m json.tool
echo ""
echo "-----------------------------------"
echo ""

# Test 3: Basic Optimization
echo "Test 3: Basic Optimization (2 orders)"
echo "POST /api/v1/load-optimizer/optimize"
START=$(date +%s%N | cut -b1-13)
curl -s -X POST "${BASE_URL}/api/v1/load-optimizer/optimize" \
  -H "Content-Type: application/json" \
  -d @"${PROJECT_ROOT}/examples/sample-request.json" | python3 -m json.tool
END=$(date +%s%N | cut -b1-13)
DURATION=$((END - START))
echo ""
echo "Response time: ${DURATION}ms"
echo "-----------------------------------"
echo ""

# Test 4: Performance Test (22 orders)
echo "Test 4: Performance Test (22 orders)"
echo "POST /api/v1/load-optimizer/optimize"
START=$(date +%s%N | cut -b1-13)
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/load-optimizer/optimize" \
  -H "Content-Type: application/json" \
  -d @"${PROJECT_ROOT}/examples/test-22-orders.json")
END=$(date +%s%N | cut -b1-13)
DURATION=$((END - START))
echo "$RESPONSE" | python3 -m json.tool
echo ""
echo "Response time: ${DURATION}ms"
if [ $DURATION -lt 2000 ]; then
  echo "✓ Performance requirement met (< 2000ms)"
else
  echo "✗ Performance requirement NOT met (>= 2000ms)"
fi
echo "-----------------------------------"
echo ""

# Test 5: Validation Error (missing truck)
echo "Test 5: Validation Error (missing truck)"
echo "POST /api/v1/load-optimizer/optimize"
curl -s -X POST "${BASE_URL}/api/v1/load-optimizer/optimize" \
  -H "Content-Type: application/json" \
  -d '{"orders": []}' | python3 -m json.tool
echo ""
echo "-----------------------------------"
echo ""

# Test 6: Validation Error (too many orders)
echo "Test 6: Validation Error (too many orders - 23 orders)"
echo "POST /api/v1/load-optimizer/optimize"
cat > /tmp/test-too-many.json << 'EOF'
{
  "truck": {
    "id": "truck-123",
    "max_weight_lbs": 44000,
    "max_volume_cuft": 3000
  },
  "orders": [
EOF

for i in {1..23}; do
  if [ $i -eq 23 ]; then
    echo "    {\"id\": \"ord-$i\", \"payout_cents\": 10000, \"weight_lbs\": 100, \"volume_cuft\": 10, \"origin\": \"LA\", \"destination\": \"TX\", \"pickup_date\": \"2025-12-05\", \"delivery_date\": \"2025-12-09\", \"is_hazmat\": false}" >> /tmp/test-too-many.json
  else
    echo "    {\"id\": \"ord-$i\", \"payout_cents\": 10000, \"weight_lbs\": 100, \"volume_cuft\": 10, \"origin\": \"LA\", \"destination\": \"TX\", \"pickup_date\": \"2025-12-05\", \"delivery_date\": \"2025-12-09\", \"is_hazmat\": false}," >> /tmp/test-too-many.json
  fi
done

echo "  ]" >> /tmp/test-too-many.json
echo "}" >> /tmp/test-too-many.json

curl -s -X POST "${BASE_URL}/api/v1/load-optimizer/optimize" \
  -H "Content-Type: application/json" \
  -d @/tmp/test-too-many.json | python3 -m json.tool

rm /tmp/test-too-many.json
echo ""
echo "-----------------------------------"
echo ""

echo "==================================="
echo "All tests completed!"
echo "==================================="
