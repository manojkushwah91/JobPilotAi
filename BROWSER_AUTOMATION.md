# JobPilot AI v2.0 — Browser Automation Framework

**Version:** 2.0  
**Status:** Draft  
**Product:** JobPilot AI — "Offline-First Autonomous AI Job Agent"  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

**Generic browser automation framework** for filling and submitting job applications on external job boards. The framework is **not site-specific** — site-specific logic is isolated in adapters. The framework provides reusable components for DOM analysis, page classification, action planning, form filling, file uploading, question answering, screenshot capture, retry logic, and error recovery.

---

## 2. Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Agent Runtime (Browser Tools)               │
│  BrowserManagerTool, FormEngineTool, UploadEngineTool, etc. │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│              Generic Browser Automation Framework              │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │  BrowserManager  │  │   DOMAnalyzer    │                │
│  │  (Playwright)    │  │   (Element detect)│                │
│  └──────────────────┘  └──────────────────┘                │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ PageClassifier   │  │  ActionPlanner   │                │
│  │ (Page type)      │  │ (Action sequence) │                │
│  └──────────────────┘  └──────────────────┘                │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │   FormEngine     │  │  UploadEngine    │                │
│  │  (Form filling)  │  │  (File upload)   │                │
│  └──────────────────┘  └──────────────────┘                │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ QuestionEngine   │  │ ScreenshotEngine │                │
│  │ (Answer Qs)      │  │ (Capture)        │                │
│  └──────────────────┘  └──────────────────┘                │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │   RetryEngine    │  │  RecoveryEngine  │                │
│  │ (Retry logic)    │  │ (Error recovery) │                │
│  └──────────────────┘  └──────────────────┘                │
│  ┌──────────────────┐                                        │
│  │ SessionManager   │                                        │
│  │ (Session mgmt)   │                                        │
│  └──────────────────┘                                        │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    Site Adapters (Site-Specific Only)          │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ LinkedInAdapter  │  │  IndeedAdapter   │                │
│  │ (Selectors only) │  │ (Selectors only) │                │
│  └──────────────────┘  └──────────────────┘                │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ GreenhouseAdapter│  │  LeverAdapter    │                │
│  │ (Selectors only) │  │ (Selectors only) │                │
│  └──────────────────┘  └──────────────────┘                │
│  ┌──────────────────┐                                        │
│  │ WorkdayAdapter   │                                        │
│  │ (Selectors only) │                                        │
│  └──────────────────┘                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Generic Framework Components

### 3.1 BrowserManager

**Purpose:** Manages Playwright browser instances and contexts.

**Responsibilities:**
- Launch headless Chromium browser
- Create browser contexts (isolated per session)
- Set viewport, locale, timezone
- Inject stealth patches
- Close browser and contexts

**Interface:**
```java
public interface BrowserManager {
    void initialize();
    BrowserContext getContext(String sessionId);
    Page getPage(String sessionId);
    void closeContext(String sessionId);
    void cleanup();
}
```

### 3.2 DOMAnalyzer

**Purpose:** Analyzes DOM structure and detects elements.

**Responsibilities:**
- Detect form fields (input, textarea, select)
- Detect buttons (submit, cancel)
- Detect file upload inputs
- Detect CAPTCHA elements
- Detect MFA elements

**Interface:**
```java
public interface DOMAnalyzer {
    List<FormField> detectFormFields(Page page);
    List<ButtonElement> detectButtons(Page page);
    List<FileUploadElement> detectFileUploads(Page page);
    boolean detectCaptcha(Page page);
    boolean detectMFA(Page page);
}
```

### 3.3 PageClassifier

**Purpose:** Classifies page type based on DOM structure.

**Responsibilities:**
- Classify as LOGIN, FORM, LISTING, CONFIRMATION, ERROR
- Use selector patterns for classification

**Interface:**
```java
public interface PageClassifier {
    PageType classify(Page page);
}
```

### 3.4 ActionPlanner

**Purpose:** Plans action sequence based on page type.

**Responsibilities:**
- Generate action sequence (navigate → fill → upload → submit)
- Handle conditional logic (if CAPTCHA detected, pause)
- Optimize action order

**Interface:**
```java
public interface ActionPlanner {
    List<Action> planActions(Page page, ApplicationData data);
}
```

### 3.5 FormEngine

**Purpose:** Fills form fields intelligently.

**Responsibilities:**
- Fill text fields (name, email, phone)
- Fill dropdowns (select)
- Fill checkboxes and radio buttons
- Handle dynamic fields

**Interface:**
```java
public interface FormEngine {
    void fillForm(Page page, ApplicationData data);
}
```

### 3.6 UploadEngine

**Purpose:** Uploads files (resume, cover letter).

**Responsibilities:**
- Detect file upload inputs
- Upload files from local filesystem
- Handle multiple file uploads
- Verify upload success

