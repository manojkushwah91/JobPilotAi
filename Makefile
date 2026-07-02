.PHONY: help start-dev stop-dev reset-dev seed-db build test clean docker-up docker-down

SHELL := /bin/bash

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

start-dev: ## Start local development environment
	@echo "Starting development environment..."
	docker compose -f infrastructure/docker/dev/docker-compose.yml up -d
	@echo "Waiting for PostgreSQL..."
	until docker compose -f infrastructure/docker/dev/docker-compose.yml exec -T postgres pg_isready -U jobpilot; do sleep 2; done
	@echo "Starting backend..."
	cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev &
	@echo "Starting frontend..."
	cd frontend && npm run dev &

stop-dev: ## Stop development environment
	@echo "Stopping all services..."
	-docker compose -f infrastructure/docker/dev/docker-compose.yml down
	-pkill -f "spring-boot:run" 2>/dev/null || true
	-pkill -f "next dev" 2>/dev/null || true
	@echo "Done"

reset-dev: stop-dev ## Full reset: stop, clean volumes, restart
	@echo "Removing volumes..."
	docker compose -f infrastructure/docker/dev/docker-compose.yml down -v
	@echo "Clean Maven build..."
	cd backend && mvn clean
	@echo "Remove node_modules..."
	rm -rf frontend/node_modules
	$(MAKE) start-dev

seed-db: ## Seed development database with sample data
	@echo "Running Flyway migration + seed..."
	cd backend && mvn flyway:migrate -Dflyway.configFiles=src/main/resources/flyway-dev.conf
	@echo "Database seeded"

build: ## Build everything
	@echo "Building backend..."
	cd backend && mvn clean package -DskipTests
	@echo "Building frontend..."
	cd frontend && npm ci && npm run build
	@echo "Build complete"

test: ## Run all tests
	@echo "Running backend tests..."
	cd backend && mvn test
	@echo "Running frontend tests..."
	cd frontend && npm run test

clean: ## Clean all build artifacts
	cd backend && mvn clean
	rm -rf frontend/node_modules frontend/.next
	docker compose -f infrastructure/docker/dev/docker-compose.yml down -v

docker-up: ## Start all Docker services
	docker compose -f infrastructure/docker/dev/docker-compose.yml up -d

docker-down: ## Stop all Docker services
	docker compose -f infrastructure/docker/dev/docker-compose.yml down

ci-build: ## CI build (no Docker dependency)
	cd backend && mvn validate compile test -B
	cd frontend && npm ci && npm run type-check && npm run lint

lint: ## Run linters
	cd backend && mvn checkstyle:check pmd:check spotbugs:check -B
	cd frontend && npm run lint

format: ## Format code
	cd backend && mvn com.coveo:fmt-maven-plugin:format
	cd frontend && npm run format

dep-update: ## Check dependency updates
	cd backend && mvn versions:display-dependency-updates
	cd frontend && npm outdated

logs: ## Show service logs
	docker compose -f infrastructure/docker/dev/docker-compose.yml logs -f

ps: ## Show running containers
	docker compose -f infrastructure/docker/dev/docker-compose.yml ps
