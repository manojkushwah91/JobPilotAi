# JobPilot AI — ATS Resume Optimizer

**Version:** 1.0  
**Status:** Draft  
**Phase:** 12 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Analyze resumes against job descriptions and provide actionable scores and suggestions for ATS compatibility optimization.

---

## 2. Scoring Architecture

```
┌────────────────────────────────────────────┐
│           AtsOptimizerService              │
│                                            │
│  + analyze(resumeId, jobDescription):      │
│       AtsAnalysis                          │
│                                            │
│  Scoring Pipeline:                         │
│  1. Parse resume content → plain text      │
│  2. Parse job description → extract:       │
│     - Required skills                      │
│     - Preferred skills                     │
│     - Qualifications                       │
│     - Keywords (frequency analysis)        │
│  3. Keyword overlap calculation            │
│  4. Structure analysis (headers, order)    │
│  5. Format analysis (file type, layout)    │
│  6. AI-enhanced scoring (PRO+ only)        │
│  7. Generate suggestions                   │
│  8. Return AtsAnalysis                     │
└────────────────────────────────────────────┘
```

---

## 3. AtsAnalysis Domain Model

```
AtsAnalysis:
  - resumeId: UUID
  - jobDescriptionHash: String (SHA-256, for dedup)
  - overallScore: Integer (0-100)

  - sectionScores: Map<SectionType, Integer>
    { SUMMARY: 85, EXPERIENCE: 72, SKILLS: 60, EDUCATION: 90 }

  - keywordMatches: Map<String, Integer>    // keyword → frequency in resume
    { "Java": 8, "Spring": 4, "AWS": 0, "Kubernetes": 0 }

  - missingKeywords: List<String>
    ["AWS", "Kubernetes", "Docker", "Microservices"]

  - weakKeywords: List<String>
    // Present but only mentioned once or buried in description not skills section

  - keywordDensity: Double                  // 0.0 - 1.0 (target: 2-5% of total words)

  - formatScore: Integer (0-100)
  - formattingIssues: List<FormattingIssue>

  - suggestions: List<AtsSuggestion>

  - analyzedAt: Instant
```

---

## 4. Scoring Weight Matrix

| Factor | Weight | Heuristic Scoring Logic |
|--------|--------|------------------------|
| Keyword Match Rate | 30% | matchedRequired / totalRequired × 100 |
| Keyword Density | 10% | Natural frequency curve — penalize <1% or >8% |
| Section Headers | 10% | Standard headers (Experience, Education, Skills) = 100%, missing standard = penalty |
| Reverse Chronological | 10% | Most recent experience first = 100% |
| Bullet Points | 10% | Uses bullet lists in experience sections = 100% |
| Quantified Achievements | 15% | Contains numbers (%, $, numbers) in bullet points = higher |
| File Format | 5% | .docx = 100%, .pdf = 80%, .png = 20% |
| Contact Info | 5% | Email + phone + LinkedIn present = 100% |
| Length | 5% | 1 page = 100%, 2 pages = 60%, 3+ = 30% |

Final score = weighted sum × 100. AI override adds 0-15 bonus for contextual optimization suggestions.

---

## 5. AtsSuggestion Structure

```
AtsSuggestion:
  - category: SuggestionCategory (KEYWORD | FORMAT | CONTENT | STRUCTURE)
  - severity: SuggestionSeverity (CRITICAL | MAJOR | MINOR)
  - message: String                          // "Add 'Kubernetes' to your Skills section"
  - section: SectionType                     // SKILLS
  - actionableItems: List<String>            // ["Add line: 'Kubernetes — container orchestration, 3 years'"]
```

---

## 6. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/resumes/{id}/score` | Score resume against job description (PRO+) |
| GET | `/api/v1/resumes/{id}/scores` | Get score history for resume |
| GET | `/api/v1/resumes/{id}/scores/latest` | Get latest score |

---

**End of ATS Resume Optimizer v1.0**
