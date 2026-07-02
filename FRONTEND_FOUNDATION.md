# JobPilot AI — Frontend Foundation

**Version:** 1.0  
**Status:** Draft  
**Phase:** 6 of 35  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Next.js Architecture
2. Component Tree
3. Folder Structure
4. State Management
5. Routing & Navigation
6. Theme & Design System
7. Data Fetching Strategy
8. Form Handling
9. Error Handling
10. Performance Strategy

---

## 1. Next.js Architecture

### 1.1 Framework Choice

| Decision | Value | Rationale |
|----------|-------|-----------|
| Framework | Next.js 14 (App Router) | SSR for SEO, ISR for job listings, RSC for performance |
| Language | TypeScript 5.4+ | Strict mode, type safety across all components |
| Styling | Tailwind CSS 4 | Utility-first, design system tokens |
| Components | shadcn/ui (Radix UI primitives) | Accessible, customizable, tree-shakeable |
| State | React Query + Zustand | Server cache vs client state separation |
| Forms | React Hook Form + Zod | Type-safe validation, performant |

### 1.2 Rendering Strategy

| Page Type | Strategy | Rationale |
|-----------|----------|-----------|
| Marketing / Landing | SSG (Static Generation) | Fastest load, no per-request cost |
| Job listing detail | ISR (revalidate: 300s) | Content changes minutely, stale-while-revalidate |
| Job search results | SSR + Client hydrate | Personalized per user, need fresh data |
| Dashboard | CSR (Client-side fetch) | Fully personalized, cached via React Query |
| Resume editor | CSR | Rich client interaction, real-time preview |
| Interview simulator | CSR | Real-time updates, voice/audio handling |
| Admin panel | SSR + CSR | Mix of static layout + dynamic data |

### 1.3 App Router Layout

```
app/
├── layout.tsx                        # Root layout (fonts, metadata, providers)
├── page.tsx                          # Landing page (/) — SSG
├── (auth)/                           # Auth routes — Route Group (no dashboard layout)
│   ├── layout.tsx                    # Auth layout (centered card, no sidebar)
│   ├── login/page.tsx                # Login page
│   ├── register/page.tsx             # Registration page
│   ├── forgot-password/page.tsx
│   └── reset-password/page.tsx
├── (dashboard)/                      # Dashboard routes — Route Group
│   ├── layout.tsx                    # Dashboard layout (sidebar, header, content)
│   ├── dashboard/
│   │   ├── page.tsx                  # Main dashboard — CSR
│   │   └── loading.tsx               # Skeleton loading state
│   ├── jobs/
│   │   ├── page.tsx                  # Job search — SSR + client hydrate
│   │   ├── [id]/page.tsx             # Job detail — ISR
│   │   └── saved/page.tsx            # Saved jobs — CSR
│   ├── applications/
│   │   ├── page.tsx                  # ATS pipeline — CSR
│   │   └── [id]/page.tsx             # Application detail — CSR
│   ├── resumes/
│   │   ├── page.tsx                  # Resume list — CSR
│   │   └── [id]/page.tsx             # Resume editor — CSR
│   ├── interviews/
│   │   ├── page.tsx                  # Interview sessions — CSR
│   │   └── [id]/page.tsx             # Interview session — CSR
│   ├── career/
│   │   ├── page.tsx                  # Career analytics — CSR
│   │   └── path/page.tsx             # Career path — CSR
│   ├── networking/
│   │   └── page.tsx                  # Networking — CSR
│   ├── analytics/
│   │   └── page.tsx                  # Analytics dashboard — CSR
│   ├── settings/
│   │   └── page.tsx                  # Settings — CSR
│   ├── billing/
│   │   └── page.tsx                  # Billing — CSR
│   └── admin/                        # Admin routes — Route Group
│       ├── layout.tsx                # Admin layout (restricted to ADMIN role)
│       ├── users/page.tsx
│       ├── jobs/page.tsx
│       ├── ai-providers/page.tsx
│       └── logs/page.tsx
├── api/                              # API routes (BFF layer if needed)
│   └── webhooks/
│       └── stripe/route.ts           # Stripe webhook handler
├── not-found.tsx                     # Custom 404
├── error.tsx                         # Global error boundary
└── loading.tsx                       # Global loading state
```

