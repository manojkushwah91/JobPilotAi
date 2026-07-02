# JobPilot AI — Logging & Observability

**Version:** 1.0  
**Status:** Draft  
**Phase:** 27 of 35  
**Author:** Chief Software Architect  

---

## 1. Observability Pillars

```
┌──────────────────────────────────────────────────────────────┐
│                   Observability Stack                        │
│                                                              │
│  Logs    → ELK Stack (Elasticsearch + Logstash + Kibana)    │
│  Metrics → Prometheus + Grafana (custom dashboards)         │
│  Traces  → OpenTelemetry + Jaeger (distributed tracing)     │
│  Alerts  → Alertmanager (PagerDuty / Slack / Email)         │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. Logging Strategy

```
Framework: Logback (SLF4J)
Format: JSON (structured logging — logstash-logback-encoder)

Log Levels:
  REST Controller endpoints:     INFO  (request method + path + duration + status)
  Service domain logic:          INFO  (key decisions, state transitions)
  Repository/DB queries:         DEBUG (parameterized query logging)
  AI provider calls:             INFO  (provider, model, tokens, duration)
  Browser automation:            INFO  (action, sessionId, status, screenshots)
  Security (auth failures):      WARN  (failed login, invalid token, rate limit hit)
  Security (suspicious):         ERROR (possible brute force, CSRF attempts)
  Errors (expected):             WARN  (validation errors, not found, bad request)
  Errors (unexpected):           ERROR (500s, NPE, DB failures)

MDC Fields (injected via OncePerRequestFilter):
  - traceId (UUID, propagated across async boundaries)
  - spanId
  - userId
  - sessionId
  - requestId
  - clientIp
  - userAgent
  - correlationId (for event-driven flows)
```

---

## 3. Metrics (Prometheus)

```
Custom metrics (Micrometer @Timed, MeterRegistry):

  Application metrics:
    application.created.total         Counter
    application.status.change        Counter (tagged by from→to)
    application.automation.success   Counter
    application.automation.failure   Counter

  AI metrics:
    ai.inference.calls               Counter (tagged by provider + model)
    ai.inference.duration            Timer
    ai.inference.tokens              DistributionSummary
    ai.inference.cost                Counter (tagged by provider)
    ai.cache.hit.ratio               Gauge
    ai.provider.fallback             Counter

  Resume metrics:
    resume.scored                    Counter
    resume.ats.score                 Gauge (average)
    resume.generated                 Counter

  Performance metrics:
    api.request.duration             Timer (tagged by endpoint + method + status)
    api.request.total                Counter (tagged by endpoint + status)
    db.query.duration                Timer
    db.connection.pool.active        Gauge
    db.connection.pool.idle          Gauge
    cache.hit.ratio                  Gauge (per cache region)

  Business metrics:
    user.active.daily                Gauge
    user.registrations               Counter
    subscription.upgrade             Counter
    subscription.downgrade           Counter
    job.listings.scraped             Counter

Alert rules (sample):
  - ai.inference.error.rate > 5% over 5min → P1 (critical)
  - api.5xx.rate > 1% over 5min → P2
  - db.connection.pool.active > 80% → P2
  - application.automation.failure.rate > 10% → P3
```

---

## 4. Distributed Tracing (OpenTelemetry)

```
Propagation: W3C Trace Context (traceparent header)
Auto-instrumentation: Spring Boot + JDBC + HTTP + RabbitMQ + Redis
Sampling:
  - Production: 10% (head-based), 100% for errors
  - Staging: 100%
  - Dev: 0%

Spans per request:
  HTTP Request → Controller → Service → Repository → DB
                                       → AI Provider → HTTP call
                                       → RabbitMQ publish
                                       → Cache read/write

Trace view in Jaeger:
  ┌─ POST /resumes/{id}/score ─────────────────────── 4.2s
  │  ├─ Controller.getResumeScore ████████████ 12ms
  │  ├─ Service.loadResume       ██ 3ms
  │  ├─ Service.loadJobDesc      █ 1ms
  │  ├─ AtsAnalyzer.score        ██████████████████████████████████████ 4.1s
  │  │  ├─ AiService.generate    ████████████████████████████████████ 3.9s
  │  │  └─ Cache.put             █ 2ms
  │  └─ Response serialization   █ 1ms
```

---

## 5. Logging Sample Config

```yaml
# logback-spring.xml
<appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <includeMdc>true</includeMdc>
    <timestampPattern>yyyy-MM-dd'T'HH:mm:ss.SSSZ</timestampPattern>
  </encoder>
</appender>

<logger name="com.jobpilot" level="INFO"/>
<logger name="org.springframework.security" level="WARN"/>
<logger name="org.hibernate.SQL" level="DEBUG"/>
<logger name="org.hibernate.type.descriptor.sql" level="TRACE"/>
```

---

**End of Logging & Observability v1.0**
