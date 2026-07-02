# JobPilot AI — User & Profile Module

**Version:** 1.0  
**Status:** Draft  
**Phase:** 8 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Manage user identity, extended profiles, preferences, and account lifecycle. Single source of truth for all user data used across other modules.

---

## 2. Domain Model

### 2.1 Entities

```
User (Aggregate Root)
  - id: UserId
  - email: Email
  - passwordHash: PasswordHash
  - role: Role (FREE_USER, PRO_USER, ENTERPRISE_USER, ADMIN)
  - oauthProviders: List<OAuthProvider>
  - emailVerifiedAt: Instant
  - emailVerifyToken: String (hashed)
  - passwordResetToken: String (hashed)
  - passwordResetSentAt: Instant
  - failedLoginAttempts: int
  - lockedUntil: Instant
  - lastLoginAt: Instant
  - lastLoginIp: String
  - deletedAt: Instant (soft delete)

UserProfile (Entity, 1:1 with User)
  - id: ProfileId
  - userId: UserId
  - fullName: FullName
  - headline: Headline
  - phoneEncrypted: byte[] (pgcrypto AES-256)
  - location: Location (JSON)
  - workAuthorization: WorkAuthorization (JSON)
  - socialLinks: SocialLinks (JSON)
  - skills: List<Skill> (JSON)
  - experiences: List<Experience> (JSON)
  - education: List<Education> (JSON)
  - avatarUrl: String
  - preferences: UserPreferences (JSON)

UserSettings (Entity, 1:1 with User)
  - userId: UserId
  - theme: Theme
  - language: String
  - timezone: String
  - aiPreferences: AiPreferences (JSON)
  - notificationPrefs: NotificationPreferences (JSON)
  - privacySettings: PrivacySettings (JSON)
  - displaySettings: DisplaySettings (JSON)
```

### 2.2 Value Objects

```
Email(email: String)             — validated format, normalized lowercase
FullName(first, last)            — validated length
PhoneNumber(number)              — E.164 format
Headline(text: String)           — max 200 chars
Location(city, state, country, remoteType)
WorkAuthorization(citizenship, visaType, requiresSponsorship)
SocialLinks(linkedin, github, portfolio, twitter, website)
Skill(name, proficiency, yearsOfExperience)
Experience(title, company, location, startDate, endDate, current, description, technologies)
Education(degree, institution, fieldOfStudy, startDate, endDate, gpa)
SkillProficiency(BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
RemoteType(ONSITE, REMOTE, HYBRID)
Theme(LIGHT, DARK, SYSTEM)
```

---

## 3. API Endpoints

| Method | Path | Handler | Permission |
|--------|------|---------|------------|
| GET | `/api/v1/users/me` | Get current user | Authenticated |
| PUT | `/api/v1/users/me` | Update current user | Authenticated |
| DELETE | `/api/v1/users/me` | Delete account (GDPR) | Authenticated |
| GET | `/api/v1/users/me/profile` | Get full profile | Authenticated |
| PUT | `/api/v1/users/me/profile/basic` | Update basic info | Authenticated |
| POST | `/api/v1/users/me/profile/skills` | Add skill | Authenticated |
| DELETE | `/api/v1/users/me/profile/skills/{skill}` | Remove skill | Authenticated |
| POST | `/api/v1/users/me/profile/experience` | Add experience | Authenticated |
| PUT | `/api/v1/users/me/profile/experience/{id}` | Update experience | Authenticated |
| DELETE | `/api/v1/users/me/profile/experience/{id}` | Remove experience | Authenticated |
| POST | `/api/v1/users/me/profile/education` | Add education | Authenticated |
| DELETE | `/api/v1/users/me/profile/education/{id}` | Remove education | Authenticated |
| POST | `/api/v1/users/me/profile/photo` | Upload avatar | Authenticated |
| GET | `/api/v1/users/me/settings` | Get settings | Authenticated |
| PUT | `/api/v1/users/me/settings` | Update settings | Authenticated |
| PUT | `/api/v1/users/me/settings/privacy` | Update privacy | Authenticated |
| POST | `/api/v1/users/me/export` | GDPR data export | Authenticated |

---

## 4. Application Service Structure

```
UserApplicationService
  + getCurrentUser(userId): UserResponse
  + updateUser(userId, command): UserResponse
  + deleteAccount(userId, command): void

ProfileApplicationService
  + getProfile(userId): ProfileResponse
  + updateBasicInfo(userId, command): ProfileResponse
  + addSkill(userId, command): ProfileResponse
  + removeSkill(userId, skillName): void
  + addExperience(userId, command): ProfileResponse
  + updateExperience(userId, experienceId, command): ProfileResponse
  + removeExperience(userId, experienceId): void
  + addEducation(userId, command): ProfileResponse
  + removeEducation(userId, educationId): void
  + uploadAvatar(userId, file): ProfileResponse

SettingsApplicationService
  + getSettings(userId): SettingsResponse
  + updateSettings(userId, command): SettingsResponse
  + updatePrivacy(userId, command): SettingsResponse

DataExportService
  + exportUserData(userId): ExportResponse (async — returns download URL)
```

---

## 5. GDPR Compliance

| Requirement | Implementation |
|-------------|----------------|
| Right to access | `POST /users/me/export` — generates JSON file of all user data, emailed within 72h |
| Right to rectification | All profile update endpoints |
| Right to erasure | `DELETE /users/me` — soft delete, cascade to related data, 30-day hard delete |
| Right to data portability | Export includes all user-submitted data in machine-readable JSON |
| Consent records | `consent_log` table tracking opt-in/opt-out for marketing, AI training |
| Data retention | Soft-deleted data hard-deleted after 30 days. Analytics anonymized. |

---

## 6. Data Export Format (GDPR)

```json
{
  "exported_at": "2026-07-02T12:00:00Z",
  "user": {
    "id": "usr_xxx",
    "email": "user@example.com",
    "role": "PRO_USER",
    "created_at": "2026-01-15T10:30:00Z"
  },
  "profile": { ... },
  "resumes": [ ... ],
  "cover_letters": [ ... ],
  "applications": [
    { "id": "app_xxx", "job_title": "...", "company": "...", "status": "...", "notes": [...], "timeline": [...] }
  ],
  "interview_sessions": [ ... ],
  "saved_searches": [ ... ],
  "saved_jobs": [ ... ],
  "subscription": { ... },
  "settings": { ... }
}
```

---

**End of User & Profile Module v1.0**