---

## 2. Component Tree

### 2.1 Top-Level Component Hierarchy

```
<RootLayout>
  <Providers>                           // Theme, QueryClient, Auth, Toaster
    <RootStyleRegistry />               // Tailwind + shadcn/ui setup
    {children}                          // Route content
    <Toaster />                         // Toast notifications
  </Providers>
</RootLayout>

<AuthLayout>                            // Route group: (auth)
  <AuthCard>
    <Logo />
    {children}                          // Login, Register, ForgotPassword forms
  </AuthCard>
</AuthLayout>

<DashboardLayout>                       // Route group: (dashboard)
  <Sidebar>
    <SidebarHeader>
      <Logo />
      <SubscriptionBadge />
    </SidebarHeader>
    <SidebarNavigation>
      <NavItem icon="LayoutDashboard" label="Dashboard" href="/dashboard" />
      <NavItem icon="Search" label="Jobs" href="/jobs" />
      <NavItem icon="Briefcase" label="Applications" href="/applications" />
      <NavItem icon="FileText" label="Resumes" href="/resumes" />
      <NavItem icon="Mic" label="Interviews" href="/interviews" />
      <NavItem icon="TrendingUp" label="Career" href="/career" />
      <NavItem icon="Users" label="Networking" href="/networking" />
      <NavItem icon="BarChart" label="Analytics" href="/analytics" />
    </SidebarNavigation>
    <SidebarFooter>
      <UpgradeCTA />                    // Free tier upgrade prompt
      <ThemeToggle />
    </SidebarFooter>
  </Sidebar>
  <MainContent>
    <Header>
      <Breadcrumbs />
      <SearchCommand />                 // Cmd+K command palette
      <NotificationBell />
      <UserMenu />
    </Header>
    <PageContent>
      {children}
    </PageContent>
    <Footer />
  </MainContent>
</DashboardLayout>
```

### 2.2 Feature Component Trees

```
// Jobs Page
<JobsPage>
  <SearchHeader>
    <JobSearchInput />                  // Full-text search with debounce
    <FilterDrawer>
      <LocationFilter />
      <SalaryRangeFilter />
      <RemoteTypeFilter />
      <ExperienceLevelFilter />
      <EmploymentTypeFilter />
      <DatePostedFilter />
    </FilterDrawer>
    <SavedSearchButton />
  </SearchHeader>
  <JobList>
    <JobCard />                         // Repeated
    <JobCardSkeleton />                 // Loading state
    <EmptyState />                      // No results
    <LoadMoreButton />                  // Cursor-based pagination
  </JobList>
  <JobDetailPanel>                      // Slide-over panel
    <JobHeader />
    <JobDescription />
    <CompanyInsights />
    <MatchScore />
    <ActionButtons>
      <SaveJobButton />
      <ApplyNowButton />
      <AutoApplyButton />               // PRO feature
    </ActionButtons>
  </JobDetailPanel>
</JobsPage>

// Resume Editor
<ResumeEditorPage>
  <ResumeEditorLayout>
    <EditorSidebar>
      <SectionList />                   // Drag-and-drop reorder
      <AddSectionButton />
      <TemplateSelector />
    </EditorSidebar>
    <EditorCanvas>
      <ResumePreview />                 // Live preview
      <SectionEditor />                 // Inline editing
      <AIAssistPanel />                 // Suggestions drawer
    </EditorCanvas>
    <EditorToolbar>
      <UndoButton />
      <RedoButton />
      <AITailorButton />
      <ATSScoreButton />
      <ExportButton />
      <VersionHistory />
    </EditorToolbar>
  </ResumeEditorLayout>
  <ATSScoreModal />                     // Modal overlay
  <AITailorModal />
</ResumeEditorPage>

// Application Tracker (Kanban)
<ApplicationsPage>
  <PipelineViewToggle>
    <KanbanView />
    <ListView />
    <TableView />
  </PipelineViewToggle>
  <KanbanBoard>
    <KanbanColumn status="SAVED">
      <ApplicationCard /> ⋯
    </KanbanColumn>
    <KanbanColumn status="APPLIED">
      <ApplicationCard /> ⋯
    </KanbanColumn>
    <KanbanColumn status="PHONE_SCREEN">⋯</KanbanColumn>
    <KanbanColumn status="TECHNICAL_INTERVIEW">⋯</KanbanColumn>
    <KanbanColumn status="ONSITE_INTERVIEW">⋯</KanbanColumn>
    <KanbanColumn status="OFFER">⋯</KanbanColumn>
  </KanbanBoard>
</ApplicationsPage>

// Interview Session
<InterviewSessionPage>
  <SessionHeader>
    <Timer />
    <ProgressIndicator />
  </SessionHeader>
  <QuestionCard>
    <QuestionText />
    <AnswerTextArea />
    <RecordButton />                    // Voice mode
    <SubmitAnswerButton />
  </QuestionCard>
  <FeedbackPanel>                      // After each answer
    <ScoreBar />
    <StrengthsList />
    <ImprovementsList />
  </FeedbackPanel>
</InterviewSessionPage>
```

