# JobPilot AI — Search Engine

**Version:** 1.0  
**Status:** Draft  
**Phase:** 24 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Unified search across all platform entities (jobs, companies, resumes, users) with full-text, semantic, and faceted search capabilities.

---

## 2. Search Capabilities

```
┌────────────────────────────────────────────────────┐
│                  SearchEngine                       │
│                                                    │
│  Unified search: GET /api/v1/search?q=...&type=...│
│                                                    │
│  Search Types:                                     │
│   - JOBS (full-text on title, description, skills) │
│   - COMPANIES (full-text on name, description)     │
│   - RESUMES (user's own full-text search)          │
│                                                    │
│  Primary: PostgreSQL full-text search (tsvector)   │
│  Secondary: pgvector cosine similarity (embeddings)│
│  Combined: Reciprocal Rank Fusion (RRF)            │
└────────────────────────────────────────────────────┘
```

---

## 3. Text Search (PostgreSQL FTS)

```
Indexed columns:
  jobs.title:                  tsvector (English, weighted A)
  jobs.description:             tsvector (English, weighted B)
  jobs.company_name:            tsvector (English, weighted A)
  jobs.skills:                  tsvector (English, weighted A)
  companies.name:               tsvector (English, weighted A)
  companies.description:        tsvector (English, weighted B)
  resumes.title:                tsvector (English, weighted A)
  resumes.sections:             tsvector (English, weighted C)

Index type: GIN on tsvector columns
Query: plainto_tsquery('english', :query) with
        ts_rank_cd(tsv, query, 32) as score
```

---

## 4. Semantic Search (pgvector)

```
Embedding model: text-embedding-3-small (1536 dimensions)
Index: IVFFlat (lists=100), cosine distance
Threshold: min similarity 0.7

Query: SELECT * FROM jobs
        ORDER BY embeddings <=> :queryEmbedding
        LIMIT 20
```

---

## 5. Fusion: RRF

```
RRF Score = Σ( 1 / (60 + rank_text(i)) + 1 / (60 + rank_semantic(i)) )
Top K results returned with faceted aggregation.

Facets (returned alongside results):
  - job_type: { FULL_TIME: 42, CONTRACT: 8, ... }
  - experience_level: { SENIOR: 25, MID: 20, ... }
  - location: { "San Francisco": 15, "Remote": 30, ... }
  - salary_range: { "100k-150k": 20, ... }
  - posted_date: { "Last 24h": 5, "Last 7d": 30, ... }
```

---

## 6. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/search` | Unified search (q, type, filters, page) |
| GET | `/api/v1/search/suggestions` | Auto-complete suggestions |
| POST | `/api/v1/search/reindex` | Rebuild search indexes (admin) |

---

**End of Search Engine v1.0**
