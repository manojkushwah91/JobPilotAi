# JobPilot AI — Notification Module

**Version:** 1.0  
**Status:** Draft  
**Phase:** 21 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Multi-channel notification delivery (in-app, email, push) with templates, delivery guarantees, and user preferences.

---

## 2. Domain Model

```
Notification:
  - id: UUID
  - userId: UUID
  - type: NotificationType
  - channel: IN_APP | EMAIL | PUSH
  - title, body: String
  - metadata: JSON  // { applicationId, jobListingId, url, etc. }
  - status: PENDING | SENT | DELIVERED | FAILED | READ
  - readAt, sentAt, deliveredAt, createdAt

NotificationType:
  APPLICATION_STATUS_CHANGE
  NEW_JOB_MATCH
  FOLLOW_UP_REMINDER
  RESUME_SCORE_UPDATE
  INTERVIEW_FEEDBACK_READY
  AUTOMATION_COMPLETED
  AUTOMATION_FAILED
  SAVED_SEARCH_RESULT
  OFFER_DEADLINE
  WELCOME
  SECURITY_ALERT
  SUBSCRIPTION_EXPIRY

NotificationTemplate (sysdata):
  - type, channel, subject, body(Thymeleaf)
  - Variables resolved at send time

UserNotificationPreferences:
  - userId
  - emailDigest: INSTANT | DAILY | WEEKLY | NEVER
  - pushEnabled, inAppEnabled
  - perTypePreferences: Map<NotificationType, Channel[]>
```

---

## 3. Delivery Flow

```
1. Any service publishes event (e.g., ApplicationStatusChangedEvent)

2. NotificationService listens:
   a. Resolve NotificationType from event type
   b. Load UserNotificationPreferences
   c. Determine target channels
   d. For each channel:
      - Resolve NotificationTemplate
      - Render with event data
      - Create Notification record (PENDING)
      - Enqueue to channel-specific queue

3. InAppChannel: WebSocket via STOMP → deliver immediately → mark DELIVERED

4. EmailChannel: SendGrid (or SES) → on success: DELIVERED, on failure: retry (3x)

5. PushChannel: Firebase Cloud Messaging → DELIVERED/FAILED

6. User opens in-app → fetch GET /notifications?status=SENT → mark READ
```

---

## 4. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/notifications` | List user notifications |
| GET | `/api/v1/notifications/unread-count` | Unread badge count |
| PUT | `/api/v1/notifications/{id}/read` | Mark as read |
| PUT | `/api/v1/notifications/read-all` | Mark all as read |
| GET | `/api/v1/notifications/preferences` | Get user notification prefs |
| PUT | `/api/v1/notifications/preferences` | Update preferences |

---

**End of Notification Module v1.0**
