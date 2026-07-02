# JobPilot AI — Low Level Design (LLD)

**Version:** 1.0  
**Status:** Draft  
**Phase:** 2 of 35  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Package Structure & Naming Conventions
2. Domains & Subdomains Deep Dive
3. Module 1: Identity & Access
4. Module 2: User & Profile
5. Module 3: Resume Studio
6. Module 4: AI Provider Layer
7. Module 5: Prompt Engine
8. Module 6: ATS Resume Optimizer
9. Module 7: Cover Letter Engine
10. Module 8: Job Discovery
11. Module 9: Job Matching Engine
12. Module 10: Application Tracker
13. Module 11: Company Intelligence
14. Module 12: Interview Hub
15. Module 13: Career Analytics
16. Module 14: Browser Automation
17. Module 15: Notification Service
18. Module 16: Admin Portal
19. Module 17: Settings Module
20. Module 18: Search Engine
21. Cross-Cutting Concerns
22. Sequence Diagrams (Complete)
23. Class Relationship Diagrams
24. Dependency Rules Enforcement

---

## 1. Package Structure & Naming Conventions

### 1.1 Top-Level Maven Modules

```
jobpilot/
  ├── jobpilot-common/              # Shared primitives, utilities, constants
  ├── jobpilot-domain/              # Domain entities, value objects, domain events
  ├── jobpilot-application/         # Use cases, application services, ports (interfaces)
  ├── jobpilot-infrastructure/      # JPA, Redis, Kafka, AI clients, external API implementations
  ├── jobpilot-interfaces/          # REST controllers, WebSocket handlers, DTOs, mappers
  ├── jobpilot-automation/          # Browser automation service (Playwright Java)
  ├── jobpilot-ai-service/          # AI orchestration service (separate deployable)
  ├── jobpilot-gateway/             # Spring Cloud Gateway configuration
  └── jobpilot-bootstrap/           # Main Spring Boot application entry point
```

### 1.2 Package Naming Convention (Per Module)

```
com.jobpilot.modules.<module-name>.{layer}
```

Layers:
- `domain` — Entities, value objects, aggregates, domain events, domain services, repository ports, domain exceptions
- `application` — Use case interfaces, application services, DTOs, input/output ports, mappers
- `infrastructure` — JPA entities, repository implementations, external API clients, configuration
- `interfaces` — REST controllers, request/response DTOs, WebSocket handlers, exception handlers

### 1.3 Class Naming Conventions

| Type | Suffix | Example |
|------|--------|---------|
| Domain Entity | (none) | `User`, `Resume`, `JobListing` |
| Value Object | (none) | `Email`, `PhoneNumber`, `Money`, `ResumeScore` |
| Domain Service | `DomainService` | `ResumeScoringDomainService` |
| Aggregate Root | (none) | `Application` (aggregate of notes, timeline events) |
| Domain Event | `Event` (past tense) | `ApplicationSubmittedEvent` |
| Use Case Interface | `UseCase` | `SubmitApplicationUseCase` |
| Application Service | `Service` | `ApplicationService` |
| Inbound Port (Interface) | `UseCase` or `Command` | `CreateApplicationUseCase` |
| Outbound Port (Interface) | `Port` or `Repository` | `ResumeRepository`, `NotificationPort` |
| Repository Impl | `Impl` or `JpaRepository` | `ResumeRepositoryImpl` |
| REST Controller | `Controller` | `ResumeController` |
| Request DTO | `Request` (or `Command`) | `CreateResumeRequest` |
| Response DTO | `Response` | `ResumeResponse` |
| Mapper | `Mapper` | `ResumeMapper` |
| Exception | `Exception` | `ResumeNotFoundException` |
| Configuration | `Config` or `Properties` | `AiProviderConfig`, `RedisCacheConfig` |

### 1.4 Layer Dependency Rules (ArchUnit Enforced)

```
Presentation (interfaces)  ─────►  Application (application)
     │                                   │
     │                                   │
     ▼                                   ▼
Domain (domain)  ◄──────────────  Infrastructure (infrastructure)

Rules:
1. interfaces → application → domain  (interfaces depends on application, application depends on domain)
2. infrastructure → domain  (infrastructure implements domain ports)
3. interfaces → infrastructure  (wiring)
4. domain → NOTHING  (domain is pure — zero framework dependencies)
5. No cyclic dependencies between packages
6. domain layer may NOT import: spring, jakarta, javax, com.fasterxml, redis, kafka
7. infrastructure may NOT be imported by: interfaces, application, domain
```

---

## 2. Domains & Subdomains Deep Dive

### 2.1 Core Domain (Competitive Advantage)

```
Resume Studio
  ├── Resume entity (aggregate root)
  ├── ResumeSection value object
  ├── ResumeVersion value object
  ├── AtsScore value object
  ├── CoverLetter entity
  ├── ResumeDomainService
  ├── ResumeTailoringDomainService
  └── CoverLetterGenerationDomainService

Browser Automation
  ├── AutomationSession (aggregate root)
  ├── FormField value object
  ├── ApplicationResult value object
  ├── SessionState machine (state enum + transition rules)
  ├── AutomationOrchestrationDomainService
  └── FormDetectionDomainService

Job Matching
  ├── JobMatchScore value object
  ├── SkillGap value object
  ├── MatchingDomainService
  └── RecommendationDomainService
```

### 2.2 Supporting Subdomains

```
Identity & Access     — User registration, authentication, authorization
User & Profile        — Profile management, preferences
Job Discovery         — Job listing aggregation, search, filtering
Application Tracker   — Pipeline management, status tracking
Company Intelligence  — Company profiles, tech stack, salary data
Interview Hub         — Mock interviews, question bank, answer scoring
Career Analytics      — Metrics, charts, reports, funnel analysis
Notification          — Email, push, in-app, reminder scheduling
Admin                 — User management, system config, feature flags
Settings              — Theme, preferences, notification settings
Search Engine         — Full-text search, filters, pagination
```

### 2.3 Generic Subdomains

```
Payment / Subscription — Stripe integration, plan management, invoicing
Audit Logging          — All sensitive operations tracked
Caching                — Redis-based distributed cache
File Storage           — S3/MinIO for resumes, screenshots, exports
```

---

## 3. Module 1: Identity & Access

### 3.1 Domain Layer

**Entities:**
```
User (Aggregate Root)
  - id: UserId (value object)
  - email: Email (value object)
  - passwordHash: PasswordHash (value object)
  - role: Role (enum: FREE, PRO, ENTERPRISE, ADMIN)
  - oauthProviders: Set<OAuthProvider> (value object collection)
  - emailVerifiedAt: Instant
  - createdAt: Instant
  - updatedAt: Instant
  - deletedAt: Instant (nullable, soft delete)
  
  Methods:
  - verifyEmail()
  - updateRole(Role)
  - softDelete()
  - addOAuthProvider(OAuthProvider)
  - removeOAuthProvider(OAuthProvider)
```

**Value Objects:**
```
UserId (UUID wrapper)
Email (validated format, normalized lowercase)
PasswordHash (bcrypt hash)
OAuthProvider (provider: enum GOOGLE|LINKEDIN|GITHUB|MICROSOFT, providerUserId: String)
Role (enum)
```

**Domain Events:**
```
UserRegisteredEvent(userId, email, role, occurredAt)
UserVerifiedEvent(userId, occurredAt)
UserDeletedEvent(userId, occurredAt)
UserRoleChangedEvent(userId, oldRole, newRole, occurredAt)
```

**Domain Exceptions:**
```
EmailAlreadyExistsException
InvalidEmailException
WeakPasswordException
UserNotFoundException
EmailNotVerifiedException
```

### 3.2 Application Layer

**Inbound Ports (Use Cases):**
```
RegisterUserUseCase
  Input: RegisterUserCommand(email, password, fullName)
  Output: UserResponse
  Flow: Validate email uniqueness → Hash password → Create User → Publish UserRegisteredEvent → Return

AuthenticateUserUseCase
  Input: AuthenticateCommand(email, password)
  Output: AuthTokenResponse(accessToken, refreshToken)
  Flow: Find user → Verify password → Check email verified → Generate tokens → Return

OAuthLoginUseCase
  Input: OAuthLoginCommand(provider, code, redirectUri)
  Output: AuthTokenResponse
  Flow: Exchange code for token → Get user info from provider → Find or create user → Generate tokens → Return

RefreshTokenUseCase
  Input: RefreshTokenCommand(refreshToken)
  Output: AuthTokenResponse
  Flow: Validate refresh token in Redis → Rotate → Generate new pair → Return

VerifyEmailUseCase
  Input: VerifyEmailCommand(token)
  Output: void
  Flow: Decode token → Find user → Mark verified → Publish UserVerifiedEvent

ForgotPasswordUseCase
  Input: ForgotPasswordCommand(email)
  Output: void
  Flow: Find user → Generate reset token → Send email via NotificationPort

ResetPasswordUseCase
  Input: ResetPasswordCommand(token, newPassword)
  Output: void
  Flow: Validate token → Hash new password → Update user

LogoutUseCase
  Input: LogoutCommand(userId, refreshToken)
  Output: void
  Flow: Invalidate refresh token in Redis
```

**Outbound Ports:**
```
UserRepository (interface)
  - findByEmail(Email): Optional<User>
  - findById(UserId): Optional<User>
  - save(User): User
  - existsByEmail(Email): boolean

PasswordEncoder (interface) — wraps bcrypt
TokenProvider (interface)
  - generateAccessToken(User): String
  - generateRefreshToken(User): String
  - validateAccessToken(String): TokenClaims
  - validateRefreshToken(String, UserId): boolean
  - invalidateRefreshToken(String)

OAuthClientPort (interface)
  - exchangeCode(provider, code, redirectUri): OAuthUserInfo
  - getUserInfo(provider, accessToken): OAuthUserInfo

NotificationPort (interface)
  - sendEmail(Email, Template, Context)
```

**Application DTOs:**
```
RegisterUserCommand(email, password, fullName)
AuthenticateCommand(email, password)
OAuthLoginCommand(provider, code, redirectUri)
RefreshTokenCommand(refreshToken)
VerifyEmailCommand(token)
ForgotPasswordCommand(email)
ResetPasswordCommand(token, newPassword)
LogoutCommand(userId, refreshToken)

UserResponse(id, email, fullName, role, emailVerified, createdAt)
AuthTokenResponse(accessToken, refreshToken, expiresIn, tokenType)
TokenClaims(userId, role, email, issuedAt, expiresAt)
```

### 3.3 Infrastructure Layer

**JPA Entities:**
```
UserEntity
  - id: UUID
  - email: String (unique, indexed)
  - passwordHash: String
  - role: String (enum)
  - emailVerifiedAt: Instant
  - deletedAt: Instant
  - createdAt: Instant
  - updatedAt: Instant
  - oauthProviders: List<OAuthProviderEntity> (@ElementCollection)
```

**Repositories:**
```
UserJpaRepository (Spring Data JPA interface)
UserRepositoryImpl (implements UserRepository, delegates to JPA + mapper)
```

**Token Implementations:**
```
JwtTokenProvider (implements TokenProvider)
  - RS256 signing with KeyPair
  - Access token: 15 min expiry, claims: sub, role, email
  - Refresh token: opaque UUID, stored in Redis hashed
```

**OAuth Implementations:**
```
GoogleOAuthClient (implements OAuthClientPort)
LinkedInOAuthClient (implements OAuthClientPort)
GitHubOAuthClient (implements OAuthClientPort)
MicrosoftOAuthClient (implements OAuthClientPort)
```

### 3.4 Interfaces Layer

**REST Controllers:**
```
AuthController
  POST /api/v1/auth/register  → 200/400
  POST /api/v1/auth/login     → 200/401
  POST /api/v1/auth/refresh   → 200/401
  POST /api/v1/auth/logout    → 204
  POST /api/v1/auth/oauth/{provider} → 200
  POST /api/v1/auth/verify-email → 200
  POST /api/v1/auth/forgot-password → 200
  POST /api/v1/auth/reset-password → 200
```

**Request/Response DTOs:**
```
RegisterRequest(email, password, confirmPassword, fullName)
LoginRequest(email, password)
RefreshTokenRequest(refreshToken)
OAuthRequest(code, redirectUri)
ForgotPasswordRequest(email)
ResetPasswordRequest(token, newPassword)

AuthResponse(accessToken, refreshToken, expiresIn, tokenType, user: UserProfileResponse)
```

