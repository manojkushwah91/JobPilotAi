# JobPilot AI v2.0 — Testing Strategy

**Version:** 2.0  
**Status:** Draft  
**Product:** JobPilot AI — "Offline-First Autonomous AI Job Agent"  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Test Pyramid
2. Unit Testing
3. Integration Testing
4. E2E Testing
5. Agent Testing
6. Browser Automation Testing
7. AI Provider Testing
8. ArchUnit Tests
9. Test Data Strategy
10. CI/CD Pipeline
11. Test Coverage Goals

---

## 1. Test Pyramid

```
         ╱╲
        ╱ E2E ╲               Playwright (5-10 tests per critical flow)
       ╱────────╲
      ╱ Integration ╲         SpringBootTest + TestContainers (100+ tests)
     ╱──────────────╲
    ╱   Unit Test     ╲       JUnit 5 + Mockito (1000+ tests)
   ╱──────────────────╲
```

---

## 2. Unit Testing

### 2.1 Framework

- **Framework:** JUnit 5 + Mockito + AssertJ
- **Coverage Target:** 90%+ for domain logic, 80% overall (JaCoCo)

### 2.2 Structure per Class

```java
class AgentLoopTest {
    @Test
    void shouldExecuteLoopWhenAgentIsRunning() { ... }

    @Test
    void shouldPauseLoopWhenAgentIsPaused() { ... }

    @Test
    void shouldStopLoopWhenMissionCompleted() { ... }

    @ParameterizedTest
    @CsvSource(...)
    void shouldHandlePhaseCorrectly(String phase, String expected) { ... }

    @Nested
    class EdgeCases {
        @Test void whenAgentNotInitialized() { ... }
        @Test void whenTaskQueueEmpty() { ... }
        @Test void whenAiProviderUnavailable() { ... }
    }
}
```

### 2.3 Naming Convention

`{method}Should{expected}[When{condition}]`

Examples:
- `executeLoopShouldRunWhenAgentIsRunning`
- `pauseLoopShouldStopWhenAgentIsPaused`
- `planPhaseShouldCreateTasksWhenReasoningComplete`

### 2.4 Mocking Rules

- Mock external ports (interfaces only)
- Never mock domain entities or value objects
- Use @InjectMocks for SUT, @Mock for dependencies
- Mock AI provider for deterministic tests

### 2.5 Domain Layer Tests

**Focus:** Business logic, domain rules, invariants

```java
class MissionTest {
    @Test
    void shouldStartWhenStatusIsDraft() {
        var mission = Mission.create(command);
        mission.start();
        assertThat(mission.status()).isEqualTo(MissionStatus.ACTIVE);
    }

    @Test
    void shouldThrowWhenStartFromInvalidStatus() {
        var mission = Mission.create(command);
        mission.complete();
        assertThatThrownBy(() -> mission.start())
            .isInstanceOf(InvalidMissionStatusTransitionException.class);
    }
}
```

### 2.6 Agent Runtime Tests

**Focus:** Agent loop phases, tool execution, memory operations

```java
class AgentLoopTest {
    @Mock
    private ObservePhase observePhase;
    
    @Mock
    private ThinkPhase thinkPhase;
    
    @InjectMocks
    private AgentLoop agentLoop;
    
    @Test
    void shouldExecuteAllPhases() {
        // Given
        var agentState = AgentState.create(userId, missionId);
        
        // When
        agentLoop.executeLoop(agentState);
        
        // Then
        verify(observePhase).execute(agentState);
        verify(thinkPhase).execute(agentState);
        // ... verify other phases
    }
}
```

---

## 3. Integration Testing

### 3.1 Framework

- **Framework:** @SpringBootTest + @TestContainers
- **Databases:** PostgreSQL (testcontainers), Redis (testcontainers)
- **AI Provider:** Mock AI provider (WireMock)

### 3.2 Categories

**Repository Tests (30+):**
- CRUD operations
- Custom queries (@Query)
- Full-text search (tsvector)
- Pagination + sorting
- Partitioned table queries

**Controller Tests (50+):**
- @WebMvcTest with @MockBean services
- JSON request/response validation
- Validation error responses
- Security: auth required, role-based access
- Pagination response structure

