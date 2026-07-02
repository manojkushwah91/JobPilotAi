# JobPilot AI — Backend Foundation

**Version:** 1.0  
**Status:** Draft  
**Phase:** 5 of 35  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Maven Multi-Module Structure
2. Clean Architecture Layers
3. Module Dependency Rules
4. Package Organization (Per Module)
5. Folder Structure
6. Naming Standards
7. Configuration Management
8. Dependency Injection Conventions
9. Exception Handling Conventions
10. Coding Conventions

---

## 1. Maven Multi-Module Structure

```
jobpilot/
├── pom.xml                                    # Parent POM
├── jobpilot-common/                           # Shared primitives, no business logic
├── jobpilot-domain/                           # Domain entities, VOs, events, ports
├── jobpilot-application/                      # Use cases, application services
├── jobpilot-infrastructure/                   # JPA, Redis, Kafka, external clients
├── jobpilot-interfaces/                       # REST controllers, DTOs, WebSocket
├── jobpilot-automation/                       # Browser automation (separate deployable)
├── jobpilot-ai-service/                       # AI orchestration (separate deployable)
├── jobpilot-gateway/                          # Spring Cloud Gateway
└── jobpilot-bootstrap/                        # Spring Boot entry point
```

### 1.1 Parent POM Configuration

```
Group ID: com.jobpilot
Artifact ID: jobpilot
Package: com.jobpilot
Java Version: 21
Spring Boot Version: 3.3.x
Spring Cloud Version: 2023.0.x

Managed Dependencies:
  - Spring Boot Starter Parent (BOM)
  - Spring Cloud Dependencies (BOM)
  - Spring AI BOM
  - Testcontainers BOM
  - MapStruct
  - Lombok
  - OpenAPI / SpringDoc
  - ArchUnit
  - Playwright Java
  - pgvector JDBC
```

### 1.2 Module Dependency Graph

```
jobpilot-bootstrap
    ├── depends on: jobpilot-interfaces
    ├── depends on: jobpilot-infrastructure
    └── depends on: jobpilot-gateway

jobpilot-interfaces
    └── depends on: jobpilot-application

jobpilot-application
    ├── depends on: jobpilot-domain
    └── depends on: jobpilot-common

jobpilot-infrastructure
    ├── depends on: jobpilot-domain
    ├── depends on: jobpilot-application
    └── depends on: jobpilot-common

jobpilot-domain
    └── depends on: jobpilot-common       (common only — zero framework deps)

jobpilot-automation
    ├── depends on: jobpilot-domain
    ├── depends on: jobpilot-common
    └── depends on: jobpilot-application  (for ports only)

jobpilot-ai-service
    ├── depends on: jobpilot-domain
    ├── depends on: jobpilot-common
    └── depends on: jobpilot-application  (for ports only)

jobpilot-gateway
    └── depends on: jobpilot-common       (minimal — only shared constants)

jobpilot-common
    └── NO dependencies (zero deps)
```

---

## 2. Clean Architecture Layers

### 2.1 Layer Definitions (Per Module)

```
┌─────────────────────────────────────────────────────────┐
│                    INTERFACES LAYER                       │
│  (jobpilot-interfaces — "Adapters/Inbound")             │
│                                                          │
│  Role: Translate HTTP/WS requests into application calls │
│  Contents: REST controllers, WebSocket handlers,         │
│            request DTOs, response DTOs, mappers,         │
│            global exception handlers, filters            │
│  Depends on: application layer only                      │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  APPLICATION LAYER                        │
│  (jobpilot-application — "Use Cases / Ports")            │
│                                                          │
│  Role: Orchestrate business workflows, define ports      │
│  Contents: Use case interfaces, application services,    │
│            inbound/outbound port interfaces, DTOs,       │
│            application exceptions, mappers               │
│  Depends on: domain layer only                            │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                           │
│  (jobpilot-domain — "Enterprise Business Rules")         │
│                                                          │
│  Role: Pure business logic, no framework dependencies    │
│  Contents: Entities, value objects, aggregates,          │
│            domain events, domain services,               │
│            domain exceptions, repository interfaces      │
│  Depends on: common layer only                            │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                INFRASTRUCTURE LAYER                       │
│  (jobpilot-infrastructure — "Adapters/Outbound")         │
│                                                          │
│  Role: Implement ports defined in application layer      │
│  Contents: JPA repositories, Redis clients, Kafka        │
│            producers/consumers, AI adapters, email,      │
│            S3, OAuth clients, scheduling                 │
│  Depends on: domain + application layers                  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Layer Isolation Rules

```
RULE 1:  Domain layer must have ZERO imports from:
         - Spring Framework (any spring.*)
         - Jakarta EE (any jakarta.*)
         - Any database driver
         - Any web framework
         - Any JSON library
         - Lombok (controversial — but allowed for @Value records)

