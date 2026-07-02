# JobPilot AI — Cover Letter Engine

**Version:** 1.0  
**Status:** Draft  
**Phase:** 13 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

AI-powered cover letter generation with customizable tone, templates, and PDF export. Generates tailored cover letters by combining user profile, resume, and job description context.

---

## 2. Domain Model

```
CoverLetter (Aggregate Root):
  - id: UUID
  - userId: UUID
  - resumeId: UUID (optional — context)
  - jobListingId: UUID (optional — target)
  - title: String                          // "Cover Letter — Acme Corp Sr Java"
  - recipientName: String (optional)
  - recipientTitle: String (optional)
  - companyName: String
  - body: StructuredCoverLetter
  - tone: CoverLetterTone
  - wordCount: Integer
  - version: Integer
  - fileUrl: String (exported PDF)
  - deletedAt, createdAt, updatedAt

StructuredCoverLetter (JSON):
  - salutation: String                     // "Dear Hiring Manager,"
  - openingParagraph: String               // Hook — why this role/company
  - bodyParagraphs: List<String>           // 2-3 paragraphs
  - closingParagraph: String               // Call to action
  - signature: String                      // "Best regards,\nJohn Doe"

CoverLetterTone:
  PROFESSIONAL, ENTHUSIASTIC, CONFIDENT, FORMAL, WARM
```

---

## 3. Generation Flow

```
1. User requests: POST /cover-letters/generate
   Input: jobListingId, resumeId, tone, recipientName?, recipientTitle?

2. Load context:
   - UserProfile (skills, experience, education)
   - Resume (tailored version if available)
   - JobListing (title, company, description, requirements)
   - CompanyProfile (industry, culture, mission)

3. Build prompt context via CoverLetterContextBuilder

4. Resolve prompt via PromptEngine (useCase: COVER_LETTER_GENERATION)

5. Call AiOrchestrationService.generateText(request)

6. Parse AI response into StructuredCoverLetter JSON

7. Save CoverLetter entity

8. Return CoverLetterResponse

9. Optional: POST /cover-letters/{id}/export → generate PDF
```

---

## 4. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/cover-letters/generate` | Generate new cover letter |
| GET | `/api/v1/cover-letters` | List user's cover letters |
| GET | `/api/v1/cover-letters/{id}` | Get cover letter detail |
| PUT | `/api/v1/cover-letters/{id}` | Update (edit generated text) |
| DELETE | `/api/v1/cover-letters/{id}` | Soft delete |
| POST | `/api/v1/cover-letters/{id}/regenerate/{paragraphIndex}` | Regenerate specific paragraph |
| POST | `/api/v1/cover-letters/{id}/export` | Export to PDF |
| PUT | `/api/v1/cover-letters/{id}/tone` | Change tone (regenerate) |

---

**End of Cover Letter Engine v1.0**
