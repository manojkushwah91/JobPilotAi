# JobPilot AI v2.0 — Low Level Design (LLD)

**Version:** 2.0  
**Status:** Draft  
**Product:** JobPilot AI — "Offline-First Autonomous AI Job Agent"  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Package Structure & Naming Conventions
2. Domains & Subdomains Deep Dive
3. Module 1: Agent Runtime (CORE)
4. Module 2: AI Provider Layer
5. Module 3: Browser Automation Framework
6. Module 4: Mission Management
7. Module 5: Candidate Profile
8. Module 6: Job Discovery
9. Module 7: Application Tracking (Read-Only)
10. Module 8: Identity & Access
11. Module 9: Notification Service
12. Cross-Cutting Concerns
13. Sequence Diagrams (Complete)
14. Class Relationship Diagrams
15. Dependency Rules Enforcement

---

## 1. Package Structure & Naming Conventions

### 1.1 Top-Level Maven Modules

```
jobpilot-backend/
  ├── jobpilot-common/              # Shared primitives, utilities, constants
  ├── jobpilot-domain/              # Domain entities, value objects, domain events
  ├── jobpilot-agent-runtime/       # Agent Runtime (NEW - CORE)
  ├── jobpilot-ai-provider/        # AI Provider Layer (NEW)
  ├── jobpilot-browser-automation/ # Browser Automation Framework (NEW)
  ├── jobpilot-application/         # Application services
  ├── jobpilot-infrastructure/      # JPA, Redis, external integrations
  ├── jobpilot-interfaces/          # REST controllers, WebSocket handlers, DTOs
  └── jobpilot-bootstrap/           # Main Spring Boot application entry point
```

### 1.2 Package Naming Convention (Per Module)

```
com.jobpilot.<module-name>.{layer}
```

Layers:
- `domain` — Entities, value objects, aggregates, domain events, domain services, repository ports, domain exceptions
- `application` — Use case interfaces, application services, DTOs, input/output ports, mappers
- `infrastructure` — JPA entities, repository implementations, external API clients, configuration
- `interfaces` — REST controllers, request/response DTOs, WebSocket handlers, exception handlers

### 1.3 Class Naming Conventions

