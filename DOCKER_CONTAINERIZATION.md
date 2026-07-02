# JobPilot AI — Docker & Containerization

**Version:** 1.0  
**Status:** Draft  
**Phase:** 32 of 35  
**Author:** Chief Software Architect  

---

## 1. Container Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Compose (dev)                     │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │ jobpilot │  │ postgres │  │  redis   │  │ rabbitmq │  │
│  │ -api:dev │  │  :16     │  │  :7-alp  │  │ :3-mgmt  │  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  │
│       │             │             │             │         │
│       └─────────────┴─────────────┴─────────────┘         │
│                       Network: jobpilot-net                 │
└─────────────────────────────────────────────────────────────┘

Production (Kubernetes):
  ├── Deployment: jobpilot-api (3 replicas, rolling update)
  │   ├── Resource req: 2 CPU / 4GB RAM
  │   ├── Resource limit: 4 CPU / 8GB RAM
  │   └── Probes: liveness (/actuator/health/liveness),
  │               readiness (/actuator/health/readiness),
  │               startup (initialDelay=30s)
  ├── Service: ClusterIP (port 8080)
  ├── Ingress: ALB (AWS) + SSL termination
  ├── ConfigMap: application-prod.yml (non-sensitive)
  └── Secret: DB creds, JWT keys, API keys, SendGrid key
```

---

## 2. Dockerfile (Multi-stage)

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY jobpilot-common/pom.xml jobpilot-common/
COPY jobpilot-domain/pom.xml jobpilot-domain/
# ... (all module POMs)
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn package -DskipTests -Pproduction

# Stage 2: Runtime
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

---

## 3. Docker Compose (Dev)

```yaml
version: '3.8'
services:
  postgres:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_DB: jobpilot
      POSTGRES_USER: jobpilot
      POSTGRES_PASSWORD: jobpilot_dev
    ports: ['5432:5432']
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./db/init.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    ports: ['6379:6379']
    command: redis-server --maxmemory 2gb --maxmemory-policy allkeys-lru

  rabbitmq:
    image: rabbitmq:3-management
    ports: ['5672:5672', '15672:15672']
    environment:
      RABBITMQ_DEFAULT_USER: jobpilot
      RABBITMQ_DEFAULT_PASS: jobpilot_dev

volumes:
  pgdata:
```

---

## 4. Kubernetes Manifests (Key Resources)

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
---
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

**End of Docker & Containerization v1.0**