---

## 3. Folder Structure

```
src/
├── app/                                # Next.js App Router pages
│   ├── (auth)/                         # Auth route group
│   ├── (dashboard)/                    # Dashboard route group
│   ├── api/                            # BFF API routes
│   ├── layout.tsx                      # Root layout
│   ├── page.tsx                        # Landing page
│   ├── loading.tsx
│   ├── error.tsx
│   └── not-found.tsx
│
├── components/                         # React components
│   ├── ui/                             # shadcn/ui primitives (auto-generated)
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── dropdown-menu.tsx
│   │   ├── form.tsx
│   │   ├── input.tsx
│   │   ├── select.tsx
│   │   ├── table.tsx
│   │   ├── tabs.tsx
│   │   ├── toast.tsx
│   │   └── ... (all Radix primitives)
│   │
│   ├── shared/                         # Shared application components
│   │   ├── layout/
│   │   │   ├── sidebar.tsx
│   │   │   ├── header.tsx
│   │   │   ├── breadcrumbs.tsx
│   │   │   └── page-container.tsx
│   │   ├── data/
│   │   │   ├── data-table.tsx           # Generic sortable/filterable table
│   │   │   ├── pagination.tsx           # Cursor + offset pagination
│   │   │   ├── empty-state.tsx
│   │   │   ├── loading-skeleton.tsx
│   │   │   └── error-state.tsx
│   │   ├── feedback/
│   │   │   ├── toast.tsx
│   │   │   ├── alert.tsx
│   │   │   ├── confirm-dialog.tsx
│   │   │   └── notification-bell.tsx
│   │   ├── auth/
│   │   │   ├── protected-route.tsx
│   │   │   ├── role-guard.tsx
│   │   │   └── user-menu.tsx
│   │   └── common/
│   │       ├── logo.tsx
│   │       ├── theme-toggle.tsx
│   │       ├── search-command.tsx       # Cmd+K palette
│   │       ├── subscription-badge.tsx
│   │       └── upgrade-cta.tsx
│   │
│   └── features/                       # Feature-specific components
│       ├── auth/
│       │   ├── login-form.tsx
│       │   ├── register-form.tsx
│       │   ├── oauth-buttons.tsx
│       │   ├── forgot-password-form.tsx
│       │   └── reset-password-form.tsx
│       ├── jobs/
│       │   ├── job-search-input.tsx
│       │   ├── job-card.tsx
│       │   ├── job-detail-panel.tsx
│       │   ├── filter-drawer.tsx
│       │   ├── saved-search-dropdown.tsx
│       │   └── match-score-badge.tsx
│       ├── resumes/
│       │   ├── resume-list.tsx
│       │   ├── resume-editor-layout.tsx
│       │   ├── resume-preview.tsx
│       │   ├── section-editor.tsx
│       │   ├── section-list.tsx
│       │   ├── template-selector.tsx
│       │   ├── ai-assist-panel.tsx
│       │   ├── ats-score-modal.tsx
│       │   ├── ai-tailor-modal.tsx
│       │   ├── version-history.tsx
│       │   └── export-dropdown.tsx
│       ├── applications/
│       │   ├── kanban-board.tsx
│       │   ├── kanban-column.tsx
│       │   ├── application-card.tsx
│       │   ├── application-detail.tsx
│       │   ├── application-timeline.tsx
│       │   ├── status-badge.tsx
│       │   ├── notes-section.tsx
│       │   └── automation-progress.tsx
│       ├── interviews/
│       │   ├── session-list.tsx
│       │   ├── session-card.tsx
│       │   ├── question-card.tsx
│       │   ├── answer-input.tsx
│       │   ├── feedback-panel.tsx
│       │   ├── timer.tsx
│       │   └── voice-recorder.tsx
│       ├── career/
│       │   ├── path-visualization.tsx
│       │   ├── skills-gap-chart.tsx
│       │   ├── salary-benchmark-chart.tsx
│       │   └── recommendation-list.tsx
│       ├── networking/
│       │   ├── message-generator.tsx
│       │   ├── campaign-list.tsx
│       │   └── contact-card.tsx
│       ├── analytics/
│       │   ├── funnel-chart.tsx
│       │   ├── metric-card.tsx
│       │   ├── activity-chart.tsx
│       │   └── report-download.tsx
│       ├── company/
│       │   ├── company-profile-card.tsx
│       │   ├── tech-stack-badge.tsx
│       │   └── salary-insights.tsx
│       ├── notifications/
│       │   ├── notification-center.tsx
│       │   └── notification-item.tsx
│       ├── admin/
│       │   ├── user-table.tsx
│       │   ├── job-source-config.tsx
│       │   ├── feature-flag-list.tsx
│       │   └── system-health.tsx
│       ├── billing/
│       │   ├── subscription-card.tsx
│       │   ├── plan-comparison.tsx
│       │   ├── checkout-button.tsx
│       │   └── invoice-list.tsx
│       └── settings/
│           ├── profile-form.tsx
│           ├── preference-form.tsx
│           ├── notification-form.tsx
│           ├── privacy-form.tsx
│           ├── theme-selector.tsx
│           └── danger-zone.tsx          // Delete account
│
├── lib/                                 # Utility libraries
│   ├── api/
│   │   ├── client.ts                    # Axios/fetch wrapper with JWT refresh
│   │   ├── auth-client.ts              # Auth-specific API calls
│   │   ├── jobs-client.ts
│   │   ├── resumes-client.ts
│   │   ├── applications-client.ts
│   │   ├── interviews-client.ts
│   │   ├── analytics-client.ts
│   │   ├── admin-client.ts
│   │   └── billing-client.ts
│   ├── auth/
│   │   ├── AuthContext.tsx             # Auth state provider
│   │   ├── useAuth.ts                  # Auth hook
│   │   ├── useRoleAccess.ts           # RBAC hook
│   │   └── ProtectedRoute.tsx         # Route guard component
│   ├── hooks/                          # Custom React hooks
│   │   ├── useDebounce.ts
│   │   ├── useIntersectionObserver.ts
│   │   ├── useMediaQuery.ts
│   │   ├── useLocalStorage.ts
│   │   ├── useWebSocket.ts
│   │   └── useInfiniteScroll.ts
│   ├── stores/                         # Zustand client state
│   │   ├── auth-store.ts              # Auth state (user, tokens)
│   │   ├── ui-store.ts                # Sidebar, theme, preferences
│   │   ├── job-filter-store.ts        # Active job search filters
│   │   └── application-store.ts       # Currently selected application
│   ├── utils/
│   │   ├── cn.ts                       # clsx + tailwind-merge utility
│   │   ├── format.ts                   # Date, currency, number formatters
│   │   ├── validators.ts              # Zod schemas for shared types
│   │   ├── constants.ts
│   │   └── error-handler.ts           # Client-side error parsing
│   ├── validations/                    # Zod validation schemas
│   │   ├── auth-schema.ts
│   │   ├── resume-schema.ts
│   │   ├── application-schema.ts
│   │   └── settings-schema.ts
│   └── types/                          # TypeScript type definitions
│       ├── api.ts                      # API response envelope types
│       ├── user.ts
│       ├── job.ts
│       ├── resume.ts
│       ├── application.ts
│       ├── interview.ts
│       ├── analytics.ts
│       └── notification.ts
│
├── public/                              # Static assets
│   ├── images/
│   │   ├── logo.svg
│   │   ├── logo-icon.svg
│   │   └── og-image.png                # Open Graph image
│   ├── icons/                          # SVG icons (if not using Lucide)
│   └── fonts/                          # Self-hosted fonts (optional)
│
├── styles/                              # Global styles
│   ├── globals.css                      # Tailwind directives + base styles
│   └── tailwind.config.ts              # Custom theme, colors, spacing
│
├── middleware.ts                        # Next.js middleware (auth redirect)
├── next.config.ts                       # Next.js configuration
├── package.json
├── tsconfig.json
├── tailwind.config.ts
├── postcss.config.js
└── components.json                      # shadcn/ui configuration
```