### 3.5 Module Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Identity & Access Module                      │
│                                                                      │
│  interfaces                                                          │
│  ┌───────────────────────────────────────────────┐                  │
│  │ AuthController                                │                  │
│  │   → RegisterRequest → RegisterUserUseCase     │                  │
│  │   → LoginRequest    → AuthenticateUserUseCase │                  │
│  │   → OAuthRequest    → OAuthLoginUseCase       │                  │
│  │   → RefreshTokenRequest → RefreshTokenUseCase │                  │
│  └───────────────────────┬───────────────────────┘                  │
│                          │                                          │
│  application             ▼                                          │
│  ┌───────────────────────────────────────────────┐                  │
│  │ AuthApplicationService (implements use cases) │                  │
│  │   depends on:                                  │                  │
│  │   • UserRepository (port)                     │                  │
│  │   • TokenProvider   (port)                     │                  │
│  │   • PasswordEncoder (port)                     │                  │
│  │   • OAuthClientPort (port)                     │                  │
│  │   • NotificationPort (port)                    │                  │
│  │   • EventPublisher  (port)                     │                  │
│  └───────────────────────┬───────────────────────┘                  │
│                          │                                          │
│  domain                  ▼                                          │
│  ┌───────────────────────────────────────────────┐                  │
│  │ User (entity)                                  │                  │
│  │ Email, PasswordHash, UserId, Role (VOs)        │                  │
│  │ UserRegisteredEvent, UserVerifiedEvent         │                  │
│  │ EmailAlreadyExistsException                    │                  │
│  └───────────────────────┬───────────────────────┘                  │
│                          │                                          │
│  infrastructure           ▼                                         │
│  ┌───────────────────────────────────────────────┐                  │
│  │ JwtTokenProvider      implements TokenProvider │                  │
│  │ UserRepositoryImpl    implements UserRepository│                  │
│  │ BCryptPasswordEncoder implements PasswordEncoder│                 │
│  │ GoogleOAuthClient     implements OAuthClient   │                  │
│  │ SendGridNotification  implements Notification  │                  │
│  │ RedisTokenStore       (refresh token storage)  │                  │
│  └───────────────────────────────────────────────┘                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 4. Module 2: User & Profile

### 4.1 Domain Layer

**Aggregate Root:**
```
UserProfile (Aggregate Root)
  - id: ProfileId
  - userId: UserId
  - fullName: FullName
  - headline: Headline
  - phone: PhoneNumber (encrypted)
  - location: Location
  - workAuthorization: WorkAuthorization (value object)
  - socialLinks: SocialLinks (value object)
  - skills: List<Skill>
  - experiences: List<Experience>
  - educations: List<Education>
  - preferences: UserPreferences (value object)
  - updatedAt: Instant

  Methods:
  - updateBasicInfo(FullName, Headline, PhoneNumber, Location)
  - addSkill(Skill)
  - removeSkill(Skill)
  - addExperience(Experience)
  - updateExperience(index, Experience)
  - removeExperience(index)
  - addEducation(Education)
  - updatePreferences(UserPreferences)
```

**Value Objects:**
```
ProfileId (UUID)
FullName (validated, max 100 chars)
Headline (max 200 chars)
PhoneNumber (E.164 format, encrypted at infrastructure level)
Location (city, state, country, remote_preference)
WorkAuthorization (citizenship, visa_type, requires_sponsorship)
SocialLinks (linkedin, github, portfolio, twitter, personal_website)
Skill (name: String, proficiency: enum BEGINNER|INTERMEDIATE|ADVANCED|EXPERT, yearsOfExperience: int)
Experience (title, company, location, startDate, endDate, current, description, technologies: String[])
Education (degree, institution, fieldOfStudy, startDate, endDate, gpa: optional)
UserPreferences (theme: LIGHT|DARK|SYSTEM, language: String, timezone: String, emailFrequency: enum INSTANT|DAILY|WEEKLY, aiProvider: enum OPENAI|ANTHROPIC|OLLAMA)
```

**Domain Events:**
```
ProfileUpdatedEvent(userId, profileId, occurredAt)
SkillAddedEvent(userId, skill, occurredAt)
ExperienceAddedEvent(userId, experience, occurredAt)
PreferencesChangedEvent(userId, preferences, occurredAt)
```

**Domain Exceptions:**
```
ProfileNotFoundException
SkillAlreadyExistsException
InvalidPhoneNumberException
```

### 4.2 Application Layer

**Use Cases:**
```
GetProfileUseCase → ProfileResponse
UpdateBasicInfoUseCase(UpdateBasicInfoCommand) → ProfileResponse
AddSkillUseCase(AddSkillCommand) → ProfileResponse
RemoveSkillUseCase(RemoveSkillCommand) → ProfileResponse
AddExperienceUseCase(AddExperienceCommand) → ProfileResponse
UpdateExperienceUseCase(UpdateExperienceCommand) → ProfileResponse
RemoveExperienceUseCase(RemoveExperienceCommand) → ProfileResponse
AddEducationUseCase(AddEducationCommand) → ProfileResponse
UpdatePreferencesUseCase(UpdatePreferencesCommand) → ProfileResponse
UploadProfilePhotoUseCase(UploadPhotoCommand) → ProfileResponse
```

**Ports:**
```
ProfileRepository
  - findByUserId(UserId): Optional<UserProfile>
  - save(UserProfile): UserProfile

FileStoragePort
  - upload(fileName, contentType, bytes): FileUrl
  - delete(url): void
```

### 4.3 Infrastructure + Interfaces

```
UserProfileController
  GET    /api/v1/users/me/profile          → ProfileResponse
  PUT    /api/v1/users/me/profile/basic    → ProfileResponse
  POST   /api/v1/users/me/profile/skills   → ProfileResponse
  DELETE /api/v1/users/me/profile/skills/{skill} → 204
  POST   /api/v1/users/me/profile/experience   → ProfileResponse
  PUT    /api/v1/users/me/profile/experience/{id} → ProfileResponse
  DELETE /api/v1/users/me/profile/experience/{id} → 204
  POST   /api/v1/users/me/profile/education → ProfileResponse
  PUT    /api/v1/users/me/profile/preferences → ProfileResponse
  POST   /api/v1/users/me/profile/photo    → ProfileResponse
```

---

## 5. Module 3: Resume Studio

### 5.1 Domain Layer

**Aggregate Root:**
```
Resume (Aggregate Root)
  - id: ResumeId
  - userId: UserId
  - title: ResumeTitle
  - templateId: TemplateId
  - sections: List<ResumeSection>
  - versions: List<ResumeVersion> (ordered, newest first)
  - currentVersion: ResumeVersion
  - atsScores: List<AtsScore> (past scores for tracking)
  - createdAt: Instant
  - updatedAt: Instant
  - deletedAt: Instant (nullable)

  Methods:
  - createFromProfile(UserProfile): Resume  (factory method)
  - addSection(ResumeSection)
  - updateSection(sectionId, ResumeSection)
  - removeSection(sectionId)
  - changeTemplate(TemplateId)
  - createNewVersion(label): ResumeVersion
  - restoreVersion(versionNumber): ResumeVersion
  - calculateAtsScore(JobDescription): AtsScore
  - tailorToJob(JobDescription, TailoringConfig): Resume (returns new version)
```

**Value Objects:**
```
ResumeId (UUID)
ResumeTitle (max 100 chars)
TemplateId (UUID)
ResumeSection
  - id: SectionId
  - type: SectionType (enum: SUMMARY, EXPERIENCE, EDUCATION, SKILLS, CERTIFICATIONS, PROJECTS, CUSTOM)
  - title: String
  - content: StructuredContent (JSONB — flexible schema per section type)
  - order: int

ResumeVersion
  - versionNumber: int
  - label: String
  - contentSnapshot: ResumeContent (snapshot of all sections at version)
  - createdAt: Instant
  - isActive: boolean

StructuredContent (polymorphic via discriminated union)
  - type: SectionType
  - items: List<ContentItem> (varies by type)

ContentItem (base)
  - text: String
  - bulletPoints: List<String>

ExperienceContent extends ContentItem
  - company: String
  - role: String
  - dateRange: DateRange
  - achievements: List<String>
  - technologies: List<String>

EducationContent extends ContentItem
  - institution: String
  - degree: String
  - fieldOfStudy: String
  - dateRange: DateRange
  - gpa: String (optional)

AtsScore
  - score: int (0-100)
  - keywordMatches: Map<String, int>
  - missingKeywords: List<String>
  - suggestions: List<String>
  - analyzedAt: Instant
  - jobDescriptionHash: String

ResumeContent (snapshot value object, stored as JSONB)
```

**Domain Events:**
```
ResumeCreatedEvent(resumeId, userId, occurredAt)
ResumeVersionCreatedEvent(resumeId, userId, versionNumber, occurredAt)
ResumeTailoredEvent(resumeId, userId, jobListingId, occurredAt)
ResumeDeletedEvent(resumeId, userId, occurredAt)
```

**Domain Services:**
```
ResumeScoringDomainService
  - calculateScore(Resume, JobDescription): AtsScore
  - analyzeKeywords(resumeText, jdText): KeywordAnalysis
  - generateSuggestions(score, missingKeywords): List<String>

ResumeTailoringDomainService
  - tailor(Resume, JobDescription, TailoringConfig): ResumeContent
  - optimizeBulletPoints(experience: List<Experience>, jd: JobDescription): List<String>
  - reorderSections(Resume, JobDescription): List<SectionId>
```

### 5.2 Application Layer

**Use Cases:**
```
CreateResumeUseCase(CreateResumeCommand) → ResumeResponse
GetResumeUseCase(resumeId) → ResumeResponse
ListResumesUseCase(userId) → List<ResumeResponse>
UpdateResumeUseCase(UpdateResumeCommand) → ResumeResponse
DeleteResumeUseCase(resumeId) → void
AddSectionUseCase(AddSectionCommand) → ResumeResponse
UpdateSectionUseCase(UpdateSectionCommand) → ResumeResponse
RemoveSectionUseCase(RemoveSectionCommand) → ResumeResponse
ChangeTemplateUseCase(ChangeTemplateCommand) → ResumeResponse
CreateVersionUseCase(CreateVersionCommand) → ResumeResponse
RestoreVersionUseCase(RestoreVersionCommand) → ResumeResponse
TailorResumeUseCase(TailorResumeCommand) → ResumeResponse
ScoreResumeUseCase(ScoreResumeCommand) → AtsScoreResponse
ExportResumeUseCase(ExportResumeCommand) → FileDownloadResponse
UploadResumeUseCase(UploadResumeCommand) → ResumeResponse (parse uploaded resume)
ParseResumeUseCase(ParseResumeCommand) → ParsedResumeResponse (extract structured data)
```

**Ports:**
```
ResumeRepository
  - findById(ResumeId): Optional<Resume>
  - findByUserId(UserId): List<Resume>
  - save(Resume): Resume
  - delete(ResumeId): void
  - findActiveVersion(ResumeId): Optional<ResumeVersion>

AiResumePort (to AI module)
  - tailorResume(ResumeContent, JobDescription): TailoredContent
  - generateBulletPoints(experience, jd): List<String>
  - scoreResume(content, jdDescription): AtsScore

ExportPort
  - exportToPdf(ResumeContent, TemplateId): byte[]
  - exportToDocx(ResumeContent, TemplateId): byte[]
  - exportToText(ResumeContent): String

FileStoragePort
  - upload(fileName, contentType, bytes): FileUrl
  - getDownloadUrl(FileUrl): URL

ResumeParserPort
  - parse(byte[], originalFileName): ParsedResumeData
```

### 5.3 Infrastructure + Interfaces

```
ResumeController
  POST   /api/v1/resumes                     → 201 ResumeResponse
  GET    /api/v1/resumes                     → List<ResumeResponse>
  GET    /api/v1/resumes/{id}                → ResumeResponse
  PUT    /api/v1/resumes/{id}                → ResumeResponse
  DELETE /api/v1/resumes/{id}                → 204
  POST   /api/v1/resumes/{id}/sections       → ResumeResponse
  PUT    /api/v1/resumes/{id}/sections/{sectionId} → ResumeResponse
  DELETE /api/v1/resumes/{id}/sections/{sectionId} → ResumeResponse
  PUT    /api/v1/resumes/{id}/template       → ResumeResponse
  POST   /api/v1/resumes/{id}/versions       → ResumeResponse
  POST   /api/v1/resumes/{id}/restore/{version} → ResumeResponse
  POST   /api/v1/resumes/{id}/tailor         → ResumeResponse   (body: jobId)
  POST   /api/v1/resumes/{id}/score          → AtsScoreResponse (body: jobId or jdText)
  POST   /api/v1/resumes/{id}/export         → FileDownloadResponse
  POST   /api/v1/resumes/upload              → ResumeResponse   (multipart file)
  POST   /api/v1/resumes/parse               → ParsedResumeResponse
```

---

## 6. Module 4: AI Provider Layer

### 6.1 Domain Layer (Ports Only — pure interfaces)

