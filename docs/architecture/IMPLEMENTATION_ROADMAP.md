# Implementation Roadmap

## Recommended Order (with Rationale)

```
Phase 1: Platform Foundation (68 SP) ⬅️ YOU ARE HERE
  └─ Everything else depends on this

Phase 2: Authentication & Identity (34 SP)
  └─ Every feature requires authenticated users

Phase 3: User & Profile (21 SP)
  └─ Every feature needs user profile data

Phase 4: AI Platform (26 SP)
  └─ Resume Studio, Cover Letter, Interview Hub depend on AI

Phase 5: Resume Studio (34 SP)
  └─ Core value proposition — users join for resumes

Phase 6: Job Discovery (34 SP)
  └─ Users need jobs before tracking applications

Phase 7: Application Management (26 SP)
  └─ Natural next step after finding jobs

Phase 8: Company Intelligence (21 SP)
  └─ Enhances job discovery + applications

Phase 9: Interview Hub (29 SP)
  └─ Users apply → get interviews → need prep

Phase 10: Career Analytics (21 SP)
  └─ Meaningful after users have data to analyze

Phase 11: Browser Automation (21 SP)
  └─ Advanced feature — good to have after core is stable

Phase 12: Production Readiness (21 SP)
  └─ Polish, security audit, load testing, docs
```

## Why This Order?

### 1. Platform Foundation First
Foundation is not infrastructure for its own sake — every single feature depends on it. Without the common module, domain entities, Flyway migrations, and CI pipeline, all subsequent work will be inconsistent, untestable, and unreleasable. Starting with foundation means every subsequent feature team works on a stable, standardized platform.

### 2. Authentication Before Features
Building features without authentication is building in a vacuum. Auth touches every API endpoint, every frontend page, every security decision. Implementing it first means:
- Every subsequent PR includes proper @PreAuthorize annotations
- Frontend routes have auth guards from day one
- No retrofitting security onto existing endpoints
- OAuth integration tested once, used everywhere

### 3. AI Platform Before AI Features
The AI Provider Layer and Prompt Engine are the backbone of Resume Studio, Cover Letter Engine, Interview Hub, and ATS Optimizer. Building AI capabilities on top of ad-hoc AI integration would create a maintenance nightmare. By abstracting AI first:
- All AI features share the same circuit breaker, caching, cost tracking
- Provider selection is config per use case — not hardcoded per feature
- Prompt versioning is centralized — update once, effect everywhere
- Switch from GPT-4 to Claude 3 is a config change, not a code change

### 4. Resume Studio as First Feature
Resume Studio is the highest-value feature:
- Direct value to job seekers (the reason they sign up)
- Demonstrates AI integration end-to-end
- Generates the ATS scoring data that feeds into Analytics
- Resume data is used by Cover Letter Engine, Application Tracker, and Analytics
- Building it early validates the architecture with real business logic

### 5. Job Discovery Before Application Tracker
Users need to find jobs before they can track applications. Job Discovery feeds directly into Application Tracker via the "Apply" button. Building them in this order means:
- Application Tracker can be tested with real job data immediately
- The "SAVED" → "APPLIED" transition is natural
- Match scoring provides immediate value in the job search UI

### 6. Application Tracker Before Interview Hub
Users apply → get responses → schedule interviews. The state machine naturally flows SAVED → APPLIED → PHONE_SCREEN → TECHNICAL_INTERVIEW. Building Application Tracker first means Interview Hub can consume its data (e.g., "Which applications are in TECHNICAL_INTERVIEW status? Prep for those.")

### 7. Analytics After Data Exists
Analytics is meaningless without data. Building it after 4-5 features are live means:
- Real application pipeline data for funnel visualization
- Actual resume scores for trend analysis
- Real interview performance data for skill gap analysis
- The dashboards are immediately useful, not placeholders

### 8. Browser Automation Last
Automation is the most complex, fragile, and controversial feature:
- Requires Playwright Java with stealth patches
- External portal changes can break it at any time
- Legal/compliance considerations for automated form submission
- High maintenance cost relative to other features

Building it last means:
- The rest of the platform is stable and released
- Users already have value from manual application management
- Automation is a "nice to have" power feature, not a core dependency

## Test Strategy Per Phase

| Phase | Unit | Integration | E2E | Focus |
|-------|------|-------------|-----|-------|
| Foundation | ✅ | ✅ | ❌ | ArchUnit rules, Flyway migration |
| Auth | ✅ | ✅ | ❌ | JWT validity, OAuth flow, RBAC |
| User/Profile | ✅ | ✅ | ❌ | CRUD validation, GDPR export |
| AI Platform | ✅ | ✅ | ❌ | Provider selection, circuit breaker |
| Resume Studio | ✅ | ✅ | ❌ | ATS scoring, PDF parsing |
| Job Discovery | ✅ | ✅ | ❌ | Source adapters, search ranking |
| App Tracker | ✅ | ✅ | ✅ | State machine, Kanban UI |
| Company Intel | ✅ | ✅ | ❌ | Enrichment pipeline |
| Interview Hub | ✅ | ✅ | ✅ | Mock session flow |
| Analytics | ✅ | ✅ | ❌ | Metric aggregation |
| Automation | ✅ | ❌ | ✅ | Cross-portal application submit |
| Production | — | — | ✅ | Full regression |

E2E (Playwright Java) is added in App Tracker because that's when critical user paths emerge: register → find job → apply → track → interview → offer.