| Type | Suffix | Example |
|------|--------|---------|
| Domain Entity | (none) | `Mission`, `CandidateProfile`, `JobListing` |
| Value Object | (none) | `Money`, `CompatibilityScore`, `MemoryType` |
| Domain Service | `DomainService` | `JobRankingDomainService` |
| Aggregate Root | (none) | `Application` (aggregate of notes, timeline events) |
| Domain Event | `Event` (past tense) | `MissionStartedEvent`, `ApplicationSubmittedEvent` |
| Use Case Interface | `UseCase` | `CreateMissionUseCase`, `StartAgentUseCase` |
| Application Service | `Service` | `MissionService`, `AgentService` |
| Inbound Port (Interface) | `UseCase` or `Command` | `CreateMissionCommand` |
| Outbound Port (Interface) | `Port` or `Repository` | `MissionRepository`, `AiProviderPort` |
| Repository Impl | `Impl` or `JpaRepository` | `MissionRepositoryImpl` |
| REST Controller | `Controller` | `MissionController` |
| Request DTO | `Request` (or `Command`) | `CreateMissionRequest` |
| Response DTO | `Response` | `MissionResponse` |
| Mapper | `Mapper` | `MissionMapper` |
| Exception | `Exception` | `MissionNotFoundException` |
| Configuration | `Config` or `Properties` | `OllamaConfig`, `AgentConfig` |
| Tool | `Tool` | `ResumeParserTool`, `JobAnalyzerTool` |

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
3. agent-runtime → domain  (agent-runtime uses domain entities)
4. agent-runtime → ai-provider  (agent-runtime uses AI provider)
5. agent-runtime → browser-automation  (agent-runtime uses browser automation)
6. No circular dependencies
```

---

## 2. Domains & Subdomains Deep Dive

### 2.1 Agent Runtime Domain (NEW - CORE)

**Purpose:** The heart of the system. Orchestrates the autonomous agent loop.

**Entities:**
- `AgentState` - Current state of the agent
- `Task` - Unit of work for the agent
- `Memory` - Agent's memory (long-term, short-term, knowledge, episode)

**Value Objects:**
- `AgentStatus` - IDLE, RUNNING, PAUSED, STOPPED, ERROR
- `AgentPhase` - OBSERVE, THINK, PLAN, EXECUTE, VERIFY, LEARN
- `TaskType` - DISCOVER_JOBS, ANALYZE_JOB, TAILOR_RESUME, etc.
- `TaskStatus` - PENDING, QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED
- `MemoryType` - PREFERENCE, OBSERVATION, OUTCOME, STRATEGY, KNOWLEDGE

**Domain Services:**
- `AgentLoopDomainService` - Orchestrates the agent loop phases
- `TaskPlanningDomainService` - Plans and prioritizes tasks
- `MemoryDomainService` - Manages memory operations

**Ports (Interfaces):**
- `AgentRepository` - Persist agent state
- `TaskRepository` - Persist tasks
- `MemoryRepository` - Persist memory

### 2.2 Mission Domain (NEW)

**Purpose:** User's job hunting goals.

**Entities:**
- `Mission` - User's job hunting goal (Aggregate Root)

**Value Objects:**
- `MissionStatus` - DRAFT, ACTIVE, PAUSED, COMPLETED, CANCELLED
- `MissionMetrics` - Jobs found, analyzed, applied, interviews, offers
- `Money` - Salary amount with currency
- `ExperienceRange` - Min/max years of experience

**Domain Services:**
- `MissionValidationDomainService` - Validates mission parameters

**Ports (Interfaces):**
- `MissionRepository` - Persist missions

### 2.3 Candidate Profile Domain (NEW)

**Purpose:** User's professional profile extracted from resume.

**Entities:**
- `CandidateProfile` - User's professional profile (Aggregate Root)
- `Skill` - User's skill with proficiency
- `Experience` - Work experience
- `Education` - Educational background
- `Certification` - Professional certifications

**Value Objects:**
- `ProficiencyLevel` - BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
- `RemotePreference` - REMOTE, HYBRID, ONSITE
- `CompanyType` - PRODUCT, SERVICE, STARTUP, ENTERPRISE

**Domain Services:**
- `ResumeParsingDomainService` - Parses resume and extracts profile
- `SkillGapAnalysisDomainService` - Analyzes skill gaps

**Ports (Interfaces):**
- `CandidateProfileRepository` - Persist candidate profiles
- `ResumeParsingPort` - Parse resume (implemented by AI tool)

### 2.4 Job Domain (REFACTOR)

**Purpose:** Job listings from external sources.

**Entities:**
- `JobListing` - Job from external source (Aggregate Root)
- `JobAnalysis` - AI analysis of job (NEW)

**Value Objects:**
- `EmploymentType` - FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
- `ExperienceLevel` - ENTRY, MID, SENIOR, LEAD, EXECUTIVE
- `CompatibilityScore` - 0-100 score
- `ScamDetectionResult` - Scam detection result

**Domain Services:**
- `JobDeduplicationDomainService` - Removes duplicate job listings
- `JobRankingDomainService` - Ranks jobs by compatibility

**Ports (Interfaces):**
- `JobRepository` - Persist job listings
- `JobAnalysisRepository` - Persist job analysis
- `JobDiscoveryPort` - Discover jobs (implemented by discovery tool)

### 2.5 Application Domain (REFACTOR - Read-Only)

**Purpose:** Agent-submitted applications (read-only for users).

**Entities:**
- `Application` - Agent-submitted application (Aggregate Root)
- `AutomationResult` - Result of browser automation (NEW)

**Value Objects:**
- `ApplicationStatus` - SUBMITTED, UNDER_REVIEW, INTERVIEW_SCHEDULED, OFFER, REJECTED
- `AutomationStatus` - SUCCESS, FAILED, CAPTCHA_DETECTED, MFA_DETECTED

**Domain Services:**
- None (read-only for users, agent writes via automation)

**Ports (Interfaces):**
- `ApplicationRepository` - Persist applications
- `AutomationPort` - Execute browser automation (implemented by browser tool)

### 2.6 Identity Domain (KEEP)

**Purpose:** Authentication and authorization.

**Entities:**
- `User` - System user (Aggregate Root)
- `RefreshToken` - JWT refresh token

**Value Objects:**
- `Email` - Email address
- `UserId` - User identifier
- `UserRole` - USER, ADMIN

**Domain Services:**
- `PasswordValidationDomainService` - Validates password strength

**Ports (Interfaces):**
- `UserRepository` - Persist users
- `RefreshTokenRepository` - Persist refresh tokens

### 2.7 Notification Domain (KEEP - SIMPLIFIED)

**Purpose:** Agent alerts to user.

**Entities:**
- `Notification` - User notification (Aggregate Root)

**Value Objects:**
- `NotificationType` - CAPTCHA_DETECTED, APPLICATION_SUBMITTED, STATUS_CHANGE, ERROR, MISSION_COMPLETE
- `NotificationChannel` - IN_APP, EMAIL

**Domain Services:**
- None

**Ports (Interfaces):**
- `NotificationRepository` - Persist notifications
- `NotificationPort` - Send notifications (email, push)

---

## 3. Module 1: Agent Runtime (CORE)

### 3.1 Package Structure

```
com.jobpilot.agent/
├── AgentRuntime.java              # Main runtime orchestrator
├── loop/
│   ├── AgentLoop.java             # Main loop: Observe-Think-Plan-Execute-Verify-Learn
│   ├── ObservePhase.java
│   ├── ThinkPhase.java
│   ├── PlanPhase.java
│   ├── ExecutePhase.java
│   ├── VerifyPhase.java
│   └── LearnPhase.java
├── tools/
│   ├── ai/
│   │   ├── ResumeParserTool.java
│   │   ├── JobAnalyzerTool.java
│   │   ├── ResumeTailorTool.java
│   │   ├── CoverLetterTool.java
│   │   ├── AnswerGeneratorTool.java
│   │   ├── JobRankerTool.java
│   │   ├── ScamDetectorTool.java
│   │   └── SkillGapTool.java
│   ├── browser/
│   │   ├── BrowserManagerTool.java
│   │   ├── DOMAnalyzerTool.java
│   │   ├── PageClassifierTool.java
│   │   ├── ActionPlannerTool.java
│   │   ├── FormEngineTool.java
│   │   ├── UploadEngineTool.java
│   │   ├── QuestionEngineTool.java
│   │   ├── ScreenshotTool.java
│   │   ├── RetryEngineTool.java
│   │   ├── RecoveryEngineTool.java
│   │   └── SessionManagerTool.java
│   ├── discovery/
│   │   ├── JobDiscoveryTool.java
│   │   └── JobDeduplicationTool.java
│   └── storage/
│       ├── ResumeStorageTool.java
│       ├── JobStorageTool.java
│       ├── ApplicationStorageTool.java
│       └── ScreenshotStorageTool.java
├── memory/
│   ├── LongTermMemory.java
│   ├── ShortTermMemory.java
│   ├── KnowledgeStore.java
│   └── EpisodeMemory.java
├── planning/
│   ├── Planner.java
│   ├── TaskPlanner.java
│   └── WorkflowEngine.java
├── reasoning/
│   ├── Reasoner.java
│   └── DecisionEngine.java
├── queue/
│   ├── TaskQueue.java
│   └── PriorityQueue.java
├── observation/
│   ├── ObservationEngine.java
│   └── StateMonitor.java
└── notification/
    ├── NotificationEngine.java
    └── AlertManager.java
