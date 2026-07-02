# JobPilot AI — Backend Implementation Plan

**Version:** 1.0  
**Status:** Draft  
**Phase:** 29 of 35  
**Author:** Chief Software Architect  

---

## 1. Build Order

```
Iteration 1 — Foundation (files: ~60)
  ├── Maven multi-module POM
  ├── Common module (shared VOs, exceptions, constants)
  ├── Domain module (entities, value objects, ports)
  ├── Persistence module (JPA entities, repositories, Flyway migrations)
  ├── Config module (security, caching, Jackson, async, CORS)
  └── Application entry point (SpringBootApplication)

Iteration 2 — Identity & User (files: ~40)
  ├── Auth REST controllers + services
  ├── OAuth 2.0 adapters
  ├── User module (domain + app + infra)
  ├── Profile module
  └── Settings module

Iteration 3 — Resume & Content (files: ~35)
  ├── Resume Studio
  ├── Cover Letter Engine
  ├── ATS Optimizer
  └── AI Provider Layer + Prompt Engine

Iteration 4 — Job & Application (files: ~45)
  ├── Job Discovery (adapters, aggregation)
  ├── Job Matching Engine
  ├── Application Tracker
  ├── Browser Automation
  └── Interview Hub

Iteration 5 — Cross-Cutting (files: ~30)
  ├── Company Intelligence
  ├── Career Analytics
  ├── Notification module
  ├── Admin module
  ├── Search Engine
  └── Auditing + Rate Limiting
```

---

## 2. Module File Counts

```
Module                     │ Java Files │ Test Files
───────────────────────────┼────────────┼────────────
common                     │    8       │     2
domain                     │    12      │     6
persistence                │    26      │     10
config                     │    15      │     0
auth                       │    12      │     8
user                       │    14      │     10
profile                    │     8      │     6
resume                     │    18      │     14
cover-letter               │    10      │     8
ats-optimizer              │     8      │     6
ai-provider                │    20      │     14
prompt-engine              │    12      │     8
job-discovery              │    18      │     10
job-matching               │     8      │     6
application-tracker        │    14      │     10
browser-automation         │    16      │     8
interview-hub              │    12      │     8
company-intelligence       │    12      │     6
career-analytics           │    10      │     6
notification               │    10      │     6
admin                      │     6      │     4
search                     │     6      │     4
───────────────────────────┼────────────┼────────────
Total                      │   275      │    140
```

---

## 3. Directory Structure

```
jobpilot-api/
  ├── pom.xml (parent)
  ├── jobpilot-common/
  │   └── src/main/java/com/jobpilot/common/
  ├── jobpilot-domain/
  │   └── src/main/java/com/jobpilot/domain/
  ├── jobpilot-persistence/
  │   └── src/main/java/com/jobpilot/persistence/
  ├── jobpilot-config/
  │   └── src/main/java/com/jobpilot/config/
  ├── jobpilot-module-auth/
  ├── jobpilot-module-user/
  ├── jobpilot-module-profile/
  ├── jobpilot-module-resume/
  ├── jobpilot-module-cover-letter/
  ├── jobpilot-module-ats/
  ├── jobpilot-module-ai-provider/
  ├── jobpilot-module-prompt-engine/
  ├── jobpilot-module-job-discovery/
  ├── jobpilot-module-job-matching/
  ├── jobpilot-module-application/
  ├── jobpilot-module-automation/
  ├── jobpilot-module-interview/
  ├── jobpilot-module-company/
  ├── jobpilot-module-analytics/
  ├── jobpilot-module-notification/
  ├── jobpilot-module-admin/
  ├── jobpilot-module-search/
  └── jobpilot-bootstrap/
      └── src/main/java/com/jobpilot/JobPilotApplication.java
```

---

## 4. Iteration 1 Files (Exact List)

```
pom.xml                          — Parent POM (modules, dependency management)
jobpilot-common/pom.xml
jobpilot-common/src/main/java/com/jobpilot/common/
  ├── exception/
  │   ├── BaseException.java
  │   ├── NotFoundException.java
  │   ├── InvalidTransitionException.java
  │   ├── DuplicateException.java
  │   ├── RateLimitException.java
  │   └── UnauthorizedException.java
  ├── model/
  │   ├── PageResponse.java
  │   ├── ApiResponse.java
  │   └── PaginatedQuery.java
  ├── util/
  │   └── ValidationUtils.java
  └── constant/
      ├── AppConstants.java
      └── PermissionConstants.java

jobpilot-domain/pom.xml
jobpilot-domain/src/main/java/com/jobpilot/domain/
  ├── user/
  │   ├── User.java
  │   ├── UserRole.java
  │   └── UserTier.java
  ├── resume/
  │   ├── Resume.java
  │   ├── ResumeSection.java
  │   └── SectionType.java
  ├── application/
  │   ├── Application.java
  │   └── ApplicationStatus.java
  ├── shared/
  │   ├── BaseAggregateRoot.java
  │   ├── BaseEntity.java
  │   └── BaseValueObject.java
  └── event/
      ├── DomainEvent.java
      └── DomainEventPublisher.java

jobpilot-persistence/pom.xml
jobpilot-persistence/src/main/java/com/jobpilot/persistence/
  ├── entity/ (JPA entities matching domain)
  ├── repository/ (Spring Data JPA repos)
  ├── mapper/ (Entity ↔ Domain)
  ├── config/FlywayConfig.java
  └── migration/ (V1__init.sql through V10__)

jobpilot-config/pom.xml
jobpilot-config/src/main/java/com/jobpilot/config/
  ├── SecurityConfig.java
  ├── JwtConfig.java
  ├── RedisConfig.java
  ├── CorsConfig.java
  ├── AsyncConfig.java
  ├── JacksonConfig.java
  ├── RateLimitConfig.java
  └── OpenApiConfig.java

jobpilot-bootstrap/pom.xml
jobpilot-bootstrap/src/main/java/com/jobpilot/
  ├── JobPilotApplication.java
  ├── JobPilotProperties.java
  └── resources/
      ├── application.yml
      ├── application-dev.yml
      ├── application-staging.yml
      └── application-prod.yml
```

---

**End of Backend Implementation Plan v1.0**
