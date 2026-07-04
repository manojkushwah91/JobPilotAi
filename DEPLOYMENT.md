# JobPilot AI v2.0 — Deployment Documentation

**Version:** 2.0  
**Status:** Draft  
**Product:** JobPilot AI — "Offline-First Autonomous AI Job Agent"  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Deployment Overview
2. Prerequisites
3. Development Deployment
4. Production Deployment (Single-User)
5. Production Deployment (Multi-User - Future)
6. Docker Configuration
7. Environment Configuration
8. Ollama Setup
9. Monitoring & Observability
10. Backup & Recovery
11. Troubleshooting

---

## 1. Deployment Overview

### 1.1 Deployment Philosophy

JobPilot AI v2.0 is designed for **single-user deployment** by default. The system runs on the user's machine with local AI inference (Ollama). This ensures privacy and offline operation.

### 1.2 Deployment Options

| Option | Description | Use Case |
|--------|-------------|----------|
| Development | Docker Compose with hot reload | Local development |
| Single-User Production | Docker Compose or systemd | Personal use on workstation/server |
| Multi-User Production | Kubernetes (future) | SaaS deployment |

---

## 2. Prerequisites

### 2.1 Hardware Requirements (Single-User)

**Minimum:**
- CPU: 4 cores
- RAM: 8GB
- Storage: 50GB SSD
- OS: Linux (Ubuntu 22.04+), Windows 10+, macOS 12+

**Recommended:**
- CPU: 8 cores
- RAM: 16GB
- Storage: 100GB SSD
- GPU: Optional (for faster AI inference)

### 2.2 Software Requirements

- Docker 24.0+
- Docker Compose 2.20+
- Java 21 (for local development)
- Node.js 18+ (for local development)
- Ollama 0.1.0+

### 2.3 Network Requirements

- Internet connection (for job board scraping only)
- No internet required for core functionality (offline-first)
- Port 8080 (API)
- Port 3000 (Frontend)
- Port 5432 (PostgreSQL)
- Port 6379 (Redis)
- Port 11434 (Ollama)

---

## 3. Development Deployment

### 3.1 Docker Compose Setup

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: pgvector/pgvector:pg16
    container_name: jobpilot-postgres
    environment:
      POSTGRES_DB: jobpilot
      POSTGRES_USER: jobpilot
      POSTGRES_PASSWORD: jobpilot_dev
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./backend/jobpilot-infrastructure/src/main/resources/db/migration:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U jobpilot"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: jobpilot-redis
    ports:
      - "6379:6379"
    command: redis-server --maxmemory 2gb --maxmemory-policy allkeys-lru
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: jobpilot-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/jobpilot
      SPRING_DATASOURCE_USERNAME: jobpilot
      SPRING_DATASOURCE_PASSWORD: jobpilot_dev
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      AI_PROVIDER_DEFAULT: ollama
      AI_OLLAMA_BASE_URL: http://host.docker.internal:11434
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    volumes:
      - ./backend:/app
      - ./uploads:/uploads
    command: mvn spring-boot:run

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: jobpilot-frontend
    ports:
      - "3000:3000"
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080
    depends_on:
      - backend
    volumes:
      - ./frontend:/app
      - /app/node_modules
      - /app/.next
    command: npm run dev

volumes:
  pgdata:
```

### 3.2 Starting Development Environment

```bash
# Clone repository
git clone https://github.com/manojkushwah91/JobPilotAi.git
cd JobPilotAi

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop services
docker-compose down
```

### 3.3 Hot Reload

Backend hot reload is enabled via Spring Boot DevTools. Frontend hot reload is enabled via Next.js dev server.

---

## 4. Production Deployment (Single-User)

### 4.1 Docker Compose Production

```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  postgres:
    image: pgvector/pgvector:pg16
    container_name: jobpilot-postgres-prod
    environment:
      POSTGRES_DB: jobpilot
      POSTGRES_USER: jobpilot
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./backups:/backups
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U jobpilot"]
      interval: 30s
      timeout: 10s
      retries: 3

  redis:
    image: redis:7-alpine
    container_name: jobpilot-redis-prod
    ports:
      - "6379:6379"
    command: redis-server --maxmemory 4gb --maxmemory-policy allkeys-lru --requirepass ${REDIS_PASSWORD}
    volumes:
      - redisdata:/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile.prod
    container_name: jobpilot-backend-prod
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/jobpilot
      SPRING_DATASOURCE_USERNAME: jobpilot
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      SPRING_REDIS_PASSWORD: ${REDIS_PASSWORD}
      AI_PROVIDER_DEFAULT: ollama
      AI_OLLAMA_BASE_URL: http://host.docker.internal:11434
      JWT_SECRET: ${JWT_SECRET}
      ENCRYPTION_KEY: ${ENCRYPTION_KEY}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    volumes:
      - ./uploads:/uploads
      - ./logs:/logs
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.prod
    container_name: jobpilot-frontend-prod
    ports:
      - "80:3000"
    environment:
      NEXT_PUBLIC_API_URL: https://localhost
    depends_on:
      - backend
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: jobpilot-nginx-prod
    ports:
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/ssl:/etc/nginx/ssl
    depends_on:
      - frontend
      - backend
    restart: unless-stopped

volumes:
  pgdata:
  redisdata:
```

### 4.2 Environment Variables

```bash
# .env
DB_PASSWORD=your_strong_password_here
REDIS_PASSWORD=your_redis_password_here
JWT_SECRET=your_jwt_secret_here_256_bits
ENCRYPTION_KEY=your_encryption_key_here_256_bits
```

### 4.3 Starting Production Environment

```bash
# Build images
docker-compose -f docker-compose.prod.yml build