**Interface:**
```java
public interface UploadEngine {
    void uploadResume(Page page, String resumePath);
    void uploadCoverLetter(Page page, String coverLetterPath);
}
```

### 3.7 QuestionEngine

**Purpose:** Answers application questions.

**Responsibilities:**
- Detect question fields
- Generate answers using AI
- Fill answer fields
- Handle different question types (text, multiple choice)

**Interface:**
```java
public interface QuestionEngine {
    void answerQuestions(Page page, List<Question> questions, CandidateProfile profile);
}
```

### 3.8 ScreenshotEngine

**Purpose:** Captures screenshots.

**Responsibilities:**
- Capture full page screenshots
- Capture element screenshots
- Store screenshots to filesystem
- Return screenshot URLs

**Interface:**
```java
public interface ScreenshotEngine {
    String captureFullPage(Page page);
    String captureElement(Page page, String selector);
}
```

### 3.9 RetryEngine

**Purpose:** Retries failed actions with exponential backoff.

**Responsibilities:**
- Retry failed actions up to N times
- Implement exponential backoff
- Log retry attempts

**Interface:**
```java
public interface RetryEngine {
    <T> T executeWithRetry(Callable<T> action, int maxRetries);
}
```

### 3.10 RecoveryEngine

**Purpose:** Recovers from errors.

**Responsibilities:**
- Handle network failures
- Handle session timeouts
- Handle page load failures
- Implement recovery strategies

**Interface:**
```java
public interface RecoveryEngine {
    void recoverFromError(Exception error, Page page);
}
```

### 3.11 SessionManager

**Purpose:** Manages browser sessions and cookies.

**Responsibilities:**
- Create isolated sessions per mission
- Persist cookies
- Clear sessions on completion
- Handle session timeouts

**Interface:**
```java
public interface SessionManager {
    String createSession();
    void persistSession(String sessionId);
    void clearSession(String sessionId);
}
```

---

## 4. Site Adapters

### 4.1 SiteAdapter Interface

**Purpose:** Site-specific logic only (selectors and workflow rules).

**Interface:**
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

### 4.2 LinkedInAdapter

**Selectors Only:**
```java
@Component
public class LinkedInAdapter implements SiteAdapter {
    
    private static final String JOB_TITLE_SELECTOR = ".job-title";
    private static final String COMPANY_NAME_SELECTOR = ".company-name";
    private static final String APPLY_BUTTON_SELECTOR = ".apply-button";
    private static final String FIRST_NAME_SELECTOR = "#first-name";
    private static final String LAST_NAME_SELECTOR = "#last-name";
    private static final String EMAIL_SELECTOR = "#email";
    private static final String PHONE_SELECTOR = "#phone";
    private static final String RESUME_UPLOAD_SELECTOR = "input[type='file']";
    private static final String CAPTCHA_SELECTOR = "#captcha-challenge";
    
    // Implementation uses generic framework components
    // Only selectors are site-specific
}
```

### 4.3 IndeedAdapter

**Selectors Only:**
```java
@Component
public class IndeedAdapter implements SiteAdapter {
    
    private static final String JOB_TITLE_SELECTOR = ".jobtitle";
    private static final String COMPANY_NAME_SELECTOR = ".companyName";
    private static final String APPLY_BUTTON_SELECTOR = ".indeed-apply-button";
    // ... other selectors
}
```

### 4.4 GreenhouseAdapter

**Selectors Only:**
```java
@Component
public class GreenhouseAdapter implements SiteAdapter {
    
    private static final String JOB_TITLE_SELECTOR = ".job-title";
    private static final String COMPANY_NAME_SELECTOR = ".company-name";
    // ... other selectors
}
```

### 4.5 LeverAdapter

**Selectors Only:**
```java
@Component
public class LeverAdapter implements SiteAdapter {
    
    private static final String JOB_TITLE_SELECTOR = ".job-title";
    private static final String COMPANY_NAME_SELECTOR = ".company-name";
    // ... other selectors
}
```

### 4.6 WorkdayAdapter

**Selectors Only:**
```java
@Component
public class WorkdayAdapter implements SiteAdapter {
    
    private static final String JOB_TITLE_SELECTOR = ".job-title";
    private static final String COMPANY_NAME_SELECTOR = ".company-name";
    // ... other selectors
}
```

---

## 5. Automation Flow

```
1. Agent Runtime executes ApplicationSubmissionTool
   ↓
2. Tool selects SiteAdapter based on jobListing.source
   ↓
3. Adapter uses BrowserManager to get page
   ↓
4. Adapter uses PageClassifier to classify page type
   ↓
5. Adapter uses ActionPlanner to plan actions
   ↓
6. Adapter uses FormEngine to fill form
   ↓
7. Adapter uses UploadEngine to upload resume and cover letter
   ↓
8. Adapter uses QuestionEngine to answer questions
   ↓
9. Adapter uses ScreenshotEngine to capture screenshot before submit
   ↓
10. Adapter uses RetryEngine to submit with retry logic
   ↓
11. On success: Store application result, update Mission metrics
   ↓
12. On CAPTCHA detected: Pause agent, notify user, wait for manual completion
   ↓
13. On error: Use RecoveryEngine to recover, or fail with error message
```