---

## 4. State Management

### 4.1 State Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                      STATE ARCHITECTURE                             │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │              SERVER STATE (React Query)                    │      │
│  │                                                          │       │
│  │  • All API-fetched data                                  │       │
│  │  • Cache, dedup, background refetch, optimistic updates │       │
│  │  • Automatic garbage collection                          │       │
│  │  • Cache keys: ['user', userId], ['jobs', filters], ... │       │
│  │  • Stale time per resource type:                         │       │
│  │    - Jobs: 30s                                           │       │
│  │    - User profile: 5min                                  │       │
│  │    - Application status: 15s                             │       │
│  │    - AI operation: cache result for 24h (prompt hash)   │       │
│  └──────────────────────────────────────────────────────────┘       │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │              CLIENT STATE (Zustand)                       │       │
│  │                                                          │       │
│  │  • Auth state: user, tokens, role                        │       │
│  │  • UI state: sidebar open, theme, active filters        │       │
│  │  • Transient state: selected application, current filter │       │
│  │  • Not persisted to server                                │       │
│  └──────────────────────────────────────────────────────────┘       │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │              LOCAL STATE (useState/useReducer)            │      │
│  │                                                          │       │
│  │  • Form state (React Hook Form manages its own)         │       │
│  │  • Modal/drawer open/close                               │       │
│  │  • Accordion/collapse state                               │       │
│  │  • Drag-and-drop state (resume editor sections)          │       │
│  └──────────────────────────────────────────────────────────┘       │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │              URL STATE (useSearchParams)                  │       │
│  │                                                          │       │
│  │  • Job search filters (keywords, location, salary)      │       │
│  │  • Pagination cursors                                    │       │
│  │  • Active tab in settings                                │       │
│  │  • Shareable URLs (deep links)                           │       │
│  └──────────────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────────────┘
```

### 4.2 React Query Configuration

```typescript
// Query client defaults
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,          // 30s default
      gcTime: 5 * 60_000,         // 5 min garbage collection
      retry: 2,                    // Retry twice on failure
      refetchOnWindowFocus: false, // Don't refetch on focus (privacy)
      refetchOnReconnect: true,
    },
    mutations: {
      retry: 0,                    // Don't retry mutations
    },
  },
});

