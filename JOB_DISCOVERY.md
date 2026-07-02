# JobPilot AI — Job Discovery

**Version:** 1.0  
**Status:** Draft  
**Phase:** 14 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Aggregate job listings from multiple external sources, provide full-text and semantic search, rich filtering, and personalized job alerts.

---

## 2. Domain Model

```
JobListing (Aggregate Root):
  - id: UUID
  - source: String (LINKEDIN, INDEED, GLASSDOOR, GOOGLE_JOBS, COMPANY, MANUAL)
  - sourceId: String                    // Source's internal ID (for dedup)
  - title: String
  - companyName: String
  - companyLogoUrl: String
  - companyId: UUID (FK to company_profiles)
  - location: JobLocation (JSON)
  - salary: SalaryRange (JSON)
  - description: Text
  - requirements: List<String> (JSON)
  - responsibilities: List<String> (JSON)
  - benefits: List<String> (JSON)
  - employmentType: EmploymentType
  - experienceLevel: ExperienceLevel
  - industry: String
  - skills: List<String> (JSON)
  - applicationUrl: String
  - postedAt: Instant
  - scrapedAt: Instant
  - embeddings: vector(1536)
  - isActive: Boolean
  - createdAt, updatedAt

JobLocation: { city, state, country, remoteType: ONSITE|REMOTE|HYBRID }
SalaryRange: { min, max, currency, period: YEARLY|MONTHLY|HOURLY }
EmploymentType: FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, TEMPORARY
ExperienceLevel: ENTRY, MID, SENIOR, LEAD, EXECUTIVE

SavedSearch (Entity):
  - id: UUID
  - userId: UUID
  - name: String
  - query: SearchQuery (JSON)
  - notificationsEnabled: Boolean
  - notifyFrequency: INSTANT|DAILY|WEEKLY
  - lastNotifiedAt: Instant

SavedJob (join table):
  - userId, jobListingId, notes, savedAt
```

---

## 3. Source Adapters (Strategy Pattern)

```
JobSourceAdapter (interface):
  + fetchNewJobs(since: Instant): List<RawJobData>
  + fetchJobDetails(sourceId: String): RawJobData
  + sourceName(): String
  + isAvailable(): Boolean

Implementations:
  - IndeedAdapter          (REST API)
  - LinkedInAdapter        (REST API + scraping fallback)
  - GoogleJobsAdapter      (REST API)
  - GlassdoorAdapter       (REST API)
  - CompanyCareerPageAdapter (HTML scraping via Jsoup)

Each adapter:
  - Configurable via job_source_configs table
  - Rate-limit aware (reads source config)
  - Transforms RawJobData to canonical JobListing
```

---

## 4. Aggregation Flow

```
JobAggregationScheduler (@Scheduled, every 30 min):
  For each active job_source_config:
    1. Instantiate adapter via reflection (adapter_class)
    2. Call adapter.fetchNewJobs(lastScrapedAt)
    3. For each raw job:
       a. Transform to JobListing
       b. Dedup: find existing by (source, sourceId)
       c. If new: insert
       d. If existing with changes: update
    4. For new/updated listings:
       a. Generate embeddings (batch: call AI embedding service)
       b. Store in job_listings table
    5. For each active SavedSearch:
       a. Match against new listings
       b. If matched: publish NewJobMatchEvent → notification service
    6. Update job_source_config.lastRunAt + lastRunStatus
```

---

## 5. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/jobs` | Search jobs (query, filters, pagination) |
| GET | `/api/v1/jobs/{id}` | Job detail |
| GET | `/api/v1/jobs/sources` | List job sources |
| POST | `/api/v1/jobs/{id}/save` | Save job |
| DELETE | `/api/v1/jobs/{id}/save` | Unsave job |
| GET | `/api/v1/jobs/saved` | List saved jobs |
| GET | `/api/v1/jobs/facets` | Get filter facet counts |
| POST | `/api/v1/jobs/semantic-search` | AI-powered semantic search |
| GET | `/api/v1/saved-searches` | List saved searches |
| POST | `/api/v1/saved-searches` | Create saved search |
| PUT | `/api/v1/saved-searches/{id}` | Update saved search |
| DELETE | `/api/v1/saved-searches/{id}` | Delete saved search |

---

**End of Job Discovery v1.0**