```

### 3.2 Agent Loop Implementation

```java
@Component
public class AgentLoop {
    
    private static final Logger log = LoggerFactory.getLogger(AgentLoop.class);
    
    private final ObservePhase observePhase;
    private final ThinkPhase thinkPhase;
    private final PlanPhase planPhase;
    private final ExecutePhase executePhase;
    private final VerifyPhase verifyPhase;
    private final LearnPhase learnPhase;
    private final AgentRepository agentRepository;
    
    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    public void executeLoop() {
        var agentState = agentRepository.findCurrentAgent()
            .orElseThrow(() -> new AgentNotInitializedException());
        
        if (agentState.status() != AgentStatus.RUNNING) {
            return;
        }
        
        try {
            // Phase 1: Observe
            var observation = observePhase.execute(agentState);
            agentState.updateContext(observation);
            
            // Phase 2: Think
            var reasoning = thinkPhase.execute(agentState);
            agentState.addThought(reasoning);
            
            // Phase 3: Plan
            var tasks = planPhase.execute(agentState);
            taskQueue.addAll(tasks);
            
            // Phase 4: Execute
            var results = executePhase.execute(agentState, taskQueue.poll());
            
            // Phase 5: Verify
            var verification = verifyPhase.execute(results);
            
            // Phase 6: Learn
            learnPhase.execute(agentState, results, verification);
            
            // Update agent state
            agentState.incrementLoopIteration();
            agentRepository.save(agentState);
            
        } catch (Exception e) {
            log.error("Agent loop failed: {}", e.getMessage(), e);
            agentState.fail(e.getMessage());
            agentRepository.save(agentState);
        }
    }
}
```

### 3.3 Tool Interface

```java
public interface Tool {
    String name();
    ToolResult execute(ToolContext context);
    boolean isAvailable();
}
```

### 3.4 AI Tool Example: JobAnalyzerTool

```java
@Component
public class JobAnalyzerTool implements Tool {
    
    private final AiProviderPort aiProvider;
    private final JobAnalysisRepository jobAnalysisRepository;
    
    @Override
    public String name() {
        return "JobAnalyzer";
    }
    
    @Override
    public ToolResult execute(ToolContext context) {
        var jobListing = context.get("jobListing", JobListing.class);
        var candidateProfile = context.get("candidateProfile", CandidateProfile.class);
        
        var prompt = buildAnalysisPrompt(jobListing, candidateProfile);
        var aiResponse = aiProvider.generateText(AiRequest.builder()
            .systemPrompt("You are an expert job analyst...")
            .userPrompt(prompt)
            .build());
        
        var analysis = parseAnalysisResponse(aiResponse.content());
        var jobAnalysis = JobAnalysis.create(
            jobListing.jobId(),
            candidateProfile.candidateId(),
            analysis.compatibilityScore(),
            analysis.scoreBreakdown(),
            analysis.matchedSkills(),
            analysis.missingSkills()
        );
        
        jobAnalysisRepository.save(jobAnalysis);
        
        return ToolResult.success()
            .add("jobAnalysis", jobAnalysis)
            .add("compatibilityScore", analysis.compatibilityScore());
    }
    