```
AIProviderPort (interface)
  - generateText(AiRequest): AiResponse
  - generateStream(AiRequest): Flux<AiChunk> (reactive)
  - generateEmbedding(String): List<Float>
  - countTokens(String): int

AiRequest (value object)
  - model: String (e.g. "gpt-4", "claude-3-opus")
  - messages: List<AiMessage> (system, user, assistant, tool)
  - temperature: double (0.0–2.0)
  - maxTokens: int
  - stopSequences: List<String> (optional)
  - responseFormat: ResponseFormat (json_object | text)
  - tools: List<AiTool> (optional)

AiMessage (value object)
  - role: AiMessageRole (SYSTEM | USER | ASSISTANT | TOOL)
  - content: String
  - name: String (optional, for tool calls)

AiResponse (value object)
  - content: String
  - finishReason: FinishReason (STOP | LENGTH | CONTENT_FILTER | TOOL_CALLS)
  - usage: TokenUsage (promptTokens, completionTokens, totalTokens)
  - modelUsed: String
  - latencyMs: long

AiChunk (value object)
  - content: String (partial)
  - finishReason: FinishReason (null until last chunk)

AiModel (enum-ish)
  - id: String
  - provider: AiProviderType (OPENAI | ANTHROPIC | OLLAMA | GEMINI)
  - contextWindow: int
  - supportsStreaming: boolean
  - supportsTools: boolean
  - costPer1kInputTokens: Money
  - costPer1kOutputTokens: Money

AiProviderType (enum: OPENAI, ANTHROPIC, OLLAMA, GEMINI)

TokenUsage (value object)
  - promptTokens: int
  - completionTokens: int
  - totalTokens: int

AiTool (value object)
  - name: String
  - description: String
  - parameters: JsonSchema (tool-specific schema)

ResponseFormat (enum: JSON_OBJECT, TEXT)
FinishReason (enum: STOP, LENGTH, CONTENT_FILTER, TOOL_CALLS, ERROR)
```

### 6.2 Application Layer

```
AiOrchestrationService (application service)
  - Implements core AI orchestration logic:
    - Provider selection (based on model, availability, cost)
    - Retry with fallback provider
    - Token usage tracking
    - Response caching (configurable per use case)
    - Rate limit awareness
    - Circuit breaker per provider

  Methods: (delegates to AI provider ports)
  - generateText(request, useCase): AiResponse
  - generateStream(request, useCase): Flux<AiChunk>
  - generateEmbedding(text): List<Float>

ProviderSelector (strategy)
  - selectProvider(useCase, requestedModel, requirements): AiProviderType
  - getFallback(primaryProvider): AiProviderType

AiCacheService (cross-cutting)
  - buildCacheKey(useCase, request): String
  - getCached(key): Optional<AiResponse>
  - cache(key, response, ttl): void

TokenTracker
  - trackUsage(userId, useCase, tokens): void
  - getUsageThisMonth(userId): Map<String, Long>
  - checkLimit(userId, useCase): boolean
```

**Ports:**
```
AiModelRepository (port)
  - getAvailableModels(): List<AiModel>
  - isAvailable(provider): boolean
  - markUnavailable(provider): void (circuit breaker)

TokenUsageRepository (port)
  - recordUsage(userId, useCase, tokenUsage): void
  - getMonthlyUsage(userId): TokenUsageAggregate

ProviderHealthPort (port)
  - checkHealth(provider): ProviderHealth
  - recordFailure(provider): void
  - recordSuccess(provider): void
```

### 6.3 Infrastructure Layer

```
OpenAiAdapter (implements AIProviderPort)
  - Uses Spring AI OpenAiChatClient  (or direct HttpClient)
  - Configuration: apiKey, baseUrl, organizationId, timeout
  - Implements: generateText, generateStream, generateEmbedding, countTokens

AnthropicAdapter (implements AIProviderPort)
  - Uses Spring AI AnthropicChatClient
  - Configuration: apiKey, baseUrl, version, timeout
  - Implements: generateText, generateStream, countTokens

OllamaAdapter (implements AIProviderPort)
  - Uses Spring AI OllamaChatClient
  - Configuration: baseUrl, modelName (default: llama3)
  - Implements: generateText, generateEmbedding, countTokens

GeminiAdapter (implements AIProviderPort)
  - Uses Google AI client SDK
  - Configuration: apiKey, projectId, location
  - Implements: generateText, generateStream, countTokens

CircuitBreakerAspect (AOP around AIProviderPort calls)
  - Wraps each provider call with circuit breaker pattern
  - States: CLOSED (normal), OPEN (failing), HALF_OPEN (testing recovery)
  - Fallback: switch to alternative provider

ProviderHealthCheckScheduler
  - Cron job: every 30 seconds, ping each provider's health endpoint
  - Updates ProviderHealthStore in Redis
```

### 6.4 Module Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        AI Provider Layer                             │
│                                                                      │
│  application                                                        │
│  ┌───────────────────────────────────────────────┐                  │
│  │ AiOrchestrationService                        │                  │
│  │  • ProviderSelector     (strategy — selects   │                  │
│  │                           best provider)      │                  │
│  │  • AiCacheService       (caching layer)       │                  │
│  │  • TokenTracker         (usage + limits)      │                  │
│  │                                               │                  │
│  │  Uses: AIProviderPort (interface)             │                  │
│  └──────┬────────────────────────────────────────┘                  │
│         │                                                           │
│  domain ▼                                                           │
│  ┌───────────────────────────────────────────────┐                  │
│  │ <<interface>> AIProviderPort                  │                  │
│  │  + generateText(AiRequest): AiResponse        │                  │
│  │  + generateStream(AiRequest): Flux<AiChunk>   │                  │
│  │  + generateEmbedding(String): List<Float>     │                  │
│  │                                               │                  │
│  │ Value Objects:                                 │                  │
│  │  AiRequest, AiResponse, AiChunk, AiMessage    │                  │
│  │  TokenUsage, AiModel, AiProviderType          │                  │
│  └───────────────────────────────────────────────┘                  │
│         │                                                           │
│  infra   ▼                                                           │
│  ┌───────────────────────────────────────────────┐                  │
│  │ OpenAiAdapter    ─── implements ─── AIProviderPort              │
│  │ AnthropicAdapter ─── implements ─── AIProviderPort              │
│  │ OllamaAdapter    ─── implements ─── AIProviderPort              │
│  │ GeminiAdapter    ─── implements ─── AIProviderPort              │
│  │                                                               │
│  │ CircuitBreakerAspect  (AOP wrapper)                           │
│  │ ProviderHealthCheckScheduler (cron)                           │
│  └───────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
```

---

## 7. Module 5: Prompt Engine

### 7.1 Domain Layer

```
PromptTemplate (aggregate root)
  - id: TemplateId
  - useCase: PromptUseCase (enum)
  - name: String
  - version: int
  - systemPrompt: String    (with {{variable}} placeholders)
  - userPromptTemplate: String  (with {{variable}} placeholders)
  - variables: List<PromptVariable> (metadata about expected variables)
  - model: String (preferred model)
  - temperature: double
  - maxTokens: int
  - isActive: boolean
  - createdAt: Instant
  - updatedAt: Instant

PromptVariable
  - name: String
  - type: VariableType (STRING | JSON | LIST | NUMBER)
  - required: boolean
  - description: String
  - defaultValue: String (optional)

PromptUseCase (enum)
  RESUME_TAILORING,
  RESUME_SCORING,
  COVER_LETTER_GENERATION,
  INTERVIEW_QUESTION_PREDICTION,
  INTERVIEW_ANSWER_SCORING,
  CAREER_PATH_SUGGESTION,
  SKILLS_GAP_ANALYSIS,
  NETWORKING_MESSAGE,
  JOB_MATCHING_EXPLANATION
```

### 7.2 Application Layer

```
PromptEngineService
  - getTemplate(useCase, version?): PromptTemplate
  - resolvePrompt(templateId, context: Map<String, Object>): ResolvedPrompt
  - createTemplate(CreateTemplateCommand): PromptTemplate
  - updateTemplate(UpdateTemplateCommand): PromptTemplate
  - activateVersion(templateId, version): void
  - listTemplates(useCase): List<PromptTemplate>

ResolvedPrompt (value object)
  - systemPrompt: String (fully resolved)
  - userPrompt: String (fully resolved)
  - variables: Map<String, Object> (for logging/audit)
  - model: String
  - temperature: double
  - maxTokens: int

ContextBuilder
  - buildTailoringContext(userProfile, resumeContent, jobDescription): Map<String, Object>
  - buildScoringContext(resumeContent, jobDescription): Map<String, Object>
  - buildCoverLetterContext(userProfile, jobDescription, companyInfo): Map<String, Object>
  - buildInterviewContext(role, company, userProfile): Map<String, Object>
  - buildCareerPathContext(currentRole, targetRole, skillProfile): Map<String, Object>
```

### 7.3 Variable Resolution Engine

```
Resolution algorithm:
1. Load template (systemPrompt + userPrompt)
2. Extract all {{variable}} patterns from template strings
3. For each variable:
   a. Look up in provided context map
   b. If missing and required → throw MissingPromptVariableException
   c. If missing and not required → use default or replace with ""
   d. If present → apply type formatting (JSON.stringify for JSON type)
4. Apply escaping (remove control characters, limit length per variable)
5. Build systemMessage + userMessage
6. Wrap into AiRequest with template's model/temperature/maxTokens
7. Return ResolvedPrompt

Variable helpers:
{{user_profile}}  → JSON serialized UserProfile object
{{resume_content}} → JSON serialized ResumeContent
{{job_description}} → Full job description text
{{company_info}}   → JSON serialized CompanyProfile
{{skills_list}}    → Comma-separated skill names
{{experience_years}} → Computed total years
{{target_role}}    → Role title string
{{current_role}}   → Current role title
{{job_requirements}} → Structured requirements from job listing
```

### 7.4 Infrastructure

```
PromptTemplateRepository
  - findByUseCaseAndVersion(useCase, version): Optional<PromptTemplate>
  - findActiveByUseCase(useCase): Optional<PromptTemplate>
  - save(PromptTemplate): PromptTemplate
  - listVersions(useCase): List<PromptTemplate>

PromptTemplateJpaRepository (Spring Data JPA)
PromptTemplateRepositoryImpl (implements PromptTemplateRepository)

