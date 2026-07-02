# JobPilot AI — Frontend Implementation Plan

**Version:** 1.0  
**Status:** Draft  
**Phase:** 30 of 35  
**Author:** Chief Software Architect  

---

## 1. Build Order

```
Iteration 1 — Foundation (~20 files)
  ├── Next.js project setup (app router, tsconfig, tailwind, shadcn/ui)
  ├── Design tokens + theme
  ├── Layout components (Navbar, Sidebar, Footer)
  ├── Auth pages (login, register, OAuth callback)
  ├── API client setup (Axios, interceptors, React Query providers)
  └── Protected route middleware

Iteration 2 — Resume Studio (~15 files)
  ├── Resume list page
  ├── Resume editor (drag-drop sections, Tailor button)
  ├── ATS Score display component
  └── Cover letter generator page

Iteration 3 — Job Discovery (~18 files)
  ├── Job search page (filters, facets, pagination)
  ├── Job detail page (match badge, save, apply)
  ├── Saved searches page
  ├── Job recommendations page (personalized)
  └── Application tracker Kanban board

Iteration 4 — Dashboard & Analytics (~12 files)
  ├── Dashboard overview page
  ├── Application funnel chart
  ├── Skill gap visualization
  ├── Interview hub
  └── Company intelligence pages

Iteration 5 — Polish (~10 files)
  ├── Settings pages (profile, notifications, billing, security)
  ├── Admin dashboard (users, metrics, audit log, flags)
  └── Landing page (marketing)
```

---

## 2. Route Map

```
/                     → Landing page (public)
/login                → Login page (public)
/register             → Register page (public)
/auth/callback        → OAuth callback handler (public)

/dashboard            → Analytics overview (auth)
/resumes              → Resume list (auth)
/resumes/new          → Create resume (auth)
/resumes/[id]         → Resume editor (auth)
/resumes/[id]/score   → ATS score detail (auth, PRO+)
/cover-letters        → Cover letter list (auth)
/cover-letters/[id]   → Cover letter editor (auth)
/jobs                 → Job search (auth)
/jobs/[id]            → Job detail (auth)
/jobs/matches         → Personalized matches (auth)
/jobs/recommendations → AI recommendations (auth)
/jobs/saved           → Saved jobs (auth)
/saved-searches       → Saved searches (auth)
/applications         → Tracker Kanban (auth)
/applications/[id]    → Application detail (auth)
/interviews           → Interview sessions (auth)
/interviews/[id]      → Mock interview (auth)
/companies            → Company search (auth)
/companies/[id]       → Company profile (auth)
/settings             → Settings (auth)
/settings/*           → Sub-pages (auth)
/admin                → Admin dashboard (auth, ADMIN)
/admin/*              → Admin sub-pages (auth, ADMIN)
```

---

## 3. Component Tree

```
<App>
  <Providers>          // React Query, Theme, Auth
    <Layout>
      <Navbar />      // Logo, search, notifications, user menu
      <Sidebar />     // Navigation links (collapsible)
      <main>
        {children}    // Page content (SSR/ISR/CSR per route)
      </main>
      <Footer />
    </Layout>
  </Providers>
</App>

Dashboard:
  <DashboardOverview>
    <ApplicationFunnelChart />
    <SkillGapChart />
    <ResumeScoreTrend />
    <ActivitySummary />
    <RecentApplications />
  </DashboardOverview>

Job Search:
  <JobSearchPage>
    <SearchBar />
    <FacetedFilters />      // Location, type, salary, remote, etc.
    <JobList>                // Virtualized scroll
      <JobCard>             // Title, company, match badge, salary, location
        <MatchScoreBadge /> // Green/orange/red based on score
      </JobCard>
    </JobList>
    <Pagination />
  </JobSearchPage>
```

---

## 4. State Management

```
Global (Zustand):
  - auth: { user, token, isAuthenticated }
  - ui: { sidebarOpen, theme, activeModal }
  - notifications: { unreadCount, list }

Server State (React Query):
  - resumes, jobs, applications, savedSearches
  - settings, analytics, interviewSessions
  - companies, notifications

URL State:
  - search params (q, filters, page, sort)
  - tab selection
  - modal IDs

Local State (useState):
  - form input values (before submit)
  - dropdown open/close
  - tooltip visibility
```

---

## 5. Data Fetching Strategy

| Route | Strategy | Reason |
|-------|----------|--------|
| Landing | Static (ISR 1h) | Marketing content, SEO |
| Job search | SSR | Dynamic filters, SEO |
| Job detail | SSR | SEO, initial paint |
| Resume editor | CSR | Interactive, real-time saves |
| Dashboard | CSR (React Query) | Personalized, cached |
| Admin | CSR (React Query) | Real-time metrics |
| Settings | CSR (React Query) | Form-heavy |
| Interview | CSR | Real-time scoring |

---

**End of Frontend Implementation Plan v1.0**
