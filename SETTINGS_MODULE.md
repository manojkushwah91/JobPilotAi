# JobPilot AI — Settings Module

**Version:** 1.0  
**Status:** Draft  
**Phase:** 23 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

User-configurable platform settings including profile, preferences, notification prefs, privacy, billing, and account management.

---

## 2. Settings Categories

| Category | Settings |
|----------|----------|
| Profile | Name, headline, bio, location, phone, timezone |
| Job Preferences | Desired roles, industries, locations, remote preference, salary expectations, employment types, availability |
| Resume Settings | Default template, default tone, auto-save interval |
| Notification | Per-type channel preferences, email digest frequency |
| Privacy | Profile visibility (PUBLIC | PRIVATE | CONNECTIONS_ONLY), data export, account deletion |
| AI Preferences | Preferred AI provider, voice mode setting |
| Billing | Plan, payment method, invoices, cancel subscription |
| Account | Password change, 2FA, active sessions, API keys |

---

## 3. Data Model

```
UserSettings:
  - userId: UUID (PK)
  - jobPreferences: JobPreferences (JSONB)
  - resumeDefaults: ResumeDefaults (JSONB)
  - privacy: PrivacySettings (JSONB)
  - aiPreferences: AiPreferences (JSONB)
  - appearance: AppearanceSettings (JSONB)   // dark/light mode, etc.
  - twoFactorEnabled: Boolean
  - twoFactorMethod: AUTHENTICATOR | SMS | EMAIL
  - activeApiKeys: List<ApiKey> (JSONB)
  - updatedAt, createdAt

ApiKey (VO):
  - id, keyPrefix, name, lastUsedAt, createdAt, expiresAt

JobPreferences (VO):
  - desiredRoles[], desiredIndustries[], preferredLocations[], remotePreference,
    salaryMin/Max, salaryCurrency, employmentTypes[], availability,
    openToRelocate, openToContract
```

---

## 4. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/settings` | Get all settings |
| PUT | `/api/v1/settings/profile` | Update profile |
| PUT | `/api/v1/settings/job-preferences` | Update job preferences |
| PUT | `/api/v1/settings/profile/headline` | Update headline |
| PUT | `/api/v1/settings/resume` | Resume defaults |
| PUT | `/api/v1/settings/privacy` | Privacy settings |
| PUT | `/api/v1/settings/ai` | AI preferences |
| GET | `/api/v1/settings/billing` | Billing info |
| GET | `/api/v1/settings/billing/invoices` | Invoice history |
| PUT | `/api/v1/settings/billing/cancel` | Cancel subscription |
| POST | `/api/v1/settings/api-keys` | Generate API key |
| DELETE | `/api/v1/settings/api-keys/{id}` | Revoke API key |
| GET | `/api/v1/settings/api-keys` | List API keys |

---

**End of Settings Module v1.0**