PromptSeeder (CommandLineRunner)
  - On startup, seeds database with default prompt templates from classpath:/prompts/*.md
```

---

## 8. Module 6: ATS Resume Optimizer

### 8.1 Domain Layer

```
AtsAnalysis (value object)
  - resumeId: ResumeId
  - jobDescriptionHash: String
  - overallScore: int (0-100)
  - sectionScores: Map<SectionType, int>
  - keywordMatches: Map<String, int> (keyword → frequency)
  - missingKeywords: List<String>
  - weakKeywords: List<String> (present but rare/buried)
  - keywordDensity: double
  - formatScore: int (parsability, structure, font choice)
  - suggestions: List<AtsSuggestion>
  - formattingIssues: List<FormattingIssue>
  - analyzedAt: Instant

AtsSuggestion (value object)
  - category: SuggestionCategory (KEYWORD | FORMAT | CONTENT | STRUCTURE)
  - severity: SuggestionSeverity (CRITICAL | MAJOR | MINOR)
  - message: String
  - section: SectionType (where to apply)
  - actionableItems: List<String>

SuggestionCategory (enum): KEYWORD, FORMAT, CONTENT, STRUCTURE
SuggestionSeverity (enum): CRITICAL, MAJOR, MINOR
FormattingIssue (value object): type, description, location
```

### 8.2 Application Layer

```
AtsOptimizerService
  - analyze(resumeId, jobDescription): AtsAnalysis
  - generateSuggestions(analysis): List<AtsSuggestion>
  - autoOptimize(resumeId, jobDescription): ResumeContent (AI-powered rewrite)

  Flow:
  1. Parse resume content → extract text
  2. Parse job description → extract key requirements, skills, qualifications
  3. Tokenize and normalize both texts (stemming, stop-word removal)
  4. Calculate keyword overlap (density + frequency)
  5. Identify missing critical keywords
  6. Score formatting (section headers, reverse-chronological, bullet points)
  7. Combine heuristic score + AI score (weighted: 60% heuristics, 40% AI)
  8. Generate actionable suggestions
  9. Return AtsAnalysis
```

### 8.3 Heuristic Scoring Rules

| Factor | Weight | Scoring |
|--------|--------|---------|
| Keyword Match Rate | 25% | matched_required_keywords / total_required_keywords × 100 |
| Keyword Density | 10% | Based on natural frequency (penalize stuffing) |
| Section Headers | 10% | Standard headers (Experience, Education etc.) = 100% |
| Reverse Chronological | 10% | Most recent first in experience = 100% |
| Bullet Points | 10% | Experience uses bullet lists = 100% |
| Quantified Achievements | 15% | Contains numbers/percentages = higher score |
| File Format | 5% | .docx = 100%, .pdf = 80%, .png = 20% |
| Contact Information | 5% | Email + phone + LinkedIn present |
| Length | 5% | 1 page = 100%, 2 pages = 70%, 3+ = 40% |
| Custom Section | 5% | No custom sections = 100% (ATS may miss them) |

### 8.4 Ports

```
AtsAnalysisRepository (port)
  - save(AtsAnalysis): AtsAnalysis
  - findByResumeIdAndJobHash(resumeId, jobHash): Optional<AtsAnalysis>
  - findByResumeId(resumeId): List<AtsAnalysis>
```

---

## 9. Module 7: Cover Letter Engine

### 9.1 Domain Layer

```
CoverLetter (aggregate root)
  - id: CoverLetterId
  - userId: UserId
  - resumeId: ResumeId (optional)
  - jobListingId: JobListingId (optional)
  - title: String
  - recipientName: String (optional)
  - recipientTitle: String (optional)
  - companyName: String
  - body: StructuredCoverLetter
  - tone: CoverLetterTone
  - wordCount: int
  - version: int
  - createdAt: Instant
  - updatedAt: Instant

StructuredCoverLetter (value object, stored as JSONB)
  - salutation: String
  - openingParagraph: String (hook — why this role/company)
  - bodyParagraphs: List<String> (2-3 paragraphs: experience match, skills, motivation)
  - closingParagraph: String (call to action)
  - signature: String

CoverLetterTone (enum): PROFESSIONAL, ENTHUSIASTIC, CONFIDENT, FORMAL, WARM
```

### 9.2 Application Layer

```
GenerateCoverLetterUseCase
  Input: GenerateCoverLetterCommand(userId, jobListingId, resumeId, tone, recipientName?, recipientTitle?)
  Output: CoverLetterResponse
  Flow:
  1. Load user profile + selected resume + job listing
  2. Load company info (from Company Intel module)
  3. Build prompt context via ContextBuilder
  4. Resolve prompt via PromptEngine (useCase: COVER_LETTER_GENERATION)
  5. Call AI provider via AiOrchestrationService
  6. Parse AI response into StructuredCoverLetter
  7. Save CoverLetter
  8. Return response

SaveCoverLetterUseCase
RegenerateParagraphUseCase (regenerate specific paragraph)
ListCoverLettersUseCase
DeleteCoverLetterUseCase
ExportCoverLetterUseCase (PDF/DOCX)
```

### 9.3 Ports

```
CoverLetterRepository
AiCoverLetterPort (to AI module)
ExportPort (shared with Resume module)
```

---

## 10. Module 8: Job Discovery

### 10.1 Domain Layer

```
JobListing (aggregate root)
  - id: JobListingId
  - source: JobSource (value object: name, sourceId, sourceUrl)
  - title: String
  - company: CompanyRef (value object: name, logoUrl, companyId)
  - location: JobLocation (city, state, country, remoteType)
  - salary: SalaryRange (min, max, currency, period)
  - description: HtmlContent (stripped for search)
  - requirements: List<String>
  - responsibilities: List<String>
  - benefits: List<String>
  - employmentType: EmploymentType (FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP)
  - experienceLevel: ExperienceLevel (ENTRY, MID, SENIOR, LEAD, EXECUTIVE)
  - industry: String
  - skills: List<String> (extracted from description)
  - applicationUrl: String
  - applicationDeadline: Instant (optional)
  - postedAt: Instant
  - scrapedAt: Instant
  - embeddings: List<Float> (pgvector)
  - isActive: boolean
  - createdAt: Instant

  Methods:
  - deactivate()
  - updateDetails(updatedFields)
  - generateEmbedding(): List<Float> (via AI port)

SavedSearch (entity)
  - id: SavedSearchId
  - userId: UserId
  - name: String
  - query: SearchQuery (criteria snapshot)
  - notificationsEnabled: boolean
  - lastNotifiedAt: Instant (for digest)
  - createdAt: Instant

SearchQuery (value object)
  - keywords: String
  - location: String
  - remoteType: RemoteType
  - salaryMin: BigDecimal
  - salaryMax: BigDecimal
  - employmentTypes: List<EmploymentType>
  - experienceLevels: List<ExperienceLevel>
  - industries: List<String>
  - postedWithin: Duration
  - sortBy: SortField (RELEVANCE | DATE | SALARY)
  - page: int
  - size: int
```

**Value Objects:**
```
JobSource (name, sourceId, sourceUrl)
JobLocation (city, state, country, remoteType: ONSITE|REMOTE|HYBRID)
SalaryRange (min, max, currency, period: YEARLY|MONTHLY|HOURLY)
CompanyRef (name, logoUrl, companyId — may link to Company Intelligence)
EmploymentType (enum)
ExperienceLevel (enum)
RemoteType (enum)
SortField (enum)
```

**Domain Events:**
```
NewJobListingsFoundEvent(source, count, occurredAt)
JobListingExpiredEvent(listingId, occurredAt)
SavedSearchMatchedEvent(searchId, userId, listingIds, occurredAt)
```

### 10.2 Application Layer

**Use Cases:**
```
SearchJobsUseCase(SearchQuery) → PaginatedResult<JobListingResponse>
GetJobDetailsUseCase(jobId) → JobDetailsResponse
SaveJobUseCase(userId, jobId) → void
UnsaveJobUseCase(userId, jobId) → void
GetSavedJobsUseCase(userId, page, size) → PaginatedResult<JobListingResponse>
CreateSavedSearchUseCase(CreateSavedSearchCommand) → SavedSearchResponse
UpdateSavedSearchUseCase(UpdateSavedSearchCommand) → SavedSearchResponse
DeleteSavedSearchUseCase(searchId) → void
ListSavedSearchesUseCase(userId) → List<SavedSearchResponse>
GetJobSourcesUseCase → List<JobSourceResponse>
```

**Ports:**
```
JobListingRepository
  - search(SearchQuery): Page<JobListing>
  - findById(JobListingId): Optional<JobListing>
  - findByIds(List<JobListingId>): List<JobListing>
  - findActiveBySourceAndSourceId(source, sourceId): Optional<JobListing>
  - save(JobListing): JobListing
  - saveAll(List<JobListing>): List<JobListing>
  - deactivateExpired(olderThan): int
  - searchByEmbedding(vector, limit): List<JobListing>
  - countBySource(source): int

SavedSearchRepository
  - findById(searchId): Optional<SavedSearch>
  - findByUserId(userId): List<SavedSearch>
  - save(SavedSearch): SavedSearch
  - delete(searchId): void

SavedJobRepository
  - findByUserIdAndJobId(userId, jobId): Optional<SavedJob>
  - findByUserId(userId): Page<JobListing> (join)
  - save(SavedJob): SavedJob
  - delete(userId, jobId): void

JobSourceAdapter (interface — Strategy pattern for scraping)
  - fetchNewJobs(since: Instant): List<RawJobData>
  - fetchJobDetails(sourceId): RawJobData
  - sourceName(): String
  - isAvailable(): boolean
```

### 10.3 Infrastructure

```
JobListingJpaRepository
  - Custom @Query for full-text search (PostgreSQL tsvector)
  - Custom @Query for pgvector similarity search

IndeedAdapter implements JobSourceAdapter
LinkedInAdapter implements JobSourceAdapter
GoogleJobsAdapter implements JobSourceAdapter
GlassdoorAdapter implements JobSourceAdapter
CompanyCareerPageAdapter implements JobSourceAdapter

JobAggregationScheduler
  - @Scheduled(cron = "0 */30 * * * *") — every 30 minutes
  - For each active adapter:
    - fetchNewJobs
    - deduplicate
    - generate embeddings (batch)
    - save
    - check saved searches → publish notifications
```

---

## 11. Module 9: Job Matching Engine

### 11.1 Domain Layer

```
JobMatch (value object)
  - jobListingId: JobListingId
  - overallScore: double (0.0 – 100.0)
  - categoryScores: Map<MatchCategory, double>
  - skillMatch: SkillMatchAnalysis
  - experienceFit: ExperienceFit
  - salaryFit: SalaryFit
  - locationFit: LocationFit
  - explanation: String (AI-generated)
  - matchedAt: Instant

SkillMatchAnalysis (value object)
  - matchedSkills: List<String>
  - missingSkills: List<String>
  - additionalSkills: List<String> (user has but not required)
  - matchRate: double (matched / total_required × 100)

ExperienceFit (value object)
  - requiredYears: int
  - userYears: int
  - fitRating: FitRating (OVERQUALIFIED | GOOD_FIT | UNDERQUALIFIED | UNCLEAR)

SalaryFit (value object)
  - expectedRange: SalaryRange
  - listedRange: SalaryRange
  - overlapPercentage: double
  - fitRating: FitRating

LocationFit (value object)
  - userLocation: String
  - jobLocation: JobLocation
  - remotePossible: boolean
  - fitRating: FitRating

MatchCategory (enum): SKILLS, EXPERIENCE, SALARY, LOCATION, EDUCATION, SENIORITY
FitRating (enum): POOR, FAIR, GOOD, EXCELLENT, OVERQUALIFIED, UNDERQUALIFIED
```

### 11.2 Application Layer

```
MatchJobsUseCase(userId, searchQuery?) → List<JobMatch> (ranked)
  Flow:
  1. Load user profile (skills, experience, preferences)
  2. Get candidate jobs (from search or recent listings)
  3. For each job:
     a. Calculate skill match (direct overlap)
     b. Calculate experience fit (years comparison)
     c. Calculate salary fit (overlap)
     d. Calculate location fit (same city? remote?)
     e. Weighted aggregation → overall score
     f. Generate AI explanation (optional, cached)
  4. Sort by overall score descending
  5. Return top N

GetMatchExplanationUseCase(userId, jobId) → String
  - Calls AI to generate natural-language explanation of match score

SemanticSearchUseCase(query, limit) → List<JobListing>
  - Convert query to embedding
  - Search by cosine similarity via pgvector
```

### 11.3 Matching Algorithm

```
// Heuristic matching (pre-AI, fast path)
overallScore =
    skillWeight  × skillMatch.matchRate +
    expWeight    × experienceFit.score +
    salaryWeight × salaryFit.score +
    locWeight    × locationFit.score

Default weights: skill=0.40, exp=0.30, salary=0.15, loc=0.15

// AI-enhanced matching (for Pro+ users)
AI overrides heuristic for:
  - explanation text
  - skills gap analysis
  - nuanced fit assessment (e.g., "React experience compensates for not having Next.js")
```

---

## 12. Module 10: Application Tracker

### 12.1 Domain Layer

```
Application (aggregate root)
  - id: ApplicationId
  - userId: UserId
  - jobListingId: JobListingId
  - resumeId: ResumeId (used for this application)
  - coverLetterId: CoverLetterId (optional)
  - status: ApplicationStatus (state machine)
  - statusHistory: List<StatusChange> (ordered)
  - automationInfo: AutomationInfo (optional)
  - notes: List<ApplicationNote>
  - attachments: List<ApplicationAttachment>
  - events: List<TimelineEvent>
  - followUp: FollowUp (optional)
  - salaryOffered: SalaryRange (optional)
  - appliedAt: Instant
  - updatedAt: Instant
  - createdAt: Instant

ApplicationStatus (enum — ordered state machine)
  SAVED → APPLIED → PHONE_SCREEN → TECHNICAL_INTERVIEW → ONSITE_INTERVIEW → OFFER → ACCEPTED
                                                                    ↘ REJECTED (any state)
                                                                    ↘ WITHDRAWN (any state)

StatusChange (value object)
  - from: ApplicationStatus
  - to: ApplicationStatus
  - changedBy: String (USER | SYSTEM | AUTOMATION)
  - note: String (optional)
  - timestamp: Instant

ApplicationNote (entity)
  - id: NoteId
  - content: String
  - category: NoteCategory (GENERAL, PREP, FOLLOW_UP, RESEARCH, OFFER)
  - createdAt: Instant
  - updatedAt: Instant

TimelineEvent (value object)
  - type: TimelineEventType
  - title: String
  - description: String
  - timestamp: Instant
  - metadata: Map<String, String> (links, email ids)

AutomationInfo (value object)
  - status: AutomationStatus (PENDING, IN_PROGRESS, SUBMITTED, CAPTCHA, FAILED, MANUAL_APPROVAL)
  - sessionId: AutomationSessionId
  - submittedAt: Instant
  - evidenceUrl: String (screenshot S3 link)
  - formData: Map<String, String> (submitted fields — debug/audit)
  - errorMessage: String

FollowUp (value object)
  - dueDate: Instant
  - type: FollowUpType (EMAIL | PHONE | LINKEDIN_MESSAGE)
  - notes: String
  - isCompleted: boolean
  - remindedAt: Instant

ApplicationAttachment (entity)
  - id: AttachmentId
  - fileName: String
  - fileUrl: String (S3)
  - contentType: String
  - uploadedAt: Instant

TimelineEventType (enum)
  APPLICATION_SUBMITTED, STATUS_CHANGED, NOTE_ADDED, INTERVIEW_SCHEDULED,
  FOLLOW_UP_SET, OFFER_RECEIVED, AUTOMATION_STARTED, AUTOMATION_COMPLETED,
  EMAIL_RECEIVED, RESEARCH_ADDED
