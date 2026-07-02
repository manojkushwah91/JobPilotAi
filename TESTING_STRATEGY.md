# JobPilot AI — Testing Strategy

**Version:** 1.0  
**Status:** Draft  
**Phase:** 28 of 35  
**Author:** Chief Software Architect  

---

## 1. Test Pyramid

```
         ╱╲
        ╱ E2E ╲               Playwright Java (5-10 tests per critical flow)
       ╱────────╲
      ╱ Integration ╲         SpringBootTest + @TestContainers (200+ tests)
     ╱──────────────╲
    ╱   Unit Test     ╲       JUnit 5 + Mockito (1500+ tests)
   ╱──────────────────╲
```

---

## 2. Unit Testing

```
Framework: JUnit 5 + Mockito + AssertJ
Coverage target: 90%+ for domain logic, 80% overall (JaCoCo)

Structure per class:
  class MyServiceTest {
    @Test
    void shouldCreateApplicationWhenValidInput() { ... }

    @Test
    void shouldThrowWhenInvalidTransition() { ... }

    @Test
    void shouldRejectWhenDuplicate() { ... }

    @ParameterizedTest
    @CsvSource(....)
    void shouldComputeScoreCorrectly(type, expected) { ... }

    @Nested
    class EdgeCases {
      @Test void whenNullInput() { ... }
      @Test void whenEmptyList() { ... }
      @Test void whenConcurrentAccess() { ... }
    }
  }

Naming convention: {method}Should{expected}[When{condition}]
Examples:
  - createShouldSucceedWhenValidInput
  - updateStatusShouldThrowWhenInvalidTransition
  - calculateScoreShouldReturnZeroWhenNoKeywords

Mocking rules:
  - Mock external ports (interfaces only)
  - Never mock domain entities or value objects
  - Use @InjectMocks for SUT, @Mock for dependencies
```

---

## 3. Integration Testing

```
Framework: @SpringBootTest + @TestContainers
Databases: PostgreSQL (testcontainers), Redis (testcontainers)
Broker: RabbitMQ (testcontainers with management)

Categories:
  ├── Repository tests (30+)
  │   - CRUD operations
  │   - Custom queries (@Query)
  │   - Full-text search (tsvector)
  │   - Pagination + sorting
  │
  ├── Controller tests (50+)
  │   - @WebMvcTest with @MockBean services
  │   - JSON request/response validation
  │   - Validation error responses
  │   - Security: auth required, role-based access
  │   - Pagination response structure
  │
  ├── Service tests with real DB (30+)
  │   - Transactional boundaries
  │   - Event publishing (verify Outbox entries)
  │   - Concurrency (e.g., simultaneous resume score requests)
  │   - Status state machine transitions
  │
  ├── AI Provider integration (10+)
  │   - WireMock for OpenAI/Anthropic endpoints
  │   - Circuit breaker behavior
  │   - Cache hit/miss verification
  │   - Fallback provider selection
  │
  └── Browser Automation tests (5+)
      - Playwright Java against mock portal (simple HTML form page)
      - CAPTCHA simulation
      - Screenshot capture verification

Test configuration:
  application-test.yml:
    spring.datasource.url: jdbc:tc:postgresql:16:///jobpilot
    spring.redis.host: ${REDIS_HOST:localhost}
```

---

## 4. E2E Testing

```
Framework: Playwright Java
Environment: Staging (isolated QA deployment)

Test scenarios (critical paths):
  1. Registration → login → create resume → score against job → apply → track
  2. OAuth login → generate cover letter → export PDF
  3. Admin login → manage users → view audit log → toggle feature flag
  4. Job discovery → save search → receive notification → apply
  5. Application → trigger automation → verify status updated

CI Pipeline (GitHub Actions):
  ├── lint (Checkstyle, PMD)
  ├── unit-test (mvn test)
  ├── integration-test (mvn verify -P integration)
  ├── e2e-test (Playwright against staging)
  └── security-scan (OWASP Dependency-Check)
```

---

## 5. ArchUnit Tests

```
Rules enforced in CI (ArchUnit):
  - Domain layer must not depend on Spring
  - Domain layer must not depend on Infrastructure
  - Application layer may depend on Domain
  - Infrastructure may depend on Domain + Application
  - Controllers must be in REST package
  - Classes named *Service must be in application package
  - Interfaces named *Port must be in domain.port package
  - No circular dependencies between modules
  - All public methods must have @PreAuthorize (controllers)
  - All aggregate roots must have @AggregateRoot annotation
```

---

## 6. Test Data Strategy

```
Unit tests: Builder pattern test factories (TestDataFactory)
  - TestDataFactory.aUser().withRole(PREMIUM).build()
  - TestDataFactory.aResume().withSections(3).build()

Integration tests: SQL scripts in src/test/resources/db/
  - V900__test_data.sql (Flyway clean migration)

E2E tests: API calls to seed data via admin endpoints
  - POST /api/v1/admin/test/seed
  - POST /api/v1/admin/test/clean

Database per test: Transactional test execution → rollback
  @Transactional on test class (Spring default)
```

---

**End of Testing Strategy v1.0**
