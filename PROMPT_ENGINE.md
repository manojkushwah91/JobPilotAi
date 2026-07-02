# JobPilot AI — Prompt Engine

**Version:** 1.0  
**Status:** Draft  
**Phase:** 11 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Centralized prompt management — templates, variables, versioning, context injection, and history. Ensures all AI interactions use consistent, version-controlled prompts that can be updated without code changes.

---

## 2. Architecture

```
┌─────────────────────────────────────────────┐
│         Application Services                 │
│  ResumeService  InterviewService  etc.      │
│          │              │                   │
│          ▼              ▼                   │
│  ┌─────────────────────────────────────┐   │
│  │       PromptEngineService           │   │
│  │  + resolve(useCase, context):       │   │
│  │       ResolvedPrompt                │   │
│  │  + getTemplate(useCase, version)    │   │
│  │  + createTemplate(command)          │   │
│  │  + activateVersion(templateId, ver) │   │
│  └─────────────┬───────────────────────┘   │
└────────────────┼───────────────────────────┘
                 │
┌────────────────▼───────────────────────────┐
│              Domain Model                   │
│  PromptTemplate(useCase, name, version,    │
│    systemPrompt, userTemplate, variables,  │
│    model, temperature, maxTokens, active)  │
│                                             │
│  ResolvedPrompt(systemPrompt, userPrompt,  │
│    variables, model, temperature, maxTokens)│
└────────────────┬───────────────────────────┘
                 │
┌────────────────▼───────────────────────────┐
│            Infrastructure                   │
│  PromptTemplateRepository (JPA + cache)    │
│  PromptSeeder (classpath:/prompts/*.md)    │
└────────────────────────────────────────────┘
```

---

## 3. Prompt Template Model

```
PromptTemplate:
  - id: UUID
  - useCase: PromptUseCase (enum)
  - name: String                 // "Resume Tailoring v2"
  - version: Integer             // Incrementing
  - systemPrompt: Text           // System role definition
  - userPromptTemplate: Text     // With {{variable}} placeholders
  - variables: List<PromptVariable>
  - model: String                // "gpt-4", "claude-3-sonnet"
  - temperature: Float           // 0.0 - 2.0
  - maxTokens: Integer
  - isActive: Boolean
  - createdAt, updatedAt

PromptVariable:
  - name: String                 // "user_profile"
  - type: VariableType           // STRING, JSON, LIST, NUMBER
  - required: Boolean
  - description: String
  - defaultValue: String

PromptUseCase:
  RESUME_TAILORING, RESUME_SCORING, COVER_LETTER_GENERATION,
  INTERVIEW_QUESTION_PREDICTION, INTERVIEW_ANSWER_SCORING,
  CAREER_PATH_SUGGESTION, SKILLS_GAP_ANALYSIS, NETWORKING_MESSAGE,
  JOB_MATCH_EXPLANATION, COMPANY_SUMMARY

VariableType: STRING, JSON, LIST, NUMBER
```

---

## 4. Context Builders

Each use case has a dedicated context builder that assembles the variable map:

```
ResumeTailoringContextBuilder:
  Input: UserProfile, ResumeContent, JobListing, CompanyProfile
  Output: {
    "user_profile": { serialized UserProfile },
    "resume_content": { serialized ResumeContent },
    "job_description": "Full JD text...",
    "company_name": "Acme Corp",
    "company_industry": "Tech",
    "target_role": "Senior Java Engineer",
    "target_skills": ["Java", "Spring", "AWS"],
    "key_requirements": ["5+ years Java", "Spring Boot", "Microservices"]
  }

InterviewQuestionContextBuilder:
  Input: UserProfile, targetRole, targetCompany, CompanyProfile
  Output: { "role": "...", "company": "...", "user_skills": [...], "company_tech_stack": [...] }

CoverLetterContextBuilder:
  Input: UserProfile, Resume, JobListing, CompanyProfile, tone
  Output: { "user_profile": ..., "experience": ..., "company": ..., "role": ..., "tone": ... }
```

---

## 5. Variable Resolution

```
Resolution algorithm:
  1. Load active PromptTemplate for useCase
  2. Extract all {{variable}} patterns via regex: \{\{(\w+)\}\}
  3. For each extracted variable:
     a. Look up in provided context map
     b. If found: JSON-serialize (for JSON type) or toString
     c. If not found but required: throw MissingPromptVariableException
     d. If not found and optional: replace with empty string
  4. Apply length limits (truncate if > 8000 chars per variable)
  5. Build AiMessage list: system = systemPrompt, user = resolvedUserPrompt
  6. Build AiRequest with template's model/temperature/maxTokens
  7. Return ResolvedPrompt
```

---

## 6. Default Prompt Templates (Seeded)

Stored in `classpath:/prompts/` as Markdown files with frontmatter:

```markdown
---
use_case: RESUME_TAILORING
name: Resume Tailoring v2
version: 1
model: gpt-4
temperature: 0.7
max_tokens: 2048
variables:
  - name: user_profile
    type: JSON
    required: true
  - name: resume_content
    type: JSON
    required: true
  - name: job_description
    type: STRING
    required: true
  - name: company_name
    type: STRING
    required: false
---

# System Prompt
You are an expert resume writer and ATS optimization specialist...

# User Prompt
Please tailor the following resume for this job:

Job Description: {{job_description}}

Company: {{company_name}}

Current Resume: {{resume_content}}

User Profile: {{user_profile}}

Please:
1. Rewrite bullet points to match JD keywords
2. Reorder skills to prioritize JD-matching ones
3. Add missing relevant keywords naturally
4. Keep all achievements quantified
5. Maintain reverse-chronological order
```

---

## 7. Prompt Versioning

```
- Each change to a prompt creates a new version
- Only one version per useCase is active at a time
- Version history preserved for rollback
- Prompts are cached in Redis (key: prompt:{useCase}:{version})
- Cache invalidated when admin activates a new version
- Metrics tracked per version for A/B testing effectiveness
```

---

**End of Prompt Engine v1.0**
