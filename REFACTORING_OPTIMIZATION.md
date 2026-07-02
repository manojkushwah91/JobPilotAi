# JobPilot AI — Refactoring & Optimization

**Version:** 1.0  
**Status:** Draft  
**Phase:** 34 of 35  
**Author:** Chief Software Architect  

---

## 1. Performance Targets

| Metric | Target | Current (est.) | Gap |
|--------|--------|---------------|-----|
| P95 API latency | <500ms | ~1.2s (AI calls) | Acceptable (AI-bound) |
| P99 non-AI API latency | <200ms | ~300ms | Cache tuning + connection pool |
| DB query P99 | <50ms | ~80ms | Index review + connection pool |
| Page load (SSR) | <2s | ~3s | ISR + lazy loading |
| Lighthouse score | >90 | ~75 | Code splitting + image opt |
| Concurrent users per pod | 500 | ~300 | Thread pool + connection pool |

---

## 2. Optimization Plan

```
Phase 1 (2 days):
  - Analyze slow queries via pg_stat_statements
  - Add missing composite indexes
  - Tune PostgreSQL: shared_buffers, work_mem, effective_cache_size
  - Tune HikariCP: maxPoolSize=20, minIdle=5, connectionTimeout=5000
  - Enable PostgreSQL query JIT compilation

Phase 2 (2 days):
  - Add Redis caching for frequently accessed endpoints
  - Add Caffeine L1 cache for hot data
  - Implement response compression (gzip/brotli)
  - Optimize JSON serialization (Jackson: enable afterburner)

Phase 3 (2 days):
  - Frontend code splitting (dynamic imports for charts, editors)
  - Image optimization (next/image, WebP format)
  - Font subsetting
  - Bundle analysis + tree shaking
  - Implement ISR for job search pages

Phase 4 (2 days):
  - Connection pooling for AI provider HTTP clients
  - HTTP/2 for downstream API calls
  - AI response streaming (SSE)
  - Batch embeddings generation

Phase 5 (1 day):
  - JVM tuning: G1GC, -Xms=-Xmx, -XX:MaxGCPauseMillis=100
  - Thread pool tuning: taskExecutor core=10, max=50
  - Rate limit tuning per tier
```

---

## 3. Refactoring Checklist

```
Pre-release refactoring:
  [ ] Extract magic strings → constants/enums
  [ ] Replace if-else chains → polymorphism/strategy
  [ ] Split God classes (any service >300 lines)
  [ ] Remove unused imports + dead code
  [ ] Standardize exception handling (ControllerAdvice audit)
  [ ] Ensure all @Transactional annotations have proper boundaries
  [ ] Verify no N+1 queries (add @EntityGraph where needed)
  [ ] Add missing @Index annotations on JPA entities
  [ ] Ensure all API responses use ApiResponse wrapper
  [ ] Verify all dates use Instant (not LocalDateTime)
  [ ] Standardize pagination (PageResponse)
  [ ] Remove System.out.println / log.debug (for prod config)
  [ ] Add equals/hashCode to all entities (by UUID only)
  [ ] Verify dependency injection (no field injection, all constructor)
```

---

**End of Refactoring & Optimization v1.0**
