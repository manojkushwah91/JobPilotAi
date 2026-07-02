# JobPilot AI — Resume Studio

**Version:** 1.0  
**Status:** Draft  
**Phase:** 9 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Full lifecycle management of user resumes — upload, build, parse, tailor with AI, score against ATS, version, and export to multiple formats.

---

## 2. Domain Model

### 2.1 Aggregate Root

```
Resume (Aggregate Root)
  - id: ResumeId (UUID)
  - userId: UserId
  - title: ResumeTitle (String, max 200)
  - templateId: TemplateId (String — "modern", "classic", "minimal")
  - sections: List<ResumeSection>  (ordered by section.order)
  - versions: List<ResumeVersion>  (immutable log, newest first)
  - currentVersion: Integer (version number)
  - atsScores: List<AtsScore>
  - fileUrl: String (original uploaded file)
  - fileType: String (pdf, docx, txt)
  - wordCount: Integer
  - deletedAt: Instant

  Domain Methods:
    + createFromProfile(UserProfile, title, templateId): Resume (factory)
    + addSection(ResumeSection): void
    + updateSection(sectionId, ResumeSection): void
    + removeSection(sectionId): void
    + reorderSections(List<SectionId>): void
    + changeTemplate(templateId): void
    + createVersion(label): ResumeVersion
    + restoreVersion(versionNumber): void
    + recordAtsScore(AtsScore): void
    + delete(): void

  Invariants:
    - At least one section (auto-created: Summary + Skills on creation)
    - Section order values are unique within the resume
    - Versions list is never empty (initial version on creation)
    - Only one version is active at a time
```

### 2.2 Supporting Entities & VOs

```
ResumeSection (Entity within aggregate)
  - id: SectionId (UUID)
  - type: SectionType (SUMMARY, EXPERIENCE, EDUCATION, SKILLS, CERTIFICATIONS, PROJECTS, LANGUAGES, VOLUNTEER, CUSTOM)
  - title: String
  - content: StructuredContent (polymorphic JSON)
  - order: Integer

StructuredContent (sealed interface — per section type):
  - SummaryContent: { summary: String }
  - ExperienceContent: { items: List<ExperienceItem> }
  - EducationContent: { items: List<EducationItem> }
  - SkillsContent: { skills: List<SkillItem> }
  - CertificationsContent: { items: List<CertificationItem> }
  - ProjectsContent: { items: List<ProjectItem> }
  - CustomContent: { entries: List<{title: String, body: String}> }

ExperienceItem: company, role, location, startDate, endDate, current, achievements: List<String>, technologies: List<String>
EducationItem: institution, degree, field, startDate, endDate, gpa
SkillItem: name, proficiency (BEGINNER|INTERMEDIATE|ADVANCED|EXPERT), years
CertificationItem: name, issuer, date, url
ProjectItem: name, description, technologies, url

ResumeVersion (Value Object):
  - versionNumber: Integer
  - label: String
  - contentSnapshot: JSON (frozen copy of all sections)
  - createdAt: Instant
  - isActive: Boolean

AtsScore (Value Object):
  - overallScore: Integer (0-100)
  - sectionScores: JSON (per section)
  - keywordMatches: JSON
  - missingKeywords: JSON
  - suggestions: JSON
  - jobDescriptionHash: String (SHA-256)
  - analyzedAt: Instant

SectionType: SUMMARY, EXPERIENCE, EDUCATION, SKILLS, CERTIFICATIONS, PROJECTS, LANGUAGES, VOLUNTEER, CUSTOM
TemplateId: "modern", "classic", "minimal", "technical", "executive"
```

---

## 3. API Endpoints

| Method | Path | Handler | Permission |
|--------|------|---------|------------|
| POST | `/api/v1/resumes` | Create resume (from profile or blank) | Authenticated |
| GET | `/api/v1/resumes` | List user's resumes | Authenticated |
| GET | `/api/v1/resumes/{id}` | Get resume detail | Owner |
| PUT | `/api/v1/resumes/{id}` | Update resume metadata | Owner |
| DELETE | `/api/v1/resumes/{id}` | Soft delete resume | Owner |
| POST | `/api/v1/resumes/{id}/sections` | Add section | Owner |
| PUT | `/api/v1/resumes/{id}/sections/{sectionId}` | Update section | Owner |
| DELETE | `/api/v1/resumes/{id}/sections/{sectionId}` | Remove section | Owner |
| PUT | `/api/v1/resumes/{id}/sections/reorder` | Reorder sections | Owner |
| PUT | `/api/v1/resumes/{id}/template` | Change template | Owner |
| POST | `/api/v1/resumes/{id}/versions` | Create version snapshot | Owner |
| POST | `/api/v1/resumes/{id}/restore/{version}` | Restore version | Owner |
| POST | `/api/v1/resumes/{id}/tailor` | AI-tailor for job | PRO+ |
| POST | `/api/v1/resumes/{id}/score` | ATS score against job | PRO+ |
| POST | `/api/v1/resumes/{id}/export` | Export (pdf, docx, txt) | Authenticated |
| POST | `/api/v1/resumes/upload` | Upload and parse resume | Authenticated |
| POST | `/api/v1/resumes/parse` | Parse resume text (no save) | Authenticated |
| GET | `/api/v1/resumes/templates` | List available templates | Authenticated |

---

## 4. Application Services

```
ResumeApplicationService
  + create(userId, command): ResumeResponse
  + findById(userId, resumeId): ResumeResponse
  + findByUserId(userId): List<ResumeResponse>
  + update(userId, resumeId, command): ResumeResponse
  + delete(userId, resumeId): void
  + addSection(userId, resumeId, command): ResumeResponse
  + updateSection(userId, resumeId, sectionId, command): ResumeResponse
  + removeSection(userId, resumeId, sectionId): ResumeResponse
  + reorderSections(userId, resumeId, command): ResumeResponse
  + changeTemplate(userId, resumeId, command): ResumeResponse
  + createVersion(userId, resumeId, command): ResumeResponse
  + restoreVersion(userId, resumeId, versionNumber): ResumeResponse
  + tailorToJob(userId, resumeId, command): ResumeResponse
  + scoreAgainstJob(userId, resumeId, command): AtsScoreResponse
  + export(userId, resumeId, command): FileDownloadResponse
  + uploadAndParse(userId, file): ResumeResponse
  + parseOnly(userId, file): ParsedResumeResponse
```

## 5. External Ports Used

| Port | Purpose | Implementation |
|------|---------|----------------|
| `ResumeRepository` | Persistence | ResumeJpaRepository + ResumeEntityMapper |
| `AiResumePort` | AI tailoring + scoring | AiOrchestrationService |
| `ExportPort` | PDF/DOCX/TXT generation | PdfExportService, DocxExportService |
| `FileStoragePort` | Uploaded file storage | S3FileStorageService |
| `ResumeParserPort` | Parse uploaded resume | ResumeParserService (Apache Tika / custom) |

---

**End of Resume Studio v1.0**
