# JobPilot AI — Company Intelligence

**Version:** 1.0  
**Status:** Draft  
**Phase:** 17 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Aggregate company profiles, technology stacks, interview insights, salary data, and hiring trends from multiple external sources to provide actionable intelligence during job search.

---

## 2. Domain Model

```
CompanyProfile (Aggregate Root):
  - id: UUID
  - name: String (unique)
  - description: Text
  - website, logoUrl: String
  - industry: String
  - headquarters: Location (JSON)
  - foundedYear: SmallInt
  - companySizeMin/Max: Integer
  - stockSymbol: String
  - fundingRounds: List<FundingRound> (JSON)
  - technologyStack: List<String> (JSON)     // ["React","Python","AWS","K8s"]
  - cultureKeywords: List<String> (JSON)
  - benefits: List<String> (JSON)
  - interviewNotes: List<InterviewNote> (JSON)
  - salaryData: List<SalaryDataPoint> (JSON)
  - hiringTrends: HiringTrends (JSON)
  - glassdoorRating: Decimal
  - linkedinUrl, crunchbaseUrl: String
  - lastEnrichedAt, createdAt, updatedAt

FundingRound: { date, amount, roundType, investors }
SalaryDataPoint: { role, min, max, currency, source }
InterviewNote: { role, difficulty, rounds, topics, tips, outcome }
HiringTrends: { openRoles, growthRate, recentHires }
```

---

## 3. Enrichment Sources

| Source | Data | Method |
|--------|------|--------|
| LinkedIn | Company info, recent hires, open roles | LinkedIn API + scraping |
| Glassdoor | Ratings, reviews, salary, interview insights | Glassdoor API |
| Crunchbase | Funding rounds, acquisitions, investors | Crunchbase API |
| Levels.fyi | Salary data by role/level | Web scraping |
| Company website | Tech stack (Wappalyzer-like detection) | Custom scraper |
| SEC filings (public) | Revenue, employee count | EDGAR API |

---

## 4. Enrichment Flow

```
CompanyEnrichmentScheduler (daily, midnight):
  For companies not enriched in 7+ days:
    1. LinkedIn: fetch/update company profile
    2. Glassdoor: fetch rating + reviews + salary
    3. Crunchbase: fetch funding data
    4. Company website: detect tech stack
    5. Levels.fyi: fetch salary benchmarks
    6. Aggregate and store in CompanyProfile
    7. Update lastEnrichedAt
```

---

## 5. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/companies/search` | Search companies by name |
| GET | `/api/v1/companies/{id}` | Company profile detail |
| GET | `/api/v1/companies/{id}/tech-stack` | Technology stack |
| GET | `/api/v1/companies/{id}/salary` | Salary data by role |
| GET | `/api/v1/companies/{id}/interviews` | Interview insights |
| GET | `/api/v1/companies/{id}/hiring-trends` | Hiring trends |

---

**End of Company Intelligence v1.0**
