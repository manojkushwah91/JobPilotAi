# JobPilot AI — Career Analytics

**Version:** 1.0  
**Status:** Draft  
**Phase:** 19 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Analytics dashboards and actionable insights — application funnel metrics, skill gap analysis, resume score trends, market salary benchmarks, interview performance trends, and personalized career growth recommendations.

---

## 2. Analytics Architecture

```
┌──────────────────────────────────────────────┐
│          CareerAnalyticsService              │
│                                              │
│  Read Models (materialized from events):     │
│   - ApplicationFunnelStats                   │
│   - SkillGapAnalysis                         │
│   - ResumeScoreTrend                         │
│   - MarketBenchmark                          │
│   - InterviewPerformance                     │
│   - CareerTimeline                           │
│   - ActivitySummary                          │
│                                              │
│  Sources:                                    │
│   - application_events stream                │
│   - resume_scores table                      │
│   - interview_sessions table                 │
│   - saved_jobs + saved_searches              │
│   - salary_market_data                       │
└──────────────────────────────────────────────┘
```

---

## 3. Analytics Read Models

```
ApplicationFunnelStats:
  - totalApplications
  - byStatus: Map<ApplicationStatus, Integer>
  - conversionRate: Decimal   // SAVED→APPLIED → ... → OFFER
  - avgDaysPerStage: Map<ApplicationStatus, Double>
  - responseRate: Decimal     // APPLICATIONS that got first response
  - latestMonthActivity: Map<Month, Integer>

SkillGapAnalysis:
  - topRequestedSkills: List<String>
  - matchedSkills: List<String>
  - gapSkills: List<String>                       // Skills in demand but missing
  - marketDemandScore: Map<String, Decimal>       // skill → demand index
  - recommendations: List<SkillRecommendation>

ResumeScoreTrend:
  - scoreHistory: List<DateScorePoint>            // date → score
  - latestScore: Integer
  - trend: UP | STABLE | DOWN
  - topSuggestionCategories: List<SuggestionCategory>

MarketBenchmark:
  - targetRoles: String[]
  - experienceLevel: ExperienceLevel
  - salaryPercentiles: { p10, p25, p50, p75, p90 }
  - byLocation: Map<String, SalaryRange>          // city → range
  - byCompanySize: Map<String, SalaryRange>

InterviewPerformance:
  - totalSessions: Integer
  - averageScore: Decimal
  - scoreByType: Map<QuestionType, Decimal>
  - improvementRate: Decimal                      // first session vs latest
  - strongestCategory, weakestCategory

CareerTimeline:
  - events: List<CareerEvent>                     // application, interview, offer, course
  - duration: total job search duration
  - progress: String                              // "You've completed 60% of your pipeline"

ActivitySummary:
  - daysActiveThisMonth: Integer
  - applicationsThisWeek: Integer
  - interviewsScheduled: Integer
  - lastActivityDate: Instant
  - streakDays: Integer
```

---

## 4. Data Refresh Strategy

| Read Model | Refresh Strategy | Schedule |
|-----------|-----------------|----------|
| ApplicationFunnelStats | Event-driven on status change | Real-time |
| SkillGapAnalysis | On-demand + nightly rebuild | On-demand / daily |
| ResumeScoreTrend | Event-driven on new score | Real-time |
| MarketBenchmark | Nightly cache refresh (external data) | Daily |
| InterviewPerformance | On session completion | Real-time |
| CareerTimeline | Event-driven | Real-time |
| ActivitySummary | Event-driven | Real-time |

---

## 5. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/analytics/overview` | Dashboard overview (aggregated) |
| GET | `/api/v1/analytics/application-funnel` | Application pipeline funnel |
| GET | `/api/v1/analytics/skill-gaps` | Skill gap analysis |
| GET | `/api/v1/analytics/resume-trend` | Resume score trend |
| GET | `/api/v1/analytics/market-benchmarks` | Salary + market data |
| GET | `/api/v1/analytics/interview-performance` | Interview performance |
| GET | `/api/v1/analytics/timeline` | Career timeline |
| GET | `/api/v1/analytics/activity` | Activity summary & streak |

---

**End of Career Analytics v1.0**