---

## 6. CAPTCHA/MFA Handling

### 6.1 Detection

**CAPTCHA Detection:**
- DOMAnalyzer detects CAPTCHA elements via selector patterns
- Common CAPTCHA selectors: `#captcha`, `.captcha`, `iframe[src*="recaptcha"]`

**MFA Detection:**
- DOMAnalyzer detects MFA elements via selector patterns
- Common MFA selectors: `#mfa`, `.mfa`, `.two-factor`

### 6.2 Handling

**Agent Pause:**
1. Agent detects CAPTCHA/MFA
2. Agent takes screenshot
3. Agent pauses Agent Loop
4. Agent updates agent status to AWAITING_USER
5. Agent notifies user via WebSocket with screenshot
6. User manually completes CAPTCHA/MFA
7. User clicks RESUME in Mission Control
8. Agent resumes Agent Loop
9. Agent continues application submission

**Screenshot Storage:**
- Screenshots stored encrypted at rest
- Screenshots deleted after 90 days
- User can view screenshots in Mission Control

---

## 7. Rate Limiting

### 7.1 Per-Site Rate Limits

| Site | Rate Limit | Reason |
|------|------------|--------|
| LinkedIn | 30 requests/minute | LinkedIn anti-bot measures |
| Indeed | 20 requests/minute | Indeed anti-bot measures |
| Greenhouse | 10 requests/minute | Greenhouse anti-bot measures |
| Lever | 10 requests/minute | Lever anti-bot measures |
| Workday | 5 requests/minute | Workday anti-bot measures |

### 7.2 Implementation

```java
@Component
public class RateLimiter {
    
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String site) {
        var limiter = limiters.computeIfAbsent(site, this::createLimiter);
        return limiter.tryAcquire();
    }
    
    private RateLimiter createLimiter(String site) {
        var rateLimit = getRateLimitForSite(site);
        return RateLimiter.create(rateLimit);
    }
}
```

---

## 8. Human-Like Behavior

### 8.1 Delays

- Random delays between actions (2-5 seconds)
- Longer delays before submission (5-10 seconds)
- Simulate human typing speed (100-200ms per character)

### 8.2 User Agent Rotation

- Rotate user agents periodically
- Use realChrome user agents
- Avoid detection as bot

### 8.3 Viewport Randomization

- Randomize viewport size (±10%)
- Set realistic viewport (1920x1080, 1366x768)

---

## 9. Error Handling

### 9.1 Common Errors

| Error | Recovery Strategy |
|-------|-------------------|
| Network failure | Retry with exponential backoff |
| Page load timeout | Retry, then fail |
| Element not found | Retry, then fail with screenshot |
| CAPTCHA detected | Pause and notify user |
| MFA detected | Pause and notify user |
| Session timeout | Re-login and retry |
| Rate limit exceeded | Wait and retry after delay |

### 9.2 Error Logging

All errors are logged with:
- Error type
- Site name
- Job ID
- Screenshot URL
- Timestamp
- Stack trace

---

## 10. Testing

### 10.1 Unit Tests

Mock Playwright for fast tests:

```java
@SpringBootTest
class FormEngineTest {
    
    @Mock
    private Page page;
    
    @InjectMocks
    private FormEngine formEngine;
    
    @Test
    void shouldFillFormCorrectly() {
        var data = ApplicationData.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();
        
        formEngine.fillForm(page, data);
        
        verify(page).fill("#first-name", "John");
        verify(page).fill("#last-name", "Doe");
        verify(page).fill("#email", "john@example.com");
    }
}
```

### 10.2 Integration Tests

Test with real Playwright (optional, requires browser):

```java
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "PLAYWRIGHT_ENABLED", matches = "true")
class LinkedInAdapterIntegrationTest {
    
    @Test
    void shouldSearchJobs() {
        var criteria = SearchCriteria.builder()
            .roles(List.of("Java Backend"))
            .locations(List.of("Remote"))
            .build();
        
        var jobs = linkedInAdapter.searchJobs(criteria);
        
        assertThat(jobs).isNotEmpty();
    }
}
```

---

## 11. Security

### 11.1 Credential Management

- Job board credentials stored encrypted in PostgreSQL
- Credentials never stored in browser profiles
- Credentials provided per session by user
- Credentials never logged

### 11.2 Session Isolation

- Each mission has isolated browser context
- Cookies and local storage isolated per session
- Sessions cleared after mission completion

---

**End of Browser Automation Framework v2.0**