```

**State Machine Rules:**

```
SAVED → APPLIED: User or automation submits application
APPLIED → PHONE_SCREEN: Recruiter outreach / HR screen
PHONE_SCREEN → TECHNICAL_INTERVIEW: Pass phone screen
TECHNICAL_INTERVIEW → ONSITE_INTERVIEW: Pass technical
↑ Any → OFFER: Received offer
↑ Any → REJECTED: No longer in process
↑ Any → WITHDRAWN: User withdraws
OFFER → ACCEPTED: User accepts
OFFER → REJECTED: User declines
```

**Domain Events:**
```
ApplicationCreatedEvent(applicationId, userId, jobListingId, occurredAt)
ApplicationStatusChangedEvent(applicationId, userId, oldStatus, newStatus, occurredAt)
ApplicationNoteAddedEvent(applicationId, userId, noteId, occurredAt)
FollowUpDueEvent(applicationId, userId, dueDate, occurredAt)
```

**Domain Exceptions:**
```
InvalidStatusTransitionException(from, to)
ApplicationNotFoundException
NoteNotFoundException
AutomationAlreadyInProgressException
```

### 12.2 Application Layer

**Use Cases:**
```
CreateApplicationUseCase(CreateApplicationCommand) → ApplicationResponse
GetApplicationUseCase(applicationId) → ApplicationResponse
ListApplicationsUseCase(userId, status?, page, size) → PaginatedResult<ApplicationResponse>
UpdateApplicationStatusUseCase(UpdateStatusCommand) → ApplicationResponse (validates state machine)
AddNoteUseCase(AddNoteCommand) → ApplicationResponse
DeleteNoteUseCase(applicationId, noteId) → void
AddAttachmentUseCase(AddAttachmentCommand) → ApplicationResponse
DeleteAttachmentUseCase(applicationId, attachmentId) → void
SetFollowUpUseCase(SetFollowUpCommand) → ApplicationResponse
CompleteFollowUpUseCase(followUpId) → void
DeleteApplicationUseCase(applicationId) → void (soft delete)
GetTimelineUseCase(applicationId) → List<TimelineEventResponse>
TriggerAutomationUseCase(applicationId) → AutomationResponse (delegates to automation module)
```

**Ports:**
```
ApplicationRepository
  - findById(ApplicationId): Optional<Application>
  - findByUserId(userId, status?, pageable): Page<Application>
  - findByUserIdAndJobId(userId, jobId): Optional<Application>
  - findByAutomationStatus(automationStatus): List<Application>
  - save(Application): Application
  - delete(applicationId): void
  - countByUserIdAndStatus(userId, status): long

FileStoragePort (attachments)
AutomationRequestPort (to automation module — sends start command via Kafka)
FollowUpNotificationPort (scheduler integration)
```

### 12.3 Infrastructure

```
ApplicationJpaRepository
ApplicationRepositoryImpl

ApplicationMapper (MapStruct)
  - toResponse(Application): ApplicationResponse
  - toStatusChangeResponse(StatusChange): StatusChangeResponse
  - toTimelineEventResponse(TimelineEvent): TimelineEventResponse

FollowUpScheduler
  - @Scheduled(cron = "0 */15 * * * *") — every 15 minutes
  - Query applications with upcoming follow-ups
  - Publish FollowUpDueEvent → Notification module
```

### 12.4 Interfaces

```
ApplicationController
  POST   /api/v1/applications                    → 201 ApplicationResponse
  GET    /api/v1/applications                    → PaginatedResult (query: status, page, size)
  GET    /api/v1/applications/{id}               → ApplicationResponse
  PUT    /api/v1/applications/{id}/status        → ApplicationResponse
  DELETE /api/v1/applications/{id}               → 204
  POST   /api/v1/applications/{id}/notes         → ApplicationResponse
  DELETE /api/v1/applications/{id}/notes/{noteId} → 204
  POST   /api/v1/applications/{id}/attachments   → ApplicationResponse (multipart)
  DELETE /api/v1/applications/{id}/attachments/{attachId} → 204
  POST   /api/v1/applications/{id}/follow-ups    → ApplicationResponse
  PUT    /api/v1/applications/{id}/follow-ups/{followUpId}/complete → 204
  GET    /api/v1/applications/{id}/timeline      → List<TimelineEventResponse>
  POST   /api/v1/applications/{id}/automate      → AutomationResponse
```

---

## 13. Module 11: Company Intelligence

### 13.1 Domain Layer

```
CompanyProfile (aggregate root)
  - id: CompanyId
  - name: String
  - description: String
  - website: String
  - logoUrl: String
  - industry: String
  - headquarters: Location
  - foundedYear: int
  - companySize: Range (min, max employees)
  - stockSymbol: String (optional)
  - fundingRounds: List<FundingRound>
  - technologyStack: List<String>
  - cultureKeywords: List<String>
  - benefits: List<String>
  - interviewNotes: List<InterviewNote> (anonymized aggregation)
  - salaryData: List<SalaryDataPoint>
  - hiringTrends: HiringTrends
  - glassdoorRating: double (optional)
  - linkedinUrl: String
  - crunchbaseUrl: String
  - lastUpdatedAt: Instant
  - createdAt: Instant

FundingRound (value object)
  - date: Instant
  - amount: Money
  - roundType: String (SEED, SERIES_A, SERIES_B, etc.)
  - investors: List<String>

SalaryDataPoint (value object)
  - role: String
  - min: BigDecimal
  - max: BigDecimal
  - currency: String
  - source: String (SELF_REPORTED | GLASSDOOR | LEVELS_FYI | BLIND)

HiringTrends (value object)
  - openRoles: int
  - growthRate: double (percentage month-over-month)
  - recentHires: List<RecentHire> (role, date, source)

RecentHire (value object)
  - role: String
  - hiredAt: Instant
  - source: String (LINKEDIN)

InterviewNote (value object)
  - role: String
  - difficulty: int (1-5)
  - duration: int (minutes)
  - rounds: int
  - topics: List<String>
  - tips: String
  - outcome: String (OFFER, REJECTION, WITHDREW)
```

### 13.2 Application Layer

**Use Cases:**
```
GetCompanyProfileUseCase(companyId/name) → CompanyProfileResponse
SearchCompaniesUseCase(query, page, size) → PaginatedResult<CompanySummaryResponse>
GetTechnologyStackUseCase(companyId) → List<String>
GetSalaryDataUseCase(companyId, role?) → List<SalaryDataPointResponse>
GetInterviewInsightsUseCase(companyId, role?) → InterviewInsightsResponse
GetHiringTrendsUseCase(companyId) → HiringTrendsResponse
EnrichCompanyUseCase(companyId) → void (trigger enrichment from external sources)
```

**Ports:**
```
CompanyRepository
  - findById(CompanyId): Optional<CompanyProfile>
  - findByName(String): Optional<CompanyProfile>
  - search(String): List<CompanyProfile>
  - save(CompanyProfile): CompanyProfile

CompanyEnrichmentPort (interface for external data)
  - fetchBasicInfo(companyName): RawCompanyData
  - fetchTechnologyStack(domain): List<String>
  - fetchGlassdoorData(companyId): GlassdoorData
  - fetchCrunchbaseData(companyName): CrunchbaseData
  - fetchLinkedInData(companyUrl): LinkedInCompanyData
```

### 13.3 Infrastructure

```
LinkedInCompanyScraperAdapter implements CompanyEnrichmentPort
GlassdoorApiAdapter implements CompanyEnrichmentPort
CrunchbaseApiAdapter implements CompanyEnrichmentPort
LevelsFyiAdapter implements CompanyEnrichmentPort (salary data)

CompanyEnrichmentScheduler
  - Daily batch: enrich profiles that haven't been updated in 7+ days
```

---

## 14. Module 12: Interview Hub

### 14.1 Domain Layer

```
InterviewSession (aggregate root)
  - id: SessionId
  - userId: UserId
  - targetRole: String
  - targetCompany: String (optional)
  - mode: InterviewMode (TEXT | VOICE)
  - status: SessionStatus (IN_PROGRESS, COMPLETED, ABANDONED)
  - questions: List<InterviewQuestion>
  - currentQuestionIndex: int
  - overallScore: InterviewScore (null until completed)
  - durationSeconds: int
  - feedback: SessionFeedback
  - startedAt: Instant
  - completedAt: Instant

InterviewQuestion (entity)
  - id: QuestionId
  - type: QuestionType (BEHAVIORAL, TECHNICAL, SYSTEM_DESIGN, CODING, SITUATIONAL, DOMAIN)
  - category: String (e.g. "Leadership", "Algorithms", "React")
  - question: String
  - difficulty: int (1-5)
  - expectedDuration: int (seconds)
  - userResponse: UserResponse (optional)
  - score: QuestionScore (optional)
  - feedback: String (optional)
  - orderIndex: int

UserResponse (value object)
  - text: String
  - audioUrl: String (optional, for voice mode)
  - durationSeconds: int
  - submittedAt: Instant

QuestionScore (value object)
  - overall: int (1-10)
  - relevance: int (1-10)
  - clarity: int (1-10)
  - structure: int (STAR method adherence)
  - depth: int (1-10)
  - confidence: int (1-10)

SessionFeedback (value object)
  - strengths: List<String>
  - areasForImprovement: List<String>
  - overallAssessment: String
  - suggestedResources: List<String>
  - generatedAt: Instant

InterviewQuestionBank (entity — curated, not per-session)
  - id: QuestionBankId
  - type: QuestionType
  - category: String
  - question: String
  - difficulty: int
  - expectedAnswer: String (ideal points)
  - tags: List<String>
  - source: String (COMMUNITY, AI_GENERATED, CURATED)

QuestionType (enum): BEHAVIORAL, TECHNICAL, SYSTEM_DESIGN, CODING, SITUATIONAL, DOMAIN
InterviewMode (enum): TEXT, VOICE
SessionStatus (enum): IN_PROGRESS, COMPLETED, ABANDONED
```

### 14.2 Application Layer

**Use Cases:**
```
CreateSessionUseCase(CreateSessionCommand) → SessionResponse
GetSessionUseCase(sessionId) → SessionResponse
ListSessionsUseCase(userId, page, size) → PaginatedResult<SessionSummaryResponse>
GetNextQuestionUseCase(sessionId) → QuestionResponse
SubmitAnswerUseCase(SubmitAnswerCommand) → QuestionScoreResponse (AI scores answer)
CompleteSessionUseCase(sessionId) → SessionFeedbackResponse
AbandonSessionUseCase(sessionId) → void
DeleteSessionUseCase(sessionId) → void
GetQuestionBankUseCase(type?, category?, difficulty?) → List<QuestionBankResponse>
GetCompanyQuestionsUseCase(companyId) → List<QuestionBankResponse> (scraped from Glassdoor)
```

**Ports:**
```
InterviewSessionRepository
  - findById(SessionId): Optional<InterviewSession>
  - findByUserId(userId, pageable): Page<InterviewSession>
  - save(InterviewSession): InterviewSession
  - delete(sessionId): void

QuestionBankRepository
  - findByFilters(type, category, difficulty, pageable): Page<InterviewQuestionBank>
  - findByCompanyId(companyId): List<InterviewQuestionBank>
  - findRandom(type, difficulty, limit): List<InterviewQuestionBank>
  - save(InterviewQuestionBank): InterviewQuestionBank

AiInterviewPort (to AI module)
  - predictQuestions(role, company, userProfile): List<InterviewQuestion>
  - scoreAnswer(question, userResponse): QuestionScore
  - generateFeedback(session): SessionFeedback
```

---

## 15. Module 13: Career Analytics

### 15.1 Domain Layer

```
AnalyticsAggregate (read model — computed, not an aggregate root)
  - userId: UserId
  - period: AnalyticsPeriod (WEEKLY, MONTHLY, ALL_TIME)
  - applicationMetrics: ApplicationMetrics
  - interviewMetrics: InterviewMetrics
  - resumeMetrics: ResumeMetrics
  - timeSeries: List<DataPoint>

ApplicationMetrics (value object)
  - totalApplications: int
  - applicationsByStatus: Map<ApplicationStatus, int>
  - applicationFunnel: FunnelStage[]
  - conversionRate: double (OFFER/TOTAL)
  - averageResponseTime: Duration
  - applicationsBySource: Map<String, int>
  - applicationsByDay: List<DataPoint>

InterviewMetrics (value object)
  - totalSessions: int
  - averageScore: double
  - scoreTrend: List<DataPoint>
  - topStrengths: List<String>
  - weakestAreas: List<String>

ResumeMetrics (value object)
  - averageAtsScore: double
  - scoreHistory: List<DataPoint>
  - templateUsage: Map<String, int>