**Service Tests with Real DB (30+):**
- Transactional boundaries
- Event publishing (verify outbox entries)
- Concurrency (simultaneous agent loop executions)
- Status state machine transitions

**AI Provider Integration Tests (10+):**
- WireMock for Ollama/OpenAI endpoints
- Circuit breaker behavior
- Cache hit/miss verification
- Fallback provider selection

**Agent Runtime Integration Tests (20+):**
- Agent loop execution with real database
- Task queue operations
- Memory persistence
- WebSocket message publishing

### 3.3 Test Configuration

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:tc:postgresql:16:///jobpilot
  redis:
    host: localhost
    port: 6379
  jpa:
    hibernate:
      ddl-auto: none

ai:
  provider:
    default: mock
    mock:
      enabled: true
```

### 3.4 Test Example

```java
@SpringBootTest
@Testcontainers
class MissionServiceIntegrationTest {
    
    @.container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16");
    
    @container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine");
    
    @Autowired
    private MissionService missionService;
    
    @Autowired
    private MissionRepository missionRepository;
    
    @Test
    void shouldCreateAndRetrieveMission() {
        // Given
        var command = CreateMissionCommand.builder()
            .userId(userId)
            .name("Java Backend Hunt")
            .goal("Get a Java Backend job")
            .build();
        
        // When
        var mission = missionService.create(command);
        
        // Then
        var retrieved = missionRepository.findById(mission.id());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().name()).isEqualTo("Java Backend Hunt");
    }
}
```

---

## 4. E2E Testing

### 4.1 Framework

- **Framework:** Playwright (TypeScript)
- **Environment:** Staging (isolated QA deployment)
- **Browser:** Chromium (headless)

### 4.2 Test Scenarios

**Critical Paths:**

1. **Complete Agent Journey:**
   - Register → Login → Upload Resume → Create Mission → Start Agent → Monitor Progress → Stop Agent

2. **Mission Management:**
   - Create Mission → Edit Mission → Start Mission → Pause Mission → Resume Mission → Stop Mission

3. **Candidate Profile:**
   - Upload Resume → Parse Resume → Edit Profile → Add Skills → Save

4. **Chat Control:**
   - Start Agent → Send Chat Command → Verify Command Executed

5. **Application Tracking:**
   - View Applications → Filter by Status → View Application Details → View Screenshot

### 4.3 CI Pipeline

```yaml
# GitHub Actions
name: E2E Tests

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  e2e:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Install dependencies
        run: cd frontend && npm ci
      - name: Run E2E tests
        run: cd frontend && npm run test:e2e
```

---

## 5. Agent Testing

### 5.1 Agent Loop Testing

**Focus:** Agent loop execution, phase transitions, error handling

```java
@SpringBootTest
class AgentLoopIntegrationTest {
    
    @Test
    void shouldExecuteCompleteLoop() {
        // Given
        var mission = createActiveMission();
        var agentState = AgentState.create(userId, mission.id());
        
        // When
        agentLoop.executeLoop(agentState);
        
        // Then
        assertThat(agentState.loopIteration()).isEqualTo(1);
        assertThat(agentState.status()).isEqualTo(AgentStatus.RUNNING);
    }
    
    @Test
    void shouldPauseOnCaptchaDetection() {
        // Given
        var mission = createActiveMission();
        var agentState = AgentState.create(userId, mission.id());
        
        // Mock browser tool to detect CAPTCHA
        when(browserTool.execute(any())).thenReturn(
            ToolResult.failure().add("captchaDetected", true)
        );
        
        // When
        agentLoop.executeLoop(agentState);
        
        // Then
        assertThat(agentState.status()).isEqualTo(AgentStatus.AWAITING_USER);
    }
}
```

### 5.2 Tool Testing

**AI Tools:**
- Mock AI provider for deterministic tests
- Verify correct prompt construction
- Verify response parsing

**Browser Tools:**
- Mock Playwright for fast tests
- Verify correct selector usage
- Verify error handling

**Discovery Tools:**
- Mock site adapters
- Verify deduplication logic
- Verify filtering

### 5.3 Memory Testing

```java
@SpringBootTest
class MemoryServiceIntegrationTest {
    