# Start services
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f backend
```

### 4.4 SSL/TLS Setup

```nginx
# nginx/nginx.conf
server {
    listen 443 ssl http2;
    server_name localhost;

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;

    location / {
        proxy_pass http://frontend:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /ws/ {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

---

## 5. Production Deployment (Multi-User - Future)

### 5.1 Kubernetes Deployment

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jobpilot-api
  labels:
    app: jobpilot-api
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0
      maxSurge: 1
  selector:
    matchLabels:
      app: jobpilot-api
  template:
    metadata:
      labels:
        app: jobpilot-api
    spec:
      containers:
      - name: jobpilot-api
        image: ${ECR_REPO}/jobpilot-api:${IMAGE_TAG}
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: jobpilot-secrets
              key: db-password
        resources:
          requests:
            cpu: "2"
            memory: "4Gi"
          limits:
            cpu: "4"
            memory: "8Gi"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 15
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        startupProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          failureThreshold: 30
```

### 5.2 Horizontal Pod Autoscaler

```yaml
# hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: jobpilot-api-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: jobpilot-api
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

---

## 6. Docker Configuration

### 6.1 Backend Dockerfile

```dockerfile
# Dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build
COPY pom.xml .
COPY jobpilot-common/pom.xml jobpilot-common/
COPY jobpilot-domain/pom.xml jobpilot-domain/
COPY jobpilot-agent-runtime/pom.xml jobpilot-agent-runtime/
COPY jobpilot-ai-provider/pom.xml jobpilot-ai-provider/
COPY jobpilot-browser-automation/pom.xml jobpilot-browser-automation/
COPY jobpilot-application/pom.xml jobpilot-application/
COPY jobpilot-infrastructure/pom.xml jobpilot-infrastructure/
COPY jobpilot-interfaces/pom.xml jobpilot-interfaces/
COPY jobpilot-bootstrap/pom.xml jobpilot-bootstrap/

RUN mvn dependency:go-offline -B
COPY . .
RUN mvn package -DskipTests -Pproduction

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S jobpilot && adduser -S jobpilot -G jobpilot
USER jobpilot

WORKDIR /app
COPY --from=builder /build/jobpilot-bootstrap/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 6.2 Frontend Dockerfile

```dockerfile
# Dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM node:18-alpine

WORKDIR /app
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY package*.json ./

RUN npm ci --production

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:3000 || exit 1

CMD ["node", "server.js"]
```

---

## 7. Environment Configuration

### 7.1 Application Configuration

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/jobpilot
    username: jobpilot
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  redis:
    host: redis
    port: 6379
    password: ${REDIS_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true

agent:
  loop-interval-ms: 5000
  max-concurrent-tasks: 5
  task-timeout-seconds: 300
  enable-auto-approval: false
  approval-rule: ALL

ai:
  provider:
    default: ollama
    ollama:
      base-url: http://host.docker.internal:11434
      model: llama3
    enable-cloud-fallback: false

logging:
  level:
    com.jobpilot: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
  file:
    name: /logs/jobpilot.log
```

### 7.2 Ollama Configuration

```yaml
# Ollama configuration (managed by Ollama, not application)
models:
  - llama3
  - qwen2.5
  - mistral
  - deepseek
  - gemma
```

---

## 8. Ollama Setup

### 8.1 Installation

**Linux:**
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

**macOS:**
```bash
brew install ollama
```

**Windows:**
```powershell
winget install ollama
```

### 8.2 Pull Models

```bash
# Pull default model
ollama pull llama3

# Pull additional models (optional)
ollama pull qwen2.5
ollama pull mistral
ollama pull deepseek
ollama pull gemma
```

### 8.3 Verify Installation

```bash
# Check Ollama is running
ollama list

# Test inference
ollama run llama3 "What is 2+2?"
```

### 8.4 Auto-Detection

The application automatically detects Ollama at `http://localhost:11434` on startup. If Ollama is not detected, the application logs a warning and guides the user through installation.

---

## 9. Monitoring & Observability

### 9.1 Health Checks

**Backend Health Endpoints:**
- `/actuator/health` - Overall health
- `/actuator/health/liveness` - Liveness probe
- `/actuator/health/readiness` - Readiness probe

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0"
      }
    },
    "ollama": {
      "status": "UP",
      "details": {
        "url": "http://localhost:11434"
      }
    }
  }
}
```

### 9.2 Metrics

**Prometheus Endpoint:**
- `/actuator/prometheus` - Metrics in Prometheus format

**Key Metrics:**
- `agent_loop_duration_seconds` - Agent loop execution time
- `agent_task_success_total` - Successful task completions
- `ai_inference_duration_seconds` - AI inference time
- `browser_automation_success_total` - Successful automations
- `jobs_found_total` - Jobs discovered
- `applications_submitted_total` - Applications submitted

### 9.3 Logging

**Log Location:**
- Development: Console output
- Production: `/logs/jobpilot.log`

**Log Rotation:**
- Size-based: 100MB per file
- Time-based: Daily
- Retention: 30 days

---

## 10. Backup & Recovery

### 10.1 Database Backup

**Manual Backup:**
```bash
docker exec jobpilot-postgres-prod pg_dump -U jobpilot jobpilot > backup_$(date +%Y%m%d).sql
```

**Automated Backup (Cron):**
```bash
# Add to crontab
0 2 * * * docker exec jobpilot-postgres-prod pg_dump -U jobpilot jobpilot > /backups/jobpilot_$(date +\%Y\%m\%d).sql
```

### 10.2 Database Recovery

```bash
# Restore from backup
docker exec -i jobpilot-postgres-prod psql -U jobpilot jobpilot < backup_20240101.sql
```

### 10.3 File Backup

**Backup Uploads:**
```bash
tar -czf uploads_backup_$(date +%Y%m%d).tar.gz uploads/
```

**Restore Uploads:**
```bash
tar -xzf uploads_backup_20240101.tar.gz
```

---

## 11. Troubleshooting

### 11.1 Common Issues

**Issue: Ollama not detected**
- Solution: Install Ollama and pull required models
- Verify: `ollama list`

**Issue: Database connection failed**
- Solution: Check PostgreSQL container is running
- Verify: `docker-compose ps`

**Issue: Agent not starting**
- Solution: Check agent state in database
- Verify: Check logs for errors

**Issue: Browser automation failing**
- Solution: Check job board is accessible
- Verify: Check CAPTCHA detection logs

### 11.2 Debug Mode

Enable debug logging:

```yaml
# application-debug.yml
logging:
  level:
    com.jobpilot: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE
```

### 11.3 Reset Database

```bash
# Stop services
docker-compose down

# Remove volumes
docker volume rm jobpilot_pgdata

# Restart services
docker-compose up -d
```

---

**End of Deployment Documentation v2.0**
