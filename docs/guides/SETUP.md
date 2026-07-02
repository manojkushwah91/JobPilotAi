# Local Setup Guide

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 21+ (Temurin recommended) | Backend runtime |
| Maven | 3.9+ | Backend build |
| Node.js | 20 LTS | Frontend runtime |
| npm | 10+ | Frontend package management |
| Docker | 24+ with Docker Compose | Infrastructure services |
| Git | 2.40+ | Version control |

## Step 1 — Clone & Configure

```bash
git clone https://github.com/jobpilot/jobpilot.git
cd jobpilot

# Copy environment template
cp .env.example .env
# Edit .env with your API keys (AI providers, OAuth, etc.)
```

## Step 2 — Start Infrastructure

```bash
# Start all services (PostgreSQL, Redis, Kafka, MinIO, Ollama, monitoring)
make docker-up

# Or on Windows:
.\scripts\dev\start-dev.ps1

# Verify services are running
make ps
```

Services:
| Service | Port | Purpose |
|---------|------|---------|
| PostgreSQL | 5432 | Primary database |
| Redis | 6379 | Cache, rate limiting |
| Kafka | 9092 | Event streaming |
| MinIO | 9000 | S3-compatible storage |
| Ollama | 11434 | Local AI inference |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3000 | Dashboards |
| Loki | 3100 | Log aggregation |
| Tempo | 4317 | Distributed tracing |
| MailHog | 1025/8025 | Email testing |
| pgAdmin | 5050 | Database management |

## Step 3 — Database Migration

```bash
make seed-db
# This runs Flyway migrations + seed data (demo user, feature flags, templates)
```

## Step 4 — Start Backend

```bash
# Terminal 1
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# API ready at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
```

## Step 5 — Start Frontend

```bash
# Terminal 2
cd frontend
npm ci
npm run dev
# Frontend ready at http://localhost:3000
```

## Step 6 — Verify

```bash
# Health check
curl http://localhost:8080/actuator/health

# API ping
curl http://localhost:8080/api/v1/ai/health

# Frontend
open http://localhost:3000
```

## Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@jobpilot.dev | admin123 |
| Demo User | demo@jobpilot.dev | demo1234 |

## IDE Configuration

### IntelliJ IDEA
1. Open `backend/pom.xml` as project
2. Enable annotation processing (Lombok)
3. Set SDK to JDK 21 (Temurin)
4. Import Checkstyle config from `checkstyle.xml`
5. Install plugins: Lombok, MapStruct, SonarLint

### VS Code
1. Open frontend folder
2. Install extensions: ESLint, Prettier, Tailwind CSS IntelliSense
3. Enable TypeScript strict mode

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Port already in use | Change ports in `.env` or stop competing services |
| Docker not starting | Ensure Docker Desktop is running and has 8GB+ RAM allocated |
| Maven build fails | `cd backend && mvn clean -U` to refresh dependencies |
| npm install fails | Delete `node_modules` and `package-lock.json`, rerun `npm ci` |
| Database connection refused | Ensure PostgreSQL is running: `make docker-up` |
| AI provider errors | Verify API keys in `.env` — app works without AI (degraded mode) |