RULE 2:  Domain classes may only import:
         - java.*
         - Common layer (com.jobpilot.common.*)
         - Other domain classes within same bounded context
         - Third-party libs ONLY if they have zero transitive framework deps

RULE 3:  Application layer must NOT import:
         - Any infrastructure class
         - Any Spring stereotype (@Service is the ONLY exception)
         - Any JPA annotation
         - Any HTTP-specific class

RULE 4:  Infrastructure layer must NOT be imported by:
         - Interfaces layer (wire via constructor injection)
         - Application layer
         - Domain layer

RULE 5:  Interfaces layer may import:
         - Application layer (use cases, DTOs)
         - Spring Web annotations (@RestController, @RequestMapping)
         - Validation annotations (@Valid, jakarta.validation)
         - OpenAPI annotations (@Schema)

RULE 6:  NEVER skip layers:
         - Controller → Service → Repository (not Controller → Repository)
         - Service → Port → Implementation (not Service → Implementation directly)
```

---

## 3. Module Dependency Rules

### 3.1 Bounded Context Module Structure

Each bounded context follows this structure within `jobpilot-application`, `jobpilot-domain`, and `jobpilot-infrastructure`:

```
com.jobpilot.modules.<context-name>/
    ├── domain/
    ├── application/
    └── infrastructure/
```

### 3.2 Context Isolation Rules

| Rule | Description |
|------|-------------|
| **No cross-context entity references** | Context A must not directly reference a JPA entity from Context B |
| **Cross-context communication via events** | Use domain events + event handlers for cross-context side effects |
| **Anti-corruption layer** | When Context A needs data from Context B, use a dedicated port interface implemented by Context B's service |
| **Shared Kernel module** | Only stable primitives (Email, PhoneNumber, Money, UserId) live here |
| **Context independence** | Each context should be conceptually extractable to its own microservice |

### 3.3 Event-Based Cross-Context Flow

```
Application Module (Context A)
    │ Publish ApplicationSubmittedEvent
    ▼
Kafka (topic: application.events)
    ▼
Notification Module (Context B — consumer)
    │ Handle event → send email
    ▼
Analytics Module (Context B — consumer)
    │ Handle event → update metrics