// Cache invalidation strategy:
// 1. After mutation: invalidate related queries
// 2. On WebSocket event: invalidate specific query (application status change)
// 3. Optimistic updates for: save job, status change, follow-up complete
```

### 4.3 Zustand Store Examples

```typescript
// Auth Store — persisted in localStorage (access token) + httpOnly cookie
interface AuthState {
  user: User | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  login: (tokens: AuthTokens, user: User) => void;
  logout: () => void;
  refreshToken: () => Promise<void>;
}

// UI Store — persisted in localStorage (theme, sidebar)
interface UIState {
  sidebarOpen: boolean;
  theme: 'light' | 'dark' | 'system';
  toggleSidebar: () => void;
  setTheme: (theme: 'light' | 'dark' | 'system') => void;
}
```

### 4.4 WebSocket Integration

```typescript
// useWebSocket hook
// Connects to: ws://host/ws/notifications/{userId}
// Events handled:
//   - notification.new → show toast, invalidate notifications query
//   - application.status_changed → invalidate application query
//   - automation.progress → update progress bar
//   - interview.question → receive next question (live mode)

// Reconnection: exponential backoff (1s, 2s, 4s, 8s, max 30s)
// Auth: JWT token in connection query param (ws://host/ws?token=xxx)
```

---

## 5. Routing & Navigation

### 5.1 Route Structure

```
/                                    Landing page (public)
/login                               Login (public)
/register                            Register (public)
/forgot-password                     Forgot password (public)
/reset-password?token=xxx            Reset password (public)

