# JobPilot AI — Job Matching Engine

**Version:** 1.0  
**Status:** Draft  
**Phase:** 15 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Compute match scores between user profiles and job listings using a hybrid heuristic + AI approach. Power the "match score" badge on job cards and provide personalized recommendations.

---

## 2. Matching Architecture

```
┌──────────────────────────────────────────────┐
│            JobMatchingService                 │
│                                              │
│  + matchJobs(userId, filters?): List<JobMatch>│
│  + getMatchScore(userId, jobId): JobMatch    │
│  + getExplanation(userId, jobId): String     │
│  + recommendJobs(userId, limit): List<JobMatch>│
└──────────────────┬───────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────┐
│         Scoring Pipeline (per job)           │
│                                              │
│  1. Skill Match (40% weight)                 │
│     - Extract user skills from profile       │
│     - Extract required skills from job       │
│     - Score = matched / total × 100          │
│                                              │
│  2. Experience Fit (30% weight)              │
│     - Compare years of experience            │
│     - Job level vs user seniority            │
│     - Title similarity (NLP)                 │
│                                              │
│  3. Location Fit (15% weight)                │
│     - Same city/region = 100%                │
│     - Same country, different city = 60%     │
│     - Remote job + user open to remote = 90% │
│     - Different country, no remote = 0%      │
│                                              │
│  4. Salary Fit (15% weight)                  │
│     - If both have salary: overlap ratio     │
│     - If missing salary: neutral (50%)       │
│                                              │
│  5. AI Enhancement (+10% bonus)              │
│     - GPT-4 generates human-readable match   │
│       explanation and nuanced fit assessment │
└──────────────────────────────────────────────┘
```

---

## 3. Match Scoring

```
Overall Score = (skillMatch × 0.40 + expFit × 0.30 + locFit × 0.15 + salaryFit × 0.15) × 100

Score Interpretation:
  90-100: Excellent match — apply immediately
  75-89:  Strong match — good fit
  60-74:  Moderate match — worth applying with tailored resume
  40-59:  Weak match — some alignment, may need significant tailoring
  0-39:   Poor match — not recommended (filtered out by default)
```

---

## 4. Skill Match Detail

```
SkillMatchAnalysis:
  - matchedSkills: ["Java", "Spring", "SQL"]
  - missingSkills: ["Kubernetes", "AWS", "Docker"]
  - additionalSkills (user has but not required): ["Python", "React"]
  - matchRate: 0.60                    // 3/5 required skills matched
  - recommendations: ["Add Kubernetes to your profile to match this role"]
```

---

## 5. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/jobs/matches` | Get matched jobs for current user (ranked) |
| GET | `/api/v1/jobs/matches/{jobId}` | Get match score for specific job |
| GET | `/api/v1/jobs/matches/{jobId}/explanation` | AI explanation of match |
| GET | `/api/v1/jobs/recommendations` | Personalized job recommendations |
| POST | `/api/v1/jobs/matches/reindex` | Re-index all match scores (admin) |

---

**End of Job Matching Engine v1.0**
