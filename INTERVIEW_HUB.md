# JobPilot AI — Interview Hub

**Version:** 1.0  
**Status:** Draft  
**Phase:** 18 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

AI-powered interview preparation — question prediction, mock interview sessions (text + voice), answer scoring, progress tracking, and curated question bank.

---

## 2. Domain Model

```
InterviewSession (Aggregate Root):
  - id: UUID
  - userId: UUID
  - targetRole: String
  - targetCompany: String (optional)
  - mode: TEXT | VOICE
  - status: IN_PROGRESS | COMPLETED | ABANDONED
  - questions: List<InterviewQuestion> (JSON)
  - currentQuestionIndex: Integer
  - overallScore: Decimal (1-10, null until completed)
  - feedback: SessionFeedback (JSON)
  - durationSeconds: Integer
  - startedAt, completedAt, createdAt

InterviewQuestion (VO):
  { id, type, category, question, difficulty(1-5), expectedDuration, userResponse, score, feedback, orderIndex }

UserResponse (VO):
  { text, audioUrl, durationSeconds, submittedAt }

QuestionScore (VO):
  { overall, relevance, clarity, structure, depth, confidence } // each 1-10

SessionFeedback (VO):
  { strengths[], improvements[], overallAssessment, suggestedResources[] }

InterviewQuestionBank (Entity — curated):
  - id, type, category, question, difficulty, expectedAnswer, tags[], source, companyId, timesUsed
```

---

## 3. Session Flow

```
1. User creates session: POST /interviews/sessions
   Input: { targetRole, targetCompany?, mode }
   → AI predicts questions based on role + company research
   → Returns first question

2. For each question:
   POST /interviews/sessions/{id}/questions/{qid}/answer
   Input: { text } or { audioBlob }
   → AI scores the answer
   → Returns score + feedback + next question

3. After all questions:
   POST /interviews/sessions/{id}/complete
   → AI generates overall session feedback
   → Calculates aggregate scores
   → Saves to session
   → Returns summary
```

---

## 4. Question Types

| Type | Weight | Description |
|------|--------|-------------|
| BEHAVIORAL | 30% | STAR method questions ("Tell me about a time...") |
| TECHNICAL | 30% | Role-specific technical questions |
| SYSTEM_DESIGN | 15% | Architecture/design problems |
| CODING | 10% | Algorithm/coding problems (text) |
| SITUATIONAL | 10% | "What would you do if..." |
| DOMAIN | 5% | Industry-specific knowledge |

---

## 5. Answer Scoring Rubric

| Criterion | Weight | Scoring |
|-----------|--------|---------|
| Relevance | 25% | Does answer address the question directly? |
| Structure | 25% | STAR method usage, clear narrative flow |
| Depth | 20% | Specific details, metrics, concrete examples |
| Clarity | 15% | Concise, well-articulated, easy to follow |
| Confidence | 15% | Assertive language, ownership of achievements |

---

## 6. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/interviews/sessions` | Create new session |
| GET | `/api/v1/interviews/sessions` | List user's sessions |
| GET | `/api/v1/interviews/sessions/{id}` | Session detail |
| GET | `/api/v1/interviews/sessions/{id}/next-question` | Get next question |
| POST | `/api/v1/interviews/sessions/{id}/questions/{qid}/answer` | Submit answer |
| POST | `/api/v1/interviews/sessions/{id}/complete` | Complete + get feedback |
| DELETE | `/api/v1/interviews/sessions/{id}` | Delete session |
| GET | `/api/v1/interviews/questions` | Question bank (filterable) |
| GET | `/api/v1/interviews/questions/company/{companyId}` | Company-specific questions |

---

**End of Interview Hub v1.0**
