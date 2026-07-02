# JobPilot AI — Browser Automation

**Version:** 1.0  
**Status:** Draft  
**Phase:** 20 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Headless browser automation for filling and submitting job applications on external portals (LinkedIn, Indeed, Workday, Greenhouse, Lever) and scraping job/company data when APIs are unavailable.

---

## 2. Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                 BrowserAutomationService                    │
│  @RabbitListener(queues = "automation.jobs")               │
│                                                             │
│  + automateApplication(Application, JobListing):            │
│       AutomationResult                                     │
│  + scrapeJobDetails(url): RawJobData                       │
│  + scrapeCompanyProfile(url): RawCompanyData               │
└──────────┬──────────────────────────────────────┬──────────┘
           │                                      │
           ▼                                      ▼
┌──────────────────────┐            ┌──────────────────────┐
│   PortalAdapter      │            │  SiteScraper         │
│   (Strategy)         │            │  (Strategy)          │
│                      │            │                      │
│ - LinkedInAdapter    │            │ - LinkedInScraper    │
│ - IndeedAdapter      │            │ - GlassdoorScraper   │
│ - WorkdayAdapter     │            │ - CrunchbaseScraper  │
│ - GreenhouseAdapter  │            │ - LevelsFyiScraper   │
│ - LeverAdapter       │            │ - CompanyPageScraper │
└──────────┬───────────┘            └──────────┬───────────┘
           │                                   │
           └──────────────┬───────────────────┘
                          ▼
          ┌──────────────────────────┐
          │  PlaywrightJavaService   │
          │  (BrowserManager)        │
          │                          │
          │  - launch headless       │
          │  - create browser ctx    │
          │  - set viewport, locale  │
          │  - inject stealth        │
          │  - handle CAPTCHA        │
          │  - navigate + interact   │
          │  - take screenshots      │
          └──────────────────────────┘
```

---

## 3. Automation Flow (Job Submission)

```
1. User triggers: POST /applications/{id}/automate

2. ApplicationCreatedEvent published → automation.jobs queue

3. BrowserAutomationService.consume(event):
   a. Load Application + JobListing
   b. Select PortalAdapter by jobListing.source
   c. Adapter builds action plan:
      - Visit career portal
      - Detect existing account → login or create
      - Navigate to application form
      - Fill fields: name, email, phone, resume upload, cover letter, etc.
      - Handle "additional questions"
      - Review and submit
   d. Take screenshots at each step (evidence)
   e. On success: publish ApplicationAutomatedEvent
      → Update application status to APPLIED
      → Store automationInfo (sessionId, evidenceUrl, formData)
   f. On failure: publish AutomationFailedEvent
      → Log error
      → Set automationInfo.status = FAILED
      → Create notification for user
```

---

## 4. CAPTCHA Handling

| Strategy | Trigger | Method |
|----------|---------|--------|
| Avoid | DD/MM before CAPTCHA loads | Adjust navigation timing |
| Wait | CAPTCHA appears | Wait up to 30s for manual user intervention via WebSocket |
| Retry | After failed submit | Rotate IP via proxy + user agent |
| 2Captcha | Persistent CAPTCHA | Integrate 2Captcha API (configurable in application.yml) |

---

## 5. Stealth Configuration

```
BrowserContext config:
  - viewport: 1920x1080 (randomized ±10%)
  - userAgent: Real Chrome 120+ UA (rotating)
  - locale: en-US
  - timezoneId: America/New_York
  - geolocation: based on user IP
  - permissions: geolocation granted
  - extraHTTPHeaders: Accept-Language: en-US
Stealth patches:
  - WebDriver navigator property removed
  - Chrome runtime flags hidden
  - Canvas/WebGL fingerprint spoofed
  - Font fingerprint spoofed
  - Navigator.plugins populated
  - AudioContext spoofed
```

---

## 6. Rate Limiting & Proxy

```
- Per-portal QPS limits (configurable):
  LinkedIn: 1 req/5s
  Indeed:   1 req/3s
  Workday:  1 req/2s (per customer domain)

- Proxy rotation via ProxyService:
  Residential proxies for scraping
  Datacenter proxies for form submission
  Automatic failover on HTTP 429/403

- Session isolation: Each user's automation uses dedicated context
  Context lifetime: 30 min max (configurable), then cleanup
```

---

## 7. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/automation/apply/{applicationId}` | Trigger application automation |
| GET | `/api/v1/automation/sessions` | List active automation sessions |
| GET | `/api/v1/automation/sessions/{id}` | Session detail + screenshots |
| DELETE | `/api/v1/automation/sessions/{id}` | Kill session |
| GET | `/api/v1/automation/captcha/{sessionId}` | Get CAPTCHA status |
| POST | `/api/v1/automation/captcha/{sessionId}/solve` | Submit manual CAPTCHA solve |

---

**End of Browser Automation v1.0**