    @Test
    void shouldStoreAndRetrieveMemory() {
        // Given
        var memory = Memory.create(userId, MemoryType.PREFERENCE, "key", "value");
        
        // When
        memoryService.store(memory);
        var retrieved = memoryService.retrieve(userId, MemoryType.PREFERENCE, "key");
        
        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().value()).isEqualTo("value");
    }
    
    @Test
    void shouldArchiveLowConfidenceMemories() {
        // Given
        var memory = Memory.create(userId, MemoryType.PREFERENCE, "key", "value")
            .withConfidence(0.3);
        
        // When
        memoryService.archiveLowConfidenceMemories();
        
        // Then
        var retrieved = memoryService.retrieve(userId, MemoryType.PREFERENCE, "key");
        assertThat(retrieved).isEmpty();
    }
}
```

---

## 6. Browser Automation Testing

### 6.1 Adapter Testing

**Focus:** Site adapter logic, selector correctness, error handling

```java
@SpringBootTest
class LinkedInAdapterTest {
    
    @Mock
    private BrowserManager browserManager;
    
    @InjectMocks
    private LinkedInAdapter linkedInAdapter;
    
    @Test
    void shouldSearchJobs() {
        // Given
        var criteria = SearchCriteria.builder()
            .roles(List.of("Java Backend"))
            .locations(List.of("Remote"))
            .build();
        
        var mockPage = mock(Page.class);
        when(browserManager.getPage(any())).thenReturn(mockPage);
        when(mockPage.evaluate(anyString())).thenReturn(mockJobsJson);
        
        // When
        var jobs = linkedInAdapter.searchJobs(criteria);
        
        // Then
        assertThat(jobs).hasSize(10);
        assertThat(jobs.get(0).title()).contains("Java");
    }
    
    @Test
    void shouldDetectCaptcha() {
        // Given
        var mockPage = mock(Page.class);
        when(browserManager.getPage(any())).thenReturn(mockPage);
        when(mockPage.locator("#captcha-challenge")).thenReturn(mockLocator);
        when(mockLocator.count()).thenReturn(1);
        
        // When
        var captchaDetected = linkedInAdapter.detectCaptcha();
        
        // Then
        assertThat(captchaDetected).isTrue();
    }
}
```

### 6.2 Form Engine Testing

```java
@SpringBootTest
class FormEngineTest {
    
    @Test
    void shouldFillFormCorrectly() {
        // Given
        var mockPage = mock(Page.class);
        var data = ApplicationData.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();
        
        // When
        formEngine.fillForm(mockPage, data);
        
        // Then
        verify(mockPage).fill("#first-name", "John");
        verify(mockPage).fill("#last-name", "Doe");
        verify(mockPage).fill("#email", "john@example.com");
    }
}
```

---

## 7. AI Provider Testing

### 7.1 Ollama Provider Testing

**Unit Tests:**
- Test request construction
- Test response parsing
- Test error handling

**Integration Tests:**
- Test with real Ollama (optional, requires Ollama running)
- Test model selection
- Test streaming responses

```java
@SpringBootTest
class OllamaProviderIntegrationTest {
    
    @Autowired
    private OllamaProvider ollamaProvider;
    
    @Test
    @EnabledIfEnvironmentVariable(named = "OLLAMA_ENABLED", matches = "true")
    void shouldGenerateText() {
        // Given
        var request = AiRequest.builder()
            .systemPrompt("You are a helpful assistant")
            .userPrompt("What is 2+2?")
            .build();
        
        // When
        var response = ollamaProvider.generateText(request);
        
        // Then
        assertThat(response.content()).isNotEmpty();
    }
}
```

### 7.2 Mock AI Provider

For deterministic tests, use a mock AI provider:

```java
@Component
@Profile("test")
public class MockAiProvider implements AiProvider {
    
    @Override
    public AiResponse generateText(AiRequest request) {
        return AiResponse.builder()
            .content("Mock response")
            .model("mock-model")
            .promptTokens(10)
            .completionTokens(20)
            .build();
    }
    
    // ... other methods
}
```

---

## 8. ArchUnit Tests

### 8.1 Architecture Rules

```java
@AnalyzeClasses(packages = "com.jobpilot")
public class ArchitectureTest {
    
