# JobPilot AI — Caching Strategy

**Version:** 1.0  
**Status:** Draft  
**Phase:** 25 of 35  
**Author:** Chief Software Architect  

---

## 1. Cache Layers

```
┌──────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│                                                              │
│  L1: Caffeine (in-process, per JVM)                         │
│  L2: Redis (distributed, cluster mode)                      │
│  L3: PostgreSQL (materialized views / read replicas)        │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. Caffeine Cache (L1)

```
CacheManager:
  - ResumeSectionsCache:        maxSize=100, expireAfterWrite=30m
  - UserProfileCache:           maxSize=500, expireAfterWrite=10m
  - CompanyProfileCache:        maxSize=200, expireAfterWrite=60m
  - FeatureFlagCache:           maxSize=50,  expireAfterWrite=5m
  - JobSourceConfigCache:       maxSize=20,  expireAfterWrite=5m
  - AtsAnalysisResultCache:     maxSize=100, expireAfterWrite=60m

Benefit: Sub-millisecond access, no network hop.
Downside: Lost on JVM restart, not shared across pods.
```

---

## 3. Redis Cache (L2) — Primary

```
RedisTemplate<String, Object> with JSON serialization (Jackson)

Cache Regions (key prefix → TTL → eviction):
  ┌──────────────────────┬───────────────────┬──────────┐
  │ Key Prefix            │ TTL               │ Use Case │
  ├──────────────────────┼───────────────────┼──────────┤
  │ session:{token}       │ session timeout   │ Opaque   │
  │                       │ (24h prod, 2h free)│ refresh  │
  │                       │                   │ tokens   │
  │ user:{id}:profile     │ 15 min            │ User     │
  │                       │                   │ profile  │
  │ resume:{id}:sections  │ 30 min            │ Resume   │
  │                       │                   │ sections │
  │ resume:{id}:score     │ 60 min            │ ATS      │
  │                       │                   │ score    │
  │ jobs:search:{hash}    │ 10 min            │ Job      │
  │                       │                   │ search   │
  │                       │                   │ results  │
  │ company:{id}:profile  │ 60 min            │ Company  │
  │                       │                   │ profile  │
  │ job:{id}:listing      │ 15 min            │ Job      │
  │                       │                   │ listing  │
  │ market:benchmarks     │ 12 hours          │ Salary   │
  │                       │                   │ data     │
  │ rate-limit:{key}      │ sliding window    │ Rate     │
  │                       │                   │ limiting │
  │ feature-flags         │ 1 min             │ Feature  │
  │                       │                   │ flags    │
  │ ai:provider:usage     │ 1 hour            │ Token    │
  │                       │                   │ usage    │
  └──────────────────────┴───────────────────┴──────────┘

Eviction: LRU (allkeys-lru) for keys without explicit TTL
Memory: maxmemory 2GB with maxmemory-policy allkeys-lru
```

---

## 4. Cache-Aside Pattern (Standard)

```
Read:
  1. Check L1 (Caffeine) → hit → return
  2. Check L2 (Redis) → hit → populate L1 → return
  3. Query DB → populate L2 → populate L1 → return

Write:
  1. Write to DB
  2. Invalidate L2 key
  3. Invalidate L1 key

Exception for AI results: Write-through (cache until TTL)
Exception for rate limits: Write-behind (increment in Redis, periodic sync)
```

---

## 5. Cache Invalidation Events

| Trigger | Invalidation Action |
|---------|-------------------|
| User profile updated | `user:{id}:profile` invalidated |
| Resume edited | `resume:{id}:sections` + `resume:{id}:score` invalidated |
| Job listing scraped | `job:{id}:listing` invalidated |
| Feature flag toggled | `feature-flags` invalidated |
| Company enriched | `company:{id}:profile` invalidated |
| Search results stale | `jobs:search:*` pattern invalidation (by TTL only) |

---

**End of Caching Strategy v1.0**