    @Override
    public boolean isAvailable() {
        return aiProvider.isAvailable();
    }
}
```

---

## 4. Module 2: AI Provider Layer

### 4.1 Package Structure

```
com.jobpilot.ai/
├── AiProvider.java                # Interface
├── AiRequest.java
├── AiResponse.java
├── AiConfig.java
├── ollama/
│   ├── OllamaProvider.java         # Default: Local Ollama
│   ├── OllamaConfig.java
│   └── OllamaClient.java
├── openai/
│   ├── OpenAIProvider.java         # Optional: OpenAI
│   ├── OpenAIConfig.java
│   └── OpenAIClient.java
├── gemini/
│   ├── GeminiProvider.java         # Optional: Gemini
│   ├── GeminiConfig.java
│   └── GeminiClient.java
└── claude/
    ├── ClaudeProvider.java         # Optional: Claude
    ├── ClaudeConfig.java
    └── ClaudeClient.java
```

### 4.2 AiProvider Interface

```java
public interface AiProvider {
    
    AiResponse generateText(AiRequest request);
    
    Flux<AiChunk> generateStream(AiRequest request);
    
    List<Float> generateEmbedding(String text);
    
    int countTokens(String text);
    
    boolean isAvailable();
    
    String providerName();
}
```

### 4.3 OllamaProvider Implementation

```java
@Service
@ConditionalOnProperty(name = "ai.provider.default", havingValue = "ollama", matchIfMissing = true)
public class OllamaProvider implements AiProvider {
    
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";
    private final OllamaClient ollamaClient;
    private final OllamaConfig config;
    
    @Override
    public AiResponse generateText(AiRequest request) {
        var ollamaRequest = OllamaRequest.builder()
            .model(config.getModel())
            .prompt(request.userPrompt())
            .system(request.systemPrompt())
            .temperature(request.temperature())
            .stream(false)
            .build();
        
        var response = ollamaClient.generate(ollamaRequest);
        
        return AiResponse.builder()
            .content(response.response())
            .model(response.model())
            .promptTokens(response.evalCount())
            .completionTokens(response.evalCount())
            .build();
    }
    