/dashboard                           Main dashboard (auth required)
/jobs                                Job search (auth required)
/jobs/saved                          Saved jobs (auth required)
/jobs/[id]                           Job detail (auth required)
/applications                        ATS pipeline (auth required)
/applications/[id]                   Application detail (auth required)
/resumes                             Resume list (auth required)
/resumes/[id]                        Resume editor (auth required)
/interviews                          Interview sessions (auth required)
/interviews/[id]                     Active interview (auth required)
/career                              Career analytics (auth required)
/career/path                         Career path planner (auth required)
/networking                          Networking hub (auth required)
/analytics                           Analytics dashboard (auth required)
/settings                            Settings (auth required)
/settings/profile                    Profile settings
/settings/preferences                Preferences
/settings/notifications              Notification settings
/settings/privacy                    Privacy settings
/settings/billing                    Billing & subscription
/settings/danger                     Danger zone (delete account)

/admin                               Admin dashboard (admin only)
/admin/users                         User management (admin only)
/admin/job-sources                   Job source config (admin only)
/admin/ai-providers                  AI provider config (admin only)
/admin/feature-flags                 Feature flags (admin only)
/admin/logs                          Audit logs (admin only)
/admin/system                        System health (admin only)
```

### 5.2 Middleware Auth Flow

```
Request → middleware.ts
  ├── Public route? → pass through
  ├── /api/webhooks/* → pass through (stripe signature check)
  ├── /admin/* → check admin role → pass or 403 redirect
  ├── /(dashboard)/* → check JWT → valid: pass | invalid: redirect /login
  └── /(auth)/* → already logged in? → redirect /dashboard
```

---

## 6. Theme & Design System

### 6.1 Design Tokens

```typescript
// Tailwind CSS custom theme
const theme = {
  colors: {
    primary: {
      50:  '#eff6ff',   // Blue
      100: '#dbeafe',
      200: '#bfdbfe',
      300: '#93c5fd',
      400: '#60a5fa',
      500: '#3b82f6',   // Primary
      600: '#2563eb',
      700: '#1d4ed8',
      800: '#1e40af',
      900: '#1e3a8a',
    },
    accent: {
      50:  '#f0fdf4',   // Emerald (growth, career)
      100: '#dcfce7',
      500: '#22c55e',   // Success, match score
      700: '#15803d',
    },
    surface: {
      light: '#ffffff',
      dark:  '#0f172a',  // Slate 900
    },
    muted: {
      light: '#f8fafc',
      dark:  '#1e293b',
    },
    border: {
      light: '#e2e8f0',
      dark:  '#334155',
    },
  },
  borderRadius: {
    DEFAULT: '0.5rem',   // 8px base
    lg: '0.75rem',
    xl: '1rem',
  },
  fontFamily: {
    sans: ['Inter', 'system-ui', 'sans-serif'],
    mono: ['JetBrains Mono', 'monospace'],
  },
};
```

### 6.2 Component Library (shadcn/ui)

```
Installed primitives (via components.json):
  - Button, Input, Label, Textarea
  - Card, Badge, Avatar
  - Dialog, Sheet (slide-over), Popover
  - DropdownMenu, ContextMenu
  - Select, Command (Cmd+K palette)
  - Table, Tabs, Toggle
  - Toast, Sonner (toasts)
  - Separator, Skeleton
  - Progress, Slider
  - Switch, Checkbox, RadioGroup
  - Tooltip, HoverCard
  - Form (React Hook Form adapter)
  - Calendar, DatePicker

Custom overrides:
  - All primitives extended with brand colors
  - Dark mode via class strategy (dark: variant)
  - Consistent spacing: 4px base unit (p-1 = 4px, p-2 = 8px, etc.)
```

### 6.3 Dark Mode

```
Strategy: class-based (next-themes <ThemeProvider>)
  - 'class' on HTML element
  - Tailwind dark: variant
  - Persisted in localStorage
  - System preference detection (respects OS setting)
  - Components: unstyled in light, dark mode via CSS variables
```

---

## 7. Data Fetching Strategy

### 7.1 React Query Hooks by Feature

```typescript
// Auth
useLogin()           → POST /auth/login
useRegister()        → POST /auth/register
useOAuthLogin()      → POST /auth/oauth/{provider}
useLogout()          → POST /auth/logout
useRefreshToken()    → POST /auth/refresh

// Jobs
useJobSearch(filters)    → GET /jobs (paginated, SSR + client refresh)
useJobDetail(id)         → GET /jobs/{id} (ISR, revalidate 300s)
useSavedJobs()           → GET /jobs/saved
useSaveJob()             → POST /jobs/{id}/save
useUnsaveJob()           → DELETE /jobs/{id}/save

// Resumes
useResumes()             → GET /resumes
useResume(id)            → GET /resumes/{id}
useCreateResume()        → POST /resumes
useUpdateResume(id)      → PUT /resumes/{id}
useDeleteResume(id)      → DELETE /resumes/{id}
useTailorResume(id)      → POST /resumes/{id}/tailor  (mutation — AI)
useScoreResume(id)       → POST /resumes/{id}/score   (mutation — AI)
useExportResume(id)      → POST /resumes/{id}/export

// Applications
useApplications(filters)  → GET /applications
useApplication(id)        → GET /applications/{id}
useCreateApplication()    → POST /applications
useUpdateStatus(id)       → PUT /applications/{id}/status
useAddNote(id)            → POST /applications/{id}/notes
useTriggerAutomation(id)  → POST /applications/{id}/automate
useAutomationProgress(id) → WebSocket /ws/automation/{sessionId}

// Interviews
useInterviewSessions()       → GET /interviews/sessions
useInterviewSession(id)      → GET /interviews/sessions/{id}
useCreateSession()           → POST /interviews/sessions
useSubmitAnswer(sessionId, questionId) → POST .../answer
useCompleteSession(id)       → POST .../complete

// Career
useCareerPath(current, target) → POST /career/path-analyze
useSkillGap(userId)             → GET /career/skills-gap
useSalaryBenchmarks(role, loc)  → GET /career/salary-benchmarks
useDashboard()                  → GET /analytics/dashboard
useFunnel()                     → GET /analytics/funnel
```

### 7.2 Optimistic Updates

```typescript
// Example: Save/unsave job — optimistic update
const queryClient = useQueryClient();

const saveMutation = useMutation({
  mutationFn: (jobId) => jobsClient.saveJob(jobId),
  onMutate: async (jobId) => {
    await queryClient.cancelQueries({ queryKey: ['jobs', filters] });
    const previous = queryClient.getQueryData(['jobs', filters]);
    queryClient.setQueryData(['jobs', filters], (old) => ({
      ...old,
      pages: old.pages.map(page => ({
        ...page,
        jobs: page.jobs.map(j =>
          j.id === jobId ? { ...j, isSaved: true } : j
        ),
      })),
    }));
    return { previous };
  },
  onError: (err, jobId, context) => {
    queryClient.setQueryData(['jobs', filters], context.previous);
    toast.error('Failed to save job');
  },
  onSettled: () => {
    queryClient.invalidateQueries({ queryKey: ['jobs', filters] });
  },
});
```

### 7.3 API Client Architecture

```typescript
// Axios instance with interceptors
const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 30_000,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: attach JWT
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response interceptor: handle 401 → refresh → retry
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !error.config._retry) {
      error.config._retry = true;
      await useAuthStore.getState().refreshToken();
      error.config.headers.Authorization = `Bearer ${useAuthStore.getState().accessToken}`;
      return apiClient(error.config);
    }
    return Promise.reject(error);
  }
);
```

---

## 8. Form Handling

### 8.1 Form Library Stack

```
React Hook Form   → Form state management (performance, minimal re-renders)
Zod               → Schema validation (type-safe, composable)
shadcn/ui Form    → Pre-built form components with error states

// Pattern:
const formSchema = z.object({
  email: z.string().email('Invalid email'),
  password: z.string().min(12, 'Min 12 characters')
    .regex(/[A-Z]/, 'Need uppercase')
    .regex(/[0-9]/, 'Need number'),
});
```

### 8.2 Key Forms

| Form | Schema Fields | Validation Rules |
|------|--------------|-----------------|
| Register | email, password, confirmPassword, fullName | Email format, password strength, name 2-100 chars |
| Login | email, password | Required fields |
| Profile | fullName, headline, phone, location | Phone E.164, headline max 200 |
| Resume Section | type, title, content (polymorphic) | Per-type validation |
| Application Note | content, category | Content min 1 char |
| Settings | theme, language, timezone | Enum validation |
| Interview Question | (AI generated, no form) | — |

---

## 9. Error Handling

### 9.1 Error Boundary Strategy

```
app/error.tsx               — Global error boundary (catches all unhandled)
app/jobs/error.tsx          — Job search specific errors
app/resumes/error.tsx       — Resume editor specific
app/interviews/error.tsx    — Interview session specific

Each error boundary:
  - Logs error to Sentry
  - Shows friendly error UI with retry button
  - Does NOT reset React Query cache (keeps stale data)
```

### 9.2 API Error Handling

```typescript
// Error response envelope expected from API:
interface ApiError {
  status: 'error';
  error: {
    code: string;       // e.g., "VALIDATION_ERROR", "TOKEN_EXPIRED"
    message: string;    // Human-readable
    details?: Array<{ field: string; message: string }>;
  };
  meta: { request_id: string; timestamp: string };
}

// Client-side error mapping:
// 400 → Show field-level validation errors on form
// 401 → Auto-refresh token (via interceptor), fallback redirect login
// 403 → Show "Access denied" toast
// 404 → Show "Not found" state with search suggestion
// 409 → Show conflict message (e.g., "Already applied")
// 429 → Show rate limit toast with Retry-After
// 5xx → Show "Something went wrong" with request ID
```

---

## 10. Performance Strategy

### 10.1 Bundle Optimization

```typescript
// next.config.ts
const nextConfig = {
  // Image optimization
  images: {
    formats: ['image/avif', 'image/webp'],
    remotePatterns: [
      { protocol: 'https', hostname: 'logo.clearbit.com' },
      { protocol: 'https', hostname: '**.linkedin.com' },
    ],
  },

  // Bundle analysis (CI only)
  // webpack: (config, { isServer }) => {
  //   if (!isServer) config.plugins.push(new BundleAnalyzerPlugin());
  //   return config;
  // },

  // Experimental features
  experimental: {
    optimizePackageImports: ['lucide-react', 'date-fns', '@radix-ui/*'],
    scrollRestoration: true,
  },

  // Compression
  compress: true,
};
```

### 10.2 Code Splitting

```typescript
// Dynamic imports for heavy components:
const ResumeEditor = dynamic(() => import('@/features/resumes/resume-editor-layout'), {
  loading: () => <ResumeEditorSkeleton />,
  ssr: false,       // No SSR for interactive editor
});

const InterviewVoiceRecorder = dynamic(
  () => import('@/features/interviews/voice-recorder'),
  { ssr: false }
);

const KanbanBoard = dynamic(
  () => import('@/features/applications/kanban-board'),
  { ssr: false }
);
```

### 10.3 Image Optimization

```typescript
// All images use Next.js <Image> component:
<Image
  src={company.logoUrl}
  alt={company.name}
  width={48}
  height={48}
  className="rounded-full"
  placeholder="blur"          // Blur-up loading
  loading="lazy"              // Lazy load below fold
/>

// Priority images (above fold):
<Image priority src="/logo.svg" alt="JobPilot AI" width={120} height={32} />
```

### 10.4 Caching Strategy (Client-Side)

| Resource | Cache Duration | Strategy |
|----------|---------------|----------|
| Static assets (CSS, JS) | 1 year | Content hash in filename |
| Images (logos, icons) | 30 days | CDN cache with revalidate |
| API responses (jobs) | 30s stale, 5min cache | React Query + Redis |
| API responses (profile) | 5min stale | React Query |
| AI responses | 24h (prompt hash) | Redis server-side |
| Fonts | 1 year | CDN + preload |

### 10.5 Web Vitals Targets

| Metric | Target |
|--------|--------|
| Largest Contentful Paint (LCP) | < 2.0s |
| First Input Delay (FID) | < 100ms |
| Cumulative Layout Shift (CLS) | < 0.1 |
| First Contentful Paint (FCP) | < 1.5s |
| Time to Interactive (TTI) | < 3.0s |
| Speed Index | < 3.0s |

---

*This Frontend Foundation defines the complete architecture for the Next.js application — from routing and component hierarchy to state management, theming, and performance optimization. Zero UI code — only structure and patterns.*

---

**End of Frontend Foundation v1.0**
