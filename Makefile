.PHONY: help build test run clean docker-build docker-run docker-stop docker-clean all

# Variables
APP_NAME := smartload-optimizer
VERSION := 1.0.0
DOCKER_IMAGE := $(APP_NAME):$(VERSION)
DOCKER_CONTAINER := $(APP_NAME)-container
PORT := 8080

# Default target
help: ## Show this help message
	@echo "SmartLoad Optimizer - Available targets:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
	@echo ""

build: ## Build the application with Maven
	@echo "Building application..."
	mvn clean package -DskipTests

build-with-tests: ## Build the application and run tests
	@echo "Building application with tests..."
	mvn clean package

test: ## Run unit and integration tests
	@echo "Running tests..."
	mvn test

run: ## Run the application locally
	@echo "Starting application on port $(PORT)..."
	java -jar target/$(APP_NAME)-$(VERSION).jar

run-bg: ## Run the application in background
	@echo "Starting application in background..."
	java -jar target/$(APP_NAME)-$(VERSION).jar > app.log 2>&1 &
	@echo "Application started. Logs: tail -f app.log"

stop: ## Stop the background application
	@echo "Stopping application..."
	@pkill -f "$(APP_NAME)-$(VERSION).jar" || true

clean: ## Clean build artifacts
	@echo "Cleaning build artifacts..."
	mvn clean
	rm -f app.log

docker-build: ## Build Docker image
	@echo "Building Docker image: $(DOCKER_IMAGE)..."
	docker build -t $(DOCKER_IMAGE) .
	docker tag $(DOCKER_IMAGE) $(APP_NAME):latest

docker-run: ## Run application in Docker
	@echo "Starting Docker container..."
	docker run -d \
		--name $(DOCKER_CONTAINER) \
		-p $(PORT):8080 \
		$(DOCKER_IMAGE)
	@echo "Container started. Checking health..."
	@sleep 5
	@curl -s http://localhost:$(PORT)/healthz || echo "Waiting for service..."

docker-stop: ## Stop Docker container
	@echo "Stopping Docker container..."
	docker stop $(DOCKER_CONTAINER) || true
	docker rm $(DOCKER_CONTAINER) || true

docker-logs: ## Show Docker container logs
	docker logs -f $(DOCKER_CONTAINER)

docker-clean: ## Remove Docker images
	@echo "Removing Docker images..."
	docker rmi $(DOCKER_IMAGE) $(APP_NAME):latest || true

compose-up: ## Start with docker-compose
	@echo "Starting services with docker-compose..."
	docker compose up --build -d
	@sleep 5
	@curl -s http://localhost:$(PORT)/healthz

compose-down: ## Stop docker-compose services
	@echo "Stopping services..."
	docker compose down

compose-logs: ## Show docker-compose logs
	docker compose logs -f

test-api: ## Run API integration tests
	@echo "Running API tests..."
	@chmod +x scripts/test-api.sh
	@bash scripts/test-api.sh

test-sample: ## Test with sample request
	@echo "Testing with sample request..."
	@curl -s -X POST http://localhost:$(PORT)/api/v1/load-optimizer/optimize \
		-H "Content-Type: application/json" \
		-d @examples/sample-request.json | python3 -m json.tool

test-performance: ## Test with 22 orders (performance test)
	@echo "Running performance test with 22 orders..."
	@echo "Starting timer..."
	@time curl -s -X POST http://localhost:$(PORT)/api/v1/load-optimizer/optimize \
		-H "Content-Type: application/json" \
		-d @examples/test-22-orders.json | python3 -m json.tool

health-check: ## Check service health
	@echo "Checking service health..."
	@curl -s http://localhost:$(PORT)/healthz | python3 -m json.tool
	@echo ""
	@curl -s http://localhost:$(PORT)/actuator/health | python3 -m json.tool

install-deps: ## Install required dependencies (macOS)
	@echo "Installing dependencies..."
	@which java || echo "Please install Java 17: brew install openjdk@17"
	@which mvn || echo "Please install Maven: brew install maven"
	@which docker || echo "Please install Docker: brew install --cask docker"

verify: ## Verify installation and setup
	@echo "Verifying installation..."
	@echo -n "Java version: "
	@java -version 2>&1 | head -n 1
	@echo -n "Maven version: "
	@mvn -version | head -n 1
	@echo -n "Docker version: "
	@docker --version

all: clean build docker-build ## Clean, build app, and build Docker image

quick-start: build run-bg ## Quick start: build and run in background
	@echo "Waiting for service to start..."
	@sleep 5
	@make health-check
	@echo ""
	@echo "Service is ready! Test with: make test-sample"

dev: ## Development mode: run with auto-reload
	@echo "Starting in development mode..."
	mvn spring-boot:run

format: ## Format code
	@echo "Formatting code..."
	mvn spotless:apply || echo "Install spotless plugin for formatting"

lint: ## Run code quality checks
	@echo "Running code quality checks..."
	mvn checkstyle:check || echo "Install checkstyle plugin for linting"

coverage: ## Generate test coverage report
	@echo "Generating coverage report..."
	mvn jacoco:report || echo "Install jacoco plugin for coverage"

package: build ## Create distributable package
	@echo "Creating package..."
	@mkdir -p dist
	@cp target/$(APP_NAME)-$(VERSION).jar dist/
	@cp README.md dist/
	@cp examples/* dist/ 2>/dev/null || true
	@echo "Package created in dist/"

.DEFAULT_GOAL := help