    @Override
    public boolean isAvailable() {
        try {
            var response = ollamaClient.list();
            return response != null && !response.models().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String providerName() {
        return "ollama";
    }
}
```

### 4.4 Auto-Detection

```java
@Component
public class OllamaAutoDetector {
    
    @EventListener(ApplicationReadyEvent.class)
    public void detectOllama() {
        var ollamaAvailable = checkOllamaAvailability();
        
        if (!ollamaAvailable) {
            log.warn("Ollama not detected. Please install Ollama from https://ollama.ai");
            log.warn("After installation, run: ollama pull llama3");
        } else {
            log.info("Ollama detected and available");
        }
    }
    
    private boolean checkOllamaAvailability() {
        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/tags"))
                .timeout(Duration.ofSeconds(5))
                .build();
            
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

## 5. Module 3: Browser Automation Framework

### 5.1 Package Structure

```
com.jobpilot.browser/
├── BrowserManager.java            # Manages Playwright browser instances
├── DOMAnalyzer.java              # Analyzes DOM structure
├── PageClassifier.java           # Classifies page types
├── ActionPlanner.java            # Plans action sequences
├── FormEngine.java               # Fills form fields
├── UploadEngine.java             # Uploads files
├── QuestionEngine.java           # Answers questions
├── ScreenshotEngine.java         # Captures screenshots
├── RetryEngine.java              # Retries failed actions
├── RecoveryEngine.java           # Recovers from errors
├── SessionManager.java           # Manages sessions
└── adapters/
    ├── SiteAdapter.java          # Interface
    ├── LinkedInAdapter.java
    ├── IndeedAdapter.java
    ├── GreenhouseAdapter.java
    ├── LeverAdapter.java
    └── WorkdayAdapter.java
```

### 5.2 SiteAdapter Interface

```java
public interface SiteAdapter {
    
    String siteName();
    
    List<JobListing> searchJobs(SearchCriteria criteria);
    
    void openJob(JobListing job);
    
    void login(Credentials credentials);
    
    void fillForm(ApplicationData data);
    
    void uploadResume(File resume);
    
    void uploadCoverLetter(File coverLetter);
    
    void answerQuestions(List<Question> questions);
    
    void submit();
    
    String takeScreenshot();
    
    boolean detectCaptcha();
    
    boolean detectMFA();
}
```

### 5.3 Generic BrowserManager

```java
@Component
public class BrowserManager {
    
    private Playwright playwright;
    private Browser browser;
    private Map<String, BrowserContext> contexts = new ConcurrentHashMap<>();
    
    public void initialize() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(true)
            .setArgs(List.of("--no-sandbox", "--disable-setuid-sandbox")));
    }
    
    public BrowserContext getContext(String sessionId) {
        return contexts.computeIfAbsent(sessionId, id -> 
            browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 720)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"))
        );
    }
    
    public Page getPage(String sessionId) {
        var context = getContext(sessionId);
        return context.pages().isEmpty() 
            ? context.newPage() 
            : context.pages().get(0);
    }
    
    public void closeContext(String sessionId) {
        var context = contexts.remove(sessionId);
        if (context != null) {
            context.close();
        }
    }
    
    @PreDestroy
    public void cleanup() {
        contexts.values().forEach(BrowserContext::close);
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
```

### 5.4 LinkedInAdapter Example

```java
@Component
public class LinkedInAdapter implements SiteAdapter {
    
    private final BrowserManager browserManager;
    
    @Override
    public String siteName() {
        return "linkedin";
    }
    
    @Override
    public List<JobListing> searchJobs(SearchCriteria criteria) {
        var page = browserManager.getPage("linkedin");
        
        var url = buildSearchUrl(criteria);
        page.navigate(url);
        
        page.waitForSelector(".job-search-card", new Page.WaitForSelectorOptions()
            .setTimeout(10000));
        
        var jobs = page.evaluate("""
            () => Array.from(document.querySelectorAll('.job-search-card')).map(card => ({
                title: card.querySelector('.job-title')?.textContent,
                company: card.querySelector('.company-name')?.textContent,
                location: card.querySelector('.job-location')?.textContent,
                url: card.querySelector('a')?.href
            }))
        """);
        
        return parseJobs(jobs);
    }
    
    @Override
    public void fillForm(ApplicationData data) {
        var page = browserManager.getPage("linkedin");
        
        // Fill name
        page.fill("#first-name", data.firstName());
        page.fill("#last-name", data.lastName());
        
        // Fill email
        page.fill("#email", data.email());
        
        // Fill phone
        page.fill("#phone", data.phone());
        
        // Upload resume
        var resumeInput = page.querySelector("input[type='file']");
        resumeInput.setInputFiles(Paths.get(data.resumePath()));
        
        // Fill cover letter
        if (data.coverLetter() != null) {
            page.fill("#cover-letter", data.coverLetter());
        }
    }
    
    @Override
    public boolean detectCaptcha() {
        var page = browserManager.getPage("linkedin");
        return page.locator("#captcha-challenge").count() > 0;
    }
    
    // ... other methods
}
```

---

## 6. Module 4: Mission Management

### 6.1 Package Structure

```
com.jobpilot.application.mission/
├── ports/
│   ├── MissionRepository.java
│   └── MissionServicePort.java
├── service/
│   ├── MissionService.java
│   └── MissionValidationService.java
├── usecase/
│   ├── CreateMissionUseCase.java
│   ├── UpdateMissionUseCase.java
│   ├── StartMissionUseCase.java
│   ├── PauseMissionUseCase.java
│   ├── ResumeMissionUseCase.java
│   └── StopMissionUseCase.java
└── dto/
    ├── CreateMissionRequest.java
    ├── UpdateMissionRequest.java
    ├── MissionResponse.java
    └── MissionMetricsResponse.java
```

### 6.2 Mission Entity

```java
@Entity
@Table(name = "missions")
public class Mission extends BaseAggregateRoot {
    
    @Id
    @Column(name = "id")
    private MissionId id;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "goal")
    private String goal;
    
    @Column(name = "target_salary")
    private Money targetSalary;
    
    @Column(name = "locations")
    private List<String> locations;
    
    @Column(name = "experience_min")
    private Integer experienceMin;
    
    @Column(name = "experience_max")
    private Integer experienceMax;
    
    @Column(name = "preferred_roles")
    private List<String> preferredRoles;
    
    @Column(name = "preferred_companies")
    private List<String> preferredCompanies;
    
    @Column(name = "avoid_companies")
    private List<String> avoidCompanies;
    
    @Column(name = "daily_apply_limit")
    private Integer dailyApplyLimit;
    
    @Column(name = "deadline")
    private LocalDate deadline;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MissionStatus status;
    
    @Embedded
    private MissionMetrics metrics;
    
    @Column(name = "started_at")
    private Instant startedAt;
    
    @Column(name = "completed_at")
    private Instant completedAt;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    // Factory methods
    public static Mission create(CreateMissionCommand command) {
        var mission = new Mission();
        mission.id = MissionId.generate();
        mission.userId = command.userId();
        mission.name = command.name();
        mission.goal = command.goal();
        mission.targetSalary = command.targetSalary();
        mission.locations = command.locations();
        mission.experienceMin = command.experienceMin();
        mission.experienceMax = command.experienceMax();
        mission.preferredRoles = command.preferredRoles();
        mission.preferredCompanies = command.preferredCompanies();
        mission.avoidCompanies = command.avoidCompanies();
        mission.dailyApplyLimit = command.dailyApplyLimit();
        mission.deadline = command.deadline();
        mission.status = MissionStatus.DRAFT;
        mission.metrics = new MissionMetrics();
        mission.createdAt = Instant.now();
        mission.updatedAt = Instant.now();
        return mission;
    }
    
    // Domain methods
    public void start() {
        if (status != MissionStatus.DRAFT && status != MissionStatus.PAUSED) {
            throw new InvalidMissionStatusTransitionException(status, MissionStatus.ACTIVE);
        }
        this.status = MissionStatus.ACTIVE;
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public void pause() {
        if (status != MissionStatus.ACTIVE) {
            throw new InvalidMissionStatusTransitionException(status, MissionStatus.PAUSED);
        }
        this.status = MissionStatus.PAUSED;
        this.updatedAt = Instant.now();
    }
    
    public void resume() {
        if (status != MissionStatus.PAUSED) {
            throw new InvalidMissionStatusTransitionException(status, MissionStatus.ACTIVE);
        }
        this.status = MissionStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }
    
    public void stop() {
        if (status == MissionStatus.COMPLETED) {
            throw new InvalidMissionStatusTransitionException(status, MissionStatus.CANCELLED);
        }
        this.status = MissionStatus.CANCELLED;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public void complete() {
        if (status != MissionStatus.ACTIVE) {
            throw new InvalidMissionStatusTransitionException(status, MissionStatus.COMPLETED);
        }
        this.status = MissionStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public void updateMetrics(MissionMetricsUpdate update) {
        this.metrics = this.metrics.update(update);
        this.updatedAt = Instant.now();
    }
}
```

---

## 7. Module 5: Candidate Profile

### 7.1 Package Structure

```
com.jobpilot.application.candidate/
├── ports/
│   ├── CandidateProfileRepository.java
│   └── ResumeParsingPort.java
├── service/
│   ├── CandidateProfileService.java
│   └── ResumeParsingService.java
├── usecase/
│   ├── UploadResumeUseCase.java
│   ├── UpdateProfileUseCase.java
│   └── AddSkillUseCase.java
└── dto/
    ├── UploadResumeRequest.java
    ├── CandidateProfileResponse.java
    └── SkillDto.java
```

### 7.2 Resume Parsing Service

```java
@Service
public class ResumeParsingService {
    
    private final ResumeParserTool resumeParserTool;
    private final CandidateProfileRepository candidateProfileRepository;
    
    public CandidateProfile parseResume(File resumeFile) {
        // Extract text from resume
        var resumeText = extractText(resumeFile);
        
        // Use AI to parse resume
        var parsingResult = resumeParserTool.execute(ToolContext.builder()
            .add("resumeText", resumeText)
            .build());
        
        // Create candidate profile
        var profile = CandidateProfile.create(
            parsingResult.get("fullName"),
            parsingResult.get("email"),
            parsingResult.get("phone"),
            parsingResult.get("location"),
            parsingResult.get("skills"),
            parsingResult.get("experience"),
            parsingResult.get("education"),
            parsingResult.get("certifications"),
            parsingResult.get("summary")
        );
        
        candidateProfileRepository.save(profile);
        
        return profile;
    }
    
    private String extractText(File file) {
        // Use Apache PDFBox for PDF, Apache POI for DOCX
        // Implementation depends on file type
    }
}
```

---

## 8. Module 6: Job Discovery

### 8.1 Package Structure

```
com.jobpilot.application.job/
├── ports/
│   ├── JobRepository.java
│   └── JobDiscoveryPort.java
├── service/
│   ├── JobDiscoveryService.java
│   └── JobDeduplicationService.java
├── usecase/
│   ├── DiscoverJobsUseCase.java
│   └── AnalyzeJobsUseCase.java
└── dto/
    ├── JobDiscoveryRequest.java
    └── JobListingResponse.java
```

### 8.2 Job Discovery Service

```java
@Service
public class JobDiscoveryService {
    
    private final List<SiteAdapter> siteAdapters;
    private final JobDeduplicationService deduplicationService;
    private final JobRepository jobRepository;
    
    @Scheduled(cron = "0 */6 * * * *") // Every 6 hours
    public void discoverJobs() {
        var activeMissions = missionRepository.findActiveMissions();
        
        for (var mission : activeMissions) {
            var criteria = buildSearchCriteria(mission);
            var allJobs = new ArrayList<JobListing>();
            
            // Search all job boards
            for (var adapter : siteAdapters) {
                try {
                    var jobs = adapter.searchJobs(criteria);
                    allJobs.addAll(jobs);
                } catch (Exception e) {
                    log.error("Failed to search {}: {}", adapter.siteName(), e.getMessage());
                }
            }
            
            // Deduplicate jobs
            var uniqueJobs = deduplicationService.deduplicate(allJobs);
            
            // Save jobs
            jobRepository.saveAll(uniqueJobs);
            
            // Update mission metrics
            mission.updateMetrics(MissionMetricsUpdate.builder()
                .jobsFound(uniqueJobs.size())
                .build());
            missionRepository.save(mission);
        }
    }
}
```

---

## 9. Module 7: Application Tracking (Read-Only)

### 9.1 Package Structure

```
com.jobpilot.application.application/
├── ports/
│   ├── ApplicationRepository.java
│   └── AutomationPort.java
├── service/
│   ├── ApplicationQueryService.java
│   └── AutomationService.java
├── usecase/
│   ├── GetApplicationsUseCase.java
│   └── GetApplicationUseCase.java
└── dto/
    ├── ApplicationResponse.java
    └── AutomationResultResponse.java
```

### 9.2 Application Query Service

```java
@Service
public class ApplicationQueryService {
    
    private final ApplicationRepository applicationRepository;
    
    public List<Application> getApplications(UUID userId) {
        return applicationRepository.findByUserId(userId);
    }
    
    public Application getApplication(UUID applicationId, UUID userId) {
        return applicationRepository.findByIdAndUserId(applicationId, userId)
            .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
    }
}
```

---

## 10. Module 8: Identity & Access

### 10.1 Package Structure (KEEP)

```
com.jobpilot.application.identity/
├── ports/
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
├── service/
│   ├── AuthService.java
│   ├── RegisterUserService.java
│   └── RefreshTokenService.java
├── usecase/
│   ├── RegisterUserUseCase.java
│   ├── AuthenticateUserUseCase.java
│   └── RefreshTokenUseCase.java
└── dto/
    ├── RegisterUserRequest.java
    ├── AuthenticateUserRequest.java
    └── AuthResponse.java
```

---

## 11. Module 9: Notification Service

### 11.1 Package Structure (KEEP - SIMPLIFIED)

```
com.jobpilot.application.notification/
├── ports/
│   ├── NotificationRepository.java
│   └── NotificationPort.java
├── service/
│   └── NotificationService.java
├── usecase/
│   ├── SendNotificationUseCase.java
│   └── GetNotificationsUseCase.java
└── dto/
    ├── NotificationRequest.java
    └── NotificationResponse.java
```

---

## 12. Cross-Cutting Concerns

### 12.1 Configuration

```java
@Configuration
@ConfigurationProperties(prefix = "agent")
public class AgentConfig {
    
    private int loopIntervalMs = 5000;
    private int maxConcurrentTasks = 5;
    private int taskTimeoutSeconds = 300;
    private boolean enableAutoApproval = false;
    private ApprovalRule approvalRule = ApprovalRule.ALL;
    
    // Getters and setters
}

@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    
    private String defaultProvider = "ollama";
    private String ollamaBaseUrl = "http://localhost:11434";
    private String ollamaModel = "llama3";
    private boolean enableCloudFallback = false;
    
    // Getters and setters
}
```

### 12.2 Exception Handling

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AgentException.class)
    public ResponseEntity<ErrorResponse> handleAgentException(AgentException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.builder()
                .code("AGENT_ERROR")
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build());
    }
    
    @ExceptionHandler(MissionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMissionNotFound(MissionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.builder()
                .code("MISSION_NOT_FOUND")
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build());
    }
    
    // ... other exception handlers
}
```

### 12.3 WebSocket Configuration

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(agentWebSocketHandler(), "/ws/agent")
            .setAllowedOrigins("*");
    }
    
    @Bean
    public AgentWebSocketHandler agentWebSocketHandler() {
        return new AgentWebSocketHandler();
    }
}
```

---

## 13. Sequence Diagrams

### 13.1 Mission Creation Sequence

```
User → MissionController: POST /api/v1/missions
MissionController → CreateMissionUseCase: execute(command)
CreateMissionUseCase → MissionService: create(command)
MissionService → Mission: create(command)
Mission → MissionRepository: save(mission)
MissionRepository → PostgreSQL: INSERT
MissionRepository ← PostgreSQL: success
Mission ← MissionRepository: mission
MissionService ← Mission: mission
CreateMissionUseCase ← MissionService: mission
MissionController ← CreateMissionUseCase: missionResponse
User ← MissionController: missionResponse
```

### 13.2 Agent Start Sequence

```
User → MissionController: POST /api/v1/missions/{id}/start
MissionController → StartMissionUseCase: execute(missionId)
StartMissionUseCase → AgentRuntime: start(missionId)
AgentRuntime → AgentLoop: start()
AgentLoop → ObservePhase: execute(agentState)
ObservePhase → MissionRepository: findById(missionId)
ObservePhase → TaskRepository: findPendingTasks(missionId)
ObservePhase → MemoryRepository: findRecent(missionId)
ObservePhase ← AgentLoop: observation
AgentLoop → ThinkPhase: execute(agentState, observation)
ThinkPhase → AiProvider: generateText(prompt)
ThinkPhase ← AiProvider: reasoning
ThinkPhase ← AgentLoop: reasoning
AgentLoop → PlanPhase: execute(agentState, reasoning)
PlanPhase → TaskPlanner: plan(mission, reasoning)
PlanPhase ← AgentLoop: tasks
AgentLoop → TaskQueue: addAll(tasks)
AgentLoop → WebSocket: push(agentStatus=RUNNING)
User ← WebSocket: agentStatus update
```

### 13.3 Job Discovery Sequence

```
AgentLoop (Execute Phase) → JobDiscoveryTool: execute(criteria)
JobDiscoveryTool → JobDiscoveryService: discoverJobs(criteria)
JobDiscoveryService → LinkedInAdapter: searchJobs(criteria)
LinkedInAdapter → BrowserManager: getPage(sessionId)
LinkedInAdapter → Playwright: navigate(url)
LinkedInAdapter → Playwright: evaluate(script)
LinkedInAdapter ← Playwright: jobs
LinkedInAdapter ← BrowserManager: jobs
JobDiscoveryService ← LinkedInAdapter: jobs
JobDiscoveryService → IndeedAdapter: searchJobs(criteria)
IndeedAdapter ← BrowserManager: jobs
JobDiscoveryService ← IndeedAdapter: jobs
JobDiscoveryService → JobDeduplicationService: deduplicate(allJobs)
JobDeduplicationService ← JobDiscoveryService: uniqueJobs
JobDiscoveryService → JobRepository: saveAll(uniqueJobs)
JobDiscoveryService ← JobRepository: savedJobs
JobDiscoveryTool ← JobDiscoveryService: jobCount
AgentLoop ← JobDiscoveryTool: result
```

### 13.4 Application Submission Sequence

```
AgentLoop (Execute Phase) → BrowserManagerTool: execute(applicationData)
BrowserManagerTool → SiteAdapter: fillForm(applicationData)
SiteAdapter → BrowserManager: getPage(sessionId)
SiteAdapter → FormEngine: fillField(selector, value)
SiteAdapter → UploadEngine: uploadFile(resumePath)
SiteAdapter → QuestionEngine: answerQuestions(questions)
SiteAdapter → ScreenshotTool: capture()
SiteAdapter ← ScreenshotTool: screenshotUrl
SiteAdapter → Playwright: click(submitButton)
SiteAdapter ← Playwright: response
SiteAdapter ← BrowserManager: result
BrowserManagerTool ← SiteAdapter: automationResult
BrowserManagerTool → ApplicationStorageTool: save(application, screenshot)
BrowserManagerTool ← ApplicationStorageTool: saved
AgentLoop ← BrowserManagerTool: result
AgentLoop → WebSocket: push(applicationSubmitted)
User ← WebSocket: notification
```

---

## 14. Class Relationship Diagrams

### 14.1 Agent Runtime Class Diagram

```
┌──────────────────┐
│   AgentRuntime   │
│                  │
│ + start()        │
│ + pause()        │
│ + stop()         │
│ + executeLoop()  │
└────────┬─────────┘
         │
         │ uses
         │
┌────────▼─────────┐
│   AgentLoop      │
│                  │
│ + execute()      │
└────────┬─────────┘
         │
         │ uses
         │
┌──────────────────────────────────────────────────┐
│                  Phases                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │ Observe   │  │  Think   │  │   Plan   │     │
│  └──────────┘  └──────────┘  └──────────┘     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │ Execute  │  │  Verify  │  │   Learn  │     │
│  └──────────┘  └──────────┘  └──────────┘     │
└──────────────────────────────────────────────────┘
         │
         │ uses
         │
┌──────────────────────────────────────────────────┐
│                   Tools                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │ AI Tools │  │Browser    │  │Discovery │     │
│  │          │  │ Tools     │  │ Tools    │     │
│  └──────────┘  └──────────┘  └──────────┘     │
└──────────────────────────────────────────────────┘
         │
         │ uses
         │
┌──────────────────────────────────────────────────┐
│                  Memory                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │Long-term │  │Short-term│  │Knowledge │     │
│  └──────────┘  └──────────┘  └──────────┘     │
└──────────────────────────────────────────────────┘
```

### 14.2 Domain Class Diagram

```
┌──────────────┐
│    Mission    │◄────────┐
│              │         │
│ + start()     │         │
│ + pause()     │         │
│ + resume()    │         │
│ + stop()      │         │
└──────┬───────┘         │
       │                 │
       │1                │
       │                 │
┌──────▼───────┐         │
│    Task      │         │
│              │         │
│ + execute()   │         │
└──────────────┘         │
                         │
┌────────────────────────┴──────────────────┐
│              AgentState                   │
│                                            │
│ + updateStatus()                           │
│ + updatePhase()                            │
│ + addThought()                              │
└────────────────────────────────────────────┘
```

---

## 15. Dependency Rules Enforcement

### 15.1 ArchUnit Tests

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
    static final ArchRule application_layer_may_depend_on_domain =
        classes().that().resideInAPackage("..application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..application..", "..domain..", "java..", "org.springframework..");
    
    @ArchTest
    static final ArchRule infrastructure_should_depend_on_domain =
        classes().that().resideInAPackage("..infrastructure..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..domain..", "java..", "org.springframework..");
    
    @ArchTest
    static final ArchRule interfaces_should_depend_on_application =
        classes().that().resideInAPackage("..interfaces..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..interfaces..", "..application..", "..domain..", "java..");
    
    @ArchTest
    static final ArchRule agent_runtime_should_not_depend_on_interfaces =
        noClasses().that().resideInAPackage("..agent..")
            .should().dependOnClassesThat().resideInAPackage("..interfaces..");
    
    @ArchTest
    static final ArchRule controllers_must_be_in_rest_package =
        classes().that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..interfaces.rest..");
    
    @ArchTest
    static final ArchRule services_must_be_in_application_package =
        classes().that().haveSimpleNameEndingWith("Service")
            .should().resideInAPackage("..application..");
    
    @ArchTest
    static final ArchRule no_circular_dependencies =
        slices().matching("com.jobpilot.(*)..")
            .should().beFreeOfCycles();
}
```

---

**End of LLD v2.0**