FunnelStage (value object)
  - stage: ApplicationStatus
  - count: int
  - dropOffRate: double (% that didn't advance to next stage)
  - averageDurationInStage: Duration

DataPoint (value object)
  - date: LocalDate
  - value: double
```

### 15.2 Application Layer

**Use Cases:**
```
GetDashboardUseCase(userId, period) → DashboardResponse
GetFunnelAnalysisUseCase(userId) → FunnelResponse
GetActivityReportUseCase(userId, from, to) → ActivityReportResponse
GetResumePerformanceUseCase(userId) → ResumePerformanceResponse
GetWeeklyDigestUseCase(userId) → WeeklyDigestResponse
```

**Ports:**
```
AnalyticsRepository (read-only, optimized for aggregation queries)
  - getDashboard(userId, period): AnalyticsAggregate
  - getFunnel(userId): FunnelStage[]
  - getTimeSeries(userId, metric, from, to): List<DataPoint>

EventStorePort (for time-series events)
  - getEvents(userId, eventTypes, from, to): List<AnalyticsEvent>
```

### 15.3 Infrastructure

```
AnalyticsCalculatorService (scheduled computation)
  - @Scheduled(cron = "0 0 2 * * ?") — daily at 2 AM
  - Pre-compute analytics for active users
  - Store results in analytics_read_model table (denormalized)

Materialized View (PostgreSQL):
CREATE MATERIALIZED VIEW mv_daily_analytics AS
  SELECT ... (complex aggregation query)
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_analytics;
```

---

## 16. Module 14: Browser Automation

### 16.1 Domain Layer

```
AutomationSession (aggregate root)
  - id: AutomationSessionId
  - userId: UserId
  - applicationId: ApplicationId
  - state: SessionState (state machine)
  - jobUrl: URL
  - formFields: List<DetectedFormField>
  - submittedData: Map<String, String>
  - attemptCount: int
  - maxRetries: int
  - proxyUsed: String
  - userAgentUsed: String
  - startedAt: Instant
  - completedAt: Instant
  - errorMessage: String
  - evidence: AutomationEvidence (screenshots, logs)
  - createdAt: Instant

SessionState (enum — state machine)
  QUEUED → INITIALIZING → NAVIGATING → FORM_DETECT →
  FORM_FILL → SUBMIT → VERIFY → COMPLETED
                                  ↘ CAPTCHA_DETECTED → BLOCKED → MANUAL_REQUIRED
                                  ↘ FORM_ERROR → RETRYING → (max retries → FAILED)
                                  ↘ TIMEOUT → FAILED

DetectedFormField (value object)
  - fieldName: String
  - fieldType: FieldType (TEXT, EMAIL, PHONE, TEXTAREA, SELECT, FILE, CHECKBOX, RADIO)
  - selector: String (CSS/XPath)
  - isRequired: boolean
  - detectedLabel: String
  - mappedProfileField: String (which profile field to use)

FieldType (enum)
  TEXT, EMAIL, PHONE, TEXTAREA, SELECT, FILE, CHECKBOX, RADIO, DATE, HIDDEN

AutomationEvidence (value object)
  - preSubmitScreenshot: String (URL)
  - postSubmitScreenshot: String (URL)
  - confirmationText: String (detected confirmation message)
  - pageTitle: String
  - logs: List<AutomationLogEntry>

AutomationLogEntry (value object)
  - timestamp: Instant
  - level: LogLevel (DEBUG, INFO, WARN, ERROR)
  - action: String
  - details: String
  - durationMs: long

ApplicationResult (value object)
  - success: boolean
  - confirmationUrl: String (if available)
  - message: String
  - evidence: AutomationEvidence

AtsPlatformType (enum)
  GREENHOUSE, LEVER, WORKDAY, TALEO, BAMBOOHR, SUCCESSFACTORS, ICIMS, CUSTOM
```

**Domain Events:**
```
AutomationSessionCreatedEvent(sessionId, applicationId, occurredAt)
AutomationSessionStateChangedEvent(sessionId, oldState, newState, occurredAt)
AutomationCompletedEvent(sessionId, applicationId, result, occurredAt)
AutomationFailedEvent(sessionId, applicationId, error, occurredAt)
AutomationCaptchaDetectedEvent(sessionId, applicationId, occurredAt)
AutomationProgressEvent(sessionId, state, progress: double, message, occurredAt)
```

**Domain Exceptions:**
```
AutomationSessionNotFoundException
AutomationAlreadyRunningException
UnsupportedAtsPlatformException
FormFieldNotMappedException
MaxRetriesExceededException
BrowserTimeoutException
NavigationFailedException
```

### 16.2 Application Layer

**Use Cases:**
```
StartAutomationUseCase(StartAutomationCommand) → AutomationSessionResponse
  Flow: Validate user tier → Create session → Enqueue to Kafka → Return

GetAutomationSessionUseCase(sessionId) → AutomationSessionResponse
ListAutomationSessionsUseCase(userId, page, size) → PaginatedResult
RetryAutomationUseCase(sessionId) → AutomationSessionResponse
CancelAutomationUseCase(sessionId) → void
GetAutomationProgressUseCase(sessionId, lastEventId?) → Flux<ProgressEvent> (SSE/WS)
GetAutomationEvidenceUseCase(sessionId) → EvidenceResponse
ResolveManualActionUseCase(sessionId, resolvedByUser) → AutomationSessionResponse
```

**Ports:**
```
AutomationSessionRepository
  - findById(SessionId): Optional<AutomationSession>
  - findByApplicationId(applicationId): Optional<AutomationSession>
  - findByUserId(userId, pageable): Page<AutomationSession>
  - findByState(SessionState): List<AutomationSession>
  - save(AutomationSession): AutomationSession

BrowserInstancePort (to browser infrastructure)
  - createSession(url, proxy, userAgent): BrowserSessionId
  - navigate(url): NavigationResult
  - detectFormFields(): List<DetectedFormField>
  - fillField(selector, value): void
  - uploadFile(selector, fileUrl): void
  - click(selector): void
  - takeScreenshot(): byte[]
  - getPageText(): String
  - closeSession(): void
  - detectCaptcha(): boolean
  - solveCaptcha(apiKey?): boolean  (optional — use 3rd party service)

ProxyManagerPort
  - getNextProxy(): ProxyConfig
  - markBad(proxy): void
  - markGood(proxy): void

FormMapperPort
  - mapFields(detectedFields, userProfile): Map<String, String>
  - identifyAtsPlatform(dom): AtsPlatformType
```

### 16.3 Infrastructure

```
PlaywrightBrowserEngine implements BrowserInstancePort
  - Uses Playwright Java library
  - Manages browser context lifecycle
  - Anti-detection: viewport randomization, human-like typing, mouse movements
  - Screenshot capture at key states
  - CAPTCHA detection via image analysis (basic heuristics + 3rd party API)

SimpleProxyManager implements ProxyManagerPort
  - Rotates through proxy pool
  - Tracks success/failure per proxy
  - Integrates with residential proxy service (BrightData, Oxylabs)

AtsFormDetector
  - Platform-specific field detection strategies
  - Greenhouse: meta tags, known CSS classes
  - Lever: known form structure, field names
  - Workday: complex multi-page, AJAX-heavy
  - Generic: form element analysis, label-field association

Pipeline:
┌─────────────────────────────────────────────────────────────┐
│ AutomationOrchestrationWorker (Kafka consumer)              │
│   Consumes: automation-sessions topic                       │
│                                                              │
│   1. Dequeue → Load AutomationSession                       │
│   2. Update state: QUEUED → INITIALIZING                    │
│   3. Emit progress event (WebSocket)                        │
│   4. PlaywrightBrowserEngine.createSession(url, proxy, ua) │
│   5. Try:                                                    │
│      a. Navigate to URL                                     │
│      b. Wait for page load (networkidle)                     │
│      c. Detect CAPTCHA → if yes → MANUAL_REQUIRED           │
│      d. Detect form fields                                   │
│      e. Map fields to profile data                           │
│      f. Fill fields (one by one with realistic delays)      │
│      g. Upload resume file                                   │
│      h. Submit form                                          │
│      i. Wait for confirmation                                │
│      j. Capture evidence (screenshots + confirmation)       │
│      k. Update state: COMPLETED                              │
│   6. Catch:                                                  │
│      - CAPTCHA → BLOCKED → MANUAL_REQUIRED                  │
│      - Retryable error → RETRYING (up to 3)                  │
│      - Non-retryable → FAILED                                 │
│   7. Publish completion event                                │
│   8. Emit final progress event                                │
└─────────────────────────────────────────────────────────────┘
```

### 16.4 Anti-Detection Strategy

```
User-Agent Rotation:
  - Pool of 50+ realistic user agents
  - Rotated per session (not per request)

Viewport Randomization:
  - Width: 1280–1920px
  - Height: 720–1080px
  - Random devicePixelRatio (1, 1.5, 2)

Typing Simulation:
  - Random delay between keystrokes: 50–150ms
  - Random typos + correction (optional, 5% chance)
  - Tab between fields (not instant)

Mouse Movements:
  - Bezier curve paths between elements
  - Random hover time: 200–800ms
  - Scroll behavior (random scroll before clicking)

Session Management:
  - Cookies persisted per domain (not per session)
  - LocalStorage/sessionStorage for known sites
  - Maximum 3 concurrent sessions per IP

Rate Limiting:
  - Max 1 application per 60 seconds per account
  - Max 10 applications per hour per account
  - Max 50 applications per day per account
```

---

## 17. Module 15: Notification Service

### 17.1 Domain Layer

```
Notification (aggregate root)
  - id: NotificationId
  - userId: UserId
  - type: NotificationType
  - title: String
  - body: String
  - data: NotificationData (JSONB — actionable context)
  - channels: List<NotificationChannel>
  - status: NotificationStatus (PENDING, SENT, READ, FAILED)
  - readAt: Instant (nullable)
  - sentAt: Instant
  - createdAt: Instant

NotificationType (enum)
  APPLICATION_CONFIRMED, APPLICATION_STATUS_CHANGE, AUTOMATION_COMPLETE,
  AUTOMATION_FAILED, NEW_JOB_MATCH, FOLLOW_UP_REMINDER, INTERVIEW_REMINDER,
  WELCOME, SUBSCRIPTION_EXPIRING, PAYMENT_FAILED, WEEKLY_DIGEST, SYSTEM

NotificationChannel (enum): IN_APP, EMAIL, PUSH

NotificationStatus (enum): PENDING, SENT, READ, FAILED, CANCELLED

NotificationData (value object)
  - typeSpecific: Map<String, Object>
  - deepLink: String (routing link in app)
  - imageUrl: String (optional)
  - priority: NotificationPriority (LOW, NORMAL, HIGH, CRITICAL)

NotificationTemplate (value object)
  - type: NotificationType
  - titleTemplate: String (with {{variables}})
  - bodyTemplate: String
  - channels: List<NotificationChannel>
  - version: int

UserNotificationPreferences (entity, per user)
  - userId: UserId
  - channelPreferences: Map<NotificationChannel, boolean>
  - typePreferences: Map<NotificationType, boolean>
  - digestFrequency: DigestFrequency (INSTANT, DAILY, WEEKLY, NEVER)
  - quietHoursStart: LocalTime (optional)
  - quietHoursEnd: LocalTime (optional)
```

**Domain Events:**
```
NotificationCreatedEvent(notificationId, userId, type, channels, occurredAt)
NotificationSentEvent(notificationId, channel, occurredAt)
NotificationReadEvent(notificationId, userId, occurredAt)
```

### 17.2 Application Layer

**Use Cases:**
```
SendNotificationUseCase(SendNotificationCommand) → void (creates + sends)
MarkAsReadUseCase(notificationId) → void
MarkAllAsReadUseCase(userId) → void
ListNotificationsUseCase(userId, unreadOnly?, page, size) → PaginatedResult
GetUnreadCountUseCase(userId) → int
GetNotificationPreferencesUseCase(userId) → PreferencesResponse
UpdateNotificationPreferencesUseCase(UpdatePreferencesCommand) → void
ProcessDigestUseCase → void (scheduled — sends daily/weekly digests)
```

**Ports:**
```
NotificationRepository
  - findById(NotificationId): Optional<Notification>
  - findByUserId(userId, unreadOnly?, pageable): Page<Notification>
  - countUnreadByUserId(userId): long
  - save(Notification): Notification
  - markAllRead(userId): int

EmailSenderPort
  - send(to, subject, body, templateId?): SendResult
  - sendWithTemplate(to, templateName, context): SendResult

PushNotificationPort
  - send(userId, title, body, data): SendResult (Web Push API / Firebase)

TemplateRendererPort
  - render(template, context): RenderedContent

UserPreferencesRepository
  - findByUserId(userId): Optional<UserNotificationPreferences>
  - save(preferences): void
```

### 17.3 Infrastructure

```
SendGridEmailSender implements EmailSenderPort
  - Uses SendGrid v3 API
  - Template support (SendGrid dynamic templates)
  - Batch sending for digests

WebPushSender implements PushNotificationPort
  - Uses Web Push API (VAPID keys)
  - Subscription management (store/update/delete push subscriptions)

InAppNotificationHandler (same process — no port needed)
  - Direct save to DB
  - WebSocket push via STOMP

DigestScheduler
  - @Scheduled(cron = "0 0 8 * * MON") — weekly digest every Monday
  - @Scheduled(cron = "0 0 18 * * *") — daily digest at 6 PM
  - Aggregates unread events, renders email, sends batch
```

---

## 18. Module 16: Admin Portal

### 18.1 Domain Layer

```
AdminAction (value object — audit trail)
  - adminUserId: UserId
  - action: ActionType
  - targetType: String (USER, SUBSCRIPTION, JOB_SOURCE, SYSTEM_CONFIG, FEATURE_FLAG)
  - targetId: String
  - before: Map<String, Object> (snapshot)
  - after: Map<String, Object> (snapshot)
  - performedAt: Instant

ActionType (enum)
  SUSPEND_USER, ACTIVATE_USER, DELETE_USER, CHANGE_ROLE, CANCEL_SUBSCRIPTION,
  REFUND, ADD_JOB_SOURCE, REMOVE_JOB_SOURCE, TOGGLE_FEATURE, UPDATE_SYSTEM_CONFIG,
  UPDATE_AI_PROVIDER_CONFIG, VIEW_ANALYTICS
```

### 18.2 Application Layer

**Use Cases:**
```
GetAdminDashboardUseCase → AdminDashboardResponse (system health, user count, revenue, active sessions)
ListUsersUseCase(query, filters, page, size) → PaginatedResult
GetUserDetailUseCase(userId) → AdminUserDetailResponse
SuspendUserUseCase(userId, reason) → void
ActivateUserUseCase(userId) → void
ChangeUserRoleUseCase(userId, newRole) → void
DeleteUserUseCase(userId, reason) → void (admin-delete)

ListSubscriptionsUseCase(filters, page) → PaginatedResult
CancelSubscriptionUseCase(subscriptionId, reason) → void
RefundUseCase(subscriptionId, amount) → RefundResponse

ManageJobSourcesUseCase → List<JobSourceConfigResponse>
AddJobSourceUseCase(AddJobSourceCommand) → JobSourceConfigResponse
RemoveJobSourceUseCase(sourceId) → void
ToggleJobSourceUseCase(sourceId, active) → void

ManageFeatureFlagsUseCase → List<FeatureFlagResponse>
ToggleFeatureFlagUseCase(flagName, enabled) → void
CreateFeatureFlagUseCase(CreateFlagCommand) → FeatureFlagResponse

GetSystemHealthUseCase → SystemHealthResponse
GetAdminAnalyticsUseCase(period) → AdminAnalyticsResponse
ListAuditLogsUseCase(filters, page, size) → PaginatedResult
```

**Ports:**
```
AdminUserRepository (read-only admin queries, may use different projections)
SubscriptionAdminRepository
AuditLogRepository
FeatureFlagRepository
JobSourceConfigRepository
SystemHealthPort (aggregates health from all components)
```

### 18.3 Infrastructure

```
AdminUserJpaRepository (with admin-specific @Query projections)
AuditLogJpaRepository

FeatureFlagService
  - In-memory cache (refreshed from DB every 60 seconds)
  - Used by @ConditionalOnExpression or custom annotation
  - Flags: enable_ai_tailoring, enable_automation, enable_voice_interview, maintenance_mode
```

---

## 19. Module 17: Settings Module

### 19.1 Domain Layer

```
UserSettings (aggregate root — per user)
  - id: SettingsId
  - userId: UserId
  - theme: Theme (LIGHT | DARK | SYSTEM)
  - language: Language
  - timezone: String
  - aiProvider: AiProviderPreference
  - notificationPrefs: UserNotificationPreferences (shared with Notif module)
  - privacySettings: PrivacySettings
  - displaySettings: DisplaySettings

AiProviderPreference (value object)
  - preferredProvider: AiProviderType
  - fallbackProvider: AiProviderType
  - maxTokensPerMonth: int (tier-based)
  - enableAiSuggestions: boolean
  - enableAutoTailoring: boolean
  - modelPreference: String (optional, e.g. "gpt-4" vs "gpt-3.5-turbo")

PrivacySettings (value object)
  - shareProfileWithRecruiters: boolean
  - anonymizeAnalytics: boolean
  - allowDataForTraining: boolean
  - profileVisibility: Visibility (PUBLIC, CONNECTIONS_ONLY, PRIVATE)

DisplaySettings (value object)
  - compactMode: boolean
  - showSalaryInSearch: boolean
  - defaultJobSearchFilters: SearchQuery (snapshot)
  - pipelineViewMode: PipelineView (KANBAN | LIST | TABLE)
```

### 19.2 Application Layer

**Use Cases:**
```
GetSettingsUseCase(userId) → SettingsResponse
UpdateSettingsUseCase(UpdateSettingsCommand) → SettingsResponse
UpdatePrivacyUseCase(UpdatePrivacyCommand) → SettingsResponse
UpdateDisplayUseCase(UpdateDisplayCommand) → SettingsResponse
ExportDataUseCase(userId) → DataExportResponse (GDPR)
DeleteAccountUseCase(userId, DeleteAccountCommand) → void (GDPR)
```

---

## 20. Module 18: Search Engine

### 20.1 Domain Layer

```
SearchIndexEntry (read model — maintained via CQRS)
  - id: UUID
  - entityType: SearchEntityType (JOB, COMPANY, RESUME, USER)
  - entityId: UUID (foreign key to actual entity)
  - title: String (indexed, weighted)
  - content: String (indexed, description/body)
  - metadata: Map<String, String> (filterable fields)
  - tsvector: Object (PostgreSQL full-text search vector)
  - embedding: List<Float> (pgvector, for semantic search)
  - updatedAt: Instant

SearchEntityType (enum): JOB, COMPANY, RESUME, USER

SearchResult (value object)
  - entityType: SearchEntityType
  - entityId: UUID
  - title: String
  - snippet: String (highlighted excerpt)
  - score: double (relevance)
  - matchedTerms: List<String>
  - metadata: Map<String, String>
```

### 20.2 Application Layer

**Use Cases:**
```
FullTextSearchUseCase(SearchQuery) → PaginatedResult<SearchResult>
  - Build PostgreSQL tsquery from keywords
  - Weighted search: title^5, company^3, content^1
  - Apply filters (location, salary, date, etc.)
  - Apply pagination (cursor-based)
  - Return results with highlighted snippets

SemanticSearchUseCase(query, limit) → PaginatedResult<SearchResult>
  - Generate embedding from query text
  - Cosine similarity search via pgvector
  - Combine with full-text scores (weighted hybrid)

FacetedSearchUseCase(SearchQuery) → SearchResultWithFacets
  - Same as full-text, plus facet counts:
    - locations, companies, salary ranges, experience levels, date ranges
```

### 20.3 Infrastructure

```
PostgreSQL Full-Text Search Implementation:
  - tsvector column with weighted tokens (A=title, B=company, C=description)
  - GIN index on tsvector
  - ts_rank for relevance scoring
  - plainto_tsquery for query parsing
  - highlight results using ts_headline

pgvector Implementation:
  - 1536-dimension vectors (OpenAI ada-002 compatible)
  - IVFFlat index with 100 lists (good balance of speed/recall)
  - Cosine distance operator (<=>)
  - Hybrid query: full-text filter first, then vector re-rank

Index Maintenance:
  - Trigger-based updates: on INSERT/UPDATE of job_listings, companies
  - Full reindex nightly
  - VACUUM ANALYZE weekly
```

---

## 21. Cross-Cutting Concerns

### 21.1 Global Exception Handling

```
GlobalExceptionHandler (@ControllerAdvice)
  - Handles all domain exceptions → 4xx with specific codes
  - Handles validation errors → 400 with field-level messages
  - Handles auth errors → 401
  - Handles authorization errors → 403
  - Handles not found → 404
  - Handles conflicts → 409
  - Handles rate limit → 429
  - Handles unexpected → 500 (with request_id for traceability)

Error Codes:
  VALIDATION_ERROR, AUTHENTICATION_ERROR, AUTHORIZATION_ERROR,
  RESOURCE_NOT_FOUND, CONFLICT, RATE_LIMIT_EXCEEDED,
  INTERNAL_ERROR, SERVICE_UNAVAILABLE, DEPENDENCY_FAILURE,
  BUSINESS_RULE_VIOLATION, INVALID_STATE_TRANSITION
```

### 21.2 Auditing

```
AuditAspect (@Aspect)
  - Intercepts all AdminService methods + sensitive user operations
  - Records: who, what, when, before-state, after-state, IP, user-agent
  - Persists to audit_log table (partitioned by month)

@Audited (custom annotation)
  - Applied to service methods requiring audit trail
  - Parameters: action, resourceType, resourceId extraction SpEL
```

### 21.3 Request Context

```
RequestContextFilter
  - Extracts X-Request-Id from header (or generates)
  - Extracts X-User-Id from JWT (set by Gateway)
  - Populates MDC: request_id, user_id, ip_address
  - Provides RequestContextHolder (ThreadLocal) for service layer access
```

### 21.4 Feature Flags

```
FeatureFlagService (cross-cutting)
  - Evaluates: isEnabled(flagName, userId?): boolean
  - Cached in Redis (TTL: 60 seconds)
  - Admin can toggle without deployment
  - Flags: maintenance_mode, new_ai_tailoring, automation_v2, voice_interview
```

### 21.5 Rate Limiting

```
RateLimitingFilter (in Gateway)
  - Per-user: 100 requests/minute (general), 10/minute (AI)
  - Per-IP: 200 requests/minute (unauthenticated)
  - Per-endpoint: custom limits (POST /auth/login: 5/minute)
  - Sliding window implementation via Redis sorted sets
  - Returns 429 with Retry-After header
```

---

## 22. Sequence Diagrams (Complete)

### 22.1 User Registers + Onboards

```
USER                  GATEWAY              AUTH MODULE          USER MODULE         INFRA
 │                       │                     │                    │                 │
 │ POST /auth/register   │                     │                    │                 │
 │ (email, password)     │                     │                    │                 │
 │──────────────────────►│                     │                    │                 │
 │                       │ Validate + route    │                    │                 │
 │                       │────────────────────►│                    │                 │
 │                       │                     │                    │                 │
 │                       │                     │ Validate email     │                 │
 │                       │                     │ Check uniqueness   │                 │
 │                       │                     │───────────────────►│ (DB query)      │
 │                       │                     │◄───────────────────│                 │
 │                       │                     │                    │                 │
 │                       │                     │ Hash password      │                 │
 │                       │                     │ Create User entity │                 │
 │                       │                     │ Create Profile     │─────► User Mod   │
 │                       │                     │───────────────────►│ (DB insert)     │
 │                       │                     │                    │                 │
 │                       │                     │ Publish event      │─────► Kafka     │
 │                       │                     │ UserRegistered     │                 │
 │                       │                     │                    │                 │
 │                       │                     │ Generate JWT       │                 │
 │                       │                     │ + Refresh token    │                 │
 │                       │                     │                    │                 │
 │                       │◄────────────────────│                    │                 │
 │◄──────────────────────│                     │                    │                 │
 │ 200 + AuthResponse    │                     │                    │                 │
 │                       │                     │                    │                 │
 │ (Async)               │                     │                    │                 │
 │ KAFKA CONSUMER        │                     │                    │                 │
 │ - Notification Service reads UserRegistered │                    │                 │
 │ - Sends welcome email via SendGrid          │                    │                 │
 │ - Analytics logs new user signup            │                    │                 │
```

### 22.2 Automated Application (Full Flow)

```
USER              GATEWAY          ATS MODULE      AUTOMATION SERVICE      PLAYWRIGHT       KAFKA
 │                    │                │                    │                  │              │
 │ POST /apps/{id}    │                │                    │                  │              │
 │ /automate          │                │                    │                  │              │
 │───────────────────►│                │                    │                  │              │
 │                    │ Route + JWT    │                    │                  │              │
 │                    │──────────────►│                    │                  │              │
 │                    │                │                    │                  │              │
 │                    │                │ 1. Validate user   │                  │              │
 │                    │                │    is PRO tier      │                  │              │
 │                    │                │ 2. Check limits     │                  │              │
 │                    │                │ 3. Create session   │                  │              │
 │                    │                │    (QUEUED)         │                  │              │
 │                    │                │                    │                  │              │
 │                    │                │ 4. Publish event    │                  │              │
 │                    │                │─────────────────────────────────────────────────►│
 │                    │                │   AutomationRequested│                 │              │
 │                    │                │                    │                  │              │
 │                    │◄───────────────│                    │                  │              │
 │◄───────────────────│                │                    │                  │              │
 │ 202 {session_id}   │                │                    │                  │              │
 │                    │                │                    │                  │              │
 │ [Async on Kafka consumer]          │                    │                  │              │
 │                    │                │                    │                  │              │
 │                    │                │              5. Consume event        │              │
 │                    │                │              Update state:           │              │
 │                    │                │              INITIALIZING            │              │
 │                    │                │                    │                  │              │
 │                    │                │              6. Select proxy + UA   │              │
 │                    │                │              Create browser context  │              │
 │                    │                │─────────────────────────────────────►│              │
 │                    │                │                    │                  │              │
 │                    │                │              7. Navigate to job URL │              │
 │                    │                │─────────────────────────────────────►│              │
 │                    │                │◄─────────────────────────────────────│ done         │
 │                    │                │                    │                  │              │
 │                    │                │              8. Detect form fields   │              │
 │                    │                │─────────────────────────────────────►│              │
 │                    │                │◄─────────────────────────────────────│ fields       │
 │                    │                │                    │                  │              │
 │                    │                │              9. Map fields to       │              │
 │                    │                │                 user profile data    │              │
 │                    │                │                    │                  │              │
 │  WS: /topic/auto/   ◄─────── 10. Emit PROGRESS (filling) ◄─── Kafka ────│              │
 │  {sessionId}       │                │                    │                  │              │
 │                    │                │                    │                  │              │
 │                    │                │ 11. Fill form fields (with delays)  │              │
 │                    │                │─────────────────────────────────────►│              │
 │                    │                │ 12. Upload resume                    │              │
 │                    │                │─────────────────────────────────────►│              │
 │                    │                │ 13. Submit form                      │              │
 │                    │                │─────────────────────────────────────►│              │
 │                    │                │◄─────────────────────────────────────│ result       │
 │                    │                │                    │                  │              │
 │                    │                │ 14. Take screenshot                  │              │
 │                    │                │─────────────────────────────────────►│              │
 │                    │                │◄─────────────────────────────────────│ screenshot   │
 │                    │                │                    │                  │              │
 │                    │                │ 15. Close browser    │               │              │
 │                    │                │─────────────────────────────────────►│              │
 │                    │                │                    │                  │              │
 │                    │                │ 16. Update session: COMPLETED       │              │
 │                    │                │ 17. Upload evidence to S3           │              │
 │                    │                │ 18. Update Application status        │              │
 │                    │                │     (APPLIED) in DB                  │              │
 │                    │                │                    │                  │              │
 │                    │                │ 19. Publish event    │               │              │
 │                    │                │──────────────────────────────────────────────────►│
 │                    │                │   AutomationCompleted│                 │              │
 │                    │                │                    │                  │              │
 │  WS: COMPLETED     ◄─────── 20. Emit result ◄─── Kafka ────────────────────│              │
 │                    │                │                    │                  │              │
 │                    │                │                    │                  │              │
 │ [Async consumers]  │                │                    │                  │              │
 │ - Notification: send confirmation  │                    │                  │              │
 │ - Analytics: update funnel metrics │                    │                  │              │
```

### 22.3 Resume Tailoring + Scoring

```
USER              GATEWAY          RESUME MODULE      AI ORCHESTRATOR        AI PROVIDER
 │                    │                │                    │                    │
 │ POST /resumes/{id} │                │                    │                    │
 │ /tailor?job_id=x   │                │                    │                    │
 │───────────────────►│                │                    │                    │
 │                    │ Route + JWT    │                    │                    │
 │                    │──────────────►│                    │                    │
 │                    │                │                    │                    │
 │                    │                │ 1. Load Resume     │                    │
 │                    │                │ 2. Load JobListing │                    │
 │                    │                │ 3. Load UserProfile│                    │
 │                    │                │                    │                    │
 │                    │                │ 4. Check cache     │                    │
 │                    │                │ (prompt hash)      │                    │
 │                    │                │───────────────────►│                    │
 │                    │                │◄───────────────────│ cache miss         │
 │                    │                │                    │                    │
 │                    │                │ 5. Build context   │                    │
 │                    │                │ (via ContextBuilder)│                   │
 │                    │                │                    │                    │
 │                    │                │ 6. Resolve prompt  │                    │
 │                    │                │ (RESUME_TAILORING) │                    │
 │                    │                │───────────────────►│                    │
 │                    │                │                    │ 7. Call provider   │
 │                    │                │                    │ (OpenAI/Claude)    │
 │                    │                │                    │───────────────────►│
 │                    │                │                    │◄───────────────────│
 │                    │                │                    │ (tailored content) │
 │                    │                │◄───────────────────│                    │
 │                    │                │                    │                    │
 │                    │                │ 8. Cache response   │                    │
 │                    │                │ 9. Parse tailored   │                    │
 │                    │                │    content into     │                    │
 │                    │                │    ResumeSections   │                    │
 │                    │                │                    │                    │
 │                    │                │ 10. Save new version│                   │
 │                    │                │     (version +1)    │                    │
 │                    │                │                    │                    │
 │                    │◄───────────────│                    │                    │
 │◄───────────────────│                │                    │                    │
 │ 200 {resume +      │                │                    │                    │
 │ tailored content}  │                │                    │                    │
```

### 22.4 Interview Practice Session

```
USER              GATEWAY          INTERVIEW HUB      AI ORCHESTRATOR        AI PROVIDER
 │                    │                │                    │                    │
 │ POST /interviews   │                │                    │                    │
 │ /sessions          │                │                    │                    │
 │ (role, company?)   │                │                    │                    │
 │───────────────────►│                │                    │                    │
 │                    │──────────────►│                    │                    │
 │                    │                │                    │                    │
 │                    │                │ 1. Get user profile │                   │
 │                    │                │ 2. Get company info │                   │
 │                    │                │    (if specified)   │                    │
 │                    │                │                    │                    │
 │                    │                │ 3. Call AI:         │                    │
 │                    │                │    predictQuestions │                    │
 │                    │                │───────────────────►│                    │
 │                    │                │                    │───────────────────►│
 │                    │                │                    │◄───────────────────│
 │                    │                │◄───────────────────│ (5-10 questions)   │
 │                    │                │                    │                    │
 │                    │                │ 4. Save session    │                    │
 │                    │                │    (IN_PROGRESS)    │                    │
 │                    │                │                    │                    │
 │                    │◄───────────────│                    │                    │
 │◄───────────────────│                │                    │                    │
 │ 201 {session +     │                │                    │                    │
 │  first question}   │                │                    │                    │
 │                    │                │                    │                    │
 │ [User answers]     │                │                    │                    │
 │                    │                │                    │                    │
 │ POST /sessions/{id} │               │                    │                    │
 │ /questions/{qid}    │               │                    │                    │
 │ /answer             │               │                    │                    │
 │ (response text)     │               │                    │                    │
 │───────────────────►│──────────────►│                    │                    │
 │                    │                │                    │                    │
 │                    │                │ 5. Score answer    │                    │
 │                    │                │───────────────────►│                    │
 │                    │                │                    │───────────────────►│
 │                    │                │                    │◄───────────────────│
 │                    │                │◄───────────────────│ (score + feedback) │
 │                    │                │                    │                    │
 │                    │                │ 6. Save response   │                    │
 │                    │                │ 7. Return score    │                    │
 │                    │◄───────────────│                    │                    │
 │◄───────────────────│                │                    │                    │
 │ 200 {score,        │                │                    │                    │
 │  feedback, next_q} │                │                    │                    │
 │                    │                │                    │                    │
 │ [Repeat until      │                │                    │                    │
 │  all questions done]│               │                    │                    │
 │                    │                │                    │                    │
 │ POST /sessions/{id} │               │                    │                    │
 │ /complete          │               │                    │                    │
 │───────────────────►│──────────────►│                    │                    │
 │                    │                │                    │                    │
 │                    │                │ 8. Call AI:         │                   │
 │                    │                │    generateFeedback │                    │
 │                    │                │───────────────────►│───────────────────►│
 │                    │                │◄───────────────────│◄───────────────────│
 │                    │                │                    │                    │
 │                    │                │ 9. Calculate scores│                    │
 │                    │                │ 10. Save summary   │                    │
 │                    │                │                    │                    │
 │                    │◄───────────────│                    │                    │
 │◄───────────────────│                │                    │                    │
 │ 200 {session       │                │                    │                    │
 │  summary, feedback,│                │                    │                    │
 │  overall score}    │                │                    │                    │
```

### 22.5 Career Path Analysis

```
USER              GATEWAY          CAREER ANALYTICS    AI ORCHESTRATOR        AI PROVIDER
 │                    │                │                    │                    │
 │ POST /career       │                │                    │                    │
 │ /path-analyze      │                │                    │                    │
 │ (current,target)   │                │                    │                    │
 │───────────────────►│──────────────►│                    │                    │
 │                    │                │                    │                    │
 │                    │                │ 1. Load user skills │                   │
 │                    │                │    + experience     │                    │
 │                    │                │                    │                    │
 │                    │                │ 2. Retrieve target  │                    │
 │                    │                │    role requirements│                    │
 │                    │                │    (from internal   │                    │
 │                    │                │     skills DB)     │                    │
 │                    │                │                    │                    │
 │                    │                │ 3. Heuristic:       │                    │
 │                    │                │    Compare skills   │                    │
 │                    │                │    Experience match │                    │
 │                    │                │    Education match  │                    │
 │                    │                │                    │                    │
 │                    │                │ 4. Call AI for:     │                    │
 │                    │                │    Gap analysis     │                    │
 │                    │                │    Recommendations  │                    │
 │                    │                │───────────────────►│───────────────────►│
 │                    │                │◄───────────────────│◄───────────────────│
 │                    │                │                    │                    │
 │                    │                │ 5. Calculate       │                    │
 │                    │                │    timeline estimate│                   │
 │                    │                │ 6. Get salary data │                    │
 │                    │                │ 7. Save career path│                    │
 │                    │                │                    │                    │
 │                    │◄───────────────│                    │                    │
 │◄───────────────────│                │                    │                    │
 │ 200 {gaps,         │                │                    │                    │
 │  recommendations,  │                │                    │                    │
 │  timeline,         │                │                    │                    │
 │  salary projection}│                │                    │                    │
```

---

## 23. Class Relationship Diagrams

### 23.1 Core Domain Relationships

```
User (AR) ◄──1:1──► UserProfile (AR)
  │                      │
  │                      ├── List<Skill> (VO)
  │                      ├── List<Experience> (VO)
  │                      ├── List<Education> (VO)
  │                      └── UserPreferences (VO)
  │
  ├── List<Resume> (AR)
  │     └── List<ResumeVersion> (VO)
  │     └── List<AtsScore> (VO)
  │     └── List<ResumeSection> (VO)
  │
  ├── List<CoverLetter> (AR)
  │
  ├── List<Application> (AR)
  │     ├── List<StatusChange> (VO)
  │     ├── List<ApplicationNote> (Entity)
  │     ├── List<TimelineEvent> (VO)
  │     └── AutomationInfo (VO) ────► AutomationSession (AR)
  │
  ├── List<InterviewSession> (AR)
  │     └── List<InterviewQuestion> (Entity)
  │           └── UserResponse (VO)
  │           └── QuestionScore (VO)
  │
  └── List<SavedSearch> (Entity)
        └── SearchQuery (VO)

JobListing (AR)
  ├── SalaryRange (VO)
  ├── JobLocation (VO)
  └── CompanyRef (VO) ────► CompanyProfile (AR)

CompanyProfile (AR)
  ├── List<SalaryDataPoint> (VO)
  ├── List<FundingRound> (VO)
  └── HiringTrends (VO)

AutomationSession (AR)
  ├── List<DetectedFormField> (VO)
  ├── AutomationEvidence (VO)
  └── List<AutomationLogEntry> (VO)
```

### 23.2 Service Layer Relationships

```
AuthController
  └── AuthApplicationService
        ├── UserRepository (port)
        ├── TokenProvider (port)
        ├── PasswordEncoder (port)
        ├── OAuthClientPort (port)
        ├── NotificationPort (port)
        └── EventPublisher (port)

ResumeController
  └── ResumeApplicationService
        ├── ResumeRepository (port)
        ├── UserProfileRepository (port)
        ├── AiResumePort (port)
        ├── ExportPort (port)
        ├── FileStoragePort (port)
        └── ResumeParserPort (port)

ApplicationController
  └── ApplicationService
        ├── ApplicationRepository (port)
        ├── FileStoragePort (port)
        ├── AutomationRequestPort (port)
        └── EventPublisher (port)

JobDiscoveryController
  └── JobSearchService
        ├── JobListingRepository (port)
        ├── SavedSearchRepository (port)
        └── SavedJobRepository (port)

JobMatchService
  ├── JobListingRepository (port)
  ├── UserProfileRepository (port)
  └── AiMatchPort (port)

InterviewController
  └── InterviewService
        ├── InterviewSessionRepository (port)
        ├── QuestionBankRepository (port)
        └── AiInterviewPort (port)

AutomationWorker (Kafka Consumer)
  └── AutomationOrchestrator
        ├── AutomationSessionRepository (port)
        ├── BrowserInstancePort (port)
        ├── ProxyManagerPort (port)
        ├── FormMapperPort (port)
        └── FileStoragePort (port)
```

---

## 24. Dependency Rules Enforcement

### 24.1 ArchUnit Rules (to be codified in tests)

```
Rule 1: Domain layer classes must only import:
  - java.*, jakarta.validation.* (if needed)
  - Other domain classes within same module
  - NO Spring, NO JPA, NO infrastructure

Rule 2: Application layer classes must only import:
  - Domain classes
  - Other application classes (within application layer)
  - java.*
  - NO Spring stereotypes (@Service OK, @Entity NOT OK)

Rule 3: Infrastructure classes may import:
  - Domain classes
  - Application ports (interfaces only)
  - Any framework/library (Spring, Hibernate, Kafka client)
  - NOT application services directly

Rule 4: Interfaces (REST) classes may import:
  - Application use cases (via interface)
  - Application DTOs
  - Own request/response DTOs
  - NOT domain entities directly (use DTOs)

Rule 5: No cyclic dependencies between packages at any layer.

Rule 6: Naming conventions:
  - All domain entities must be suffixed with matching DTO
  - All repositories must implement port interfaces
  - All controllers must have @RestController + @RequestMapping
```

### 24.2 Package Dependency Violation Examples

```
✔ Correct:
  ResumeController → ResumeApplicationService → ResumeRepository (port)
  ↓
  ResumeJpaRepository implements ResumeRepository (infrastructure)

✗ Incorrect:
  ResumeController → ResumeJpaRepository (bypasses service layer)
  ResumeApplicationService → ResumeJpaRepository (depends on infra)
  ResumeEntity (infra) → Resume (domain)  (OK — infra depends on domain)
  Resume (domain) → ResumeJpaRepository  (NOT OK — domain depends on infra)
```

---

*This LLD defines every class, interface, service, DTO, repository, package, and relationship across all 18 modules. Every sequence diagram, state machine, and dependency rule is documented. This is the blueprint architects hand to developers — no ambiguity, no surprises.*

---

**End of LLD v1.0**