```

---

## 4. Package Organization (Per Module)

### 4.1 jobpilot-domain: Package Structure

```
com.jobpilot.modules.identity.domain/
    ├── model/
    │   ├── entity/
    │   │   └── User.java
    │   ├── valueobject/
    │   │   ├── Email.java
    │   │   ├── UserId.java
    │   │   ├── PasswordHash.java
    │   │   ├── Role.java
    │   │   └── OAuthProvider.java
    │   └── aggregate/
    │       └── User.java  (if User is aggregate root)
    ├── event/
    │   ├── UserRegisteredEvent.java
    │   ├── UserVerifiedEvent.java
    │   └── UserDeletedEvent.java
    ├── service/
    │   └── PasswordStrengthDomainService.java
    ├── port/
    │   ├── outbound/
    │   │   └── UserRepository.java
    │   └── inbound/
    │       └── (use case interfaces — in module's application layer)
    └── exception/
        ├── EmailAlreadyExistsException.java
        ├── InvalidEmailException.java
        ├── WeakPasswordException.java
        └── UserNotFoundException.java
```

### 4.2 jobpilot-application: Package Structure

```
com.jobpilot.modules.identity.application/
    ├── service/
    │   └── AuthApplicationService.java
    ├── port/
    │   ├── inbound/
    │   │   ├── RegisterUserUseCase.java
    │   │   ├── AuthenticateUserUseCase.java
    │   │   ├── OAuthLoginUseCase.java
    │   │   ├── RefreshTokenUseCase.java
    │   │   └── LogoutUseCase.java
    │   └── outbound/
    │       ├── TokenProvider.java
    │       ├── PasswordEncoder.java
    │       ├── OAuthClientPort.java
    │       └── NotificationPort.java
    ├── dto/
    │   ├── command/
    │   │   ├── RegisterUserCommand.java
    │   │   ├── AuthenticateCommand.java
    │   │   ├── OAuthLoginCommand.java
    │   │   └── RefreshTokenCommand.java
    │   └── response/
    │       ├── AuthTokenResponse.java
    │       ├── UserResponse.java
    │       └── TokenClaimsResponse.java
    ├── mapper/
    │   └── UserMapper.java  (DTO ↔ domain)
    └── exception/
        ├── AuthenticationException.java
        ├── AuthorizationException.java
        └── TokenExpiredException.java
```

### 4.3 jobpilot-infrastructure: Package Structure

```
com.jobpilot.modules.identity.infrastructure/
    ├── persistence/
    │   ├── entity/
    │   │   └── UserEntity.java
    │   ├── repository/
    │   │   ├── UserJpaRepository.java
    │   │   └── UserRepositoryImpl.java
    │   └── mapper/
    │       └── UserEntityMapper.java  (JPA entity ↔ domain entity)
    ├── security/
    │   ├── JwtTokenProvider.java
    │   ├── BCryptPasswordEncoder.java
    │   └── config/
    │       └── SecurityConfig.java
    ├── oauth/
    │   ├── GoogleOAuthClient.java
    │   ├── LinkedInOAuthClient.java
    │   ├── GitHubOAuthClient.java
    │   └── MicrosoftOAuthClient.java
    ├── messaging/
    │   ├── KafkaEventPublisher.java
    │   └── config/
    │       └── KafkaConfig.java
    ├── cache/
    │   ├── RedisTokenStore.java
    │   └── config/
    │       └── RedisConfig.java
    └── config/
        └── AuthInfrastructureConfig.java  (bean wiring)
```

### 4.4 jobpilot-interfaces: Package Structure

```
com.jobpilot.modules.identity.interfaces/
    ├── rest/
    │   ├── AuthController.java
    │   └── UserProfileController.java
    ├── dto/
    │   ├── request/
    │   │   ├── RegisterRequest.java
    │   │   ├── LoginRequest.java
    │   │   ├── RefreshTokenRequest.java
    │   │   ├── OAuthRequest.java
    │   │   ├── ForgotPasswordRequest.java
    │   │   └── ResetPasswordRequest.java
    │   └── response/
    │       ├── AuthResponse.java
    │       └── ErrorResponse.java
    ├── websocket/
    │   ├── NotificationWebSocketHandler.java
    │   └── AutomationProgressHandler.java
    ├── mapper/
    │   └── AuthDtoMapper.java  (request/response DTO ↔ application DTO)
    └── handler/
        └── GlobalExceptionHandler.java
```

---

## 5. Folder Structure (Complete Tree)

```
jobpilot/
│
├── pom.xml                                    # Parent POM (Spring Boot, Cloud, versions)
├── README.md
├── .gitignore
├── .editorconfig
├── checkstyle.xml                             # Code style rules
├── lgtm.yml                                   # LGTM config
│
├── jobpilot-common/                           # ── SHARED KERNEL ──
│   ├── pom.xml
│   └── src/main/java/com/jobpilot/common/
│       ├── model/
│       │   ├── Email.java                     # Validated email value object
│       │   ├── PhoneNumber.java               # E.164 format
│       │   ├── Money.java                     # BigDecimal + currency
│       │   ├── Percentage.java
│       │   ├── DateRange.java
│       │   ├── Address.java
│       │   ├── Url.java
│       │   ├── FileRef.java
│       │   └── Duration.java
│       ├── validation/
│       │   ├── ValidationUtils.java
│       │   └── SelfValidating.java            # Abstract base for VOs
│       ├── exception/
│       │   ├── BaseException.java
│       │   └── ValidationException.java
│       └── util/
│           ├── IdGenerator.java               # UUID v7
│           └── TimeProvider.java              # Clock abstraction
│
├── jobpilot-domain/                           # ── DOMAIN LAYER ──
│   ├── pom.xml
│   └── src/main/java/com/jobpilot/modules/
│       ├── identity/domain/                   # Identity & Access context
│       │   ├── model/entity/User.java
│       │   ├── model/valueobject/...
│       │   ├── event/...
│       │   ├── service/...
│       │   ├── port/outbound/UserRepository.java
│       │   └── exception/...
│       ├── resume/domain/                     # Resume Studio context
│       │   ├── model/aggregate/Resume.java
│       │   ├── model/entity/...
│       │   ├── model/valueobject/...
│       │   ├── event/...
│       │   ├── service/ResumeScoringDomainService.java
│       │   └── port/outbound/ResumeRepository.java
│       ├── job/domain/                        # Job Discovery context
│       │   ├── model/aggregate/JobListing.java
│       │   ├── model/valueobject/...
│       │   └── ...
│       ├── application/domain/                # Application Tracker context
│       │   ├── model/aggregate/Application.java
│       │   ├── model/entity/ApplicationNote.java
│       │   ├── model/valueobject/StatusChange.java
│       │   └── event/ApplicationSubmittedEvent.java
│       ├── interview/domain/                  # Interview Hub context
│       │   ├── model/aggregate/InterviewSession.java
│       │   └── ...
│       ├── company/domain/                    # Company Intelligence context
│       ├── analytics/domain/                  # Career Analytics context
│       ├── notification/domain/               # Notification context
│       ├── automation/domain/                 # Browser Automation context
│       ├── ai/domain/                         # AI Provider Layer context
│       │   ├── model/AiRequest.java
│       │   ├── model/AiResponse.java
│       │   ├── model/AiMessage.java
│       │   ├── port/outbound/AIProviderPort.java
│       │   └── ...
│       ├── billing/domain/                    # Payment context
│       └── admin/domain/                      # Admin context
│
├── jobpilot-application/                      # ── APPLICATION LAYER ──
│   ├── pom.xml
│   └── src/main/java/com/jobpilot/modules/
│       ├── identity/application/
│       │   ├── service/AuthApplicationService.java
│       │   ├── port/inbound/RegisterUserUseCase.java
│       │   ├── port/outbound/TokenProvider.java
│       │   ├── dto/command/RegisterUserCommand.java
│       │   ├── dto/response/AuthTokenResponse.java
│       │   └── mapper/UserMapper.java
│       ├── resume/application/
│       │   ├── service/ResumeApplicationService.java
│       │   ├── port/inbound/TailorResumeUseCase.java
│       │   ├── port/outbound/AiResumePort.java
│       │   └── ...
│       ├── job/application/
│       ├── application/application/
│       ├── interview/application/
│       ├── company/application/
│       ├── analytics/application/
│       ├── notification/application/
│       ├── automation/application/
│       ├── ai/application/
│       │   ├── service/AiOrchestrationService.java
│       │   └── service/ProviderSelector.java
│       ├── billing/application/
│       └── admin/application/
│
├── jobpilot-infrastructure/                   # ── INFRASTRUCTURE ──
│   ├── pom.xml
│   └── src/main/java/com/jobpilot/modules/
│       ├── identity/infrastructure/
│       │   ├── persistence/entity/UserEntity.java
│       │   ├── persistence/repository/UserJpaRepository.java
│       │   ├── persistence/repository/UserRepositoryImpl.java
│       │   ├── persistence/mapper/UserEntityMapper.java
│       │   ├── security/JwtTokenProvider.java
│       │   ├── security/BCryptPasswordEncoder.java
│       │   ├── security/config/SecurityConfig.java
│       │   ├── oauth/GoogleOAuthClient.java
│       │   ├── oauth/LinkedInOAuthClient.java
│       │   └── cache/RedisTokenStore.java
│       ├── resume/infrastructure/
│       │   ├── persistence/entity/ResumeEntity.java
│       │   ├── persistence/repository/ResumeJpaRepository.java
│       │   ├── persistence/repository/ResumeRepositoryImpl.java
│       │   ├── export/PdfExportService.java
│       │   ├── export/DocxExportService.java
│       │   ├── parser/ResumeParserService.java
│       │   └── storage/S3FileStorageService.java
│       ├── job/infrastructure/
│       │   ├── persistence/entity/JobListingEntity.java
│       │   ├── persistence/repository/JobListingJpaRepository.java
│       │   ├── persistence/repository/JobListingRepositoryImpl.java
│       │   ├── adapter/IndeedAdapter.java
│       │   ├── adapter/LinkedInAdapter.java
│       │   ├── adapter/GoogleJobsAdapter.java
│       │   ├── adapter/GlassdoorAdapter.java
│       │   └── scheduler/JobAggregationScheduler.java
│       ├── application/infrastructure/
│       │   ├── persistence/entity/ApplicationEntity.java
│       │   └── persistence/repository/ApplicationJpaRepository.java
│       ├── interview/infrastructure/
│       ├── company/infrastructure/
│       ├── analytics/infrastructure/
│       ├── notification/infrastructure/
│       │   ├── email/SendGridEmailSender.java
│       │   ├── push/WebPushSender.java
│       │   └── scheduler/DigestScheduler.java
│       ├── automation/infrastructure/
│       │   └── (empty — automation is separate deployable)
│       ├── ai/infrastructure/
│       │   ├── adapter/OpenAiAdapter.java
│       │   ├── adapter/AnthropicAdapter.java
│       │   ├── adapter/OllamaAdapter.java
│       │   ├── adapter/GeminiAdapter.java
│       │   ├── aspect/CircuitBreakerAspect.java
│       │   └── config/AiProviderConfig.java
│       ├── billing/infrastructure/
│       │   ├── stripe/StripePaymentGateway.java
│       │   └── webhook/StripeWebhookHandler.java
│       ├── admin/infrastructure/
│       │   └── (minimal — mostly queries)
│       └── shared/
│           ├── persistence/BaseJpaEntity.java    # @MappedSuperclass
│           ├── persistence/OutboxPoller.java
│           ├── messaging/KafkaEventPublisher.java
│           ├── messaging/KafkaConfig.java
│           ├── cache/RedisCacheService.java
│           ├── cache/RedisRateLimiter.java
│           ├── audit/AuditAspect.java
│           └── config/
│               ├── FlywayConfig.java
│               ├── RedisConfig.java
│               └── SchedulingConfig.java
│
├── jobpilot-interfaces/                       # ── PRESENTATION ──
│   ├── pom.xml
│   └── src/main/java/com/jobpilot/modules/
│       ├── identity/interfaces/
│       │   ├── rest/AuthController.java
│       │   ├── rest/UserProfileController.java
│       │   ├── dto/request/RegisterRequest.java
│       │   ├── dto/response/AuthResponse.java
│       │   └── mapper/AuthDtoMapper.java
│       ├── resume/interfaces/
│       │   ├── rest/ResumeController.java
│       │   ├── rest/CoverLetterController.java
│       │   └── dto/...
│       ├── job/interfaces/
│       │   ├── rest/JobDiscoveryController.java
│       │   └── rest/JobMatchingController.java
│       ├── application/interfaces/
│       │   ├── rest/ApplicationController.java
│       │   └── websocket/ApplicationWebSocketHandler.java
│       ├── interview/interfaces/
│       ├── company/interfaces/
│       ├── analytics/interfaces/
│       ├── notification/interfaces/
│       ├── admin/interfaces/
│       │   └── rest/AdminController.java
│       ├── billing/interfaces/
│       │   └── rest/BillingController.java
│       └── shared/
│           └── handler/GlobalExceptionHandler.java
│
├── jobpilot-gateway/                          # ── API GATEWAY ──
│   ├── pom.xml
│   └── src/main/java/com/jobpilot/gateway/
│       ├── config/
│       │   ├── RouteConfig.java
│       │   ├── CorsConfig.java
│       │   └── RateLimitConfig.java
│       ├── filter/
│       │   ├── JwtAuthFilter.java
│       │   ├── RateLimitFilter.java
│       │   └── RequestLoggingFilter.java
│       └── GatewayApplication.java
│
├── jobpilot-automation/                       # ── BROWSER AUTOMATION ──
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/jobpilot/automation/
│       ├── orchestrator/
│       │   ├── AutomationOrchestrator.java
│       │   └── state/SessionStateMachine.java
│       ├── browser/
│       │   ├── PlaywrightBrowserEngine.java
│       │   ├── FormDetectionEngine.java
│       │   └── AtsFormDetector.java
│       ├── adapter/
│       │   ├── GreenhouseAdapter.java
│       │   ├── LeverAdapter.java
│       │   ├── WorkdayAdapter.java
│       │   └── GenericAdapter.java
│       ├── proxy/
│       │   └── SimpleProxyManager.java
│       ├── consumer/
│       │   └── AutomationKafkaConsumer.java
│       └── Application.java
│
├── jobpilot-ai-service/                       # ── AI ORCHESTRATION ──
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/jobpilot/ai/
│       ├── orchestrator/
│       │   ├── AiOrchestrationService.java
│       │   └── ProviderSelector.java
│       ├── adapter/
│       │   ├── OpenAiAdapter.java
│       │   ├── AnthropicAdapter.java
│       │   ├── OllamaAdapter.java
│       │   └── GeminiAdapter.java
│       ├── cache/
│       │   └── AiCacheService.java
│       ├── tracking/
│       │   └── TokenTracker.java
│       ├── consumer/
│       │   └── AiKafkaConsumer.java
│       └── config/
│           └── CircuitBreakerConfig.java
│
└── jobpilot-bootstrap/                        # ── BOOTSTRAP ──
    ├── pom.xml
    └── src/main/java/com/jobpilot/bootstrap/
        ├── JobPilotApplication.java            # @SpringBootApplication
        └── config/
            ├── ModuleConfig.java               # @Import all module configs
            └── OpenApiConfig.java              # SpringDoc / OpenAPI 3.1
```

---

## 6. Naming Standards

### 6.1 Class Naming

| Element | Pattern | Example |
|---------|---------|---------|
| Domain Entity | Noun (singular) | `User`, `Resume`, `Application` |
| Aggregate Root | Noun (singular, context name) | `Resume` (in resume context), `Application` (in ATS context) |
| Value Object | Noun describing the value | `Email`, `UserId`, `Money`, `AtsScore` |
| Domain Event | `{PastTenseVerb}Event` | `UserRegisteredEvent`, `ApplicationSubmittedEvent` |
| Domain Service | `{Noun}DomainService` | `ResumeScoringDomainService` |
| Use Case Interface | `{Verb}UseCase` | `RegisterUserUseCase`, `SubmitApplicationUseCase` |
| Application Service | `{Context}ApplicationService` | `AuthApplicationService` |
| Inbound Port | `{Verb}UseCase` | `CreateResumeUseCase` |
| Outbound Port | `{Noun}{Role}Port` | `NotificationPort`, `UserRepository` |
| Repository Interface | `{Aggregate}Repository` | `UserRepository`, `ResumeRepository` |
| JPA Repository | `{Entity}JpaRepository` | `UserJpaRepository` |
| Repository Impl | `{Aggregate}RepositoryImpl` | `UserRepositoryImpl` |
| JPA Entity | `{DomainEntity}Entity` | `UserEntity`, `ResumeEntity` |
| Controller | `{Noun}Controller` | `AuthController`, `ResumeController` |
| Request DTO | `{Action}Request` | `RegisterRequest`, `CreateResumeRequest` |
| Response DTO | `{Noun}Response` | `AuthResponse`, `ResumeResponse` |
| Command DTO | `{Verb}Command` | `RegisterUserCommand`, `TailorResumeCommand` |
| Entity → Domain Mapper | `{Entity}Mapper` | `UserEntityMapper` |
| DTO Mapper | `{Context}DtoMapper` | `AuthDtoMapper` |
| Exception | `{Error}Exception` | `EmailAlreadyExistsException` |
| Configuration | `{Module}Config` | `SecurityConfig`, `RedisConfig` |
| Adapter | `{Provider}Adapter` | `OpenAiAdapter`, `IndeedAdapter` |

### 6.2 Package Naming

```
com.jobpilot.modules.{context-name}.{layer}
                            ↓              ↓
                     identity, resume    domain, application,
                     job, application    infrastructure, interfaces
```

### 6.3 Method Naming

```
Commands:     create{Resource}, update{Resource}, delete{Resource}, submit{Resource}
Queries:      find{Resource}ById, find{Resource}sBy{Field}, exists{Resource}
Actions:      generate{Thing}, calculate{Thing}, validate{Thing}, export{Thing}
Events:       handle{EventName}
Port methods: findBy{Field}, save, delete, exists
```

### 6.4 Resource Naming (REST)

```
Plural nouns:          /api/v1/users, /api/v1/resumes, /api/v1/applications
Nested resources:      /api/v1/applications/{id}/notes, /api/v1/resumes/{id}/versions
Actions (non-CRUD):    /api/v1/resumes/{id}/tailor, /api/v1/resumes/{id}/score
                       /api/v1/applications/{id}/automate
```

---

## 7. Configuration Management

### 7.1 Profile Strategy

| Profile | Purpose | Configuration Source |
|---------|---------|---------------------|
| `local` | Local development | `application-local.yml` + Docker Compose |
| `dev` | Development/Integration | `application-dev.yml` + env vars |
| `staging` | Pre-production | `application-staging.yml` + Vault |
| `prod` | Production | `application-prod.yml` + Vault |

### 7.2 Configuration Files

```
src/main/resources/
├── application.yml                     # Default config
├── application-local.yml               # Local overrides
├── application-dev.yml                 # Development
├── application-staging.yml             # Staging
├── application-prod.yml                # Production
├── db/migration/                       # Flyway migrations
│   ├── V1__init_schema.sql
│   ├── V2__add_embeddings.sql
│   └── ...
└── prompts/                            # Default prompt templates (seeded)
    ├── resume-tailoring.md
    ├── resume-scoring.md
    ├── cover-letter.md
    ├── interview-questions.md
    └── career-path.md
```

### 7.3 Secrets Management

```
NEVER in config files:
- Database passwords
- API keys (OpenAI, Anthropic, Stripe, SendGrid)
- JWT signing keys
- OAuth client secrets

USE: HashiCorp Vault / AWS Secrets Manager
Access: @Value("${vault://secret/jobpilot/db/password}")
```

---

## 8. Dependency Injection Conventions

| Convention | Rule |
|------------|------|
| Constructor injection | Always prefer constructor injection over field injection |
| `final` fields | All injected dependencies are `private final` |
| `@RequiredArgsConstructor` | Use Lombok for constructor generation |
| Interface injection | Inject port interfaces, not concrete implementations |
| `@Qualifier` | Only when multiple implementations of same interface |
| Configuration classes | `@Configuration` classes for `@Bean` definitions |
| No `@Autowired` on fields | Never. Constructor injection only. |

---

## 9. Exception Handling Conventions

### 9.1 Exception Hierarchy

```
BaseException (extends RuntimeException)
├── DomainException
│   ├── EmailAlreadyExistsException
│   ├── UserNotFoundException
│   ├── InvalidStatusTransitionException
│   └── ApplicationNotFoundException
├── ApplicationException
│   ├── AuthenticationException
│   ├── AuthorizationException
│   └── TokenExpiredException
└── InfrastructureException
    ├── AiProviderUnavailableException
    ├── EmailSendFailedException
    └── FileStorageException
```

### 9.2 Exception to HTTP Status Mapping

| Exception | HTTP Status | Error Code |
|-----------|-------------|------------|
| `DomainException` (validation) | 400 | `VALIDATION_ERROR` |
| `EmailAlreadyExistsException` | 409 | `CONFLICT` |
| `AuthenticationException` | 401 | `AUTHENTICATION_ERROR` |
| `AuthorizationException` | 403 | `AUTHORIZATION_ERROR` |
| `NotFoundException` | 404 | `RESOURCE_NOT_FOUND` |
| `InvalidStatusTransitionException` | 409 | `INVALID_STATE_TRANSITION` |
| `AiProviderUnavailableException` | 503 | `SERVICE_UNAVAILABLE` |
| `RateLimitExceededException` | 429 | `RATE_LIMIT_EXCEEDED` |

---

## 10. Coding Conventions

### 10.1 Language Level

```
- Java 21 features REQUIRED:
  - Records for value objects and DTOs
  - Pattern matching for instanceof
  - Sealed classes for discriminated unions
  - Text blocks for multi-line strings
  - Virtual threads (where appropriate)
  - Sequenced collections (List.reversed(), etc.)

- Java 21 features PREFERRED:
  - Switch expressions (with pattern matching)
  - Helpful NullPointerExceptions

- Java 21 features AVOID:
  - StringTemplate (preview)
  - ScopedValue (preview — stick with MDC for now)
```

### 10.2 Code Style

```
- Indentation: 4 spaces (no tabs)
- Line length: 120 characters max
- Braces: Egyptian style (opening brace on same line)
- Imports: explicit (no wildcard imports)
- Blank line between methods
- Javadoc: on all public APIs, domain entities, and complex logic
- No `System.out.println` (use Logger)
- No magic strings/numbers (use constants or enums)
```

### 10.3 Testing Conventions

```
Test class name: {ClassUnderTest}Test
Test method name: {methodName}_{scenario}_{expectedResult}
Location: src/test/java (mirrors src/main/java structure)

Framework: JUnit 5 + AssertJ + Mockito
Integration: Testcontainers (PostgreSQL, Redis, Kafka)
Architecture: ArchUnit tests in dedicated test class
```

---

*This Backend Foundation document defines the complete project structure, package organization, naming conventions, dependency rules, and coding standards. Every subsequent phase must conform to these conventions. There is zero business logic — only architectural skeleton.*

---

**End of Backend Foundation v1.0**
