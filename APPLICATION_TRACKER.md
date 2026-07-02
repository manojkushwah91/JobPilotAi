# JobPilot AI — Application Tracker (ATS)

**Version:** 1.0  
**Status:** Draft  
**Phase:** 16 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Full pipeline management for job applications — Kanban-style tracking, status transitions, notes, attachments, timeline, follow-up reminders, and automation integration.

---

## 2. Domain Model

```
Application (Aggregate Root):
  - id: UUID
  - userId: UUID
  - jobListingId: UUID
  - resumeId: UUID (optional)
  - coverLetterId: UUID (optional)
  - status: ApplicationStatus (state machine)
  - statusHistory: List<StatusChange> (JSONB, append-only)
  - automationInfo: AutomationInfo (JSONB, optional)
  - notes: List<ApplicationNote>
  - attachments: List<ApplicationAttachment>
  - followUp: FollowUp (JSONB, optional)
  - salaryOffered: SalaryRange (JSONB, optional)
  - appliedAt: Instant
  - deletedAt, createdAt, updatedAt

ApplicationStatus (ordered state machine):
  SAVED → APPLIED → PHONE_SCREEN → TECHNICAL_INTERVIEW → ONSITE_INTERVIEW → OFFER → ACCEPTED
                                                                              ↘ REJECTED (any)
                                                                              ↘ WITHDRAWN (any)

StatusChange (VO, immutable):
  { from, to, changedBy: USER|SYSTEM|AUTOMATION, note, timestamp }

ApplicationNote (Entity):
  - id: UUID, applicationId, userId, content, category(GENERAL|PREP|FOLLOW_UP|RESEARCH|OFFER), createdAt, updatedAt

AutomationInfo (VO):
  { status, sessionId, submittedAt, evidenceUrl, formData, errorMessage }

FollowUp (VO):
  { dueDate, type: EMAIL|PHONE|MESSAGE, notes, isCompleted, remindedAt }

TimelineEvent (VO, append-only):
  { type, title, description, metadata, eventTimestamp }
```

---

## 3. State Machine Rules

```
SAVED:
  → APPLIED  (user submits manually or automation succeeds)
  → WITHDRAWN (user removes)

APPLIED:
  → PHONE_SCREEN       (recruiter outreach)
  → REJECTED
  → WITHDRAWN

PHONE_SCREEN:
  → TECHNICAL_INTERVIEW (pass)
  → REJECTED            (fail)

TECHNICAL_INTERVIEW:
  → ONSITE_INTERVIEW    (pass)
  → OFFER               (fast track)
  → REJECTED

ONSITE_INTERVIEW:
  → OFFER
  → REJECTED

OFFER:
  → ACCEPTED            (user accepts)
  → REJECTED            (user declines)
  → WITHDRAWN           (candidate withdraws)

ACCEPTED:    terminal
REJECTED:    terminal
WITHDRAWN:   terminal

Invalid transitions throw InvalidStatusTransitionException
```

---

## 4. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/applications` | Create application |
| GET | `/api/v1/applications` | List applications (filter by status, page) |
| GET | `/api/v1/applications/{id}` | Application detail |
| PUT | `/api/v1/applications/{id}/status` | Update status (validates state machine) |
| DELETE | `/api/v1/applications/{id}` | Soft delete |
| POST | `/api/v1/applications/{id}/notes` | Add note |
| DELETE | `/api/v1/applications/{id}/notes/{noteId}` | Delete note |
| POST | `/api/v1/applications/{id}/attachments` | Upload attachment |
| DELETE | `/api/v1/applications/{id}/attachments/{attachId}` | Delete attachment |
| POST | `/api/v1/applications/{id}/follow-ups` | Set follow-up reminder |
| PUT | `/api/v1/applications/{id}/follow-ups/complete` | Complete follow-up |
| GET | `/api/v1/applications/{id}/timeline` | Get timeline events |
| POST | `/api/v1/applications/{id}/automate` | Trigger browser automation |

---

## 5. Follow-Up Reminder Flow

```
FollowUpScheduler (runs every 15 min):
  1. Query applications where follow_up.dueDate < now
     AND follow_up.isCompleted = false
     AND follow_up.remindedAt IS NULL
  2. For each: publish FollowUpDueEvent
  3. Notification service sends email/push/in-app
  4. Set follow_up.remindedAt = now
```

---

**End of Application Tracker v1.0**