    @ArchTest
    static final ArchRule domain_layer_should_not_depend_on_spring =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..");
    
    @ArchTest
    static final ArchRule domain_layer_should_not_depend_on_infrastructure =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
    
    @ArchTest
    static final ArchRule agent_runtime_should_not_depend_on_interfaces =
        noClasses().that().resideInAPackage("..agent..")
            .should().dependOnClassesThat().resideInAPackage("..interfaces..");
    
    @ArchTest
    static final ArchRule controllers_must_be_in_rest_package =
        classes().that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..interfaces.rest..");
    
    @ArchTest
    static final ArchRule tools_must_implement_tool_interface =
        classes().that().haveSimpleNameEndingWith("Tool")
            .should().implement(Tool.class);
    
    @ArchTest
    static final ArchRule no_circular_dependencies =
        slices().matching("com.jobpilot.(*)..")
            .should().beFreeOfCycles();
}
```

---

## 9. Test Data Strategy

### 9.1 Unit Tests

**Builder Pattern Test Factories:**

```java
class TestDataFactory {
    
    static MissionBuilder aMission() {
        return Mission.builder()
            .id(MissionId.generate())
            .userId(UUID.randomUUID())
            .name("Test Mission")
            .goal("Test goal");
    }
    
    static CandidateProfileBuilder aCandidate() {
        return CandidateProfile.builder()
            .id(CandidateId.generate())
            .userId(UUID.randomUUID())
            .fullName("John Doe")
            .email("john@example.com");
    }
    
    static JobListingBuilder aJob() {
        return JobListing.builder()
            .id(JobId.generate())
            .source("linkedin")
            .sourceId("12345")
            .title("Java Backend Developer")
            .companyName("Test Company");
    }
}
```

### 9.2 Integration Tests

**SQL Scripts:**

```sql
-- src/test/resources/db/V900__test_data.sql
INSERT INTO users (id, email, password_hash, role) VALUES
('00000000-0000-0000-0000-000000000001', 'test@example.com', '$2a$10$...', 'USER');

INSERT INTO missions (id, user_id, name, goal, status) VALUES
('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'Test Mission', 'Test goal', 'DRAFT');
```

### 9.3 E2E Tests

**API Calls:**

```typescript
// Setup test data via API
await request(app.getHttpServer())
  .post('/api/v1/missions')
  .set('Authorization', `Bearer ${token}`)
  .send(testMission);

// Cleanup
await request(app.getHttpServer())
  .delete(`/api/v1/missions/${missionId}`)
  .set('Authorization', `Bearer ${token}`);
```

---

## 10. CI/CD Pipeline

### 10.1 GitHub Actions

```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  test:
    runs-on; ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Run unit tests
        run: cd backend && mvn test
      
      - name: Run integration tests
        run: cd backend && mvn verify -P integration
      
      - name: Run ArchUnit tests
        run: cd backend && mvn test -Dtest=ArchitectureTest
      
      - name: Checkstyle
        run: cd backend && mvn checkstyle:check
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: backend/target/site/jacoco/jacoco.xml
  
  frontend-test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install dependencies
        run: cd frontend && npm ci
      
      - name: Run unit tests
        run: cd frontend && npm test
      
      - name: Run E2E tests
        run: cd frontend && npm run test:e2e
```

---

## 11. Test Coverage Goals

### 11.1 Coverage Targets

| Layer | Target Coverage |
|-------|----------------|
| Domain | 95%+ |
| Application | 85%+ |
| Infrastructure | 75%+ |
| Interfaces | 70%+ |
| Agent Runtime | 90%+ |
| AI Provider | 80%+ |
| Browser Automation | 75%+ |
| Overall | 80%+ |

### 11.2 Coverage Exclusions

- Generated code (Lombok, MapStruct)
- Configuration classes
- Exception classes
- DTOs (data transfer objects)

---

## 12. Performance Testing

### 12.1 Load Testing

**Tools:** k6, JMeter

**Scenarios:**
- 100 concurrent users creating missions
- 50 concurrent agent loops running
- 1000 job listings being analyzed

**Metrics:**
- API response time (p95 < 2s)
- Agent loop iteration time (p95 < 30s)
- Database query time (p95 < 100ms)

---

**End of Testing Strategy v2.0**
